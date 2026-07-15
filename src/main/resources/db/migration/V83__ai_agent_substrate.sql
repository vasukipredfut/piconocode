-- V83 — AI Agent substrate.
-- Introduces AI agents as first-class entities in EARMS.
-- Every table here is registered in entity_registry + entity_field_registry so
-- the M0 page substrate auto-renders CRUD pages.
--
-- Tables:
--   ai_agent           — one row per agent (SAFETY/QUALITY/... × SHADOW..AUTONOMOUS)
--   ai_agent_prompt    — versioned system + user prompt templates
--   ai_agent_tool      — allowlisted tools an agent may call
--   ai_agent_binding   — where an agent fires from (workflow / sensor / cron / page)
--   ai_agent_run       — full audit trail of every invocation
--   ai_agent_promotion — mode-change (SHADOW→SUPERVISED→AUTONOMOUS) history
--
-- Substrate pages stamped:
--   ai:agents-list        (list of ai_agent)
--   ai:runs-list          (audit trail)
--   ai:bindings-list      (where agents fire)
--   ai:promotions-list    (mode-change log)
--   ai:kill-switch        (emergency stop layout page)
--
-- Idempotent: uses INSERT IGNORE / NOT EXISTS / CREATE TABLE IF NOT EXISTS.

-- ==========================================================================
-- 1. ai_agent
-- ==========================================================================
CREATE TABLE IF NOT EXISTS ai_agent (
  id                    BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id             BIGINT NOT NULL,
  code                  VARCHAR(64) NOT NULL,
  display_name          VARCHAR(128) NOT NULL,
  category              VARCHAR(16) NOT NULL,
  mode                  VARCHAR(16) NOT NULL DEFAULT 'SHADOW',
  model                 VARCHAR(64) NOT NULL,
  kind                  VARCHAR(16) NOT NULL DEFAULT 'LLM',
  population            VARCHAR(16) NOT NULL DEFAULT 'RUNTIME',
  input_schema_json     JSON NULL,
  output_schema_json    JSON NULL,
  guardrails_json       JSON NULL,
  prompt_version_id     BIGINT NULL,
  escalation_role       VARCHAR(64) NULL,
  confidence_threshold  DECIMAL(3,2) NOT NULL DEFAULT 0.75,
  status                VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_ai_agent_tenant_code (tenant_id, code),
  KEY ix_ai_agent_tenant (tenant_id),
  KEY ix_ai_agent_category (category),
  KEY ix_ai_agent_mode (mode),
  KEY ix_ai_agent_status (status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ==========================================================================
-- 2. ai_agent_prompt
-- ==========================================================================
CREATE TABLE IF NOT EXISTS ai_agent_prompt (
  id             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  agent_id       BIGINT NOT NULL,
  version        INT NOT NULL,
  system_prompt  TEXT NOT NULL,
  user_template  TEXT NOT NULL,
  tools_json     JSON NULL,
  status         VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  published_at   DATETIME NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_ai_agent_prompt_agent_version (agent_id, version),
  KEY ix_ai_agent_prompt_agent (agent_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ==========================================================================
-- 3. ai_agent_tool (registry of allowed tools)
-- ==========================================================================
CREATE TABLE IF NOT EXISTS ai_agent_tool (
  code          VARCHAR(64) NOT NULL PRIMARY KEY,
  handler_bean  VARCHAR(128) NOT NULL,
  schema_json   JSON NOT NULL,
  safety        VARCHAR(16) NOT NULL DEFAULT 'READ'
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO ai_agent_tool (code, handler_bean, schema_json, safety) VALUES
  ('datasource.query',    'datasourceQueryToolHandler',
      JSON_OBJECT('name','datasource.query','description','Run a safe SELECT via /api/m0/datasource/query',
        'parameters', JSON_OBJECT('type','object',
          'properties', JSON_OBJECT(
            'entity', JSON_OBJECT('type','string'),
            'select', JSON_OBJECT('type','array','items', JSON_OBJECT('type','string')),
            'where',  JSON_OBJECT('type','object'),
            'limit',  JSON_OBJECT('type','integer'))),
        'required', JSON_ARRAY('entity')), 'READ'),
  ('workflow.transition', 'workflowTransitionToolHandler',
      JSON_OBJECT('name','workflow.transition','description','Move a workflow instance to a new state',
        'parameters', JSON_OBJECT('type','object',
          'properties', JSON_OBJECT(
            'instanceId', JSON_OBJECT('type','integer'),
            'toState',    JSON_OBJECT('type','string'),
            'note',       JSON_OBJECT('type','string')),
          'required', JSON_ARRAY('instanceId','toState'))), 'WRITE'),
  ('notify.toast',        'notifyToastToolHandler',
      JSON_OBJECT('name','notify.toast','description','Show a toast/notification to the operator',
        'parameters', JSON_OBJECT('type','object',
          'properties', JSON_OBJECT(
            'severity', JSON_OBJECT('type','string','enum', JSON_ARRAY('info','warn','error')),
            'message',  JSON_OBJECT('type','string')),
          'required', JSON_ARRAY('message'))), 'NOTIFY'),
  ('save',                'entitySaveToolHandler',
      JSON_OBJECT('name','save','description','Insert or update a row in a registered entity',
        'parameters', JSON_OBJECT('type','object',
          'properties', JSON_OBJECT(
            'entity', JSON_OBJECT('type','string'),
            'id',     JSON_OBJECT('type','integer'),
            'values', JSON_OBJECT('type','object')),
          'required', JSON_ARRAY('entity','values'))), 'WRITE'),
  ('entity.get',          'entityGetToolHandler',
      JSON_OBJECT('name','entity.get','description','Fetch a single row from a registered entity by id',
        'parameters', JSON_OBJECT('type','object',
          'properties', JSON_OBJECT(
            'entity', JSON_OBJECT('type','string'),
            'id',     JSON_OBJECT('type','integer')),
          'required', JSON_ARRAY('entity','id'))), 'READ');

-- ==========================================================================
-- 4. ai_agent_binding
-- ==========================================================================
CREATE TABLE IF NOT EXISTS ai_agent_binding (
  id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id     BIGINT NOT NULL,
  agent_id      BIGINT NOT NULL,
  trigger_kind  VARCHAR(32) NOT NULL,
  trigger_ref   VARCHAR(256) NOT NULL,
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  KEY ix_ai_agent_binding_tenant (tenant_id),
  KEY ix_ai_agent_binding_agent (agent_id),
  KEY ix_ai_agent_binding_trigger (trigger_kind, trigger_ref)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ==========================================================================
-- 5. ai_agent_run (audit trail)
-- ==========================================================================
CREATE TABLE IF NOT EXISTS ai_agent_run (
  id                    BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id             BIGINT NOT NULL,
  agent_id              BIGINT NOT NULL,
  binding_id            BIGINT NULL,
  workflow_instance_id  BIGINT NULL,
  triggered_by          VARCHAR(64) NOT NULL,
  started_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  finished_at           DATETIME NULL,
  inputs_json           JSON NULL,
  outputs_json          JSON NULL,
  mode_at_run           VARCHAR(16) NOT NULL,
  action_taken          VARCHAR(24) NOT NULL DEFAULT 'PROPOSED',
  operator_id           BIGINT NULL,
  override_reason       TEXT NULL,
  error                 TEXT NULL,
  KEY ix_ai_agent_run_recent (tenant_id, agent_id, started_at DESC),
  KEY ix_ai_agent_run_binding (binding_id),
  KEY ix_ai_agent_run_workflow (workflow_instance_id),
  KEY ix_ai_agent_run_action (action_taken)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ==========================================================================
-- 6. ai_agent_promotion (mode-change history)
-- ==========================================================================
CREATE TABLE IF NOT EXISTS ai_agent_promotion (
  id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  agent_id      BIGINT NOT NULL,
  from_mode     VARCHAR(16) NOT NULL,
  to_mode       VARCHAR(16) NOT NULL,
  approved_by   BIGINT NOT NULL,
  approved_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reason        TEXT NULL,
  metrics_json  JSON NULL,
  KEY ix_ai_agent_promotion_agent (agent_id),
  KEY ix_ai_agent_promotion_when (approved_at)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ==========================================================================
-- entity_registry — register all 6 entities under category='AI'
-- ==========================================================================
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'AiAgent','AI Agent','ai_agent','LLM / ML agents governed by the SHADOW→AUTONOMOUS ladder.','AI' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='AiAgent');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'AiAgentPrompt','AI Agent Prompt','ai_agent_prompt','Versioned system + user prompt templates for each agent.','AI' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='AiAgentPrompt');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'AiAgentTool','AI Agent Tool','ai_agent_tool','Registry of tools (function calls) an agent is allowed to invoke.','AI' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='AiAgentTool');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'AiAgentBinding','AI Agent Binding','ai_agent_binding','Where an agent fires from — workflow transition, sensor threshold, cron, page.','AI' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='AiAgentBinding');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'AiAgentRun','AI Agent Run','ai_agent_run','Append-only audit trail of every agent invocation, inputs, outputs, action taken.','AI' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='AiAgentRun');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'AiAgentPromotion','AI Agent Promotion','ai_agent_promotion','Mode-change history — who promoted an agent from SHADOW to SUPERVISED etc.','AI' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='AiAgentPromotion');

-- ==========================================================================
-- entity_field_registry — AiAgent
-- ==========================================================================
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','id','ID','LONG',FALSE,TRUE,FALSE,FALSE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','tenant_id','Tenant','LONG',TRUE,TRUE,FALSE,TRUE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='tenant_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','code','Code','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','display_name','Name','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='display_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgent','category','Category','STRING',TRUE,TRUE,FALSE,TRUE,'select','["SAFETY","QUALITY","MAINTENANCE","PLANNING","EXPLAIN","DEV"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='category');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgent','mode','Mode','STRING',TRUE,TRUE,FALSE,TRUE,'select','["SHADOW","SUPERVISED","GUARDED","AUTONOMOUS","RETIRED"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='mode');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','model','Model','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='model');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgent','kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE,'select','["LLM","ML_MODEL","RULE_CEP","HYBRID"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgent','population','Population','STRING',TRUE,TRUE,FALSE,TRUE,'select','["DEV","RUNTIME"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='population');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','input_schema_json','Input Schema','JSON',FALSE,FALSE,FALSE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='input_schema_json');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','output_schema_json','Output Schema','JSON',FALSE,FALSE,FALSE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='output_schema_json');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','guardrails_json','Guardrails','JSON',FALSE,FALSE,FALSE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='guardrails_json');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','prompt_version_id','Active Prompt','LONG',TRUE,TRUE,FALSE,FALSE,'entity-ref' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='prompt_version_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','escalation_role','Escalation Role','STRING',TRUE,TRUE,TRUE,FALSE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='escalation_role');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','confidence_threshold','Confidence Threshold','DECIMAL',TRUE,TRUE,FALSE,TRUE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='confidence_threshold');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgent','status','Status','STRING',TRUE,TRUE,FALSE,TRUE,'select','["DRAFT","PUBLISHED","RETIRED"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','created_at','Created','DATETIME',TRUE,TRUE,FALSE,FALSE,'datetime' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='created_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgent','updated_at','Updated','DATETIME',TRUE,TRUE,FALSE,FALSE,'datetime' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgent' AND field_code='updated_at');

