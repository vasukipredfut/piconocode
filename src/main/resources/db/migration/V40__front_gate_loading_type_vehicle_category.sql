-- V38 — Front-Gate Intake form: drop Driver Name / Driver Phone / Coming From,
-- add Loading Type + Vehicle Category as dropdowns that pull live from the
-- m10_loading_type / m10_vehicle_category masters (V37) via optionsSource,
-- same pattern as EwayBill in V31 — the generic GET /api/m0/entity/{code}/options
-- falls back to entity_registry.value_column/display_column ('code'/'display_name'
-- for both, set in V37), so editing the masters updates this dropdown with no
-- further migration needed.
--
-- Full-body rewrite of page_version id=358 (page_definition.code='aria:front-gate-create'),
-- same technique as V10/V11 — everything else (flowVariant select, vehicleNo,
-- onSuccessOpen jump to the slip, the in-progress queue) is carried over unchanged.

UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json='{"kind":"layout","schemaVersion":2,"slug":"aria:front-gate-create","title":"Front Gate — Tanker Intake","status":"published","version":1,"roles":["FrontGate","TenantAdmin"],"dataSources":[],"streams":[],"entity":"GatePass","layout":[{"kind":"Grid","columns":12,"gap":16,"children":[{"kind":"Section","title":"New Vehicle Intake","span":12,"subtitle":"Capture the vehicle at the gate, then Create Gate Pass to start the tanker-inbound flow.","children":[{"kind":"Form","submitLabel":"","bindings":{"fields":[  {
    "name":"flowVariant",
    "label":"Flow Variant",
    "type":"select",
    "required":true,
    "optionsSource":{
      "type":"entity",
      "entity":"FlowVariant",
      "valueField":"code",
      "labelField":"display_name"
    }
  },{"name":"vehicleNo","label":"Vehicle No","type":"text","required":true,"placeholder":"TN-46-CJ-0001"},{"name":"loadingType","label":"Loading Type","required":true,"type":"select","optionsSource":{"type":"entity","entity":"LoadingType","valueField":"code","labelField":"display_name"}},{"name":"vehicleCategory","label":"Vehicle Category","required":true,"type":"select","optionsSource":{"type":"entity","entity":"VehicleCategory","valueField":"code","labelField":"display_name"}}]},"children":[{"kind":"WorkflowActionButton","label":"Create Gate Pass","action":"workflow.transition","workflowCode":"{{form.flowVariant}}","targetState":"GATE_PASS_CREATED","trigger":"page.submitted","color":"primary","onSuccessOpen":{"page":"aria:gate-pass-slip","lookupEntity":"GatePass","lookupField":"workflow_instance_id","param":"id"}}]}]},{"kind":"Section","title":"Gate Passes In Progress","span":12,"subtitle":"Vehicles you have logged that are moving through parking, weighment and QC.","children":[{"kind":"WorkflowQueueList","persona":"frontgate","pageSize":25,"cardFields":["vehicleNo","gpNumber","currentState"]}]}]}]}'
WHERE pd.code='aria:front-gate-create' AND pv.id=pd.current_version_id;
