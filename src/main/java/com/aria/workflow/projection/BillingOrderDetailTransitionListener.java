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
 * billing_order_details child rows driven by aria:skipped-item-allocation's
 * "Loaded SKU Details" grid — one row per order/SKU line, capturing what
 * was actually loaded vs skipped for that loading transaction.
 *
 * <p>Kept as its own listener (like {@link ParkingBayEntryTransitionListener})
 * rather than a generic {@code m0_workflow_projection} config row, because
 * the submitted {@code loaded_sku_details} field is an ARRAY — the generic
 * projection mechanism only ever writes one row per instance per table, it
 * has no fan-out from a list to N child rows.
 *
 * <p>Not scoped to a single workflow code — aria:skipped-item-allocation is
 * reused across M10 flow variants (its own table migration references
 * M10.PackUnload; seen live in production use on M10.FGLoading too), so this
 * only gates on a submitted {@code loaded_sku_details} list being present
 * under this page's key in the bag.
 *
 * <p>V192 created {@code billing_order_details} + its entity registry rows
 * but explicitly deferred this write-side wiring ("pending confirmation of
 * the intended FSM placement") — this listener is that follow-up.
 */
@Component
public class BillingOrderDetailTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(BillingOrderDetailTransitionListener.class);
    private static final String PAGE_CODE = "aria:skipped-item-allocation";

    private final JdbcTemplate jdbc;

    public BillingOrderDetailTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null) return;
        Object pageDataRaw = bag.get(PAGE_CODE);
        if (!(pageDataRaw instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;
        Object linesRaw = pageData.get("loaded_sku_details");
        if (!(linesRaw instanceof List<?> lines) || lines.isEmpty()) return;

        long tenantId = TenantContext.get();
        String vehicleNo = str(pageData, "vehicle_no");
        String gatePassNo = str(pageData, "gpNumber", "gp_number");

        int written = 0;
        for (Object lineRaw : lines) {
            if (!(lineRaw instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> line = (Map<String, Object>) lineRaw;
            try {
                jdbc.update(
                        "INSERT INTO billing_order_details (tenant_id, workflow_instance_id, vehicle_no, "
                                + "gate_pass_no, order_id, rd_code, rd_desc, sku_code, sku_desc, order_qty, "
                                + "loading_qty, skipped_quantity) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                        tenantId, instance.getId(), vehicleNo, gatePassNo,
                        str(line, "order_id"), str(line, "rd_code"), str(line, "rd_desc"),
                        str(line, "sku_code"), str(line, "sku_desc"),
                        num(line, "order_qty"), num(line, "loading_qty"), num(line, "skipped_quantity"));
                written++;
            } catch (Exception e) {
                // One bad line must not sink the rest — insert what we can, log what we can't.
                log.warn("billing_order_details insert failed for instance {} line order_id={}: {}",
                        instance.getId(), line.get("order_id"), e.getMessage());
            }
        }

        writeAudit(tenantId, instance, vehicleNo, gatePassNo, written, lines.size());
        log.info("billing_order_details: wrote {}/{} row(s) for instance {} (vehicle={})",
                written, lines.size(), instance.getId(), vehicleNo);
    }

    private void writeAudit(long tenantId, WorkflowInstance instance, String vehicleNo, String gatePassNo,
                             int written, int total) {
        try {
            jdbc.update(
                    "INSERT INTO m0_audit_log (tenant_id, ts, actor_id, action_kind, entity_code, "
                            + "workflow_code, workflow_instance_id, vehicle_no, gp_number, message) "
                            + "VALUES (?, CURRENT_TIMESTAMP, ?, 'BILLING_ORDER_DETAIL_CAPTURED', 'BillingOrderDetail', "
                            + "?, ?, ?, ?, ?)",
                    tenantId, currentUserId(), instance.getWorkflowCode(), instance.getId(), vehicleNo, gatePassNo,
                    "Captured " + written + "/" + total + " loaded/skipped SKU line(s) via " + PAGE_CODE);
        } catch (Exception e) {
            // Audit-log writes must never break the mutation that triggered them.
            log.warn("billing_order_details audit-log write failed for instance {}: {}",
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
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
