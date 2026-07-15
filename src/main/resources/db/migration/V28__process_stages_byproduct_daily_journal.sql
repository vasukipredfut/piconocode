-- =====================================================================
-- V28 — Real factory operations the user called out as missing:
--   1. m5_process_stage_catalog  — defines stages per process (oil-mill)
--   2. m5_process_batch         — one batch flowing through the pipeline
--   3. m5_process_stage_log     — per-stage timing + input/output/byproduct
--   4. m5_bom_line + byproduct  — extend with byproduct allocation %
--   5. m5_daily_journal         — day-end shift summary
--   6. m5_shift_handover        — expand existing thin table
--   7. View m5_yield_summary    — output-yield rollup per product/period
--
-- Pico Power Project (edible oil mill) pre-processing flow seeded:
--   RECEIVING → CLEANING → CONDITIONING → DEHULLING → FLAKING →
--   EXPELLING → DEGUMMING → NEUTRALIZING → BLEACHING → DEODORIZING →
--   FILTRATION → PACKING → DISPATCH
-- =====================================================================

-- 1. Stage catalog (defines the canonical pre-processing / processing
--    steps; per product, ordered, with expected duration + yield).
CREATE TABLE IF NOT EXISTS m5_process_stage_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  process_code VARCHAR(32) NOT NULL,             -- e.g. OIL_MILL_REFINED
  stage_code VARCHAR(32) NOT NULL,                -- e.g. EXPELLING
  stage_label VARCHAR(96) NOT NULL,
  stage_order INT NOT NULL,
  stage_type VARCHAR(16) NOT NULL,                -- PRE / EXTRACT / REFINE / PACK / POST
  expected_duration_min INT NULL,
  expected_yield_pct DECIMAL(5,2) NULL,           -- input -> main output yield
  byproduct_pct DECIMAL(5,2) NULL,                -- input -> byproduct
  loss_pct DECIMAL(5,2) NULL,                     -- waste / evaporation
  byproduct_item_code VARCHAR(64) NULL,
  primary_equipment VARCHAR(64) NULL,
  qc_required TINYINT(1) NOT NULL DEFAULT 0,
  notes VARCHAR(256) NULL,
  UNIQUE KEY uq_stage (tenant_id, process_code, stage_code, stage_order)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Pico oil-mill canonical refined-oil flow
INSERT IGNORE INTO m5_process_stage_catalog (tenant_id, process_code, stage_code, stage_label, stage_order, stage_type,
  expected_duration_min, expected_yield_pct, byproduct_pct, loss_pct, byproduct_item_code, primary_equipment, qc_required, notes) VALUES
