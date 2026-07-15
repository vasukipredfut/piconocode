-- V84 — Tenant-owned AI credentials + query log.
-- Lets each tenant register their own AI provider + API key + model so that
-- customer AI queries run under the customer's own credentials, not the
-- platform's. Every LLM/embedding call is logged for cost tracking + audit.
--
-- Ships:
--   tenant_ai_credential   — one row per (tenant, provider), api_key encrypted
--                            via com.aria.m11.secrets.EnvelopeCipher
--   tenant_ai_query_log    — every LLM/embedding call for cost + audit
--   entity_registry + entity_field_registry entries for both tables
--   substrate pages:
--     ai:tenant-credentials  — settings page for adding provider + key
--     ai:query-log           — cost + usage dashboard
--
-- Idempotent: uses CREATE TABLE IF NOT EXISTS / INSERT IGNORE / NOT EXISTS.

-- --------------------------------------------------------------------------
-- 1. Tables
-- --------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS tenant_ai_credential (
  id                        BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id                 BIGINT NOT NULL,
  provider                  VARCHAR(32) NOT NULL,
  model                     VARCHAR(128) NOT NULL,
  api_key_ciphertext        TEXT NOT NULL,
  endpoint                  VARCHAR(256) NULL,
  max_tokens_per_day        INT NOT NULL DEFAULT 500000,
  cost_cap_paise_per_day    INT NOT NULL DEFAULT 100000,
  kill_switch               BOOLEAN NOT NULL DEFAULT FALSE,
  created_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_tenant_ai_credential_tenant_provider (tenant_id, provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS tenant_ai_query_log (
  id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id       BIGINT NOT NULL,
  credential_id   BIGINT NOT NULL,
  agent_run_id    BIGINT NULL,
  operator_id     BIGINT NULL,
  called_at       TIMESTAMP NOT NULL,
  kind            VARCHAR(16) NOT NULL,
  tokens_in       INT NOT NULL,
  tokens_out      INT NOT NULL,
  cost_paise      INT NOT NULL,
  prompt_hash     CHAR(64) NULL,
  response_hash   CHAR(64) NULL,
  status          VARCHAR(16) NOT NULL,
  error           TEXT NULL,
  CONSTRAINT fk_tenant_ai_query_log_credential
      FOREIGN KEY (credential_id) REFERENCES tenant_ai_credential(id),
  INDEX idx_tenant_ai_query_log_tenant_called (tenant_id, called_at DESC),
  INDEX idx_tenant_ai_query_log_tenant_credential_called (tenant_id, credential_id, called_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------------------------
-- 2. entity_registry — expose both tables to the generic CRUD substrate
-- --------------------------------------------------------------------------

INSERT INTO entity_registry (code, display_name, table_name, category)
SELECT 'TenantAiCredential', 'Tenant AI Credential', 'tenant_ai_credential', 'AI'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='TenantAiCredential');

INSERT INTO entity_registry (code, display_name, table_name, category)
SELECT 'TenantAiQueryLog', 'Tenant AI Query Log', 'tenant_ai_query_log', 'AI'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='TenantAiQueryLog');

-- --------------------------------------------------------------------------
-- 3. entity_field_registry — TenantAiCredential fields
--    api_key_ciphertext:  password widget, NOT filterable, NOT searchable.
-- --------------------------------------------------------------------------

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','id','ID','NUMBER','number',
       FALSE, TRUE, TRUE, FALSE, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='id');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','tenant_id','Tenant','NUMBER','number',
       TRUE, TRUE, TRUE, FALSE, NULL, 2
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='tenant_id');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','provider','Provider','STRING','select',
       TRUE, TRUE, TRUE, TRUE,
       JSON_OBJECT('options', JSON_ARRAY(
          JSON_OBJECT('value','anthropic',   'label','Anthropic'),
          JSON_OBJECT('value','openai',      'label','OpenAI'),
          JSON_OBJECT('value','gemini',      'label','Google Gemini'),
          JSON_OBJECT('value','ollama-local','label','Ollama (self-hosted)'),
          JSON_OBJECT('value','aws-bedrock', 'label','AWS Bedrock')
       )),
       3
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='provider');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','model','Model','STRING','text',
       TRUE, TRUE, TRUE, TRUE, NULL, 4
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='model');

