-- M10.TankerInbound: drop the 4 dedicated parking-scan states (PARKING_IN,
-- PARKING_OUT_AFTER_QC, PARKING_IN_AFTER_UNLOAD, PARKING_OUT_FINAL). Parking
-- is a physical stop, not a gated step that should hold up the workflow with
-- its own page — the 4 removed transitions merge into their neighbors:
--   GATE_PASS_CREATED   --(was ->PARKING_IN->)--        DISPATCH_QUEUED
--   QC_APPROVED         --(was ->PARKING_OUT_AFTER_QC->) SUPERVISOR_ALLOCATED
--   WEIGHMENT_EMPTY     --(was ->PARKING_IN_AFTER_UNLOAD->) QTY_RECONCILED/QTY_PENDING_MANAGER
--   QTY_RECONCILED      --(was ->PARKING_OUT_FINAL->)    WEIGHMENT_RECHECK
-- assignedRole on the 4 surviving predecessor states is updated to the role
-- that was on the removed successor state, so the right persona's queue
-- still picks up the pending item (aria:dispatch-vehicle-details filters its
-- queue by assigned_role, not by state name, so this is enough — no page
-- change needed). aria:parking-scan and ParkingBayEntryTransitionListener are
-- shared across all 6 M10 gate-pass workflows and untouched; they simply
-- stop firing for TankerInbound since no transition points at that page
-- anymore.
--
-- Also: QC_APPROVED -> SUPERVISOR_ALLOCATED was pointed at
-- aria:dispatch-vehicle-details (vehicle details re-entry — already captured
-- at GATE_PASS_CREATED -> DISPATCH_QUEUED). What that state actually needs is
-- allocating the unloading supervisor, so it's repointed at
-- aria:loading-allocation instead (same page PackLoading already uses for
-- its LOADING_ALLOCATED step — "targetState" in its button is just a UI
-- hint, WorkflowEngineService resolves the real next state from
-- workflow_definition transitions by from-state+pageCode, so it's safe to
-- reuse verbatim). That page's Loading Bay / OrderApp Order# fields (and
-- their auto-filled customerName/orderRoute/orderedCases/orderedQty) are
-- PackLoading-specific and don't apply to a bulk-tanker inbound — gated off
-- for M10.TankerInbound below.
UPDATE workflow_definition
   SET body_json = '{"code":"M10.TankerInbound","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT"},{"code":"GATE_PASS_CREATED","assignedRole":"Dispatch"},{"code":"DISPATCH_QUEUED","assignedRole":"QC"},{"code":"QC_APPROVED","assignedRole":"Dispatch"},{"code":"QC_REJECTED","terminal":true},{"code":"SUPERVISOR_ALLOCATED","assignedRole":"WeighmentOfficer"},{"code":"WEIGHMENT_1","assignedRole":"UnloadingSupervisor"},{"code":"UNLOADING_DONE","assignedRole":"WeighmentOfficer"},{"code":"WEIGHMENT_2","assignedRole":"Dispatch"},{"code":"QTY_RECONCILED","assignedRole":"WeighmentOfficer"},{"code":"QTY_PENDING_MANAGER","assignedRole":"Manager"},{"code":"WEIGHMENT_3","assignedRole":"FrontGate"},{"code":"WEIGHT_DIFF_PENDING_MANAGER","assignedRole":"Manager"},{"code":"CLOSED","terminal":true},{"code":"REJECTED_BY_MANAGER","terminal":true}],"transitions":[{"from":"DRAFT","to":"GATE_PASS_CREATED","trigger":"page.submitted","pageCode":"aria:front-gate-create"},{"from":"GATE_PASS_CREATED","to":"DISPATCH_QUEUED","trigger":"page.submitted","pageCode":"aria:dispatch-vehicle-details"},{"from":"DISPATCH_QUEUED","to":"QC_APPROVED","trigger":"page.submitted","pageCode":"aria:qc-approval","guard":"#submitted[\'status\'] == \'APPROVED\'"},{"from":"DISPATCH_QUEUED","to":"QC_REJECTED","trigger":"page.submitted","pageCode":"aria:qc-approval","guard":"#submitted[\'status\'] == \'REJECTED\'"},{"from":"QC_APPROVED","to":"SUPERVISOR_ALLOCATED","trigger":"page.submitted","pageCode":"aria:loading-allocation"},{"from":"SUPERVISOR_ALLOCATED","to":"WEIGHMENT_1","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHMENT_1","to":"UNLOADING_DONE","trigger":"page.submitted","pageCode":"aria:supervisor-unloading"},{"from":"UNLOADING_DONE","to":"WEIGHMENT_2","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHMENT_2","to":"QTY_RECONCILED","trigger":"page.submitted","pageCode":"aria:reconciliation","guard":"#submitted[\'withinTolerance\'] == \'YES\'"},{"from":"WEIGHMENT_2","to":"QTY_PENDING_MANAGER","trigger":"page.submitted","pageCode":"aria:reconciliation","guard":"#submitted[\'withinTolerance\'] == \'NO\'"},{"from":"QTY_PENDING_MANAGER","to":"QTY_RECONCILED","trigger":"page.submitted","pageCode":"aria:manager-qty-approval","guard":"#submitted[\'status\'] == \'APPROVED\'"},{"from":"QTY_PENDING_MANAGER","to":"REJECTED_BY_MANAGER","trigger":"page.submitted","pageCode":"aria:manager-qty-approval","guard":"#submitted[\'status\'] == \'REJECTED\'"},{"from":"QTY_RECONCILED","to":"WEIGHMENT_3","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHMENT_3","to":"CLOSED","trigger":"page.submitted","pageCode":"aria:gate-pass-closure","guard":"#submitted[\'withinTolerance\'] == \'YES\'"},{"from":"WEIGHMENT_3","to":"WEIGHT_DIFF_PENDING_MANAGER","trigger":"page.submitted","pageCode":"aria:gate-pass-closure","guard":"#submitted[\'withinTolerance\'] == \'NO\'"},{"from":"WEIGHT_DIFF_PENDING_MANAGER","to":"CLOSED","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[\'status\'] == \'APPROVED\'"},{"from":"WEIGHT_DIFF_PENDING_MANAGER","to":"REJECTED_BY_MANAGER","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[\'status\'] == \'REJECTED\'"}]}'
 WHERE code = 'M10.TankerInbound' AND status = 'PUBLISHED';

