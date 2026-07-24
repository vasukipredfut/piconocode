package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Captures aria:reconciliation's variance_reason and withinTolerance decision
 * into its own dedicated table, unload_reconciliation_detail, for
 * M10.TankerInbound.
 *
 * <p>Kept OUT of WorkflowProjectionListener's generic field_map_json on
 * purpose, same reasoning as {@link PmtSharedReasonListener} and
 * {@link QcApprovalDecisionListener}: "variance_reason" is reused later in
 * this same workflow by aria:manager-qty-approval and
 * aria:manager-weight-approval, and WorkflowProjectionListener.pluck()
 * searches every page's submitted data (a plain HashMap, undefined iteration
 * order) for a bare key match — it would risk grabbing one of those later
 * pages' variance_reason instead of aria:reconciliation's own. Gating on
 * {@code previousState == WEIGHMENT_2} (only aria:reconciliation fires from
 * that state in this workflow) and reading the page's own bag entry directly
 * avoids that ambiguity entirely.
 *
 * <p>Unlike gate_pass (which always pre-exists by this point), this table has
 * no row yet the first time — INSERT ... ON DUPLICATE KEY UPDATE, keyed on
 * the table's (tenant_id, workflow_instance_id) unique constraint.
 */
@Component
public class ReconciliationDecisionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationDecisionListener.class);

    private final JdbcTemplate jdbc;

    public ReconciliationDecisionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null || !"WEIGHMENT_2".equals(previousState)
                || !"M10.TankerInbound".equals(instance.getWorkflowCode())) {
            return;
        }
        try {
            Map<String, Object> page = pageData(bag, "aria:reconciliation");
            String reason = str(page, "variance_reason");
            String withinTolerance = str(page, "withinTolerance");
            if (reason == null && withinTolerance == null) return;

            Map<String, Object> cols = new LinkedHashMap<>();
            if (reason != null) cols.put("variance_reason", reason);
            if (withinTolerance != null) cols.put("within_tolerance", withinTolerance);

            Long tenantId = TenantContext.get();
            StringBuilder colNames = new StringBuilder("tenant_id, workflow_instance_id, flow_variant_code, current_step");
            StringBuilder placeholders = new StringBuilder("?,?,?,?");
            StringBuilder updates = new StringBuilder("flow_variant_code=VALUES(flow_variant_code), current_step=VALUES(current_step)");
            Object[] args = new Object[4 + cols.size()];
            args[0] = tenantId;
            args[1] = instance.getId();
            args[2] = instance.getWorkflowCode();
            args[3] = instance.getCurrentState();
            int i = 4;
            for (Map.Entry<String, Object> e : cols.entrySet()) {
                colNames.append(", ").append(e.getKey());
                placeholders.append(",?");
                updates.append(", ").append(e.getKey()).append("=VALUES(").append(e.getKey()).append(")");
                args[i++] = e.getValue();
            }
            jdbc.update(
                "INSERT INTO unload_reconciliation_detail (" + colNames + ") VALUES (" + placeholders + ") " +
                    "ON DUPLICATE KEY UPDATE " + updates,
                args);
        } catch (Exception e) {
            log.warn("reconciliation decision capture failed for instance={}: {}", instance.getId(), e.getMessage());
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
