package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import com.aria.workflow.dto.WorkflowTransitionResult;
import com.aria.workflow.engine.WorkflowDefinitionNotFoundException;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.engine.WorkflowGuardRejectedException;
import com.aria.workflow.engine.WorkflowInstanceNotFoundException;
import com.aria.workflow.engine.WorkflowTransitionConflictException;
import com.aria.workflow.engine.WorkflowTransitionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@code m10.start-bay} — the "Start Bay" button on aria:loading-in-progress-entry
 * (page 428). "Bay" here is a named session (bay_name, e.g. "Bay1") under
 * which one or more SKU line items from this page's Order/SKU grid
 * (formData.order_sku_line_items — backed by RouteSkuSummary/OrderDispatchDetail,
 * see V75/V76/V169) are loaded together — NOT a physical loading dock;
 * {@code m10_loading_point}/LoadingPoint is an unrelated M10 master an
 * earlier version of this handler mistakenly reused, and it is not a
 * 1-SKU-per-bay restriction either — several SKU rows can share one bay.
 * bay_name is restricted to a fixed set of 12 physical bays, "Bay1"
 * through "Bay12" — checked here AND at the DB level (V180's CHECK
 * constraint on loading_progress_detail_bay.bay_name).
 *
 * <p>Opens one {@code loading_progress_detail_bay} row (V179, child of the
 * one-row-per-instance {@code loading_progress_detail}, V94/V177) per line
 * item, rolls their combined quantity into loading_progress_detail.total_quantity,
 * then drives the LOADING_IN_PROGRESS -> BAY_ASSIGNED transition (V176) by
 * delegating to {@link WorkflowEngineService}. This handler never writes
 * workflow_instance itself — the engine stays the sole state mutator
 * (ArchUnit Rule G).
 *
 * <p>Also opens one conveyor {@code dispatch_task} row (V102/V269) per line
 * item alongside its {@code loading_progress_detail_bay} row — this is what
 * lets the barcode scanner covering this physical bay (see
 * {@code CONVEYOR_SCANNER_WORKFLOW_GUIDE.md}) track/enforce this exact
 * route/order/SKU target as scans come in. Written in the same loop and the
 * same transaction as its {@code loading_progress_detail_bay} counterpart —
 * NOT gated on whether the FSM actually transitioned below, since a
 * second/third Start Bay in one multi-bay transaction still needs its own
 * dispatch task even though no new transition fires for it (see the
 * multi-bay-session note above). Stamped with {@link #CONVEYOR_WORKFLOW_CODE}
 * (the conveyor line's own fixed workflow, not this instance's M10 business
 * workflow code — see that constant's javadoc for why), and {@code type =
 * 'Loading'} (V271) — this handler is the loading side only; {@link
 * M10StartUnloadingBayActionHandler} (M10.PackUnload's receiving side) opens
 * its own {@code type = 'UnLoading'} dispatch_task rows the same way, but the
 * Reject Flow Unload sibling ({@link M10StartUnloadBayActionHandler}) still
 * doesn't. {@code rule_set_id} is left {@code NULL} here — attaching a real
 * Rule Builder rule set for enforcement is a separate, not-yet-wired concern.
 *
 * <p>A single transaction can span several bay sessions (Bay 1 with 2 SKUs
 * = 100, Bay 2 with 1 SKU = 200, ...). Only the FIRST bay needs an FSM
 * transition — by the second/third Start Bay the instance is already
 * BAY_ASSIGNED, and there is no BAY_ASSIGNED -> BAY_ASSIGNED edge to match
 * (nor should there be one), so the engine is only called while still in
 * LOADING_IN_PROGRESS; once in BAY_ASSIGNED this handler just opens another
 * bay's rows. Each vehicle processes one bay at a time — Bay Complete
 * (m10.bay-complete) must close every row opened here before another Start
 * Bay is accepted for that same instance.
 *
 * <p>Payload shape — bay_name rides inside formData (the page's current
 * form state) alongside everything else, not in args; this handler takes
 * no args at all. {@code order_id} per line is optional (V252) — some
 * loading steps group SKU lines by route_id, others by order_id; whichever
 * is given here is stored on the opened {@code loading_progress_detail_bay}
 * row and must be echoed back (or omitted, matching what was sent here) at
 * Bay Complete for that row to be found:
 * <pre>
 *   { "instanceId": 1495, "pageCode": "aria:loading-in-progress-entry",
 *     "formData": {
 *       "vehicle_no": "TN86J8070", "route_id": "ROU028912", "gpNumber": "GP-1-1495",
 *       "bay_name": "Bay1",
 *       "order_sku_line_items": [
 *         { "route_id": "ROU028912", "order_id": "ORD1123", "sku_code": "SKU1215", "sku_desc": "GROUNDNUT RAW OIL 15 KG TIN", "order_qty": 45,
 *           "scanning_status": "BARCODE", "conveyor_mode": "CONVEYOR" },
 *         { "route_id": "ROU028912", "order_id": "ORD1124", "sku_code": "SKU1461", "sku_desc": "REF SUNFLOWER OIL 15 LTR TIN ROYAL", "order_qty": 250,
 *           "scanning_status": "WITHOUT_BARCODE", "conveyor_mode": "WITHOUT_CONVEYOR" }
 *       ]
 *     } }
 * </pre>
 *
 * <p>{@code scanning_status} and {@code conveyor_mode} are mandatory per line
 * -- straight off that SKU's {@code m5_sku_master} row (V276/V277), the
 * page's own lookup, not re-validated here (a line missing either is
 * rejected before any row is written). Together they control whether a
 * {@code dispatch_task} is opened below: {@code WITHOUT_CONVEYOR} lines (pure
 * manual entry, nothing will ever scan or count against them) get none, and
 * neither does a {@code WITHOUT_BARCODE} line even when its {@code conveyor_mode}
 * is {@code CONVEYOR_MANUAL_SELECT} (still physically on the belt, but with no
 * barcode for the scanner to read, a scan-tracking task for it would never be
 * fulfilled by an actual scan) -- only a {@code BARCODE} line on a plain
 * {@code CONVEYOR} gets one.
 *
 * <p>M10.TankerOutbound has no physical bay — the operator instead picks a
 * tank from {@code m10_tanker_location} (V217, codes like "ROT-1", "F7",
 * "GNP-2"), sent as {@code formData.loading_tank_location}. When
 * {@code bay_name} is absent, that tank code is used in its place (saved
 * into the same {@code loading_progress_detail_bay.bay_name} column) and the
 * Bay1..Bay12 check is skipped for it — {@code bay_name}, when given, is
 * still restricted to Bay1..Bay12 exactly as before. The DB-level CHECK
 * constraint that used to mirror that restriction was dropped (V236): it
 * can't express "Bay1..Bay12 OR any code in m10_tanker_location" (no
 * subqueries in a MariaDB CHECK), so this validation now lives here alone.
 */
