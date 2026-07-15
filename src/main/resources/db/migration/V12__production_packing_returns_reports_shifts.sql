-- =====================================================================
-- V12 — One mega-migration that lands six interlocking use cases on the
--       same M0 substrate. Each follows the same recipe (table + workflow
--       + pages) and writes through StockMovementListener for stock impact.
--
--   1. Production / Job Card           — consume RM, declare output FG
--   2. Packing                         — consume bulk + produce packed FG
--   3. FG Dispatch with bin chooser    — pick bay/bin + qty, conveyor mode
--   4. Returns                         — receive, inspect, restock/scrap/rework
--   5. Reports                         — day-end + quality + govt views
--   6. Shift handling + auto-rotation  — schedule + handover + rotation rules
-- =====================================================================

-- ===== 1. PRODUCTION / JOB CARD ======================================

CREATE TABLE IF NOT EXISTS m5_production_order (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  po_number VARCHAR(32) NOT NULL,
  product_code VARCHAR(64) NOT NULL,
  product_name VARCHAR(256) NULL,
  planned_qty DECIMAL(14,3) NOT NULL,
  uom VARCHAR(16) NOT NULL,
  production_line VARCHAR(32) NULL,
  required_by_date DATE NULL,
  linked_so_id BIGINT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_m5prod_number (tenant_id, po_number)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_job_card (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  jc_number VARCHAR(32) NOT NULL,
  production_order_id BIGINT NULL,
  operator_id BIGINT NULL,
  shift_code VARCHAR(16) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  output_qty DECIMAL(14,3) NULL,
  output_lot_no VARCHAR(64) NULL,
  output_bin VARCHAR(64) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  closed_at TIMESTAMP NULL,
  UNIQUE KEY uq_jc_number (tenant_id, jc_number),
  KEY idx_jc_status (tenant_id, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_production_order (tenant_id, po_number, product_code, product_name, planned_qty, uom, production_line, required_by_date, status) VALUES
  (1,'PRO-2026-001','OIL-SUN-RFD','Refined Sunflower Oil',  25000.000,'KG','LINE-1','2026-06-15','IN_PROGRESS'),
  (1,'PRO-2026-002','PET-5L-SUN', '5L Sunflower PET',        2500.000,'PCS','PACK-1','2026-06-18','PLANNED');
INSERT IGNORE INTO m5_job_card (tenant_id, jc_number, production_order_id, shift_code, status, output_qty, output_lot_no, output_bin) VALUES
  (1,'JC-2026-001',(SELECT id FROM m5_production_order WHERE po_number='PRO-2026-001'),'A','CONSUMED', 10000.000,'LOT-SUN-RFD-A1','T-OIL-08'),
  (1,'JC-2026-002',(SELECT id FROM m5_production_order WHERE po_number='PRO-2026-001'),'B','OPEN', NULL,NULL,NULL);

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.JobCard',1,'PUBLISHED','{"code":"M5.JobCard","version":1,"initialState":"OPEN","states":[{"code":"OPEN","assignedRole":"UnloadingSupervisor"},{"code":"CONSUMED","assignedRole":"UnloadingSupervisor"},{"code":"OUTPUT_DECLARED","assignedRole":"QC"},{"code":"QC_PASSED","assignedRole":"Dispatch"},{"code":"CLOSED","terminal":true},{"code":"QC_REJECTED","terminal":true}],"transitions":[{"from":"OPEN","to":"CONSUMED","trigger":"page.submitted","pageCode":"pico:jc-consume"},{"from":"CONSUMED","to":"OUTPUT_DECLARED","trigger":"page.submitted","pageCode":"pico:jc-output"},{"from":"OUTPUT_DECLARED","to":"QC_PASSED","trigger":"page.submitted","pageCode":"pico:jc-qc","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"OUTPUT_DECLARED","to":"QC_REJECTED","trigger":"page.submitted","pageCode":"pico:jc-qc","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"QC_PASSED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:jc-close"}]}');

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'ProductionOrder','Production Order','m5_production_order','MES production order header.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='ProductionOrder');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'JobCard','Job Card','m5_job_card','MES per-shift job card.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='JobCard');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ProductionOrder','po_number','PRO #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ProductionOrder' AND field_code='po_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ProductionOrder','product_code','Product','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ProductionOrder' AND field_code='product_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ProductionOrder','planned_qty','Planned Qty','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ProductionOrder' AND field_code='planned_qty');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ProductionOrder','production_line','Line','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ProductionOrder' AND field_code='production_line');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ProductionOrder','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ProductionOrder' AND field_code='status');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'JobCard','jc_number','JC #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='JobCard' AND field_code='jc_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'JobCard','production_order_id','Production Order','LONG',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='JobCard' AND field_code='production_order_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'JobCard','shift_code','Shift','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='JobCard' AND field_code='shift_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'JobCard','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='JobCard' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'JobCard','output_qty','Output Qty','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='JobCard' AND field_code='output_qty');

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:production-list','Production Orders','MES production orders.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:production-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:jc-list','Job Cards','Per-shift job cards.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:jc-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:jc-consume','JC — Consume Material','Log material consumed.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:jc-consume');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:jc-output','JC — Declare Output','Declare output produced.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:jc-output');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:jc-qc','JC — QC Pass/Reject','In-line QC on output.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:jc-qc');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:jc-close','JC — Close','Final close-out.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:jc-close');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ProductionOrder","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","po_number","product_code","product_name","planned_qty","uom","production_line","required_by_date","status"]},"columns":[{"field":"po_number","label":"PRO #","sticky":"left","sortable":true},{"field":"product_code","label":"Product"},{"field":"product_name","label":"Name"},{"field":"planned_qty","label":"Planned","render":"number","align":"right"},{"field":"production_line","label":"Line","render":"badge"},{"field":"required_by_date","label":"By","render":"date"},{"field":"status","label":"Status","render":"badge","colorMap":{"PLANNED":"grey","IN_PROGRESS":"amber","COMPLETED":"green","CANCELLED":"red"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["PLANNED","IN_PROGRESS","COMPLETED","CANCELLED"]}}],"sort":{"default":[{"field":"required_by_date","dir":"asc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Production Orders"}}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:production-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"JobCard","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","jc_number","production_order_id","shift_code","status","output_qty","output_lot_no","output_bin"]},"columns":[{"field":"jc_number","label":"JC #","sticky":"left","sortable":true},{"field":"production_order_id","label":"PRO"},{"field":"shift_code","label":"Shift","render":"badge","colorMap":{"A":"blue","B":"amber","C":"purple"}},{"field":"status","label":"Status","render":"badge","colorMap":{"OPEN":"grey","CONSUMED":"blue","OUTPUT_DECLARED":"amber","QC_PASSED":"green","CLOSED":"green","QC_REJECTED":"red"}},{"field":"output_qty","label":"Output","render":"number","align":"right"},{"field":"output_lot_no","label":"Output Lot"},{"field":"output_bin","label":"Output Bin"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["OPEN","CONSUMED","OUTPUT_DECLARED","QC_PASSED","CLOSED","QC_REJECTED"]}},{"field":"shift_code","label":"Shift","type":"multi-select","optionsSource":{"type":"static","options":["A","B","C"]}}],"sort":{"default":[{"field":"jc_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Job Cards"}}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:jc-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Consume Material","fields":[{"kind":"FormField","type":"text","name":"itemCode","label":"Item Consumed"},{"kind":"FormField","type":"text","name":"lotNo","label":"Lot # (FEFO suggested)"},{"kind":"FormField","type":"text","name":"fromBin","label":"From Bin/Tank"},{"kind":"FormField","type":"number","name":"qtyConsumed","label":"Qty Consumed"},{"kind":"FormField","type":"text","name":"uom","label":"UoM"},{"kind":"FormField","type":"text","name":"jcNumber","label":"JC #"},{"kind":"WorkflowActionButton","label":"Log Consumption","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:jc-consume' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Declare Output","fields":[{"kind":"FormField","type":"text","name":"itemCode","label":"Output Item"},{"kind":"FormField","type":"number","name":"qtyReceived","label":"Output Qty Produced"},{"kind":"FormField","type":"text","name":"finalLotNo","label":"Output Lot #"},{"kind":"FormField","type":"text","name":"storeTankCode","label":"To Bin/Tank"},{"kind":"FormField","type":"text","name":"uom","label":"UoM"},{"kind":"WorkflowActionButton","label":"Declare Output","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:jc-output' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"In-line QC","fields":[{"kind":"FormField","type":"text","name":"sampleAnalysis","label":"Sample Analysis"},{"kind":"FormField","type":"select","name":"status","label":"Decision","options":[{"label":"APPROVED","value":"APPROVED"},{"label":"REJECTED","value":"REJECTED"}]},{"kind":"WorkflowActionButton","label":"Submit QC","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:jc-qc' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close Job Card","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Closing Notes"},{"kind":"WorkflowActionButton","label":"Close JC","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:jc-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

