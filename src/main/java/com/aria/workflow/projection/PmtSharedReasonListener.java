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
import java.util.Set;

/**
 * Routes submitted 'variance_reason' (and, for manager-qty-approval /
 * manager-weight-approval, 'status') into whichever of four satellite
 * tables actually owns this step (V88 pmt_weight_diff_detail, V89
 * pmt_camera_verification_detail, V90 manager_qty_approval_detail, V91
 * manager_weight_approval_detail) — kept OUT of WorkflowProjectionListener's
 * generic field_map on purpose.
 *
 * <p>'variance_reason' is reused, by field name only, across at least six
 * different pages (pmt-weight-diff-check, pmt-camera-verification,
 * manager-qty-approval, weight-diff-entry, reconciliation,
 * manager-weight-approval) — each capturing their own step's reason into
 * the same shared gate_pass.variance_reason column (pre-existing behaviour,
 * untouched here). 'status' is similarly reused by aria:qc-preload-approval,
 * aria:qc-final-out, aria:manager-qty-approval and
 * aria:manager-weight-approval. WorkflowProjectionListener applies a
 * target_table's field_map to EVERY transition of a workflow_code, not just
 * the page that "owns" that table, so a generic field_map keyed on either
 * name would also capture the other pages' values whenever they submit at
 * unrelated states. Gating on {@code previousState} (each page transitions
 * from a distinct state) is the only way to route the value to the right
 * table.
 *
 * <p>{@code bag} (WorkflowInstance.submittedDataJson) is keyed by
 * pageCode -&gt; that page's own submitted form data, accumulated over the
 * instance's ENTIRE lifetime (old pages' entries are never cleared) — and
 * {@code WorkflowEngineService.readBag} returns a plain {@code HashMap}, so
 * iteration order over multiple pages is undefined. A naive "search every
 * nested map for this key" (matching WorkflowProjectionListener.pluck()'s
 * fallback) would risk returning a DIFFERENT, older page's value for a
 * shared key like 'variance_reason' instead of the one just submitted.
 * Since each branch below is already gated on a {@code previousState} that
 * uniquely identifies which single page just fired, the page's own bag
 * entry is looked up directly by its known pageCode instead — precise, no
 * cross-page ambiguity.
 */
