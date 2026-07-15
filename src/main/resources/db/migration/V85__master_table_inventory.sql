-- V85 — Substrate fix: master table inventory.
-- Exposes 100% visibility of every substrate table — its entity_registry code,
-- the pages that reference it (via body_json), the workflows whose transitions
-- write to it, recent write activity (last 7 days from m0_audit_log) and a
-- current row_count. Ships four flagship discovery pages so a developer can
-- never lose track of what exists in the platform:
--
--   m0:master-inventory   — every table + its coverage
--   m0:coverage-matrix    — test coverage / binding health dashboard
--   m0:page-catalog       — every page code in the system
--   m0:workflow-catalog   — every workflow code in the system
--
-- Idempotent: view is CREATE OR REPLACE; every INSERT uses WHERE NOT EXISTS.

-- --------------------------------------------------------------------------
-- v_master_table_inventory
-- One row per (entity_registry.code) — the substrate view of the world.
-- Joins:
--   entity_registry            — the table's registry code + category + label
--   information_schema.tables  — physical existence + row-count estimate
--   page_definition            — pages referencing this entity in body_json
--   workflow_definition        — workflows whose transitions target a page
--                                that references this entity
--   m0_audit_log (7d)          — count & last-write timestamp
--
-- MariaDB DOES allow reading information_schema from a view definition
-- (it is a SQL/foundation catalog, not a user table with row-level triggers).
-- We keep the view definitional and simple; the four heavier joins are
-- rolled up as scalar subqueries so the optimizer can hoist them per row.
-- --------------------------------------------------------------------------

CREATE OR REPLACE VIEW v_master_table_inventory AS
SELECT
  er.table_name                                       AS table_name,
  er.code                                             AS entity_code,
  er.category                                         AS category,
  er.display_name                                     AS display_name,
  (
    SELECT COUNT(DISTINCT pd.id)
      FROM page_definition pd
     WHERE pd.tenant_id = 1
       AND (
             JSON_SEARCH(
               (SELECT pv.body_json FROM page_version pv WHERE pv.id = pd.current_version_id),
               'one', er.code, NULL, '$**.entity'
             ) IS NOT NULL
          OR JSON_SEARCH(
               (SELECT pv.body_json FROM page_version pv WHERE pv.id = pd.current_version_id),
               'one', er.table_name, NULL, '$**.table'
             ) IS NOT NULL
           )
  )                                                    AS page_count,
  (
    SELECT COUNT(DISTINCT wd.id)
      FROM workflow_definition wd
     WHERE wd.tenant_id = 1
       AND wd.status = 'PUBLISHED'
       AND EXISTS (
             SELECT 1
               FROM page_definition pd2
              WHERE pd2.tenant_id = 1
                AND JSON_SEARCH(wd.body_json, 'one', pd2.code, NULL, '$**.pageCode') IS NOT NULL
                AND (
                      JSON_SEARCH(
                        (SELECT pv2.body_json FROM page_version pv2 WHERE pv2.id = pd2.current_version_id),
                        'one', er.code, NULL, '$**.entity'
                      ) IS NOT NULL
                   OR JSON_SEARCH(
                        (SELECT pv2.body_json FROM page_version pv2 WHERE pv2.id = pd2.current_version_id),
                        'one', er.table_name, NULL, '$**.table'
                      ) IS NOT NULL
                    )
           )
  )                                                    AS workflow_count,
  (
    SELECT MAX(al.ts)
      FROM m0_audit_log al
     WHERE al.entity_code = er.code
       AND al.ts >= (CURRENT_TIMESTAMP - INTERVAL 7 DAY)
       AND al.action_kind IN ('CREATE','UPDATE','DELETE','WRITE','INSERT')
  )                                                    AS last_write_at,
  (
    SELECT COUNT(*)
      FROM m0_audit_log al2
     WHERE al2.entity_code = er.code
       AND al2.ts >= (CURRENT_TIMESTAMP - INTERVAL 7 DAY)
       AND al2.action_kind IN ('CREATE','UPDATE','DELETE','WRITE','INSERT')
  )                                                    AS writes_7d,
  COALESCE(t.table_rows, 0)                            AS row_count
