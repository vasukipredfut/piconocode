-- V6_2 — M11 frontend page seed (Round 1H.1)
-- Registers `IntegrationInstance` in the entity_registry so the Schema V2
-- ListView primitive can read it, then publishes one page:
--   pico:m11-integrations-list  — tenant-scoped table of registered instances.
-- Idempotent on (tenant_id, code) for pages and on the registry PK for entities.

-- ----- entity_registry + entity_field_registry --------------------------
INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, description) VALUES
  ('IntegrationInstance', 'Integration Instance', 'integration_instance', 'M11', 'M11 integration hub instances.');

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) VALUES
  ('IntegrationInstance', 'id',                'Id',          'LONG',    FALSE, TRUE,  FALSE, FALSE),
  ('IntegrationInstance', 'code',              'Code',        'STRING',  TRUE,  TRUE,  TRUE,  TRUE),
  ('IntegrationInstance', 'connector_code',    'Connector',   'STRING',  TRUE,  TRUE,  TRUE,  TRUE),
  ('IntegrationInstance', 'connector_version', 'Version',     'LONG',    FALSE, TRUE,  FALSE, FALSE),
  ('IntegrationInstance', 'name',              'Name',        'STRING',  TRUE,  TRUE,  TRUE,  TRUE),
  ('IntegrationInstance', 'status',            'Status',      'ENUM',    TRUE,  TRUE,  FALSE, TRUE),
  ('IntegrationInstance', 'last_health_at',    'Last Health', 'STRING',  TRUE,  TRUE,  FALSE, FALSE),
  ('IntegrationInstance', 'last_event_at',     'Last Event',  'STRING',  TRUE,  TRUE,  FALSE, FALSE),
  ('IntegrationInstance', 'error_count_24h',   'Errors (24h)','LONG',    TRUE,  TRUE,  FALSE, FALSE),
  ('IntegrationInstance', 'tenant_id',         'Tenant',      'LONG',    TRUE,  FALSE, FALSE, TRUE);

-- ----- pico:m11-integrations-list --------------------------------------
SET @tenant := 1;
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT @tenant, 'pico:m11-integrations-list', 'Integrations',
       'M11 — Integration Hub list view (Schema V2).', 'M11', 'PUBLISHED'
 WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=@tenant AND code='pico:m11-integrations-list');
SET @pid := (SELECT id FROM page_definition WHERE tenant_id=@tenant AND code='pico:m11-integrations-list');

INSERT INTO page_version (page_id, version, status, body_json, published_at)
SELECT @pid, 1, 'PUBLISHED',
  JSON_OBJECT(
    'kind','list',
    'schemaVersion',2,
    'slug','pico:m11-integrations-list',
    'title','Integrations',
    'dataSource', JSON_OBJECT(
      'type','entity',
      'entity','IntegrationInstance',
      'filter', JSON_OBJECT('and', JSON_ARRAY(
        JSON_OBJECT('field','tenant_id','op','eq','value','ctx:tenantId')
      )),
      'select', JSON_ARRAY('id','code','connector_code','name','status','last_health_at','error_count_24h')
    ),
    'columns', JSON_ARRAY(
      JSON_OBJECT('field','code',           'label','Code',          'render','text','sticky','left','sortable',TRUE),
      JSON_OBJECT('field','name',           'label','Name',          'render','text','sortable',TRUE),
      JSON_OBJECT('field','connector_code', 'label','Connector',     'render','text','sortable',TRUE),
      JSON_OBJECT('field','status',         'label','Status',        'render','badge'),
      JSON_OBJECT('field','last_health_at', 'label','Last Health',   'render','text'),
      JSON_OBJECT('field','error_count_24h','label','Errors (24h)',  'render','text','align','right')
    ),
    'filters', JSON_ARRAY(
      JSON_OBJECT('field','status','label','Status','type','multi-select',
                  'options', JSON_ARRAY('DRAFT','ENROLLED','ACTIVE','DEGRADED','DOWN','DISABLED','RETIRED')),
      JSON_OBJECT('field','_q','label','Search','type','text-contains','applyTo', JSON_ARRAY('code','name','connector_code'))
    ),
    'actionToolbar', JSON_ARRAY(
      JSON_OBJECT('action','export.csv','label','Export CSV','variant','tertiary')
    )
  ),
  CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=@pid AND version=1);

UPDATE page_definition
   SET current_version_id = (SELECT id FROM page_version WHERE page_id=@pid AND version=1)
 WHERE id=@pid
   AND (current_version_id IS NULL
        OR current_version_id <> (SELECT id FROM page_version WHERE page_id=@pid AND version=1));
