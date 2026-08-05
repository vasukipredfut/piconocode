-- V214 -- revert V162's approach: it added invoice_qty, w1_weight_kg,
-- w2_weight_kg, qty_difference_kg, grn_status, grn_number directly onto
-- gate_pass so the print summary (M10.TankerInbound/M10.PackUnload) could
-- read them without a join. But the codebase already has an established
-- mechanism for exactly this situation: GenericEntityController's
-- GATE_PASS_SATELLITE_JOIN/SELECT, introduced by V84 and V96 specifically so
-- gate_pass reads never carry duplicate/stale columns once the real data
-- lives on a satellite table. w1/w2 already lived on weighment_capture_detail,
-- invoice_qty on dispatch_vehicle_detail; V162's ADD COLUMN duplicated them
-- back onto gate_pass (its "invoice_qty already exists" comment was wrong —
-- V96 had already dropped it, and nothing re-added it before V162).
--
-- Fix: extend the shared join (now com.aria.entity.GatePassSatelliteJoin,
-- used by both GenericEntityController and PrintController, same change) with
-- unload_reconciliation_detail for grn_status/grn_number and a computed
-- qty_difference_kg, and drop the 6 columns this reintroduced. total_unloading_qty
-- is untouched — it's a pre-existing cross-cutting gate_pass summary column
-- (same pattern as qc_status/variance_reason), not part of this duplication.

ALTER TABLE gate_pass
  DROP COLUMN IF EXISTS invoice_qty,
  DROP COLUMN IF EXISTS w1_weight_kg,
  DROP COLUMN IF EXISTS w2_weight_kg,
  DROP COLUMN IF EXISTS qty_difference_kg,
  DROP COLUMN IF EXISTS grn_status,
  DROP COLUMN IF EXISTS grn_number;

DELETE FROM entity_field_registry
WHERE entity_code='GatePass'
  AND field_code IN ('invoice_qty','w1_weight_kg','w2_weight_kg','qty_difference_kg','grn_status','grn_number');