(1,'OIL_MILL_REFINED','RECEIVING',     'Raw material receipt + weigh',         1,'PRE',     30,100.00, 0.00,  0.00, NULL,            'Weighbridge',         1,'Vehicle, weighbridge, seal, sample'),
(1,'OIL_MILL_REFINED','CLEANING',      'Sieving + de-stoning + de-dusting',    2,'PRE',     60, 98.50, 0.00,  1.50, 'WASTE-CLEAN',   'Vibro Sieve + Dust Cyclone', 0,'Dirt / dust / metal removal'),
(1,'OIL_MILL_REFINED','CONDITIONING', 'Heat conditioning (60-80°C)',            3,'PRE',     45, 99.50, 0.00,  0.50, NULL,            'Steam Conditioner',    0,'Moisture adjustment'),
(1,'OIL_MILL_REFINED','DEHULLING',     'Cracking + hull separation',            4,'PRE',     60, 75.00,25.00,  0.00, 'HULL-SUN',      'Husker',               0,'Hull is byproduct (cattle feed)'),
(1,'OIL_MILL_REFINED','FLAKING',       'Roller flaking to ~0.3mm',              5,'PRE',     45, 99.80, 0.00,  0.20, NULL,            'Flaker Roll',          0,NULL),
(1,'OIL_MILL_REFINED','EXPELLING',     'Screw press oil extraction',            6,'EXTRACT',180, 32.00,66.00,  2.00, 'CAKE-SUN',      'Screw Press',          1,'Oil 30-35%, cake 65% (high-protein feed)'),
(1,'OIL_MILL_REFINED','DEGUMMING',     'Water + acid degumming (phospholipids)',7,'REFINE',  90, 97.00, 2.50,  0.50, 'GUMS-LECITH',   'Centrifuge',           1,'Lecithin byproduct'),
(1,'OIL_MILL_REFINED','NEUTRALIZING',  'Caustic soda neutralization (FFA)',     8,'REFINE',  90, 94.00, 5.00,  1.00, 'SOAPSTOCK',     'Reactor + Centrifuge', 1,'Soapstock to soap industry'),
(1,'OIL_MILL_REFINED','BLEACHING',     'Bleaching earth color removal',         9,'REFINE',  60, 98.50, 1.50,  0.00, 'SPENT-EARTH',   'Bleacher Vessel',      1,'Spent earth to landfill'),
(1,'OIL_MILL_REFINED','DEODORIZING',   'Steam deodorization (volatiles + odor)',10,'REFINE',180, 98.00, 1.50,  0.50, 'FA-DISTILLATE', 'Deodorizer Column',    1,'Fatty acid distillate to vitamin E recovery'),
(1,'OIL_MILL_REFINED','FILTRATION',    'Polish filtration (final clarity)',     11,'REFINE', 30, 99.80, 0.00,  0.20, NULL,            'Polish Filter',        1,NULL),
(1,'OIL_MILL_REFINED','PACKING',       'Bottle / pouch / tin pack',             12,'PACK',  120, 99.40, 0.60,  0.00, 'SPOIL-PACK',    'Packing Line',         1,'See m5_packing_run'),
(1,'OIL_MILL_REFINED','DISPATCH',      'Outward dispatch + EWB',                13,'POST',   60,100.00, 0.00,  0.00, NULL,            'Loading Bay',          0,'See m5_fg_dispatch');

-- Groundnut crude variant (no refining)
INSERT IGNORE INTO m5_process_stage_catalog (tenant_id, process_code, stage_code, stage_label, stage_order, stage_type,
  expected_duration_min, expected_yield_pct, byproduct_pct, loss_pct, byproduct_item_code, primary_equipment, qc_required, notes) VALUES
(1,'OIL_MILL_CRUDE_GND','RECEIVING',  'Seed receipt + weigh',                  1,'PRE',     30,100.00, 0.00, 0.00, NULL,        'Weighbridge',       1,NULL),
(1,'OIL_MILL_CRUDE_GND','CLEANING',   'Sieving + magnetic separation',          2,'PRE',     45, 98.00, 0.00, 2.00,'WASTE-CLEAN','Vibro Sieve',       0,NULL),
(1,'OIL_MILL_CRUDE_GND','CONDITIONING','Steam conditioning',                    3,'PRE',     45, 99.50, 0.00, 0.50,NULL,         'Conditioner',       0,NULL),
(1,'OIL_MILL_CRUDE_GND','EXPELLING',  'Cold-press oil extraction',              4,'EXTRACT',150, 38.00,60.00, 2.00,'CAKE-GND',   'Cold Press',         1,'Groundnut cake'),
(1,'OIL_MILL_CRUDE_GND','FILTRATION', 'Sediment removal',                        5,'REFINE',  20, 99.50, 0.00, 0.50,NULL,         'Cloth Filter',       0,NULL),
(1,'OIL_MILL_CRUDE_GND','PACKING',    'Filter-pressed pack',                    6,'PACK',    60, 99.40, 0.60, 0.00,'SPOIL-PACK', 'Cold-press Pack Line',1,NULL),
(1,'OIL_MILL_CRUDE_GND','DISPATCH',   'Outward dispatch + EWB',                 7,'POST',    60,100.00, 0.00, 0.00,NULL,         'Loading Bay',       0,NULL);

