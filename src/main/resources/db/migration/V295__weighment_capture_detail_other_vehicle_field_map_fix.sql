-- V267's "Projection row 1" INSERT for (M10.OtherVehicles,
-- weighment_capture_detail) was meant to REPLACE V259's original 4-field
-- field_map_json ({w1_weight_kg, w2_weight_kg, weight_officer,
-- weight_entry_at}) with a fuller one adding wb_display_photo,
-- vehicle_photo, notes, skip_weighment, time_taken -- but it's a plain
-- INSERT guarded only by "WHERE NOT EXISTS (... same tenant_id/workflow_code
-- /target_table ...)". V259 always runs first and already satisfies that
-- guard, so V267's INSERT has been a silent no-op on every database since
-- it shipped: the row stayed on V259's original 4 keys. Confirmed live --
-- a real M10.OtherVehicles submission saved w1_weight_kg/weight_officer/
-- movement_direction/expected_weight_kg (the last two added later by V286's
-- JSON_MERGE_PATCH, which reapplies regardless of row content) but silently
-- dropped notes and skip_weighment, both entered by the weighment officer.
--
-- Fix the same way V286/V288 correctly did it: an UPDATE ... JSON_MERGE_PATCH
-- that reapplies regardless of whatever the row's current content is,
-- instead of an INSERT that only fires once.
--
-- Idempotent -- safe to (re)run on any database, including ones where V267's
-- INSERT happened to succeed already (JSON_MERGE_PATCH just overwrites the
-- same keys with the same values in that case).

UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(field_map_json, JSON_OBJECT(
  'wb_display_photo', JSON_ARRAY('wb_display_photo', 'wbDisplayPhoto'),
  'vehicle_photo', JSON_ARRAY('vehicle_photo', 'vehiclePhoto'),
  'notes', JSON_ARRAY('notes'),
  'skip_weighment', JSON_ARRAY('skip_weighment', 'skipWeighment'),
  'time_taken', JSON_ARRAY('time_taken', 'timeTaken')
))
WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'weighment_capture_detail'
  AND field_map_json IS NOT NULL AND JSON_VALID(field_map_json);
