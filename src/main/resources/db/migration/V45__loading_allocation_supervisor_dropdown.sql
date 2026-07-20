-- V44 — Loading Supervisor Assign (aria:loading-allocation): supervisor_name
-- was free-text (widget_kind='text', see V26__gatepass_picker_scan_select.sql,
-- the last migration to touch this page). Same fix as V43's loading_supervisor
-- on QC Pre-Load Approval: a select sourced via POST /api/m0/datasource/query
-- (entity=Employee, filtered to employeetype='Supervisor' AND status='A') —
-- declared entirely in page JSON, no backend code change.
--
-- Full-body rewrite of the page_version, same technique as V28/V38/V40/V43 —
-- everything else (gpNumber picker, onChangeFill, Assign Supervisor button)
-- is carried over unchanged.

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json='{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "dataSources": [{"id": "q", "kind": "rest", "method": "GET", "url": "/api/workflow/queue/Dispatch"}], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Loading Supervisor Assign — Queue", "span": 12, "subtitle": "Vehicles approved by QC, awaiting a loading supervisor.", "children": [{"kind": "WorkflowQueueList", "persona": "Dispatch", "dataSource": "q", "pageSize": 25, "cardFields": ["gpNumber", "vehicleNo", "flowVariant", "currentState"]}]}, {"kind": "Section", "title": "Capture & Submit", "span": 12, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (pick one)", "type": "select", "required": true, "optionsSource": {"type": "entity", "entity": "GatePass", "lookupType": "gpNumbers", "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "flow_variant_code": "flow_variant_code", "instanceId": "workflow_instance_id"}, "scannable": true, "freeSolo": true}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "flow_variant_code", "label": "Flow", "type": "text", "readOnly": true}, {"name": "supervisor_name", "label": "Supervisor Name", "type": "select", "required": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "Employee", "select": ["id", "employeename"], "filter": {"and": [{"field": "employeetype", "op": "eq", "value": "Supervisor"}, {"field": "status", "op": "eq", "value": "A"}]}, "sort": [{"field": "employeename", "dir": "asc"}]}, "valueField": "id", "labelField": "employeename"}}]}, "children": [{"kind": "WorkflowActionButton", "label": "Assign Supervisor", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "LOADING_ALLOCATED", "color": "primary"}]}]}]}]}'
WHERE pd.code='aria:loading-allocation';
