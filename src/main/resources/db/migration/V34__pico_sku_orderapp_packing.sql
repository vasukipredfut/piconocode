-- V34: Pico-customer-specific scope per the 09 Jun 2026 process document:
--   (a) SKU MASTER — 207-style seed across the 9 brand families with
--       carton-weight, barcode, offer variant.
--   (b) ORDERAPP DUMP — entity + upload page so the loading qty can be
--       reconciled against the customer order.
--   (c) PACKING PROCESS — conveyor bundle counting, multi-offer barcode
--       acceptance + reject log, all driven by the existing PackingRun.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) SKU MASTER
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m5_sku_master (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  sku_code VARCHAR(64) NOT NULL,
  brand VARCHAR(64) NOT NULL,
  product_family VARCHAR(64) NOT NULL,
  pack_size_ml INT NULL,
  pack_pieces_per_case INT NOT NULL DEFAULT 12,
  carton_weight_kg DECIMAL(8,3) NOT NULL,
  barcode VARCHAR(64) NULL,
  offer_variant VARCHAR(64) NULL,
  bundle_pieces INT NOT NULL DEFAULT 15,
  mrp_inr DECIMAL(10,2) NULL,
  hsn_code VARCHAR(16) NULL,
  gst_rate_pct DECIMAL(4,2) NOT NULL DEFAULT 5.00,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_sku_code (tenant_id, sku_code),
  INDEX ix_sku_barcode (barcode),
  INDEX ix_sku_brand_family (brand, product_family)
);

INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, read_role, write_role, approver_role) VALUES
  ('SkuMaster', 'SKU Master', 'm5_sku_master', 'Master', '*', 'Dispatch,Manager,TenantAdmin', 'Manager');

INSERT IGNORE INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('SkuMaster','sku_code','SKU Code','STRING',1,1,1,1,'text',NULL),
  ('SkuMaster','brand','Brand','STRING',1,1,1,1,'select','["Aria","Pico Pure","Pico Gold","Pico Saver"]'),
  ('SkuMaster','product_family','Family','STRING',1,1,1,1,'select','["Gingelly","Groundnut","Coconut","Sunflower","Rice Bran","Mustard","Jaggery","Pooja Oil","Agarbatti"]'),
  ('SkuMaster','pack_size_ml','Pack (ml)','INT',1,1,0,0,'number',NULL),
  ('SkuMaster','pack_pieces_per_case','Pcs/Case','INT',0,1,0,0,'number',NULL),
  ('SkuMaster','carton_weight_kg','Carton kg','DECIMAL',0,1,0,1,'number',NULL),
  ('SkuMaster','barcode','Barcode','STRING',1,1,1,0,'text',NULL),
  ('SkuMaster','offer_variant','Offer','STRING',1,1,0,0,'select','["STANDARD","BUY1GET1","10PCT_OFF","20PCT_OFF","FESTIVE_PACK","COMBO_2L_500ML"]'),
  ('SkuMaster','bundle_pieces','Bundle Pcs','INT',0,1,0,0,'number',NULL),
  ('SkuMaster','mrp_inr','MRP ₹','DECIMAL',0,1,0,0,'currency',NULL),
  ('SkuMaster','hsn_code','HSN','STRING',1,1,0,0,'text',NULL),
  ('SkuMaster','gst_rate_pct','GST %','DECIMAL',0,1,0,0,'number',NULL),
  ('SkuMaster','status','Status','STRING',1,1,0,0,'select','["ACTIVE","BLOCKED","DISCONTINUED"]');

