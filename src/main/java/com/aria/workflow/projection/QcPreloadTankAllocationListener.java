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
 * under {@code tankAllocation} (confirmed off a live transition payload —
 * one object per line, the underlying OrderDispatchDetail row's
 * {@code route_id}/{@code order_id}/{@code sku_code}/{@code sku_desc}/
 * {@code load_wgt} plus whatever tank got picked for it, expected under
 * {@code tank_location} — {@code order_sku_line_items},
 * {@link com.aria.action.handler.M10StartBayActionHandler}'s own convention,
 * is accepted as a fallback name). That lands in
 * {@code WorkflowInstance.submittedDataJson} keyed by pageCode — this
 * listener reads it back from there, never from a table. A line with no
 * {@code tank_location} (or {@code bay_name}) is skipped — nothing to seed
 * for it yet. {@code order_id} (V252) is stored alongside {@code route_id}
 * on the seeded row, whichever this grid's rows carry — see
 * {@code TankerWeighmentSkuCaptureActionHandler}, which must echo the same
 * value back to close it.
 *
 * <p><b>Multiple tank locations per product:</b> the page's "Order Details —
 * Tank Allocation" section is a {@code BatchAllocationTable} — {@code
 * tankAllocation} (this method's {@code linesObj}) carries the PARENT rows
 * (one per OrderDispatchDetail line, each with its own {@code id}), and a
 * SEPARATE sibling list, {@code tankLocationAllocations}, carries the child
 * allocation rows: {@code order_dispatch_detail_id} (the parent's {@code id}
 * — {@code allocationParentField}), {@code tank_location}, {@code alloc_qty}
 * (confirmed off a live transition payload; NOT {@code qty} — that key
 * belongs to the parent's own, whole-line order quantity). Allocations are
 * grouped by {@code order_dispatch_detail_id} (string-compared, since a JSON
 * id can deserialize as Integer/Long/String depending on what submitted it)
 * and matched back to each parent line by its own {@code id}; one {@code
 * loading_progress_detail_bay} row is written per matched allocation. A
 * parent line with no matching allocations falls back to its own single
 * {@code tank_location}/{@code bay_name} + whole-line quantity (the
 * pre-BatchAllocationTable shape) unchanged — so an older/unmigrated
 * submission still seeds correctly.
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
 * separate follow-up work, intentionally not done here — see
 * {@code TankerWeighmentSkuCaptureActionHandler}, which also stamps
 * {@code loading_finish_time}. This listener stamps {@code loading_start_time}
 * on the head row at tank-allocation time (loading genuinely begins here for
 * this flow), since {@code WorkflowProjectionListener}'s generic
 * {@code state_ts_columns_json} config maps that column to a
 * LOADING_IN_PROGRESS state this workflow's live FSM never reaches.
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
        // QC_PRELOAD_APPROVED normally; LOADING_COMPLETED on a reload, where the first weighment (W1) is
        // skipped and QC preload approve goes straight to loading (see M10.TankerOutbound's workflow).
        String toState = instance.getCurrentState();
        if (!"QC_PRELOAD_APPROVED".equals(toState) && !"LOADING_COMPLETED".equals(toState)) return;
        if (submittedDataBag == null) return;

        Long tenantId = TenantContext.get();
        Long instanceId = instance.getId();

        try {
            Object pageObj = submittedDataBag.get(PAGE_CODE);
            if (!(pageObj instanceof Map)) return;
            Map<String, Object> page = (Map<String, Object>) pageObj;

            // "tankAllocation" is what aria:qc-preload-approval's "Order Details —
            // Tank Allocation" ListView actually submits (confirmed off a live
            // transition payload); order_sku_line_items (M10StartBayActionHandler's
            // convention) is kept as a fallback in case that naming changes again.
            Object linesObj = firstNonNull(page.get("tankAllocation"), page.get("order_sku_line_items"));
            if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) return;

            // Child allocation rows, grouped by their parent's id (string-keyed
            // — see class javadoc on why). Absent/empty allocationsRaw simply
            // yields an empty map, so every line falls back to its own
            // single-location fields below (pre-BatchAllocationTable shape).
            Map<String, List<Map<String, Object>>> allocationsByParentId = new java.util.HashMap<>();
            Object allocationsRaw = page.get("tankLocationAllocations");
            if (allocationsRaw instanceof List<?> allocList) {
                for (Object allocRaw : allocList) {
                    if (!(allocRaw instanceof Map)) continue;
                    Map<String, Object> alloc = (Map<String, Object>) allocRaw;
                    String parentId = str(alloc.get("order_dispatch_detail_id"));
                    if (parentId == null) continue;
                    allocationsByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(alloc);
                }
            }

            // Diff / Diff Reason are typed once per SKU line on the parent grid row;
            // every tank row seeded for that SKU carries the same values.
            Map<String, Diff> diffByOrderSku = new java.util.HashMap<>();
            Map<String, Diff> diffBySku = new java.util.HashMap<>();
            for (Object raw : (List<?>) linesObj) {
                if (!(raw instanceof Map)) continue;
                Map<String, Object> pl = (Map<String, Object>) raw;
                String sku = str(pl.get("sku_code"));
                Diff d = diffOf(pl);
                if (sku == null || d == null) continue;
                diffByOrderSku.put(str(pl.get("order_id")) + "|" + sku, d);
                diffBySku.put(sku, d);
            }

            List<LineEntry> parsed = new ArrayList<>();
            BigDecimal totalQty = BigDecimal.ZERO;
            java.util.Set<String> consumedParentIds = new java.util.HashSet<>();
            for (Object raw : (List<?>) linesObj) {
                if (!(raw instanceof Map)) continue;
                Map<String, Object> line = (Map<String, Object>) raw;
                String skuCode = str(line.get("sku_code"));
                if (skuCode == null) continue;
                String routeId = str(line.get("route_id"));
                String orderId = str(line.get("order_id"));
                String skuDesc = str(line.get("sku_desc"));

                String lineId = str(line.get("id"));
                List<Map<String, Object>> lineAllocations = lineId == null
                        ? List.of() : allocationsByParentId.getOrDefault(lineId, List.of());

                if (!lineAllocations.isEmpty()) {
                    consumedParentIds.add(lineId);
                    for (Map<String, Object> alloc : lineAllocations) {
                        String allocTank = firstNonBlank(str(alloc.get("tank_location")), str(alloc.get("bay_name")));
                        if (allocTank == null) continue;
                        BigDecimal allocQty = toBig(firstNonNull(alloc.get("alloc_qty"), alloc.get("qty"), alloc.get("quantity")));
                        parsed.add(new LineEntry(routeId, orderId, skuCode, skuDesc, allocTank, allocQty));
                        totalQty = totalQty.add(allocQty);
                    }
                    continue;
                }

                String tankLocation = firstNonBlank(str(line.get("tank_location")), str(line.get("bay_name")));
                if (tankLocation == null) continue; // this line wasn't allocated a tank — skip
                BigDecimal qty = toBig(firstNonNull(line.get("load_wgt"), line.get("quantity"), line.get("order_qty")));
                parsed.add(new LineEntry(routeId, orderId, skuCode, skuDesc, tankLocation, qty));
                totalQty = totalQty.add(qty);
            }
            // Child allocations no parent line claimed by id. The live page's
            // tankAllocation parent lines carry only sku_code/order_id/order_qty
            // (no id, no route_id), so nothing above can match them -- the
            // child row's own order_dispatch_detail_id is the one reliable key,
            // so resolve route_id/order_id/sku_code/sku_desc from that
            // m5_order_dispatch_detail row directly.
            for (Map.Entry<String, List<Map<String, Object>>> e : allocationsByParentId.entrySet()) {
                if (consumedParentIds.contains(e.getKey())) continue;
                Map<String, Object> detail = orderDispatchDetail(tenantId, e.getKey());
                if (detail == null) {
                    log.warn("qc-preload tank allocation: instance={} order_dispatch_detail_id={} not found -- its {} allocation(s) skipped",
                            instanceId, e.getKey(), e.getValue().size());
                    continue;
                }
                String skuCode = str(detail.get("sku_code"));
                if (skuCode == null) continue;
                for (Map<String, Object> alloc : e.getValue()) {
                    String allocTank = firstNonBlank(str(alloc.get("tank_location")), str(alloc.get("bay_name")));
                    if (allocTank == null) continue;
                    BigDecimal allocQty = toBig(firstNonNull(alloc.get("alloc_qty"), alloc.get("qty"), alloc.get("quantity")));
                    parsed.add(new LineEntry(str(detail.get("route_id")), str(detail.get("order_id")), skuCode,
                            str(detail.get("sku_desc")), allocTank, allocQty));
                    totalQty = totalQty.add(allocQty);
                }
            }

            if (parsed.isEmpty()) {
                // Lines arrived but none yielded a tank -- say why, since the only
                // other symptom is an empty loading_progress_detail_bay.
                log.warn("qc-preload tank allocation: instance={} got {} tankAllocation line(s) and {} tankLocationAllocations "
                                + "row(s) but seeded nothing. First line keys={}, first line id={}, allocation parent ids={}",
                        instanceId, ((List<?>) linesObj).size(),
                        allocationsRaw instanceof List<?> al ? al.size() : 0,
                        ((List<?>) linesObj).get(0) instanceof Map<?, ?> m0 ? m0.keySet() : "n/a",
                        ((List<?>) linesObj).get(0) instanceof Map<?, ?> m1 ? m1.get("id") : "n/a",
                        allocationsByParentId.keySet());
                return;
            }

            // This edge fires again after a manager reject -> reload (planning -> QC preload) -- the vehicle
            // is NOT unloaded, it is topped up: the same order line can get a further allocation for what is
            // still short, and an extra order sheet's lines get their own. So rows that are already DONE
            // (loaded earlier) must not block a new allocation. Only a line + tank that already has an
            // IN_PROGRESS row is skipped, which is exactly what a re-delivered event of this same
            // transition would find (its own just-seeded rows).
            List<LineEntry> fresh = new ArrayList<>();
            BigDecimal freshQty = BigDecimal.ZERO;
            for (LineEntry e : parsed) {
                Integer active = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                                + "AND route_id <=> ? AND order_id <=> ? AND sku_code = ? AND bay_name = ? "
                                + "AND status = 'IN_PROGRESS'",
                        Integer.class, tenantId, instanceId, e.routeId, e.orderId, e.skuCode, e.tankLocation);
                if (active != null && active > 0) continue;
                fresh.add(e);
                freshQty = freshQty.add(e.quantity());
            }
            if (fresh.isEmpty()) return;
            parsed = fresh;
            totalQty = freshQty;

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
                    "INSERT INTO loading_progress_detail (tenant_id, workflow_instance_id, vehicle_no, gate_pass_no, flow_variant_code, current_step, total_quantity, processed_quantity, loading_start_time) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 0, CURRENT_TIMESTAMP) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), " +
                            "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no), " +
                            "flow_variant_code = COALESCE(flow_variant_code, VALUES(flow_variant_code)), " +
                            "current_step = VALUES(current_step), " +
                            "total_quantity = total_quantity + VALUES(total_quantity), " +
                            "loading_start_time = COALESCE(loading_start_time, VALUES(loading_start_time))",
                    tenantId, instanceId, vehicleNo, gatePassNo, instance.getWorkflowCode(), instance.getCurrentState(), totalQty);

            Long parentId = jdbc.queryForObject(
                    "SELECT id FROM loading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                    Long.class, tenantId, instanceId);

            for (LineEntry e : parsed) {
                Diff diff = diffByOrderSku.getOrDefault(e.orderId() + "|" + e.skuCode(), diffBySku.get(e.skuCode()));
                jdbc.update(
                        "INSERT INTO loading_progress_detail_bay " +
                                "(tenant_id, loading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, status, diff_qty, diff_reason) " +
                                "VALUES (?,?,?,?,?,?,?,?,?, 'IN_PROGRESS', ?, ?)",
                        tenantId, parentId, instanceId, e.routeId, e.orderId, e.skuCode, e.skuDesc, e.tankLocation, e.quantity,
                        diff == null ? null : diff.qty(), diff == null ? null : diff.reason());
            }
        } catch (Exception e) {
            log.warn("qc-preload tank allocation seeding failed for instance={}: {}", instanceId, e.getMessage());
        }
    }

    private Map<String, Object> orderDispatchDetail(Long tenantId, String id) {
        try {
            return jdbc.queryForMap(
                    "SELECT route_id, order_id, sku_code, sku_desc FROM m5_order_dispatch_detail WHERE tenant_id = ? AND id = ?",
                    tenantId, Long.parseLong(id.trim()));
        } catch (Exception e) {
            return null;
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

    private record LineEntry(String routeId, String orderId, String skuCode, String skuDesc, String tankLocation, BigDecimal quantity) {}

    private record Diff(BigDecimal qty, String reason) {}

    /** diff_box (the grid's "Diff") + diff_reason off a parent line; null when neither was filled in. */
    private static Diff diffOf(Map<String, Object> line) {
        String raw = str(line.get("diff_box"));
        BigDecimal qty = null;
        if (raw != null) {
            try { qty = new BigDecimal(raw.trim()); } catch (RuntimeException ignored) { /* non-numeric -> no qty */ }
        }
        String reason = str(line.get("diff_reason"));
        return (qty == null && reason == null) ? null : new Diff(qty, reason);
    }

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
