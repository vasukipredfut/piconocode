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
 * Parking bay in/out log (dispatch_parking_bay_entry) driven by the
 * pico:parking-scan page's Direction field. Kept as its own listener,
 * separate from WorkflowProjectionListener, because the write here is
 * genuinely conditional on Direction (IN inserts a new row with in_time;
 * OUT updates the matching open row's out_time) — the generic
 * config-driven projection can only map submitted fields onto columns
 * unconditionally, it can't express "only touch this column sometimes."
 *
 * <p>Vehicle/flow-variant/loading-type context is looked up from gate_pass
 * by gp_number (server-side), not read off the submitted form bag — the
 * page only submits gp_number + direction, and gate_pass already has
 * those columns from when the pass was created.
 *
 * <p>Scoped to M10.TankerInbound + a submitted "direction" key so it never
 * reacts to unrelated transitions (e.g. an entity with its own unrelated
 * "direction" field is never routed through workflow.transition, but the
 * workflow-code check is cheap insurance regardless).
 */
@Component
public class ParkingBayEntryTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ParkingBayEntryTransitionListener.class);

    private final JdbcTemplate jdbc;

    public ParkingBayEntryTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null || !"M10.TankerInbound".equals(instance.getWorkflowCode())) return;
        String direction = str(bag, "direction");
        if (direction == null || direction.isBlank()) return;
        String gateNo = firstNonBlank(str(bag, "gp_number"), str(bag, "gpNumber"), str(bag, "gateNo"));
        if (gateNo == null || gateNo.isBlank()) return;
        Long tenantId = TenantContext.get();
        try {
            if ("OUT".equalsIgnoreCase(direction)) {
                jdbc.update(
                    "UPDATE dispatch_parking_bay_entry SET out_time=CURRENT_TIMESTAMP " +
                    "WHERE tenant_id=? AND gate_pass_no=? AND out_time IS NULL " +
                    "ORDER BY in_time DESC LIMIT 1",
                    tenantId, gateNo);
            } else if ("IN".equalsIgnoreCase(direction)) {
                Map<String, Object> gp;
                try {
                    gp = jdbc.queryForMap(
                        "SELECT vehicle_no, flow_variant_code, loading_type FROM gate_pass " +
                        "WHERE tenant_id=? AND gp_number=?", tenantId, gateNo);
                } catch (Exception e) {
                    log.warn("parking bay in: gate pass {} not found ({})", gateNo, e.getMessage());
                    return;
                }
                jdbc.update(
                    "INSERT INTO dispatch_parking_bay_entry " +
                    "(tenant_id, gate_pass_no, vehicle_no, flow_variant_code, loading_type, in_time) " +
                    "VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)",
                    tenantId, gateNo, gp.get("vehicle_no"), gp.get("flow_variant_code"), gp.get("loading_type"));
            }
        } catch (Exception e) {
            log.warn("parking bay entry log failed for gate pass {} direction {}: {}", gateNo, direction, e.getMessage());
        }
    }

    private static String str(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