-- 2. Process batch — one physical lot moving through the pipeline
CREATE TABLE IF NOT EXISTS m5_process_batch (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  batch_no VARCHAR(48) NOT NULL,
  process_code VARCHAR(32) NOT NULL,                -- FK to catalog
  product_code VARCHAR(64) NOT NULL,
  product_name VARCHAR(160) NULL,
  bom_code VARCHAR(64) NULL,
  source_grn_id BIGINT NULL,                        -- where the raw material came from
  source_lot_no VARCHAR(64) NULL,
  planned_input_qty DECIMAL(14,3) NOT NULL,
  planned_output_qty DECIMAL(14,3) NULL,
  actual_input_qty DECIMAL(14,3) NULL,
  actual_output_qty DECIMAL(14,3) NULL,
  total_byproduct_qty DECIMAL(14,3) NULL,
  total_loss_qty DECIMAL(14,3) NULL,
  overall_yield_pct DECIMAL(5,2) GENERATED ALWAYS AS (
    CASE WHEN actual_input_qty > 0 THEN (COALESCE(actual_output_qty,0) / actual_input_qty * 100) ELSE NULL END
  ) STORED,
  started_at DATETIME NULL,
  completed_at DATETIME NULL,
  current_stage VARCHAR(32) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PLANNED',    -- PLANNED/RUNNING/HOLD/CLOSED/CANCELLED
  supervisor_emp_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_batch (tenant_id, batch_no),
  KEY idx_pb_proc (process_code, status),
  KEY idx_pb_source (source_lot_no)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- 3. Stage log — one row per stage per batch
CREATE TABLE IF NOT EXISTS m5_process_stage_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  batch_id BIGINT NOT NULL,
  stage_code VARCHAR(32) NOT NULL,
  stage_order INT NOT NULL,
  started_at DATETIME NULL,
  completed_at DATETIME NULL,
  duration_min INT GENERATED ALWAYS AS (
    CASE WHEN started_at IS NOT NULL AND completed_at IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, started_at, completed_at) ELSE NULL END
  ) VIRTUAL,
  input_qty DECIMAL(14,3) NOT NULL,
  output_qty DECIMAL(14,3) NULL,
  byproduct_qty DECIMAL(14,3) NULL,
  byproduct_item_code VARCHAR(64) NULL,
  byproduct_lot_no VARCHAR(64) NULL,
  loss_qty DECIMAL(14,3) NULL,
  stage_yield_pct DECIMAL(5,2) GENERATED ALWAYS AS (
    CASE WHEN input_qty > 0 THEN (COALESCE(output_qty,0) / input_qty * 100) ELSE NULL END
  ) STORED,
  expected_yield_pct DECIMAL(5,2) NULL,             -- denorm from catalog
  yield_variance_pct DECIMAL(5,2) GENERATED ALWAYS AS (
    CASE WHEN input_qty > 0 THEN ((COALESCE(output_qty,0) / input_qty * 100) - COALESCE(expected_yield_pct,0)) ELSE NULL END
  ) STORED,
  operator_emp_id BIGINT NULL,
  shift_code VARCHAR(8) NULL,
  equipment_code VARCHAR(32) NULL,
  -- Process parameters (oil mill specific)
  temperature_c DECIMAL(6,2) NULL,
  pressure_bar DECIMAL(6,2) NULL,
  rpm INT NULL,
  energy_kwh DECIMAL(10,2) NULL,
  steam_kg DECIMAL(10,2) NULL,
  water_l DECIMAL(10,2) NULL,
  chemicals_used TEXT NULL,                          -- e.g. "Caustic NaOH 12kg, Citric Acid 4kg"
  qc_sample_drawn TINYINT(1) NOT NULL DEFAULT 0,
  qc_result VARCHAR(16) NULL,
  qc_remarks TEXT NULL,
  notes TEXT NULL,
  CONSTRAINT fk_psl_batch FOREIGN KEY (batch_id) REFERENCES m5_process_batch(id) ON DELETE CASCADE,
  KEY idx_psl_batch (batch_id, stage_order)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- 4. BOM byproduct support
ALTER TABLE m5_bom_line
  ADD COLUMN IF NOT EXISTS line_type VARCHAR(16) NOT NULL DEFAULT 'INPUT' AFTER input_item_code,  -- INPUT/BYPRODUCT/SCRAP
  ADD COLUMN IF NOT EXISTS yield_pct DECIMAL(5,2) NULL,        -- for BYPRODUCT lines
  ADD COLUMN IF NOT EXISTS recovery_value DECIMAL(12,2) NULL;   -- per-unit byproduct recovery rate

