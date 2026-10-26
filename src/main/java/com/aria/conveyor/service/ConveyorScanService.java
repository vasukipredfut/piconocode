package com.aria.conveyor.service;

import com.aria.conveyor.dto.RuleEvalResult;
import com.aria.conveyor.dto.ScanRequest;
import com.aria.integration.adapter.OutboundResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The barcode-scanned-validate-and-respond orchestration, shared by both
 * {@code POST /api/conveyor/scan} (HTTP, when the device calls the backend
 * directly) and the MQTT inbound path (when a device instead publishes a
 * {@code barcode-scanned} event) — one implementation, two transports.
 */
@Service
public class ConveyorScanService {

    private final DispatchTaskService tasks;
    private final RuleEvaluationService rules;
    private final WorkflowIntegrationBindingService bindings;
    private final PlcSignalService plc;
    private final SkuMasterLookupService skuMasterLookup;

    public ConveyorScanService(DispatchTaskService tasks, RuleEvaluationService rules,
                                WorkflowIntegrationBindingService bindings, PlcSignalService plc,
                                SkuMasterLookupService skuMasterLookup) {
        this.tasks = tasks;
        this.rules = rules;
        this.bindings = bindings;
        this.plc = plc;
        this.skuMasterLookup = skuMasterLookup;
    }

    public Map<String, Object> scan(ScanRequest req) {
        if (req.workflowCode() == null || req.bayCode() == null || req.barcode() == null) {
            return Map.of("outcome", "ERROR", "reason", "workflowCode, bayCode, and barcode are required");
        }

        // Resolve the authoritative SKU from m5_sku_master BEFORE any task
        // resolution or rule evaluation — the device's own req.sku() is
        // never trusted (same principle as rules only ever running on the
        // backend, never the device): a scanned barcode that isn't a known,
        // active SKU is rejected right here, before a Dispatch Task is even
        // looked up.
        Map<String, Object> skuMaster = skuMasterLookup.resolveByBarcode(req.barcode()).orElse(null);
        if (skuMaster == null) {
            tasks.recordRejectedScanLogOnly(runningTaskId(req), req.barcode(), "UNKNOWN_BARCODE", req.scannerIntegrationId());
            return response("REJECT", "UNKNOWN_BARCODE", null, null);
        }
        if (!"ACTIVE".equalsIgnoreCase(String.valueOf(skuMaster.get("status")))) {
            tasks.recordRejectedScanLogOnly(runningTaskId(req), req.barcode(), "SKU_INACTIVE", req.scannerIntegrationId());
            return response("REJECT", "SKU_INACTIVE", null, null);
        }
        String resolvedSku = (String) skuMaster.get("sku_code");

        Map<String, Object> task = resolveTask(req, resolvedSku).orElse(null);
        if (task == null) {
            // No open task. If the bay's task for this SKU has just been filled, this carton is over the order quantity:
            // it counts against THAT task (rejected_qty) as well as in the scan log. No task at all stays a log-only reject.
            Map<String, Object> filled = tasks.findLatestCompleted(req.workflowCode(), req.bayCode(), resolvedSku).orElse(null);
            if (filled != null) return rejectOverOrderQty(req, filled);
            tasks.recordRejectedScanLogOnly(runningTaskId(req), req.barcode(), "NO_ACTIVE_TASK", req.scannerIntegrationId());
            return response("REJECT", "NO_ACTIVE_TASK", null, null);
        }
        if ("Completed".equalsIgnoreCase(String.valueOf(task.get("status")))) {
            return rejectOverOrderQty(req, task);   // a finished task named by taskId
        }
        Long taskId = ((Number) task.get("id")).longValue();

        Map<String, Object> context = buildContext(req, task, resolvedSku);
        Long ruleSetId = task.get("rule_set_id") != null ? ((Number) task.get("rule_set_id")).longValue() : null;
        Object workflowInstanceIdRaw = task.get("workflow_instance_id");
        Long workflowInstanceId = workflowInstanceIdRaw != null ? ((Number) workflowInstanceIdRaw).longValue() : null;
        RuleEvalResult verdict = rules.loadAndEvaluate(ruleSetId, context, taskId, workflowInstanceId, req.barcode());

        Long plcIntegrationId = bindings.resolve(req.workflowCode(), req.stepCode(), "PLC", req.bayCode()).orElse(null);

        if (!verdict.passed()) {
            tasks.recordRejectedScan(taskId, req.barcode(), verdict.failedReason(), req.scannerIntegrationId());
            OutboundResult signal = plcIntegrationId == null ? null : plc.signalReject(plcIntegrationId);
            return response("REJECT", verdict.failedReason(), task, signal);
        }

        BigDecimal qty = req.qty() == null ? BigDecimal.ONE : req.qty();
        if (DispatchTaskMath.exceedsTarget((BigDecimal) task.get("processed_qty"), qty, (BigDecimal) task.get("target_qty"))) {
            return rejectOverOrderQty(req, task);   // would take the task past its order quantity: nothing is added
        }
        Map<String, Object> updated = tasks.recordAcceptedScan(taskId, req.barcode(), resolvedSku, req.orderRef(),
                req.scannerIntegrationId(), qty).orElse(task);
        OutboundResult signal = plcIntegrationId == null ? null : plc.signalAccept(plcIntegrationId);
        return response("ACCEPT", null, updated, signal);
    }

