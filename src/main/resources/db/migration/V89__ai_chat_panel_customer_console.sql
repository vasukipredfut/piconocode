-- V89 — Register AiChatPanel primitive + rebuild ai:customer-console page
-- to expose all 4 customer agents through chat inputs.
--
-- Idempotent — safe to reapply.

INSERT IGNORE INTO page_element_catalog (code, category, description) VALUES
  ('AiChatPanel', 'INPUT',
   'Customer input box that invokes an AI agent via /api/m0/ai/agents/{code}/invoke and renders the AgentDecision with apply/dismiss buttons');

-- Ensure ai:customer-console exists (would already from V88; guarded for safety)
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'ai:customer-console', 'AI Assistant',
       'Ask the platform to design pages, fields, workflows, or answer analytical questions.',
       'AI', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='ai:customer-console');

-- Publish page_version 2 with the full 4-panel layout — the version 1 from V88
-- was Markdown-only.
INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 2, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'layout',
    'layout', JSON_ARRAY(
      JSON_OBJECT('kind', 'Section', 'props', JSON_OBJECT('title', 'AI Assistant',
        'subtitle', 'Ask EARMS to design pages, fields, workflows, or answer analytical questions. All proposals require your confirmation before they apply.')),

      JSON_OBJECT('kind', 'AiChatPanel', 'props', JSON_OBJECT(
        'agentCode',   'customer.page-designer',
        'inputField',  'prompt',
        'title',       'Page designer',
        'placeholder', 'e.g. I need a list page for suppliers with columns name, gstin, primary contact'
      )),

      JSON_OBJECT('kind', 'AiChatPanel', 'props', JSON_OBJECT(
        'agentCode',   'customer.field-designer',
        'inputField',  'fieldsDescription',
        'title',       'Field designer',
        'placeholder', 'e.g. add fields to Supplier: name, gstin, contact number, is_active checkbox'
      )),

      JSON_OBJECT('kind', 'AiChatPanel', 'props', JSON_OBJECT(
        'agentCode',   'customer.workflow-designer',
        'inputField',  'description',
        'title',       'Workflow designer',
        'placeholder', 'e.g. supplier onboarding: submit KYC then compliance review then approve or reject'
      )),

      JSON_OBJECT('kind', 'AiChatPanel', 'props', JSON_OBJECT(
        'agentCode',   'customer.decision-analyzer',
        'inputField',  'question',
        'title',       'Decision analyzer',
        'placeholder', 'e.g. which purchase orders are overdue by more than 7 days?'
      )),

      JSON_OBJECT('kind', 'Section', 'props', JSON_OBJECT('title', 'Recent AI decisions on your data')),

      JSON_OBJECT('kind', 'ListView', 'props', JSON_OBJECT(
        'dataSource', JSON_OBJECT(
          'type',   'entity',
          'entity', 'AiAgentRun',
          'select', JSON_ARRAY('id', 'agent_id', 'triggered_by', 'started_at', 'action_taken', 'operator_id')
        ),
        'pageSize', 20
      ))
    )
  ),
  'V89 — 4-panel customer console', NOW()
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='ai:customer-console'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=2);

-- Promote version 2 as the current version
UPDATE page_definition p
   JOIN page_version v ON v.page_id=p.id AND v.version=2
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='ai:customer-console';
