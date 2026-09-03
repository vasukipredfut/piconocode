-- vehicle_no already saves to gate_pass (V259) and gate_pass.current_step
-- already auto-updates on every transition via state_columns -- but
-- dispatch_vehicle_detail.vehicle_no (column already exists, used by every
-- other M10 workflow) was never mapped for M10.OtherVehicles, so it stayed
-- NULL there even though gate_pass had the value all along.

UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(field_map_json, JSON_OBJECT(
  'vehicle_no', JSON_ARRAY('vehicle_no', 'vehicleNo')
))
WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'dispatch_vehicle_detail'
  AND field_map_json IS NOT NULL AND JSON_VALID(field_map_json);
