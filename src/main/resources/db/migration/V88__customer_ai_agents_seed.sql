-- V88 — Seed the 4 customer AI agents into ai_agent so the dispatcher
-- discovers them at startup and the substrate renders their config pages.
--
-- Each agent has a Java implementation under com.aria.ai.runtime.*
-- annotated with @AiAgent(code=...). The AiAgentDispatcher wires them up
-- automatically. This migration adds the DB row so mode + confidence
-- threshold + escalation rules are tenant-configurable.
--
-- Idempotent — safe to reapply.

-- Customer page-designer agent
INSERT INTO ai_agent
  (tenant_id, code, display_name, category, mode, model, kind, population,
   input_schema_json, output_schema_json, guardrails_json,
   escalation_role, confidence_threshold, status, created_at, updated_at)
SELECT 1, 'customer.page-designer', 'Page designer (customer)', 'PLANNING',
       'SUPERVISED', 'TBD-LlmProxy', 'LLM', 'RUNTIME',
       JSON_OBJECT(
         'type', 'object',
         'properties', JSON_OBJECT(
           'prompt', JSON_OBJECT('type', 'string', 'description', 'Natural-language description of the page needed'),
           'entity', JSON_OBJECT('type', 'string', 'description', 'Optional — the registered entity this page reads/writes')
         ),
         'required', JSON_ARRAY('prompt')
       ),
       JSON_OBJECT(
         'action', 'SAVE',
         'actionArgs', JSON_OBJECT(
           'pageCode', 'string', 'category', 'string', 'kind', 'string',
           'entity', 'string', 'bodyJson', 'string (JSON)', 'proposedTitle', 'string'
         )
       ),
       JSON_OBJECT(
         'rate_limit_per_hour', 20,
         'max_tokens_per_call', 4000,
         'blocked_actions', JSON_ARRAY('delete', 'workflow.transition')
       ),
       'PLANT_ADMIN', 0.80, 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ai_agent WHERE tenant_id=1 AND code='customer.page-designer');

-- Customer field-designer agent
INSERT INTO ai_agent
  (tenant_id, code, display_name, category, mode, model, kind, population,
   input_schema_json, output_schema_json, guardrails_json,
   escalation_role, confidence_threshold, status, created_at, updated_at)
SELECT 1, 'customer.field-designer', 'Field designer (customer)', 'PLANNING',
       'SUPERVISED', 'TBD-LlmProxy', 'LLM', 'RUNTIME',
       JSON_OBJECT(
         'type', 'object',
         'properties', JSON_OBJECT(
           'entityCode', JSON_OBJECT('type', 'string', 'description', 'Entity_registry.code to add fields to'),
           'fieldsDescription', JSON_OBJECT('type', 'string', 'description', 'Comma-separated natural-language field descriptions')
         ),
         'required', JSON_ARRAY('entityCode', 'fieldsDescription')
       ),
       JSON_OBJECT(
         'action', 'SAVE',
         'actionArgs', JSON_OBJECT(
           'entityCode', 'string', 'skippedExisting', 'number',
           'fields', 'array of { field_code, display_label, data_type, widget_kind, required }'
         )
       ),
       JSON_OBJECT(
         'rate_limit_per_hour', 20,
         'max_tokens_per_call', 3000,
         'blocked_actions', JSON_ARRAY('delete')
       ),
       'PLANT_ADMIN', 0.80, 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ai_agent WHERE tenant_id=1 AND code='customer.field-designer');

-- Customer workflow-designer agent
INSERT INTO ai_agent
  (tenant_id, code, display_name, category, mode, model, kind, population,
   input_schema_json, output_schema_json, guardrails_json,
   escalation_role, confidence_threshold, status, created_at, updated_at)
