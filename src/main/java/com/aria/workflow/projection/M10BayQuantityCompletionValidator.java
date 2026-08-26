package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionValidator;
import com.aria.workflow.engine.WorkflowValidationException;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * A single loading transaction on aria:loading-in-progress-entry (page 428)
 * can span several bays — Bay 1 = 100, Bay 2 = 200, Bay 3 = 500 (see
 * {@code M10StartBayActionHandler} / {@code M10BayCompleteActionHandler},
 * recorded in {@code loading_progress_detail_bay}, V179). quantity vs
 * processed_quantity are tracked per bay (and rolled up onto
 * loading_progress_detail, V177) purely for visibility — "Completed" always
 * proceeds regardless of whether the cumulative processed total reached the
 * cumulative target; there is no reason/override needed because there is no
 * quantity gate at all.
 *
 * <p>The only thing this still blocks is an open bay: every bay row must be
 * closed via Bay Complete before Completed, since a bay left IN_PROGRESS
 * forever would be an orphaned row with no processed_quantity recorded.
 *
 * <p>Scoped to WEIGHMENT_2/RELOAD_WEIGHMENT_2 AND FGLoading/PackLoading only
 * — TankerOutbound also uses page 428's bay tracking but is explicitly
 * exempted from this gate per business request. WEIGHMENT_2 is a common
 * state name reused by unrelated workflows (ReturnStockIn, TankerInbound,
 * ...; see V133) that have nothing to do with bay tracking, so both checks
 * are needed, not just the toState name. RELOAD_WEIGHMENT_2 is the
 * RELOAD_LOADING_PLANNING sub-cycle's counterpart to WEIGHMENT_2 (reached via
 * LOADING_COMPLETED_R, not LOADING_COMPLETED) — without it a reload's
 * "Completed" could proceed to re-weighing with a bay still open.
 * Instances that never used Start Bay (zero loading_progress_detail_bay
 * rows) are left alone entirely — this feature is opt-in per vehicle, not
 * mandatory for every gate pass.
 */
@Component
public class M10BayQuantityCompletionValidator implements WorkflowTransitionValidator {

    private static final Set<String> SCOPED_WORKFLOWS =
            Set.of("M10.FGLoading", "M10.PackLoading");
    private static final Set<String> GATED_TO_STATES =
            Set.of("WEIGHMENT_2", "RELOAD_WEIGHMENT_2");

    private final JdbcTemplate jdbc;

    public M10BayQuantityCompletionValidator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void validate(WorkflowInstance instance, String toState, Map<String, Object> formData) {
        if (!GATED_TO_STATES.contains(toState)) return;
        if (!SCOPED_WORKFLOWS.contains(instance.getWorkflowCode())) return;

        long tenantId = TenantContext.get();
        Integer openCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'IN_PROGRESS'",
                Integer.class, tenantId, instance.getId());
        if (openCount != null && openCount > 0) {
            throw new WorkflowValidationException(
                    openCount + " bay(s) still open — close them with Bay Complete before Completed");
        }
    }
}
