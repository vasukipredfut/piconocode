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
 * Snapshots aria:qc-approval's "Unloading Invoices" list (each row backed by
 * {@code dispatch_invoice_item}, plus whatever {@code tank_location} the QC
 * officer picked per line — the only new information this page's ListView
 * adds) into {@code unloading_progress_detail} (parent) / {@code
 * unloading_progress_detail_bay} (child) at {@code IN_PROGRESS}, the moment
 * QC approval is submitted.
 *
 * <p>Shares these two tables with the FG/PackLoading Reject Flow Unload
 * process ({@code M10StartUnloadBayActionHandler} / {@code
 * M10UnloadBayCompleteActionHandler}) rather than a dedicated table pair —
 * one "unloading" concept, one place to look (V258). On a row written by
 * THIS listener, {@code route_id}/{@code order_id} stay NULL (those are the
 * reject-unload process's own fields); this listener only ever populates
 * {@code invoice_no}/{@code vendor_name}/{@code rd_code} plus the fields
 * both processes share ({@code sku_code}, {@code sku_desc} — tanker's
 * {@code sku_name} maps onto this same column, {@code quantity}, {@code
 * status}). {@code bay_name} is reused for the tank location the QC officer
 * picked — same precedent as {@code M10StartBayActionHandler}'s
 * M10.TankerOutbound handling (no separate tank_location column, see V258).
 * The two processes can never collide on the same {@code
 * workflow_instance_id} — they belong to disjoint workflow codes
 * (M10.FGLoading/PackLoading vs M10.TankerInbound).
 *
 * <p>Scoped to {@code M10.TankerInbound}'s {@code DISPATCH_QUEUED ->
 * QC_APPROVED} edge specifically (not every {@code aria:qc-approval}
 * submission across every workflow that reuses this page, e.g. PackUnload /
 * ReturnStockIn — this ListView itself is {@code visibleWhen
 * flow_variant_code == 'M10.TankerInbound'}, so other workflows never
 * populate it).
 *
 * <p>Deliberately does NOT close/finalize these rows — that happens later
 * via the explicit {@code m10.tanker-unload-receipt} action (weighment
 * operator enters an actual weight per product), never automatically off a
 * page-submit transition. See {@code M10TankerUnloadReceiptActionHandler}.
 *
 * <p>Same delete-then-insert idempotency as {@link DispatchInvoiceItemTransitionListener}
 * — the submitted-data bag is cumulative and {@code onTransition} could in
 * principle fire again for this instance; converges to "current
 * unloadingInvoices for this instance" regardless of how many times it runs.
 */
@Component
public class TankerUnloadInvoiceTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(TankerUnloadInvoiceTransitionListener.class);
    private static final String WORKFLOW_CODE = "M10.TankerInbound";
    private static final String TO_STATE = "QC_APPROVED";
    private static final String PAGE_CODE = "aria:qc-approval";
    private static final String LIST_FIELD = "unloadingInvoices";

    private final JdbcTemplate jdbc;

    public TankerUnloadInvoiceTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode()) || !TO_STATE.equals(instance.getCurrentState())) return;
        if (bag == null) return;
        Object pageDataRaw = bag.get(PAGE_CODE);
        if (!(pageDataRaw instanceof Map)) return;
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;
        Object linesRaw = pageData.get(LIST_FIELD);
        if (!(linesRaw instanceof List<?> lines) || lines.isEmpty()) return;

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();

        // Idempotent re-run: this instance's prior snapshot (if any) is fully
        // replaced, same reasoning as DispatchInvoiceItemTransitionListener.
        // Scoped to invoice_no NOT NULL so this never touches a reject-unload
        // row for the same instance (impossible in practice — see class
        // javadoc — but the guard costs nothing and documents the intent).
        jdbc.update("DELETE FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND invoice_no IS NOT NULL",
                tenantId, instanceId);
        jdbc.update("DELETE FROM unloading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instanceId);

        String vehicleNo = str(pageData, "vehicleNo", "vehicle_no");
        String gatePassNo = str(pageData, "gpNumber", "gp_number");

        BigDecimal totalQty = BigDecimal.ZERO;
        int written = 0;
        List<Object[]> rows = new ArrayList<>();
        for (Object lineRaw : lines) {
            if (!(lineRaw instanceof Map)) continue;
            Map<String, Object> line = (Map<String, Object>) lineRaw;
            BigDecimal qty = num(line, "qty");
            rows.add(new Object[]{
                    str(line, "invoice_no", "invoiceNo"), str(line, "order_id", "orderId"),
                    str(line, "vendor_name", "vendorName"), str(line, "rd_code", "rdCode"),
                    str(line, "sku_code", "skuCode"), str(line, "sku_name", "skuName"),
                    str(line, "tank_location", "tankLocation"), qty
            });
            totalQty = totalQty.add(qty);
        }

        jdbc.update(
                "INSERT INTO unloading_progress_detail (tenant_id, workflow_instance_id, vehicle_no, gate_pass_no, total_quantity, processed_quantity, status) "
                        + "VALUES (?,?,?,?,?,0,'IN_PROGRESS')",
                tenantId, instanceId, vehicleNo, gatePassNo, totalQty);
        Long parentId = jdbc.queryForObject(
                "SELECT id FROM unloading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                Long.class, tenantId, instanceId);

        for (Object[] r : rows) {
            try {
                jdbc.update(
                        "INSERT INTO unloading_progress_detail_bay (tenant_id, unloading_progress_detail_id, workflow_instance_id, "
                                + "invoice_no, order_id, vendor_name, rd_code, sku_code, sku_desc, bay_name, quantity, status) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                        tenantId, parentId, instanceId, r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]);
                written++;
            } catch (Exception e) {
                log.warn("unloading_progress_detail_bay insert failed for instance {} invoice_no={}: {}", instanceId, r[0], e.getMessage());
            }
        }

        log.info("unloading_progress_detail (tanker): wrote {}/{} row(s) for instance {} (vehicle={}, totalQty={})",
                written, rows.size(), instanceId, vehicleNo, totalQty);
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
