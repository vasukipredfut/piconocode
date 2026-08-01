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
 * Captures aria:qc-approval's literal decision ("APPROVED" / "REJECTED") into
 * qc_preload_detail.qc_decision, plus qc_reject_reason (only present when
 * REJECTED) and unloading_location (only present when item_category ==
 * RETURN or the flow is M10.PackUnload), for M10.TankerInbound and
 * M10.ReturnStockIn (both reach aria:qc-approval from DISPATCH_QUEUED) and
 * M10.PackUnload (reaches it from DISPATCH_VEHICLE_ENTRY instead).
 *
 * <p>Kept OUT of WorkflowProjectionListener's generic field_map_json on
 * purpose, same reasoning as {@link PmtSharedReasonListener}: aria:qc-approval's
 * "status" field name is reused later in this workflow by
 * aria:manager-qty-approval and aria:manager-weight-approval, and
 * WorkflowProjectionListener.pluck() searches every page's submitted data
 * (a plain HashMap, undefined iteration order) for a bare key match — it
 * would risk grabbing a later manager-approval's "status" instead of
 * aria:qc-approval's own. Gating on the previousState the QC step actually
 * transitions from for each workflow (only aria:qc-approval fires from
 * those states) and reading the page's own bag entry directly avoids that
 * ambiguity entirely.
 */
@Component
public class QcApprovalDecisionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(QcApprovalDecisionListener.class);
    private static final Set<String> WORKFLOW_CODES = Set.of("M10.TankerInbound", "M10.ReturnStockIn", "M10.PackUnload");
    private static final Set<String> QC_PREVIOUS_STATES = Set.of("DISPATCH_QUEUED", "DISPATCH_VEHICLE_ENTRY");

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
            String unloadingLocation = str(page, "unloading_location");

            Long tenantId = TenantContext.get();
            jdbc.update(
                "INSERT INTO qc_preload_detail (tenant_id, workflow_instance_id, flow_variant_code, current_step, qc_decision, qc_reject_reason, unloading_location) " +
                "VALUES (?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE flow_variant_code=VALUES(flow_variant_code), " +
                "current_step=VALUES(current_step), qc_decision=VALUES(qc_decision), " +
                "qc_reject_reason=VALUES(qc_reject_reason), unloading_location=VALUES(unloading_location)",
                tenantId, instance.getId(), instance.getWorkflowCode(), instance.getCurrentState(),
                decision, rejectReason, unloadingLocation);
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
