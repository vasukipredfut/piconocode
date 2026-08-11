package com.aria.m10.handscanner.service;

import com.aria.action.ActionDispatcher;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Orchestrates the hand-scanner-side of the M10.FGLoading flow: one scanned
 * Gate Pass QR (gp_number) resolves gate pass -> workflow instance -> the
 * vehicle's order/SKU dispatch details, and — only when the workflow is at
 * exactly the state where the real "Start Bay" transition applies
 * (LOADING_IN_PROGRESS, see {@code com.aria.action.handler.M10StartBayActionHandler})
 * — attempts to assign a bay by dispatching the SAME {@code m10.start-bay}
 * action the frontend uses (via {@link ActionDispatcher}, in-process, not a
 * second HTTP round-trip), so this doesn't duplicate or diverge from that
 * handler's FSM-transition + loading_progress_detail_bay bookkeeping.
 *
 * <p>Bay picking here is a simple placeholder — first of Bay1..Bay12 with no
 * open {@code loading_progress_detail_bay} row — since there is no bay-
 * master/occupancy table to consult (confirmed: bay_name is a fixed 12-value
 * set, not a real entity). Swap {@link #pickFreeBay} for real business logic
 * (route/zone rules, operator choice, etc.) as needed.
 */
@Service
public class HandScannerService {

    private static final List<String> BAY_NAMES =
            IntStream.rangeClosed(1, 12).mapToObj(n -> "Bay" + n).collect(Collectors.toUnmodifiableList());

    private final JdbcTemplate jdbc;
    private final ActionDispatcher actions;

    public HandScannerService(JdbcTemplate jdbc, ActionDispatcher actions) {
        this.jdbc = jdbc;
        this.actions = actions;
    }

    public Map<String, Object> scanGatePass(String gpNumber) {
        long tenantId = TenantContext.get();

        Map<String, Object> gatePass = findGatePass(tenantId, gpNumber);
        if (gatePass == null) {
            return Map.of("error", "gate pass not found: " + gpNumber);
        }

        Object workflowInstanceIdRaw = gatePass.get("workflow_instance_id");
        Long workflowInstanceId = workflowInstanceIdRaw == null ? null : ((Number) workflowInstanceIdRaw).longValue();
        Map<String, Object> workflowInstance = workflowInstanceId == null ? null
                : findWorkflowInstance(tenantId, workflowInstanceId);

        String vehicleNo = (String) gatePass.get("vehicle_no");
        List<Map<String, Object>> orderDispatchDetails = findOrderDispatchDetails(tenantId, vehicleNo);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("gatePass", gatePass);
        out.put("workflowInstance", workflowInstance);
        out.put("orderDispatchDetails", orderDispatchDetails);
        out.put("bayAssignment", tryAssignBay(tenantId, workflowInstanceId, workflowInstance, gatePass, vehicleNo,
                orderDispatchDetails));
        return out;
    }

    private Map<String, Object> tryAssignBay(long tenantId, Long workflowInstanceId, Map<String, Object> workflowInstance,
                                              Map<String, Object> gatePass, String vehicleNo,
                                              List<Map<String, Object>> orderDispatchDetails) {
        if (workflowInstanceId == null) {
            return notAttempted("gate pass has no linked workflow instance yet");
        }
        String currentState = workflowInstance == null ? null : (String) workflowInstance.get("current_state");
        if (!"LOADING_IN_PROGRESS".equals(currentState)) {
            return notAttempted("workflow is not ready for bay assignment (current state: " + currentState
                    + ", expected LOADING_IN_PROGRESS)");
        }
        if (orderDispatchDetails.isEmpty()) {
            return notAttempted("no order dispatch details found for vehicle " + vehicleNo);
        }

        List<Map<String, Object>> lineItems = orderDispatchDetails.stream()
                .map(this::toLineItem)
                .filter(l -> l.get("route_id") != null && l.get("sku_code") != null)
                .toList();
        if (lineItems.isEmpty()) {
            return notAttempted("order dispatch rows are missing route_id/sku_code");
        }

        String bayName = pickFreeBay(tenantId);
        if (bayName == null) {
            return notAttempted("no free bay available (Bay1..Bay12 all occupied)");
        }

        Map<String, Object> formData = new LinkedHashMap<>();
        formData.put("vehicle_no", vehicleNo);
        formData.put("gpNumber", gatePass.get("gp_number"));
        formData.put("bay_name", bayName);
        formData.put("order_sku_line_items", lineItems);

        ActionInvocation inv = ActionInvocation.builder()
                .instanceId(workflowInstanceId)
                .pageCode("aria:loading-in-progress-entry")
                .formData(formData)
                .build();
        ActionResult result = actions.dispatch("m10.start-bay", inv);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("attempted", true);
        out.put("bayName", bayName);
        out.put("ok", result.isOk());
        out.put("message", result.getMessage());
        out.put("data", result.getData());
        return out;
    }

    private Map<String, Object> toLineItem(Map<String, Object> row) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("route_id", row.get("route_id"));
        line.put("sku_code", row.get("sku_code"));
        line.put("sku_desc", row.get("sku_desc"));
        line.put("order_qty", row.get("remaining_qty") != null ? row.get("remaining_qty") : row.get("order_qty"));
        return line;
    }

    private String pickFreeBay(long tenantId) {
        List<String> occupied = jdbc.queryForList(
                "SELECT DISTINCT bay_name FROM loading_progress_detail_bay WHERE tenant_id = ? AND status = 'IN_PROGRESS'",
                String.class, tenantId);
        return BAY_NAMES.stream().filter(b -> !occupied.contains(b)).findFirst().orElse(null);
    }

    private Map<String, Object> notAttempted(String reason) {
        return Map.of("attempted", false, "reason", reason);
    }

    private Map<String, Object> findGatePass(long tenantId, String gpNumber) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM gate_pass WHERE tenant_id = ? AND gp_number = ?", tenantId, gpNumber);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Map<String, Object> findWorkflowInstance(long tenantId, Long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, workflow_code, current_state, status, assigned_role, business_ref "
                        + "FROM workflow_instance WHERE tenant_id = ? AND id = ?", tenantId, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private List<Map<String, Object>> findOrderDispatchDetails(long tenantId, String vehicleNo) {
        if (vehicleNo == null) return List.of();
        return jdbc.queryForList(
                "SELECT * FROM m5_order_dispatch_detail WHERE tenant_id = ? AND vehicle_no = ?", tenantId, vehicleNo);
    }
}
