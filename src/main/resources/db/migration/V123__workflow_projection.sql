-- V123 — generic workflow→row projection config.
--
-- Replaces the hardcoded M10 GatePassWorkflowListener (a bespoke Java listener
-- that mirrored 6 named workflows' state into gate_pass) with ONE generic,
-- config-driven mechanism: a WorkflowProjectionListener reads these rows and,
-- on every transition, upserts the target row for ANY workflow — no per-flow
-- Java. "Which workflow projects into which table, mapping which fields" is now
-- DATA. Table is customer-agnostic; projection rows are seeded via onboarding.

CREATE TABLE IF NOT EXISTS m0_workflow_projection (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL,
    workflow_code       VARCHAR(64)  NOT NULL,
    target_table        VARCHAR(64)  NOT NULL,
    key_column          VARCHAR(64)  NOT NULL DEFAULT 'workflow_instance_id',
    state_columns       VARCHAR(255) NOT NULL DEFAULT 'status',   -- comma list set to the new state
    variant_column      VARCHAR(64)  NULL,                        -- set to workflow_code on create
    number_column       VARCHAR(64)  NULL,                        -- e.g. gp_number
    number_pattern      VARCHAR(128) NULL,                        -- e.g. GP-{tenant}-{instance}
    terminal_states     VARCHAR(255) NULL,                        -- comma list; sets terminal_ts_column
    terminal_ts_column  VARCHAR(64)  NULL,                        -- e.g. closed_at
    field_map_json      TEXT         NULL,                        -- {"col":["bagKey1","bagKey2"]}
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_wf_projection (tenant_id, workflow_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- Manageable through the substrate.
INSERT INTO entity_registry (code, display_name, table_name, category, lifecycle_kind)
  SELECT 'WorkflowProjection', 'Workflow Projection', 'm0_workflow_projection', 'M0', 'DEFINED'
  WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='WorkflowProjection' OR table_name='m0_workflow_projection');