FROM entity_registry er
LEFT JOIN information_schema.tables t
       ON t.table_schema = DATABASE()
      AND t.table_name   = er.table_name;

-- --------------------------------------------------------------------------
-- Register the view as a virtual entity so ListView can point at it.
-- No entity_field_registry rows needed — ListView columns are declared
-- inline in body_json.
-- --------------------------------------------------------------------------

INSERT INTO entity_registry (code, display_name, table_name, description, category)
SELECT 'MasterTableInventory', 'Master Table Inventory', 'v_master_table_inventory',
       'Read-only view: every substrate table with its entity code, page/workflow coverage and recent write activity.',
       'M0'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'MasterTableInventory');

-- --------------------------------------------------------------------------
-- Page 1 of 4: m0:master-inventory
--   Flagship "every table in one place" list, columns match the view.
--   category uses pill renderer; last_write_at uses delta cell.
-- --------------------------------------------------------------------------

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'm0:master-inventory', 'Master Table Inventory',
       'Every substrate table with its entity registry code, page count, workflow count, and last write activity. The flagship "what exists in the platform" page.',
       'M0', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='m0:master-inventory');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'Master Table Inventory',
    'dataSource', JSON_OBJECT(
       'type', 'entity',
       'entity', 'MasterTableInventory',
       'select', JSON_ARRAY(
         'table_name','entity_code','category','display_name',
         'page_count','workflow_count','last_write_at','writes_7d','row_count'
       )
    ),
    'pageSize', 100,
    'defaultSort', JSON_OBJECT('by','last_write_at','dir','desc'),
    'columns', JSON_ARRAY(
       JSON_OBJECT('field','table_name',    'label','Table',            'sortable', true, 'filterable', true, 'searchable', true),
       JSON_OBJECT('field','entity_code',   'label','Entity Code',      'sortable', true, 'filterable', true, 'searchable', true),
       JSON_OBJECT('field','category',      'label','Category',         'render','pill',
          'colorMap', JSON_OBJECT('M0','grey','M5','blue','M6','green','M7','purple','M10','amber','M11','teal')),
       JSON_OBJECT('field','display_name',  'label','Display Name',     'sortable', true, 'searchable', true),
       JSON_OBJECT('field','page_count',    'label','Pages',            'sortable', true, 'align','right'),
       JSON_OBJECT('field','workflow_count','label','Workflows',        'sortable', true, 'align','right'),
       JSON_OBJECT('field','writes_7d',     'label','Writes (7d)',      'render','delta', 'align','right'),
       JSON_OBJECT('field','last_write_at', 'label','Last Write',       'render','datetime', 'sortable', true),
       JSON_OBJECT('field','row_count',     'label','Rows (est.)',      'sortable', true, 'align','right')
    ),
    'filters', JSON_ARRAY(
       JSON_OBJECT('field','category', 'kind','select',
          'options', JSON_ARRAY('M0','M5','M6','M7','M10','M11')),
       JSON_OBJECT('field','page_count', 'kind','number', 'label','Min pages'),
       JSON_OBJECT('field','workflow_count', 'kind','number', 'label','Min workflows')
    )
  ),
  'V85 — master table inventory flagship list', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='m0:master-inventory'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='m0:master-inventory' AND p.current_version_id IS NULL;