-- ===== 2. PACKING + SPOILAGE =========================================

CREATE TABLE IF NOT EXISTS m5_packing_run (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  pr_number VARCHAR(32) NOT NULL,
  from_bulk_lot VARCHAR(64) NULL,
  from_bin VARCHAR(64) NULL,
  packaging_sku VARCHAR(64) NULL,
  packed_lot_no VARCHAR(64) NULL,
  to_bin VARCHAR(64) NULL,
  planned_packs DECIMAL(14,3) NULL,
  actual_packs DECIMAL(14,3) NULL,
  spoil_packs DECIMAL(14,3) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_pr_number (tenant_id, pr_number)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_packing_run (tenant_id, pr_number, from_bulk_lot, from_bin, packaging_sku, packed_lot_no, to_bin, planned_packs, actual_packs, spoil_packs, status) VALUES
  (1,'PR-2026-001','LOT-SUN-RFD-A1','T-OIL-08','PET-5L-SUN','LOT-PK-260608-A','FG-BIN-A',2500.000,2480.000,20.000,'CLOSED'),
  (1,'PR-2026-002','LOT-GND-RFD-A1','T-OIL-17','PET-1L-GND','LOT-PK-260609-B','FG-BIN-B',5000.000,NULL,NULL,'OPEN');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.PackingRun',1,'PUBLISHED','{"code":"M5.PackingRun","version":1,"initialState":"OPEN","states":[{"code":"OPEN","assignedRole":"UnloadingSupervisor"},{"code":"RUNNING","assignedRole":"UnloadingSupervisor"},{"code":"PACKED","assignedRole":"UnloadingSupervisor"},{"code":"SPOIL","assignedRole":"UnloadingSupervisor"},{"code":"CLOSED","terminal":true}],"transitions":[{"from":"OPEN","to":"RUNNING","trigger":"page.submitted","pageCode":"pico:pack-start"},{"from":"RUNNING","to":"PACKED","trigger":"page.submitted","pageCode":"pico:pack-finish"},{"from":"PACKED","to":"SPOIL","trigger":"page.submitted","pageCode":"pico:pack-spoil","guard":"#submitted[''hasSpoil''] == ''YES''"},{"from":"PACKED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:pack-close"},{"from":"SPOIL","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:pack-close"}]}');

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'PackingRun','Packing Run','m5_packing_run','Bulk-to-pack conversion run.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='PackingRun');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PackingRun','pr_number','PR #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PackingRun' AND field_code='pr_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PackingRun','packaging_sku','SKU','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PackingRun' AND field_code='packaging_sku');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PackingRun','from_bin','From Bin','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PackingRun' AND field_code='from_bin');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PackingRun','to_bin','To Bin','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PackingRun' AND field_code='to_bin');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PackingRun','planned_packs','Planned','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PackingRun' AND field_code='planned_packs');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PackingRun','actual_packs','Actual','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PackingRun' AND field_code='actual_packs');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PackingRun','spoil_packs','Spoil','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PackingRun' AND field_code='spoil_packs');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PackingRun','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PackingRun' AND field_code='status');

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pack-list','Packing Runs','All packing runs by status.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pack-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pack-start','Packing — Start','Start a packing run.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pack-start');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pack-finish','Packing — Finish','Mark packing run finished.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pack-finish');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pack-spoil','Packing — Spoil','Split spoiled packs to new lot OR write-off.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pack-spoil');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pack-close','Packing — Close','Final close-out.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pack-close');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"PackingRun","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","pr_number","packaging_sku","from_bin","to_bin","planned_packs","actual_packs","spoil_packs","status"]},"columns":[{"field":"pr_number","label":"PR #","sticky":"left","sortable":true},{"field":"packaging_sku","label":"SKU"},{"field":"from_bin","label":"From"},{"field":"to_bin","label":"To"},{"field":"planned_packs","label":"Planned","render":"number","align":"right"},{"field":"actual_packs","label":"Actual","render":"number","align":"right"},{"field":"spoil_packs","label":"Spoil","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge","colorMap":{"OPEN":"grey","RUNNING":"amber","PACKED":"blue","SPOIL":"red","CLOSED":"green"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["OPEN","RUNNING","PACKED","SPOIL","CLOSED"]}}],"sort":{"default":[{"field":"pr_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Packing Runs"}}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pack-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Start Packing Run","fields":[{"kind":"FormField","type":"text","name":"fromBulkLot","label":"From Bulk Lot"},{"kind":"FormField","type":"text","name":"fromBin","label":"From Bin/Tank"},{"kind":"FormField","type":"text","name":"packagingSku","label":"Packaging SKU"},{"kind":"FormField","type":"number","name":"plannedPacks","label":"Planned Packs"},{"kind":"FormField","type":"text","name":"toBin","label":"To FG Bin"},{"kind":"WorkflowActionButton","label":"Start Run","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pack-start' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Finish Packing","fields":[{"kind":"FormField","type":"number","name":"actualPacks","label":"Actual Packs"},{"kind":"FormField","type":"text","name":"packedLotNo","label":"Packed Lot #"},{"kind":"FormField","type":"select","name":"hasSpoil","label":"Any spoilage?","options":[{"label":"NO","value":"NO"},{"label":"YES","value":"YES"}]},{"kind":"WorkflowActionButton","label":"Finish Run","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pack-finish' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Handle Spoilage","fields":[{"kind":"FormField","type":"number","name":"spoilPacks","label":"Spoil Packs"},{"kind":"FormField","type":"select","name":"action","label":"Action","options":[{"label":"Split to NEW lot (rework)","value":"SPLIT"},{"label":"Write-off (-stock)","value":"WRITE_OFF"}]},{"kind":"FormField","type":"text","name":"newLotNo","label":"New Lot # (if SPLIT)"},{"kind":"FormField","type":"text","name":"reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Post Spoilage","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pack-spoil' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close Packing","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Closing Notes"},{"kind":"WorkflowActionButton","label":"Close Run","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pack-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

-- ===== 3. FG DISPATCH WITH BIN CHOOSER ===============================

CREATE TABLE IF NOT EXISTS m5_fg_dispatch (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  dispatch_number VARCHAR(32) NOT NULL,
  linked_so_id BIGINT NULL,
  customer_name VARCHAR(256) NULL,
  source_bin VARCHAR(64) NULL,
  loading_bay VARCHAR(32) NULL,
  packed_lot_no VARCHAR(64) NULL,
  qty_to_dispatch DECIMAL(14,3) NULL,
  qty_dispatched DECIMAL(14,3) NULL,
  uom VARCHAR(16) NULL,
  feed_mode VARCHAR(16) DEFAULT 'MANUAL',
  conveyor_id VARCHAR(32) NULL,
  vehicle_no VARCHAR(32) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  gate_pass_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_dispatch_number (tenant_id, dispatch_number)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_fg_dispatch (tenant_id, dispatch_number, customer_name, source_bin, loading_bay, packed_lot_no, qty_to_dispatch, uom, feed_mode, vehicle_no, status) VALUES
  (1,'DSP-2026-001','Coimbatore Foods Pvt Ltd','FG-BIN-A','BAY-1','LOT-PK-260608-A',2480.000,'PCS','CONVEYOR','TN-46-CJ-1234','DISPATCHED'),
  (1,'DSP-2026-002','Madurai Wholesalers',     'FG-BIN-B','BAY-2','LOT-PK-260609-B',5000.000,'PCS','MANUAL',  'TN-46-CK-5678','PLANNED');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.FGDispatch',1,'PUBLISHED','{"code":"M5.FGDispatch","version":1,"initialState":"PLANNED","states":[{"code":"PLANNED","assignedRole":"Dispatch"},{"code":"BIN_CHOSEN","assignedRole":"Dispatch"},{"code":"BAY_ASSIGNED","assignedRole":"UnloadingSupervisor"},{"code":"LOADING","assignedRole":"UnloadingSupervisor"},{"code":"DISPATCHED","assignedRole":"FrontGate"},{"code":"CLOSED","terminal":true}],"transitions":[{"from":"PLANNED","to":"BIN_CHOSEN","trigger":"page.submitted","pageCode":"pico:dsp-pick-bin"},{"from":"BIN_CHOSEN","to":"BAY_ASSIGNED","trigger":"page.submitted","pageCode":"pico:dsp-pick-bay"},{"from":"BAY_ASSIGNED","to":"LOADING","trigger":"page.submitted","pageCode":"pico:dsp-start-loading"},{"from":"LOADING","to":"DISPATCHED","trigger":"page.submitted","pageCode":"pico:dsp-confirm-load"},{"from":"DISPATCHED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:dsp-close"}]}');

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'FGDispatch','FG Dispatch','m5_fg_dispatch','Finished-goods dispatch with bin+bay chooser.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='FGDispatch');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'FGDispatch','dispatch_number','Dispatch #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='FGDispatch' AND field_code='dispatch_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'FGDispatch','customer_name','Customer','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='FGDispatch' AND field_code='customer_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'FGDispatch','source_bin','From Bin','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='FGDispatch' AND field_code='source_bin');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'FGDispatch','loading_bay','Bay','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='FGDispatch' AND field_code='loading_bay');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'FGDispatch','feed_mode','Feed Mode','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='FGDispatch' AND field_code='feed_mode');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'FGDispatch','qty_to_dispatch','Qty','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='FGDispatch' AND field_code='qty_to_dispatch');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'FGDispatch','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='FGDispatch' AND field_code='status');

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:dsp-list','FG Dispatches','All FG dispatches.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dsp-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:dsp-pick-bin','Dispatch — Pick Bin','Choose source FG bin/lot.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dsp-pick-bin');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:dsp-pick-bay','Dispatch — Pick Bay','Choose loading bay + feed mode.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dsp-pick-bay');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:dsp-start-loading','Dispatch — Start Loading','Begin manual / conveyor feed.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dsp-start-loading');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:dsp-confirm-load','Dispatch — Confirm','Confirm loaded qty + vehicle.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dsp-confirm-load');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:dsp-close','Dispatch — Close','Final close + link to GP.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dsp-close');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"FGDispatch","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","dispatch_number","customer_name","source_bin","loading_bay","feed_mode","qty_to_dispatch","status"]},"columns":[{"field":"dispatch_number","label":"Dispatch #","sticky":"left","sortable":true},{"field":"customer_name","label":"Customer"},{"field":"source_bin","label":"From Bin","render":"badge"},{"field":"loading_bay","label":"Bay","render":"badge"},{"field":"feed_mode","label":"Feed","render":"badge","colorMap":{"MANUAL":"grey","CONVEYOR":"green","DRUM":"amber"}},{"field":"qty_to_dispatch","label":"Qty","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge","colorMap":{"PLANNED":"grey","BIN_CHOSEN":"blue","BAY_ASSIGNED":"blue","LOADING":"amber","DISPATCHED":"green","CLOSED":"green"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["PLANNED","BIN_CHOSEN","BAY_ASSIGNED","LOADING","DISPATCHED","CLOSED"]}},{"field":"feed_mode","label":"Feed","type":"multi-select","optionsSource":{"type":"static","options":["MANUAL","CONVEYOR","DRUM"]}}],"sort":{"default":[{"field":"dispatch_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"FG Dispatches"}}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dsp-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Choose Source Bin","fields":[{"kind":"FormField","type":"text","name":"sourceBin","label":"Source FG Bin (e.g. FG-BIN-A)"},{"kind":"FormField","type":"text","name":"packedLotNo","label":"Packed Lot # (FEFO)"},{"kind":"FormField","type":"text","name":"itemCode","label":"Item Code"},{"kind":"FormField","type":"number","name":"qtyAvailable","label":"Qty Available (read-only)","readOnly":true},{"kind":"WorkflowActionButton","label":"Confirm Bin","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dsp-pick-bin' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Choose Loading Bay","fields":[{"kind":"FormField","type":"select","name":"loadingBay","label":"Loading Bay","options":[{"label":"BAY-1","value":"BAY-1"},{"label":"BAY-2","value":"BAY-2"},{"label":"BAY-3","value":"BAY-3"},{"label":"BAY-4","value":"BAY-4"}]},{"kind":"FormField","type":"select","name":"feedMode","label":"Feed Mode","options":[{"label":"MANUAL pallet","value":"MANUAL"},{"label":"CONVEYOR (auto)","value":"CONVEYOR"},{"label":"DRUM hand-load","value":"DRUM"}]},{"kind":"FormField","type":"text","name":"conveyorId","label":"Conveyor ID (if CONVEYOR)"},{"kind":"WorkflowActionButton","label":"Assign Bay","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dsp-pick-bay' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Start Loading","fields":[{"kind":"FormField","type":"text","name":"startedAt","label":"Started At"},{"kind":"FormField","type":"text","name":"operatorId","label":"Operator ID"},{"kind":"WorkflowActionButton","label":"Start Loading","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dsp-start-loading' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Confirm Load","fields":[{"kind":"FormField","type":"number","name":"qtyDispatched","label":"Qty Dispatched (actual)"},{"kind":"FormField","type":"text","name":"vehicleNo","label":"Vehicle #"},{"kind":"FormField","type":"text","name":"linkedGpNumber","label":"Linked Gate Pass # (M10)"},{"kind":"WorkflowActionButton","label":"Confirm Dispatched","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dsp-confirm-load' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close Dispatch","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Closing Notes"},{"kind":"WorkflowActionButton","label":"Close","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dsp-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

