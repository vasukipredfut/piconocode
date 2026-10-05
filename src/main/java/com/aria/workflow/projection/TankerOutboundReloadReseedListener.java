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
 * On {@code M10.TankerOutbound}'s {@code UNLOADING_WEIGHMENT -> RELOAD_LOADING}
 * transition, clones each {@code REVERSED} {@code loading_progress_detail_bay}
 * row for this instance into a fresh {@code IN_PROGRESS} row with the same
 * {@code route_id}/{@code order_id}/{@code sku_code}/{@code sku_desc}/
 * {@code bay_name} (same tank)/{@code quantity} — so {@code
 * TankerWeighmentSkuCaptureActionHandler}'s {@code m10.tanker-weighment-capture}
 * (which only matches rows in status {@code IN_PROGRESS}) has something to
 * close again on reload, without re-running {@code QcPreloadTankAllocationListener}
 * (deliberately bypassed here — that listener's own idempotency guard treats
 * "any row already exists for this instance" as "already seeded," so it
 * would never reseed once the REVERSED rows from the first load are present;
 * routing reload through {@code QC_PRELOAD_APPROVED} again would silently
 * produce nothing to weigh against).
 *
 * <p>{@code loading_progress_detail_bay.quantity} is never touched on the
 * REVERSED row itself (same "never touch the original loaded numbers" rule
 * {@code M10UnloadBayCompleteActionHandler}'s class doc states) — this
 * listener only ever inserts new rows, never mutates the REVERSED ones.
 * {@code loading_progress_detail}'s own {@code total_quantity}/{@code
 * processed_quantity} roll-ups are also left untouched here, consistent with
 * the same reject-flow precedent: neither is decremented on reversal, so
 * neither is re-incremented here either — the parent stays a cumulative
 * ledger across the reject+reload cycle, not a live balance.
 *
 * <p>Idempotent per line: skips a (route_id, order_id, sku_code) triple that
 * already has an {@code IN_PROGRESS} row (handles a re-delivered event
 * without creating duplicate open rows).
 */
@Component
public class TankerOutboundReloadReseedListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(TankerOutboundReloadReseedListener.class);
    private static final String WORKFLOW_CODE = "M10.TankerOutbound";
    private static final String FROM_STATE = "UNLOADING_WEIGHMENT";
    private static final String TO_STATE = "RELOAD_LOADING";

    private final JdbcTemplate jdbc;

    public TankerOutboundReloadReseedListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> submittedDataBag) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode())) return;
        if (!FROM_STATE.equals(previousState) || !TO_STATE.equals(instance.getCurrentState())) return;

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();

        try {
            List<Map<String, Object>> reversedRows = jdbc.queryForList(
                    "SELECT loading_progress_detail_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity "
                            + "FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'REVERSED'",
                    tenantId, instanceId);
            if (reversedRows.isEmpty()) {
                log.warn("tanker-outbound-reload-reseed: no REVERSED loading rows for instance {} — nothing to reseed", instanceId);
                return;
            }

            int seeded = 0;
            for (Map<String, Object> r : reversedRows) {
                Object routeId = r.get("route_id");
                Object orderId = r.get("order_id");
                Object skuCode = r.get("sku_code");

                Integer alreadyOpen = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                                + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                        Integer.class, tenantId, instanceId, routeId, orderId, orderId, skuCode);
                if (alreadyOpen != null && alreadyOpen > 0) continue;

                jdbc.update(
                        "INSERT INTO loading_progress_detail_bay "
                                + "(tenant_id, loading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, status) "
                                + "VALUES (?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                        tenantId, r.get("loading_progress_detail_id"), instanceId, routeId, orderId, skuCode,
                        r.get("sku_desc"), r.get("bay_name"), r.get("quantity"));
                seeded++;
            }

            log.info("tanker-outbound-reload-reseed: reseeded {}/{} loading row(s) for instance {}",
                    seeded, reversedRows.size(), instanceId);
        } catch (Exception e) {
            log.warn("tanker-outbound-reload-reseed failed for instance {}: {}", instanceId, e.getMessage());
        }
    }
}
