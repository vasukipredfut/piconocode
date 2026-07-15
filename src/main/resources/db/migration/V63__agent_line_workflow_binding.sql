-- V63: Bind each agent line to a current packing-run / workflow instance
-- so the agent attributes scans to the right run, enforces SO qty caps,
-- and auto-halts on completion.
--
-- Columns added to m11_agent:
--   * line_workflow_json — per-line "what is currently running on this line"
--     [{line_id, workflow_code, packing_run_id, start_ts, target_pieces}, ...]
-- The agent's config endpoint already returns m11_agent.* — adding this
-- column makes the binding flow through automatically.

ALTER TABLE m11_agent
  ADD COLUMN IF NOT EXISTS line_workflow_json LONGTEXT NULL AFTER devices_json;

-- Seed an example so the agent has something to read on first start
UPDATE m11_agent
SET line_workflow_json = JSON_OBJECT(
  'lines', JSON_ARRAY(
    JSON_OBJECT(
      'line_id','PACK-LINE-1',
      'workflow_code','M5.PackingRun',         -- which workflow this line is executing
      'packing_run_id',1,                       -- m5_packing_run.id currently active
      'sku_code','GIN-1L-IDH',                   -- the SKU the line should ONLY accept
      'expected_offer','STANDARD',
      'target_pieces',1200,                      -- stop when this many ACCEPTED
      'started_at','2026-06-27 09:00:00',
      'shift','S1'
    ),
    JSON_OBJECT(
      'line_id','PACK-LINE-2',
      'workflow_code','M5.PackingRun',
      'packing_run_id',2,
      'sku_code','AGB-IDH-100ST',
      'expected_offer','STANDARD',
      'target_pieces',800,
      'started_at','2026-06-27 09:00:00',
      'shift','S1'
    ),
    JSON_OBJECT(
      'line_id','PACK-LINE-3',
      'workflow_code','M5.PackingRun',
      'packing_run_id',3,
      'sku_code','CNT-1L-PCP',
      'expected_offer','STANDARD',
      'target_pieces',500,
      'started_at','2026-06-27 09:00:00',
      'shift','S1'
    )
  )
), config_version = config_version + 1
WHERE agent_id='pico-line-1-agent';

-- Register the field so it shows in the Agents list page modal
INSERT IGNORE INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('Agent','line_workflow_json','Per-Line Workflow Binding (JSON)','TEXT',0,0,1,0,'textarea',NULL);
