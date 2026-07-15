-- =====================================================================
-- V27 — Comprehensive field expansion for 6 core operational tables.
--   1. m5_stock_balance + m5_stock_movement   (aging, FIFO, holds)
--   2. m5_sales_order + line                  (customer, terms, dispatch)
--   3. m5_production_order                    (BOM, planned vs actual, OEE)
--   4. m5_job_card                            (shift, downtime, scrap, QC)
--   5. m5_packing_run                         (line, machine, downtime, scrap reason)
--   6. m5_fg_dispatch                         (lorry, driver, weighbridge, E-way, IRN)
-- Each gets the "lorry / QC / audit / inspection" depth treatment.
-- =====================================================================

-- ============================ 1. STOCK ============================
ALTER TABLE m5_stock_balance
  ADD COLUMN IF NOT EXISTS warehouse VARCHAR(32) NULL AFTER bin_code,
  ADD COLUMN IF NOT EXISTS rack VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS qty_available DECIMAL(14,3) GENERATED ALWAYS AS (qty_on_hand - qty_reserved) STORED,
  ADD COLUMN IF NOT EXISTS qty_in_qc DECIMAL(14,3) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS qty_on_hold DECIMAL(14,3) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS qty_in_transit DECIMAL(14,3) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS receipt_date DATE NULL,
  ADD COLUMN IF NOT EXISTS mfg_date DATE NULL,
  ADD COLUMN IF NOT EXISTS exp_date DATE NULL,
  ADD COLUMN IF NOT EXISTS days_in_stock INT GENERATED ALWAYS AS (DATEDIFF(CURRENT_DATE, receipt_date)) VIRTUAL,
  ADD COLUMN IF NOT EXISTS days_to_expiry INT GENERATED ALWAYS AS (DATEDIFF(exp_date, CURRENT_DATE)) VIRTUAL,
  ADD COLUMN IF NOT EXISTS aging_bucket VARCHAR(8) GENERATED ALWAYS AS (
    CASE
      WHEN DATEDIFF(CURRENT_DATE, receipt_date) <= 30 THEN '0-30'
      WHEN DATEDIFF(CURRENT_DATE, receipt_date) <= 60 THEN '31-60'
      WHEN DATEDIFF(CURRENT_DATE, receipt_date) <= 90 THEN '61-90'
      WHEN DATEDIFF(CURRENT_DATE, receipt_date) <= 180 THEN '91-180'
      ELSE '180+'
    END
  ) VIRTUAL,
  ADD COLUMN IF NOT EXISTS unit_cost DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS valuation DECIMAL(14,2) GENERATED ALWAYS AS (qty_on_hand * COALESCE(unit_cost,0)) STORED,
  ADD COLUMN IF NOT EXISTS hold_reason VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS qc_status VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS last_count_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS abc_class VARCHAR(1) NULL;

INSERT IGNORE INTO m5_stock_balance (tenant_id, item_code, item_name, lot_no, bin_code, warehouse, uom,
  qty_on_hand, qty_reserved, qty_in_qc, qty_on_hold, receipt_date, mfg_date, exp_date,
  unit_cost, qc_status, abc_class) VALUES
(1,'OIL-SUN-RAW','Raw Sunflower Oil',   'LOT-SUN-260607-A','T-OIL-03','PLANT-1','KG', 21500.000,  0.000,    0.000, 0.000,'2026-06-07','2026-06-05','2027-06-05', 85.50, 'PASSED','A'),
(1,'OIL-GND-RAW','Raw Groundnut Oil',   'LOT-GND-260608-A','T-OIL-17','PLANT-1','KG', 18750.000,  0.000, 18750.000, 0.000,'2026-06-08','2026-06-06','2027-06-06',165.00, 'PENDING','A'),
(1,'OIL-SUN-RFD','Refined Sunflower Oil','LOT-RFD-260601-A','T-FG-01','PLANT-1','KG', 12300.000,  2000.000, 0.000, 0.000,'2026-06-01','2026-06-01','2027-12-01',115.00, 'PASSED','A'),
(1,'CAP-25MM-WH','25mm White Caps',    NULL,             'RACK-A1','PLANT-1','PCS',42000.000,  0.000,    0.000, 0.000,'2026-04-15','2026-03-01','2030-03-01',  1.10, NULL,    'B'),
(1,'OIL-GND-RFD','Refined Groundnut Oil','LOT-RFD-260525','T-FG-02','PLANT-1','KG',  3500.000,    0.000, 0.000, 3500.000,'2025-12-15','2025-12-10','2026-12-10',195.00,'PASSED','A');

