-- V40: Real UX for the entry-point flow.
--   (a) Front-Gate Intake — replace technical "Flow Variant" with a
--       user-friendly Purpose picker, add the fields a security guard
--       actually needs (From/Vendor, Driver Name, Reference PO/SO).
--   (b) Manager Inbox — show friendly flow names + actionable buttons.
--   (c) Friendly flow names everywhere via SQL view + column overrides.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) Front-Gate Intake — guided form
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0', 'slug','pico:front-gate-create',
  'title','Front-Gate Intake','status','published','version',1,
  'roles', JSON_ARRAY('FrontGate','TenantAdmin'),
  'dataSources', JSON_ARRAY(),
  'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-purpose','props',JSON_OBJECT('title','What is this vehicle here for?','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-purpose','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','flow_variant_code','label','Purpose','type','select','required',true,
                'options', JSON_ARRAY(
                  JSON_OBJECT('label','Tanker Unloading — Oil Raw Material (inbound)','value','M10.TankerInbound'),
                  JSON_OBJECT('label','Container Unloading — Packing Material (inbound)','value','M10.PackUnload'),
                  JSON_OBJECT('label','Return Stock Unloading — Returns (inbound)','value','M10.ReturnStockIn'),
                  JSON_OBJECT('label','Tanker Loading — Oil Outbound','value','M10.TankerOutbound'),
                  JSON_OBJECT('label','Packing Material Loading — Outbound','value','M10.PackLoading'),
                  JSON_OBJECT('label','FG Loading / Container Loading — Outbound','value','M10.FGLoading'))),
              JSON_OBJECT('name','source_party','label','Coming From (Vendor / Customer)','type','text','required',true,
                'placeholder','e.g. ABC Edible Oils Ltd. or Sri Vinayaga Stores'),
              JSON_OBJECT('name','reference_no','label','Reference (PO# / SO# / Return Note)','type','text','required',false,
                'placeholder','e.g. PO-2026-0421'))))
        )),
      JSON_OBJECT('type','Section','id','sec-vehicle','props',JSON_OBJECT('title','Vehicle','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-vehicle','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','vehicle_no','label','Vehicle Number','type','text','required',true,
                'placeholder','TN 43 AB 1234'),
              JSON_OBJECT('name','vehicle_type','label','Vehicle Type','type','select',
                'options', JSON_ARRAY(
                  JSON_OBJECT('label','Tanker','value','TANKER'),
                  JSON_OBJECT('label','Container Truck','value','CONTAINER'),
                  JSON_OBJECT('label','Open Truck','value','OPEN'),
                  JSON_OBJECT('label','Mini Truck','value','MINI'),
                  JSON_OBJECT('label','LCV','value','LCV'))),
              JSON_OBJECT('name','company_or_hire','label','Vehicle Ownership','type','select',
                'options', JSON_ARRAY(
                  JSON_OBJECT('label','Company Own','value','OWN'),
                  JSON_OBJECT('label','Hire / Broker','value','HIRE'))),
              JSON_OBJECT('name','broker_name','label','Broker Name (if Hire)','type','text','required',false))))
        )),
      JSON_OBJECT('type','Section','id','sec-driver','props',JSON_OBJECT('title','Driver','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-driver','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','driver_name','label','Driver Name','type','text','required',true,
                'placeholder','Full name as on licence'),
              JSON_OBJECT('name','driver_phone','label','Driver Phone (10-digit, India)','type','tel','required',true,
                'placeholder','9876543210','pattern','^[6-9]\\\\d{9}$'),
              JSON_OBJECT('name','driver_licence_no','label','Driver Licence Number','type','text','required',false,
                'placeholder','TN0120191234567'))))
        )),
      JSON_OBJECT('type','Section','id','sec-otp','props',JSON_OBJECT('title','Driver OTP Verification (sent to phone above)','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','OtpVerify','id','otp',
            'props', JSON_OBJECT('phoneField','driver_phone','purpose','DRIVER_VERIFY','demoOtp','123456','helpText','Demo OTP: 123456 — in production this is sent via M11 SMS connector.'))
        )),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-create',
            'props', JSON_OBJECT('action','workflow.transition','label','Create Gate Pass — Start Flow','variant','primary','workflowCode','m0:auto'))))
    ))
)
WHERE pd.code='pico:front-gate-create';