UPDATE page_version pv
  JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = '{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "dataSources": [{"id": "q", "kind": "rest", "method": "GET", "url": "/api/workflow/queue/Dispatch"}], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Loading Supervisor Assign — Queue", "span": 12, "subtitle": "Vehicles approved by QC, awaiting a loading supervisor.", "children": [{"kind": "WorkflowQueueList", "persona": "Dispatch", "dataSource": "q", "pageSize": 25, "cardFields": ["gpNumber", "vehicleNo", "flowVariant", "currentState"]}]}, {"kind": "Section", "title": "Capture & Submit", "span": 12, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (pick one)", "type": "select", "required": true, "optionsSource": {"type": "entity", "entity": "GatePass", "lookupType": "gpNumbers", "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "flow_variant_code": "flow_variant_code", "instanceId": "workflow_instance_id"}, "scannable": true, "freeSolo": true}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "flow_variant_code", "label": "Flow", "type": "text", "readOnly": true}, {"name": "loadingBay", "label": "Loading Bay / Point", "type": "select", "visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'", "requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'", "placeholder": "Which bay is this vehicle loading at?", "optionsSource": {"type": "entity", "entity": "LoadingPoint", "valueField": "code", "labelField": "display_name"}}, {"name": "orderRef", "label": "OrderApp Order #", "type": "select", "visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'", "requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'", "placeholder": "Pick the dispatch order this vehicle is loading", "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "OrderAppOrder", "filter": {"field": "vehicle_no", "op": "eq", "value": "pageData:vehicle_no"}, "select": ["order_ref"], "sort": [{"field": "order_ref", "dir": "asc"}]}, "valueField": "order_ref", "labelField": "order_ref"}, "onChangeFill": {"customerName": "customer_name", "orderRoute": "customer_route", "orderedQty": "ordered_weight_kg", "orderedCases": "ordered_cases"}, "scannable": false, "freeSolo": false}, {"name": "customerName", "label": "Customer", "type": "text", "disabled": true, "visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}, {"name": "orderRoute", "label": "Route", "type": "text", "disabled": true, "visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}, {"name": "orderedCases", "label": "Ordered Cases", "type": "text", "disabled": true, "visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}, {"name": "orderedQty", "label": "Ordered Wt (kg)", "type": "text", "disabled": true, "visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}, {"name": "supervisor_name", "label": "Supervisor Name", "type": "select", "required": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "Employee", "select": ["id", "employeename"], "filter": {"and": [{"field": "employeetype", "op": "eq", "value": "Supervisor"}, {"field": "status", "op": "eq", "value": "A"}]}, "sort": [{"field": "employeename", "dir": "asc"}]}, "valueField": "id", "labelField": "employeename"}}]}, "children": [{"kind": "WorkflowActionButton", "label": "Assign Supervisor", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "LOADING_ALLOCATED", "color": "primary"}]}]}]}]}'
 WHERE pd.code = 'aria:loading-allocation' AND pv.status = 'PUBLISHED';

-- M10.TankerInbound was missing an m0_workflow_projection row for
-- dispatch_vehicle_detail — FGLoading/PackLoading/TankerOutbound all have one
-- (ids 13/15/20), but TankerInbound only had rows for gate_pass and
-- weighment_capture_detail. Without it, everything the operator submits on
-- aria:dispatch-vehicle-details for TankerInbound (broker/invoice/vendor/
-- oil-RM/tanker-no/PO/dispatch-officer) has a real column to land in but no
-- config telling WorkflowProjectionListener to write it there — it was
-- silently dropped. Same field_map_json as the other 3 workflows.
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.TankerInbound', 'dispatch_vehicle_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  '{"ownership_type": ["ownershipType", "ownership_type"], "vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverContact"], "broker_name": ["brokerName", "broker_name"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "vendor_name": ["vendorName", "vendor_name"], "oil_rm_item": ["oilRmItem", "oil_rm_item"], "tanker_no": ["tankerNo", "tanker_no"], "po_verified": ["poVerified", "po_verified"], "po_override_reason": ["poOverrideReason", "po_override_reason"], "dispatch_officer_name": ["dispatchOfficerName", "dispatch_officer_name"], "gp_number": ["gp_number", "gpNumber"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.TankerInbound' AND target_table = 'dispatch_vehicle_detail'
);