-- ===== 4. RETURNS ====================================================

CREATE TABLE IF NOT EXISTS m5_return_note (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  rn_number VARCHAR(32) NOT NULL,
  return_kind VARCHAR(16) NOT NULL,
  source_ref VARCHAR(128) NULL,
  customer_or_dept VARCHAR(256) NULL,
  reason VARCHAR(256) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'REQUESTED',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  qty_returned DECIMAL(14,3) NULL,
  destination VARCHAR(16) NULL,
  item_code VARCHAR(64) NULL,
  lot_no VARCHAR(64) NULL,
  to_bin VARCHAR(64) NULL,
  uom VARCHAR(16) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_rn_number (tenant_id, rn_number)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_return_note (tenant_id, rn_number, return_kind, customer_or_dept, reason, status, qty_returned, destination, item_code, lot_no, to_bin, uom) VALUES
  (1,'RN-2026-001','CUSTOMER','Coimbatore Foods Pvt Ltd','Damaged outer carton','CLOSED', 120.000,'RESTOCK','PET-5L-SUN','LOT-PK-260605','FG-BIN-A','PCS'),
  (1,'RN-2026-002','INTERNAL','Production line 1','Wrong UOM picked','REQUESTED',500.000,NULL,'OIL-SUN-RFD','LOT-SUN-RFD-A1','T-OIL-08','KG');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.ReturnNote',1,'PUBLISHED','{"code":"M5.ReturnNote","version":1,"initialState":"REQUESTED","states":[{"code":"REQUESTED","assignedRole":"Dispatch"},{"code":"RECEIVED","assignedRole":"QC"},{"code":"INSPECTED","assignedRole":"UnloadingSupervisor"},{"code":"DESTINATION_DECIDED","assignedRole":"UnloadingSupervisor"},{"code":"CLOSED","terminal":true}],"transitions":[{"from":"REQUESTED","to":"RECEIVED","trigger":"page.submitted","pageCode":"pico:rn-receive"},{"from":"RECEIVED","to":"INSPECTED","trigger":"page.submitted","pageCode":"pico:rn-inspect"},{"from":"INSPECTED","to":"DESTINATION_DECIDED","trigger":"page.submitted","pageCode":"pico:rn-destination"},{"from":"DESTINATION_DECIDED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:rn-close"}]}');

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'ReturnNote','Return Note','m5_return_note','Customer + internal returns.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='ReturnNote');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ReturnNote','rn_number','RN #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ReturnNote' AND field_code='rn_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ReturnNote','return_kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ReturnNote' AND field_code='return_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ReturnNote','customer_or_dept','From','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ReturnNote' AND field_code='customer_or_dept');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ReturnNote','reason','Reason','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ReturnNote' AND field_code='reason');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ReturnNote','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ReturnNote' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ReturnNote','destination','Dest','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ReturnNote' AND field_code='destination');

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rn-list','Returns','Customer + internal returns.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rn-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rn-create','New Return','Log a return request.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rn-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rn-receive','RN — Receive','Receive returned items physically.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rn-receive');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rn-inspect','RN — Inspect','QC inspects the return.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rn-inspect');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rn-destination','RN — Destination','RESTOCK / SCRAP / REWORK.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rn-destination');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rn-close','RN — Close','Final close.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rn-close');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ReturnNote","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","rn_number","return_kind","customer_or_dept","reason","status","destination"]},"columns":[{"field":"rn_number","label":"RN #","sticky":"left","sortable":true},{"field":"return_kind","label":"Kind","render":"badge","colorMap":{"CUSTOMER":"blue","INTERNAL":"amber","VENDOR":"purple"}},{"field":"customer_or_dept","label":"From"},{"field":"reason","label":"Reason"},{"field":"status","label":"Status","render":"badge","colorMap":{"REQUESTED":"grey","RECEIVED":"blue","INSPECTED":"amber","DESTINATION_DECIDED":"amber","CLOSED":"green"}},{"field":"destination","label":"Dest","render":"badge","colorMap":{"RESTOCK":"green","SCRAP":"red","REWORK":"amber"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["REQUESTED","RECEIVED","INSPECTED","DESTINATION_DECIDED","CLOSED"]}},{"field":"return_kind","label":"Kind","type":"multi-select","optionsSource":{"type":"static","options":["CUSTOMER","INTERNAL","VENDOR"]}}],"sort":{"default":[{"field":"rn_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Returns"}}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rn-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New Return Request","fields":[{"kind":"FormField","type":"select","name":"returnKind","label":"Kind","options":[{"label":"CUSTOMER","value":"CUSTOMER"},{"label":"INTERNAL","value":"INTERNAL"},{"label":"VENDOR","value":"VENDOR"}]},{"kind":"FormField","type":"text","name":"customerOrDept","label":"From"},{"kind":"FormField","type":"text","name":"sourceRef","label":"Original Document Ref"},{"kind":"FormField","type":"text","name":"itemCode","label":"Item"},{"kind":"FormField","type":"number","name":"qtyReturned","label":"Qty Returned"},{"kind":"FormField","type":"text","name":"reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Log Return","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rn-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Receive Physical","fields":[{"kind":"FormField","type":"text","name":"receivedAt","label":"Received At"},{"kind":"FormField","type":"number","name":"countedQty","label":"Counted Qty"},{"kind":"WorkflowActionButton","label":"Mark Received","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rn-receive' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"QC Inspection","fields":[{"kind":"FormField","type":"text","name":"inspectionNotes","label":"Notes"},{"kind":"FormField","type":"text","name":"sampleResult","label":"Sample Result"},{"kind":"WorkflowActionButton","label":"Mark Inspected","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rn-inspect' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Decide Destination","fields":[{"kind":"FormField","type":"select","name":"destination","label":"Destination","options":[{"label":"RESTOCK (back into stock)","value":"RESTOCK"},{"label":"SCRAP (write-off)","value":"SCRAP"},{"label":"REWORK (split to new lot)","value":"REWORK"}]},{"kind":"FormField","type":"text","name":"toBin","label":"To Bin (RESTOCK only)"},{"kind":"FormField","type":"text","name":"reworkLotNo","label":"New Lot # (REWORK only)"},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rn-destination' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close Return","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Closing Notes"},{"kind":"WorkflowActionButton","label":"Close RN","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rn-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

