-- =====================================================================
-- V30 — Raid-readiness compliance vault.
--
-- User asked: 'tomorrow income tax raid may come, labour law raid..
-- all we need to have document and pull right'.
-- This migration adds the tables a plant manager would need to instantly
-- pull when IT or Labour inspectors walk in.
--
-- IT Raid:        TDS challans · day book · bank recon · director register
--                  · vendor KYC (PAN/MSME)
-- Labour / Fact:  licence renewals · accident register (Form 15) ·
--                  safety committee · drill log · visitor / contractor
--                  · POSH · apprentice · health check
-- =====================================================================

-- =================================================================
-- 1. LICENCE RENEWAL TRACKER (factory lic / IBR / PCB / Fire NOC / FSSAI / GST etc.)
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_license_renewal (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  licence_code VARCHAR(32) NOT NULL,
  licence_type VARCHAR(32) NOT NULL,             -- FACTORY/IBR/PCB_CTE/PCB_CTO/FIRE_NOC/FSSAI/GST/LABOUR/MSME_UDYAM/TRADE/SHOP_EST
  licence_no VARCHAR(64) NOT NULL,
  issuing_authority VARCHAR(96) NOT NULL,
  issue_date DATE NOT NULL,
  valid_from DATE NULL,
  valid_to DATE NOT NULL,
  renewal_fee DECIMAL(12,2) NULL,
  renewal_cycle_months INT NULL,                  -- 12/24/36/60
  document_url VARCHAR(255) NULL,
  responsible_emp_id BIGINT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE/EXPIRED/UNDER_RENEWAL/REVOKED
  notes TEXT NULL,
  days_to_expiry INT GENERATED ALWAYS AS (DATEDIFF(valid_to, CURRENT_DATE)) VIRTUAL,
  expiry_bucket VARCHAR(16) GENERATED ALWAYS AS (
    CASE
      WHEN DATEDIFF(valid_to, CURRENT_DATE) < 0    THEN 'EXPIRED'
      WHEN DATEDIFF(valid_to, CURRENT_DATE) <= 30  THEN 'CRITICAL'
      WHEN DATEDIFF(valid_to, CURRENT_DATE) <= 90  THEN 'WARNING'
      ELSE 'OK'
    END
  ) VIRTUAL,
  UNIQUE KEY uq_lic (tenant_id, licence_code),
  KEY idx_lic_type (licence_type, valid_to)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_license_renewal (tenant_id, licence_code, licence_type, licence_no, issuing_authority,
  issue_date, valid_to, renewal_fee, renewal_cycle_months, status, notes) VALUES
(1,'LIC-FACTORY-001','FACTORY','TN/FAC/CBE/2023/01188','Dept of Industrial Safety & Health, TN','2023-01-15','2026-01-14',25000.00,36,'EXPIRED','License expired Jan 2026 — renewal pending'),
(1,'LIC-IBR-001',    'IBR',    'IBR/TN/2024/4421',    'Director of Boilers, TN',                 '2024-08-12','2025-08-11',18500.00,12,'EXPIRED','Boiler statutory inspection due'),
(1,'LIC-PCB-CTE-001','PCB_CTE','TNPCB/CTE/CBE/2018/01122','Tamil Nadu Pollution Control Board',  '2018-04-01','2028-03-31',45000.00,120,'ACTIVE','Consent to Establish — 10-yr validity'),
(1,'LIC-PCB-CTO-001','PCB_CTO','TNPCB/CTO/CBE/2024/02234','Tamil Nadu Pollution Control Board',  '2024-04-01','2026-09-30',65000.00,30,'ACTIVE','Consent to Operate — to be renewed'),
(1,'LIC-FIRE-001',  'FIRE_NOC','TNFRD/NOC/CBE/2024/0712','TN Fire & Rescue Services',            '2024-05-10','2026-05-09',12000.00,24,'ACTIVE','Critical renewal: <30 days'),
(1,'LIC-FSSAI-001', 'FSSAI',  'FSSAI-22318011000451','Food Safety & Standards Authority of India','2023-08-22','2028-08-21',7500.00,60,'ACTIVE','5-yr Central licence'),
(1,'LIC-GST-001',   'GST',    '33AAACS1234L1ZA',     'CBIC',                                     '2017-07-01','9999-12-31',0.00,NULL,'ACTIVE','GSTIN - permanent'),
(1,'LIC-LWF-001',   'LABOUR', 'TN/LWF/CBE/2020/00118','Office of Labour Commissioner, TN',       '2020-04-01','2027-03-31',2500.00,84,'ACTIVE','LWF Registration — 7-yr cycle'),
(1,'LIC-UDYAM-001', 'MSME_UDYAM','UDYAM-TN-04-0023118','Ministry of MSME',                       '2020-07-01','9999-12-31',0.00,NULL,'ACTIVE','Udyam — permanent'),
(1,'LIC-TRADE-001', 'TRADE',  'CCMC/TRADE/2024/1188', 'Coimbatore City Municipal Corp',          '2024-04-01','2027-03-31',8500.00,36,'ACTIVE','Trade Licence');

-- =================================================================
-- 2. ACCIDENT REGISTER (Factories Act Form 15)
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_accident_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  accident_no VARCHAR(32) NOT NULL,
  occurred_at DATETIME NOT NULL,
  reported_at DATETIME NULL,
  location VARCHAR(96) NULL,
  injured_emp_id BIGINT NULL,
  injured_name VARCHAR(128) NULL,
  injured_designation VARCHAR(64) NULL,
  age INT NULL,
  gender VARCHAR(8) NULL,
  body_part_affected VARCHAR(96) NULL,
  injury_type VARCHAR(32) NULL,                  -- CUT/BURN/FRACTURE/SLIP/CHEMICAL/CRUSH/ELECTRIC/EYE/OTHER
  severity VARCHAR(16) NOT NULL,                  -- NEAR_MISS/FIRST_AID/REPORTABLE/LTI/FATAL
  cause_category VARCHAR(32) NULL,                -- MACHINE/MANUAL_HANDLING/SLIP_TRIP/CHEMICAL/HEAT/ELECTRICAL/OTHER
  description TEXT NULL,
  first_aid_given_by VARCHAR(96) NULL,
  hospital_referred VARCHAR(96) NULL,
  days_lost INT NULL,
  -- Statutory reporting
  reported_to_inspector TINYINT(1) NOT NULL DEFAULT 0,
  inspector_ref VARCHAR(64) NULL,
  reported_to_esic TINYINT(1) NOT NULL DEFAULT 0,
  esic_ref VARCHAR(64) NULL,
  -- Investigation
  investigated_by_emp_id BIGINT NULL,
  root_cause TEXT NULL,
  corrective_action TEXT NULL,
  capa_closed_at DATETIME NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',     -- OPEN/INVESTIGATING/CAPA_PENDING/CLOSED
  UNIQUE KEY uq_acc (tenant_id, accident_no),
  KEY idx_acc_date (occurred_at, severity)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_accident_log (tenant_id, accident_no, occurred_at, reported_at, location,
  injured_emp_id, injured_name, injured_designation, age, gender, body_part_affected, injury_type,
  severity, cause_category, description, first_aid_given_by, days_lost,
  reported_to_inspector, reported_to_esic, root_cause, corrective_action, status) VALUES
(1,'ACC-2026-001','2026-03-15 14:30:00','2026-03-15 14:45:00','Expeller Area — PRESS-1',
  3, 'Arun Murugan', 'Trainee Operator', 30, 'MALE', 'LEFT HAND', 'CUT',
  'FIRST_AID', 'MACHINE','Hand bruise while loading seed hopper — sleeve caught in feed inlet',
  'Priya S. (QC)', 0, 0, 0,
  'Wide-fit sleeve + missed lockout','Replaced with tight-fit sleeves; refresher training',
  'CLOSED'),
(1,'ACC-2026-002','2026-05-22 11:10:00','2026-05-22 11:30:00','Packing Line PACK-LINE-1',
  NULL,'Ravi K.','Contract Operator', 38,'MALE','RIGHT FOOT','SLIP',
  'REPORTABLE','SLIP_TRIP','Slipped on oil spill near filler — minor sprain, 1 day rest',
  'Murugan (Supervisor)', 1, 1, 1,
  'Spillage not cleared promptly','5S audit + spill response training','CLOSED'),
(1,'ACC-2026-003','2026-06-08 16:45:00','2026-06-08 17:00:00','Refining Section DEOD-1',
  NULL,'Selvi R.','Helper', 33,'FEMALE','RIGHT EYE','CHEMICAL',
  'REPORTABLE','CHEMICAL','Caustic splash during NaOH transfer — eye wash given, hospital referred',
  'Priya S. (QC)', 2, 1, 1,
  'Face shield not worn','Mandatory PPE check + interlocked dosing pump','INVESTIGATING');

-- =================================================================
-- 3. SAFETY COMMITTEE + EMERGENCY DRILLS
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_safety_committee_meeting (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  meeting_no VARCHAR(32) NOT NULL,
  meeting_date DATE NOT NULL,
  meeting_type VARCHAR(16) NOT NULL,              -- MONTHLY/QUARTERLY/EMERGENCY
  chair_emp_id BIGINT NULL,
  attendees TEXT NULL,
  attendees_count INT NULL,
  agenda TEXT NULL,
  key_decisions TEXT NULL,
  action_items TEXT NULL,
  minutes_url VARCHAR(255) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'CLOSED',
  UNIQUE KEY uq_safety_meet (tenant_id, meeting_no),
  KEY idx_meet_date (meeting_date)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_safety_committee_meeting (tenant_id, meeting_no, meeting_date, meeting_type,
  chair_emp_id, attendees_count, agenda, key_decisions, action_items) VALUES
(1,'SCM-2026-001','2026-04-15','MONTHLY', 1, 9,
   'Q4 incident review, PPE compliance, expeller area lockout-tagout audit',
   'Mandatory face shield in NaOH area; weekly 5S audit; quarterly drill calendar approved',
   'Procure 25 face shields by 30-Apr; biometric LOTO by 15-May; drill schedule by 30-Apr'),
(1,'SCM-2026-002','2026-05-20','MONTHLY', 1, 8,
   'NaOH splash incident ACC-2026-003 RCA; mock drill outcome',
   'Interlocked dosing pump approved; emergency stop button audit',
   'Dosing pump purchase order by 30-May; ESD button check by 10-Jun'),
(1,'SCM-2026-003','2026-06-15','MONTHLY', 1, 9,
   'CAPA review for ACC-2026-003; pre-monsoon readiness audit',
   'Drainage cleaning approved; emergency lighting upgrade',
   'Drainage works by 30-Jun; LED emergency lights by 15-Jul');

CREATE TABLE IF NOT EXISTS m5_emergency_drill_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  drill_no VARCHAR(32) NOT NULL,
  drill_date DATE NOT NULL,
  drill_type VARCHAR(16) NOT NULL,                -- FIRE/CHEMICAL_SPILL/EVACUATION/FIRST_AID/RESCUE
  area VARCHAR(64) NULL,
  conducted_by_emp_id BIGINT NULL,
  participants_count INT NULL,
  evacuation_time_sec INT NULL,
  target_time_sec INT NULL,
  observations TEXT NULL,
  gaps_identified TEXT NULL,
  corrective_action TEXT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'CONDUCTED', -- CONDUCTED/CAPA_PENDING/CLOSED
  UNIQUE KEY uq_drill (tenant_id, drill_no),
  KEY idx_drill_date (drill_date, drill_type)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_emergency_drill_log (tenant_id, drill_no, drill_date, drill_type, area, conducted_by_emp_id,
  participants_count, evacuation_time_sec, target_time_sec, observations, gaps_identified, corrective_action) VALUES
(1,'DRL-2026-001','2026-01-22','FIRE',         'Main Plant', 1, 145, 280, 240,'Fire alarm response good; lift used by 2 — must use stairs','3 unaccounted in muster','Refresher briefing + signage upgrade'),
(1,'DRL-2026-002','2026-04-10','EVACUATION',   'Refining',    1, 38,  145, 180,'Quick muster; good lead from supervisors','None','—'),
(1,'DRL-2026-003','2026-05-15','CHEMICAL_SPILL','NaOH bund area',1,12,180,150,'Reaction OK; PPE donning slow','PPE storage too far','Local PPE cabinet installed');

-- =================================================================
-- 4. VISITOR + CONTRACTOR / CONTRACT LABOUR (CLRA register)
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_visitor_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  visit_no VARCHAR(32) NOT NULL,
  visit_date DATE NOT NULL,
  visitor_name VARCHAR(96) NOT NULL,
  visitor_company VARCHAR(96) NULL,
  visitor_mobile VARCHAR(16) NULL,
  visitor_id_proof VARCHAR(32) NULL,            -- AADHAAR/PAN/PASSPORT/DL
  visitor_id_number VARCHAR(32) NULL,
  purpose VARCHAR(96) NULL,
  whom_to_meet_emp_id BIGINT NULL,
  whom_to_meet_name VARCHAR(96) NULL,
  area_access TEXT NULL,                         -- comma list e.g. OFFICE,FLOOR-1
  badge_no VARCHAR(16) NULL,
  in_time DATETIME NOT NULL,
  out_time DATETIME NULL,
  duration_min INT GENERATED ALWAYS AS (
    CASE WHEN out_time IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, in_time, out_time) ELSE NULL END
  ) VIRTUAL,
  vehicle_no VARCHAR(16) NULL,
  ppe_issued TINYINT(1) NOT NULL DEFAULT 0,
  hygiene_brief_given TINYINT(1) NOT NULL DEFAULT 0,
  notes TEXT NULL,
  UNIQUE KEY uq_visit (tenant_id, visit_no),
  KEY idx_visit_date (visit_date)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_visitor_log (tenant_id, visit_no, visit_date, visitor_name, visitor_company,
  visitor_mobile, visitor_id_proof, visitor_id_number, purpose, whom_to_meet_name, area_access,
  badge_no, in_time, out_time, vehicle_no, ppe_issued, hygiene_brief_given) VALUES
(1,'VIS-2026-1101','2026-06-08','Rajesh Kumar','FSSAI Auditor','+919900812001','AADHAAR','XXXX-XXXX-1234',
  'FSSAI surveillance audit','Plant Manager','OFFICE,QC,REFINING','BG-101','2026-06-08 09:30:00','2026-06-08 16:30:00','TN37AB7700',1,1),
(1,'VIS-2026-1102','2026-06-09','Maintainer Pvt Ltd','Spare-parts vendor','+919900812002','AADHAAR','XXXX-XXXX-5678',
  'Press-1 worm shaft replacement quote','Maintenance Head','MAINTENANCE,STORES','BG-102','2026-06-09 11:00:00',NULL,'TN37BA8821',1,0);

CREATE TABLE IF NOT EXISTS m5_contractor (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  contractor_code VARCHAR(32) NOT NULL,
  contractor_name VARCHAR(160) NOT NULL,
  service_category VARCHAR(48) NULL,             -- HOUSEKEEPING/SECURITY/MAINTENANCE/LOADING/CIVIL/CATERING
  agreement_no VARCHAR(48) NULL,
  agreement_from DATE NULL,
  agreement_to DATE NULL,
  contract_value DECIMAL(14,2) NULL,
  -- Statutory registrations of the contractor
  clra_licence_no VARCHAR(48) NULL,              -- Contract Labour Regulation & Abolition Act licence
  clra_licence_to DATE NULL,
  gstin VARCHAR(16) NULL,
  pan VARCHAR(16) NULL,
  pf_registration VARCHAR(32) NULL,
  esi_registration VARCHAR(32) NULL,
  contact_person VARCHAR(96) NULL,
  contact_phone VARCHAR(16) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uq_contractor (tenant_id, contractor_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_contractor (tenant_id, contractor_code, contractor_name, service_category, agreement_no,
  agreement_from, agreement_to, contract_value, clra_licence_no, clra_licence_to, gstin, pan,
  pf_registration, esi_registration, contact_person, contact_phone, status) VALUES
(1,'CON-HK-001','SriGanesh Housekeeping','HOUSEKEEPING','AGR-2025-HK-01','2025-04-01','2026-03-31',420000.00,
  'CLRA/TN/CBE/2024/0118','2027-04-01','33AABCS5821L1Z3','AABCS5821L','TN/PF/MAS/12388','33-001818-12','S. Ganesh','+919800012001','ACTIVE'),
(1,'CON-SEC-001','TVS Security Services','SECURITY','AGR-2025-SEC-01','2025-04-01','2027-03-31',1250000.00,
  'CLRA/TN/CBE/2023/0091','2026-04-01','33AABCT4421L1ZA','AABCT4421L','TN/PF/MAS/12099','33-001212-11','R. Karthik','+919800012002','ACTIVE'),
(1,'CON-LDG-001','BalajiLoading & Allied','LOADING','AGR-2025-LD-01','2025-07-01','2026-06-30',880000.00,
  'CLRA/TN/CBE/2025/0212','2028-07-01','33AABCB1188L1ZB','AABCB1188L','TN/PF/MAS/12555','33-002211-09','M. Balaji','+919800012003','ACTIVE');

CREATE TABLE IF NOT EXISTS m5_contractor_labour (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  contractor_id BIGINT NOT NULL,
  emp_full_name VARCHAR(128) NOT NULL,
  father_name VARCHAR(128) NULL,
  designation VARCHAR(64) NULL,
  aadhaar_last4 VARCHAR(4) NULL,
  pan VARCHAR(16) NULL,
  uan VARCHAR(16) NULL,
  esic_ip VARCHAR(24) NULL,
  joining_date DATE NULL,
  separation_date DATE NULL,
  gender VARCHAR(8) NULL,
  age INT NULL,
  daily_wage DECIMAL(10,2) NULL,
  esic_wage_pct DECIMAL(5,2) NULL,
  pf_applicable TINYINT(1) NOT NULL DEFAULT 1,
  esi_applicable TINYINT(1) NOT NULL DEFAULT 1,
  badge_no VARCHAR(16) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_cl_contractor FOREIGN KEY (contractor_id) REFERENCES m5_contractor(id) ON DELETE CASCADE,
  KEY idx_cl_active (contractor_id, active)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_contractor_labour (tenant_id, contractor_id, emp_full_name, father_name, designation,
  aadhaar_last4, joining_date, gender, age, daily_wage, pf_applicable, esi_applicable, badge_no)
SELECT 1, c.id,'Ramu Pandi','Murugesan','Housekeeping Staff','1122','2025-04-15','MALE',28, 450.00, 1, 1,'HK-001'
  FROM m5_contractor c WHERE c.contractor_code='CON-HK-001';
INSERT IGNORE INTO m5_contractor_labour (tenant_id, contractor_id, emp_full_name, father_name, designation,
  aadhaar_last4, joining_date, gender, age, daily_wage, pf_applicable, esi_applicable, badge_no)
SELECT 1, c.id,'Lakshmi V.','Vairavan','Housekeeping Staff','2233','2025-04-20','FEMALE',32, 420.00, 1, 1,'HK-002'
  FROM m5_contractor c WHERE c.contractor_code='CON-HK-001';
INSERT IGNORE INTO m5_contractor_labour (tenant_id, contractor_id, emp_full_name, father_name, designation,
  aadhaar_last4, joining_date, gender, age, daily_wage, pf_applicable, esi_applicable, badge_no)
SELECT 1, c.id,'Suresh M.','Mohan','Security Guard','3344','2025-04-10','MALE',45, 550.00, 1, 1,'SEC-001'
  FROM m5_contractor c WHERE c.contractor_code='CON-SEC-001';
INSERT IGNORE INTO m5_contractor_labour (tenant_id, contractor_id, emp_full_name, father_name, designation,
  aadhaar_last4, joining_date, gender, age, daily_wage, pf_applicable, esi_applicable, badge_no)
SELECT 1, c.id,'Mahesh P.','Pandian','Loader','4455','2025-07-05','MALE',26, 600.00, 1, 1,'LD-001'
  FROM m5_contractor c WHERE c.contractor_code='CON-LDG-001';
INSERT IGNORE INTO m5_contractor_labour (tenant_id, contractor_id, emp_full_name, father_name, designation,
  aadhaar_last4, joining_date, gender, age, daily_wage, pf_applicable, esi_applicable, badge_no)
SELECT 1, c.id,'Gowtham R.','Ramamurthy','Loader','5566','2025-07-05','MALE',24, 600.00, 1, 1,'LD-002'
  FROM m5_contractor c WHERE c.contractor_code='CON-LDG-001';

-- =================================================================
-- 5. APPRENTICE REGISTER (Apprentices Act 1961)
-- =================================================================
CREATE TABLE IF NOT EXISTS m7_apprentice (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  apprentice_code VARCHAR(32) NOT NULL,
  emp_full_name VARCHAR(128) NOT NULL,
  father_name VARCHAR(128) NULL,
  trade VARCHAR(64) NOT NULL,                    -- e.g. FITTER/ELECTRICIAN/MILLWRIGHT
  trade_code VARCHAR(8) NULL,                    -- NCVT code
  qualification VARCHAR(64) NULL,
  institute VARCHAR(160) NULL,
  registration_no VARCHAR(48) NULL,              -- portal.apprenticeshipindia.gov.in reg
  start_date DATE NOT NULL,
  end_date DATE NULL,
  stipend_monthly DECIMAL(10,2) NULL,
  supervisor_emp_id BIGINT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE/COMPLETED/TERMINATED/ABSCONDED
  completion_cert_no VARCHAR(48) NULL,
  UNIQUE KEY uq_apprentice (tenant_id, apprentice_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_apprentice (tenant_id, apprentice_code, emp_full_name, father_name, trade, trade_code,
  qualification, institute, registration_no, start_date, end_date, stipend_monthly, status) VALUES
(1,'APR-2026-01','Vijay Anand','Anand Kumar','Fitter','FIT','ITI','Govt ITI Coimbatore','APP/TN/2025/12011','2025-08-01','2026-07-31',9100.00,'ACTIVE'),
(1,'APR-2026-02','Karthik R.','Ramesh','Electrician','ELE','ITI','Pollachi ITI','APP/TN/2025/12022','2025-08-01','2026-07-31',9100.00,'ACTIVE'),
(1,'APR-2026-03','Praveen S.','Senthilvel','Machinist','MAC','Dip','Govt Poly Coimbatore','APP/TN/2024/11888','2024-09-01','2026-08-31',10500.00,'ACTIVE');

-- =================================================================
-- 6. POSH COMMITTEE
-- =================================================================
CREATE TABLE IF NOT EXISTS m7_posh_committee (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  member_role VARCHAR(24) NOT NULL,              -- PRESIDING/INTERNAL/EXTERNAL/MEMBER
  emp_id BIGINT NULL,
  external_name VARCHAR(96) NULL,
  external_org VARCHAR(96) NULL,
  external_email VARCHAR(96) NULL,
  external_phone VARCHAR(16) NULL,
  gender VARCHAR(8) NOT NULL,
  effective_from DATE NOT NULL,
  effective_to DATE NULL,
  appointment_letter_url VARCHAR(255) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_posh_committee (tenant_id, member_role, emp_id, gender, effective_from) VALUES
(1,'PRESIDING',  2,'FEMALE','2025-04-01'),   -- Priya
(1,'INTERNAL',   1,'MALE','2025-04-01');
INSERT IGNORE INTO m7_posh_committee (tenant_id, member_role, external_name, external_org, external_email, external_phone, gender, effective_from) VALUES
(1,'EXTERNAL','Adv. Hema Pillai','Pillai & Associates, Coimbatore','hema@pillai.law','+919800018800','FEMALE','2025-04-01');

CREATE TABLE IF NOT EXISTS m7_posh_incident (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  case_no VARCHAR(32) NOT NULL,
  filed_at DATE NOT NULL,
  complainant_emp_id BIGINT NULL,
  respondent_emp_id BIGINT NULL,
  nature_of_complaint TEXT NULL,
  inquiry_started_at DATE NULL,
  inquiry_closed_at DATE NULL,
  findings TEXT NULL,
  action_taken TEXT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',     -- OPEN/INQUIRY/CLOSED/APPEALED
  reported_in_annual_return TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY uq_posh (tenant_id, case_no)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- =================================================================
-- 7. HEALTH CHECK RECORDS (Factories Act Form 7 / annual statutory)
-- =================================================================
CREATE TABLE IF NOT EXISTS m7_health_check (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  emp_id BIGINT NULL,
  contract_labour_id BIGINT NULL,
  emp_name VARCHAR(128) NOT NULL,
  check_type VARCHAR(24) NOT NULL,                -- ANNUAL/PRE_EMPLOYMENT/RETURN_FROM_LEAVE/EXIT
  check_date DATE NOT NULL,
  doctor_name VARCHAR(96) NULL,
  clinic VARCHAR(96) NULL,
  bp_systolic INT NULL,
  bp_diastolic INT NULL,
  heart_rate INT NULL,
  vision VARCHAR(8) NULL,                          -- e.g. 6/6
  audiometry VARCHAR(16) NULL,
  lung_function_pct DECIMAL(5,2) NULL,
  bmi DECIMAL(5,2) NULL,
  ecg_normal TINYINT(1) NULL,
  blood_sugar_random DECIMAL(6,2) NULL,
  haemoglobin DECIMAL(4,2) NULL,
  liver_function_normal TINYINT(1) NULL,
  fitness_verdict VARCHAR(24) NOT NULL,            -- FIT/FIT_WITH_RESTRICTIONS/UNFIT_TEMPORARY/UNFIT
  restrictions TEXT NULL,
  next_check_due DATE NULL,
  certificate_url VARCHAR(255) NULL,
  notes TEXT NULL,
  KEY idx_hc_emp (emp_id, check_date),
  KEY idx_hc_next (next_check_due)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_health_check (tenant_id, emp_id, emp_name, check_type, check_date, doctor_name, clinic,
  bp_systolic, bp_diastolic, vision, audiometry, lung_function_pct, bmi, ecg_normal,
  fitness_verdict, next_check_due) VALUES
(1, 1,'Ramesh Kumar','ANNUAL','2026-03-20','Dr Srinivasan','KMCH Hospital, Coimbatore', 122, 80,'6/6','Normal',96.0,24.5,1,'FIT','2027-03-20'),
(1, 2,'Priya Selvam','ANNUAL','2026-03-22','Dr Latha','KMCH Hospital, Coimbatore',     118, 78,'6/6','Normal',98.0,22.1,1,'FIT','2027-03-22'),
(1, 3,'Arun Murugan','PRE_EMPLOYMENT','2024-12-01','Dr Srinivasan','KMCH Hospital, Coimbatore',128,82,'6/6','Normal',94.0,25.8,1,'FIT','2025-12-01');

-- =================================================================
-- 8. TDS CHALLAN REGISTER
-- =================================================================
CREATE TABLE IF NOT EXISTS m6_tds_challan (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  challan_no VARCHAR(48) NOT NULL,
  paid_date DATE NOT NULL,
  fy VARCHAR(8) NOT NULL,
  period VARCHAR(16) NOT NULL,                    -- YYYY-MM or Q1/Q2/Q3/Q4
  section VARCHAR(16) NOT NULL,                    -- 192/194C/194I/194J/194Q/206C
  basic_tax DECIMAL(14,2) NOT NULL,
  surcharge DECIMAL(14,2) NULL,
  cess DECIMAL(14,2) NULL,
  interest DECIMAL(14,2) NULL,
  fee_u_s_234e DECIMAL(14,2) NULL,
  penalty DECIMAL(14,2) NULL,
  total_paid DECIMAL(14,2) NOT NULL,
  bsr_code VARCHAR(8) NOT NULL,
  bank_name VARCHAR(96) NULL,
  bank_branch VARCHAR(96) NULL,
  challan_url VARCHAR(255) NULL,
  return_filed_in VARCHAR(16) NULL,                -- 24Q/26Q/27Q/27EQ
  status VARCHAR(16) NOT NULL DEFAULT 'PAID',
  UNIQUE KEY uq_challan (tenant_id, challan_no),
  KEY idx_chal_section (section, paid_date)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m6_tds_challan (tenant_id, challan_no, paid_date, fy, period, section, basic_tax, surcharge, cess,
  interest, total_paid, bsr_code, bank_name, bank_branch, return_filed_in, status) VALUES
(1,'CIN/0512345/26052026','2026-05-07','2025-26','2026-04','192',  38125.00, 0, 1525.00, 0,  39650.00,'0510308','State Bank of India','Coimbatore Main','24Q','PAID'),
(1,'CIN/0512346/26052026','2026-05-07','2025-26','2026-04','194C', 12450.00, 0,  498.00, 0,  12948.00,'0510308','State Bank of India','Coimbatore Main','26Q','PAID'),
(1,'CIN/0512347/26052026','2026-05-07','2025-26','2026-04','194I',  8000.00, 0,  320.00, 0,   8320.00,'0510308','State Bank of India','Coimbatore Main','26Q','PAID'),
(1,'CIN/0612501/26062026','2026-06-07','2025-26','2026-05','192',  38125.00, 0, 1525.00, 0,  39650.00,'0510308','State Bank of India','Coimbatore Main','24Q','PAID'),
(1,'CIN/0612502/26062026','2026-06-07','2025-26','2026-05','194C',  9870.00, 0,  395.00, 0,  10265.00,'0510308','State Bank of India','Coimbatore Main','26Q','PAID');

-- =================================================================
-- 9. DAY BOOK / CASH BOOK / JOURNAL  (single ledger with type column)
-- =================================================================
CREATE TABLE IF NOT EXISTS m6_day_book (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  entry_no VARCHAR(32) NOT NULL,
  entry_date DATE NOT NULL,
  voucher_type VARCHAR(16) NOT NULL,              -- RECEIPT/PAYMENT/JOURNAL/CONTRA/PURCHASE/SALES
  ledger_account VARCHAR(128) NOT NULL,
  contra_account VARCHAR(128) NULL,
  narration TEXT NULL,
  debit DECIMAL(14,2) NULL,
  credit DECIMAL(14,2) NULL,
  reference_doc VARCHAR(64) NULL,                  -- invoice no / GRN no / SO no
  cost_center VARCHAR(32) NULL,
  approved_by_emp_id BIGINT NULL,
  approved_at DATETIME NULL,
  posted TINYINT(1) NOT NULL DEFAULT 0,
  reversal_of_entry_id BIGINT NULL,
  UNIQUE KEY uq_db (tenant_id, entry_no),
  KEY idx_db_date (entry_date, voucher_type),
  KEY idx_db_ledger (ledger_account, entry_date)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m6_day_book (tenant_id, entry_no, entry_date, voucher_type, ledger_account, contra_account,
  narration, debit, credit, reference_doc, cost_center, posted) VALUES
(1,'DB-2026-06-001','2026-06-05','SALES','SBI Current A/c 1234','Bangalore Foods Pvt Ltd','Sales invoice INV-2026-0234 net realised', 245000.00, NULL, 'INV-2026-0234','SALES',1),
(1,'DB-2026-06-002','2026-06-05','SALES','Bangalore Foods Pvt Ltd','Sales A/c','Sales invoice INV-2026-0234 raised', NULL, 245000.00,'INV-2026-0234','SALES',1),
(1,'DB-2026-06-003','2026-06-06','PURCHASE','TESTRO Industries','Purchase A/c','Purchase invoice TI-9234 booked',  NULL, 2150000.00,'TI-9234','PURCHASE',1),
(1,'DB-2026-06-004','2026-06-06','PURCHASE','Purchase A/c','TESTRO Industries','Sunflower oil received GRN-2026-001', 2150000.00, NULL,'TI-9234','PURCHASE',1),
(1,'DB-2026-06-005','2026-06-07','PAYMENT','TDS Payable','SBI Current A/c 1234','TDS 192+194C+194I May 2026 paid', 60918.00, NULL,'CIN-26052026','HR',1),
(1,'DB-2026-06-006','2026-06-07','PAYMENT','SBI Current A/c 1234','TDS Payable','TDS challan paid', NULL, 60918.00,'CIN-26052026','HR',1),
(1,'DB-2026-06-007','2026-06-08','JOURNAL','Depreciation A/c','Plant & Machinery','Monthly dep PRESS-1 + DEOD-1', 48500.00, NULL,'DEP-2026-06','PROD',1),
(1,'DB-2026-06-008','2026-06-08','JOURNAL','Plant & Machinery','Depreciation A/c','Monthly dep credit', NULL, 48500.00,'DEP-2026-06','PROD',1);

-- =================================================================
-- 10. VENDOR / CUSTOMER KYC
-- =================================================================
CREATE TABLE IF NOT EXISTS m6_vendor_kyc (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  vendor_code VARCHAR(48) NOT NULL,
  vendor_name VARCHAR(160) NOT NULL,
  pan VARCHAR(16) NULL,
  gstin VARCHAR(16) NULL,
  msme_status VARCHAR(16) NULL,                    -- MICRO/SMALL/MEDIUM/NOT_MSME
  udyam_no VARCHAR(48) NULL,
  iec_code VARCHAR(16) NULL,                       -- import/export
  bank_name VARCHAR(96) NULL,
  bank_acct VARCHAR(32) NULL,
  bank_ifsc VARCHAR(16) NULL,
  cancelled_cheque_url VARCHAR(255) NULL,
  agreement_url VARCHAR(255) NULL,
  agreement_from DATE NULL,
  agreement_to DATE NULL,
  credit_days INT NULL,
  credit_limit DECIMAL(14,2) NULL,
  tds_section VARCHAR(8) NULL,                     -- 194C / 194I / 194J / 194Q
  tds_rate_pct DECIMAL(5,2) NULL,
  msme_form1_compliant TINYINT(1) NULL,            -- payment within 45 days
  last_kyc_review_date DATE NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uq_vendor_kyc (tenant_id, vendor_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m6_vendor_kyc (tenant_id, vendor_code, vendor_name, pan, gstin, msme_status, udyam_no,
  bank_name, bank_acct, bank_ifsc, agreement_from, agreement_to, credit_days, credit_limit,
  tds_section, tds_rate_pct, msme_form1_compliant, last_kyc_review_date) VALUES
(1,'VEN-001','TESTRO Industries Pvt Ltd','AABCT1234L','27AABCT1234L1ZA','SMALL','UDYAM-MH-04-0011228',
  'HDFC Bank','50100012345678','HDFC0001234','2024-04-01','2027-03-31',30, 5000000.00,'194Q', 0.10, 1,'2026-04-01'),
(1,'VEN-002','Iyer Lubricants','BBCDE5678M','33BBCDE5678M1ZB','MICRO','UDYAM-TN-04-0011220',
  'ICICI Bank','12345600054321','ICIC0001234','2024-04-01','2027-03-31',45, 2500000.00,'194Q', 0.10, 1,'2026-04-01'),
(1,'VEN-003','BalajiLoading & Allied','AABCB1188L','33AABCB1188L1ZB','MICRO','UDYAM-TN-04-0023118',
  'SBI','30000111222333','SBIN0005511','2025-07-01','2026-06-30',45, 1000000.00,'194C', 1.00, 1,'2025-07-01');

-- =================================================================
-- 11. DIRECTOR / KMP REGISTER
-- =================================================================
CREATE TABLE IF NOT EXISTS m6_director_register (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  din VARCHAR(16) NOT NULL,
  full_name VARCHAR(128) NOT NULL,
  designation VARCHAR(48) NOT NULL,                -- MD/WTD/CFO/CS/INDEPENDENT_DIR/NOMINEE_DIR
  pan VARCHAR(16) NULL,
  date_of_birth DATE NULL,
  appointed_at DATE NOT NULL,
  cessation_at DATE NULL,
  shareholding_pct DECIMAL(5,2) NULL,
  resident_status VARCHAR(16) NULL,                -- INDIAN_RESIDENT/NRI/FOREIGN
  pan_url VARCHAR(255) NULL,
  dir_kyc_done TINYINT(1) NULL,
  dir_kyc_year VARCHAR(8) NULL,
  email VARCHAR(96) NULL,
  mobile VARCHAR(16) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uq_din (tenant_id, din)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m6_director_register (tenant_id, din, full_name, designation, pan, date_of_birth,
  appointed_at, shareholding_pct, resident_status, dir_kyc_done, dir_kyc_year, email, mobile, status) VALUES
(1,'00012345','S. Karthikeyan','MD',  'AAAPK1234N','1965-04-15','2018-04-01',55.00,'INDIAN_RESIDENT',1,'2025-26','karthik@picopower.in','+919800000111','ACTIVE'),
(1,'00012346','R. Vidya',      'WTD', 'AAAPV5678P','1968-09-22','2018-04-01',25.00,'INDIAN_RESIDENT',1,'2025-26','vidya@picopower.in', '+919800000112','ACTIVE'),
(1,'00098765','Adv. Hema Pillai','INDEPENDENT_DIR','AAAPH9090Q','1972-01-10','2022-04-01',0.00,'INDIAN_RESIDENT',1,'2025-26','hema@pillai.law','+919800018800','ACTIVE');

-- =================================================================
-- 12. DOCUMENT VAULT (already have m0_document somewhere, add comprehensive catalog table)
-- =================================================================
CREATE TABLE IF NOT EXISTS m0_document_vault (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  doc_no VARCHAR(48) NOT NULL,
  doc_category VARCHAR(32) NOT NULL,               -- LICENCE/CONTRACT/STATUTORY_RETURN/AUDIT/INSURANCE/POLICY/HR/FINANCIAL/SAFETY/QC
  doc_type VARCHAR(48) NOT NULL,                   -- e.g. FACTORY_LICENCE, ITR_V, FORM_16, AOC_4, INSURANCE_POLICY
  title VARCHAR(160) NOT NULL,
  related_to_entity VARCHAR(64) NULL,              -- e.g. EmployeeMaster, Vendor, Asset
  related_to_id BIGINT NULL,
  fy VARCHAR(8) NULL,
  period VARCHAR(16) NULL,
  document_url VARCHAR(255) NOT NULL,
  document_size_kb INT NULL,
  document_hash VARCHAR(64) NULL,                  -- sha256
  issued_by VARCHAR(96) NULL,
  issued_at DATE NULL,
  valid_to DATE NULL,
  uploaded_by_emp_id BIGINT NULL,
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tags TEXT NULL,                                   -- comma-separated for search
  access_level VARCHAR(16) NOT NULL DEFAULT 'INTERNAL',  -- PUBLIC/INTERNAL/CONFIDENTIAL/SECRET
  archived TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY uq_doc (tenant_id, doc_no),
  KEY idx_doc_cat (doc_category, doc_type),
  KEY idx_doc_fy (fy, period),
  KEY idx_doc_related (related_to_entity, related_to_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m0_document_vault (tenant_id, doc_no, doc_category, doc_type, title, fy, period,
  document_url, issued_by, issued_at, valid_to, tags, access_level) VALUES
(1,'DOC-LIC-001','LICENCE','FACTORY_LICENCE','Factory Licence — Coimbatore Plant','2024-25',NULL,'/uploads/factory_lic_2024.pdf','Dept of Industrial Safety, TN','2023-01-15','2026-01-14','factory,licence,statutory','INTERNAL'),
(1,'DOC-LIC-002','LICENCE','IBR_CERT','IBR Boiler Inspection Certificate','2024-25',NULL,'/uploads/ibr_cert_2024.pdf','Director of Boilers','2024-08-12','2025-08-11','boiler,ibr,statutory','INTERNAL'),
(1,'DOC-LIC-003','LICENCE','FSSAI_LICENCE','FSSAI Central Licence','2023-24',NULL,'/uploads/fssai_2023.pdf','FSSAI','2023-08-22','2028-08-21','fssai,food,statutory','INTERNAL'),
(1,'DOC-LIC-004','LICENCE','PCB_CTO','PCB Consent to Operate','2024-25',NULL,'/uploads/pcb_cto_2024.pdf','TNPCB','2024-04-01','2026-09-30','pollution,environment,statutory','INTERNAL'),
(1,'DOC-LIC-005','LICENCE','FIRE_NOC','Fire NOC','2024-25',NULL,'/uploads/fire_noc_2024.pdf','TN Fire Services','2024-05-10','2026-05-09','fire,safety,statutory','INTERNAL'),
(1,'DOC-STAT-001','STATUTORY_RETURN','ITR_V','ITR-V FY 2024-25','2024-25','ANNUAL','/uploads/itrv_2024_25.pdf','Income Tax Dept','2025-10-15',NULL,'income-tax,return,confidential','CONFIDENTIAL'),
(1,'DOC-STAT-002','STATUTORY_RETURN','TAX_AUDIT_3CD','Tax Audit Report 3CD FY 2024-25','2024-25','ANNUAL','/uploads/3cd_2024_25.pdf','Statutory Auditor','2025-09-30',NULL,'audit,3cd,confidential','CONFIDENTIAL'),
(1,'DOC-STAT-003','STATUTORY_RETURN','AOC_4','AOC-4 FY 2024-25','2024-25','ANNUAL','/uploads/aoc4_2024_25.pdf','MCA','2025-10-29',NULL,'mca,roc,financials','CONFIDENTIAL'),
(1,'DOC-STAT-004','STATUTORY_RETURN','MGT_7','MGT-7 FY 2024-25','2024-25','ANNUAL','/uploads/mgt7_2024_25.pdf','MCA','2025-11-29',NULL,'mca,roc,annual-return','CONFIDENTIAL'),
(1,'DOC-INS-001','INSURANCE','FIRE_POLICY','Fire & Special Perils Policy','2025-26',NULL,'/uploads/fire_policy_2025.pdf','New India Assurance','2025-04-01','2026-03-31','insurance,fire,asset','INTERNAL'),
(1,'DOC-INS-002','INSURANCE','WC_POLICY','Workmen Compensation Policy','2025-26',NULL,'/uploads/wc_policy_2025.pdf','National Insurance','2025-04-01','2026-03-31','insurance,workers,statutory','INTERNAL'),
(1,'DOC-HR-001','POLICY','EMP_HANDBOOK','Employee Handbook v3.2',NULL,NULL,'/uploads/handbook_v3_2.pdf','HR','2025-04-01',NULL,'policy,handbook,hr','INTERNAL'),
(1,'DOC-HR-002','POLICY','POSH_POLICY','POSH Policy',NULL,NULL,'/uploads/posh_policy.pdf','HR + Legal','2025-04-01',NULL,'policy,posh,statutory','INTERNAL');

-- =================================================================
-- 13. Register entities + workflow for accident
-- =================================================================
INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('LicenseRenewal',         'Licence Renewal',         'm5_license_renewal'),
  ('AccidentLog',            'Accident Register (F15)', 'm5_accident_log'),
  ('SafetyCommitteeMeeting', 'Safety Committee Meet',   'm5_safety_committee_meeting'),
  ('EmergencyDrillLog',      'Emergency Drill Log',     'm5_emergency_drill_log'),
  ('VisitorLog',             'Visitor Register',        'm5_visitor_log'),
  ('Contractor',             'Contractor',              'm5_contractor'),
  ('ContractorLabour',       'Contractor Labour',       'm5_contractor_labour'),
  ('Apprentice',             'Apprentice Register',     'm7_apprentice'),
  ('PoshCommittee',          'POSH Committee',          'm7_posh_committee'),
  ('PoshIncident',           'POSH Incident',           'm7_posh_incident'),
  ('HealthCheck',            'Health Check Record',     'm7_health_check'),
  ('TdsChallan',             'TDS Challan',             'm6_tds_challan'),
  ('DayBook',                'Day Book / Cash Book',    'm6_day_book'),
  ('VendorKyc',              'Vendor KYC',              'm6_vendor_kyc'),
  ('DirectorRegister',       'Director / KMP Register', 'm6_director_register'),
  ('DocumentVault',          'Document Vault',          'm0_document_vault');

-- Accident workflow
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.AccidentInvestigation',1,'PUBLISHED','{\"code\":\"M5.AccidentInvestigation\",\"version\":1,\"initialState\":\"OPEN\",\"states\":[{\"code\":\"OPEN\",\"assignedRole\":\"EHS\"},{\"code\":\"INVESTIGATING\",\"assignedRole\":\"EHS\"},{\"code\":\"CAPA_PENDING\",\"assignedRole\":\"Manager\"},{\"code\":\"CLOSED\",\"terminal\":true}],\"transitions\":[{\"from\":\"OPEN\",\"to\":\"INVESTIGATING\",\"trigger\":\"page.submitted\"},{\"from\":\"INVESTIGATING\",\"to\":\"CAPA_PENDING\",\"trigger\":\"page.submitted\"},{\"from\":\"CAPA_PENDING\",\"to\":\"CLOSED\",\"trigger\":\"page.submitted\"}]}');
