-- Per direct request: whenever a workflow instance lands on a new assigned
-- role (e.g. "Dispatch"), save a notification row carrying tenant_id,
-- workflow_instance_id, gate_pass_id, vehicle_no, and the full request JSON
-- (the accumulated submitted-data bag at that point), so a role's pending
-- work generates an actual saved notification instead of only being
-- derivable by polling the queue.
CREATE TABLE IF NOT EXISTS m0_notification (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  gate_pass_id BIGINT DEFAULT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  assigned_role VARCHAR(64) DEFAULT NULL,
  payload_json JSON DEFAULT NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notif_role_unread (tenant_id, assigned_role, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
