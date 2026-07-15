-- V61: Remote-configurable edge agent. The Python agent on the Pi polls
-- EARMS every 10 s for its current config (PLC host/port, etc.) and its
-- "acceptance scope" (e.g. "for run #42, accept up to 100 cartons of
-- GIN-1L-IDH; reject everything else"). When this row is updated in
-- EARMS the version bumps and the agent re-applies on next poll.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) m11_agent — registry of every deployed agent (Pi or PC) with current
--     PLC connection params and live config version.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m11_agent (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  agent_id VARCHAR(64) NOT NULL,            -- unique per Pi install
  agent_kind VARCHAR(32) NOT NULL DEFAULT 'conveyor',
  line_id VARCHAR(64) NOT NULL,
  display_name VARCHAR(128) NULL,
  location VARCHAR(128) NULL,
  -- Live PLC / checkweigher connection params (admin edits → agent picks
  -- up on next poll, reconnects socket)
  plc_host VARCHAR(64) NULL,
  plc_port INT NOT NULL DEFAULT 502,
  plc_unit_id INT NOT NULL DEFAULT 1,
  plc_timeout_ms INT NOT NULL DEFAULT 2000,
  -- Bumped by every UPDATE; agent compares to its cached config_version
  config_version INT NOT NULL DEFAULT 1,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  notes VARCHAR(512) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_agent_id (tenant_id, agent_id),
  INDEX ix_agent_line (line_id)
);

-- ---------------------------------------------------------------------------
-- (b) m11_agent_acceptance_scope — what the agent should ACCEPT vs REJECT
--     LOCALLY (without round-tripping to EARMS for every box). Set per
--     active packing run / SO. Agent caches and enforces; EARMS still gets
--     the box-event POST for the audit log.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m11_agent_acceptance_scope (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  agent_id VARCHAR(64) NOT NULL,
  scope_kind VARCHAR(32) NOT NULL,          -- PACKING_RUN | SALES_ORDER | DISPATCH | OPEN
  scope_ref_id BIGINT NULL,                 -- m5_packing_run.id / m5_sales_order.id / etc.
  expected_sku VARCHAR(64) NULL,            -- accept only this SKU (NULL = any)
  expected_offer VARCHAR(32) NULL,          -- accept only this offer variant
  expected_barcode VARCHAR(64) NULL,        -- accept only this barcode
  max_pieces INT NULL,                      -- hard cap; reject the 101st piece
  current_pieces INT NOT NULL DEFAULT 0,    -- running count (EARMS updates as boxes accepted)
  weight_min_kg DECIMAL(8,3) NULL,          -- inclusive
  weight_max_kg DECIMAL(8,3) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  ttl_s INT NOT NULL DEFAULT 60,            -- agent caches for this many seconds
  notes VARCHAR(512) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX ix_scope_agent_active (agent_id, active)
);

-- ---------------------------------------------------------------------------
-- (c) Extend m11_conveyor_command with new agent-level commands:
--     RESTART_AGENT (exec self), RECONNECT_PLC (rebuild Modbus socket)
-- ---------------------------------------------------------------------------
-- (kind is already varchar(32), no schema change needed)

-- ---------------------------------------------------------------------------
-- (d) Seed registry row for the demo agent + an acceptance scope:
--     "PACK-LINE-1: accept GIN-1L-IDH up to 100 cartons; reject 101+"
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO m11_agent
  (tenant_id, agent_id, agent_kind, line_id, display_name, location,
   plc_host, plc_port, plc_unit_id, plc_timeout_ms, config_version, enabled)
VALUES
  (1, 'pico-line-1-agent', 'conveyor', 'PACK-LINE-1',
   'Pico Pack Line 1 Conveyor', 'Plant 1 — Packing Bay A',
   '192.168.1.50', 502, 1, 2000, 1, 1);

INSERT INTO m11_agent_acceptance_scope
  (tenant_id, agent_id, scope_kind, scope_ref_id, expected_sku,
   expected_offer, max_pieces, current_pieces, weight_min_kg, weight_max_kg,
   active, ttl_s, notes)
