package com.aria.conveyor.controller;

import com.aria.conveyor.dto.HeartbeatRequest;
import com.aria.conveyor.service.DeviceHeartbeatService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * {@code POST /api/conveyor/heartbeat} — every device upserts here roughly
 * every 5 seconds (workflow status, current task, device status, last scan
 * time, integration health). {@link com.aria.conveyor.service.DeviceHeartbeatStalenessScheduler}
 * flips a device to OFFLINE if its heartbeat goes stale.
 */
@RestController
public class ConveyorHeartbeatController {

    private final DeviceHeartbeatService heartbeats;

    public ConveyorHeartbeatController(DeviceHeartbeatService heartbeats) {
        this.heartbeats = heartbeats;
    }

    @PostMapping("/api/conveyor/heartbeat")
    @Operation(summary = "Device heartbeat upsert (workflow status, current task, device status, last scan time, integration health).")
    public Map<String, Object> heartbeat(@RequestBody HeartbeatRequest req) {
        if (req.workflowCode() == null || req.deviceId() == null) {
            return Map.of("ok", false, "error", "workflowCode and deviceId are required");
        }
        heartbeats.upsert(req);
        return Map.of("ok", true);
    }
}