@Component
public class M10StartBayActionHandler implements ActionHandler {

    /**
     * The conveyor-line workflow code stamped onto every {@code dispatch_task}
     * row this handler opens (see class javadoc) -- the physical conveyor
     * scanner/PLC infrastructure's own workflow (a device polls
     * {@code GET /api/scanner/workflows/M12.ConveyorTest?bayCode=...}), which
     * is a fixed, separate thing from whichever M10 business workflow
     * (M10.FGLoading, M10.TankerOutbound, ...) this instanceId belongs to.
     * Not derived from workflow_instance.workflow_code -- a task stamped with
     * the M10 workflow code would never be found by a device querying the
     * M12 one.
     */
    static final String CONVEYOR_WORKFLOW_CODE = "M12.ConveyorTest";

    /** Fixed set of 12 physical bays -- "Bay1".."Bay12" -- mirrored as a DB-level
     *  CHECK constraint on loading_progress_detail_bay.bay_name (V180). */
    private static final Set<String> VALID_BAY_NAMES =
            IntStream.rangeClosed(1, 12).mapToObj(n -> "Bay" + n).collect(Collectors.toUnmodifiableSet());

    @Autowired private JdbcTemplate jdbc;
    @Autowired private WorkflowEngineService engine;

    @Override
    public String code() { return "m10.start-bay"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");
        if (inv.getPageCode() == null) return ActionResult.error("pageCode required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        String bayNameInput = strReq(formData, "bay_name");
        String bayName;
        if (bayNameInput != null) {
            if (!VALID_BAY_NAMES.contains(bayNameInput)) {
                return ActionResult.error("formData.bay_name must be one of Bay1..Bay12, got: " + bayNameInput);
            }
            bayName = bayNameInput;
        } else {
            // No bay_name — M10.TankerOutbound: the operator-picked tank code
            // (loading_tank_location) takes its place, unchecked against
            // Bay1..Bay12 (see class javadoc).
            bayName = strReq(formData, "loading_tank_location");
            if (bayName == null) {
                return ActionResult.error("formData.bay_name or formData.loading_tank_location is required");
            }
        }

        // Optional — the Plant/Godown pickers on this page are UI work still
        // in flight; null here just means Bay Complete falls back to its
        // previous any-Godown FEFO pick for this bay's rows.
        String godownCode = strOpt(formData, "godown_code");
        String plantCode = strOpt(formData, "plant_code");

        Object linesObj = formData.get("order_sku_line_items");
        if (!(linesObj instanceof List) || ((List<?>) linesObj).isEmpty()) {
            return ActionResult.error("formData.order_sku_line_items must be a non-empty array");
        }
        List<Map<String, Object>> lines = (List<Map<String, Object>>) linesObj;

        List<LineEntry> parsed = new ArrayList<>();
        BigDecimal totalQty = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            Map<String, Object> line = lines.get(i);
            String routeId = strReq(line, "route_id");
            String skuCode = strReq(line, "sku_code");
            if (routeId == null || skuCode == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] is missing route_id or sku_code");
            }
            BigDecimal qty;
            try {
                Object qtyObj = line.get("order_qty") != null ? line.get("order_qty") : line.get("quantity");
                qty = qtyObj == null ? null : new BigDecimal(qtyObj.toString());
            } catch (NumberFormatException e) {
                return ActionResult.error("order_sku_line_items[" + i + "].order_qty must be a number");
            }
            if (qty == null || qty.signum() <= 0) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") needs order_qty > 0");
            }
            String scanningStatus = strReq(line, "scanning_status");
            if (scanningStatus == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") is missing scanning_status");
            }
            String conveyorMode = strReq(line, "conveyor_mode");
            if (conveyorMode == null) {
                return ActionResult.error("order_sku_line_items[" + i + "] (" + skuCode + ") is missing conveyor_mode");
            }
            parsed.add(new LineEntry(routeId, strOpt(line, "order_id"), skuCode, strOpt(line, "sku_desc"), qty,
                    scanningStatus, conveyorMode));
            totalQty = totalQty.add(qty);
        }

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();

        Integer openForThisInstance = jdbc.queryForObject(
                "SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'IN_PROGRESS'",
                Integer.class, tenantId, instanceId);
        if (openForThisInstance != null && openForThisInstance > 0) {
            return ActionResult.error("this vehicle already has an open bay — close it with Bay Complete before starting another");
        }

        String currentState = jdbc.queryForObject(
                "SELECT current_state FROM workflow_instance WHERE id = ? AND tenant_id = ?",
                String.class, instanceId, tenantId);

        Long actorUserId = inv.getContext() != null && inv.getContext().get("actorUserId") instanceof Number
                ? ((Number) inv.getContext().get("actorUserId")).longValue()
                : null;

        String newState = currentState;
        boolean transitioned = false;
        // LOADING_IN_PROGRESS_R / BAY_ASSIGNED_R are the RELOAD_LOADING_PLANNING
        // sub-cycle's counterparts to LOADING_IN_PROGRESS / BAY_ASSIGNED (see
        // workflow_definition transitions LOADING_IN_PROGRESS_R->BAY_ASSIGNED_R
        // and BAY_ASSIGNED_R->BAY_ASSIGNED_R) -- a vehicle sent back for a
        // reload reaches Start Bay through those states instead, and this
        // handler must accept them the same way or every reload's first Start
        // Bay click fails with "cannot start a bay from workflow state
        // LOADING_IN_PROGRESS_R" before a single bay row is ever opened.
        if ("LOADING_IN_PROGRESS".equals(currentState) || "LOADING_IN_PROGRESS_R".equals(currentState)) {
            Map<String, Object> engineFormData = new HashMap<>(formData);
            engineFormData.put("bayAction", "start");

            // ActionController has no @ExceptionHandler for these (unlike
            // WorkflowController, which maps them to 404/409/422) -- left
            // uncaught they'd surface as a bare Spring Boot 500. Caught here
            // and turned into the same ActionResult.error(...) + HTTP 200
            // contract every other action returns on failure, but a caught
            // exception does NOT itself roll back a Spring @Transactional
            // method -- the writes below would otherwise silently commit
            // even though the transition failed, so the transaction is
            // explicitly marked rollback-only before returning.
            try {
                WorkflowTransitionResult r = engine.handlePageSubmission(instanceId, inv.getPageCode(), engineFormData, actorUserId);
                newState = r.getNewState();
                transitioned = r.isTransitioned();
            } catch (WorkflowGuardRejectedException | WorkflowTransitionNotFoundException
                    | WorkflowTransitionConflictException | WorkflowInstanceNotFoundException
                    | WorkflowDefinitionNotFoundException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ActionResult.error("bay not started, workflow transition failed: " + e.getMessage());
            }
        } else if (!"BAY_ASSIGNED".equals(currentState) && !"BAY_ASSIGNED_R".equals(currentState)) {
            return ActionResult.error("cannot start a bay from workflow state " + currentState);
        }
        // else: already BAY_ASSIGNED/BAY_ASSIGNED_R (a later bay in the same transaction) — no FSM call needed.

        String vehicleNo = strOpt(formData, "vehicle_no");
        String gatePassNo = strOpt(formData, "gpNumber");

        // loading_progress_detail (V94/V177) is the one-row-per-instance
        // parent -- row should already exist (WorkflowProjectionListener
        // creates it earlier, at WEIGHMENT_1 -> LOADING_IN_PROGRESS), but
        // INSERT .. ON DUPLICATE KEY UPDATE covers Start Bay somehow being
        // the first write for this instance.
        jdbc.update(
                "INSERT INTO loading_progress_detail (tenant_id, workflow_instance_id, vehicle_no, gate_pass_no, total_quantity, processed_quantity) "
                        + "VALUES (?, ?, ?, ?, ?, 0) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "vehicle_no = COALESCE(VALUES(vehicle_no), vehicle_no), "
                        + "gate_pass_no = COALESCE(VALUES(gate_pass_no), gate_pass_no), "
                        + "total_quantity = total_quantity + VALUES(total_quantity)",
                tenantId, instanceId, vehicleNo, gatePassNo, totalQty);

        Long parentId = jdbc.queryForObject(
                "SELECT id FROM loading_progress_detail WHERE tenant_id = ? AND workflow_instance_id = ?",
                Long.class, tenantId, instanceId);

        // scanning_status/conveyor_mode per line are mandatory (validated
        // above) -- straight off the SKU's m5_sku_master row (V276/V277), the
        // page's own lookup. WITHOUT_CONVEYOR items are pure manual-entry,
        // never scanned or counted on the conveyor; WITHOUT_BARCODE items
        // have no barcode for the scanner to read regardless of conveyor_mode
        // (CONVEYOR_MANUAL_SELECT still puts them on the belt, but a
        // scan-tracking task for them would never be fulfilled). Either way,
        // no dispatch_task is opened -- nothing will ever reference it.
        int dispatchTasksSkipped = 0;
        for (int i = 0; i < parsed.size(); i++) {
            LineEntry e = parsed.get(i);
            jdbc.update(
                    "INSERT INTO loading_progress_detail_bay "
                            + "(tenant_id, loading_progress_detail_id, workflow_instance_id, route_id, order_id, sku_code, sku_desc, bay_name, quantity, godown_code, plant_code, status) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?, 'IN_PROGRESS')",
                    tenantId, parentId, instanceId, e.routeId, e.orderId, e.skuCode, e.skuDesc, bayName, e.quantity, godownCode, plantCode);

            if ("WITHOUT_CONVEYOR".equals(e.conveyorMode) || "WITHOUT_BARCODE".equals(e.scanningStatus)) {
                dispatchTasksSkipped++;
                continue;
            }

            // One conveyor dispatch_task per SKU line, so the barcode scanner
            // covering this physical bay (see CONVEYOR_SCANNER_WORKFLOW_GUIDE.md)
            // can track/enforce this exact route/order/SKU target as scans come
            // in. task_code is synthetic and must never repeat for this tenant
            // (UNIQUE(tenant_id, task_code) forever, V102) -- instanceId+bay+sku+
            // submit-time alone is NOT enough: the same SKU can appear on more
            // than one line of the same Start Bay submission (e.g. split across
            // several routes/orders into one bay), and those inserts land close
            // enough together to share a System.currentTimeMillis() value,
            // violating uq_dt_task_code (seen in prod: instance 1906/Bay10/
            // SKU1461 across two routes). The line index is appended so every
            // line in this submission gets a distinct code regardless of timing.
            String taskCode = "DT-" + instanceId + "-" + bayName + "-" + e.skuCode + "-" + System.currentTimeMillis() + "-" + i;
            jdbc.update(
                    "INSERT INTO dispatch_task "
                            + "(tenant_id, workflow_code, type, workflow_instance_id, bay_code, route_id, order_id, task_code, "
                            + "order_ref, sku_code, gp_number, target_qty, processed_qty, status) "
                            + "VALUES (?,?,'Loading',?,?,?,?,?,?,?,?,?, 0, 'Open')",
                    tenantId, CONVEYOR_WORKFLOW_CODE, instanceId, bayName, e.routeId, e.orderId, taskCode,
                    e.orderId, e.skuCode, gatePassNo, e.quantity);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("bayName", bayName);
        data.put("lineCount", parsed.size());
        data.put("totalQuantity", totalQty);
        data.put("dispatchTasksSkipped", dispatchTasksSkipped);
        data.put("fromState", currentState);
        data.put("toState", newState);
        data.put("transitioned", transitioned);

        return ActionResult.builder()
                .ok(true)
                .message("bay '" + bayName + "' started with " + parsed.size() + " SKU line(s), total qty " + totalQty + ", workflow state " + newState)
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

    private record LineEntry(String routeId, String orderId, String skuCode, String skuDesc, BigDecimal quantity,
                              String scanningStatus, String conveyorMode) {}
}
