-- V80 — Substrate fix #21 rich page kit.
-- Registers KanbanView + WorkflowStepper in page_element_catalog and
-- ships two demo pages:
--   pico:dispatch-kanban   — swimlane board of workflow instances
--   pico:workflow-stepper-demo — a form/step page with the new stepper on top
-- Idempotent: uses INSERT IGNORE / NOT EXISTS.

INSERT IGNORE INTO page_element_catalog (code, category, description) VALUES
  ('KanbanView',       'DISPLAY',
      'Swimlane board grouping rows by a status/state column with drag-drop transitions'),
  ('WorkflowStepper',  'DISPLAY',
      'Horizontal Stepper showing every state of a workflow with the current step highlighted'),
  ('SparklineCell',    'CELL',
      'Inline 80x22 SVG mini-chart shown in a ListView column (render: sparkline)'),
  ('ProgressCell',     'CELL',
      'Inline 0..max progress bar shown in a ListView column (render: progress)'),
  ('PillCell',         'CELL',
      'Coloured pill shown in a ListView column (render: pill, colorMap wins)'),
  ('DeltaCell',        'CELL',
      'Signed number with up/down arrow shown in a ListView column (render: delta)');

-- --------------------------------------------------------------------------
-- Demo page: dispatch board (kanban).
-- Groups WorkflowInstance rows by current_state, into visible lanes covering
-- Pico's dispatch workflow. Cards show workflow_code + business_ref, badges
-- for status/version. Drag a card between lanes -> PUT current_state.
-- --------------------------------------------------------------------------

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'pico:dispatch-kanban', 'Dispatch Board (Kanban)',
       'Live board of dispatch workflow instances grouped by state. Drag a card between lanes to move it forward.',
       'Dashboard', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dispatch-kanban');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'kanban',
    'title', 'Dispatch Board',
    'dataSource', JSON_OBJECT(
       'type', 'entity',
       'entity', 'WorkflowInstance',
       'select', JSON_ARRAY('id','workflow_code','current_state','business_ref','status','workflow_version','updated_at')
    ),
    'groupBy', 'current_state',
    'pageSize', 200,
    'columns', JSON_ARRAY(
       JSON_OBJECT('value','SUBMITTED',        'label','Submitted',        'color','grey'),
       JSON_OBJECT('value','QC_APPROVED',      'label','QC Approved',      'color','blue'),
       JSON_OBJECT('value','READY_FOR_DISPATCH','label','Ready',            'color','amber'),
       JSON_OBJECT('value','DISPATCHED',       'label','Dispatched',       'color','green'),
       JSON_OBJECT('value','DELIVERED',        'label','Delivered',        'color','purple'),
       JSON_OBJECT('value','CANCELLED',        'label','Cancelled',        'color','red')
    ),
    'card', JSON_OBJECT(
       'title', '{business_ref}',
       'subtitle', '{workflow_code}',
       'badges', JSON_ARRAY('status','workflow_version')
    )
  ),
  'V80 — kanban primitive demo', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='pico:dispatch-kanban'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='pico:dispatch-kanban' AND p.current_version_id IS NULL;

-- --------------------------------------------------------------------------
-- Demo page: workflow stepper (shown embedded in a layout page).
-- --------------------------------------------------------------------------

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'pico:workflow-stepper-demo', 'Workflow Stepper Demo',
       'Shows the new WorkflowStepper primitive resolving states from a workflow definition and highlighting the current step from ?id=<instanceId>.',
       'M0', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:workflow-stepper-demo');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'layout',
    'layout', JSON_ARRAY(
      JSON_OBJECT('kind','WorkflowStepper','props', JSON_OBJECT(
        'title','E-way Bill Flow',
        'workflowCode','M10.EwayBill',
        'instanceIdFromQuery','id'
      )),
      JSON_OBJECT('kind','Section','props', JSON_OBJECT(
        'title','About',
        'subtitle','Add ?id=<workflow_instance.id> to the URL to see the stepper highlight the current step.'
      ))
    )
  ),
  'V80 — WorkflowStepper primitive demo', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='pico:workflow-stepper-demo'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='pico:workflow-stepper-demo' AND p.current_version_id IS NULL;
