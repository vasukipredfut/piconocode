-- V84 — drop the 10 gate_pass columns that were moved to qc_preload_detail
-- (V79), loading_allocation_detail (V80) and weighment_capture_detail
-- (V81/V83). They've been NULL-only since those migrations (nothing writes
-- to them anymore); per user decision, historical values in them are not
-- needed, so drop rather than leave as dead columns.
--
-- GenericEntityController's GATE_PASS_SATELLITE_SELECT no longer needs its
-- COALESCE(satellite, gp.col) fallback — gp.col doesn't exist any more, so
-- that Java change ships alongside this migration.

ALTER TABLE gate_pass
  DROP COLUMN IF EXISTS loading_supervisor,
  DROP COLUMN IF EXISTS loading_tank_location,
  DROP COLUMN IF EXISTS qc_reject_reason,
  DROP COLUMN IF EXISTS supervisor_name,
  DROP COLUMN IF EXISTS loading_bay,
  DROP COLUMN IF EXISTS reference_no,
  DROP COLUMN IF EXISTS ordered_qty,
  DROP COLUMN IF EXISTS w1_weight_kg,
  DROP COLUMN IF EXISTS w2_weight_kg,
  DROP COLUMN IF EXISTS w3_weight_kg;

DELETE FROM entity_field_registry
WHERE entity_code='GatePass'
  AND field_code IN ('loading_supervisor','loading_tank_location','qc_reject_reason',
                      'supervisor_name','loading_bay','reference_no','ordered_qty',
                      'w1_weight_kg','w2_weight_kg','w3_weight_kg');