INSERT IGNORE INTO m5_bom_line (bom_id, input_item_code, input_item_name, qty_per_yield, uom, loss_pct, line_type, yield_pct, recovery_value)
SELECT b.id, 'CAKE-SUN', 'Sunflower Oil Cake',  66.00,'KG', NULL,'BYPRODUCT',66.00, 22.00 FROM m5_bom b WHERE b.bom_code='BOM-OIL-SUN-RFD' AND NOT EXISTS (SELECT 1 FROM m5_bom_line bl WHERE bl.bom_id=b.id AND bl.input_item_code='CAKE-SUN');
INSERT IGNORE INTO m5_bom_line (bom_id, input_item_code, input_item_name, qty_per_yield, uom, loss_pct, line_type, yield_pct, recovery_value)
SELECT b.id, 'HULL-SUN', 'Sunflower Hull',      25.00,'KG', NULL,'BYPRODUCT',25.00,  4.00 FROM m5_bom b WHERE b.bom_code='BOM-OIL-SUN-RFD' AND NOT EXISTS (SELECT 1 FROM m5_bom_line bl WHERE bl.bom_id=b.id AND bl.input_item_code='HULL-SUN');
INSERT IGNORE INTO m5_bom_line (bom_id, input_item_code, input_item_name, qty_per_yield, uom, loss_pct, line_type, yield_pct, recovery_value)
SELECT b.id, 'SOAPSTOCK','Soapstock',            5.00,'KG', NULL,'BYPRODUCT', 5.00, 18.00 FROM m5_bom b WHERE b.bom_code='BOM-OIL-SUN-RFD' AND NOT EXISTS (SELECT 1 FROM m5_bom_line bl WHERE bl.bom_id=b.id AND bl.input_item_code='SOAPSTOCK');
INSERT IGNORE INTO m5_bom_line (bom_id, input_item_code, input_item_name, qty_per_yield, uom, loss_pct, line_type, yield_pct, recovery_value)
SELECT b.id, 'SPENT-EARTH','Spent Bleaching Earth',1.50,'KG',NULL,'BYPRODUCT',1.50,  1.50 FROM m5_bom b WHERE b.bom_code='BOM-OIL-SUN-RFD' AND NOT EXISTS (SELECT 1 FROM m5_bom_line bl WHERE bl.bom_id=b.id AND bl.input_item_code='SPENT-EARTH');

