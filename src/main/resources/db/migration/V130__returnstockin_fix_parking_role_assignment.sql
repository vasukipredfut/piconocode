-- Bug fix: V127 gave 3 of ReturnStockIn's states an assignedRole based on
-- loose "who conceptually owns this step" guesses, but every M0 picker/queue
-- is filtered by a HARDCODED wi.assigned_role value baked into that specific
-- page's body_json (confirmed by querying the live page_version rows):
--   aria:parking-scan          -> wi.assigned_role = 'ParkingSecurity' (always)
--   aria:weighment-capture     -> wi.assigned_role = 'WeighmentOfficer'
--   aria:supervisor-unloading  -> wi.assigned_role = 'UnloadingSupervisor'
--   aria:reconciliation        -> wi.assigned_role = 'Dispatch'
--   aria:manager-weight-approval -> wi.assigned_role = 'Manager'
--   aria:gate-pass-closure     -> wi.assigned_role = 'FrontGate'
-- A state's assignedRole MUST equal whatever role the NEXT page's picker
-- filters by, or that gate pass simply never appears in anyone's picker —
-- the transition itself is still correctly configured (WorkflowEngineService
-- resolves purely on current_state + pageCode, confirmed in
-- WorkflowEngineService.transitionsFor), but nobody can reach the page to
-- fire it. This is what broke 3 of the 4 parking-bay checkpoints:
--   SUPERVISOR_ALLOCATED  (feeds aria:parking-scan)   Wrong: UnloadingSupervisor -> Fixed: ParkingSecurity
--   PARKING_OUT_1         (feeds aria:weighment-capture) Wrong: UnloadingSupervisor -> Fixed: WeighmentOfficer
--   UNLOADING_DONE        (feeds aria:parking-scan)   Wrong: Dispatch -> Fixed: ParkingSecurity
--   STOCK_DIFF_RECORDED   (feeds aria:parking-scan)   Wrong: WeighmentOfficer -> Fixed: ParkingSecurity
-- (GATE_PASS_CREATED, PARKING_IN_1, DISPATCH_QUEUED, QC_APPROVED, WEIGHMENT_1,
-- PARKING_IN_2, PARKING_OUT_2, WEIGHT_DIFF_PENDING_MANAGER, READY_FOR_CLOSURE
-- already matched their next page's role and are unchanged.)
--
-- Only assignedRole values change below; states/transitions/pageCodes/guards
-- are byte-identical to V127.
UPDATE workflow_definition
   SET body_json = '{"code":"M10.ReturnStockIn","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT"},{"code":"GATE_PASS_CREATED","assignedRole":"ParkingSecurity"},{"code":"PARKING_IN_1","assignedRole":"Dispatch"},{"code":"DISPATCH_QUEUED","assignedRole":"QC"},{"code":"QC_APPROVED","assignedRole":"Dispatch"},{"code":"QC_REJECTED","terminal":true},{"code":"SUPERVISOR_ALLOCATED","assignedRole":"ParkingSecurity"},{"code":"PARKING_OUT_1","assignedRole":"WeighmentOfficer"},{"code":"WEIGHMENT_1","assignedRole":"UnloadingSupervisor"},{"code":"UNLOADING_DONE","assignedRole":"ParkingSecurity"},{"code":"PARKING_IN_2","assignedRole":"Dispatch"},{"code":"STOCK_DIFF_RECORDED","assignedRole":"ParkingSecurity"},{"code":"PARKING_OUT_2","assignedRole":"WeighmentOfficer"},{"code":"WEIGHT_DIFF_PENDING_MANAGER","assignedRole":"Manager"},{"code":"READY_FOR_CLOSURE","assignedRole":"FrontGate"},{"code":"CLOSED","terminal":true},{"code":"REJECTED_BY_MANAGER","terminal":true}],"transitions":[{"from":"DRAFT","to":"GATE_PASS_CREATED","trigger":"page.submitted","pageCode":"aria:front-gate-create"},{"from":"GATE_PASS_CREATED","to":"PARKING_IN_1","trigger":"page.submitted","pageCode":"aria:parking-scan"},{"from":"PARKING_IN_1","to":"DISPATCH_QUEUED","trigger":"page.submitted","pageCode":"aria:dispatch-vehicle-details"},{"from":"DISPATCH_QUEUED","to":"QC_APPROVED","trigger":"page.submitted","pageCode":"aria:qc-approval","guard":"#submitted[\'status\'] == \'APPROVED\'"},{"from":"DISPATCH_QUEUED","to":"QC_REJECTED","trigger":"page.submitted","pageCode":"aria:qc-approval","guard":"#submitted[\'status\'] == \'REJECTED\'"},{"from":"QC_APPROVED","to":"SUPERVISOR_ALLOCATED","trigger":"page.submitted","pageCode":"aria:loading-allocation"},{"from":"SUPERVISOR_ALLOCATED","to":"PARKING_OUT_1","trigger":"page.submitted","pageCode":"aria:parking-scan"},{"from":"PARKING_OUT_1","to":"WEIGHMENT_1","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHMENT_1","to":"UNLOADING_DONE","trigger":"page.submitted","pageCode":"aria:supervisor-unloading"},{"from":"UNLOADING_DONE","to":"PARKING_IN_2","trigger":"page.submitted","pageCode":"aria:parking-scan"},{"from":"PARKING_IN_2","to":"STOCK_DIFF_RECORDED","trigger":"page.submitted","pageCode":"aria:reconciliation"},{"from":"STOCK_DIFF_RECORDED","to":"PARKING_OUT_2","trigger":"page.submitted","pageCode":"aria:parking-scan"},{"from":"PARKING_OUT_2","to":"WEIGHT_DIFF_PENDING_MANAGER","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHT_DIFF_PENDING_MANAGER","to":"READY_FOR_CLOSURE","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[\'status\'] == \'APPROVED\'"},{"from":"WEIGHT_DIFF_PENDING_MANAGER","to":"REJECTED_BY_MANAGER","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[\'status\'] == \'REJECTED\'"},{"from":"READY_FOR_CLOSURE","to":"CLOSED","trigger":"page.submitted","pageCode":"aria:gate-pass-closure"}]}'
 WHERE code = 'M10.ReturnStockIn' AND status = 'PUBLISHED';
