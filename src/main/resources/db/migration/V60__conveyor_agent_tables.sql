-- V60: Backing tables for the LAN-side conveyor edge agent.
-- (a) m11_conveyor_command — async commands queued by EARMS for the agent
--     to pick up on its 2-second poll. Set ack_at when the agent confirms.
-- (b) m11_agent_heartbeat — last-seen timestamp + queue depth per agent
--     line. Dashboards alert when last_ts is older than 60 s.

CREATE TABLE IF NOT EXISTS m11_conveyor_command (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  line_id VARCHAR(64) NOT NULL,
  kind VARCHAR(32) NOT NULL,           -- HALT | RESUME | SET_SPEED
  rpm INT NULL,                        -- for SET_SPEED only
  reason VARCHAR(255) NULL,            -- why was this issued (audit)
  issued_by BIGINT NULL,
  issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ack_at TIMESTAMP NULL,
  ack_result VARCHAR(128) NULL,
  INDEX ix_ccmd_line_pending (line_id, ack_at)
);

CREATE TABLE IF NOT EXISTS m11_agent_heartbeat (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  agent_kind VARCHAR(32) NOT NULL,     -- conveyor | weighbridge | label_printer
  line_id VARCHAR(64) NOT NULL,
  queue_depth INT NOT NULL DEFAULT 0,
  last_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_agent_line (tenant_id, agent_kind, line_id)
);

-- planned_total_pieces column on m5_packing_run so the dispatch-qty
-- guard in ConveyorAgentController can compare scans-so-far vs planned.
ALTER TABLE m5_packing_run
  ADD COLUMN IF NOT EXISTS planned_total_pieces INT NULL AFTER status;

INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, read_role, write_role, approver_role) VALUES
  ('ConveyorCommand','Conveyor Commands','m11_conveyor_command','M11','*','Manager,TenantAdmin','Manager'),
  ('AgentHeartbeat','Edge Agent Heartbeats','m11_agent_heartbeat','M11','*','TenantAdmin',NULL);

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('ConveyorCommand','line_id','Line','STRING',1,1,1,1,'text',NULL),
  ('ConveyorCommand','kind','Command','STRING',1,1,0,1,'select','["HALT","RESUME","SET_SPEED"]'),
  ('ConveyorCommand','rpm','RPM','INT',0,1,0,0,'number',NULL),
  ('ConveyorCommand','reason','Reason','STRING',0,0,1,0,'text',NULL),
  ('ConveyorCommand','issued_at','Issued','DATETIME',1,1,0,0,'datetime',NULL),
  ('ConveyorCommand','ack_at','Acknowledged','DATETIME',1,1,0,0,'datetime',NULL),
  ('ConveyorCommand','ack_result','Result','STRING',0,0,1,0,'text',NULL),
  ('AgentHeartbeat','agent_kind','Agent Type','STRING',1,1,1,1,'select','["conveyor","weighbridge","label_printer"]'),
  ('AgentHeartbeat','line_id','Line','STRING',1,1,1,1,'text',NULL),
  ('AgentHeartbeat','queue_depth','Offline Queue','INT',0,1,0,0,'number',NULL),
  ('AgentHeartbeat','last_ts','Last Seen','DATETIME',1,1,0,0,'datetime',NULL);

-- Page-defs for the two new lists
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1,'pico:conveyor-commands','Conveyor Commands','Outbound commands EARMS has issued to edge agents. Agent acks within 2 seconds typically.','M11','PUBLISHED',NOW(),NOW()),
       (1,'pico:agent-heartbeats','Edge Agent Heartbeats','Last-seen timestamp + offline-queue depth per agent. Alert in dashboard when last_ts > 60 s.','M11','PUBLISHED',NOW(),NOW());

INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','ConveyorCommand','select',JSON_ARRAY('id','line_id','kind','rpm','reason','issued_at','ack_at','ack_result'),'sort',JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','line_id','label','Line','sortable',true),
    JSON_OBJECT('field','kind','label','Command','sortable',true,'render','badge','colorMap',JSON_OBJECT('HALT','red','RESUME','green','SET_SPEED','blue')),
    JSON_OBJECT('field','rpm','label','RPM','render','number'),
    JSON_OBJECT('field','reason','label','Reason'),
    JSON_OBJECT('field','issued_at','label','Issued','render','datetime'),
    JSON_OBJECT('field','ack_at','label','Acknowledged','render','datetime'),
    JSON_OBJECT('field','ack_result','label','Result')),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','line_id','label','Line','type','text-contains','applyTo',JSON_ARRAY('line_id')),
    JSON_OBJECT('field','kind','label','Command','type','multi-select','options',JSON_ARRAY('HALT','RESUME','SET_SPEED'))),
  'pagination', JSON_OBJECT('pageSize',25)
)
FROM page_definition pd WHERE pd.code='pico:conveyor-commands' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:conveyor-commands' AND current_version_id IS NULL;

INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','AgentHeartbeat','select',JSON_ARRAY('id','agent_kind','line_id','queue_depth','last_ts')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','agent_kind','label','Type','sortable',true,'render','badge','colorMap',JSON_OBJECT('conveyor','blue','weighbridge','purple','label_printer','green')),
    JSON_OBJECT('field','line_id','label','Line','sortable',true),
    JSON_OBJECT('field','queue_depth','label','Offline Queue','render','number'),
    JSON_OBJECT('field','last_ts','label','Last Seen','render','datetime','sortable',true)),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','agent_kind','label','Type','type','multi-select','options',JSON_ARRAY('conveyor','weighbridge','label_printer'))),
  'pagination', JSON_OBJECT('pageSize',50)
)
FROM page_definition pd WHERE pd.code='pico:agent-heartbeats' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:agent-heartbeats' AND current_version_id IS NULL;
