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
 * <p>Payload shape mirrors {@link M10StartBayActionHandler}, with two
 * distinct quantity keys per line (unlike the loading side, which has only
 * one): {@code quantity} (required, &gt;= 0) is the planned amount, stored on
 * {@code unloading_progress_detail_bay.quantity} and rolled into
 * {@code unloading_progress_detail.total_quantity}; {@code unloading_qty}
 * (optional, &gt;= 0 if given) is stored directly onto that same row's
 * {@code processed_quantity} column right away, at Start Bay time — it does
 * NOT wait for Unload Bay Complete, which still overwrites it with whatever
 * it's given when the row closes (status {@code DONE}), same as before.
 * Lines may also carry {@code rd_code} and an "RD name" (accepted as
 * {@code vendor_name}, or {@code rd_desc}/{@code rd_name} as aliases) — both
 * optional, stored straight onto the row (columns added in V258 for the
 * M10.TankerInbound side that shares this table; this handler previously
 * ignored all four of these fields):
 * <pre>
 *   { "instanceId": 1495, "formData": {
 *       "vehicle_no": "TN86J8070", "gpNumber": "GP-1-1495", "bay_name": "Bay1",
 *       "order_sku_line_items": [
 *         { "route_id": "ROU028912", "order_id": "ORD1123", "sku_code": "SKU1215", "sku_desc": "GROUNDNUT RAW OIL 15 KG TIN",
 *           "rd_code": "RD1", "vendor_name": "RD One", "quantity": 20, "unloading_qty": 20 }
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
                Object qtyObj = line.get("quantity");
                qty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") quantity must be a number");
            }
            if (qty == null || qty.signum() < 0) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs quantity >= 0");
            }

            // Distinct from `quantity` above -- optional, stored straight onto
            // processed_quantity at Start Bay time (see class javadoc); Unload
            // Bay Complete still overwrites it with whatever it's given then.
            BigDecimal unloadingQty;
            try {
                Object unloadingQtyObj = line.get("unloading_qty");
                unloadingQty = unloadingQtyObj == null ? null : new BigDecimal(unloadingQtyObj.toString());
            } catch (NumberFormatException e) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") unloading_qty must be a number");
            }
            if (unloadingQty != null && unloadingQty.signum() < 0) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs unloading_qty >= 0");
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

            String rdCode = strOpt(line, "rd_code", "rdCode");
            // "RD name" reuses the existing vendor_name column (V258) rather than
            // a new one -- the payload may send it under vendor_name or rd_desc/rd_name.
            String vendorName = strOpt(line, "vendor_name", "vendorName", "rd_desc", "rdDesc", "rd_name", "rdName");

            parsed.add(new LineEntry(routeId, orderId, skuCode, strOpt(line, "sku_desc"), rdCode, vendorName, qty, unloadingQty));
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
        // Optional — same Plant/Godown pickers as the regular unloading side
        // (M10StartUnloadingBayActionHandler); this reject-flow unload shares
        // the same unloading_progress_detail_bay table and columns.
        String godownCode = strOpt(formData, "godown_code");
        String plantCode = strOpt(formData, "plant_code");

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
                            + "(tenant_id, unloading_progress_detail_id, workflow_instance_id, route_id, order_id, vendor_name, rd_code, sku_code, sku_desc, bay_name, quantity, processed_quantity, godown_code, plant_code, status) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                    tenantId, parentId, instanceId, e.routeId, e.orderId, e.vendorName, e.rdCode, e.skuCode, e.skuDesc, bayName, e.quantity, e.unloadingQty, godownCode, plantCode);

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

    private static String strOpt(Map<String, Object> bag, String... keys) {
        for (String key : keys) {
            Object v = bag.get(key);
            if (v != null && !v.toString().isBlank()) return v.toString();
        }
        return null;
    }

    private record LineEntry(String routeId, String orderId, String skuCode, String skuDesc, String rdCode, String vendorName, BigDecimal quantity, BigDecimal unloadingQty) {}
}