-- 5. Daily journal — day-end summary entry
CREATE TABLE IF NOT EXISTS m5_daily_journal (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  journal_date DATE NOT NULL,
  journal_no VARCHAR(32) NOT NULL,
  -- Production
  raw_received_kg DECIMAL(14,3) NULL,
  raw_consumed_kg DECIMAL(14,3) NULL,
  oil_produced_kg DECIMAL(14,3) NULL,
  cake_produced_kg DECIMAL(14,3) NULL,
  other_byproduct_kg DECIMAL(14,3) NULL,
  overall_yield_pct DECIMAL(5,2) GENERATED ALWAYS AS (
    CASE WHEN raw_consumed_kg > 0 THEN (COALESCE(oil_produced_kg,0) / raw_consumed_kg * 100) ELSE NULL END
  ) STORED,
  -- Packing
  packs_produced INT NULL,
  spoil_packs INT NULL,
  -- Dispatch
  trucks_dispatched INT NULL,
  qty_dispatched_kg DECIMAL(14,3) NULL,
  invoice_value DECIMAL(14,2) NULL,
  -- Utilities consumption
  power_kwh DECIMAL(12,2) NULL,
  steam_tons DECIMAL(12,3) NULL,
  water_kl DECIMAL(12,2) NULL,
  fuel_l DECIMAL(12,2) NULL,
  -- HR
  manpower_present INT NULL,
  manpower_absent INT NULL,
  ot_hours DECIMAL(8,2) NULL,
  -- Safety
  near_miss_count INT NULL DEFAULT 0,
  first_aid_count INT NULL DEFAULT 0,
  lti_count INT NULL DEFAULT 0,                       -- Lost-Time Injuries
  -- Incidents
  power_failures_min INT NULL DEFAULT 0,
  major_breakdowns INT NULL DEFAULT 0,
  qc_rejections INT NULL DEFAULT 0,
  -- Narrative
  highlights TEXT NULL,
  issues TEXT NULL,
  next_day_plan TEXT NULL,
  prepared_by_emp_id BIGINT NULL,
  approved_by_emp_id BIGINT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',        -- DRAFT/SUBMITTED/APPROVED
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_dj (tenant_id, journal_date),
  KEY idx_dj_status (status, journal_date)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- 6. Extend shift handover with operational metrics
ALTER TABLE m5_shift_handover
  ADD COLUMN IF NOT EXISTS shift_date DATE NULL,
  ADD COLUMN IF NOT EXISTS oil_produced_kg DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS cake_produced_kg DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS packs_produced INT NULL,
  ADD COLUMN IF NOT EXISTS power_kwh DECIMAL(12,2) NULL,
  ADD COLUMN IF NOT EXISTS open_qc_holds INT NULL,
  ADD COLUMN IF NOT EXISTS breakdowns_min INT NULL,
  ADD COLUMN IF NOT EXISTS safety_incidents INT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS pending_actions TEXT NULL,
  ADD COLUMN IF NOT EXISTS visitor_log TEXT NULL,
  ADD COLUMN IF NOT EXISTS environment_remarks TEXT NULL;

-- 7. Seed sample data
INSERT IGNORE INTO m5_process_batch (tenant_id, batch_no, process_code, product_code, product_name, bom_code,
  source_lot_no, planned_input_qty, planned_output_qty, actual_input_qty, actual_output_qty,
  total_byproduct_qty, total_loss_qty, started_at, completed_at, current_stage, status,
  supervisor_emp_id, notes) VALUES
(1,'PB-2026-1001','OIL_MILL_REFINED','OIL-SUN-RFD','Refined Sunflower Oil','BOM-OIL-SUN-RFD',
  'LOT-SUN-260607-A', 30000.000, 9600.000, 30000.000, 9580.000,
  19850.000, 570.000, '2026-06-07 06:00:00','2026-06-07 22:30:00', 'DISPATCH','CLOSED',
  1, 'Full refining cycle, FFA 1.42% pre-refining'),
(1,'PB-2026-1002','OIL_MILL_REFINED','OIL-SUN-RFD','Refined Sunflower Oil','BOM-OIL-SUN-RFD',
  'LOT-SUN-260608-A', 25000.000, 8000.000, 25000.000, 4100.000,
   8200.000, 100.000, '2026-06-08 06:00:00', NULL, 'NEUTRALIZING','RUNNING',
  1, 'In progress — Lot LOT-SUN-260608-A'),
(1,'PB-2026-1003','OIL_MILL_CRUDE_GND','OIL-GND-RAW','Raw Groundnut Oil','BOM-OIL-GND-RAW',
  'LOT-GND-260608-A', 18750.000, 7125.000, NULL, NULL,
   NULL, NULL, NULL, NULL, NULL,'PLANNED',
  1, 'Awaiting QC clearance on input lot');

-- Stage logs for PB-2026-1001 (all 13 stages CLOSED)
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at,
  input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct,
  operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id, 'RECEIVING',    1, '2026-06-07 06:00:00','2026-06-07 06:30:00', 30000.000, 30000.000,    0.000, NULL,           0.000,100.00, 3,'A','WB-1',    25.0,  10.5, 1,'PASSED' FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id, 'CLEANING',     2, '2026-06-07 06:30:00','2026-06-07 07:30:00', 30000.000, 29550.000,    0.000, NULL,         450.000, 98.50, 3,'A','SIEVE-1', 25.0,  24.0, 0, NULL    FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id, 'CONDITIONING', 3, '2026-06-07 07:30:00','2026-06-07 08:15:00', 29550.000, 29400.000,    0.000, NULL,         150.000, 99.50, 3,'A','COND-1',  72.0,  56.0, 0, NULL    FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id, 'DEHULLING',    4, '2026-06-07 08:15:00','2026-06-07 09:15:00', 29400.000, 22050.000, 7350.000,'HULL-SUN',     0.000, 75.00, 3,'A','HUSK-1',  25.0,  72.0, 0, NULL    FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id, 'FLAKING',      5, '2026-06-07 09:15:00','2026-06-07 10:00:00', 22050.000, 22010.000,    0.000, NULL,          40.000, 99.80, 3,'A','FLAKE-1', 25.0,  38.0, 0, NULL    FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, pressure_bar, rpm, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id, 'EXPELLING',    6, '2026-06-07 10:00:00','2026-06-07 13:00:00', 22010.000,  7050.000,14520.000,'CAKE-SUN',   440.000, 32.00, 3,'B','PRESS-1', 60.0,  85.0, 380, 320.0, 1,'PASSED' FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, chemicals_used, qc_sample_drawn, qc_result)
