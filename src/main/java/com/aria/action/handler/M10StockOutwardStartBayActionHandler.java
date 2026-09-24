package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import com.aria.workflow.dto.WorkflowTransitionResult;
import com.aria.workflow.engine.WorkflowDefinitionNotFoundException;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.engine.WorkflowGuardRejectedException;
import com.aria.workflow.engine.WorkflowInstanceNotFoundException;
import com.aria.workflow.engine.WorkflowTransitionConflictException;
import com.aria.workflow.engine.WorkflowTransitionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@code m10.stockoutward-start-bay} — M10.StockOutward's dedicated "Start
 * Bay", kept separate from {@link M10StartBayActionHandler} (the shared
 * M10.FGLoading one) per explicit business request rather than adding a
 * workflow_code branch to that shared handler.
 *
 * <p>Identical mechanics to {@link M10StartBayActionHandler} (opens one
 * {@code loading_progress_detail_bay} row per SKU line, drives the
 * LOADING_IN_PROGRESS(_R) -&gt; BAY_ASSIGNED(_R) transition), plus captures
 * {@code rd_code}/{@code vendor_name} per line (the order sheet's
 * destination/vendor code) onto the row, for {@link
 * M10StockOutwardBayCompleteActionHandler} to derive the stock movement's
 * {@code to_bin} from.
 *
 * <pre>
 *   { "instanceId": 1495, "pageCode": "aria:loading-in-progress-entry",
 *     "formData": {
 *       "vehicle_no": "TN86J8070", "gpNumber": "GP-1-1495", "bay_name": "Bay1",
 *       "godown_code": "SRG0001", "plant_code": "SR0001",
 *       "order_sku_line_items": [
 *         { "route_id": "ROU028912", "order_id": "ORD1123", "sku_code": "SKU1215", "sku_desc": "GROUNDNUT RAW OIL 15 KG TIN",
 *           "rd_code": "CUST001", "vendor_name": "XYZ Distributors", "order_qty": 45,
 *           "scanning_status": "BARCODE", "conveyor_mode": "CONVEYOR" }
 *       ]
 *     } }
 * </pre>
 */
@Component
public class M10StockOutwardStartBayActionHandler implements ActionHandler {

    private static final Set<String> VALID_BAY_NAMES =
            IntStream.rangeClosed(1, 12).mapToObj(n -> "Bay" + n).collect(Collectors.toUnmodifiableSet());

    @Autowired private JdbcTemplate jdbc;
    @Autowired private WorkflowEngineService engine;

