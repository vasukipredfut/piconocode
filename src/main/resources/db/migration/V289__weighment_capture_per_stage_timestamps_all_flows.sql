-- Extends V288's per-weighment capture timestamps from M10.OtherVehicles to
-- ALL 8 workflows sharing weighment_capture_detail. Turns out the OTHER 7
-- workflows' shared page (aria:weighment-capture) never submitted any
-- weight_entry_at/weightEntryAt field at all -- their field_map_json's
-- "weight_entry_at" key (V83/V278) has been dead since day one, always NULL.
--
-- WorkflowProjectionListener now special-cases the "weight_entry_at" key
-- (see that class) to server-stamp CURRENT_TIMESTAMP into w1_/w2_/w3_
-- captured_at based on weight_seq, the same way wb_display_photo/
-- vehicle_photo are already routed -- so every workflow whose field_map_json
-- still carries a "weight_entry_at" key picks this up with NO config change
-- (the 7 generic-page workflows already have that key). Only M10.OtherVehicles
-- needs a config change here, undoing V288's split-alias approach (now
-- superseded by the Java-side server timestamp, which doesn't depend on the
-- client's clock the way the other-vehicle page's old "{{now}}" default did).

ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS w3_captured_at TIMESTAMP NULL DEFAULT NULL;

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(
  JSON_REMOVE(field_map_json, '$.w1_captured_at', '$.w2_captured_at'),
  '$.weight_entry_at', JSON_ARRAY('weight_entry_at', 'weightEntryAt')
)
WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'weighment_capture_detail'
  AND field_map_json IS NOT NULL AND JSON_VALID(field_map_json);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'WeighmentCaptureDetail' b,'w3_captured_at' c,'Third Weight Time' d,'DATETIME' e,1 f,1 g,0 h,0 i,'datetime' j
) x
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'WeighmentCaptureDetail')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = x.b AND field_code = x.c);

-- weight_entry_at itself is now write-only-dead (no field_map_json key ever
-- targets that literal column name any more -- the special case above always
-- writes w1_/w2_/w3_captured_at instead). Left in place rather than dropped:
-- it may still hold historical data from before this change, and dropping a
-- column is a one-way door this migration shouldn't take unilaterally.
