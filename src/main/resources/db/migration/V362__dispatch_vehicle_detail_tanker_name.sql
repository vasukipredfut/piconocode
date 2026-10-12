-- M10.TankerInbound's aria:dispatch-vehicle-details page has a "Tanker Name" field (name="tanker_name",
-- visible for flow_variant_code M10.TankerInbound only) that only ever reached
-- workflow_instance.submitted_data_json. Same treatment as vendor_location (V348):
-- 1. New column on dispatch_vehicle_detail.
-- 2. entity_field_registry row so it is editable via generic entity CRUD.
-- 3. m0_workflow_projection.field_map_json extended (JSON_SET) for M10.TankerInbound's
--    dispatch_vehicle_detail row(s) so the page's tanker_name/tankerName is saved automatically.
-- 4. GatePassSatelliteJoin.SELECT (Java) exposes it on a plain GatePass read.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE dispatch_vehicle_detail ADD COLUMN IF NOT EXISTS tanker_name VARCHAR(255) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'DispatchVehicleDetail', 'tanker_name', 'Tanker Name', 'STRING', 1, 1, 1, 0, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'DispatchVehicleDetail' AND field_code = 'tanker_name');

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.tanker_name', JSON_ARRAY('tankerName', 'tanker_name'))
WHERE target_table = 'dispatch_vehicle_detail'
  AND workflow_code = 'M10.TankerInbound'
  AND (field_map_json IS NULL OR JSON_EXTRACT(field_map_json, '$.tanker_name') IS NULL);
