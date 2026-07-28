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
 * Captures aria:manager-qty-approval's fields into its own dedicated table,
 * manager_qty_approval_detail (V90 — pre-existing, already used by
 * FGLoading/PackLoading/TankerOutbound via {@link PmtSharedReasonListener}),
 * for M10.TankerInbound: manager_decision/manager_qty_reason (columns that
 * already existed) plus invoice_qty/unloading_qty/grn_status (new,
 * TankerInbound-only).
 *
 * <p>Kept OUT of WorkflowProjectionListener's generic field_map_json on
 * purpose, same reasoning as {@link ReconciliationDecisionListener} and
 * {@link QcApprovalDecisionListener}: "variance_reason" and "status" are
 * both reused by aria:reconciliation earlier in this same workflow (and by
 * aria:manager-weight-approval later), so WorkflowProjectionListener.pluck()
 * searching every page's submitted data (a plain HashMap, undefined
 * iteration order) would risk grabbing the wrong page's value. Gating on
 * {@code previousState == QTY_PENDING_MANAGER} (only aria:manager-qty-approval
 * fires from that state in this workflow) and reading the page's own bag
 * entry directly avoids that ambiguity.
 *
 * <p>Note: PmtSharedReasonListener also reacts to
 * {@code previousState == WEIGHT_DIFF_PENDING_MANAGER} for every workflow
 * (no workflow_code guard there), writing this same table from
 * aria:manager-qty-approval's bag entry. In M10.TankerInbound,
 * WEIGHT_DIFF_PENDING_MANAGER is a later, unrelated state (leads to
 * aria:manager-weight-approval instead) — when a TankerInbound instance
 * reaches it, PmtSharedReasonListener re-fires and re-writes this table with
 * the same (already-stale but unchanged) values this listener wrote earlier.
 * Harmless re-write, not a correctness bug, but worth knowing about.
 */
@Component
public class ManagerQtyApprovalDecisionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ManagerQtyApprovalDecisionListener.class);

    private final JdbcTemplate jdbc;

    public ManagerQtyApprovalDecisionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null || !"QTY_PENDING_MANAGER".equals(previousState)
                || !"M10.TankerInbound".equals(instance.getWorkflowCode())) {
            return;
        }
        try {
            Map<String, Object> page = pageData(bag, "aria:manager-qty-approval");
            Map<String, Object> cols = new LinkedHashMap<>();
            putIfPresent(cols, "invoice_qty", page, "invoice_qty_display");
            putIfPresent(cols, "unloading_qty", page, "unloading_qty_display");
            putIfPresent(cols, "manager_qty_reason", page, "variance_reason");
            putIfPresent(cols, "manager_decision", page, "status");
            putIfPresent(cols, "grn_status", page, "grn_status");
            if (cols.isEmpty()) return;

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
                "INSERT INTO manager_qty_approval_detail (" + colNames + ") VALUES (" + placeholders + ") " +
                    "ON DUPLICATE KEY UPDATE " + updates,
                args);
        } catch (Exception e) {
            log.warn("manager qty approval capture failed for instance={}: {}", instance.getId(), e.getMessage());
        }
    }

    private static void putIfPresent(Map<String, Object> cols, String column, Map<String, Object> page, String key) {
        String v = str(page, key);
        if (v != null) cols.put(column, v);
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
