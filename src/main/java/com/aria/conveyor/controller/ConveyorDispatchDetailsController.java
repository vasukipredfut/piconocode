package com.aria.conveyor.controller;

import com.aria.conveyor.service.DispatchTaskService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * {@code GET /api/conveyor/dispatchdetails?workflowCode=&bayCode=&skuCode=&status=Open} —
 * tokenless (permit-all, same {@code /api/conveyor/**} convention as {@link
 * ConveyorScanController}/{@link ConveyorSkuMasterController}) live lookup of the ONE Dispatch
 * Task matching all four filters, fetched fresh right before a scan — not from the aggregate
 * {@code GET /api/scanner/workflows/{id}} snapshot cached at startup. Called by the device
 * after {@code GET /api/conveyor/skumaster} resolves a barcode to its SKU
 * ({@code raspberry-pi-client/conveyor_client.py}'s {@code fetch_dispatch_details}), so its
 * local qty pre-check always sees up-to-date {@code target_qty}/{@code processed_qty} — even
 * if another device or process changed the task since the device's last workflow-config fetch.
 */
@RestController
public class ConveyorDispatchDetailsController {

    private final DispatchTaskService tasks;

    public ConveyorDispatchDetailsController(DispatchTaskService tasks) {
        this.tasks = tasks;
    }

    @GetMapping("/api/conveyor/dispatchdetails")
    @Operation(summary = "Fetch the one Dispatch Task matching workflowCode/bayCode/skuCode/status "
            + "(status defaults to Open). found:false when none matches.")
    public Map<String, Object> details(@RequestParam String workflowCode, @RequestParam String bayCode,
                                        @RequestParam String skuCode,
                                        @RequestParam(defaultValue = "Open") String status) {
        return tasks.findOne(workflowCode, bayCode, skuCode, status)
                .<Map<String, Object>>map(row -> Map.of("found", true, "dispatchTask", row))
                .orElseGet(() -> Map.of("found", false));
    }
}
