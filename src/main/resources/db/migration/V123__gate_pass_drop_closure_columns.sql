-- V123 — finish the Gate Pass Closure split (V119/V120): drop the 6 closure-checklist
-- columns from gate_pass now that gate_pass_closure is the sole write target for them
-- (V120 already repointed every M10 gate-pass workflow's field_map_json away from
-- target_table='gate_pass' for these keys, so nothing writes to them anymore).
--
-- gate_pass keeps status/current_step/workflow_instance_id/closed_at (vehicle & lifecycle
-- master data) plus its own unrelated columns; the closure checklist lives only in
-- gate_pass_closure from now on. Historical values in these columns are not carried
-- forward — same "drop rather than leave as dead columns" call as V84.
--
-- Added by: V20 (invoice_count_verified), V47 (seal_verified, weighment_verified,
-- qc_clearance, order_recon_done, within_tolerance).
--
-- GenericEntityController's GATE_PASS_SATELLITE_JOIN/SELECT is updated in the same
-- change to join gate_pass_closure, so GET .../GatePass/{id} keeps surfacing these
-- fields (now read from gate_pass_closure) instead of silently losing them.

ALTER TABLE gate_pass
  DROP COLUMN IF EXISTS seal_verified,
  DROP COLUMN IF EXISTS invoice_count_verified,
  DROP COLUMN IF EXISTS qc_clearance,
  DROP COLUMN IF EXISTS weighment_verified,
  DROP COLUMN IF EXISTS order_recon_done,
  DROP COLUMN IF EXISTS within_tolerance;

DELETE FROM entity_field_registry
WHERE entity_code='GatePass'
  AND field_code IN ('seal_verified','invoice_count_verified','qc_clearance',
                      'weighment_verified','order_recon_done','within_tolerance');
