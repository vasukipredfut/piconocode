package com.aria.mes.stock;

import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Writes {@code m5_stock_movement} ledger rows and upserts
 * {@code m5_stock_balance} when key M5 workflows reach a stock-affecting
 * state. The mapping is intentionally small + explicit (not a config-driven
 * rules engine yet — that's TASK_M5_STOCK_RULES); each new state handled
 * is a single switch arm here.
 *
 * <h3>Handled transitions</h3>
 * <table>
 *   <tr><th>Workflow</th><th>Target state</th><th>Kind</th><th>Effect</th></tr>
 *   <tr><td>M5.InwardReceipt</td><td>STORED</td><td>RECEIPT</td><td>+qty into to_bin</td></tr>
 *   <tr><td>M5.MaterialRequisition</td><td>ISSUED</td><td>ISSUE</td><td>-qty from from_bin</td></tr>
 *   <tr><td>M5.JobCard</td><td>CONSUMED</td><td>CONSUME</td><td>-qty from from_bin</td></tr>
 *   <tr><td>M5.PackingRun</td><td>SPOIL</td><td>SPOIL</td><td>-qty from from_bin</td></tr>
 *   <tr><td>M5.StockAdjust</td><td>POSTED</td><td>ADJUST</td><td>delta-set to_bin</td></tr>
 *   <tr><td>M5.FGDispatch</td><td>DISPATCHED</td><td>ISSUE</td><td>-qty from from_bin (FG)</td></tr>
 *   <tr><td>M10.PackUnload</td><td>UNLOADING_COMPLETED</td><td>RECEIPT</td><td>+qty into bin_code/godown_code (aria:supervisor-unloading)</td></tr>
 *   <tr><td>M10.ReturnStockIn</td><td>UNLOADING_DONE</td><td>RECEIPT</td><td>+qty into bin_code/godown_code (aria:supervisor-unloading)</td></tr>
 *   <tr><td>M10.TankerInbound</td><td>QTY_RECONCILED</td><td>RECEIPT</td><td>+net weight (W1-W2) into a synthetic "BULK-TANK" bin; item comes from
 *       gate_pass.oil_rm_item (free text — no ItemMaster link exists for tanker inbound today), not the aria:reconciliation submission itself.
 *       Manager-override branch (QTY_PENDING_MANAGER -&gt; PMT_CHECK_OK) is NOT handled yet — flagged, not fixed here.</td></tr>
 * </table>
 *
 * <p>Listener failures are caught upstream by the engine — they cannot fail
 * the transition. Mismatch between balance and movement is reported via
 * {@code log.warn}; the ledger is the source of truth.
 */
@Component
public class StockMovementListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(StockMovementListener.class);

    private final JdbcTemplate jdbc;

    public StockMovementListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Intentionally NOT @Transactional — the calling WorkflowEngineService
    // already holds a JpaTransactionManager transaction on the same connection,
    // and starting a nested DataSourceTransactionManager tx here triggered
    // "Pre-bound JDBC Connection found!" which escaped the engine's listener
    // try/catch and surfaced as a /error 401. JdbcTemplate works fine without
    // it (auto-commit per statement) and a listener failure must never fail
    // the parent workflow transition.
    @Override
    public void onTransition(WorkflowInstance instance, String previousState,
                              Map<String, Object> submittedDataBag) {
        String workflow = instance.getWorkflowCode();
        String target = instance.getCurrentState();
        StockOp op = mapToStockOp(workflow, target);
        if (op == null) return;

        // Pull common fields from the submitted bag (page submissions can
        // appear under several keys depending on the workflow page); fall
        // back to "latest" page if not found on a specific code.
        Map<String, Object> data = mostRecentPageData(submittedDataBag);
        if (data == null) {
            log.debug("StockMovementListener: no submission data for {}->{}", workflow, target);
            return;
        }

        try {
            String itemCode = str(data, "itemCode", "item_code");
            String itemName = str(data, "itemName", "item_description");
            String lotNo = str(data, "lotNo", "finalLotNo", "linkedLotNo", "batch_no");
            String bin = str(data, "storeTankCode", "binCode", "binLocation", "fgLocation", "toBin", "bin_code", "godown_code");
            String fromBin = str(data, "fromBin", "binLocation", "bin_code");
            BigDecimal qty = decimal(data, "qtyIssued", "qtyConsumed", "qtyReceived", "qtyDispatched", "countedQty", "qty", "received_qty");
            String uom = str(data, "uom");
            String refType = op.refType;
            String refNumber = str(data, "grnNumber", "mrNumber", "jcNumber", "gpNumber", "soNumber", "dispatchNumber", "gp_number");
            Long tenantId = instance.getTenantId() != null ? instance.getTenantId() : 1L;

            if ("M10.TankerInbound".equals(workflow)) {
                // No item/qty/bin fields exist on this flow's pages at all
                // (aria:reconciliation only carries weights + variance reason;
                // Goods-Receipt/Bin-Allocation sections on aria:supervisor-unloading
                // are explicitly hidden for this flow variant — see
                // V125__tankerinbound_remove_parking_states.sql). Best-effort
                // fallback: item = the free-text oil_rm_item captured earlier at
                // GATE_PASS_CREATED->DISPATCH_QUEUED, qty = net weight (W1-W2)
                // already computed on this same aria:reconciliation submission
                // (or re-derived from weighment_capture_detail if absent), bin =
                // a synthetic bucket since no physical tank is tracked per-flow.
                if (itemCode == null) itemCode = tankerInboundOilItem(tenantId, instance.getId());
                if (qty == null) qty = tankerInboundNetWeight(data, tenantId, instance.getId());
                if (bin == null) bin = "BULK-TANK";
                if (uom == null) uom = "KG";
            }
            uom = Optional.ofNullable(uom).orElse("EA");

            if (itemCode == null || qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("StockMovementListener: insufficient data ({} qty={}), skipping", itemCode, qty);
                return;
            }

            String fb = op.kind.equals("RECEIPT") ? null : Optional.ofNullable(fromBin).orElse(bin);
            String tb = op.kind.equals("RECEIPT") ? bin : null;

            jdbc.update(
                    "INSERT INTO m5_stock_movement (tenant_id, kind, item_code, item_name, lot_no, " +
                            "from_bin, to_bin, qty, uom, ref_type, ref_id, ref_number, notes) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    tenantId, op.kind, itemCode, itemName, lotNo,
                    fb, tb, qty, uom, refType, instance.getId(), refNumber,
                    "Auto-posted from " + workflow + " " + previousState + "->" + target);

            // Upsert the balance row. Net delta into qty_on_hand:
            //   RECEIPT/ADJUST(+): +qty into to_bin
            //   ISSUE/CONSUME/SPOIL: -qty from from_bin
            int sign = op.kind.equals("RECEIPT") || op.kind.equals("ADJUST") ? 1 : -1;
            String affectedBin = sign > 0 ? tb : fb;
            if (affectedBin != null) {
                int updated = jdbc.update(
                        "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, " +
                                "last_movement_at = CURRENT_TIMESTAMP " +
                                "WHERE tenant_id=? AND item_code=? AND " +
                                "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code=?",
                        qty.multiply(BigDecimal.valueOf(sign)), tenantId, itemCode, lotNo, lotNo, affectedBin);
                if (updated == 0 && sign > 0) {
                    // New balance row for a fresh lot+bin combination
                    jdbc.update(
                            "INSERT INTO m5_stock_balance (tenant_id, item_code, item_name, lot_no, " +
                                    "bin_code, uom, qty_on_hand, last_movement_at) " +
                                    "VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                            tenantId, itemCode, itemName, lotNo, affectedBin, uom, qty);
                } else if (updated == 0) {
                    log.warn("StockMovementListener: ISSUE/CONSUME on non-existent balance ({} lot={} bin={}); ledger row still recorded",
                            itemCode, lotNo, affectedBin);
                }
            }
        } catch (Exception e) {
            log.warn("StockMovementListener failed for {}->{}: {}", workflow, target, e.toString());
        }
    }

    // ---- internal ----------------------------------------------------------

    private static StockOp mapToStockOp(String workflow, String target) {
        if (workflow == null || target == null) return null;
        return switch (workflow + "/" + target) {
            case "M5.InwardReceipt/STORED" -> new StockOp("RECEIPT", "GRN");
            case "M5.MaterialRequisition/ISSUED" -> new StockOp("ISSUE", "MR");
            case "M5.JobCard/CONSUMED" -> new StockOp("CONSUME", "JC");
            case "M5.PackingRun/SPOIL" -> new StockOp("SPOIL", "PACK");
            case "M5.StockAdjust/POSTED" -> new StockOp("ADJUST", "STK-ADJ");
            case "M5.FGDispatch/DISPATCHED" -> new StockOp("ISSUE", "DSP");
            case "M10.PackUnload/UNLOADING_COMPLETED" -> new StockOp("RECEIPT", "GRN");
            case "M10.ReturnStockIn/UNLOADING_DONE" -> new StockOp("RECEIPT", "GRN");
            case "M10.TankerInbound/QTY_RECONCILED" -> new StockOp("RECEIPT", "GRN");
            default -> null;
        };
    }

    /** gate_pass.oil_rm_item for this instance — see class javadoc on M10.TankerInbound. */
    private String tankerInboundOilItem(Long tenantId, Long instanceId) {
        List<String> rows = jdbc.queryForList(
                "SELECT oil_rm_item FROM gate_pass WHERE tenant_id=? AND workflow_instance_id=? AND oil_rm_item IS NOT NULL",
                String.class, tenantId, instanceId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Net weight (W1-W2), preferring the current submission's own fields over a DB lookup. */
    private BigDecimal tankerInboundNetWeight(Map<String, Object> data, Long tenantId, Long instanceId) {
        BigDecimal w1 = decimal(data, "first_weight_kg", "w1_weight_kg");
        BigDecimal w2 = decimal(data, "second_weight_kg", "w2_weight_kg");
        if (w1 == null || w2 == null) {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT w1_weight_kg, w2_weight_kg FROM weighment_capture_detail WHERE tenant_id=? AND workflow_instance_id=?",
                    tenantId, instanceId);
            if (!rows.isEmpty()) {
                if (w1 == null) w1 = (BigDecimal) rows.get(0).get("w1_weight_kg");
                if (w2 == null) w2 = (BigDecimal) rows.get(0).get("w2_weight_kg");
            }
        }
        return (w1 == null || w2 == null) ? null : w1.subtract(w2);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> mostRecentPageData(Map<String, Object> bag) {
        if (bag == null || bag.isEmpty()) return null;
        // Prefer the LAST entry — most callers append in temporal order.
        Object last = null;
        for (Object v : bag.values()) {
            if (v instanceof Map<?, ?>) last = v;
        }
        return last == null ? null : (Map<String, Object>) last;
    }

    private static String str(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
        }
        return null;
    }

    private static BigDecimal decimal(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v == null) continue;
            try {
                return new BigDecimal(String.valueOf(v).trim());
            } catch (NumberFormatException ignored) { /* try next */ }
        }
        return null;
    }

    private record StockOp(String kind, String refType) { }

    // Helpers used by IT to seed minimal balance rows from setup code.
    public static Map<String, Object> sampleSubmission(String item, String lot, String bin, BigDecimal qty) {
        Map<String, Object> m = new HashMap<>();
        m.put("itemCode", item);
        m.put("lotNo", lot);
        m.put("storeTankCode", bin);
        m.put("qtyReceived", qty);
        m.put("uom", "KG");
        return m;
    }
}
