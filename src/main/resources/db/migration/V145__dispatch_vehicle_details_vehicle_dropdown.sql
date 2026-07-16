-- =====================================================================
-- V145 — pico:dispatch-vehicle-details: replace the plain-text vehicleNo
-- field (V87/V134) with a live GatePass dropdown + auto-fill, and surface
-- the Dispatch role's OPEN queue on the same page (persona-based
-- WorkflowQueueList, same mechanism pico:front-gate-create already uses
-- for its "Gate Passes In Progress" queue).
--
-- Same convention as V144 (pico:parking-scan): vehicleNo becomes a
-- "select" with optionsSource entity GatePass (valueField/labelField
-- vehicle_no — officers think "which vehicle", not "which gate pass"),
-- onChangeFill maps the selected row's flow_variant_code / loading_type /
-- workflow_instance_id onto disabled sibling fields, and the Save button
-- targets that instanceId via "{{form.x}}" templating (V136 precedent).
-- Not re-verified against the frontend in this session — same caveat as
-- V144: if it renders empty, the fix is frontend Renderer support, not
-- this migration.
--
-- V134's variant-conditional fields (Tanker vs Packing/FG) are otherwise
-- UNCHANGED in content, just re-pointed from '#form.flow_variant_code'
-- to '#form.flowVariantCode' — this page didn't previously have a bound
-- flow-variant FIELD at all (V134 relied on the renderer seeding #form
-- from instance context before any input); now flowVariantCode is a real
-- field on the page (filled by onChangeFill), so the visibleWhen/
-- requiredWhen expressions must reference it by its actual field name.
--
-- Container loading = M10.FGLoading — already shares PackLoading's field
-- set (transportName/packingFgStockDetails) in V134, no change needed.
-- =====================================================================

UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_OBJECT(
  'kind','layout','schemaVersion',2,
  'dataSources', JSON_ARRAY(
    JSON_OBJECT('id','dispatchQueue','kind','rest','method','GET','url','/api/workflow/queue/Dispatch')
  ),
  'layout', JSON_ARRAY(
    JSON_OBJECT('kind','Section','title','Dispatch Queue (Open)',
      'fields', JSON_ARRAY(
        JSON_OBJECT('kind','WorkflowQueueList','persona','Dispatch','dataSource','dispatchQueue','pageSize',25,
          'cardFields',JSON_ARRAY('vehicleNo','gpNumber','flowVariant','currentState'))
      )),
    JSON_OBJECT('kind','Section','title','Vehicle & Dispatch Details',
      'fields', JSON_ARRAY(
        JSON_OBJECT('kind','FormField','type','select','name','vehicleNo','label','Vehicle Number','required',true,
          'placeholder','Select vehicle awaiting dispatch entry',
          'optionsSource',JSON_OBJECT('type','entity','entity','GatePass','lookupType','vehicleNumbers','valueField','vehicle_no','labelField','vehicle_no'),
          'onChangeFill',JSON_OBJECT('flowVariantCode','flow_variant_code','loadingType','loading_type','instanceId','workflow_instance_id')),
        JSON_OBJECT('kind','FormField','type','text','name','flowVariantCode','label','Flow Variant','disabled',true),
        JSON_OBJECT('kind','FormField','type','text','name','loadingType','label','Loading Type','disabled',true),
        JSON_OBJECT('kind','FormField','type','text','name','instanceId','label','Workflow Instance','disabled',true),
        JSON_OBJECT('kind','FormField','type','select','name','ownershipType','label','Ownership',
          'options',JSON_ARRAY('OWN VEHICLE','HIRED VEHICLE')),
        JSON_OBJECT('kind','FormField','type','text','name','driverName','label','Driver Name'),
        JSON_OBJECT('kind','FormField','type','text','name','driverPhone','label','Driver Contact'),
        JSON_OBJECT('kind','FormField','type','text','name','brokerName','label','Broker Details',
          'visibleWhen','#form.flowVariantCode == ''M10.TankerOutbound'''),
        JSON_OBJECT('kind','FormField','type','text','name','invoiceNo','label','Invoice No',
          'visibleWhen','#form.flowVariantCode == ''M10.TankerOutbound'''),
        JSON_OBJECT('kind','FormField','type','number','name','invoiceQty','label','Invoice Qty',
          'visibleWhen','#form.flowVariantCode == ''M10.TankerOutbound'''),
        JSON_OBJECT('kind','FormField','type','text','name','vendorName','label','Vendor Details',
          'visibleWhen','#form.flowVariantCode == ''M10.TankerOutbound'''),
        JSON_OBJECT('kind','FormField','type','text','name','oilRmItem','label','Oil RM Details',
          'visibleWhen','#form.flowVariantCode == ''M10.TankerOutbound'''),
        JSON_OBJECT('kind','FormField','type','text','name','tankerNo','label','Tanker Number',
          'visibleWhen','#form.flowVariantCode == ''M10.TankerOutbound'''),
        JSON_OBJECT('kind','FormField','type','select','name','poVerified','label','PO Verified',
          'options',JSON_ARRAY('YES','NO - MANAGER APPROVAL'),
          'visibleWhen','#form.flowVariantCode == ''M10.TankerOutbound'''),
        JSON_OBJECT('kind','FormField','type','text','name','poOverrideReason','label','Reason (if PO not verified)',
          'visibleWhen','#form.flowVariantCode == ''M10.TankerOutbound'' && #form.poVerified != ''YES''',
          'requiredWhen','#form.flowVariantCode == ''M10.TankerOutbound'' && #form.poVerified != ''YES'''),
        JSON_OBJECT('kind','FormField','type','text','name','dispatchOfficerName','label','Dispatch Front Officer Name',
          'visibleWhen','#form.flowVariantCode == ''M10.TankerOutbound'''),
        JSON_OBJECT('kind','FormField','type','text','name','transportName','label','Transport Name',
          'visibleWhen','#form.flowVariantCode == ''M10.PackLoading'' || #form.flowVariantCode == ''M10.FGLoading'''),
        JSON_OBJECT('kind','FormField','type','textarea','name','packingFgStockDetails','label','Packing / FG Stock Details',
          'visibleWhen','#form.flowVariantCode == ''M10.PackLoading'' || #form.flowVariantCode == ''M10.FGLoading'''),
        JSON_OBJECT('kind','WorkflowActionButton','label','Save Vehicle Details','action','workflow.transition',
          'instanceId','{{form.instanceId}}','variant','primary')
      ))
  )
)
WHERE pd.code = 'pico:dispatch-vehicle-details' AND pv.status = 'PUBLISHED';
