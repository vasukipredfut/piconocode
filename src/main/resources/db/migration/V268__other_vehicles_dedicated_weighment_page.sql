-- M10.OtherVehicles' workflow_definition (V259) still declared
-- "pageCode":"aria:weighment-capture" on its DRAFT->WEIGHMENT_1,
-- LOADING_UNLOADING_IN_PROGRESS->WEIGHMENT_2, and
-- LOADING_UNLOADING_IN_PROGRESS->GATE_PASS_CLOSURE (skip_weighment branch)
-- transitions -- but the actual page built for this flow is the dedicated
-- aria:weighment-capture-other-vehicle (own weight_seq/skip_weighment/
-- time_taken fields, V267), not the shared generic one.
--
-- WorkflowEngineService.transitionsFor() matches a submission's pageCode
-- against the transition's own pageCode EXACTLY -- a non-null mismatch skips
-- the candidate entirely (see the "!defPage.equals(pageCode) -> continue"
-- check). Left as "aria:weighment-capture", every submission from the real
-- page would be rejected outright with "no transition from <state> via page
-- aria:weighment-capture-other-vehicle" -- the workflow could never advance
-- past DRAFT at all.
--
-- Idempotent -- safe to (re)run on any database.

UPDATE workflow_definition
SET body_json = REPLACE(body_json, '"pageCode":"aria:weighment-capture"', '"pageCode":"aria:weighment-capture-other-vehicle"')
WHERE tenant_id = -1 AND code = 'M10.OtherVehicles' AND version = 1
  AND body_json LIKE '%"pageCode":"aria:weighment-capture"%';
