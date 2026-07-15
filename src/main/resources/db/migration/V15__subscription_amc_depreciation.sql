-- =====================================================================
-- V15 — Subscription/AMC orders + Asset depreciation cycle.
-- =====================================================================

CREATE TABLE IF NOT EXISTS m5_subscription (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  sub_number VARCHAR(32) NOT NULL,
  customer_name VARCHAR(256) NOT NULL,
  product_code VARCHAR(64) NULL,
  plan_name VARCHAR(128) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NULL,
  frequency VARCHAR(16) NOT NULL,
  recurring_amount DECIMAL(14,2) NOT NULL,
  next_invoice_date DATE NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_sub_number (tenant_id, sub_number),
  KEY idx_sub_next (next_invoice_date, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_amc_contract (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  amc_number VARCHAR(32) NOT NULL,
  asset_id BIGINT NULL,
  vendor_name VARCHAR(256) NOT NULL,
  cover_kind VARCHAR(16) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  annual_value DECIMAL(14,2) NOT NULL,
  pm_freq_months INT NOT NULL DEFAULT 3,
  last_pm_on DATE NULL,
  next_pm_on DATE NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_amc_number (tenant_id, amc_number),
  KEY idx_amc_status (status, next_pm_on)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_asset_depreciation (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  asset_id BIGINT NOT NULL,
  asset_code VARCHAR(32) NOT NULL,
  method VARCHAR(16) NOT NULL,
  acquisition_cost DECIMAL(14,2) NOT NULL,
  salvage_value DECIMAL(14,2) NOT NULL DEFAULT 0,
  useful_life_months INT NOT NULL,
  start_date DATE NOT NULL,
  posted_for_period VARCHAR(8) NOT NULL,
  depreciation_amount DECIMAL(14,2) NOT NULL,
  accumulated_dep DECIMAL(14,2) NOT NULL,
  net_book_value DECIMAL(14,2) NOT NULL,
  posted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_dep_period (tenant_id, asset_id, posted_for_period)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_subscription (tenant_id, sub_number, customer_name, product_code, plan_name, start_date, frequency, recurring_amount, next_invoice_date, status) VALUES
  (1,'SUB-2026-001','Hotel Saravana Bhavan',  'OIL-SUN-RFD','Monthly 500kg sunflower', '2026-01-01','MONTHLY', 11500.00,'2026-07-01','ACTIVE'),
  (1,'SUB-2026-002','Coimbatore Foods Pvt Ltd','OIL-GND-RFD','Weekly 100kg groundnut',  '2026-03-15','WEEKLY',   2300.00,'2026-06-15','ACTIVE'),
  (1,'SUB-2026-003','Madurai Hotels Co',     'PET-1L-GND','Bi-weekly 500 PCS',         '2025-12-01','BIWEEKLY', 11500.00, NULL,'PAUSED');

INSERT IGNORE INTO m5_amc_contract (tenant_id, amc_number, asset_id, vendor_name, cover_kind, start_date, end_date, annual_value, pm_freq_months, last_pm_on, next_pm_on, status) VALUES
  (1,'AMC-2026-001',(SELECT id FROM m5_asset WHERE asset_code='WB-01'),'Avery Weigh-Tronix','COMPREHENSIVE','2026-04-01','2027-03-31', 120000.00, 3, '2026-04-15','2026-07-15','ACTIVE'),
  (1,'AMC-2026-002',(SELECT id FROM m5_asset WHERE asset_code='CONV-PACK-1'),'SSI Schaefer','PARTS_ONLY',  '2026-01-01','2027-12-31',  85000.00, 6, '2026-01-15','2026-07-15','ACTIVE');

INSERT IGNORE INTO m5_asset_depreciation (tenant_id, asset_id, asset_code, method, acquisition_cost, salvage_value, useful_life_months, start_date, posted_for_period, depreciation_amount, accumulated_dep, net_book_value) VALUES
  (1,(SELECT id FROM m5_asset WHERE asset_code='WB-01'),'WB-01','STRAIGHT_LINE',1500000.00,150000.00,120,'2022-04-15','2026-05', 11250.00, 412500.00, 1087500.00),
  (1,(SELECT id FROM m5_asset WHERE asset_code='WB-01'),'WB-01','STRAIGHT_LINE',1500000.00,150000.00,120,'2022-04-15','2026-06', 11250.00, 423750.00, 1076250.00),
  (1,(SELECT id FROM m5_asset WHERE asset_code='CONV-PACK-1'),'CONV-PACK-1','STRAIGHT_LINE',850000.00,85000.00,84,'2023-01-10','2026-06',  9107.14, 318750.00,  531250.00);

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.Subscription',1,'PUBLISHED','{"code":"M5.Subscription","version":1,"initialState":"ACTIVE","states":[{"code":"ACTIVE","assignedRole":"Dispatch"},{"code":"PAUSED","assignedRole":"Manager"},{"code":"CANCELLED","terminal":true},{"code":"EXPIRED","terminal":true}],"transitions":[{"from":"ACTIVE","to":"PAUSED","trigger":"page.submitted","pageCode":"pico:sub-pause"},{"from":"PAUSED","to":"ACTIVE","trigger":"page.submitted","pageCode":"pico:sub-resume"},{"from":"ACTIVE","to":"CANCELLED","trigger":"page.submitted","pageCode":"pico:sub-cancel"},{"from":"PAUSED","to":"CANCELLED","trigger":"page.submitted","pageCode":"pico:sub-cancel"}]}'),
(1,'M5.AmcContract',1,'PUBLISHED','{"code":"M5.AmcContract","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Dispatch"},{"code":"ACTIVE","assignedRole":"Dispatch"},{"code":"PM_DUE","assignedRole":"UnloadingSupervisor"},{"code":"EXPIRED","terminal":true},{"code":"TERMINATED","terminal":true}],"transitions":[{"from":"DRAFT","to":"ACTIVE","trigger":"page.submitted","pageCode":"pico:amc-activate"},{"from":"ACTIVE","to":"PM_DUE","trigger":"page.submitted","pageCode":"pico:amc-pm-trigger"},{"from":"PM_DUE","to":"ACTIVE","trigger":"page.submitted","pageCode":"pico:amc-pm-done"},{"from":"ACTIVE","to":"TERMINATED","trigger":"page.submitted","pageCode":"pico:amc-terminate"}]}');

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'Subscription','Subscription','m5_subscription','Recurring customer subscription.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='Subscription');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'AmcContract','AMC Contract','m5_amc_contract','Annual maintenance contract.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='AmcContract');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'AssetDepreciation','Asset Depreciation','m5_asset_depreciation','Per-period depreciation posting.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='AssetDepreciation');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Subscription','sub_number','Sub #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Subscription' AND field_code='sub_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Subscription','customer_name','Customer','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Subscription' AND field_code='customer_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Subscription','plan_name','Plan','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Subscription' AND field_code='plan_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Subscription','frequency','Freq','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Subscription' AND field_code='frequency');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Subscription','recurring_amount','Amount','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Subscription' AND field_code='recurring_amount');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Subscription','next_invoice_date','Next Inv','DATE',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Subscription' AND field_code='next_invoice_date');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Subscription','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Subscription' AND field_code='status');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AmcContract','amc_number','AMC #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AmcContract' AND field_code='amc_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AmcContract','vendor_name','Vendor','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AmcContract' AND field_code='vendor_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AmcContract','cover_kind','Cover','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AmcContract' AND field_code='cover_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AmcContract','end_date','Ends','DATE',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AmcContract' AND field_code='end_date');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AmcContract','next_pm_on','Next PM','DATE',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AmcContract' AND field_code='next_pm_on');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AmcContract','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AmcContract' AND field_code='status');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AssetDepreciation','asset_code','Asset','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AssetDepreciation' AND field_code='asset_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AssetDepreciation','posted_for_period','Period','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AssetDepreciation' AND field_code='posted_for_period');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AssetDepreciation','method','Method','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AssetDepreciation' AND field_code='method');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AssetDepreciation','depreciation_amount','Period Dep','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AssetDepreciation' AND field_code='depreciation_amount');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AssetDepreciation','accumulated_dep','Accum','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AssetDepreciation' AND field_code='accumulated_dep');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AssetDepreciation','net_book_value','NBV','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AssetDepreciation' AND field_code='net_book_value');

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:sub-list','Subscriptions','Recurring customer subs.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:sub-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:sub-create','New Subscription','Set up recurring plan.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:sub-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:sub-pause','Sub — Pause','Pause subscription.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:sub-pause');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:sub-resume','Sub — Resume','Resume from pause.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:sub-resume');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:sub-cancel','Sub — Cancel','Cancel subscription.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:sub-cancel');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:amc-list','AMC Contracts','Annual maintenance contracts.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:amc-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:amc-create','New AMC','Set up AMC.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:amc-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:amc-activate','AMC — Activate','Activate after signing.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:amc-activate');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:amc-pm-trigger','AMC — Trigger PM','Trigger preventive maintenance.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:amc-pm-trigger');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:amc-pm-done','AMC — Mark PM Done','PM completed.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:amc-pm-done');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:amc-terminate','AMC — Terminate','Early termination.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:amc-terminate');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:dep-list','Asset Depreciation','Per-period depreciation postings.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dep-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:dep-run','Depreciation Run','Post monthly depreciation.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dep-run');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Subscription","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","sub_number","customer_name","plan_name","frequency","recurring_amount","next_invoice_date","status"]},"columns":[{"field":"sub_number","label":"Sub #","sticky":"left","sortable":true},{"field":"customer_name","label":"Customer"},{"field":"plan_name","label":"Plan"},{"field":"frequency","label":"Freq","render":"badge","colorMap":{"WEEKLY":"blue","BIWEEKLY":"purple","MONTHLY":"green","QUARTERLY":"amber","ANNUAL":"red"}},{"field":"recurring_amount","label":"Amount","render":"number","align":"right"},{"field":"next_invoice_date","label":"Next Inv","render":"date"},{"field":"status","label":"Status","render":"badge","colorMap":{"ACTIVE":"green","PAUSED":"amber","CANCELLED":"red","EXPIRED":"grey"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["ACTIVE","PAUSED","CANCELLED","EXPIRED"]}}],"sort":{"default":[{"field":"next_invoice_date","dir":"asc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Subscriptions"}}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:sub-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New Subscription","fields":[{"kind":"FormField","type":"text","name":"customerName","label":"Customer"},{"kind":"FormField","type":"text","name":"productCode","label":"Product"},{"kind":"FormField","type":"text","name":"planName","label":"Plan Name"},{"kind":"FormField","type":"text","name":"startDate","label":"Start Date"},{"kind":"FormField","type":"select","name":"frequency","label":"Frequency","options":[{"label":"WEEKLY","value":"WEEKLY"},{"label":"BIWEEKLY","value":"BIWEEKLY"},{"label":"MONTHLY","value":"MONTHLY"},{"label":"QUARTERLY","value":"QUARTERLY"},{"label":"ANNUAL","value":"ANNUAL"}]},{"kind":"FormField","type":"number","name":"recurringAmount","label":"Recurring Amount"},{"kind":"WorkflowActionButton","label":"Create Subscription","action":"save","variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:sub-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Pause Subscription","fields":[{"kind":"FormField","type":"text","name":"reason","label":"Reason"},{"kind":"FormField","type":"text","name":"resumeOn","label":"Planned Resume"},{"kind":"WorkflowActionButton","label":"Pause","action":"workflow.transition","variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:sub-pause' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Resume Subscription","fields":[{"kind":"FormField","type":"text","name":"nextInvoiceDate","label":"Next Invoice Date"},{"kind":"WorkflowActionButton","label":"Resume","action":"workflow.transition","variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:sub-resume' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Cancel Subscription","fields":[{"kind":"FormField","type":"text","name":"reason","label":"Cancellation Reason"},{"kind":"WorkflowActionButton","label":"Cancel","action":"workflow.transition","variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:sub-cancel' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"AmcContract","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","amc_number","vendor_name","cover_kind","end_date","next_pm_on","status"]},"columns":[{"field":"amc_number","label":"AMC #","sticky":"left","sortable":true},{"field":"vendor_name","label":"Vendor"},{"field":"cover_kind","label":"Cover","render":"badge","colorMap":{"COMPREHENSIVE":"green","PARTS_ONLY":"amber","LABOUR_ONLY":"blue"}},{"field":"end_date","label":"Ends","render":"date"},{"field":"next_pm_on","label":"Next PM","render":"date"},{"field":"status","label":"Status","render":"badge","colorMap":{"DRAFT":"grey","ACTIVE":"green","PM_DUE":"amber","EXPIRED":"red","TERMINATED":"red"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["DRAFT","ACTIVE","PM_DUE","EXPIRED","TERMINATED"]}}],"sort":{"default":[{"field":"next_pm_on","dir":"asc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"AMC Contracts"}}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:amc-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New AMC","fields":[{"kind":"FormField","type":"text","name":"assetCode","label":"Asset"},{"kind":"FormField","type":"text","name":"vendorName","label":"Vendor"},{"kind":"FormField","type":"select","name":"coverKind","label":"Cover","options":[{"label":"COMPREHENSIVE","value":"COMPREHENSIVE"},{"label":"PARTS_ONLY","value":"PARTS_ONLY"},{"label":"LABOUR_ONLY","value":"LABOUR_ONLY"}]},{"kind":"FormField","type":"text","name":"startDate","label":"Start"},{"kind":"FormField","type":"text","name":"endDate","label":"End"},{"kind":"FormField","type":"number","name":"annualValue","label":"Annual Value"},{"kind":"FormField","type":"number","name":"pmFreqMonths","label":"PM Freq (months)"},{"kind":"WorkflowActionButton","label":"Save Draft","action":"save","variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:amc-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Activate AMC","fields":[{"kind":"FormField","type":"text","name":"signedOn","label":"Signed On"},{"kind":"WorkflowActionButton","label":"Activate","action":"workflow.transition","variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:amc-activate' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Trigger PM","fields":[{"kind":"FormField","type":"text","name":"scheduledOn","label":"Schedule On"},{"kind":"WorkflowActionButton","label":"Trigger PM","action":"workflow.transition","variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:amc-pm-trigger' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Mark PM Done","fields":[{"kind":"FormField","type":"text","name":"completedOn","label":"Completed On"},{"kind":"FormField","type":"text","name":"workNotes","label":"Work Notes"},{"kind":"WorkflowActionButton","label":"Mark Done","action":"workflow.transition","variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:amc-pm-done' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Terminate AMC","fields":[{"kind":"FormField","type":"text","name":"reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Terminate","action":"workflow.transition","variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:amc-terminate' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"AssetDepreciation","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","asset_code","posted_for_period","method","depreciation_amount","accumulated_dep","net_book_value"]},"columns":[{"field":"asset_code","label":"Asset","sticky":"left","sortable":true},{"field":"posted_for_period","label":"Period","sortable":true},{"field":"method","label":"Method","render":"badge","colorMap":{"STRAIGHT_LINE":"blue","WDV":"amber","UNITS":"purple"}},{"field":"depreciation_amount","label":"Period Dep","render":"number","align":"right"},{"field":"accumulated_dep","label":"Accumulated","render":"number","align":"right"},{"field":"net_book_value","label":"NBV","render":"number","align":"right"}],"filters":[{"field":"method","label":"Method","type":"multi-select","optionsSource":{"type":"static","options":["STRAIGHT_LINE","WDV","UNITS"]}}],"sort":{"default":[{"field":"posted_for_period","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Asset Depreciation"}}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dep-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Post Monthly Depreciation","fields":[{"kind":"FormField","type":"text","name":"period","label":"Period (YYYY-MM)"},{"kind":"FormField","type":"select","name":"method","label":"Method","options":[{"label":"STRAIGHT_LINE","value":"STRAIGHT_LINE"},{"label":"WDV","value":"WDV"},{"label":"UNITS","value":"UNITS"}]},{"kind":"WorkflowActionButton","label":"Run Depreciation","action":"external.api.call","args":{"connector":"INTERNAL","operation":"depreciation.run"},"variant":"primary"}]}]}', 'V15', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dep-run' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code IN ('pico:sub-list','pico:sub-create','pico:sub-pause','pico:sub-resume','pico:sub-cancel','pico:amc-list','pico:amc-create','pico:amc-activate','pico:amc-pm-trigger','pico:amc-pm-done','pico:amc-terminate','pico:dep-list','pico:dep-run') AND current_version_id IS NULL;
