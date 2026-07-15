-- V48: Per-role inboxes — fully inlined to avoid the collation mismatch.
-- Cross-ref: SAP Worklist / ServiceNow My Open Items / Tally Pending Tasks
-- ===========================================================================
SET @cols = JSON_ARRAY(
  JSON_OBJECT('field','gp_number','label','GP #','sortable',true),
  JSON_OBJECT('field','vehicle_no','label','Vehicle','sortable',true),
  JSON_OBJECT('field','driver_name','label','Driver','sortable',true),
  JSON_OBJECT('field','flow_variant_code','label','Flow','sortable',true,'render','badge',
    'colorMap', JSON_OBJECT('M10.TankerInbound','blue','M10.PackUnload','blue','M10.ReturnStockIn','amber','M10.TankerOutbound','green','M10.PackLoading','green','M10.FGLoading','green')),
  JSON_OBJECT('field','current_step','label','Step','sortable',true,'render','badge',
    'colorMap', JSON_OBJECT('GATE_PASS_CREATED','grey','PARKING_IN','blue','DISPATCH_QUEUED','blue','QC_APPROVED','green','PARKING_OUT_AFTER_QC','blue','SUPERVISOR_ALLOCATED','purple','WEIGHMENT_LOADED','purple','UNLOADING_DONE','green','WEIGHMENT_EMPTY','purple','PARKING_IN_AFTER_UNLOAD','blue','QTY_RECONCILED','green','PARKING_OUT_FINAL','blue','WEIGHMENT_RECHECK','purple')),
  JSON_OBJECT('field','source_party','label','From / To','sortable',true),
  JSON_OBJECT('field','reference_no','label','Ref #'),
  JSON_OBJECT('field','updated_at','label','Last Activity','render','datetime','sortable',true));

SET @filters = JSON_ARRAY(
  JSON_OBJECT('field','flow_variant_code','label','Flow','type','multi-select','options',JSON_ARRAY('M10.TankerInbound','M10.PackUnload','M10.ReturnStockIn','M10.TankerOutbound','M10.PackLoading','M10.FGLoading')),
  JSON_OBJECT('field','_q','label','Search Vehicle / Driver / GP#','type','text-contains','applyTo',JSON_ARRAY('vehicle_no','driver_name','gp_number')));

SET @rowActions = JSON_ARRAY(
  JSON_OBJECT('label','Open in Tracker','action','page.open','page','pico:tanker-tracker'),
  JSON_OBJECT('label','Audit Trail','action','page.open','page','pico:audit-log'));

-- Helper to make a body for a given step-filter
-- Build body for each inbox and upsert via INSERT ON DUPLICATE
-- We can't use a procedure due to collation; instead inline each.

-- ============ Front Gate ============
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
  VALUES (1, 'pico:fg-inbox', 'Front Gate — My Inbox', 'Vehicles arriving (to issue gate pass) or leaving (to stamp exit). Use Front-Gate Intake to start a new pass.', 'M10', 'PUBLISHED', NOW(), NOW())
  ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), updated_at=NOW();
SET @b = JSON_OBJECT('kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass','select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','source_party','reference_no','assignee_id','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value', JSON_ARRAY('GATE_PASS_CREATED','WEIGHMENT_RECHECK','PARKING_OUT_FINAL')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', @cols, 'filters', @filters, 'rowActions', @rowActions, 'pagination', JSON_OBJECT('pageSize',25));
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:fg-inbox';
INSERT INTO page_version (page_id, version, status, body_json) SELECT id, 1, 'PUBLISHED', @b FROM page_definition WHERE code='pico:fg-inbox';
UPDATE page_definition SET current_version_id=(SELECT id FROM page_version WHERE page_id=page_definition.id ORDER BY version DESC LIMIT 1) WHERE code='pico:fg-inbox';

-- ============ Parking Security ============
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
  VALUES (1, 'pico:ps-inbox', 'Parking Security — My Inbox', 'Vehicles to scan into bay or release. Use Parking Scan to record IN/OUT.', 'M10', 'PUBLISHED', NOW(), NOW())
  ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), updated_at=NOW();
SET @b = JSON_OBJECT('kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass','select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','source_party','reference_no','assignee_id','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value', JSON_ARRAY('PARKING_IN','PARKING_OUT_AFTER_QC','PARKING_IN_AFTER_UNLOAD','PARKING_OUT_FINAL')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', @cols, 'filters', @filters, 'rowActions', @rowActions, 'pagination', JSON_OBJECT('pageSize',25));
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:ps-inbox';
INSERT INTO page_version (page_id, version, status, body_json) SELECT id, 1, 'PUBLISHED', @b FROM page_definition WHERE code='pico:ps-inbox';
UPDATE page_definition SET current_version_id=(SELECT id FROM page_version WHERE page_id=page_definition.id ORDER BY version DESC LIMIT 1) WHERE code='pico:ps-inbox';

-- ============ Dispatch Officer ============
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
  VALUES (1, 'pico:dispatch-inbox', 'Dispatch Officer — My Inbox', 'Vehicles in queue for PO/SO reference check + dispatch officer approval to send to QC.', 'M10', 'PUBLISHED', NOW(), NOW())
  ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), updated_at=NOW();
