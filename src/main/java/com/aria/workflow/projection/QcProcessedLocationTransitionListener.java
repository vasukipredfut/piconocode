package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * qc_processed_location child rows -- QC Officer's Processed Area/Processed
 * Type/Box Qty/PC Qty allocation entries, splitting ONE Return Stock
 * Officer batch/SKU row's Box/PC across one or more processed locations
 * (M10.ReturnStockIn only). Each allocation line is a child of exactly one
 * {@code return_stock_officer_item} row (Option A: nested per batch, not
 * flat per vehicle), so every submitted line must carry the parent row's id
 * ({@code return_stock_officer_item_id} / {@code returnStockOfficerItemId}).
 *
 * <p>Submitted field key is {@code qcProcessedLocations} (camelCase, on
 * {@link #PAGE_CODE}'s own bag entry -- matches the sibling
 * {@link ReturnStockQcTransitionListener}'s {@code returnStockItems} naming
 * convention). Whatever frontend shape eventually collects this (a nested
 * table per batch row, or a flat table with a per-row batch picker) is
 * irrelevant here -- the backend only needs each line to name its parent.
 *
 * <p>Delete-then-insert per instance, same idempotency reasoning as
 * {@link ReturnStockOfficerItemTransitionListener}: safe to re-run, since
 * nothing else references {@code qc_processed_location.id} externally.
 * Scoped to the {@code RETURN_STOCK_OFFICER -> QC_APPROVED/QC_REJECTED}
 * edge (this data only ever gets submitted once, from aria:qc-approval).
 *
 * <p>Also recomputes {@code diff_box}/{@code diff_pc} on every affected
 * {@code return_stock_officer_item} row after writing the allocations:
 * {@code diff = box/pc - SUM(this batch's qc_processed_location box_qty/pc_qty)}.
 * Done here, server-side, because the frontend expression engine
 * ({@code ariafrontend/src/configurable/ruleEval.js}) only evaluates scalar
 * arithmetic -- it has no array sum/filter, so "sum this batch's allocated
 * Box/PC" can't be a {@code computedFrom} on the Return Stock Details
 * ListView. The ListView just displays these two columns back, same
 * write-via-listener/read-via-ListView pattern as qc_batch_no/unloading_area.
 */
@Component
public class QcProcessedLocationTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(QcProcessedLocationTransitionListener.class);
    private static final String WORKFLOW_CODE = "M10.ReturnStockIn";
    private static final String FROM_STATE = "RETURN_STOCK_OFFICER";
    private static final String PAGE_CODE = "aria:qc-approval";
    private static final String LIST_FIELD = "qcProcessedLocations";

    private final JdbcTemplate jdbc;

    public QcProcessedLocationTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode()) || !FROM_STATE.equals(previousState)) return;
        if (bag == null) return;
        Object pageDataRaw = bag.get(PAGE_CODE);
        if (!(pageDataRaw instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;
        Object linesRaw = pageData.get(LIST_FIELD);

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();

        jdbc.update("DELETE FROM qc_processed_location WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instanceId);

        int written = 0;
        if (linesRaw instanceof List<?> lines && !lines.isEmpty()) {
            for (Object lineRaw : lines) {
                if (!(lineRaw instanceof Map)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> line = (Map<String, Object>) lineRaw;
                Long parentItemId = num(line, "return_stock_officer_item_id", "returnStockOfficerItemId");
                if (parentItemId == null) {
                    log.warn("qc_processed_location line skipped for instance {}: no return_stock_officer_item_id",
                            instanceId);
                    continue;
                }
                try {
                    jdbc.update(
                            "INSERT INTO qc_processed_location (tenant_id, workflow_instance_id, "
                                    + "return_stock_officer_item_id, processed_area, processed_type, box_qty, pc_qty) "
                                    + "VALUES (?,?,?,?,?,?,?)",
                            tenantId, instanceId, parentItemId,
                            str(line, "processed_area", "processedArea"),
                            str(line, "processed_type", "processedType"),
                            dec(line, "box_qty", "boxQty"), dec(line, "pc_qty", "pcQty"));
                    written++;
                } catch (Exception e) {
                    log.warn("qc_processed_location insert failed for instance {} parentItemId={}: {}",
                            instanceId, parentItemId, e.getMessage());
                }
            }
            log.info("qc_processed_location: wrote {}/{} row(s) for instance {}", written, lines.size(), instanceId);
        }

        recomputeDiffs(tenantId, instanceId);
    }

    /**
     * For every return_stock_officer_item row on this instance, diff = own
     * box/pc minus the sum of its qc_processed_location children's
     * box_qty/pc_qty (0 if it has none). Runs unconditionally (even when
     * this submission carried no allocation lines) so an instance whose
     * batches have zero allocations still shows the full original box/pc as
     * the diff, rather than a stale value from a previous submission.
     */
    private void recomputeDiffs(long tenantId, Long instanceId) {
        List<Map<String, Object>> items = jdbc.queryForList(
                "SELECT id, box, pc FROM return_stock_officer_item WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instanceId);
        for (Map<String, Object> item : items) {
            Long itemId = ((Number) item.get("id")).longValue();
            BigDecimal box = toBigDecimal(item.get("box"));
            BigDecimal pc = toBigDecimal(item.get("pc"));
            Map<String, Object> sums = jdbc.queryForMap(
                    "SELECT COALESCE(SUM(box_qty),0) AS allocated_box, COALESCE(SUM(pc_qty),0) AS allocated_pc "
                            + "FROM qc_processed_location WHERE tenant_id = ? AND return_stock_officer_item_id = ?",
                    tenantId, itemId);
            BigDecimal allocatedBox = toBigDecimal(sums.get("allocated_box"));
            BigDecimal allocatedPc = toBigDecimal(sums.get("allocated_pc"));
            BigDecimal diffBox = box == null ? null : box.subtract(allocatedBox);
            BigDecimal diffPc = pc == null ? null : pc.subtract(allocatedPc);
            jdbc.update("UPDATE return_stock_officer_item SET diff_box = ?, diff_pc = ? WHERE id = ? AND tenant_id = ?",
                    diffBox, diffPc, itemId, tenantId);
        }
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static String str(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
        }
        return null;
    }

    private static Long num(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v == null) continue;
            if (v instanceof Number n) return n.longValue();
            try {
                return Long.valueOf(String.valueOf(v).trim());
            } catch (NumberFormatException ignored) {
                // try next alias
            }
        }
        return null;
    }

    private static BigDecimal dec(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v == null || String.valueOf(v).isBlank()) continue;
            if (v instanceof BigDecimal bd) return bd;
            if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
            try {
                return new BigDecimal(String.valueOf(v));
            } catch (NumberFormatException ignored) {
                // try next alias
            }
        }
        return null;
    }
}
