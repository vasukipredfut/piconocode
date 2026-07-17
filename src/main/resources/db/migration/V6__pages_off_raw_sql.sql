-- V6 — take the three admin pages off raw SQL and onto registered entities.
--
-- THE BUG (reported from the UI: "0 rows / Unsupported filter: / spec.entity is required?")
-- Three pages carry a hand-written SQL string in their page body:
--     m0:page-catalog        SELECT ... FROM page_definition ... WHERE pd.tenant_id = 1
--     m0:workflow-catalog    SELECT ... FROM workflow_definition ...
--     m0:coverage-matrix     KpiGrid props with embedded SELECTs
--
--   {"dataSource": {"type": "sql", "sql": "SELECT ..."}}
--
-- type=sql HAS NEVER BEEN IMPLEMENTED. DataSourceQueryService supports only
-- type=entity and throws "spec.entity is required" — which is exactly the error
-- on screen. These pages have never worked. The 342/342 QA walk scores them OK
-- because they RENDER; the error is inside the rendered page.
--
-- WHY NOT JUST IMPLEMENT type=sql
-- Because page bodies are tenant-editable data. Executing a SQL string from a
-- page body turns "customise a page" into "run arbitrary SQL as the app user".
-- The substrate already has the right answer: page_definition and
-- workflow_definition are REGISTERED ENTITIES, and both carry tenant_id, so
-- type=entity gets correct per-tenant scoping from TenantScope for free.
--
-- That also kills a second bug hiding in the same string: `WHERE pd.tenant_id = 1`
-- was hardcoded. Had the SQL path ever worked, every firm would have seen
-- tenant 1's pages.
--
-- THIRD BUG on the same page: the filters use `kind`, but the widget dispatches
-- on `type` (FILTER_WIDGETS[config.type]) and prints "Unsupported filter:" with
-- an empty value when it is undefined. Hence the two blank filter errors.
-- `select` is also not a widget; the multi-value one is `multi-select`.

-- ---------------------------------------------------------------- page catalog

UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_OBJECT(
  'kind', 'list',
  'schemaVersion', 2,
  'dataSource', JSON_OBJECT('type', 'entity', 'entity', 'PageDefinition'),
  'pageSize', 100,
  'defaultSort', JSON_OBJECT('field', 'updated_at', 'dir', 'desc'),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','code','label','Page Code','sortable',true),
    JSON_OBJECT('field','title','label','Title','sortable',true),
    JSON_OBJECT('field','category','label','Category','render','badge'),
    JSON_OBJECT('field','status','label','Status','render','pill'),
    JSON_OBJECT('field','updated_at','label','Last Modified','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    -- `type`, not `kind`; `multi-select`, not `select`.
    JSON_OBJECT('field','category','label','Category','type','multi-select',
                'options', JSON_ARRAY('M0','M5','M6','M7','M10','M11','HR','Master','Report','Dashboard','CRM','AI')),
    JSON_OBJECT('field','status','label','Status','type','multi-select',
                'options', JSON_ARRAY('PUBLISHED','DRAFT','ARCHIVED')),
    JSON_OBJECT('field','_q','label','Search code or title','type','text-contains',
                'applyTo', JSON_ARRAY('code','title'))
  )
)
WHERE pd.code = 'm0:page-catalog' AND pv.id = pd.current_version_id;

-- ------------------------------------------------------------ workflow catalog

UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_OBJECT(
  'kind', 'list',
  'schemaVersion', 2,
  'dataSource', JSON_OBJECT('type', 'entity', 'entity', 'WorkflowDefinition'),
  'pageSize', 100,
  'defaultSort', JSON_OBJECT('field', 'code', 'dir', 'asc'),
  -- workflow_definition is (id, tenant_id, code, version, status, body_json,
  -- created_at). There is NO `name` column — the display name lives inside
  -- body_json — and selecting one yields a 500 BadSqlGrammarException. Checked
  -- against information_schema rather than assumed, after doing exactly that.
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','code','label','Workflow Code','sortable',true),
    JSON_OBJECT('field','version','label','Version','align','right','sortable',true),
    JSON_OBJECT('field','status','label','Status','render','pill'),
    JSON_OBJECT('field','created_at','label','Created','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select',
                'options', JSON_ARRAY('PUBLISHED','DRAFT','ARCHIVED')),
    JSON_OBJECT('field','_q','label','Search workflow code','type','text-contains',
                'applyTo', JSON_ARRAY('code'))
  )
)
WHERE pd.code = 'm0:workflow-catalog' AND pv.id = pd.current_version_id;

-- ------------------------------------------------------------ coverage matrix
-- This one referenced KpiGrid, a primitive that does not exist in the renderer
-- (the page showed "Unknown primitive: KpiGrid" to the user), and carried its
-- counts as raw SQL. Rebuild it from real elements: a Markdown explainer plus
-- the entity registry as a list. The per-KPI counts it wanted are a reporting
-- feature that needs a real, server-side, named query — not SELECTs in a body.

UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_OBJECT(
  'kind', 'layout',
  'schemaVersion', 2,
  'layout', JSON_ARRAY(
    JSON_OBJECT('kind','Section','title','Entity Coverage',
      'children', JSON_ARRAY(
        JSON_OBJECT('kind','Markdown',
          'text', CONCAT('Every entity registered in the substrate. ',
                         'Counts that previously lived here were computed from raw SQL embedded in this page body; ',
                         'they need a named server-side query before they can come back.'))
      )),
    JSON_OBJECT('kind','ListView',
      'dataSource', JSON_OBJECT('type','entity','entity','EntityRegistry'),
      'pageSize', 100,
      'defaultSort', JSON_OBJECT('field','code','dir','asc'),
      'columns', JSON_ARRAY(
        JSON_OBJECT('field','code','label','Entity','sortable',true),
        JSON_OBJECT('field','display_name','label','Name'),
        JSON_OBJECT('field','table_name','label','Table')
      ),
      'filters', JSON_ARRAY(
        JSON_OBJECT('field','_q','label','Search entity','type','text-contains',
                    'applyTo', JSON_ARRAY('code','display_name','table_name'))
      ))
  )
)
WHERE pd.code = 'm0:coverage-matrix' AND pv.id = pd.current_version_id;
