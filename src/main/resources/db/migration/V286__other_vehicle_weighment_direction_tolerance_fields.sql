-- aria:weighment-capture-other-vehicle's Weighbridge Reading section
-- (movement_direction, expected_weight_kg, loaded_unloaded_weight_kg,
-- weight_tally_variance_kg -- the second-weighment tolerance check) has
-- never been mapped anywhere: V267's field_map_json for
-- weighment_capture_detail only ever covered weight_officer/w1_weight_kg/
-- w2_weight_kg/weight_entry_at/photos/notes/skip_weighment/time_taken, so
-- these 4 fields survived only in workflow_instance.submitted_data_json.
-- All 4 are plain scalars (no repeater fan-out needed), so the generic
-- WorkflowProjectionListener handles them once mapped -- no Java change.

ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS movement_direction VARCHAR(16) DEFAULT NULL COMMENT 'LOADING/UNLOADING -- Other Vehicles flow only',
  ADD COLUMN IF NOT EXISTS expected_weight_kg DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS loaded_unloaded_weight_kg DECIMAL(12,3) DEFAULT NULL COMMENT 'abs(w1_weight_kg - w2_weight_kg), client-computed',
  ADD COLUMN IF NOT EXISTS weight_tally_variance_kg DECIMAL(12,3) DEFAULT NULL COMMENT 'loaded_unloaded_weight_kg - expected_weight_kg';

UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(field_map_json, JSON_OBJECT(
  'movement_direction', JSON_ARRAY('movement_direction', 'movementDirection'),
  'expected_weight_kg', JSON_ARRAY('expected_weight_kg', 'expectedWeightKg'),
  'loaded_unloaded_weight_kg', JSON_ARRAY('loaded_unloaded_weight_kg', 'loadedUnloadedWeightKg'),
  'weight_tally_variance_kg', JSON_ARRAY('weight_tally_variance_kg', 'weightTallyVarianceKg')
))
WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'weighment_capture_detail'
  AND field_map_json IS NOT NULL AND JSON_VALID(field_map_json);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'WeighmentCaptureDetail' b,'movement_direction' c,'Movement Direction' d,'STRING' e,1 f,1 g,0 h,0 i,'text' j UNION ALL
  SELECT -1,'WeighmentCaptureDetail','expected_weight_kg','Expected Weight (kg)','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'WeighmentCaptureDetail','loaded_unloaded_weight_kg','Loaded/Unloaded Weight (kg)','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'WeighmentCaptureDetail','weight_tally_variance_kg','Weight Tally Variance (kg)','DECIMAL',0,1,0,0,'number'
) x
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'WeighmentCaptureDetail')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = x.b AND field_code = x.c);