UPDATE m5_stock_balance SET hold_reason='Below reorder + customer complaint pending', last_count_at='2026-06-08 10:00:00'
 WHERE item_code='OIL-GND-RFD' AND qty_on_hold > 0;

-- Stock movements with sources
ALTER TABLE m5_stock_movement
  ADD COLUMN IF NOT EXISTS warehouse VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS unit_cost DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS value_amount DECIMAL(14,2) GENERATED ALWAYS AS (qty * COALESCE(unit_cost,0)) STORED,
  ADD COLUMN IF NOT EXISTS reason VARCHAR(32) NULL;

-- ============================ 2. SALES ORDER ============================
ALTER TABLE m5_sales_order
  ADD COLUMN IF NOT EXISTS customer_id BIGINT NULL AFTER customer_name,
  ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS customer_email VARCHAR(96) NULL,
  ADD COLUMN IF NOT EXISTS billing_address TEXT NULL,
  ADD COLUMN IF NOT EXISTS shipping_address TEXT NULL,
  ADD COLUMN IF NOT EXISTS shipping_state_code VARCHAR(8) NULL,
  ADD COLUMN IF NOT EXISTS so_date DATE NULL,
  ADD COLUMN IF NOT EXISTS quote_ref VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS po_ref_no VARCHAR(32) NULL,         -- customer PO #
  ADD COLUMN IF NOT EXISTS po_ref_date DATE NULL,
  ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(32) NULL,     -- ADVANCE/NET30/NET45
  ADD COLUMN IF NOT EXISTS payment_received_pct DECIMAL(5,2) NULL,
  ADD COLUMN IF NOT EXISTS delivery_terms VARCHAR(32) NULL,    -- FOR/EXW/CIF/DAP
  ADD COLUMN IF NOT EXISTS sales_person VARCHAR(96) NULL,
  ADD COLUMN IF NOT EXISTS discount_pct DECIMAL(5,2) NULL,
  ADD COLUMN IF NOT EXISTS subtotal DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS tax_amount DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS freight_amount DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS credit_check_status VARCHAR(16) NULL,  -- OK/EXCEEDED/BYPASSED
  ADD COLUMN IF NOT EXISTS credit_limit DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS credit_used DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS lost_reason VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS expected_dispatch_date DATE NULL,
  ADD COLUMN IF NOT EXISTS actual_dispatch_date DATE NULL;

