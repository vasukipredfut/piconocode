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
 *   <li>a tank can never take back more than it gave: the request is rejected if a tank's
 *       share exceeds what is still returnable from it (nothing is written then);</li>
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
                    "SELECT id, bay_name, quantity FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS' ORDER BY id",
                    tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (openRows == null || openRows.isEmpty()) {
                // First Save for this SKU: open its unload rows from what is loaded.
                if (seedFromLoadedRows(tenantId, instanceId, routeId, orderId, skuCode) > 0) {
                    openRows = jdbc.queryForList(
                            "SELECT id, bay_name, quantity FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                                    + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS' ORDER BY id",
                            tenantId, instanceId, routeId, orderId, orderId, skuCode);
                }
            }
            if (openRows == null || openRows.isEmpty()) {
                markRollbackOnly();
                return ActionResult.error("nothing loaded to unload for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode
                        + " (already unloaded, or this SKU was never loaded)");
            }
            // tank_locations: ONE total unloading_qty for just these tanks of the SKU (e.g. 2 of its 3), divided
            // across them by what each can still give back; the SKU's other tanks stay open.
            List<String> tankSubset = onlyTank != null ? List.of() : tankList(line.get("tank_locations"));
            if (!tankSubset.isEmpty()) {
                final List<Map<String, Object>> open = openRows;
                for (String t : tankSubset) {
                    if (open.stream().noneMatch(r -> t.equals(r.get("bay_name")))) {
                        markRollbackOnly();
                        return ActionResult.error("tankerUnloadItems[" + i + "] (" + skuCode + ") has no open unload row for tank " + t
                                + " (already unloaded, or not loaded in that tank)");
                    }
                }
                openRows = openRows.stream().filter(r -> tankSubset.contains((String) r.get("bay_name"))).toList();
            }
            if (onlyTank != null) {
                final String wanted = onlyTank.trim();
                openRows = openRows.stream().filter(r -> wanted.equals(r.get("bay_name"))).toList();
                if (openRows.isEmpty()) {
                    markRollbackOnly();
                    return ActionResult.error("tankerUnloadItems[" + i + "] (" + skuCode + ") has no open unload row for tank " + wanted
                            + " (already unloaded, or not loaded in that tank)");
                }
            }

            // What each tank can still give back = what loading drew from it, per lot, minus earlier returns.
            List<TankPlan> plans = new ArrayList<>();
            BigDecimal totalReturnable = BigDecimal.ZERO;
            for (Map<String, Object> r : openRows) {
                String tank = (String) r.get("bay_name");
                if (tank == null || tank.isBlank()) {
                    markRollbackOnly();
                    return ActionResult.error("tankerUnloadItems[" + i + "] (" + skuCode + ") has no tank location recorded on its unload row");
                }
                List<LotQty> lots = returnableLots(tenantId, instanceId, skuCode, tank);
                BigDecimal returnable = lots.stream().map(LotQty::qty).reduce(BigDecimal.ZERO, BigDecimal::add);
                plans.add(new TankPlan(asLong(r.get("id")), tank, lots, returnable));
                totalReturnable = totalReturnable.add(returnable);
            }
            if (unloadQty.compareTo(totalReturnable) > 0) {
                markRollbackOnly();
                return ActionResult.error("tankerUnloadItems[" + i + "] (" + skuCode + ") unloading_qty " + unloadQty
                        + " is more than was loaded (" + totalReturnable + " still returnable"
                        + (onlyTank != null ? " from tank " + onlyTank : "") + ")");
            }

            BigDecimal assigned = BigDecimal.ZERO;
            for (int k = 0; k < plans.size(); k++) {
                TankPlan p = plans.get(k);
                BigDecimal share;
                if (plans.size() == 1) {
                    share = unloadQty;
                } else if (k == plans.size() - 1) {
                    share = unloadQty.subtract(assigned);
                } else {
                    share = unloadQty.multiply(p.returnable()).divide(totalReturnable, 3, RoundingMode.HALF_UP);
                }
                assigned = assigned.add(share);
                if (share.compareTo(p.returnable()) > 0) {
                    markRollbackOnly();
                    return ActionResult.error("tankerUnloadItems[" + i + "] (" + skuCode + ") tank " + p.tank() + " can take back at most "
                            + p.returnable() + " but " + share + " was entered for it");
                }
                if (share.signum() == 0) continue;

                BigDecimal left = share;
                for (LotQty lot : p.lots()) {
                    if (left.signum() <= 0) break;
                    BigDecimal take = lot.qty().compareTo(left) >= 0 ? left : lot.qty();
                    addStock(tenantId, instanceId, routeId, skuCode, lot.lotNo(), p.tank(),
                            toLocation != null ? toLocation : p.tank(), take, lot.uom());
                    left = left.subtract(take);
                }

                boolean tankDone = forceFinal || share.compareTo(p.returnable()) >= 0;
                int n = tankDone
                        ? jdbc.update("UPDATE unloading_progress_detail_bay SET processed_quantity = COALESCE(processed_quantity, 0) + ?, status = 'DONE' "
                                + "WHERE tenant_id = ? AND id = ? AND status = 'IN_PROGRESS'", share, tenantId, p.rowId())
                        : jdbc.update("UPDATE unloading_progress_detail_bay SET processed_quantity = COALESCE(processed_quantity, 0) + ? "
                                + "WHERE tenant_id = ? AND id = ? AND status = 'IN_PROGRESS'", share, tenantId, p.rowId());
                if (n == 0) {
                    markRollbackOnly();
                    return ActionResult.error("no open unload row found for route_id=" + routeId + " order_id=" + orderId
                            + " sku_code=" + skuCode + " tank=" + p.tank());
                }
                if (tankDone) {
                    jdbc.update(
                            "UPDATE loading_progress_detail_bay SET status = 'REVERSED' "
                                    + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                                    + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND bay_name = ? "
                                    + "AND status IN ('UNLOAD_IN_PROGRESS','DONE')",
                            tenantId, instanceId, routeId, orderId, orderId, skuCode, p.tank());
                }
                log.info("action.tanker-unload-capture.tank tenantId={} instanceId={} routeId={} skuCode={} tank={} qty={} tankClosed={}",
                        tenantId, instanceId, routeId, skuCode, p.tank(), share, tankDone);

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("routeId", routeId);
                row.put("orderId", orderId);
                row.put("skuCode", skuCode);
                row.put("tank", p.tank());
                row.put("returnedTo", toLocation != null ? toLocation : p.tank());
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
        log.info("action.tanker-unload-capture.stock-return tenantId={} instanceId={} skuCode={} lotNo={} fromBin={} toBin={} qty={}",
                tenantId, instanceId, skuCode, lotNo, fromBin, toBin, qty);
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
