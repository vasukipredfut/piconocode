package com.aria.conveyor.dto;

import java.time.Instant;
import java.util.Map;

/** {@code POST /api/conveyor/heartbeat} body — sent by every device roughly every 5s. */
public record HeartbeatRequest(
        String workflowCode,
        String deviceId,
        String workflowStatus,
        Long currentDispatchTaskId,
        String deviceStatus,
        Instant lastScanAt,
        Map<String, Object> integrationHealth) {
}
