-- Dynamic Conveyor Workflow & Integration — Step 1: bind Integration IDs
-- (barcode scanner / PLC / MQTT broker / etc.) to workflow steps, so a
-- Raspberry Pi can fetch "which Integration ID handles which role at this
-- step" purely from data (GET /api/workflows/{workflowId}), with zero
-- hardcoded line-specific logic in the Python client.
--
-- Mirrors page_workflow_binding (page_code/workflow_code/step_code/role) —
-- same shape, integration_id instead of page_code.
--
-- bay_code = '' means "applies to every bay of this workflow"; a specific
-- bay_code overrides it. Resolution: exact bay_code match first, else ''.

CREATE TABLE IF NOT EXISTS workflow_integration_binding (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  workflow_code VARCHAR(128) NOT NULL,
  step_code VARCHAR(128) NOT NULL,
  bay_code VARCHAR(64) NOT NULL DEFAULT '',
  integration_role VARCHAR(32) NOT NULL,
  integration_id BIGINT NOT NULL,
  config_overrides_json TEXT DEFAULT NULL CHECK (json_valid(config_overrides_json)),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_wib (tenant_id, workflow_code, step_code, integration_role, bay_code),
  KEY idx_wib_workflow (tenant_id, workflow_code),
  CONSTRAINT fk_wib_integration FOREIGN KEY (integration_id) REFERENCES integration_instance (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, created_at, value_column, display_column)
SELECT -1, 'WorkflowIntegrationBinding', 'Workflow Integration Binding', 'workflow_integration_binding',
       'Binds an Integration ID (barcode scanner, PLC, MQTT broker, ...) to a workflow step, optionally scoped to one bay.',
       'M12', NOW(), 'id', 'integration_role'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'WorkflowIntegrationBinding');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'WorkflowIntegrationBinding', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, c.req, c.wk
FROM (
  SELECT 'workflow_code' fc, 'Workflow Code' lbl, 'STRING' dt, 1 filt, 1 sort, 1 srch, 1 req, 'text' wk
  UNION ALL SELECT 'step_code', 'Step Code', 'STRING', 1, 1, 1, 1, 'text'
  UNION ALL SELECT 'bay_code', 'Bay Code', 'STRING', 1, 1, 1, 0, 'text'
  UNION ALL SELECT 'integration_role', 'Integration Role', 'STRING', 1, 1, 1, 1, 'text'
  UNION ALL SELECT 'integration_id', 'Integration', 'LONG', 1, 1, 0, 1, 'number'
) c
WHERE NOT EXISTS (
  SELECT 1 FROM entity_field_registry WHERE entity_code = 'WorkflowIntegrationBinding' AND field_code = c.fc
);
