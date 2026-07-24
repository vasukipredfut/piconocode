-- V87 — aria:pmt-weight-diff-check: add a third outcome. Today the system's
-- computed verdict (DispatchToleranceListener's pmt_within_tolerance) drives
-- which of 2 transitions fires — PMT_CHECK_OK (YES) or PMT_CAMERA_VERIFICATION
-- (NO) — via PARKING_IN_2. There was no way to send an over-tolerance vehicle
-- straight to the manager without going through camera verification first.
-- Adds a "Send to Manager — Skip Camera" button that submits a distinct
-- sentinel ('MANAGER_DIRECT') so it deterministically hits this new
-- transition regardless of the system's own verdict, straight to
-- PMT_CHECK_PENDING_MANAGER (the same state PMT_CAMERA_VERIFICATION already
-- feeds into via aria:pmt-camera-verification) — same manager review, no new
-- state needed.
--
-- Only M10.FGLoading and M10.PackLoading use aria:pmt-weight-diff-check with
-- the camera-verification path (M10.TankerOutbound's equivalent step is
-- aria:weight-diff-entry, which has no camera step at all — out of scope).
--
-- REPLACE-based (matches the live body exactly) rather than a full rewrite —
-- FGLoading's workflow_definition JSON has no spaces after ':', PackLoading's
-- does; both are handled as separate statements.

UPDATE workflow_definition
SET body_json = REPLACE(body_json,
  '"from":"PARKING_IN_2","to":"PMT_CAMERA_VERIFICATION","trigger":"page.submitted","pageCode":"aria:pmt-weight-diff-check","guard":"#submitted[''withinTolerance''] == ''NO''"}',
  '"from":"PARKING_IN_2","to":"PMT_CAMERA_VERIFICATION","trigger":"page.submitted","pageCode":"aria:pmt-weight-diff-check","guard":"#submitted[''withinTolerance''] == ''NO''"},{"from":"PARKING_IN_2","to":"PMT_CHECK_PENDING_MANAGER","trigger":"page.submitted","pageCode":"aria:pmt-weight-diff-check","guard":"#submitted[''withinTolerance''] == ''MANAGER_DIRECT''"}')
WHERE code='M10.FGLoading' AND status='PUBLISHED'
  AND body_json NOT LIKE '%MANAGER_DIRECT%';

UPDATE workflow_definition
SET body_json = REPLACE(body_json,
  '"from": "PARKING_IN_2", "to": "PMT_CAMERA_VERIFICATION", "trigger": "page.submitted", "pageCode": "aria:pmt-weight-diff-check", "guard": "#submitted[''withinTolerance''] == ''NO''"}',
  '"from": "PARKING_IN_2", "to": "PMT_CAMERA_VERIFICATION", "trigger": "page.submitted", "pageCode": "aria:pmt-weight-diff-check", "guard": "#submitted[''withinTolerance''] == ''NO''"}, {"from": "PARKING_IN_2", "to": "PMT_CHECK_PENDING_MANAGER", "trigger": "page.submitted", "pageCode": "aria:pmt-weight-diff-check", "guard": "#submitted[''withinTolerance''] == ''MANAGER_DIRECT''"}')
WHERE code='M10.PackLoading' AND status='PUBLISHED'
  AND body_json NOT LIKE '%MANAGER_DIRECT%';

-- Page: add the third button.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = REPLACE(pv.body_json,
  '"children": [{"kind": "WorkflowActionButton", "label": "Check OK", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CHECK_OK", "color": "primary"}, {"kind": "WorkflowActionButton", "label": "Camera Verification", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CAMERA_VERIFICATION", "color": "warning"}]',
  '"children": [{"kind": "WorkflowActionButton", "label": "Check OK", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CHECK_OK", "color": "primary"}, {"kind": "WorkflowActionButton", "label": "Camera Verification", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CAMERA_VERIFICATION", "color": "warning"}, {"kind": "WorkflowActionButton", "label": "Send to Manager — Skip Camera", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CHECK_PENDING_MANAGER", "color": "error", "submitValues": {"withinTolerance": "MANAGER_DIRECT"}}]')
WHERE pd.code='aria:pmt-weight-diff-check' AND pv.body_json NOT LIKE '%MANAGER_DIRECT%';
