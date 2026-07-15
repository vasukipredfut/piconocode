-- V36: fix orderapp-upload — the submit button must be a sibling FormField
-- of kind "WorkflowActionButton" inline in the fields array, not a separate
-- submit block (which is just dropped by the Renderer). Match the working
-- pico:lr-apply pattern.
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','layout','schemaVersion',2,
  'layout', JSON_ARRAY(
    JSON_OBJECT('kind','Section','title','Upload OrderApp Order Sheet',
      'fields', JSON_ARRAY(
        JSON_OBJECT('kind','FormField','type','text','name','order_ref','label','Order Ref'),
        JSON_OBJECT('kind','FormField','type','text','name','customer_name','label','Customer'),
        JSON_OBJECT('kind','FormField','type','select','name','customer_route','label','Route',
          'options', JSON_ARRAY(
            JSON_OBJECT('label','Coimbatore-City','value','Coimbatore-City'),
            JSON_OBJECT('label','Coimbatore-North','value','Coimbatore-North'),
            JSON_OBJECT('label','Salem','value','Salem'),
            JSON_OBJECT('label','Erode','value','Erode'),
            JSON_OBJECT('label','Tirupur','value','Tirupur'),
            JSON_OBJECT('label','Pollachi','value','Pollachi'),
            JSON_OBJECT('label','Karur','value','Karur'))),
        JSON_OBJECT('kind','FormField','type','text','name','sku_code','label','SKU Code'),
        JSON_OBJECT('kind','FormField','type','number','name','ordered_qty_cases','label','Cases Ordered'),
        JSON_OBJECT('kind','FormField','type','number','name','ordered_weight_kg','label','Weight (kg)'),
        JSON_OBJECT('kind','FormField','type','text','name','expected_dispatch_date','label','Expected Dispatch (YYYY-MM-DD)'),
        JSON_OBJECT('kind','WorkflowActionButton','action','save','label','Add to OrderApp Dump','variant','primary')
      )
    )
  )
)
WHERE pd.code='pico:orderapp-upload';
