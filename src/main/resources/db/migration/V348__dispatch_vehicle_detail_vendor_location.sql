-- Adds vendor_location to dispatch_vehicle_detail (satellite table for the
-- M10.FGLoading / M10.PackLoading / M10.TankerOutbound dispatch workflows):
-- 1. New column.
-- 2. entity_field_registry row so it's editable via generic entity CRUD.
-- 3. m0_workflow_projection.field_map_json extended (JSON_SET, not a full
--    rewrite -- avoids clobbering keys added by any later migration this one
--    doesn't know about) so a page submitting vendor_location/vendorLocation
--    during any of these 3 workflows' transitions gets it saved automatically,
--    same convention as the existing vendor_name field.
-- 4. GatePassSatelliteJoin.SELECT (Java) extended so a plain GatePass read
--    (GenericEntityController.getRow/getRowByField, used by the Gate Pass
--    GET API) also surfaces it -- see that class for the join itself.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE dispatch_vehicle_detail ADD COLUMN IF NOT EXISTS vendor_location VARCHAR(255) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'DispatchVehicleDetail', 'vendor_location', 'Vendor Location', 'STRING', 1, 1, 1, 0, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'DispatchVehicleDetail' AND field_code = 'vendor_location');

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.vendor_location', JSON_ARRAY('vendorLocation', 'vendor_location'))
WHERE tenant_id = -1 AND target_table = 'dispatch_vehicle_detail'
  AND workflow_code IN ('M10.FGLoading', 'M10.PackLoading', 'M10.TankerOutbound')
  AND (field_map_json IS NULL OR JSON_EXTRACT(field_map_json, '$.vendor_location') IS NULL);
