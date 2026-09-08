-- V301: Replace aria:front-gate-create's body per explicit user request.
--
-- WARNING (documented at the requester's direction, confirmed after being
-- shown the diff): this REMOVES flow-variant routing from the intake form.
-- The prior body had a "flowVariant" field (filtered by loadingType) that
-- set flow_variant_code on the new Gate Pass, and the submit button used
-- workflow.transition with workflowCode="{{form.flowVariant}}" to start the
-- correct one of the ~7 M10 workflows. The new body has no flowVariant
-- field and submits via the "gatepass.create" action instead. It also
-- drops the PackUnload-only "securityOfficerName" field and the "Gate
-- Passes In Progress" WorkflowQueueList section.
--
-- Net effect: gate passes created through this page after this migration
-- will have no flow_variant_code set unless "gatepass.create" independently
-- derives one server-side. Confirmed explicitly by the requester.
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind":"layout","schemaVersion":2,"slug":"aria:front-gate-create","title":"Front Gate — Vehicle Intake","status":"published","version":1,"roles":["FrontGate","TenantAdmin"],"dataSources":[],"streams":[],"entity":"GatePass","layout":[{"kind":"Grid","columns":12,"gap":16,"children":[{"kind":"Section","title":"New Vehicle Intake","span":12,"subtitle":"Capture the vehicle at the gate, then Create Gate Pass to start the selected flow.","children":[{"kind":"Form","bindings":{"fields":[{"name":"loading_type","label":"Vehicle Movement Type","required":true,"type":"select","optionsSource":{"type":"entity","entity":"LoadingType","valueField":"code","labelField":"display_name"}},{"name":"vehicle_no","label":"Vehicle No","type":"text","required":true,"placeholder":"TN86J6790","uppercase":true,"pattern":"^[A-Z]{2}-?[0-9]{2}-?[A-Z]{1,2}-?[0-9]{4}$","patternError":"Enter valid vehicle number"},{"name":"vehicle_category","label":"Vehicle Category","required":true,"type":"select","optionsSource":{"type":"entity","entity":"VehicleCategory","valueField":"code","labelField":"display_name"}}]},"children":[{"kind":"WorkflowActionButton","label":"Create Gate Pass","action":"gatepass.create","entity":"GatePass","color":"primary","onSuccessOpen":{"page":"aria:gate-pass-slip","lookupEntity":"GatePass","lookupField":"id","param":"id"}}]}]}]}]}'
WHERE pd.code = 'aria:front-gate-create' AND pv.status = 'PUBLISHED';
