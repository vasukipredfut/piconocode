-- =====================================================================
-- M0 — Schema V2 substrate (Round 1G)
-- =====================================================================
-- Six new tables that implement the Schema V2 contract from
-- docs/configurable-pages/SCHEMA-V2.md §18:
--   1. page_def_workflow_binding (versioned attachment; complements the
--      existing page_workflow_binding which is keyed by page_code rather
--      than page_version_id — both coexist during the v1→v2 migration
--      window)
--   2. tenant_list_view  (saved-views; per-page, per-scope)
--   3. entity_registry + entity_field_registry (Builder palette source)
--   4. api_endpoint_registry (named endpoints for dataSource type=api)
--   5. action_registry (all 30 ACTION-CATALOG codes as data; only 7 ship
--      with implemented=TRUE in Round 1G — the other 23 are still browseable
--      but ActionDispatcher returns 422 implementation_pending)
--
-- Conventions: InnoDB / utf8mb4 / ROW_FORMAT=DYNAMIC / JSON columns map via
-- Hibernate 6 @JdbcTypeCode(SqlTypes.JSON). Idempotent (CREATE IF NOT
-- EXISTS + INSERT IGNORE) so M0PageSchemaInitialiser can replay safely.
-- =====================================================================

CREATE TABLE IF NOT EXISTS page_def_workflow_binding (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  page_version_id BIGINT NOT NULL,
  workflow_code VARCHAR(128) NOT NULL,
  workflow_version INT NOT NULL,
  binding_role ENUM('SUBMIT_OPENS','SUBMIT_ADVANCES','SUBMIT_CLOSES','READ_ONLY') NOT NULL,
  field_map_json JSON NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pdwb_pv FOREIGN KEY (page_version_id) REFERENCES page_version(id),
  KEY idx_pdwb_workflow (workflow_code, workflow_version)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tenant_list_view (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  page_slug VARCHAR(128) NOT NULL,
  name VARCHAR(128) NOT NULL,
  description TEXT NULL,
  icon VARCHAR(64) NULL,
  scope ENUM('TENANT','ROLE','USER') NOT NULL,
  scope_value VARCHAR(128) NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  body_json JSON NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT NULL,
  KEY idx_listview_tenant_page (tenant_id, page_slug, scope)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS entity_registry (
  code VARCHAR(128) NOT NULL PRIMARY KEY,
  display_name VARCHAR(256) NOT NULL,
  table_name VARCHAR(128) NOT NULL,
  description TEXT NULL,
  category VARCHAR(64) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS entity_field_registry (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  entity_code VARCHAR(128) NOT NULL,
  field_code VARCHAR(128) NOT NULL,
  display_label VARCHAR(256) NOT NULL,
  data_type ENUM('STRING','INT','LONG','DECIMAL','BOOLEAN','DATE','DATETIME','TIME','TEXT','JSON','ENUM','RELATION','COMPUTED') NOT NULL,
  filterable BOOLEAN NOT NULL DEFAULT TRUE,
  sortable BOOLEAN NOT NULL DEFAULT TRUE,
  searchable BOOLEAN NOT NULL DEFAULT FALSE,
  required BOOLEAN NOT NULL DEFAULT FALSE,
  default_value TEXT NULL,
  options_source_json JSON NULL,
  relation_entity VARCHAR(128) NULL,
  UNIQUE KEY uq_field (entity_code, field_code),
  CONSTRAINT fk_efr_entity FOREIGN KEY (entity_code) REFERENCES entity_registry(code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS api_endpoint_registry (
  code VARCHAR(128) NOT NULL PRIMARY KEY,
  display_name VARCHAR(256) NOT NULL,
  method VARCHAR(8) NOT NULL,
  url_template VARCHAR(512) NOT NULL,
  request_schema_json JSON NULL,
  response_schema_json JSON NULL,
  internal BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS action_registry (
  code VARCHAR(64) NOT NULL PRIMARY KEY,
  display_name VARCHAR(256) NOT NULL,
  category VARCHAR(32) NOT NULL,
  params_schema_json JSON NOT NULL,
  implemented BOOLEAN NOT NULL DEFAULT FALSE,
  requires_role VARCHAR(64) NULL,
  description TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- Action registry seed (all 30 codes per ACTION-CATALOG.md) --------
-- implemented=TRUE only for the 7 Round 1G handlers; the rest return
-- 422 implementation_pending until Round 1G.2 lands them.
INSERT IGNORE INTO action_registry (code, display_name, category, implemented, params_schema_json, description) VALUES
  ('save',                 'Save',                       'PERSIST',     TRUE,  JSON_OBJECT('type','object','properties',JSON_OBJECT('entity',JSON_OBJECT('type','string'),'formData',JSON_OBJECT('type','object')),'required',JSON_ARRAY('entity','formData')), 'Persist the current form payload to its bound entity.'),
  ('save.and.new',         'Save and New',               'PERSIST',     TRUE,  JSON_OBJECT('type','object'), 'Save then clear the form for a new entry.'),
  ('save.and.close',       'Save and Close',             'PERSIST',     TRUE,  JSON_OBJECT('type','object'), 'Save then navigate back.'),
  ('delete',               'Delete',                     'PERSIST',     TRUE,  JSON_OBJECT('type','object','properties',JSON_OBJECT('entity',JSON_OBJECT('type','string'),'recordId',JSON_OBJECT('type','integer'))), 'Soft-delete the record.'),
  ('workflow.transition',  'Workflow Transition',        'WORKFLOW',    TRUE,  JSON_OBJECT('type','object','properties',JSON_OBJECT('instanceId',JSON_OBJECT('type','integer'),'pageCode',JSON_OBJECT('type','string'),'formData',JSON_OBJECT('type','object'))), 'Submit a page to the workflow engine.'),
  ('page.open',            'Open Page',                  'NAV',         TRUE,  JSON_OBJECT('type','object','properties',JSON_OBJECT('page',JSON_OBJECT('type','string'),'params',JSON_OBJECT('type','object'))), 'Navigate to another configurable page.'),
  ('export.csv',           'Export CSV',                 'EXPORT',      TRUE,  JSON_OBJECT('type','object','properties',JSON_OBJECT('spec',JSON_OBJECT('type','object'))), 'Export the current list view to a CSV file.'),
  -- PERSIST family — remaining
  ('discard',              'Discard',                    'PERSIST',     FALSE, JSON_OBJECT('type','object'), 'Discard unsaved changes.'),
  ('duplicate',            'Duplicate',                  'PERSIST',     FALSE, JSON_OBJECT('type','object'), 'Clone an existing row.'),
  ('archive',              'Archive',                    'PERSIST',     FALSE, JSON_OBJECT('type','object'), 'Archive without deletion.'),
  ('restore',              'Restore',                    'PERSIST',     FALSE, JSON_OBJECT('type','object'), 'Restore from archived state.'),
  -- WORKFLOW family
  ('workflow.start',       'Start Workflow',             'WORKFLOW',    FALSE, JSON_OBJECT('type','object'), 'Start a new workflow instance.'),
  ('workflow.assign',      'Assign Workflow',            'WORKFLOW',    FALSE, JSON_OBJECT('type','object'), 'Reassign instance to a user.'),
  ('workflow.cancel',      'Cancel Workflow',            'WORKFLOW',    FALSE, JSON_OBJECT('type','object'), 'Cancel an open instance.'),
  -- NAV family
  ('page.back',            'Go Back',                    'NAV',         FALSE, JSON_OBJECT('type','object'), 'Pop the navigation stack.'),
  ('page.refresh',         'Refresh',                    'NAV',         FALSE, JSON_OBJECT('type','object'), 'Re-fetch current page data.'),
  -- PRINT family
  ('print',                'Print',                      'PRINT',       FALSE, JSON_OBJECT('type','object'), 'Print via the browser dialog.'),
  ('print.template',       'Print Template',             'PRINT',       FALSE, JSON_OBJECT('type','object'), 'Render and print a named template.'),
  -- EXPORT family
  ('export.xlsx',          'Export XLSX',                'EXPORT',      FALSE, JSON_OBJECT('type','object'), 'Export current list to XLSX.'),
  ('export.pdf',           'Export PDF',                 'EXPORT',      FALSE, JSON_OBJECT('type','object'), 'Export current view to PDF.'),
  -- NOTIFY family
  ('notify.email',         'Send Email',                 'NOTIFY',      FALSE, JSON_OBJECT('type','object'), 'Send an email via tenant SMTP.'),
  ('notify.sms',           'Send SMS',                   'NOTIFY',      FALSE, JSON_OBJECT('type','object'), 'Send an SMS notification.'),
  ('notify.toast',         'Toast Notification',         'NOTIFY',      FALSE, JSON_OBJECT('type','object'), 'Show an in-page toast.'),
  -- REPORT family
  ('report.run',           'Run Report',                 'REPORT',      FALSE, JSON_OBJECT('type','object'), 'Execute a registered report.'),
  ('report.schedule',      'Schedule Report',            'REPORT',      FALSE, JSON_OBJECT('type','object'), 'Schedule a recurring report.'),
  -- BULK family
  ('bulk.update',          'Bulk Update',                'BULK',        FALSE, JSON_OBJECT('type','object'), 'Update multiple rows in one action.'),
  ('bulk.delete',          'Bulk Delete',                'BULK',        FALSE, JSON_OBJECT('type','object'), 'Delete multiple rows in one action.'),
  -- EXPR family
  ('expr.evaluate',        'Evaluate Expression',        'EXPR',        FALSE, JSON_OBJECT('type','object'), 'Evaluate a sandboxed expression.'),
  ('expr.compute',         'Compute Derived Value',      'EXPR',        FALSE, JSON_OBJECT('type','object'), 'Recompute a derived field.'),
  -- INTEGRATION family
  ('external.api.call',    'External API Call',          'INTEGRATION', FALSE, JSON_OBJECT('type','object'), 'Invoke an api_endpoint_registry endpoint.');

-- ----- Entity registry seed -----------------------------------------------
INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, description) VALUES
  ('PageDefinition',     'Page Definition',     'page_definition',   'M0', 'Configurable page metadata.'),
  ('PageVersion',        'Page Version',        'page_version',      'M0', 'Versioned page bodies.'),
  ('WorkflowDefinition', 'Workflow Definition', 'workflow_definition','M0', 'Workflow blueprints (versioned).'),
  ('WorkflowInstance',   'Workflow Instance',   'workflow_instance', 'M0', 'Running workflow state machines.'),
  ('SavedView',          'Saved View',          'tenant_list_view',  'M0', 'User-saved list view definitions.'),
  ('Vendor',             'Vendor',              'm0_vendor',         'M3', 'Vendor master (Schema V2 demo).'),
  ('State',              'State',               'state',             'COMMON', 'GST state master.'),
  ('City',               'City',                'city',              'COMMON', 'City master keyed to state.');

-- ----- Entity field registry seed (Vendor + State + City for the demo) ----
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) VALUES
  ('Vendor', 'id',         'Id',        'LONG',    FALSE, TRUE,  FALSE, FALSE),
  ('Vendor', 'name',       'Vendor',    'STRING',  TRUE,  TRUE,  TRUE,  TRUE),
  ('Vendor', 'gstin',      'GSTIN',     'STRING',  TRUE,  TRUE,  TRUE,  FALSE),
  ('Vendor', 'city_id',    'City',      'RELATION',TRUE,  TRUE,  FALSE, FALSE),
  ('Vendor', 'state_id',   'State',     'RELATION',TRUE,  TRUE,  FALSE, FALSE),
  ('Vendor', 'is_msme',    'MSME',      'BOOLEAN', TRUE,  TRUE,  FALSE, FALSE),
  ('Vendor', 'status',     'Status',    'ENUM',    TRUE,  TRUE,  FALSE, TRUE),
  ('Vendor', 'tenant_id',  'Tenant',    'LONG',    TRUE,  FALSE, FALSE, TRUE),
  ('State',  'id',         'Id',        'LONG',    FALSE, TRUE,  FALSE, FALSE),
  ('State',  'name',       'Name',      'STRING',  TRUE,  TRUE,  TRUE,  TRUE),
  ('State',  'gst_state_code','GST Code','STRING', TRUE,  TRUE,  FALSE, FALSE),
  ('City',   'id',         'Id',        'LONG',    FALSE, TRUE,  FALSE, FALSE),
  ('City',   'name',       'Name',      'STRING',  TRUE,  TRUE,  TRUE,  TRUE),
  ('City',   'stateid',    'State Id',  'LONG',    TRUE,  TRUE,  FALSE, FALSE),
  ('PageDefinition','code','Code',      'STRING',  TRUE,  TRUE,  TRUE,  TRUE),
  ('PageDefinition','title','Title',    'STRING',  TRUE,  TRUE,  TRUE,  TRUE),
  ('PageDefinition','status','Status',  'ENUM',    TRUE,  TRUE,  FALSE, TRUE),
  ('PageDefinition','category','Category','STRING',TRUE,  TRUE,  FALSE, FALSE),
  ('WorkflowInstance','currentState','Current State','STRING',TRUE,TRUE,FALSE,TRUE),
  ('WorkflowInstance','status','Status','ENUM',    TRUE,  TRUE,  FALSE, TRUE),
  ('WorkflowInstance','assignedRole','Assigned Role','STRING',TRUE,TRUE,FALSE,FALSE),
  ('SavedView','name',      'Name',      'STRING',  TRUE,  TRUE,  TRUE,  TRUE),
  ('SavedView','scope',     'Scope',     'ENUM',    TRUE,  TRUE,  FALSE, TRUE),
  ('SavedView','page_slug', 'Page Slug', 'STRING',  TRUE,  TRUE,  FALSE, TRUE),
  ('SavedView','scope_value','Scope Value','STRING',TRUE,  TRUE,  FALSE, FALSE),
  ('SavedView','body_json', 'Body JSON', 'JSON',    FALSE, FALSE, FALSE, TRUE),
  ('SavedView','is_default','Is Default','BOOLEAN', TRUE,  TRUE,  FALSE, FALSE),
  ('SavedView','tenant_id', 'Tenant',    'LONG',    FALSE, FALSE, FALSE, TRUE),
  ('SavedView','created_by','Created By','LONG',    TRUE,  TRUE,  FALSE, FALSE);

-- ----- API endpoint registry seed (named endpoints the renderer can call) -
INSERT IGNORE INTO api_endpoint_registry (code, display_name, method, url_template, internal) VALUES
  ('m0.datasource.query',  'DataSource Query',    'POST', '/api/m0/datasource/query', TRUE),
  ('m0.actions.invoke',    'Action Invoke',       'POST', '/api/m0/actions/{code}/invoke', TRUE),
  ('m0.savedviews.list',   'Saved Views List',    'GET',  '/api/m0/saved-views', TRUE),
  ('m0.entityregistry',    'Entity Registry',     'GET',  '/api/m0/entity-registry', TRUE),
  ('workflow.instance.submit','Workflow Submit',  'POST', '/api/workflow/instances/{id}/submit', TRUE);