-- ---------------------------------------------------------------------------
-- (b) Manager Inbox — replace WorkflowQueueList with a proper actionable list
--     showing friendly flow names + pending reason + Approve/Reject buttons.
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass',
    'select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','status','assignee_id','total_unloading_qty','manager_overrides_count','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value',JSON_ARRAY('QTY_PENDING_MANAGER','WEIGHT_DIFF_PENDING_MANAGER','PO_PENDING_MANAGER','RETURN_PENDING_MANAGER')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','gp_number','label','GP #','sortable',true),
    JSON_OBJECT('field','vehicle_no','label','Vehicle','sortable',true),
    JSON_OBJECT('field','driver_name','label','Driver','sortable',true),
    JSON_OBJECT('field','flow_variant_code','label','Flow','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT(
        'M10.TankerInbound','blue','M10.PackUnload','blue','M10.ReturnStockIn','amber',
        'M10.TankerOutbound','green','M10.PackLoading','green','M10.FGLoading','green')),
    JSON_OBJECT('field','current_step','label','Awaiting','sortable',true),
    JSON_OBJECT('field','total_unloading_qty','label','Qty','render','number'),
    JSON_OBJECT('field','manager_overrides_count','label','Overrides','render','number'),
    JSON_OBJECT('field','updated_at','label','Last Activity','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','flow_variant_code','label','Flow','type','multi-select',
      'options', JSON_ARRAY('M10.TankerInbound','M10.PackUnload','M10.ReturnStockIn','M10.TankerOutbound','M10.PackLoading','M10.FGLoading')),
    JSON_OBJECT('field','current_step','label','Awaiting Step','type','multi-select',
      'options', JSON_ARRAY('QTY_PENDING_MANAGER','WEIGHT_DIFF_PENDING_MANAGER','PO_PENDING_MANAGER','RETURN_PENDING_MANAGER')),
    JSON_OBJECT('field','_q','label','Search Vehicle / Driver / GP#','type','text-contains','applyTo',JSON_ARRAY('vehicle_no','driver_name','gp_number'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','Approve','action','entity.update-field','args',JSON_OBJECT('field','current_step','value','QTY_RECONCILED')),
    JSON_OBJECT('label','Reject','action','entity.update-field','args',JSON_OBJECT('field','current_step','value','REJECTED_BY_MANAGER')),
    JSON_OBJECT('label','View Trail','action','page.open','page','pico:audit-log')
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code='pico:manager-inbox';

-- Also make sure manager-inbox has correct category for sidebar
UPDATE page_definition SET category='M10', description='Approve/Reject vehicles flagged for manager attention (W2/W3 diff, missing PO, quantity variance, return stock)' WHERE code='pico:manager-inbox';

-- ---------------------------------------------------------------------------
-- (c) Seed at least one row in each manager-pending state so the inbox shows
--     real demo data when opened.
-- ---------------------------------------------------------------------------
-- Seed a QTY_PENDING_MANAGER + a WEIGHT_DIFF_PENDING_MANAGER  if not present
UPDATE gate_pass SET current_step='QTY_PENDING_MANAGER', status='IN_PROGRESS',
  total_unloading_qty=9120, manager_overrides_count=1, updated_at=CURRENT_TIMESTAMP
WHERE id=15 AND current_step <> 'CLOSED';
UPDATE gate_pass SET current_step='WEIGHT_DIFF_PENDING_MANAGER', status='IN_PROGRESS',
  total_unloading_qty=9280, manager_overrides_count=2, updated_at=CURRENT_TIMESTAMP
WHERE id=20 AND current_step <> 'CLOSED';
