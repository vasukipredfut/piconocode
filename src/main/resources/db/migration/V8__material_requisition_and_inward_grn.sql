-- =====================================================================
-- V8 — M5 Inventory primitives: Material Requisition + Inward GRN.
--
-- Two more end-to-end use cases on the same M0 substrate:
--   1. Material Requisition (MR) — department requests material from stores
--      DRAFT → SUBMITTED → APPROVED → ISSUED → CLOSED  (REJECTED terminal)
--   2. Goods Receipt Note (GRN) — items received into stores after unloading
--      DRAFT → ITEMS_LOGGED → QC_CHECKED → STORED → CLOSED  (QC_REJECTED terminal)
--
-- Both reuse the WorkflowEngine + WorkflowTransitionListener that already
-- writes M10.gate_pass on every transition. Same pattern means an MR/GRN
-- row writes status + current_step + workflow_instance_id on each step
-- once the listener is extended (or wired via a generic mapping).
-- =====================================================================

-- 1. Tables ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS material_requisition (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  mr_number VARCHAR(32) NOT NULL,
  requested_by BIGINT NOT NULL,
  requested_for_dept_id BIGINT NULL,
  required_by_date DATE NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  closed_at TIMESTAMP NULL,
  UNIQUE KEY uq_mr_number (tenant_id, mr_number),
  KEY idx_mr_status (tenant_id, status, current_step)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS material_requisition_line (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  mr_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(256) NULL,
  qty_requested DECIMAL(12,3) NOT NULL,
  qty_approved DECIMAL(12,3) NULL,
  qty_issued DECIMAL(12,3) NULL,
  uom VARCHAR(16) NOT NULL,
  purpose VARCHAR(256) NULL,
  line_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  CONSTRAINT fk_mrl_mr FOREIGN KEY (mr_id) REFERENCES material_requisition(id),
  KEY idx_mrl_status (line_status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goods_receipt_note (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  grn_number VARCHAR(32) NOT NULL,
  source_type VARCHAR(16) NOT NULL,
  source_ref VARCHAR(128) NULL,
  gate_pass_id BIGINT NULL,
  vendor_id BIGINT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  received_at TIMESTAMP NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_grn_number (tenant_id, grn_number),
  KEY idx_grn_status (tenant_id, status, current_step),
  KEY idx_grn_gp (gate_pass_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goods_receipt_line (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  grn_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(256) NULL,
  qty_received DECIMAL(12,3) NOT NULL,
  uom VARCHAR(16) NOT NULL,
  lot_no VARCHAR(64) NULL,
  stored_in_tank VARCHAR(16) NULL,
  qc_status VARCHAR(16) NULL,
  qc_notes TEXT NULL,
  CONSTRAINT fk_grnl_grn FOREIGN KEY (grn_id) REFERENCES goods_receipt_note(id),
  KEY idx_grnl_item (item_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- 2. Seed sample rows so the list pages have data --------------------------

INSERT IGNORE INTO material_requisition (tenant_id, mr_number, requested_by, requested_for_dept_id, required_by_date, status, notes) VALUES
  (1,'MR-2026-001', 2003, 1, '2026-06-15', 'SUBMITTED', 'Sunflower oil batch for Coimbatore order'),
  (1,'MR-2026-002', 2004, 1, '2026-06-12', 'APPROVED',  'Groundnut for export run'),
  (1,'MR-2026-003', 2003, 1, '2026-06-20', 'DRAFT',     'Spare bottle caps - 25mm');

INSERT IGNORE INTO material_requisition_line (mr_id, line_no, item_code, item_name, qty_requested, qty_approved, uom, purpose) VALUES
  ((SELECT id FROM material_requisition WHERE mr_number='MR-2026-001'),1,'OIL-SUN-RFD','Refined Sunflower Oil',5000.000,5000.000,'KG','Production line 1'),
  ((SELECT id FROM material_requisition WHERE mr_number='MR-2026-002'),1,'OIL-GND-RAW','Raw Groundnut Oil',  2500.000,2500.000,'KG','Export packing'),
  ((SELECT id FROM material_requisition WHERE mr_number='MR-2026-003'),1,'CAP-25MM-WH','25mm White Caps',     5000.000,NULL,    'PCS','Bottling line replenishment');

INSERT IGNORE INTO goods_receipt_note (tenant_id, grn_number, source_type, source_ref, status, notes, received_at) VALUES
  (1,'GRN-2026-001','VENDOR','TESTRO Industries','STORED','Sunflower RM tanker — accepted', '2026-06-07 10:30:00'),
  (1,'GRN-2026-002','VENDOR','Iyer Lubricants',  'QC_CHECKED','Groundnut RM — QC pending', '2026-06-08 09:10:00');

INSERT IGNORE INTO goods_receipt_line (grn_id, line_no, item_code, item_name, qty_received, uom, lot_no, stored_in_tank, qc_status) VALUES
  ((SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-001'),1,'OIL-SUN-RAW','Raw Sunflower Oil', 21500.000,'KG','LOT-SUN-260607-A','T-OIL-03','ACCEPTED'),
  ((SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-002'),1,'OIL-GND-RAW','Raw Groundnut Oil', 18750.000,'KG','LOT-GND-260608-A','T-OIL-17','PENDING');

-- 3. Workflow definitions --------------------------------------------------

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.MaterialRequisition',1,'PUBLISHED','{"code":"M5.MaterialRequisition","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Dispatch"},{"code":"SUBMITTED","assignedRole":"Manager"},{"code":"APPROVED","assignedRole":"UnloadingSupervisor"},{"code":"ISSUED","assignedRole":"Dispatch"},{"code":"CLOSED","terminal":true},{"code":"REJECTED","terminal":true}],"transitions":[{"from":"DRAFT","to":"SUBMITTED","trigger":"page.submitted","pageCode":"pico:mr-create"},{"from":"SUBMITTED","to":"APPROVED","trigger":"page.submitted","pageCode":"pico:mr-approve","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"SUBMITTED","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:mr-approve","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"APPROVED","to":"ISSUED","trigger":"page.submitted","pageCode":"pico:mr-issue"},{"from":"ISSUED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:mr-close"}]}'),
(1,'M5.InwardReceipt',1,'PUBLISHED','{"code":"M5.InwardReceipt","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"UnloadingSupervisor"},{"code":"ITEMS_LOGGED","assignedRole":"QC"},{"code":"QC_CHECKED","assignedRole":"UnloadingSupervisor"},{"code":"STORED","assignedRole":"Dispatch"},{"code":"CLOSED","terminal":true},{"code":"QC_REJECTED","terminal":true}],"transitions":[{"from":"DRAFT","to":"ITEMS_LOGGED","trigger":"page.submitted","pageCode":"pico:grn-create"},{"from":"ITEMS_LOGGED","to":"QC_CHECKED","trigger":"page.submitted","pageCode":"pico:grn-qc","guard":"#submitted[''status''] == ''ACCEPTED''"},{"from":"ITEMS_LOGGED","to":"QC_REJECTED","trigger":"page.submitted","pageCode":"pico:grn-qc","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"QC_CHECKED","to":"STORED","trigger":"page.submitted","pageCode":"pico:grn-store"},{"from":"STORED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:grn-close"}]}');

-- 4. Entity registry -------------------------------------------------------

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'MaterialRequisition','Material Requisition','material_requisition','Department requests for material issue.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='MaterialRequisition');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'GoodsReceiptNote','Goods Receipt Note','goods_receipt_note','Inward — items received into stores.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='GoodsReceiptNote');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaterialRequisition','mr_number','MR #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaterialRequisition' AND field_code='mr_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaterialRequisition','requested_by','Requester','LONG',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaterialRequisition' AND field_code='requested_by');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaterialRequisition','required_by_date','Required By','DATE',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaterialRequisition' AND field_code='required_by_date');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaterialRequisition','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaterialRequisition' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaterialRequisition','current_step','Step','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaterialRequisition' AND field_code='current_step');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaterialRequisition','created_at','Created','DATETIME',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaterialRequisition' AND field_code='created_at');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'GoodsReceiptNote','grn_number','GRN #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GoodsReceiptNote' AND field_code='grn_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'GoodsReceiptNote','source_type','Source','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GoodsReceiptNote' AND field_code='source_type');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'GoodsReceiptNote','source_ref','Reference','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GoodsReceiptNote' AND field_code='source_ref');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'GoodsReceiptNote','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GoodsReceiptNote' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'GoodsReceiptNote','received_at','Received At','DATETIME',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GoodsReceiptNote' AND field_code='received_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'GoodsReceiptNote','gate_pass_id','Linked GP','LONG',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GoodsReceiptNote' AND field_code='gate_pass_id');

-- 5. Pages -----------------------------------------------------------------

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mr-list','Material Requisitions','All material requisitions across status/dept.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mr-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mr-create','New Material Requisition','Create a material requisition request.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mr-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mr-approve','Approve MR','Manager approves/rejects an MR.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mr-approve');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mr-issue','Issue Material','Store keeper issues approved qty with lot.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mr-issue');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mr-close','Close MR','Final close-out and audit stamp.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mr-close');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:grn-list','Goods Receipt Notes','Inward GRNs — items received into stores.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:grn-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:grn-create','New GRN','Unloading supervisor logs items received.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:grn-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:grn-qc','GRN QC','QC accepts or rejects the inward batch.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:grn-qc');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:grn-store','GRN Store','Move accepted items into the storage tank.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:grn-store');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:grn-close','GRN Close','Final inward close + audit row.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:grn-close');

-- 6. Page versions (list views + minimal forms) ---------------------------

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"MaterialRequisition","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","mr_number","requested_by","required_by_date","status","current_step","created_at"]},"columns":[{"field":"mr_number","label":"MR #","sticky":"left","sortable":true},{"field":"requested_by","label":"Requester"},{"field":"required_by_date","label":"Required By","render":"date"},{"field":"status","label":"Status","render":"badge","colorMap":{"DRAFT":"grey","SUBMITTED":"blue","APPROVED":"amber","ISSUED":"green","CLOSED":"green","REJECTED":"red"}},{"field":"current_step","label":"Step"},{"field":"created_at","label":"Created","render":"datetime"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["DRAFT","SUBMITTED","APPROVED","ISSUED","CLOSED","REJECTED"]}},{"field":"_q","label":"Search","type":"text-contains","applyTo":["mr_number"]}],"sort":{"default":[{"field":"created_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Material Requisitions"},"responsive":{"mobile":{"renderAs":"cards","cardFields":["mr_number","status","required_by_date"]}}}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mr-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New Material Requisition","fields":[{"kind":"FormField","type":"text","name":"itemCode","label":"Item Code"},{"kind":"FormField","type":"text","name":"itemName","label":"Item Name"},{"kind":"FormField","type":"number","name":"qtyRequested","label":"Qty Requested"},{"kind":"FormField","type":"text","name":"uom","label":"UoM (KG/L/PCS)"},{"kind":"FormField","type":"text","name":"purpose","label":"Purpose"},{"kind":"FormField","type":"text","name":"requiredByDate","label":"Required By"},{"kind":"WorkflowActionButton","label":"Submit Requisition","action":"workflow.transition","variant":"primary"}]}]}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mr-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"MR Approval","fields":[{"kind":"FormField","type":"number","name":"qtyApproved","label":"Approved Qty"},{"kind":"FormField","type":"text","name":"reason","label":"Approval/Rejection Reason"},{"kind":"FormField","type":"select","name":"status","label":"Decision","options":[{"label":"APPROVED","value":"APPROVED"},{"label":"REJECTED","value":"REJECTED"}]},{"kind":"WorkflowActionButton","label":"Submit Decision","action":"workflow.transition","variant":"primary"}]}]}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mr-approve' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Issue Material","fields":[{"kind":"FormField","type":"number","name":"qtyIssued","label":"Qty Issued"},{"kind":"FormField","type":"text","name":"lotNo","label":"Lot #"},{"kind":"FormField","type":"text","name":"binLocation","label":"From Bin/Tank"},{"kind":"WorkflowActionButton","label":"Issue & Update Stock","action":"workflow.transition","variant":"primary"}]}]}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mr-issue' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close Material Requisition","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Closing Notes"},{"kind":"WorkflowActionButton","label":"Close Requisition","action":"workflow.transition","variant":"primary"}]}]}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mr-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"GoodsReceiptNote","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","grn_number","source_type","source_ref","status","gate_pass_id","received_at"]},"columns":[{"field":"grn_number","label":"GRN #","sticky":"left","sortable":true},{"field":"source_type","label":"Source","render":"badge","colorMap":{"VENDOR":"blue","TRANSFER":"amber","RETURN":"purple","3RD_PARTY":"grey"}},{"field":"source_ref","label":"Reference"},{"field":"status","label":"Status","render":"badge","colorMap":{"DRAFT":"grey","ITEMS_LOGGED":"blue","QC_CHECKED":"amber","STORED":"green","CLOSED":"green","QC_REJECTED":"red"}},{"field":"gate_pass_id","label":"GP #"},{"field":"received_at","label":"Received","render":"datetime"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["DRAFT","ITEMS_LOGGED","QC_CHECKED","STORED","CLOSED","QC_REJECTED"]}},{"field":"source_type","label":"Source","type":"multi-select","optionsSource":{"type":"static","options":["VENDOR","TRANSFER","RETURN","3RD_PARTY"]}},{"field":"_q","label":"Search","type":"text-contains","applyTo":["grn_number","source_ref"]}],"sort":{"default":[{"field":"received_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Goods Receipt Notes"},"responsive":{"mobile":{"renderAs":"cards","cardFields":["grn_number","source_type","status","received_at"]}}}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:grn-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Log Received Items","fields":[{"kind":"FormField","type":"text","name":"sourceRef","label":"Source / Vendor"},{"kind":"FormField","type":"text","name":"itemCode","label":"Item Code"},{"kind":"FormField","type":"text","name":"itemName","label":"Item Name"},{"kind":"FormField","type":"number","name":"qtyReceived","label":"Qty Received"},{"kind":"FormField","type":"text","name":"uom","label":"UoM"},{"kind":"FormField","type":"text","name":"lotNo","label":"Lot #"},{"kind":"FormField","type":"text","name":"linkedGpNumber","label":"Linked Gate Pass # (optional)"},{"kind":"WorkflowActionButton","label":"Submit to QC","action":"workflow.transition","variant":"primary"}]}]}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:grn-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"QC Check","fields":[{"kind":"FormField","type":"text","name":"sampleColour","label":"Sample Colour"},{"kind":"FormField","type":"text","name":"sampleOdour","label":"Sample Odour"},{"kind":"FormField","type":"text","name":"densityCheck","label":"Density (g/cc)"},{"kind":"FormField","type":"text","name":"qcNotes","label":"QC Notes"},{"kind":"FormField","type":"select","name":"status","label":"QC Decision","options":[{"label":"ACCEPTED","value":"ACCEPTED"},{"label":"REJECTED","value":"REJECTED"}]},{"kind":"WorkflowActionButton","label":"Submit QC Result","action":"workflow.transition","variant":"primary"}]}]}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:grn-qc' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Store into Tank","fields":[{"kind":"FormField","type":"text","name":"storeTankCode","label":"Storage Tank (T-OIL-01..30)"},{"kind":"FormField","type":"text","name":"finalLotNo","label":"Final Lot # (may differ from inbound)"},{"kind":"FormField","type":"text","name":"storedAt","label":"Stored At (timestamp)"},{"kind":"WorkflowActionButton","label":"Confirm Stored","action":"workflow.transition","variant":"primary"}]}]}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:grn-store' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close GRN","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Closing Notes"},{"kind":"WorkflowActionButton","label":"Close GRN","action":"workflow.transition","variant":"primary"}]}]}', 'V8', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:grn-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code IN ('pico:mr-list','pico:mr-create','pico:mr-approve','pico:mr-issue','pico:mr-close','pico:grn-list','pico:grn-create','pico:grn-qc','pico:grn-store','pico:grn-close') AND current_version_id IS NULL;
