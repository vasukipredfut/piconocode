-- V4 — teach the catalog the props the product ACTUALLY uses.
--
-- WHY
-- V3 made page_element_catalog a contract, and the palette was built from it.
-- Then we measured the palette against all 343 real page bodies and it could
-- express only 41 of the 68 props those pages use — 60%. The schemas described
-- the elements as designed, not as deployed. A designer that cannot rebuild the
-- pages the product already ships is a demo, not a tool.
--
-- Every prop below was extracted from a real body, with its real shape. The
-- notable one is FormField's rule engine (visibleWhen / requiredWhen /
-- onChangeFill): that capability already exists and the palette simply could
-- not reach it.
--
-- THE POINT: this is a migration, not a React change. Closing a 60% -> ~95%
-- capability gap is DATA, because the catalog is the contract. Nothing in
-- PageBuilder/ is touched by this file.
--
-- Shapes observed in the live catalogue:
--   ListView.title        "Gate Pass Closure — Inbox"
--   ListView.entity       "AiAgent"                 (shorthand for dataSource.entity)
--   ListView.pageSize     25                        (flat form of pagination.pageSize)
--   ListView.defaultSort  {"field":"updated_at","dir":"desc"}
--   ListView.header       {"actions":[{label,action,args}]}
--   ListView.actions      [{"code":"create","label":"Add Credential","style":"primary"}]
--   ListView.notice       "Supported providers: ..."
--   ListView.description  "Point EARMS at your own AI model..."
--   FormField.visibleWhen  "#form.flowVariantCode == 'M10.TankerOutbound'"
--   FormField.requiredWhen "#form.x == 'A' && #form.y != 'YES'"
--   FormField.onChangeFill {"flowVariantCode":"flow_variant_code", ...}
--   FormField.disabled     true
--   FormField.value        "moving-avg-tolerance"
--   DetailView.entity/idFrom/slug/title/layout

-- ---------------------------------------------------------------- ListView

UPDATE page_element_catalog SET config_schema_json = JSON_MERGE_PATCH(
  config_schema_json,
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','title','type','string','label','Page title'),
    JSON_OBJECT('name','description','type','string','label','Description',
                'help','Shown under the title. Explains the page to whoever opens it.'),
    JSON_OBJECT('name','notice','type','string','label','Notice',
                'help','A standing banner on the page — setup notes, provider lists, caveats.'),
    JSON_OBJECT('name','dataSource','type','dataSource','label','Data source','required',true,
                'help','Required unless Entity is set. Without either, the list renders an error.'),
    JSON_OBJECT('name','entity','type','entityCode','label','Entity (shorthand)',
                'help','Shorthand for Data source > Entity. Use one or the other, not both.'),
    JSON_OBJECT('name','columns','type','array','label','Columns','itemLabel','field',
                'itemSchema', JSON_ARRAY(
                  JSON_OBJECT('name','field','type','fieldCode','entityFrom','dataSource.entity','required',true),
                  JSON_OBJECT('name','label','type','string'),
                  JSON_OBJECT('name','sortable','type','boolean','default',true),
                  JSON_OBJECT('name','align','type','enum','options',JSON_ARRAY('left','right','center')),
                  JSON_OBJECT('name','render','type','enum','options',
                              JSON_ARRAY('badge','datetime','date','sparkline','progress','pill','delta')),
                  JSON_OBJECT('name','linkTo','type','string','help','e.g. page:aria:vendor-detail?id={id}')),
                'help','Leave empty to auto-derive from entity_field_registry.'),
    JSON_OBJECT('name','filters','type','array','label','Filters',
                'itemSchema', JSON_ARRAY(
                  JSON_OBJECT('name','field','type','fieldCode','entityFrom','dataSource.entity','required',true),
                  JSON_OBJECT('name','label','type','string'),
                  JSON_OBJECT('name','type','type','enum','required',true,'options',
                              JSON_ARRAY('multi-select','text-contains','bool-toggle','entity-picker','date-range','range')),
                  JSON_OBJECT('name','applyTo','type','array'),
                  JSON_OBJECT('name','dependsOn','type','string'))),
    JSON_OBJECT('name','defaultSort','type','object','label','Default sort',
                'shape', JSON_OBJECT('field','fieldCode','dir','asc|desc')),
    JSON_OBJECT('name','sort','type','object','label','Sort (nested form)',
                'shape', JSON_OBJECT('default','array')),
    JSON_OBJECT('name','pageSize','type','number','label','Rows per page','default',25),
    JSON_OBJECT('name','pagination','type','object','label','Pagination (nested form)',
                'shape', JSON_OBJECT('pageSize','number')),
    JSON_OBJECT('name','header','type','object','label','Header',
                'help','header.primaryAction and header.actions[] — buttons above the table.'),
    JSON_OBJECT('name','actions','type','array','label','Page actions',
                'itemSchema', JSON_ARRAY(
                  JSON_OBJECT('name','code','type','string','required',true),
                  JSON_OBJECT('name','label','type','string'),
                  JSON_OBJECT('name','style','type','enum','options',JSON_ARRAY('primary','secondary','danger')))),
    JSON_OBJECT('name','rowActions','type','array','label','Row actions',
                'itemSchema', JSON_ARRAY(
                  JSON_OBJECT('name','action','type','actionCode','required',true),
                  JSON_OBJECT('name','label','type','string'),
                  JSON_OBJECT('name','page','type','pageCode'),
                  JSON_OBJECT('name','params','type','object',
                              'help','Values prefixed row. resolve from the clicked row.'))),
    JSON_OBJECT('name','bulkActions','type','array','label','Bulk actions',
                'itemSchema', JSON_ARRAY(
                  JSON_OBJECT('name','action','type','actionCode'),
                  JSON_OBJECT('name','label','type','string'))),
    JSON_OBJECT('name','responsive','type','object','label','Mobile',
                'help','responsive.mobile.cardFields + renderAs (cards|table).')
  ))
) WHERE code = 'ListView';