VALUES
  (1, 'pico-line-1-agent', 'PACKING_RUN', 1, 'GIN-1L-IDH', 'STANDARD',
   100, 0, 11.0, 14.0, 1, 60,
   'Demo: 100 cartons of Aria Gingelly 1L, weight 11-14 kg per carton.');

-- ---------------------------------------------------------------------------
-- (e) Entity registry + page-defs so admins can manage these from the UI
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, read_role, write_role, approver_role) VALUES
  ('Agent','Edge Agents','m11_agent','M11','*','Manager,TenantAdmin','Manager'),
  ('AgentAcceptanceScope','Agent Acceptance Scope','m11_agent_acceptance_scope','M11','*','Manager,TenantAdmin','Manager');

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('Agent','agent_id','Agent ID','STRING',1,1,1,1,'text',NULL),
  ('Agent','agent_kind','Type','STRING',1,1,0,1,'select','["conveyor","weighbridge","label_printer","camera"]'),
  ('Agent','line_id','Line','STRING',1,1,1,1,'text',NULL),
  ('Agent','display_name','Display Name','STRING',1,1,1,0,'text',NULL),
  ('Agent','location','Location','STRING',1,1,1,0,'text',NULL),
  ('Agent','plc_host','PLC Host / IP','STRING',0,1,1,0,'text',NULL),
  ('Agent','plc_port','PLC Port','INT',0,1,0,0,'number',NULL),
  ('Agent','plc_unit_id','Modbus Unit ID','INT',0,1,0,0,'number',NULL),
  ('Agent','plc_timeout_ms','Timeout (ms)','INT',0,1,0,0,'number',NULL),
  ('Agent','config_version','Config Ver','INT',0,1,0,0,'number',NULL),
  ('Agent','enabled','Enabled','BOOLEAN',1,1,0,0,'checkbox',NULL),
  ('Agent','notes','Notes','TEXT',0,0,1,0,'textarea',NULL),
  ('AgentAcceptanceScope','agent_id','Agent','STRING',1,1,1,1,'text',NULL),
  ('AgentAcceptanceScope','scope_kind','Scope','STRING',1,1,0,1,'select','["PACKING_RUN","SALES_ORDER","DISPATCH","OPEN"]'),
  ('AgentAcceptanceScope','scope_ref_id','Ref #','LONG',1,1,0,0,'number',NULL),
  ('AgentAcceptanceScope','expected_sku','Expected SKU','STRING',1,1,1,0,'text',NULL),
  ('AgentAcceptanceScope','expected_offer','Expected Offer','STRING',1,1,0,0,'select','["STANDARD","BUY1GET1","10PCT_OFF","20PCT_OFF","FESTIVE_PACK","COMBO_2L_500ML"]'),
  ('AgentAcceptanceScope','expected_barcode','Expected Barcode','STRING',1,1,1,0,'text',NULL),
  ('AgentAcceptanceScope','max_pieces','Max Pieces','INT',0,1,0,0,'number',NULL),
  ('AgentAcceptanceScope','current_pieces','Current Pieces','INT',0,1,0,0,'number',NULL),
  ('AgentAcceptanceScope','weight_min_kg','Weight Min (kg)','DECIMAL',0,1,0,0,'number',NULL),
  ('AgentAcceptanceScope','weight_max_kg','Weight Max (kg)','DECIMAL',0,1,0,0,'number',NULL),
  ('AgentAcceptanceScope','active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL),
  ('AgentAcceptanceScope','ttl_s','Cache TTL (s)','INT',0,1,0,0,'number',NULL),
  ('AgentAcceptanceScope','notes','Notes','TEXT',0,0,1,0,'textarea',NULL);

INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1,'pico:agents','Edge Agents','Every deployed agent (Pi or PC). Edit row → agent picks up new PLC host/port / enable flag on next 10-s poll.','M11','PUBLISHED',NOW(),NOW()),
       (1,'pico:agent-scope','Acceptance Scopes','Per-agent business rules: accept only this SKU, cap at N pieces, weight band. Agent enforces locally without round-trip per box.','M11','PUBLISHED',NOW(),NOW());

INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','Agent','select',JSON_ARRAY('id','agent_id','agent_kind','line_id','display_name','location','plc_host','plc_port','config_version','enabled','updated_at'),'sort',JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','agent_id','label','Agent ID','sortable',true),
    JSON_OBJECT('field','agent_kind','label','Type','sortable',true,'render','badge','colorMap',JSON_OBJECT('conveyor','blue','weighbridge','purple','label_printer','green','camera','amber')),
    JSON_OBJECT('field','line_id','label','Line','sortable',true),
    JSON_OBJECT('field','display_name','label','Name'),
    JSON_OBJECT('field','location','label','Location'),
    JSON_OBJECT('field','plc_host','label','PLC Host'),
    JSON_OBJECT('field','plc_port','label','Port','render','number'),
    JSON_OBJECT('field','config_version','label','Cfg Ver','render','number'),
    JSON_OBJECT('field','enabled','label','Enabled','render','badge','colorMap',JSON_OBJECT('1','green','0','red')),
    JSON_OBJECT('field','updated_at','label','Updated','render','datetime','sortable',true)),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','agent_kind','label','Type','type','multi-select','options',JSON_ARRAY('conveyor','weighbridge','label_printer','camera')),
    JSON_OBJECT('field','enabled','label','Enabled','type','bool-toggle'),
    JSON_OBJECT('field','_q','label','Search','type','text-contains','applyTo',JSON_ARRAY('agent_id','line_id','display_name','plc_host'))),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','🔄 Restart Agent','action','entity.update-field','args',JSON_OBJECT('field','config_version','value','999')),
    JSON_OBJECT('label','Audit','action','page.open','page','pico:audit-log')),
  'pagination', JSON_OBJECT('pageSize',50)
)
FROM page_definition pd WHERE pd.code='pico:agents' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:agents' AND current_version_id IS NULL;

INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','AgentAcceptanceScope','select',JSON_ARRAY('id','agent_id','scope_kind','scope_ref_id','expected_sku','expected_offer','max_pieces','current_pieces','weight_min_kg','weight_max_kg','active','ttl_s','updated_at'),'sort',JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','agent_id','label','Agent','sortable',true),
    JSON_OBJECT('field','scope_kind','label','Scope','sortable',true,'render','badge','colorMap',JSON_OBJECT('PACKING_RUN','blue','SALES_ORDER','green','DISPATCH','purple','OPEN','grey')),
    JSON_OBJECT('field','scope_ref_id','label','Ref #','render','number'),
    JSON_OBJECT('field','expected_sku','label','SKU','sortable',true),
    JSON_OBJECT('field','expected_offer','label','Offer'),
    JSON_OBJECT('field','max_pieces','label','Max','render','number'),
    JSON_OBJECT('field','current_pieces','label','Current','render','number'),
    JSON_OBJECT('field','weight_min_kg','label','Wt Min','render','number'),
    JSON_OBJECT('field','weight_max_kg','label','Wt Max','render','number'),
    JSON_OBJECT('field','active','label','Active','render','badge','colorMap',JSON_OBJECT('1','green','0','grey')),
    JSON_OBJECT('field','updated_at','label','Updated','render','datetime')),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','scope_kind','label','Scope','type','multi-select','options',JSON_ARRAY('PACKING_RUN','SALES_ORDER','DISPATCH','OPEN')),
    JSON_OBJECT('field','active','label','Active','type','bool-toggle'),
    JSON_OBJECT('field','_q','label','Search Agent / SKU','type','text-contains','applyTo',JSON_ARRAY('agent_id','expected_sku','expected_barcode'))),
  'pagination', JSON_OBJECT('pageSize',50)
)
FROM page_definition pd WHERE pd.code='pico:agent-scope' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:agent-scope' AND current_version_id IS NULL;
