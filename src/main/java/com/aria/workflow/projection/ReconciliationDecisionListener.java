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
 * M10.TankerInbound (previousState WEIGHMENT_2), M10.ReturnStockIn
 * (previousState UNLOADING_DONE — its equivalent invoice-vs-received stock
 * diff step, reusing this same page/listener rather than duplicating the
 * pattern; ReturnStockIn has no dedicated parking states to land in between,
 * per V131), and M10.PackUnload (previousState WEIGHMENT_2 — also reusing
 * this same page/table, since it's a generic reconciliation-decision record,
 * not TankerInbound-specific despite its original migration's naming).
 *
 * <p>Kept OUT of WorkflowProjectionListener's generic field_map_json on
 * purpose, same reasoning as {@link PmtSharedReasonListener} and
 * {@link QcApprovalDecisionListener}: "variance_reason" is reused later in
 * this same workflow by aria:manager-qty-approval and
 * aria:manager-weight-approval, and WorkflowProjectionListener.pluck()
 * searches every page's submitted data (a plain HashMap, undefined iteration
 * order) for a bare key match — it would risk grabbing one of those later
 * pages' variance_reason instead of aria:reconciliation's own. Gating on the
 * exact (workflowCode, previousState) pair per workflow (only
 * aria:reconciliation fires from that state in either workflow) and reading
 * the page's own bag entry directly avoids that ambiguity entirely.
 *
 * <p>Unlike gate_pass (which always pre-exists by this point), this table has
 * no row yet the first time — INSERT ... ON DUPLICATE KEY UPDATE, keyed on
 * the table's (tenant_id, workflow_instance_id) unique constraint.
 *
 * <p>Also updates gate_pass.total_unloading_qty (w1 - w2) — an existing
 * cross-cutting summary column gate_pass already carries for other workflows
 * (same pattern as qc_status/variance_reason). invoice_qty, w1/w2 weight, and
 * GRN status/number are NOT copied onto gate_pass: they already live on
 * dispatch_vehicle_detail / weighment_capture_detail / unload_reconciliation_detail,
 * and {@code PrintController} (like {@code GenericEntityController}) reads
 * gate_pass through {@link com.aria.entity.GatePassSatelliteJoin}, which joins
 * those tables in directly — see V214, which reverted V162's attempt to
 * duplicate these columns onto gate_pass. w1/w2 weight can't be read off the
 * submission bag reliably (both weighments share the same pageCode, so the
 * bag only ever holds the LATEST one), so they're read back from
 * weighment_capture_detail instead.
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
        boolean fires = ("M10.TankerInbound".equals(instance.getWorkflowCode()) && "WEIGHMENT_2".equals(previousState))
                || ("M10.ReturnStockIn".equals(instance.getWorkflowCode()) && "UNLOADING_DONE".equals(previousState))
                || ("M10.PackUnload".equals(instance.getWorkflowCode()) && "WEIGHMENT_2".equals(previousState));
        if (bag == null || !fires) {
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

            projectGatePassSummary(instance, tenantId);
        } catch (Exception e) {
            log.warn("reconciliation decision capture failed for instance={}: {}", instance.getId(), e.getMessage());
        }
    }

    private void projectGatePassSummary(WorkflowInstance instance, Long tenantId) {
        Double w1 = null, w2 = null;
        try {
            Map<String, Object> w = jdbc.queryForMap(
                "SELECT w1_weight_kg, w2_weight_kg FROM weighment_capture_detail WHERE tenant_id=? AND workflow_instance_id=?",
                tenantId, instance.getId());
            w1 = toDouble(w.get("w1_weight_kg"));
            w2 = toDouble(w.get("w2_weight_kg"));
        } catch (Exception ignored) { }
        if (w1 == null || w2 == null) return;

        jdbc.update("UPDATE gate_pass SET total_unloading_qty=? WHERE workflow_instance_id=? AND tenant_id=?",
                w1 - w2, instance.getId(), tenantId);
    }

    private static Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return null; }
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
