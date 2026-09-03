-- aria:dispatch-vehicle-details (M10.ReturnStockIn): add "Unloading Supervisor"
-- as a saved field. No existing column/alias covers this today — it's a new
-- id-based FK to Employee, following the same convention already used for
-- Loading Supervisor / QC Unloading Supervisor elsewhere in this app (select
-- stores employee id, display name resolved via a join at read time).

-- 1. New column.
ALTER TABLE dispatch_vehicle_detail
  ADD COLUMN IF NOT EXISTS unloading_supervisor_id BIGINT DEFAULT NULL;

-- 2. Register the field so Designer / generic-CRUD tooling knows about it.
--    entity_field_registry has no "widget_config" column -- an entity-ref's
--    JSON config lives in options_json, with relation_entity holding the
--    target entity code (same shape as the existing MaterialRequisition/
--    GatePass "Assignee" RELATION rows in V2__global_catalog.sql).
INSERT INTO entity_field_registry
  (id, tenant_id, entity_code, field_code, display_label, data_type,
   filterable, sortable, searchable, required, relation_entity, widget_kind, options_json)
SELECT (SELECT COALESCE(MAX(id), 0) FROM entity_field_registry x) + 1,
       -1, 'DispatchVehicleDetail', 'unloading_supervisor_id', 'Unloading Supervisor',
       'RELATION', 1, 1, 0, 0, 'Employee', 'entity-ref',
       '{"entity":"Employee","valueField":"id","labelField":"employeename"}'
WHERE NOT EXISTS (
  SELECT 1 FROM entity_field_registry
  WHERE entity_code = 'DispatchVehicleDetail' AND field_code = 'unloading_supervisor_id'
);

-- 3. WORKFLOW PROJECTION: tell WorkflowProjectionListener to actually write
--    this field into the column when M10.ReturnStockIn submits this page.
--    (See section below — this is the piece that makes "Save" actually persist it.)
UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(
  field_map_json,
  JSON_OBJECT('unloading_supervisor_id', JSON_ARRAY('unloadingSupervisorId', 'unloading_supervisor_id'))
)
WHERE target_table = 'dispatch_vehicle_detail'
  AND workflow_code = 'M10.ReturnStockIn'
  AND JSON_EXTRACT(field_map_json, '$.unloading_supervisor_id') IS NULL;

-- 4. Page field: add the select dropdown, right after "Oil Type", before the
--    Save button. REPLACE is guarded (NOT LIKE) so this is safe to re-run.
--    NOTE: this anchor string matches the tenant -1 body as last written by
--    V209__dispatch_vehicle_details_full_reset.sql. If the LIVE tenant-1 copy
--    has drifted since (same risk V209 itself flagged), re-check the actual
--    body_json before running this in that environment.
UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = REPLACE(
  pv.body_json,
  '{"kind":"FormField","type":"text","name":"oilType","label":"Oil Type","visibleWhen":"#form.flowVariantCode == \'M10.ReturnStockIn\'"},{"kind":"WorkflowActionButton","label":"Save Vehicle Details","action":"workflow.transition","instanceId":"{{form.instanceId}}","variant":"primary"}',
  '{"kind":"FormField","type":"text","name":"oilType","label":"Oil Type","visibleWhen":"#form.flowVariantCode == \'M10.ReturnStockIn\'"},{"kind":"FormField","type":"select","name":"unloadingSupervisorId","label":"Unloading Supervisor","visibleWhen":"#form.flowVariantCode == \'M10.ReturnStockIn\'","optionsSource":{"type":"datasource","spec":{"type":"entity","entity":"Employee","select":["id","employeename"],"filter":{"and":[{"field":"employeetype","op":"eq","value":"Supervisor"},{"field":"status","op":"eq","value":"A"}]},"sort":[{"field":"employeename","dir":"asc"}]},"valueField":"id","labelField":"employeename"}},{"kind":"WorkflowActionButton","label":"Save Vehicle Details","action":"workflow.transition","instanceId":"{{form.instanceId}}","variant":"primary"}'
)
WHERE pd.code = 'aria:dispatch-vehicle-details'
  AND pv.status = 'PUBLISHED'
  AND pv.body_json NOT LIKE '%unloadingSupervisorId%';