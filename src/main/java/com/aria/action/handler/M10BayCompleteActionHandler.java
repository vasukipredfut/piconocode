package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
 * <p>Matched by (route_id, sku_code) per line, the same identifying pair
 * the grid already keys rows by — the operator edits loading_qty inline per
 * SKU row, then Bay Complete submits the whole array at once.
 *
 * <p>quantity vs processed quantity (Bay 1 = 100 + Bay 2 = 200 + Bay 3 =
 * 500 assigned, vs however much actually gets processed) is tracked for
 * visibility on loading_progress_detail (V177) — it does not gate
 * "Completed"; see {@code M10BayQuantityCompletionValidator}, which only
 * requires every bay to be closed, not the totals to match.
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

    @Autowired private JdbcTemplate jdbc;

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

        List<Map<String, Object>> closed = new ArrayList<>();
        BigDecimal totalProcessed = BigDecimal.ZERO;

        for (int i = 0; i < lines.size(); i++) {
            Map<String, Object> line = lines.get(i);
            String routeId = str(line, "route_id");
            String skuCode = str(line, "sku_code");
            if (routeId == null || skuCode == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] is missing route_id or sku_code");
            }

            BigDecimal processedQty;
            try {
                Object qtyObj = line.get("loading_qty") != null ? line.get("loading_qty") : line.get("processed_quantity");
                processedQty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") loading_qty must be a number");
            }
            if (processedQty == null || processedQty.signum() < 0) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs loading_qty >= 0");
            }

            int n = jdbc.update(
                    "UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                            + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND sku_code = ? AND status = 'IN_PROGRESS'",
                    processedQty, tenantId, instanceId, routeId, skuCode);
            if (n == 0) {
                return ActionResult.error("no open bay row found for route_id=" + routeId + " sku_code=" + skuCode);
            }

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

        return ActionResult.builder()
                .ok(true)
                .message(closed.size() + " bay line(s) closed, processed " + totalProcessed + " total — pick another bay or press Completed")
                .data(Map.of("closed", closed, "totalProcessed", totalProcessed))
                .build();
    }

    private static String str(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        return v == null ? null : v.toString();
    }
}
