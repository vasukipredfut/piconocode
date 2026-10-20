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
 * already existed) plus invoice_qty/unloading_qty/grn_status (TankerInbound-
 * only) and time_taken (V266 — shared with every other workflow via {@link
 * PmtSharedReasonListener}'s own manager-qty-approval cases).
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
        // aria:manager-qty-approval is submitted from WEIGHMENT_2 on the first pass (the tolerance routing: within -> document
        // verification, outside -> QTY_PENDING_MANAGER) and again from QTY_PENDING_MANAGER when the manager decides. Only that
        // page has a transition out of either state in M10.TankerInbound, so the previous state alone identifies it.
        if (bag == null || !("QTY_PENDING_MANAGER".equals(previousState) || "WEIGHMENT_2".equals(previousState))
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
            // Invoice - Unloading (the page's own "Difference"), plus the tolerance figures the page shows: expected / loaded
            // weight, the variance % and the within-tolerance verdict (columns of V263).
            putIfPresent(cols, "qty_difference", page, "qty_difference_display");
            putIfPresent(cols, "expected_weight_kg", page, "expected_weight_kg");
            putIfPresent(cols, "loaded_weight_kg", page, "loaded_weight_kg");
            putIfPresent(cols, "pmt_variance_pct", page, "pmt_variance_pct");
            String within = firstNonBlank(str(page, "within_tolerance"), str(page, "withinTolerance"));
            if (within != null) cols.put("pmt_within_tolerance", within);
            // pmt_variance_pct holds the ACTUAL difference in kg here too (loaded - expected; negative = short): the page's own
            // figure is saved as sent (above); when it sends none, it is worked out from the two weights.
            if (cols.get("pmt_variance_pct") == null && cols.get("expected_weight_kg") != null && cols.get("loaded_weight_kg") != null) {
                try {
                    cols.put("pmt_variance_pct", new java.math.BigDecimal(String.valueOf(cols.get("loaded_weight_kg")))
                            .subtract(new java.math.BigDecimal(String.valueOf(cols.get("expected_weight_kg")))));
                } catch (NumberFormatException ignored) { /* not numbers: leave it out */ }
            }
            // manager_name (V358): the approving manager's name, sent by the UI.
            String managerName = firstNonBlank(str(page, "manager_name"), str(page, "managerName"));
            if (managerName != null) cols.put("manager_name", managerName);
            String managerContact = firstNonBlank(str(page, "manager_contact"), str(page, "managerContact"),
                    str(page, "manager_contact_no"), str(page, "managerContactNo"));
            if (managerContact != null) cols.put("manager_contact", managerContact);
            putIfPresent(cols, "time_taken", page, "time_taken");
            // queue_reason (V290): every other workflow's own manager-qty-
            // approval capture (PmtSharedReasonListener) has always saved this;
            // M10.TankerInbound's own capture here never did, silently dropping
            // an out-of-order-pick reason whenever a manager skipped a priority
            // vehicle for this specific workflow.
            String queueReason = firstNonBlank(str(page, "queue_reason"), str(page, "queueReason"));
            if (queueReason != null) cols.put("queue_reason", queueReason);
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

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
