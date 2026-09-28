package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.engine.WorkflowInstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code m10.report-breakdown} — workflow-agnostic escape hatch: an operator
 * reports a vehicle breakdown from ANY state of ANY M10 workflow instance
 * (loading, unloading, stock-inward, stock-outward — anything driving
 * {@code gate_pass}), without needing a declared FSM transition for every
 * state it could happen from.
 *
 * <p>Two outcomes, decided purely by whether stock was already loaded onto
 * the vehicle (checked once, against the shared {@code loading_progress_detail_bay}
 * table every loading-direction workflow — including M10.StockOutward's own
 * dedicated bay handlers — writes to):
 * <ul>
 *   <li><b>Nothing loaded yet</b> (no row {@code status IN ('DONE','UNLOAD_IN_PROGRESS')}):
 *       closes the gate pass directly — {@link WorkflowEngineService#cancel}
 *       (the existing admin escape hatch, works from any state) marks
 *       {@code workflow_instance.status = 'CANCELLED'}, then this handler
 *       itself closes {@code gate_pass} (cancel() never touches it).</li>
 *   <li><b>Already loaded</b>: refused — the operator must first reverse the
 *       loaded stock using the EXISTING Reject Flow Unload actions
 *       ({@code m10.start-unload-bay} / {@code m10.unload-bay-complete}, see
 *       {@code docs/m10/REJECT-FLOW-UNLOAD.md}) before reporting breakdown;
 *       this handler deliberately does not build a second, parallel
 *       auto-reversal path.</li>
 * </ul>
 *
 * <p>{@code gate_pass.status}/{@code current_step} are set to {@code ABORTED}
 * — the same terminal-state token every M10 workflow's own
 * {@code m0_workflow_projection} row already uses for an abnormal close
 * (see {@code terminal_states} in V47/V164), so nothing downstream that
 * already filters on it needs to learn a new value.
 *
 * <pre>
 *   { "instanceId": 1495, "formData": { "reason": "engine failure on NH45" } }
 * </pre>
 */
@Component
public class M10ReportBreakdownActionHandler implements ActionHandler {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private WorkflowEngineService engine;

    @Override
    public String code() { return "m10.report-breakdown"; }

    @Override
    @Transactional("m0PageTransactionManager")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();
        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        String reason = strOpt(formData, "reason");

        Map<String, Object> instanceRow;
        try {
            instanceRow = jdbc.queryForMap(
                    "SELECT workflow_code, current_state, status FROM workflow_instance WHERE id = ? AND tenant_id = ?",
                    instanceId, tenantId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return ActionResult.error("workflow instance " + instanceId + " not found");
        }
        String workflowCode = (String) instanceRow.get("workflow_code");
        String currentState = (String) instanceRow.get("current_state");
        String instanceStatus = (String) instanceRow.get("status");
        if ("CLOSED".equals(instanceStatus) || "CANCELLED".equals(instanceStatus)) {
            return ActionResult.error("this gate pass is already closed");
        }

        Integer loadedRows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM loading_progress_detail_bay "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND status IN ('DONE','UNLOAD_IN_PROGRESS')",
                Integer.class, tenantId, instanceId);
        if (loadedRows != null && loadedRows > 0) {
            return ActionResult.error("stock is already loaded on this vehicle — use Start Bay / Stop Bay "
                    + "(Reject Flow Unload) to return it to stock first, then report the breakdown again");
        }

        Long actorUserId = inv.getContext() != null && inv.getContext().get("actorUserId") instanceof Number
                ? ((Number) inv.getContext().get("actorUserId")).longValue()
                : null;

        try {
            engine.cancel(instanceId, actorUserId, reason == null ? "vehicle breakdown" : "vehicle breakdown: " + reason);
        } catch (WorkflowInstanceNotFoundException e) {
            return ActionResult.error("workflow instance " + instanceId + " not found");
        }

        jdbc.update(
                "UPDATE gate_pass SET status = 'ABORTED', current_step = 'ABORTED', closed_at = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instanceId);

        writeAudit(tenantId, instanceId, workflowCode, currentState, actorUserId, reason);

        Map<String, Object> data = new HashMap<>();
        data.put("instanceId", instanceId);
        data.put("workflowCode", workflowCode);
        data.put("closedFromState", currentState);

        return ActionResult.builder()
                .ok(true)
                .message("vehicle breakdown reported — gate pass closed")
                .data(data)
                .build();
    }

    private void writeAudit(long tenantId, Long instanceId, String workflowCode, String fromState,
                             Long actorUserId, String reason) {
        try {
            jdbc.update(
                    "INSERT INTO m0_audit_log (tenant_id, ts, actor_id, action_kind, entity_code, "
                            + "workflow_code, workflow_instance_id, vehicle_no, gp_number, from_state, to_state, message) "
                            + "SELECT ?, CURRENT_TIMESTAMP, ?, 'VEHICLE_BREAKDOWN_REPORTED', 'GatePass', "
                            + "?, ?, gp.vehicle_no, gp.gp_number, ?, 'ABORTED', ? "
                            + "FROM gate_pass gp WHERE gp.tenant_id = ? AND gp.workflow_instance_id = ?",
                    tenantId, actorUserId, workflowCode, instanceId, fromState,
                    reason == null || reason.isBlank() ? "vehicle breakdown reported" : reason,
                    tenantId, instanceId);
        } catch (Exception e) {
            // Audit-log write must never break the breakdown close itself.
        }
    }

    private static String strOpt(Map<String, Object> bag, String... keys) {
        for (String key : keys) {
            Object v = bag.get(key);
            if (v != null && !v.toString().isBlank()) return v.toString();
        }
        return null;
    }
}
