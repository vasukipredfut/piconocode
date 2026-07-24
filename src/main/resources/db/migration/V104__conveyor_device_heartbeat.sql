-- Dynamic Conveyor Workflow & Integration — Step 5: device heartbeat.
-- Every Raspberry Pi upserts here every 5s: workflow status, current task,
-- device status, last scan time, and a compound integration-health map.
--
-- integration_instance.last_health_at does NOT suffice for this — it is a
-- single scalar timestamp per ONE integration's own ping, whereas a
-- heartbeat is device-level, self-attested, and carries a struct covering
-- MULTIPLE integrations' health at once. integration_event_log remains the
-- correct place for the generic outbound/inbound call audit trail (unchanged,
-- reused as-is) — no separate MQTT event log table is needed.

CREATE TABLE IF NOT EXISTS device_heartbeat (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  workflow_code VARCHAR(128) NOT NULL,
  device_id VARCHAR(64) NOT NULL,
  workflow_status VARCHAR(32) DEFAULT NULL,
  current_dispatch_task_id BIGINT DEFAULT NULL,
  device_status VARCHAR(32) DEFAULT NULL,
  last_scan_at TIMESTAMP NULL DEFAULT NULL,
  integration_health_json TEXT DEFAULT NULL CHECK (json_valid(integration_health_json)),
  received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_dh (tenant_id, workflow_code, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, created_at, value_column, display_column)
SELECT -1, 'DeviceHeartbeat', 'Device Heartbeat', 'device_heartbeat',
       'Latest self-attested heartbeat (workflow status, current task, device status, last scan, integration health) per conveyor device, upserted every ~5s.',
       'M12', NOW(), 'id', 'device_id'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'DeviceHeartbeat');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'DeviceHeartbeat', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, 0, c.wk
FROM (
  SELECT 'workflow_code' fc, 'Workflow Code' lbl, 'STRING' dt, 1 filt, 1 sort, 1 srch, 'text' wk
  UNION ALL SELECT 'device_id', 'Device Id', 'STRING', 1, 1, 1, 'text'
  UNION ALL SELECT 'workflow_status', 'Workflow Status', 'STRING', 1, 1, 0, 'text'
  UNION ALL SELECT 'device_status', 'Device Status', 'STRING', 1, 1, 0, 'text'
  UNION ALL SELECT 'last_scan_at', 'Last Scan At', 'DATETIME', 1, 1, 0, 'datetime'
  UNION ALL SELECT 'received_at', 'Received At', 'DATETIME', 1, 1, 0, 'datetime'
) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'DeviceHeartbeat' AND field_code = c.fc);
