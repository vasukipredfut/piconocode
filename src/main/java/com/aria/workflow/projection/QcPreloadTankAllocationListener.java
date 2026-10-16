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
        boolean loadingRoute = "QC_PRELOAD_APPROVED".equals(toState) || "LOADING_COMPLETED".equals(toState);
        if (submittedDataBag == null) return;

        Long tenantId = TenantContext.get();
        Long instanceId = instance.getId();

        try {
            Object pageObj = submittedDataBag.get(PAGE_CODE);
            if (!(pageObj instanceof Map)) return;
            Map<String, Object> page = (Map<String, Object>) pageObj;

            // Reject-unload route: a vehicle QC rejected at QC Final Out after loading has its unload tanks chosen on this page and
            // goes on to Weighment Capture (QC_SAMPLE_APPROVED -- there is no QC sample for an unload), not to a loading state. The
            // unload plan is still saved here, whatever state the vehicle goes to; no new loading is seeded on this route.
            if (!loadingRoute) {
                if (hasUnloadEntry(page) && rejectedAtQcFinalOut(tenantId, instanceId, submittedDataBag)) seedUnloadPlan(tenantId, instance, page);
                return;
            }

            // Oil already loaded that QC wants taken back off: the page's unload allocations go to
            // unloading_progress_detail(_bay), same way the loading allocations below go to loading_progress_detail(_bay).
            // Independent of whether this submission also allocated NEW loading (a pure unload plan has none).
            seedUnloadPlan(tenantId, instance, page);

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
                        if (alreadyLoadedSlot(alloc)) continue;
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
                    if (alreadyLoadedSlot(alloc)) continue;
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

    /** At least one {@code tankUnloadAllocations} entry with both a tank and a qty above zero. */
    private static boolean hasUnloadEntry(Map<String, Object> page) {
        if (!(page.get("tankUnloadAllocations") instanceof List<?> list)) return false;
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) continue;
            String tank = firstNonBlank(str(m.get("tank_location")), str(m.get("bay_name")));
            BigDecimal qty = toBig(firstNonNull(m.get("alloc_qty"), m.get("qty"), m.get("quantity")));
            if (tank != null && qty.signum() > 0) return true;
        }
        return false;
    }

    /**
     * Did QC reject this vehicle at QC Final Out? The decision is on that page's submission ({@code status}, or
     * {@code qc_final_outbound_status}) and, once projected, on {@code qc_final_outbound_detail.status}.
     */
    @SuppressWarnings("unchecked")
    private boolean rejectedAtQcFinalOut(Long tenantId, Long instanceId, Map<String, Object> bag) {
        Object qc = bag.get("aria:qc-final-out");
        if (qc instanceof Map<?, ?> m) {
            String v = firstNonBlank(str(((Map<String, Object>) m).get("qc_final_outbound_status")), str(((Map<String, Object>) m).get("status")));
            if (v != null) return "REJECTED".equalsIgnoreCase(v.trim());
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT status FROM qc_final_outbound_detail WHERE tenant_id = ? AND workflow_instance_id = ?", tenantId, instanceId);
        return !rows.isEmpty() && rows.get(0).get("status") != null
                && "REJECTED".equalsIgnoreCase(String.valueOf(rows.get(0).get("status")).trim());
    }

    /**
     * aria:qc-preload-approval's "unload" allocations ({@code tankUnloadAllocations}: the BatchAllocationTable child list,
     * {@code order_dispatch_detail_id} + {@code tank_location} + {@code alloc_qty}) -> one {@code unloading_progress_detail_bay}
     * row per destination tank (route_id / order_id / sku_code / sku_desc resolved from the order-sheet row, bay_name = the
     * tank the oil goes to, quantity = alloc_qty) under the {@code unloading_progress_detail} parent.
     *
     * <p>Rows are written {@code IN_PROGRESS} with {@code plan_source = 'QC_PRELOAD'} (V367): the marker tells
     * {@code m10.tanker-unload-capture} that {@code bay_name} is the DESTINATION tank (oil goes there from the tank(s) it was
     * loaded from), not an origin tank. Re-running replaces the instance's not-yet-started plan rows, so a re-delivered event
     * or a re-submitted page never duplicates them.
     */
    @SuppressWarnings("unchecked")
    private void seedUnloadPlan(Long tenantId, WorkflowInstance instance, Map<String, Object> page) {
        Object raw = page.get("tankUnloadAllocations");
        if (!(raw instanceof List<?> list) || list.isEmpty()) return;
        Long instanceId = instance.getId();
        try {
            List<Object[]> rows = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            for (Object o : list) {
                if (!(o instanceof Map)) continue;
                Map<String, Object> alloc = (Map<String, Object>) o;
                String detailId = str(alloc.get("order_dispatch_detail_id"));
                String tank = firstNonBlank(str(alloc.get("tank_location")), str(alloc.get("bay_name")));
                BigDecimal qty = toBig(firstNonNull(alloc.get("alloc_qty"), alloc.get("qty"), alloc.get("quantity")));
                if (detailId == null || tank == null || qty.signum() <= 0) continue;
                Map<String, Object> detail = orderDispatchDetail(tenantId, detailId);
                if ((detail == null || str(detail.get("sku_code")) == null) && str(alloc.get("sku_code")) != null) {
                    // the page also sends sku_code / sku_desc / order_id / route_id on each unload row (unloadExtraFields)
                    detail = new java.util.HashMap<>();
                    detail.put("sku_code", alloc.get("sku_code"));
                    detail.put("sku_desc", alloc.get("sku_desc"));
                    detail.put("order_id", alloc.get("order_id"));
                    detail.put("route_id", alloc.get("route_id"));
                }
                if (detail == null || str(detail.get("sku_code")) == null) {
                    log.warn("qc-preload unload plan: instance={} order_dispatch_detail_id={} not found -- allocation skipped", instanceId, detailId);
                    continue;
                }
                rows.add(new Object[]{str(detail.get("route_id")), str(detail.get("order_id")), str(detail.get("sku_code")),
                        str(detail.get("sku_desc")), tank, qty, str(alloc.get("rd_code")), str(alloc.get("rd_desc"))});
                total = total.add(qty);
            }
            if (rows.isEmpty()) return;

            String vehicleNo = firstNonBlank(str(page.get("vehicle_no")), str(page.get("vehicleNo")));
            String gatePassNo = firstNonBlank(str(page.get("gpNumber")), str(page.get("gp_number")));
            if (vehicleNo == null || gatePassNo == null) {
                Map<String, Object> gp = gatePassRow(tenantId, instanceId);
                if (gp != null) {
                    if (vehicleNo == null) vehicleNo = str(gp.get("vehicle_no"));
                    if (gatePassNo == null) gatePassNo = str(gp.get("gp_number"));
                }
            }

            // replace this instance's not-yet-started plan rows (a re-submitted page / re-delivered event never duplicates them;
            // rows that already unloaded something keep their progress)
            jdbc.update("DELETE FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND plan_source = 'QC_PRELOAD' AND status = 'IN_PROGRESS' AND (processed_quantity IS NULL OR processed_quantity = 0)",
                    tenantId, instanceId);
            jdbc.update(
                    "INSERT INTO unloading_progress_detail (tenant_id, workflow_instance_id, flow_variant_code, current_step, vehicle_no, gate_pass_no, total_quantity, processed_quantity, status) "
                            + "VALUES (?,?,?,?,?,?,?,0,'IN_PROGRESS') "
                            + "ON DUPLICATE KEY UPDATE "
                            + "flow_variant_code = COALESCE(unloading_progress_detail.flow_variant_code, VALUES(flow_variant_code)), "
                            + "current_step = VALUES(current_step), "
                            + "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), "
                            + "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no)",
                    tenantId, instanceId, instance.getWorkflowCode(), instance.getCurrentState(), vehicleNo, gatePassNo, total);
            Long parentId = jdbc.queryForObject(
                    "SELECT id FROM unloading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                    Long.class, tenantId, instanceId);
            for (Object[] r : rows) {
                jdbc.update(
                        "INSERT INTO unloading_progress_detail_bay "
                                + "(tenant_id, unloading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, rd_code, vendor_name, status, plan_source) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?, 'IN_PROGRESS', 'QC_PRELOAD')",
                        tenantId, parentId, instanceId, r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]);
            }
            log.info("qc-preload unload plan: instance={} wrote {} PLANNED unloading_progress_detail_bay row(s), total={}", instanceId, rows.size(), total);
        } catch (Exception e) {
            log.warn("qc-preload unload plan seeding failed for instance={}: {}", instanceId, e.getMessage());
        }
    }

    /**
     * On a reload the allocation grid is pre-filled from what is already loaded (seedAllocationsFrom, read-only slots marked
     * status DONE, carrying the existing loading_progress_detail_bay id). Those slots are not new allocations: seeding them
     * again would load the same oil twice, so they are skipped.
     */
    private static boolean alreadyLoadedSlot(Map<String, Object> alloc) {
        String status = str(alloc.get("status"));
        if (status != null && "DONE".equalsIgnoreCase(status.trim())) return true;
        return str(alloc.get("id")) != null;
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
