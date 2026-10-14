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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code m10.stockoutward-bay-complete} — M10.StockOutward's dedicated "Bay
 * Complete", closing rows {@link M10StockOutwardStartBayActionHandler}
 * opened. Kept separate from {@link M10BayCompleteActionHandler} (the shared
 * M10.FGLoading one) per explicit business request, so this flow's stock
 * movement shape can differ without a workflow_code branch in the shared
 * handler.
 *
 * <p>Same FEFO-pick-and-deduct mechanics as {@link M10BayCompleteActionHandler}
 * (scoped to the row's Godown, one {@code m5_stock_balance} decrement per lot
 * picked, insufficient-stock and concurrency-race guards identical), but the
 * {@code m5_stock_movement} row this posts is shaped for Stock Outward
 * specifically:
 * <ul>
 *   <li>{@code kind = 'STOCK_OUTWARD'} (not the shared handler's generic
 *       {@code 'CONSUME'}), {@code ref_type = 'UNLOAD_BAY'}.
 *   <li>{@code from_bin} = the Godown {@link M10StockOutwardStartBayActionHandler}
 *       recorded on the row (physical source) — NOT the specific lot's
 *       bin_code the shared handler uses (the actual stock DEDUCTION below
 *       still targets that precise lot/bin for correctness; only the ledger
 *       row's displayed from_bin is Godown-level).
 *   <li>{@code to_bin} = the row's {@code rd_code} (the order sheet's
 *       destination/vendor code) — treated as the conceptual destination
 *       this stock is going to.
 * </ul>
 * One movement row is written per lot picked (same as the shared handler,
 * since FEFO can split one line across several lots) — all sharing the same
 * from_bin/to_bin for that line.
 */
@Component
public class M10StockOutwardBayCompleteActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(M10StockOutwardBayCompleteActionHandler.class);

    @Autowired private JdbcTemplate jdbc;
    @Autowired private FefoStockPickerService fefoPicker;

    @Override
    public String code() { return "m10.stockoutward-bay-complete"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        Object linesObj = formData.get("order_sku_line_items");
        if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) {
            return ActionResult.error("formData.order_sku_line_items must be a non-empty array");
        }
        List<Map<String, Object>> lines = (List<Map<String, Object>>) linesObj;

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();

        log.info("action.stockoutward-bay-complete.start tenantId={} instanceId={} lineCount={}", tenantId, instanceId, lines.size());

        List<Map<String, Object>> closed = new ArrayList<>();
        BigDecimal totalProcessed = BigDecimal.ZERO;

        for (int i = 0; i < lines.size(); i++) {
            Object lineObj = lines.get(i);
            if (!(lineObj instanceof Map)) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lineObj;
            String routeId = str(line, "route_id");
            String orderId = str(line, "order_id");
            String skuCode = str(line, "sku_code");
            if (routeId == null || skuCode == null) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] is missing route_id or sku_code");
            }

            BigDecimal processedQty;
            try {
                Object qtyObj = line.get("loading_qty") != null ? line.get("loading_qty") : line.get("processed_quantity");
                processedQty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") loading_qty must be a number");
            }
            if (processedQty == null || processedQty.signum() < 0) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs loading_qty >= 0");
            }

            BigDecimal balanceQty;
            try {
                Object balObj = line.get("balance_qty");
                balanceQty = balObj == null ? null : new BigDecimal(balObj.toString());
            } catch (NumberFormatException e) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") balance_qty must be a number");
            }

            List<Map<String, Object>> bayRow = jdbc.queryForList(
                    "SELECT godown_code, rd_code FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                    tenantId, instanceId, routeId, orderId, orderId, skuCode);
            String godownCode = bayRow.isEmpty() ? null : (String) bayRow.get(0).get("godown_code");
            String rdCode = bayRow.isEmpty() ? null : (String) bayRow.get(0).get("rd_code");

            List<FefoStockPickerService.FefoPick> picks = fefoPicker.pick(tenantId, skuCode, null, godownCode, processedQty);
            BigDecimal residual = fefoPicker.residualQty(processedQty, picks);
            if (residual.signum() > 0) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") insufficient stock: "
                        + "requested " + processedQty + ", available " + processedQty.subtract(residual));
            }

            for (FefoStockPickerService.FefoPick pick : picks) {
                int deducted = jdbc.update(
                        "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand - ?, last_movement_at = CURRENT_TIMESTAMP "
                                + "WHERE tenant_id = ? AND item_code = ? AND "
                                + "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code = ? AND qty_on_hand >= ?",
                        pick.qty(), tenantId, skuCode, pick.lotNo(), pick.lotNo(), pick.binCode(), pick.qty());
                if (deducted == 0) {
                    markRollbackOnly();
                    return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") stock changed "
                            + "concurrently — lot " + pick.lotNo() + "/bin " + pick.binCode()
                            + " no longer has enough qty_on_hand, retry Bay Complete");
                }
                // from_bin = the Godown (not the specific lot's bin_code --
                // see class javadoc); to_bin = the destination/vendor code
                // (rd_code) off the order sheet. The deduction above still
                // targets the precise lot/bin for correctness.
                jdbc.update(
                        "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, to_bin, qty, uom, "
                                + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                        tenantId, "STOCK_OUTWARD", skuCode, pick.lotNo(), godownCode, rdCode, pick.qty(), pick.uom(),
                        "UNLOAD_BAY", instanceId, routeId);
                log.info("action.stockoutward-bay-complete.stock-consume tenantId={} instanceId={} routeId={} skuCode={} lotNo={} fromBin={} toBin={} qty={} uom={}",
                        tenantId, instanceId, routeId, skuCode, pick.lotNo(), godownCode, rdCode, pick.qty(), pick.uom());
            }

            int n = jdbc.update(
                    "UPDATE loading_progress_detail_bay SET processed_quantity = ?, balance_qty = ?, status = 'DONE' "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                    processedQty, balanceQty, tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (n == 0) {
                markRollbackOnly();
                return ActionResult.error("no open bay row found for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode);
            }

            RowActorStamp.completedLoading(jdbc, tenantId, instanceId, routeId, orderId, skuCode, RowActorStamp.performedBy(line, formData));

            int dispatchTasksClosed = jdbc.update(
                    "UPDATE dispatch_task SET status = 'Completed', "
                            + "completed_at = CASE WHEN completed_at IS NULL THEN CURRENT_TIMESTAMP ELSE completed_at END "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'Open'",
                    tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (dispatchTasksClosed > 0) {
                log.info("action.stockoutward-bay-complete.dispatch-task-closed tenantId={} instanceId={} routeId={} skuCode={} count={}",
                        tenantId, instanceId, routeId, skuCode, dispatchTasksClosed);
            }

            totalProcessed = totalProcessed.add(processedQty);
            Map<String, Object> row = new HashMap<>();
            row.put("routeId", routeId);
            row.put("skuCode", skuCode);
            row.put("processedQuantity", processedQty);
            closed.add(row);
        }

        jdbc.update(
                "UPDATE loading_progress_detail SET processed_quantity = processed_quantity + ? "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                totalProcessed, tenantId, instanceId);

        log.info("action.stockoutward-bay-complete.done tenantId={} instanceId={} linesClosed={} totalProcessed={}",
                tenantId, instanceId, closed.size(), totalProcessed);

        return ActionResult.builder()
                .ok(true)
                .message(closed.size() + " bay line(s) closed, processed " + totalProcessed + " total — pick another bay or press Completed")
                .data(Map.of("closed", closed, "totalProcessed", totalProcessed))
                .build();
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
