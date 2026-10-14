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
 * {@code m10.stockinward-bay-complete} — M10.StockInward's dedicated "Bay
 * Complete" / "Stop Bay", closing rows {@link M10StockInwardStartBayActionHandler}
 * opened. Kept separate from {@link M10UnloadingBayCompleteActionHandler}
 * (the shared M10.PackUnload one) per explicit business request, so this
 * flow's stock-movement shape can differ without a workflow_code branch in
 * the shared handler.
 *
 * <p>Same receiving mechanics as {@link M10UnloadingBayCompleteActionHandler}
 * (one {@code m5_stock_balance} {@code qty_on_hand} increment per line,
 * inserting the row if it doesn't exist), but the {@code m5_stock_movement}
 * row this posts is shaped for Stock Inward specifically:
 * <ul>
 *   <li>{@code kind = 'STOCK_INWARD'} (not the shared handler's generic
 *       {@code 'RECEIPT'}), {@code ref_type = 'BAY'}.
 *   <li>{@code to_bin} = the Godown {@link M10StockInwardStartBayActionHandler}
 *       recorded on the row (physical destination) — NOT the bay name (the
 *       shared handler uses bay_name here; Stock Inward wants the Godown).
 *   <li>{@code from_bin} = the row's {@code rd_code} (the order sheet's
 *       vendor code) — the vendor is treated as the conceptual source
 *       location this stock is coming from.
 * </ul>
 * Both {@code to_bin}/{@code from_bin} are null when the corresponding
 * picker/order-sheet value was never supplied — same "degrade gracefully"
 * convention as every other optional field in this module.
 */
@Component
public class M10StockInwardBayCompleteActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(M10StockInwardBayCompleteActionHandler.class);

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.stockinward-bay-complete"; }

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

        log.info("action.stockinward-bay-complete.start tenantId={} instanceId={} lineCount={}", tenantId, instanceId, lines.size());

        List<Map<String, Object>> closed = new ArrayList<>();
        BigDecimal totalReceived = BigDecimal.ZERO;

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

            BigDecimal qty;
            try {
                Object qtyObj = line.get("unloading_qty") != null ? line.get("unloading_qty") : line.get("processed_quantity");
                qty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") unloading_qty must be a number");
            }
            if (qty == null || qty.signum() < 0) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs unloading_qty >= 0");
            }

            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT id, bay_name, godown_code, rd_code FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                    tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (rows.isEmpty()) {
                markRollbackOnly();
                return ActionResult.error("no open unload bay row found for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode);
            }
            String binCode = (String) rows.get(0).get("bay_name");
            String godownCode = (String) rows.get(0).get("godown_code");
            String rdCode = (String) rows.get(0).get("rd_code");

            if (qty.signum() > 0) {
                String uom = lookupUom(tenantId, skuCode);
                int updated = jdbc.update(
                        "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, warehouse = COALESCE(?, warehouse), last_movement_at = CURRENT_TIMESTAMP "
                                + "WHERE tenant_id = ? AND item_code = ? AND lot_no IS NULL AND bin_code = ?",
                        qty, godownCode, tenantId, skuCode, binCode);
                if (updated == 0) {
                    jdbc.update(
                            "INSERT INTO m5_stock_balance (tenant_id, item_code, lot_no, bin_code, warehouse, uom, qty_on_hand, last_movement_at) "
                                    + "VALUES (?,?,NULL,?,?,?,?,CURRENT_TIMESTAMP)",
                            tenantId, skuCode, binCode, godownCode, uom, qty);
                }
                // to_bin = the Godown (not bay_name -- see class javadoc);
                // from_bin = the vendor code (rd_code) off the order sheet,
                // treated as the conceptual source location.
                jdbc.update(
                        "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, from_bin, to_bin, qty, uom, "
                                + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)",
                        tenantId, "STOCK_INWARD", skuCode, rdCode, godownCode, qty, uom, "BAY", instanceId, routeId);
                log.info("action.stockinward-bay-complete.stock-receipt tenantId={} instanceId={} routeId={} skuCode={} fromBin={} toBin={} qty={} uom={}",
                        tenantId, instanceId, routeId, skuCode, rdCode, godownCode, qty, uom);
            }

            int n = jdbc.update(
                    "UPDATE unloading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                    qty, tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (n == 0) {
                markRollbackOnly();
                return ActionResult.error("unloading_progress_detail_bay row changed concurrently for route_id=" + routeId + " sku_code=" + skuCode + " — retry");
            }

            RowActorStamp.completedUnloading(jdbc, tenantId, instanceId, routeId, orderId, skuCode, RowActorStamp.performedBy(line, formData));

            int dispatchTasksClosed = jdbc.update(
                    "UPDATE dispatch_task SET status = 'Completed', "
                            + "completed_at = CASE WHEN completed_at IS NULL THEN CURRENT_TIMESTAMP ELSE completed_at END "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'Open'",
                    tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (dispatchTasksClosed > 0) {
                log.info("action.stockinward-bay-complete.dispatch-task-closed tenantId={} instanceId={} routeId={} skuCode={} count={}",
                        tenantId, instanceId, routeId, skuCode, dispatchTasksClosed);
            }

            totalReceived = totalReceived.add(qty);
            Map<String, Object> row = new HashMap<>();
            row.put("routeId", routeId);
            row.put("skuCode", skuCode);
            row.put("processedQuantity", qty);
            closed.add(row);
        }

        String currentStep = jdbc.queryForObject(
                "SELECT current_state FROM workflow_instance WHERE id = ? AND tenant_id = ?",
                String.class, instanceId, tenantId);

        jdbc.update(
                "UPDATE unloading_progress_detail SET processed_quantity = processed_quantity + ?, current_step = ? "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                totalReceived, currentStep, tenantId, instanceId);

        Integer stillOpen = jdbc.queryForObject(
                "SELECT COUNT(*) FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status != 'DONE'",
                Integer.class, tenantId, instanceId);
        if (stillOpen != null && stillOpen == 0) {
            jdbc.update(
                    "UPDATE unloading_progress_detail SET status = 'DONE' WHERE tenant_id = ? AND workflow_instance_id = ?",
                    tenantId, instanceId);
        }

        log.info("action.stockinward-bay-complete.done tenantId={} instanceId={} linesClosed={} totalReceived={}",
                tenantId, instanceId, closed.size(), totalReceived);

        return ActionResult.builder()
                .ok(true)
                .message(closed.size() + " unload bay line(s) closed, received " + totalReceived + " total into stock")
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
