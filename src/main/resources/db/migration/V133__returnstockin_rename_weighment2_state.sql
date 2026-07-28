-- M10.ReturnStockIn: rename the state entered right after the second
-- weighment from WEIGHT_DIFF_PENDING_MANAGER to WEIGHMENT_2, per direct
-- request — the state list should show a distinct "second weighment"
-- checkpoint mirroring WEIGHMENT_1, not jump straight into a
-- pending-manager-only name with no visible second-weight step.
--
-- Purely a rename: same page (aria:weighment-capture), same behavior — it
-- still always escalates to aria:manager-weight-approval (no
-- within-tolerance auto-pass branch), matching the spec's "second weight and
-- expected weight difference with reason, update with manager approval"
-- requirement. ManagerWeightApprovalDecisionListener.java updated alongside
-- this to gate on WEIGHMENT_2 (not WEIGHT_DIFF_PENDING_MANAGER) specifically
-- for M10.ReturnStockIn — M10.TankerInbound keeps its own
-- WEIGHT_DIFF_PENDING_MANAGER name unchanged, so the listener now checks
-- (workflowCode, previousState) as a pair rather than one shared state name.
UPDATE workflow_definition
   SET body_json = '{"code":"M10.ReturnStockIn","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT"},{"code":"GATE_PASS_CREATED","assignedRole":"Dispatch"},{"code":"DISPATCH_QUEUED","assignedRole":"QC"},{"code":"QC_APPROVED","assignedRole":"Dispatch"},{"code":"QC_REJECTED","terminal":true},{"code":"SUPERVISOR_ALLOCATED","assignedRole":"WeighmentOfficer"},{"code":"WEIGHMENT_1","assignedRole":"UnloadingSupervisor"},{"code":"UNLOADING_DONE","assignedRole":"Dispatch"},{"code":"STOCK_DIFF_RECORDED","assignedRole":"WeighmentOfficer"},{"code":"WEIGHMENT_2","assignedRole":"Manager"},{"code":"READY_FOR_CLOSURE","assignedRole":"FrontGate"},{"code":"CLOSED","terminal":true},{"code":"REJECTED_BY_MANAGER","terminal":true}],"transitions":[{"from":"DRAFT","to":"GATE_PASS_CREATED","trigger":"page.submitted","pageCode":"aria:front-gate-create"},{"from":"GATE_PASS_CREATED","to":"DISPATCH_QUEUED","trigger":"page.submitted","pageCode":"aria:dispatch-vehicle-details"},{"from":"DISPATCH_QUEUED","to":"QC_APPROVED","trigger":"page.submitted","pageCode":"aria:qc-approval","guard":"#submitted[\'status\'] == \'APPROVED\'"},{"from":"DISPATCH_QUEUED","to":"QC_REJECTED","trigger":"page.submitted","pageCode":"aria:qc-approval","guard":"#submitted[\'status\'] == \'REJECTED\'"},{"from":"QC_APPROVED","to":"SUPERVISOR_ALLOCATED","trigger":"page.submitted","pageCode":"aria:loading-allocation"},{"from":"SUPERVISOR_ALLOCATED","to":"WEIGHMENT_1","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHMENT_1","to":"UNLOADING_DONE","trigger":"page.submitted","pageCode":"aria:supervisor-unloading"},{"from":"UNLOADING_DONE","to":"STOCK_DIFF_RECORDED","trigger":"page.submitted","pageCode":"aria:reconciliation"},{"from":"STOCK_DIFF_RECORDED","to":"WEIGHMENT_2","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHMENT_2","to":"READY_FOR_CLOSURE","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[\'status\'] == \'APPROVED\'"},{"from":"WEIGHMENT_2","to":"REJECTED_BY_MANAGER","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[\'status\'] == \'REJECTED\'"},{"from":"READY_FOR_CLOSURE","to":"CLOSED","trigger":"page.submitted","pageCode":"aria:gate-pass-closure"}]}'
 WHERE code = 'M10.ReturnStockIn' AND status = 'PUBLISHED';
