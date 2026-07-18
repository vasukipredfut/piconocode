-- The live (PUBLISHED) version of the Front-Gate Intake page (page_definition.id=1,
-- code='', page_version.id=358) lost its "entity":"GatePass" tag somewhere between
-- the earlier ARCHIVED version (page_version.id=309, slug 'aria:front-gate-create')
-- and the current one. That tag is what /api/m0/designer/pages?entity=GatePass
-- greps for, so the page was invisible to that filter. Restore it via JSON_SET so
-- the json_valid() CHECK constraint on body_json is respected regardless of the
-- surrounding formatting.
UPDATE page_version
SET body_json = JSON_SET(body_json, '$.entity', 'GatePass')
WHERE id = 358
  AND JSON_EXTRACT(body_json, '$.entity') IS NULL;
  
  UPDATE page_version
SET body_json = JSON_SET(body_json, '$.entity', 'ParkingBayEntry')
WHERE id = 2
  AND JSON_EXTRACT(body_json, '$.entity') IS NULL;
  
    UPDATE page_version
SET body_json = JSON_SET(body_json, '$.entity', 'DispatchVehicleDetail')
WHERE id = 743
  AND JSON_EXTRACT(body_json, '$.entity') IS NULL;
  
      UPDATE page_version
SET body_json = JSON_SET(body_json, '$.entity', 'OrderDispatchDetail')
WHERE id = 805
  AND JSON_EXTRACT(body_json, '$.entity') IS NULL;
  
      UPDATE page_version
SET body_json = JSON_SET(body_json, '$.entity', 'OrderDispatchDetail')
WHERE id = 806
  AND JSON_EXTRACT(body_json, '$.entity') IS NULL;
