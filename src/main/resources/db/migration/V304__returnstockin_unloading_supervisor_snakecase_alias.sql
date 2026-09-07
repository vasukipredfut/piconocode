-- A real captured payload from aria:return-stock-officer's submission shows
-- the frontend's shared form-state also carries the field as
-- "unloading_supervisor" (snake_case, no "_id") -- a fourth spelling never
-- covered by V285/V296's alias list (unloadingSupervisor/
-- unloadingSupervisorId/unloading_supervisor_id). Whichever page actually
-- submits this exact key for M10.ReturnStockIn's dispatch_vehicle_detail
-- projection would have silently dropped it until now. Purely additive.
UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(
  field_map_json,
  JSON_OBJECT('unloading_supervisor_id', JSON_ARRAY('unloadingSupervisor', 'unloadingSupervisorId', 'unloading_supervisor_id', 'unloading_supervisor'))
)
WHERE target_table = 'dispatch_vehicle_detail' AND workflow_code = 'M10.ReturnStockIn'
  AND JSON_CONTAINS_PATH(field_map_json, 'one', '$.unloading_supervisor_id');
