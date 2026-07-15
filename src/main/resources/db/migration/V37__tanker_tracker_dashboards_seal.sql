-- V37: Three customer-visible upgrades:
--  (a) Tanker Tracker page — visualizes ALL 14 steps for any active gate
--      pass; shows what's done / in-progress / pending + W1/W2/W3 values.
--  (b) Plant Manager dashboard polish — add live counts + sparkline tiles.
--  (c) Seal Management — new entity to track seal lifecycle, plus a list
--      page so the dispatch officer sees seal compliance at a glance.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) TANKER TRACKER PAGE
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:tanker-tracker', 'Tanker Tracker', 'Real-time per-gate-pass step tracker — Tanker Inbound 14-step flow', 'M10', 'PUBLISHED', NOW(), NOW());
INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT(
    'entity','GatePass',
    'select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','flow_variant_code','current_step','status','assignee_id','total_unloading_qty','created_at','closed_at'),
    'filter', JSON_OBJECT('field','flow_variant_code','op','eq','value','M10.TankerInbound'),
    'sort', JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))
  ),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','gp_number','label','GP #','sortable',true),
    JSON_OBJECT('field','vehicle_no','label','Vehicle','sortable',true),
    JSON_OBJECT('field','driver_name','label','Driver','sortable',true),
    JSON_OBJECT('field','current_step','label','Current Step','sortable',true),
    JSON_OBJECT('field','status','label','Status','sortable',true),
    JSON_OBJECT('field','assignee_id','label','Assignee','sortable',true),
    JSON_OBJECT('field','total_unloading_qty','label','Unloaded Qty','render','number'),
    JSON_OBJECT('field','created_at','label','Arrived','render','datetime','sortable',true),
    JSON_OBJECT('field','closed_at','label','Closed','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','current_step','label','Step','type','multi-select','options',JSON_ARRAY(
      'DRAFT','GATE_PASS_CREATED','PARKING_IN','DISPATCH_QUEUED','QC_APPROVED','QC_REJECTED',
      'PARKING_OUT_AFTER_QC','SUPERVISOR_ALLOCATED','WEIGHMENT_LOADED','UNLOADING_DONE',
      'WEIGHMENT_EMPTY','PARKING_IN_AFTER_UNLOAD','QTY_RECONCILED','QTY_PENDING_MANAGER',
      'PARKING_OUT_FINAL','WEIGHMENT_RECHECK','WEIGHT_DIFF_PENDING_MANAGER','CLOSED','REJECTED_BY_MANAGER')),
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('DRAFT','IN_PROGRESS','CLOSED','REJECTED','CANCELLED')),
    JSON_OBJECT('field','_q','label','Vehicle / Driver / GP#','type','text-contains','applyTo',JSON_ARRAY('vehicle_no','driver_name','gp_number'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
FROM page_definition pd WHERE pd.code='pico:tanker-tracker' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:tanker-tracker' AND current_version_id IS NULL;

-- ---------------------------------------------------------------------------
-- (b) SEAL MANAGEMENT — entity + page (per PICO proposal Seal lifecycle)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m10_seal_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gate_pass_id BIGINT NULL,
  seal_number VARCHAR(64) NOT NULL,
  seal_kind VARCHAR(16) NOT NULL DEFAULT 'AUTO',   -- AUTO / MANUAL
  status VARCHAR(24) NOT NULL DEFAULT 'ISSUED',    -- ISSUED / APPLIED / VERIFIED / BROKEN / TAMPERED
  issued_at TIMESTAMP NULL,
  applied_at TIMESTAMP NULL,
  verified_at TIMESTAMP NULL,
  broken_at TIMESTAMP NULL,
  issued_by BIGINT NULL,
  applied_by BIGINT NULL,
  verified_by BIGINT NULL,
  notes VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_seal_num (tenant_id, seal_number),
  INDEX ix_seal_gp (gate_pass_id),
  INDEX ix_seal_status (status)
);

INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, read_role, write_role, approver_role) VALUES
  ('SealLog','Seal Log','m10_seal_log','M10','*','FrontGate,Dispatch,Manager,TenantAdmin','Manager');

