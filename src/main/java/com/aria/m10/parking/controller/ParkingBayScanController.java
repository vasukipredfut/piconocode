package com.aria.m10.parking.controller;

import com.aria.m10.parking.dto.ParkingScanRequest;
import com.aria.m10.parking.service.ParkingBayScanService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * {@code POST /api/parking/scan} — the standalone Raspberry Pi ParkingBayScanner
 * device endpoint (no interactive login, permit-all per WebSecurityConfig).
 * Toggles IN/OUT for a gate pass; see {@link ParkingBayScanService}.
 */
@RestController
public class ParkingBayScanController {

    private final ParkingBayScanService scanService;

    public ParkingBayScanController(ParkingBayScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping("/api/parking/scan")
    @Operation(summary = "Scan a gate pass QR at the parking bay; opens an IN entry if none is open for this gate "
            + "pass, or closes the open one with OUT if there is one.")
    public Map<String, Object> scan(@RequestBody ParkingScanRequest req) {
        return scanService.scan(req.gpNumber());
    }
}
