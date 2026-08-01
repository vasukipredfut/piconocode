-- QC Approval page (aria:qc-approval) captures 3 unloading-related fields
-- when flow_variant_code = M10.TankerInbound / M10.ReturnStockIn: unloading
-- supervisor, oil sample recovered, and unloading location. Today only the
-- first two are projected, and they land in qc_preload_detail under a reused
-- QC-loading column (loading_supervisor) that has nothing to do with
-- unloading; unloading_location isn't captured anywhere at all (only
-- survives in workflow_instance.submitted_data_json).
--
-- tanker_unloading_detail already exists but belongs to a DIFFERENT page
-- (the physical supervisor-unloading process, aria:supervisor-unloading) --
-- reusing it here would mix two unrelated pages' data in one table. Instead:
-- a dedicated qc_unloading_approval table, same workflow_instance_id-keyed
-- shape as the other M10 satellite detail tables, holding only what the QC
-- Approval page itself captures. The old qc_preload_detail rows carrying
-- these fields are dropped so each field saves in exactly one table.

CREATE TABLE IF NOT EXISTS qc_unloading_approval (
  id                      BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id               BIGINT       NOT NULL DEFAULT 1,
  workflow_instance_id    BIGINT       NOT NULL,
  flow_variant_code       VARCHAR(64)  DEFAULT NULL,
  current_step            VARCHAR(64)  DEFAULT NULL,
  unloading_supervisor_id VARCHAR(128) DEFAULT NULL COMMENT 'Employee id, picked on QC Approval as "Unloading Supervisor Name"',
  oil_sample_recovered    VARCHAR(8)   DEFAULT NULL COMMENT 'YES/NO',
  unloading_location      VARCHAR(16)  DEFAULT NULL COMMENT 'G1-G4 stock location, captured for RETURN item category',
  created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_qc_unloading_approval_instance (workflow_instance_id, tenant_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, field_map_json, terminal_states)
SELECT -1, 'M10.TankerInbound', 'qc_unloading_approval',
  '{"unloading_supervisor_id": ["unloadingSupervisorName", "unloading_supervisor_name"], "oil_sample_recovered": ["oilSampleRecovered", "oil_sample_recovered"], "unloading_location": ["unloadingLocation", "unloading_location"]}',
  (SELECT terminal_states FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.TankerInbound' AND target_table = 'gate_pass' LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.TankerInbound' AND target_table = 'qc_unloading_approval'
);

INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, field_map_json, terminal_states)
SELECT -1, 'M10.ReturnStockIn', 'qc_unloading_approval',
  '{"unloading_supervisor_id": ["unloadingSupervisorName", "unloading_supervisor_name"], "oil_sample_recovered": ["oilSampleRecovered", "oil_sample_recovered"], "unloading_location": ["unloadingLocation", "unloading_location"]}',
  (SELECT terminal_states FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.ReturnStockIn' AND target_table = 'gate_pass' LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.ReturnStockIn' AND target_table = 'qc_unloading_approval'
);

-- These two rows only ever carried the fields moved above (verified: no
-- qc_reject_reason or other field mixed in for TankerInbound/ReturnStockIn) --
-- safe to drop outright so the fields save in qc_unloading_approval alone.
DELETE FROM m0_workflow_projection
WHERE tenant_id = -1 AND target_table = 'qc_preload_detail'
  AND workflow_code IN ('M10.TankerInbound', 'M10.ReturnStockIn');
