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
 * {@code m10.bay-complete} — the "Bay Complete" button on
 * aria:loading-in-progress-entry (page 428). Records the actual processed
 * quantity for every open {@code loading_progress_detail_bay} row belonging
 * to the current bay session (opened by {@link M10StartBayActionHandler})
 * and closes them (status DONE), freeing this vehicle to Start Bay again
 * for the next bay. Deliberately does NOT touch workflow state — the
 * workflow engine is never called here — matching how this button has
 * always behaved: it saves the bay's data without advancing the page.
 *
 * <p>Matched by (route_id, order_id, sku_code) per line — order_id (V252) is
 * NULL-safe-matched alongside route_id since not every loading step groups
 * by the same identifier (see {@link M10StartBayActionHandler}'s javadoc);
 * whatever this line's row was opened with must be echoed back here
 * unchanged for the match to find it. The operator edits loading_qty inline
 * per SKU row, then Bay Complete submits the whole array at once.
 *
 * <p>quantity vs processed quantity (Bay 1 = 100 + Bay 2 = 200 + Bay 3 =
 * 500 assigned, vs however much actually gets processed) is tracked for
 * visibility on loading_progress_detail (V177) — it does not gate
 * "Completed"; see {@code M10BayQuantityCompletionValidator}, which only
 * requires every bay to be closed, not the totals to match. The leftover
 * (quantity - processed_quantity) is stored on the row itself as
 * {@code balance_qty} (V257) in the same UPDATE that closes it — a plain
 * column, not computed on read, so a datasource query can filter directly
 * on it (e.g. balance_qty &gt; 10) with an ordinary literal-value
 * comparison. Stays NULL while the row is IN_PROGRESS, same lifecycle as
 * processed_quantity.
 *
 * <p>Each closed line also deducts {@code loading_qty} from stock: picked
 * FEFO (oldest-first, across bins — {@code route_id} is a delivery-route
 * identifier, not a warehouse bin, so there is no bin to key the deduction
 * on) via {@link FefoStockPickerService}, one {@code m5_stock_movement}
 * ledger row (kind {@code CONSUME}) plus one {@code m5_stock_balance}
 * {@code qty_on_hand} decrement per lot picked. A line whose SKU doesn't
 * have enough on-hand stock to cover {@code loading_qty} errors out (same
 * as any other line validation failure) before its bay row is closed —
 * never mark a bay DONE for stock that wasn't actually available to pull.
 *
 * <p><b>Concurrency guard:</b> {@code FefoStockPickerService.pick} is a
 * plain unlocked {@code SELECT} — two near-simultaneous Bay Complete calls
 * against the same SKU could both read "enough stock" from the same stale
 * snapshot. The actual deduction is therefore re-checked atomically at
 * UPDATE time ({@code WHERE ... AND qty_on_hand >= ?}, row-count verified) —
 * {@code qty_on_hand} can never be driven negative by this handler, and a
 * lot that lost the race errors the whole line out (before its bay row is
 * closed) rather than silently under-deducting or going negative. Per-line
 * deduction happens fully before that line's bay row closes, for the same
 * "never mark DONE for stock we didn't actually get" reason above.
 *
 * <p><b>Whole-request atomicity:</b> a plain {@code return ActionResult.error(...)}
 * does NOT roll back a Spring {@code @Transactional} method — only a thrown
 * exception does (see {@link M10StartBayActionHandler}'s identical note on
 * its own workflow-transition branch). Without an explicit rollback, a later
 * line failing (bad payload, insufficient stock, a lost stock race, or an
 * already-closed bay row) would let every earlier line's stock deduction +
 * bay-row close commit anyway while the whole action reports failure — and
 * since deduction happens before the bay-row-open check, a resubmitted line
 * that's already DONE would silently double-consume stock before erroring.
 * {@link #markRollbackOnly()} is called on every error return from inside
 * the line loop so a failed Bay Complete never leaves partial stock/bay
 * changes behind — same as the whole-request-must-fully-succeed workflow
 * this button already implies.
 *
 * <p>Payload shape (formData.order_sku_line_items — the grid's current rows
 * with loading_qty already edited by the operator):
 * <pre>
 *   { "instanceId": 1495,
 *     "formData": { "order_sku_line_items": [
 *       { "route_id": "ROU028912", "sku_code": "SKU1215", "loading_qty": 45 },
 *       { "route_id": "ROU028912", "sku_code": "SKU1461", "loading_qty": 250 }
 *     ] } }
 * </pre>
 */
@Component
public class M10BayCompleteActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(M10BayCompleteActionHandler.class);

    @Autowired private JdbcTemplate jdbc;
    @Autowired private FefoStockPickerService fefoPicker;

    @Override
    public String code() { return "m10.bay-complete"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        Object linesObj = formData.get("order_sku_line_items");
        if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) {
            return ActionResult.error("formData.order_sku_line_items must be a non-empty array");
        }
        List<Map<String, Object>> lines = (List<Map<String, Object>>) linesObj;

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();

        log.info("action.bay-complete.start tenantId={} instanceId={} lineCount={}", tenantId, instanceId, lines.size());

        List<Map<String, Object>> closed = new ArrayList<>();
        BigDecimal totalProcessed = BigDecimal.ZERO;

        for (int i = 0; i < lines.size(); i++) {
            Object lineObj = lines.get(i);
            if (!(lineObj instanceof Map)) {
                markRollbackOnly();
                log.warn("action.bay-complete.bad-line tenantId={} instanceId={} index={} reason=not-an-object", tenantId, instanceId, i);
                return ActionResult.error("order_sku_line_items[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lineObj;
            String routeId = str(line, "route_id");
            String orderId = str(line, "order_id");
            String skuCode = str(line, "sku_code");
            if (routeId == null || skuCode == null) {
                markRollbackOnly();
                log.warn("action.bay-complete.bad-line tenantId={} instanceId={} index={} reason=missing-route-or-sku", tenantId, instanceId, i);
                return ActionResult.error("order_sku_line_items[" + i + "] is missing route_id or sku_code");
            }

            BigDecimal processedQty;
            try {
                Object qtyObj = line.get("loading_qty") != null ? line.get("loading_qty") : line.get("processed_quantity");
                processedQty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                markRollbackOnly();
                log.warn("action.bay-complete.bad-line tenantId={} instanceId={} routeId={} skuCode={} reason=non-numeric-qty",
                        tenantId, instanceId, routeId, skuCode);
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") loading_qty must be a number");
            }
            if (processedQty == null || processedQty.signum() < 0) {
                markRollbackOnly();
                log.warn("action.bay-complete.bad-line tenantId={} instanceId={} routeId={} skuCode={} reason=negative-or-missing-qty",
                        tenantId, instanceId, routeId, skuCode);
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs loading_qty >= 0");
            }

            List<FefoStockPickerService.FefoPick> picks = fefoPicker.pick(tenantId, skuCode, null, processedQty);
            BigDecimal residual = fefoPicker.residualQty(processedQty, picks);
            if (residual.signum() > 0) {
                markRollbackOnly();
                log.warn("action.bay-complete.insufficient-stock tenantId={} instanceId={} routeId={} skuCode={} requestedQty={} availableQty={}",
                        tenantId, instanceId, routeId, skuCode, processedQty, processedQty.subtract(residual));
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") insufficient stock: "
                        + "requested " + processedQty + ", available " + processedQty.subtract(residual));
            }

            for (FefoStockPickerService.FefoPick pick : picks) {
                int deducted = jdbc.update(
                        "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand - ?, last_movement_at = CURRENT_TIMESTAMP "
                                + "WHERE tenant_id = ? AND item_code = ? AND "
                                + "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code = ? AND qty_on_hand >= ?",
                        pick.qty(), tenantId, skuCode, pick.lotNo(), pick.lotNo(), pick.binCode(), pick.qty());
                if (deducted == 0) {
                    markRollbackOnly();
                    log.warn("action.bay-complete.stock-race tenantId={} instanceId={} routeId={} skuCode={} lotNo={} binCode={} qty={}",
                            tenantId, instanceId, routeId, skuCode, pick.lotNo(), pick.binCode(), pick.qty());
                    return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") stock changed "
                            + "concurrently — lot " + pick.lotNo() + "/bin " + pick.binCode()
                            + " no longer has enough qty_on_hand, retry Bay Complete");
                }
                jdbc.update(
                        "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, qty, uom, "
                                + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)",
                        tenantId, "CONSUME", skuCode, pick.lotNo(), pick.binCode(), pick.qty(), pick.uom(),
                        "BAY", instanceId, routeId);
                log.info("action.bay-complete.stock-consume tenantId={} instanceId={} routeId={} skuCode={} lotNo={} binCode={} qty={} uom={}",
                        tenantId, instanceId, routeId, skuCode, pick.lotNo(), pick.binCode(), pick.qty(), pick.uom());
            }

            int n = jdbc.update(
                    "UPDATE loading_progress_detail_bay SET processed_quantity = ?, balance_qty = quantity - ?, status = 'DONE' "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                    processedQty, processedQty, tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (n == 0) {
                markRollbackOnly();
                log.warn("action.bay-complete.bay-row-missing tenantId={} instanceId={} routeId={} orderId={} skuCode={}",
                        tenantId, instanceId, routeId, orderId, skuCode);
                return ActionResult.error("no open bay row found for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode);
            }

            log.info("action.bay-complete.bay-row-closed tenantId={} instanceId={} routeId={} skuCode={} processedQty={}",
                    tenantId, instanceId, routeId, skuCode, processedQty);

            totalProcessed = totalProcessed.add(processedQty);
            Map<String, Object> row = new HashMap<>();
            row.put("routeId", routeId);
            row.put("skuCode", skuCode);
            row.put("processedQuantity", processedQty);
            closed.add(row);
        }

        jdbc.update(
                "UPDATE loading_progress_detail SET processed_quantity = processed_quantity + ? "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                totalProcessed, tenantId, instanceId);

        log.info("action.bay-complete.done tenantId={} instanceId={} linesClosed={} totalProcessed={}",
                tenantId, instanceId, closed.size(), totalProcessed);

        return ActionResult.builder()
                .ok(true)
                .message(closed.size() + " bay line(s) closed, processed " + totalProcessed + " total — pick another bay or press Completed")
                .data(Map.of("closed", closed, "totalProcessed", totalProcessed))
                .build();
    }

    /**
     * Marks the enclosing {@code @Transactional} method rollback-only before
     * an error return, so a mid-loop failure doesn't leave earlier lines'
     * stock deduction / bay-row close committed (see class javadoc). Guarded
     * on {@link TransactionSynchronizationManager#isActualTransactionActive()}
     * because {@code TransactionAspectSupport.currentTransactionStatus()}
     * throws {@code NoTransactionException} outside a live Spring AOP proxy —
     * exactly the unit-test setup this handler's own test suite uses (plain
     * {@code new M10BayCompleteActionHandler()} + a mocked JdbcTemplate, no
     * real transaction manager), same constraint {@code M10StartBayActionHandler}
     * documents on its own rollback branch.
     */
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
