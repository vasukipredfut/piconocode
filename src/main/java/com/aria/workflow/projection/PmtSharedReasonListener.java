package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Routes submitted 'variance_reason' (and, for manager-qty-approval /
 * manager-weight-approval, 'status') into whichever of three satellite
 * tables actually owns this step (V89 pmt_camera_verification_detail, V90
 * manager_qty_approval_detail, V91 manager_weight_approval_detail) — kept
 * OUT of WorkflowProjectionListener's generic field_map on purpose.
 *
 * <p>Used to also own a WEIGHMENT_2/SEALING_DONE case routing
 * aria:pmt-weight-diff-check (FGLoading/PackLoading) and
 * aria:weight-diff-entry (TankerOutbound)'s variance_reason into
 * pmt_weight_diff_detail (V88). Both pages were retired from the UI (V264),
 * so that case — and pmt_weight_diff_detail's write path entirely — was
 * removed; the table itself is untouched (historical rows only, nothing
 * writes to it anymore). TankerOutbound's client-computed tolerance columns
 * (expected_weight_kg/loaded_weight_kg/pmt_variance_pct/pmt_within_tolerance)
 * moved WITH it — now read off aria:manager-qty-approval's own submission
 * instead (its PMT_CHECK_PENDING_MANAGER/WEIGHT_DIFF_PENDING_MANAGER case
 * below), since that's the only page left that still supplies them for this
 * one workflow (DispatchToleranceListener has no server-side basis for it).
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
    private static final String TANKER_OUTBOUND_WORKFLOW = "M10.TankerOutbound";
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
     * (workflowCode, previousState) pair into one of the three satellite tables above.
     * Used by {@link WorkflowProjectionListener} to skip the same key so it stops
     * duplicating into gate_pass.variance_reason for these four steps specifically —
     * kept in sync with the switch in {@link #onTransition} on purpose (single source
     * of truth for "who owns this transition").
     */
    public static boolean ownsVarianceReason(String workflowCode, String previousState) {
        if (previousState == null) return false;
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
                // WEIGHMENT_2/SEALING_DONE: aria:manager-qty-approval's OWN
                // tolerance-routing pass (withinTolerance YES/NO/MANAGER_DIRECT)
                // -- previously only captured the 4 client-computed tolerance
                // columns (TankerOutbound branch below); manager_decision/
                // manager_qty_reason/queue_reason/time_taken from THIS same
                // submission were silently dropped because neither state was
                // in this switch at all. Safe to key on previousState alone
                // here despite WEIGHMENT_2 also being aria:sealing-process's
                // own previousState (a DIFFERENT page): each instance passes
                // through WEIGHMENT_2 at most once in this workflow's FSM
                // (no transition ever re-enters it), and aria:sealing-process
                // never submits any of the fields read below, so its bag
                // entry for "aria:manager-qty-approval" is empty whenever
                // sealing-process is the one actually firing -- cols stays
                // empty and the upsert is skipped, same as before this case
                // existed.
                case "WEIGHMENT_2":
                case "SEALING_DONE":
                case "PMT_CHECK_PENDING_MANAGER":
                case "WEIGHT_DIFF_PENDING_MANAGER": {
                    Map<String, Object> page = pageData(bag, "aria:manager-qty-approval");
                    String reason = firstNonBlank(str(page, "variance_reason"), str(page, "varianceReason"));
                    String decision = str(page, "status");
                    String queueReason = queueReasonOf(page);
                    // time_taken (V266): UI-computed elapsed minutes for this step,
                    // read directly off this page's own bag entry -- no cross-page
                    // ambiguity risk here (unlike WorkflowProjectionListener's generic
                    // pluck()) since pageData() already scopes to aria:manager-qty-approval.
                    BigDecimal timeTaken = firstNonNull(toBig(page.get("time_taken")), toBig(page.get("timeTaken")));
                    // grn_status (M10.PackUnload/M10.FGUnloading only — the page shows
                    // this field solely for those two flow variants): manager_qty_
                    // approval_detail.grn_status already existed as an unused column,
                    // no listener wrote it, so a manager's GRN pick here was silently
                    // discarded. aria:reconciliation's own grn_status is a DIFFERENT
                    // page's bag entry (pageData() below scopes to THIS page only), so
                    // this doesn't collide with or replace that separate capture.
                    String grnStatus = str(page, "grn_status");
                    // unloading_qty: deliberately NOT set from this page's own
                    // client-computed variance_display_kg -- DispatchToleranceListener
                    // now writes it server-side instead (same server-verified
                    // loaded/unloaded weight it already computes for loaded_weight_kg),
                    // consistent with this whole class's "trust the server, not the
                    // client" design (see DispatchToleranceListener's own javadoc).
                    Map<String, Object> cols = new LinkedHashMap<>();
                    if (reason != null) cols.put("manager_qty_reason", reason);
                    if (decision != null) cols.put("manager_decision", decision);
                    if (queueReason != null) cols.put("queue_reason", queueReason);
                    if (timeTaken != null) cols.put("time_taken", timeTaken);
                    if (grnStatus != null) cols.put("grn_status", grnStatus);
                    // TankerOutbound only: since aria:weight-diff-entry was retired
                    // from the UI (V264), aria:manager-qty-approval is now the ONLY
                    // place these 4 tolerance columns get submitted for this workflow
                    // -- DispatchToleranceListener has no server-side basis for
                    // TankerOutbound (no loading_allocation_detail step, and its
                    // tanker-oil SKUs aren't registered in m5_sku_master with a
                    // unit_weight_kg), so it never populates them here either.
                    // FGLoading/PackLoading keep relying on DispatchToleranceListener's
                    // own server-verified figure -- this branch is scoped strictly to
                    // TankerOutbound so it never overwrites that with a client value.
                    if (TANKER_OUTBOUND_WORKFLOW.equals(instance.getWorkflowCode())) {
                        BigDecimal expected = toBig(page.get("expected_weight_kg"));
                        BigDecimal loaded = toBig(page.get("loaded_weight_kg"));
                        String within = firstNonBlank(str(page, "within_tolerance"), str(page, "withinTolerance"));
                        if (expected != null) cols.put("expected_weight_kg", expected);
                        if (loaded != null) cols.put("loaded_weight_kg", loaded);
                        if (within != null) cols.put("pmt_within_tolerance", within);
                        if (expected != null && loaded != null && expected.signum() > 0) {
                            cols.put("pmt_variance_pct", loaded.subtract(expected).abs()
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(expected, 3, RoundingMode.HALF_UP));
                        }
                    }
                    if (!cols.isEmpty()) {
                        upsert("manager_qty_approval_detail", instance, cols);
                    }
                    break;
                }
                case "WEIGHMENT_3":
                case "WEIGHT_APPROVAL_PENDING": {
                    // aria:manager-weight-approval — WEIGHMENT_3 is its
                    // original entry state; WEIGHT_APPROVAL_PENDING is the
                    // second round for the same page, reached from
                    // READY_FOR_EXIT -> WEIGHT_APPROVAL_PENDING (aria:gate-
                    // pass-closure-form, guard #submitted['report_weight_diff']
                    // == 'true') when a weight diff is flagged after the
                    // first approval. Both land in the same table via the
                    // same page, so no extra workflow-code guard needed —
                    // and this case MUST stay in sync with ownsVarianceReason()'s
                    // NO_WORKFLOW_GUARD_STATES set above, which already
                    // lists WEIGHT_APPROVAL_PENDING as owned (so
                    // WorkflowProjectionListener skips gate_pass for it);
                    // leaving this case out, as before, meant that data was
                    // skipped from gate_pass AND never captured here either
                    // — silently dropped.
                    Map<String, Object> page = pageData(bag, "aria:manager-weight-approval");
                    String reason = firstNonBlank(str(page, "variance_reason"), str(page, "varianceReason"));
                    String decision = str(page, "status");
                    String queueReason = queueReasonOf(page);
                    // time_taken (V294): same UI-computed elapsed-time field every
                    // other queue page submits -- manager_qty_approval_detail's own
                    // case above already captures it; this table never did.
                    BigDecimal timeTaken = firstNonNull(toBig(page.get("time_taken")), toBig(page.get("timeTaken")));
                    Map<String, Object> cols = new LinkedHashMap<>();
                    if (reason != null) cols.put("weight_variance_reason", reason);
                    if (decision != null) cols.put("weight_decision", decision);
                    if (queueReason != null) cols.put("queue_reason", queueReason);
                    if (timeTaken != null) cols.put("time_taken", timeTaken);
                    // weight_difference_display (TankerInbound: W2-W3) /
                    // rsi_weight_difference_display (ReturnStockIn: Expected-
                    // Second) are client-computed FormFields, not server-derived
                    // -- same gap as TankerOutbound's expected/loaded weight on
                    // aria:weight-diff-entry. Same fix: trust the page's own
                    // already-computed value directly, picking whichever variant
                    // is actually present for this flow.
                    BigDecimal difference = firstNonNull(
                            toBig(page.get("weight_difference_display")),
                            toBig(page.get("rsi_weight_difference_display")));
                    if (difference != null) cols.put("weight_difference", difference);
                    if (!cols.isEmpty()) {
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
                    BigDecimal timeTaken = firstNonNull(toBig(page.get("time_taken")), toBig(page.get("timeTaken")));
                    String grnStatus = str(page, "grn_status");
                    // GRN detail fields (V290): only visible on the page for
                    // M10.PackUnload/M10.FGUnloading, i.e. only ever present in
                    // this exact branch (the only workflows reaching
                    // QUANTITY_APPROVAL_PENDING) -- grn_status already saved,
                    // these 5 never did, only ever surviving in
                    // submitted_data_json.
                    String grnNumber = str(page, "grn_number");
                    String grnDate = str(page, "grn_date");
                    String grnNotRequiredReason = str(page, "grn_not_required_reason");
                    String grnExceptionReason = str(page, "grn_exception_reason");
                    String grnUpdatedBy = str(page, "grn_updated_by");
                    if (reason != null || decision != null || queueReason != null || timeTaken != null || grnStatus != null
                            || grnNumber != null || grnDate != null || grnNotRequiredReason != null
                            || grnExceptionReason != null || grnUpdatedBy != null) {
                        Map<String, Object> cols = new LinkedHashMap<>();
                        if (reason != null) cols.put("manager_qty_reason", reason);
                        if (decision != null) cols.put("manager_decision", decision);
                        if (queueReason != null) cols.put("queue_reason", queueReason);
                        if (timeTaken != null) cols.put("time_taken", timeTaken);
                        if (grnStatus != null) cols.put("grn_status", grnStatus);
                        if (grnNumber != null) cols.put("grn_number", grnNumber);
                        if (grnDate != null) cols.put("grn_date", grnDate);
                        if (grnNotRequiredReason != null) cols.put("grn_not_required_reason", grnNotRequiredReason);
                        if (grnExceptionReason != null) cols.put("grn_exception_reason", grnExceptionReason);
                        if (grnUpdatedBy != null) cols.put("grn_updated_by", grnUpdatedBy);
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

    /** Form-submitted numbers arrive as strings (e.g. {@code "19000"}) — parse defensively. */
    private static BigDecimal toBig(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal b) return b;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(o.toString().trim()); } catch (RuntimeException e) { return null; }
    }

    private static BigDecimal firstNonNull(BigDecimal a, BigDecimal b) {
        return a != null ? a : b;
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
