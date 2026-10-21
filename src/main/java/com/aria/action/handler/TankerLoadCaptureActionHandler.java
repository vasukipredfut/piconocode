package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.mes.stock.FefoStockPickerService;
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
 * {@code m10.tanker-load-capture} -- the reverse of {@link TankerUnloadCaptureActionHandler}, for M10.TankerInbound's
 * reject / "load again" flow: the vehicle that unloaded is loaded again, from the tanks, against the invoice lines the
 * QC approval sent ({@code loadingInvoices}, saved by {@code TankerUnloadInvoiceTransitionListener} as
 * {@code loading_progress_detail_bay} rows, status IN_PROGRESS, {@code invoice_no} set, {@code route_id} NULL).
 *
 * <p>This is {@link TankerWeighmentSkuCaptureActionHandler} for rows that have an invoice instead of a route: per line
 * the weighed quantity is divided across the SKU's open tank rows in proportion to what each was allocated, the oil is
 * taken out of THAT tank (FEFO inside the bin, {@code m5_stock_movement} kind CONSUME, ref_type TANKER_LOAD), and the
 * row closes (IN_PROGRESS -> DONE, {@code processed_quantity}). A tank that does not hold enough fails the whole
 * request, nothing is written. Does not change workflow state. The oil taken back also counts against the invoice's
 * unload rows ({@code unloaded}: {@code loaded_back_quantity}, V376; fully loaded back -> REVERSED).
 *
 * <p>A line names its rows by {@code invoice_no} (or {@code route_id}), {@code sku_code} and optionally
 * {@code order_id}; {@code tank_location} weighs just that tank, {@code tank_locations} (array / comma list) one total
 * for just those tanks -- the SKU's other tanks stay open.
 *
 * <p>Payload ({@code tankerLoadItems}; {@code order_sku_line_items} / {@code tankerSkuWeights} are accepted too; the
 * quantity is {@code load_wgt}, {@code loading_qty} or {@code qty}):
 * <pre>
 *   { "instanceId": 1542,
 *     "formData": { "performed_by_name": "Ravi", "tankerLoadItems": [
 *       { "invoice_no": "INV1", "sku_code": "SKU1", "tank_location": "ROT-1", "load_wgt": 60 }
 *     ] } }
 * </pre>
 */
