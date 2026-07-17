-- V3 — make page_element_catalog a real contract, so a palette can be driven
-- from data instead of a hardcoded list in React.
--
-- WHY THIS EXISTS
-- The catalog shipped as names + a description sentence: config_schema_json and
-- default_props_json were NULL on all 21 rows. That is enough to LIST elements
-- and not enough to CONFIGURE one — a palette needs to know what props an
-- element takes, which are required, and what a sane starting value looks like.
-- So the Page Designer stayed a field editor and the page body stayed hand-written.
--
-- Three problems this fixes:
--   1. NULL schemas          -> every element now declares its props.
--   2. Seven omissions       -> ListView + DetailView (the two most-used
--                              primitives in the product; every list page is a
--                              ListView) plus Markdown, Tabs, PivotTable,
--                              PrintButton, PushNotification were never listed.
--   3. Three lies            -> DataTable / LineChart / TelemetryGauge are
--                              placeholder shims in registry.js that render
--                              "Table (0 rows)" and similar. The catalog
--                              advertised them as working widgets. They are
--                              marked STUB rather than deleted, so the palette
--                              can grey them out and nobody ships one by
--                              accident.
--
-- CATEGORY 'CELL' is NOT a page element. DeltaCell / PillCell / ProgressCell /
-- SparklineCell are column render modes (columns[].render = 'sparkline'), which
-- is why the renderer has no primitive for them. The palette must offer them as
-- column options, never as droppable elements. Filter on category <> 'CELL'.
--
-- PROP TYPE VOCABULARY (config_schema_json.props[].type)
--   Plain:     string | number | boolean | enum | array | object | markdown
--   Substrate: entityCode | fieldCode | pageCode | workflowCode | stateCode
--              | actionCode | printCode | agentCode | personaCode | dataSource
-- The substrate types are the point: a 'fieldCode' prop renders as a picker
-- fed from entity_field_registry, not as a free-text box the user can typo.
-- 'entityFrom' names the prop holding the entity whose fields to offer.
--
-- This table has no tenant_id: it describes the PRODUCT's elements, so it is
-- global by construction. A tenant does not get their own set of primitives;
-- they get their own PAGES built from these.

ALTER TABLE page_element_catalog
  ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE'
      COMMENT 'AVAILABLE = real implementation; STUB = placeholder, do not offer as usable',
  ADD COLUMN display_name VARCHAR(96) NULL
      COMMENT 'Palette label. Falls back to code when null.',
  ADD COLUMN icon VARCHAR(32) NULL
      COMMENT 'MUI icon name for the palette tile.';