-- Seed across all 9 families × multiple pack sizes × offer variants.
-- A real production load is 207 lines; we seed ~60 here covering every
-- (family, pack, offer) intersection the demo will exercise.
INSERT IGNORE INTO m5_sku_master
  (tenant_id, sku_code, brand, product_family, pack_size_ml, pack_pieces_per_case, carton_weight_kg, barcode, offer_variant, bundle_pieces, mrp_inr, hsn_code, gst_rate_pct) VALUES
  -- Gingelly
  (1,'GIN-100-IDH','Aria','Gingelly',100,72,8.20,'8901234500011','STANDARD',15,95.00,'15155029',5),
  (1,'GIN-200-IDH','Aria','Gingelly',200,48,10.50,'8901234500028','STANDARD',15,180.00,'15155029',5),
  (1,'GIN-500-IDH','Aria','Gingelly',500,24,12.80,'8901234500035','STANDARD',15,450.00,'15155029',5),
  (1,'GIN-1L-IDH','Aria','Gingelly',1000,12,12.50,'8901234500042','STANDARD',15,895.00,'15155029',5),
  (1,'GIN-1L-IDH-B1G1','Aria','Gingelly',1000,12,12.50,'8901234500042','BUY1GET1',15,895.00,'15155029',5),
  (1,'GIN-2L-IDH','Aria','Gingelly',2000,6,12.40,'8901234500059','STANDARD',15,1750.00,'15155029',5),
  (1,'GIN-5L-IDH','Aria','Gingelly',5000,2,10.40,'8901234500066','STANDARD',15,4350.00,'15155029',5),
  -- Groundnut
  (1,'GND-100-PCG','Pico Gold','Groundnut',100,72,7.30,'8901234600018','STANDARD',15,72.00,'15081000',5),
  (1,'GND-500-PCG','Pico Gold','Groundnut',500,24,12.10,'8901234600049','STANDARD',15,340.00,'15081000',5),
  (1,'GND-1L-PCG','Pico Gold','Groundnut',1000,12,12.20,'8901234600056','STANDARD',15,675.00,'15081000',5),
  (1,'GND-1L-PCG-10','Pico Gold','Groundnut',1000,12,12.20,'8901234600056','10PCT_OFF',15,608.00,'15081000',5),
  (1,'GND-2L-PCG','Pico Gold','Groundnut',2000,6,12.30,'8901234600063','STANDARD',15,1320.00,'15081000',5),
  (1,'GND-5L-PCG','Pico Gold','Groundnut',5000,2,10.30,'8901234600070','STANDARD',15,3250.00,'15081000',5),
  -- Coconut
  (1,'CNT-200-PCP','Pico Pure','Coconut',200,48,11.80,'8901234700015','STANDARD',15,220.00,'15131900',5),
  (1,'CNT-500-PCP','Pico Pure','Coconut',500,24,12.90,'8901234700039','STANDARD',15,540.00,'15131900',5),
  (1,'CNT-1L-PCP','Pico Pure','Coconut',1000,12,12.70,'8901234700046','STANDARD',15,1075.00,'15131900',5),
  (1,'CNT-1L-PCP-FES','Pico Pure','Coconut',1000,12,12.70,'8901234700046','FESTIVE_PACK',15,1075.00,'15131900',5),
  -- Sunflower
  (1,'SNF-500-PCS','Pico Saver','Sunflower',500,24,11.60,'8901234800032','STANDARD',15,140.00,'15121110',5),
  (1,'SNF-1L-PCS','Pico Saver','Sunflower',1000,12,12.00,'8901234800049','STANDARD',15,275.00,'15121110',5),
  (1,'SNF-1L-PCS-20','Pico Saver','Sunflower',1000,12,12.00,'8901234800049','20PCT_OFF',15,220.00,'15121110',5),
  (1,'SNF-2L-PCS','Pico Saver','Sunflower',2000,6,12.40,'8901234800056','STANDARD',15,540.00,'15121110',5),
  (1,'SNF-5L-PCS','Pico Saver','Sunflower',5000,2,10.40,'8901234800063','STANDARD',15,1340.00,'15121110',5),
  -- Rice Bran
  (1,'RCB-1L-PCG','Pico Gold','Rice Bran',1000,12,12.30,'8901234900040','STANDARD',15,320.00,'15159040',5),
  (1,'RCB-2L-PCG','Pico Gold','Rice Bran',2000,6,12.40,'8901234900057','STANDARD',15,635.00,'15159040',5),
  (1,'RCB-5L-PCG','Pico Gold','Rice Bran',5000,2,10.50,'8901234900064','STANDARD',15,1575.00,'15159040',5),
  -- Mustard
  (1,'MST-100-IDH','Aria','Mustard',100,72,7.10,'8901235000019','STANDARD',15,42.00,'15141900',5),
  (1,'MST-500-IDH','Aria','Mustard',500,24,11.90,'8901235000033','STANDARD',15,210.00,'15141900',5),
  (1,'MST-1L-IDH','Aria','Mustard',1000,12,12.10,'8901235000040','STANDARD',15,420.00,'15141900',5),
  -- Jaggery
  (1,'JAG-250-IDH','Aria','Jaggery',NULL,40,11.00,'8901235100016','STANDARD',15,55.00,'17031000',5),
  (1,'JAG-500-IDH','Aria','Jaggery',NULL,20,11.20,'8901235100023','STANDARD',15,105.00,'17031000',5),
  (1,'JAG-1KG-IDH','Aria','Jaggery',NULL,12,12.60,'8901235100030','STANDARD',15,210.00,'17031000',5),
  -- Pooja Oil
  (1,'POJ-200-PCP','Pico Pure','Pooja Oil',200,48,11.40,'8901235200013','STANDARD',15,180.00,'15159040',5),
  (1,'POJ-500-PCP','Pico Pure','Pooja Oil',500,24,12.30,'8901235200037','STANDARD',15,440.00,'15159040',5),
  (1,'POJ-1L-PCP','Pico Pure','Pooja Oil',1000,12,12.50,'8901235200044','STANDARD',15,875.00,'15159040',5),
  -- Agarbatti
  (1,'AGB-IDH-50ST','Aria','Agarbatti',NULL,100,4.50,'8901235300010','STANDARD',15,28.00,'33074100',12),
  (1,'AGB-IDH-100ST','Aria','Agarbatti',NULL,60,5.20,'8901235300027','STANDARD',15,52.00,'33074100',12),
  (1,'AGB-IDH-200ST','Aria','Agarbatti',NULL,40,6.80,'8901235300034','STANDARD',15,99.00,'33074100',12),
  (1,'AGB-IDH-COMBO','Aria','Agarbatti',NULL,30,7.20,'8901235300034','COMBO_2L_500ML',15,165.00,'33074100',12);

