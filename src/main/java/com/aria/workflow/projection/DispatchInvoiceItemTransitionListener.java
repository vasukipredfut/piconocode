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
 * dispatch_invoice_item child rows driven by aria:dispatch-vehicle-details'
 * invoice-items grid — one row per invoice/product line, capturing a
 * vehicle's multiple invoices (each its own vendor) each with multiple
 * SKU+qty lines.
 *
 * <p>Kept as its own listener (like {@link BillingOrderDetailTransitionListener})
 * rather than a generic {@code m0_workflow_projection} config row, because the
 * submitted {@code invoice_items} field is an ARRAY — the generic projection
 * mechanism only ever writes one row per instance per table, it has no
 * fan-out from a list to N child rows.
 */
@Component
public class DispatchInvoiceItemTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(DispatchInvoiceItemTransitionListener.class);
    private static final String PAGE_CODE = "aria:dispatch-vehicle-details";

    private final JdbcTemplate jdbc;

    public DispatchInvoiceItemTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null) return;
        Object pageDataRaw = bag.get(PAGE_CODE);
        if (!(pageDataRaw instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;
        Object linesRaw = pageData.get("invoice_items");
        if (!(linesRaw instanceof List<?> lines) || lines.isEmpty()) return;

        long tenantId = TenantContext.get();
        String vehicleNo = str(pageData, "vehicleNo", "vehicle_no");
        String gatePassNo = str(pageData, "gpNumber", "gp_number");

        // submitted_data_json is cumulative for the instance's whole lifetime
        // (WorkflowEngineService keys it by pageCode and never clears a key),
        // and onTransition fires on EVERY later transition of this instance —
        // not just the one where this page actually submitted. Without this,
        // the same invoice_items list would re-insert as duplicate rows on
        // every subsequent weighment/QC/etc. transition. Delete-then-insert
        // makes this idempotent: converges to "current invoice_items for this
        // instance" no matter how many times it fires.
        jdbc.update("DELETE FROM dispatch_invoice_item WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instance.getId());

        int written = 0;
        for (Object lineRaw : lines) {
            if (!(lineRaw instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> line = (Map<String, Object>) lineRaw;
            try {
                jdbc.update(
                        "INSERT INTO dispatch_invoice_item (tenant_id, workflow_instance_id, vehicle_no, "
                                + "gate_pass_no, invoice_no, order_id, vendor_name, rd_code, sku_code, sku_name, qty) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                        tenantId, instance.getId(), vehicleNo, gatePassNo,
                        str(line, "invoice_no", "invoiceNo"), str(line, "order_id", "orderId"),
                        str(line, "vendor_name", "vendorName"), str(line, "rd_code", "rdCode"),
                        str(line, "sku_code", "skuCode"), str(line, "sku_name", "skuName"), num(line, "qty"));
                written++;
            } catch (Exception e) {
                // One bad line must not sink the rest — insert what we can, log what we can't.
                log.warn("dispatch_invoice_item insert failed for instance {} invoice_no={}: {}",
                        instance.getId(), line.get("invoice_no"), e.getMessage());
            }
        }

        writeAudit(tenantId, instance, vehicleNo, gatePassNo, written, lines.size());
        log.info("dispatch_invoice_item: wrote {}/{} row(s) for instance {} (vehicle={})",
                written, lines.size(), instance.getId(), vehicleNo);
    }

    private void writeAudit(long tenantId, WorkflowInstance instance, String vehicleNo, String gatePassNo,
                             int written, int total) {
        try {
            jdbc.update(
                    "INSERT INTO m0_audit_log (tenant_id, ts, actor_id, action_kind, entity_code, "
                            + "workflow_code, workflow_instance_id, vehicle_no, gp_number, message) "
                            + "VALUES (?, CURRENT_TIMESTAMP, ?, 'DISPATCH_INVOICE_ITEM_CAPTURED', 'DispatchInvoiceItem', "
                            + "?, ?, ?, ?, ?)",
                    tenantId, currentUserId(), instance.getWorkflowCode(), instance.getId(), vehicleNo, gatePassNo,
                    "Captured " + written + "/" + total + " invoice/SKU line(s) via " + PAGE_CODE);
        } catch (Exception e) {
            // Audit-log writes must never break the mutation that triggered them.
            log.warn("dispatch_invoice_item audit-log write failed for instance {}: {}",
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
