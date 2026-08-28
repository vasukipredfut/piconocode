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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code m10.unload-bay-complete} — the "Stop Bay" button on the Reject Flow
 * Unloading page. Sibling of {@link M10BayCompleteActionHandler}, reversed:
 * closes every open {@code unloading_progress_detail_bay} row named in
 * {@code formData.order_sku_line_items}, adds the unloaded quantity back to
 * stock, and soft-reverses the matching {@code loading_progress_detail_bay}
 * row (status {@code REVERSED} — never hard-deleted, so the vehicle's
 * original load history stays intact for audit).
 *
 * <p><b>Which lot/bin gets the stock back:</b> rather than picking an
 * arbitrary bin, this reverses the SAME lots/bins the original Bay Complete
 * (loading) consumed for this exact (instance, sku) — read back from their
 * {@code m5_stock_movement} CONSUME rows ({@code ref_type='BAY'}), oldest
 * first, capped per row at what that row actually took. If the unload qty
 * exceeds what was ever recorded as consumed here (shouldn't happen — can't
 * unload more than was loaded — but data can be messy), the residual goes
 * into a synthetic {@code RETURNS} bin (lot_no NULL) rather than failing the
 * whole request, same fallback-bin spirit as {@code StockMovementListener}'s
 * TankerInbound {@code BULK-TANK} bucket.
 *
 * <p>Movement kind is {@code RETURN} (not {@code RECEIPT} — this is a
 * reversal of a specific prior CONSUME, not a fresh goods receipt).
 *
 * <p><b>Partial unload across multiple bays:</b> a vehicle can come back for
 * a second (or third...) Start Bay / Bay Complete cycle against the SAME
 * originally-loaded (route_id, order_id, sku_code) to take off the rest of
 * it later. The original {@code loading_progress_detail_bay} row is only
 * flipped to {@code REVERSED} once everything unloaded so far — summed
 * across every {@code unloading_progress_detail_bay} session on record for
 * that SKU/route, not just this call's line — reaches its original
 * {@code quantity}; otherwise it's put back to {@code DONE} so the next
 * Start Bay can find and re-open it. {@code quantity} itself is never
 * touched (same "never touch the original loaded numbers" rule as the class
 * doc on {@code loading_progress_detail_bay} — see V256/docs/m10/REJECT-FLOW-UNLOAD.md).
 *
 * <p>Whole-request atomicity and the rollback-only note on every error
 * return mirror {@link M10BayCompleteActionHandler} exactly — a plain
 * {@code ActionResult.error(...)} does not itself roll back a Spring
 * {@code @Transactional} method.
 */