@Component
public class PmtSharedReasonListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(PmtSharedReasonListener.class);
    private static final Set<String> PMT_WEIGHT_DIFF_WORKFLOWS = Set.of("M10.FGLoading", "M10.PackLoading");
    private static final String TANKER_OUTBOUND_WORKFLOW = "M10.TankerOutbound";
    // None of the three workflows have a PARKING_IN_2 hop in their
    // definitions anymore -- aria:pmt-weight-diff-check (FGLoading/
    // PackLoading) / aria:weight-diff-entry (TankerOutbound) can each fire
    // directly off either WEIGHMENT_2 or SEALING_DONE:
    //  - TankerOutbound has no sealing step at all -> always WEIGHMENT_2.
    //  - FGLoading/PackLoading: OPEN_BODY vehicles skip sealing (fire off
    //    WEIGHMENT_2 directly); everyone else goes through SEALING_DONE
    //    first (fires off SEALING_DONE).
    // Both previousState cases below resolve the right page via this method
    // instead of being split apart, since either state can legitimately
    // belong to either the Tanker page or the FG/Pack page depending on
    // workflow code -- gated per-workflow so FGLoading's WEIGHMENT_2 ->
    // aria:sealing-process (unrelated data) is never mistaken for a PMT
    // check submission.
    private static String pmtWeightDiffPageFor(String workflowCode) {
        if (TANKER_OUTBOUND_WORKFLOW.equals(workflowCode)) return "aria:weight-diff-entry";
        if (PMT_WEIGHT_DIFF_WORKFLOWS.contains(workflowCode)) return "aria:pmt-weight-diff-check";
        return null;
    }
    // M10.PackUnload adds 1 more precisely-gated state here (own state name,
    // not shared with PackLoading/FGLoading/TankerInbound):
    // QUANTITY_APPROVAL_PENDING -> manager_qty_approval_detail (same table,
    // different previousState literal). See the switch case in
    // onTransition() below.
    //
    // Neither PO verification nor GRN status get a case here: matching
    // M10.TankerOutbound's own existing pattern (po_verified/
    // po_override_reason on aria:dispatch-vehicle-details), both are
    // captured as plain manual fields (GRN on aria:reconciliation) with no
    // workflow gate and no separate approval state/page/table.
    private static final Set<String> NO_WORKFLOW_GUARD_STATES = Set.of(
            "PMT_CAMERA_VERIFICATION", "PMT_CHECK_PENDING_MANAGER", "WEIGHT_DIFF_PENDING_MANAGER", "WEIGHMENT_3",
            "QUANTITY_APPROVAL_PENDING", "WEIGHT_APPROVAL_PENDING");

    private final JdbcTemplate jdbc;

    /**
     * True when this listener already captures {@code variance_reason} for this exact
     * (workflowCode, previousState) pair into one of the four satellite tables above.
     * Used by {@link WorkflowProjectionListener} to skip the same key so it stops
     * duplicating into gate_pass.variance_reason for these four steps specifically —
     * kept in sync with the switch in {@link #onTransition} on purpose (single source
     * of truth for "who owns this transition").
     */
    public static boolean ownsVarianceReason(String workflowCode, String previousState) {
        if (previousState == null) return false;
        if ("WEIGHMENT_2".equals(previousState) || "SEALING_DONE".equals(previousState)) {
            return pmtWeightDiffPageFor(workflowCode) != null;
        }
        return NO_WORKFLOW_GUARD_STATES.contains(previousState);
    }

    public PmtSharedReasonListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (previousState == null || bag == null) return;

        try {
            switch (previousState) {
                case "WEIGHMENT_2":
                case "SEALING_DONE": {
                    // See pmtWeightDiffPageFor() javadoc above: either state can
                    // lead here depending on workflow (and, for FGLoading/
                    // PackLoading, whether the vehicle skipped sealing).
                    String pageCode = pmtWeightDiffPageFor(instance.getWorkflowCode());
                    if (pageCode == null) break;
                    Map<String, Object> page = pageData(bag, pageCode);
                    String reason = firstNonBlank(str(page, "variance_reason"), str(page, "varianceReason"));
                    String queueReason = queueReasonOf(page);
                    if (reason != null || queueReason != null) {
                        Map<String, Object> cols = new LinkedHashMap<>();
                        if (reason != null) cols.put("pmt_variance_reason", reason);
                        if (queueReason != null) cols.put("queue_reason", queueReason);
                        upsert("pmt_weight_diff_detail", instance, cols);
                    }
                    break;
                }
                case "PMT_CAMERA_VERIFICATION": {
                    // No 'status' here — aria:pmt-camera-verification's single
                    // button carries no submitValues; its decision field is
                    // 'issue_identified', already captured generically (V89).
                    Map<String, Object> page = pageData(bag, "aria:pmt-camera-verification");
                    String reason = firstNonBlank(str(page, "variance_reason"), str(page, "varianceReason"));
                    if (reason != null) {
                        upsert("pmt_camera_verification_detail", instance, Map.of("camera_variance_reason", reason));
                    }
                    break;
                }
                case "PMT_CHECK_PENDING_MANAGER":
                case "WEIGHT_DIFF_PENDING_MANAGER": {
                    Map<String, Object> page = pageData(bag, "aria:manager-qty-approval");
                    String reason = firstNonBlank(str(page, "variance_reason"), str(page, "varianceReason"));
                    String decision = str(page, "status");
                    String queueReason = queueReasonOf(page);
                    if (reason != null || decision != null || queueReason != null) {
                        Map<String, Object> cols = new LinkedHashMap<>();
                        if (reason != null) cols.put("manager_qty_reason", reason);
                        if (decision != null) cols.put("manager_decision", decision);
                        if (queueReason != null) cols.put("queue_reason", queueReason);
                        upsert("manager_qty_approval_detail", instance, cols);
                    }
                    break;
                }
                case "WEIGHMENT_3": {
                    // aria:manager-weight-approval — confirmed the only
                    // transition originating from WEIGHMENT_3 in all 3
                    // workflows, so no extra workflow-code guard needed
                    // (unlike SEALING_DONE/WEIGHMENT_2 above).
                    Map<String, Object> page = pageData(bag, "aria:manager-weight-approval");
                    String reason = firstNonBlank(str(page, "variance_reason"), str(page, "varianceReason"));
                    String decision = str(page, "status");
                    String queueReason = queueReasonOf(page);
                    if (reason != null || decision != null || queueReason != null) {
                        Map<String, Object> cols = new LinkedHashMap<>();
                        if (reason != null) cols.put("weight_variance_reason", reason);
                        if (decision != null) cols.put("weight_decision", decision);
                        if (queueReason != null) cols.put("queue_reason", queueReason);
                        upsert("manager_weight_approval_detail", instance, cols);
                    }
                    break;
                }
                case "QUANTITY_APPROVAL_PENDING": {
                    // aria:manager-qty-approval under M10.PackUnload's own
                    // state name — same target table PackLoading/FGLoading/
                    // TankerOutbound already write via the
                    // PMT_CHECK_PENDING_MANAGER/WEIGHT_DIFF_PENDING_MANAGER
                    // case above.
                    Map<String, Object> page = pageData(bag, "aria:manager-qty-approval");
                    String reason = firstNonBlank(str(page, "variance_reason"), str(page, "varianceReason"));
                    String decision = str(page, "status");
                    String queueReason = queueReasonOf(page);
                    if (reason != null || decision != null || queueReason != null) {
                        Map<String, Object> cols = new LinkedHashMap<>();
                        if (reason != null) cols.put("manager_qty_reason", reason);
                        if (decision != null) cols.put("manager_decision", decision);
                        if (queueReason != null) cols.put("queue_reason", queueReason);
                        upsert("manager_qty_approval_detail", instance, cols);
                    }
                    break;
                }
                default:
                    // Not one of the states this listener owns —
                    // reconciliation still only writes gate_pass.variance_reason,
                    // unchanged.
            }
        } catch (Exception e) {
            log.warn("pmt shared-reason routing failed for instance={} previousState={}: {}",
                    instance.getId(), previousState, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> pageData(Map<String, Object> bag, String pageCode) {
        Object v = bag.get(pageCode);
        return v instanceof Map ? (Map<String, Object>) v : Map.of();
    }

    private void upsert(String table, WorkflowInstance instance, Map<String, Object> cols) {
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
                "INSERT INTO " + table + " (" + colNames + ") VALUES (" + placeholders + ") " +
                        "ON DUPLICATE KEY UPDATE " + updates,
                args);
    }

    private static String str(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        return (v == null || v.toString().isBlank()) ? null : String.valueOf(v);
    }

    /**
     * Why the operator picked this instance out of order in its queue —
     * same field name convention as WorkflowProjectionListener's generic
     * field_map (V232/V233), looked up here directly off the triggering
     * page's own bag entry instead, for the same reason variance_reason is:
     * a plain cross-page pluck() would risk returning a DIFFERENT page's
     * older queue_reason value.
     */
    private static String queueReasonOf(Map<String, Object> page) {
        return firstNonBlank(str(page, "queue_reason"), str(page, "queueReason"));
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