-- ==========================================================================
-- entity_field_registry — AiAgentPrompt
-- ==========================================================================
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPrompt','id','ID','LONG',FALSE,TRUE,FALSE,FALSE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPrompt' AND field_code='id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPrompt','agent_id','Agent','LONG',TRUE,TRUE,FALSE,TRUE,'entity-ref' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPrompt' AND field_code='agent_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPrompt','version','Version','LONG',TRUE,TRUE,FALSE,TRUE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPrompt' AND field_code='version');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPrompt','system_prompt','System Prompt','STRING',FALSE,FALSE,TRUE,TRUE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPrompt' AND field_code='system_prompt');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPrompt','user_template','User Template','STRING',FALSE,FALSE,TRUE,TRUE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPrompt' AND field_code='user_template');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPrompt','tools_json','Tools','JSON',FALSE,FALSE,FALSE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPrompt' AND field_code='tools_json');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgentPrompt','status','Status','STRING',TRUE,TRUE,FALSE,TRUE,'select','["DRAFT","PUBLISHED","RETIRED"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPrompt' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPrompt','published_at','Published At','DATETIME',TRUE,TRUE,FALSE,FALSE,'datetime' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPrompt' AND field_code='published_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPrompt','created_at','Created','DATETIME',TRUE,TRUE,FALSE,FALSE,'datetime' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPrompt' AND field_code='created_at');

