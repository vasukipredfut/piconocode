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
 * M10.TankerOutbound: QC rejects the WHOLE vehicle on aria:qc-final-out ({@code LOADING_COMPLETED} with
 * {@code status = REJECTED}, whichever state the workflow routes it to). Whatever was never weighed
 * ({@code IN_PROGRESS} -- no stock issued) is flagged {@code REJECTED} so nothing is left open, and the parent
 * {@code loading_progress_detail.total_quantity} is recomputed without those rows. Rows already {@code DONE}
 * (weighed, stock issued) are left alone: they are what {@code m10.tanker-unload-capture} takes back off the
 * vehicle.
 */
@Component
public class QcSampleRejectListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(QcSampleRejectListener.class);
    private static final String WORKFLOW_CODE = "M10.TankerOutbound";
    private static final String FROM_STATE = "LOADING_COMPLETED";
    private static final String PAGE_CODE = "aria:qc-final-out";

    private final JdbcTemplate jdbc;

    public QcSampleRejectListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode()) || !FROM_STATE.equals(previousState)) return;
        if (bag == null || !(bag.get(PAGE_CODE) instanceof Map)) return;
        Object status = ((Map<String, Object>) bag.get(PAGE_CODE)).get("status");
        if (status == null || !"REJECTED".equalsIgnoreCase(String.valueOf(status).trim())) return;

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();
        try {
            int open = jdbc.update(
                    "UPDATE loading_progress_detail_bay SET status = 'REJECTED' "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'IN_PROGRESS'",
                    tenantId, instanceId);
            jdbc.update(
                    "UPDATE loading_progress_detail SET total_quantity = ("
                            + "SELECT COALESCE(SUM(b.quantity), 0) FROM loading_progress_detail_bay b "
                            + "WHERE b.tenant_id = ? AND b.workflow_instance_id = ? AND b.status <> 'REJECTED') "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                    tenantId, instanceId, tenantId, instanceId);
            log.info("qc-final-out full reject: instance={} unweighed rows set REJECTED={}", instanceId, open);
        } catch (Exception e) {
            log.warn("qc-final-out full reject cleanup failed for instance={}: {}", instanceId, e.getMessage());
        }
    }
}
