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
 * {@code m10.start-unloading-bay} — the "Start Bay" button on M10.PackUnload's
 * unloading-mark-in-progress step. Sibling of {@link M10StartBayActionHandler}
 * for the receiving direction: an incoming vehicle is being unloaded and its
 * cargo received INTO stock, not reversing a prior load — unlike
 * {@link M10StartUnloadBayActionHandler} (the Reject Flow Unload sibling),
 * there is no prior {@code loading_progress_detail_bay} row required, since
 * nothing was loaded by this system beforehand.
 *
 * <p>Opens one {@code unloading_progress_detail_bay} row (status IN_PROGRESS)
 * per SKU line, matched by (route_id, order_id, sku_code) — same key
 * convention as the loading side. {@code m10.unloading-bay-complete} closes
 * these rows and posts the RECEIPT stock movement.
 *
 * <p>Also opens one conveyor {@code dispatch_task} row (V102/V269/V271) per
 * SKU line alongside its {@code unloading_progress_detail_bay} counterpart —
 * same purpose as {@link M10StartBayActionHandler}'s own dispatch_task
 * writes (lets the barcode scanner covering this physical bay track/enforce
 * this exact route/order/SKU target as scans come in), stamped with the
 * same fixed {@link M10StartBayActionHandler#CONVEYOR_WORKFLOW_CODE} but
 * {@code type = 'UnLoading'} instead of {@code 'Loading'} — this is the
 * receiving direction. {@code rule_set_id} is left {@code NULL}, same as
 * the loading side. Gated by the same {@code scanning_status}/{@code
 * conveyor_mode} pair (V276/V277) as {@link M10StartBayActionHandler} —
 * mandatory per line, straight off that SKU's {@code m5_sku_master} row (the
 * page's own lookup, not re-validated here) — a {@code WITHOUT_CONVEYOR} or
 * {@code WITHOUT_BARCODE} line gets no {@code dispatch_task}, same reasoning
 * as the loading side (see that handler's javadoc).
 *
 * <p>Payload shape mirrors {@link M10StartUnloadBayActionHandler}, with two
 * distinct quantity keys per line: {@code quantity} (required, &gt;= 0) is the
 * planned amount, stored on {@code unloading_progress_detail_bay.quantity} and
 * rolled into {@code unloading_progress_detail.total_quantity}; {@code unloading_qty}
 * (optional, &gt;= 0 if given) is stored directly onto that same row's
 * {@code processed_quantity} column right away, at Start Bay time — it does
 * NOT roll into the parent's {@code processed_quantity} yet (that happens at
 * Bay Complete) and Unload Bay Complete still overwrites the row's
 * {@code processed_quantity} with whatever it's given when the row closes.
 * <pre>
 *   { "instanceId": 1495, "formData": {
 *       "vehicle_no": "TN86J8070", "gpNumber": "GP-1-1495", "bay_name": "Bay1",
 *       "order_sku_line_items": [
 *         { "route_id": "ROU028912", "order_id": "ORD1123", "sku_code": "SKU1215", "sku_desc": "GROUNDNUT RAW OIL 15 KG TIN", "quantity": 20, "unloading_qty": 0,
 *           "scanning_status": "BARCODE", "conveyor_mode": "CONVEYOR" }
 *       ] } }
 * </pre>
 */
@Component
public class M10StartUnloadingBayActionHandler implements ActionHandler {

    private static final Set<String> VALID_BAY_NAMES =
            IntStream.rangeClosed(1, 12).mapToObj(n -> "Bay" + n).collect(Collectors.toUnmodifiableSet());

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.start-unloading-bay"; }

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

            // Distinct from `quantity` above -- optional, stored straight onto
            // processed_quantity at Start Bay time (see class javadoc); Unload
            // Bay Complete still overwrites it with whatever it's given then.
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
                    scanningStatus, conveyorMode));
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
        // Optional — the Plant/Godown pickers on this page are UI work still
        // in flight; null here just means Bay Complete posts the received
        // quantity without them, same as before these columns existed.
        String godownCode = strOpt(formData, "godown_code");
        String plantCode = strOpt(formData, "plant_code");

        // flow_variant_code/current_step aren't projected here automatically
        // (this table has no m0_workflow_projection row -- see V261), so read
        // them straight off workflow_instance and stamp them ourselves.
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

        // scanning_status/conveyor_mode per line are mandatory (validated
        // above) -- same WITHOUT_CONVEYOR/WITHOUT_BARCODE skip rule as
        // M10StartBayActionHandler (see that handler's javadoc): either way,
        // no dispatch_task is opened for that line -- nothing will ever
        // reference it.
        int dispatchTasksSkipped = 0;
        for (LineEntry e : parsed) {
            jdbc.update(
                    "INSERT INTO unloading_progress_detail_bay "
                            + "(tenant_id, unloading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, processed_quantity, godown_code, plant_code, status) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                    tenantId, parentId, instanceId, e.routeId, e.orderId, e.skuCode, e.skuDesc, bayName, e.quantity, e.unloadingQty, godownCode, plantCode);

            RowActorStamp.startedUnloading(jdbc, tenantId, instanceId, e.routeId, e.orderId, e.skuCode, bayName, RowActorStamp.performedBy(null, formData));

            if ("WITHOUT_CONVEYOR".equals(e.conveyorMode) || "WITHOUT_BARCODE".equals(e.scanningStatus)) {
                dispatchTasksSkipped++;
                continue;
            }

            // One conveyor dispatch_task per SKU line, so the barcode scanner
            // covering this physical bay can track/enforce this exact
            // route/order/SKU target as scans come in -- same task_code
            // convention as M10StartBayActionHandler (instanceId+bay+sku+
            // submit-time is unique enough even across a reload cycle).
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
                              String scanningStatus, String conveyorMode) {}
}