    /**
     * The task running on this bay, for the scan log only (so the task's screen lists scans that have no task of their own: invalid
     * barcode, inactive SKU): the {@code taskId} the scanner sent, else the bay's only open task. Several open tasks and no taskId = unknown.
     */
    private Long runningTaskId(ScanRequest req) {
        if (req.taskId() != null) return tasks.findById(req.taskId()).map(t -> ((Number) t.get("id")).longValue()).orElse(null);
        if (req.workflowCode() == null || req.bayCode() == null) return null;
        List<Map<String, Object>> active = tasks.listActiveForBay(req.workflowCode(), req.bayCode());
        return active != null && active.size() == 1 ? ((Number) active.get(0).get("id")).longValue() : null;
    }

    /** Over the order quantity: logged with the task, counted in its rejected_qty, PLC told to reject. */
    private Map<String, Object> rejectOverOrderQty(ScanRequest req, Map<String, Object> task) {
        Long taskId = ((Number) task.get("id")).longValue();
        tasks.recordRejectedScan(taskId, req.barcode(), "ORDER_QTY_EXCEEDED", req.scannerIntegrationId());
        Long plcIntegrationId = bindings.resolve(req.workflowCode(), req.stepCode(), "PLC", req.bayCode()).orElse(null);
        OutboundResult signal = plcIntegrationId == null ? null : plc.signalReject(plcIntegrationId);
        return response("REJECT", "ORDER_QTY_EXCEEDED", task, signal);
    }

    /** Explicit taskId wins; else the bay's one active task; else, among several, the one matching sku/orderRef. */
    private Optional<Map<String, Object>> resolveTask(ScanRequest req, String resolvedSku) {
        if (req.taskId() != null) return tasks.findById(req.taskId());
        List<Map<String, Object>> active = tasks.listActiveForBay(req.workflowCode(), req.bayCode());
        if (active.isEmpty()) return Optional.empty();
        if (active.size() == 1) return Optional.of(active.get(0));
        for (Map<String, Object> t : active) {
            boolean skuMatch = resolvedSku.equals(t.get("sku_code"));
            boolean orderMatch = req.orderRef() != null && req.orderRef().equals(t.get("order_ref"));
            if (skuMatch || orderMatch) return Optional.of(t);
        }
        return Optional.empty(); // ambiguous — caller must disambiguate with sku/orderRef/taskId
    }

    private Map<String, Object> buildContext(ScanRequest req, Map<String, Object> task, String resolvedSku) {
        BigDecimal target = (BigDecimal) task.get("target_qty");
        BigDecimal processed = (BigDecimal) task.get("processed_qty");
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("barcode", req.barcode());
        ctx.put("scannedSku", resolvedSku);
        ctx.put("scannedOrderRef", req.orderRef());
        ctx.put("scannedQty", req.qty() == null ? BigDecimal.ONE : req.qty());
        ctx.put("taskSkuCode", task.get("sku_code"));
        ctx.put("taskOrderRef", task.get("order_ref"));
        ctx.put("taskTargetQty", target);
        ctx.put("taskProcessedQty", processed);
        ctx.put("taskRemainingQty", target != null && processed != null ? target.subtract(processed) : null);
        ctx.put("taskStatus", task.get("status"));
        return ctx;
    }

    private Map<String, Object> response(String outcome, String reason, Map<String, Object> task, OutboundResult plcSignal) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("outcome", outcome);
        out.put("reason", reason);
        out.put("dispatchTask", task);
        out.put("plcSignalOk", plcSignal == null ? null : plcSignal.isOk());
        return out;
    }
}