    @Override
    public String code() { return "m10.stockoutward-start-bay"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");
        if (inv.getPageCode() == null) return ActionResult.error("pageCode required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        String bayNameInput = strReq(formData, "bay_name");
        String bayName;
        if (bayNameInput != null) {
            if (!VALID_BAY_NAMES.contains(bayNameInput)) {
                return ActionResult.error("formData.bay_name must be one of Bay1..Bay12, got: " + bayNameInput);
            }
            bayName = bayNameInput;
        } else {
            bayName = strReq(formData, "loading_tank_location");
            if (bayName == null) {
                return ActionResult.error("formData.bay_name or formData.loading_tank_location is required");
            }
        }

        String godownCode = strOpt(formData, "godown_code");
        String plantCode = strOpt(formData, "plant_code");

        Object linesObj = formData.get("order_sku_line_items");
        if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) {
            return ActionResult.error("formData.order_sku_line_items must be a non-empty array");
        }
        List<Map<String, Object>> lines = (List<Map<String, Object>>) linesObj;

        List<LineEntry> parsed = new ArrayList<>();
        BigDecimal totalQty = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            Map<String, Object> line = lines.get(i);
            String routeId = strReq(line, "route_id");
            String skuCode = strReq(line, "sku_code");
            if (routeId == null || skuCode == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] is missing route_id or sku_code");
            }
            BigDecimal qty;
            try {
                Object qtyObj = line.get("order_qty") != null ? line.get("order_qty") : line.get("quantity");
                qty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                return ActionResult.error("order_sku_line_items[" + i + "].order_qty must be a number");
            }
            if (qty == null || qty.signum() <= 0) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs order_qty > 0");
            }
            String scanningStatus = strReq(line, "scanning_status");
            if (scanningStatus == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") is missing scanning_status");
            }
            String conveyorMode = strReq(line, "conveyor_mode");
            if (conveyorMode == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") is missing conveyor_mode");
            }
            parsed.add(new LineEntry(routeId, strOpt(line, "order_id"), skuCode, strOpt(line, "sku_desc"), qty,
                    scanningStatus, conveyorMode, strOpt(line, "rd_code"), strOpt(line, "vendor_name")));
            totalQty = totalQty.add(qty);
        }

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();

        Integer openForThisInstance = jdbc.queryForObject(
                "SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'IN_PROGRESS'",
                Integer.class, tenantId, instanceId);
        if (openForThisInstance != null && openForThisInstance > 0) {
            return ActionResult.error("this vehicle already has an open bay — close it with Bay Complete before starting another");
        }

        String currentState = jdbc.queryForObject(
                "SELECT current_state FROM workflow_instance WHERE id = ? AND tenant_id = ?",
                String.class, instanceId, tenantId);

        Long actorUserId = inv.getContext() != null && inv.getContext().get("actorUserId") instanceof Number
                ? ((Number) inv.getContext().get("actorUserId")).longValue()
                : null;

        String newState = currentState;
        boolean transitioned = false;
        if ("LOADING_IN_PROGRESS".equals(currentState) || "LOADING_IN_PROGRESS_R".equals(currentState)) {
            Map<String, Object> engineFormData = new HashMap<>(formData);
            engineFormData.put("bayAction", "start");
            try {
                WorkflowTransitionResult r = engine.handlePageSubmission(instanceId, inv.getPageCode(), engineFormData, actorUserId);
                newState = r.getNewState();
                transitioned = r.isTransitioned();
            } catch (WorkflowGuardRejectedException | WorkflowTransitionNotFoundException
                    | WorkflowTransitionConflictException | WorkflowInstanceNotFoundException
                    | WorkflowDefinitionNotFoundException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ActionResult.error("bay not started, workflow transition failed: " + e.getMessage());
            }
        } else if (!"BAY_ASSIGNED".equals(currentState) && !"BAY_ASSIGNED_R".equals(currentState)) {
            return ActionResult.error("cannot start a bay from workflow state " + currentState);
        }

        String vehicleNo = strOpt(formData, "vehicle_no");
        String gatePassNo = strOpt(formData, "gpNumber");

        jdbc.update(
                "INSERT INTO loading_progress_detail (tenant_id, workflow_instance_id, vehicle_no, gate_pass_no, total_quantity, processed_quantity) "
                        + "VALUES (?, ?, ?, ?, ?, 0) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), "
                        + "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no), "
                        + "total_quantity = total_quantity + VALUES(total_quantity)",
                tenantId, instanceId, vehicleNo, gatePassNo, totalQty);

        Long parentId = jdbc.queryForObject(
                "SELECT id FROM loading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                Long.class, tenantId, instanceId);

        int dispatchTasksSkipped = 0;
        for (int i = 0; i < parsed.size(); i++) {
            LineEntry e = parsed.get(i);
            jdbc.update(
                    "INSERT INTO loading_progress_detail_bay "
                            + "(tenant_id, loading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, godown_code, plant_code, rd_code, vendor_name, status) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                    tenantId, parentId, instanceId, e.routeId, e.orderId, e.skuCode, e.skuDesc, bayName, e.quantity,
                    godownCode, plantCode, e.rdCode, e.vendorName);

            if ("WITHOUT_CONVEYOR".equals(e.conveyorMode) || "WITHOUT_BARCODE".equals(e.scanningStatus)) {
                dispatchTasksSkipped++;
                continue;
            }

            String taskCode = "DT-" + instanceId + "-" + bayName + "-" + e.skuCode + "-" + System.currentTimeMillis() + "-" + i;
            jdbc.update(
                    "INSERT INTO dispatch_task "
                            + "(tenant_id, workflow_code, type, workflow_instance_id, bay_code, route_id, order_id, task_code, "
                            + "order_ref, sku_code, gp_number, target_qty, processed_qty, status) "
                            + "VALUES (?,?,'Loading',?,?,?,?,?,?,?,?,?, 0, 'Open')",
                    tenantId, M10StartBayActionHandler.CONVEYOR_WORKFLOW_CODE, instanceId, bayName, e.routeId, e.orderId, taskCode,
                    e.orderId, e.skuCode, gatePassNo, e.quantity);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("bayName", bayName);
        data.put("lineCount", parsed.size());
        data.put("totalQuantity", totalQty);
        data.put("dispatchTasksSkipped", dispatchTasksSkipped);
        data.put("fromState", currentState);
        data.put("toState", newState);
        data.put("transitioned", transitioned);

        return ActionResult.builder()
                .ok(true)
                .message("bay '" + bayName + "' started with " + parsed.size() + " SKU line(s), total qty " + totalQty + ", workflow state " + newState)
                .data(data)
                .build();
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

    private record LineEntry(String routeId, String orderId, String skuCode, String skuDesc, BigDecimal quantity,
                              String scanningStatus, String conveyorMode, String rdCode, String vendorName) {}
}
