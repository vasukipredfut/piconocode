package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.mes.stock.FefoStockPickerService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code m10.tanker-weighment-capture} — the "Save Weight &amp; Update Stock"
 * button on aria:weighment-capture's "Order Details — Per-SKU Weight
 * Capture" grid (M10.TankerOutbound, WEIGHMENT_2 only). Separate from the
 * page's "Capture &amp; Save Weighment" button (which only does
 * {@code workflow.transition} to WEIGHMENT_2) — this one saves the per-SKU
 * weighbridge reading and does NOT touch workflow state, same division of
 * labour as {@link M10StartBayActionHandler}/{@link M10BayCompleteActionHandler}
 * on page 428.
 *
 * <p>TankerOutbound has no Start Bay / Bay Complete step of its own —
 * {@code QcPreloadTankAllocationListener} seeds {@code loading_progress_detail_bay}
 * rows (status IN_PROGRESS) at QC_PRELOAD_APPROVED from the tank-allocation
 * grid, and deliberately leaves closing them as "separate follow-up work"
 * (see its javadoc). This handler is that follow-up: it closes those same
 * rows once the tanker's actual per-SKU weight is known.
 *
 * <p>Per line, exactly four tables move — {@code m5_order_dispatch_detail}
 * itself is deliberately left untouched: FEFO-deduct stock for that SKU, but
 * constrained to the specific tank this SKU was allocated to at tank-
 * allocation time ({@code loading_progress_detail_bay.bay_name}, read fresh
 * here before picking) — not an anonymous FEFO pick across every bin. A
 * truck can only physically draw from the tank it's connected to, so if that
 * one tank alone doesn't have enough, the line fails rather than silently
 * pulling the shortfall from some other bin (same picker as Bay Complete,
 * just bin-scoped via its existing {@code binCode} parameter). Insert one
 * {@code m5_stock_movement} row per lot picked within that tank (kind
 * {@code CONSUME}, same as Bay Complete; {@code ref_type TANKER}), decrement
 * {@code m5_stock_balance.qty_on_hand} for each lot picked, then close the
 * matching {@code loading_progress_detail_bay} row (route_id + sku_code,
 * status IN_PROGRESS -&gt; DONE) and roll the total into
 * {@code loading_progress_detail.processed_quantity}. Whole-request
 * atomicity and the stock-race guard mirror {@link M10BayCompleteActionHandler}
 * exactly — see its javadoc for the reasoning.
 *
 * <p>Also stamps {@code loading_progress_detail.loading_finish_time} (always
 * overwritten with the real completion time here — not
 * {@code COALESCE}-guarded — because {@code WorkflowProjectionListener}'s
 * generic {@code state_ts_columns_json} config already stamps this same
 * column prematurely, at TankerOutbound's FIRST/tare weighment
 * (QC_PRELOAD_APPROVED -&gt; LOADING_COMPLETED, V190), long before loading
 * has actually happened; this handler's stamp is the one that reflects
 * reality. {@code loading_start_time} is stamped separately by
 * {@link com.aria.workflow.projection.QcPreloadTankAllocationListener} at
 * tank-allocation time, since TankerOutbound's live FSM never reaches the
 * LOADING_IN_PROGRESS state that config also (uselessly) maps.
 *
 * <p>Matched by (route_id, order_id, sku_code) per line — same identifying
 * key {@link M10BayCompleteActionHandler} uses, not the row's numeric id —
 * order_id (V252) is NULL-safe-matched alongside route_id since
 * {@code QcPreloadTankAllocationListener} may have seeded the row with
 * either identifier depending on how that tank-allocation grid grouped its
 * lines; whatever it stored must be echoed back here unchanged.
 *
 * <p><b>Multi-tank SKUs:</b> a line without {@code tank_location} carries the SKU's TOTAL
 * weight, divided across its open tank rows in proportion to each row's allocated
 * quantity. A line WITH {@code tank_location} (or {@code bay_name}) weighs just that
 * tank: the whole {@code load_wgt} is issued from it and only its row closes, the
 * SKU's other tanks stay IN_PROGRESS for a later weighment.
 *
 * <p>Payload shape (formData.tankerSkuWeights — the grid's current rows,
 * the weight already edited by the officer from the scale reading;
 * {@code loading_qty} is accepted as well as {@code load_wgt} — the grid's
 * own column name — same "accept either" flexibility
 * {@link M10BayCompleteActionHandler} already has for its own qty field):
 * <pre>
 *   { "instanceId": 1495,
 *     "formData": { "tankerSkuWeights": [
 *       { "route_id": "ROU028912", "order_id": "ORD1123", "sku_code": "SKU1215", "loading_qty": 2921.60 },
 *       { "route_id": "ROU028912", "order_id": "ORD1124", "sku_code": "SKU1461", "loading_qty": 1180.00 }
 *     ] } }
 * </pre>
 */
@Component
public class TankerWeighmentSkuCaptureActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(TankerWeighmentSkuCaptureActionHandler.class);

    @Autowired private JdbcTemplate jdbc;
    @Autowired private FefoStockPickerService fefoPicker;

    @Override
    public String code() { return "m10.tanker-weighment-capture"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        Object linesObj = formData.get("tankerSkuWeights");
        if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) {
            return ActionResult.error("formData.tankerSkuWeights must be a non-empty array");
        }
        List<Map<String, Object>> lines = (List<Map<String, Object>>) linesObj;

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();

        log.info("action.tanker-weighment-capture.start tenantId={} instanceId={} lineCount={}", tenantId, instanceId, lines.size());

        List<Map<String, Object>> closed = new ArrayList<>();
        BigDecimal totalProcessed = BigDecimal.ZERO;

        for (int i = 0; i < lines.size(); i++) {
            Object lineObj = lines.get(i);
            if (!(lineObj instanceof Map)) {
                markRollbackOnly();
                return ActionResult.error("tankerSkuWeights[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lineObj;
            String routeId = str(line, "route_id");
            String orderId = str(line, "order_id");
            String skuCode = str(line, "sku_code");
            // Optional: weigh ONE tank of a multi-tank SKU on its own (see class javadoc).
            String onlyTank = str(line, "tank_location") != null ? str(line, "tank_location") : str(line, "bay_name");
            if (routeId == null || skuCode == null) {
                markRollbackOnly();
                return ActionResult.error("tankerSkuWeights[" + i + "] is missing route_id or sku_code");
            }

            BigDecimal loadWgt;
            try {
                Object qtyObj = line.get("loading_qty") != null ? line.get("loading_qty") : line.get("load_wgt");
                loadWgt = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                markRollbackOnly();
                return ActionResult.error("tankerSkuWeights[" + i + "] (" + skuCode + ") loading_qty must be a number");
            }
            if (loadWgt == null || loadWgt.signum() <= 0) {
                markRollbackOnly();
                return ActionResult.error("tankerSkuWeights[" + i + "] (" + skuCode + ") needs loading_qty > 0");
            }

            // Deduct specifically from the tank this SKU was allocated to at
            // tank-allocation time (QcPreloadTankAllocationListener stamps
            // bay_name onto this same row) -- not an anonymous FEFO pick
            // across every bin. You can't physically pump oil out of a tank
            // the truck isn't connected to, so if THIS tank alone doesn't
            // have enough, the line fails rather than silently pulling from
            // some other bin.
            List<Map<String, Object>> bayRows = jdbc.queryForList(
                    "SELECT id, bay_name, quantity FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND route_id = ? AND ((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS' ORDER BY id",
                    tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (bayRows.isEmpty()) {
                markRollbackOnly();
                return ActionResult.error("no open bay row found for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode);
            }

            // A line naming a tank closes only that tank's row(s); the SKU's other
            // tanks stay IN_PROGRESS for a later weighment. Rows are then closed by id
            // whenever the SKU has more than one open row, so a single-tank weighment
            // can never close its siblings.
            boolean multiRowSku = bayRows.size() > 1;
            if (onlyTank != null) {
                final String wanted = onlyTank.trim();
                bayRows = bayRows.stream().filter(r -> wanted.equals(r.get("bay_name"))).toList();
                if (bayRows.isEmpty()) {
                    markRollbackOnly();
                    return ActionResult.error("tankerSkuWeights[" + i + "] (" + skuCode + ") has no open tank row for " + wanted
                            + " (already weighed, or not allocated to that tank)");
                }
            }

            // One tank allocated -> the whole weighed qty comes from it (original
            // behavior). Several tanks (multi-location split, see
            // QcPreloadTankAllocationListener) -> the weighed qty is divided across
            // them in proportion to each row's allocated quantity.
            List<TankShare> shares = splitByAllocation(bayRows, loadWgt);

            for (TankShare share : shares) {
                String tankLocation = share.tank();
                if (tankLocation == null || tankLocation.isBlank()) {
                    markRollbackOnly();
                    return ActionResult.error("tankerSkuWeights[" + i + "] (" + skuCode + ") has no tank location recorded — allocate one before weighing");
                }

                List<FefoStockPickerService.FefoPick> picks = fefoPicker.pick(tenantId, skuCode, tankLocation, share.qty());
                BigDecimal residual = fefoPicker.residualQty(share.qty(), picks);
                if (residual.signum() > 0) {
                    markRollbackOnly();
                    log.warn("action.tanker-weighment-capture.insufficient-stock tenantId={} instanceId={} routeId={} skuCode={} tankLocation={} requestedQty={} availableQty={}",
                            tenantId, instanceId, routeId, skuCode, tankLocation, share.qty(), share.qty().subtract(residual));
                    return ActionResult.error("tankerSkuWeights[" + i + "] (" + skuCode + ") insufficient stock in tank " + tankLocation + ": "
                            + "requested " + share.qty() + ", available " + share.qty().subtract(residual));
                }

                for (FefoStockPickerService.FefoPick pick : picks) {
                    int deducted = jdbc.update(
                            "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand - ?, last_movement_at = CURRENT_TIMESTAMP "
                                    + "WHERE tenant_id = ? AND item_code = ? AND "
                                    + "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code = ? AND qty_on_hand >= ?",
                            pick.qty(), tenantId, skuCode, pick.lotNo(), pick.lotNo(), pick.binCode(), pick.qty());
                    if (deducted == 0) {
                        markRollbackOnly();
                        log.warn("action.tanker-weighment-capture.stock-race tenantId={} instanceId={} routeId={} skuCode={} lotNo={} binCode={} qty={}",
                                tenantId, instanceId, routeId, skuCode, pick.lotNo(), pick.binCode(), pick.qty());
                        return ActionResult.error("tankerSkuWeights[" + i + "] (" + skuCode + ") stock changed "
                                + "concurrently — lot " + pick.lotNo() + "/bin " + pick.binCode()
                                + " no longer has enough qty_on_hand, retry");
                    }
                    jdbc.update(
                            "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, qty, uom, "
                                    + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)",
                            tenantId, "CONSUME", skuCode, pick.lotNo(), pick.binCode(), pick.qty(), pick.uom(),
                            "TANKER", instanceId, routeId);
                    log.info("action.tanker-weighment-capture.stock-issue tenantId={} instanceId={} routeId={} skuCode={} lotNo={} binCode={} qty={} uom={}",
                            tenantId, instanceId, routeId, skuCode, pick.lotNo(), pick.binCode(), pick.qty(), pick.uom());
                }

                int n;
                if (!multiRowSku) {
                    n = jdbc.update(
                            "UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                                    + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                                    + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                            share.qty(), tenantId, instanceId, routeId, orderId, orderId, skuCode);
                } else {
                    n = jdbc.update(
                            "UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                                    + "WHERE tenant_id = ? AND id = ? AND status = 'IN_PROGRESS'",
                            share.qty(), tenantId, share.rowId());
                }
                if (n == 0) {
                    markRollbackOnly();
                    log.warn("action.tanker-weighment-capture.bay-row-missing tenantId={} instanceId={} routeId={} orderId={} skuCode={}",
                            tenantId, instanceId, routeId, orderId, skuCode);
                    return ActionResult.error("no open bay row found for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode);
                }

                log.info("action.tanker-weighment-capture.bay-row-closed tenantId={} instanceId={} routeId={} skuCode={} tank={} loadWgt={}",
                        tenantId, instanceId, routeId, skuCode, tankLocation, share.qty());
            }

            totalProcessed = totalProcessed.add(loadWgt);
            Map<String, Object> row = new HashMap<>();
            row.put("routeId", routeId);
            row.put("orderId", orderId);
            row.put("skuCode", skuCode);
            row.put("loadWgt", loadWgt);
            closed.add(row);
        }

        jdbc.update(
                "UPDATE loading_progress_detail SET processed_quantity = processed_quantity + ?, "
                        + "loading_finish_time = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                totalProcessed, tenantId, instanceId);

        log.info("action.tanker-weighment-capture.done tenantId={} instanceId={} linesClosed={} totalProcessed={}",
                tenantId, instanceId, closed.size(), totalProcessed);

        return ActionResult.builder()
                .ok(true)
                .message(closed.size() + " SKU line(s) weighed, " + totalProcessed + " kg total issued from stock")
                .data(Map.of("closed", closed, "totalProcessed", totalProcessed))
                .build();
    }

    /** One tank's portion of a weighed SKU line; rowId is the loading_progress_detail_bay row it closes. */
    private record TankShare(Long rowId, String tank, BigDecimal qty) {}

    /**
     * Divides the weighed qty across this SKU's open bay rows in proportion to
     * each row's allocated {@code quantity} (the planned per-tank split from
     * the tank-allocation grid). A single row takes everything. If every
     * allocated quantity is zero/missing the split is equal. Each share is
     * rounded to 3 dp; the LAST row takes the remainder so shares always sum
     * exactly to the weighed qty.
     */
    private static List<TankShare> splitByAllocation(List<Map<String, Object>> bayRows, BigDecimal loadWgt) {
        List<TankShare> out = new ArrayList<>();
        if (bayRows.size() == 1) {
            Map<String, Object> only = bayRows.get(0);
            out.add(new TankShare(asLong(only.get("id")), (String) only.get("bay_name"), loadWgt));
            return out;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> r : bayRows) total = total.add(asDecimal(r.get("quantity")));
        BigDecimal assigned = BigDecimal.ZERO;
        for (int k = 0; k < bayRows.size(); k++) {
            Map<String, Object> r = bayRows.get(k);
            BigDecimal share;
            if (k == bayRows.size() - 1) {
                share = loadWgt.subtract(assigned);
            } else if (total.signum() > 0) {
                share = loadWgt.multiply(asDecimal(r.get("quantity"))).divide(total, 3, java.math.RoundingMode.HALF_UP);
            } else {
                share = loadWgt.divide(BigDecimal.valueOf(bayRows.size()), 3, java.math.RoundingMode.HALF_UP);
            }
            assigned = assigned.add(share);
            out.add(new TankShare(asLong(r.get("id")), (String) r.get("bay_name"), share));
        }
        return out;
    }

    private static BigDecimal asDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    private static Long asLong(Object v) {
        return v instanceof Number n ? n.longValue() : null;
    }

    /** Same rollback-only guard as {@link M10BayCompleteActionHandler} — see its javadoc. */
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
