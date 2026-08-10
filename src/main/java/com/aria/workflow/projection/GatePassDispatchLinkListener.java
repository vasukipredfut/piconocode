package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Links the just-started workflow instance back onto the {@code gate_pass}
 * row that was saved earlier, independently of the workflow, via
 * aria:front-gate-create's plain {@code save} action (no Flow Variant picked
 * there — see V222__un_loading_flows_dispatch_entry_start.sql). For
 * M10.TankerInbound / M10.PackUnload / M10.ReturnStockIn, the workflow now
 * starts directly at aria:dispatch-vehicle-details, so there is no
 * GATE_PASS_CREATED transition left to create the gate_pass row the way
 * WorkflowProjectionListener normally would (insert-on-first-transition,
 * keyed by workflow_instance_id).
 *
 * <p>Without this, WorkflowProjectionListener's own gate_pass projection
 * would find no row keyed by this brand-new instance id and INSERT a second,
 * disconnected one — orphaning the row that already has gp_number,
 * loading_type, vehicle_category, etc. This listener finds that pre-existing
 * unlinked row (by vehicle_no, the one field both pages share) and backfills
 * workflow_instance_id onto it BEFORE the generic listener runs, so its
 * exists-check finds the row and takes the UPDATE branch instead.
 *
 * <p>Also sets flow_variant_code explicitly: WorkflowProjectionListener's
 * variant_column handling only fires on its INSERT branch (see
 * WorkflowProjectionListener.project()), never on UPDATE — so without this,
 * flow_variant_code would stay NULL forever even though a workflow clearly
 * started. Does NOT touch gp_number — {@link com.aria.action.handler.GatePassCreateActionHandler}
 * already stamped a permanent one from the per-tenant sequence at save time,
 * and the operator needs that same number to stay on the printed slip.
 *
 * <p>{@code @Order(HIGHEST_PRECEDENCE)} so this runs before
 * WorkflowProjectionListener in WorkflowEngineService's
 * {@code List<WorkflowTransitionListener>} autowiring — otherwise the generic
 * listener's exists-check could race ahead and insert the duplicate this
 * listener exists to prevent.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatePassDispatchLinkListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(GatePassDispatchLinkListener.class);
    private static final Set<String> WORKFLOW_CODES =
            Set.of("M10.TankerInbound", "M10.PackUnload", "M10.ReturnStockIn");

    private final JdbcTemplate jdbc;

    public GatePassDispatchLinkListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null || !"DRAFT".equals(previousState) || !WORKFLOW_CODES.contains(instance.getWorkflowCode())) {
            return;
        }
        try {
            Map<String, Object> page = pageData(bag, "aria:dispatch-vehicle-details");
            String vehicleNo = str(page, "vehicleNo");
            if (vehicleNo == null) return;

            Long tenantId = TenantContext.get();
            Long instanceId = instance.getId();
            Long gatePassId = jdbc.query(
                    "SELECT id FROM gate_pass WHERE tenant_id=? AND vehicle_no=? " +
                    "AND workflow_instance_id IS NULL " +
                    "ORDER BY created_at DESC LIMIT 1",
                    rs -> rs.next() ? rs.getLong(1) : null,
                    tenantId, vehicleNo);
            if (gatePassId == null) return;

            jdbc.update(
                    "UPDATE gate_pass SET workflow_instance_id=?, flow_variant_code=? " +
                    "WHERE id=? AND tenant_id=? AND workflow_instance_id IS NULL",
                    instanceId, instance.getWorkflowCode(), gatePassId, tenantId);
        } catch (Exception e) {
            log.warn("gate pass dispatch link failed for instance={}: {}", instance.getId(), e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> pageData(Map<String, Object> bag, String pageCode) {
        Object v = bag.get(pageCode);
        return v instanceof Map ? (Map<String, Object>) v : Map.of();
    }

    private static String str(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        return (v == null || v.toString().isBlank()) ? null : String.valueOf(v);
    }
}