SELECT 1, 'customer.workflow-designer', 'Workflow designer (customer)', 'PLANNING',
       'SUPERVISED', 'TBD-LlmProxy', 'LLM', 'RUNTIME',
       JSON_OBJECT(
         'type', 'object',
         'properties', JSON_OBJECT(
           'description', JSON_OBJECT('type', 'string', 'description', 'Natural-language process description (e.g. "submit → review → approve")')
         ),
         'required', JSON_ARRAY('description')
       ),
       JSON_OBJECT(
         'action', 'SAVE',
         'actionArgs', JSON_OBJECT(
           'workflowCode', 'string', 'stateCount', 'number', 'transitionCount', 'number',
           'hasApprovalBranch', 'boolean', 'bodyJson', 'string (JSON)'
         )
       ),
       JSON_OBJECT(
         'rate_limit_per_hour', 10,
         'max_tokens_per_call', 4000,
         'blocked_actions', JSON_ARRAY('delete')
       ),
       'PLANT_ADMIN', 0.75, 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ai_agent WHERE tenant_id=1 AND code='customer.workflow-designer');

-- Customer decision-analyzer agent
INSERT INTO ai_agent
  (tenant_id, code, display_name, category, mode, model, kind, population,
   input_schema_json, output_schema_json, guardrails_json,
   escalation_role, confidence_threshold, status, created_at, updated_at)
SELECT 1, 'customer.decision-analyzer', 'Decision analyzer (customer)', 'EXPLAIN',
       'SUPERVISED', 'TBD-LlmProxy', 'HYBRID', 'RUNTIME',
       JSON_OBJECT(
         'type', 'object',
         'properties', JSON_OBJECT(
           'question', JSON_OBJECT('type', 'string', 'description', 'Analytical question grounded in tenant data'),
           'entityCode', JSON_OBJECT('type', 'string', 'description', 'Optional — force analysis against a specific entity')
         ),
         'required', JSON_ARRAY('question')
       ),
       JSON_OBJECT(
         'action', 'NOTIFY',
         'actionArgs', JSON_OBJECT(
           'question', 'string', 'answer', 'string (LLM narrative)',
           'intent', 'string enum', 'evidence', 'array of rows', 'evidenceRowCount', 'number'
         )
       ),
       JSON_OBJECT(
         'rate_limit_per_hour', 50,
         'max_tokens_per_call', 6000,
         'blocked_actions', JSON_ARRAY('delete', 'workflow.transition', 'save')
       ),
       'PLANT_ADMIN', 0.65, 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ai_agent WHERE tenant_id=1 AND code='customer.decision-analyzer');


-- Register a stamped landing page for the customer AI console
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'ai:customer-console', 'AI Assistant',
       'Ask the platform to design pages, fields, workflows, or answer analytical questions — grounded in your own data.',
       'AI', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='ai:customer-console');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'layout',
    'layout', JSON_ARRAY(
      JSON_OBJECT('kind', 'Markdown', 'props', JSON_OBJECT(
        'text', '## AI Assistant

Ask EARMS to design pages, fields, workflows, or answer analytical questions. All proposals require your confirmation before they apply.

**Available agents:**
- **Page designer** — "I need a list page for suppliers with name and gstin"
- **Field designer** — "add fields: name, gstin, contact number, is_active checkbox"
- **Workflow designer** — "submit → review → approve or reject"
- **Decision analyzer** — "which orders are overdue by more than 7 days?"'
      )),
      JSON_OBJECT('kind', 'ListView', 'props', JSON_OBJECT(
        'title', 'Recent AI decisions on your data',
        'dataSource', JSON_OBJECT(
          'type', 'entity', 'entity', 'AiAgentRun',
          'select', JSON_ARRAY('id', 'agent_id', 'triggered_by', 'started_at', 'action_taken', 'operator_id')
        ),
        'pageSize', 20
      ))
    )
  ),
  'V88 — customer AI console', NOW()
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:customer-console'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id=p.id AND v.version=1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:customer-console' AND p.current_version_id IS NULL;
