-- V71 — M10 role reassignments across three workflows (already applied live,
-- this backfills fresh DBs / other environments):
--
-- M10.TankerOutbound
--   DISPATCH_VEHICLE_ENTRY: Planning -> QC
--   BILLING_DONE: WeighmentOfficer -> ParkingSecurity
--
-- M10.FGLoading
--   LOADING_ALLOCATED: WeighmentOfficer -> ParkingSecurity
--   BILLING_DONE: WeighmentOfficer -> ParkingSecurity
--   RELOAD_REQUIRED: LoadingSupervisor -> WeighmentOfficer
--
-- M10.PackLoading
--   LOADING_ALLOCATED: WeighmentOfficer -> ParkingSecurity
--   BILLING_DONE: WeighmentOfficer -> ParkingSecurity
--   (this workflow's body_json is stored with spaces after ':'/',' —
--    unlike TankerOutbound/FGLoading's compact form — patterns below match
--    each workflow's actual stored style.)

-- M10.TankerOutbound
UPDATE workflow_definition
   SET body_json = REPLACE(body_json,
         '"code":"DISPATCH_VEHICLE_ENTRY","assignedRole":"Planning"',
         '"code":"DISPATCH_VEHICLE_ENTRY","assignedRole":"QC"')
 WHERE code = 'M10.TankerOutbound' AND status = 'PUBLISHED';

UPDATE workflow_definition
   SET body_json = REPLACE(body_json,
         '"code":"BILLING_DONE","assignedRole":"WeighmentOfficer"',
         '"code":"BILLING_DONE","assignedRole":"ParkingSecurity"')
 WHERE code = 'M10.TankerOutbound' AND status = 'PUBLISHED';

-- M10.FGLoading
UPDATE workflow_definition
   SET body_json = REPLACE(body_json,
         '"code":"LOADING_ALLOCATED","assignedRole":"WeighmentOfficer"',
         '"code":"LOADING_ALLOCATED","assignedRole":"ParkingSecurity"')
 WHERE code = 'M10.FGLoading' AND status = 'PUBLISHED';

UPDATE workflow_definition
   SET body_json = REPLACE(body_json,
         '"code":"BILLING_DONE","assignedRole":"WeighmentOfficer"',
         '"code":"BILLING_DONE","assignedRole":"ParkingSecurity"')
 WHERE code = 'M10.FGLoading' AND status = 'PUBLISHED';

UPDATE workflow_definition
   SET body_json = REPLACE(body_json,
         '"code":"RELOAD_REQUIRED","assignedRole":"LoadingSupervisor"',
         '"code":"RELOAD_REQUIRED","assignedRole":"WeighmentOfficer"')
 WHERE code = 'M10.FGLoading' AND status = 'PUBLISHED';

-- M10.PackLoading (spaced JSON style)
UPDATE workflow_definition
   SET body_json = REPLACE(body_json,
         '"code": "LOADING_ALLOCATED", "assignedRole": "WeighmentOfficer"',
         '"code": "LOADING_ALLOCATED", "assignedRole": "ParkingSecurity"')
 WHERE code = 'M10.PackLoading' AND status = 'PUBLISHED';

UPDATE workflow_definition
   SET body_json = REPLACE(body_json,
         '"code": "BILLING_DONE", "assignedRole": "WeighmentOfficer"',
         '"code": "BILLING_DONE", "assignedRole": "ParkingSecurity"')
 WHERE code = 'M10.PackLoading' AND status = 'PUBLISHED';
