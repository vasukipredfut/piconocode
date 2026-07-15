-- =====================================================================
-- V23 — Comprehensive Indian-statutory HR + Payroll backbone.
--
-- Existing employee_details (legacy 100+ varchar columns) is fragile;
-- this introduces a clean m7_employee table + the full set of supporting
-- structures (salary structure, components, statutory settings, payroll
-- run/line, attendance summary). Designer can stamp out forms over any of
-- these tables.
--
-- Indian statutory references used (all percentages confirmed):
--   - EPFO Act 1952: PF 12% employee + 12% employer
--                     Of employer 12%: 8.33% to EPS (capped at ₹15000 wage)
--                                       3.67% to EPF
--                     0.5% EDLI (employer) + 0.5% admin charges
--   - ESI Act 1948 (amended 2019): 0.75% employee + 3.25% employer
--                                   Up to ₹21000 gross wage ceiling
--   - Income Tax Act, New Regime FY 2024-25:
--          0-3L nil | 3-7L 5% | 7-10L 10% | 10-12L 15% | 12-15L 20% | >15L 30%
--          + standard deduction ₹75000 + rebate u/s 87A (income < ₹7L)
--   - Professional Tax (TN): ₹0/₹235/mo slabs; (MH): ₹0/₹175/₹200/mo
--   - Gratuity Act 1972: 4.81% of (basic+DA), paid after 5 yrs service
--   - Payment of Bonus Act 1965: 8.33%-20% of (basic+DA) capped at ₹7000/min wage
--   - Factories Act 1948 §59: OT = 2× ordinary wage for hours > 9/day or > 48/week
--   - LWF (TN): ₹20 emp + ₹50 employer annually
-- =====================================================================

