package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@code m10.start-unload-bay} — the "Start Bay" button on the Reject Flow
 * Unloading page. Sibling of {@link M10StartBayActionHandler}, for the
 * opposite direction: a vehicle that was already loaded gets some or all of
 * its cargo taken back off. Unlike the loading handler, this one never
 * drives a workflow transition itself — the page's own "Bay Assign" /
 * "Start Bay" {@code WorkflowActionButton}s call {@code workflow.transition}
 * directly (with {@code preTransition}), so this handler is pure data entry.
 *
 * <p>Per SKU line: opens one {@code unloading_progress_detail_bay} row
 * (status IN_PROGRESS) and flags the matching, already-loaded
 * {@code loading_progress_detail_bay} row {@code UNLOAD_IN_PROGRESS} — a
 * line can only be unloaded if it was actually loaded (status DONE) first;
 * there is nothing to take off a vehicle for a SKU that was never loaded.
 *
 * <p>Payload shape mirrors {@link M10StartBayActionHandler} exactly, with
 * {@code unloading_qty} in place of {@code order_qty}:
 * <pre>
 *   { "instanceId": 1495, "formData": {
 *       "vehicle_no": "TN86J8070", "gpNumber": "GP-1-1495", "bay_name": "Bay1",
 *       "order_sku_line_items": [
 *         { "route_id": "ROU028912", "order_id": "ORD1123", "sku_code": "SKU1215", "sku_desc": "GROUNDNUT RAW OIL 15 KG TIN", "unloading_qty": 20 }
 *       ] } }
 * </pre>
 */
@Component
public class M10StartUnloadBayActionHandler implements ActionHandler {

    private static final Set<String> VALID_BAY_NAMES =
            IntStream.rangeClosed(1, 12).mapToObj(n -> "Bay" + n).collect(Collectors.toUnmodifiableSet());

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.start-unload-bay"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        String bayNameInput = strReq(formData, "bay_name");
        if (bayNameInput == null) {
            return ActionResult.error("formData.bay_name is required");
        }
        if (!VALID_BAY_NAMES.contains(bayNameInput)) {
            return ActionResult.error("formData.bay_name must be one of Bay1..Bay12, got: " + bayNameInput);
        }
        String bayName = bayNameInput;

        Object linesObj = formData.get("order_sku_line_items");
        if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) {
            return ActionResult.error("formData.order_sku_line_items must be a non-empty array");
        }
        List<Map<String, Object>> lines = (List<Map<String, Object>>) linesObj;

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();

        List<LineEntry> parsed = new ArrayList<>();
        BigDecimal totalQty = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            Object lineObj = lines.get(i);
            if (!(lineObj instanceof Map)) {
                return ActionResult.error("order_sku_line_items[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lineObj;
            String routeId = strReq(line, "route_id");
            String skuCode = strReq(line, "sku_code");
            if (routeId == null || skuCode == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] is missing route_id or sku_code");
            }
            BigDecimal qty;
            try {
                Object qtyObj = line.get("unloading_qty") != null ? line.get("unloading_qty") : line.get("quantity");
                qty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") unloading_qty must be a number");
            }
            if (qty == null || qty.signum() <= 0) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs unloading_qty > 0");
            }
            String orderId = strOpt(line, "order_id");

            Integer loadedRows = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'DONE'",
                    Integer.class, tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (loadedRows == null || loadedRows == 0) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") has no loaded record to unload against "
                        + "(route_id=" + routeId + " order_id=" + orderId + ")");
            }

            parsed.add(new LineEntry(routeId, orderId, skuCode, strOpt(line, "sku_desc"), qty));
            totalQty = totalQty.add(qty);
        }

        Integer openForThisInstance = jdbc.queryForObject(
                "SELECT COUNT(*) FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'IN_PROGRESS'",
                Integer.class, tenantId, instanceId);
        if (openForThisInstance != null && openForThisInstance > 0) {
            return ActionResult.error("this vehicle already has an open unload bay — close it with Stop Bay before starting another");
        }

        String vehicleNo = strOpt(formData, "vehicle_no");
        String gatePassNo = strOpt(formData, "gpNumber");

        // flow_variant_code/current_step aren't projected here automatically
        // (this table has no m0_workflow_projection row -- see V261), so read
        // them straight off workflow_instance and stamp them ourselves.
        Map<String, Object> instanceRow = jdbc.queryForMap(
                "SELECT workflow_code, current_state FROM workflow_instance WHERE id = ? AND tenant_id = ?",
                instanceId, tenantId);
        String flowVariantCode = (String) instanceRow.get("workflow_code");
        String currentStep = (String) instanceRow.get("current_state");

        jdbc.update(
                "INSERT INTO unloading_progress_detail (tenant_id, workflow_instance_id, flow_variant_code, current_step, vehicle_no, gate_pass_no, total_quantity, processed_quantity) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, 0) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "flow_variant_code = COALESCE(unloading_progress_detail.flow_variant_code, VALUES(flow_variant_code)), "
                        + "current_step = VALUES(current_step), "
                        + "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), "
                        + "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no), "
                        + "total_quantity = total_quantity + VALUES(total_quantity)",
                tenantId, instanceId, flowVariantCode, currentStep, vehicleNo, gatePassNo, totalQty);

        Long parentId = jdbc.queryForObject(
                "SELECT id FROM unloading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                Long.class, tenantId, instanceId);

        for (LineEntry e : parsed) {
            jdbc.update(
                    "INSERT INTO unloading_progress_detail_bay "
                            + "(tenant_id, unloading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, status) "
                            + "VALUES (?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                    tenantId, parentId, instanceId, e.routeId, e.orderId, e.skuCode, e.skuDesc, bayName, e.quantity);

            int flagged = jdbc.update(
                    "UPDATE loading_progress_detail_bay SET status = 'UNLOAD_IN_PROGRESS' "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'DONE'",
                    tenantId, instanceId, e.routeId, e.orderId, e.orderId, e.skuCode);
            if (flagged == 0) {
                // The DONE-row existence check above ran in the same transaction,
                // so this can only be a genuine race (another request flagged/closed
                // it between the check and this UPDATE) -- fail loudly rather than
                // silently leaving an unloading_progress_detail_bay row with no
                // corresponding flagged loading row.
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items (" + e.skuCode + ") loaded row changed concurrently — retry Start Bay");
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("bayName", bayName);
        data.put("lineCount", parsed.size());
        data.put("totalQuantity", totalQty);

        return ActionResult.builder()
                .ok(true)
                .message("unload bay '" + bayName + "' started with " + parsed.size() + " SKU line(s), total qty " + totalQty)
                .data(data)
                .build();
    }

    private static void markRollbackOnly() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    private static String strReq(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        if (v == null || v.toString().isBlank()) return null;
        return v.toString();
    }

    private static String strOpt(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        return v == null ? null : v.toString();
    }

    private record LineEntry(String routeId, String orderId, String skuCode, String skuDesc, BigDecimal quantity) {}
}
