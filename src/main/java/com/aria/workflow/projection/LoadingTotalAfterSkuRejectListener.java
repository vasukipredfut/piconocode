package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * M10.TankerOutbound's aria:qc-final-out lets QC reject individual SKUs; the UI marks those
 * SKUs' {@code loading_progress_detail_bay} rows {@code REJECTED} through the generic entity API.
 * The parent {@code loading_progress_detail.total_quantity} was seeded as the sum of every bay row's
 * quantity (QcPreloadTankAllocationListener), so it still counts the rejected SKUs.
 *
 * <p>On {@code LOADING_COMPLETED -> WEIGHMENT_2} (QC Pass) this recomputes it as the sum of the
 * bay rows that are not {@code REJECTED}. A recompute, not a subtraction, so re-running it (or a
 * second QC Pass after a reload loop) always converges to the same value.
 */
@Component
public class LoadingTotalAfterSkuRejectListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(LoadingTotalAfterSkuRejectListener.class);
    private static final String WORKFLOW_CODE = "M10.TankerOutbound";
    private static final String FROM_STATE = "LOADING_COMPLETED";
    private static final String TO_STATE = "WEIGHMENT_2";

    private final JdbcTemplate jdbc;

    public LoadingTotalAfterSkuRejectListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode())) return;
        if (!FROM_STATE.equals(previousState) || !TO_STATE.equals(instance.getCurrentState())) return;

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();
        try {
            int n = jdbc.update(
                    "UPDATE loading_progress_detail SET total_quantity = ("
                            + "SELECT COALESCE(SUM(b.quantity), 0) FROM loading_progress_detail_bay b "
                            + "WHERE b.tenant_id = ? AND b.workflow_instance_id = ? AND b.status <> 'REJECTED') "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                    tenantId, instanceId, tenantId, instanceId);
            log.info("loading total after sku reject: instance={} parent rows updated={}", instanceId, n);
        } catch (Exception e) {
            log.warn("loading total recompute failed for instance={}: {}", instanceId, e.getMessage());
        }
    }
}
