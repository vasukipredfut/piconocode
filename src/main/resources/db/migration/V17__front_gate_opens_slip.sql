-- V17 — the front gate opens the slip the moment a gate pass is created.
--
-- Creating a gate pass and printing its slip were disjointed: the operator hit
-- "Create Gate Pass", got a toast, then had to hunt for the pass in a list to
-- print the driver's slip. Now the Create button carries onSuccessOpen, so on a
-- successful create the UI jumps straight to the flow-variant-aware slip for the
-- brand-new pass (resolved from the workflow instance it just started).
--
-- Substrate data only — the generic onSuccessOpen capability lives in
-- WorkflowActionButton; here we just switch it on for the front gate.

UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_SET(
  pv.body_json,
  '$.layout[0].children[0].children[0].children[0].onSuccessOpen',
  JSON_OBJECT(
    'page', 'aria:gate-pass-slip',
    'lookupEntity', 'GatePass',
    'lookupField', 'workflow_instance_id',
    'param', 'id'
  )
)
WHERE pd.code = 'aria:front-gate-create'
  AND JSON_EXTRACT(pv.body_json, '$.layout[0].children[0].children[0].children[0].kind') = 'WorkflowActionButton';
