-- V24 — map the new outbound capture fields into the workflow projection so they
-- persist to gate_pass. Without this, the sealing / seal-lock / invoice-count /
-- variance-reason fields the capture forms collect land in the workflow bag but
-- never reach the gate_pass columns. JSON_MERGE_PATCH is idempotent and keyed by
-- workflow_code so it's portable across databases.
UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(field_map_json, JSON_OBJECT(
  'sealing_no',             JSON_ARRAY('sealing_no','sealingNo'),
  'sealing_supervisor',     JSON_ARRAY('sealing_supervisor','sealingSupervisor'),
  'seal_lock_status',       JSON_ARRAY('seal_lock_status','sealLockStatus'),
  'invoice_count_verified', JSON_ARRAY('invoice_count_verified','invoiceCountVerified'),
  'variance_reason',        JSON_ARRAY('variance_reason','varianceReason'),
  'checking_officer',       JSON_ARRAY('checking_officer','checkingOfficer','billingOfficer'),
  'supervisor_name',        JSON_ARRAY('supervisor_name','supervisorName')
))
WHERE target_table='gate_pass'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerOutbound');
