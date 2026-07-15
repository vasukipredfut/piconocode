-- =====================================================================
-- V16 — Reporting cube: saved_view + pivot_view tables for user-defined
--       reports + onboarding wizard page.
-- =====================================================================

CREATE TABLE IF NOT EXISTS m0_saved_view (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  owner_id BIGINT NULL,
  view_code VARCHAR(64) NOT NULL,
  view_name VARCHAR(256) NOT NULL,
  base_page_code VARCHAR(128) NOT NULL,
  filter_json TEXT NULL,
  sort_json TEXT NULL,
  columns_json TEXT NULL,
  shared BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_saved_view (tenant_id, view_code),
  KEY idx_sv_page (base_page_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m0_pivot_view (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  pivot_code VARCHAR(64) NOT NULL,
  pivot_name VARCHAR(256) NOT NULL,
  base_entity VARCHAR(64) NOT NULL,
  row_fields_json TEXT NOT NULL,
  col_fields_json TEXT NULL,
  measure_field VARCHAR(64) NOT NULL,
  agg_kind VARCHAR(16) NOT NULL DEFAULT 'SUM',
  filter_json TEXT NULL,
  shared BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_pivot (tenant_id, pivot_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m0_saved_view (tenant_id, owner_id, view_code, view_name, base_page_code, filter_json, shared) VALUES
  (1, 1001, 'sv:my-pending-grns', 'My Pending GRNs',  'pico:grn-list',  '{"status":["DRAFT","ITEMS_LOGGED","QC_CHECKED"]}', FALSE),
  (1, 1001, 'sv:overdue-arc',     'Overdue Receivables', 'pico:si-list','{"status":["SENT","PARTIAL","UNPAID"]}', TRUE),
  (1, 1001, 'sv:p1-mos-only',     'P1 Maintenance Orders Only', 'pico:mo-list','{"priority":["P1"]}', TRUE);

INSERT IGNORE INTO m0_pivot_view (tenant_id, pivot_code, pivot_name, base_entity, row_fields_json, col_fields_json, measure_field, agg_kind, shared) VALUES
  (1,'pv:stock-by-item-bin','Stock by Item × Bin','StockBalance','["item_code"]','["bin_code"]','qty_on_hand','SUM',TRUE),
  (1,'pv:movements-by-day-kind','Movements by Day × Kind','StockMovement','["DATE(ts)"]','["kind"]','qty','SUM',TRUE),
  (1,'pv:si-by-customer-status','Sales Invoices by Customer × Status','SalesInvoice','["customer_name"]','["status"]','total_amount','SUM',TRUE),
  (1,'pv:energy-by-meter-shift','Energy by Meter × Shift','EnergyReading','["meter_code"]','["shift_code"]','consumption','SUM',TRUE);

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'SavedView','Saved View','m0_saved_view','User-saved filter+sort views.','M0' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='SavedView');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'PivotView','Pivot View','m0_pivot_view','Row × Col × Measure pivot config.','M0' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='PivotView');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SavedView','view_code','Code','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SavedView' AND field_code='view_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SavedView','view_name','Name','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SavedView' AND field_code='view_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SavedView','base_page_code','Base Page','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SavedView' AND field_code='base_page_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SavedView','shared','Shared','BOOLEAN',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SavedView' AND field_code='shared');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PivotView','pivot_code','Code','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PivotView' AND field_code='pivot_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PivotView','pivot_name','Name','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PivotView' AND field_code='pivot_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PivotView','base_entity','Entity','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PivotView' AND field_code='base_entity');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PivotView','measure_field','Measure','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PivotView' AND field_code='measure_field');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PivotView','agg_kind','Agg','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PivotView' AND field_code='agg_kind');

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:saved-views','Saved Views','User-defined report views.','Report','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:saved-views');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pivot-views','Pivot Tables','Configured pivot tables.','Report','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pivot-views');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pivot-stock','Pivot — Stock by Item × Bin','Live pivot.','Report','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pivot-stock');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:onboarding-wizard','Tenant Onboarding','Multi-tenant onboarding chooser.','M0','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:onboarding-wizard');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"SavedView","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","view_code","view_name","base_page_code","shared"]},"columns":[{"field":"view_code","label":"Code","sticky":"left","sortable":true},{"field":"view_name","label":"Name"},{"field":"base_page_code","label":"Base Page","render":"badge"},{"field":"shared","label":"Shared","render":"badge","colorMap":{"true":"green","false":"grey","1":"green","0":"grey"}}],"filters":[{"field":"shared","label":"Shared only","type":"bool-toggle"}],"sort":{"default":[{"field":"view_name","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Saved Views"}}', 'V16', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:saved-views' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"PivotView","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","pivot_code","pivot_name","base_entity","measure_field","agg_kind","shared"]},"columns":[{"field":"pivot_code","label":"Code","sticky":"left","sortable":true},{"field":"pivot_name","label":"Name"},{"field":"base_entity","label":"Entity","render":"badge"},{"field":"measure_field","label":"Measure"},{"field":"agg_kind","label":"Agg","render":"badge","colorMap":{"SUM":"green","COUNT":"blue","AVG":"amber","MIN":"purple","MAX":"red"}}],"sort":{"default":[{"field":"pivot_name","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Pivot Tables"}}', 'V16', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pivot-views' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"schemaVersion":2,"layout":{"type":"PivotTable","props":{"pivotCode":"pv:stock-by-item-bin","baseEntity":"StockBalance","rowFields":["item_code"],"colFields":["bin_code"],"measureField":"qty_on_hand","aggKind":"SUM"}}}', 'V16', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pivot-stock' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Welcome — Tenant Onboarding","fields":[{"kind":"FormField","type":"text","name":"tenantName","label":"Company Name"},{"kind":"FormField","type":"text","name":"gstin","label":"GSTIN"},{"kind":"FormField","type":"select","name":"industry","label":"Industry","options":[{"label":"Edible Oil Mill","value":"OIL_MILL"},{"label":"Packaging","value":"PACKAGING"},{"label":"Logistics","value":"LOGISTICS"},{"label":"FMCG","value":"FMCG"}]},{"kind":"FormField","type":"select","name":"presetPack","label":"Preset Pack","options":[{"label":"Pico Power (Oil)","value":"PICO"},{"label":"Generic MSME","value":"GENERIC"},{"label":"Custom","value":"CUSTOM"}]},{"kind":"FormField","type":"select","name":"modules","label":"Enable Modules","options":[{"label":"Dispatch + Inventory + Finance","value":"M5_M6_M10"},{"label":"+ HR + Production","value":"FULL"},{"label":"Minimal — Inventory only","value":"M5_ONLY"}]},{"kind":"WorkflowActionButton","label":"Provision Tenant","action":"workflow.transition","args":{"workflowCode":"M0.TenantOnboarding"},"variant":"primary"}]},{"kind":"Section","title":"Next steps","fields":[{"kind":"WorkflowActionButton","label":"Configure Connectors (M11)","action":"page.open","args":{"pageCode":"pico:m11-integrations-list"}},{"kind":"WorkflowActionButton","label":"Set up Users & Roles","action":"page.open","args":{"pageCode":"pico:employee-master"}},{"kind":"WorkflowActionButton","label":"Import Item / BOM Masters","action":"page.open","args":{"pageCode":"pico:bom-list"}}]}]}', 'V16', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:onboarding-wizard' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code IN ('pico:saved-views','pico:pivot-views','pico:pivot-stock','pico:onboarding-wizard') AND current_version_id IS NULL;
