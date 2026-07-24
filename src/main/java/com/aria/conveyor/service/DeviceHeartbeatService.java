package com.aria.conveyor.service;

import com.aria.conveyor.dto.HeartbeatRequest;
import com.aria.security.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Device heartbeat upsert (mirrors the existing
 * {@code m11_agent_heartbeat} INSERT ... ON DUPLICATE KEY UPDATE idiom) plus
 * a staleness sweep so a device that stops heartbeating gets marked OFFLINE
 * rather than showing its last-known status forever.
 */
@Service
public class DeviceHeartbeatService {

    private static final Logger log = LoggerFactory.getLogger(DeviceHeartbeatService.class);
    private static final int STALE_AFTER_SECONDS = 15; // 3x the 5s expected cadence

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public DeviceHeartbeatService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void upsert(HeartbeatRequest req) {
        long tenantId = TenantContext.get();
        String healthJson;
        try {
            healthJson = req.integrationHealth() == null ? null : mapper.writeValueAsString(req.integrationHealth());
        } catch (Exception e) {
            healthJson = null;
        }
        jdbc.update(
                "INSERT INTO device_heartbeat (tenant_id, workflow_code, device_id, workflow_status, "
                        + "current_dispatch_task_id, device_status, last_scan_at, integration_health_json, received_at) "
                        + "VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP) "
                        + "ON DUPLICATE KEY UPDATE workflow_status = VALUES(workflow_status), "
                        + "current_dispatch_task_id = VALUES(current_dispatch_task_id), "
                        + "device_status = VALUES(device_status), last_scan_at = VALUES(last_scan_at), "
                        + "integration_health_json = VALUES(integration_health_json), received_at = CURRENT_TIMESTAMP",
                tenantId, req.workflowCode(), req.deviceId(), req.workflowStatus(), req.currentDispatchTaskId(),
                req.deviceStatus(), req.lastScanAt() == null ? null : Timestamp.from(req.lastScanAt()), healthJson);
    }

    public List<Map<String, Object>> listForWorkflow(String workflowCode) {
        long tenantId = TenantContext.get();
        return jdbc.queryForList(
                "SELECT * FROM device_heartbeat WHERE tenant_id = ? AND workflow_code = ? ORDER BY device_id",
                tenantId, workflowCode);
    }

    /** Marks devices OFFLINE whose last heartbeat is older than the stale window. Called by the scheduler, per-tenant. */
    public int markStaleOffline() {
        long tenantId = TenantContext.get();
        int updated = jdbc.update(
                "UPDATE device_heartbeat SET device_status = 'OFFLINE' "
                        + "WHERE tenant_id = ? AND device_status <> 'OFFLINE' "
                        + "AND received_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? SECOND)",
                tenantId, STALE_AFTER_SECONDS);
        if (updated > 0) log.info("DeviceHeartbeatService: marked {} device(s) OFFLINE (stale > {}s)", updated, STALE_AFTER_SECONDS);
        return updated;
    }
}
