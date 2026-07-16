-- =====================================================================
-- V136 — pico:front-gate-create: Loading Type, Vehicle Category, a
-- Flow-Variant dropdown that now DRIVES which workflow starts, a vehicle
-- number spacing warning, and a real QR + print template for gate pass.
--
-- Confirmed before writing this: all 6 M10 gate-pass workflows
-- (TankerInbound, PackUnload, ReturnStockIn, TankerOutbound, PackLoading,
-- FGLoading) share the same first transition DRAFT -> GATE_PASS_CREATED
-- (checked V87 lines 48/51/54, V7_13 lines 9/21) — so only workflowCode
-- needs to become dynamic; targetState stays fixed.
--
-- NOTE — page-body keys below that are THIS migration's own convention,
-- not confirmed elsewhere in this repo (same caveat as V134/V135 — verify
-- against the frontend Renderer, adjust key names if it expects different
-- ones):
--   1) WorkflowActionButton "workflowCode":"{{form.flowVariant}}" —
--      templating a start-workflow button from a form field. The
--      "{{row.x}}" template syntax itself IS confirmed (V5_2 detail page
--      header title), but not for this specific prop.
--   2) FormField "warnWhen"/"warnMessage" (mirrors the confirmed
--      visibleWhen/requiredWhen convention, but for a soft warning
--      rather than show/hide).
-- =====================================================================

-- 1) WorkflowDefinition as a queryable entity (for the Flow Variant
--    dropdown's optionsSource) — same "entity" options_source_json shape
--    as V135's GatePass dropdown.
INSERT INTO entity_registry (code, display_name, table_name, description, category)
  SELECT 'WorkflowDefinition','Workflow Definition','workflow_definition','M10 gate-pass workflow variants (Tanker/Pack/FG, in/out).','M0'
  WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='WorkflowDefinition');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'WorkflowDefinition','code','Code','STRING','text',1,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='WorkflowDefinition' AND field_code='code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'WorkflowDefinition','status','Status','STRING','text',0,1,1,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='WorkflowDefinition' AND field_code='status');

-- 2) print_template for gate_pass — real QR (PrintController now
--    rasterises spec.showQr into an actual scannable PNG, not text).
INSERT INTO print_template (tenant_id, code, name, output_kind, target_table, spec_json)
  SELECT 1,'gate-pass','Gate Pass','html','gate_pass',
    '{"documentLabel":"GATE PASS","numberColumn":"gp_number","showQr":true,"rows":[{"label":"Vehicle No","column":"vehicle_no"},{"label":"Flow Variant","column":"flow_variant_code"},{"label":"Loading Type","column":"loading_type"},{"label":"Vehicle Category","column":"vehicle_category"},{"label":"Status","column":"status"}],"signatures":["Front Gate Officer","Driver"]}'
  WHERE NOT EXISTS (SELECT 1 FROM print_template WHERE tenant_id=1 AND code='gate-pass');

-- 3) pico:front-gate-create — variant fields + dynamic workflow start ---
UPDATE page_version pv
SET pv.body_json = '{"schemaVersion": "1.0.0", "slug": "pico:front-gate-create", "title": "Front Gate \\u2014 Live Inward / Outward Gatepass Details", "status": "published", "version": 1, "roles": ["FrontGate", "TenantAdmin"], "dataSources": [], "streams": [], "layout": {"type": "Grid", "id": "root", "props": {"columns": 12, "gap": 16}, "children": [{"type": "Section", "id": "sec-intake", "props": {"title": "New Vehicle Intake", "span": 12, "subtitle": "Capture the vehicle at the gate, then Create Gate Pass to start the selected flow."}, "children": [{"type": "Form", "id": "form-intake", "props": {"submitLabel": ""}, "bindings": {"fields": [{"name": "vehicleNo", "label": "Vehicle No", "type": "text", "required": true, "placeholder": "TN 33 P 8452", "helperText": "Enter with spaces, e.g. TN 33 P 8452", "warnWhen": "!#form.vehicleNo.includes('' '')", "warnMessage": "Vehicle number entered without spaces \\u2014 please use format: TN 33 P 8452"}, {"name": "loadingType", "label": "Loading Type", "type": "select", "required": true, "options": ["LOADING", "UN-LOADING", "RETURN STOCK", "EMPTY", "OTHERS"]}, {"name": "vehicleCategory", "label": "Vehicle Category", "type": "select", "required": true, "options": ["BODY VEHICLE", "TANKER", "OPEN BODY", "CRANE", "JCB", "TRACTOR", "STAFF BUS", "CAR", "TWO-WHEELER", "AUTO"]}, {"name": "flowVariant", "label": "Flow Variant", "type": "select", "required": true, "optionsSource": {"type": "entity", "entity": "WorkflowDefinition", "valueField": "code", "labelField": "code"}}]}, "children": [{"type": "WorkflowActionButton", "id": "btn-create", "props": {"label": "Create Gate Pass", "action": "workflow.transition", "workflowCode": "{{form.flowVariant}}", "targetState": "GATE_PASS_CREATED", "trigger": "page.submitted", "color": "primary"}}]}]}, {"type": "Section", "id": "sec-print", "props": {"title": "Gate Pass QR", "span": 12, "subtitle": "Shown after Create Gate Pass succeeds."}, "children": [{"type": "PrintButton", "id": "btn-print", "props": {"printCode": "gate-pass", "idFrom": "lastCreatedId", "label": "Print Gate Pass"}}]}, {"type": "Section", "id": "sec-queue", "props": {"title": "Gate Passes In Progress", "span": 12, "subtitle": "Vehicles you have logged that are moving through parking, weighment and QC."}, "children": [{"type": "WorkflowQueueList", "id": "queue-frontgate", "props": {"persona": "frontgate", "pageSize": 25, "cardFields": ["vehicleNo", "gpNumber", "flowVariant", "currentState"]}}]}]}}'
WHERE pv.page_id = (SELECT id FROM page_definition WHERE code = 'pico:front-gate-create')
  AND pv.status = 'PUBLISHED';