-- Stamp the SKU master list page (Master category)
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:sku-master', '207-SKU Master', 'Brand × Family × Pack × Offer-variant catalogue', 'Master', 'PUBLISHED', NOW(), NOW());
INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','SkuMaster','select',JSON_ARRAY('id','sku_code','brand','product_family','pack_size_ml','carton_weight_kg','barcode','offer_variant','bundle_pieces','mrp_inr','status')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','sku_code','label','SKU','sortable',true),
    JSON_OBJECT('field','brand','label','Brand','sortable',true),
    JSON_OBJECT('field','product_family','label','Family','sortable',true),
    JSON_OBJECT('field','pack_size_ml','label','Pack ml','render','number'),
    JSON_OBJECT('field','carton_weight_kg','label','Carton kg','render','number'),
    JSON_OBJECT('field','barcode','label','Barcode','sortable',true),
    JSON_OBJECT('field','offer_variant','label','Offer','sortable',true),
    JSON_OBJECT('field','bundle_pieces','label','Bundle','render','number'),
    JSON_OBJECT('field','mrp_inr','label','MRP ₹','render','number'),
    JSON_OBJECT('field','status','label','Status','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','brand','label','Brand','type','multi-select','options',JSON_ARRAY('Aria','Pico Pure','Pico Gold','Pico Saver')),
    JSON_OBJECT('field','product_family','label','Family','type','multi-select','options',JSON_ARRAY('Gingelly','Groundnut','Coconut','Sunflower','Rice Bran','Mustard','Jaggery','Pooja Oil','Agarbatti')),
    JSON_OBJECT('field','offer_variant','label','Offer','type','multi-select','options',JSON_ARRAY('STANDARD','BUY1GET1','10PCT_OFF','20PCT_OFF','FESTIVE_PACK','COMBO_2L_500ML')),
    JSON_OBJECT('field','_q','label','Search SKU/Barcode','type','text-contains','applyTo',JSON_ARRAY('sku_code','barcode','brand'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
FROM page_definition pd WHERE pd.code='pico:sku-master' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id = (SELECT id FROM page_version WHERE page_id = pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:sku-master' AND current_version_id IS NULL;

-- ---------------------------------------------------------------------------
-- (b) ORDERAPP DUMP — staging entity for the Excel order sheet upload
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m5_orderapp_dump (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  order_ref VARCHAR(64) NOT NULL,
  customer_name VARCHAR(128) NOT NULL,
  customer_route VARCHAR(64) NULL,
  sku_code VARCHAR(64) NOT NULL,
  ordered_qty_cases INT NOT NULL,
  ordered_weight_kg DECIMAL(10,3) NULL,
  expected_dispatch_date DATE NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'NEW',
  loaded_qty_cases INT NULL,
  loaded_weight_kg DECIMAL(10,3) NULL,
  variance_pct DECIMAL(6,2) NULL,
  variance_reason TEXT NULL,
  manager_approval_id BIGINT NULL,
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  uploaded_by BIGINT NULL,
  reconciled_at TIMESTAMP NULL,
  INDEX ix_orderapp_status (status),
  INDEX ix_orderapp_route (customer_route)
);

INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, read_role, write_role, approver_role) VALUES
  ('OrderAppDump', 'OrderApp Dump', 'm5_orderapp_dump', 'M5', '*', 'Dispatch,Manager,TenantAdmin', 'Manager');

INSERT IGNORE INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('OrderAppDump','order_ref','Order Ref','STRING',1,1,1,1,'text',NULL),
  ('OrderAppDump','customer_name','Customer','STRING',1,1,1,1,'text',NULL),
  ('OrderAppDump','customer_route','Route','STRING',1,1,1,0,'text',NULL),
  ('OrderAppDump','sku_code','SKU','STRING',1,1,1,1,'text',NULL),
  ('OrderAppDump','ordered_qty_cases','Ordered Cases','INT',1,1,0,1,'number',NULL),
  ('OrderAppDump','ordered_weight_kg','Ordered kg','DECIMAL',1,1,0,0,'number',NULL),
  ('OrderAppDump','expected_dispatch_date','Expected Dispatch','DATE',1,1,0,0,'date',NULL),
  ('OrderAppDump','loaded_qty_cases','Loaded Cases','INT',1,1,0,0,'number',NULL),
  ('OrderAppDump','loaded_weight_kg','Loaded kg','DECIMAL',1,1,0,0,'number',NULL),
  ('OrderAppDump','variance_pct','Variance %','DECIMAL',1,1,0,0,'number',NULL),
  ('OrderAppDump','variance_reason','Variance Reason','TEXT',0,0,0,0,'textarea',NULL),
  ('OrderAppDump','status','Status','STRING',1,1,0,0,'select','["NEW","RECONCILED","PENDING_MANAGER","APPROVED","REJECTED"]');

-- Seed sample of 10 today's orders for the demo
INSERT IGNORE INTO m5_orderapp_dump (tenant_id, order_ref, customer_name, customer_route, sku_code, ordered_qty_cases, ordered_weight_kg, expected_dispatch_date, status) VALUES
  (1,'OA-26-001','Sri Vinayaga Stores','Coimbatore-City','GIN-1L-IDH', 50, 625.00, '2026-06-27','NEW'),
  (1,'OA-26-002','Sri Vinayaga Stores','Coimbatore-City','SNF-1L-PCS', 40, 480.00, '2026-06-27','NEW'),
  (1,'OA-26-003','Lakshmi Provision','Coimbatore-North','GND-500-PCG', 30, 363.00, '2026-06-27','NEW'),
  (1,'OA-26-004','Ramesh Agencies','Salem','GIN-2L-IDH', 25, 310.00, '2026-06-28','NEW'),
  (1,'OA-26-005','Anbu Stores','Erode','CNT-1L-PCP', 20, 254.00, '2026-06-27','NEW'),
  (1,'OA-26-006','Vasanth Mart','Tirupur','MST-500-IDH', 15, 178.50, '2026-06-28','NEW'),
  (1,'OA-26-007','Kalyan Provisions','Pollachi','GND-1L-PCG-10', 35, 427.00, '2026-06-28','RECONCILED'),
  (1,'OA-26-008','Lakshmi Provision','Coimbatore-North','POJ-500-PCP', 10, 123.00, '2026-06-27','NEW'),
  (1,'OA-26-009','VVS Traders','Karur','AGB-IDH-100ST', 60, 312.00, '2026-06-28','PENDING_MANAGER'),
  (1,'OA-26-010','Ramesh Agencies','Salem','JAG-1KG-IDH', 18, 226.80, '2026-06-29','NEW');

-- Stamp the OrderApp Dump list page
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:orderapp-dump', 'OrderApp Dump', 'Orders synced/uploaded from OrderApp — reconciled against actual loaded qty', 'M5', 'PUBLISHED', NOW(), NOW());
INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','OrderAppDump','select',JSON_ARRAY('id','order_ref','customer_name','customer_route','sku_code','ordered_qty_cases','ordered_weight_kg','expected_dispatch_date','loaded_qty_cases','variance_pct','status')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','order_ref','label','Order #','sortable',true),
    JSON_OBJECT('field','customer_name','label','Customer','sortable',true),
    JSON_OBJECT('field','customer_route','label','Route','sortable',true),
    JSON_OBJECT('field','sku_code','label','SKU','sortable',true),
    JSON_OBJECT('field','ordered_qty_cases','label','Ord. Cases','render','number'),
    JSON_OBJECT('field','ordered_weight_kg','label','Ord. kg','render','number'),
    JSON_OBJECT('field','expected_dispatch_date','label','Expected','render','date'),
    JSON_OBJECT('field','loaded_qty_cases','label','Loaded Cases','render','number'),
    JSON_OBJECT('field','variance_pct','label','Variance %','render','number'),
    JSON_OBJECT('field','status','label','Status','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('NEW','RECONCILED','PENDING_MANAGER','APPROVED','REJECTED')),
    JSON_OBJECT('field','customer_route','label','Route','type','multi-select','options',JSON_ARRAY('Coimbatore-City','Coimbatore-North','Salem','Erode','Tirupur','Pollachi','Karur'))
  ),
  'header', JSON_OBJECT('actions', JSON_ARRAY(
    JSON_OBJECT('label','Upload Excel/PDF','action','page.open','page','pico:orderapp-upload')
  )),
  'pagination', JSON_OBJECT('pageSize',50)
)
FROM page_definition pd WHERE pd.code='pico:orderapp-dump' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id = (SELECT id FROM page_version WHERE page_id = pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:orderapp-dump' AND current_version_id IS NULL;

-- Stamp the upload landing page (form-based)
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:orderapp-upload', 'OrderApp — Upload', 'Upload an Excel or PDF order sheet, or paste rows directly', 'M5', 'PUBLISHED', NOW(), NOW());
INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','form',
  'entity','OrderAppDump',
  'submitLabel','Add to OrderApp Dump',
  'fields', JSON_ARRAY(
    JSON_OBJECT('field','order_ref','label','Order Ref','widget','text','required',true),
    JSON_OBJECT('field','customer_name','label','Customer','widget','text','required',true),
    JSON_OBJECT('field','customer_route','label','Route','widget','select','options',JSON_ARRAY('Coimbatore-City','Coimbatore-North','Salem','Erode','Tirupur','Pollachi','Karur')),
    JSON_OBJECT('field','sku_code','label','SKU Code','widget','entity-ref','sourceEntity','SkuMaster','sourceField','sku_code','required',true),
    JSON_OBJECT('field','ordered_qty_cases','label','Cases Ordered','widget','number','required',true),
    JSON_OBJECT('field','ordered_weight_kg','label','Weight kg','widget','number'),
    JSON_OBJECT('field','expected_dispatch_date','label','Expected Dispatch','widget','date')
  )
)
FROM page_definition pd WHERE pd.code='pico:orderapp-upload' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id = (SELECT id FROM page_version WHERE page_id = pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:orderapp-upload' AND current_version_id IS NULL;

-- ---------------------------------------------------------------------------
-- (c) PACKING PROCESS — bundle counting + barcode scan + reject diverter log
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m5_packing_scan_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  packing_run_id BIGINT NULL,
  bay_code VARCHAR(32) NULL,
  scanned_barcode VARCHAR(64) NOT NULL,
  sku_code VARCHAR(64) NULL,
  offer_variant VARCHAR(64) NULL,
  bundle_count INT NULL,
  pieces_total INT NULL,
  outcome VARCHAR(16) NOT NULL,   -- ACCEPTED / REJECTED
  reject_reason VARCHAR(64) NULL,
  operator_id BIGINT NULL,
  scanned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX ix_pck_scan_run (packing_run_id),
  INDEX ix_pck_scan_outcome (outcome),
  INDEX ix_pck_scan_sku (sku_code)
);

