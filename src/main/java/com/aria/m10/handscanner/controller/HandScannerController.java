package com.aria.m10.handscanner.controller;

import com.aria.m10.handscanner.service.HandScannerService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * {@code POST /api/handscanner/scan-gate-pass} — the single call a Pi-based
 * hand scanner makes after reading a Gate Pass QR (see
 * {@code raspberry-pi-client/hand_scanner.py}): send the raw gp_number, and
 * the backend resolves gate pass -> workflow instance -> order/SKU dispatch
 * details -> (if ready) bay assignment, in one round trip. Same tokenless-
 * device convention as {@code /api/parking/**}/{@code /api/conveyor/**} —
 * see WebSecurityConfig.
 */
@RestController
@RequestMapping("/api/handscanner")
public class HandScannerController {

    private final HandScannerService service;

    public HandScannerController(HandScannerService service) {
        this.service = service;
    }

    @PostMapping("/scan-gate-pass")
    @Operation(summary = "Resolve a scanned Gate Pass QR into gate pass, workflow instance, order/SKU dispatch "
            + "details, and (when the workflow is at LOADING_IN_PROGRESS) an attempted bay assignment.")
    public Map<String, Object> scanGatePass(@RequestBody Map<String, String> body) {
        String gpNumber = body == null ? null : body.get("gpNumber");
        if (gpNumber == null || gpNumber.isBlank()) {
            return Map.of("error", "gpNumber is required");
        }
        return service.scanGatePass(gpNumber);
    }
}
