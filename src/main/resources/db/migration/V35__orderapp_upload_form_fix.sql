-- V35: orderapp-upload page rendered blank because it used kind:'form'
-- which the Renderer does not handle as a top-level body; the working
-- form-pages all use kind:'layout' + Section + FormField. Replace.
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','layout','schemaVersion',2,
  'layout', JSON_ARRAY(
    JSON_OBJECT('kind','Section','title','Upload OrderApp Order Sheet',
      'description','Either upload an Excel/PDF order sheet, or enter the row manually below. Multiple uploads append to the dump.',
      'fields', JSON_ARRAY(
        JSON_OBJECT('kind','FormField','type','file','name','orderSheetFile','label','Order Sheet (Excel / PDF)','accept','.xlsx,.xls,.pdf,.csv'),
        JSON_OBJECT('kind','FormField','type','text','name','order_ref','label','Order Ref','required',true,'placeholder','OA-26-011'),
        JSON_OBJECT('kind','FormField','type','text','name','customer_name','label','Customer','required',true,'placeholder','e.g. Sri Vinayaga Stores'),
        JSON_OBJECT('kind','FormField','type','select','name','customer_route','label','Route',
          'options', JSON_ARRAY(
            JSON_OBJECT('label','Coimbatore-City','value','Coimbatore-City'),
            JSON_OBJECT('label','Coimbatore-North','value','Coimbatore-North'),
            JSON_OBJECT('label','Salem','value','Salem'),
            JSON_OBJECT('label','Erode','value','Erode'),
            JSON_OBJECT('label','Tirupur','value','Tirupur'),
            JSON_OBJECT('label','Pollachi','value','Pollachi'),
            JSON_OBJECT('label','Karur','value','Karur'))),
        JSON_OBJECT('kind','FormField','type','text','name','sku_code','label','SKU Code','required',true,'placeholder','GIN-1L-IDH'),
        JSON_OBJECT('kind','FormField','type','number','name','ordered_qty_cases','label','Cases Ordered','required',true),
        JSON_OBJECT('kind','FormField','type','number','name','ordered_weight_kg','label','Weight (kg)'),
        JSON_OBJECT('kind','FormField','type','date','name','expected_dispatch_date','label','Expected Dispatch Date')
      ),
      'submit', JSON_OBJECT('label','Add to OrderApp Dump','action','save','entity','OrderAppDump','nextPage','pico:orderapp-dump')
    )
  )
)
WHERE pd.code='pico:orderapp-upload';
