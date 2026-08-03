-- M10.PackUnload's QUANTITY_RECONCILIATION state was assigned to "Dispatch"
-- -- every other M10 workflow's equivalent "waiting for the weighment
-- officer to take the next weight" state (SUPERVISOR_ALLOCATED,
-- UNLOADING_DONE, QTY_RECONCILED, PARKING_OUT_1/2, LOADING_IN_PROGRESS,
-- STOCK_DIFF_RECORDED) is assigned "WeighmentOfficer", matching
-- aria:weighment-capture's own gate-pass picker filter
-- (wi.assigned_role = 'WeighmentOfficer'). Reported live: a PackUnload gate
-- pass sitting at QUANTITY_RECONCILIATION never appears in that picker's
-- dropdown, because its assigned_role ('Dispatch') simply doesn't match
-- the filter every other flow's equivalent state satisfies.
--
-- 1. Fix the state's role assignment so future transitions into
--    QUANTITY_RECONCILIATION get the correct role.
UPDATE workflow_definition
SET body_json = REPLACE(body_json,
  '{"code":"QUANTITY_RECONCILIATION","assignedRole":"Dispatch"}',
  '{"code":"QUANTITY_RECONCILIATION","assignedRole":"WeighmentOfficer"}')
WHERE code = 'M10.PackUnload' AND status = 'PUBLISHED'
  AND body_json LIKE '%{"code":"QUANTITY_RECONCILIATION","assignedRole":"Dispatch"}%';

-- 2. Backfill: any gate pass ALREADY sitting at QUANTITY_RECONCILIATION
--    right now has "Dispatch" baked into its own workflow_instance row
--    (assigned_role is copied at transition time, not looked up live) --
--    fix those in place too, so this unblocks immediately, not just for
--    instances that reach this state after the fix.
UPDATE workflow_instance
SET assigned_role = 'WeighmentOfficer'
WHERE workflow_code = 'M10.PackUnload' AND current_state = 'QUANTITY_RECONCILIATION' AND assigned_role = 'Dispatch';
