-- =====================================================================
-- V25 — Product Master + GST/HSN + Invoice Line + Tracking + Payslip
--       + Increment + Appraisal.
--
-- Brings the missing pieces the user called out:
--   1. m5_item_master                — Product master (HSN/GST/UOM/batch)
--   2. m5_hsn_master + m5_gst_rate   — HSN -> GST rate lookup table
--   3. m6_sales_invoice_line + tax   — line-level GST breakdown
--   4. m6_shipment_tracking          — multi-leg shipment + checkpoint log
--   5. m7_payslip                    — generated payslip per emp per period
--   6. m7_increment_letter           — annual / off-cycle increment workflow
--   7. m7_appraisal_cycle + form     — KPI + rating + manager review
-- =====================================================================

-- ============================ 1. Item Master ============================
CREATE TABLE IF NOT EXISTS m5_item_master (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(256) NOT NULL,
  short_name VARCHAR(64) NULL,
  description TEXT NULL,
  item_type VARCHAR(16) NOT NULL DEFAULT 'FINISHED',  -- RAW/WIP/FINISHED/SPARE/CONSUMABLE/SERVICE
  category VARCHAR(64) NULL,
  sub_category VARCHAR(64) NULL,
  brand VARCHAR(64) NULL,
  uom VARCHAR(16) NOT NULL DEFAULT 'NOS',              -- KG/L/NOS/MT/PCS/...
  alt_uom VARCHAR(16) NULL,
  uom_conversion_factor DECIMAL(12,4) NULL,             -- 1 KG = 1000 G etc.
  hsn_code VARCHAR(16) NULL,
  sac_code VARCHAR(16) NULL,                            -- for services
  gst_rate_pct DECIMAL(5,2) NULL,                       -- 0/5/12/18/28
  cess_pct DECIMAL(5,2) NULL,
  is_batch_tracked TINYINT(1) NOT NULL DEFAULT 0,
  is_serial_tracked TINYINT(1) NOT NULL DEFAULT 0,
  shelf_life_days INT NULL,
  storage_method VARCHAR(16) NULL,                      -- FIFO/FEFO/LIFO
  abc_class VARCHAR(1) NULL,                             -- A/B/C
  reorder_min DECIMAL(12,3) NULL,
  reorder_max DECIMAL(12,3) NULL,
  safety_stock DECIMAL(12,3) NULL,
  eoq DECIMAL(12,3) NULL,                                -- Economic Order Qty
  lead_time_days INT NULL,
  std_cost DECIMAL(12,2) NULL,
  list_price DECIMAL(12,2) NULL,
  vendor_default_id BIGINT NULL,
  rack VARCHAR(16) NULL,
  bin_default VARCHAR(16) NULL,
  weight_per_unit DECIMAL(12,3) NULL,
  weight_uom VARCHAR(8) NULL,
  density_at_15c DECIMAL(8,4) NULL,                      -- for oils
  qc_required TINYINT(1) NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',          -- ACTIVE/DISCONTINUED/HOLD
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_item_code (tenant_id, item_code),
  KEY idx_item_hsn (hsn_code),
  KEY idx_item_cat (category, sub_category),
  KEY idx_item_status (tenant_id, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_item_master (tenant_id, item_code, item_name, item_type, category, uom, hsn_code, gst_rate_pct,
  is_batch_tracked, shelf_life_days, storage_method, abc_class, reorder_min, reorder_max, safety_stock,
  std_cost, list_price, qc_required, density_at_15c, status) VALUES
(1,'OIL-SUN-RAW','Raw Sunflower Oil',   'RAW',     'OILS', 'KG','15121110', 5.00, 1, 365, 'FEFO','A', 5000.000, 20000.000, 2000.000,  85.50, 92.00, 1, 0.9180,'ACTIVE'),
(1,'OIL-SUN-RFD','Refined Sunflower Oil','FINISHED','OILS','KG','15121990', 5.00, 1, 540, 'FEFO','A', 3000.000, 15000.000, 1500.000, 115.00,135.00, 1, 0.9220,'ACTIVE'),
(1,'OIL-GND-RAW','Raw Groundnut Oil',   'RAW',     'OILS','KG','15081000', 5.00, 1, 365, 'FEFO','A', 4000.000, 18000.000, 1800.000, 165.00,182.00, 1, 0.9100,'ACTIVE'),
(1,'OIL-GND-RFD','Refined Groundnut Oil','FINISHED','OILS','KG','15081000', 5.00, 1, 540, 'FEFO','A', 2500.000, 12000.000, 1200.000, 195.00,225.00, 1, 0.9140,'ACTIVE'),
(1,'CAP-25MM-WH','25mm White Caps',     'CONSUMABLE','PACKAGING','PCS','39235010', 18.00, 0, NULL, 'FIFO','B', 5000.000, 50000.000, 5000.000,   1.10,  1.40, 0,    NULL,'ACTIVE'),
(1,'BTL-1L-PET', '1L PET Bottle',       'CONSUMABLE','PACKAGING','PCS','39233010', 18.00, 0, NULL, 'FIFO','B',10000.000,100000.000,10000.000,   6.50,  8.00, 0,    NULL,'ACTIVE'),
(1,'LBL-SUN-1L', 'Sunflower 1L Label',  'CONSUMABLE','PACKAGING','PCS','48211010', 18.00, 0, NULL, 'FIFO','C',10000.000,200000.000,10000.000,   0.80,  1.20, 0,    NULL,'ACTIVE'),
(1,'CTN-12-1L',  '12x1L Carton',        'CONSUMABLE','PACKAGING','PCS','48191000', 12.00, 0, NULL, 'FIFO','C', 1000.000, 10000.000, 1000.000,  18.00, 22.00, 0,    NULL,'ACTIVE'),
(1,'CAKE-SUN',   'Sunflower Oilcake',   'FINISHED','BYPRODUCT','KG','23069090', 5.00, 0, 180, 'FIFO','B', 1000.000, 10000.000, 1000.000,  16.00, 22.00, 0,    NULL,'ACTIVE'),
(1,'SVC-FREIGHT','Outbound Freight',    'SERVICE','SERVICES','NOS',NULL,    18.00, 0, NULL, NULL,    NULL,    NULL,     NULL,     NULL,   NULL,    NULL, 0,   NULL,'ACTIVE');
UPDATE m5_item_master SET sac_code='996511' WHERE item_code='SVC-FREIGHT';

-- ============================ 2. HSN / SAC + GST rates ===================
CREATE TABLE IF NOT EXISTS m5_hsn_master (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  hsn_code VARCHAR(16) NOT NULL,
  description VARCHAR(256) NOT NULL,
  chapter VARCHAR(4) NULL,
  cgst_pct DECIMAL(5,2) NOT NULL,
  sgst_pct DECIMAL(5,2) NOT NULL,
  igst_pct DECIMAL(5,2) NOT NULL,
  cess_pct DECIMAL(5,2) NULL,
  effective_from DATE NOT NULL,
  effective_to DATE NULL,
  notes VARCHAR(256) NULL,
  UNIQUE KEY uq_hsn (hsn_code, effective_from)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_hsn_master (hsn_code, description, chapter, cgst_pct, sgst_pct, igst_pct, effective_from) VALUES
('15121110','Sunflower oil — crude',         '15', 2.50, 2.50,  5.00, '2024-04-01'),
('15121990','Sunflower oil — refined',       '15', 2.50, 2.50,  5.00, '2024-04-01'),
('15081000','Groundnut oil',                 '15', 2.50, 2.50,  5.00, '2024-04-01'),
('15179010','Edible oil — blended',          '15', 2.50, 2.50,  5.00, '2024-04-01'),
('23069090','Oilcake (sunflower/groundnut)', '23', 2.50, 2.50,  5.00, '2024-04-01'),
('39235010','Plastic stoppers / caps',       '39', 9.00, 9.00, 18.00, '2024-04-01'),
('39233010','PET / plastic bottles',         '39', 9.00, 9.00, 18.00, '2024-04-01'),
('48211010','Printed paper labels',          '48', 9.00, 9.00, 18.00, '2024-04-01'),
('48191000','Cartons of paper / paperboard', '48', 6.00, 6.00, 12.00, '2024-04-01'),
('22021010','Aerated beverages',             '22',14.00,14.00, 28.00, '2024-04-01'),
('24021090','Tobacco products',              '24',14.00,14.00, 28.00, '2024-04-01'),
('99',      'Default services',              '99', 9.00, 9.00, 18.00, '2024-04-01');

-- SAC for services
INSERT IGNORE INTO m5_hsn_master (hsn_code, description, chapter, cgst_pct, sgst_pct, igst_pct, effective_from) VALUES
('996511','Road transport — freight',         '99', 2.50, 2.50,  5.00, '2024-04-01'),
('997212','Real estate rental',               '99', 9.00, 9.00, 18.00, '2024-04-01'),
('998511','Employment services',              '99', 9.00, 9.00, 18.00, '2024-04-01'),
('998313','IT consulting',                    '99', 9.00, 9.00, 18.00, '2024-04-01');

-- ============================ 3. Sales Invoice Line + tax ================
CREATE TABLE IF NOT EXISTS m6_sales_invoice_line (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  invoice_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  item_code VARCHAR(64) NULL,
  item_name VARCHAR(256) NOT NULL,
  hsn_code VARCHAR(16) NULL,
  qty DECIMAL(14,3) NOT NULL,
  uom VARCHAR(16) NOT NULL,
  rate DECIMAL(14,4) NOT NULL,
  amount DECIMAL(14,2) NOT NULL,
  discount_pct DECIMAL(5,2) NOT NULL DEFAULT 0,
  discount_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  taxable_amount DECIMAL(14,2) NOT NULL,
  cgst_pct DECIMAL(5,2) NOT NULL DEFAULT 0,
  cgst_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  sgst_pct DECIMAL(5,2) NOT NULL DEFAULT 0,
  sgst_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  igst_pct DECIMAL(5,2) NOT NULL DEFAULT 0,
  igst_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  cess_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  total_amount DECIMAL(14,2) NOT NULL,
  KEY idx_sil_inv (invoice_id),
  KEY idx_sil_item (item_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Extend m6_sales_invoice with IRN + ack + supply place
ALTER TABLE m6_sales_invoice
  ADD COLUMN IF NOT EXISTS irn VARCHAR(64) NULL AFTER total_amount,
  ADD COLUMN IF NOT EXISTS irn_ack_no VARCHAR(32) NULL AFTER irn,
  ADD COLUMN IF NOT EXISTS irn_ack_date DATETIME NULL AFTER irn_ack_no,
  ADD COLUMN IF NOT EXISTS irn_signed_qr LONGTEXT NULL AFTER irn_ack_date,
  ADD COLUMN IF NOT EXISTS place_of_supply_state VARCHAR(8) NULL AFTER gstin,
  ADD COLUMN IF NOT EXISTS supplier_gstin VARCHAR(16) NULL AFTER place_of_supply_state,
  ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS due_date DATE NULL;

-- Seed 2 invoices with line items
INSERT IGNORE INTO m6_sales_invoice (tenant_id, invoice_no, invoice_date, customer_name, gstin, subtotal, tax_amount, total_amount, status, supplier_gstin, place_of_supply_state, payment_terms, due_date) VALUES
(1,'INV-2026-0234','2026-06-05','Bangalore Foods Pvt Ltd','29AAACX1234L1ZB',233333.33, 11666.67, 245000.00, 'POSTED','33AAACS1234L1ZA','29','NET30','2026-07-05'),
(1,'INV-2026-0235','2026-06-06','Mumbai Distributors',     '27AAACY1234L1ZC',170000.00,  8500.00, 178500.00, 'POSTED','33AAACS1234L1ZA','27','NET45','2026-07-21');

INSERT IGNORE INTO m6_sales_invoice_line (invoice_id, line_no, item_code, item_name, hsn_code, qty, uom, rate, amount, taxable_amount, cgst_pct, sgst_pct, igst_pct, igst_amount, total_amount)
SELECT (SELECT id FROM m6_sales_invoice WHERE invoice_no='INV-2026-0234'),1,'OIL-SUN-RFD','Refined Sunflower Oil','15121990',
       2000.000,'KG',116.6667, 233333.33, 233333.33, 0.00, 0.00, 5.00, 11666.67, 245000.00
WHERE EXISTS (SELECT 1 FROM m6_sales_invoice WHERE invoice_no='INV-2026-0234');

INSERT IGNORE INTO m6_sales_invoice_line (invoice_id, line_no, item_code, item_name, hsn_code, qty, uom, rate, amount, taxable_amount, cgst_pct, sgst_pct, igst_pct, igst_amount, total_amount)
SELECT (SELECT id FROM m6_sales_invoice WHERE invoice_no='INV-2026-0235'),1,'OIL-GND-RFD','Refined Groundnut Oil','15081000',
       1000.000,'KG',170.0000, 170000.00, 170000.00, 0.00, 0.00, 5.00,  8500.00, 178500.00
WHERE EXISTS (SELECT 1 FROM m6_sales_invoice WHERE invoice_no='INV-2026-0235');

-- ============================ 4. Shipment tracking =======================
CREATE TABLE IF NOT EXISTS m6_shipment_tracking (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  tracking_no VARCHAR(48) NOT NULL,
  shipment_type VARCHAR(16) NOT NULL,                    -- OUTBOUND/INBOUND/RETURN
  reference_no VARCHAR(48) NOT NULL,                     -- invoice/PO/GRN no
  origin_city VARCHAR(64) NULL,
  destination_city VARCHAR(64) NULL,
  expected_eta DATETIME NULL,
  actual_eta DATETIME NULL,
  carrier_code VARCHAR(32) NULL,
  carrier_name VARCHAR(96) NULL,
  vehicle_no VARCHAR(16) NULL,
  driver_phone VARCHAR(16) NULL,
  current_status VARCHAR(32) NOT NULL DEFAULT 'CREATED', -- CREATED/PICKED_UP/IN_TRANSIT/OUT_FOR_DELIVERY/DELIVERED/RETURNED/LOST
  current_location VARCHAR(128) NULL,
  current_geo VARCHAR(48) NULL,                          -- lat,lng
  total_weight_kg DECIMAL(12,3) NULL,
  total_pkg_count INT NULL,
  ewb_no VARCHAR(32) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delivered_at TIMESTAMP NULL,
  pod_url VARCHAR(255) NULL,
  notes TEXT NULL,
  UNIQUE KEY uq_track (tenant_id, tracking_no),
  KEY idx_track_ref (reference_no),
  KEY idx_track_status (current_status, expected_eta)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m6_shipment_checkpoint (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tracking_id BIGINT NOT NULL,
  checkpoint_at DATETIME NOT NULL,
  location VARCHAR(128) NULL,
  geo VARCHAR(48) NULL,
  status VARCHAR(32) NOT NULL,
  remarks VARCHAR(256) NULL,
  recorded_by VARCHAR(64) NULL,
  KEY idx_ckpt_track (tracking_id, checkpoint_at)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m6_shipment_tracking (tenant_id, tracking_no, shipment_type, reference_no, origin_city, destination_city,
  expected_eta, carrier_name, vehicle_no, current_status, total_weight_kg, total_pkg_count, ewb_no) VALUES
(1,'TRK-2026-0001','OUTBOUND','INV-2026-0234','Coimbatore','Bangalore',  '2026-06-06 18:00:00','TVS Logistics','TN37BC0011','IN_TRANSIT', 2000.000, 4, 'EWB-2026-001'),
(1,'TRK-2026-0002','OUTBOUND','INV-2026-0235','Coimbatore','Mumbai',     '2026-06-09 22:00:00','Gati',         'TN37BC0012','PICKED_UP',  1000.000, 2, 'EWB-2026-002'),
(1,'TRK-2026-0003','INBOUND', 'PO-2026-0156','TESTRO Industries','Coimbatore','2026-06-08 14:00:00','TESTRO Own','MH04JK1234','DELIVERED',21500.000,1,NULL);

UPDATE m6_shipment_tracking SET delivered_at='2026-06-08 13:45:00' WHERE tracking_no='TRK-2026-0003';

INSERT IGNORE INTO m6_shipment_checkpoint (tracking_id, checkpoint_at, location, status, remarks) VALUES
((SELECT id FROM m6_shipment_tracking WHERE tracking_no='TRK-2026-0001'),'2026-06-05 11:00:00','Coimbatore Plant','CREATED','GP-2026-0512 issued'),
((SELECT id FROM m6_shipment_tracking WHERE tracking_no='TRK-2026-0001'),'2026-06-05 14:30:00','Coimbatore Plant Gate','PICKED_UP','Vehicle TN37BC0011 left'),
((SELECT id FROM m6_shipment_tracking WHERE tracking_no='TRK-2026-0001'),'2026-06-06 02:15:00','Salem Tollgate','IN_TRANSIT','Toll crossed'),
((SELECT id FROM m6_shipment_tracking WHERE tracking_no='TRK-2026-0001'),'2026-06-06 09:30:00','Hosur Border','IN_TRANSIT','State entry'),
((SELECT id FROM m6_shipment_tracking WHERE tracking_no='TRK-2026-0002'),'2026-06-06 14:15:00','Coimbatore Plant Gate','PICKED_UP','GP-2026-0513'),
((SELECT id FROM m6_shipment_tracking WHERE tracking_no='TRK-2026-0003'),'2026-06-07 19:30:00','Bombay Pune Hwy','IN_TRANSIT','En route'),
((SELECT id FROM m6_shipment_tracking WHERE tracking_no='TRK-2026-0003'),'2026-06-08 13:45:00','Coimbatore Plant','DELIVERED','Unloaded at gate');

-- ============================ 5. Payslip =================================
CREATE TABLE IF NOT EXISTS m7_payslip (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  emp_id BIGINT NOT NULL,
  payroll_period VARCHAR(8) NOT NULL,                    -- YYYY-MM
  generated_at TIMESTAMP NULL,
  generated_by VARCHAR(64) NULL,
  pdf_url VARCHAR(255) NULL,
  html_snapshot LONGTEXT NULL,
  email_sent_at TIMESTAMP NULL,
  email_to VARCHAR(128) NULL,
  whatsapp_sent_at TIMESTAMP NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',           -- DRAFT/GENERATED/EMAILED/ACKNOWLEDGED
  gross DECIMAL(12,2) NULL,
  total_deduct DECIMAL(12,2) NULL,
  net_pay DECIMAL(12,2) NULL,
  UNIQUE KEY uq_payslip (tenant_id, emp_id, payroll_period),
  KEY idx_payslip_period (payroll_period, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ============================ 6. Increment Letter ========================
CREATE TABLE IF NOT EXISTS m7_increment_letter (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  emp_id BIGINT NOT NULL,
  letter_no VARCHAR(32) NOT NULL,
  cycle_period VARCHAR(16) NOT NULL,                     -- "FY 2025-26 Annual" / "Off-Cycle Promotion"
  effective_from DATE NOT NULL,
  prev_ctc_annual DECIMAL(12,2) NOT NULL,
  new_ctc_annual DECIMAL(12,2) NOT NULL,
  increment_amount DECIMAL(12,2) GENERATED ALWAYS AS (new_ctc_annual - prev_ctc_annual) STORED,
  increment_pct DECIMAL(6,2) GENERATED ALWAYS AS ((new_ctc_annual - prev_ctc_annual) / prev_ctc_annual * 100) STORED,
  reason VARCHAR(64) NULL,                                -- ANNUAL/PROMOTION/MARKET_CORRECTION/RETENTION
  prev_designation VARCHAR(64) NULL,
  new_designation VARCHAR(64) NULL,
  prev_grade VARCHAR(16) NULL,
  new_grade VARCHAR(16) NULL,
  manager_recommendation TEXT NULL,
  hr_remarks TEXT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',           -- DRAFT/MGR_APPROVED/HR_APPROVED/CEO_APPROVED/RELEASED/ACCEPTED/REJECTED
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  released_at TIMESTAMP NULL,
  accepted_at TIMESTAMP NULL,
  letter_url VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_inc_letter (tenant_id, letter_no),
  KEY idx_inc_emp (emp_id, effective_from)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_increment_letter (tenant_id, emp_id, letter_no, cycle_period, effective_from,
  prev_ctc_annual, new_ctc_annual, reason, prev_designation, new_designation, prev_grade, new_grade,
  manager_recommendation, status) VALUES
(1, 1, 'INC-2026-001','FY 2025-26 Annual', '2026-04-01', 540000.00, 594000.00, 'ANNUAL',     'Production Supervisor','Senior Production Supervisor','L3','L4','Exceeds expectations; key player in monsoon ramp-up','RELEASED'),
(1, 2, 'INC-2026-002','FY 2025-26 Annual', '2026-04-01', 480000.00, 528000.00, 'ANNUAL',     'QC Chemist',           'QC Chemist',                  'L2','L2','Solid contributor — 10% hike','RELEASED'),
(1, 3, 'INC-2026-003','Confirmation Offer','2026-06-01', 240000.00, 264000.00, 'PROMOTION',  'Trainee Operator',     'Junior Operator',             'L1','L1','Probation cleared with consistent attendance','DRAFT');

-- Increment workflow
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M7.IncrementLetter',1,'PUBLISHED','{"code":"M7.IncrementLetter","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Manager"},{"code":"MGR_APPROVED","assignedRole":"HR"},{"code":"HR_APPROVED","assignedRole":"CEO"},{"code":"CEO_APPROVED","assignedRole":"HR"},{"code":"RELEASED","assignedRole":"Employee"},{"code":"ACCEPTED","terminal":true},{"code":"REJECTED","terminal":true}],"transitions":[{"from":"DRAFT","to":"MGR_APPROVED","trigger":"page.submitted","pageCode":"pico:increment-mgr-approve"},{"from":"MGR_APPROVED","to":"HR_APPROVED","trigger":"page.submitted","pageCode":"pico:increment-hr-approve"},{"from":"HR_APPROVED","to":"CEO_APPROVED","trigger":"page.submitted","pageCode":"pico:increment-ceo-approve"},{"from":"CEO_APPROVED","to":"RELEASED","trigger":"page.submitted","pageCode":"pico:increment-release"},{"from":"RELEASED","to":"ACCEPTED","trigger":"page.submitted","pageCode":"pico:increment-accept"},{"from":"RELEASED","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:increment-reject"}]}');

-- ============================ 7. Appraisal ===============================
CREATE TABLE IF NOT EXISTS m7_appraisal_cycle (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  cycle_code VARCHAR(32) NOT NULL,
  cycle_name VARCHAR(96) NOT NULL,
  fy VARCHAR(8) NOT NULL,
  period_from DATE NOT NULL,
  period_to DATE NOT NULL,
  self_window_from DATE NULL,
  self_window_to DATE NULL,
  mgr_window_from DATE NULL,
  mgr_window_to DATE NULL,
  hr_window_from DATE NULL,
  hr_window_to DATE NULL,
  rating_scale_max INT NOT NULL DEFAULT 5,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',         -- DRAFT/OPEN/CLOSED
  notes TEXT NULL,
  UNIQUE KEY uq_cycle (tenant_id, cycle_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_appraisal_cycle (tenant_id, cycle_code, cycle_name, fy, period_from, period_to,
  self_window_from, self_window_to, mgr_window_from, mgr_window_to, hr_window_from, hr_window_to, status) VALUES
(1,'APR-FY25-ANNUAL','Annual Appraisal FY 2024-25','2024-25','2024-04-01','2025-03-31','2025-04-01','2025-04-15','2025-04-16','2025-04-30','2025-05-01','2025-05-15','CLOSED'),
(1,'APR-FY26-HY1','Half-Yearly H1 FY 2025-26','2025-26','2025-04-01','2025-09-30','2025-10-01','2025-10-10','2025-10-11','2025-10-20','2025-10-21','2025-10-31','OPEN');

CREATE TABLE IF NOT EXISTS m7_appraisal_form (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  cycle_id BIGINT NOT NULL,
  emp_id BIGINT NOT NULL,
  manager_emp_id BIGINT NULL,
  self_submitted_at TIMESTAMP NULL,
  mgr_submitted_at TIMESTAMP NULL,
  hr_finalised_at TIMESTAMP NULL,
  -- KPI ratings (1..rating_scale_max)
  rating_productivity DECIMAL(3,1) NULL,
  rating_quality DECIMAL(3,1) NULL,
  rating_attendance DECIMAL(3,1) NULL,
  rating_initiative DECIMAL(3,1) NULL,
  rating_teamwork DECIMAL(3,1) NULL,
  rating_technical DECIMAL(3,1) NULL,
  rating_safety DECIMAL(3,1) NULL,
  rating_overall DECIMAL(3,1) NULL,
  -- Open-ended
  self_achievements TEXT NULL,
  self_improvement TEXT NULL,
  mgr_strengths TEXT NULL,
  mgr_dev_areas TEXT NULL,
  mgr_overall_remark TEXT NULL,
  hr_remarks TEXT NULL,
  -- Outcomes
  promotion_recommended TINYINT(1) NOT NULL DEFAULT 0,
  increment_pct_recommended DECIMAL(5,2) NULL,
  pip_required TINYINT(1) NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING_SELF',  -- PENDING_SELF/PENDING_MGR/PENDING_HR/CLOSED
  CONSTRAINT fk_apr_cycle FOREIGN KEY (cycle_id) REFERENCES m7_appraisal_cycle(id),
  KEY idx_apr_emp_cycle (emp_id, cycle_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_appraisal_form (tenant_id, cycle_id, emp_id, manager_emp_id,
  rating_productivity, rating_quality, rating_attendance, rating_initiative, rating_teamwork, rating_technical, rating_safety, rating_overall,
  mgr_overall_remark, increment_pct_recommended, status)
SELECT 1, (SELECT id FROM m7_appraisal_cycle WHERE cycle_code='APR-FY25-ANNUAL'), 1, NULL,
       4.5, 4.0, 4.5, 4.0, 4.5, 4.0, 5.0, 4.4, 'Reliable shift in-charge; promoted to L4',  10.0, 'CLOSED'
WHERE EXISTS (SELECT 1 FROM m7_appraisal_cycle WHERE cycle_code='APR-FY25-ANNUAL');

INSERT IGNORE INTO m7_appraisal_form (tenant_id, cycle_id, emp_id, manager_emp_id,
  rating_productivity, rating_quality, rating_attendance, rating_initiative, rating_teamwork, rating_technical, rating_safety, rating_overall,
  mgr_overall_remark, increment_pct_recommended, status)
SELECT 1, (SELECT id FROM m7_appraisal_cycle WHERE cycle_code='APR-FY25-ANNUAL'), 2, NULL,
       4.0, 4.5, 5.0, 3.5, 4.0, 4.5, 4.5, 4.3, 'Strong QC owner; 10% hike approved', 10.0, 'CLOSED'
WHERE EXISTS (SELECT 1 FROM m7_appraisal_cycle WHERE cycle_code='APR-FY25-ANNUAL');

-- Appraisal workflow
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M7.Appraisal',1,'PUBLISHED','{"code":"M7.Appraisal","version":1,"initialState":"PENDING_SELF","states":[{"code":"PENDING_SELF","assignedRole":"Employee"},{"code":"PENDING_MGR","assignedRole":"Manager"},{"code":"PENDING_HR","assignedRole":"HR"},{"code":"CLOSED","terminal":true}],"transitions":[{"from":"PENDING_SELF","to":"PENDING_MGR","trigger":"page.submitted","pageCode":"pico:appraisal-self"},{"from":"PENDING_MGR","to":"PENDING_HR","trigger":"page.submitted","pageCode":"pico:appraisal-mgr"},{"from":"PENDING_HR","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:appraisal-hr"}]}');

-- ============================ 8. Register new entities ===================
INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('ItemMaster','Item Master','m5_item_master'),
  ('HsnMaster','HSN / SAC Master','m5_hsn_master'),
  ('SalesInvoiceLine','Sales Invoice Line','m6_sales_invoice_line'),
  ('ShipmentTracking','Shipment Tracking','m6_shipment_tracking'),
  ('ShipmentCheckpoint','Shipment Checkpoint','m6_shipment_checkpoint'),
  ('Payslip','Payslip','m7_payslip'),
  ('IncrementLetter','Increment Letter','m7_increment_letter'),
  ('AppraisalCycle','Appraisal Cycle','m7_appraisal_cycle'),
  ('AppraisalForm','Appraisal Form','m7_appraisal_form');
