-- V91 — On-prem AI: widen provider dropdown + add Test action to the
-- ai:tenant-credentials settings page.
--
-- Why:
--   Customers deploying EARMS on-premise bring their own LLM host — most
--   speak the OpenAI Chat-Completions wire format (LM Studio, vLLM,
--   LiteLLM, LocalAI, real OpenAI). One `openai-compatible` provider code
--   covers them all; a matching Java adapter ships alongside this
--   migration (OpenAiCompatibleLlmProvider + LlmProxy dispatch).
--
--   The dropdown in V84 offered `anthropic` / `openai` / `gemini` /
--   `aws-bedrock` / `ollama-local` but only Ollama was actually wired.
--   This migration replaces the options with what we truly support today
--   and adds the `openai-compatible` bucket used by every local model
--   host.
--
--   It also adds a Test row action to the settings page so the customer
--   gets an immediate green/red signal that their endpoint + key work,
--   before they save.
--
-- Idempotent — safe to reapply.

-- --------------------------------------------------------------------------
-- 1. Widen the provider dropdown on TenantAiCredential.provider
-- --------------------------------------------------------------------------

UPDATE entity_field_registry
SET options_json = JSON_OBJECT('options', JSON_ARRAY(
    JSON_OBJECT('value','ollama-local',      'label','Ollama (self-hosted, local)'),
    JSON_OBJECT('value','ollama-hosted',     'label','Ollama (self-hosted, remote GPU)'),
    JSON_OBJECT('value','openai',            'label','OpenAI'),
    JSON_OBJECT('value','openai-compatible', 'label','OpenAI-compatible (LM Studio / vLLM / LiteLLM)')
))
WHERE entity_code='TenantAiCredential' AND field_code='provider';

-- --------------------------------------------------------------------------
-- 2. Republish ai:tenant-credentials body — add Test action + notice update
-- --------------------------------------------------------------------------

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 2, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'AI Credentials',
    'description',
      'Point EARMS at your own AI model. Register the provider, endpoint (e.g. http://localhost:1234 for LM Studio, or your on-prem Ollama URL), model name, and optional API key. Press Test on any row to verify the connection.',
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
                      'ollama-local','grey',
                      'ollama-hosted','teal',
                      'openai','green',
                      'openai-compatible','blue')),
       JSON_OBJECT('field','model','label','Model'),
       JSON_OBJECT('field','endpoint','label','Endpoint'),
       JSON_OBJECT('field','max_tokens_per_day','label','Tokens/Day','align','right'),
       JSON_OBJECT('field','cost_cap_paise_per_day','label','Cost Cap (paise)','align','right'),
       JSON_OBJECT('field','kill_switch','label','Kill Switch','render','pill',
                   'colorMap', JSON_OBJECT('true','red','false','green')),
       JSON_OBJECT('field','updated_at','label','Updated')
    ),
    'actions', JSON_ARRAY(
       JSON_OBJECT('code','create', 'label','Add Credential', 'style','primary'),
       JSON_OBJECT('code','edit',   'label','Edit'),
       JSON_OBJECT('code','delete', 'label','Delete',         'style','danger')
    ),
    'rowActions', JSON_ARRAY(
       JSON_OBJECT('code','test',
                   'label','Test',
                   'kind','external.api.call',
                   'method','POST',
                   'url','/api/ai/credentials/{id}/test',
                   'successToast','Connection OK ({latencyMs} ms) — model {model} on {endpoint}',
                   'errorToast','Test failed: {message}')
    ),
    'pageSize', 25,
    'notice', 'Supported providers: ollama-local, ollama-hosted, openai, openai-compatible. Keys are stored on the tenant database only.'
  ),
  'V91 — add Test row action + widen provider list', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:tenant-credentials'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=2);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 2
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:tenant-credentials';