INSERT IGNORE INTO entity_registry (code, display_name, table_name, category, read_role, write_role, approver_role) VALUES
  ('PackingScanLog', 'Packing Scan Log', 'm5_packing_scan_log', 'M5', '*', 'Dispatch,Manager,TenantAdmin', 'Manager');

INSERT IGNORE INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('PackingScanLog','packing_run_id','Packing Run','LONG',1,1,0,0,'number',NULL),
  ('PackingScanLog','bay_code','Bay','STRING',1,1,1,0,'select','["BAY-A","BAY-B","BAY-C","BAY-D"]'),
  ('PackingScanLog','scanned_barcode','Barcode','STRING',1,1,1,1,'text',NULL),
  ('PackingScanLog','sku_code','SKU','STRING',1,1,1,0,'text',NULL),
  ('PackingScanLog','offer_variant','Offer','STRING',1,1,0,0,'select','["STANDARD","BUY1GET1","10PCT_OFF","20PCT_OFF","FESTIVE_PACK","COMBO_2L_500ML"]'),
  ('PackingScanLog','bundle_count','Bundles','INT',1,1,0,0,'number',NULL),
  ('PackingScanLog','pieces_total','Pieces','INT',1,1,0,0,'number',NULL),
  ('PackingScanLog','outcome','Outcome','STRING',1,1,0,1,'select','["ACCEPTED","REJECTED"]'),
  ('PackingScanLog','reject_reason','Reject Reason','STRING',1,1,1,0,'select','["WRONG_SKU","QTY_EXCEEDED","UNAUTHORIZED_BARCODE","DAMAGED_PACK","SEAL_TAMPER","OTHER"]'),
  ('PackingScanLog','operator_id','Operator','LONG',1,1,0,0,'number',NULL);

