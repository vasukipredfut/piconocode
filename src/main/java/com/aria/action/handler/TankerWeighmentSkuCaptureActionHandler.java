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
 * itself is deliberately left untouched: FEFO-deduct stock for that SKU
 * (same picker as Bay Complete), insert one {@code m5_stock_movement} row
 * per lot picked (kind {@code CONSUME}, same as Bay Complete;
 * {@code ref_type TANKER}), decrement {@code m5_stock_balance.qty_on_hand}
 * for each lot picked, then close the matching
 * {@code loading_progress_detail_bay} row (route_id + sku_code, status
 * IN_PROGRESS -&gt; DONE) and roll the total into
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

            List<FefoStockPickerService.FefoPick> picks = fefoPicker.pick(tenantId, skuCode, null, loadWgt);
            BigDecimal residual = fefoPicker.residualQty(loadWgt, picks);
            if (residual.signum() > 0) {
                markRollbackOnly();
                log.warn("action.tanker-weighment-capture.insufficient-stock tenantId={} instanceId={} routeId={} skuCode={} requestedQty={} availableQty={}",
                        tenantId, instanceId, routeId, skuCode, loadWgt, loadWgt.subtract(residual));
                return ActionResult.error("tankerSkuWeights[" + i + "] (" + skuCode + ") insufficient stock: "
                        + "requested " + loadWgt + ", available " + loadWgt.subtract(residual));
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

            int n = jdbc.update(
                    "UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                    loadWgt, tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (n == 0) {
                markRollbackOnly();
                log.warn("action.tanker-weighment-capture.bay-row-missing tenantId={} instanceId={} routeId={} orderId={} skuCode={}",
                        tenantId, instanceId, routeId, orderId, skuCode);
                return ActionResult.error("no open bay row found for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode);
            }

            log.info("action.tanker-weighment-capture.bay-row-closed tenantId={} instanceId={} routeId={} skuCode={} loadWgt={}",
                    tenantId, instanceId, routeId, skuCode, loadWgt);

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