SELECT b.id, 'DEGUMMING',    7, '2026-06-07 13:00:00','2026-06-07 14:30:00',  7050.000,  6835.000,  175.000,'GUMS-LECITH', 40.000, 97.00, 2,'B','DEGUM-1', 75.0,  44.0,'Water 705L, Citric 7kg', 0, NULL FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, chemicals_used, qc_sample_drawn, qc_result)
SELECT b.id, 'NEUTRALIZING', 8, '2026-06-07 14:30:00','2026-06-07 16:00:00',  6835.000,  6420.000,  340.000,'SOAPSTOCK',   75.000, 94.00, 2,'B','NEUT-1',  90.0,  62.0,'NaOH 18% 35kg',          1,'PASSED' FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, chemicals_used, qc_sample_drawn, qc_result)
SELECT b.id, 'BLEACHING',    9, '2026-06-07 16:00:00','2026-06-07 17:00:00',  6420.000,  6320.000,  100.000,'SPENT-EARTH',  0.000, 98.50, 2,'B','BLEACH-1',105.0, 38.0,'Bleach Earth 38kg',     1,'PASSED' FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, chemicals_used, qc_sample_drawn, qc_result)
SELECT b.id, 'DEODORIZING', 10, '2026-06-07 17:00:00','2026-06-07 20:00:00',  6320.000,  6190.000,   95.000,'FA-DISTILLATE',35.000, 98.00, 2,'B','DEOD-1', 230.0,144.0,'Steam stripped',         1,'PASSED' FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, qc_sample_drawn, qc_result, qc_remarks)
SELECT b.id, 'FILTRATION', 11, '2026-06-07 20:00:00','2026-06-07 20:30:00',  6190.000,  6175.000,    0.000, NULL,          15.000, 99.80, 2,'C','POLISH-1', 70.0, 12.0, 1,'PASSED','Final: FFA 0.08%, color L1.0, moisture 0.05%' FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id, 'PACKING',    12, '2026-06-07 20:30:00','2026-06-07 22:00:00',  6175.000,  6137.000,   38.000,'SPOIL-PACK',    0.000, 99.40, 2,'C','PACK-1',         62.0, 1,'PASSED' FROM m5_process_batch b WHERE batch_no='PB-2026-1001';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, qc_sample_drawn, qc_result)
SELECT b.id, 'DISPATCH',   13, '2026-06-07 22:00:00','2026-06-07 22:30:00',  6137.000,  6137.000,    0.000, NULL,            0.000,100.00, 1,'C','BAY-1',                 0, NULL FROM m5_process_batch b WHERE batch_no='PB-2026-1001';

-- A few stages for the IN_PROGRESS batch
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at,
  input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct,
  operator_emp_id, shift_code, equipment_code, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id,'RECEIVING',     1,'2026-06-08 06:00:00','2026-06-08 06:30:00',25000,25000,    0,NULL,    0,100.00, 3,'A','WB-1',     8.5, 1,'PASSED' FROM m5_process_batch b WHERE batch_no='PB-2026-1002';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id,'CLEANING',      2,'2026-06-08 06:30:00','2026-06-08 07:30:00',25000,24625,    0,NULL,  375, 98.50, 3,'A','SIEVE-1', 20.0, 0, NULL    FROM m5_process_batch b WHERE batch_no='PB-2026-1002';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id,'CONDITIONING',  3,'2026-06-08 07:30:00','2026-06-08 08:15:00',24625,24500,    0,NULL,  125, 99.50, 3,'A','COND-1',  46.0, 0, NULL    FROM m5_process_batch b WHERE batch_no='PB-2026-1002';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id,'DEHULLING',     4,'2026-06-08 08:15:00','2026-06-08 09:15:00',24500,18375, 6125,'HULL-SUN', 0, 75.00, 3,'A','HUSK-1',  60.0, 0, NULL    FROM m5_process_batch b WHERE batch_no='PB-2026-1002';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id,'FLAKING',       5,'2026-06-08 09:15:00','2026-06-08 10:00:00',18375,18340,    0,NULL,   35, 99.80, 3,'A','FLAKE-1', 32.0, 0, NULL    FROM m5_process_batch b WHERE batch_no='PB-2026-1002';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, pressure_bar, energy_kwh, qc_sample_drawn, qc_result)
