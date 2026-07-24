package com.aria.conveyor.controller;

import com.aria.conveyor.dto.ScanRequest;
import com.aria.conveyor.service.ConveyorScanService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * {@code POST /api/conveyor/scan} — the HTTP transport for the barcode-
 * scanned-validate-and-respond step; delegates to {@link ConveyorScanService},
 * which is also used by the MQTT inbound path (a device may call this
 * endpoint directly, or publish a {@code barcode-scanned} MQTT event —
 * either way the same validation/quantity/PLC-signal logic runs).
 */
@RestController
public class ConveyorScanController {

    private final ConveyorScanService scanService;

    public ConveyorScanController(ConveyorScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping("/api/conveyor/scan")
    @Operation(summary = "Validate a barcode scan against the Rule Builder; accept (increment dispatch task, "
            + "signal PLC accept) or reject (signal PLC reject, log reason).")
    public Map<String, Object> scan(@RequestBody ScanRequest req) {
        return scanService.scan(req);
    }
}
