-- V127 — repair pico:front-gate-create: restore the operator create-form.
--
-- The page had regressed to an inbox-only list (kind:list, entity GatePass, 2
-- columns) with no way to actually CREATE a gate pass — the Front Gate operator
-- could not start the tanker-inbound flow. Restore a guided intake FORM
-- (vehicleNo / driverName / driverPhone / sourceParty) whose "Create Gate Pass"
-- WorkflowActionButton starts M10.TankerInbound (DRAFT -> GATE_PASS_CREATED),
-- plus a WorkflowQueueList showing the operator's in-progress vehicles.
--
-- Grandfathered page (pre-CUSTOMIZATION-POLICY, seeded in V3_2/V40) — this is a
-- REPAIR of an existing Pico page, mirroring V40's UPDATE pattern, not a new
-- customer-scoped page.
--
-- NOTE: PageRendererService caches published bodies in Redis (m0:page:<tenant>:<code>,
-- 1h TTL). A running instance must evict that key (publish flow / restart) to
-- serve the new body; a fresh DB has no cache entry so this applies cleanly.

UPDATE page_version pv
SET pv.body_json = '{"schemaVersion": "1.0.0", "slug": "pico:front-gate-create", "title": "Front Gate \\u2014 Tanker Intake", "status": "published", "version": 1, "roles": ["FrontGate", "TenantAdmin"], "dataSources": [], "streams": [], "layout": {"type": "Grid", "id": "root", "props": {"columns": 12, "gap": 16}, "children": [{"type": "Section", "id": "sec-intake", "props": {"title": "New Vehicle Intake", "span": 12, "subtitle": "Capture the vehicle + driver at the gate, then Create Gate Pass to start the tanker-inbound flow."}, "children": [{"type": "Form", "id": "form-intake", "props": {"submitLabel": ""}, "bindings": {"fields": [{"name": "vehicleNo", "label": "Vehicle No", "type": "text", "required": true, "placeholder": "TN-46-CJ-0001"}, {"name": "driverName", "label": "Driver Name", "type": "text", "required": true, "placeholder": "e.g. Suresh Kumar"}, {"name": "driverPhone", "label": "Driver Phone", "type": "tel", "placeholder": "+91 90000 00000"}, {"name": "sourceParty", "label": "Coming From (Vendor)", "type": "text", "placeholder": "e.g. ABC Edible Oil"}]}, "children": [{"type": "WorkflowActionButton", "id": "btn-create", "props": {"label": "Create Gate Pass", "action": "workflow.transition", "workflowCode": "M10.TankerInbound", "targetState": "GATE_PASS_CREATED", "trigger": "page.submitted", "color": "primary"}}]}]}, {"type": "Section", "id": "sec-queue", "props": {"title": "Gate Passes In Progress", "span": 12, "subtitle": "Vehicles you have logged that are moving through parking, weighment and QC."}, "children": [{"type": "WorkflowQueueList", "id": "queue-frontgate", "props": {"persona": "frontgate", "pageSize": 25, "cardFields": ["vehicleNo", "driverName", "gpNumber", "currentState"]}}]}]}}'
WHERE pv.page_id = (SELECT id FROM page_definition WHERE code = 'pico:front-gate-create')
  AND pv.status = 'PUBLISHED';
