-- =====================================================================
-- V7_8 — Extend M10.TankerInbound workflow from 4 → 7 steps.
--
-- New states (in addition to DRAFT / GATE_PASS_CREATED / PARKING_IN /
-- DISPATCH_QUEUED / QC_APPROVED / QC_REJECTED):
--   - WEIGHMENT_LOADED      (after weighment seq=1 captured)
--   - UNLOADING_DONE        (after supervisor closes unloading)
--   - CLOSED                (terminal — gate pass exit-stamped)
--
-- New transitions:
--   QC_APPROVED      → WEIGHMENT_LOADED   (pico:weighment-capture, WeighmentOfficer)
--   WEIGHMENT_LOADED → UNLOADING_DONE     (pico:supervisor-unloading, UnloadingSupervisor)
--   UNLOADING_DONE   → CLOSED             (pico:gate-pass-closure, FrontGate)
--
-- New page: pico:gate-pass-closure — FrontGate exit-stamps the GP.
--
-- The workflow_instance table doesn't pin version; the engine reads
-- body_json fresh on every transition. Existing instances stuck mid-flow
-- can continue under the new body if their current_state still exists
-- in the new states[] (all old states are preserved).
-- =====================================================================

-- 1. UPDATE the WDL body to v2-shape (still version=1 — engine doesn't pin) -
UPDATE workflow_definition
SET body_json = '{"code":"M10.TankerInbound","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT"},{"code":"GATE_PASS_CREATED","assignedRole":"ParkingSecurity"},{"code":"PARKING_IN","assignedRole":"Dispatch"},{"code":"DISPATCH_QUEUED","assignedRole":"QC"},{"code":"QC_APPROVED","assignedRole":"WeighmentOfficer"},{"code":"QC_REJECTED","terminal":true},{"code":"WEIGHMENT_LOADED","assignedRole":"UnloadingSupervisor"},{"code":"UNLOADING_DONE","assignedRole":"FrontGate"},{"code":"CLOSED","terminal":true}],"transitions":[{"from":"DRAFT","to":"GATE_PASS_CREATED","trigger":"page.submitted","pageCode":"pico:front-gate-create"},{"from":"GATE_PASS_CREATED","to":"PARKING_IN","trigger":"page.submitted","pageCode":"pico:parking-scan"},{"from":"PARKING_IN","to":"DISPATCH_QUEUED","trigger":"page.submitted","pageCode":"pico:dispatch-vehicle-entry"},{"from":"DISPATCH_QUEUED","to":"QC_APPROVED","trigger":"page.submitted","pageCode":"pico:qc-approval","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"DISPATCH_QUEUED","to":"QC_REJECTED","trigger":"page.submitted","pageCode":"pico:qc-approval","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"QC_APPROVED","to":"WEIGHMENT_LOADED","trigger":"page.submitted","pageCode":"pico:weighment-capture"},{"from":"WEIGHMENT_LOADED","to":"UNLOADING_DONE","trigger":"page.submitted","pageCode":"pico:supervisor-unloading"},{"from":"UNLOADING_DONE","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:gate-pass-closure"}]}'
WHERE tenant_id=1 AND code='M10.TankerInbound' AND version=1;

-- 2. New page: pico:gate-pass-closure (FrontGate exit-stamps the GP) --------
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:gate-pass-closure','Close Gate Pass','Front-gate exit stamp + final verification.','M10.Dispatch','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:gate-pass-closure');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Final Verification","fields":[{"kind":"FormField","type":"text","name":"gpNumber","label":"Gate Pass #","placeholder":"GP-..."},{"kind":"FormField","type":"text","name":"exitReason","label":"Exit Reason","placeholder":"Optional notes"},{"kind":"WorkflowActionButton","label":"Close Gate Pass","action":"workflow.transition","variant":"primary"}]}]}', 'V7_8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:gate-pass-closure' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code='pico:gate-pass-closure' AND current_version_id IS NULL;