-- ==========================================================================
-- entity_field_registry — AiAgentTool
-- ==========================================================================
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentTool','code','Code','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentTool' AND field_code='code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentTool','handler_bean','Handler Bean','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentTool' AND field_code='handler_bean');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentTool','schema_json','Schema','JSON',FALSE,FALSE,FALSE,TRUE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentTool' AND field_code='schema_json');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgentTool','safety','Safety','STRING',TRUE,TRUE,FALSE,TRUE,'select','["READ","WRITE","NOTIFY","CRITICAL"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentTool' AND field_code='safety');

-- ==========================================================================
-- entity_field_registry — AiAgentBinding
-- ==========================================================================
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentBinding','id','ID','LONG',FALSE,TRUE,FALSE,FALSE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentBinding' AND field_code='id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentBinding','tenant_id','Tenant','LONG',TRUE,TRUE,FALSE,TRUE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentBinding' AND field_code='tenant_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentBinding','agent_id','Agent','LONG',TRUE,TRUE,FALSE,TRUE,'entity-ref' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentBinding' AND field_code='agent_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgentBinding','trigger_kind','Trigger Kind','STRING',TRUE,TRUE,FALSE,TRUE,'select','["workflow.transition","sensor.threshold","cron","page.action","inline.explain","github.pr.opened","git.commit","ci.test.failed"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentBinding' AND field_code='trigger_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentBinding','trigger_ref','Trigger Ref','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentBinding' AND field_code='trigger_ref');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentBinding','active','Active','BOOLEAN',TRUE,TRUE,FALSE,FALSE,'checkbox' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentBinding' AND field_code='active');

