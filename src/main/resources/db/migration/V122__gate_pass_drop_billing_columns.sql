-- V105 — finish the Billing Entry split (V103/V104): drop the 2
-- billing-capture columns from gate_pass now that billing_entry is the sole
-- write target for them. GenericEntityController's GATE_PASS_SATELLITE_JOIN/
-- SELECT is updated in the same change to join billing_entry, so GET
-- .../GatePass/{id} keeps surfacing these fields (now read from
-- billing_entry) instead of silently losing them — invoice_no is re-derived
-- as a comma-joined string of billing_entry.invoice_numbers for the sake of
-- existing scalar consumers (slip_field_map rows for M10.FGLoading /
-- M10.TankerOutbound print an 'invoice_no' line via GET /api/m0/slip-fields;
-- see V15__flow_variant_slip.sql).

ALTER TABLE gate_pass
  DROP COLUMN IF EXISTS invoice_no,
  DROP COLUMN IF EXISTS checking_officer;

DELETE FROM entity_field_registry
WHERE entity_code='GatePass'
  AND field_code IN ('invoice_no','checking_officer');
