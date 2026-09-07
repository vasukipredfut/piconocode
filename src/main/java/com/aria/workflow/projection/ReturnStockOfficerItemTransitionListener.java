package com.aria.workflow.projection;

import com.aria.auth.AuthUserDetails;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * return_stock_officer_item child rows driven by the "Batch / SKU / Qty /
 * Good & Expiry Stock" grid on aria:return-stock-officer (M10.ReturnStockIn
 * only) -- one row per batch/SKU line, mirroring
 * {@link DispatchInvoiceItemTransitionListener}'s exact shape and reasoning:
 * the submitted lines are an ARRAY, which the generic m0_workflow_projection
 * mechanism can't fan out into N child rows.
 *
 * <p>PAGE_CODE is aria:return-stock-officer, a dedicated page (see V300).
 * Earlier builds mistakenly reused aria:dispatch-vehicle-details a second
 * time for this step (submitted again at WEIGHMENT_1); that meant no
 * distinct Return Stock Officer queue and the grid appearing on the wrong
 * page. V300 moved the grid to its own page and repointed the
 * WEIGHMENT_1 -> RETURN_STOCK_OFFICER transition's pageCode to match.
 * Scoped to M10.ReturnStockIn only since that's the only workflow this
 * field exists on.
 *
 * <p>Delete-then-insert per instance, same idempotency reasoning as
 * DispatchInvoiceItemTransitionListener: submitted_data_json is cumulative
 * for the instance's whole lifetime and this listener's onTransition fires on
 * every later transition too, not just the one where this page submitted.
 */
@Component
public class ReturnStockOfficerItemTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ReturnStockOfficerItemTransitionListener.class);
    private static final String PAGE_CODE = "aria:return-stock-officer";

    private final JdbcTemplate jdbc;

    public ReturnStockOfficerItemTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null || !"M10.ReturnStockIn".equals(instance.getWorkflowCode())) return;
        Object pageDataRaw = bag.get(PAGE_CODE);
        if (!(pageDataRaw instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;
        Object linesRaw = pageData.get("return_stock_items");
        if (!(linesRaw instanceof List<?> lines) || lines.isEmpty()) return;

        long tenantId = TenantContext.get();
        String vehicleNo = str(pageData, "vehicleNo", "vehicle_no");
        String gatePassNo = str(pageData, "gpNumber", "gp_number");

        jdbc.update("DELETE FROM return_stock_officer_item WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instance.getId());

        int written = 0;
        for (Object lineRaw : lines) {
            if (!(lineRaw instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> line = (Map<String, Object>) lineRaw;
            try {
                jdbc.update(
                        "INSERT INTO return_stock_officer_item (tenant_id, workflow_instance_id, vehicle_no, "
                                + "gate_pass_no, batch_no, sku_code, sku_name, qty, good_stock_qty, expiry_stock_qty) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?)",
                        tenantId, instance.getId(), vehicleNo, gatePassNo,
                        str(line, "batch_no", "batchNo"), str(line, "sku_code", "skuCode"),
                        str(line, "sku_name", "skuName"), num(line, "qty"),
                        num(line, "good_stock_qty"), num(line, "expiry_stock_qty"));
                written++;
            } catch (Exception e) {
                log.warn("return_stock_officer_item insert failed for instance {} batch_no={}: {}",
                        instance.getId(), line.get("batch_no"), e.getMessage());
            }
        }

        writeAudit(tenantId, instance, vehicleNo, gatePassNo, written, lines.size());
        log.info("return_stock_officer_item: wrote {}/{} row(s) for instance {} (vehicle={})",
                written, lines.size(), instance.getId(), vehicleNo);
    }

    private void writeAudit(long tenantId, WorkflowInstance instance, String vehicleNo, String gatePassNo,
                             int written, int total) {
        try {
            jdbc.update(
                    "INSERT INTO m0_audit_log (tenant_id, ts, actor_id, action_kind, entity_code, "
                            + "workflow_code, workflow_instance_id, vehicle_no, gp_number, message) "
                            + "VALUES (?, CURRENT_TIMESTAMP, ?, 'RETURN_STOCK_ITEM_CAPTURED', 'ReturnStockOfficerItem', "
                            + "?, ?, ?, ?, ?)",
                    tenantId, currentUserId(), instance.getWorkflowCode(), instance.getId(), vehicleNo, gatePassNo,
                    "Captured " + written + "/" + total + " return-stock batch/SKU line(s) via " + PAGE_CODE);
        } catch (Exception e) {
            log.warn("return_stock_officer_item audit-log write failed for instance {}: {}",
                    instance.getId(), e.getMessage());
        }
    }

    private static Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return null;
        Object p = a.getPrincipal();
        return p instanceof AuthUserDetails uid ? uid.getId() : null;
    }

    private static String str(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
        }
        return null;
    }

    private static BigDecimal num(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