ALTER TABLE m5_sales_order_line
  ADD COLUMN IF NOT EXISTS hsn_code VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS discount_pct DECIMAL(5,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS discount_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS taxable_amount DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS cgst_pct DECIMAL(5,2) NULL,
  ADD COLUMN IF NOT EXISTS sgst_pct DECIMAL(5,2) NULL,
  ADD COLUMN IF NOT EXISTS igst_pct DECIMAL(5,2) NULL,
  ADD COLUMN IF NOT EXISTS tax_amount DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS line_total DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS qty_reserved DECIMAL(12,3) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS line_status VARCHAR(16) NOT NULL DEFAULT 'OPEN';   -- OPEN/PARTIAL/CLOSED/CANCELLED

INSERT IGNORE INTO m5_sales_order (tenant_id, so_number, customer_name, customer_gstin, customer_phone, customer_email,
  shipping_address, shipping_state_code, so_date, po_ref_no, po_ref_date,
  payment_terms, delivery_terms, sales_person, subtotal, tax_amount, total_value,
  credit_check_status, credit_limit, credit_used, expected_dispatch_date, status, currency) VALUES
(1,'SO-2026-0421','Bangalore Foods Pvt Ltd','29AAACX1234L1ZB','+918012345001','procurement@bfoods.in',
 '12 Industrial Estate, Peenya, Bangalore - 560058','29','2026-06-04','BFPL-PO-2026-0921','2026-06-03',
 'NET30','FOR','Suresh K.', 233333.33, 11666.67, 245000.00, 'OK',500000.00, 245000.00,'2026-06-06','DISPATCHED','INR'),
(1,'SO-2026-0422','Mumbai Distributors','27AAACY1234L1ZC','+919812345002','orders@mumbaidist.in',
 'B-12 Andheri MIDC, Mumbai - 400093','27','2026-06-05','MD-9821','2026-06-04',
 'NET45','CIF','Suresh K.', 170000.00,  8500.00, 178500.00, 'OK',700000.00, 178500.00,'2026-06-09','DISPATCHED','INR'),
(1,'SO-2026-0423','Coimbatore Retailers','33AAACZ1234L1ZD','+919812345003','orders@cbe-ret.in',
 '45 Town Hall, Coimbatore - 641001','33','2026-06-07','CR-44521','2026-06-07',
 'ADVANCE','EXW','Suresh K.', 94000.00,  4700.00,  98700.00, 'OK',300000.00,  98700.00,'2026-06-10','APPROVED','INR'),
(1,'SO-2026-0424','Chennai Wholesalers','33AAACA1234L1ZE','+919812345004','buyers@cw.in',
 '67 Vepery, Chennai - 600007','33','2026-06-08','CW-12022','2026-06-08',
 'NET30','FOR','Lakshmi V.',187500.00,  9375.00, 196875.00, 'EXCEEDED',200000.00, 215000.00,'2026-06-12','HOLD_CREDIT','INR');

INSERT IGNORE INTO m5_sales_order_line (so_id, line_no, item_code, item_name, qty_ordered, qty_dispatched, uom, rate, line_value, hsn_code, taxable_amount, igst_pct, tax_amount, line_total, line_status)
SELECT (SELECT id FROM m5_sales_order WHERE so_number='SO-2026-0421'),1,'OIL-SUN-RFD','Refined Sunflower Oil',2000.000,2000.000,'KG',116.6667, 233333.33,'15121990', 233333.33, 5.00, 11666.67, 245000.00, 'CLOSED'
WHERE EXISTS (SELECT 1 FROM m5_sales_order WHERE so_number='SO-2026-0421');
INSERT IGNORE INTO m5_sales_order_line (so_id, line_no, item_code, item_name, qty_ordered, qty_dispatched, uom, rate, line_value, hsn_code, taxable_amount, igst_pct, tax_amount, line_total, line_status)
SELECT (SELECT id FROM m5_sales_order WHERE so_number='SO-2026-0422'),1,'OIL-GND-RFD','Refined Groundnut Oil',1000.000,1000.000,'KG',170.0000, 170000.00,'15081000', 170000.00, 5.00,  8500.00, 178500.00, 'CLOSED'
WHERE EXISTS (SELECT 1 FROM m5_sales_order WHERE so_number='SO-2026-0422');
INSERT IGNORE INTO m5_sales_order_line (so_id, line_no, item_code, item_name, qty_ordered, qty_dispatched, uom, rate, line_value, hsn_code, taxable_amount, cgst_pct, sgst_pct, tax_amount, line_total, line_status)
SELECT (SELECT id FROM m5_sales_order WHERE so_number='SO-2026-0423'),1,'OIL-SUN-RFD','Refined Sunflower Oil', 800.000,   0.000,'KG',117.5000,  94000.00,'15121990',  94000.00, 2.50, 2.50,  4700.00,  98700.00, 'OPEN'
WHERE EXISTS (SELECT 1 FROM m5_sales_order WHERE so_number='SO-2026-0423');
INSERT IGNORE INTO m5_sales_order_line (so_id, line_no, item_code, item_name, qty_ordered, qty_dispatched, uom, rate, line_value, hsn_code, taxable_amount, cgst_pct, sgst_pct, tax_amount, line_total, line_status)
SELECT (SELECT id FROM m5_sales_order WHERE so_number='SO-2026-0424'),1,'OIL-SUN-RFD','Refined Sunflower Oil',1500.000,   0.000,'KG',125.0000, 187500.00,'15121990', 187500.00, 2.50, 2.50,  9375.00, 196875.00, 'OPEN'
WHERE EXISTS (SELECT 1 FROM m5_sales_order WHERE so_number='SO-2026-0424');

-- ============================ 3. PRODUCTION ORDER ============================
ALTER TABLE m5_production_order
  ADD COLUMN IF NOT EXISTS bom_code VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS planned_start_date DATETIME NULL,
  ADD COLUMN IF NOT EXISTS planned_end_date DATETIME NULL,
  ADD COLUMN IF NOT EXISTS actual_start_date DATETIME NULL,
  ADD COLUMN IF NOT EXISTS actual_end_date DATETIME NULL,
  ADD COLUMN IF NOT EXISTS actual_qty_good DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS actual_qty_rework DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS actual_qty_scrap DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS yield_pct DECIMAL(5,2) GENERATED ALWAYS AS (
    CASE WHEN planned_qty > 0 THEN (COALESCE(actual_qty_good,0) / planned_qty * 100) ELSE NULL END
  ) STORED,
  ADD COLUMN IF NOT EXISTS scrap_pct DECIMAL(5,2) GENERATED ALWAYS AS (
    CASE WHEN planned_qty > 0 THEN (COALESCE(actual_qty_scrap,0) / planned_qty * 100) ELSE NULL END
  ) STORED,
  ADD COLUMN IF NOT EXISTS planned_runtime_min INT NULL,
  ADD COLUMN IF NOT EXISTS actual_runtime_min INT NULL,
  ADD COLUMN IF NOT EXISTS planned_downtime_min INT NULL,
  ADD COLUMN IF NOT EXISTS actual_downtime_min INT NULL,
  ADD COLUMN IF NOT EXISTS oee_availability_pct DECIMAL(5,2) NULL,
  ADD COLUMN IF NOT EXISTS oee_performance_pct DECIMAL(5,2) NULL,
  ADD COLUMN IF NOT EXISTS oee_quality_pct DECIMAL(5,2) NULL,
  ADD COLUMN IF NOT EXISTS oee_overall_pct DECIMAL(5,2) GENERATED ALWAYS AS (
    COALESCE(oee_availability_pct,0) * COALESCE(oee_performance_pct,0) * COALESCE(oee_quality_pct,0) / 10000
  ) STORED,
  ADD COLUMN IF NOT EXISTS work_center VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS supervisor_emp_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS qc_passed TINYINT(1) NULL,
  ADD COLUMN IF NOT EXISTS qc_remarks TEXT NULL,
  ADD COLUMN IF NOT EXISTS notes TEXT NULL;

INSERT IGNORE INTO m5_production_order (tenant_id, po_number, product_code, product_name, bom_code,
  planned_qty, uom, production_line, work_center, required_by_date,
  planned_start_date, planned_end_date, actual_start_date, actual_end_date,
  actual_qty_good, actual_qty_rework, actual_qty_scrap,
  planned_runtime_min, actual_runtime_min, actual_downtime_min,
  oee_availability_pct, oee_performance_pct, oee_quality_pct, supervisor_emp_id,
  qc_passed, status) VALUES
(1,'PO-2026-0301','OIL-SUN-RFD','Refined Sunflower Oil','BOM-SUN-RFD-V3',
  10000.000,'KG','LINE-A','REFINERY','2026-06-10',
  '2026-06-05 06:00:00','2026-06-05 18:00:00','2026-06-05 06:15:00','2026-06-05 17:55:00',
  9750.000, 50.000, 200.000,
  720, 700, 25,
  96.5, 92.3, 99.5, 1,
  1,'CLOSED'),
(1,'PO-2026-0302','OIL-GND-RFD','Refined Groundnut Oil','BOM-GND-RFD-V2',
   8000.000,'KG','LINE-B','REFINERY','2026-06-12',
  '2026-06-07 06:00:00','2026-06-07 18:00:00','2026-06-07 06:45:00', NULL,
  4000.000,  0.000,  50.000,
  720,  NULL,  60,
  NULL, NULL, NULL, 1,
  NULL,'IN_PROGRESS');

-- ============================ 4. JOB CARD ============================
ALTER TABLE m5_job_card
  ADD COLUMN IF NOT EXISTS shift_start DATETIME NULL,
  ADD COLUMN IF NOT EXISTS shift_end DATETIME NULL,
  ADD COLUMN IF NOT EXISTS planned_qty DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS scrap_qty DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS rework_qty DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS downtime_min INT NULL,
  ADD COLUMN IF NOT EXISTS downtime_reason VARCHAR(64) NULL,  -- BREAKDOWN/CHANGEOVER/MATERIAL/POWER/QUALITY/CLEANING/OTHER
  ADD COLUMN IF NOT EXISTS energy_kwh DECIMAL(12,2) NULL,
  ADD COLUMN IF NOT EXISTS steam_consumed_kg DECIMAL(12,2) NULL,
  ADD COLUMN IF NOT EXISTS water_consumed_l DECIMAL(12,2) NULL,
  ADD COLUMN IF NOT EXISTS qc_sample_drawn TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS qc_result VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS supervisor_signoff_emp_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS supervisor_signoff_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS notes TEXT NULL;

CREATE TABLE IF NOT EXISTS m5_job_card_consumption (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  jc_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(256) NULL,
  planned_qty DECIMAL(14,3) NULL,
  actual_qty DECIMAL(14,3) NOT NULL,
  variance_qty DECIMAL(14,3) GENERATED ALWAYS AS (COALESCE(actual_qty,0) - COALESCE(planned_qty,0)) STORED,
  variance_pct DECIMAL(6,2) GENERATED ALWAYS AS (
    CASE WHEN planned_qty > 0 THEN ((actual_qty - planned_qty) / planned_qty * 100) ELSE NULL END
  ) STORED,
  uom VARCHAR(16) NOT NULL,
  lot_no VARCHAR(64) NULL,
  unit_cost DECIMAL(12,2) NULL,
  total_cost DECIMAL(14,2) GENERATED ALWAYS AS (COALESCE(actual_qty,0) * COALESCE(unit_cost,0)) STORED,
  KEY idx_jcc_jc (jc_id),
  KEY idx_jcc_item (item_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_job_card (tenant_id, jc_number, production_order_id, operator_id, shift_code,
  shift_start, shift_end, planned_qty, output_qty, scrap_qty, rework_qty,
  downtime_min, downtime_reason, energy_kwh, qc_sample_drawn, qc_result,
  output_lot_no, output_bin, status, closed_at)
SELECT 1,'JC-2026-0701', po.id, 1, 'A',
       '2026-06-05 06:15:00', '2026-06-05 17:55:00', 10000.000, 9750.000, 200.000, 50.000,
       25,'CHANGEOVER', 1842.50, 1,'PASSED',
       'LOT-RFD-260605-A','T-FG-01','CLOSED','2026-06-05 18:00:00'
  FROM m5_production_order po WHERE po.po_number='PO-2026-0301'
  AND NOT EXISTS (SELECT 1 FROM m5_job_card WHERE jc_number='JC-2026-0701');

INSERT IGNORE INTO m5_job_card_consumption (jc_id, line_no, item_code, item_name, planned_qty, actual_qty, uom, lot_no, unit_cost)
SELECT j.id, 1, 'OIL-SUN-RAW','Raw Sunflower Oil',  10500.000, 10520.000,'KG','LOT-SUN-260607-A', 85.50  FROM m5_job_card j WHERE jc_number='JC-2026-0701';
INSERT IGNORE INTO m5_job_card_consumption (jc_id, line_no, item_code, item_name, planned_qty, actual_qty, uom, lot_no, unit_cost)
SELECT j.id, 2, 'CITRIC-ACID','Citric Acid',           5.000,  4.800,'KG',NULL,                 95.00 FROM m5_job_card j WHERE jc_number='JC-2026-0701';
INSERT IGNORE INTO m5_job_card_consumption (jc_id, line_no, item_code, item_name, planned_qty, actual_qty, uom, lot_no, unit_cost)
SELECT j.id, 3, 'CAUSTIC-SODA','Caustic Soda',       15.000, 15.500,'KG',NULL,                 48.00 FROM m5_job_card j WHERE jc_number='JC-2026-0701';
INSERT IGNORE INTO m5_job_card_consumption (jc_id, line_no, item_code, item_name, planned_qty, actual_qty, uom, lot_no, unit_cost)
SELECT j.id, 4, 'BLEACH-EARTH','Bleaching Earth',    80.000, 82.000,'KG',NULL,                 22.00 FROM m5_job_card j WHERE jc_number='JC-2026-0701';

-- ============================ 5. PACKING RUN ============================
ALTER TABLE m5_packing_run
  ADD COLUMN IF NOT EXISTS packing_line VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS machine_code VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS operator_emp_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS shift_code VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS shift_start DATETIME NULL,
  ADD COLUMN IF NOT EXISTS shift_end DATETIME NULL,
  ADD COLUMN IF NOT EXISTS pack_size DECIMAL(8,3) NULL,
  ADD COLUMN IF NOT EXISTS pack_uom VARCHAR(8) NULL,
  ADD COLUMN IF NOT EXISTS bulk_qty_consumed DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS bulk_qty_uom VARCHAR(8) NULL,
  ADD COLUMN IF NOT EXISTS caps_consumed INT NULL,
  ADD COLUMN IF NOT EXISTS bottles_consumed INT NULL,
  ADD COLUMN IF NOT EXISTS labels_consumed INT NULL,
  ADD COLUMN IF NOT EXISTS cartons_consumed INT NULL,
  ADD COLUMN IF NOT EXISTS spoil_reason VARCHAR(64) NULL,  -- UNDERFILL/OVERFILL/CAP_LOOSE/LABEL_MISALIGN/SEAL_FAIL/CONTAMINATION
  ADD COLUMN IF NOT EXISTS yield_pct DECIMAL(5,2) GENERATED ALWAYS AS (
    CASE WHEN planned_packs > 0 THEN (COALESCE(actual_packs,0) / planned_packs * 100) ELSE NULL END
  ) STORED,
  ADD COLUMN IF NOT EXISTS spoil_pct DECIMAL(5,2) GENERATED ALWAYS AS (
    CASE WHEN actual_packs > 0 THEN (COALESCE(spoil_packs,0) / actual_packs * 100) ELSE NULL END
  ) STORED,
  ADD COLUMN IF NOT EXISTS downtime_min INT NULL,
  ADD COLUMN IF NOT EXISTS downtime_reason VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS qc_status VARCHAR(16) NULL,  -- WEIGHT_OK/WEIGHT_FAIL/SEAL_FAIL/LABEL_FAIL
  ADD COLUMN IF NOT EXISTS avg_pack_weight_g DECIMAL(8,2) NULL,
  ADD COLUMN IF NOT EXISTS notes TEXT NULL;

INSERT IGNORE INTO m5_packing_run (tenant_id, pr_number, from_bulk_lot, from_bin, packaging_sku,
  packed_lot_no, to_bin, planned_packs, actual_packs, spoil_packs,
  packing_line, machine_code, operator_emp_id, shift_code, shift_start, shift_end,
  pack_size, pack_uom, bulk_qty_consumed, bulk_qty_uom, caps_consumed, bottles_consumed,
  labels_consumed, cartons_consumed, downtime_min, downtime_reason, qc_status,
  avg_pack_weight_g, status) VALUES
(1,'PR-2026-0801','LOT-RFD-260605-A','T-FG-01','SKU-SUN-1L',
   'PACK-260606-001','RACK-FG-A2', 5000.000, 4928.000, 42.000,
   'PACK-LINE-1','PKG-MC-04', 2, 'A','2026-06-06 06:00:00','2026-06-06 14:00:00',
   1.000,'L', 4970.000,'KG', 4970, 4970, 4970, 415, 18, 'CAP_LOOSE', 'WEIGHT_OK',
   1002.50,'CLOSED'),
(1,'PR-2026-0802','LOT-RFD-260605-A','T-FG-01','SKU-SUN-5L',
   'PACK-260606-002','RACK-FG-A3', 1000.000,  978.000,  8.000,
   'PACK-LINE-2','PKG-MC-05', 2, 'A','2026-06-06 06:00:00','2026-06-06 14:00:00',
   5.000,'L', 4920.000,'KG', NULL,  978, 978, 82, 12, 'LABEL_MISALIGN','WEIGHT_OK',
   5005.20,'CLOSED');

-- ============================ 6. FG DISPATCH ============================
ALTER TABLE m5_fg_dispatch
  ADD COLUMN IF NOT EXISTS customer_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS dispatch_date DATE NULL,
  ADD COLUMN IF NOT EXISTS shipping_address TEXT NULL,
  ADD COLUMN IF NOT EXISTS shipping_pincode VARCHAR(10) NULL,
  ADD COLUMN IF NOT EXISTS shipping_state_code VARCHAR(8) NULL,
  ADD COLUMN IF NOT EXISTS pkg_count INT NULL,
  ADD COLUMN IF NOT EXISTS pkg_type VARCHAR(16) NULL,                 -- CTN/PALLET/DRUM/BAG/CAN
  ADD COLUMN IF NOT EXISTS gross_weight_kg DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS net_weight_kg DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS vehicle_type VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS driver_name VARCHAR(96) NULL,
  ADD COLUMN IF NOT EXISTS driver_phone VARCHAR(16) NULL,
  ADD COLUMN IF NOT EXISTS driver_dl_no VARCHAR(24) NULL,
  ADD COLUMN IF NOT EXISTS transporter_name VARCHAR(96) NULL,
  ADD COLUMN IF NOT EXISTS gate_out_time DATETIME NULL,
  ADD COLUMN IF NOT EXISTS expected_delivery_date DATETIME NULL,
  ADD COLUMN IF NOT EXISTS weighbridge_in_kg DECIMAL(12,2) NULL,        -- loaded
  ADD COLUMN IF NOT EXISTS weighbridge_out_kg DECIMAL(12,2) NULL,       -- empty
  ADD COLUMN IF NOT EXISTS weighbridge_slip_no VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS seal_no VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS seal_applied_by VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS invoice_no VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS invoice_value DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS irn VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS ewb_no VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS ewb_valid_upto DATETIME NULL,
  ADD COLUMN IF NOT EXISTS qc_check_at_load TINYINT(1) NULL,
  ADD COLUMN IF NOT EXISTS qc_remarks_at_load TEXT NULL,
  ADD COLUMN IF NOT EXISTS pod_received TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS pod_received_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS pod_url VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS notes TEXT NULL;

UPDATE m5_fg_dispatch SET
  dispatch_date='2026-06-05', pkg_count=4, pkg_type='PALLET',
  gross_weight_kg=2050.000, net_weight_kg=2000.000,
  vehicle_type='TRUCK', driver_name='Selva R.', driver_phone='+919800110001',
  transporter_name='TVS Logistics', gate_out_time='2026-06-05 14:30:00',
  expected_delivery_date='2026-06-06 18:00:00',
  weighbridge_in_kg=14050.00, weighbridge_out_kg=12000.00,
  weighbridge_slip_no='WB-260605-OUT-001',
  seal_no='SEAL-OUT-9931', seal_applied_by='Murugan',
  invoice_no='INV-2026-0234', invoice_value=245000.00,
  irn='35aa9c3e8b1a4d27ad8e7c5b2e8a9f4d35aa9c3e8b1a4d27ad8e7c5b2e8a9f4d',
  ewb_no='EWB-2026-001', ewb_valid_upto='2026-06-09 09:30:00',
  qc_check_at_load=1, pod_received=1, pod_received_at='2026-06-06 16:45:00'
WHERE dispatch_number IS NOT NULL AND linked_so_id IN (SELECT id FROM m5_sales_order WHERE so_number='SO-2026-0421');

-- Add another dispatch row (in case existing seed didn't have one tied to SO-2026-0421)
INSERT IGNORE INTO m5_fg_dispatch (tenant_id, dispatch_number, linked_so_id, customer_name,
  source_bin, loading_bay, packed_lot_no, qty_to_dispatch, qty_dispatched, uom,
  vehicle_no, dispatch_date, pkg_count, pkg_type, gross_weight_kg, net_weight_kg,
  vehicle_type, driver_name, driver_phone, transporter_name,
  weighbridge_in_kg, weighbridge_out_kg, weighbridge_slip_no, seal_no,
  invoice_no, invoice_value, irn, ewb_no, status) VALUES
(1,'DSP-2026-0901',(SELECT id FROM m5_sales_order WHERE so_number='SO-2026-0421'),'Bangalore Foods Pvt Ltd',
 'T-FG-01','BAY-2','PACK-260606-001', 2000.000, 2000.000,'KG',
 'TN37BC0011','2026-06-05', 4, 'PALLET', 2050.000, 2000.000,
 'TRUCK','Selva R.','+919800110001','TVS Logistics',
  14050.00,12000.00,'WB-260605-OUT-001','SEAL-OUT-9931',
 'INV-2026-0234',245000.00,'35aa9c3e8b1a4d27ad8e7c5b2e8a9f4d35aa9c3e8b1a4d27ad8e7c5b2e8a9f4d','EWB-2026-001','CLOSED'),
(1,'DSP-2026-0902',(SELECT id FROM m5_sales_order WHERE so_number='SO-2026-0422'),'Mumbai Distributors',
 'T-FG-01','BAY-1','PACK-260606-002', 1000.000, 1000.000,'KG',
 'TN37BC0012','2026-06-06', 2, 'PALLET', 1035.000, 1000.000,
 'TRUCK','Karthik V.','+919800110002','Gati',
  12500.00,11465.00,'WB-260606-OUT-002','SEAL-OUT-9932',
 'INV-2026-0235',178500.00,NULL,'EWB-2026-002','IN_TRANSIT');

-- ============================ 7. Register new entities ============================
INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('StockBalance',     'Stock Balance',     'm5_stock_balance'),
  ('StockMovement',    'Stock Movement',    'm5_stock_movement'),
  ('SalesOrderLine',   'Sales Order Line',  'm5_sales_order_line'),
  ('JobCardConsumption','Job-Card Consumption','m5_job_card_consumption');
