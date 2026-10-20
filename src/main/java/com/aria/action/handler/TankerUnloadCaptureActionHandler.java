package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code m10.tanker-unload-capture} -- the reverse of
 * {@link TankerWeighmentSkuCaptureActionHandler}, for M10.TankerOutbound's reject
 * flow ({@code UNLOADING_WEIGHMENT}, aria:weighment-capture's reject-unload grid).
 * Separate from {@link M10UnloadBayCompleteActionHandler} (FG/PackLoading's Stop Bay),
 * which is deliberately left alone.
 *
 * <p>No Start/Stop: on the first Save for a SKU this handler itself opens one
 * {@code unloading_progress_detail_bay} row (IN_PROGRESS) per loaded tank row of that SKU
 * (the {@code loading_progress_detail_bay} rows still DONE, flagged UNLOAD_IN_PROGRESS),
 * then closes them, tank by tank:
 * <ul>
 *   <li>the SKU's {@code unloading_qty} is divided across its open tank rows in
 *       proportion to what each tank actually gave (net of anything already returned);
 *       a line naming {@code tank_location} (or {@code bay_name}) unloads just that tank;</li>
 *   <li>stock goes back into THAT tank's bin, into the same lots the loading drew from
 *       (oldest first), kind {@code RETURN}, ref_type {@code TANKER_UNLOAD};</li>
 *   <li>there is no upper limit on the quantity entered (only {@code > 0}): whatever the weighbridge shows is
 *       saved. What loading drew from a tank is returned lot by lot; anything above that goes in as lot-less
 *       stock;</li>
 *   <li>when a tank has taken back everything it gave -- or the line carries
 *       {@code "final": true}, for a weighbridge reading a little under the loaded figure --
 *       its unload row closes (DONE) and its {@code loading_progress_detail_bay} row is
 *       set REVERSED. A smaller
 *       unload leaves the tank open for another weighing.</li>
 * </ul>
 * Once every loading row of the instance is REVERSED the parent {@code loading_progress_detail}
 * is flagged REVERSED too. Whole-request atomicity mirrors the loading handler.
 *
 * <p>Payload ({@code tankerUnloadItems}; {@code order_sku_line_items} is accepted too, so the
 * existing grid's row action keeps working; the quantity key is {@code unloading_qty} or
 * {@code unload_wgt}):
 * <pre>
 *   { "instanceId": 2028,
 *     "formData": { "tankerUnloadItems": [
 *       { "route_id": "ROU1", "order_id": "ORD1", "sku_code": "SKU1", "tank_location": "COT-9", "unloading_qty": 2000 }
 *     ] } }
 * </pre>
 */
@Component
public class TankerUnloadCaptureActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(TankerUnloadCaptureActionHandler.class);

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.tanker-unload-capture"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        Object linesObj = formData.get("tankerUnloadItems") != null ? formData.get("tankerUnloadItems") : formData.get("order_sku_line_items");
        if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) {
            return ActionResult.error("formData.tankerUnloadItems must be a non-empty array");
        }
        List<Map<String, Object>> lines = (List<Map<String, Object>>) linesObj;

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();
        log.info("action.tanker-unload-capture.start tenantId={} instanceId={} lineCount={}", tenantId, instanceId, lines.size());

        List<Map<String, Object>> closed = new ArrayList<>();
        BigDecimal totalReturned = BigDecimal.ZERO;

        for (int i = 0; i < lines.size(); i++) {
            Object lineObj = ((List<Object>) linesObj).get(i);
            if (!(lineObj instanceof Map)) {
                markRollbackOnly();
                return ActionResult.error("tankerUnloadItems[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lineObj;
            String routeId = str(line, "route_id");
            String orderId = str(line, "order_id");
            String skuCode = str(line, "sku_code");
            String onlyTank = str(line, "tank_location") != null ? str(line, "tank_location") : str(line, "bay_name");
            boolean forceFinal = Boolean.parseBoolean(String.valueOf(line.get("final")));
            // Optional: put the unloaded oil into a DIFFERENT tank than it was drawn from.
            String toLocation = str(line, "to_location") != null ? str(line, "to_location") : str(line, "return_location");
            if (routeId == null || skuCode == null) {
                markRollbackOnly();
                return ActionResult.error("tankerUnloadItems[" + i + "] is missing route_id or sku_code");
            }

            BigDecimal unloadQty;
            try {
                Object q = line.get("unloading_qty") != null ? line.get("unloading_qty") : line.get("unload_wgt");
                unloadQty = q == null ? null : new BigDecimal(q.toString());
            } catch (NumberFormatException e) {
                markRollbackOnly();
                return ActionResult.error("tankerUnloadItems[" + i + "] (" + skuCode + ") unloading_qty must be a number");
            }
            if (unloadQty == null || unloadQty.signum() <= 0) {
                markRollbackOnly();
                return ActionResult.error("tankerUnloadItems[" + i + "] (" + skuCode + ") needs unloading_qty > 0");
            }
            if (toLocation != null) {
                toLocation = toLocation.trim();
                Integer known = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM m10_tanker_location WHERE code = ? AND active = 1", Integer.class, toLocation);
                if (known == null || known == 0) {
                    markRollbackOnly();
                    return ActionResult.error("tankerUnloadItems[" + i + "] (" + skuCode + ") to_location '" + toLocation
                            + "' is not an active tank location");
                }
            }

            List<Map<String, Object>> openRows = jdbc.queryForList(
                    "SELECT id, bay_name, quantity, processed_quantity, plan_source FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS' ORDER BY id",
                    tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (openRows == null || openRows.isEmpty()) {
                // First Save for this SKU: open its unload rows from what is loaded.
                if (seedFromLoadedRows(tenantId, instanceId, routeId, orderId, skuCode) > 0) {
                    openRows = jdbc.queryForList(
                            "SELECT id, bay_name, quantity, processed_quantity, plan_source FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                                    + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS' ORDER BY id",
                            tenantId, instanceId, routeId, orderId, orderId, skuCode);
                }
            }
            if (openRows == null || openRows.isEmpty()) {
                markRollbackOnly();
                return ActionResult.error("nothing loaded to unload for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode
                        + " (already unloaded, or this SKU was never loaded)");
            }
            // Unload rows planned at pre-load (plan_source QC_PRELOAD for the reject flow, MANAGER_UNLOAD for a deviation unload):
            // bay_name is the tank the oil GOES TO. They are handled on their own -- the oil is taken from the tank(s) it was
            // loaded from and put into those tanks.
            List<Map<String, Object>> planRows = openRows.stream()
                    .filter(r -> "QC_PRELOAD".equals(r.get("plan_source")) || "MANAGER_UNLOAD".equals(r.get("plan_source"))).toList();
            if (!planRows.isEmpty()) {
                String err = unloadPlannedLine(tenantId, instanceId, i, routeId, orderId, skuCode, unloadQty,
                        toLocation != null ? toLocation : onlyTank, forceFinal, planRows, RowActorStamp.performedBy(line, formData), closed);
                if (err != null) {
                    markRollbackOnly();
                    return ActionResult.error(err);
                }
                totalReturned = totalReturned.add(unloadQty);
                continue;
            }
            // FIFO, no ratio: the one total quantity is taken from the SKU's loaded tanks in the order they were loaded (the
            // unload rows follow the loading rows' order), each tank up to what it can still give back; the last tank takes
            // whatever is left. The location selected (to_location, else tank_location) is where the oil goes -- it does not
            // narrow the tanks the oil comes from (tank_locations is no longer used). With none selected each tank takes its
            // own oil back.
            String destination = toLocation != null ? toLocation : onlyTank;
            String performedBy = RowActorStamp.performedBy(line, formData);
            LinkedHashMap<String, List<Map<String, Object>>> rowsByTank = new LinkedHashMap<>();
            for (Map<String, Object> r : openRows) {
                String tank = (String) r.get("bay_name");
                if (tank == null || tank.isBlank()) {
                    markRollbackOnly();
                    return ActionResult.error("tankerUnloadItems[" + i + "] (" + skuCode + ") has no tank location recorded on its unload row");
                }
                rowsByTank.computeIfAbsent(tank, k -> new ArrayList<>()).add(r);
            }
            List<String> tanks = new ArrayList<>(rowsByTank.keySet());
            BigDecimal left = unloadQty;
            // No upper limit on the quantity entered: whatever the weighbridge shows is saved, the last tank takes the surplus.
            for (int k = 0; k < tanks.size() && left.signum() > 0; k++) {
                String tank = tanks.get(k);
                // What the tank can still give back = what loading drew from it, per lot, minus earlier returns.
                List<LotQty> lots = returnableLots(tenantId, instanceId, skuCode, tank);
                BigDecimal returnable = lots.stream().map(LotQty::qty).reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean lastTank = k == tanks.size() - 1;
                BigDecimal share = lastTank ? left : left.min(returnable);
                if (share.signum() <= 0) continue;
                left = left.subtract(share);
                String to = destination != null ? destination : tank;

                BigDecimal rest = share;
                String lastUom = "KG";
                for (LotQty lot : lots) {
                    lastUom = lot.uom();
                    if (rest.signum() <= 0) break;
                    BigDecimal take = lot.qty().compareTo(rest) >= 0 ? rest : lot.qty();
                    addStock(tenantId, instanceId, routeId, skuCode, lot.lotNo(), tank, to, take, lot.uom());
                    rest = rest.subtract(take);
                }
                if (rest.signum() > 0) {
                    // more came back than loading drew from this tank: the surplus goes in as lot-less stock
                    addStock(tenantId, instanceId, routeId, skuCode, null, tank, to, rest, lastUom);
                }

                boolean tankDone = forceFinal || share.compareTo(returnable) >= 0;
                // the tank's unload rows (one per loading row) fill in order, each up to its planned quantity; the last takes the rest
                List<Map<String, Object>> tankRows = rowsByTank.get(tank);
                BigDecimal toRows = share;
                for (int j = 0; j < tankRows.size(); j++) {
                    Map<String, Object> ur = tankRows.get(j);
                    BigDecimal room = asDecimal(ur.get("quantity")).subtract(asDecimal(ur.get("processed_quantity")));
                    if (room.signum() < 0) room = BigDecimal.ZERO;
                    BigDecimal add = j == tankRows.size() - 1 ? toRows : toRows.min(room);
                    toRows = toRows.subtract(add);
                    if (add.signum() <= 0 && !tankDone) continue;
                    int n = tankDone
                            ? jdbc.update("UPDATE unloading_progress_detail_bay SET processed_quantity = COALESCE(processed_quantity, 0) + ?, status = 'DONE' "
                                    + "WHERE tenant_id = ? AND id = ? AND status = 'IN_PROGRESS'", add, tenantId, asLong(ur.get("id")))
                            : jdbc.update("UPDATE unloading_progress_detail_bay SET processed_quantity = COALESCE(processed_quantity, 0) + ? "
                                    + "WHERE tenant_id = ? AND id = ? AND status = 'IN_PROGRESS'", add, tenantId, asLong(ur.get("id")));
                    if (n == 0) {
                        markRollbackOnly();
                        return ActionResult.error("no open unload row found for route_id=" + routeId + " order_id=" + orderId
                                + " sku_code=" + skuCode + " tank=" + tank);
                    }
                    // who unloaded this tank: the logged-in user (JWT) + the person the form names (performed_by_name)
                    RowActorStamp.unloadedRow(jdbc, tenantId, asLong(ur.get("id")), performedBy);
                }
                if (tankDone) {
                    jdbc.update(
                            "UPDATE loading_progress_detail_bay SET status = 'REVERSED' "
                                    + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                                    + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND bay_name = ? "
                                    + "AND status IN ('UNLOAD_IN_PROGRESS','DONE')",
                            tenantId, instanceId, routeId, orderId, orderId, skuCode, tank);
                }
                log.info("action.tanker-unload-capture.tank tenantId={} instanceId={} routeId={} skuCode={} tank={} qty={} tankClosed={}",
                        tenantId, instanceId, routeId, skuCode, tank, share, tankDone);

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("routeId", routeId);
                row.put("orderId", orderId);
                row.put("skuCode", skuCode);
                row.put("tank", tank);
                row.put("returnedTo", to);
                row.put("unloaded", share);
                row.put("tankClosed", tankDone);
                closed.add(row);
            }
            totalReturned = totalReturned.add(unloadQty);
        }

        String currentStep = jdbc.queryForObject(
                "SELECT current_state FROM workflow_instance WHERE id = ? AND tenant_id = ?", String.class, instanceId, tenantId);
        jdbc.update("UPDATE unloading_progress_detail SET processed_quantity = processed_quantity + ?, current_step = ? "
                + "WHERE tenant_id = ? AND workflow_instance_id = ?", totalReturned, currentStep, tenantId, instanceId);

        jdbc.update("UPDATE loading_progress_detail SET unloaded_quantity = (SELECT COALESCE(SUM(b.unloaded_quantity), 0) "
                        + "FROM loading_progress_detail_bay b WHERE b.tenant_id = ? AND b.workflow_instance_id = ?) "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instanceId, tenantId, instanceId);

        Integer stillActive = jdbc.queryForObject(
                "SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status NOT IN ('REVERSED','REJECTED')",
                Integer.class, tenantId, instanceId);
        if (stillActive != null && stillActive == 0) {
            jdbc.update("UPDATE loading_progress_detail SET status = 'REVERSED' WHERE tenant_id = ? AND workflow_instance_id = ?",
                    tenantId, instanceId);
        }

        log.info("action.tanker-unload-capture.done tenantId={} instanceId={} tanks={} totalReturned={}", tenantId, instanceId, closed.size(), totalReturned);
        return ActionResult.builder()
                .ok(true)
                .message(closed.size() + " tank(s) unloaded, " + totalReturned + " returned to stock")
                .data(Map.of("unloaded", closed, "totalReturned", totalReturned))
                .build();
    }

    /** A slice of what loading drew from an origin tank (one lot), consumed as it is handed to destination tanks. */
    private static final class Chunk {
        final String originTank; final String lotNo; final String uom; BigDecimal left;
        Chunk(String originTank, String lotNo, String uom, BigDecimal left) { this.originTank = originTank; this.lotNo = lotNo; this.uom = uom; this.left = left; }
    }

    /**
     * Unloads one SKU line against the QC's unload plan. The plan rows (IN_PROGRESS, plan_source QC_PRELOAD or MANAGER_UNLOAD) name the
     * DESTINATION tanks and how much each should get; the oil comes out of the tank(s) loading drew it from (lots oldest
     * first, net of earlier returns) and is put into the destination tank, one RETURN movement per slice
     * (from_bin = origin tank, to_bin = destination). A line naming a tank unloads just that plan row; otherwise the
     * quantity is divided across the plan rows by what each still needs. A plan row closes (DONE) when filled, or on
     * {@code final}; an origin tank's loading rows go REVERSED once it has given everything back.
     *
     * @return an error message, or null on success
     */
    private String unloadPlannedLine(long tenantId, Long instanceId, int index, String routeId, String orderId, String skuCode,
                                     BigDecimal unloadQty, String destination, boolean forceFinal,
                                     List<Map<String, Object>> planRows, String performedBy, List<Map<String, Object>> closed) {
        List<Map<String, Object>> rows = planRows;
        if (destination != null) {
            final String wanted = destination.trim();
            rows = planRows.stream().filter(r -> wanted.equals(r.get("bay_name"))).toList();
            if (rows.isEmpty()) {
                return "tankerUnloadItems[" + index + "] (" + skuCode + ") has no open unload plan row for tank " + wanted;
            }
        }
        // what is still to be unloaded per plan row
        List<BigDecimal> remaining = new ArrayList<>();
        BigDecimal totalRemaining = BigDecimal.ZERO;
        for (Map<String, Object> r : rows) {
            BigDecimal rem = asDecimal(r.get("quantity")).subtract(asDecimal(r.get("processed_quantity")));
            if (rem.signum() < 0) rem = BigDecimal.ZERO;
            remaining.add(rem);
            totalRemaining = totalRemaining.add(rem);
        }
        // what loading can still give back, across the tanks the SKU was loaded from
        List<String> originTanks = jdbc.queryForList(
                "SELECT bay_name FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                        + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? "
                        + "AND status IN ('DONE','UNLOAD_IN_PROGRESS') GROUP BY bay_name ORDER BY MIN(id)",
                String.class, tenantId, instanceId, routeId, orderId, orderId, skuCode);
        List<Chunk> chunks = new ArrayList<>();
        BigDecimal totalReturnable = BigDecimal.ZERO;
        for (String tank : originTanks) {
            if (tank == null || tank.isBlank()) continue;
            for (LotQty lot : returnableLots(tenantId, instanceId, skuCode, tank)) {
                chunks.add(new Chunk(tank, lot.lotNo(), lot.uom(), lot.qty()));
                totalReturnable = totalReturnable.add(lot.qty());
            }
        }
        // No upper limit on the quantity entered (neither what was loaded nor what the plan still needs): whatever the
        // weighbridge shows is saved; the last plan row takes anything above the plan.

        BigDecimal assigned = BigDecimal.ZERO;
        int cursor = 0;
        for (int k = 0; k < rows.size(); k++) {
            Map<String, Object> r = rows.get(k);
            BigDecimal rem = remaining.get(k);
            BigDecimal share;
            if (rows.size() == 1) {
                share = unloadQty;
            } else if (k == rows.size() - 1) {
                share = unloadQty.subtract(assigned);
            } else if (totalRemaining.signum() > 0) {
                share = unloadQty.multiply(rem).divide(totalRemaining, 3, RoundingMode.HALF_UP);
            } else {
                share = BigDecimal.ZERO;
            }
            assigned = assigned.add(share);
            if (share.signum() <= 0) continue;
            String destTank = (String) r.get("bay_name");
            BigDecimal need = share;
            while (need.signum() > 0 && cursor < chunks.size()) {
                Chunk c = chunks.get(cursor);
                BigDecimal take = c.left.compareTo(need) >= 0 ? need : c.left;
                if (take.signum() > 0) {
                    addStock(tenantId, instanceId, routeId, skuCode, c.lotNo, c.originTank, destTank, take, c.uom);
                    c.left = c.left.subtract(take);
                    need = need.subtract(take);
                }
                if (c.left.signum() <= 0) cursor++;
            }
            if (need.signum() > 0) {
                // more came back than loading drew: the surplus goes into the destination tank as lot-less stock
                Chunk lastChunk = chunks.isEmpty() ? null : chunks.get(chunks.size() - 1);
                addStock(tenantId, instanceId, routeId, skuCode, null, lastChunk != null ? lastChunk.originTank : destTank,
                        destTank, need, lastChunk != null ? lastChunk.uom : "KG");
            }
            boolean rowDone = forceFinal || share.compareTo(rem) >= 0;
            int n = rowDone
                    ? jdbc.update("UPDATE unloading_progress_detail_bay SET processed_quantity = COALESCE(processed_quantity, 0) + ?, status = 'DONE' "
                            + "WHERE tenant_id = ? AND id = ? AND status = 'IN_PROGRESS'", share, tenantId, asLong(r.get("id")))
                    : jdbc.update("UPDATE unloading_progress_detail_bay SET processed_quantity = COALESCE(processed_quantity, 0) + ? "
                            + "WHERE tenant_id = ? AND id = ? AND status = 'IN_PROGRESS'", share, tenantId, asLong(r.get("id")));
            if (n == 0) {
                return "no open unload plan row found for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode + " tank=" + destTank;
            }
            RowActorStamp.unloadedRow(jdbc, tenantId, asLong(r.get("id")), performedBy);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("routeId", routeId);
            out.put("orderId", orderId);
            out.put("skuCode", skuCode);
            out.put("tank", destTank);
            out.put("returnedTo", destTank);
            out.put("unloaded", share);
            out.put("tankClosed", rowDone);
            closed.add(out);
        }

        // an origin tank that has given everything back is finished: its loading rows are reversed
        for (String tank : originTanks) {
            BigDecimal left = chunks.stream().filter(c -> tank.equals(c.originTank)).map(c -> c.left).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (left.signum() <= 0) {
                jdbc.update(
                        "UPDATE loading_progress_detail_bay SET status = 'REVERSED' "
                                + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                                + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND bay_name = ? "
                                + "AND status IN ('UNLOAD_IN_PROGRESS','DONE')",
                        tenantId, instanceId, routeId, orderId, orderId, skuCode, tank);
            }
        }
        return null;
    }

    /**
     * Opens the unload rows for one SKU line: one IN_PROGRESS {@code unloading_progress_detail_bay}
     * row per loaded ({@code DONE}) {@code loading_progress_detail_bay} row, which is flagged
     * UNLOAD_IN_PROGRESS. Creates/updates the {@code unloading_progress_detail} parent first.
     *
     * @return number of unload rows opened
     */
    private int seedFromLoadedRows(long tenantId, Long instanceId, String routeId, String orderId, String skuCode) {
        List<Map<String, Object>> loaded = jdbc.queryForList(
                "SELECT sku_desc, bay_name, quantity FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                        + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'DONE' ORDER BY id",
                tenantId, instanceId, routeId, orderId, orderId, skuCode);
        if (loaded == null || loaded.isEmpty()) return 0;

        List<Map<String, Object>> parent = jdbc.queryForList(
                "SELECT vehicle_no, gate_pass_no FROM loading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                tenantId, instanceId);
        Object vehicleNo = parent.isEmpty() ? null : parent.get(0).get("vehicle_no");
        Object gatePassNo = parent.isEmpty() ? null : parent.get(0).get("gate_pass_no");
        String currentStep = jdbc.queryForObject(
                "SELECT current_state FROM workflow_instance WHERE id = ? AND tenant_id = ?", String.class, instanceId, tenantId);

        BigDecimal totalQty = BigDecimal.ZERO;
        for (Map<String, Object> r : loaded) totalQty = totalQty.add(asDecimal(r.get("quantity")));

        jdbc.update(
                "INSERT INTO unloading_progress_detail (tenant_id, workflow_instance_id, flow_variant_code, current_step, vehicle_no, gate_pass_no, total_quantity, processed_quantity, status) "
                        + "VALUES (?,?,?,?,?,?,?,0,'IN_PROGRESS') "
                        + "ON DUPLICATE KEY UPDATE "
                        + "flow_variant_code = COALESCE(unloading_progress_detail.flow_variant_code, VALUES(flow_variant_code)), "
                        + "current_step = VALUES(current_step), "
                        + "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), "
                        + "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no), "
                        + "total_quantity = total_quantity + VALUES(total_quantity)",
                tenantId, instanceId, "M10.TankerOutbound", currentStep, vehicleNo, gatePassNo, totalQty);
        Long parentId = jdbc.queryForObject(
                "SELECT id FROM unloading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                Long.class, tenantId, instanceId);

        for (Map<String, Object> r : loaded) {
            jdbc.update(
                    "INSERT INTO unloading_progress_detail_bay "
                            + "(tenant_id, unloading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, status) "
                            + "VALUES (?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                    tenantId, parentId, instanceId, routeId, orderId, skuCode, r.get("sku_desc"), r.get("bay_name"), r.get("quantity"));
        }
        jdbc.update(
                "UPDATE loading_progress_detail_bay SET status = 'UNLOAD_IN_PROGRESS' "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                        + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'DONE'",
                tenantId, instanceId, routeId, orderId, orderId, skuCode);
        log.info("action.tanker-unload-capture.seeded tenantId={} instanceId={} routeId={} skuCode={} rows={} totalQty={}",
                tenantId, instanceId, routeId, skuCode, loaded.size(), totalQty);
        return loaded.size();
    }

    /** Per lot: what loading drew from {@code tank} for this SKU, minus what earlier unloads put back. */
    private List<LotQty> returnableLots(long tenantId, Long instanceId, String skuCode, String tank) {
        List<Map<String, Object>> consumed = jdbc.queryForList(
                "SELECT lot_no, uom, SUM(qty) AS qty FROM m5_stock_movement "
                        + "WHERE tenant_id = ? AND kind = 'CONSUME' AND ref_type IN ('BAY','TANKER') AND ref_id = ? "
                        + "AND item_code = ? AND from_bin = ? GROUP BY lot_no, uom ORDER BY MIN(id)",
                tenantId, instanceId, skuCode, tank);
        Map<String, BigDecimal> returned = new HashMap<>();
        for (Map<String, Object> r : jdbc.queryForList(
                "SELECT lot_no, SUM(qty) AS qty FROM m5_stock_movement "
                        + "WHERE tenant_id = ? AND kind = 'RETURN' AND ref_type IN ('UNLOAD_BAY','TANKER_UNLOAD') AND ref_id = ? "
                        + "AND item_code = ? AND (from_bin = ? OR (from_bin IS NULL AND to_bin = ?)) GROUP BY lot_no",
                tenantId, instanceId, skuCode, tank, tank)) {
            returned.merge(lotKey(r.get("lot_no")), asDecimal(r.get("qty")), BigDecimal::add);
        }
        List<LotQty> out = new ArrayList<>();
        for (Map<String, Object> row : consumed) {
            String lotNo = (String) row.get("lot_no");
            BigDecimal net = asDecimal(row.get("qty")).subtract(returned.getOrDefault(lotKey(lotNo), BigDecimal.ZERO));
            if (net.signum() > 0) out.add(new LotQty(lotNo, net, row.get("uom") != null ? (String) row.get("uom") : "KG"));
        }
        return out;
    }

    /**
     * Puts {@code qty} into {@code toBin}. {@code fromBin} is the tank the loading drew it from and is always
     * stamped on the RETURN movement, so what has already been returned per origin tank can be netted off
     * whether the oil went back to the same tank or into a different one.
     */
    private void addStock(long tenantId, Long instanceId, String routeId, String skuCode, String lotNo,
                          String fromBin, String toBin, BigDecimal qty, String uom) {
        int updated = jdbc.update(
                "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, last_movement_at = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND item_code = ? AND ((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code = ?",
                qty, tenantId, skuCode, lotNo, lotNo, toBin);
        if (updated == 0) {
            jdbc.update("INSERT INTO m5_stock_balance (tenant_id, item_code, lot_no, bin_code, uom, qty_on_hand, last_movement_at) "
                    + "VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)", tenantId, skuCode, lotNo, toBin, uom, qty);
        }
        jdbc.update("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, to_bin, qty, uom, ref_type, ref_id, ref_number) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)", tenantId, "RETURN", skuCode, lotNo, fromBin, toBin, qty, uom, "TANKER_UNLOAD", instanceId, routeId);
        // The origin tank's loading row keeps what it loaded (processed_quantity) untouched and counts what has come back
        // (unloaded_quantity), so a tank unloaded only in part shows loaded / unloaded / still on the vehicle. One row only:
        // the first that still has something left, so a SKU loaded twice into the same tank is not counted twice.
        if (fromBin != null) countUnloaded(tenantId, instanceId, routeId, skuCode, fromBin, qty);
        log.info("action.tanker-unload-capture.stock-return tenantId={} instanceId={} skuCode={} lotNo={} fromBin={} toBin={} qty={}",
                tenantId, instanceId, skuCode, lotNo, fromBin, toBin, qty);
    }

    /**
     * What came back counts against the origin tank's loading rows, first loaded first: each row takes up to what it still has
     * ({@code processed_quantity - unloaded_quantity}), the last row takes any surplus. {@code processed_quantity} itself is never
     * changed.
     */
    private void countUnloaded(long tenantId, Long instanceId, String routeId, String skuCode, String bay, BigDecimal qty) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, processed_quantity, unloaded_quantity FROM loading_progress_detail_bay WHERE tenant_id = ? "
                        + "AND workflow_instance_id = ? AND route_id = ? AND sku_code = ? AND bay_name = ? "
                        + "AND status IN ('DONE','UNLOAD_IN_PROGRESS') ORDER BY id",
                tenantId, instanceId, routeId, skuCode, bay);
        if (rows == null || rows.isEmpty()) return;
        BigDecimal left = qty;
        for (int j = 0; j < rows.size() && left.signum() > 0; j++) {
            Map<String, Object> r = rows.get(j);
            BigDecimal room = asDecimal(r.get("processed_quantity")).subtract(asDecimal(r.get("unloaded_quantity")));
            if (room.signum() < 0) room = BigDecimal.ZERO;
            BigDecimal take = j == rows.size() - 1 ? left : left.min(room);
            if (take.signum() <= 0) continue;
            jdbc.update("UPDATE loading_progress_detail_bay SET unloaded_quantity = COALESCE(unloaded_quantity, 0) + ? "
                    + "WHERE tenant_id = ? AND id = ?", take, tenantId, asLong(r.get("id")));
            left = left.subtract(take);
        }
    }

    private record LotQty(String lotNo, BigDecimal qty, String uom) {}

    private record TankPlan(Long rowId, String tank, List<LotQty> lots, BigDecimal returnable) {}

    /** {@code tank_locations}: a JSON array of tank codes, or a comma-separated string. Blank entries dropped. */
    private static List<String> tankList(Object raw) {
        List<String> out = new ArrayList<>();
        if (raw instanceof List<?> l) {
            for (Object o : l) if (o != null && !o.toString().isBlank()) out.add(o.toString().trim());
        } else if (raw != null && !raw.toString().isBlank()) {
            for (String s : raw.toString().split(",")) if (!s.isBlank()) out.add(s.trim());
        }
        return out;
    }

    private static String lotKey(Object lotNo) { return lotNo == null ? "" : lotNo.toString(); }

    private static BigDecimal asDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    private static Long asLong(Object v) { return v instanceof Number n ? n.longValue() : null; }

    private static void markRollbackOnly() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    private static String str(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        if (v == null) return null;
        String s = v.toString();
        return s.isBlank() ? null : s;
    }
}
