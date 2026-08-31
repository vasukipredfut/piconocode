-- Conveyor Dispatch Task: add `type` (Loading / UnLoading) so a task records
-- which physical direction it belongs to. Every dispatch_task created so far
-- (via m10.start-bay, see CONVEYOR_SCANNER_WORKFLOW_GUIDE.md section 4.1) is
-- a loading task, hence the default -- an unloading counterpart isn't wired
-- up yet (M10's unload-bay handlers don't create dispatch_task rows at all
-- currently).
ALTER TABLE dispatch_task
  ADD COLUMN IF NOT EXISTS type ENUM('Loading','UnLoading') NOT NULL DEFAULT 'Loading' AFTER workflow_code;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json)
SELECT -1, 'DispatchTask', 'type', 'Type', 'STRING', 1, 1, 0, 1, 'select',
       '[{"label":"Loading","value":"Loading"},{"label":"UnLoading","value":"UnLoading"}]'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'DispatchTask' AND field_code = 'type');

UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = '{"kind":"list","dataSource":{"type":"entity","entity":"DispatchTask"},"columns":[{"field":"workflow_code","label":"Workflow Code","sortable":true},{"field":"type","label":"Type","sortable":true},{"field":"bay_code","label":"Bay Code","sortable":true},{"field":"route_id","label":"Route ID","sortable":true},{"field":"order_id","label":"Order ID","sortable":true},{"field":"task_code","label":"Task Code","sortable":true},{"field":"order_ref","label":"Order Ref","sortable":true},{"field":"sku_code","label":"SKU","sortable":true},{"field":"vehicle_no","label":"Vehicle No","sortable":true},{"field":"gp_number","label":"Gate Pass No","sortable":true},{"field":"target_qty","label":"Target Qty","sortable":true},{"field":"processed_qty","label":"Processed Qty","sortable":true},{"field":"rejected_qty","label":"Rejected Qty","sortable":true},{"field":"status","label":"Status","sortable":true}],"pageSize":25}'
WHERE pd.tenant_id = 1 AND pd.code = 'aria:conveyor-dispatch-tasks' AND pv.id = pd.current_version_id;
