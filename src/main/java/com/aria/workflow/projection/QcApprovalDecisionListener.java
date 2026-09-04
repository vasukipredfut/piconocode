package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Captures aria:qc-approval's literal decision ("APPROVED" / "REJECTED") plus
 * qc_reject_reason (only present when REJECTED) into
 * qc_unloading_approval.status, for M10.TankerInbound (reaches
 * aria:qc-approval from DISPATCH_QUEUED), M10.ReturnStockIn (reaches it from
 * RETURN_STOCK_OFFICER -- QC Approval sits after the first weighment +
 * Return Stock Officer capture for this flow variant only, see the tenant-1
 * M10.ReturnStockIn workflow_definition), and M10.PackUnload (reaches it
 * from DISPATCH_VEHICLE_ENTRY).
 *
 * <p>Kept OUT of WorkflowProjectionListener's generic field_map_json on
 * purpose, same reasoning as {@link PmtSharedReasonListener}: aria:qc-approval's
 * "status" field name is reused later in this workflow by
 * aria:manager-qty-approval and aria:manager-weight-approval, and
 * WorkflowProjectionListener re-evaluates every configured field against the
 * whole accumulated submitted-data bag on EVERY later transition — a generic
 * mapping would get silently overwritten by a manager-approval's "status" on
 * a later step, corrupting the original QC decision. Reading the page's own
 * bag entry directly (not a bag-wide key search) avoids that ambiguity
 * entirely.
 *
 * <p>Does its own exists-check-then-insert-or-update (mirroring
 * WorkflowProjectionListener.project()) rather than INSERT ... ON DUPLICATE
 * KEY UPDATE, since qc_unloading_approval has no unique key on
 * (tenant_id, workflow_instance_id) — only a plain index — so ON DUPLICATE
 * KEY would never actually match and would insert a fresh row every time.
 * This also makes the write correct regardless of whether this listener or
 * WorkflowProjectionListener (which owns every other qc_unloading_approval
 * column) runs first within the same transition's listener dispatch.
 */
@Component
public class QcApprovalDecisionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(QcApprovalDecisionListener.class);
    private static final Set<String> WORKFLOW_CODES = Set.of("M10.TankerInbound", "M10.ReturnStockIn", "M10.PackUnload", "M10.FGUnloading");
    private static final Set<String> QC_PREVIOUS_STATES = Set.of("DISPATCH_QUEUED", "DISPATCH_VEHICLE_ENTRY", "RETURN_STOCK_OFFICER");

    private final JdbcTemplate jdbc;

    public QcApprovalDecisionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null || !QC_PREVIOUS_STATES.contains(previousState)
                || !WORKFLOW_CODES.contains(instance.getWorkflowCode())) {
            return;
        }
        try {
            Map<String, Object> page = pageData(bag, "aria:qc-approval");
            String decision = str(page, "status");
            if (decision == null) return;
            String rejectReason = str(page, "qc_reject_reason");

            Long tenantId = TenantContext.get();
            Long instanceId = instance.getId();
            Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM qc_unloading_approval WHERE workflow_instance_id=? AND tenant_id=?",
                Integer.class, instanceId, tenantId);
            if (n != null && n > 0) {
                jdbc.update(
                    "UPDATE qc_unloading_approval SET current_step=?, status=?, qc_reject_reason=? " +
                    "WHERE workflow_instance_id=? AND tenant_id=?",
                    instance.getCurrentState(), decision, rejectReason, instanceId, tenantId);
            } else {
                jdbc.update(
                    "INSERT INTO qc_unloading_approval (tenant_id, workflow_instance_id, flow_variant_code, current_step, status, qc_reject_reason) " +
                    "VALUES (?,?,?,?,?,?)",
                    tenantId, instanceId, instance.getWorkflowCode(), instance.getCurrentState(), decision, rejectReason);
            }
        } catch (Exception e) {
            log.warn("qc decision capture failed for instance={}: {}", instance.getId(), e.getMessage());
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