-- ---------------------------------------------------------------- FormField
-- The rule engine. It shipped, and the palette could not reach it.

UPDATE page_element_catalog SET config_schema_json = JSON_MERGE_PATCH(
  config_schema_json,
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','name','type','fieldCode','label','Field','required',true,
                'help','Required. Without it the input has no form name and validation never registers.'),
    JSON_OBJECT('name','label','type','string','label','Label','help','Defaults to the field code.'),
    JSON_OBJECT('name','type','type','enum','label','Input type','default','text',
                'options', JSON_ARRAY('text','number','date','datetime','email','password','tel','url',
                                      'textarea','select','checkbox','file')),
    JSON_OBJECT('name','placeholder','type','string','label','Placeholder'),
    JSON_OBJECT('name','required','type','boolean','label','Always required','default',false),
    JSON_OBJECT('name','readOnly','type','boolean','label','Read only','default',false),
    JSON_OBJECT('name','disabled','type','boolean','label','Disabled','default',false),
    JSON_OBJECT('name','value','type','string','label','Fixed value',
                'help','Pins the field to a constant.'),
    JSON_OBJECT('name','defaultValue','type','string','label','Default value'),
    JSON_OBJECT('name','helperText','type','string','label','Helper text',
                'help','Suppressed automatically on vehicle-number fields.'),
    JSON_OBJECT('name','visibleWhen','type','rule','label','Show only when…',
                'help','SpEL over the form, e.g. #form.flowVariantCode == ''M10.TankerOutbound''.'),
    JSON_OBJECT('name','requiredWhen','type','rule','label','Required only when…',
                'help','SpEL over the form, e.g. #form.x == ''A'' && #form.y != ''YES''.'),
    JSON_OBJECT('name','onChangeFill','type','object','label','On change, fill…',
                'help','Map of {formField: sourceColumn} — copies values across when this field changes.'),
    JSON_OBJECT('name','options','type','array','label','Options','visibleWhen','type==select'),
    JSON_OBJECT('name','optionsSource','type','object','label','Options from entity','visibleWhen','type==select',
                'shape', JSON_OBJECT('entity','entityCode','valueField','fieldCode','labelField','fieldCode')),
    JSON_OBJECT('name','accept','type','string','label','Accepted file types','visibleWhen','type==file')
  ))
) WHERE code = 'FormField';