-- Secret: password widget + NOT filterable + NOT searchable so it never
-- leaks through list APIs, search boxes, or filter chips.
INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','api_key_ciphertext','API Key','STRING','password',
       TRUE, FALSE, FALSE, FALSE,
       JSON_OBJECT('secret', TRUE,
                   'help','Encrypted at rest via EnvelopeCipher. Never displayed in list APIs.'),
       5
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='api_key_ciphertext');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','endpoint','Endpoint','STRING','text',
       FALSE, FALSE, TRUE, TRUE,
       JSON_OBJECT('help','For self-hosted (Ollama) or custom Bedrock endpoints. Leave blank for provider default.'),
       6
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='endpoint');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','max_tokens_per_day','Max Tokens / Day','NUMBER','number',
       TRUE, TRUE, TRUE, FALSE, NULL, 7
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='max_tokens_per_day');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','cost_cap_paise_per_day','Cost Cap / Day (paise)','NUMBER','number',
       TRUE, TRUE, TRUE, FALSE,
       JSON_OBJECT('help','Emergency ceiling — expressed in paise. Default 100000 = Rs 1000.'),
       8
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='cost_cap_paise_per_day');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','kill_switch','Kill Switch','BOOLEAN','toggle',
       TRUE, TRUE, TRUE, FALSE,
       JSON_OBJECT('help','Customer-controlled emergency stop. When on, no AI calls run against this credential.'),
       9
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='kill_switch');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','created_at','Created At','DATETIME','datetime',
       FALSE, TRUE, TRUE, FALSE, NULL, 10
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='created_at');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiCredential','updated_at','Updated At','DATETIME','datetime',
       FALSE, TRUE, TRUE, FALSE, NULL, 11
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiCredential' AND field_code='updated_at');

-- --------------------------------------------------------------------------
-- 4. entity_field_registry — TenantAiQueryLog fields
-- --------------------------------------------------------------------------

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','id','ID','NUMBER','number',
       FALSE, TRUE, TRUE, FALSE, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='id');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','tenant_id','Tenant','NUMBER','number',
       TRUE, TRUE, TRUE, FALSE, NULL, 2
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='tenant_id');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','credential_id','Credential','NUMBER','fk',
       TRUE, TRUE, TRUE, FALSE,
       JSON_OBJECT('refEntity','TenantAiCredential','labelField','provider'),
       3
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='credential_id');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','agent_run_id','Agent Run','NUMBER','fk',
       FALSE, TRUE, TRUE, FALSE,
       JSON_OBJECT('refEntity','AiAgentRun','labelField','id'),
       4
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='agent_run_id');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','operator_id','Operator','NUMBER','number',
       FALSE, TRUE, TRUE, FALSE, NULL, 5
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='operator_id');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','called_at','Called At','DATETIME','datetime',
       TRUE, TRUE, TRUE, FALSE, NULL, 6
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='called_at');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','kind','Kind','STRING','select',
       TRUE, TRUE, TRUE, TRUE,
       JSON_OBJECT('options', JSON_ARRAY(
          JSON_OBJECT('value','CHAT',      'label','Chat'),
          JSON_OBJECT('value','EMBED',     'label','Embed'),
          JSON_OBJECT('value','COMPLETION','label','Completion')
       )),
       7
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='kind');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','tokens_in','Tokens In','NUMBER','number',
       TRUE, TRUE, TRUE, FALSE, NULL, 8
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='tokens_in');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','tokens_out','Tokens Out','NUMBER','number',
       TRUE, TRUE, TRUE, FALSE, NULL, 9
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='tokens_out');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','cost_paise','Cost (paise)','NUMBER','number',
       TRUE, TRUE, TRUE, FALSE,
       JSON_OBJECT('help','Computed from tokens + provider rate card, in paise.'),
       10
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='cost_paise');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','prompt_hash','Prompt Hash','STRING','text',
       FALSE, FALSE, TRUE, TRUE,
       JSON_OBJECT('help','SHA256 of prompt for dedup.'),
       11
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='prompt_hash');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','response_hash','Response Hash','STRING','text',
       FALSE, FALSE, TRUE, TRUE, NULL, 12
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='response_hash');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','status','Status','STRING','select',
       TRUE, TRUE, TRUE, TRUE,
       JSON_OBJECT('options', JSON_ARRAY(
          JSON_OBJECT('value','OK',              'label','OK',              'color','green'),
          JSON_OBJECT('value','ERROR',           'label','Error',           'color','red'),
          JSON_OBJECT('value','RATE_LIMITED',    'label','Rate Limited',    'color','amber'),
          JSON_OBJECT('value','BUDGET_EXCEEDED', 'label','Budget Exceeded', 'color','red')
       )),
       13
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='status');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, sortable, filterable, searchable, options_json, display_order)
SELECT 'TenantAiQueryLog','error','Error','STRING','textarea',
       FALSE, FALSE, FALSE, TRUE, NULL, 14
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='TenantAiQueryLog' AND field_code='error');

