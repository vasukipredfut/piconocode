-- M10.TankerOutbound's aria:dispatch-vehicle-details page has a "Lab Reports" radio (name="lab_reports", YES / NO,
-- visible for flow_variant_code M10.TankerOutbound only) that only ever reached workflow_instance.submitted_data_json.
-- Same treatment as tanker_name (V362):
-- 1. New column on dispatch_vehicle_detail (the page's own table).
-- 2. entity_field_registry row so it is editable via generic entity CRUD.
-- 3. m0_workflow_projection.field_map_json extended (JSON_SET) for M10.TankerOutbound's dispatch_vehicle_detail
--    row(s) so the page's lab_reports/labReports is saved automatically.
-- 4. GatePassSatelliteJoin.SELECT (Java) exposes it on a plain GatePass read (the gate pass API response).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE dispatch_vehicle_detail ADD COLUMN IF NOT EXISTS lab_reports VARCHAR(8) DEFAULT NULL COMMENT 'Lab Reports radio (YES / NO), Tanker Outbound';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'DispatchVehicleDetail', 'lab_reports', 'Lab Reports', 'STRING', 1, 1, 0, 0, 'radio'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'DispatchVehicleDetail' AND field_code = 'lab_reports');

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.lab_reports', JSON_ARRAY('lab_reports', 'labReports'))
WHERE target_table = 'dispatch_vehicle_detail'
  AND workflow_code = 'M10.TankerOutbound'
  AND (field_map_json IS NULL OR JSON_EXTRACT(field_map_json, '$.lab_reports') IS NULL);