SET @b = JSON_OBJECT('kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass','select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','source_party','reference_no','assignee_id','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value', JSON_ARRAY('PARKING_IN','DISPATCH_QUEUED','SUPERVISOR_ALLOCATED','QTY_RECONCILED')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', @cols, 'filters', @filters, 'rowActions', @rowActions, 'pagination', JSON_OBJECT('pageSize',25));
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:dispatch-inbox';
INSERT INTO page_version (page_id, version, status, body_json) SELECT id, 1, 'PUBLISHED', @b FROM page_definition WHERE code='pico:dispatch-inbox';
UPDATE page_definition SET current_version_id=(SELECT id FROM page_version WHERE page_id=page_definition.id ORDER BY version DESC LIMIT 1) WHERE code='pico:dispatch-inbox';

-- ============ QC Officer ============
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
  VALUES (1, 'pico:qc-inbox', 'QC Officer — My Inbox', 'Vehicles queued for sample inspection. Item-category-aware QC form opens on row.', 'M10', 'PUBLISHED', NOW(), NOW())
  ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), updated_at=NOW();
SET @b = JSON_OBJECT('kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass','select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','source_party','reference_no','assignee_id','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value', JSON_ARRAY('DISPATCH_QUEUED')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', @cols, 'filters', @filters, 'rowActions', @rowActions, 'pagination', JSON_OBJECT('pageSize',25));
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:qc-inbox';
INSERT INTO page_version (page_id, version, status, body_json) SELECT id, 1, 'PUBLISHED', @b FROM page_definition WHERE code='pico:qc-inbox';
UPDATE page_definition SET current_version_id=(SELECT id FROM page_version WHERE page_id=page_definition.id ORDER BY version DESC LIMIT 1) WHERE code='pico:qc-inbox';

-- ============ Weighment Officer ============
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
  VALUES (1, 'pico:weighment-inbox', 'Weighment Officer — My Inbox', 'Vehicles at the weighbridge for Loaded / Empty / Re-check reading.', 'M10', 'PUBLISHED', NOW(), NOW())
  ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), updated_at=NOW();
SET @b = JSON_OBJECT('kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass','select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','source_party','reference_no','assignee_id','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value', JSON_ARRAY('SUPERVISOR_ALLOCATED','UNLOADING_DONE','PARKING_OUT_FINAL')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', @cols, 'filters', @filters, 'rowActions', @rowActions, 'pagination', JSON_OBJECT('pageSize',25));
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:weighment-inbox';
INSERT INTO page_version (page_id, version, status, body_json) SELECT id, 1, 'PUBLISHED', @b FROM page_definition WHERE code='pico:weighment-inbox';
UPDATE page_definition SET current_version_id=(SELECT id FROM page_version WHERE page_id=page_definition.id ORDER BY version DESC LIMIT 1) WHERE code='pico:weighment-inbox';

-- ============ Unloading Supervisor ============
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
  VALUES (1, 'pico:supervisor-inbox', 'Unloading Supervisor — My Inbox', 'Vehicles cleared for unloading. Receive into godown bin, count cases, capture damages.', 'M10', 'PUBLISHED', NOW(), NOW())
  ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), updated_at=NOW();
SET @b = JSON_OBJECT('kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass','select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','source_party','reference_no','assignee_id','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value', JSON_ARRAY('WEIGHMENT_LOADED')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', @cols, 'filters', @filters, 'rowActions', @rowActions, 'pagination', JSON_OBJECT('pageSize',25));
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:supervisor-inbox';
INSERT INTO page_version (page_id, version, status, body_json) SELECT id, 1, 'PUBLISHED', @b FROM page_definition WHERE code='pico:supervisor-inbox';
UPDATE page_definition SET current_version_id=(SELECT id FROM page_version WHERE page_id=page_definition.id ORDER BY version DESC LIMIT 1) WHERE code='pico:supervisor-inbox';

-- ============ Plant Owner ============
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
  VALUES (1, 'pico:plantowner-inbox', 'Plant Owner — My Inbox', 'Items above manager authority — variance > 1%, missing PO + value > Rs 5L, returns > 100 cases, anomalies.', 'M10', 'PUBLISHED', NOW(), NOW())
  ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), updated_at=NOW();
SET @b = JSON_OBJECT('kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass','select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','source_party','reference_no','assignee_id','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value', JSON_ARRAY('WEIGHT_DIFF_PENDING_MANAGER','QTY_PENDING_MANAGER','PO_PENDING_MANAGER','RETURN_PENDING_MANAGER')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', @cols, 'filters', @filters, 'rowActions', @rowActions, 'pagination', JSON_OBJECT('pageSize',25));
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:plantowner-inbox';
INSERT INTO page_version (page_id, version, status, body_json) SELECT id, 1, 'PUBLISHED', @b FROM page_definition WHERE code='pico:plantowner-inbox';
UPDATE page_definition SET current_version_id=(SELECT id FROM page_version WHERE page_id=page_definition.id ORDER BY version DESC LIMIT 1) WHERE code='pico:plantowner-inbox';