@Component
public class M10UnloadBayCompleteActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(M10UnloadBayCompleteActionHandler.class);
    private static final String FALLBACK_BIN = "RETURNS";

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.unload-bay-complete"; }

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

        log.info("action.unload-bay-complete.start tenantId={} instanceId={} lineCount={}", tenantId, instanceId, lines.size());

        List<Map<String, Object>> closed = new ArrayList<>();
        BigDecimal totalProcessed = BigDecimal.ZERO;

        for (int i = 0; i < lines.size(); i++) {
            Object lineObj = lines.get(i);
            if (!(lineObj instanceof Map)) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lineObj;
            String routeId = str(line, "route_id");
            String orderId = str(line, "order_id");
            String skuCode = str(line, "sku_code");
            if (routeId == null || skuCode == null) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] is missing route_id or sku_code");
            }

            BigDecimal unloadQty;
            try {
                Object qtyObj = line.get("unloading_qty") != null ? line.get("unloading_qty") : line.get("processed_quantity");
                unloadQty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") unloading_qty must be a number");
            }
            if (unloadQty == null || unloadQty.signum() < 0) {
                markRollbackOnly();
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs unloading_qty >= 0");
            }

            for (ReturnTarget target : returnTargets(tenantId, instanceId, skuCode, unloadQty)) {
                int updated = jdbc.update(
                        "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, last_movement_at = CURRENT_TIMESTAMP "
                                + "WHERE tenant_id = ? AND item_code = ? AND "
                                + "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code = ?",
                        target.qty(), tenantId, skuCode, target.lotNo(), target.lotNo(), target.binCode());
                if (updated == 0) {
                    jdbc.update(
                            "INSERT INTO m5_stock_balance (tenant_id, item_code, lot_no, bin_code, uom, qty_on_hand, last_movement_at) "
                                    + "VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                            tenantId, skuCode, target.lotNo(), target.binCode(), target.uom(), target.qty());
                }
                jdbc.update(
                        "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, to_bin, qty, uom, "
                                + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)",
                        tenantId, "RETURN", skuCode, target.lotNo(), target.binCode(), target.qty(), target.uom(),
                        "UNLOAD_BAY", instanceId, routeId);
                log.info("action.unload-bay-complete.stock-return tenantId={} instanceId={} routeId={} skuCode={} lotNo={} binCode={} qty={}",
                        tenantId, instanceId, routeId, skuCode, target.lotNo(), target.binCode(), target.qty());
            }

            int n = jdbc.update(
                    "UPDATE unloading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'",
                    unloadQty, tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (n == 0) {
                markRollbackOnly();
                return ActionResult.error("no open unload bay row found for route_id=" + routeId + " order_id=" + orderId + " sku_code=" + skuCode);
            }

            // Partial unload support: this vehicle may come back for a SECOND
            // (or third...) bay against the same originally-loaded SKU/route
            // to take off the rest of it. Only mark the original row fully
            // REVERSED once everything unloaded so far (summed across every
            // unloading_progress_detail_bay session for this SKU/route, not
            // just this one) reaches what was originally loaded -- otherwise
            // put it back to DONE so the next Start Bay can find it again
            // (mirrors the very first load: DONE == "available to act on").
            List<Map<String, Object>> loadedRow = jdbc.queryForList(
                    "SELECT quantity FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                            + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'UNLOAD_IN_PROGRESS'",
                    tenantId, instanceId, routeId, orderId, orderId, skuCode);
            if (!loadedRow.isEmpty()) {
                BigDecimal originalQty = (BigDecimal) loadedRow.get(0).get("quantity");
                BigDecimal unloadedSoFar = jdbc.queryForObject(
                        "SELECT COALESCE(SUM(processed_quantity), 0) FROM unloading_progress_detail_bay "
                                + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                                + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'DONE'",
                        BigDecimal.class, tenantId, instanceId, routeId, orderId, orderId, skuCode);
                String nextStatus = unloadedSoFar.compareTo(originalQty) >= 0 ? "REVERSED" : "DONE";
                jdbc.update(
                        "UPDATE loading_progress_detail_bay SET status = ? "
                                + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                                + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'UNLOAD_IN_PROGRESS'",
                        nextStatus, tenantId, instanceId, routeId, orderId, orderId, skuCode);
                log.info("action.unload-bay-complete.loading-row-status tenantId={} instanceId={} routeId={} skuCode={} originalQty={} unloadedSoFar={} nextStatus={}",
                        tenantId, instanceId, routeId, skuCode, originalQty, unloadedSoFar, nextStatus);
            }

            totalProcessed = totalProcessed.add(unloadQty);
            Map<String, Object> row = new HashMap<>();
            row.put("routeId", routeId);
            row.put("skuCode", skuCode);
            row.put("processedQuantity", unloadQty);
            closed.add(row);
        }

        String currentStep = jdbc.queryForObject(
                "SELECT current_state FROM workflow_instance WHERE id = ? AND tenant_id = ?",
                String.class, instanceId, tenantId);

        jdbc.update(
                "UPDATE unloading_progress_detail SET processed_quantity = processed_quantity + ?, current_step = ? "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ?",
                totalProcessed, currentStep, tenantId, instanceId);

        // Once every loading_progress_detail_bay row for this vehicle has been
        // reversed (or was never DONE to begin with), flip the parent rollup too
        // -- the natural point at which "this vehicle's load" is fully given back.
        Integer stillActive = jdbc.queryForObject(
                "SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? "
                        + "AND status NOT IN ('REVERSED')",
                Integer.class, tenantId, instanceId);
        if (stillActive != null && stillActive == 0) {
            jdbc.update(
                    "UPDATE loading_progress_detail SET status = 'REVERSED' WHERE tenant_id = ? AND workflow_instance_id = ?",
                    tenantId, instanceId);
        }

        log.info("action.unload-bay-complete.done tenantId={} instanceId={} linesClosed={} totalProcessed={}",
                tenantId, instanceId, closed.size(), totalProcessed);

        return ActionResult.builder()
                .ok(true)
                .message(closed.size() + " unload bay line(s) closed, returned " + totalProcessed + " total to stock")
                .data(Map.of("closed", closed, "totalProcessed", totalProcessed))
                .build();
    }

    /**
     * Walks this (instance, sku)'s original CONSUME movements oldest-first,
     * capping each at its own recorded qty, until {@code unloadQty} is
     * covered. Any residual (unloadQty exceeds everything ever consumed here)
     * goes to {@link #FALLBACK_BIN}.
     */
    private List<ReturnTarget> returnTargets(long tenantId, Long instanceId, String skuCode, BigDecimal unloadQty) {
        List<Map<String, Object>> consumed = jdbc.queryForList(
                "SELECT lot_no, from_bin, uom, qty FROM m5_stock_movement "
                        + "WHERE tenant_id = ? AND kind = 'CONSUME' AND ref_type = 'BAY' AND ref_id = ? AND item_code = ? "
                        + "ORDER BY id ASC",
                tenantId, instanceId, skuCode);

        List<ReturnTarget> targets = new ArrayList<>();
        BigDecimal remaining = unloadQty;
        String uom = "EA";
        for (Map<String, Object> row : consumed) {
            if (remaining.signum() <= 0) break;
            BigDecimal rowQty = (BigDecimal) row.get("qty");
            uom = row.get("uom") != null ? (String) row.get("uom") : uom;
            BigDecimal take = rowQty.compareTo(remaining) >= 0 ? remaining : rowQty;
            targets.add(new ReturnTarget((String) row.get("lot_no"), (String) row.get("from_bin"), take, uom));
            remaining = remaining.subtract(take);
        }
        if (remaining.signum() > 0) {
            targets.add(new ReturnTarget(null, FALLBACK_BIN, remaining, uom));
        }
        return targets;
    }

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

    private record ReturnTarget(String lotNo, String binCode, BigDecimal qty, String uom) {}
}
