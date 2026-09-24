package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * <p>Writes {@code expected_weight_kg}, {@code loaded_weight_kg},
 * {@code pmt_variance_pct}, and {@code pmt_within_tolerance} ∈ {@code {YES, NO}}
 * (YES also means "not applicable / not yet computable" so legacy flows
 * without SKU weights are never spuriously blocked) into {@code
 * manager_qty_approval_detail} (V90/V263) — keyed on (tenant_id, workflow_instance_id),
 * not the {@code target_table} the config row names (still {@code gate_pass};
 * that row only supplies {@code tolerance_pct}, the enable/threshold switch —
 * V92 originally moved these 4 columns off gate_pass onto pmt_weight_diff_detail;
 * V263 moved them again onto manager_qty_approval_detail, since this data is
 * fully server-computed and doesn't need its own dedicated satellite table —
 * pmt_weight_diff_detail keeps only its own operator-entered
 * pmt_variance_reason/queue_reason, untouched by this listener). Exceptions
 * never propagate — the engine catches listener failures, so a bad config or
 * missing column can't fail a transition.
 *
 * <p>{@code @Order(LOWEST_PRECEDENCE)} so this runs AFTER {@link
 * WorkflowProjectionListener} in WorkflowEngineService's {@code
 * List<WorkflowTransitionListener>} autowiring — this listener reads
 * w1_weight_kg/w2_weight_kg back off weighment_capture_detail, the row
 * WorkflowProjectionListener writes on the same transition; running first
 * would compute expected/loaded weight off the previous transition's stale
 * reading instead of the one just submitted.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
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
            // Product-default (tenant_id=-1) config applies to every tenant; a
            // tenant-specific row for the same target_table overrides it.
            List<Map<String, Object>> raw = jdbc.queryForList(
                    "SELECT target_table, key_column, tolerance_pct FROM m0_workflow_projection " +
                            "WHERE workflow_code=? AND tenant_id IN (?, -1) AND tolerance_pct IS NOT NULL " +
                            "ORDER BY tenant_id DESC",
                    code, tenantId);
            LinkedHashMap<String, Map<String, Object>> byTable = new LinkedHashMap<>();
            for (Map<String, Object> c : raw) byTable.putIfAbsent(String.valueOf(c.get("target_table")), c);
            configs = new ArrayList<>(byTable.values());
        } catch (Exception e) {
            return; // column/table not applied yet — nothing to enforce
        }
        for (Map<String, Object> cfg : configs) {
            try {
                computeVerdict(cfg, instance.getId(), tenantId, code);
            } catch (Exception ex) {
                log.warn("tolerance verdict failed for workflow={} instance={}: {}",
                        code, instance.getId(), ex.getMessage());
            }
        }
    }

    /** Workflows whose direction is receiving/unloading rather than dispatch/
     *  loading — for these, the same server-verified |W1-W2| this method
     *  already computes into loaded_weight_kg is ALSO the physically-unloaded
     *  quantity manager_qty_approval_detail.unloading_qty exists to record
     *  (that column previously had nothing writing to it at all). Loading-
     *  direction workflows (FGLoading/PackLoading/TankerOutbound) never touch
     *  unloading_qty — same reasoning as loaded_weight_kg being the wrong
     *  concept for them to duplicate into a second column. */
    private static final Set<String> UNLOADING_DIRECTION_WORKFLOWS = Set.of("M10.PackUnload", "M10.FGUnloading", "M10.StockInward");

    private void computeVerdict(Map<String, Object> cfg, Long instanceId, Long tenantId, String workflowCode) {
        // Only gate_pass is wired today; guard the identifier defensively anyway.
        String table = (String) cfg.get("target_table");
        String keyCol = orDefault((String) cfg.get("key_column"), "workflow_instance_id");
        if (!"gate_pass".equals(table) || !"workflow_instance_id".equals(keyCol)) return;

        BigDecimal tolerancePct = toBig(cfg.get("tolerance_pct"));
        if (tolerancePct == null || tolerancePct.signum() <= 0) return;

        Map<String, Object> gp;
        try {
            // w1/w2_weight_kg and ordered_qty moved out of gate_pass into
            // weighment_capture_detail / loading_allocation_detail respectively
            // (V80/V83) — the LEFT JOINs keep this working for the workflows
            // that populate those rows; TankerOutbound has no
            // loading_allocation_detail row (no such step) and falls through
            // to the route_id basis below, same as before those migrations.
            gp = jdbc.queryForMap(
                    "SELECT gpw.w1_weight_kg, gpw.w2_weight_kg, lpd.route_id, lad.ordered_qty " +
                            "FROM gate_pass gp " +
                            "LEFT JOIN weighment_capture_detail gpw " +
                            "  ON gpw.workflow_instance_id = gp.workflow_instance_id AND gpw.tenant_id = gp.tenant_id " +
                            "LEFT JOIN loading_allocation_detail lad " +
                            "  ON lad.workflow_instance_id = gp.workflow_instance_id AND lad.tenant_id = gp.tenant_id " +
                            "LEFT JOIN loading_progress_detail lpd " +
                            "  ON lpd.workflow_instance_id = gp.workflow_instance_id AND lpd.tenant_id = gp.tenant_id " +
                            "WHERE gp.workflow_instance_id=? AND gp.tenant_id=?", instanceId, tenantId);
        } catch (Exception e) {
            return; // no gate_pass row yet
        }

        BigDecimal w1 = toBig(gp.get("w1_weight_kg"));
        BigDecimal w2 = toBig(gp.get("w2_weight_kg"));
        String route = (String) gp.get("route_id");
        BigDecimal ordered = toBig(gp.get("ordered_qty"));
        // Not enough to judge yet — need both weighbridge reads. Don't block.
        if (w1 == null || w2 == null) return;

        // Prefer the linked OrderApp order's ordered weight — "OrderApp qty vs
        // loading qty" is exactly what the PMT screen compares. Fall back to the
        // route's SKU-weight basis when no order is linked to the gate pass.
        BigDecimal expected = (ordered != null && ordered.signum() > 0)
                ? ordered
                : (route != null && !route.isBlank() ? expectedWeightForRoute(route, tenantId) : null);
        if (expected == null || expected.signum() <= 0) return; // no basis → not applicable

        BigDecimal loaded = w1.subtract(w2).abs();
        BigDecimal variancePct = variancePct(loaded, expected);
        String verdict = verdict(loaded, expected, tolerancePct);

        if (UNLOADING_DIRECTION_WORKFLOWS.contains(workflowCode)) {
            jdbc.update(
                    "INSERT INTO manager_qty_approval_detail " +
                            "(tenant_id, workflow_instance_id, expected_weight_kg, loaded_weight_kg, unloading_qty, pmt_variance_pct, pmt_within_tolerance) " +
                            "VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                            "expected_weight_kg=VALUES(expected_weight_kg), loaded_weight_kg=VALUES(loaded_weight_kg), " +
                            "unloading_qty=VALUES(unloading_qty), " +
                            "pmt_variance_pct=VALUES(pmt_variance_pct), pmt_within_tolerance=VALUES(pmt_within_tolerance)",
                    tenantId, instanceId, expected, loaded, loaded, variancePct, verdict);
        } else {
            jdbc.update(
                    "INSERT INTO manager_qty_approval_detail " +
                            "(tenant_id, workflow_instance_id, expected_weight_kg, loaded_weight_kg, pmt_variance_pct, pmt_within_tolerance) " +
                            "VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                            "expected_weight_kg=VALUES(expected_weight_kg), loaded_weight_kg=VALUES(loaded_weight_kg), " +
                            "pmt_variance_pct=VALUES(pmt_variance_pct), pmt_within_tolerance=VALUES(pmt_within_tolerance)",
                    tenantId, instanceId, expected, loaded, variancePct, verdict);
        }
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