-- ===== 5. REPORTS (Day-end / Quality / Government) ===================

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rpt-day-end','Day-End Report','Receipts + issues + dispatches + variance for the day.','Report','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rpt-day-end');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rpt-quality','Quality Report','Algorithmic quality summary using weighment + QC data.','Report','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rpt-quality');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rpt-gstr1','GSTR-1 Export','Sales for the period in GSTR-1 schema.','Report','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rpt-gstr1');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:rpt-ewb','E-Waybill Summary','E-waybills issued for outbound moves.','Report','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:rpt-ewb');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"schemaVersion":2,"layout":{"type":"Grid","props":{"gap":16,"style":{"padding":16,"gridTemplateColumns":"repeat(auto-fit, minmax(180px, 1fr))"}},"children":[{"type":"KPI","props":{"label":"Receipts Today (kg)","intervalMs":60000,"dataSource":{"type":"entity","entity":"StockMovement","filter":{"and":[{"field":"tenant_id","op":"eq","value":1},{"field":"kind","op":"eq","value":"RECEIPT"}]}}}},{"type":"KPI","props":{"label":"Issues Today","intervalMs":60000,"dataSource":{"type":"entity","entity":"StockMovement","filter":{"and":[{"field":"tenant_id","op":"eq","value":1},{"field":"kind","op":"eq","value":"ISSUE"}]}}}},{"type":"KPI","props":{"label":"Consumption","intervalMs":60000,"dataSource":{"type":"entity","entity":"StockMovement","filter":{"and":[{"field":"tenant_id","op":"eq","value":1},{"field":"kind","op":"eq","value":"CONSUME"}]}}}},{"type":"KPI","props":{"label":"Spoilage","intervalMs":60000,"dataSource":{"type":"entity","entity":"StockMovement","filter":{"and":[{"field":"tenant_id","op":"eq","value":1},{"field":"kind","op":"eq","value":"SPOIL"}]}}}},{"type":"KPI","props":{"label":"Gate Passes Closed","intervalMs":60000,"dataSource":{"type":"entity","entity":"GatePass","filter":{"and":[{"field":"tenant_id","op":"eq","value":1},{"field":"status","op":"eq","value":"CLOSED"}]}}}},{"type":"KPI","props":{"label":"Returns Logged","intervalMs":60000,"dataSource":{"type":"entity","entity":"ReturnNote","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]}}}}]}}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rpt-day-end' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Quality Algorithm Summary","fields":[{"kind":"FormField","type":"text","name":"algorithm","label":"Algorithm","value":"moving-avg-tolerance"},{"kind":"FormField","type":"text","name":"sampleSize","label":"Sample Size (events)","value":"100"},{"kind":"FormField","type":"text","name":"toleranceKg","label":"Tolerance ±kg","value":"50"},{"kind":"WorkflowActionButton","label":"Run Algorithm","action":"expr.compute","args":{"expr":"qualityScore(weighmentSeries, sampleSize, toleranceKg)"},"variant":"primary"},{"kind":"FormField","type":"text","name":"resultScore","label":"Score 0-100 (auto)","readOnly":true},{"kind":"FormField","type":"text","name":"flaggedBatches","label":"Flagged Batches (auto)","readOnly":true}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rpt-quality' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"GSTR-1 Export","fields":[{"kind":"FormField","type":"text","name":"period","label":"Period (YYYYMM)","value":"202606"},{"kind":"WorkflowActionButton","label":"Export GSTR-1 JSON","action":"export.csv","args":{"entity":"SalesOrder","format":"gstr-1-json","period":"period"},"variant":"primary"},{"kind":"WorkflowActionButton","label":"Send to NIC","action":"external.api.call","args":{"connector":"NIC_IRP_V1.1","operation":"generateIrn"}}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rpt-gstr1' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"GatePass","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","gp_number","vehicle_no","status","total_unloading_qty","updated_at"]},"columns":[{"field":"gp_number","label":"GP #","sticky":"left"},{"field":"vehicle_no","label":"Vehicle"},{"field":"total_unloading_qty","label":"Qty","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge"},{"field":"updated_at","label":"Updated","render":"datetime"}],"sort":{"default":[{"field":"updated_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"E-Waybill Summary (outbound GPs)"}}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:rpt-ewb' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

-- ===== 6. SHIFT HANDLING + AUTO-ROTATION =============================

CREATE TABLE IF NOT EXISTS m5_shift_schedule (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  shift_date DATE NOT NULL,
  shift_code VARCHAR(8) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  supervisor_id BIGINT NULL,
  notes VARCHAR(256) NULL,
  UNIQUE KEY uq_shift (tenant_id, shift_date, shift_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_shift_handover (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  handed_off_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  outgoing_shift VARCHAR(8) NOT NULL,
  incoming_shift VARCHAR(8) NOT NULL,
  outgoing_supervisor_id BIGINT NULL,
  incoming_supervisor_id BIGINT NULL,
  open_jc_count INT DEFAULT 0,
  open_dispatch_count INT DEFAULT 0,
  notes TEXT NULL,
  KEY idx_handover_ts (handed_off_at)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_shift_schedule (tenant_id, shift_date, shift_code, start_time, end_time, supervisor_id) VALUES
  (1,'2026-06-08','A','06:00:00','14:00:00',2003),
  (1,'2026-06-08','B','14:00:00','22:00:00',2004),
  (1,'2026-06-08','C','22:00:00','06:00:00',2003),
  (1,'2026-06-09','A','06:00:00','14:00:00',2004),
  (1,'2026-06-09','B','14:00:00','22:00:00',2003);

INSERT IGNORE INTO m5_shift_handover (tenant_id, handed_off_at, outgoing_shift, incoming_shift, outgoing_supervisor_id, incoming_supervisor_id, open_jc_count, open_dispatch_count, notes) VALUES
  (1,'2026-06-08 14:00:00','A','B',2003,2004,2,1,'JC-2026-002 pending consumption; DSP-2026-002 pending');

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'ShiftSchedule','Shift Schedule','m5_shift_schedule','Shift roster.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='ShiftSchedule');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'ShiftHandover','Shift Handover','m5_shift_handover','Handover log between shifts.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='ShiftHandover');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftSchedule','shift_date','Date','DATE',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftSchedule' AND field_code='shift_date');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftSchedule','shift_code','Shift','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftSchedule' AND field_code='shift_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftSchedule','start_time','Start','STRING',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftSchedule' AND field_code='start_time');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftSchedule','end_time','End','STRING',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftSchedule' AND field_code='end_time');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftSchedule','supervisor_id','Supervisor','LONG',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftSchedule' AND field_code='supervisor_id');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftHandover','handed_off_at','When','DATETIME',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftHandover' AND field_code='handed_off_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftHandover','outgoing_shift','Out','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftHandover' AND field_code='outgoing_shift');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftHandover','incoming_shift','In','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftHandover' AND field_code='incoming_shift');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftHandover','open_jc_count','Open JCs','LONG',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftHandover' AND field_code='open_jc_count');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ShiftHandover','open_dispatch_count','Open DSPs','LONG',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftHandover' AND field_code='open_dispatch_count');

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:shift-schedule','Shift Schedule','Per-day shift roster.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:shift-schedule');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:shift-handover','Shift Handover','Hand over to next shift.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:shift-handover');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:shift-rotation','Shift Rotation Rules','Auto-rotation config.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:shift-rotation');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ShiftSchedule","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","shift_date","shift_code","start_time","end_time","supervisor_id"]},"columns":[{"field":"shift_date","label":"Date","render":"date","sticky":"left","sortable":true},{"field":"shift_code","label":"Shift","render":"badge","colorMap":{"A":"blue","B":"amber","C":"purple"}},{"field":"start_time","label":"Start"},{"field":"end_time","label":"End"},{"field":"supervisor_id","label":"Supervisor"}],"filters":[{"field":"shift_code","label":"Shift","type":"multi-select","optionsSource":{"type":"static","options":["A","B","C"]}}],"sort":{"default":[{"field":"shift_date","dir":"desc"},{"field":"shift_code","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Shift Schedule"}}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:shift-schedule' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Hand Over to Next Shift","fields":[{"kind":"FormField","type":"select","name":"outgoingShift","label":"Outgoing","options":[{"label":"A","value":"A"},{"label":"B","value":"B"},{"label":"C","value":"C"}]},{"kind":"FormField","type":"select","name":"incomingShift","label":"Incoming","options":[{"label":"A","value":"A"},{"label":"B","value":"B"},{"label":"C","value":"C"}]},{"kind":"FormField","type":"text","name":"openJcCount","label":"Open JCs Count","value":"auto"},{"kind":"FormField","type":"text","name":"openDispatchCount","label":"Open Dispatches Count","value":"auto"},{"kind":"FormField","type":"text","name":"notes","label":"Handover Notes"},{"kind":"WorkflowActionButton","label":"Confirm Handover","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:shift-handover' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Auto-Rotation Rules","fields":[{"kind":"FormField","type":"select","name":"rotationPattern","label":"Pattern","options":[{"label":"A→B→C (daily)","value":"DAILY_FORWARD"},{"label":"A→B→C (weekly)","value":"WEEKLY_FORWARD"},{"label":"Manual only","value":"MANUAL"}]},{"kind":"FormField","type":"text","name":"effectiveFrom","label":"Effective From"},{"kind":"FormField","type":"text","name":"holidayCalendar","label":"Holiday Calendar (CSV)"},{"kind":"WorkflowActionButton","label":"Save Rules","action":"workflow.transition","variant":"primary"}]}]}', 'V12', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:shift-rotation' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

-- =====================================================================
-- 7. Final: publish all V12 pages -------------------------------------
-- =====================================================================
UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code IN ('pico:production-list','pico:jc-list','pico:jc-consume','pico:jc-output','pico:jc-qc','pico:jc-close','pico:pack-list','pico:pack-start','pico:pack-finish','pico:pack-spoil','pico:pack-close','pico:dsp-list','pico:dsp-pick-bin','pico:dsp-pick-bay','pico:dsp-start-loading','pico:dsp-confirm-load','pico:dsp-close','pico:rn-list','pico:rn-create','pico:rn-receive','pico:rn-inspect','pico:rn-destination','pico:rn-close','pico:rpt-day-end','pico:rpt-quality','pico:rpt-gstr1','pico:rpt-ewb','pico:shift-schedule','pico:shift-handover','pico:shift-rotation') AND current_version_id IS NULL;
