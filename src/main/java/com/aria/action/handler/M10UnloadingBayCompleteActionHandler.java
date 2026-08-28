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
 * {@code m10.unloading-bay-complete} — closes the {@code unloading_progress_detail_bay}
 * line(s) opened by {@link M10StartUnloadingBayActionHandler} for M10.PackUnload's
 * unloading-mark-in-progress step and posts the received quantity to stock.
 *
 * <p>Opposite direction of {@link M10BayCompleteActionHandler} (loading's Bay
 * Complete, which FEFO-picks and {@code CONSUME}s existing stock): this
 * receives NEW stock into the bay's own bin — one {@code m5_stock_balance}
 * {@code qty_on_hand} increment (row inserted if the lot+bin never had one)
 * and one {@code m5_stock_movement} row per line, {@code kind='RECEIPT'},
 * {@code ref_type='BAY'} (matching the loading side's own {@code ref_type},
 * not {@code UNLOAD_BAY}/{@code TANKER_UNLOAD} used by the Reject Flow Unload
 * / Tanker Inbound siblings — this is a distinct receiving process, not a
 * reversal). {@code lot_no} is left NULL, same convention as
 * {@link M10TankerUnloadReceiptActionHandler}'s tank receipt — this process
 * has no lot tracking of its own.
 *
 * <p>Matched by (route_id, order_id, sku_code) — order_id NULL-safe, same as
 * every other bay handler in this module.
 *
 * <p><b>Whole-request atomicity:</b> same as {@link M10BayCompleteActionHandler}
 * — a plain {@code ActionResult.error(...)} return does not roll back a
 * Spring {@code @Transactional} method, so {@link #markRollbackOnly()} is
 * called before every error return inside the line loop.
 *
 * <p>Payload shape:
 * <pre>
 *   { "instanceId": 1495, "formData": { "order_sku_line_items": [
 *       { "route_id": "ROU028912", "order_id": "ORD1123", "sku_code": "SKU1215", "unloading_qty": 20 }
 *   ] } }
 * </pre>
 */
@Component
public class M10UnloadingBayCompleteActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(M10UnloadingBayCompleteActionHandler.class);

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.unloading-bay-complete"; }

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

        log.info("action.unloading-bay-complete.start tenantId={} instanceId={} lineCount={}", tenantId, instanceId, lines.size());

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
                    "SELECT id, bay_name FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                    tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (rows.isEmpty()) {
                markRollbackOnly();
                return ActionResult.error("no open unload bay row found for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode);
            }
            String binCode = (String) rows.get(0).get("bay_name");

            if (qty.signum() > 0) {
                String uom = lookupUom(tenantId, skuCode);
                int updated = jdbc.update(
                        "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, last_movement_at = CURRENT_TIMESTAMP "
                                + "WHERE tenant_id = ? AND item_code = ? AND lot_no IS NULL AND bin_code = ?",
                        qty, tenantId, skuCode, binCode);
                if (updated == 0) {
                    jdbc.update(
                            "INSERT INTO m5_stock_balance (tenant_id, item_code, lot_no, bin_code, uom, qty_on_hand, last_movement_at) "
                                    + "VALUES (?,?,NULL,?,?,?,CURRENT_TIMESTAMP)",
                            tenantId, skuCode, binCode, uom, qty);
                }
                jdbc.update(
                        "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, to_bin, qty, uom, "
                                + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?)",
                        tenantId, "RECEIPT", skuCode, binCode, qty, uom, "BAY", instanceId, routeId);
                log.info("action.unloading-bay-complete.stock-receipt tenantId={} instanceId={} routeId={} skuCode={} binCode={} qty={} uom={}",
                        tenantId, instanceId, routeId, skuCode, binCode, qty, uom);
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

        log.info("action.unloading-bay-complete.done tenantId={} instanceId={} linesClosed={} totalReceived={}",
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