-- ==================== 1. Employee Master ============================
CREATE TABLE IF NOT EXISTS m7_employee (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  emp_code VARCHAR(32) NOT NULL,
  -- Identity
  full_name VARCHAR(128) NOT NULL,
  father_or_spouse_name VARCHAR(128) NULL,
  gender VARCHAR(8) NULL,                       -- MALE/FEMALE/OTHER
  dob DATE NULL,
  marital_status VARCHAR(16) NULL,              -- SINGLE/MARRIED/DIVORCED/WIDOWED
  blood_group VARCHAR(4) NULL,                  -- A+/A-/B+/B-/O+/O-/AB+/AB-
  religion VARCHAR(32) NULL,
  category VARCHAR(16) NULL,                    -- GENERAL/OBC/SC/ST
  nationality VARCHAR(32) NULL DEFAULT 'Indian',
  language_known VARCHAR(128) NULL,             -- comma-separated
  photo_url VARCHAR(256) NULL,
  -- Contact
  mobile_primary VARCHAR(16) NULL,
  mobile_alt VARCHAR(16) NULL,
  email_personal VARCHAR(96) NULL,
  email_official VARCHAR(96) NULL,
  emergency_contact_name VARCHAR(96) NULL,
  emergency_contact_phone VARCHAR(16) NULL,
  emergency_contact_relation VARCHAR(32) NULL,
  current_address TEXT NULL,
  permanent_address TEXT NULL,
  city VARCHAR(64) NULL,
  state VARCHAR(64) NULL,
  pincode VARCHAR(10) NULL,
  country VARCHAR(64) NULL DEFAULT 'India',
  -- Employment
  designation VARCHAR(64) NULL,
  department VARCHAR(64) NULL,
  grade VARCHAR(16) NULL,                       -- L1/L2/L3 ...
  employment_type VARCHAR(16) NULL,             -- PERMANENT/CONTRACT/PROBATION/INTERN/CASUAL/APPRENTICE
  work_location VARCHAR(64) NULL,
  reports_to_emp_id BIGINT NULL,
  date_of_joining DATE NULL,
  probation_end_date DATE NULL,
  confirmation_date DATE NULL,
  separation_date DATE NULL,
  separation_reason VARCHAR(64) NULL,           -- RESIGNED/TERMINATED/RETIRED/ABSCONDED/EXPIRED
  notice_period_days INT NULL DEFAULT 30,
  shift_code VARCHAR(16) NULL,
  -- Identity proofs
  aadhaar_no VARCHAR(20) NULL,
  pan_no VARCHAR(16) NULL,
  voter_id VARCHAR(20) NULL,
  driving_licence VARCHAR(24) NULL,
  passport_no VARCHAR(20) NULL,
  uan_no VARCHAR(16) NULL,                      -- Universal Account Number for PF
  esic_ip_no VARCHAR(24) NULL,                  -- ESI Insurance Person number
  -- Bank
  bank_account_no VARCHAR(32) NULL,
  bank_ifsc VARCHAR(16) NULL,
  bank_name VARCHAR(64) NULL,
  bank_branch VARCHAR(64) NULL,
  bank_account_holder VARCHAR(96) NULL,        -- as per bank
  -- Statutory IDs
  pf_no VARCHAR(32) NULL,                       -- legacy PF number
  esi_no VARCHAR(24) NULL,                      -- legacy
  pt_state VARCHAR(8) NULL,                     -- TN/MH/KA — controls PT slab
  lwf_state VARCHAR(8) NULL,
  -- PF / Gratuity nominee
  pf_nominee_name VARCHAR(96) NULL,
  pf_nominee_relation VARCHAR(32) NULL,
  pf_nominee_share_pct DECIMAL(5,2) NULL DEFAULT 100.00,
  gratuity_nominee_name VARCHAR(96) NULL,
  gratuity_nominee_relation VARCHAR(32) NULL,
  -- Eligibility flags
  pf_applicable TINYINT(1) NOT NULL DEFAULT 1,
  esi_applicable TINYINT(1) NOT NULL DEFAULT 1,
  pt_applicable TINYINT(1) NOT NULL DEFAULT 1,
  tds_applicable TINYINT(1) NOT NULL DEFAULT 1,
  lwf_applicable TINYINT(1) NOT NULL DEFAULT 1,
  -- Misc
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE/INACTIVE/ON_NOTICE/SEPARATED
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_emp_code (tenant_id, emp_code),
  KEY idx_emp_status (tenant_id, status),
  KEY idx_emp_dept (department),
  KEY idx_emp_designation (designation)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Family / dependents
CREATE TABLE IF NOT EXISTS m7_employee_family (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  emp_id BIGINT NOT NULL,
  member_name VARCHAR(96) NOT NULL,
  relation VARCHAR(32) NOT NULL,                -- FATHER/MOTHER/SPOUSE/SON/DAUGHTER
  dob DATE NULL,
  occupation VARCHAR(64) NULL,
  is_esic_dependent TINYINT(1) NOT NULL DEFAULT 0,
  is_mediclaim_dependent TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_emp_fam (emp_id),
  CONSTRAINT fk_emp_fam FOREIGN KEY (emp_id) REFERENCES m7_employee(id) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Education / qualification
CREATE TABLE IF NOT EXISTS m7_employee_qualification (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  emp_id BIGINT NOT NULL,
  qualification VARCHAR(64) NOT NULL,            -- 10TH/12TH/DIPLOMA/UG/PG/PHD
  specialization VARCHAR(96) NULL,
  institute VARCHAR(128) NULL,
  university VARCHAR(128) NULL,
  year_of_passing INT NULL,
  percentage DECIMAL(5,2) NULL,
  CONSTRAINT fk_emp_qual FOREIGN KEY (emp_id) REFERENCES m7_employee(id) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Prior employment
CREATE TABLE IF NOT EXISTS m7_employee_experience (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  emp_id BIGINT NOT NULL,
  company VARCHAR(128) NOT NULL,
  designation VARCHAR(64) NULL,
  from_date DATE NULL,
  to_date DATE NULL,
  ctc_lpa DECIMAL(10,2) NULL,
  reason_for_leaving VARCHAR(128) NULL,
  CONSTRAINT fk_emp_exp FOREIGN KEY (emp_id) REFERENCES m7_employee(id) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ==================== 2. Salary Structure ==========================
CREATE TABLE IF NOT EXISTS m7_salary_component (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  component_type VARCHAR(16) NOT NULL,           -- EARNING / DEDUCTION / REIMBURSEMENT / EMPLOYER
  calc_method VARCHAR(16) NOT NULL,              -- FIXED / PERCENT / FORMULA
  percent_of VARCHAR(16) NULL,                   -- BASIC / GROSS / CTC
  default_percent DECIMAL(6,3) NULL,
  taxable TINYINT(1) NOT NULL DEFAULT 1,
  show_on_payslip TINYINT(1) NOT NULL DEFAULT 1,
  pf_eligible TINYINT(1) NOT NULL DEFAULT 0,
  esi_eligible TINYINT(1) NOT NULL DEFAULT 0,
  formula_text TEXT NULL,
  sort_order INT NOT NULL DEFAULT 100,
  UNIQUE KEY uq_comp (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Seed all standard Indian payroll components
INSERT IGNORE INTO m7_salary_component (tenant_id, code, name, component_type, calc_method, percent_of, default_percent, taxable, pf_eligible, esi_eligible, sort_order) VALUES
-- Earnings (taxable + statutory base)
(1,'BASIC',       'Basic Pay',                    'EARNING','PERCENT','CTC',50.000,1,1,1,10),
(1,'DA',          'Dearness Allowance',            'EARNING','PERCENT','BASIC',0.000,1,1,1,15),
(1,'HRA',         'House Rent Allowance',          'EARNING','PERCENT','BASIC',40.000,1,0,1,20),
(1,'CONVEYANCE',  'Conveyance Allowance',          'EARNING','FIXED',NULL,NULL,1,0,1,25),
(1,'SPECIAL',     'Special Allowance',             'EARNING','FORMULA',NULL,NULL,1,0,1,30),
(1,'MEDICAL_ALLOW','Medical Allowance',            'EARNING','FIXED',NULL,NULL,1,0,1,35),
(1,'LTA',         'Leave Travel Allowance',        'EARNING','FIXED',NULL,NULL,1,0,0,40),
(1,'CEA',         'Children Education Allowance',  'EARNING','FIXED',NULL,NULL,1,0,1,45),
(1,'HOSTEL_ALLOW','Hostel Allowance',              'EARNING','FIXED',NULL,NULL,1,0,1,50),
(1,'CCA',         'City Compensatory Allowance',   'EARNING','FIXED',NULL,NULL,1,0,1,55),
(1,'SHIFT_ALLOW', 'Shift Allowance',               'EARNING','FIXED',NULL,NULL,1,0,1,60),
(1,'OT',          'Overtime',                      'EARNING','FORMULA',NULL,NULL,1,1,1,65),
(1,'BONUS',       'Bonus',                         'EARNING','PERCENT','BASIC',8.330,1,0,0,70),
(1,'INCENTIVE',   'Performance Incentive',         'EARNING','FIXED',NULL,NULL,1,0,1,75),
(1,'ARREARS',     'Arrears',                       'EARNING','FIXED',NULL,NULL,1,0,1,80),
-- Reimbursements (non-taxable up to limits)
(1,'MEDICAL_REIMB','Medical Reimbursement',        'REIMBURSEMENT','FIXED',NULL,NULL,0,0,0,200),
(1,'FUEL_REIMB',  'Fuel Reimbursement',            'REIMBURSEMENT','FIXED',NULL,NULL,0,0,0,205),
(1,'TELEPHONE_REIMB','Telephone Reimbursement',    'REIMBURSEMENT','FIXED',NULL,NULL,0,0,0,210),
(1,'INTERNET_REIMB','Internet Reimbursement',      'REIMBURSEMENT','FIXED',NULL,NULL,0,0,0,215),
-- Deductions (employee share)
(1,'PF_EMP',      'PF Employee Contribution',      'DEDUCTION','PERCENT','BASIC',12.000,0,0,0,300),
(1,'ESI_EMP',     'ESI Employee Contribution',     'DEDUCTION','PERCENT','GROSS',0.750,0,0,0,310),
(1,'PT',          'Professional Tax',              'DEDUCTION','FORMULA',NULL,NULL,0,0,0,320),
(1,'TDS',         'TDS on Salary',                 'DEDUCTION','FORMULA',NULL,NULL,0,0,0,330),
(1,'LWF_EMP',     'LWF Employee Contribution',     'DEDUCTION','FIXED',NULL,NULL,0,0,0,340),
(1,'LOAN_RECOVER','Loan / Advance Recovery',       'DEDUCTION','FIXED',NULL,NULL,0,0,0,350),
(1,'LOP_DEDUCT',  'Loss of Pay Deduction',         'DEDUCTION','FORMULA',NULL,NULL,0,0,0,360),
(1,'OTHER_DEDUCT','Other Deduction',               'DEDUCTION','FIXED',NULL,NULL,0,0,0,370),
-- Employer side (CTC components — not paid to employee)
(1,'PF_EMP_ER',   'PF Employer (EPF 3.67%)',       'EMPLOYER','PERCENT','BASIC',3.670,0,0,0,400),
(1,'PF_EPS_ER',   'PF Employer (EPS 8.33%)',       'EMPLOYER','PERCENT','BASIC',8.330,0,0,0,405),
(1,'PF_EDLI_ER',  'PF EDLI 0.5%',                  'EMPLOYER','PERCENT','BASIC',0.500,0,0,0,410),
(1,'PF_ADMIN_ER', 'PF Admin Charges 0.5%',         'EMPLOYER','PERCENT','BASIC',0.500,0,0,0,415),
(1,'ESI_EMP_ER',  'ESI Employer 3.25%',            'EMPLOYER','PERCENT','GROSS',3.250,0,0,0,420),
(1,'LWF_EMP_ER',  'LWF Employer',                  'EMPLOYER','FIXED',NULL,NULL,0,0,0,425),
(1,'GRATUITY_ER', 'Gratuity Provision 4.81%',      'EMPLOYER','PERCENT','BASIC',4.810,0,0,0,430);

-- Per-employee salary card (annual structure)
CREATE TABLE IF NOT EXISTS m7_employee_salary (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  emp_id BIGINT NOT NULL,
  effective_from DATE NOT NULL,
  effective_to   DATE NULL,
  ctc_annual DECIMAL(12,2) NOT NULL,
  basic_monthly DECIMAL(12,2) NOT NULL,
  da_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  hra_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  conveyance_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  medical_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  special_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  lta_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  cea_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  hostel_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  cca_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  shift_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  bonus_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  incentive_monthly DECIMAL(12,2) NOT NULL DEFAULT 0,
  -- Reimbursements (capped, paid against bills)
  medical_reimb_yearly DECIMAL(12,2) NOT NULL DEFAULT 0,
  fuel_reimb_yearly DECIMAL(12,2) NOT NULL DEFAULT 0,
  telephone_reimb_yearly DECIMAL(12,2) NOT NULL DEFAULT 0,
  -- Variable
  variable_pay_yearly DECIMAL(12,2) NOT NULL DEFAULT 0,
  -- Snapshot of structure-level statutory employer-side numbers
  pf_employer_basic_capped TINYINT(1) NOT NULL DEFAULT 1,
  notes TEXT NULL,
  CONSTRAINT fk_emp_sal FOREIGN KEY (emp_id) REFERENCES m7_employee(id) ON DELETE CASCADE,
  KEY idx_emp_sal_eff (emp_id, effective_from)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ==================== 3. Statutory Settings ========================
CREATE TABLE IF NOT EXISTS m7_pf_setting (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  effective_from DATE NOT NULL,
  emp_rate_pct DECIMAL(5,3) NOT NULL DEFAULT 12.000,
  employer_rate_pct DECIMAL(5,3) NOT NULL DEFAULT 12.000,
  eps_share_pct DECIMAL(5,3) NOT NULL DEFAULT 8.330,
  epf_share_pct DECIMAL(5,3) NOT NULL DEFAULT 3.670,
  edli_pct DECIMAL(5,3) NOT NULL DEFAULT 0.500,
  admin_pct DECIMAL(5,3) NOT NULL DEFAULT 0.500,
  wage_ceiling DECIMAL(10,2) NOT NULL DEFAULT 15000.00,
  notes TEXT NULL
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO m7_pf_setting (tenant_id, effective_from) VALUES (1, '2024-04-01');

CREATE TABLE IF NOT EXISTS m7_esi_setting (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  effective_from DATE NOT NULL,
  emp_rate_pct DECIMAL(5,3) NOT NULL DEFAULT 0.750,
  employer_rate_pct DECIMAL(5,3) NOT NULL DEFAULT 3.250,
  gross_wage_ceiling DECIMAL(10,2) NOT NULL DEFAULT 21000.00,
  notes TEXT NULL
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO m7_esi_setting (tenant_id, effective_from) VALUES (1, '2024-04-01');

-- Professional Tax — slab table (state-wise).
-- TN PT is statutorily HALF-YEARLY (Article 276 cap Rs.2500/yr). MH PT is MONTHLY
-- with a male/female split (Apr-2023 amendment). KA PT is monthly.
-- cadence column tells the payroll engine how to divide/multiply when applying.
-- Source: TN State Web Portal Profession_Tax_Updated.pdf (Oct 2024 revised, 3-0 verified)
-- Source: ClearTax / TaxGuru cross-confirmed for MH male/female (3-0 verified)
CREATE TABLE IF NOT EXISTS m7_pt_slab (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  state_code VARCHAR(8) NOT NULL,                 -- TN/MH/KA/...
  cadence VARCHAR(16) NOT NULL DEFAULT 'MONTHLY',  -- MONTHLY / HALF_YEAR
  gender VARCHAR(8) NULL,                          -- NULL = both; M / F for split states
  effective_from DATE NOT NULL,
  gross_from DECIMAL(10,2) NOT NULL,               -- bound matches cadence (monthly or half-year)
  gross_to DECIMAL(10,2) NULL,                     -- NULL = infinity
  pt_amount DECIMAL(8,2) NOT NULL,                 -- amount per cadence period
  feb_surcharge DECIMAL(8,2) NULL,                 -- MH adds Rs.100 in Feb on top
  KEY idx_pt_state (state_code, effective_from)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO m7_pt_slab (tenant_id, state_code, cadence, gender, effective_from, gross_from, gross_to, pt_amount, feb_surcharge) VALUES
-- Tamil Nadu — Greater Chennai Corp Oct-2024 revised HALF-YEARLY slabs (verified 3-0)
(1,'TN','HALF_YEAR',NULL,'2024-10-01',      0.00, 21000.00,    0.00, NULL),
(1,'TN','HALF_YEAR',NULL,'2024-10-01',  21001.00, 30000.00,  180.00, NULL),
(1,'TN','HALF_YEAR',NULL,'2024-10-01',  30001.00, 45000.00,  425.00, NULL),
(1,'TN','HALF_YEAR',NULL,'2024-10-01',  45001.00, 60000.00,  930.00, NULL),
(1,'TN','HALF_YEAR',NULL,'2024-10-01',  60001.00, 75000.00, 1025.00, NULL),
(1,'TN','HALF_YEAR',NULL,'2024-10-01',  75001.00,     NULL, 1250.00, NULL),
-- Maharashtra MALE — monthly (Apr-2023 amendment, verified 3-0)
(1,'MH','MONTHLY','M','2023-04-01',     0.00,  7500.00,    0.00, NULL),
(1,'MH','MONTHLY','M','2023-04-01',  7501.00, 10000.00,  175.00, NULL),
(1,'MH','MONTHLY','M','2023-04-01', 10001.00,     NULL,  200.00, 100.00),
-- Maharashtra FEMALE — monthly with Rs.25,000 exemption (Apr-2023 amendment, verified 3-0)
(1,'MH','MONTHLY','F','2023-04-01',     0.00, 25000.00,    0.00, NULL),
(1,'MH','MONTHLY','F','2023-04-01', 25001.00,     NULL,  200.00, 100.00),
-- Karnataka — monthly
(1,'KA','MONTHLY',NULL,'2024-04-01',     0.00, 25000.00,    0.00, NULL),
(1,'KA','MONTHLY',NULL,'2024-04-01', 25001.00,     NULL,  200.00, NULL);

-- TDS slab — current (AY 2026-27 / FY 2025-26) + prior-year for back-period payrolls.
-- Source: incometax.gov.in (primary, verified 3-0)
-- Section 87A rebate: Rs.60,000 (new regime, FY 2025-26) -> nil tax up to Rs.12L
-- Standard deduction (salaried): Rs.75,000
CREATE TABLE IF NOT EXISTS m7_tds_slab (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  regime VARCHAR(8) NOT NULL,                     -- NEW / OLD
  fy VARCHAR(8) NOT NULL,                          -- 2025-26 (current) / 2024-25 (back period)
  annual_from DECIMAL(12,2) NOT NULL,
  annual_to DECIMAL(12,2) NULL,                    -- NULL = infinity
  rate_pct DECIMAL(5,2) NOT NULL,
  rebate_87a DECIMAL(10,2) NULL,                   -- rebate ceiling if applicable
  std_deduction DECIMAL(10,2) NULL,                -- salaried std deduction
  KEY idx_tds_regime (regime, fy)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;
-- New regime FY 2025-26 (AY 2026-27) — CURRENT
INSERT IGNORE INTO m7_tds_slab (tenant_id, regime, fy, annual_from, annual_to, rate_pct, rebate_87a, std_deduction) VALUES
(1,'NEW','2025-26',        0.00,   400000.00,   0.00, 60000.00, 75000.00),
(1,'NEW','2025-26',   400001.00,   800000.00,   5.00, 60000.00, 75000.00),
(1,'NEW','2025-26',   800001.00,  1200000.00,  10.00, 60000.00, 75000.00),
(1,'NEW','2025-26',  1200001.00,  1600000.00,  15.00,     NULL, 75000.00),
(1,'NEW','2025-26',  1600001.00,  2000000.00,  20.00,     NULL, 75000.00),
(1,'NEW','2025-26',  2000001.00,  2400000.00,  25.00,     NULL, 75000.00),
(1,'NEW','2025-26',  2400001.00,        NULL,  30.00,     NULL, 75000.00),
-- New regime FY 2024-25 (kept for back-period payrolls)
(1,'NEW','2024-25',        0.00,   300000.00,   0.00, 25000.00, 75000.00),
(1,'NEW','2024-25',   300001.00,   700000.00,   5.00, 25000.00, 75000.00),
(1,'NEW','2024-25',   700001.00,  1000000.00,  10.00, 25000.00, 75000.00),
(1,'NEW','2024-25',  1000001.00,  1200000.00,  15.00,     NULL, 75000.00),
(1,'NEW','2024-25',  1200001.00,  1500000.00,  20.00,     NULL, 75000.00),
(1,'NEW','2024-25',  1500001.00,        NULL,  30.00,     NULL, 75000.00),
-- Old regime (unchanged, both FYs)
(1,'OLD','2025-26',        0.00,   250000.00,   0.00, 12500.00, 50000.00),
(1,'OLD','2025-26',   250001.00,   500000.00,   5.00, 12500.00, 50000.00),
(1,'OLD','2025-26',   500001.00,  1000000.00,  20.00,     NULL, 50000.00),
(1,'OLD','2025-26',  1000001.00,        NULL,  30.00,     NULL, 50000.00);

-- LWF (state-wise)
CREATE TABLE IF NOT EXISTS m7_lwf_setting (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  state_code VARCHAR(8) NOT NULL,
  cadence VARCHAR(16) NOT NULL,                    -- ANNUAL / HALF_YEAR
  emp_amount DECIMAL(8,2) NOT NULL,
  employer_amount DECIMAL(8,2) NOT NULL,
  effective_from DATE NOT NULL,
  KEY idx_lwf_state (state_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO m7_lwf_setting (tenant_id, state_code, cadence, emp_amount, employer_amount, effective_from) VALUES
(1,'TN','ANNUAL',  20.00,  50.00, '2024-01-01'),
(1,'MH','HALF_YEAR', 12.00, 36.00, '2024-01-01'),
(1,'KA','ANNUAL',  20.00,  40.00, '2024-01-01');

-- ==================== 4. Attendance + Leave ========================
CREATE TABLE IF NOT EXISTS m7_attendance_summary (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  emp_id BIGINT NOT NULL,
  period VARCHAR(8) NOT NULL,                       -- YYYY-MM
  total_days INT NOT NULL,
  present_days DECIMAL(5,2) NOT NULL,
  weekly_off_days INT NOT NULL DEFAULT 0,
  holiday_days INT NOT NULL DEFAULT 0,
  paid_leave_days DECIMAL(5,2) NOT NULL DEFAULT 0,
  lop_days DECIMAL(5,2) NOT NULL DEFAULT 0,
  ot_hours DECIMAL(6,2) NOT NULL DEFAULT 0,
  late_count INT NOT NULL DEFAULT 0,
  early_out_count INT NOT NULL DEFAULT 0,
  missing_punch_count INT NOT NULL DEFAULT 0,
  payable_days DECIMAL(5,2) AS (present_days + paid_leave_days + holiday_days + weekly_off_days) VIRTUAL,
  UNIQUE KEY uq_att_emp_period (emp_id, period),
  KEY idx_att_period (period)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ==================== 5. Payroll Run Detail ========================
-- Augment m7_payroll_run with all component lines (one row per component per emp per run)
CREATE TABLE IF NOT EXISTS m7_payroll_line (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,                          -- FK to m7_payroll_run
  emp_id BIGINT NOT NULL,
  component_code VARCHAR(32) NOT NULL,
  component_type VARCHAR(16) NOT NULL,             -- EARNING/DEDUCTION/REIMBURSEMENT/EMPLOYER
  amount DECIMAL(12,2) NOT NULL,
  formula_used VARCHAR(256) NULL,
  base_amount DECIMAL(12,2) NULL,                  -- amount the % was applied to
  base_pct DECIMAL(6,3) NULL,
  KEY idx_pl_run (run_id),
  KEY idx_pl_emp (emp_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ==================== 6. Statutory Returns Tracker =================
CREATE TABLE IF NOT EXISTS m7_statutory_return_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  return_code VARCHAR(32) NOT NULL,
  return_name VARCHAR(128) NOT NULL,
  authority VARCHAR(32) NOT NULL,                  -- EPFO/ESIC/IT/GST/MCA/FSSAI/STATE
  cadence VARCHAR(16) NOT NULL,                    -- MONTHLY/QUARTERLY/HALF_YEAR/ANNUAL
  due_day_of_period INT NULL,
  applicable TINYINT(1) NOT NULL DEFAULT 1,
  next_due_date DATE NULL,
  last_filed_period VARCHAR(16) NULL,
  last_filed_date DATE NULL,
  notes TEXT NULL
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_statutory_return_catalog (tenant_id, return_code, return_name, authority, cadence, due_day_of_period, applicable) VALUES
(1,'PF_ECR','PF Electronic Challan-cum-Return','EPFO','MONTHLY',15,1),
(1,'PF_3A6A','PF Annual Return Form 3A/6A','EPFO','ANNUAL',30,1),
(1,'ESI_CHALLAN','ESIC Monthly Contribution Challan','ESIC','MONTHLY',15,1),
(1,'ESI_HALFYR','ESIC Half-Yearly Return','ESIC','HALF_YEAR',12,1),
(1,'TDS_24Q','TDS Form 24Q (Salary)','IT','QUARTERLY',31,1),
(1,'TDS_26Q','TDS Form 26Q (Other)','IT','QUARTERLY',31,1),
(1,'FORM_16','Form 16','IT','ANNUAL',15,1),
(1,'GSTR1','GSTR-1 (Outward Supplies)','GST','MONTHLY',11,1),
(1,'GSTR3B','GSTR-3B (Summary)','GST','MONTHLY',20,1),
(1,'GSTR9','GSTR-9 (Annual)','GST','ANNUAL',31,1),
(1,'EINVOICE_IRN','e-Invoice IRN via NIC','GST','MONTHLY',NULL,1),
(1,'EWAY','e-Waybill','GST','MONTHLY',NULL,1),
(1,'PT_RETURN','Professional Tax Return','STATE','MONTHLY',NULL,1),
(1,'MSME_1','MSME-1 (Delayed Payments)','MCA','HALF_YEAR',30,1),
(1,'LWF_RETURN','Labour Welfare Fund Return','STATE','ANNUAL',NULL,1),
(1,'FACTORIES_21','Factories Act Form 21 (Annual)','STATE','ANNUAL',NULL,1),
(1,'FACTORIES_22','Factories Act Form 22 (Half-Yr)','STATE','HALF_YEAR',NULL,1),
(1,'AOC_4','Companies Act AOC-4','MCA','ANNUAL',30,1),
(1,'MGT_7','Companies Act MGT-7','MCA','ANNUAL',60,1),
(1,'DPT_3','Companies Act DPT-3 (Deposits)','MCA','ANNUAL',30,1),
(1,'FSSAI_D1','FSSAI Annual Return D1','FSSAI','ANNUAL',31,1),
(1,'PF_NOMINEE_FORM2','PF Nominee Form 2','EPFO','EVENT',NULL,1);

-- ==================== 7. Register entities for Designer =============
INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('EmployeeMaster','Employee Master','m7_employee'),
  ('EmployeeFamily','Family Member','m7_employee_family'),
  ('EmployeeQualification','Qualification','m7_employee_qualification'),
  ('EmployeeExperience','Prior Experience','m7_employee_experience'),
  ('SalaryComponent','Salary Component','m7_salary_component'),
  ('EmployeeSalary','Employee Salary Card','m7_employee_salary'),
  ('PFSetting','PF Setting','m7_pf_setting'),
  ('ESISetting','ESI Setting','m7_esi_setting'),
  ('PTSlab','PT Slab','m7_pt_slab'),
  ('TDSSlab','TDS Slab','m7_tds_slab'),
  ('LWFSetting','LWF Setting','m7_lwf_setting'),
  ('AttendanceSummary','Attendance Summary','m7_attendance_summary'),
  ('PayrollLine','Payroll Line','m7_payroll_line'),
  ('StatutoryReturnCatalog','Statutory Return Catalog','m7_statutory_return_catalog');

-- ==================== 8. Seed sample employee ======================
INSERT IGNORE INTO m7_employee (
  tenant_id, emp_code, full_name, father_or_spouse_name, gender, dob, marital_status,
  blood_group, language_known, mobile_primary, email_personal,
  current_address, city, state, pincode,
  designation, department, grade, employment_type, work_location,
  date_of_joining, aadhaar_no, pan_no, uan_no, esic_ip_no,
  bank_account_no, bank_ifsc, bank_name, bank_branch, bank_account_holder,
  pf_no, esi_no, pt_state, lwf_state,
  pf_nominee_name, pf_nominee_relation, pf_nominee_share_pct,
  pf_applicable, esi_applicable, pt_applicable, tds_applicable
) VALUES
(1,'EMP-001','Ramesh Kumar','Govindan','MALE','1985-04-12','MARRIED','O+','ta,en','+919840012345','ramesh.k@pico.in',
 '12 Anna Salai','Coimbatore','TN','641001',
 'Production Supervisor','PRODUCTION','L3','PERMANENT','Coimbatore Plant',
 '2018-06-01','512345678901','ABCDE1234F','100123456789','3300012345',
 '12345678901234','SBIN0001234','State Bank of India','Coimbatore Main','RAMESH KUMAR',
 'TN/MAS/12345/000','3300012345','TN','TN',
 'Lakshmi','SPOUSE',100.00,
 1,1,1,1),
(1,'EMP-002','Priya Selvam','Selvam','FEMALE','1990-09-25','MARRIED','A+','ta,en,hi','+919900012346','priya@pico.in',
 '45 Race Course','Coimbatore','TN','641018',
 'QC Chemist','QUALITY','L2','PERMANENT','Coimbatore Plant',
 '2020-01-15','512345678902','BCDEF2345G','100123456790','3300012346',
 '12345678901235','HDFC0001235','HDFC Bank','Coimbatore Race Course','PRIYA SELVAM',
 'TN/MAS/12345/001','3300012346','TN','TN',
 'Selvam','SPOUSE',100.00,
 1,1,1,1),
(1,'EMP-003','Arun Murugan','Murugan','MALE','1995-12-08','SINGLE','B+','ta,en','+919900012347','arun.m@pico.in',
 '78 Saibaba Colony','Coimbatore','TN','641011',
 'Trainee Operator','PRODUCTION','L1','PROBATION','Coimbatore Plant',
 '2024-12-01','512345678903','CDEFG3456H','100123456791','3300012347',
 '12345678901236','ICIC0001236','ICICI Bank','Saibaba Colony','ARUN MURUGAN',
 'TN/MAS/12345/002','3300012347','TN','TN',
 'Lalitha','MOTHER',100.00,
 1,1,1,1);

-- Seed salary cards for the same 3 employees
INSERT IGNORE INTO m7_employee_salary
  (tenant_id, emp_id, effective_from, ctc_annual, basic_monthly, hra_monthly,
   conveyance_monthly, medical_monthly, special_monthly)
SELECT 1, e.id, '2024-04-01',
       CASE e.emp_code WHEN 'EMP-001' THEN 540000.00 WHEN 'EMP-002' THEN 480000.00 ELSE 240000.00 END,
       CASE e.emp_code WHEN 'EMP-001' THEN 22500.00  WHEN 'EMP-002' THEN 20000.00  ELSE 10000.00 END,
       CASE e.emp_code WHEN 'EMP-001' THEN  9000.00  WHEN 'EMP-002' THEN  8000.00  ELSE  4000.00 END,
       1600.00, 1250.00,
       CASE e.emp_code WHEN 'EMP-001' THEN 10650.00 WHEN 'EMP-002' THEN  9150.00 ELSE  3150.00 END
  FROM m7_employee e
  WHERE NOT EXISTS (SELECT 1 FROM m7_employee_salary s WHERE s.emp_id = e.id);
