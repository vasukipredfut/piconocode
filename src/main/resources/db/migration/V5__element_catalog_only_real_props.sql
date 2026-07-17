-- V5 — the catalog must declare only props the component ACTUALLY READS.
--
-- WHAT WENT WRONG IN V4
-- V4 claimed to lift palette coverage from 60% to 99%. The measurement was
-- wrong. I asked "does the catalog mention the props real pages use?" and never
-- "does the component read them?" — so I counted DEAD props as covered and
-- declared twelve settings the palette would offer and the renderer would
-- ignore. That is the exact silent-no-op failure this codebase is riddled with,
-- and V4 added twelve more of it.
--
-- WHAT ListView REALLY READS (grepped, not assumed):
--   bulkActions, code, columns, dataSource, filters, header, pagination,
--   responsive, rowActions, slug, sort
-- WHAT DetailView REALLY READS:
--   actionToolbar, dataSource, header, slug, tabs
-- WHAT WorkflowQueueList REALLY READS:
--   persona, pageSize, cardFields
--
-- TWO OUTCOMES, and the distinction matters:
--
--   FIXED IN THE COMPONENT — ListView now accepts both spellings, so these are
--   real and stay declared:
--     pageSize     214 pages said it; only pagination.pageSize was honoured.
--                  m0:page-catalog asked for 100 rows and rendered 25.
--     defaultSort    7 pages said it; only sort.default was honoured.
--
--   GENUINELY DEAD — removed, because offering a switch that does nothing is
--   worse than not offering it:
--     ListView.title        131 pages set it; the visible title comes from
--                           page_definition.title, so the body copy is inert.
--     ListView.entity         4 pages; ListView reads dataSource.entity only.
--     ListView.notice         1 page — never rendered.
--     ListView.description    1 page — never rendered.
--     ListView.actions        1 page — reads header.actions instead.
--     DetailView.title/entity/idFrom/layout — none are read; the record id
--                           comes from the URL query string.
--     WorkflowQueueList.dataSource — the queue derives its own.
--
-- notice/description/title are reasonable FEATURES someone may want to build in
-- ListView later. Until they are built, the palette must not pretend they exist.

-- ---------------------------------------------------------------- ListView

UPDATE page_element_catalog SET config_schema_json = JSON_MERGE_PATCH(
  config_schema_json,
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','dataSource','type','dataSource','label','Data source','required',true,
                'help','Required. Without it the list renders "no dataSource on page body".'),
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
                              JSON_ARRAY('multi-select','text-contains','bool-toggle','entity-picker','date-range','range'),
                              'help','Must be `type`. A filter with `kind` instead renders "Unsupported filter:".'),
                  JSON_OBJECT('name','applyTo','type','array'),
                  JSON_OBJECT('name','dependsOn','type','string'))),
    JSON_OBJECT('name','pageSize','type','number','label','Rows per page','default',25,
                'help','Flat form, used by most of the catalogue. Honoured since the both-spellings fix.'),
    JSON_OBJECT('name','pagination','type','object','label','Pagination (nested form)',
                'shape', JSON_OBJECT('pageSize','number'),
                'help','Equivalent to Rows per page. Wins if both are set.'),
    JSON_OBJECT('name','defaultSort','type','object','label','Default sort',
                'shape', JSON_OBJECT('field','fieldCode','dir','asc|desc'),
                'help','Flat form. `by` is accepted as an alias of `field`.'),
    JSON_OBJECT('name','sort','type','object','label','Sort (nested form)',
                'shape', JSON_OBJECT('default','array')),
    JSON_OBJECT('name','header','type','object','label','Header',
                'help','header.primaryAction and header.actions[] — the buttons above the table.'),
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
                'help','responsive.mobile.cardFields + renderAs (cards|table).'),
    JSON_OBJECT('name','slug','type','pageCode','label','Slug',
                'help','Used for the column-preference key in localStorage.')
  ))
) WHERE code = 'ListView';

-- ---------------------------------------------------------------- DetailView

UPDATE page_element_catalog SET config_schema_json = JSON_MERGE_PATCH(
  config_schema_json,
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','dataSource','type','dataSource','label','Data source','required',true,
                'help','Required. The record id comes from the URL (?id=), not from a prop.'),
    JSON_OBJECT('name','header','type','object','label','Header',
                'shape', JSON_OBJECT('title','string','subtitle','string'),
                'help','Supports {{row.field}} templates. This is where a detail page title goes.'),
    JSON_OBJECT('name','tabs','type','array','label','Tabs',
                'help','Each tab holds sections; each section holds fields ({field,label,type,options}).'),
    JSON_OBJECT('name','actionToolbar','type','array','label','Toolbar actions',
                'itemSchema', JSON_ARRAY(
                  JSON_OBJECT('name','action','type','actionCode','required',true,
                              'options', JSON_ARRAY('page.open','save','save.and.close','save.and.new','delete')),
                  JSON_OBJECT('name','label','type','string'),
                  JSON_OBJECT('name','page','type','pageCode','visibleWhen','action==page.open'),
                  JSON_OBJECT('name','variant','type','enum','options',JSON_ARRAY('primary','danger','outlined')),
                  JSON_OBJECT('name','requiresConfirm','type','boolean'))),
    JSON_OBJECT('name','slug','type','pageCode','label','Slug')
  ))
) WHERE code = 'DetailView';

-- ------------------------------------------------------- WorkflowQueueList

UPDATE page_element_catalog SET config_schema_json = JSON_MERGE_PATCH(
  config_schema_json,
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','persona','type','personaCode','label','Persona','required',true,
                'help','Required. The component errors with "persona prop required" if unset.'),
    JSON_OBJECT('name','pageSize','type','number','label','Page size','default',25),
    JSON_OBJECT('name','cardFields','type','array','label','Card fields','itemType','fieldCode')
  ))
) WHERE code = 'WorkflowQueueList';