@Component
public class TankerLoadCaptureActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(TankerLoadCaptureActionHandler.class);

    @Autowired private JdbcTemplate jdbc;
    @Autowired private FefoStockPickerService fefoPicker;

    @Override
    public String code() { return "m10.tanker-load-capture"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        Object linesObj = null;
        for (String key : new String[]{"tankerLoadItems", "order_sku_line_items", "tankerSkuWeights"}) {
            if (formData.get(key) instanceof List<?> l && !l.isEmpty()) { linesObj = l; break; }
        }
        if (linesObj == null) {
            return ActionResult.error("formData.tankerLoadItems must be a non-empty array");
        }
        List<Object> lines = (List<Object>) linesObj;

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();
        log.info("action.tanker-load-capture.start tenantId={} instanceId={} lineCount={}", tenantId, instanceId, lines.size());

        List<Map<String, Object>> closed = new ArrayList<>();
        BigDecimal totalProcessed = BigDecimal.ZERO;

        for (int i = 0; i < lines.size(); i++) {
            if (!(lines.get(i) instanceof Map)) {
                markRollbackOnly();
                return ActionResult.error("tankerLoadItems[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lines.get(i);
            String invoiceNo = str(line, "invoice_no");
            String routeId = str(line, "route_id");
            String orderId = str(line, "order_id");
            String skuCode = str(line, "sku_code");
            if (skuCode == null || (invoiceNo == null && routeId == null)) {
                markRollbackOnly();
                return ActionResult.error("tankerLoadItems[" + i + "] is missing sku_code or invoice_no");
            }
            String onlyTank = str(line, "tank_location") != null ? str(line, "tank_location") : str(line, "bay_name");
            List<String> tankSubset = onlyTank != null ? List.of() : tankList(line.get("tank_locations"));

            BigDecimal loadWgt;
            try {
                Object q = line.get("load_wgt") != null ? line.get("load_wgt")
                        : line.get("loading_qty") != null ? line.get("loading_qty") : line.get("qty");
                loadWgt = q == null ? null : new BigDecimal(q.toString());
            } catch (NumberFormatException e) {
                markRollbackOnly();
                return ActionResult.error("tankerLoadItems[" + i + "] (" + skuCode + ") load_wgt must be a number");
            }
            if (loadWgt == null || loadWgt.signum() <= 0) {
                markRollbackOnly();
                return ActionResult.error("tankerLoadItems[" + i + "] (" + skuCode + ") needs load_wgt > 0");
            }

            // the SKU's open rows for this invoice (or route); order_id narrows it only when the line names one
            List<Object> args = new ArrayList<>(List.of(tenantId, instanceId, skuCode));
            StringBuilder sql = new StringBuilder("SELECT id, bay_name, quantity FROM loading_progress_detail_bay "
                    + "WHERE tenant_id = ? AND workflow_instance_id = ? AND sku_code = ? AND status = 'IN_PROGRESS'");
            if (invoiceNo != null) { sql.append(" AND invoice_no = ?"); args.add(invoiceNo); }
            else { sql.append(" AND route_id = ?"); args.add(routeId); }
            if (orderId != null) { sql.append(" AND order_id = ?"); args.add(orderId); }
            sql.append(" ORDER BY id");
            List<Map<String, Object>> bayRows = jdbc.queryForList(sql.toString(), args.toArray());
            String ref = invoiceNo != null ? invoiceNo : routeId;
            if (bayRows.isEmpty()) {
                markRollbackOnly();
                return ActionResult.error("no open load row found for " + (invoiceNo != null ? "invoice_no=" + invoiceNo : "route_id=" + routeId)
                        + " sku_code=" + skuCode);
            }

            if (!tankSubset.isEmpty()) {
                for (String t : tankSubset) {
                    if (bayRows.stream().noneMatch(r -> t.equals(r.get("bay_name")))) {
                        markRollbackOnly();
                        return ActionResult.error("tankerLoadItems[" + i + "] (" + skuCode + ") has no open tank row for " + t
                                + " (already loaded, or not allocated to that tank)");
                    }
                }
                bayRows = bayRows.stream().filter(r -> tankSubset.contains((String) r.get("bay_name"))).toList();
            }
            if (onlyTank != null) {
                final String wanted = onlyTank.trim();
                bayRows = bayRows.stream().filter(r -> wanted.equals(r.get("bay_name"))).toList();
                if (bayRows.isEmpty()) {
                    markRollbackOnly();
                    return ActionResult.error("tankerLoadItems[" + i + "] (" + skuCode + ") has no open tank row for " + wanted
                            + " (already loaded, or not allocated to that tank)");
                }
            }

            for (TankShare share : splitByAllocation(bayRows, loadWgt)) {
                String tank = share.tank();
                if (tank == null || tank.isBlank()) {
                    markRollbackOnly();
                    return ActionResult.error("tankerLoadItems[" + i + "] (" + skuCode + ") has no tank location recorded");
                }
                List<FefoStockPickerService.FefoPick> picks = fefoPicker.pick(tenantId, skuCode, tank, share.qty());
                BigDecimal residual = fefoPicker.residualQty(share.qty(), picks);
                if (residual.signum() > 0) {
                    markRollbackOnly();
                    return ActionResult.error("tankerLoadItems[" + i + "] (" + skuCode + ") insufficient stock in tank " + tank + ": "
                            + "requested " + share.qty() + ", available " + share.qty().subtract(residual));
                }
                for (FefoStockPickerService.FefoPick pick : picks) {
                    int deducted = jdbc.update(
                            "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand - ?, last_movement_at = CURRENT_TIMESTAMP "
                                    + "WHERE tenant_id = ? AND item_code = ? AND "
                                    + "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code = ? AND qty_on_hand >= ?",
                            pick.qty(), tenantId, skuCode, pick.lotNo(), pick.lotNo(), pick.binCode(), pick.qty());
                    if (deducted == 0) {
                        markRollbackOnly();
                        return ActionResult.error("tankerLoadItems[" + i + "] (" + skuCode + ") stock changed concurrently -- lot "
                                + pick.lotNo() + "/bin " + pick.binCode() + " no longer has enough qty_on_hand, retry");
                    }
                    jdbc.update(
                            "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, qty, uom, "
                                    + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)",
                            tenantId, "CONSUME", skuCode, pick.lotNo(), pick.binCode(), pick.qty(), pick.uom(),
                            "TANKER_LOAD", instanceId, ref);
                }

                int n = jdbc.update(
                        "UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                                + "WHERE tenant_id = ? AND id = ? AND status = 'IN_PROGRESS'",
                        share.qty(), tenantId, share.rowId());
                if (n == 0) {
                    markRollbackOnly();
                    return ActionResult.error("no open load row found for " + ref + " sku_code=" + skuCode);
                }
                RowActorStamp.weighedLoadingRow(jdbc, tenantId, share.rowId(), RowActorStamp.performedBy(line, formData));
                if (invoiceNo != null) countLoadedBack(tenantId, instanceId, invoiceNo, skuCode, tank, share.qty());
                log.info("action.tanker-load-capture.row-closed tenantId={} instanceId={} ref={} skuCode={} tank={} qty={}",
                        tenantId, instanceId, ref, skuCode, tank, share.qty());
            }

            totalProcessed = totalProcessed.add(loadWgt);
            Map<String, Object> row = new HashMap<>();
            row.put("invoiceNo", invoiceNo);
            row.put("routeId", routeId);
            row.put("skuCode", skuCode);
            row.put("loadWgt", loadWgt);
            closed.add(row);
        }

        jdbc.update("UPDATE unloading_progress_detail p SET p.loaded_back_quantity = (SELECT COALESCE(SUM(b.loaded_back_quantity), 0) "
                        + "FROM unloading_progress_detail_bay b WHERE b.tenant_id = p.tenant_id AND b.workflow_instance_id = p.workflow_instance_id) "
                        + "WHERE p.tenant_id = ? AND p.workflow_instance_id = ?",
                tenantId, instanceId);
        jdbc.update("UPDATE loading_progress_detail SET processed_quantity = processed_quantity + ?, "
                        + "loading_finish_time = CURRENT_TIMESTAMP WHERE tenant_id = ? AND workflow_instance_id = ?",
                totalProcessed, tenantId, instanceId);

        return ActionResult.builder()
                .ok(true)
                .message(closed.size() + " SKU line(s) loaded, " + totalProcessed + " total taken from stock")
                .data(Map.of("closed", closed, "totalProcessed", totalProcessed))
                .build();
    }

    /**
     * What was loaded back counts against the unload rows of the SAME tank it was taken from: that tank's unload rows for the invoice + SKU (DONE, with something left) take
     * {@code qty} into {@code loaded_back_quantity}. Oil taken from a tank the invoice was not unloaded into counts against
     * nothing -- the unload table never shows a load-back that did not happen there.
     * The unloaded quantity ({@code processed_quantity}) itself is never changed -- it stays the record of what was unloaded.
     * A row that has had everything loaded back is REVERSED, the mirror of what the reject-unload does to loading rows.
     * No upper limit: a quantity above what was unloaded is simply not counted anywhere.
     */
    private void countLoadedBack(long tenantId, Long instanceId, String invoiceNo, String skuCode, String tank, BigDecimal qty) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, bay_name, processed_quantity, loaded_back_quantity FROM unloading_progress_detail_bay "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND invoice_no = ? AND sku_code = ? AND bay_name = ? "
                        + "AND status = 'DONE' ORDER BY id",
                tenantId, instanceId, invoiceNo, skuCode, tank);
        BigDecimal left = qty;
        for (Map<String, Object> r : rows) {
            if (left.signum() <= 0) break;
            BigDecimal room = asDecimal(r.get("processed_quantity")).subtract(asDecimal(r.get("loaded_back_quantity")));
            if (room.signum() <= 0) continue;
            BigDecimal take = room.compareTo(left) >= 0 ? left : room;
            // status first: MariaDB evaluates the SET list left to right, so it still sees the old loaded_back_quantity
            jdbc.update("UPDATE unloading_progress_detail_bay SET "
                            + "status = CASE WHEN COALESCE(loaded_back_quantity, 0) + ? >= COALESCE(processed_quantity, 0) THEN 'REVERSED' ELSE status END, "
                            + "loaded_back_quantity = COALESCE(loaded_back_quantity, 0) + ? WHERE tenant_id = ? AND id = ?",
                    take, take, tenantId, r.get("id"));
            left = left.subtract(take);
        }
    }

    private record TankShare(Long rowId, String tank, BigDecimal qty) {}

    /** Divides the weighed qty over the open rows by allocated quantity; the last row takes the remainder. */
    private static List<TankShare> splitByAllocation(List<Map<String, Object>> rows, BigDecimal wgt) {
        List<TankShare> out = new ArrayList<>();
        if (rows.size() == 1) {
            out.add(new TankShare(asLong(rows.get(0).get("id")), (String) rows.get(0).get("bay_name"), wgt));
            return out;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> r : rows) total = total.add(asDecimal(r.get("quantity")));
        BigDecimal assigned = BigDecimal.ZERO;
        for (int k = 0; k < rows.size(); k++) {
            Map<String, Object> r = rows.get(k);
            BigDecimal share;
            if (k == rows.size() - 1) share = wgt.subtract(assigned);
            else if (total.signum() > 0) share = wgt.multiply(asDecimal(r.get("quantity"))).divide(total, 3, RoundingMode.HALF_UP);
            else share = wgt.divide(BigDecimal.valueOf(rows.size()), 3, RoundingMode.HALF_UP);
            assigned = assigned.add(share);
            out.add(new TankShare(asLong(r.get("id")), (String) r.get("bay_name"), share));
        }
        return out;
    }

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

    private static Long asLong(Object v) { return v instanceof Number n ? n.longValue() : null; }

    private static void markRollbackOnly() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    private static String str(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        if (v == null) return null;
        String s = v.toString();
        return s.isBlank() ? null : s.trim();
    }
}
