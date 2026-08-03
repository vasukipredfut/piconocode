-- Fixes a silent insert failure: ParkingBayEntryTransitionListener has always
-- written flow_variant_code/loading_type into dispatch_parking_bay_entry, but
-- neither column exists on the table (confirmed against V1 baseline) - every
-- parking IN insert throws and is swallowed by the listener's own try/catch,
-- so parking IN rows have never actually been written via aria:parking-scan.
-- Column types match gate_pass.flow_variant_code/loading_type, the source
-- columns this data is copied from (see ParkingBayEntryTransitionListener).
ALTER TABLE dispatch_parking_bay_entry
  ADD COLUMN IF NOT EXISTS flow_variant_code VARCHAR(64) DEFAULT NULL AFTER vehicle_no,
  ADD COLUMN IF NOT EXISTS loading_type VARCHAR(32) DEFAULT NULL AFTER flow_variant_code;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'ParkingBayEntry', c.fc, c.lbl, 'STRING', 1, 1, 1, 0, 'text'
FROM (
  SELECT 'flow_variant_code' fc, 'Flow Variant' lbl
  UNION ALL SELECT 'loading_type', 'Loading Type'
) c
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'ParkingBayEntry')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ParkingBayEntry' AND field_code = c.fc);