-- aria:qc-approval — add an Oil Sample Recovered confirmation column, plus
-- qc_decision to capture the literal Approved/Approved-with-notes/Rejected
-- value. (That value was never actually saved anywhere before, for ANY M10
-- workflow — the page's field is named "status" but every sibling's
-- gate_pass.qc_status mapping expects aliases "qcStatus"/"qc_status", so it
-- never matched; only the resulting state name (QC_APPROVED/QC_REJECTED)
-- landed, via state_columns, which can't tell APPROVED from APPROVED_NOTES.
-- Fixing that mismatch for every workflow is out of scope here — this only
-- adds a dedicated, correctly-aliased column for TankerInbound.
--
-- qc_decision is deliberately NOT in the field_map_json below — "status" is
-- reused by aria:manager-qty-approval / aria:manager-weight-approval later
-- in this same workflow, and WorkflowProjectionListener's generic pluck()
-- searches every page's submitted data for a bare key match (undefined
-- HashMap iteration order), so it could grab a later manager-approval's
-- "status" instead of aria:qc-approval's own. QcApprovalDecisionListener.java
-- (new, mirrors PmtSharedReasonListener's precise-pageCode-lookup pattern)
-- writes qc_decision directly from aria:qc-approval's own bag entry instead.
ALTER TABLE qc_preload_detail
  ADD COLUMN IF NOT EXISTS oil_sample_recovered VARCHAR(8) DEFAULT NULL AFTER qc_reject_reason,
  ADD COLUMN IF NOT EXISTS qc_decision VARCHAR(24) DEFAULT NULL AFTER oil_sample_recovered;

-- M10.TankerInbound was also missing an m0_workflow_projection row for
-- qc_preload_detail (FGLoading/PackLoading/TankerOutbound all have one —
-- ids 22/23/24 — mapping loading_supervisor/loading_tank_location/
-- qc_reject_reason). Reused here for unloading_supervisor_name ->
-- loading_supervisor (same column, different page-side label —
-- TankerInbound calls it "unloading", the table doesn't care) and
-- oil_sample_recovered -> the new column just added. loading_tank_location /
-- qc_reject_reason / qc_decision are left unmapped here — qc_decision is
-- handled by the dedicated listener above, the other two aren't requested
-- this round.
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.TankerInbound', 'qc_preload_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  '{"loading_supervisor": ["unloadingSupervisorName", "unloading_supervisor_name"], "oil_sample_recovered": ["oilSampleRecovered", "oil_sample_recovered"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.TankerInbound' AND target_table = 'qc_preload_detail'
);

-- aria:qc-approval: capture flow_variant_code (like aria:dispatch-vehicle-details
-- already does) so the two new TankerInbound-only fields can gate on it, and
-- add those fields (Oil Sample Recovered, Unloading Supervisor Name) to the
-- Photo + Seal section. seal_unlock_no and the Approved/Rejected decision
-- already exist and are shared by every item category, unchanged.
UPDATE page_version pv
  JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = '{"kind": "layout", "schemaVersion": 2, "slug": "aria:qc-approval", "title": "QC Approval", "status": "published", "version": 1, "roles": ["QC", "Manager", "TenantAdmin"], "dataSources": [], "streams": [], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Sample Identification", "span": 12, "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "gp_number", "label": "Gate Pass # (scan / pick / type)", "type": "select", "required": true, "scannable": true, "freeSolo": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "GatePass", "joins": [{"entity": "WorkflowInstance", "as": "wi", "on": "gate_pass.workflow_instance_id = wi.id"}], "filter": {"and": [{"field": "wi.assigned_role", "op": "eq", "value": "QC"}, {"field": "wi.status", "op": "eq", "value": "OPEN"}]}, "select": ["id", "gp_number"], "sort": [{"field": "gp_number", "dir": "desc"}]}, "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"item_category": "item_category", "flow_variant_code": "flow_variant_code", "instanceId": "workflow_instance_id"}}, {"name": "item_category", "label": "Item Category (auto-fill from dispatch entry)", "type": "select", "required": true, "options": [{"label": "RM Oil — bottled / canned / drum", "value": "RM_OIL"}, {"label": "RM Other — jaggery / agarbatti raw", "value": "RM_OTHER"}, {"label": "Packing — cartons / cases", "value": "PACK_CARTON"}, {"label": "Packing — bottles / cans", "value": "PACK_BOTTLE"}, {"label": "Packing — caps / labels", "value": "PACK_SMALL"}, {"label": "FG — packed for dispatch", "value": "FG"}, {"label": "Return Stock", "value": "RETURN"}]}, {"name": "sample_jar_id", "label": "Sample / Sample Lot ID", "type": "text", "required": true, "placeholder": "SAMPLE-2026-0617"}, {"name": "qc_officer_id", "label": "QC Officer (auto-fill)", "type": "text", "readOnly": true}, {"name": "flow_variant_code", "label": "Flow Variant", "type": "text", "readOnly": true}]}}]}, {"kind": "Section", "title": "Oil Parameters (only if item category = RM Oil / FG)", "span": 12, "subtitle": "Skip this section if the receipt is packing material / return / non-oil.", "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "density_kgl", "label": "Density (kg/L) — spec 0.910–0.930 for groundnut", "type": "number"}, {"name": "ffa_pct", "label": "Free Fatty Acid (%) — spec ≤ 0.20", "type": "number"}, {"name": "colour_rating", "label": "Colour vs reference", "type": "select", "options": [{"label": "Golden / Pale yellow — Pass", "value": "PASS_GOLDEN"}, {"label": "Light orange — Pass", "value": "PASS_ORANGE"}, {"label": "Dark brown — Fail", "value": "FAIL_DARK"}, {"label": "Cloudy — Fail", "value": "FAIL_CLOUDY"}]}, {"name": "temperature_c", "label": "Temperature (°C) — spec ≤ 35", "type": "number"}]}}]}, {"kind": "Section", "title": "Packing-Material Parameters (only if Packing)", "span": 12, "subtitle": "Skip if not packing material.", "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "print_quality", "label": "Print quality (labels / cartons)", "type": "select", "options": ["PASS", "PASS_MINOR", "FAIL_PRINT"]}, {"name": "dimensional_check", "label": "Dimensions match spec? (±2 mm)", "type": "select", "options": ["PASS", "MARGINAL", "FAIL"]}, {"name": "bottle_integrity", "label": "Bottle integrity — burst pressure / cap fit", "type": "select", "options": ["PASS", "PARTIAL", "FAIL_CRACK", "FAIL_CAP_FIT"]}, {"name": "carton_strength", "label": "Carton strength — drop test", "type": "select", "options": ["PASS", "MARGINAL", "FAIL"]}]}}]}, {"kind": "Section", "title": "FG / Return Inspection (only if FG / Return)", "span": 12, "subtitle": "Skip if not FG or return.", "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "seal_intact", "label": "Seal intact?", "type": "select", "options": ["YES", "BROKEN", "MISSING"]}, {"name": "label_match", "label": "Label matches SKU master?", "type": "select", "options": ["YES", "BATCH_MISMATCH", "EXPIRY_MISMATCH", "MRP_MISMATCH"]}, {"name": "batch_expiry_check", "label": "Within shelf life?", "type": "select", "options": ["YES", "NEAR_EXPIRY", "EXPIRED"]}]}}]}, {"kind": "Section", "title": "Photo + Seal", "span": 12, "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "sample_photo", "label": "Sample Photo", "type": "file", "accept": "image/*"}, {"name": "seal_unlock_no", "label": "Seal Unlock #", "type": "text"}, {"name": "oil_sample_recovered", "label": "Oil Sample Recovered (after seal lock)", "type": "select", "options": ["YES", "NO"], "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'", "requiredWhen": "#form.flow_variant_code == \'M10.TankerInbound\'"}, {"name": "unloading_supervisor_name", "label": "Unloading Supervisor Name", "type": "select", "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'", "requiredWhen": "#form.flow_variant_code == \'M10.TankerInbound\'", "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "Employee", "select": ["id", "employeename"], "filter": {"and": [{"field": "employeetype", "op": "eq", "value": "Supervisor"}, {"field": "status", "op": "eq", "value": "A"}]}, "sort": [{"field": "employeename", "dir": "asc"}]}, "valueField": "id", "labelField": "employeename"}}, {"name": "qc_notes", "label": "Notes", "type": "textarea", "rows": 3}]}}]}, {"kind": "Section", "title": "Decision", "span": 12, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "status", "label": "QC Decision", "type": "select", "required": true, "options": [{"label": "✓ APPROVED — all parameters pass", "value": "APPROVED"}, {"label": "✓ APPROVED with notes (managed exception)", "value": "APPROVED_NOTES"}, {"label": "✗ REJECTED — see notes / debit vendor", "value": "REJECTED"}]}]}}, {"kind": "WorkflowActionButton", "action": "workflow.transition", "label": "Submit QC Decision", "variant": "primary"}]}]}]}'
 WHERE pd.code = 'aria:qc-approval' AND pv.status = 'PUBLISHED';

-- aria:weighment-capture: add a Seal Lock Status (YES/NO) confirmation,
-- visible for M10.TankerInbound on the first and second weighment only (not
-- the third/recheck). Named seal_lock_status_yn, NOT seal_lock_status — that
-- name is already taken on this page by an AUTO_LOCK/SEAL_NO_UPDATE dropdown
-- gated on M10.TankerOutbound + SECOND, which maps to a different table
-- (sealing_process_detail, and only for FGLoading/PackLoading — TankerOutbound
-- has no projection row for it either, so that field silently saves nowhere
-- even once visible). That field was ALSO effectively dead until now: its
-- visibleWhen checks #form.flow_variant_code_hidden, but flow_variant_code_hidden
-- was never populated — gp_number's onChangeFill maps vehicle_no/instanceId/
-- w1_prev_display/w2_prev_display but not flow_variant_code_hidden. Fixed
-- below (added to onChangeFill) since our new field depends on the same
-- mechanism — this incidentally also makes the pre-existing TankerOutbound
-- field visible again, though its missing sealing_process_detail projection
-- row for TankerOutbound is a separate, pre-existing gap left untouched.
ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS seal_lock_status VARCHAR(8) DEFAULT NULL AFTER w3_weight_kg;

-- Found while chasing why weighment_capture_detail was empty on a fresh
-- environment: none of the 4 m0_workflow_projection rows for it
-- (FGLoading/PackLoading/TankerInbound/TankerOutbound) exist in ANY
-- migration — they only ever existed as manual/ad-hoc rows on this dev
-- machine's local DB, so a server migrated purely from Flyway history (or
-- any fresh clone) never gets them, and W1/W2/W3 weights silently go
-- nowhere for every M10 workflow, not just TankerInbound. Recreating them
-- here (idempotent, NOT EXISTS-guarded — no-ops on this machine, fixes
-- everywhere else) before the seal_lock_status JSON_SET below, which
-- otherwise assumes the TankerInbound row already exists and would
-- silently update 0 rows on an environment missing it.
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
SELECT -1, w.code, 'weighment_capture_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  '{"w1_weight_kg": ["w1_weight_kg", "w1WeightKg", "grossKg"], "w2_weight_kg": ["w2_weight_kg", "w2WeightKg"], "w3_weight_kg": ["w3_weight_kg", "w3WeightKg"], "weight_officer": ["weightOfficer", "weight_officer"], "weight_entry_at": ["weightEntryAt", "weight_entry_at"]}'
FROM (SELECT 'M10.FGLoading' code UNION ALL SELECT 'M10.PackLoading' UNION ALL SELECT 'M10.TankerInbound' UNION ALL SELECT 'M10.TankerOutbound') w
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = w.code AND target_table = 'weighment_capture_detail'
);

