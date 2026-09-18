package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Posts M5 stock for M10.ReturnStockIn once unloading is confirmed complete.
 *
 * <p>{@code aria:unloading-progress}'s {@code qcAllocatedLocations} ListView
 * (added alongside this listener) shows one row per {@code
 * qc_processed_location} allocation — QC's Box/PC split across one or more
 * put-away locations — and lets the operator record a per-row Scan Method
 * (Gun Scanner / Manual) and confirm the Unloading Qty actually put away.
 * That confirmed qty (not the QC-recorded box_qty, though Gun Scanner mode
 * defaults it to exactly that on the frontend — see the page's {@code
 * formula}/{@code formulaWhen} on the Unloading Qty column) is what posts
 * here as a RECEIPT into {@code m5_stock_movement} + {@code
 * m5_stock_balance}, one row per location, the moment the instance reaches
 * {@code UNLOADING_COMPLETED}.
 *
 * <p>Checked against the CURRENT state (like {@link
 * com.aria.mes.stock.StockMovementListener}), not the previous one — this
 * flow has two possible predecessor states (QC_APPROVED directly, or via the
 * legacy UNLOADING_IN_PROGRESS hop) after V325's workflow_definition update,
 * and both must post identically.
 *
 * <p>Deliberately a DEDICATED listener rather than another switch arm on
 * {@link com.aria.mes.stock.StockMovementListener}: that listener posts
 * exactly ONE movement per transition from a handful of scalar fields
 * (itemCode/qty/bin); this flow posts N movements (one per allocated
 * location) from a submitted LIST field, a fundamentally different shape —
 * same reasoning that made {@link QcProcessedLocationTransitionListener} its
 * own class instead of a generic-projection row.
 *
 * <p>Reads the bag by its EXACT page code key ({@link #PAGE_CODE}), not
 * {@code StockMovementListener.mostRecentPageData()} — that helper picks
 * "the last entry" by iterating a plain {@code HashMap}'s values, whose
 * iteration order is NOT insertion/temporal order, so it can silently return
 * a different page's submission on any given JVM. Exact-key lookup (same
 * pattern {@link QcProcessedLocationTransitionListener} already uses) is the
 * only way to reliably reach precisely this page's data.
 */
@Component
public class ReturnStockUnloadingStockPostingListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ReturnStockUnloadingStockPostingListener.class);
    private static final String WORKFLOW_CODE = "M10.ReturnStockIn";
    private static final String TARGET_STATE = "UNLOADING_COMPLETED";
    private static final String PAGE_CODE = "aria:unloading-progress";
    private static final String LIST_FIELD = "qcAllocatedLocations";
    private static final String UOM = "BOX";

    private final JdbcTemplate jdbc;

    public ReturnStockUnloadingStockPostingListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Intentionally NOT @Transactional — same reasoning as StockMovementListener:
    // the engine already holds its own transaction on this connection, and a
    // listener failure must never fail the parent workflow transition.
    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode())) return;
        if (!TARGET_STATE.equals(instance.getCurrentState())) return;
        if (bag == null) return;

        Object pageDataRaw = bag.get(PAGE_CODE);
        if (!(pageDataRaw instanceof Map)) {
            log.debug("ReturnStockUnloadingStockPostingListener: no {} submission for instance {}",
                    PAGE_CODE, instance.getId());
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;
        Object linesRaw = pageData.get(LIST_FIELD);
        if (!(linesRaw instanceof List<?> lines) || lines.isEmpty()) {
            log.debug("ReturnStockUnloadingStockPostingListener: no {} rows for instance {}",
                    LIST_FIELD, instance.getId());
            return;
        }

        long tenantId = TenantContext.get();
        Long instanceId = instance.getId();
        String refNumber = gatePassNumber(tenantId, instanceId);
        int posted = 0;

        for (Object lineRaw : lines) {
            if (!(lineRaw instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> line = (Map<String, Object>) lineRaw;

            String itemCode = str(line, "sku_code", "skuCode");
            String itemName = str(line, "sku_name", "skuName");
            String lotNo = str(line, "batch_no", "batchNo");
            String bin = str(line, "processed_area", "processedArea");
            BigDecimal qty = dec(line, "unloading_qty", "unloadingQty");
            BigDecimal pcQty = dec(line, "pc_qty", "pcQty");

            if (itemCode == null || bin == null || qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("ReturnStockUnloadingStockPostingListener: skipping line for instance {} "
                                + "(itemCode={}, bin={}, unloading_qty={}) — insufficient data",
                        instanceId, itemCode, bin, qty);
                continue;
            }
            if (pcQty == null) pcQty = BigDecimal.ZERO;

            try {
                postOne(tenantId, instanceId, refNumber, itemCode, itemName, lotNo, bin, qty, pcQty);
                posted++;
            } catch (Exception e) {
                log.warn("ReturnStockUnloadingStockPostingListener: post failed for instance {} item={} bin={}: {}",
                        instanceId, itemCode, bin, e.toString());
            }
        }
        log.info("ReturnStockUnloadingStockPostingListener: posted {}/{} location(s) for instance {}",
                posted, lines.size(), instanceId);
    }

    private void postOne(long tenantId, Long instanceId, String refNumber, String itemCode, String itemName,
                          String lotNo, String bin, BigDecimal qty, BigDecimal pcQty) {
        jdbc.update(
                "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, item_name, lot_no, "
                        + "to_bin, qty, uom, ref_type, ref_id, ref_number, notes) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                tenantId, "RECEIPT", itemCode, itemName, lotNo,
                bin, qty, UOM, "DSP", instanceId, refNumber,
                "Auto-posted from M10.ReturnStockIn unloading completion");

        int updated = jdbc.update(
                "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, "
                        + "pc_qty_on_hand = pc_qty_on_hand + ?, last_movement_at = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id=? AND item_code=? AND "
                        + "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code=?",
                qty, pcQty, tenantId, itemCode, lotNo, lotNo, bin);
        if (updated == 0) {
            jdbc.update(
                    "INSERT INTO m5_stock_balance (tenant_id, item_code, item_name, lot_no, "
                            + "bin_code, uom, qty_on_hand, pc_qty_on_hand, last_movement_at) "
                            + "VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                    tenantId, itemCode, itemName, lotNo, bin, UOM, qty, pcQty);
        }
    }

    /** gate_pass.gp_number for this instance, for the movement ledger's ref_number — best-effort. */
    private String gatePassNumber(long tenantId, Long instanceId) {
        List<String> rows = jdbc.queryForList(
                "SELECT gp_number FROM gate_pass WHERE tenant_id=? AND workflow_instance_id=?",
                String.class, tenantId, instanceId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static String str(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
        }
        return null;
    }

    private static BigDecimal dec(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v == null || String.valueOf(v).isBlank()) continue;
            if (v instanceof BigDecimal bd) return bd;
            if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
            try {
                return new BigDecimal(String.valueOf(v));
            } catch (NumberFormatException ignored) {
                // try next alias
            }
        }
        return null;
    }
}
