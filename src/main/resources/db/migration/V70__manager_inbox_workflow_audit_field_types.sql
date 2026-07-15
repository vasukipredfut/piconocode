-- V70: Fix two gaps reported by the user.
--   (a) pico:manager-approval-inbox is an old V1 Grid page with NULL
--       columns/rowActions — rebuild as Schema V2 list with full decision
--       context + Approve/Reject row actions + Print + Trail.
--   (b) WorkflowDefinition / WorkflowInstance / WorkflowEvent / AuditLog
--       field types were sloppy: `select` widgets with no options_json,
--       ENUMs with no widget_kind, technical codes shown as plain text
--       when they should be either entity-ref pickers or option-list
--       selects. Fill them in so every modal renders the right control
--       and every list cell shows a friendly badge.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) pico:manager-approval-inbox — proper Schema V2 list
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass',
    'select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','status','source_party','reference_no','ordered_qty','total_unloading_qty','w1_weight_kg','w2_weight_kg','w3_weight_kg','variance_reason','manager_overrides_count','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value',JSON_ARRAY('QTY_PENDING_MANAGER','WEIGHT_DIFF_PENDING_MANAGER','PO_PENDING_MANAGER','RETURN_PENDING_MANAGER','QC_REJECTED','REJECTED_BY_MANAGER')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','gp_number','label','GP #','sortable',true),
    JSON_OBJECT('field','vehicle_no','label','Vehicle','sortable',true),
    JSON_OBJECT('field','driver_name','label','Driver','sortable',true),
    JSON_OBJECT('field','flow_variant_code','label','Flow','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('M10.TankerInbound','blue','M10.PackUnload','blue','M10.ReturnStockIn','amber','M10.TankerOutbound','green','M10.PackLoading','green','M10.FGLoading','green')),
    JSON_OBJECT('field','current_step','label','Awaiting','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('QTY_PENDING_MANAGER','amber','WEIGHT_DIFF_PENDING_MANAGER','amber','PO_PENDING_MANAGER','amber','RETURN_PENDING_MANAGER','amber','QC_REJECTED','red','REJECTED_BY_MANAGER','red')),
    JSON_OBJECT('field','source_party','label','From','sortable',true),
    JSON_OBJECT('field','reference_no','label','Ref #','sortable',true),
    JSON_OBJECT('field','w1_weight_kg','label','W1 (kg)','render','number'),
    JSON_OBJECT('field','w2_weight_kg','label','W2 (kg)','render','number'),
    JSON_OBJECT('field','w3_weight_kg','label','W3 (kg)','render','number'),
    JSON_OBJECT('field','total_unloading_qty','label','Unloaded','render','number'),
    JSON_OBJECT('field','manager_overrides_count','label','Overrides #','render','number'),
    JSON_OBJECT('field','variance_reason','label','Reason'),
    JSON_OBJECT('field','updated_at','label','Last Activity','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','flow_variant_code','label','Flow','type','multi-select','options',JSON_ARRAY('M10.TankerInbound','M10.PackUnload','M10.ReturnStockIn','M10.TankerOutbound','M10.PackLoading','M10.FGLoading')),
    JSON_OBJECT('field','current_step','label','Awaiting','type','multi-select','options',JSON_ARRAY('QTY_PENDING_MANAGER','WEIGHT_DIFF_PENDING_MANAGER','PO_PENDING_MANAGER','RETURN_PENDING_MANAGER','QC_REJECTED','REJECTED_BY_MANAGER')),
    JSON_OBJECT('field','_q','label','Search GP / Vehicle / Driver / Ref','type','text-contains','applyTo',JSON_ARRAY('gp_number','vehicle_no','driver_name','reference_no','source_party'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','✓ Approve','action','entity.update-field','args',JSON_OBJECT('field','current_step','value','QTY_RECONCILED')),
    JSON_OBJECT('label','✗ Reject','action','entity.update-field','args',JSON_OBJECT('field','current_step','value','REJECTED_BY_MANAGER')),
    JSON_OBJECT('label','🖨 Print','action','print.html','args',JSON_OBJECT('kind','gate-pass')),
    JSON_OBJECT('label','Trail','action','page.open','page','pico:audit-log')
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code='pico:manager-approval-inbox';

UPDATE page_definition SET title='Manager Approval Inbox',
  description='Vehicles awaiting manager decision — quantity variance, weight discrepancy (W2 vs W3), missing PO, return-stock variance, or QC rejected. Shows full decision context per row; one-click Approve/Reject/Print.',
  category='M10'
 WHERE code='pico:manager-approval-inbox';

-- ---------------------------------------------------------------------------
-- (b) Field-type fixes for workflow + audit entities
-- ---------------------------------------------------------------------------

-- WorkflowEvent.kind — was select with no options. Source of truth comes
-- from WorkflowEvent.java constants. Add the full set.
UPDATE entity_field_registry
SET options_json = JSON_ARRAY(
  'workflow.start','workflow.transition','page.submitted',
  'workflow.cancel','workflow.error','workflow.guard_denied',
  'workflow.role_denied','workflow.timer','workflow.action_fired'
)
WHERE entity_code='WorkflowEvent' AND field_code='kind';

-- WorkflowInstance.status — was ENUM with NULL widget. Add select + options.
UPDATE entity_field_registry
SET widget_kind='select',
    options_json=JSON_ARRAY('OPEN','CLOSED','CANCELLED','ERROR')
WHERE entity_code='WorkflowInstance' AND field_code='status';

-- WorkflowInstance.currentState — was NULL widget. Free text per workflow,
-- so we expose as text but with a placeholder hint.
UPDATE entity_field_registry SET widget_kind='text'
WHERE entity_code='WorkflowInstance' AND field_code='currentState' AND widget_kind IS NULL;

-- WorkflowInstance.assignedRole — pickable from role table
UPDATE entity_field_registry
SET widget_kind='select',
    options_json=JSON_ARRAY('FrontGate','ParkingSecurity','Dispatch','QC','WeighmentOfficer','UnloadingSupervisor','Manager','Plantowner','TenantAdmin','StoreKeeper')
WHERE entity_code='WorkflowInstance' AND field_code='assignedRole';

-- AuditLog.action_kind — common values across mutations + workflow events
UPDATE entity_field_registry
SET options_json = JSON_ARRAY(
  'CREATE','UPDATE','DELETE',
  'workflow.start','workflow.transition','workflow.cancel',
  'agent.heartbeat','agent.boxevent','agent.command_ack',
  'cron.shift-rotation','cron.statutory-reminder','cron.day-end',
  'export.csv','print.html'
)
WHERE entity_code='AuditLog' AND field_code='action_kind';

-- AuditLog.entity_code — entity-ref to entity_registry (so admins pick
-- "MaterialRequisition" from a dropdown instead of typing)
UPDATE entity_field_registry
SET widget_kind='entity-ref',
    options_json=JSON_OBJECT('entity','EntityRegistry','valueField','code','labelField','display_name')
WHERE entity_code='AuditLog' AND field_code='entity_code';

-- AuditLog.workflow_code — entity-ref to workflow_definition
UPDATE entity_field_registry
SET widget_kind='entity-ref',
    options_json=JSON_OBJECT('entity','WorkflowDefinition','valueField','code','labelField','code')
WHERE entity_code='AuditLog' AND field_code='workflow_code';

-- AuditLog.details_json + AuditLog.message → textarea (already textarea
-- per current registry, but ensure)
UPDATE entity_field_registry SET widget_kind='textarea'
WHERE entity_code='AuditLog' AND field_code IN ('details_json','payload_json','message');

-- WorkflowDefinition: status enum
UPDATE entity_field_registry
SET widget_kind='select',
    options_json=JSON_ARRAY('DRAFT','PUBLISHED','SUPERSEDED','DEPRECATED')
WHERE entity_code='WorkflowDefinition' AND field_code='status';

-- WorkflowDefinition.body_json — JSON textarea (large)
UPDATE entity_field_registry SET widget_kind='textarea'
WHERE entity_code='WorkflowDefinition' AND field_code='body_json';

-- entity_code on every workflow-related table — entity-ref to EntityRegistry
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json, relation_entity) VALUES
  ('WorkflowDefinition','entity_code','For Entity','RELATION',1,1,1,0,'entity-ref',JSON_OBJECT('entity','EntityRegistry','valueField','code','labelField','display_name'),'EntityRegistry');

-- ---------------------------------------------------------------------------
-- (c) Make sure EntityRegistry itself is registered + has a list page so
--     entity-ref pickers can populate
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, read_role, write_role, approver_role) VALUES
  ('EntityRegistry','Entity Registry','entity_registry','M0','*','TenantAdmin','TenantAdmin');

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('EntityRegistry','code','Code','STRING',1,1,1,1,'text',NULL),
  ('EntityRegistry','display_name','Display Name','STRING',1,1,1,1,'text',NULL),
  ('EntityRegistry','table_name','Table','STRING',1,1,1,1,'text',NULL),
  ('EntityRegistry','category','Category','STRING',1,1,0,0,'select',JSON_ARRAY('M0','M5','M6','M7','M8','M9','M10','M11','HR','CRM','Master','Report','Dashboard'));

-- WorkflowDefinition list-able too
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('WorkflowDefinition','code','Code','STRING',1,1,1,1,'text',NULL),
  ('WorkflowDefinition','version','Version','INT',1,1,0,0,'number',NULL);

-- ---------------------------------------------------------------------------
-- (d) Workflow Activity page (pico:workflow-activity) — if it's a List
--     over WorkflowEvent or text-heavy, give it proper columns + badges
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','WorkflowEvent',
    'select', JSON_ARRAY('id','ts','instance_id','kind','from_state','to_state','page_code','actor_user_id'),
    'sort', JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','ts','label','When','render','datetime','sortable',true),
    JSON_OBJECT('field','instance_id','label','Instance','render','number','sortable',true),
    JSON_OBJECT('field','kind','label','Kind','sortable',true,'render','badge',
      'colorMap',JSON_OBJECT('workflow.start','blue','workflow.transition','green','page.submitted','blue','workflow.cancel','red','workflow.error','red','workflow.guard_denied','amber','workflow.role_denied','amber','workflow.timer','grey','workflow.action_fired','purple')),
    JSON_OBJECT('field','from_state','label','From','sortable',true),
    JSON_OBJECT('field','to_state','label','To','sortable',true),
    JSON_OBJECT('field','page_code','label','Via Page','sortable',true),
    JSON_OBJECT('field','actor_user_id','label','Actor','render','number','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','kind','label','Event Kind','type','multi-select','options',JSON_ARRAY('workflow.start','workflow.transition','page.submitted','workflow.cancel','workflow.error','workflow.guard_denied','workflow.role_denied','workflow.timer','workflow.action_fired')),
    JSON_OBJECT('field','instance_id','label','Instance ID','type','text-contains','applyTo',JSON_ARRAY('instance_id')),
    JSON_OBJECT('field','_q','label','Search From / To / Page','type','text-contains','applyTo',JSON_ARRAY('from_state','to_state','page_code'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
WHERE pd.code='pico:workflow-activity';

UPDATE page_definition SET title='Workflow Activity Trail',
  description='Every workflow event in the platform — start, transition, page-submission, cancel, error. Use the kind filter to focus on a single category; click a row to see the SpEL guard payload that drove the decision.',
  category='M0'
 WHERE code='pico:workflow-activity';