UPDATE m0_workflow_projection
   SET field_map_json = JSON_SET(field_map_json, '$.seal_lock_status', JSON_ARRAY('sealLockStatusYn', 'seal_lock_status_yn'))
 WHERE tenant_id = -1 AND workflow_code = 'M10.TankerInbound' AND target_table = 'weighment_capture_detail';

UPDATE page_version pv
  JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = '{"kind": "layout", "schemaVersion": 2, "slug": "aria:weighment-capture", "title": "Weighment Capture", "status": "published", "version": 1, "roles": ["WeighmentOfficer", "Manager", "TenantAdmin"], "dataSources": [], "streams": [], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Vehicle & Weighbridge", "span": 12, "subtitle": "One weighment per direction. Loaded weight on entry, empty on exit; difference = quantity received/dispatched.", "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "gp_number", "label": "Gate Pass # (scan / pick / type)", "type": "select", "required": true, "scannable": true, "freeSolo": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "GatePass", "joins": [{"entity": "WorkflowInstance", "as": "wi", "on": "gate_pass.workflow_instance_id = wi.id"}], "filter": {"and": [{"field": "wi.assigned_role", "op": "eq", "value": "WeighmentOfficer"}, {"field": "wi.status", "op": "eq", "value": "OPEN"}]}, "select": ["id", "gp_number"], "sort": [{"field": "gp_number", "dir": "desc"}]}, "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "instanceId": "workflow_instance_id", "w1_prev_display": "w1_weight_kg", "w2_prev_display": "w2_weight_kg", "flow_variant_code_hidden": "flow_variant_code"}}, {"name": "vehicle_no", "label": "Vehicle (auto-fill)", "type": "text", "readOnly": true}, {"name": "weighbridge_code", "label": "Weighbridge", "type": "text", "required": true, "readOnly": true, "value": "WB-1 (Main Gate, 60T)"}, {"name": "weighment_date", "label": "Date", "type": "date", "required": true}, {"name": "entry_time", "label": "Entry Time", "type": "time", "required": true}, {"name": "weight_officer", "label": "Weighment Officer Name", "type": "text", "required": true}]}}]}, {"kind": "Section", "title": "Reading", "span": 12, "subtitle": "Live feed from the weighbridge controller. Press Capture to lock the reading.", "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "weight_seq", "label": "Weighment", "type": "select", "required": true, "options": [{"label": "First (loaded / gross)", "value": "FIRST"}, {"label": "Second (empty / tare)", "value": "SECOND"}, {"label": "Third (re-check)", "value": "THIRD"}]}, {"kind": "FormField", "name": "w1_weight_kg", "label": "First Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == \'FIRST\'"}, {"kind": "FormField", "name": "w1_prev_display", "label": "First Weight (kg)", "type": "number", "readOnly": true, "visibleWhen": "#form.weight_seq == \'SECOND\' || #form.weight_seq == \'THIRD\'"}, {"kind": "FormField", "name": "w2_weight_kg", "label": "Second Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == \'SECOND\'"}, {"kind": "FormField", "name": "w2_prev_display", "label": "Second Weight (kg)", "type": "number", "readOnly": true, "visibleWhen": "#form.weight_seq == \'THIRD\'"}, {"kind": "FormField", "name": "w3_weight_kg", "label": "Third Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == \'THIRD\'"}, {"kind": "FormField", "name": "seal_lock_status", "label": "Seal Lock Status (2nd tanker weight)", "type": "select", "options": [{"label": "Auto Lock", "value": "AUTO_LOCK"}, {"label": "Seal No Update", "value": "SEAL_NO_UPDATE"}], "visibleWhen": "#form.weight_seq == \'SECOND\' && #form.flow_variant_code_hidden == \'M10.TankerOutbound\'"}, {"kind": "FormField", "name": "seal_lock_status_yn", "label": "Seal Lock Status", "type": "select", "options": ["YES", "NO"], "visibleWhen": "(#form.weight_seq == \'FIRST\' || #form.weight_seq == \'SECOND\') && #form.flow_variant_code_hidden == \'M10.TankerInbound\'", "requiredWhen": "(#form.weight_seq == \'FIRST\' || #form.weight_seq == \'SECOND\') && #form.flow_variant_code_hidden == \'M10.TankerInbound\'"}, {"name": "live_reading_display", "label": "Live Reading", "type": "text", "readOnly": true, "helperText": "Locked while stabilising — wait for the green tick on the controller"}, {"name": "gross_kg", "label": "Gross Weight (kg)", "type": "number", "required": true}, {"name": "tare_kg", "label": "Tare (kg) — from vehicle master or this empty reading", "type": "number"}, {"name": "net_kg", "label": "Net (kg) — auto Gross − Tare", "type": "number", "readOnly": true}, {"name": "expected_kg", "label": "Expected (kg) — from PO / SO", "type": "number", "readOnly": true}, {"name": "variance_kg", "label": "Variance (kg) — auto", "type": "number", "readOnly": true}, {"name": "within_tolerance", "label": "Within Tolerance?", "type": "select", "required": true, "options": [{"label": "YES — proceed", "value": "YES"}, {"label": "NO — route to manager", "value": "NO"}]}, {"kind": "FormField", "name": "flow_variant_code_hidden", "label": "", "type": "text", "readOnly": true}]}}]}, {"kind": "Section", "title": "Photo Evidence", "span": 12, "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "wb_display_photo", "label": "Weighbridge Display Photo", "type": "file", "accept": "image/*"}, {"name": "vehicle_photo", "label": "Vehicle on Bridge Photo", "type": "file", "accept": "image/*"}, {"name": "notes", "label": "Notes (any anomaly)", "type": "textarea", "rows": 2}]}}]}, {"kind": "Section", "span": 12, "children": [{"kind": "WorkflowActionButton", "action": "workflow.transition", "label": "Capture & Save Weighment", "variant": "primary"}]}]}]}'
 WHERE pd.code = 'aria:weighment-capture' AND pv.status = 'PUBLISHED';


