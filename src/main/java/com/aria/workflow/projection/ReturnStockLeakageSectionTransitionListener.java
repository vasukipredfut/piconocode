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
 * Persists {@code aria:unloading-progress}'s {@code qcAllocatedLocations}
 * submission into {@code return_stock_leakage_section} once a
 * M10.ReturnStockIn instance reaches {@code UNLOADING_COMPLETED} -- one row
 * per BOX/PC line (a location with both quantities submits as two lines,
 * discriminated by {@code line_type}; see the page's own {@code
 * dataSource.splitRows}).
 *
 * <p>Checked against the CURRENT state, not the previous one -- same
 * reasoning as the listener this replaces: the instance can reach {@code
 * UNLOADING_COMPLETED} from either {@code QC_APPROVED} directly or via the
 * legacy {@code UNLOADING_IN_PROGRESS} hop, and both must persist
 * identically.
 *
 * <p>Delete-then-insert per instance -- same idempotency reasoning as
 * {@link ReturnStockOfficerItemTransitionListener}/{@link
 * QcProcessedLocationTransitionListener}: {@code submitted_data_json} is
 * cumulative for the instance's whole lifetime and this listener's {@code
 * onTransition} fires on every later transition too, not just the one where
 * this page actually submitted, so without delete-then-insert the same
 * lines would duplicate on every subsequent transition.
 *
 * <p>Reads the bag by its EXACT page code key, not a "most recent page"
 * helper -- see {@link com.aria.m10.returnstock.ReturnStockBatchController}'s
 * class doc for why that matters (a plain {@code HashMap}'s iteration order
 * is not insertion order).
 */
@Component
public class ReturnStockLeakageSectionTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ReturnStockLeakageSectionTransitionListener.class);
    private static final String WORKFLOW_CODE = "M10.ReturnStockIn";
    private static final String TARGET_STATE = "UNLOADING_COMPLETED";
    private static final String PAGE_CODE = "aria:unloading-progress";
    private static final String LIST_FIELD = "qcAllocatedLocations";

    private final JdbcTemplate jdbc;

    public ReturnStockLeakageSectionTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode())) return;
        if (!TARGET_STATE.equals(instance.getCurrentState())) return;
        if (bag == null) return;

        Object pageDataRaw = bag.get(PAGE_CODE);
        if (!(pageDataRaw instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;
        Object linesRaw = pageData.get(LIST_FIELD);
        if (!(linesRaw instanceof List<?> lines) || lines.isEmpty()) return;

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();
        String vehicleNo = str(pageData, "vehicleNo", "vehicle_no");
        String gatePassNo = str(pageData, "gpNumber", "gp_number");

        jdbc.update("DELETE FROM return_stock_leakage_section WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instanceId);

        int written = 0;
        for (Object lineRaw : lines) {
            if (!(lineRaw instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> line = (Map<String, Object>) lineRaw;
            try {
                jdbc.update(
                        "INSERT INTO return_stock_leakage_section (tenant_id, workflow_instance_id, vehicle_no, "
                                + "gate_pass_no, batch_no, sku_code, sku_name, processed_area, line_type, "
                                + "scan_method, unloading_qty) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                        tenantId, instanceId, vehicleNo, gatePassNo,
                        str(line, "batch_no", "batchNo"), str(line, "sku_code", "skuCode"),
                        str(line, "sku_name", "skuName"), str(line, "processed_area", "processedArea"),
                        str(line, "line_type", "lineType"), str(line, "scan_method", "scanMethod"),
                        dec(line, "unloading_qty", "unloadingQty"));
                written++;
            } catch (Exception e) {
                log.warn("return_stock_leakage_section insert failed for instance {} batch_no={}: {}",
                        instanceId, line.get("batch_no"), e.getMessage());
            }
        }
        log.info("return_stock_leakage_section: wrote {}/{} line(s) for instance {} (vehicle={})",
                written, lines.size(), instanceId, vehicleNo);
    }

    private static String str(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
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
