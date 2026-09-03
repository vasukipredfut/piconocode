-- M10.OtherVehicles' weighment_capture_detail field_map_json (V267) routed
-- both w1_captured_at and w2_captured_at page fields into the SAME
-- weight_entry_at column (shared with every other workflow using this
-- table, V93) -- so the second weighment's save silently overwrote the
-- first weighment's timestamp; only the later one ever survived.
--
-- Give M10.OtherVehicles its own two dedicated columns instead, scoped only
-- to this workflow_code's own field_map_json row -- weight_entry_at itself
-- is untouched, so every other workflow still using it is unaffected.

ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS w1_captured_at TIMESTAMP NULL DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS w2_captured_at TIMESTAMP NULL DEFAULT NULL;

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(
  JSON_REMOVE(field_map_json, '$.weight_entry_at'),
  '$.w1_captured_at', JSON_ARRAY('w1_captured_at', 'w1CapturedAt'),
  '$.w2_captured_at', JSON_ARRAY('w2_captured_at', 'w2CapturedAt')
)
WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'weighment_capture_detail'
  AND field_map_json IS NOT NULL AND JSON_VALID(field_map_json)
  AND JSON_CONTAINS_PATH(field_map_json, 'one', '$.weight_entry_at');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'WeighmentCaptureDetail' b,'w1_captured_at' c,'First Weight Time' d,'DATETIME' e,1 f,1 g,0 h,0 i,'datetime' j UNION ALL
  SELECT -1,'WeighmentCaptureDetail','w2_captured_at','Second Weight Time','DATETIME',1,1,0,0,'datetime'
) x
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'WeighmentCaptureDetail')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = x.b AND field_code = x.c);
