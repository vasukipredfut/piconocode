-- =====================================================================
-- V9_1 — V9 corrective.
-- Legacy V1 baseline already had `purchase_order` + `_line` + sales lines
-- with a different schema (no tenant_id). My CREATE TABLE IF NOT EXISTS in
-- V9 was a no-op against those legacy shells, then the seed INSERTs aborted
-- the entire script. Move all my new tables to a `m5_` prefix and re-seed.
-- =====================================================================

-- 1. Drop the empty-or-near-empty replicas I tried to create -------------
DROP TABLE IF EXISTS m5_purchase_order_line;
DROP TABLE IF EXISTS m5_sales_order_line;
DROP TABLE IF EXISTS m5_purchase_order;
DROP TABLE IF EXISTS m5_sales_order;

-- 2. Re-create them under the m5_ namespace -----------------------------
CREATE TABLE m5_purchase_order (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  po_number VARCHAR(32) NOT NULL,
  vendor_id BIGINT NULL,
  vendor_name VARCHAR(256) NULL,
  total_value DECIMAL(14,2) NULL,
  currency VARCHAR(4) NOT NULL DEFAULT 'INR',
  required_by_date DATE NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_m5po_number (tenant_id, po_number),
  KEY idx_m5po_status (tenant_id, status, current_step)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE m5_purchase_order_line (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  po_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(256) NULL,
  qty_ordered DECIMAL(12,3) NOT NULL,
  qty_received DECIMAL(12,3) NULL,
  uom VARCHAR(16) NOT NULL,
  rate DECIMAL(12,4) NULL,
  line_value DECIMAL(14,2) NULL,
  tax_pct DECIMAL(5,2) NULL,
  CONSTRAINT fk_m5pol_po FOREIGN KEY (po_id) REFERENCES m5_purchase_order(id),
  KEY idx_m5pol_item (item_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE m5_sales_order (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  so_number VARCHAR(32) NOT NULL,
  customer_name VARCHAR(256) NOT NULL,
  customer_gstin VARCHAR(16) NULL,
  total_value DECIMAL(14,2) NULL,
  currency VARCHAR(4) NOT NULL DEFAULT 'INR',
  required_by_date DATE NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  gate_pass_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_m5so_number (tenant_id, so_number),
  KEY idx_m5so_status (tenant_id, status, current_step),
  KEY idx_m5so_gp (gate_pass_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE m5_sales_order_line (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  so_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(256) NULL,
  qty_ordered DECIMAL(12,3) NOT NULL,
  qty_dispatched DECIMAL(12,3) NULL,
  uom VARCHAR(16) NOT NULL,
  rate DECIMAL(12,4) NULL,
  line_value DECIMAL(14,2) NULL,
  tax_pct DECIMAL(5,2) NULL,
  CONSTRAINT fk_m5sol_so FOREIGN KEY (so_id) REFERENCES m5_sales_order(id),
  KEY idx_m5sol_item (item_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- 3. Seed sample rows ----------------------------------------------------
INSERT INTO m5_purchase_order (tenant_id, po_number, vendor_id, vendor_name, total_value, required_by_date, status, notes) VALUES
  (1,'PO-2026-001',  1,'TESTRO Industries', 1290000.00, '2026-06-20', 'SENT_TO_VENDOR', 'Sunflower oil — 60 MT bulk'),
  (1,'PO-2026-002',  4,'Iyer Lubricants',    650000.00, '2026-06-25', 'APPROVED',       'Groundnut oil — 30 MT'),
  (1,'PO-2026-003',  2,'TESTRA Logistics',    25000.00, '2026-06-18', 'DRAFT',          'Transport hire');

INSERT INTO m5_purchase_order_line (po_id, line_no, item_code, item_name, qty_ordered, uom, rate, line_value, tax_pct) VALUES
  ((SELECT id FROM m5_purchase_order WHERE po_number='PO-2026-001'),1,'OIL-SUN-RAW','Raw Sunflower Oil', 60000.000,'KG',21.50,1290000.00,5.00),
  ((SELECT id FROM m5_purchase_order WHERE po_number='PO-2026-002'),1,'OIL-GND-RAW','Raw Groundnut Oil', 30000.000,'KG',21.67,650100.00, 5.00),
  ((SELECT id FROM m5_purchase_order WHERE po_number='PO-2026-003'),1,'SVC-TRANSPORT','Transport service hire',  1.000,'LOT',25000.00,25000.00,18.00);

INSERT INTO m5_sales_order (tenant_id, so_number, customer_name, customer_gstin, total_value, required_by_date, status, notes) VALUES
  (1,'SO-2026-001','Coimbatore Foods Pvt Ltd', '33ABCDE9999F1Z9', 575000.00, '2026-06-15', 'CONFIRMED',        'Refined sunflower — bulk tanker'),
  (1,'SO-2026-002','Chennai Hotels Supply Co', '33ABCDE1234F1Z5', 287500.00, '2026-06-18', 'IN_PRODUCTION',    '5L PET bottles — sunflower'),
  (1,'SO-2026-003','Madurai Wholesalers',      '33ABCDE5678F1Z1', 115000.00, '2026-06-20', 'READY_TO_DISPATCH','Groundnut oil — 1L bottles');

INSERT INTO m5_sales_order_line (so_id, line_no, item_code, item_name, qty_ordered, uom, rate, line_value, tax_pct) VALUES
  ((SELECT id FROM m5_sales_order WHERE so_number='SO-2026-001'),1,'OIL-SUN-RFD','Refined Sunflower Oil',25000.000,'KG',23.00,575000.00,5.00),
  ((SELECT id FROM m5_sales_order WHERE so_number='SO-2026-002'),1,'PET-5L-SUN', '5L Sunflower PET',     2500.000,'PCS',115.00,287500.00,5.00),
  ((SELECT id FROM m5_sales_order WHERE so_number='SO-2026-003'),1,'PET-1L-GND', '1L Groundnut PET',     5000.000,'PCS', 23.00,115000.00,5.00);

-- 4. Workflows + entity registry (with m5_ table refs) -------------------
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.PurchaseOrder',1,'PUBLISHED','{"code":"M5.PurchaseOrder","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Dispatch"},{"code":"APPROVED","assignedRole":"Dispatch"},{"code":"SENT_TO_VENDOR","assignedRole":"UnloadingSupervisor"},{"code":"RECEIVED","assignedRole":"Dispatch"},{"code":"CLOSED","terminal":true},{"code":"CANCELLED","terminal":true}],"transitions":[{"from":"DRAFT","to":"APPROVED","trigger":"page.submitted","pageCode":"pico:po-approve","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"DRAFT","to":"CANCELLED","trigger":"page.submitted","pageCode":"pico:po-approve","guard":"#submitted[''status''] == ''CANCELLED''"},{"from":"APPROVED","to":"SENT_TO_VENDOR","trigger":"page.submitted","pageCode":"pico:po-send"},{"from":"SENT_TO_VENDOR","to":"RECEIVED","trigger":"page.submitted","pageCode":"pico:po-receive"},{"from":"RECEIVED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:po-close"}]}'),
(1,'M5.SalesOrder',1,'PUBLISHED','{"code":"M5.SalesOrder","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Dispatch"},{"code":"CONFIRMED","assignedRole":"UnloadingSupervisor"},{"code":"IN_PRODUCTION","assignedRole":"UnloadingSupervisor"},{"code":"READY_TO_DISPATCH","assignedRole":"Dispatch"},{"code":"DISPATCHED","assignedRole":"Dispatch"},{"code":"INVOICED","assignedRole":"Dispatch"},{"code":"CLOSED","terminal":true},{"code":"CANCELLED","terminal":true}],"transitions":[{"from":"DRAFT","to":"CONFIRMED","trigger":"page.submitted","pageCode":"pico:so-confirm"},{"from":"CONFIRMED","to":"IN_PRODUCTION","trigger":"page.submitted","pageCode":"pico:so-production"},{"from":"IN_PRODUCTION","to":"READY_TO_DISPATCH","trigger":"page.submitted","pageCode":"pico:so-ready"},{"from":"READY_TO_DISPATCH","to":"DISPATCHED","trigger":"page.submitted","pageCode":"pico:so-dispatch"},{"from":"DISPATCHED","to":"INVOICED","trigger":"page.submitted","pageCode":"pico:so-invoice"},{"from":"INVOICED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:so-close"}]}');

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'PurchaseOrder','Purchase Order','m5_purchase_order','Procure-to-Pay header (M5).','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='PurchaseOrder');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'SalesOrder','Sales Order','m5_sales_order','Order-to-Cash header (M5).','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='SalesOrder');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PurchaseOrder','po_number','PO #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PurchaseOrder' AND field_code='po_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PurchaseOrder','vendor_name','Vendor','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PurchaseOrder' AND field_code='vendor_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PurchaseOrder','total_value','Value','DECIMAL',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PurchaseOrder' AND field_code='total_value');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PurchaseOrder','required_by_date','Required By','DATE',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PurchaseOrder' AND field_code='required_by_date');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PurchaseOrder','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PurchaseOrder' AND field_code='status');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SalesOrder','so_number','SO #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrder' AND field_code='so_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SalesOrder','customer_name','Customer','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrder' AND field_code='customer_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SalesOrder','customer_gstin','GSTIN','STRING',TRUE,FALSE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrder' AND field_code='customer_gstin');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SalesOrder','total_value','Value','DECIMAL',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrder' AND field_code='total_value');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SalesOrder','required_by_date','Required By','DATE',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrder' AND field_code='required_by_date');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'SalesOrder','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrder' AND field_code='status');

-- 5. Page definitions ----------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:po-list','Purchase Orders','All POs.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:po-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:po-create','New Purchase Order','Create PO.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:po-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:po-approve','Approve PO','Manager approves/cancels.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:po-approve');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:po-send','Send PO','Email/portal dispatch.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:po-send');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:po-receive','PO Received','Mark received against GRN.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:po-receive');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:po-close','Close PO','Final closure.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:po-close');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:so-list','Sales Orders','All SOs.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:so-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:so-create','New Sales Order','Sales creates SO.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:so-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:so-confirm','Confirm SO','Confirm order.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:so-confirm');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:so-production','SO In Production','Hand to MES.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:so-production');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:so-ready','Ready to Dispatch','FG ready.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:so-ready');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:so-dispatch','SO Dispatched','Link to outbound GP.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:so-dispatch');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:so-invoice','SO Invoiced','IRN generated.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:so-invoice');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:so-close','Close SO','Final closure.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:so-close');

-- 6. Page versions -------------------------------------------------------
INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"PurchaseOrder","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","po_number","vendor_name","total_value","required_by_date","status","current_step"]},"columns":[{"field":"po_number","label":"PO #","sticky":"left","sortable":true},{"field":"vendor_name","label":"Vendor"},{"field":"total_value","label":"Value (INR)","render":"number","align":"right"},{"field":"required_by_date","label":"Required By","render":"date"},{"field":"status","label":"Status","render":"badge","colorMap":{"DRAFT":"grey","APPROVED":"blue","SENT_TO_VENDOR":"amber","RECEIVED":"green","CLOSED":"green","CANCELLED":"red"}},{"field":"current_step","label":"Step"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["DRAFT","APPROVED","SENT_TO_VENDOR","RECEIVED","CLOSED","CANCELLED"]}}],"sort":{"default":[{"field":"po_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Purchase Orders"},"responsive":{"mobile":{"renderAs":"cards","cardFields":["po_number","vendor_name","status","total_value"]}}}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:po-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New Purchase Order","fields":[{"kind":"FormField","type":"text","name":"vendorName","label":"Vendor"},{"kind":"FormField","type":"text","name":"itemCode","label":"Item Code"},{"kind":"FormField","type":"number","name":"qtyOrdered","label":"Qty Ordered"},{"kind":"FormField","type":"text","name":"uom","label":"UoM"},{"kind":"FormField","type":"number","name":"rate","label":"Rate"},{"kind":"WorkflowActionButton","label":"Save as Draft","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:po-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"PO Approval","fields":[{"kind":"FormField","type":"text","name":"reason","label":"Reason"},{"kind":"FormField","type":"select","name":"status","label":"Decision","options":[{"label":"APPROVED","value":"APPROVED"},{"label":"CANCELLED","value":"CANCELLED"}]},{"kind":"WorkflowActionButton","label":"Submit Decision","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:po-approve' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Send to Vendor","fields":[{"kind":"FormField","type":"text","name":"channel","label":"Channel (Email/Portal)"},{"kind":"WorkflowActionButton","label":"Send PO","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:po-send' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Mark Received","fields":[{"kind":"FormField","type":"text","name":"linkedGrnNumber","label":"Linked GRN #"},{"kind":"FormField","type":"number","name":"qtyReceived","label":"Qty Received"},{"kind":"WorkflowActionButton","label":"Mark Received","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:po-receive' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close PO","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Closing Notes"},{"kind":"WorkflowActionButton","label":"Close PO","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:po-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"SalesOrder","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","so_number","customer_name","customer_gstin","total_value","required_by_date","status","current_step"]},"columns":[{"field":"so_number","label":"SO #","sticky":"left","sortable":true},{"field":"customer_name","label":"Customer"},{"field":"customer_gstin","label":"GSTIN"},{"field":"total_value","label":"Value (INR)","render":"number","align":"right"},{"field":"required_by_date","label":"Required By","render":"date"},{"field":"status","label":"Status","render":"badge","colorMap":{"DRAFT":"grey","CONFIRMED":"blue","IN_PRODUCTION":"amber","READY_TO_DISPATCH":"amber","DISPATCHED":"green","INVOICED":"green","CLOSED":"green","CANCELLED":"red"}},{"field":"current_step","label":"Step"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["DRAFT","CONFIRMED","IN_PRODUCTION","READY_TO_DISPATCH","DISPATCHED","INVOICED","CLOSED","CANCELLED"]}}],"sort":{"default":[{"field":"so_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Sales Orders"},"responsive":{"mobile":{"renderAs":"cards","cardFields":["so_number","customer_name","status","total_value"]}}}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:so-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New Sales Order","fields":[{"kind":"FormField","type":"text","name":"customerName","label":"Customer"},{"kind":"FormField","type":"text","name":"customerGstin","label":"GSTIN"},{"kind":"FormField","type":"text","name":"itemCode","label":"Item Code"},{"kind":"FormField","type":"number","name":"qtyOrdered","label":"Qty Ordered"},{"kind":"FormField","type":"text","name":"uom","label":"UoM"},{"kind":"FormField","type":"number","name":"rate","label":"Rate"},{"kind":"WorkflowActionButton","label":"Save as Draft","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:so-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Confirm SO","fields":[{"kind":"FormField","type":"text","name":"confirmedRate","label":"Rate"},{"kind":"FormField","type":"text","name":"deliveryTerms","label":"Delivery Terms"},{"kind":"WorkflowActionButton","label":"Confirm","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:so-confirm' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Hand to Production","fields":[{"kind":"FormField","type":"text","name":"productionLine","label":"Production Line"},{"kind":"WorkflowActionButton","label":"Hand to MES","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:so-production' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Ready to Dispatch","fields":[{"kind":"FormField","type":"text","name":"readyLotNos","label":"Lot #s"},{"kind":"WorkflowActionButton","label":"Mark Ready","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:so-ready' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Dispatched","fields":[{"kind":"FormField","type":"text","name":"linkedGpNumber","label":"Linked GP #"},{"kind":"WorkflowActionButton","label":"Mark Dispatched","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:so-dispatch' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"E-Invoice","fields":[{"kind":"FormField","type":"text","name":"invoiceNo","label":"Invoice #"},{"kind":"FormField","type":"text","name":"irn","label":"IRN"},{"kind":"WorkflowActionButton","label":"Mark Invoiced","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:so-invoice' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close SO","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Closing Notes"},{"kind":"WorkflowActionButton","label":"Close SO","action":"workflow.transition","variant":"primary"}]}]}', 'V9_1', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:so-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code IN ('pico:po-list','pico:po-create','pico:po-approve','pico:po-send','pico:po-receive','pico:po-close','pico:so-list','pico:so-create','pico:so-confirm','pico:so-production','pico:so-ready','pico:so-dispatch','pico:so-invoice','pico:so-close') AND current_version_id IS NULL;