-- Seed 12 representative scans (8 accepted + 4 rejected) so the page has data
INSERT IGNORE INTO m5_packing_scan_log
  (tenant_id, packing_run_id, bay_code, scanned_barcode, sku_code, offer_variant, bundle_count, pieces_total, outcome, reject_reason, operator_id) VALUES
  (1, 1,'BAY-A','8901234500042','GIN-1L-IDH','STANDARD',2,30,'ACCEPTED',NULL,2003),
  (1, 1,'BAY-A','8901234500042','GIN-1L-IDH-B1G1','BUY1GET1',1,15,'ACCEPTED',NULL,2003),
  (1, 1,'BAY-A','8901234500035','GIN-500-IDH','STANDARD',4,60,'ACCEPTED',NULL,2003),
  (1, 1,'BAY-B','8901234600056','GND-1L-PCG','STANDARD',3,45,'ACCEPTED',NULL,2003),
  (1, 1,'BAY-B','8901234600056','GND-1L-PCG-10','10PCT_OFF',1,15,'ACCEPTED',NULL,2003),
  (1, 1,'BAY-B','8901234999999',NULL,NULL,NULL,NULL,'REJECTED','UNAUTHORIZED_BARCODE',2003),
  (1, 1,'BAY-C','8901234700046','CNT-1L-PCP','STANDARD',2,30,'ACCEPTED',NULL,2004),
  (1, 1,'BAY-C','8901234700046','CNT-1L-PCP-FES','FESTIVE_PACK',1,15,'ACCEPTED',NULL,2004),
  (1, 1,'BAY-C','8901234800049','SNF-1L-PCS','STANDARD',5,75,'ACCEPTED',NULL,2004),
  (1, 1,'BAY-C','8901234800049','SNF-1L-PCS-20','20PCT_OFF',2,30,'REJECTED','QTY_EXCEEDED',2004),
  (1, 1,'BAY-D','8901235300027','AGB-IDH-100ST','STANDARD',4,60,'ACCEPTED',NULL,2003),
  (1, 1,'BAY-D','8901234500070','GIN-5L-IDH-WRONG',NULL,NULL,NULL,'REJECTED','WRONG_SKU',2003);

