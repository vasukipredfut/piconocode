-- V9 — a friendly display name for workflows.
--
-- WHY: a workflow's only name is its code (M10.PackLoading). Codes are stable
-- identifiers that pages, instances, and listeners reference, so they must not
-- change — but they read like machine tokens on screen. This adds a separate,
-- editable display_name so the UI can show "Packed-Goods Loading" while the code
-- stays M10.PackLoading. Nothing that references the code is touched.
--
-- Backfill is a plain prefix-strip (M10.PackLoading -> "PackLoading"): readable,
-- and deliberately NOT a clever camelCase split — MariaDB REGEXP_REPLACE backref
-- escaping through tooling produced garbage on the first two attempts, and a
-- wrong name written to every workflow is worse than a plain one the user renames.

ALTER TABLE workflow_definition
  ADD COLUMN display_name VARCHAR(160) NULL AFTER code;

UPDATE workflow_definition
SET display_name = SUBSTRING_INDEX(code, '.', -1)
WHERE display_name IS NULL OR display_name = '';

-- Register the field so the workflow catalog (WorkflowDefinition entity) shows it
-- and the ListView inline-editor can rename it. tenant_id=-1: it is a product
-- field, mirroring the existing code/version/status rows.
INSERT INTO entity_field_registry
  (tenant_id, entity_code, field_code, display_label, data_type, widget_kind, sortable, searchable)
SELECT -1, 'WorkflowDefinition', 'display_name', 'Name', 'STRING', 'text', 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM entity_field_registry
  WHERE entity_code = 'WorkflowDefinition' AND field_code = 'display_name'
);

-- Surface Name as the first column of the workflow catalog, ahead of the code.
UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_OBJECT(
  'kind', 'list',
  'schemaVersion', 2,
  'dataSource', JSON_OBJECT('type', 'entity', 'entity', 'WorkflowDefinition'),
  'pageSize', 100,
  'defaultSort', JSON_OBJECT('field', 'display_name', 'dir', 'asc'),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','display_name','label','Name','sortable',true),
    JSON_OBJECT('field','code','label','Workflow Code','sortable',true),
    JSON_OBJECT('field','version','label','Version','align','right','sortable',true),
    JSON_OBJECT('field','status','label','Status','render','pill'),
    JSON_OBJECT('field','created_at','label','Created','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select',
                'options', JSON_ARRAY('PUBLISHED','DRAFT','ARCHIVED')),
    JSON_OBJECT('field','_q','label','Search name or code','type','text-contains',
                'applyTo', JSON_ARRAY('display_name','code'))
  )
)
WHERE pd.code = 'm0:workflow-catalog' AND pv.id = pd.current_version_id;
