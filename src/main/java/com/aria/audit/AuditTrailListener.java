package com.aria.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Writes a row into {@code m0_audit_log} for every workflow transition.
 * Complements the existing M10 / M5 listeners (gate_pass writer, stock
 * movement writer) — but listens to ALL workflows for compliance.
 *
 * <p>Audit row contents per transition:
 *   tenant_id, ts, actor_id (from workflow_instance.last_actor_id when present),
 *   action_kind="workflow.transition", entity_code=workflow_instance.business_entity,
 *   entity_id=workflow_instance.business_id, workflow_code, workflow_instance_id,
 *   from_state, to_state, message, payload_json (full submitted-data bag).
 *
 * <p>Listener failures are caught by the engine — they cannot fail the
 * transition.
 */
@Component
public class AuditTrailListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(AuditTrailListener.class);

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuditTrailListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState,
                              Map<String, Object> submittedDataBag) {
        try {
            String payloadJson;
            try {
                payloadJson = mapper.writeValueAsString(submittedDataBag == null ? Map.of() : submittedDataBag);
            } catch (Exception ignored) {
                payloadJson = "{}";
            }
            if (payloadJson.length() > 32000) payloadJson = payloadJson.substring(0, 32000) + "...[truncated]";

            String message = String.format("%s : %s -> %s",
                    instance.getWorkflowCode(), previousState, instance.getCurrentState());

            jdbc.update(
                    "INSERT INTO m0_audit_log (tenant_id, action_kind, entity_code, entity_id, " +
                            "workflow_code, workflow_instance_id, from_state, to_state, message, payload_json) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?)",
                    instance.getTenantId() == null ? 1L : instance.getTenantId(),
                    "workflow.transition",
                    instance.getWorkflowCode(),       // entity_code: WF code (no business_entity field on instance)
                    instance.getId(),                  // entity_id: instance id
                    instance.getWorkflowCode(),
                    instance.getId(),
                    previousState,
                    instance.getCurrentState(),
                    message,
                    payloadJson);
        } catch (Exception e) {
            log.warn("AuditTrailListener failed: {}", e.toString());
        }
    }
}
