-- V198's qc_unloading_approval field_map_json includes "status": ["status"].
-- aria:qc-approval's "status" field name is reused later in every workflow
-- that reaches this page (aria:manager-qty-approval / aria:manager-weight-
-- approval both submit their own "status" too), and WorkflowProjectionListener
-- re-evaluates every configured field against the whole accumulated
-- submitted-data bag on EVERY later transition -- so this generic mapping
-- gets silently overwritten by a manager-approval's "status" on a later
-- step, corrupting the original QC decision. Confirmed live for
-- M10.PackUnload: QC approves, status briefly holds "APPROVED", then
-- flips to whatever aria:manager-qty-approval submits next.
--
-- QcApprovalDecisionListener.java already reads aria:qc-approval's own
-- page-scoped bag entry (not a bag-wide key search) and now writes "status"
-- (plus qc_reject_reason) into this same qc_unloading_approval table
-- directly -- dropping it from the generic map here removes the unsafe
-- path without losing the capture.
UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json, '$.status')
WHERE target_table = 'qc_unloading_approval'
  AND JSON_CONTAINS_PATH(field_map_json, 'one', '$.status');
