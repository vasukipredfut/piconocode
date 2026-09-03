-- transport_name/driver_name/driver_phone already save to gate_pass (V259)
-- for M10.OtherVehicles -- user also wants them mirrored onto
-- dispatch_vehicle_detail, which already has all 3 columns (used by the
-- other M10 workflows sharing this table), just never mapped for this
-- workflow_code/page. Pure additive field_map_json merge -- no ALTER TABLE
-- needed, no Java change (plain scalars).

UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(field_map_json, JSON_OBJECT(
  'transport_name', JSON_ARRAY('transport_name', 'transportName'),
  'driver_name', JSON_ARRAY('driver_name', 'driverName'),
  'driver_phone', JSON_ARRAY('driver_phone', 'driverPhone')
))
WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'dispatch_vehicle_detail'
  AND field_map_json IS NOT NULL AND JSON_VALID(field_map_json);