-- --------------------------------------------------------------------------
-- Page 2 of 4: m0:coverage-matrix
--   Dashboard layout — three sections:
--     1) KPI grid — test coverage %
--     2) List of untested entities
--     3) Health count — pages with no workflow binding
-- --------------------------------------------------------------------------

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'm0:coverage-matrix', 'Coverage Matrix',
       'Test + workflow coverage across every entity. Answers: which entities have zero Playwright coverage? which pages have no workflow binding?',
       'M0', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='m0:coverage-matrix');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'layout',
    'title', 'Coverage Matrix',
    'layout', JSON_ARRAY(
      JSON_OBJECT('kind','Section','props', JSON_OBJECT(
         'title','Test coverage by entity',
         'subtitle','Every entity_registry code and whether any Playwright test currently exercises it.'
      )),
      JSON_OBJECT('kind','KpiGrid','props', JSON_OBJECT(
         'kpis', JSON_ARRAY(
           JSON_OBJECT('label','Total entities',
              'sql','SELECT COUNT(*) FROM entity_registry'),
           JSON_OBJECT('label','Entities with a Playwright test',
              'sql','SELECT COUNT(DISTINCT er.code) FROM entity_registry er WHERE EXISTS (SELECT 1 FROM m0_test_coverage tc WHERE tc.entity_code = er.code)'),
           JSON_OBJECT('label','Coverage %', 'render','percent',
              'sql','SELECT ROUND(100 * (SELECT COUNT(DISTINCT er.code) FROM entity_registry er WHERE EXISTS (SELECT 1 FROM m0_test_coverage tc WHERE tc.entity_code = er.code)) / GREATEST(1,(SELECT COUNT(*) FROM entity_registry)), 1)')
         )
      )),
      JSON_OBJECT('kind','Section','props', JSON_OBJECT(
         'title','Untested entities',
         'subtitle','Entities with zero test-agent coverage. Every row here is a gap.'
      )),
      JSON_OBJECT('kind','ListView','props', JSON_OBJECT(
         'dataSource', JSON_OBJECT(
            'type','sql',
            'sql','SELECT er.code AS entity_code, er.display_name, er.category, er.table_name FROM entity_registry er WHERE NOT EXISTS (SELECT 1 FROM m0_test_coverage tc WHERE tc.entity_code = er.code) ORDER BY er.category, er.code'
         ),
         'columns', JSON_ARRAY(
            JSON_OBJECT('field','entity_code',  'label','Entity'),
            JSON_OBJECT('field','display_name', 'label','Display Name'),
            JSON_OBJECT('field','category',     'label','Category', 'render','pill'),
            JSON_OBJECT('field','table_name',   'label','Table')
         ),
         'pageSize', 50
      )),
      JSON_OBJECT('kind','Section','props', JSON_OBJECT(
         'title','Page-workflow bindings health',
         'subtitle','Pages that reference an entity but have no workflow transition writing to them. These are pages where users can look but nothing can move.'
      )),
      JSON_OBJECT('kind','KpiGrid','props', JSON_OBJECT(
         'kpis', JSON_ARRAY(
           JSON_OBJECT('label','Total published pages',
              'sql','SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND status=''PUBLISHED'''),
           JSON_OBJECT('label','Pages with no workflow binding',
              'sql','SELECT COUNT(*) FROM page_definition pd WHERE pd.tenant_id=1 AND pd.status=''PUBLISHED'' AND NOT EXISTS (SELECT 1 FROM workflow_definition wd WHERE wd.tenant_id=1 AND wd.status=''PUBLISHED'' AND JSON_SEARCH(wd.body_json, ''one'', pd.code, NULL, ''$**.pageCode'') IS NOT NULL)'),
           JSON_OBJECT('label','Pages driven by a workflow',
              'sql','SELECT COUNT(DISTINCT pd.code) FROM page_definition pd JOIN workflow_definition wd ON wd.tenant_id=1 AND wd.status=''PUBLISHED'' AND JSON_SEARCH(wd.body_json, ''one'', pd.code, NULL, ''$**.pageCode'') IS NOT NULL WHERE pd.tenant_id=1 AND pd.status=''PUBLISHED''')
         )
      ))
    )
  ),
  'V85 — coverage matrix dashboard', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='m0:coverage-matrix'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='m0:coverage-matrix' AND p.current_version_id IS NULL;