-- ==========================================================================
-- entity_field_registry — AiAgentRun
-- ==========================================================================
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','id','ID','LONG',FALSE,TRUE,FALSE,FALSE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','tenant_id','Tenant','LONG',TRUE,TRUE,FALSE,TRUE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='tenant_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','agent_id','Agent','LONG',TRUE,TRUE,FALSE,TRUE,'entity-ref' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='agent_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','binding_id','Binding','LONG',TRUE,TRUE,FALSE,FALSE,'entity-ref' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='binding_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','workflow_instance_id','Workflow Instance','LONG',TRUE,TRUE,FALSE,FALSE,'entity-ref' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='workflow_instance_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','triggered_by','Triggered By','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='triggered_by');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','started_at','Started','DATETIME',TRUE,TRUE,FALSE,TRUE,'datetime' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='started_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','finished_at','Finished','DATETIME',TRUE,TRUE,FALSE,FALSE,'datetime' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='finished_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','inputs_json','Inputs','JSON',FALSE,FALSE,FALSE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='inputs_json');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','outputs_json','Outputs','JSON',FALSE,FALSE,FALSE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='outputs_json');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgentRun','mode_at_run','Mode At Run','STRING',TRUE,TRUE,FALSE,TRUE,'select','["SHADOW","SUPERVISED","GUARDED","AUTONOMOUS","RETIRED"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='mode_at_run');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgentRun','action_taken','Action','STRING',TRUE,TRUE,FALSE,TRUE,'select','["PROPOSED","AUTO_APPLIED","HUMAN_CONFIRMED","HUMAN_OVERRIDDEN","ESCALATED","FAILED"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='action_taken');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','operator_id','Operator','LONG',TRUE,TRUE,FALSE,FALSE,'entity-ref' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='operator_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','override_reason','Override Reason','STRING',FALSE,FALSE,TRUE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='override_reason');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentRun','error','Error','STRING',FALSE,FALSE,TRUE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentRun' AND field_code='error');