-- aria:supervisor-unloading (WEIGHMENT_1 -> UNLOADING_DONE for M10.TankerInbound):
-- this page was built for discrete-unit goods receipt (cartons/bottles/drums
-- counted by SKU into a bin/godown) — none of that fits a bulk-liquid tanker
-- unload. Added flow_variant_code capture (same onChangeFill pattern as the
-- other pages) and gated off, for M10.TankerInbound: Receipt Type + Reference
-- (Section 1 — its 4 options don't include a tanker/bulk-oil case), the
-- entire Goods Receipt / Count-by-Item section (item/UOM/batch/expiry —
-- there's no SKU to count), the entire Bin/Godown Allocation section (bulk
-- oil goes into a tank, not a bin/rack), the entire Damage/Short-Receipt
-- section (built for damaged cartons/bottles), and "Create GRN now?" (GRN is
-- for discrete inventory receipt, not bulk liquid). Left visible for every
-- flow: Vehicle/Gate Pass, Supervisor, Unloading Start/Finish, and Seal #
-- broken on unload — those already match what TankerInbound actually needs.
-- Note: start_ts/finish_ts/supervisor_id/seal_unlock_no still have nowhere to
-- save for TankerInbound (no m0_workflow_projection field_map covers this
-- page's fields) — flagged, not fixed here; not requested this round.
UPDATE page_version pv
  JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = '{"kind": "layout","schemaVersion": 2,"slug": "aria:supervisor-unloading","title": "Unloading Supervisor — Goods Receipt","status": "published","version": 1,"roles": ["UnloadingSupervisor","StoreKeeper","Manager","TenantAdmin"],"dataSources": [],"streams": [],"layout": [{"kind": "Grid","columns": 12,"gap": 16,"children": [{"kind": "Section","title": "Vehicle & Reference","span": 12,"subtitle": "Auto-fills from the gate pass assigned to your queue. Confirm before counting goods.","children": [{"kind": "Form","submitLabel": "","bindings": {"fields": [{"name": "gp_number","label": "Gate Pass # (scan / pick / type)","type": "select","required": true,"scannable": true,"freeSolo": true,"optionsSource": {"type": "datasource","spec": {"type": "entity","entity": "GatePass","joins": [{"entity": "WorkflowInstance","as": "wi","on": "gate_pass.workflow_instance_id = wi.id"}],"filter": {"and": [{"field": "wi.assigned_role","op": "eq","value": "UnloadingSupervisor"},{"field": "wi.status","op": "eq","value": "OPEN"}]},"select": ["id","gp_number"],"sort": [{"field": "gp_number","dir": "desc"}]},"valueField": "gp_number","labelField": "gp_number"},"onChangeFill": {"vehicle_no": "vehicle_no","reference_no": "reference_no","instanceId": "workflow_instance_id","flow_variant_code": "flow_variant_code"}},{"name": "vehicle_no","label": "Vehicle","type": "text","readOnly": true},{"name": "flow_variant_code","label": "Flow Variant","type": "text","readOnly": true},{"name": "flow_kind","label": "Receipt Type","type": "select","options": [{"label": "Raw Material — bottled / canned / drum (inbound from vendor)","value": "RM_INBOUND"},{"label": "Packing Material — cartons / bottles / caps / labels (inbound)","value": "PACKING_INBOUND"},{"label": "Return Stock — FG returned by customer","value": "RETURN_INBOUND"},{"label": "Inter-Unit Transfer In","value": "INTER_UNIT_IN"}],"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'","requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "reference_no","label": "Reference (PO# / GRN# / RN#)","type": "text","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'","requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}]}}]},{"kind": "Section","title": "Goods Receipt — Count by Item","span": 12,"subtitle": "Verify item code, count cases physically as you unload. Repeat the block for each item in the truck.","children": [{"kind": "Form","submitLabel": "","bindings": {"fields": [{"name": "item_code","label": "Item / SKU Code","type": "text","placeholder": "GIN-1L-IDH","helperText": "Scan barcode or pick from SKU master","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'","requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "item_description","label": "Item Name (auto-fill)","type": "text","readOnly": true,"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "uom","label": "UOM","type": "select","options": [{"label": "Cases","value": "CASE"},{"label": "Bottles","value": "BOTTLE"},{"label": "Bundles","value": "BUNDLE"},{"label": "Pallets","value": "PALLET"},{"label": "Bags","value": "BAG"},{"label": "Pieces","value": "NOS"},{"label": "Drums","value": "DRUM"},{"label": "Kilograms","value": "KG"},{"label": "Litres","value": "L"}],"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'","requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "received_qty","label": "Quantity Received","type": "number","helperText": "Counted physically as cases / bottles came off the truck","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'","requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "invoice_qty","label": "Invoice Qty (from PO / vendor invoice)","type": "number","helperText": "System flags variance if Received ≠ Invoice","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "batch_no","label": "Batch / Lot Number","type": "text","placeholder": "B26-0421-A","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "mfg_date","label": "Manufacturing Date","type": "date","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "expiry_date","label": "Expiry / Best-Before Date","type": "date","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}]}}]},{"kind": "Section","title": "Bin / Godown Allocation","span": 12,"subtitle": "Pick the destination bin/godown. Stock balance updates immediately on confirmation.","children": [{"kind": "Form","submitLabel": "","bindings": {"fields": [{"name": "godown_code","label": "Godown","type": "select","options": [{"label": "G1 — Main Stores (free 62%)","value": "G1"},{"label": "G2 — Packing Material Stores (free 41%)","value": "G2"},{"label": "G3 — Returns / Quarantine (free 88%)","value": "G3"},{"label": "G4 — FG Stores (free 35%)","value": "G4"}],"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'","requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "bin_code","label": "Bin / Rack","type": "select","options": [{"label": "A-01 (free)","value": "A-01"},{"label": "A-02 (partial — 30%)","value": "A-02"},{"label": "A-03 (free)","value": "A-03"},{"label": "B-01 (full — assign next)","value": "B-01"},{"label": "B-02 (free)","value": "B-02"},{"label": "C-01 (free)","value": "C-01"},{"label": "C-02 (free)","value": "C-02"}],"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'","requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "stack_pattern","label": "Stacking Pattern","type": "select","options": [{"label": "Standard pallet (4 cases / layer × 5 layers)","value": "STD_PALLET"},{"label": "Bin stack (max 8 layers)","value": "BIN_STACK"},{"label": "Single layer (fragile)","value": "SINGLE"}],"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "storage_condition","label": "Storage Condition","type": "select","options": [{"label": "Ambient","value": "AMBIENT"},{"label": "Cool & Dry","value": "COOL_DRY"},{"label": "Quarantine — QC pending","value": "QUARANTINE"}],"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}]}}]},{"kind": "Section","title": "Damage / Short-Receipt Check","span": 12,"subtitle": "Capture damaged or short items separately. They route to QC for vendor debit-note.","children": [{"kind": "Form","submitLabel": "","bindings": {"fields": [{"name": "damaged_qty","label": "Damaged Quantity","type": "number","helperText": "Cases / bottles found damaged on unload. Photo evidence required.","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "damage_reason","label": "Damage Reason","type": "select","options": [{"label": "Transport damage (crushed / dented)","value": "TRANSPORT"},{"label": "Packing leak","value": "LEAK"},{"label": "Seal broken","value": "SEAL_BROKEN"},{"label": "Expired before receipt","value": "EXPIRED"},{"label": "Other (see notes)","value": "OTHER"}],"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "short_qty","label": "Short Quantity (vs invoice)","type": "number","helperText": "Triggers debit-note workflow against vendor","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "damage_photo","label": "Damage / Short-Receipt Photo","type": "file","accept": "image/*","visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"},{"name": "notes","label": "Notes","type": "textarea","rows": 3,"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}]}}]},{"kind": "Section","title": "Confirm Storage Complete","span": 12,"children": [{"kind": "Form","bindings": {"fields": [{"name": "supervisor_id","label": "Supervisor (auto-fill)","type": "text","readOnly": true},{"name": "start_ts","label": "Unloading Start","type": "datetime-local"},{"name": "finish_ts","label": "Unloading Finish","type": "datetime-local"},{"name": "unload_seal_unlock_no","label": "Seal # broken on unload","type": "text"},{"name": "grn_to_create","label": "Create GRN now?","type": "select","options": [{"label": "✓ Yes — auto-create GRN against PO","value": "YES"},{"label": "Later — supervisor will create from GRN page","value": "NO"}],"visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'","requiredWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}]}},{"kind": "WorkflowActionButton","action": "workflow.transition","label": "Close Unloading — Stock Updated","variant": "primary"}]}]}]}'
 WHERE pd.code = 'aria:supervisor-unloading' AND pv.status = 'PUBLISHED';


-- aria:reconciliation (M10.TankerInbound only — WEIGHMENT_2 -> QTY_RECONCILED
-- /QTY_PENDING_MANAGER): previously just gp_number + a free-text variance
-- reason + two decision buttons, no visible numbers to actually decide
-- "within tolerance" from. Joins WeighmentCaptureDetail (keyed by
-- workflow_instance_id, same table W1/W2 already land in from
-- aria:weighment-capture) to prefill First Weight and Second Weight
-- read-only, and adds a Variance field computed client-side from those two
-- (same "computedFrom" SpEL convention V97 already uses for
-- gate_pass.within_tolerance_computed).
UPDATE page_version pv
  JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = '{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Unload Reconciliation", "span": 12, "subtitle": "Reconcile the unloaded quantity against the order. Within tolerance proceeds; a difference goes to the manager.", "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (scan / pick / type)", "type": "select", "required": true, "freeSolo": true, "scannable": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "GatePass", "joins": [{"entity": "WorkflowInstance", "as": "wi", "on": "gate_pass.workflow_instance_id = wi.id"}, {"entity": "WeighmentCaptureDetail", "as": "wcd", "on": "wcd.workflow_instance_id = gate_pass.workflow_instance_id", "fields": ["wcd.w1_weight_kg as w1WeightKg", "wcd.w2_weight_kg as w2WeightKg"]}], "filter": {"and": [{"field": "wi.assigned_role", "op": "eq", "value": "Dispatch"}, {"field": "wi.status", "op": "eq", "value": "OPEN"}]}, "select": ["id", "gp_number"], "sort": [{"field": "gp_number", "dir": "desc"}]}, "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "instanceId": "workflow_instance_id", "flow_variant_code": "flow_variant_code", "first_weight_kg": "w1WeightKg", "second_weight_kg": "w2WeightKg"}}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "first_weight_kg", "label": "First Weight (kg)", "type": "number", "readOnly": true}, {"name": "second_weight_kg", "label": "Second Weight (kg)", "type": "number", "readOnly": true}, {"name": "variance_display_kg", "label": "Variance (kg) — auto", "type": "number", "readOnly": true, "computedFrom": "#form.first_weight_kg - #form.second_weight_kg"}, {"name": "variance_reason", "label": "Variance reason (if any)", "type": "textarea"}]}}, {"kind": "Stack", "direction": "row", "gap": 12, "children": [{"kind": "WorkflowActionButton", "label": "✓ Reconciled — Within Tolerance", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "color": "primary", "submitValues": {"withinTolerance": "YES"}}, {"kind": "WorkflowActionButton", "label": "⚠ Difference → Manager", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "color": "error", "submitValues": {"withinTolerance": "NO"}}]}]}]}]}'
 WHERE pd.code = 'aria:reconciliation' AND pv.status = 'PUBLISHED';


-- aria:supervisor-unloading gets its OWN dedicated table (not a bolt-on to
-- weighment_capture_detail), per request. supervisor_id/start_ts/finish_ts
-- have no cross-page name collision within M10.TankerInbound, so they're
-- mapped in the generic field_map_json below. seal_unlock_no was already
-- renamed to unload_seal_unlock_no on the page above, since aria:qc-approval
-- (earlier in this same flow) has its OWN field literally named
-- "seal_unlock_no" in the same workflow instance's bag, and
-- WorkflowProjectionListener.pluck() searches every page's submitted data for
-- a bare key match (undefined HashMap iteration order) — same class of bug
-- as qc_decision earlier.
CREATE TABLE IF NOT EXISTS tanker_unloading_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  unloading_supervisor_id VARCHAR(128) DEFAULT NULL,
  unloading_start_ts DATETIME DEFAULT NULL,
  unloading_finish_ts DATETIME DEFAULT NULL,
  unloading_seal_unlock_no VARCHAR(32) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_tud_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'TankerUnloadingDetail', 'Tanker Unloading Detail', 'tanker_unloading_detail',
       'Unloading supervisor/start-finish/seal capture for aria:supervisor-unloading (M10.TankerInbound).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='TankerUnloadingDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'TankerUnloadingDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'unloading_supervisor_id' fc,'Supervisor' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'unloading_start_ts','Unloading Start','DATETIME',1,1,0,'datetime'
      UNION ALL SELECT 'unloading_finish_ts','Unloading Finish','DATETIME',1,1,0,'datetime'
      UNION ALL SELECT 'unloading_seal_unlock_no','Seal Unlock #','STRING',1,1,1,'text') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='TankerUnloadingDetail' AND field_code=c.fc);

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.TankerInbound', 'tanker_unloading_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  '{"unloading_supervisor_id": ["supervisor_id"], "unloading_start_ts": ["start_ts"], "unloading_finish_ts": ["finish_ts"], "unloading_seal_unlock_no": ["unload_seal_unlock_no"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.TankerInbound' AND target_table = 'tanker_unloading_detail'
);

-- aria:reconciliation gets its OWN dedicated table too. variance_reason and
-- the withinTolerance decision are NOT put in the generic field_map_json —
-- "variance_reason" is reused later in this same workflow by
-- aria:manager-qty-approval and aria:manager-weight-approval (confirmed:
-- WEIGHMENT_2 isn't one of the previousStates PmtSharedReasonListener already
-- intercepts for TankerInbound, so the generic mapping WOULD apply, but
-- pluck()'s cross-page search would risk grabbing one of those later pages'
-- variance_reason instead once they've also fired).
-- ReconciliationDecisionListener.java (new) reads aria:reconciliation's own
-- bag entry directly instead, gated on previousState == WEIGHMENT_2, same
-- precise-lookup pattern as QcApprovalDecisionListener — and upserts into
-- this new table (unlike gate_pass, which always pre-exists, this row won't
-- exist yet the first time, hence ON DUPLICATE KEY UPDATE keyed on
-- tenant_id+workflow_instance_id).
CREATE TABLE IF NOT EXISTS unload_reconciliation_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  variance_reason VARCHAR(512) DEFAULT NULL,
  within_tolerance VARCHAR(24) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_urd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'UnloadReconciliationDetail', 'Unload Reconciliation Detail', 'unload_reconciliation_detail',
       'Variance reason + within-tolerance decision for aria:reconciliation (M10.TankerInbound).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='UnloadReconciliationDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'UnloadReconciliationDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'variance_reason' fc,'Variance Reason' lbl,'STRING' dt,1 filt,1 sort,1 srch,'textarea' wk
      UNION ALL SELECT 'within_tolerance','Within Tolerance','STRING',1,1,0,'select') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='UnloadReconciliationDetail' AND field_code=c.fc);