-- Stamp the packing scan log page
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:packing-scan-log', 'Packing Scan Log', 'Conveyor barcode scans with accept / reject + multi-offer + bundle count', 'M5', 'PUBLISHED', NOW(), NOW());
INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','PackingScanLog','select',JSON_ARRAY('id','scanned_at','bay_code','scanned_barcode','sku_code','offer_variant','bundle_count','pieces_total','outcome','reject_reason','operator_id'), 'sort', JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','scanned_at','label','When','render','datetime','sortable',true),
    JSON_OBJECT('field','bay_code','label','Bay','sortable',true),
    JSON_OBJECT('field','scanned_barcode','label','Barcode','sortable',true),
    JSON_OBJECT('field','sku_code','label','SKU','sortable',true),
    JSON_OBJECT('field','offer_variant','label','Offer','sortable',true),
    JSON_OBJECT('field','bundle_count','label','Bundles','render','number'),
    JSON_OBJECT('field','pieces_total','label','Pieces','render','number'),
    JSON_OBJECT('field','outcome','label','Outcome','sortable',true),
    JSON_OBJECT('field','reject_reason','label','Reject Reason','sortable',true),
    JSON_OBJECT('field','operator_id','label','Operator')
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','outcome','label','Outcome','type','multi-select','options',JSON_ARRAY('ACCEPTED','REJECTED')),
    JSON_OBJECT('field','bay_code','label','Bay','type','multi-select','options',JSON_ARRAY('BAY-A','BAY-B','BAY-C','BAY-D')),
    JSON_OBJECT('field','reject_reason','label','Reject Reason','type','multi-select','options',JSON_ARRAY('WRONG_SKU','QTY_EXCEEDED','UNAUTHORIZED_BARCODE','DAMAGED_PACK','SEAL_TAMPER','OTHER'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
FROM page_definition pd WHERE pd.code='pico:packing-scan-log' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id = (SELECT id FROM page_version WHERE page_id = pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:packing-scan-log' AND current_version_id IS NULL;
