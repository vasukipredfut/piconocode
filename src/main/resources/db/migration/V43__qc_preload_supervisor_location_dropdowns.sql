-- V43 — QC Pre-Load Approval: Loading Supervisor + Loading Location were
-- free-text (widget_kind='text', see V28__dispatch_audit_gap_fixes.sql,
-- the last migration to touch this page). Make them dropdowns:
--   - loading_supervisor -> POST /api/m0/datasource/query (the Schema V2 §3
--     generic SQL builder, DataSourceQueryService/DataSourceController) with
--     an entity=Employee spec filtered to employeetype='Supervisor' AND
--     status='A'. employee_details is a big legacy HR table — unfiltered
--     would list every employee, not just the 11 supervisors seeded in
--     V33/V34 — but the filter is declared here in page JSON, not as a new
--     Java branch: DataSourceQueryService already compiles any entity+filter
--     generically, no code change needed.
--   - loading_tank_location -> LoadingPoint entity (m10_loading_point master,
--     added in V37), plain optionsSource — no special lookupType needed,
--     same as EwayBill in V31 (entity_registry.value_column/display_column
--     = code/display_name already covers it).
--
-- Full-body rewrite of the page_version, same technique as V28/V38/V40 —
-- everything else (gpNumber picker, onChangeFill, Approve/Reject buttons)
-- is carried over unchanged.

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json='{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "dataSources": [{"id": "q", "kind": "rest", "method": "GET", "url": "/api/workflow/queue/QC"}], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "QC Pre-Load — Queue", "span": 12, "subtitle": "Vehicles awaiting QC pre-load approval. Approve records the loading supervisor + location; Reject needs a reason.", "children": [{"kind": "WorkflowQueueList", "persona": "QC", "dataSource": "q", "pageSize": 25, "cardFields": ["gpNumber", "vehicleNo", "flowVariant", "currentState"]}]}, {"kind": "Section", "title": "Capture & Submit", "span": 12, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (pick one)", "type": "select", "required": true, "optionsSource": {"type": "entity", "entity": "GatePass", "lookupType": "gpNumbers", "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "flow_variant_code": "flow_variant_code", "instanceId": "workflow_instance_id", "vehicle_type": "vehicle_type", "driver_name": "driver_name"}, "scannable": true, "freeSolo": true}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "flow_variant_code", "label": "Flow", "type": "text", "readOnly": true}, {"name": "vehicle_type", "label": "Vehicle Type", "type": "text", "readOnly": true}, {"name": "driver_name", "label": "Driver", "type": "text", "readOnly": true}, {"name": "loading_supervisor", "label": "Loading Supervisor", "type": "select", "visibleWhen": "#form.flow_variant_code == \'M10.TankerOutbound\'", "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "Employee", "select": ["id", "employeename"], "filter": {"and": [{"field": "employeetype", "op": "eq", "value": "Supervisor"}, {"field": "status", "op": "eq", "value": "A"}]}, "sort": [{"field": "employeename", "dir": "asc"}]}, "valueField": "id", "labelField": "employeename"}}, {"name": "loading_tank_location", "label": "Loading Location", "type": "select", "optionsSource": {"type": "entity", "entity": "LoadingPoint", "valueField": "code", "labelField": "display_name"}}, {"name": "qc_reject_reason", "label": "Reject Reason (only if rejecting)", "type": "textarea"}]}, "children": [{"kind": "WorkflowActionButton", "label": "Approve", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "QC_PRELOAD_APPROVED", "color": "primary"}, {"kind": "WorkflowActionButton", "label": "Reject", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "QC_PRELOAD_REJECTED", "color": "error"}]}]}]}]}'
WHERE pd.code='aria:qc-preload-approval';
