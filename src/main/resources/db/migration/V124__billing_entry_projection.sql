-- V104 — Route Billing Entry fields to billing_entry instead of gate_pass.
--
-- Part 1: give the 3 M10 workflows whose transitions submit aria:billing-entry
-- (M10.FGLoading, M10.PackLoading, M10.TankerOutbound — confirmed via
-- workflow_definition.body_json: the only "pageCode":"aria:billing-entry"
-- transitions in the schema) a SECOND m0_workflow_projection target
-- (billing_entry), exactly like V36/V101 already do for gate_pass +
-- dispatch_vehicle_detail / gate_pass_closure on the same workflow_codes.
-- WorkflowProjectionListener needs a small code change (not zero, unlike
-- gate_pass_closure) because invoice_numbers is an ARRAY value, not a scalar
-- — see the accompanying WorkflowProjectionListener.java change, which adds a
-- billing_entry/invoice_numbers special case (serializes the submitted list
-- to JSON and writes invoice_number_count alongside it) and REMOVES the old
-- gate_pass/invoice_no special case, which existed only to resolve the
-- invoice_no collision between aria:dispatch-vehicle-details (vendor invoice)
-- and aria:billing-entry (sales invoice) on gate_pass — that collision goes
-- away once billing-entry's invoice numbers no longer land on gate_pass at
-- all.
--
-- field_map_json lists candidate bag keys for both the new plural array field
-- (invoice_numbers/invoiceNumbers, from a future repeater-widget submission)
-- and the current scalar field (invoice_no) so a page still submitting the
-- old single-value shape keeps working without another migration — the
-- listener wraps a lone scalar into a 1-element array.
--
-- Part 2: strip invoice_no/checking_officer out of the EXISTING
-- target_table='gate_pass' mapping (added by V36) for these 3 workflow_codes,
-- so new billing submissions stop writing them there. gate_pass keeps only
-- its status/current_step/closed_at + non-billing columns from now on.
-- Existing historical values in those gate_pass columns are left untouched —
-- this only changes where FUTURE writes land.

-- ---- Part 1: new billing_entry projection target (product default, tenant -1) ----
INSERT IGNORE INTO `m0_workflow_projection`
  (`tenant_id`,`workflow_code`,`target_table`,`key_column`,`state_columns`,`variant_column`,
   `number_column`,`number_pattern`,`terminal_states`,`terminal_ts_column`,`field_map_json`,`tolerance_pct`)
VALUES
(-1,'M10.FGLoading','billing_entry','workflow_instance_id','',NULL,NULL,NULL,NULL,NULL,
 '{"invoice_numbers":["invoice_numbers","invoiceNumbers","invoice_no"],"checking_officer":["checking_officer","checkingOfficer","billingOfficer"]}',
 NULL),
(-1,'M10.PackLoading','billing_entry','workflow_instance_id','',NULL,NULL,NULL,NULL,NULL,
 '{"invoice_numbers":["invoice_numbers","invoiceNumbers","invoice_no"],"checking_officer":["checking_officer","checkingOfficer","billingOfficer"]}',
 NULL),
(-1,'M10.TankerOutbound','billing_entry','workflow_instance_id','',NULL,NULL,NULL,NULL,NULL,
 '{"invoice_numbers":["invoice_numbers","invoiceNumbers","invoice_no"],"checking_officer":["checking_officer","checkingOfficer","billingOfficer"]}',
 NULL);

-- ---- Part 2: strip billing fields from the gate_pass field_map_json ----
-- JSON_MERGE_PATCH with a JSON null value REMOVES that key from the target
-- object (see V101 for the same technique / the MariaDB JSON-pseudo-type note).
UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(
  field_map_json,
  JSON_OBJECT('invoice_no', NULL, 'checking_officer', NULL)
)
WHERE target_table = 'gate_pass'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerOutbound')
  AND field_map_json IS NOT NULL;

-- ---- Metadata cleanup: tombstone (hidden=1, not delete) the orphaned
--      GatePass.invoice_no / GatePass.checking_officer field-registry rows
--      until V105 drops the columns and deletes these rows for good.
UPDATE entity_field_registry
SET hidden = 1
WHERE entity_code = 'GatePass' AND field_code IN ('invoice_no','checking_officer');
