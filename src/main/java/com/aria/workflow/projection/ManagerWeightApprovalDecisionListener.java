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
 * Captures aria:manager-weight-approval's fields into its own dedicated
 * table, manager_weight_approval_detail (V91 — pre-existing, already used by
 * FGLoading/PackLoading/TankerOutbound via {@link PmtSharedReasonListener}),
 * for M10.TankerInbound: weight_decision/weight_variance_reason (columns
 * that already existed) plus weight_difference (new, TankerInbound-only —
 * the computed W2−W3 shown on the page, snapshotted). M10.ReturnStockIn has
 * no W3 (only 2 weighments), so its own computed field is a distinct
 * rsi_weight_difference_display (W1−W2, "expected" vs "second") — both map
 * to the same weight_difference column since the two workflows never fire
 * this listener on the same transition.
 *
 * <p>Also used by M10.ReturnStockIn, whose weight-diff step reaches this same
 * page too (its only weight-diff escalation, always required — no auto-pass
 * branch) — the two workflows share identical column semantics here, so one
 * listener covers both rather than duplicating it. The two workflows use
 * DIFFERENT previousState names though: TankerInbound's is
 * WEIGHT_DIFF_PENDING_MANAGER (post-3rd-weighment), ReturnStockIn's is
 * WEIGHMENT_2 (post-2nd-weighment, named to mirror WEIGHMENT_1 rather than
 * describe the pending-manager semantics, per explicit request to keep a
 * distinct "second weighment" checkpoint visible in the state list) — so the
 * gate below checks (workflowCode, previousState) as a pair, not one shared
 * state name.
 *
 * <p>M10.FGUnloading/M10.StockInward added later — this page was previously
 * uncaptured for both (a pre-existing gap, not introduced by StockInward
 * being cloned from FGUnloading). Both use TankerInbound's 3-weighment
 * column semantics (weight_difference_display = W2−W3), fire from either
 * WEIGHMENT_3 (first attempt) or WEIGHT_APPROVAL_PENDING (retry after a
 * REJECTED decision) — see the gate below.
 *
 * <p>Kept OUT of WorkflowProjectionListener's generic field_map_json on
 * purpose, same reasoning as the other dedicated listeners in this package:
 * "variance_reason"/"status" are reused by aria:reconciliation and
 * aria:manager-qty-approval earlier in this same workflow. Gating on the
 * exact (workflowCode, previousState) pair per workflow (unlike
 * PmtSharedReasonListener's own handling of TankerInbound's state name, which
 * has no workflow_code guard — see the note in
 * ManagerQtyApprovalDecisionListener) and reading the page's own bag entry
 * directly avoids the ambiguity.
 */
@Component
public class ManagerWeightApprovalDecisionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ManagerWeightApprovalDecisionListener.class);

    private final JdbcTemplate jdbc;

    public ManagerWeightApprovalDecisionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        // M10.TankerInbound's post-3rd-weighment state is still named
        // WEIGHT_DIFF_PENDING_MANAGER; M10.ReturnStockIn's equivalent
        // (post-2nd-weighment, no 3rd weighment in this flow) is WEIGHMENT_2 —
        // named to mirror WEIGHMENT_1 rather than describe the pending-manager
        // semantics, per explicit request to make the state list show a
        // distinct "second weighment" checkpoint.
        //
        // M10.FGUnloading/M10.StockInward (3 weighments, like TankerInbound)
        // route aria:manager-weight-approval submissions from EITHER
        // WEIGHMENT_3 (first attempt) or WEIGHT_APPROVAL_PENDING (a retry
        // after a prior REJECTED decision) — both must fire this listener, or
        // a retry's decision would silently stop being captured. This page
        // was previously uncaptured for both workflows (gap predates this
        // clone; confirmed neither this listener nor PmtSharedReasonListener
        // covered FGUnloading).
        boolean fires = ("M10.TankerInbound".equals(instance.getWorkflowCode()) && "WEIGHT_DIFF_PENDING_MANAGER".equals(previousState))
                || ("M10.ReturnStockIn".equals(instance.getWorkflowCode()) && "WEIGHMENT_2".equals(previousState))
                || (("M10.FGUnloading".equals(instance.getWorkflowCode()) || "M10.StockInward".equals(instance.getWorkflowCode()))
                        && ("WEIGHMENT_3".equals(previousState) || "WEIGHT_APPROVAL_PENDING".equals(previousState)));
        if (bag == null || !fires) {
            return;
        }
        try {
            Map<String, Object> page = pageData(bag, "aria:manager-weight-approval");
            Map<String, Object> cols = new LinkedHashMap<>();
            putIfPresent(cols, "weight_variance_reason", page, "variance_reason");
            putIfPresent(cols, "weight_decision", page, "status");
            putIfPresent(cols, "weight_difference", page, "weight_difference_display");
            putIfPresent(cols, "weight_difference", page, "rsi_weight_difference_display");
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
                "INSERT INTO manager_weight_approval_detail (" + colNames + ") VALUES (" + placeholders + ") " +
                    "ON DUPLICATE KEY UPDATE " + updates,
                args);
        } catch (Exception e) {
            log.warn("manager weight approval capture failed for instance={}: {}", instance.getId(), e.getMessage());
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
