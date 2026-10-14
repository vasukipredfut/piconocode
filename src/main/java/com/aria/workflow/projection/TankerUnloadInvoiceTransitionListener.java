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
 * <p><b>Multiple tank locations per product:</b> a line may split its qty
 * across more than one tank — carried as a nested {@code tank_allocations}
 * array per line, {@code [{"tank_location":"ROT-1","qty":60}, {"tank_location":"ROT-2","qty":40}]}.
 * When present (non-empty), one {@code unloading_progress_detail_bay} row is
 * written per allocation entry (each with its own {@code bay_name}/{@code
 * quantity}) instead of one row for the whole line. A line with no {@code
 * tank_allocations} falls back to the original single {@code tank_location}
 * + whole-line {@code qty} behavior unchanged — existing submissions that
 * never adopt the nested shape keep working exactly as before.
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
    private static final String LOADING_LIST_FIELD = "loadingInvoices";

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

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();

        // "Load again" after the unload: the invoice lines the vehicle is loaded with next (loadingInvoices, same shape
        // as unloadingInvoices) go to loading_progress_detail(_bay). Independent of the unload list below, which may be empty.
        seedLoading(instance, pageData, tenantId, instanceId);

        if (!(linesRaw instanceof List<?> lines) || lines.isEmpty()) return;

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
            Object invoiceNo = str(line, "invoice_no", "invoiceNo");
            Object orderId = str(line, "order_id", "orderId");
            Object vendorName = str(line, "vendor_name", "vendorName");
            Object rdCode = str(line, "rd_code", "rdCode");
            Object skuCode = str(line, "sku_code", "skuCode");
            Object skuName = str(line, "sku_name", "skuName");
            // Per-line Diff / Diff Reason (V361): same value on every tank row the line splits into.
            BigDecimal diffQty = numOrNull(line, "diff_box", "diff_qty");
            String diffReason = str(line, "diff_reason", "diffReason");

            // Multiple tank locations for one product: a nested allocations
            // array overrides the line's own single tank_location/qty, one
            // output row per allocation entry (see class javadoc).
            Object allocationsRaw = line.get("tank_allocations");
            if (allocationsRaw instanceof List<?> allocations && !allocations.isEmpty()) {
                for (Object allocRaw : allocations) {
                    if (!(allocRaw instanceof Map)) continue;
                    Map<String, Object> alloc = (Map<String, Object>) allocRaw;
                    BigDecimal allocQty = num(alloc, "qty");
                    rows.add(new Object[]{
                            invoiceNo, orderId, vendorName, rdCode, skuCode, skuName,
                            str(alloc, "tank_location", "tankLocation"), allocQty, diffQty, diffReason
                    });
                    totalQty = totalQty.add(allocQty);
                }
            } else {
                BigDecimal qty = num(line, "qty");
                rows.add(new Object[]{
                        invoiceNo, orderId, vendorName, rdCode, skuCode, skuName,
                        str(line, "tank_location", "tankLocation"), qty, diffQty, diffReason
                });
                totalQty = totalQty.add(qty);
            }
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
                                + "invoice_no, order_id, vendor_name, rd_code, sku_code, sku_desc, bay_name, quantity, status, diff_qty, diff_reason) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?, 'IN_PROGRESS', ?, ?)",
                        tenantId, parentId, instanceId, r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9]);
                written++;
            } catch (Exception e) {
                log.warn("unloading_progress_detail_bay insert failed for instance {} invoice_no={}: {}", instanceId, r[0], e.getMessage());
            }
        }

        log.info("unloading_progress_detail (tanker): wrote {}/{} row(s) for instance {} (vehicle={}, totalQty={})",
                written, rows.size(), instanceId, vehicleNo, totalQty);
    }

    /**
     * {@code loadingInvoices} (aria:qc-approval, M10.TankerInbound "load again"): one {@code loading_progress_detail_bay}
     * row per tank allocation -- invoice_no / order_id / vendor_name / rd_code / sku_code / sku_desc (the line's sku_name) /
     * bay_name (tank) / quantity, status IN_PROGRESS, route_id NULL (an invoice line has none) -- under the
     * {@code loading_progress_detail} parent, the same way the tanker-outbound QC preload seeds its loading rows.
     *
     * <p>Re-running replaces this instance's not-yet-started invoice rows (IN_PROGRESS, nothing processed) and recomputes the
     * parent total, so a re-submitted page never duplicates them; rows already weighed (DONE) are left alone.
     */
    @SuppressWarnings("unchecked")
    private void seedLoading(WorkflowInstance instance, Map<String, Object> pageData, long tenantId, Long instanceId) {
        Object raw = pageData.get(LOADING_LIST_FIELD);
        if (!(raw instanceof List<?> lines) || lines.isEmpty()) return;
        try {
            List<Object[]> rows = new ArrayList<>();
            for (Object lineRaw : lines) {
                if (!(lineRaw instanceof Map)) continue;
                Map<String, Object> line = (Map<String, Object>) lineRaw;
                Object skuCode = str(line, "sku_code", "skuCode");
                if (skuCode == null) continue;
                Object invoiceNo = str(line, "invoice_no", "invoiceNo");
                Object orderId = str(line, "order_id", "orderId");
                Object vendorName = str(line, "vendor_name", "vendorName");
                Object rdCode = str(line, "rd_code", "rdCode");
                Object skuName = str(line, "sku_name", "skuName");
                Object allocationsRaw = line.get("tank_allocations");
                if (allocationsRaw instanceof List<?> allocations && !allocations.isEmpty()) {
                    for (Object allocRaw : allocations) {
                        if (!(allocRaw instanceof Map)) continue;
                        Map<String, Object> alloc = (Map<String, Object>) allocRaw;
                        String tank = str(alloc, "tank_location", "tankLocation");
                        BigDecimal qty = num(alloc, "qty");
                        if (tank == null || qty.signum() <= 0) continue;
                        rows.add(new Object[]{invoiceNo, orderId, vendorName, rdCode, skuCode, skuName, tank, qty});
                    }
                } else {
                    String tank = str(line, "tank_location", "tankLocation");
                    BigDecimal qty = num(line, "qty");
                    if (tank != null && qty.signum() > 0) {
                        rows.add(new Object[]{invoiceNo, orderId, vendorName, rdCode, skuCode, skuName, tank, qty});
                    }
                }
            }
            if (rows.isEmpty()) {
                log.warn("tanker load-again: instance={} got {} loadingInvoices line(s) but none had a tank allocation -- nothing seeded", instanceId, lines.size());
                return;
            }

            String vehicleNo = str(pageData, "vehicleNo", "vehicle_no");
            String gatePassNo = str(pageData, "gpNumber", "gp_number");
            if (vehicleNo == null || gatePassNo == null) {
                List<Map<String, Object>> gp = jdbc.queryForList(
                        "SELECT vehicle_no, gp_number FROM gate_pass WHERE workflow_instance_id = ? AND tenant_id = ?", instanceId, tenantId);
                if (!gp.isEmpty()) {
                    if (vehicleNo == null && gp.get(0).get("vehicle_no") != null) vehicleNo = String.valueOf(gp.get(0).get("vehicle_no"));
                    if (gatePassNo == null && gp.get(0).get("gp_number") != null) gatePassNo = String.valueOf(gp.get(0).get("gp_number"));
                }
            }

            jdbc.update("DELETE FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND invoice_no IS NOT NULL AND status = 'IN_PROGRESS' AND (processed_quantity IS NULL OR processed_quantity = 0)",
                    tenantId, instanceId);
            jdbc.update(
                    "INSERT INTO loading_progress_detail (tenant_id, workflow_instance_id, vehicle_no, gate_pass_no, flow_variant_code, current_step, total_quantity, processed_quantity, loading_start_time) "
                            + "VALUES (?, ?, ?, ?, ?, ?, 0, 0, CURRENT_TIMESTAMP) "
                            + "ON DUPLICATE KEY UPDATE "
                            + "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), "
                            + "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no), "
                            + "flow_variant_code = COALESCE(flow_variant_code, VALUES(flow_variant_code)), "
                            + "current_step = VALUES(current_step), "
                            + "loading_start_time = COALESCE(loading_start_time, VALUES(loading_start_time))",
                    tenantId, instanceId, vehicleNo, gatePassNo, instance.getWorkflowCode(), instance.getCurrentState());
            Long parentId = jdbc.queryForObject(
                    "SELECT id FROM loading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?", Long.class, tenantId, instanceId);

            int written = 0;
            for (Object[] r : rows) {
                jdbc.update(
                        "INSERT INTO loading_progress_detail_bay (tenant_id, loading_progress_detail_id, workflow_instance_id, "
                                + "invoice_no, order_id, vendor_name, rd_code, sku_code, sku_desc, bay_name, quantity, status) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                        tenantId, parentId, instanceId, r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]);
                written++;
            }
            // recompute (not add) so a re-run converges to the same total
            jdbc.update("UPDATE loading_progress_detail SET total_quantity = (SELECT COALESCE(SUM(b.quantity), 0) FROM loading_progress_detail_bay b "
                            + "WHERE b.tenant_id = ? AND b.workflow_instance_id = ? AND b.status <> 'REJECTED') "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                    tenantId, instanceId, tenantId, instanceId);
            log.info("tanker load-again: instance={} wrote {} IN_PROGRESS loading_progress_detail_bay row(s)", instanceId, written);
        } catch (Exception e) {
            log.warn("tanker load-again seeding failed for instance={}: {}", instanceId, e.getMessage());
        }
    }

    private static String str(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
        }
        return null;
    }

    /** First present key parsed as a number, or null when absent/blank/non-numeric. */
    private static BigDecimal numOrNull(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v == null || String.valueOf(v).isBlank()) continue;
            if (v instanceof BigDecimal bd) return bd;
            try {
                return new BigDecimal(String.valueOf(v).trim());
            } catch (NumberFormatException e) {
                return null;
            }
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
