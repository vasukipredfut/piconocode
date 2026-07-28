-- V120 — Route Gate Pass Closure fields to gate_pass_closure instead of gate_pass.
--
-- Part 1: give every M10 gate-pass workflow a SECOND m0_workflow_projection target
-- (gate_pass_closure), exactly like V36 already does for gate_pass + dispatch_vehicle_detail
-- on the same workflow_code. WorkflowProjectionListener needs no code change — it already
-- loops every config row for a workflow_code and upserts each target_table independently.
--
-- Part 2: strip the closure fields out of the EXISTING target_table='gate_pass' mapping
-- (added by V47) so new closure submissions stop writing them there. gate_pass keeps only
-- its status/current_step/closed_at columns from now on. Existing historical values in
-- those gate_pass columns are left untouched — this only changes where FUTURE writes land.

-- ---- Part 1: new gate_pass_closure projection target (product default, tenant -1) ----
INSERT IGNORE INTO `m0_workflow_projection`
  (`tenant_id`,`workflow_code`,`target_table`,`key_column`,`state_columns`,`variant_column`,
   `number_column`,`number_pattern`,`terminal_states`,`terminal_ts_column`,`field_map_json`,`tolerance_pct`)
VALUES
(-1,'M10.TankerInbound','gate_pass_closure','workflow_instance_id','',NULL,NULL,NULL,
 'EXIT_STAMPED,REJECTED,ABORTED,QC_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED','closed_at',
 '{"seal_verified":["seal_verified","sealVerified"],"invoice_count_verified":["invoice_count_verified","invoiceCountVerified"],"qc_clearance":["qc_clearance","qcClearance"],"weighment_verified":["weighment_verified","weighmentVerified"],"order_recon_done":["order_recon_done","orderReconDone"],"within_tolerance":["within_tolerance","withinTolerance"],"closure_note":["closure_note","closureNote"]}',
 NULL),
(-1,'M10.TankerOutbound','gate_pass_closure','workflow_instance_id','',NULL,NULL,NULL,
 'EXIT_STAMPED,REJECTED,ABORTED,QC_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED','closed_at',
 '{"seal_verified":["seal_verified","sealVerified"],"invoice_count_verified":["invoice_count_verified","invoiceCountVerified"],"qc_clearance":["qc_clearance","qcClearance"],"weighment_verified":["weighment_verified","weighmentVerified"],"order_recon_done":["order_recon_done","orderReconDone"],"within_tolerance":["within_tolerance","withinTolerance"],"closure_note":["closure_note","closureNote"]}',
 NULL),
(-1,'M10.PackLoading','gate_pass_closure','workflow_instance_id','',NULL,NULL,NULL,
 'EXIT_STAMPED,REJECTED,ABORTED,QC_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED','closed_at',
 '{"seal_verified":["seal_verified","sealVerified"],"invoice_count_verified":["invoice_count_verified","invoiceCountVerified"],"qc_clearance":["qc_clearance","qcClearance"],"weighment_verified":["weighment_verified","weighmentVerified"],"order_recon_done":["order_recon_done","orderReconDone"],"within_tolerance":["within_tolerance","withinTolerance"],"closure_note":["closure_note","closureNote"]}',
 NULL),
(-1,'M10.FGLoading','gate_pass_closure','workflow_instance_id','',NULL,NULL,NULL,
 'EXIT_STAMPED,REJECTED,ABORTED,QC_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED','closed_at',
 '{"seal_verified":["seal_verified","sealVerified"],"invoice_count_verified":["invoice_count_verified","invoiceCountVerified"],"qc_clearance":["qc_clearance","qcClearance"],"weighment_verified":["weighment_verified","weighmentVerified"],"order_recon_done":["order_recon_done","orderReconDone"],"within_tolerance":["within_tolerance","withinTolerance"],"closure_note":["closure_note","closureNote"]}',
 NULL),
(-1,'M10.PackUnload','gate_pass_closure','workflow_instance_id','',NULL,NULL,NULL,
 'EXIT_STAMPED,REJECTED,ABORTED,QC_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED','closed_at',
 '{"seal_verified":["seal_verified","sealVerified"],"invoice_count_verified":["invoice_count_verified","invoiceCountVerified"],"qc_clearance":["qc_clearance","qcClearance"],"weighment_verified":["weighment_verified","weighmentVerified"],"order_recon_done":["order_recon_done","orderReconDone"],"within_tolerance":["within_tolerance","withinTolerance"],"closure_note":["closure_note","closureNote"]}',
 NULL),
(-1,'M10.ReturnStockIn','gate_pass_closure','workflow_instance_id','',NULL,NULL,NULL,
 'EXIT_STAMPED,REJECTED,ABORTED,QC_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED','closed_at',
 '{"seal_verified":["seal_verified","sealVerified"],"invoice_count_verified":["invoice_count_verified","invoiceCountVerified"],"qc_clearance":["qc_clearance","qcClearance"],"weighment_verified":["weighment_verified","weighmentVerified"],"order_recon_done":["order_recon_done","orderReconDone"],"within_tolerance":["within_tolerance","withinTolerance"],"closure_note":["closure_note","closureNote"]}',
 NULL);

-- ---- Part 2: strip closure fields from the gate_pass field_map_json ----
-- JSON_MERGE_PATCH with a JSON null value REMOVES that key from the target object
-- (standard MySQL/MariaDB JSON_MERGE_PATCH semantics). JSON_OBJECT(key, NULL) already
-- emits a JSON null for that key on its own -- MariaDB has no JSON pseudo-type to CAST
-- to (JSON is just a LONGTEXT + CHECK(json_valid()) alias here), so CAST(NULL AS JSON)
-- is a MySQL-only construct and fails with a syntax error on MariaDB. Matches on
-- target_table='gate_pass' across BOTH tenant scopes (the tenant_id=1 row is the one
-- V47 actually populated with these keys; tenant_id=-1 rows never had them, so the
-- patch there is a harmless no-op).
UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(
  field_map_json,
  JSON_OBJECT(
    'seal_verified', NULL,
    'weighment_verified', NULL,
    'qc_clearance', NULL,
    'order_recon_done', NULL,
    'within_tolerance', NULL,
    'invoice_count_verified', NULL
  )
)
WHERE target_table = 'gate_pass'
  AND workflow_code IN (
    'M10.TankerInbound','M10.TankerOutbound','M10.PackLoading',
    'M10.FGLoading','M10.PackUnload','M10.ReturnStockIn'
  )
  AND field_map_json IS NOT NULL;

-- ---- Optional metadata cleanup: hide the now-orphaned GatePass.invoice_count_verified
--      field-registry row (tombstone, not delete — same REMOVE convention documented in
--      GenericEntityController: a hidden=1 row suppresses the field for this tenant without
--      touching the underlying column or any historical data).
UPDATE entity_field_registry
SET hidden = 1
WHERE entity_code = 'GatePass' AND field_code = 'invoice_count_verified';