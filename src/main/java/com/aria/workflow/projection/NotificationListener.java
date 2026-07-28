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
            Long gatePassId = null;
            String vehicleNo = null;
            try {
                Map<String, Object> gp = jdbc.queryForMap(
                        "SELECT id, vehicle_no FROM gate_pass WHERE workflow_instance_id = ?",
                        instance.getId());
                gatePassId = ((Number) gp.get("id")).longValue();
                vehicleNo = (String) gp.get("vehicle_no");
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
}
