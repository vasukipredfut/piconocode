-- V61__qc_preload_approval_restore_submitvalues.sql
-- V58__m10_remaining_gpnumber_datasource_filters.sql did a full body_json
-- REPLACE for aria:qc-preload-approval (to fix the Gate Pass # dropdown's
-- assigned_role filter) from a copy of the page that predates the submitValues
-- fix in V50__packloading_reconcile_with_team.sql, silently dropping it again:
-- the Approve/Reject WorkflowActionButtons went back to submitting no `status`
-- key, so the transition guard #submitted['status']=='APPROVED'/'REJECTED'
-- always rejects with 409 (confirmed live: POST /api/workflow/transition on
-- this page returns submitted-fields=[...] with no `status` entry).
--
-- Re-apply the same targeted fix V50 used — idempotent REPLACE(), no-op if
-- the target substring is already absent (i.e. if submitValues is already
-- present, this changes nothing).
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json,
        '"targetState": "QC_PRELOAD_APPROVED", "color": "primary"}',
        '"targetState": "QC_PRELOAD_APPROVED", "color": "primary", "submitValues": {"status": "APPROVED", "qc_status": "APPROVED"}}')
 WHERE pd.code = 'aria:qc-preload-approval';

UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json,
        '"targetState": "QC_PRELOAD_REJECTED", "color": "error"}',
        '"targetState": "QC_PRELOAD_REJECTED", "color": "error", "submitValues": {"status": "REJECTED", "qc_status": "REJECTED"}}')
 WHERE pd.code = 'aria:qc-preload-approval';
