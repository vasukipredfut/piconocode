package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code m10.reject-flow-unload-complete} — standalone "unloading completed"
 * signal for Reject Flow Unloading (vehicle breakdown), for a UI button that
 * isn't wired to {@code workflow.transition} (e.g. {@code aria:reject-flow-unloading}'s
 * "Unloading Completed" button, which today only does {@code page.open}).
 * Same effect as {@link com.aria.workflow.projection.RejectFlowUnloadReversalListener}
 * (which reacts when the workflow engine itself drives a transition into
 * {@code UNLOADING_COMPLETED}), exposed here directly so the frontend can
 * call it regardless of what that button's own action type ends up being.
 *
 * <p>Unconditionally marks every {@code loading_progress_detail_bay} row for
 * this instance {@code REVERSED} (and the parent {@code loading_progress_detail}
 * rollup alongside it) — does not require Stop Bay to have already gotten
 * every line there via its own per-line quantity bookkeeping, same
 * "operator says unloading is done" authority the FSM-driven listener has.
 * Does not touch {@code workflow_instance} or {@code gate_pass} itself —
 * purely the bay-reversal side effect.
 *
 * <pre>
 *   { "instanceId": 1495 }
 * </pre>
 */
@Component
public class M10RejectFlowUnloadCompleteActionHandler implements ActionHandler {

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.reject-flow-unload-complete"; }

    @Override
    @Transactional("m0PageTransactionManager")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();

        Integer exists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM workflow_instance WHERE id = ? AND tenant_id = ?",
                Integer.class, instanceId, tenantId);
        if (exists == null || exists == 0) {
            return ActionResult.error("workflow instance " + instanceId + " not found");
        }

        int reversed = jdbc.update(
                "UPDATE loading_progress_detail_bay SET status = 'REVERSED' "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND status <> 'REVERSED'",
                tenantId, instanceId);
        jdbc.update(
                "UPDATE loading_progress_detail SET status = 'REVERSED' WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instanceId);

        Map<String, Object> data = new HashMap<>();
        data.put("instanceId", instanceId);
        data.put("rowsReversed", reversed);

        return ActionResult.builder()
                .ok(true)
                .message(reversed + " loading bay row(s) marked REVERSED")
                .data(data)
                .build();
    }
}
