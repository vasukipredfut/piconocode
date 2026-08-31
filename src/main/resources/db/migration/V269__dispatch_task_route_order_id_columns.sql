-- Conveyor Dispatch Task: add route_id + order_id so a task created from the
-- M10 "Start Bay" action (m10.start-bay) can carry the same route/order
-- identifiers as its source loading_progress_detail_bay row, independent of
-- the existing order_ref column (kept as-is -- still the generic field fed
-- into Rule Builder context as taskOrderRef, see CONVEYOR_SCANNER_WORKFLOW_GUIDE.md
-- section 5). Naming matches loading_progress_detail_bay.route_id/order_id
-- (V252) used elsewhere in M10.
ALTER TABLE dispatch_task
  ADD COLUMN IF NOT EXISTS route_id VARCHAR(128) DEFAULT NULL AFTER bay_code,
  ADD COLUMN IF NOT EXISTS order_id VARCHAR(128) DEFAULT NULL AFTER route_id;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'DispatchTask', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, c.req, c.wk
FROM (
  SELECT 'route_id' fc, 'Route ID' lbl, 'STRING' dt, 1 filt, 1 sort, 1 srch, 0 req, 'text' wk
  UNION ALL SELECT 'order_id', 'Order ID', 'STRING', 1, 1, 1, 0, 'text'
) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'DispatchTask' AND field_code = c.fc);

UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = '{"kind":"list","dataSource":{"type":"entity","entity":"DispatchTask"},"columns":[{"field":"workflow_code","label":"Workflow Code","sortable":true},{"field":"bay_code","label":"Bay Code","sortable":true},{"field":"route_id","label":"Route ID","sortable":true},{"field":"order_id","label":"Order ID","sortable":true},{"field":"task_code","label":"Task Code","sortable":true},{"field":"order_ref","label":"Order Ref","sortable":true},{"field":"sku_code","label":"SKU","sortable":true},{"field":"vehicle_no","label":"Vehicle No","sortable":true},{"field":"gp_number","label":"Gate Pass No","sortable":true},{"field":"target_qty","label":"Target Qty","sortable":true},{"field":"processed_qty","label":"Processed Qty","sortable":true},{"field":"rejected_qty","label":"Rejected Qty","sortable":true},{"field":"status","label":"Status","sortable":true}],"pageSize":25}'
WHERE pd.tenant_id = 1 AND pd.code = 'aria:conveyor-dispatch-tasks' AND pv.id = pd.current_version_id;
