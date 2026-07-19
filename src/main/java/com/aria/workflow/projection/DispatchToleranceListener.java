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
import java.util.List;
import java.util.Map;

/**
 * Generic dispatch weight-tolerance verdict.
 *
 * <p>The "PMT" step of a dispatch flow verifies that what physically went onto
 * the truck ({@code loaded = |W1 - W2|}, the two weighbridge readings) matches
 * what the order says should have gone ({@code expected = Σ(order_qty ×
 * sku.unit_weight_kg)} across the vehicle's route). If they differ by more than
 * a configured tolerance the truck must not bill straight through — it has to
 * route to a camera + manager exception. Historically that verdict was an
 * operator toggle ("within tolerance? YES/NO"); this listener makes it a fact
 * the SYSTEM computes from the real persisted weights, so the workflow guard
 * blocks on arithmetic, not on a human's say-so.
 *
 * <p>Fully config-driven and generic: the check only runs for a workflow whose
 * {@code m0_workflow_projection} row carries a non-null {@code tolerance_pct}.
 * Any tenant enables it on any dispatch projection by setting that number — no
 * customer-specific code. The tolerance arithmetic lives here in Java on
 * purpose: the guard evaluator runs in a {@code SimpleEvaluationContext}
 * sandbox that (deliberately) forbids {@code T(...)}, constructors, and
 * reflection, so numeric coercion of form strings cannot happen in SpEL. The
 * guard therefore stays a trivial {@code #submitted['withinTolerance']=='YES'}
 * string compare against the value this listener stamped.
 *
 * <p>Writes onto the projected {@code target_table} row:
 * {@code expected_weight_kg}, {@code loaded_weight_kg}, {@code pmt_variance_pct},
 * and {@code pmt_within_tolerance} ∈ {@code {YES, NO}} (YES also means
 * "not applicable / not yet computable" so legacy flows without SKU weights are
 * never spuriously blocked). Exceptions never propagate — the engine catches
 * listener failures, so a bad config or missing column can't fail a transition.
 */
@Component
public class DispatchToleranceListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(DispatchToleranceListener.class);

    private final JdbcTemplate jdbc;

    public DispatchToleranceListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState,
                             Map<String, Object> submittedDataBag) {
        String code = instance.getWorkflowCode();
        if (code == null) return;
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> configs;
        try {
            configs = jdbc.queryForList(
                    "SELECT target_table, key_column, tolerance_pct FROM m0_workflow_projection " +
                            "WHERE workflow_code=? AND tenant_id=? AND tolerance_pct IS NOT NULL",
                    code, tenantId);
        } catch (Exception e) {
            return; // column/table not applied yet — nothing to enforce
        }
        for (Map<String, Object> cfg : configs) {
            try {
                computeVerdict(cfg, instance.getId(), tenantId);
            } catch (Exception ex) {
                log.warn("tolerance verdict failed for workflow={} instance={}: {}",
                        code, instance.getId(), ex.getMessage());
            }
        }
    }

    private void computeVerdict(Map<String, Object> cfg, Long instanceId, Long tenantId) {
        // Only gate_pass is wired today; guard the identifier defensively anyway.
        String table = (String) cfg.get("target_table");
        String keyCol = orDefault((String) cfg.get("key_column"), "workflow_instance_id");
        if (!"gate_pass".equals(table) || !"workflow_instance_id".equals(keyCol)) return;

        BigDecimal tolerancePct = toBig(cfg.get("tolerance_pct"));
        if (tolerancePct == null || tolerancePct.signum() <= 0) return;

        Map<String, Object> gp;
        try {
            gp = jdbc.queryForMap(
                    "SELECT w1_weight_kg, w2_weight_kg, route_id FROM gate_pass " +
                            "WHERE workflow_instance_id=? AND tenant_id=?", instanceId, tenantId);
        } catch (Exception e) {
            return; // no gate_pass row yet
        }

        BigDecimal w1 = toBig(gp.get("w1_weight_kg"));
        BigDecimal w2 = toBig(gp.get("w2_weight_kg"));
        String route = (String) gp.get("route_id");
        // Not enough to judge yet — leave the verdict untouched, don't block.
        if (w1 == null || w2 == null || route == null || route.isBlank()) return;

        BigDecimal expected = expectedWeightForRoute(route, tenantId);
        if (expected == null || expected.signum() <= 0) return; // no SKU-weight basis → not applicable

        BigDecimal loaded = w1.subtract(w2).abs();
        BigDecimal variancePct = variancePct(loaded, expected);

        jdbc.update(
                "UPDATE gate_pass SET expected_weight_kg=?, loaded_weight_kg=?, " +
                        "pmt_variance_pct=?, pmt_within_tolerance=? " +
                        "WHERE workflow_instance_id=? AND tenant_id=?",
                expected, loaded, variancePct,
                verdict(loaded, expected, tolerancePct), instanceId, tenantId);
    }

    /** |loaded − expected| / expected × 100, rounded to 3 dp. Caller ensures expected &gt; 0. */
    static BigDecimal variancePct(BigDecimal loaded, BigDecimal expected) {
        return loaded.subtract(expected).abs()
                .multiply(BigDecimal.valueOf(100))
                .divide(expected, 3, RoundingMode.HALF_UP);
    }

    /**
     * The machine verdict the PMT guard blocks on. {@code YES} = the loaded
     * weight is within {@code tolerancePct} of expected (proceed to billing);
     * {@code NO} = over tolerance (route to camera + manager). Pure — no I/O —
     * so the 0.4%-boundary behaviour is unit-testable.
     */
    static String verdict(BigDecimal loaded, BigDecimal expected, BigDecimal tolerancePct) {
        return variancePct(loaded, expected).compareTo(tolerancePct) <= 0 ? "YES" : "NO";
    }

    /** Σ(order_qty × sku.unit_weight_kg) for every dispatch line on the vehicle's route. */
    private BigDecimal expectedWeightForRoute(String route, Long tenantId) {
        try {
            // m5_sku_master and m5_order_dispatch_detail carry sku_code under
            // different collations; force one explicitly so the join isn't an
            // "illegal mix of collations" error.
            return jdbc.queryForObject(
                    "SELECT SUM(d.order_qty * s.unit_weight_kg) " +
                            "FROM m5_order_dispatch_detail d " +
                            "JOIN m5_sku_master s " +
                            "  ON s.sku_code = d.sku_code COLLATE utf8mb4_general_ci " +
                            " AND s.tenant_id = d.tenant_id " +
                            "WHERE d.route_id = ? AND d.tenant_id = ? AND s.unit_weight_kg IS NOT NULL",
                    BigDecimal.class, route, tenantId);
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal toBig(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal b) return b;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(o.toString().trim()); } catch (RuntimeException e) { return null; }
    }

    private static String orDefault(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }
}