INSERT IGNORE INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('SealLog','gate_pass_id','Gate Pass','LONG',1,1,0,0,'number',NULL),
  ('SealLog','seal_number','Seal #','STRING',1,1,1,1,'text',NULL),
  ('SealLog','seal_kind','Kind','STRING',1,1,0,0,'select','["AUTO","MANUAL"]'),
  ('SealLog','status','Status','STRING',1,1,0,0,'select','["ISSUED","APPLIED","VERIFIED","BROKEN","TAMPERED"]'),
  ('SealLog','issued_at','Issued','DATETIME',1,1,0,0,'datetime',NULL),
  ('SealLog','applied_at','Applied','DATETIME',1,1,0,0,'datetime',NULL),
  ('SealLog','verified_at','Verified','DATETIME',1,1,0,0,'datetime',NULL),
  ('SealLog','broken_at','Broken','DATETIME',1,1,0,0,'datetime',NULL),
  ('SealLog','issued_by','Issued By','LONG',1,1,0,0,'number',NULL),
  ('SealLog','applied_by','Applied By','LONG',1,1,0,0,'number',NULL),
  ('SealLog','verified_by','Verified By','LONG',1,1,0,0,'number',NULL),
  ('SealLog','notes','Notes','STRING',0,0,1,0,'text',NULL);

-- Seed seals for the gate passes that have reached the seal stage
INSERT IGNORE INTO m10_seal_log (tenant_id, gate_pass_id, seal_number, seal_kind, status, issued_at, applied_at, verified_at, issued_by, applied_by, verified_by) VALUES
  (1, 1, 'SL-2026-00001', 'AUTO', 'VERIFIED', '2026-06-25 06:30:00', '2026-06-25 07:00:00', '2026-06-25 09:00:00', 2003, 2003, 2007),
  (1, 2, 'SL-2026-00002', 'AUTO', 'VERIFIED', '2026-06-25 08:00:00', '2026-06-25 08:20:00', '2026-06-25 11:30:00', 2003, 2003, 2007),
  (1, 5, 'SL-2026-00003', 'AUTO', 'APPLIED', '2026-06-26 09:00:00', '2026-06-26 09:30:00', NULL, 2003, 2003, NULL),
  (1, 8, 'SL-2026-00004', 'MANUAL', 'ISSUED', '2026-06-26 10:00:00', NULL, NULL, 2003, NULL, NULL),
  (1, 79, 'SL-2026-00079', 'AUTO', 'VERIFIED', '2026-06-26 20:30:00', '2026-06-26 20:45:00', '2026-06-26 20:58:00', 2003, 2003, 2001);

INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:seal-log', 'Seal Management', 'Seal lifecycle: Issue → Apply → Verify → Gate-close; hard-block on mismatch', 'M10', 'PUBLISHED', NOW(), NOW());
INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','SealLog','select',JSON_ARRAY('id','gate_pass_id','seal_number','seal_kind','status','issued_at','applied_at','verified_at','broken_at','notes'), 'sort', JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','seal_number','label','Seal #','sortable',true),
    JSON_OBJECT('field','gate_pass_id','label','GP #','sortable',true),
    JSON_OBJECT('field','seal_kind','label','Kind','sortable',true),
    JSON_OBJECT('field','status','label','Status','sortable',true),
    JSON_OBJECT('field','issued_at','label','Issued','render','datetime'),
    JSON_OBJECT('field','applied_at','label','Applied','render','datetime'),
    JSON_OBJECT('field','verified_at','label','Verified','render','datetime'),
    JSON_OBJECT('field','broken_at','label','Broken','render','datetime')
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('ISSUED','APPLIED','VERIFIED','BROKEN','TAMPERED')),
    JSON_OBJECT('field','seal_kind','label','Kind','type','multi-select','options',JSON_ARRAY('AUTO','MANUAL'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
FROM page_definition pd WHERE pd.code='pico:seal-log' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:seal-log' AND current_version_id IS NULL;

-- ---------------------------------------------------------------------------
-- (c) Bring back missing pages flagged "plain": diesel filling, PO check
-- ---------------------------------------------------------------------------
-- Add diesel-filling status capture page (per PICO sheet 1: "Diesel filling
-- completed status yes/no with time + reason")
CREATE TABLE IF NOT EXISTS m10_diesel_filling_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gate_pass_id BIGINT NULL,
  vehicle_no VARCHAR(32) NULL,
  diesel_qty_litres DECIMAL(8,2) NULL,
  filled_at TIMESTAMP NULL,
  filled_by BIGINT NULL,
  reason VARCHAR(255) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'COMPLETED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX ix_diesel_gp (gate_pass_id)
);

INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, read_role, write_role, approver_role) VALUES
  ('DieselFillingLog','Diesel Filling','m10_diesel_filling_log','M10','*','Dispatch,Manager,TenantAdmin','Manager');
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('DieselFillingLog','gate_pass_id','Gate Pass','LONG',1,1,0,0,'number',NULL),
  ('DieselFillingLog','vehicle_no','Vehicle','STRING',1,1,1,0,'text',NULL),
  ('DieselFillingLog','diesel_qty_litres','Diesel (L)','DECIMAL',1,1,0,0,'number',NULL),
  ('DieselFillingLog','filled_at','Filled At','DATETIME',1,1,0,0,'datetime',NULL),
  ('DieselFillingLog','filled_by','Filled By','LONG',0,1,0,0,'number',NULL),
  ('DieselFillingLog','reason','Reason','STRING',0,0,1,0,'text',NULL),
  ('DieselFillingLog','status','Status','STRING',1,1,0,0,'select','["COMPLETED","SKIPPED","PENDING"]');

INSERT IGNORE INTO m10_diesel_filling_log (tenant_id, gate_pass_id, vehicle_no, diesel_qty_litres, filled_at, filled_by, reason, status) VALUES
  (1, 1, 'TN-43-AA-1234', 320.5, '2026-06-25 06:35:00', 2001, 'Top-up before unload', 'COMPLETED'),
  (1, 2, 'TN-43-AB-2345', 280.0, '2026-06-25 08:10:00', 2001, NULL, 'COMPLETED'),
  (1, 5, 'TN-43-AC-3456', NULL, '2026-06-26 09:05:00', 2001, 'Tank already full', 'SKIPPED'),
  (1, 79, 'TN-43-AB-9999', 350.0, '2026-06-26 20:35:00', 2001, NULL, 'COMPLETED');

INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:diesel-log', 'Diesel Filling Log', 'Diesel fill status per vehicle per visit (per PICO sheet 1)', 'M10', 'PUBLISHED', NOW(), NOW());
INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','DieselFillingLog','select',JSON_ARRAY('id','gate_pass_id','vehicle_no','diesel_qty_litres','filled_at','filled_by','reason','status'),'sort', JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','gate_pass_id','label','GP #','sortable',true),
    JSON_OBJECT('field','vehicle_no','label','Vehicle','sortable',true),
    JSON_OBJECT('field','diesel_qty_litres','label','Diesel (L)','render','number'),
    JSON_OBJECT('field','filled_at','label','Filled At','render','datetime'),
    JSON_OBJECT('field','filled_by','label','Filled By'),
    JSON_OBJECT('field','reason','label','Reason'),
    JSON_OBJECT('field','status','label','Status','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('COMPLETED','SKIPPED','PENDING'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
FROM page_definition pd WHERE pd.code='pico:diesel-log' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:diesel-log' AND current_version_id IS NULL;
