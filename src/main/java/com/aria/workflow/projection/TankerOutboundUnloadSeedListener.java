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
 * On {@code M10.TankerOutbound}'s {@code RELOAD_REQUIRED -> UNLOADING_WEIGHMENT}
 * transition (aria:weighment-capture — no separate unload page), seeds
 * {@code unloading_progress_detail} (parent) / {@code unloading_progress_detail_bay}
 * (child) from whatever this instance actually has loaded, and flags the
 * matching {@code loading_progress_detail_bay} rows {@code UNLOAD_IN_PROGRESS}
 * — the Outbound analogue of {@link TankerUnloadInvoiceTransitionListener}'s
 * Inbound seeding, but sourced from already-loaded rows (route_id/order_id/
 * sku_code/quantity/bay_name=tank) instead of a page-submitted invoice list,
 * since there is no invoice here to read.
 *
 * <p>Deliberately mirrors {@code M10StartUnloadBayActionHandler}'s own
 * insert shape for {@code unloading_progress_detail_bay} (route_id/order_id/
 * sku_code/sku_desc/bay_name/quantity, status IN_PROGRESS) and its
 * DONE -&gt; UNLOAD_IN_PROGRESS flip on {@code loading_progress_detail_bay} —
 * but runs automatically off the state transition instead of an explicit
 * Start Unload Bay action, since this flow has no separate unload page for
 * an operator to click one on.
 *
 * <p>{@code m10.unload-bay-complete} (closing these rows + reversing stock)
 * is still invoked explicitly from aria:weighment-capture's own UI once the
 * operator confirms each line's unload quantity — this listener only opens
 * the rows, exactly as {@code TankerUnloadInvoiceTransitionListener} only
 * opens Inbound's.
 *
 * <p>One-time seed per instance (this edge fires once per reject cycle in
 * the current design) — guarded on no unloading rows existing yet for this
 * instance, same reasoning as {@link QcPreloadTankAllocationListener}.
 */
@Component
public class TankerOutboundUnloadSeedListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(TankerOutboundUnloadSeedListener.class);
    private static final String WORKFLOW_CODE = "M10.TankerOutbound";
    private static final String FROM_STATE = "RELOAD_REQUIRED";
    private static final String TO_STATE = "UNLOADING_WEIGHMENT";

    private final JdbcTemplate jdbc;

    public TankerOutboundUnloadSeedListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> submittedDataBag) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode())) return;
        if (!FROM_STATE.equals(previousState) || !TO_STATE.equals(instance.getCurrentState())) return;

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();

        try {
            Integer alreadySeeded = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ?",
                    Integer.class, tenantId, instanceId);
            if (alreadySeeded != null && alreadySeeded > 0) return;

            List<Map<String, Object>> loadedRows = jdbc.queryForList(
                    "SELECT route_id, order_id, sku_code, sku_desc, bay_name, quantity "
                            + "FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'DONE'",
                    tenantId, instanceId);
            if (loadedRows.isEmpty()) {
                log.warn("tanker-outbound-unload-seed: no DONE loading rows for instance {} — nothing to unload", instanceId);
                return;
            }

            Map<String, Object> parent = jdbc.queryForMap(
                    "SELECT vehicle_no, gate_pass_no FROM loading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                    tenantId, instanceId);
            String vehicleNo = (String) parent.get("vehicle_no");
            String gatePassNo = (String) parent.get("gate_pass_no");

            BigDecimal totalQty = BigDecimal.ZERO;
            for (Map<String, Object> r : loadedRows) {
                totalQty = totalQty.add((BigDecimal) r.get("quantity"));
            }

            jdbc.update(
                    "INSERT INTO unloading_progress_detail (tenant_id, workflow_instance_id, flow_variant_code, current_step, vehicle_no, gate_pass_no, total_quantity, processed_quantity, status) "
                            + "VALUES (?,?,?,?,?,?,?,0,'IN_PROGRESS') "
                            + "ON DUPLICATE KEY UPDATE "
                            + "flow_variant_code = COALESCE(unloading_progress_detail.flow_variant_code, VALUES(flow_variant_code)), "
                            + "current_step = VALUES(current_step), "
                            + "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), "
                            + "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no), "
                            + "total_quantity = total_quantity + VALUES(total_quantity)",
                    tenantId, instanceId, WORKFLOW_CODE, instance.getCurrentState(), vehicleNo, gatePassNo, totalQty);

            Long parentId = jdbc.queryForObject(
                    "SELECT id FROM unloading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                    Long.class, tenantId, instanceId);

            int written = 0;
            for (Map<String, Object> r : loadedRows) {
                jdbc.update(
                        "INSERT INTO unloading_progress_detail_bay "
                                + "(tenant_id, unloading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, status) "
                                + "VALUES (?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                        tenantId, parentId, instanceId, r.get("route_id"), r.get("order_id"), r.get("sku_code"),
                        r.get("sku_desc"), r.get("bay_name"), r.get("quantity"));

                int flagged = jdbc.update(
                        "UPDATE loading_progress_detail_bay SET status = 'UNLOAD_IN_PROGRESS' "
                                + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                                + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'DONE'",
                        tenantId, instanceId, r.get("route_id"), r.get("order_id"), r.get("order_id"), r.get("sku_code"));
                if (flagged > 0) written++;
            }

            log.info("tanker-outbound-unload-seed: seeded {}/{} unload row(s) for instance {} (vehicle={}, totalQty={})",
                    written, loadedRows.size(), instanceId, vehicleNo, totalQty);
        } catch (Exception e) {
            log.warn("tanker-outbound-unload-seed failed for instance {}: {}", instanceId, e.getMessage());
        }
    }
}
