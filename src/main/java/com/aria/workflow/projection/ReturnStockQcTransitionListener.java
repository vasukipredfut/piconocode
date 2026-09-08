package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Persists QC Approval's per-row "QC Batch No" / "Stock-wise Unloading Area"
 * edits (aria:qc-approval's "Return Stock Details" ListView, M10.ReturnStockIn
 * only) back onto the SAME return_stock_officer_item rows the Return Stock
 * Officer step created — an UPDATE by row id, never an insert/delete.
 *
 * <p>Deliberately NOT modeled on {@link ReturnStockOfficerItemTransitionListener}'s
 * own delete-then-insert: that listener re-fires on every later transition of
 * the same instance (the submitted-data bag is cumulative, keyed by pageCode,
 * and never clears aria:return-stock-officer's entry once submitted), so if
 * this listener ALSO deleted+reinserted the same rows, whichever of the two
 * ran second on the QC_APPROVED/QC_REJECTED transition would silently wipe
 * the other's write. An UPDATE-by-id has no such race: it's a no-op restore
 * of the same values if {@code ReturnStockOfficerItemTransitionListener}
 * happens to run first and freshly re-inserts identical rows with new ids —
 * except THAT case would orphan this update against stale ids, which is why
 * this listener is scoped tightly to the RETURN_STOCK_OFFICER -&gt;
 * QC_APPROVED/QC_REJECTED edge specifically (see {@link
 * TankerUnloadInvoiceTransitionListener}'s identical scoping reasoning) —
 * the Return Stock Officer step has already completed and its own listener
 * will not fire again for this same edge.
 *
 * <p>The ListView's dataSource has no explicit {@code select}, so each
 * bundled row carries every ReturnStockOfficerItem column including {@code
 * id} — matched here, re-scoped to {@code tenant_id}/{@code
 * workflow_instance_id} rather than trusting a client-supplied id alone.
 */
@Component
public class ReturnStockQcTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ReturnStockQcTransitionListener.class);
    private static final String WORKFLOW_CODE = "M10.ReturnStockIn";
    private static final String FROM_STATE = "RETURN_STOCK_OFFICER";
    private static final String PAGE_CODE = "aria:qc-approval";
    private static final String LIST_FIELD = "returnStockItems";

    private final JdbcTemplate jdbc;

    public ReturnStockQcTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode()) || !FROM_STATE.equals(previousState)) return;
        if (bag == null) return;
        Object pageDataRaw = bag.get(PAGE_CODE);
        if (!(pageDataRaw instanceof Map)) return;
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;
        Object linesRaw = pageData.get(LIST_FIELD);
        if (!(linesRaw instanceof List<?> lines) || lines.isEmpty()) return;

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();
        int updated = 0;

        for (Object lineRaw : lines) {
            if (!(lineRaw instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> line = (Map<String, Object>) lineRaw;
            Long rowId = toLong(line.get("id"));
            if (rowId == null) continue;
            String qcBatchNo = str(line.get("qc_batch_no"));
            String unloadingArea = str(line.get("unloading_area"));
            String diffReason = str(line.get("diff_reason"));
            if (qcBatchNo == null && unloadingArea == null && diffReason == null) continue; // nothing this row actually edited
            try {
                int n = jdbc.update(
                        "UPDATE return_stock_officer_item SET qc_batch_no = COALESCE(?, qc_batch_no), "
                                + "unloading_area = COALESCE(?, unloading_area), "
                                + "diff_reason = COALESCE(?, diff_reason) "
                                + "WHERE id = ? AND tenant_id = ? AND workflow_instance_id = ?",
                        qcBatchNo, unloadingArea, diffReason, rowId, tenantId, instanceId);
                updated += n;
            } catch (Exception e) {
                log.warn("return_stock_officer_item QC update failed for instance {} row id={}: {}",
                        instanceId, rowId, e.getMessage());
            }
        }

        log.info("return_stock_officer_item: QC updated {} row(s) for instance {}", updated, instanceId);
    }

    private static String str(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return s.isBlank() ? null : s;
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try {
            return Long.valueOf(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
