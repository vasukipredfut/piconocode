-- aria:front-gate-create: "Flow Variant" becomes a cascading select driven
-- by the existing "Loading Type" field (m10_loading_type — already has
-- exactly the 5 values requested: LOADING/UN_LOADING/RETURN_STOCK/EMPTY/
-- OTHERS, no new entity needed) instead of listing all 11 flow variants
-- flat. Loading Type moves to the FIRST field; Flow Variant's options are
-- filtered to only the variants tagged with the selected loading_type_code,
-- same cascading-datasource-filter convention already used elsewhere in this
-- app (e.g. aria:loading-allocation's orderRef filtered by
-- "value": "pageData:vehicle_no").
--
-- loading_type_code mapping (confirmed with the business):
--   LOADING:    FGLoading (Container Loading), PackLoading, IntTransferOut,
--               ThirdPartyOut, TankerOutbound
--   UN_LOADING: PackUnload (Container Unload), IntTransferIn, ReturnStockIn,
--               ThirdPartyIn, TankerInbound
--   OTHERS:     EwayBill
-- RETURN_STOCK and EMPTY have no flow variant tagged yet — selecting either
-- shows an empty Flow Variant list until a variant is created and tagged for
-- them. "Pack Unloading" and "Container Unloading" collapse onto the single
-- existing PackUnload workflow — no separate Pack Unloading workflow exists.
-- "Return Stock Out" was requested but doesn't exist as a workflow — not
-- invented here; only ReturnStockIn (tagged UN_LOADING) exists today.

-- 1. Categorize existing flow variants.
ALTER TABLE m10_flow_variant
  ADD COLUMN IF NOT EXISTS loading_type_code VARCHAR(32) DEFAULT NULL;

UPDATE m10_flow_variant SET loading_type_code = CASE code
  WHEN 'M10.FGLoading' THEN 'LOADING'
  WHEN 'M10.PackLoading' THEN 'LOADING'
  WHEN 'M10.IntTransferOut' THEN 'LOADING'
  WHEN 'M10.ThirdPartyOut' THEN 'LOADING'
  WHEN 'M10.TankerOutbound' THEN 'LOADING'
   WHEN 'M10.ReturnStockOut' THEN 'LOADING'
  WHEN 'M10.PackUnload' THEN 'UN_LOADING'
  WHEN 'M10.IntTransferIn' THEN 'UN_LOADING'
  WHEN 'M10.ReturnStockIn' THEN 'UN_LOADING'
  WHEN 'M10.ThirdPartyIn' THEN 'UN_LOADING'
  WHEN 'M10.TankerInbound' THEN 'UN_LOADING'
  WHEN 'M10.EwayBill' THEN 'OTHERS'
  
  ELSE loading_type_code
END;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+1,
       -1, 'FlowVariant', 'loading_type_code', 'Loading Type', 'STRING', 1, 1, 1, 0, 'select'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='FlowVariant' AND field_code='loading_type_code');

-- 2. aria:front-gate-create: Loading Type first, Flow Variant filtered by it.
-- Everything else (Vehicle No / Vehicle Category fields, Create Gate Pass
-- button, queue section) is unchanged from the live body.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = '{"kind":"layout","schemaVersion":2,"slug":"aria:front-gate-create","title":"Front Gate — Vehicle Intake","status":"published","version":1,"roles":["FrontGate","TenantAdmin"],"dataSources":[],"streams":[],"entity":"GatePass","layout":[{"kind":"Grid","columns":12,"gap":16,"children":[{"kind":"Section","title":"New Vehicle Intake","span":12,"subtitle":"Capture the vehicle at the gate, then Create Gate Pass to start the selected flow.","children":[{"kind":"Form","submitLabel":"","bindings":{"fields":[{"name":"loadingType","label":"Loading Type","required":true,"type":"select","optionsSource":{"type":"entity","entity":"LoadingType","valueField":"code","labelField":"display_name"}},{"name":"flowVariant","label":"Flow Variant","type":"select","required":true,"placeholder":"Select Loading Type first","optionsSource":{"type":"datasource","spec":{"type":"entity","entity":"FlowVariant","filter":{"field":"loading_type_code","op":"eq","value":"pageData:loadingType"},"select":["code","display_name"],"sort":[{"field":"display_name","dir":"asc"}]},"valueField":"code","labelField":"display_name"}},{"name":"vehicleNo","label":"Vehicle No","type":"text","required":true,"placeholder":"TN-46-CJ-0001"},{"name":"vehicleCategory","label":"Vehicle Category","required":true,"type":"select","optionsSource":{"type":"entity","entity":"VehicleCategory","valueField":"code","labelField":"display_name"}}]},"children":[{"kind":"WorkflowActionButton","label":"Create Gate Pass","action":"workflow.transition","workflowCode":"{{form.flowVariant}}","targetState":"GATE_PASS_CREATED","trigger":"page.submitted","color":"primary","onSuccessOpen":{"page":"aria:gate-pass-slip","lookupEntity":"GatePass","lookupField":"workflow_instance_id","param":"id"}}]}]},{"kind":"Section","title":"Gate Passes In Progress","span":12,"subtitle":"Vehicles you have logged that are moving through parking, weighment and QC.","children":[{"kind":"WorkflowQueueList","persona":"frontgate","pageSize":25,"cardFields":["vehicleNo","gpNumber","currentState"]}]}]}]}'
 WHERE pd.code = 'aria:front-gate-create' AND pv.status = 'PUBLISHED';
