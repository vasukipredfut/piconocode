package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code m10.tanker-unload-receipt} — closes {@code unloading_progress_detail_bay}
 * line(s) (opened by {@link com.aria.workflow.projection.TankerUnloadInvoiceTransitionListener}
 * when aria:qc-approval was submitted for M10.TankerInbound) with the actual
 * weight the operator enters on the weighment-capture step that follows QC
 * approval, and posts that weight to stock.
 *
 * <p>Shares {@code unloading_progress_detail}/{@code _bay} with the
 * FG/PackLoading Reject Flow Unload process ({@code M10StartUnloadBayActionHandler}
 * / {@code M10UnloadBayCompleteActionHandler}) — matched here by {@code
 * invoice_no}+{@code sku_code} rather than that process's {@code
 * route_id}+{@code sku_code}, and the two can never collide on the same
 * {@code workflow_instance_id} (disjoint workflow codes).
 *
 * <p>Deliberately NOT wired to any page-submit transition — the weighment
 * operator enters a weight per product explicitly via this action; unlike
 * loading's {@code m10.bay-complete} there is no automatic listener path
 * here at all.
 *
 * <p>Independent of {@code StockMovementListener}'s existing
 * {@code M10.TankerInbound/QTY_RECONCILED -> RECEIPT} mapping (a later,
 * separate lump receipt of net W1-W2 weight into a synthetic BULK-TANK bin,
 * keyed off {@code gate_pass.oil_rm_item}) — this handler posts one
 * {@code RECEIPT} movement per invoice/SKU line into whatever
 * tank location the QC officer picked for that line, not touched or
 * replaced.
 *
 * <p>Tank location is read from the {@code unloading_progress_detail_bay}
 * row's {@code bay_name} column (set at QC Approval time — no separate
 * tank_location column, same precedent as {@code M10StartBayActionHandler}'s
 * M10.TankerOutbound handling), not from this action's payload — one source
 * of truth, can't drift from what QC actually picked.
 *
 * <p><b>Multiple tank locations per product:</b> when QC Approval split a SKU across several
 * tanks it has one open row per tank. A line WITHOUT {@code tank_location} (SKU-wise) carries
 * the SKU's total weight, divided across those tanks in proportion to each row's planned
 * quantity; a line WITH {@code tank_location} (or {@code bay_name}) (location-wise) receipts
 * just that tank and leaves the others open for a later weighing. Each tank's share is posted
 * to that tank's own bin.
 *
 * <p>Payload shape:
 * <pre>
 *   { "instanceId": 1495, "formData": { "unload_receipt_items": [
 *       { "invoice_no": "INV-1001", "sku_code": "SKU1215", "weight": 500 }
 *   ] } }
 * </pre>
 */
