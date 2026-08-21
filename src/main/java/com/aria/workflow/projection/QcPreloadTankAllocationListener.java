package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * On M10.TankerOutbound's DISPATCH_LOADING_PLANNING -&gt; QC_PRELOAD_APPROVED
 * transition (aria:qc-preload-approval, "Order Details — Tank Allocation" grid),
 * seeds the loading-progress tables from the per-SKU tank allocations the QC
 * officer just approved — one head row in {@code loading_progress_detail}
 * (V94/V177) and one child row per SKU in {@code loading_progress_detail_bay}
 * (V179), status {@code IN_PROGRESS}.
 *
 * <p>No DB column backs the tank pick — the page sends its edited grid rows
 * straight through {@code POST /api/workflow/transition}'s {@code formData},
 * under {@code order_sku_line_items} (same array-of-lines convention
 * {@link com.aria.action.handler.M10StartBayActionHandler} already uses:
 * one object per line, {@code route_id}/{@code sku_code}/{@code sku_desc}
 * plus this page's own {@code tank_location} and {@code load_wgt}). That
 * lands in {@code WorkflowInstance.submittedDataJson} keyed by pageCode —
 * this listener reads it back from there, never from a table.
 *
 * <p>This workflow has no Start Bay / LOADING_IN_PROGRESS / BAY_ASSIGNED step
 * (M10StartBayActionHandler's flow) — QC_PRELOAD_APPROVED goes straight to
 * LOADING_COMPLETED via aria:weighment-capture — so this listener is the only
 * writer of these rows for this flow. bay_name holds the tank code (e.g.
 * "ROT-1"), not a physical Bay1..Bay12 slot; the DB-level CHECK that used to
 * restrict bay_name to that set was dropped in V236 for exactly this reason.
 *
 * <p>Marking these rows' processed_quantity and flipping status to DONE once
 * the first weighment lands (QC_PRELOAD_APPROVED -&gt; LOADING_COMPLETED) is
 * separate follow-up work, intentionally not done here.
 */
@Component
public class QcPreloadTankAllocationListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(QcPreloadTankAllocationListener.class);
    private static final String PAGE_CODE = "aria:qc-preload-approval";

    private final JdbcTemplate jdbc;

    public QcPreloadTankAllocationListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> submittedDataBag) {
        if (!"DISPATCH_LOADING_PLANNING".equals(previousState)) return;
        if (!"M10.TankerOutbound".equals(instance.getWorkflowCode())) return;
        if (!"QC_PRELOAD_APPROVED".equals(instance.getCurrentState())) return;
        if (submittedDataBag == null) return;

        Long tenantId = TenantContext.get();
        Long instanceId = instance.getId();

        try {
            Object pageObj = submittedDataBag.get(PAGE_CODE);
            if (!(pageObj instanceof Map)) return;
            Map<String, Object> page = (Map<String, Object>) pageObj;

            Object linesObj = page.get("order_sku_line_items");
            if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) return;

            List<LineEntry> parsed = new ArrayList<>();
            BigDecimal totalQty = BigDecimal.ZERO;
            for (Object raw : (List<?>) linesObj) {
                if (!(raw instanceof Map)) continue;
                Map<String, Object> line = (Map<String, Object>) raw;
                String skuCode = str(line.get("sku_code"));
                String tankLocation = firstNonBlank(str(line.get("tank_location")), str(line.get("bay_name")));
                if (skuCode == null || tankLocation == null) continue; // this line wasn't allocated a tank — skip
                BigDecimal qty = toBig(firstNonNull(line.get("load_wgt"), line.get("quantity"), line.get("order_qty")));
                parsed.add(new LineEntry(str(line.get("route_id")), skuCode, str(line.get("sku_desc")), tankLocation, qty));
                totalQty = totalQty.add(qty);
            }
            if (parsed.isEmpty()) return;

            // One-time seed — this edge fires once per instance, but guard
            // anyway rather than risk duplicate bay rows on a re-delivered event.
            Integer already = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ?",
                    Integer.class, tenantId, instanceId);
            if (already != null && already > 0) return;

            String vehicleNo = firstNonBlank(str(page.get("vehicle_no")), str(page.get("vehicleNo")));
            String gatePassNo = firstNonBlank(str(page.get("gpNumber")), str(page.get("gp_number")));
            if (vehicleNo == null || gatePassNo == null) {
                Map<String, Object> gp = gatePassRow(tenantId, instanceId);
                if (gp != null) {
                    if (vehicleNo == null) vehicleNo = str(gp.get("vehicle_no"));
                    if (gatePassNo == null) gatePassNo = str(gp.get("gp_number"));
                }
            }

            jdbc.update(
                    "INSERT INTO loading_progress_detail (tenant_id, workflow_instance_id, vehicle_no, gate_pass_no, total_quantity, processed_quantity) " +
                            "VALUES (?, ?, ?, ?, ?, 0) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), " +
                            "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no), " +
                            "total_quantity = total_quantity + VALUES(total_quantity)",
                    tenantId, instanceId, vehicleNo, gatePassNo, totalQty);

            Long parentId = jdbc.queryForObject(
                    "SELECT id FROM loading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                    Long.class, tenantId, instanceId);

            for (LineEntry e : parsed) {
                jdbc.update(
                        "INSERT INTO loading_progress_detail_bay " +
                                "(tenant_id, loading_progress_detail_id, workflow_instance_id, route_id, sku_code, sku_desc, bay_name, quantity, status) " +
                                "VALUES (?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                        tenantId, parentId, instanceId, e.routeId, e.skuCode, e.skuDesc, e.tankLocation, e.quantity);
            }
        } catch (Exception e) {
            log.warn("qc-preload tank allocation seeding failed for instance={}: {}", instanceId, e.getMessage());
        }
    }

    private Map<String, Object> gatePassRow(Long tenantId, Long instanceId) {
        try {
            return jdbc.queryForMap(
                    "SELECT vehicle_no, gp_number FROM gate_pass WHERE workflow_instance_id = ? AND tenant_id = ?",
                    instanceId, tenantId);
        } catch (Exception e) {
            return null;
        }
    }

    private record LineEntry(String routeId, String skuCode, String skuDesc, String tankLocation, BigDecimal quantity) {}

    private static String str(Object o) {
        return (o == null || o.toString().isBlank()) ? null : o.toString();
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private static Object firstNonNull(Object... vals) {
        for (Object v : vals) if (v != null) return v;
        return null;
    }

    private static BigDecimal toBig(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal b) return b;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(o.toString().trim()); } catch (RuntimeException e) { return BigDecimal.ZERO; }
    }
}
