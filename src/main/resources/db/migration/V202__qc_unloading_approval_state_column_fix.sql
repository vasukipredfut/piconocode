-- m0_workflow_projection.state_columns defaults to 'status' table-wide (the
-- common case: a projected table's own "status" column just mirrors the raw
-- workflow state, e.g. gate_pass.status). V197/V198's INSERTs for
-- qc_unloading_approval never listed state_columns explicitly, so every row
-- silently inherited that default -- but qc_unloading_approval.status was
-- deliberately added by V198 to mean the QC decision itself ('APPROVED'/
-- 'REJECTED'), not the raw workflow state. Net effect: on every transition,
-- WorkflowProjectionListener.project() unconditionally overwrites status
-- with the raw state name (e.g. "QC_APPROVED"), stomping the real decision
-- QcApprovalDecisionListener had just written -- confirmed live: submitted
-- status=APPROVED, ended up with status="QC_APPROVED" in the table.
-- current_step already exists on this table for exactly the raw-state-mirror
-- purpose (same convention used by every other M10 satellite detail table --
-- dispatch_vehicle_detail, weighment_capture_detail, etc. all use
-- state_columns='current_step'), so this just points state_columns there
-- instead, freeing "status" to hold only what QcApprovalDecisionListener
-- writes to it.
UPDATE m0_workflow_projection
SET state_columns = 'current_step'
WHERE target_table = 'qc_unloading_approval' AND state_columns = 'status';