-- --------------------------------------------------------------------------
-- Page 3 of 4: m0:page-catalog
--   Every page in the system — code, category, current version, status,
--   last-modified. The "index of every page" page.
-- --------------------------------------------------------------------------

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'm0:page-catalog', 'Page Catalog',
       'Every page_definition row in the tenant: code, category, current version, status and last-modified. The canonical index of every UI screen.',
       'M0', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='m0:page-catalog');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'Page Catalog',
    'dataSource', JSON_OBJECT(
       'type', 'sql',
       'sql', 'SELECT pd.id, pd.code, pd.title, pd.category, pd.status, pv.version AS current_version, pv.status AS version_status, pd.updated_at FROM page_definition pd LEFT JOIN page_version pv ON pv.id = pd.current_version_id WHERE pd.tenant_id = 1'
    ),
    'pageSize', 100,
    'defaultSort', JSON_OBJECT('by','updated_at','dir','desc'),
    'columns', JSON_ARRAY(
       JSON_OBJECT('field','code',            'label','Page Code',       'sortable', true, 'searchable', true, 'filterable', true),
       JSON_OBJECT('field','title',           'label','Title',           'sortable', true, 'searchable', true),
       JSON_OBJECT('field','category',        'label','Category',        'render','pill',
          'colorMap', JSON_OBJECT('M0','grey','M5','blue','M6','green','M7','purple','M10','amber','M11','teal','Dashboard','indigo')),
       JSON_OBJECT('field','current_version', 'label','Version',         'align','right'),
       JSON_OBJECT('field','version_status',  'label','Version Status',  'render','pill',
          'colorMap', JSON_OBJECT('PUBLISHED','green','DRAFT','amber','DEPRECATED','red')),
       JSON_OBJECT('field','status',          'label','Page Status',     'render','pill',
          'colorMap', JSON_OBJECT('PUBLISHED','green','DRAFT','amber','ARCHIVED','red')),
       JSON_OBJECT('field','updated_at',      'label','Last Modified',   'render','datetime', 'sortable', true)
    ),
    'filters', JSON_ARRAY(
       JSON_OBJECT('field','category', 'kind','select',
          'options', JSON_ARRAY('M0','M5','M6','M7','M10','M11','Dashboard')),
       JSON_OBJECT('field','status', 'kind','select',
          'options', JSON_ARRAY('PUBLISHED','DRAFT','ARCHIVED'))
    )
  ),
  'V85 — page catalog', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='m0:page-catalog'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='m0:page-catalog' AND p.current_version_id IS NULL;

-- --------------------------------------------------------------------------
-- Page 4 of 4: m0:workflow-catalog
--   Every workflow_definition row — code, version, status and derived
--   state count from body_json.states array.
-- --------------------------------------------------------------------------

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'm0:workflow-catalog', 'Workflow Catalog',
       'Every workflow_definition row in the tenant: code, version, status and derived state count. The canonical index of every workflow.',
       'M0', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='m0:workflow-catalog');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'list',
    'title', 'Workflow Catalog',
    'dataSource', JSON_OBJECT(
       'type', 'sql',
       'sql', 'SELECT wd.id, wd.code, wd.version, wd.status, COALESCE(JSON_LENGTH(JSON_EXTRACT(wd.body_json, ''$.states'')),0) AS state_count, COALESCE(JSON_LENGTH(JSON_EXTRACT(wd.body_json, ''$.transitions'')),0) AS transition_count, wd.created_at FROM workflow_definition wd WHERE wd.tenant_id = 1'
    ),
    'pageSize', 100,
    'defaultSort', JSON_OBJECT('by','code','dir','asc'),
    'columns', JSON_ARRAY(
       JSON_OBJECT('field','code',             'label','Workflow Code',   'sortable', true, 'searchable', true, 'filterable', true),
       JSON_OBJECT('field','version',          'label','Version',         'align','right', 'sortable', true),
       JSON_OBJECT('field','status',           'label','Status',          'render','pill',
          'colorMap', JSON_OBJECT('PUBLISHED','green','DRAFT','amber','DEPRECATED','red')),
       JSON_OBJECT('field','state_count',      'label','States',          'align','right', 'sortable', true),
       JSON_OBJECT('field','transition_count', 'label','Transitions',     'align','right', 'sortable', true),
       JSON_OBJECT('field','created_at',       'label','Created',         'render','datetime', 'sortable', true)
    ),
    'filters', JSON_ARRAY(
       JSON_OBJECT('field','status', 'kind','select',
          'options', JSON_ARRAY('PUBLISHED','DRAFT','DEPRECATED'))
    )
  ),
  'V85 — workflow catalog', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='m0:workflow-catalog'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
   JOIN page_version v ON v.page_id = p.id AND v.version = 1
   SET p.current_version_id = v.id
 WHERE p.tenant_id=1 AND p.code='m0:workflow-catalog' AND p.current_version_id IS NULL;
