-- aria:loading-allocation and aria:manager-qty-approval are shared across
-- 5 M10 workflows, each of which uses a DIFFERENT state name for the step
-- these pages submit into (confirmed against every workflow_definition):
--   loading-allocation's real "to" state:
--     M10.FGLoading / M10.PackLoading -> LOADING_ALLOCATED
--     M10.TankerInbound / M10.ReturnStockIn -> SUPERVISOR_ALLOCATED
--     M10.PackUnload -> UNLOADING_ALLOCATED
--   manager-qty-approval's real "to" state (approve / reject):
--     M10.FGLoading / M10.PackLoading -> PMT_CHECK_OK / (no reject wired)
--     M10.TankerInbound -> QTY_RECONCILED / (no reject wired)
--     M10.PackUnload -> QUANTITY_RECONCILIATION / REJECTED_BY_MANAGER
--     M10.TankerOutbound -> WEIGHT_DIFF_OK / (no reject wired)
-- Both pages' WorkflowActionButton hardcoded ONE literal targetState (picked
-- to match FGLoading/PackLoading, the flows these pages were apparently
-- built against first) -- reported live against M10.PackUnload: "Assign
-- Supervisor" stays disabled with required fields filled in, because the
-- frontend checks the declared targetState against
-- GET /api/workflow/instances/{id}/available before enabling the submit
-- button, and PackUnload's real next state never matches the hardcoded one.
-- Confirmed the backend transition itself has always worked regardless
-- (WorkflowEngineService resolves purely off pageCode + current state +
-- guard, targetState is a frontend-only pre-flight hint) -- this is a
-- frontend-gating-only bug, fixed by dropping targetState entirely, the
-- same way the majority of WorkflowActionButtons elsewhere in this app
-- already do (aria:qc-approval, aria:reconciliation, aria:weighment-capture,
-- aria:supervisor-unloading, aria:dispatch-vehicle-details,
-- aria:gate-pass-closure, aria:gate-pass-closure-form all omit it and rely
-- on guard-based resolution).
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '{"kind": "WorkflowActionButton", "label": "Assign Supervisor", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "LOADING_ALLOCATED", "color": "primary"}',
  '{"kind": "WorkflowActionButton", "label": "Assign Supervisor", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "color": "primary"}'
)
WHERE pd.code = 'aria:loading-allocation' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%{"kind": "WorkflowActionButton", "label": "Assign Supervisor", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "LOADING_ALLOCATED", "color": "primary"}%';

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '{"kind": "WorkflowActionButton", "label": "✓ Approve & Release to Billing", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CHECK_OK", "color": "primary", "submitValues": {"status": "APPROVED"}}, {"kind": "WorkflowActionButton", "label": "✗ Reject — Send Back to Reload", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "RELOAD_REQUIRED", "color": "error", "submitValues": {"status": "REJECTED"}}',
  '{"kind": "WorkflowActionButton", "label": "✓ Approve & Release to Billing", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "color": "primary", "submitValues": {"status": "APPROVED"}}, {"kind": "WorkflowActionButton", "label": "✗ Reject — Send Back to Reload", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "color": "error", "submitValues": {"status": "REJECTED"}}'
)
WHERE pd.code = 'aria:manager-qty-approval' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%{"kind": "WorkflowActionButton", "label": "✓ Approve & Release to Billing", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CHECK_OK", "color": "primary", "submitValues": {"status": "APPROVED"}}, {"kind": "WorkflowActionButton", "label": "✗ Reject — Send Back to Reload", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "RELOAD_REQUIRED", "color": "error", "submitValues": {"status": "REJECTED"}}%';
