-- =====================================================================
-- V26 — GRN comprehensive: lorry / vehicle + QC parameters + inspection
--       audit + cycle-count audit + audit log viewer entity.
--
-- User: 'inward missing the lorry details, qc data, inspections.. auditing'
-- =====================================================================

-- 1. Extend goods_receipt_note with lorry + receipt-time + audit columns
ALTER TABLE goods_receipt_note
  ADD COLUMN IF NOT EXISTS vendor_name VARCHAR(160) NULL AFTER vendor_id,
  ADD COLUMN IF NOT EXISTS po_number VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS invoice_date DATE NULL,
  ADD COLUMN IF NOT EXISTS invoice_value DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS challan_number VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS ewb_no VARCHAR(32) NULL,
  -- Vehicle / driver
  ADD COLUMN IF NOT EXISTS vehicle_no VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS vehicle_type VARCHAR(16) NULL,           -- TANKER/TRUCK/MINI/CONTAINER
  ADD COLUMN IF NOT EXISTS driver_name VARCHAR(96) NULL,
  ADD COLUMN IF NOT EXISTS driver_phone VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS driver_dl_no VARCHAR(24) NULL,
  ADD COLUMN IF NOT EXISTS transporter_name VARCHAR(96) NULL,
  ADD COLUMN IF NOT EXISTS gate_in_time DATETIME NULL,
  ADD COLUMN IF NOT EXISTS gate_out_time DATETIME NULL,
  -- Weighbridge
  ADD COLUMN IF NOT EXISTS weighbridge_in_kg DECIMAL(12,2) NULL,    -- loaded
  ADD COLUMN IF NOT EXISTS weighbridge_out_kg DECIMAL(12,2) NULL,   -- empty
  ADD COLUMN IF NOT EXISTS weighbridge_net_kg DECIMAL(12,2) GENERATED ALWAYS AS (COALESCE(weighbridge_in_kg,0) - COALESCE(weighbridge_out_kg,0)) STORED,
  ADD COLUMN IF NOT EXISTS weighbridge_slip_no VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS weighbridge_operator VARCHAR(64) NULL,
  -- Container / seal
  ADD COLUMN IF NOT EXISTS seal_no VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS seal_intact TINYINT(1) NULL,
  ADD COLUMN IF NOT EXISTS container_no VARCHAR(24) NULL,
  ADD COLUMN IF NOT EXISTS pkg_count INT NULL,
  ADD COLUMN IF NOT EXISTS pkg_condition VARCHAR(16) NULL,          -- GOOD/DAMAGED/MIXED
  -- Documents collected (yes/no checklist)
  ADD COLUMN IF NOT EXISTS doc_invoice_recd TINYINT(1) NULL,
  ADD COLUMN IF NOT EXISTS doc_lr_recd TINYINT(1) NULL,
  ADD COLUMN IF NOT EXISTS doc_ewb_recd TINYINT(1) NULL,
  ADD COLUMN IF NOT EXISTS doc_test_cert_recd TINYINT(1) NULL,
  ADD COLUMN IF NOT EXISTS doc_msds_recd TINYINT(1) NULL,
  -- Unloading
  ADD COLUMN IF NOT EXISTS unloading_started_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS unloading_completed_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS unloading_supervisor_emp_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS unloading_remarks TEXT NULL,
  -- QC overall
  ADD COLUMN IF NOT EXISTS qc_required TINYINT(1) NOT NULL DEFAULT 1,
  ADD COLUMN IF NOT EXISTS qc_lab_tech VARCHAR(96) NULL,
  ADD COLUMN IF NOT EXISTS qc_sample_drawn_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS qc_completed_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS qc_verdict VARCHAR(16) NULL,              -- ACCEPTED/REJECTED/CONDITIONAL/RETEST
  ADD COLUMN IF NOT EXISTS qc_rejection_reason VARCHAR(256) NULL,
  ADD COLUMN IF NOT EXISTS qc_certificate_no VARCHAR(32) NULL,
  -- Manager override
  ADD COLUMN IF NOT EXISTS manager_override_required TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS manager_override_reason VARCHAR(256) NULL,
  ADD COLUMN IF NOT EXISTS manager_override_by_emp_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS manager_override_at DATETIME NULL,
  -- Storage
  ADD COLUMN IF NOT EXISTS stored_in_warehouse VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS storage_complete_at DATETIME NULL;