SELECT b.id,'EXPELLING',     6,'2026-06-08 10:00:00','2026-06-08 13:00:00',18340, 5871,12100,'CAKE-SUN',369,32.00, 3,'B','PRESS-1', 60.0, 85.0, 290.0, 1,'PASSED' FROM m5_process_batch b WHERE batch_no='PB-2026-1002';
INSERT IGNORE INTO m5_process_stage_log (batch_id, stage_code, stage_order, started_at, completed_at, input_qty, output_qty, byproduct_qty, byproduct_item_code, loss_qty, expected_yield_pct, operator_emp_id, shift_code, equipment_code, temperature_c, energy_kwh, chemicals_used, qc_sample_drawn, qc_result)
SELECT b.id,'DEGUMMING',     7,'2026-06-08 13:00:00','2026-06-08 14:30:00', 5871, 5695,  146,'GUMS-LECITH',30,97.00, 2,'B','DEGUM-1', 75.0, 38.0,'Water 587L, Citric 6kg',0, NULL FROM m5_process_batch b WHERE batch_no='PB-2026-1002';

-- Sample daily journals
INSERT IGNORE INTO m5_daily_journal (tenant_id, journal_date, journal_no, raw_received_kg, raw_consumed_kg,
  oil_produced_kg, cake_produced_kg, other_byproduct_kg, packs_produced, spoil_packs,
  trucks_dispatched, qty_dispatched_kg, invoice_value,
  power_kwh, steam_tons, water_kl, fuel_l, manpower_present, manpower_absent, ot_hours,
  near_miss_count, qc_rejections, power_failures_min, major_breakdowns,
  highlights, issues, next_day_plan, status) VALUES
(1,'2026-06-07','DJ-2026-0607', 30000.000, 30000.000, 6137.000, 14520.000, 4793.000, 5906, 50,
  2, 3000.000, 423500.00,
  864.0, 1.85, 14.20, 145.0, 18, 2, 12.5,
  0, 1, 5, 0,
  'Full sunflower cycle PB-2026-1001 completed at 22:30, FFA 0.08% well within spec. Cake yield 48.4% (target 45%).',
  '1 QC retest on Lot LOT-SUN-260607-A FFA reading; recovered after recalibration.',
  'Continue PB-2026-1002 from neutralizing stage. Receive 18750kg groundnut LOT-GND-260608-A.',
  'APPROVED'),
(1,'2026-06-08','DJ-2026-0608', 18750.000, 24500.000, 0.000, 12100.000, 6271.000, 0, 0,
  0, 0.000, 0.00,
  500.0, 1.10, 8.50, 88.0, 18, 1, 4.0,
  0, 0, 0, 0,
  'PB-2026-1002 progressed to NEUTRALIZING. Groundnut LOT-GND-260608-A received & weighed.',
  'Groundnut QC FFA 2.45% > spec 2.0%; vendor escalation raised, batch on hold pending RETEST.',
  'Complete neutralizing + bleaching for PB-2026-1002. Receive customer complaint resolution for LOT-RFD-260525.',
  'SUBMITTED');

-- 8. Register the new entities
INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('ProcessStageCatalog','Process Stage Catalog','m5_process_stage_catalog'),
  ('ProcessBatch',       'Process Batch',        'm5_process_batch'),
  ('ProcessStageLog',    'Process Stage Log',    'm5_process_stage_log'),
  ('DailyJournal',       'Daily Journal',        'm5_daily_journal'),
  ('Bom',                'BOM',                  'm5_bom'),
  ('BomLine',            'BOM Line',             'm5_bom_line'),
  ('ShiftHandover',      'Shift Handover',       'm5_shift_handover');