-- --------------------------------------------------------------------------
-- 5. Substrate page: ai:tenant-credentials
--    Customer-facing settings page — add / edit provider + key + caps.
-- --------------------------------------------------------------------------

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'ai:tenant-credentials', 'AI Credentials',
       'Register your own AI provider + API key + model. Customer AI queries run under these credentials, not the platform''s.',
       'AI', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='ai:tenant-credentials');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'AI Credentials',
    'dataSource', JSON_OBJECT(
       'type', 'entity',
       'entity', 'TenantAiCredential',
       'select', JSON_ARRAY('id','tenant_id','provider','model','endpoint',
                            'max_tokens_per_day','cost_cap_paise_per_day',
                            'kill_switch','updated_at')
    ),
    'columns', JSON_ARRAY(
       JSON_OBJECT('field','provider','label','Provider',
                   'render','pill',
                   'colorMap', JSON_OBJECT(
                      'anthropic','purple','openai','green','gemini','blue',
                      'ollama-local','grey','aws-bedrock','amber')),
       JSON_OBJECT('field','model','label','Model'),
       JSON_OBJECT('field','endpoint','label','Endpoint'),
       JSON_OBJECT('field','max_tokens_per_day','label','Tokens/Day','align','right'),
       JSON_OBJECT('field','cost_cap_paise_per_day','label','Cost Cap (paise)','align','right'),
       JSON_OBJECT('field','kill_switch','label','Kill Switch','render','pill',
                   'colorMap', JSON_OBJECT('true','red','false','green')),
       JSON_OBJECT('field','updated_at','label','Updated')
    ),
    'actions', JSON_ARRAY(
       JSON_OBJECT('code','create','label','Add Credential','style','primary'),
       JSON_OBJECT('code','edit','label','Edit'),
       JSON_OBJECT('code','delete','label','Delete','style','danger')
    ),
    'pageSize', 25,
    'notice', 'API keys are encrypted at rest via EnvelopeCipher and never returned by list APIs.'
  ),
  'V84 — tenant AI credentials list page', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:tenant-credentials'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:tenant-credentials' AND p.current_version_id IS NULL;

-- --------------------------------------------------------------------------
-- 6. Substrate page: ai:query-log
--    Cost + usage dashboard — filterable by date range + provider.
-- --------------------------------------------------------------------------

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'ai:query-log', 'AI Query Log',
       'Every LLM / embedding call made under your credentials, with token counts, cost in paise, and status.',
       'AI', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='ai:query-log');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'AI Query Log',
    'dataSource', JSON_OBJECT(
       'type', 'entity',
       'entity', 'TenantAiQueryLog',
       'select', JSON_ARRAY('id','called_at','credential_id','kind',
                            'tokens_in','tokens_out','cost_paise','status',
                            'agent_run_id','operator_id')
    ),
    'defaultSort', JSON_OBJECT('field','called_at','dir','desc'),
    'filters', JSON_ARRAY(
       JSON_OBJECT('field','called_at','label','Date Range','widget','daterange'),
       JSON_OBJECT('field','credential_id','label','Provider','widget','fk',
                   'refEntity','TenantAiCredential','labelField','provider'),
       JSON_OBJECT('field','kind','label','Kind','widget','select',
                   'options', JSON_ARRAY('CHAT','EMBED','COMPLETION')),
       JSON_OBJECT('field','status','label','Status','widget','select',
                   'options', JSON_ARRAY('OK','ERROR','RATE_LIMITED','BUDGET_EXCEEDED'))
    ),
    'columns', JSON_ARRAY(
       JSON_OBJECT('field','called_at','label','When'),
       JSON_OBJECT('field','credential_id','label','Credential'),
       JSON_OBJECT('field','kind','label','Kind','render','pill',
                   'colorMap', JSON_OBJECT('CHAT','blue','EMBED','purple','COMPLETION','teal')),
       JSON_OBJECT('field','tokens_in','label','Tokens In','align','right'),
       JSON_OBJECT('field','tokens_out','label','Tokens Out','align','right'),
       JSON_OBJECT('field','cost_paise','label','Cost (paise)','align','right'),
       JSON_OBJECT('field','status','label','Status','render','pill',
                   'colorMap', JSON_OBJECT(
                      'OK','green','ERROR','red',
                      'RATE_LIMITED','amber','BUDGET_EXCEEDED','red')),
       JSON_OBJECT('field','agent_run_id','label','Agent Run'),
       JSON_OBJECT('field','operator_id','label','Operator')
    ),
    'aggregations', JSON_ARRAY(
       JSON_OBJECT('field','tokens_in','fn','sum','label','Total Tokens In'),
       JSON_OBJECT('field','tokens_out','fn','sum','label','Total Tokens Out'),
       JSON_OBJECT('field','cost_paise','fn','sum','label','Total Cost (paise)')
    ),
    'pageSize', 50
  ),
  'V84 — tenant AI query log dashboard', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:query-log'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:query-log' AND p.current_version_id IS NULL;