-- Extend lines with batch + MFG/EXP + per-line QC verdict
ALTER TABLE goods_receipt_line
  ADD COLUMN IF NOT EXISTS qty_invoiced DECIMAL(12,3) NULL AFTER qty_received,
  ADD COLUMN IF NOT EXISTS qty_accepted DECIMAL(12,3) NULL,
  ADD COLUMN IF NOT EXISTS qty_rejected DECIMAL(12,3) NULL,
  ADD COLUMN IF NOT EXISTS qty_variance DECIMAL(12,3) GENERATED ALWAYS AS (COALESCE(qty_received,0) - COALESCE(qty_invoiced,0)) STORED,
  ADD COLUMN IF NOT EXISTS rate DECIMAL(12,2) NULL,
  ADD COLUMN IF NOT EXISTS hsn_code VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS batch_no VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS mfg_date DATE NULL,
  ADD COLUMN IF NOT EXISTS exp_date DATE NULL,
  ADD COLUMN IF NOT EXISTS shelf_life_days INT NULL,
  ADD COLUMN IF NOT EXISTS bin_assigned VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS pallet_no VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS temperature_at_receipt DECIMAL(5,2) NULL;

-- 2. QC test parameters per GRN line (one row per parameter measured)
CREATE TABLE IF NOT EXISTS m5_qc_test_result (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  source_type VARCHAR(16) NOT NULL,             -- GRN_LINE/PRODUCTION/FG/RETURN
  source_ref_id BIGINT NOT NULL,                 -- the line id
  item_code VARCHAR(64) NULL,
  parameter VARCHAR(64) NOT NULL,                -- e.g. FFA / MOISTURE / DENSITY / COLOR_LOVI / IODINE_VALUE
  method VARCHAR(32) NULL,                       -- AOCS/IS/ASTM method code
  unit VARCHAR(16) NULL,                          -- %, kg/m3, etc.
  spec_min DECIMAL(12,4) NULL,
  spec_max DECIMAL(12,4) NULL,
  measured_value DECIMAL(12,4) NULL,
  pass_fail VARCHAR(8) NULL,                      -- PASS/FAIL
  tested_at DATETIME NULL,
  tested_by_emp_id BIGINT NULL,
  remarks VARCHAR(256) NULL,
  KEY idx_qc_src (source_type, source_ref_id),
  KEY idx_qc_item (item_code, parameter)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- 3. QC parameter catalog per item (so the form pre-fills which params apply)
CREATE TABLE IF NOT EXISTS m5_qc_parameter_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  item_code VARCHAR(64) NOT NULL,
  parameter VARCHAR(64) NOT NULL,
  method VARCHAR(32) NULL,
  unit VARCHAR(16) NULL,
  spec_min DECIMAL(12,4) NULL,
  spec_max DECIMAL(12,4) NULL,
  is_mandatory TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uq_qc_cat (tenant_id, item_code, parameter)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Edible-oil QC parameters per AOCS / FSSAI (Pico's case)
INSERT IGNORE INTO m5_qc_parameter_catalog (tenant_id, item_code, parameter, method, unit, spec_min, spec_max, is_mandatory) VALUES
(1,'OIL-SUN-RAW','FFA_PCT',         'AOCS Ca 5a-40',  '%',         NULL, 2.00, 1),
(1,'OIL-SUN-RAW','MOISTURE_PCT',    'AOCS Ca 2c-25',  '%',         NULL, 0.20, 1),
(1,'OIL-SUN-RAW','DENSITY_15C',     'IS 548',         'kg/L',      0.91, 0.93, 1),
(1,'OIL-SUN-RAW','COLOR_LOVI_RED',  'AOCS Cc 13b-45', 'lovibond',  NULL, 5.00, 1),
(1,'OIL-SUN-RAW','IODINE_VALUE',    'AOCS Cd 1d-92',  '-',         100,  145,  1),
(1,'OIL-SUN-RAW','PEROXIDE_VALUE',  'AOCS Cd 8b-90',  'meq/kg',    NULL, 10.0, 1),
(1,'OIL-SUN-RAW','SAPONIFICATION',  'AOCS Cd 3-25',   'mg KOH/g',  188,  194,  0),
(1,'OIL-SUN-RAW','UNSAPON_MATTER',  'AOCS Ca 6a-40',  '%',         NULL, 1.50, 0),
(1,'OIL-GND-RAW','FFA_PCT',         'AOCS Ca 5a-40',  '%',         NULL, 2.00, 1),
(1,'OIL-GND-RAW','MOISTURE_PCT',    'AOCS Ca 2c-25',  '%',         NULL, 0.20, 1),
(1,'OIL-GND-RAW','DENSITY_15C',     'IS 548',         'kg/L',      0.91, 0.92, 1),
(1,'OIL-GND-RAW','COLOR_LOVI_RED',  'AOCS Cc 13b-45', 'lovibond',  NULL, 5.50, 1),
(1,'OIL-GND-RAW','IODINE_VALUE',    'AOCS Cd 1d-92',  '-',         84,   100,  1),
(1,'OIL-GND-RAW','PEROXIDE_VALUE',  'AOCS Cd 8b-90',  'meq/kg',    NULL, 10.0, 1),
(1,'OIL-SUN-RFD','FFA_PCT',         'AOCS Ca 5a-40',  '%',         NULL, 0.10, 1),
(1,'OIL-SUN-RFD','COLOR_LOVI_RED',  'AOCS Cc 13b-45', 'lovibond',  NULL, 1.00, 1),
(1,'OIL-SUN-RFD','SMOKE_POINT',     'AOCS Cc 9a-48',  '°C',        220,  NULL, 0),
(1,'OIL-SUN-RFD','MOISTURE_PCT',    'AOCS Ca 2c-25',  '%',         NULL, 0.10, 1),
(1,'OIL-GND-RFD','FFA_PCT',         'AOCS Ca 5a-40',  '%',         NULL, 0.10, 1),
(1,'OIL-GND-RFD','COLOR_LOVI_RED',  'AOCS Cc 13b-45', 'lovibond',  NULL, 1.50, 1);

-- 4. Inspection / Inward Audit findings (separate from QC; safety + compliance checks)
CREATE TABLE IF NOT EXISTS m5_inward_audit (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  grn_id BIGINT NOT NULL,
  audit_type VARCHAR(24) NOT NULL,                -- VEHICLE/SAFETY/HYGIENE/SECURITY/DOCUMENT
  check_item VARCHAR(96) NOT NULL,
  result VARCHAR(8) NOT NULL,                     -- PASS/FAIL/N_A
  observation VARCHAR(256) NULL,
  corrective_action VARCHAR(256) NULL,
  audited_by_emp_id BIGINT NULL,
  audited_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_aud_grn (grn_id, audit_type)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- 5. Cycle counting / stock audit
CREATE TABLE IF NOT EXISTS m5_cycle_count (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  count_no VARCHAR(32) NOT NULL,
  warehouse VARCHAR(32) NULL,
  bin_code VARCHAR(32) NULL,
  item_code VARCHAR(64) NULL,
  lot_no VARCHAR(64) NULL,
  count_date DATE NOT NULL,
  qty_book DECIMAL(14,3) NOT NULL,
  qty_physical DECIMAL(14,3) NULL,
  qty_variance DECIMAL(14,3) GENERATED ALWAYS AS (COALESCE(qty_physical,0) - qty_book) STORED,
  variance_value DECIMAL(14,2) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PLANNED',     -- PLANNED/COUNTING/RECOUNT/RECONCILED
  counted_by_emp_id BIGINT NULL,
  recount_by_emp_id BIGINT NULL,
  approved_by_emp_id BIGINT NULL,
  approved_at DATETIME NULL,
  reason VARCHAR(64) NULL,                            -- ABC_CYCLE/AUDIT/RECEIPT/COMPLAINT
  notes TEXT NULL,
  UNIQUE KEY uq_count (tenant_id, count_no),
  KEY idx_cc_bin (warehouse, bin_code),
  KEY idx_cc_status (status, count_date)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_cycle_count (tenant_id, count_no, warehouse, bin_code, item_code, lot_no, count_date, qty_book, qty_physical, variance_value, status, reason) VALUES
(1,'CC-2026-001','PLANT-1','T-OIL-03','OIL-SUN-RAW','LOT-SUN-260607-A','2026-06-08', 21500.000, 21495.000,  -42.75,'RECONCILED','ABC_CYCLE'),
(1,'CC-2026-002','PLANT-1','RACK-A1', 'CAP-25MM-WH', NULL,              '2026-06-08', 42000.000, 41985.000,   -16.50,'RECONCILED','ABC_CYCLE'),
(1,'CC-2026-003','PLANT-1','T-OIL-17','OIL-GND-RAW','LOT-GND-260608-A','2026-06-08', 18750.000, NULL,        NULL,    'COUNTING', 'RECEIPT');

-- 6. Sample inward audit row tied to existing GRN
INSERT IGNORE INTO m5_inward_audit (tenant_id, grn_id, audit_type, check_item, result, observation, audited_by_emp_id) VALUES
(1, (SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-001'),'VEHICLE',  'Vehicle washed & food-grade tanker', 'PASS', 'Tanker cert valid till 2026-12-31', 1),
(1, (SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-001'),'VEHICLE',  'Seal intact at gate-in',              'PASS', NULL, 1),
(1, (SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-001'),'DOCUMENT', 'Invoice / LR / E-way bill collected', 'PASS', NULL, 1),
(1, (SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-001'),'DOCUMENT', 'Vendor COA (Certificate of Analysis)','PASS', 'Lot LOT-SUN-260607-A', 1),
(1, (SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-001'),'HYGIENE',  'Driver gear / cap / shoes',           'PASS', NULL, 1),
(1, (SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-001'),'SAFETY',   'Fire extinguisher in vehicle',        'FAIL', 'Extinguisher last serviced > 12mo ago', 1),
(1, (SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-002'),'VEHICLE',  'Vehicle washed & food-grade tanker',  'PASS', NULL, 1),
(1, (SELECT id FROM goods_receipt_note WHERE grn_number='GRN-2026-002'),'DOCUMENT', 'Invoice received',                    'PASS', NULL, 1);

-- 7. Sample QC test results tied to existing GRN line
INSERT IGNORE INTO m5_qc_test_result (tenant_id, source_type, source_ref_id, item_code, parameter, method, unit, spec_min, spec_max, measured_value, pass_fail, tested_at, tested_by_emp_id, remarks)
SELECT 1, 'GRN_LINE', (SELECT id FROM goods_receipt_line WHERE lot_no='LOT-SUN-260607-A'),
       'OIL-SUN-RAW','FFA_PCT','AOCS Ca 5a-40','%', NULL, 2.00, 1.42, 'PASS', '2026-06-07 11:30:00', 2, 'within spec';
INSERT IGNORE INTO m5_qc_test_result (tenant_id, source_type, source_ref_id, item_code, parameter, method, unit, spec_min, spec_max, measured_value, pass_fail, tested_at, tested_by_emp_id)
SELECT 1, 'GRN_LINE', (SELECT id FROM goods_receipt_line WHERE lot_no='LOT-SUN-260607-A'),
       'OIL-SUN-RAW','MOISTURE_PCT','AOCS Ca 2c-25','%', NULL, 0.20, 0.08, 'PASS', '2026-06-07 12:00:00', 2;
INSERT IGNORE INTO m5_qc_test_result (tenant_id, source_type, source_ref_id, item_code, parameter, method, unit, spec_min, spec_max, measured_value, pass_fail, tested_at, tested_by_emp_id)
SELECT 1, 'GRN_LINE', (SELECT id FROM goods_receipt_line WHERE lot_no='LOT-SUN-260607-A'),
       'OIL-SUN-RAW','DENSITY_15C','IS 548','kg/L', 0.91, 0.93, 0.918, 'PASS', '2026-06-07 12:15:00', 2;
INSERT IGNORE INTO m5_qc_test_result (tenant_id, source_type, source_ref_id, item_code, parameter, method, unit, spec_min, spec_max, measured_value, pass_fail, tested_at, tested_by_emp_id)
SELECT 1, 'GRN_LINE', (SELECT id FROM goods_receipt_line WHERE lot_no='LOT-SUN-260607-A'),
       'OIL-SUN-RAW','COLOR_LOVI_RED','AOCS Cc 13b-45','lovibond', NULL, 5.00, 3.20, 'PASS', '2026-06-07 12:30:00', 2;
INSERT IGNORE INTO m5_qc_test_result (tenant_id, source_type, source_ref_id, item_code, parameter, method, unit, spec_min, spec_max, measured_value, pass_fail, tested_at, tested_by_emp_id)
SELECT 1, 'GRN_LINE', (SELECT id FROM goods_receipt_line WHERE lot_no='LOT-GND-260608-A'),
       'OIL-GND-RAW','FFA_PCT','AOCS Ca 5a-40','%', NULL, 2.00, 2.45, 'FAIL', '2026-06-08 10:00:00', 2;
INSERT IGNORE INTO m5_qc_test_result (tenant_id, source_type, source_ref_id, item_code, parameter, method, unit, spec_min, spec_max, measured_value, pass_fail, tested_at, tested_by_emp_id)
SELECT 1, 'GRN_LINE', (SELECT id FROM goods_receipt_line WHERE lot_no='LOT-GND-260608-A'),
       'OIL-GND-RAW','MOISTURE_PCT','AOCS Ca 2c-25','%', NULL, 0.20, 0.18, 'PASS', '2026-06-08 10:15:00', 2;

-- 8. Update sample GRN with lorry + weighbridge data
UPDATE goods_receipt_note
   SET vendor_name='TESTRO Industries', po_number='PO-2026-0156', invoice_number='TI-9234',
       invoice_date='2026-06-06', invoice_value=2150000.00,
       vehicle_no='MH04JK1234', vehicle_type='TANKER',
       driver_name='Rajendra Yadav', driver_phone='+919900099001',
       transporter_name='TESTRO Own',
       gate_in_time='2026-06-07 09:30:00', gate_out_time='2026-06-07 13:45:00',
       weighbridge_in_kg=33500.00, weighbridge_out_kg=12000.00,
       weighbridge_slip_no='WB-260607-001', weighbridge_operator='Murugan',
       seal_no='SEAL-WJ-77881', seal_intact=1, pkg_count=1, pkg_condition='GOOD',
       doc_invoice_recd=1, doc_lr_recd=1, doc_ewb_recd=1, doc_test_cert_recd=1, doc_msds_recd=1,
       unloading_started_at='2026-06-07 10:15:00', unloading_completed_at='2026-06-07 12:30:00',
       qc_required=1, qc_lab_tech='Priya S.', qc_sample_drawn_at='2026-06-07 10:30:00',
       qc_completed_at='2026-06-07 13:00:00', qc_verdict='ACCEPTED', qc_certificate_no='QC-260607-001',
       stored_in_warehouse='PLANT-1', storage_complete_at='2026-06-07 13:15:00'
 WHERE grn_number='GRN-2026-001';

UPDATE goods_receipt_note
   SET vendor_name='Iyer Lubricants', po_number='PO-2026-0157', invoice_number='IY-4501',
       invoice_date='2026-06-07', invoice_value=1690000.00,
       vehicle_no='TN37AL9988', vehicle_type='TANKER',
       driver_name='Karthik R.', driver_phone='+919900099002',
       transporter_name='Iyer Logistics',
       gate_in_time='2026-06-08 08:45:00',
       weighbridge_in_kg=28500.00, weighbridge_out_kg=9750.00,
       weighbridge_slip_no='WB-260608-002', weighbridge_operator='Murugan',
       seal_no='SEAL-IY-44521', seal_intact=1, pkg_count=1, pkg_condition='GOOD',
       doc_invoice_recd=1, doc_lr_recd=1, doc_ewb_recd=1, doc_test_cert_recd=0, doc_msds_recd=1,
       unloading_started_at='2026-06-08 09:10:00',
       qc_required=1, qc_lab_tech='Priya S.', qc_sample_drawn_at='2026-06-08 09:30:00',
       qc_verdict=NULL
 WHERE grn_number='GRN-2026-002';

UPDATE goods_receipt_line
   SET qty_invoiced=21500.000, qty_accepted=21500.000, qty_rejected=0.000,
       rate=85.50, hsn_code='15121110', batch_no='LOT-SUN-260607-A',
       mfg_date='2026-06-05', exp_date='2027-06-05', shelf_life_days=365,
       bin_assigned='T-OIL-03', pallet_no=NULL, temperature_at_receipt=27.5
 WHERE lot_no='LOT-SUN-260607-A';

UPDATE goods_receipt_line
   SET qty_invoiced=18750.000, qty_accepted=NULL, qty_rejected=NULL,
       rate=165.00, hsn_code='15081000', batch_no='LOT-GND-260608-A',
       mfg_date='2026-06-06', exp_date='2027-06-06', shelf_life_days=365,
       bin_assigned='T-OIL-17', temperature_at_receipt=28.0
 WHERE lot_no='LOT-GND-260608-A';

-- 9. Register new entities for the Designer
INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('QcTestResult','QC Test Result','m5_qc_test_result'),
  ('QcParameterCatalog','QC Parameter Catalog','m5_qc_parameter_catalog'),
  ('InwardAudit','Inward Audit','m5_inward_audit'),
  ('CycleCount','Cycle Count','m5_cycle_count'),
  ('AuditLog','Audit Log','m0_audit_log');
