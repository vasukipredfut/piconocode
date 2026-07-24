-- Dynamic Conveyor Workflow & Integration — Step 4: per-workflow conveyor
-- bay config.
--
-- NOT m10_loading_point / entity "LoadingPoint" — that is the M10 truck-dock
-- loading-point master (a global code picked via dropdown on
-- aria:loading-allocation, used by GatePass/weighbridge flows). This table
-- is a distinct, unrelated concept: per-workflow bay config for
-- Pi-driven conveyor packing/dispatch lines. Do not conflate the two.

CREATE TABLE IF NOT EXISTS conveyor_loading_bay (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  workflow_code VARCHAR(128) NOT NULL,
  bay_code VARCHAR(64) NOT NULL,
  display_name VARCHAR(128) DEFAULT NULL,
  line_id VARCHAR(64) DEFAULT NULL,
  default_rule_set_id BIGINT DEFAULT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_clb (tenant_id, workflow_code, bay_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, created_at, value_column, display_column)
SELECT -1, 'ConveyorLoadingBay', 'Conveyor Loading Bay', 'conveyor_loading_bay',
       'Per-workflow conveyor-line bay config (NOT the M10 truck-dock LoadingPoint master).',
       'M12', NOW(), 'id', 'display_name'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'ConveyorLoadingBay');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'ConveyorLoadingBay', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, c.req, c.wk
FROM (
  SELECT 'workflow_code' fc, 'Workflow Code' lbl, 'STRING' dt, 1 filt, 1 sort, 1 srch, 1 req, 'text' wk
  UNION ALL SELECT 'bay_code', 'Bay Code', 'STRING', 1, 1, 1, 1, 'text'
  UNION ALL SELECT 'display_name', 'Display Name', 'STRING', 1, 1, 1, 0, 'text'
  UNION ALL SELECT 'line_id', 'Line Id', 'STRING', 1, 1, 1, 0, 'text'
  UNION ALL SELECT 'default_rule_set_id', 'Default Rule Set', 'LONG', 1, 1, 0, 0, 'number'
  UNION ALL SELECT 'active', 'Active', 'BOOLEAN', 1, 1, 0, 0, 'checkbox'
) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ConveyorLoadingBay' AND field_code = c.fc);