@Component
public class M10TankerUnloadReceiptActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(M10TankerUnloadReceiptActionHandler.class);

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.tanker-unload-receipt"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        Object linesObj = formData.get("unload_receipt_items");
        if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) {
            return ActionResult.error("formData.unload_receipt_items must be a non-empty array");
        }
        List<Map<String, Object>> lines = (List<Map<String, Object>>) linesObj;

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();

        log.info("action.tanker-unload-receipt.start tenantId={} instanceId={} lineCount={}", tenantId, instanceId, lines.size());

        List<Map<String, Object>> closed = new ArrayList<>();
        BigDecimal totalReceived = BigDecimal.ZERO;

        for (int i = 0; i < lines.size(); i++) {
            Object lineObj = lines.get(i);
            if (!(lineObj instanceof Map)) {
                markRollbackOnly();
                return ActionResult.error("unload_receipt_items[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lineObj;
            String invoiceNo = str(line, "invoice_no");
            String orderId = str(line, "order_id");
            String skuCode = str(line, "sku_code");
            if (invoiceNo == null || skuCode == null) {
                markRollbackOnly();
                return ActionResult.error("unload_receipt_items[" + i + "] is missing invoice_no or sku_code");
            }

            BigDecimal weight;
            try {
                Object wObj = line.get("weight") != null ? line.get("weight") : line.get("processed_quantity");
                weight = wObj == null ? null : new BigDecimal(wObj.toString());
            } catch (NumberFormatException e) {
                markRollbackOnly();
                return ActionResult.error("unload_receipt_items[" + i + "] (" + skuCode + ") weight must be a number");
            }
            if (weight == null || weight.signum() < 0) {
                markRollbackOnly();
                return ActionResult.error("unload_receipt_items[" + i + "] (" + skuCode + ") needs weight >= 0");
            }

            // order_id is only a narrowing filter: QC Approval stores whatever order_id the invoice line
            // carried, so a receipt that omits it must still find the row (invoice_no + sku_code identify it).
            List<Map<String, Object>> rows = orderId == null
                    ? jdbc.queryForList(
                            "SELECT id, bay_name, quantity FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                                    + "AND invoice_no = ? AND sku_code = ? AND status = 'IN_PROGRESS' ORDER BY id",
                            tenantId, instanceId, invoiceNo, skuCode)
                    : jdbc.queryForList(
                            "SELECT id, bay_name, quantity FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                                    + "AND invoice_no = ? AND order_id = ? AND sku_code = ? AND status = 'IN_PROGRESS' ORDER BY id",
                            tenantId, instanceId, invoiceNo, orderId, skuCode);
            if (rows.isEmpty()) {
                markRollbackOnly();
                return ActionResult.error("no open unload row found for invoice_no=" + invoiceNo + " order_id=" + orderId + " sku_code=" + skuCode);
            }

            // Location-wise: a line naming a tank receipts just that tank's row, the SKU's other
            // tanks stay open. SKU-wise (no tank): the weight is divided across the SKU's open
            // tank rows in proportion to each row's planned (QC-approved) quantity.
            String onlyTank = str(line, "tank_location") != null ? str(line, "tank_location") : str(line, "bay_name");
            // tank_locations: ONE total weight for just these tanks of the SKU (e.g. 2 of its 3), split by
            // planned qty across them; the SKU's other tanks stay open. Ignored when a single tank is named.
            List<String> tankSubset = onlyTank != null ? List.of() : tankList(line.get("tank_locations"));
            if (!tankSubset.isEmpty()) {
                final List<Map<String, Object>> open = rows;
                for (String t : tankSubset) {
                    if (open.stream().noneMatch(r -> t.equals(r.get("bay_name")))) {
                        markRollbackOnly();
                        return ActionResult.error("unload_receipt_items[" + i + "] (" + skuCode + ") has no open unload row for tank " + t
                                + " (already receipted, or not allocated to that tank)");
                    }
                }
                rows = rows.stream().filter(r -> tankSubset.contains((String) r.get("bay_name"))).toList();
            }
            if (onlyTank != null) {
                final String wanted = onlyTank.trim();
                rows = rows.stream().filter(r -> wanted.equals(r.get("bay_name"))).toList();
                if (rows.isEmpty()) {
                    markRollbackOnly();
                    return ActionResult.error("unload_receipt_items[" + i + "] (" + skuCode + ") has no open unload row for tank " + wanted
                            + " (already receipted, or not allocated to that tank)");
                }
            }

            String uom = null;
            for (TankShare share : splitByAllocation(rows, weight)) {
                String tankLocation = share.tank();
                if (tankLocation == null || tankLocation.isBlank()) {
                    markRollbackOnly();
                    return ActionResult.error("unload_receipt_items[" + i + "] (" + skuCode + ") has no tank location recorded — pick one on QC Approval before receipting");
                }

                if (share.qty().signum() > 0) {
                    if (uom == null) uom = lookupUom(tenantId, skuCode);
                    int updated = jdbc.update(
                            "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, last_movement_at = CURRENT_TIMESTAMP "
                                    + "WHERE tenant_id = ? AND item_code = ? AND lot_no IS NULL AND bin_code = ?",
                            share.qty(), tenantId, skuCode, tankLocation);
                    if (updated == 0) {
                        jdbc.update(
                                "INSERT INTO m5_stock_balance (tenant_id, item_code, lot_no, bin_code, uom, qty_on_hand, last_movement_at) "
                                        + "VALUES (?,?,NULL,?,?,?,CURRENT_TIMESTAMP)",
                                tenantId, skuCode, tankLocation, uom, share.qty());
                    }
                    jdbc.update(
                            "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, to_bin, qty, uom, "
                                    + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?)",
                            tenantId, "RECEIPT", skuCode, tankLocation, share.qty(), uom, "TANKER_UNLOAD", instanceId, invoiceNo);
                    log.info("action.tanker-unload-receipt.stock-receipt tenantId={} instanceId={} invoiceNo={} skuCode={} tankLocation={} qty={}",
                            tenantId, instanceId, invoiceNo, skuCode, tankLocation, share.qty());
                }

                int n = jdbc.update(
                        "UPDATE unloading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' WHERE id = ?",
                        share.qty(), share.rowId());
                if (n == 0) {
                    markRollbackOnly();
                    return ActionResult.error("unloading_progress_detail_bay row changed concurrently for invoice_no=" + invoiceNo + " sku_code=" + skuCode + " — retry");
                }

                Map<String, Object> row = new HashMap<>();
                row.put("invoiceNo", invoiceNo);
                row.put("skuCode", skuCode);
                row.put("tankLocation", tankLocation);
                row.put("weight", share.qty());
                closed.add(row);
            }
            totalReceived = totalReceived.add(weight);
        }

        jdbc.update(
                "UPDATE unloading_progress_detail SET processed_quantity = processed_quantity + ? WHERE tenant_id = ? AND workflow_instance_id = ?",
                totalReceived, tenantId, instanceId);

        // Scoped to invoice_no IS NOT NULL: a reject-unload row for the same
        // instance would never coexist (see class javadoc), but the guard
        // documents that this check is about THIS process's rows only.
        Integer stillOpen = jdbc.queryForObject(
                "SELECT COUNT(*) FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND invoice_no IS NOT NULL AND status != 'DONE'",
                Integer.class, tenantId, instanceId);
        if (stillOpen != null && stillOpen == 0) {
            jdbc.update(
                    "UPDATE unloading_progress_detail SET status = 'DONE' WHERE tenant_id = ? AND workflow_instance_id = ?",
                    tenantId, instanceId);
        }

        log.info("action.tanker-unload-receipt.done tenantId={} instanceId={} linesClosed={} totalReceived={}",
                tenantId, instanceId, closed.size(), totalReceived);

        return ActionResult.builder()
                .ok(true)
                .message(closed.size() + " unload receipt line(s) closed, received " + totalReceived + " total into stock")
                .data(Map.of("closed", closed, "totalReceived", totalReceived))
                .build();
    }

    /** One tank's portion of a receipted line; rowId is the unloading_progress_detail_bay row it closes. */
    private record TankShare(Object rowId, String tank, BigDecimal qty) {}

    /**
     * Divides the weighed qty across the SKU's open tank rows in proportion to each row's
     * planned {@code quantity}; a single row takes everything. If every planned quantity is
     * zero/missing the split is equal. Shares are rounded to 3 dp, the LAST row takes the
     * remainder so they always sum exactly to the weight.
     */
    private static List<TankShare> splitByAllocation(List<Map<String, Object>> rows, BigDecimal weight) {
        List<TankShare> out = new ArrayList<>();
        if (rows.size() == 1) {
            Map<String, Object> only = rows.get(0);
            out.add(new TankShare(only.get("id"), (String) only.get("bay_name"), weight));
            return out;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> r : rows) total = total.add(asDecimal(r.get("quantity")));
        BigDecimal assigned = BigDecimal.ZERO;
        for (int k = 0; k < rows.size(); k++) {
            Map<String, Object> r = rows.get(k);
            BigDecimal share;
            if (k == rows.size() - 1) {
                share = weight.subtract(assigned);
            } else if (total.signum() > 0) {
                share = weight.multiply(asDecimal(r.get("quantity"))).divide(total, 3, RoundingMode.HALF_UP);
            } else {
                share = weight.divide(BigDecimal.valueOf(rows.size()), 3, RoundingMode.HALF_UP);
            }
            assigned = assigned.add(share);
            out.add(new TankShare(r.get("id"), (String) r.get("bay_name"), share));
        }
        return out;
    }

    /** {@code tank_locations}: a JSON array of tank codes, or a comma-separated string. Blank entries dropped. */
    private static List<String> tankList(Object raw) {
        List<String> out = new ArrayList<>();
        if (raw instanceof List<?> l) {
            for (Object o : l) if (o != null && !o.toString().isBlank()) out.add(o.toString().trim());
        } else if (raw != null && !raw.toString().isBlank()) {
            for (String s : raw.toString().split(",")) if (!s.isBlank()) out.add(s.trim());
        }
        return out;
    }

    private static BigDecimal asDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    private String lookupUom(long tenantId, String skuCode) {
        List<String> rows = jdbc.queryForList(
                "SELECT uom_primary FROM m5_sku_master WHERE tenant_id = ? AND sku_code = ?",
                String.class, tenantId, skuCode);
        return rows.isEmpty() || rows.get(0) == null ? "EA" : rows.get(0);
    }

    private static void markRollbackOnly() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    private static String str(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        if (v == null) return null;
        String s = v.toString();
        return s.isBlank() ? null : s;
    }
}
