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

            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT id, bay_name FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND invoice_no = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                    tenantId, instanceId, invoiceNo, orderId, orderId, skuCode);
            if (rows.isEmpty()) {
                markRollbackOnly();
                return ActionResult.error("no open unload row found for invoice_no=" + invoiceNo + " order_id=" + orderId + " sku_code=" + skuCode);
            }
            String tankLocation = (String) rows.get(0).get("bay_name");
            if (tankLocation == null || tankLocation.isBlank()) {
                markRollbackOnly();
                return ActionResult.error("unload_receipt_items[" + i + "] (" + skuCode + ") has no tank location recorded — pick one on QC Approval before receipting");
            }

            if (weight.signum() > 0) {
                String uom = lookupUom(tenantId, skuCode);
                int updated = jdbc.update(
                        "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, last_movement_at = CURRENT_TIMESTAMP "
                                + "WHERE tenant_id = ? AND item_code = ? AND lot_no IS NULL AND bin_code = ?",
                        weight, tenantId, skuCode, tankLocation);
                if (updated == 0) {
                    jdbc.update(
                            "INSERT INTO m5_stock_balance (tenant_id, item_code, lot_no, bin_code, uom, qty_on_hand, last_movement_at) "
                                    + "VALUES (?,?,NULL,?,?,?,CURRENT_TIMESTAMP)",
                            tenantId, skuCode, tankLocation, uom, weight);
                }
                jdbc.update(
                        "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, to_bin, qty, uom, "
                                + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?)",
                        tenantId, "RECEIPT", skuCode, tankLocation, weight, uom, "TANKER_UNLOAD", instanceId, invoiceNo);
                log.info("action.tanker-unload-receipt.stock-receipt tenantId={} instanceId={} invoiceNo={} skuCode={} tankLocation={} qty={}",
                        tenantId, instanceId, invoiceNo, skuCode, tankLocation, weight);
            }

            int n = jdbc.update(
                    "UPDATE unloading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' WHERE id = ?",
                    weight, rows.get(0).get("id"));
            if (n == 0) {
                markRollbackOnly();
                return ActionResult.error("unloading_progress_detail_bay row changed concurrently for invoice_no=" + invoiceNo + " sku_code=" + skuCode + " — retry");
            }

            totalReceived = totalReceived.add(weight);
            Map<String, Object> row = new HashMap<>();
            row.put("invoiceNo", invoiceNo);
            row.put("skuCode", skuCode);
            row.put("tankLocation", tankLocation);
            row.put("weight", weight);
            closed.add(row);
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
