-- =====================================================================
-- V13 — Final use-case batch on the M0 substrate.
--
--   1. Bill of Materials (BOM)         — recipe per product (item → input mix)
--   2. Quality Plan + Sample Test      — recurring QC schedule + results
--   3. Asset Master + Maintenance      — equipment + breakdown / PM workflow
--   4. Energy / Utility Reading        — meter readings per shift
--   5. Customer + Vendor KYC           — onboarding documents + verification
--
-- Each follows the same SQL pattern: table + workflow (where applicable) +
-- entity registry + pages. No new React.
-- =====================================================================

-- ===== 1. BILL OF MATERIALS ==========================================

CREATE TABLE IF NOT EXISTS m5_bom (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  bom_code VARCHAR(64) NOT NULL,
  product_code VARCHAR(64) NOT NULL,
  product_name VARCHAR(256) NULL,
  yield_uom VARCHAR(16) NOT NULL,
  yield_qty DECIMAL(14,3) NOT NULL,
  version INT NOT NULL DEFAULT 1,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  effective_from DATE NULL,
  notes TEXT NULL,
  UNIQUE KEY uq_bom (tenant_id, bom_code, version),
  KEY idx_bom_product (product_code, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_bom_line (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  bom_id BIGINT NOT NULL,
  input_item_code VARCHAR(64) NOT NULL,
  input_item_name VARCHAR(256) NULL,
  qty_per_yield DECIMAL(14,4) NOT NULL,
  uom VARCHAR(16) NOT NULL,
  loss_pct DECIMAL(5,2) NULL,
  CONSTRAINT fk_boml_bom FOREIGN KEY (bom_id) REFERENCES m5_bom(id),
  KEY idx_boml_input (input_item_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_bom (tenant_id, bom_code, product_code, product_name, yield_uom, yield_qty, status, effective_from) VALUES
  (1,'BOM-PET-5L-SUN','PET-5L-SUN','5L Sunflower PET','PCS',1.000,'ACTIVE','2026-06-01'),
  (1,'BOM-PET-1L-GND','PET-1L-GND','1L Groundnut PET','PCS',1.000,'ACTIVE','2026-06-01'),
  (1,'BOM-OIL-SUN-RFD','OIL-SUN-RFD','Refined Sunflower Oil','KG',1.000,'ACTIVE','2026-06-01');

INSERT IGNORE INTO m5_bom_line (bom_id, input_item_code, input_item_name, qty_per_yield, uom, loss_pct) VALUES
  ((SELECT id FROM m5_bom WHERE bom_code='BOM-PET-5L-SUN'),'OIL-SUN-RFD','Refined Sunflower Oil', 5.0000,'KG', 0.50),
  ((SELECT id FROM m5_bom WHERE bom_code='BOM-PET-5L-SUN'),'PET-BOTTLE-5L','5L PET Bottle',         1.0000,'PCS',1.00),
  ((SELECT id FROM m5_bom WHERE bom_code='BOM-PET-5L-SUN'),'CAP-25MM-WH','25mm White Cap',         1.0000,'PCS',2.00),
  ((SELECT id FROM m5_bom WHERE bom_code='BOM-PET-1L-GND'),'OIL-GND-RFD','Refined Groundnut Oil', 1.0000,'KG', 0.50),
  ((SELECT id FROM m5_bom WHERE bom_code='BOM-PET-1L-GND'),'PET-BOTTLE-1L','1L PET Bottle',        1.0000,'PCS',1.00),
  ((SELECT id FROM m5_bom WHERE bom_code='BOM-PET-1L-GND'),'CAP-25MM-WH','25mm White Cap',         1.0000,'PCS',2.00),
  ((SELECT id FROM m5_bom WHERE bom_code='BOM-OIL-SUN-RFD'),'OIL-SUN-RAW','Raw Sunflower Oil',     1.0500,'KG', 5.00);

-- ===== 2. QUALITY PLAN + SAMPLE TEST =================================

CREATE TABLE IF NOT EXISTS m5_quality_plan (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  plan_code VARCHAR(64) NOT NULL,
  applies_to_item VARCHAR(64) NULL,
  applies_to_stage VARCHAR(32) NULL,
  sample_size INT NOT NULL DEFAULT 1,
  frequency VARCHAR(32) NOT NULL,
  parameters_json TEXT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uq_qplan (tenant_id, plan_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_quality_test (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  test_number VARCHAR(32) NOT NULL,
  plan_id BIGINT NULL,
  taken_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  item_code VARCHAR(64) NULL,
  lot_no VARCHAR(64) NULL,
  bin_code VARCHAR(64) NULL,
  result VARCHAR(16) NOT NULL DEFAULT 'PASS',
  parameters_json TEXT NULL,
  notes TEXT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'CLOSED',
  workflow_instance_id BIGINT NULL,
  UNIQUE KEY uq_qtest (tenant_id, test_number),
  KEY idx_qtest_item (item_code, taken_at)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_quality_plan (tenant_id, plan_code, applies_to_item, applies_to_stage, sample_size, frequency, parameters_json, status) VALUES
  (1,'QP-OIL-INWARD','OIL-SUN-RAW','INWARD',3,'PER_BATCH','{"colour":"<5 Lovibond","density":"0.918-0.923","FFA":"<0.5"}','ACTIVE'),
  (1,'QP-OIL-DISPATCH','OIL-SUN-RFD','DISPATCH',2,'PER_BATCH','{"colour":"<2 Lovibond","density":"0.918-0.923"}','ACTIVE'),
  (1,'QP-PACKED-FG','PET-5L-SUN','POST_PACK',5,'PER_SHIFT','{"sealCheck":"PASS","weightG":"4995-5005"}','ACTIVE');

INSERT IGNORE INTO m5_quality_test (tenant_id, test_number, plan_id, item_code, lot_no, result, parameters_json, status) VALUES
  (1,'QT-2026-001',(SELECT id FROM m5_quality_plan WHERE plan_code='QP-OIL-INWARD'),'OIL-SUN-RAW','LOT-SUN-260607-A','PASS','{"colour":3,"density":0.920,"FFA":0.35}','CLOSED'),
  (1,'QT-2026-002',(SELECT id FROM m5_quality_plan WHERE plan_code='QP-OIL-INWARD'),'OIL-GND-RAW','LOT-GND-260605-A','PASS','{"colour":4,"density":0.917,"FFA":0.41}','CLOSED'),
  (1,'QT-2026-003',(SELECT id FROM m5_quality_plan WHERE plan_code='QP-PACKED-FG'),'PET-5L-SUN','LOT-PK-260608-A','FAIL','{"sealCheck":"FAIL","weightG":5012}','CLOSED');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.QualityTest',1,'PUBLISHED','{"code":"M5.QualityTest","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"QC"},{"code":"SAMPLED","assignedRole":"QC"},{"code":"TESTED","assignedRole":"QC"},{"code":"CLOSED","terminal":true}],"transitions":[{"from":"DRAFT","to":"SAMPLED","trigger":"page.submitted","pageCode":"pico:qt-sample"},{"from":"SAMPLED","to":"TESTED","trigger":"page.submitted","pageCode":"pico:qt-test"},{"from":"TESTED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:qt-close"}]}');

-- ===== 3. ASSET MASTER + MAINTENANCE =================================

CREATE TABLE IF NOT EXISTS m5_asset (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  asset_code VARCHAR(32) NOT NULL,
  asset_name VARCHAR(256) NOT NULL,
  category VARCHAR(32) NULL,
  location VARCHAR(64) NULL,
  installed_on DATE NULL,
  serial_no VARCHAR(64) NULL,
  manufacturer VARCHAR(128) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uq_asset (tenant_id, asset_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_maintenance_order (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  mo_number VARCHAR(32) NOT NULL,
  asset_id BIGINT NULL,
  kind VARCHAR(16) NOT NULL,
  reported_by BIGINT NULL,
  reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  description TEXT NULL,
  priority VARCHAR(8) NOT NULL DEFAULT 'P3',
  status VARCHAR(32) NOT NULL DEFAULT 'REPORTED',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  closed_at TIMESTAMP NULL,
  UNIQUE KEY uq_mo_number (tenant_id, mo_number),
  KEY idx_mo_status (status, priority)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_asset (tenant_id, asset_code, asset_name, category, location, installed_on, manufacturer, status) VALUES
  (1,'WB-01','Weighbridge #1','WEIGHBRIDGE','G-MAIN','2022-04-15','Avery Weigh-Tronix','ACTIVE'),
  (1,'WB-02','Weighbridge #2','WEIGHBRIDGE','G-RM','2022-04-15','Avery Weigh-Tronix','ACTIVE'),
  (1,'CONV-PACK-1','Packing Conveyor 1','CONVEYOR','LINE-1','2023-01-10','SSI Schaefer','ACTIVE'),
  (1,'PUMP-T-OIL-03','Tank T-OIL-03 Pump','PUMP','T-OIL-03','2023-03-22','Grundfos','ACTIVE');

INSERT IGNORE INTO m5_maintenance_order (tenant_id, mo_number, asset_id, kind, description, priority, status) VALUES
  (1,'MO-2026-001',(SELECT id FROM m5_asset WHERE asset_code='WB-01'),'BREAKDOWN','Display showing stale reading','P1','IN_PROGRESS'),
  (1,'MO-2026-002',(SELECT id FROM m5_asset WHERE asset_code='CONV-PACK-1'),'PM','Quarterly belt + bearing service','P3','REPORTED');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.MaintenanceOrder',1,'PUBLISHED','{"code":"M5.MaintenanceOrder","version":1,"initialState":"REPORTED","states":[{"code":"REPORTED","assignedRole":"Manager"},{"code":"ASSIGNED","assignedRole":"UnloadingSupervisor"},{"code":"IN_PROGRESS","assignedRole":"UnloadingSupervisor"},{"code":"AWAITING_PARTS","assignedRole":"Dispatch"},{"code":"RESOLVED","assignedRole":"Manager"},{"code":"CLOSED","terminal":true},{"code":"REJECTED","terminal":true}],"transitions":[{"from":"REPORTED","to":"ASSIGNED","trigger":"page.submitted","pageCode":"pico:mo-assign"},{"from":"REPORTED","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:mo-assign","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"ASSIGNED","to":"IN_PROGRESS","trigger":"page.submitted","pageCode":"pico:mo-start"},{"from":"IN_PROGRESS","to":"AWAITING_PARTS","trigger":"page.submitted","pageCode":"pico:mo-parts"},{"from":"AWAITING_PARTS","to":"IN_PROGRESS","trigger":"page.submitted","pageCode":"pico:mo-parts","guard":"#submitted[''status''] == ''RESUMED''"},{"from":"IN_PROGRESS","to":"RESOLVED","trigger":"page.submitted","pageCode":"pico:mo-resolve"},{"from":"RESOLVED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:mo-close"}]}');

-- ===== 4. ENERGY / UTILITY READING ===================================

CREATE TABLE IF NOT EXISTS m5_energy_reading (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  meter_code VARCHAR(32) NOT NULL,
  meter_kind VARCHAR(16) NOT NULL,
  shift_code VARCHAR(8) NULL,
  recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  start_reading DECIMAL(14,3) NULL,
  end_reading DECIMAL(14,3) NULL,
  consumption DECIMAL(14,3) NULL,
  uom VARCHAR(16) NOT NULL,
  KEY idx_em_meter_date (meter_code, recorded_at)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_energy_reading (tenant_id, meter_code, meter_kind, shift_code, recorded_at, start_reading, end_reading, consumption, uom) VALUES
  (1,'EM-MAIN-01','ELECTRIC','A','2026-06-08 06:00:00', 123456.000, 123890.000, 434.000,'kWh'),
  (1,'EM-MAIN-01','ELECTRIC','B','2026-06-08 14:00:00', 123890.000, 124350.000, 460.000,'kWh'),
  (1,'EM-MAIN-01','ELECTRIC','C','2026-06-08 22:00:00', 124350.000, 124820.000, 470.000,'kWh'),
  (1,'WM-MAIN-01','WATER',   'A','2026-06-08 06:00:00',  45200.000,  45285.000,  85.000,'KL'),
  (1,'WM-MAIN-01','WATER',   'B','2026-06-08 14:00:00',  45285.000,  45378.000,  93.000,'KL'),
  (1,'STEAM-01',  'STEAM',   'A','2026-06-08 06:00:00',   8210.000,   8265.000,  55.000,'TON');

-- ===== 5. CUSTOMER + VENDOR KYC =====================================

CREATE TABLE IF NOT EXISTS m5_kyc_record (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  party_kind VARCHAR(16) NOT NULL,
  party_id BIGINT NULL,
  party_name VARCHAR(256) NOT NULL,
  gstin VARCHAR(16) NULL,
  pan VARCHAR(16) NULL,
  fssai_no VARCHAR(20) NULL,
  bank_account_masked VARCHAR(32) NULL,
  ifsc VARCHAR(16) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  verified_at TIMESTAMP NULL,
  KEY idx_kyc_party (party_kind, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_kyc_record (tenant_id, party_kind, party_name, gstin, pan, fssai_no, status) VALUES
  (1,'VENDOR','TESTRO Industries','33ABCDE1234F1Z5','ABCDE1234F','12345678901234','VERIFIED'),
  (1,'VENDOR','Iyer Lubricants','33XYZAB6789K2P3','XYZAB6789K','98765432109876','PENDING'),
  (1,'CUSTOMER','Coimbatore Foods Pvt Ltd','33ABCDE9999F1Z9','PQRST9999F',NULL,'VERIFIED'),
  (1,'CUSTOMER','Madurai Wholesalers','33ABCDE5678F1Z1','UVWXY5678F',NULL,'IN_REVIEW');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.KycOnboarding',1,'PUBLISHED','{"code":"M5.KycOnboarding","version":1,"initialState":"PENDING","states":[{"code":"PENDING","assignedRole":"Dispatch"},{"code":"DOCS_SUBMITTED","assignedRole":"Dispatch"},{"code":"IN_REVIEW","assignedRole":"Manager"},{"code":"VERIFIED","terminal":true},{"code":"REJECTED","terminal":true}],"transitions":[{"from":"PENDING","to":"DOCS_SUBMITTED","trigger":"page.submitted","pageCode":"pico:kyc-docs"},{"from":"DOCS_SUBMITTED","to":"IN_REVIEW","trigger":"page.submitted","pageCode":"pico:kyc-submit-review"},{"from":"IN_REVIEW","to":"VERIFIED","trigger":"page.submitted","pageCode":"pico:kyc-verify","guard":"#submitted[''status''] == ''VERIFIED''"},{"from":"IN_REVIEW","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:kyc-verify","guard":"#submitted[''status''] == ''REJECTED''"}]}');

-- ===== 6. ENTITY REGISTRY ===========================================

INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'BillOfMaterials','Bill of Materials','m5_bom','BOM header (product -> recipe).','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='BillOfMaterials');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'QualityPlan','Quality Plan','m5_quality_plan','Recurring QC plan per item+stage.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='QualityPlan');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'QualityTest','Quality Test','m5_quality_test','Single sample test result.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='QualityTest');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'Asset','Asset','m5_asset','Plant asset master.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='Asset');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'MaintenanceOrder','Maintenance Order','m5_maintenance_order','Breakdown + PM order.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='MaintenanceOrder');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'EnergyReading','Energy Reading','m5_energy_reading','Per-shift utility consumption.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='EnergyReading');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'KycRecord','KYC Record','m5_kyc_record','Vendor + customer KYC.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='KycRecord');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'BillOfMaterials','bom_code','BOM #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='BillOfMaterials' AND field_code='bom_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'BillOfMaterials','product_code','Product','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='BillOfMaterials' AND field_code='product_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'BillOfMaterials','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='BillOfMaterials' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'BillOfMaterials','yield_qty','Yield','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='BillOfMaterials' AND field_code='yield_qty');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'QualityPlan','plan_code','Plan #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QualityPlan' AND field_code='plan_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'QualityPlan','applies_to_item','Item','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QualityPlan' AND field_code='applies_to_item');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'QualityPlan','applies_to_stage','Stage','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QualityPlan' AND field_code='applies_to_stage');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'QualityPlan','frequency','Frequency','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QualityPlan' AND field_code='frequency');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'QualityTest','test_number','Test #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QualityTest' AND field_code='test_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'QualityTest','item_code','Item','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QualityTest' AND field_code='item_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'QualityTest','lot_no','Lot','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QualityTest' AND field_code='lot_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'QualityTest','result','Result','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QualityTest' AND field_code='result');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'QualityTest','taken_at','Taken','DATETIME',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QualityTest' AND field_code='taken_at');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Asset','asset_code','Code','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Asset' AND field_code='asset_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Asset','asset_name','Name','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Asset' AND field_code='asset_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Asset','category','Cat','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Asset' AND field_code='category');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Asset','location','Location','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Asset' AND field_code='location');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Asset','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Asset' AND field_code='status');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaintenanceOrder','mo_number','MO #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaintenanceOrder' AND field_code='mo_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaintenanceOrder','kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaintenanceOrder' AND field_code='kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaintenanceOrder','priority','Priority','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaintenanceOrder' AND field_code='priority');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaintenanceOrder','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaintenanceOrder' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'MaintenanceOrder','reported_at','Reported','DATETIME',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='MaintenanceOrder' AND field_code='reported_at');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'EnergyReading','meter_code','Meter','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='EnergyReading' AND field_code='meter_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'EnergyReading','meter_kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='EnergyReading' AND field_code='meter_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'EnergyReading','shift_code','Shift','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='EnergyReading' AND field_code='shift_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'EnergyReading','consumption','Consumption','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='EnergyReading' AND field_code='consumption');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'EnergyReading','recorded_at','When','DATETIME',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='EnergyReading' AND field_code='recorded_at');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'KycRecord','party_kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='KycRecord' AND field_code='party_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'KycRecord','party_name','Name','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='KycRecord' AND field_code='party_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'KycRecord','gstin','GSTIN','STRING',TRUE,FALSE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='KycRecord' AND field_code='gstin');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'KycRecord','pan','PAN','STRING',TRUE,FALSE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='KycRecord' AND field_code='pan');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'KycRecord','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='KycRecord' AND field_code='status');

-- ===== 7. PAGES =====================================================

INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:bom-list','Bills of Materials','BOMs per product.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:bom-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:bom-create','New BOM','Create a BOM + lines.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:bom-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:qp-list','Quality Plans','QC plans per item+stage.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:qp-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:qt-list','Quality Tests','Sample test results.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:qt-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:qt-sample','QT — Take Sample','Record sample taken.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:qt-sample');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:qt-test','QT — Test Sample','Run sample analysis.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:qt-test');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:qt-close','QT — Close','Close test record.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:qt-close');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:asset-list','Asset Master','Plant assets.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:asset-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mo-list','Maintenance Orders','Breakdown + PM orders.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mo-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mo-assign','MO — Assign','Manager assigns/rejects.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mo-assign');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mo-start','MO — Start Work','Technician starts.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mo-start');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mo-parts','MO — Parts','Awaiting parts / resume.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mo-parts');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mo-resolve','MO — Resolve','Technician resolves.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mo-resolve');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:mo-close','MO — Close','Manager closes.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:mo-close');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:energy-list','Energy Readings','Per-shift utility consumption.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:energy-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:energy-capture','Energy — Capture Reading','Log start+end meter reading.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:energy-capture');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:kyc-list','KYC Records','Customer + vendor onboarding.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:kyc-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:kyc-docs','KYC — Upload Docs','GSTIN / PAN / FSSAI / Bank.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:kyc-docs');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:kyc-submit-review','KYC — Submit for Review','Submit to Manager.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:kyc-submit-review');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:kyc-verify','KYC — Verify/Reject','Manager final decision.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:kyc-verify');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"BillOfMaterials","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","bom_code","product_code","yield_qty","yield_uom","status"]},"columns":[{"field":"bom_code","label":"BOM #","sticky":"left","sortable":true},{"field":"product_code","label":"Product"},{"field":"yield_qty","label":"Yield","render":"number","align":"right"},{"field":"yield_uom","label":"UoM"},{"field":"status","label":"Status","render":"badge","colorMap":{"ACTIVE":"green","DRAFT":"grey","ARCHIVED":"red"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["ACTIVE","DRAFT","ARCHIVED"]}}],"sort":{"default":[{"field":"bom_code","dir":"asc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Bills of Materials"}}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:bom-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New BOM","fields":[{"kind":"FormField","type":"text","name":"bomCode","label":"BOM Code"},{"kind":"FormField","type":"text","name":"productCode","label":"Product Code"},{"kind":"FormField","type":"number","name":"yieldQty","label":"Yield Qty"},{"kind":"FormField","type":"text","name":"yieldUom","label":"Yield UoM"},{"kind":"WorkflowActionButton","label":"Save BOM","action":"save","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:bom-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"QualityPlan","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","plan_code","applies_to_item","applies_to_stage","frequency","sample_size","status"]},"columns":[{"field":"plan_code","label":"Plan #","sticky":"left","sortable":true},{"field":"applies_to_item","label":"Item"},{"field":"applies_to_stage","label":"Stage","render":"badge"},{"field":"frequency","label":"Freq","render":"badge"},{"field":"sample_size","label":"N","align":"right"},{"field":"status","label":"Status","render":"badge"}],"sort":{"default":[{"field":"plan_code","dir":"asc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Quality Plans"}}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:qp-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"QualityTest","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","test_number","item_code","lot_no","taken_at","result"]},"columns":[{"field":"test_number","label":"Test #","sticky":"left","sortable":true},{"field":"item_code","label":"Item"},{"field":"lot_no","label":"Lot"},{"field":"taken_at","label":"Taken","render":"datetime"},{"field":"result","label":"Result","render":"badge","colorMap":{"PASS":"green","FAIL":"red","BORDERLINE":"amber"}}],"filters":[{"field":"result","label":"Result","type":"multi-select","optionsSource":{"type":"static","options":["PASS","FAIL","BORDERLINE"]}}],"sort":{"default":[{"field":"taken_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Quality Tests"}}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:qt-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Take Sample","fields":[{"kind":"FormField","type":"text","name":"itemCode","label":"Item"},{"kind":"FormField","type":"text","name":"lotNo","label":"Lot"},{"kind":"FormField","type":"text","name":"binCode","label":"Bin"},{"kind":"FormField","type":"number","name":"sampleSize","label":"Sample Size"},{"kind":"WorkflowActionButton","label":"Mark Sampled","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:qt-sample' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Run Analysis","fields":[{"kind":"FormField","type":"text","name":"colour","label":"Colour"},{"kind":"FormField","type":"text","name":"density","label":"Density"},{"kind":"FormField","type":"text","name":"FFA","label":"FFA"},{"kind":"FormField","type":"select","name":"result","label":"Result","options":[{"label":"PASS","value":"PASS"},{"label":"FAIL","value":"FAIL"},{"label":"BORDERLINE","value":"BORDERLINE"}]},{"kind":"WorkflowActionButton","label":"Submit Result","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:qt-test' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close Test","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Notes"},{"kind":"WorkflowActionButton","label":"Close","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:qt-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Asset","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","asset_code","asset_name","category","location","status"]},"columns":[{"field":"asset_code","label":"Code","sticky":"left","sortable":true},{"field":"asset_name","label":"Name"},{"field":"category","label":"Category","render":"badge","colorMap":{"WEIGHBRIDGE":"blue","CONVEYOR":"amber","PUMP":"purple","TANK":"green"}},{"field":"location","label":"Location"},{"field":"status","label":"Status","render":"badge","colorMap":{"ACTIVE":"green","DOWN":"red","RETIRED":"grey"}}],"filters":[{"field":"category","label":"Category","type":"multi-select","optionsSource":{"type":"static","options":["WEIGHBRIDGE","CONVEYOR","PUMP","TANK"]}},{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["ACTIVE","DOWN","RETIRED"]}}],"sort":{"default":[{"field":"asset_code","dir":"asc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Asset Master"}}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:asset-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"MaintenanceOrder","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","mo_number","kind","priority","status","reported_at"]},"columns":[{"field":"mo_number","label":"MO #","sticky":"left","sortable":true},{"field":"kind","label":"Kind","render":"badge","colorMap":{"BREAKDOWN":"red","PM":"blue","CALIBRATION":"amber"}},{"field":"priority","label":"Priority","render":"badge","colorMap":{"P1":"red","P2":"amber","P3":"blue","P4":"grey"}},{"field":"status","label":"Status","render":"badge","colorMap":{"REPORTED":"grey","ASSIGNED":"blue","IN_PROGRESS":"amber","AWAITING_PARTS":"amber","RESOLVED":"green","CLOSED":"green","REJECTED":"red"}},{"field":"reported_at","label":"Reported","render":"datetime"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["REPORTED","ASSIGNED","IN_PROGRESS","AWAITING_PARTS","RESOLVED","CLOSED","REJECTED"]}},{"field":"priority","label":"Priority","type":"multi-select","optionsSource":{"type":"static","options":["P1","P2","P3","P4"]}}],"sort":{"default":[{"field":"reported_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Maintenance Orders"}}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mo-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Assign Technician","fields":[{"kind":"FormField","type":"text","name":"assignedTo","label":"Technician"},{"kind":"FormField","type":"select","name":"status","label":"Decision","options":[{"label":"ASSIGNED","value":"ASSIGNED"},{"label":"REJECTED","value":"REJECTED"}]},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mo-assign' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Start Work","fields":[{"kind":"FormField","type":"text","name":"startedAt","label":"Started At"},{"kind":"WorkflowActionButton","label":"Start","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mo-start' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Parts Status","fields":[{"kind":"FormField","type":"text","name":"partNo","label":"Part #"},{"kind":"FormField","type":"select","name":"status","label":"Status","options":[{"label":"AWAITING","value":"AWAITING"},{"label":"RESUMED","value":"RESUMED"}]},{"kind":"WorkflowActionButton","label":"Update","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mo-parts' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Resolve","fields":[{"kind":"FormField","type":"text","name":"resolution","label":"Resolution"},{"kind":"FormField","type":"text","name":"laborHours","label":"Labour Hours"},{"kind":"WorkflowActionButton","label":"Mark Resolved","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mo-resolve' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close MO","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Notes"},{"kind":"WorkflowActionButton","label":"Close","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:mo-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"EnergyReading","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","meter_code","meter_kind","shift_code","recorded_at","start_reading","end_reading","consumption","uom"]},"columns":[{"field":"recorded_at","label":"When","render":"datetime","sticky":"left","sortable":true},{"field":"meter_code","label":"Meter"},{"field":"meter_kind","label":"Kind","render":"badge","colorMap":{"ELECTRIC":"blue","WATER":"green","STEAM":"amber","DIESEL":"red"}},{"field":"shift_code","label":"Shift","render":"badge","colorMap":{"A":"blue","B":"amber","C":"purple"}},{"field":"start_reading","label":"Start","render":"number","align":"right"},{"field":"end_reading","label":"End","render":"number","align":"right"},{"field":"consumption","label":"Consumption","render":"number","align":"right"},{"field":"uom","label":"UoM"}],"filters":[{"field":"meter_kind","label":"Kind","type":"multi-select","optionsSource":{"type":"static","options":["ELECTRIC","WATER","STEAM","DIESEL"]}},{"field":"shift_code","label":"Shift","type":"multi-select","optionsSource":{"type":"static","options":["A","B","C"]}}],"sort":{"default":[{"field":"recorded_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Energy / Utility Readings"}}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:energy-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Capture Meter Reading","fields":[{"kind":"FormField","type":"text","name":"meterCode","label":"Meter Code"},{"kind":"FormField","type":"select","name":"meterKind","label":"Kind","options":[{"label":"ELECTRIC","value":"ELECTRIC"},{"label":"WATER","value":"WATER"},{"label":"STEAM","value":"STEAM"},{"label":"DIESEL","value":"DIESEL"}]},{"kind":"FormField","type":"select","name":"shiftCode","label":"Shift","options":[{"label":"A","value":"A"},{"label":"B","value":"B"},{"label":"C","value":"C"}]},{"kind":"FormField","type":"number","name":"startReading","label":"Start Reading"},{"kind":"FormField","type":"number","name":"endReading","label":"End Reading"},{"kind":"FormField","type":"text","name":"uom","label":"UoM"},{"kind":"WorkflowActionButton","label":"Save Reading","action":"save","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:energy-capture' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"KycRecord","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","party_kind","party_name","gstin","pan","status"]},"columns":[{"field":"party_name","label":"Name","sticky":"left","sortable":true},{"field":"party_kind","label":"Kind","render":"badge","colorMap":{"CUSTOMER":"blue","VENDOR":"amber","BROKER":"purple"}},{"field":"gstin","label":"GSTIN"},{"field":"pan","label":"PAN"},{"field":"status","label":"Status","render":"badge","colorMap":{"PENDING":"grey","DOCS_SUBMITTED":"blue","IN_REVIEW":"amber","VERIFIED":"green","REJECTED":"red"}}],"filters":[{"field":"party_kind","label":"Kind","type":"multi-select","optionsSource":{"type":"static","options":["CUSTOMER","VENDOR","BROKER"]}},{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["PENDING","DOCS_SUBMITTED","IN_REVIEW","VERIFIED","REJECTED"]}}],"sort":{"default":[{"field":"party_name","dir":"asc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"KYC Records"}}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:kyc-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Upload KYC Documents","fields":[{"kind":"FormField","type":"select","name":"partyKind","label":"Party Kind","options":[{"label":"CUSTOMER","value":"CUSTOMER"},{"label":"VENDOR","value":"VENDOR"},{"label":"BROKER","value":"BROKER"}]},{"kind":"FormField","type":"text","name":"partyName","label":"Party Name"},{"kind":"FormField","type":"text","name":"gstin","label":"GSTIN"},{"kind":"FormField","type":"text","name":"pan","label":"PAN"},{"kind":"FormField","type":"text","name":"fssai","label":"FSSAI #"},{"kind":"FormField","type":"text","name":"bankAccount","label":"Bank Account"},{"kind":"FormField","type":"text","name":"ifsc","label":"IFSC"},{"kind":"WorkflowActionButton","label":"Submit Docs","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:kyc-docs' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Submit for Review","fields":[{"kind":"FormField","type":"text","name":"submitNotes","label":"Notes for Reviewer"},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:kyc-submit-review' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Final KYC Decision","fields":[{"kind":"FormField","type":"text","name":"verificationNotes","label":"Verification Notes"},{"kind":"FormField","type":"select","name":"status","label":"Decision","options":[{"label":"VERIFIED","value":"VERIFIED"},{"label":"REJECTED","value":"REJECTED"}]},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}', 'V13', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:kyc-verify' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code IN ('pico:bom-list','pico:bom-create','pico:qp-list','pico:qt-list','pico:qt-sample','pico:qt-test','pico:qt-close','pico:asset-list','pico:mo-list','pico:mo-assign','pico:mo-start','pico:mo-parts','pico:mo-resolve','pico:mo-close','pico:energy-list','pico:energy-capture','pico:kyc-list','pico:kyc-docs','pico:kyc-submit-review','pico:kyc-verify') AND current_version_id IS NULL;