-- ---------------------------------------------------------------- LAYOUT

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('Grid', 'LAYOUT', 'Grid', 'GridView', '{"cols":3,"gap":16}', '{"acceptsChildren":true,"props":[
    {"name":"cols","type":"number","label":"Columns","default":3,"min":1,"max":12,
     "help":"Column count. Minimum column width is derived as 1100/cols."},
    {"name":"minColPx","type":"number","label":"Min column width (px)","default":200,
     "help":"Overrides the width derived from Columns."},
    {"name":"gap","type":"number","label":"Gap (px)","default":16}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('Section', 'LAYOUT', 'Section', 'ViewAgenda', '{"title":"Section"}', '{"acceptsChildren":true,"props":[
    {"name":"title","type":"string","label":"Title"},
    {"name":"subtitle","type":"string","label":"Subtitle"}
  ],"help":"Always spans the full row width."}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('Tabs', 'LAYOUT', 'Tabs', 'Tab', '{"tabs":[{"label":"Tab 1"}]}', '{"acceptsChildren":true,"props":[
    {"name":"tabs","type":"array","label":"Tabs","itemLabel":"label",
     "help":"Each tab holds its own children. Label defaults to Tab N."}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

-- ---------------------------------------------------------------- DISPLAY

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('KPI', 'DISPLAY', 'KPI Tile', 'Insights', '{"label":"KPI","format":"count","color":"primary"}', '{"props":[
    {"name":"label","type":"string","label":"Label","default":"KPI"},
    {"name":"dataSource","type":"dataSource","label":"Data source",
     "help":"Leave empty and set Value to show a static number."},
    {"name":"value","type":"string","label":"Static value","visibleWhen":"!dataSource"},
    {"name":"format","type":"string","label":"Format","default":"count",
     "help":"count = total row count. Any other value is read as a field code from the first row."},
    {"name":"unit","type":"string","label":"Unit"},
    {"name":"intervalMs","type":"number","label":"Auto-refresh (ms)",
     "help":"Above 0 enables polling and shows a refresh button."},
    {"name":"color","type":"enum","label":"Colour","default":"primary",
     "options":["primary","success","warning","error","info","purple"]},
    {"name":"linkTo","type":"pageCode","label":"Click-through page",
     "help":"Defaults to the list page for the data source entity."},
    {"name":"trend","type":"object","label":"Trend","shape":{"direction":"up|down","value":"string"}}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, description, default_props_json, config_schema_json)
VALUES ('ListView', 'DISPLAY', 'List View', 'TableRows', 'The standard list page: server-paginated table with filters, sorting, row actions and an auto mobile card layout. Columns auto-derive from the entity field registry when left empty.', '{"kind":"list","schemaVersion":2,"pagination":{"pageSize":25}}', '{"props":[
    {"name":"dataSource","type":"dataSource","label":"Data source","required":true,
     "help":"Required. Without it the list renders an error."},
    {"name":"columns","type":"array","label":"Columns","itemLabel":"field",
     "itemSchema":[
       {"name":"field","type":"fieldCode","entityFrom":"dataSource.entity","required":true},
       {"name":"label","type":"string"},
       {"name":"sortable","type":"boolean","default":true},
       {"name":"align","type":"enum","options":["left","right","center"]},
       {"name":"render","type":"enum","options":["badge","datetime","date","sparkline","progress","pill","delta"]},
       {"name":"linkTo","type":"string","help":"e.g. page:aria:vendor-detail?id={id}"}
     ],
     "help":"Leave empty to auto-derive from entity_field_registry."},
    {"name":"filters","type":"array","label":"Filters",
     "itemSchema":[
       {"name":"field","type":"fieldCode","entityFrom":"dataSource.entity","required":true},
       {"name":"label","type":"string"},
       {"name":"type","type":"enum","required":true,
        "options":["multi-select","text-contains","bool-toggle","entity-picker","date-range","range"]},
       {"name":"applyTo","type":"array"},
       {"name":"dependsOn","type":"string"}
     ]},
    {"name":"sort","type":"object","label":"Default sort","shape":{"default":"array"}},
    {"name":"pagination","type":"object","label":"Pagination","shape":{"pageSize":"number"},
     "default":{"pageSize":25}},
    {"name":"rowActions","type":"array","label":"Row actions",
     "itemSchema":[
       {"name":"action","type":"actionCode","required":true},
       {"name":"label","type":"string"},
       {"name":"page","type":"pageCode"},
       {"name":"params","type":"object","help":"Values prefixed row. resolve from the clicked row."}
     ]},
    {"name":"bulkActions","type":"array","label":"Bulk actions",
     "itemSchema":[{"name":"action","type":"actionCode"},{"name":"label","type":"string"}]},
    {"name":"responsive","type":"object","label":"Mobile",
     "help":"responsive.mobile.cardFields + renderAs (cards|table). Defaults to cards on mobile using the first 4 columns."}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), category=VALUES(category), description=VALUES(description), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('KanbanView', 'DISPLAY', 'Kanban Board', 'ViewKanban', '{"groupBy":"current_state","pageSize":200,"columns":[]}', '{"props":[
    {"name":"dataSource","type":"dataSource","label":"Data source","required":true,
     "help":"Required. Without an entity the board never fetches."},
    {"name":"groupBy","type":"fieldCode","entityFrom":"dataSource.entity","label":"Group by",
     "default":"current_state"},
    {"name":"columns","type":"array","label":"Lanes",
     "itemSchema":[
       {"name":"value","type":"string","required":true},
       {"name":"label","type":"string"},
       {"name":"color","type":"enum","options":["grey","blue","green","amber","red","purple"],"default":"grey"}
     ]},
    {"name":"card","type":"object","label":"Card",
     "shape":{"title":"fieldCode","subtitle":"fieldCode","badges":"array"}},
    {"name":"pageSize","type":"number","label":"Page size","default":200},
    {"name":"title","type":"string","label":"Title"}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('WorkflowQueueList', 'DISPLAY', 'Workflow Queue', 'Inbox', '{"pageSize":25,"cardFields":[]}', '{"props":[
    {"name":"persona","type":"personaCode","label":"Persona","required":true,
     "help":"Required. The component errors with persona prop required if unset."},
    {"name":"pageSize","type":"number","label":"Page size","default":25},
    {"name":"cardFields","type":"array","label":"Card fields","itemType":"fieldCode"}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('WorkflowStepper', 'DISPLAY', 'Workflow Stepper', 'Timeline', '{}', '{"props":[
    {"name":"workflowCode","type":"workflowCode","label":"Workflow"},
    {"name":"pageCode","type":"pageCode","label":"Page",
     "help":"Used to look the workflow up when Workflow is not set."},
    {"name":"instanceId","type":"number","label":"Instance id"},
    {"name":"instanceIdFromQuery","type":"string","label":"Instance id query param","default":"id"},
    {"name":"states","type":"array","label":"States override"},
    {"name":"currentState","type":"stateCode","label":"Current state override"},
    {"name":"title","type":"string","label":"Title"}
  ],"help":"Fails open: with nothing resolvable it still renders the state labels, unhighlighted."}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

-- ---------------------------------------------------------------- INPUT

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('Form', 'INPUT', 'Form', 'Assignment', '{"fields":[]}', '{"acceptsChildren":true,"props":[
    {"name":"fields","type":"array","label":"Fields","itemLabel":"name",
     "help":"Each entry is a Form Field config, keyed by name."}
  ],"help":"submitLabel is deliberately ignored by the renderer; add a Workflow Action Button for submit."}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('FormField', 'INPUT', 'Form Field', 'TextFields', '{"type":"text"}', '{"props":[
    {"name":"name","type":"fieldCode","label":"Field","required":true,
     "help":"Required. Without it the input has no form name and validation never registers."},
    {"name":"label","type":"string","label":"Label","help":"Defaults to the field code."},
    {"name":"type","type":"enum","label":"Input type","default":"text",
     "options":["text","number","date","datetime","email","password","tel","url","textarea","select","checkbox","file"]},
    {"name":"placeholder","type":"string","label":"Placeholder"},
    {"name":"required","type":"boolean","label":"Required","default":false},
    {"name":"readOnly","type":"boolean","label":"Read only","default":false},
    {"name":"helperText","type":"string","label":"Helper text",
     "help":"Suppressed automatically on vehicle-number fields."},
    {"name":"defaultValue","type":"string","label":"Default value"},
    {"name":"options","type":"array","label":"Options","visibleWhen":"type==select"},
    {"name":"optionsSource","type":"object","label":"Options from entity","visibleWhen":"type==select",
     "shape":{"entity":"entityCode","valueField":"fieldCode","labelField":"fieldCode"}},
    {"name":"accept","type":"string","label":"Accepted file types","visibleWhen":"type==file"}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('AiChatPanel', 'INPUT', 'AI Assistant', 'AutoAwesome', '{"agentCode":"customer.decision-analyzer","inputField":"question"}', '{"props":[
    {"name":"agentCode","type":"agentCode","label":"Agent","default":"customer.decision-analyzer"},
    {"name":"inputField","type":"enum","label":"Input key","default":"question",
     "options":["prompt","question","description","fieldsDescription"]},
    {"name":"placeholder","type":"string","label":"Placeholder"},
    {"name":"title","type":"string","label":"Title","help":"Defaults to the agent name."}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('SignaturePad', 'INPUT', 'Signature Pad', 'Draw', '{"width":400,"height":200}', '{"props":[
    {"name":"targetField","type":"fieldCode","label":"Writes to field","required":true,
     "help":"Required in practice: without it Save silently does nothing."},
    {"name":"width","type":"number","label":"Width","default":400},
    {"name":"height","type":"number","label":"Height","default":200}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

-- ---------------------------------------------------------------- ACTION

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('WorkflowActionButton', 'ACTION', 'Action Button', 'SmartButton', '{"action":"workflow.transition","label":"Submit","variant":"primary"}', '{"props":[
    {"name":"action","type":"actionCode","label":"Action","default":"workflow.transition","required":true,
     "options":["workflow.transition","save","save.and.close","save.and.new","page.open","page.refresh","page.back","external.api.call","notify.toast"],
     "help":"Anything not listed is emitted as an earms:action event for a custom handler."},
    {"name":"label","type":"string","label":"Label"},
    {"name":"variant","type":"enum","label":"Style","default":"primary",
     "options":["primary","secondary","outlined","text","danger","error","warning","success"]},
    {"name":"confirmRequired","type":"boolean","label":"Ask for confirmation","default":false},
    {"name":"toState","type":"stateCode","label":"Target state","visibleWhen":"action==workflow.transition",
     "help":"The button hides itself when this transition is not currently allowed."},
    {"name":"workflowCode","type":"workflowCode","label":"Workflow","visibleWhen":"action==workflow.transition"},
    {"name":"workflowInstanceId","type":"number","label":"Instance id","visibleWhen":"action==workflow.transition",
     "help":"Defaults to the page active instance."},
    {"name":"pageCode","type":"pageCode","label":"Page","visibleWhen":"action==page.open"},
    {"name":"args","type":"object","label":"Arguments",
     "help":"external.api.call uses connector + operation + payload. notify.toast uses kind + title + message."}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('OtpVerify', 'ACTION', 'OTP Verify', 'Pin', '{"purpose":"DRIVER_VERIFY","cooldownSeconds":30}', '{"props":[
    {"name":"phoneField","type":"fieldCode","label":"Phone field","required":true},
    {"name":"purpose","type":"string","label":"Purpose","default":"DRIVER_VERIFY"},
    {"name":"cooldownSeconds","type":"number","label":"Resend cooldown (s)","default":30}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

-- ---------------------------------------------------------------- DEVICE

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('QrScan', 'DEVICE', 'QR / Barcode Scan', 'QrCodeScanner', '{"manualFallback":true}', '{"props":[
    {"name":"targetField","type":"fieldCode","label":"Writes to field","required":true,
     "help":"Required: without it the scan result goes nowhere."},
    {"name":"placeholder","type":"string","label":"Manual entry placeholder","default":"Enter code manually"},
    {"name":"manualFallback","type":"boolean","label":"Allow manual entry","default":true}
  ],"help":"Camera capture is not implemented yet; the scan button emits a placeholder code. Manual entry works."}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('WeighbridgeLive', 'DEVICE', 'Weighbridge (live)', 'Scale', '{"unit":"kg"}', '{"props":[
    {"name":"workerId","type":"string","label":"Telemetry worker id","required":true,
     "help":"Required: identifies the M9 device worker feeding the reading."},
    {"name":"targetField","type":"fieldCode","label":"Writes to field","required":true},
    {"name":"unit","type":"string","label":"Unit","default":"kg"}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

-- ---------------------------------------------------------------- UTILITY

INSERT INTO page_element_catalog (code, category, display_name, icon, default_props_json, config_schema_json)
VALUES ('PrintButton', 'ACTION', 'Print Button', 'Print', '{"label":"Print"}', '{"props":[
    {"name":"printCode","type":"printCode","label":"Print template","required":true,
     "help":"Required: the button stays disabled without it."},
    {"name":"idFrom","type":"enum","label":"Record id source","options":["lastCreatedId","prop"],
     "help":"lastCreatedId prints the record the page just created."},
    {"name":"id","type":"number","label":"Record id","visibleWhen":"idFrom==prop"},
    {"name":"label","type":"string","label":"Label","default":"Print"}
  ]}')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), icon=VALUES(icon), default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

-- ---------------------------------------------------------------- NEW ROWS
-- Seven primitives the renderer has always drawn and the catalog never listed.
-- ListView is inserted above (it pre-existed); these did not.

INSERT INTO page_element_catalog
  (code, category, display_name, icon, status, description, default_props_json, config_schema_json)
VALUES
('DetailView','DISPLAY','Detail View','Article','AVAILABLE',
 'Single-record page: header, tabbed sections of fields, and an action toolbar. Reads the record id from the URL query string.',
 '{"kind":"detail","schemaVersion":2}',
 '{"props":[
   {"name":"dataSource","type":"dataSource","label":"Data source","required":true,
    "help":"Required. The record id is bound from the URL (?id=), not from a prop."},
   {"name":"header","type":"object","label":"Header","shape":{"title":"string","subtitle":"string"},
    "help":"Supports {{row.field}} templates."},
   {"name":"tabs","type":"array","label":"Tabs",
    "help":"Each tab holds sections; each section holds fields ({field,label,type,options})."},
   {"name":"actionToolbar","type":"array","label":"Toolbar actions",
    "itemSchema":[
      {"name":"action","type":"actionCode","required":true,
       "options":["page.open","save","save.and.close","save.and.new","delete"]},
      {"name":"label","type":"string"},
      {"name":"page","type":"pageCode","visibleWhen":"action==page.open"},
      {"name":"variant","type":"enum","options":["primary","danger","outlined"]},
      {"name":"requiresConfirm","type":"boolean"}
    ]}
 ]}'),

('Markdown','DISPLAY','Rich Text','Notes','AVAILABLE',
 'Static explanatory text. Supports **bold** and auto-links http(s) URLs and /internal/paths.',
 '{"text":"Explain this page to the person using it."}',
 '{"props":[{"name":"text","type":"markdown","label":"Text","required":true}]}'),

('PivotTable','DISPLAY','Pivot Table','PivotTableChart','AVAILABLE',
 'Cross-tab: group by row and column fields and aggregate a measure.',
 '{"aggKind":"SUM","rowFields":[],"colFields":[]}',
 '{"props":[
   {"name":"baseEntity","type":"entityCode","label":"Entity","required":true},
   {"name":"rowFields","type":"array","label":"Row fields","itemType":"fieldCode","entityFrom":"baseEntity"},
   {"name":"colFields","type":"array","label":"Column fields","itemType":"fieldCode","entityFrom":"baseEntity"},
   {"name":"measureField","type":"fieldCode","label":"Measure","entityFrom":"baseEntity","required":true},
   {"name":"aggKind","type":"enum","label":"Aggregate","default":"SUM","options":["SUM","COUNT","AVG","MIN","MAX"]}
 ]}'),

('PushNotification','DISPLAY','Toast Notifications','Notifications','AVAILABLE',
 'Renders toasts raised anywhere on the page via the earms:notify event. Drop once per page; it has no visible body of its own.',
 '{"position":"top-right","autoDismissMs":4000,"maxStack":3}',
 '{"props":[
   {"name":"position","type":"enum","label":"Position","default":"top-right",
    "options":["top-right","top-center","bottom-right"]},
   {"name":"autoDismissMs","type":"number","label":"Auto-dismiss (ms)","default":4000,
    "help":"0 keeps toasts until dismissed."},
   {"name":"maxStack","type":"number","label":"Max stacked","default":3}
 ]}')
ON DUPLICATE KEY UPDATE
  category=VALUES(category), display_name=VALUES(display_name), icon=VALUES(icon),
  status=VALUES(status), description=VALUES(description),
  default_props_json=VALUES(default_props_json), config_schema_json=VALUES(config_schema_json);

-- ---------------------------------------------------------------- THE LIARS
-- registry.js renders these as text placeholders, e.g.
--   function DataTable({props}) { return <em>Table ({props?.rows || 0} rows)</em>; }
-- The catalog described DataTable as "Server-paginated table". Marked STUB so
-- the palette can grey them out instead of handing someone a placeholder.
-- Use ListView for a real table. Delete these rows once the impls land.

INSERT INTO page_element_catalog (code, category, status, display_name, description, config_schema_json)
VALUES ('DataTable', 'DISPLAY', 'STUB', 'Data Table (not implemented)', 'PLACEHOLDER — renders the text "Table (n rows)", not a table. Use List View instead.', '{"props":[{"name":"rows","type":"number","label":"Row count","default":0}]}')
ON DUPLICATE KEY UPDATE status=VALUES(status), display_name=VALUES(display_name), description=VALUES(description), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, status, display_name, description, config_schema_json)
VALUES ('LineChart', 'CHART', 'STUB', 'Line Chart (not implemented)', 'PLACEHOLDER — renders its title as text, not a chart.', '{"props":[{"name":"title","type":"string","label":"Title"}]}')
ON DUPLICATE KEY UPDATE status=VALUES(status), display_name=VALUES(display_name), description=VALUES(description), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

INSERT INTO page_element_catalog (code, category, status, display_name, description, config_schema_json)
VALUES ('TelemetryGauge', 'DEVICE', 'STUB', 'Telemetry Gauge (not implemented)', 'PLACEHOLDER — renders the min/max range as text, not a gauge. Weighbridge (live) is the working device readout.', '{"props":[
    {"name":"min","type":"number","label":"Min","default":0},
    {"name":"max","type":"number","label":"Max","default":100},
    {"name":"unit","type":"string","label":"Unit"}
  ]}')
ON DUPLICATE KEY UPDATE status=VALUES(status), display_name=VALUES(display_name), description=VALUES(description), config_schema_json=VALUES(config_schema_json), category=VALUES(category);

-- ---------------------------------------------------------------- CELL MODES
-- Not page elements: these are columns[].render values on a List View. The
-- renderer has no primitive for them by design. Flagged so a palette that
-- filters on category <> 'CELL' cannot offer them as droppable elements.

UPDATE page_element_catalog SET
  status = 'AVAILABLE',
  display_name = CASE code
    WHEN 'SparklineCell' THEN 'Sparkline (column mode)'
    WHEN 'ProgressCell'  THEN 'Progress bar (column mode)'
    WHEN 'PillCell'      THEN 'Status pill (column mode)'
    WHEN 'DeltaCell'     THEN 'Delta arrow (column mode)'
  END,
  config_schema_json = '{"cellMode":true,"usage":"Set columns[].render on a List View. Not a droppable page element."}'
WHERE category = 'CELL';