-- ---------------------------------------------------------------- DetailView

UPDATE page_element_catalog SET config_schema_json = JSON_MERGE_PATCH(
  config_schema_json,
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','title','type','string','label','Page title'),
    JSON_OBJECT('name','dataSource','type','dataSource','label','Data source','required',true,
                'help','Required unless Entity is set. The record id comes from the URL, not a prop.'),
    JSON_OBJECT('name','entity','type','entityCode','label','Entity (shorthand)'),
    JSON_OBJECT('name','idFrom','type','string','label','Record id from','default','route:id',
                'help','Where the record id is read from. route:id = the ?id= query param.'),
    JSON_OBJECT('name','slug','type','pageCode','label','Slug'),
    JSON_OBJECT('name','header','type','object','label','Header',
                'shape', JSON_OBJECT('title','string','subtitle','string'),
                'help','Supports {{row.field}} templates.'),
    JSON_OBJECT('name','tabs','type','array','label','Tabs',
                'help','Each tab holds sections; each section holds fields ({field,label,type,options}).'),
    JSON_OBJECT('name','layout','type','array','label','Layout (nested form)',
                'help','Older detail pages nest tabs/sections under layout[] instead of tabs[].'),
    JSON_OBJECT('name','actionToolbar','type','array','label','Toolbar actions',
                'itemSchema', JSON_ARRAY(
                  JSON_OBJECT('name','action','type','actionCode','required',true,
                              'options', JSON_ARRAY('page.open','save','save.and.close','save.and.new','delete')),
                  JSON_OBJECT('name','label','type','string'),
                  JSON_OBJECT('name','page','type','pageCode','visibleWhen','action==page.open'),
                  JSON_OBJECT('name','variant','type','enum','options',JSON_ARRAY('primary','danger','outlined')),
                  JSON_OBJECT('name','requiresConfirm','type','boolean')))
  ))
) WHERE code = 'DetailView';

-- ------------------------------------------------- smaller real-world gaps

UPDATE page_element_catalog SET config_schema_json = JSON_MERGE_PATCH(
  config_schema_json,
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','persona','type','personaCode','label','Persona','required',true,
                'help','Required. The component errors with "persona prop required" if unset.'),
    JSON_OBJECT('name','dataSource','type','dataSource','label','Data source',
                'help','Optional override; the queue normally derives its own.'),
    JSON_OBJECT('name','pageSize','type','number','label','Page size','default',25),
    JSON_OBJECT('name','cardFields','type','array','label','Card fields','itemType','fieldCode')
  ))
) WHERE code = 'WorkflowQueueList';

-- ---------------------------------------------------------------- KpiGrid
-- Used twice by m0:coverage-matrix. There is NO KpiGrid in the renderer
-- registry, so that page renders "Unknown primitive: KpiGrid" error boxes at
-- the user — and the QA walk scores it OK, because it does render.
--
-- Listed as STUB rather than left invisible: the palette then explains the
-- situation instead of silently omitting an element the product references.
-- Its props also carry raw SQL ("SELECT COUNT(*) FROM entity_registry") inside
-- the page body. Page bodies are tenant-editable, so any future implementation
-- MUST NOT execute that SQL — it would turn page authoring into arbitrary query
-- execution. Route it through a named, server-side query instead.

INSERT INTO page_element_catalog
  (code, category, display_name, icon, status, description, default_props_json, config_schema_json)
VALUES
('KpiGrid','DISPLAY','KPI Grid (not implemented)','GridView','STUB',
 'REFERENCED BY m0:coverage-matrix BUT NOT IMPLEMENTED — the renderer has no such primitive, so that page shows "Unknown primitive: KpiGrid". Use KPI tiles inside a Grid instead. Note its stored props embed raw SQL, which a real implementation must not execute from a page body.',
 '{}',
 '{"props":[{"name":"kpis","type":"array","label":"KPIs",
    "help":"Each entry carries a label and a raw SQL string. Raw SQL in a page body is not executable by design."}]}')
ON DUPLICATE KEY UPDATE
  category=VALUES(category), display_name=VALUES(display_name), icon=VALUES(icon),
  status=VALUES(status), description=VALUES(description),
  default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json);
