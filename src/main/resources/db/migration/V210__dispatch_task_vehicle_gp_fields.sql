-- Conveyor Dispatch Task: add vehicle_no + gp_number so a task can record
-- which physical vehicle/gate pass it's loading, independent of task_code
-- (which stays the synthetic per-task identifier documented in
-- CONVEYOR_SCANNER_WORKFLOW_GUIDE.md). Naming matches the existing
-- GatePass.vehicle_no / GatePass.gp_number convention used elsewhere in M10.
ALTER TABLE dispatch_task
  ADD COLUMN IF NOT EXISTS vehicle_no VARCHAR(64) DEFAULT NULL AFTER sku_code,
  ADD COLUMN IF NOT EXISTS gp_number VARCHAR(64) DEFAULT NULL AFTER vehicle_no;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'DispatchTask', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, c.req, c.wk
FROM (
  SELECT 'vehicle_no' fc, 'Vehicle No' lbl, 'STRING' dt, 1 filt, 1 sort, 1 srch, 0 req, 'text' wk
  UNION ALL SELECT 'gp_number', 'Gate Pass No', 'STRING', 1, 1, 1, 0, 'text'
) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'DispatchTask' AND field_code = c.fc);