-- ==========================================================================
-- entity_field_registry — AiAgentPromotion
-- ==========================================================================
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPromotion','id','ID','LONG',FALSE,TRUE,FALSE,FALSE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPromotion' AND field_code='id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPromotion','agent_id','Agent','LONG',TRUE,TRUE,FALSE,TRUE,'entity-ref' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPromotion' AND field_code='agent_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgentPromotion','from_mode','From Mode','STRING',TRUE,TRUE,FALSE,TRUE,'select','["SHADOW","SUPERVISED","GUARDED","AUTONOMOUS","RETIRED"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPromotion' AND field_code='from_mode');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'AiAgentPromotion','to_mode','To Mode','STRING',TRUE,TRUE,FALSE,TRUE,'select','["SHADOW","SUPERVISED","GUARDED","AUTONOMOUS","RETIRED"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPromotion' AND field_code='to_mode');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPromotion','approved_by','Approved By','LONG',TRUE,TRUE,FALSE,TRUE,'entity-ref' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPromotion' AND field_code='approved_by');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPromotion','approved_at','Approved At','DATETIME',TRUE,TRUE,FALSE,TRUE,'datetime' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPromotion' AND field_code='approved_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPromotion','reason','Reason','STRING',FALSE,FALSE,TRUE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPromotion' AND field_code='reason');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'AiAgentPromotion','metrics_json','Metrics','JSON',FALSE,FALSE,FALSE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AiAgentPromotion' AND field_code='metrics_json');

-- ==========================================================================
-- Substrate pages
-- ==========================================================================

-- ai:agents-list ------------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'ai:agents-list', 'AI Agents',
       'All AI agents in the plant. Filter by category / mode; open a row to edit prompts, guardrails, and confidence threshold.',
       'AI', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='ai:agents-list');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'AI Agents',
    'entity', 'AiAgent',
    'dataSource', JSON_OBJECT('type','entity','entity','AiAgent'),
    'columns', JSON_ARRAY(
      JSON_OBJECT('field','code','label','Code','sortable',true),
      JSON_OBJECT('field','display_name','label','Name','sortable',true),
      JSON_OBJECT('field','category','label','Category','render','pill'),
      JSON_OBJECT('field','mode','label','Mode','render','pill',
        'colorMap', JSON_OBJECT('SHADOW','grey','SUPERVISED','blue','GUARDED','amber','AUTONOMOUS','green','RETIRED','red')),
      JSON_OBJECT('field','kind','label','Kind'),
      JSON_OBJECT('field','model','label','Model'),
      JSON_OBJECT('field','confidence_threshold','label','Confidence','sortable',true),
      JSON_OBJECT('field','status','label','Status','render','pill'),
      JSON_OBJECT('field','updated_at','label','Updated','sortable',true)
    ),
    'defaultSort', JSON_OBJECT('field','updated_at','dir','desc'),
    'pageSize', 25
  ),
  'V83 — AI agents list', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:agents-list'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:agents-list' AND p.current_version_id IS NULL;

-- ai:runs-list --------------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'ai:runs-list', 'AI Agent Runs',
       'Full audit trail of every AI agent invocation — inputs, outputs, mode at run, action taken, operator override.',
       'AI', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='ai:runs-list');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'AI Agent Runs',
    'entity', 'AiAgentRun',
    'dataSource', JSON_OBJECT('type','entity','entity','AiAgentRun'),
    'columns', JSON_ARRAY(
      JSON_OBJECT('field','started_at','label','Started','sortable',true),
      JSON_OBJECT('field','agent_id','label','Agent'),
      JSON_OBJECT('field','triggered_by','label','Triggered By'),
      JSON_OBJECT('field','mode_at_run','label','Mode','render','pill',
        'colorMap', JSON_OBJECT('SHADOW','grey','SUPERVISED','blue','GUARDED','amber','AUTONOMOUS','green','RETIRED','red')),
      JSON_OBJECT('field','action_taken','label','Action','render','pill',
        'colorMap', JSON_OBJECT('PROPOSED','grey','AUTO_APPLIED','green','HUMAN_CONFIRMED','blue','HUMAN_OVERRIDDEN','amber','ESCALATED','purple','FAILED','red')),
      JSON_OBJECT('field','workflow_instance_id','label','Workflow'),
      JSON_OBJECT('field','operator_id','label','Operator'),
      JSON_OBJECT('field','finished_at','label','Finished','sortable',true)
    ),
    'defaultSort', JSON_OBJECT('field','started_at','dir','desc'),
    'pageSize', 50
  ),
  'V83 — AI agent runs audit trail', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:runs-list'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:runs-list' AND p.current_version_id IS NULL;

-- ai:bindings-list ----------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'ai:bindings-list', 'AI Agent Bindings',
       'Where each agent fires from — workflow transitions, sensor thresholds, cron schedules, page actions.',
       'AI', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='ai:bindings-list');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'AI Agent Bindings',
    'entity', 'AiAgentBinding',
    'dataSource', JSON_OBJECT('type','entity','entity','AiAgentBinding'),
    'columns', JSON_ARRAY(
      JSON_OBJECT('field','agent_id','label','Agent'),
      JSON_OBJECT('field','trigger_kind','label','Trigger Kind','render','pill'),
      JSON_OBJECT('field','trigger_ref','label','Trigger Ref'),
      JSON_OBJECT('field','active','label','Active','render','pill',
        'colorMap', JSON_OBJECT('true','green','false','grey'))
    ),
    'defaultSort', JSON_OBJECT('field','id','dir','desc'),
    'pageSize', 50
  ),
  'V83 — AI agent bindings', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:bindings-list'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:bindings-list' AND p.current_version_id IS NULL;

