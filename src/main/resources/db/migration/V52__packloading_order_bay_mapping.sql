-- V50__packloading_order_bay_mapping.sql
-- Wire loading bay + OrderApp order + SKU-line ordered qty into PackLoading, so
-- the gate pass captures what/where it's loading and PMT compares the real
-- OrderApp qty vs loaded qty (was NULL). Generic (tenant -1); OrderApp rows come
-- from the customer's OrderApp ingest, not this migration.

-- 1. Aggregate view: one row per OrderApp order (for the order picker), built on
--    m5_order_dispatch_detail — the exact 35-column sheet layout that the Masters
--    CSV upload (OrderDispatchDetailImportController) populates. Line grain is
--    (order_id, sku); duplicate SKU rows are summed. Ordered weight =
--    Σ(line cases × SKU carton weight). The SKU-code join needs COLLATE (the two
--    tables were created with different default collations).
CREATE OR REPLACE VIEW v_orderapp_order AS
SELECT MIN(line.min_id) AS id, line.tenant_id, line.order_id AS order_ref,
       MAX(line.rd_desc) AS customer_name, MAX(line.route_id) AS customer_route,
       COUNT(*) AS sku_lines, SUM(line.sku_cases) AS ordered_cases,
       ROUND(SUM(line.sku_cases * COALESCE(s.carton_weight_kg,0)),3) AS ordered_weight_kg,
       MAX(line.dispatch_wgt) AS sheet_dispatch_wt,
       MAX(line.status) AS status, MAX(line.vehicle_no) AS vehicle_no
FROM (
  SELECT MIN(id) AS min_id, tenant_id, order_id, sku_code,
         MAX(rd_desc) AS rd_desc, MAX(route_id) AS route_id,
         SUM(line_order_qty) AS sku_cases,
         MAX(status) AS status, MAX(vehicle_no) AS vehicle_no, MAX(dispatch_wgt) AS dispatch_wgt
  FROM m5_order_dispatch_detail
  GROUP BY tenant_id, order_id, sku_code
) line
LEFT JOIN m5_sku_master s
       ON s.sku_code = line.sku_code COLLATE utf8mb4_unicode_ci
      AND s.tenant_id = line.tenant_id
GROUP BY line.tenant_id, line.order_id;

-- 2. Register OrderAppOrder entity for the picker + View Order.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name)
SELECT -1,'OrderAppOrder','OrderApp Order','v_orderapp_order'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='OrderAppOrder');
DELETE FROM entity_field_registry WHERE entity_code='OrderAppOrder';
INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, widget_kind, required, sortable, filterable, searchable) VALUES
 (-1,'OrderAppOrder','order_ref','Order #','STRING','text',1,1,1,1),
 (-1,'OrderAppOrder','customer_name','Customer','STRING','text',0,1,1,1),
 (-1,'OrderAppOrder','customer_route','Route','STRING','text',0,1,1,1),
 (-1,'OrderAppOrder','sku_lines','SKU Lines','LONG','number',0,1,0,0),
 (-1,'OrderAppOrder','ordered_cases','Ordered Cases','LONG','number',0,1,0,0),
 (-1,'OrderAppOrder','ordered_weight_kg','Ordered Wt (kg)','DECIMAL','number',0,1,0,0),
 (-1,'OrderAppOrder','sheet_dispatch_wt','Sheet Dispatch Wt','DECIMAL','number',0,1,0,0),
 (-1,'OrderAppOrder','vehicle_no','Vehicle (planned)','STRING','text',0,1,1,0),
 (-1,'OrderAppOrder','status','Status','STRING','text',0,1,1,0);

-- 3. Projection: map orderedQty->ordered_qty and orderRef->reference_no onto
--    gate_pass (loading_bay was already mapped). JSON_SET keeps the rest intact.
UPDATE m0_workflow_projection
   SET field_map_json = JSON_SET(field_map_json,
        '$.ordered_qty',  JSON_ARRAY('orderedQty','ordered_qty','orderedWeightKg'),
        '$.reference_no', JSON_ARRAY('orderRef','order_ref','reference_no'))
 WHERE workflow_code='M10.PackLoading' AND target_table='gate_pass';

-- 4. loading-allocation page: add Loading Bay + OrderApp Order pickers (order
--    auto-fills customer/route/ordered qty). Full body replace on the published
--    version of the page.
UPDATE page_version SET body_json = '{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "dataSources": [{"id": "q", "kind": "rest", "method": "GET", "url": "/api/workflow/queue/Dispatch"}], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Loading Supervisor Assign — Queue", "span": 12, "subtitle": "Vehicles approved by QC, awaiting a loading supervisor.", "children": [{"kind": "WorkflowQueueList", "persona": "Dispatch", "dataSource": "q", "pageSize": 25, "cardFields": ["gpNumber", "vehicleNo", "flowVariant", "currentState"]}]}, {"kind": "Section", "title": "Capture & Submit", "span": 12, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (pick one)", "type": "select", "required": true, "optionsSource": {"type": "entity", "entity": "GatePass", "lookupType": "gpNumbers", "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "flow_variant_code": "flow_variant_code", "instanceId": "workflow_instance_id"}, "scannable": true, "freeSolo": true}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "flow_variant_code", "label": "Flow", "type": "text", "readOnly": true}, {"name": "loadingBay", "label": "Loading Bay / Point", "type": "select", "required": true, "placeholder": "Which bay is this vehicle loading at?", "optionsSource": {"type": "entity", "entity": "LoadingPoint", "valueField": "code", "labelField": "display_name"}}, {"name": "orderRef", "label": "OrderApp Order #", "type": "select", "required": true, "placeholder": "Pick the dispatch order this vehicle is loading", "optionsSource": {"type": "entity", "entity": "OrderAppOrder", "valueField": "order_ref", "labelField": "order_ref"}, "onChangeFill": {"customerName": "customer_name", "orderRoute": "customer_route", "orderedQty": "ordered_weight_kg", "orderedCases": "ordered_cases"}, "scannable": false, "freeSolo": false}, {"name": "customerName", "label": "Customer", "type": "text", "disabled": true}, {"name": "orderRoute", "label": "Route", "type": "text", "disabled": true}, {"name": "orderedCases", "label": "Ordered Cases", "type": "text", "disabled": true}, {"name": "orderedQty", "label": "Ordered Wt (kg)", "type": "text", "disabled": true}, {"name": "supervisor_name", "label": "Supervisor Name", "type": "select", "required": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "Employee", "select": ["id", "employeename"], "filter": {"and": [{"field": "employeetype", "op": "eq", "value": "Supervisor"}, {"field": "status", "op": "eq", "value": "A"}]}, "sort": [{"field": "employeename", "dir": "asc"}]}, "valueField": "id", "labelField": "employeename"}}]}, "children": [{"kind": "WorkflowActionButton", "label": "Assign Supervisor", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "LOADING_ALLOCATED", "color": "primary"}]}]}]}]}'
 WHERE id = (SELECT current_version_id FROM page_definition WHERE code='aria:loading-allocation');
