-- V96 — finishes V95: broker_name/invoice_qty/oil_rm_item/tanker_no/
-- transport_name still physically existed as gate_pass columns (V1 baseline)
-- even after V95 stopped writing to them, so a generic GatePass API fetch
-- (gp.*) kept returning them — just permanently stale/NULL going forward,
-- since the real, current data now lives only in dispatch_vehicle_detail.
-- Drop them (same two-step pattern as every other satellite move this
-- session: V95 stopped the write, this finishes the cut) and join
-- dispatch_vehicle_detail into GATE_PASS_SATELLITE_SELECT so the generic
-- GatePass read transparently returns the real values instead.
--
-- invoice_no is NOT dropped/joined here — gate_pass.invoice_no is still
-- aria:billing-entry's own live save target (V95's rationale); joining
-- dvd.invoice_no unaliased alongside it would collide on the same column
-- name in the result map.

ALTER TABLE gate_pass
  DROP COLUMN IF EXISTS broker_name,
  DROP COLUMN IF EXISTS invoice_qty,
  DROP COLUMN IF EXISTS oil_rm_item,
  DROP COLUMN IF EXISTS tanker_no,
  DROP COLUMN IF EXISTS transport_name;

DELETE FROM entity_field_registry
WHERE entity_code='GatePass'
  AND field_code IN ('broker_name','invoice_qty','oil_rm_item','tanker_no','transport_name');
