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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@code m10.stockinward-start-bay} — M10.StockInward's dedicated "Start Bay",
 * kept separate from {@link M10StartUnloadingBayActionHandler} (the shared
 * M10.PackUnload one) per explicit business request rather than adding a
 * workflow_code branch to that shared handler.
 *
 * <p>Identical mechanics to {@link M10StartUnloadingBayActionHandler} (opens
 * one {@code unloading_progress_detail_bay} row per SKU line, fresh receiving
 * -- no prior loaded row required), plus captures {@code rd_code}/
 * {@code vendor_name} per line (the order sheet's vendor code) onto the row,
 * for {@link M10StockInwardBayCompleteActionHandler} to derive the stock
 * movement's {@code from_bin} from.
 *
 * <pre>
 *   { "instanceId": 1495, "formData": {
 *       "vehicle_no": "TN86J8070", "gpNumber": "GP-1-1495", "bay_name": "Bay1",
 *       "godown_code": "SRG0001", "plant_code": "SR0001",
 *       "order_sku_line_items": [
 *         { "route_id": "ROU028912", "order_id": "ORD1123", "sku_code": "SKU1215", "sku_desc": "GROUNDNUT RAW OIL 15 KG TIN",
 *           "rd_code": "VEND001", "vendor_name": "ABC Traders", "quantity": 20, "unloading_qty": 0,
 *           "scanning_status": "BARCODE", "conveyor_mode": "CONVEYOR" }
 *       ] } }
 * </pre>
 */
@Component
public class M10StockInwardStartBayActionHandler implements ActionHandler {

    private static final Set<String> VALID_BAY_NAMES =
            IntStream.rangeClosed(1, 12).mapToObj(n -> "Bay" + n).collect(Collectors.toUnmodifiableSet());

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.stockinward-start-bay"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        String bayNameInput = strReq(formData, "bay_name");
        if (bayNameInput == null) {
            return ActionResult.error("formData.bay_name is required");
        }
        if (!VALID_BAY_NAMES.contains(bayNameInput)) {
            return ActionResult.error("formData.bay_name must be one of Bay1..Bay12, got: " + bayNameInput);
        }
        String bayName = bayNameInput;

        Object linesObj = formData.get("order_sku_line_items");
        if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) {
            return ActionResult.error("formData.order_sku_line_items must be a non-empty array");
        }
        List<Map<String, Object>> lines = (List<Map<String, Object>>) linesObj;

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();

        List<LineEntry> parsed = new ArrayList<>();
        BigDecimal totalQty = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            Object lineObj = lines.get(i);
            if (!(lineObj instanceof Map)) {
                return ActionResult.error("order_sku_line_items[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lineObj;
            String routeId = strReq(line, "route_id");
            String skuCode = strReq(line, "sku_code");
            if (routeId == null || skuCode == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] is missing route_id or sku_code");
            }
            BigDecimal qty;
            try {
                Object qtyObj = line.get("quantity");
                qty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") quantity must be a number");
            }
            if (qty == null || qty.signum() < 0) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs quantity >= 0");
            }

            BigDecimal unloadingQty;
            try {
                Object unloadingQtyObj = line.get("unloading_qty");
                unloadingQty = unloadingQtyObj == null ? null : new BigDecimal(unloadingQtyObj.toString());
            } catch (NumberFormatException e) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") unloading_qty must be a number");
            }
            if (unloadingQty != null && unloadingQty.signum() < 0) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs unloading_qty >= 0");
            }

            String scanningStatus = strReq(line, "scanning_status");
            if (scanningStatus == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") is missing scanning_status");
            }
            String conveyorMode = strReq(line, "conveyor_mode");
            if (conveyorMode == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") is missing conveyor_mode");
            }

            parsed.add(new LineEntry(routeId, strOpt(line, "order_id"), skuCode, strOpt(line, "sku_desc"), qty, unloadingQty,
                    scanningStatus, conveyorMode, strOpt(line, "rd_code"), strOpt(line, "vendor_name")));
            totalQty = totalQty.add(qty);
        }

        Integer openForThisInstance = jdbc.queryForObject(
                "SELECT COUNT(*) FROM unloading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'IN_PROGRESS'",
                Integer.class, tenantId, instanceId);
        if (openForThisInstance != null && openForThisInstance > 0) {
            return ActionResult.error("this vehicle already has an open unload bay — close it with Bay Complete before starting another");
        }

        String vehicleNo = strOpt(formData, "vehicle_no");
        String gatePassNo = strOpt(formData, "gpNumber");
        String godownCode = strOpt(formData, "godown_code");
        String plantCode = strOpt(formData, "plant_code");

        Map<String, Object> instanceRow = jdbc.queryForMap(
                "SELECT workflow_code, current_state FROM workflow_instance WHERE id = ? AND tenant_id = ?",
                instanceId, tenantId);
        String flowVariantCode = (String) instanceRow.get("workflow_code");
        String currentStep = (String) instanceRow.get("current_state");

        jdbc.update(
                "INSERT INTO unloading_progress_detail (tenant_id, workflow_instance_id, flow_variant_code, current_step, vehicle_no, gate_pass_no, total_quantity, processed_quantity) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, 0) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "flow_variant_code = COALESCE(unloading_progress_detail.flow_variant_code, VALUES(flow_variant_code)), "
                        + "current_step = VALUES(current_step), "
                        + "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), "
                        + "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no), "
                        + "total_quantity = total_quantity + VALUES(total_quantity)",
                tenantId, instanceId, flowVariantCode, currentStep, vehicleNo, gatePassNo, totalQty);

        Long parentId = jdbc.queryForObject(
                "SELECT id FROM unloading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                Long.class, tenantId, instanceId);

        int dispatchTasksSkipped = 0;
        for (LineEntry e : parsed) {
            jdbc.update(
                    "INSERT INTO unloading_progress_detail_bay "
                            + "(tenant_id, unloading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, processed_quantity, godown_code, plant_code, rd_code, vendor_name, status) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                    tenantId, parentId, instanceId, e.routeId, e.orderId, e.skuCode, e.skuDesc, bayName, e.quantity, e.unloadingQty,
                    godownCode, plantCode, e.rdCode, e.vendorName);

            RowActorStamp.startedUnloading(jdbc, tenantId, instanceId, e.routeId, e.orderId, e.skuCode, bayName, RowActorStamp.performedBy(null, formData));

            if ("WITHOUT_CONVEYOR".equals(e.conveyorMode) || "WITHOUT_BARCODE".equals(e.scanningStatus)) {
                dispatchTasksSkipped++;
                continue;
            }

            String taskCode = "DT-" + instanceId + "-" + bayName + "-" + e.skuCode + "-" + System.currentTimeMillis();
            jdbc.update(
                    "INSERT INTO dispatch_task "
                            + "(tenant_id, workflow_code, type, workflow_instance_id, bay_code, route_id, order_id, task_code, "
                            + "order_ref, sku_code, gp_number, target_qty, processed_qty, status) "
                            + "VALUES (?,?,'UnLoading',?,?,?,?,?,?,?,?,?, 0, 'Open')",
                    tenantId, M10StartBayActionHandler.CONVEYOR_WORKFLOW_CODE, instanceId, bayName, e.routeId, e.orderId, taskCode,
                    e.orderId, e.skuCode, gatePassNo, e.quantity);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("bayName", bayName);
        data.put("lineCount", parsed.size());
        data.put("totalQuantity", totalQty);
        data.put("dispatchTasksSkipped", dispatchTasksSkipped);

        return ActionResult.builder()
                .ok(true)
                .message("unload bay '" + bayName + "' started with " + parsed.size() + " SKU line(s), total qty " + totalQty)
                .data(data)
                .build();
    }

    private static String strReq(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        if (v == null || v.toString().isBlank()) return null;
        return v.toString();
    }

    private static String strOpt(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        return v == null ? null : v.toString();
    }

    private record LineEntry(String routeId, String orderId, String skuCode, String skuDesc, BigDecimal quantity, BigDecimal unloadingQty,
                              String scanningStatus, String conveyorMode, String rdCode, String vendorName) {}
}
