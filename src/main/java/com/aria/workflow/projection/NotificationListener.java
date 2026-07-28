package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Every transition that lands an instance on a new assigned role is "new
 * pending work" for whoever owns that role — save one row to m0_notification
 * (V141) so the role has an actual notification to read, not just a queue
 * count to poll. Unlike the other listeners in this package, this one is
 * NOT gated to a specific workflow_code/previousState — "new pending item
 * for my role" applies uniformly across every M10 flow. Terminal transitions
 * (assignedRole null) are skipped: nobody is waiting on a closed instance.
 *
 * <p>payload_json is the accumulated submitted-data bag (same data
 * WorkflowQueueItem.submittedData already exposes) — the request context
 * the new owner needs, snapshotted at the moment the work became theirs.
 *
 * <p>vehicle_no is read out of that same bag (scanning every page's
 * submitted fields for "vehicle_no"/"vehicleNo") rather than joined from
 * gate_pass: this listener runs inside the same transaction and same
 * transitionListeners loop as the GatePass-creation listener, with no
 * guaranteed ordering between them — on the very first transition
 * (DRAFT -> GATE_PASS_CREATED, the transition that creates the gate_pass row
 * in the first place) a gate_pass join can race and come back empty. The
 * bag already carries vehicle_no from the moment it was first submitted on
 * aria:front-gate-create, so reading it from there is race-free. gate_pass_id
 * still comes from a best-effort join (harmless to leave null on that first
 * transition; populates correctly on every later one, once the row exists
 * from an earlier, already-committed transaction).
 */
@Component
public class NotificationListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JdbcTemplate jdbc;

    public NotificationListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        String role = instance.getAssignedRole();
        if (role == null || role.isBlank()) return;
        try {
            String vehicleNo = vehicleNoFromBag(bag);

            Long gatePassId = null;
            try {
                Map<String, Object> gp = jdbc.queryForMap(
                        "SELECT id, vehicle_no FROM gate_pass WHERE workflow_instance_id = ?",
                        instance.getId());
                gatePassId = ((Number) gp.get("id")).longValue();
                if (vehicleNo == null) vehicleNo = (String) gp.get("vehicle_no");
            } catch (EmptyResultDataAccessException ignored) {
                // Not every workflow has a gate_pass row — notification still saves without it.
            }

            String payload = MAPPER.writeValueAsString(bag == null ? Map.of() : bag);
            Long tenantId = TenantContext.get();
            jdbc.update(
                    "INSERT INTO m0_notification " +
                    "(tenant_id, workflow_instance_id, gate_pass_id, vehicle_no, assigned_role, payload_json) " +
                    "VALUES (?,?,?,?,?,?)",
                    tenantId, instance.getId(), gatePassId, vehicleNo, role, payload);
        } catch (Exception e) {
            log.warn("notification capture failed for instance={}: {}", instance.getId(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static String vehicleNoFromBag(Map<String, Object> bag) {
        if (bag == null) return null;
        for (Object v : bag.values()) {
            if (!(v instanceof Map)) continue;
            Map<String, Object> page = (Map<String, Object>) v;
            Object val = page.get("vehicle_no");
            if (val == null) val = page.get("vehicleNo");
            if (val != null && !val.toString().isBlank()) return String.valueOf(val);
        }
        return null;
    }
}
