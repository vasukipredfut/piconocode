package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

/**
 * Sealing start/finish timestamps for aria:sealing-process (sealing_process_detail,
 * V85). Kept as its own listener, separate from WorkflowProjectionListener,
 * because the generic config-driven projection can only copy submitted-bag
 * values onto columns — it has no way to look up workflow_event history,
 * which is what "when did sealing start" actually means here.
 *
 * <p>The page only has one submission ("Sealing Done" — no separate "start"
 * step, no extra workflow state): sealing_finished_at is simply now; sealing_started_at
 * is backfilled as the timestamp this instance entered {@code previousState}
 * (the state right before SEALING_DONE — WEIGHMENT_2 today) i.e. "when the
 * vehicle became available for sealing". sealing_status is always stamped
 * 'COMPLETED' — reaching this listener means the operator submitted Sealing
 * Done, there's no other value it could be.
 *
 * <p>Upserts (INSERT ... ON DUPLICATE KEY UPDATE) rather than assuming
 * WorkflowProjectionListener already created the row — bean invocation order
 * across WorkflowTransitionListener implementations isn't guaranteed.
 */
@Component
public class SealingTimestampListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(SealingTimestampListener.class);
    private static final String SEALING_DONE = "SEALING_DONE";

    private final JdbcTemplate jdbc;

    public SealingTimestampListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!SEALING_DONE.equals(instance.getCurrentState())) return;
        Long tenantId = TenantContext.get();
        Long instanceId = instance.getId();
        try {
            Timestamp startedAt = lookupStateEntryTs(instanceId, tenantId, previousState);
            Timestamp finishedAt = Timestamp.from(Instant.now());
            jdbc.update(
                "INSERT INTO sealing_process_detail " +
                "(tenant_id, workflow_instance_id, flow_variant_code, current_step, sealing_status, sealing_started_at, sealing_finished_at) " +
                "VALUES (?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "flow_variant_code=VALUES(flow_variant_code), current_step=VALUES(current_step), " +
                "sealing_status=VALUES(sealing_status), " +
                "sealing_started_at=COALESCE(sealing_process_detail.sealing_started_at, VALUES(sealing_started_at)), " +
                "sealing_finished_at=VALUES(sealing_finished_at)",
                tenantId, instanceId, instance.getWorkflowCode(), instance.getCurrentState(),
                "COMPLETED", startedAt, finishedAt);
        } catch (Exception e) {
            log.warn("sealing timestamp stamp failed for instance={}: {}", instanceId, e.getMessage());
        }
    }

    /** The most recent time this instance transitioned INTO {@code state}, or now if there's no such event. */
    private Timestamp lookupStateEntryTs(Long instanceId, Long tenantId, String state) {
        try {
            return jdbc.queryForObject(
                "SELECT ts FROM workflow_event WHERE instance_id=? AND tenant_id=? " +
                "AND kind='STATE_TRANSITION' AND to_state=? ORDER BY ts DESC LIMIT 1",
                Timestamp.class, instanceId, tenantId, state);
        } catch (Exception e) {
            return Timestamp.from(Instant.now());
        }
    }
}
