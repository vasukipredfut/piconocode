-- Surface the new dispatch_task.vehicle_no/gp_number columns (V210) on the
-- aria:conveyor-dispatch-tasks admin list page. GenericEntityController/API
-- already picks these up automatically via entity_field_registry; this page's
-- column list is explicit JSON (not auto-derived), so it needs its own update.
UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = '{"kind":"list","dataSource":{"type":"entity","entity":"DispatchTask"},"columns":[{"field":"workflow_code","label":"Workflow Code","sortable":true},{"field":"bay_code","label":"Bay Code","sortable":true},{"field":"task_code","label":"Task Code","sortable":true},{"field":"order_ref","label":"Order Ref","sortable":true},{"field":"sku_code","label":"SKU","sortable":true},{"field":"vehicle_no","label":"Vehicle No","sortable":true},{"field":"gp_number","label":"Gate Pass No","sortable":true},{"field":"target_qty","label":"Target Qty","sortable":true},{"field":"processed_qty","label":"Processed Qty","sortable":true},{"field":"rejected_qty","label":"Rejected Qty","sortable":true},{"field":"status","label":"Status","sortable":true}],"pageSize":25}'
WHERE pd.tenant_id = 1 AND pd.code = 'aria:conveyor-dispatch-tasks' AND pv.id = pd.current_version_id;