-- ai:promotions-list --------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'ai:promotions-list', 'AI Agent Promotions',
       'Mode-change log — who promoted an agent from SHADOW → SUPERVISED → AUTONOMOUS, with the metrics that justified it.',
       'AI', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='ai:promotions-list');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'AI Agent Promotions',
    'entity', 'AiAgentPromotion',
    'dataSource', JSON_OBJECT('type','entity','entity','AiAgentPromotion'),
    'columns', JSON_ARRAY(
      JSON_OBJECT('field','approved_at','label','When','sortable',true),
      JSON_OBJECT('field','agent_id','label','Agent'),
      JSON_OBJECT('field','from_mode','label','From','render','pill',
        'colorMap', JSON_OBJECT('SHADOW','grey','SUPERVISED','blue','GUARDED','amber','AUTONOMOUS','green','RETIRED','red')),
      JSON_OBJECT('field','to_mode','label','To','render','pill',
        'colorMap', JSON_OBJECT('SHADOW','grey','SUPERVISED','blue','GUARDED','amber','AUTONOMOUS','green','RETIRED','red')),
      JSON_OBJECT('field','approved_by','label','Approved By'),
      JSON_OBJECT('field','reason','label','Reason')
    ),
    'defaultSort', JSON_OBJECT('field','approved_at','dir','desc'),
    'pageSize', 50
  ),
  'V83 — AI agent promotions log', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:promotions-list'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:promotions-list' AND p.current_version_id IS NULL;

-- ai:kill-switch ------------------------------------------------------------
-- Emergency stop page — big red warning + a WorkflowActionButton that retires
-- every agent, plus a KanbanView grouped by mode so operators can eyeball
-- what's currently running SHADOW vs. AUTONOMOUS.
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'ai:kill-switch', 'AI Kill Switch',
       'Emergency stop. Retires every AI agent instantly. Board below shows what will be affected.',
       'AI', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='ai:kill-switch');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'layout',
    'title', 'AI Kill Switch',
    'layout', JSON_ARRAY(
      JSON_OBJECT('kind','Markdown','props', JSON_OBJECT(
        'severity','error',
        'content', CONCAT(
          '## Emergency stop — retire every AI agent\n\n',
          'Pressing the button below sets every agent''s `mode` to `RETIRED` and marks every binding as `active=false`. ',
          'No agent will fire again until an operator explicitly re-enables it from the AI Agents page. ',
          'Use this only if agents are producing dangerous outputs or the plant is in an unsafe state.\n'
        )
      )),
      JSON_OBJECT('kind','WorkflowActionButton','props', JSON_OBJECT(
        'label','RETIRE ALL AGENTS',
        'severity','error',
        'confirm', JSON_OBJECT(
          'title','Retire every AI agent?',
          'message','This will immediately stop every SHADOW / SUPERVISED / GUARDED / AUTONOMOUS agent. This is reversible but requires re-promotion.'
        ),
        'action', JSON_OBJECT(
          'kind','workflow.transition',
          'workflowCode','AI.KillSwitch',
          'toState','RETIRED'
        )
      )),
      JSON_OBJECT('kind','Section','props', JSON_OBJECT(
        'title','Currently running agents by mode',
        'subtitle','Everything not already RETIRED will be stopped.'
      )),
      JSON_OBJECT('kind','KanbanView','props', JSON_OBJECT(
        'title','Agents by mode',
        'dataSource', JSON_OBJECT(
           'type','entity',
           'entity','AiAgent',
           'select', JSON_ARRAY('id','code','display_name','category','mode','model','status')
        ),
        'groupBy','mode',
        'pageSize', 200,
        'columns', JSON_ARRAY(
          JSON_OBJECT('value','SHADOW',     'label','Shadow',     'color','grey'),
          JSON_OBJECT('value','SUPERVISED', 'label','Supervised', 'color','blue'),
          JSON_OBJECT('value','GUARDED',    'label','Guarded',    'color','amber'),
          JSON_OBJECT('value','AUTONOMOUS', 'label','Autonomous', 'color','green'),
          JSON_OBJECT('value','RETIRED',    'label','Retired',    'color','red')
        ),
        'card', JSON_OBJECT(
          'title','{display_name}',
          'subtitle','{code} · {model}',
          'badges', JSON_ARRAY('category','status')
        )
      ))
    )
  ),
  'V83 — AI kill switch layout', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:kill-switch'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:kill-switch' AND p.current_version_id IS NULL;
