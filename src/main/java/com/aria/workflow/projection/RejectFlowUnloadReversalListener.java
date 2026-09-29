package com.aria.workflow.projection;

import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Reject Flow Unloading (aria:reject-flow-unloading, M10.StockOutward's
 * REJECT_FLOW_UNLOADING -> UNLOADING_IN_PROGRESS / UNLOADING_BAY_ASSIGNED
 * -> UNLOADING_COMPLETED sub-cycle, V328): once the workflow reaches
 * UNLOADING_COMPLETED, every {@code loading_progress_detail_bay} row for
 * this instance that Reject Flow Unload was reversing is flipped straight
 * to {@code REVERSED} here, rather than relying solely on
 * {@code m10.unload-bay-complete} (Stop Bay) to have already gotten every
 * line there via its own per-line quantity bookkeeping. This is the
 * authoritative "the operator says unloading is done" signal for this
 * sub-cycle -- whatever Stop Bay left short of REVERSED (still DONE or
 * UNLOAD_IN_PROGRESS) is reversed unconditionally at this point.
 *
 * <p>Self-scoping: a workflow instance that was never loaded via
 * m10.start-bay/m10.bay-complete in the first place (M10.ReturnStockIn,
 * M10.StockInward, M10.PackUnload's own receiving-side UNLOADING_COMPLETED,
 * etc. -- see {@code M10StartUnloadingBayActionHandler}'s class javadoc: no
 * prior loading_progress_detail_bay row required for those) simply has no
 * matching rows here, so this UPDATE is a no-op for them regardless of
 * workflow_code -- no workflow-code allowlist needed.
 */
@Component
public class RejectFlowUnloadReversalListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(RejectFlowUnloadReversalListener.class);
    private static final String TARGET_STATE = "UNLOADING_COMPLETED";

    private final JdbcTemplate jdbc;

    public RejectFlowUnloadReversalListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> submittedDataBag) {
        if (instance == null || !TARGET_STATE.equals(instance.getCurrentState())) return;

        int reversed = jdbc.update(
                "UPDATE loading_progress_detail_bay SET status = 'REVERSED' "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND status <> 'REVERSED'",
                instance.getTenantId(), instance.getId());
        if (reversed > 0) {
            jdbc.update(
                    "UPDATE loading_progress_detail SET status = 'REVERSED' WHERE tenant_id = ? AND workflow_instance_id = ?",
                    instance.getTenantId(), instance.getId());
            log.info("workflow.reject-flow-unload-reversal tenantId={} instanceId={} rowsReversed={}",
                    instance.getTenantId(), instance.getId(), reversed);
        }
    }
}
