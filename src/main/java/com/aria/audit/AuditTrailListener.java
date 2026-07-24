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
 * <p>vehicle_no/gp_number are also stamped onto the row from the instance's
 * {@code business_ref}, so the Audit Tracker Group-By feature can group
 * directly on this table with no joins. business_ref has two conventions in
 * this codebase: {@code GenericEntityController.startWorkflowFor} sets it to
 * "EntityCode#id" (resolved here by looking up gate_pass by id when the code
 * is GatePass); the M10 dispatch flow instead starts workflows with
 * business_ref set directly to the vehicle registration number (confirmed
 * against live {@code workflow_instance} rows, e.g. workflow_code
 * "M10.PackLoading" with business_ref "TN45CM4589") — that convention is
 * treated as the vehicle_no itself, with gp_number resolved best-effort as
 * the most recently created gate_pass for that vehicle.
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

            Map<String, Object> gatePassFields = resolveGatePassFields(instance.getBusinessRef());

            jdbc.update(
                    "INSERT INTO m0_audit_log (tenant_id, action_kind, entity_code, entity_id, " +
                            "vehicle_no, gp_number, " +
                            "workflow_code, workflow_instance_id, from_state, to_state, message, payload_json) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    instance.getTenantId() == null ? 1L : instance.getTenantId(),
                    "workflow.transition",
                    instance.getWorkflowCode(),       // entity_code: WF code (no business_entity field on instance)
                    instance.getId(),                  // entity_id: instance id
                    gatePassFields.get("vehicle_no"),
                    gatePassFields.get("gp_number"),
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

    private Map<String, Object> resolveGatePassFields(String businessRef) {
        if (businessRef == null || businessRef.isBlank()) return Map.of();
        int hash = businessRef.indexOf('#');
        if (hash >= 0) {
            // GenericEntityController convention: "EntityCode#id".
            if (!"GatePass".equals(businessRef.substring(0, hash))) return Map.of();
            try {
                Long id = Long.valueOf(businessRef.substring(hash + 1));
                return jdbc.queryForMap("SELECT vehicle_no, gp_number FROM gate_pass WHERE id = ?", id);
            } catch (Exception e) {
                return Map.of();
            }
        }
        // M10 convention: business_ref IS the vehicle registration number.
        // Stamp it directly (even before any gate_pass row exists for it) and
        // best-effort attach the most recent matching gate_pass's gp_number.
        Map<String, Object> fields = new java.util.HashMap<>();
        fields.put("vehicle_no", businessRef);
        try {
            String gpNumber = jdbc.queryForObject(
                    "SELECT gp_number FROM gate_pass WHERE vehicle_no = ? ORDER BY created_at DESC LIMIT 1",
                    String.class, businessRef);
            fields.put("gp_number", gpNumber);
        } catch (Exception ignored) {
            // No gate_pass row yet for this vehicle — vehicle_no still gets recorded.
        }
        return fields;
    }
}
