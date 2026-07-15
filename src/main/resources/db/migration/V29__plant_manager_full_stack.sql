-- =====================================================================
-- V29 — Plant Manager Full Stack
--
-- User dictated the day-in-day-out checklist of a plant manager:
--   hiring + interview pipeline · onboarding · plant-to-plant transfers ·
--   returns · wastages · maintenance schedule · consumables · EB / energy ·
--   training / assessment · skill tracking · day-end consolidation
-- plus 3 specific end-to-end workflows they sketched.
--
-- This migration adds the tables + workflow_definition rows + sample data
-- so every line in the user's list has a real, clickable record.
-- =====================================================================

-- =================================================================
-- 1. HIRING / INTERVIEW / ONBOARDING
-- =================================================================
CREATE TABLE IF NOT EXISTS m7_job_opening (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  opening_code VARCHAR(32) NOT NULL,
  title VARCHAR(96) NOT NULL,
  department VARCHAR(64) NULL,
  grade VARCHAR(16) NULL,
  required_count INT NOT NULL DEFAULT 1,
  filled_count INT NOT NULL DEFAULT 0,
  employment_type VARCHAR(16) NULL,
  experience_min_years INT NULL,
  experience_max_years INT NULL,
  ctc_min_lpa DECIMAL(8,2) NULL,
  ctc_max_lpa DECIMAL(8,2) NULL,
  job_description TEXT NULL,
  required_skills TEXT NULL,
  hiring_manager_emp_id BIGINT NULL,
  hr_owner_emp_id BIGINT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  opened_at DATE NULL,
  filled_at DATE NULL,
  closed_at DATE NULL,
  UNIQUE KEY uq_opening (tenant_id, opening_code),
  KEY idx_open_status (status, opened_at)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_job_opening (tenant_id, opening_code, title, department, grade, required_count, employment_type,
  experience_min_years, experience_max_years, ctc_min_lpa, ctc_max_lpa,
  job_description, required_skills, status, opened_at) VALUES
(1,'OPN-2026-001','Boiler Operator',         'PRODUCTION','L2',2,'PERMANENT', 2, 5, 3.20, 4.50,
   'Operate steam boiler in oil refinery; daily logs','IBR-certified, mechanical aptitude','OPEN','2026-05-15'),
(1,'OPN-2026-002','QC Chemist',              'QUALITY',   'L3',1,'PERMANENT', 3, 7, 4.50, 6.50,
   'Edible-oil QC testing per AOCS/FSSAI','BSc Chemistry, AOCS methods, GC/HPLC','OPEN','2026-05-20'),
(1,'OPN-2026-003','Maintenance Technician',  'MAINTENANCE','L2',2,'PERMANENT', 3, 8, 3.00, 4.00,
   'Mechanical + electrical maintenance of presses/refining line','ITI Fitter/Electrician, PLC basics','OPEN','2026-06-01'),
(1,'OPN-2026-004','Forklift Operator',       'WAREHOUSE', 'L1',1,'CONTRACT',  1, NULL, 2.40, 3.00,
   'FG warehouse + dispatch material handling','Forklift licence, food-grade hygiene','OPEN','2026-06-05');

ALTER TABLE m7_job_opening ADD COLUMN IF NOT EXISTS job_posting_url VARCHAR(255) NULL;

-- Candidate / Application
CREATE TABLE IF NOT EXISTS m7_job_application (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  applicant_code VARCHAR(32) NOT NULL,
  opening_id BIGINT NOT NULL,
  full_name VARCHAR(128) NOT NULL,
  mobile VARCHAR(16) NULL,
  email VARCHAR(96) NULL,
  current_company VARCHAR(96) NULL,
  current_designation VARCHAR(64) NULL,
  current_ctc_lpa DECIMAL(8,2) NULL,
  expected_ctc_lpa DECIMAL(8,2) NULL,
  notice_period_days INT NULL,
  experience_years DECIMAL(4,1) NULL,
  source VARCHAR(24) NULL,
  source_detail VARCHAR(128) NULL,
  resume_url VARCHAR(255) NULL,
  -- Pipeline
  status VARCHAR(24) NOT NULL DEFAULT 'CV_SCREENING',
  current_step VARCHAR(32) NULL,
  workflow_instance_id BIGINT NULL,
  -- Outcome
  recommended_grade VARCHAR(16) NULL,
  offered_ctc_lpa DECIMAL(8,2) NULL,
  joining_date DATE NULL,
  decline_reason VARCHAR(96) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_app (tenant_id, applicant_code),
  KEY idx_app_open (opening_id, status),
  CONSTRAINT fk_app_open FOREIGN KEY (opening_id) REFERENCES m7_job_opening(id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_job_application (tenant_id, applicant_code, opening_id, full_name, mobile, email,
  current_company, current_designation, current_ctc_lpa, expected_ctc_lpa, notice_period_days,
  experience_years, source, resume_url, status, current_step) VALUES
(1,'APP-2026-101',(SELECT id FROM m7_job_opening WHERE opening_code='OPN-2026-001'),'Bharath Kumar','+919800100001','bharath@email.in',
  'TVS Lubes','Boiler Operator', 3.00, 3.75, 30, 4.5, 'NAUKRI','/uploads/bharath_cv.pdf','HM_DISCUSSION', 'Hiring Manager Discussion'),
(1,'APP-2026-102',(SELECT id FROM m7_job_opening WHERE opening_code='OPN-2026-001'),'Suresh Babu','+919800100002','suresh.b@email.in',
  'Adani Wilmar','Senior Operator', 4.20, 5.00, 60, 6.0, 'REFERRAL','/uploads/suresh_cv.pdf','TECH_INTERVIEW','Technical Interview'),
(1,'APP-2026-103',(SELECT id FROM m7_job_opening WHERE opening_code='OPN-2026-002'),'Pavithra D.', '+919800100003','pavithra@email.in',
  'Cargill India','QC Chemist', 5.10, 6.30, 30, 4.2, 'LINKEDIN','/uploads/pavithra_cv.pdf','OFFER_RELEASED','Offer Released'),
(1,'APP-2026-104',(SELECT id FROM m7_job_opening WHERE opening_code='OPN-2026-003'),'Manikandan R.','+919800100004','manik@email.in',
  'BPCL','Maintenance Tech', 3.40, 4.00, 30, 5.0, 'WALK_IN', NULL,'CV_SCREENING', 'CV Screening');

-- Interview rounds (one row per round, per applicant)
CREATE TABLE IF NOT EXISTS m7_interview_round (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  application_id BIGINT NOT NULL,
  round_no INT NOT NULL,
  round_type VARCHAR(24) NOT NULL,         -- TELEPHONIC/TECHNICAL/HM/HR/PSYCH/PRACTICAL
  scheduled_at DATETIME NULL,
  completed_at DATETIME NULL,
  panelist_emp_id BIGINT NULL,
  mode VARCHAR(16) NULL,                   -- ONLINE/IN_PERSON/PHONE
  meeting_link VARCHAR(255) NULL,
  rating_technical DECIMAL(3,1) NULL,
  rating_communication DECIMAL(3,1) NULL,
  rating_attitude DECIMAL(3,1) NULL,
  rating_overall DECIMAL(3,1) NULL,
  strengths TEXT NULL,
  concerns TEXT NULL,
  verdict VARCHAR(16) NULL,                -- SELECTED/REJECTED/ON_HOLD/NEXT_ROUND
  feedback TEXT NULL,
  CONSTRAINT fk_intv_app FOREIGN KEY (application_id) REFERENCES m7_job_application(id) ON DELETE CASCADE,
  KEY idx_intv (application_id, round_no)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_interview_round (application_id, round_no, round_type, scheduled_at, completed_at, mode,
  rating_technical, rating_communication, rating_attitude, rating_overall, verdict, feedback)
SELECT id,1,'TELEPHONIC','2026-05-22 11:00:00','2026-05-22 11:30:00','PHONE', 4.0, 4.5, 4.5, 4.3,'NEXT_ROUND','Good communication; basic IBR knowledge OK'
FROM m7_job_application WHERE applicant_code='APP-2026-101';
INSERT IGNORE INTO m7_interview_round (application_id, round_no, round_type, scheduled_at, completed_at, mode, panelist_emp_id,
  rating_technical, rating_communication, rating_attitude, rating_overall, verdict, feedback)
SELECT id,2,'TECHNICAL','2026-05-26 10:00:00','2026-05-26 11:30:00','IN_PERSON', 1, 4.5, 4.0, 4.5, 4.3,'NEXT_ROUND','Good steam-table knowledge, hands-on demo'
FROM m7_job_application WHERE applicant_code='APP-2026-101';
INSERT IGNORE INTO m7_interview_round (application_id, round_no, round_type, scheduled_at, mode, verdict)
SELECT id,3,'HM',         '2026-06-12 15:00:00','IN_PERSON', NULL
FROM m7_job_application WHERE applicant_code='APP-2026-101';
INSERT IGNORE INTO m7_interview_round (application_id, round_no, round_type, scheduled_at, completed_at, mode, panelist_emp_id,
  rating_technical, rating_communication, rating_attitude, rating_overall, verdict, feedback)
SELECT id,1,'TELEPHONIC','2026-06-01 14:00:00','2026-06-01 14:30:00','PHONE', NULL, 4.5, 5.0, 5.0, 4.8,'NEXT_ROUND','Strong AOCS knowledge — solid candidate'
FROM m7_job_application WHERE applicant_code='APP-2026-103';
INSERT IGNORE INTO m7_interview_round (application_id, round_no, round_type, scheduled_at, completed_at, mode, panelist_emp_id,
  rating_technical, rating_communication, rating_attitude, rating_overall, verdict, feedback)
SELECT id,2,'TECHNICAL','2026-06-04 11:00:00','2026-06-04 13:00:00','IN_PERSON', 2, 4.8, 4.5, 4.5, 4.6,'NEXT_ROUND','Lab demo passed; recommended'
FROM m7_job_application WHERE applicant_code='APP-2026-103';
INSERT IGNORE INTO m7_interview_round (application_id, round_no, round_type, scheduled_at, completed_at, mode, panelist_emp_id,
  rating_overall, verdict, feedback)
SELECT id,3,'HM','2026-06-07 16:00:00','2026-06-07 16:45:00','IN_PERSON', 1, 4.5,'SELECTED','Hire recommended; release offer at Rs.6.0 LPA'
FROM m7_job_application WHERE applicant_code='APP-2026-103';

-- Onboarding checklist (one row per task per joining)
CREATE TABLE IF NOT EXISTS m7_onboarding_task (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  emp_id BIGINT NULL,
  application_id BIGINT NULL,
  task_category VARCHAR(24) NOT NULL,      -- DOCS/IT/HR/FINANCE/SAFETY/INDUCTION
  task_code VARCHAR(32) NOT NULL,
  task_label VARCHAR(160) NOT NULL,
  due_offset_days INT NULL,
  due_date DATE NULL,
  completed_at DATETIME NULL,
  completed_by_emp_id BIGINT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  remarks TEXT NULL,
  KEY idx_onb_emp (emp_id, status),
  KEY idx_onb_app (application_id, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_onboarding_task (tenant_id, application_id, task_category, task_code, task_label, due_offset_days, due_date, status) VALUES
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'DOCS','AADHAAR','Aadhaar card original + photocopy', 0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'DOCS','PAN',    'PAN card original + photocopy',    0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'DOCS','EDU_CERT','Education certificates',          0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'DOCS','EXP_CERT','Previous experience certificate', 0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'DOCS','REL_LETTER','Relieving letter from prev employer', 0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'DOCS','BANK','Cancelled cheque + bank passbook copy',  0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'IT',  'EMAIL','Official email + Office365 account',     1,'2026-06-16','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'IT',  'BIOMETRIC','Biometric enrollment',              1,'2026-06-16','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'IT',  'EARMS','EARMS login + persona role',           1,'2026-06-16','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'IT',  'GATE_PASS','Gate pass + ID card',              0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'HR',  'OFFER_ACCEPT','Signed offer + appointment',     0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'HR',  'JOIN_BONUS','Joining bonus / signing letter',  0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'HR',  'PF_NOMINEE','PF Form-2 nominee declaration',   1,'2026-06-16','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'HR',  'GRATUITY_NOM','Gratuity nominee declaration',  1,'2026-06-16','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'HR',  'TAX_DECL','Tax declaration (Form 12BB)',      3,'2026-06-18','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'FINANCE','SALARY_ACCT','Salary account (bank acct)', 1,'2026-06-16','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'SAFETY','SAFETY_INDUCT','Safety induction + helmet allotment', 0,'2026-06-15','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'SAFETY','FIRE_DRILL','Fire/Emergency drill briefing', 1,'2026-06-16','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'INDUCTION','PLANT_TOUR','Plant tour + team introduction', 1,'2026-06-16','PENDING'),
(1,(SELECT id FROM m7_job_application WHERE applicant_code='APP-2026-103'),'INDUCTION','POLICY_BRIEF','HR policy + Code of conduct briefing', 2,'2026-06-17','PENDING');

-- Hiring workflow (Interview Call -> Assessment -> HM Discussion -> HR Discussion -> Offer Letter -> Onboarding)
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M7.Hiring',1,'PUBLISHED','{"code":"M7.Hiring","version":1,"initialState":"CV_SCREENING","states":[{"code":"CV_SCREENING","assignedRole":"HR"},{"code":"INTERVIEW_CALL","assignedRole":"HR"},{"code":"TECH_ASSESSMENT","assignedRole":"InterviewPanel"},{"code":"HM_DISCUSSION","assignedRole":"HiringManager"},{"code":"HR_DISCUSSION","assignedRole":"HR"},{"code":"OFFER_RELEASED","assignedRole":"HR"},{"code":"OFFER_ACCEPTED","assignedRole":"Candidate"},{"code":"ONBOARDED","terminal":true},{"code":"REJECTED","terminal":true},{"code":"WITHDRAWN","terminal":true}],"transitions":[{"from":"CV_SCREENING","to":"INTERVIEW_CALL","trigger":"page.submitted","pageCode":"pico:hiring-call"},{"from":"CV_SCREENING","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:hiring-reject","guard":"#submitted[''verdict''] == ''REJECTED''"},{"from":"INTERVIEW_CALL","to":"TECH_ASSESSMENT","trigger":"page.submitted","pageCode":"pico:hiring-tech"},{"from":"TECH_ASSESSMENT","to":"HM_DISCUSSION","trigger":"page.submitted","pageCode":"pico:hiring-hm","guard":"#submitted[''rating_overall''] >= 3.5"},{"from":"TECH_ASSESSMENT","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:hiring-reject"},{"from":"HM_DISCUSSION","to":"HR_DISCUSSION","trigger":"page.submitted","pageCode":"pico:hiring-hr"},{"from":"HR_DISCUSSION","to":"OFFER_RELEASED","trigger":"page.submitted","pageCode":"pico:hiring-offer"},{"from":"OFFER_RELEASED","to":"OFFER_ACCEPTED","trigger":"page.submitted","pageCode":"pico:hiring-accept"},{"from":"OFFER_RELEASED","to":"WITHDRAWN","trigger":"page.submitted","pageCode":"pico:hiring-decline"},{"from":"OFFER_ACCEPTED","to":"ONBOARDED","trigger":"page.submitted","pageCode":"pico:hiring-onboard"}]}');

-- =================================================================
-- 2. PLANT-TO-PLANT TRANSFER
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_plant_transfer (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  transfer_no VARCHAR(32) NOT NULL,
  from_plant VARCHAR(32) NOT NULL,
  to_plant VARCHAR(32) NOT NULL,
  reason VARCHAR(64) NULL,
  required_by_date DATE NULL,
  initiated_by_emp_id BIGINT NULL,
  approved_by_emp_id BIGINT NULL,
  approved_at DATETIME NULL,
  -- Loading at source
  loaded_at DATETIME NULL,
  load_supervisor_emp_id BIGINT NULL,
  vehicle_no VARCHAR(16) NULL,
  driver_name VARCHAR(96) NULL,
  driver_phone VARCHAR(16) NULL,
  transporter VARCHAR(96) NULL,
  weighbridge_out_loaded_kg DECIMAL(12,2) NULL,  -- at source-out
  weighbridge_out_slip VARCHAR(32) NULL,
  out_gate_pass_no VARCHAR(32) NULL,
  out_gate_pass_at DATETIME NULL,
  seal_no VARCHAR(32) NULL,
  -- In-transit
  dispatched_at DATETIME NULL,
  expected_arrival DATETIME NULL,
  -- Arrival at destination
  arrived_at DATETIME NULL,
  weighbridge_in_loaded_kg DECIMAL(12,2) NULL,   -- at dest-in
  weighbridge_in_slip VARCHAR(32) NULL,
  weight_variance_kg DECIMAL(12,2) GENERATED ALWAYS AS (
    COALESCE(weighbridge_out_loaded_kg,0) - COALESCE(weighbridge_in_loaded_kg,0)
  ) STORED,
  in_gate_pass_no VARCHAR(32) NULL,
  in_gate_pass_at DATETIME NULL,
  seal_intact_on_arrival TINYINT(1) NULL,
  unloaded_at DATETIME NULL,
  unload_supervisor_emp_id BIGINT NULL,
  -- Stock receipt at destination
  stocked_at DATETIME NULL,
  stored_in_warehouse VARCHAR(32) NULL,
  -- Overall
  status VARCHAR(24) NOT NULL DEFAULT 'DRAFT',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_xfer (tenant_id, transfer_no),
  KEY idx_xfer_status (status, required_by_date)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_plant_transfer_line (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  transfer_id BIGINT NOT NULL,
  line_no INT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(160) NULL,
  lot_no VARCHAR(64) NULL,
  qty_requested DECIMAL(14,3) NOT NULL,
  qty_loaded DECIMAL(14,3) NULL,
  qty_received DECIMAL(14,3) NULL,
  uom VARCHAR(16) NOT NULL,
  CONSTRAINT fk_xfer_line FOREIGN KEY (transfer_id) REFERENCES m5_plant_transfer(id) ON DELETE CASCADE,
  KEY idx_xfer_item (transfer_id, item_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_plant_transfer (tenant_id, transfer_no, from_plant, to_plant, reason, required_by_date,
  initiated_by_emp_id, approved_by_emp_id, approved_at,
  loaded_at, vehicle_no, driver_name, driver_phone, transporter,
  weighbridge_out_loaded_kg, weighbridge_out_slip, out_gate_pass_no, out_gate_pass_at, seal_no,
  arrived_at, weighbridge_in_loaded_kg, weighbridge_in_slip, in_gate_pass_no, seal_intact_on_arrival,
  unloaded_at, stocked_at, stored_in_warehouse, status, current_step) VALUES
(1,'PT-2026-001','PLANT-1','PLANT-2','Refined oil top-up for export run','2026-06-10',
  1, 1, '2026-06-08 11:30:00',
  '2026-06-08 13:00:00','TN37LL5511','Karunanidhi V.','+919810022001','TVS Logistics',
   13500.00,'WB-OUT-260608-A','GP-OUT-260608-001','2026-06-08 13:30:00','SEAL-PT-7711',
  '2026-06-08 17:30:00', 13480.00,'WB-IN-260608-B','GP-IN-260608-001', 1,
  '2026-06-08 18:30:00','2026-06-08 19:30:00','PLANT-2-WH-A','STOCKED','Stored at destination'),
(1,'PT-2026-002','PLANT-1','PLANT-2','Empty bottle topup','2026-06-12',
   1, 1, '2026-06-09 09:00:00',
   NULL, NULL, NULL, NULL, NULL,
   NULL, NULL, NULL, NULL, NULL,
   NULL, NULL, NULL, NULL, NULL,
   NULL, NULL, NULL, 'APPROVED','Ready for loading');

INSERT IGNORE INTO m5_plant_transfer_line (transfer_id, line_no, item_code, item_name, lot_no, qty_requested, qty_loaded, qty_received, uom)
SELECT id,1,'OIL-SUN-RFD','Refined Sunflower Oil','LOT-RFD-260605-A',1500.000,1500.000,1480.000,'KG' FROM m5_plant_transfer WHERE transfer_no='PT-2026-001';
INSERT IGNORE INTO m5_plant_transfer_line (transfer_id, line_no, item_code, item_name, lot_no, qty_requested, qty_loaded, qty_received, uom)
SELECT id,2,'BTL-1L-PET',  '1L PET Bottle',         NULL,             5000.000,5000.000,5000.000,'PCS' FROM m5_plant_transfer WHERE transfer_no='PT-2026-001';
INSERT IGNORE INTO m5_plant_transfer_line (transfer_id, line_no, item_code, item_name, qty_requested, uom)
SELECT id,1,'BTL-1L-PET','1L PET Bottle',12000.000,'PCS' FROM m5_plant_transfer WHERE transfer_no='PT-2026-002';

-- Plant transfer workflow (Material Request -> Approve -> Load -> Weight -> Gatepass -> Inward -> Check -> Unload -> Stock)
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.PlantTransfer',1,'PUBLISHED','{"code":"M5.PlantTransfer","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Dispatch"},{"code":"REQUESTED","assignedRole":"Manager"},{"code":"APPROVED","assignedRole":"LoadSupervisor"},{"code":"LOADED","assignedRole":"WeighbridgeOp"},{"code":"WEIGHED_OUT","assignedRole":"GateKeeper"},{"code":"GATE_PASS_OUT","assignedRole":"Driver"},{"code":"IN_TRANSIT","assignedRole":"Driver"},{"code":"ARRIVED","assignedRole":"WeighbridgeOp"},{"code":"WEIGHED_IN","assignedRole":"GateKeeper"},{"code":"GATE_PASS_IN","assignedRole":"UnloadSupervisor"},{"code":"UNLOADED","assignedRole":"StockOwner"},{"code":"STOCKED","terminal":true},{"code":"CANCELLED","terminal":true}],"transitions":[{"from":"DRAFT","to":"REQUESTED","trigger":"page.submitted","pageCode":"pico:plant-transfer-create"},{"from":"REQUESTED","to":"APPROVED","trigger":"page.submitted","pageCode":"pico:plant-transfer-approve"},{"from":"REQUESTED","to":"CANCELLED","trigger":"page.submitted","pageCode":"pico:plant-transfer-cancel"},{"from":"APPROVED","to":"LOADED","trigger":"page.submitted","pageCode":"pico:plant-transfer-load"},{"from":"LOADED","to":"WEIGHED_OUT","trigger":"page.submitted","pageCode":"pico:plant-transfer-weigh-out"},{"from":"WEIGHED_OUT","to":"GATE_PASS_OUT","trigger":"page.submitted","pageCode":"pico:plant-transfer-gate-out"},{"from":"GATE_PASS_OUT","to":"IN_TRANSIT","trigger":"system"},{"from":"IN_TRANSIT","to":"ARRIVED","trigger":"page.submitted","pageCode":"pico:plant-transfer-arrive"},{"from":"ARRIVED","to":"WEIGHED_IN","trigger":"page.submitted","pageCode":"pico:plant-transfer-weigh-in"},{"from":"WEIGHED_IN","to":"GATE_PASS_IN","trigger":"page.submitted","pageCode":"pico:plant-transfer-gate-in"},{"from":"GATE_PASS_IN","to":"UNLOADED","trigger":"page.submitted","pageCode":"pico:plant-transfer-unload"},{"from":"UNLOADED","to":"STOCKED","trigger":"page.submitted","pageCode":"pico:plant-transfer-stock"}]}');

-- End-to-end production workflow that links Inward -> QC -> Stock -> Production -> Packing -> Dispatch
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.EndToEnd',1,'PUBLISHED','{"code":"M5.EndToEnd","version":1,"initialState":"BILL_INWARD","states":[{"code":"BILL_INWARD","assignedRole":"GateKeeper"},{"code":"WEIGHT_CHECK","assignedRole":"WeighbridgeOp"},{"code":"QC_INSPECTION","assignedRole":"QC"},{"code":"STOCK","assignedRole":"StoreKeeper"},{"code":"PRODUCTION","assignedRole":"Supervisor"},{"code":"PACKING","assignedRole":"PackingOp"},{"code":"ORDER","assignedRole":"Sales"},{"code":"DISPATCH","assignedRole":"Dispatch"},{"code":"DELIVERED","terminal":true},{"code":"HOLD","assignedRole":"Manager"}],"transitions":[{"from":"BILL_INWARD","to":"WEIGHT_CHECK","trigger":"page.submitted","pageCode":"pico:inward-receipt-list"},{"from":"WEIGHT_CHECK","to":"QC_INSPECTION","trigger":"page.submitted"},{"from":"QC_INSPECTION","to":"STOCK","trigger":"page.submitted","guard":"#submitted[''qc_verdict''] == ''ACCEPTED''"},{"from":"QC_INSPECTION","to":"HOLD","trigger":"page.submitted","guard":"#submitted[''qc_verdict''] == ''REJECTED''"},{"from":"STOCK","to":"PRODUCTION","trigger":"page.submitted"},{"from":"PRODUCTION","to":"PACKING","trigger":"page.submitted"},{"from":"PACKING","to":"ORDER","trigger":"system"},{"from":"ORDER","to":"DISPATCH","trigger":"page.submitted"},{"from":"DISPATCH","to":"DELIVERED","trigger":"page.submitted"}]}');

-- =================================================================
-- 3. MAINTENANCE SCHEDULE + ACTUAL
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_maintenance_schedule (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  asset_code VARCHAR(32) NOT NULL,
  asset_name VARCHAR(96) NULL,
  maintenance_type VARCHAR(16) NOT NULL,         -- PREVENTIVE/PREDICTIVE/CALIBRATION/INSPECTION
  frequency VARCHAR(16) NOT NULL,                 -- DAILY/WEEKLY/MONTHLY/QUARTERLY/ANNUAL/RUNNING_HRS
  frequency_value INT NULL,
  task_description VARCHAR(256) NOT NULL,
  responsible_emp_id BIGINT NULL,
  next_due_date DATE NULL,
  last_done_date DATE NULL,
  spare_parts_required TEXT NULL,
  estimated_duration_min INT NULL,
  shutdown_required TINYINT(1) NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  notes TEXT NULL,
  UNIQUE KEY uq_msched (tenant_id, asset_code, task_description(64)),
  KEY idx_msched_due (next_due_date, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_maintenance_schedule (tenant_id, asset_code, asset_name, maintenance_type, frequency, frequency_value,
  task_description, next_due_date, last_done_date, shutdown_required, status) VALUES
(1,'PRESS-1', 'Sunflower Screw Press #1','PREVENTIVE','MONTHLY',  1,'Worm/cage inspection + lubrication',           '2026-06-15','2026-05-15',1,'ACTIVE'),
(1,'PRESS-1', 'Sunflower Screw Press #1','PREVENTIVE','RUNNING_HRS',500,'Cage bolt re-torque after 500 hrs',         '2026-06-12',NULL,        1,'ACTIVE'),
(1,'BOILER-1','IBR Steam Boiler',         'INSPECTION','ANNUAL',   1,'IBR statutory inspection + hydro test',        '2026-08-15','2025-08-10',1,'ACTIVE'),
(1,'WB-1',    'Weighbridge — Main Gate',  'CALIBRATION','MONTHLY', 1,'Reference weight calibration (3000kg standard)','2026-06-10','2026-05-10',0,'ACTIVE'),
(1,'DEOD-1',  'Deodorizer Column',        'PREVENTIVE','QUARTERLY',1,'Tray cleaning + vacuum pump oil change',       '2026-09-01','2026-06-01',1,'ACTIVE'),
(1,'NEUT-1',  'Neutralizer Reactor',      'PREVENTIVE','MONTHLY',  1,'Agitator seal + caustic dosing pump cal',      '2026-06-25','2026-05-25',0,'ACTIVE'),
(1,'PACK-MC-04','Packing Machine #4',     'PREVENTIVE','WEEKLY',   1,'Filler nozzle clean + sealing jaw alignment',  '2026-06-12','2026-06-05',0,'ACTIVE'),
(1,'FORKLIFT-1','Forklift — FG',          'INSPECTION','MONTHLY',  1,'Brake test + LPG cylinder check',              '2026-06-15','2026-05-15',0,'ACTIVE');

-- =================================================================
-- 4. WASTAGES / SCRAP LOG
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_wastage_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  log_no VARCHAR(32) NOT NULL,
  log_date DATE NOT NULL,
  source_type VARCHAR(16) NOT NULL,    -- PRODUCTION/PACKING/STORAGE/DISPATCH/QC
  source_ref VARCHAR(64) NULL,         -- batch / JC / PR no
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(160) NULL,
  qty DECIMAL(14,3) NOT NULL,
  uom VARCHAR(16) NOT NULL,
  unit_cost DECIMAL(12,2) NULL,
  value_loss DECIMAL(14,2) GENERATED ALWAYS AS (qty * COALESCE(unit_cost,0)) STORED,
  reason_category VARCHAR(24) NOT NULL, -- SPILLAGE/EVAPORATION/CONTAMINATION/EXPIRY/PROCESS_LOSS/PACKAGING_DEFECT/HANDLING/REJECTION
  reason_detail VARCHAR(256) NULL,
  recovery_action VARCHAR(96) NULL,
  reported_by_emp_id BIGINT NULL,
  approved_by_emp_id BIGINT NULL,
  approved_at DATETIME NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'REPORTED',
  UNIQUE KEY uq_waste (tenant_id, log_no),
  KEY idx_waste_date (log_date, reason_category)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_wastage_log (tenant_id, log_no, log_date, source_type, source_ref, item_code, item_name, qty, uom, unit_cost,
  reason_category, reason_detail, recovery_action, reported_by_emp_id, status) VALUES
(1,'WST-2026-001','2026-06-07','PRODUCTION','PB-2026-1001','OIL-SUN-RAW','Raw Sunflower Oil', 35.000,'KG', 85.50,
  'PROCESS_LOSS','Expeller startup spill during shift changeover','Track cage temp before changeover',1,'APPROVED'),
(1,'WST-2026-002','2026-06-06','PACKING',    'PR-2026-0801','OIL-SUN-RFD','Refined Sunflower Oil',42.000,'KG',115.00,
  'PACKAGING_DEFECT','Cap-loose spoilage (42 packs)','Tighten cap-torque setting, recalibrate', 2,'APPROVED'),
(1,'WST-2026-003','2026-06-06','PACKING',    'PR-2026-0801','LBL-SUN-1L','Sunflower 1L Label',  50.000,'PCS',  0.80,
  'PACKAGING_DEFECT','Misaligned labels rejected at QC','Replace label feed roller', 2,'REPORTED'),
(1,'WST-2026-004','2026-06-08','STORAGE',    'LOT-RFD-260525','OIL-GND-RFD','Refined Groundnut Oil',12.000,'KG',195.00,
  'EXPIRY',          'Short-shelf-life lot found in returns; nearing expiry','Send to soap industry recovery',2,'REPORTED');

-- =================================================================
-- 5. CONSUMABLES USAGE (chemicals, lubes, packing materials below MR)
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_consumable_issue (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  issue_no VARCHAR(32) NOT NULL,
  issue_date DATE NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(160) NULL,
  qty DECIMAL(14,3) NOT NULL,
  uom VARCHAR(16) NOT NULL,
  issued_to_dept VARCHAR(32) NULL,
  issued_to_emp_id BIGINT NULL,
  purpose VARCHAR(96) NULL,
  reference_jc VARCHAR(32) NULL,
  reference_batch VARCHAR(32) NULL,
  unit_cost DECIMAL(12,2) NULL,
  total_cost DECIMAL(14,2) GENERATED ALWAYS AS (qty * COALESCE(unit_cost,0)) STORED,
  UNIQUE KEY uq_ci (tenant_id, issue_no),
  KEY idx_ci_date (issue_date, item_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_consumable_issue (tenant_id, issue_no, issue_date, item_code, item_name, qty, uom,
  issued_to_dept, purpose, reference_jc, unit_cost) VALUES
(1,'CON-2026-101','2026-06-07','CITRIC-ACID',  'Citric Acid (food grade)', 4.800,'KG','PRODUCTION','Degumming PB-2026-1001','JC-2026-0701', 95.00),
(1,'CON-2026-102','2026-06-07','CAUSTIC-SODA', 'Caustic Soda 50%',         35.000,'KG','PRODUCTION','Neutralizing PB-2026-1001','JC-2026-0701', 48.00),
(1,'CON-2026-103','2026-06-07','BLEACH-EARTH', 'Bleaching Earth',         38.000,'KG','PRODUCTION','Bleaching PB-2026-1001','JC-2026-0701', 22.00),
(1,'CON-2026-104','2026-06-08','LUBE-OIL-32',  'Lube Oil VG-32',           5.000,'L', 'MAINTENANCE','Press-1 monthly PM',NULL,         245.00),
(1,'CON-2026-105','2026-06-08','HAND-GLOVES',  'Nitrile Gloves',          50.000,'PR','PACKING',    'Packing line operators monthly',NULL, 8.50);

-- =================================================================
-- 6. EB / Energy meter reading expansion (already partially in m5_energy_reading)
-- =================================================================
ALTER TABLE m5_energy_reading
  ADD COLUMN IF NOT EXISTS meter_code VARCHAR(24) NULL,
  ADD COLUMN IF NOT EXISTS reading_kwh DECIMAL(14,2) NULL,
  ADD COLUMN IF NOT EXISTS demand_kw DECIMAL(8,2) NULL,
  ADD COLUMN IF NOT EXISTS power_factor DECIMAL(4,3) NULL,
  ADD COLUMN IF NOT EXISTS tariff_inr_per_kwh DECIMAL(8,3) NULL,
  ADD COLUMN IF NOT EXISTS cost_inr DECIMAL(12,2) NULL,
  ADD COLUMN IF NOT EXISTS reading_type VARCHAR(16) NULL,  -- DAILY/MONTHLY/PEAK/OFFPEAK
  ADD COLUMN IF NOT EXISTS area VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS recorded_by_emp_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS notes TEXT NULL;

INSERT IGNORE INTO m5_energy_reading (tenant_id, meter_code, meter_kind, recorded_at, reading_kwh, demand_kw, power_factor,
  tariff_inr_per_kwh, cost_inr, reading_type, area, recorded_by_emp_id, uom, notes) VALUES
(1,'MAIN-HT','EB','2026-06-07 23:55:00', 142850.50, 285.5, 0.952, 9.250, 1322.36,'DAILY','PLANT-1', 1,'kWh', 'PB-2026-1001 day; expeller heavy load'),
(1,'MAIN-HT','EB','2026-06-08 23:55:00', 143710.20, 287.0, 0.948, 9.250, 7953.16,'DAILY','PLANT-1', 1,'kWh', 'Light day — no packing run, only DEGUMMING + NEUTRALIZING'),
(1,'PACK-LT','EB','2026-06-07 22:00:00',  18420.00, 65.0,  0.965, 6.500,  390.00,'DAILY','PACKING', 2,'kWh', 'Two-shift pack run');

-- =================================================================
-- 7. TRAINING / ASSESSMENT / SKILL
-- =================================================================
CREATE TABLE IF NOT EXISTS m7_training_program (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  program_code VARCHAR(32) NOT NULL,
  title VARCHAR(160) NOT NULL,
  category VARCHAR(24) NOT NULL,        -- SAFETY/QUALITY/TECHNICAL/PROCESS/COMPLIANCE/SOFT_SKILLS/STATUTORY
  description TEXT NULL,
  duration_hours DECIMAL(5,1) NULL,
  delivery_mode VARCHAR(16) NULL,       -- CLASSROOM/ON_JOB/E_LEARNING/EXTERNAL
  trainer VARCHAR(96) NULL,
  is_mandatory TINYINT(1) NOT NULL DEFAULT 0,
  refresh_months INT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uq_tp (tenant_id, program_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_training_program (tenant_id, program_code, title, category, duration_hours, delivery_mode, trainer, is_mandatory, refresh_months) VALUES
(1,'TRN-SAFETY-001','Fire Safety + Emergency Evacuation','SAFETY',     2.0,'CLASSROOM','EHS Officer',     1,  12),
(1,'TRN-SAFETY-002','PPE + Manual Handling',             'SAFETY',     1.5,'CLASSROOM','EHS Officer',     1,  12),
(1,'TRN-SAFETY-003','First Aid + CPR',                   'SAFETY',     4.0,'CLASSROOM','Red Cross',       1,  24),
(1,'TRN-QUAL-001',  'AOCS Methods Refresher',            'QUALITY',    8.0,'ON_JOB',    'QC Manager',     1,  12),
(1,'TRN-QUAL-002',  'GMP + Hygiene Standards (FSSAI)',   'QUALITY',    3.0,'CLASSROOM','QC Manager',     1,  12),
(1,'TRN-TECH-001',  'Refining Plant Operation',          'TECHNICAL',  16.0,'ON_JOB',  'Production Head',1,  24),
(1,'TRN-TECH-002',  'Boiler Operations (IBR)',           'TECHNICAL',  24.0,'EXTERNAL','IBR Inspector',  0,  36),
(1,'TRN-COMP-001',  'POSH Awareness',                    'COMPLIANCE', 2.0,'CLASSROOM','HR',             1,  12),
(1,'TRN-COMP-002',  'Anti-Bribery + Code of Conduct',    'COMPLIANCE', 1.5,'E_LEARNING','HR',            1,  12),
(1,'TRN-SOFT-001',  'Communication Skills',              'SOFT_SKILLS',8.0,'CLASSROOM','External',       0,  36);

CREATE TABLE IF NOT EXISTS m7_training_attendance (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  program_id BIGINT NOT NULL,
  emp_id BIGINT NOT NULL,
  scheduled_date DATE NULL,
  attended_date DATE NULL,
  duration_hours DECIMAL(5,1) NULL,
  attendance_status VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED/PRESENT/ABSENT/EXEMPTED
  pre_score DECIMAL(5,2) NULL,
  post_score DECIMAL(5,2) NULL,
  pass_threshold DECIMAL(5,2) NULL,
  certified TINYINT(1) NULL,
  certificate_no VARCHAR(48) NULL,
  next_refresh_due DATE NULL,
  feedback VARCHAR(256) NULL,
  KEY idx_ta_emp (emp_id, scheduled_date),
  KEY idx_ta_prog (program_id, attendance_status),
  CONSTRAINT fk_ta_prog FOREIGN KEY (program_id) REFERENCES m7_training_program(id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_training_attendance (tenant_id, program_id, emp_id, scheduled_date, attended_date, duration_hours,
  attendance_status, pre_score, post_score, pass_threshold, certified, certificate_no, next_refresh_due) VALUES
(1,(SELECT id FROM m7_training_program WHERE program_code='TRN-SAFETY-001'),1,'2026-04-12','2026-04-12',2.0,'PRESENT', 55, 88, 60, 1, 'CERT-FIRE-2026-001','2027-04-12'),
(1,(SELECT id FROM m7_training_program WHERE program_code='TRN-SAFETY-001'),2,'2026-04-12','2026-04-12',2.0,'PRESENT', 60, 92, 60, 1, 'CERT-FIRE-2026-002','2027-04-12'),
(1,(SELECT id FROM m7_training_program WHERE program_code='TRN-QUAL-001'),  2,'2026-05-20','2026-05-20',8.0,'PRESENT', 70, 95, 70, 1, 'CERT-AOCS-2026-002','2027-05-20'),
(1,(SELECT id FROM m7_training_program WHERE program_code='TRN-COMP-001'),  1,'2026-06-15',NULL,        NULL,'SCHEDULED',NULL,NULL,60,NULL, NULL,                'NULL');

-- Skill matrix per employee (assessed via training or appraisal)
CREATE TABLE IF NOT EXISTS m7_employee_skill (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  emp_id BIGINT NOT NULL,
  skill_code VARCHAR(32) NOT NULL,
  skill_name VARCHAR(96) NOT NULL,
  category VARCHAR(24) NULL,
  proficiency VARCHAR(16) NOT NULL,    -- BEGINNER/INTERMEDIATE/ADVANCED/EXPERT
  certified TINYINT(1) NOT NULL DEFAULT 0,
  cert_authority VARCHAR(64) NULL,
  cert_no VARCHAR(48) NULL,
  cert_expiry DATE NULL,
  last_assessed DATE NULL,
  notes TEXT NULL,
  UNIQUE KEY uq_emp_skill (emp_id, skill_code),
  KEY idx_skill_cat (category, proficiency)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_employee_skill (tenant_id, emp_id, skill_code, skill_name, category, proficiency, certified, cert_authority, cert_no, cert_expiry, last_assessed) VALUES
(1,1,'IBR_BOILER','IBR-certified Boiler Operation','TECHNICAL','ADVANCED',     1,'TN IBR Board','IBR-TN-2024-1102','2027-08-15','2026-05-15'),
(1,1,'FIRST_AID', 'First Aid + CPR',                 'SAFETY',   'INTERMEDIATE', 1,'Red Cross',   'RC-2024-AID-091', '2026-08-12','2026-04-12'),
(1,2,'AOCS_QC',   'AOCS QC Methods',                  'QUALITY',  'EXPERT',       1,'AOCS India',  'AOCS-IN-2024-321','2027-05-20','2026-05-20'),
(1,2,'POSH',      'POSH Awareness',                   'COMPLIANCE','INTERMEDIATE',1,'HR',          'POSH-2026-002',   '2027-04-12','2026-04-12'),
(1,3,'FORKLIFT',  'Forklift Operation',               'TECHNICAL','BEGINNER',     0, NULL,         NULL,              NULL,        '2026-03-01');

-- =================================================================
-- 8. Register entities + workflows
-- =================================================================
INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('JobOpening',          'Job Opening',           'm7_job_opening'),
  ('JobApplication',      'Job Application',       'm7_job_application'),
  ('InterviewRound',      'Interview Round',       'm7_interview_round'),
  ('OnboardingTask',      'Onboarding Task',       'm7_onboarding_task'),
  ('PlantTransfer',       'Plant Transfer',        'm5_plant_transfer'),
  ('PlantTransferLine',   'Plant Transfer Line',   'm5_plant_transfer_line'),
  ('MaintenanceSchedule', 'Maintenance Schedule',  'm5_maintenance_schedule'),
  ('WastageLog',          'Wastage / Scrap Log',   'm5_wastage_log'),
  ('ConsumableIssue',     'Consumable Issue',      'm5_consumable_issue'),
  ('EnergyReading',       'Energy Meter Reading',  'm5_energy_reading'),
  ('TrainingProgram',     'Training Program',      'm7_training_program'),
  ('TrainingAttendance',  'Training Attendance',   'm7_training_attendance'),
  ('EmployeeSkill',       'Employee Skill',        'm7_employee_skill');

-- Additional workflow definitions for the rest of the user's listed flows
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.MaintenanceTask',1,'PUBLISHED','{"code":"M5.MaintenanceTask","version":1,"initialState":"DUE","states":[{"code":"DUE","assignedRole":"MaintenanceLead"},{"code":"WORK_ORDER","assignedRole":"MaintenanceTech"},{"code":"PARTS_READY","assignedRole":"StoreKeeper"},{"code":"SHUTDOWN_APPROVED","assignedRole":"Manager"},{"code":"IN_PROGRESS","assignedRole":"MaintenanceTech"},{"code":"QC_CHECK","assignedRole":"QC"},{"code":"COMPLETED","terminal":true},{"code":"DEFERRED","assignedRole":"Manager"}],"transitions":[{"from":"DUE","to":"WORK_ORDER","trigger":"page.submitted"},{"from":"WORK_ORDER","to":"PARTS_READY","trigger":"page.submitted"},{"from":"PARTS_READY","to":"SHUTDOWN_APPROVED","trigger":"page.submitted"},{"from":"SHUTDOWN_APPROVED","to":"IN_PROGRESS","trigger":"page.submitted"},{"from":"IN_PROGRESS","to":"QC_CHECK","trigger":"page.submitted"},{"from":"QC_CHECK","to":"COMPLETED","trigger":"page.submitted"},{"from":"DUE","to":"DEFERRED","trigger":"page.submitted"}]}'),
(1,'M5.WastageReport',1,'PUBLISHED','{"code":"M5.WastageReport","version":1,"initialState":"REPORTED","states":[{"code":"REPORTED","assignedRole":"Supervisor"},{"code":"VERIFIED","assignedRole":"QC"},{"code":"APPROVED","assignedRole":"Manager"},{"code":"CLOSED","terminal":true},{"code":"REJECTED","terminal":true}],"transitions":[{"from":"REPORTED","to":"VERIFIED","trigger":"page.submitted"},{"from":"VERIFIED","to":"APPROVED","trigger":"page.submitted"},{"from":"APPROVED","to":"CLOSED","trigger":"page.submitted"},{"from":"VERIFIED","to":"REJECTED","trigger":"page.submitted"}]}'),
(1,'M5.ReturnInward',1,'PUBLISHED','{"code":"M5.ReturnInward","version":1,"initialState":"INITIATED","states":[{"code":"INITIATED","assignedRole":"Sales"},{"code":"PICKUP_SCHEDULED","assignedRole":"Dispatch"},{"code":"RECEIVED","assignedRole":"GateKeeper"},{"code":"INSPECTED","assignedRole":"QC"},{"code":"STOCK_RECEIPT","assignedRole":"StoreKeeper"},{"code":"REFUND_PROCESSED","terminal":true},{"code":"DESTROYED","terminal":true},{"code":"REPACKED","terminal":true}],"transitions":[{"from":"INITIATED","to":"PICKUP_SCHEDULED","trigger":"page.submitted"},{"from":"PICKUP_SCHEDULED","to":"RECEIVED","trigger":"page.submitted"},{"from":"RECEIVED","to":"INSPECTED","trigger":"page.submitted"},{"from":"INSPECTED","to":"STOCK_RECEIPT","trigger":"page.submitted","guard":"#submitted[\"verdict\"] == \"RESELLABLE\""},{"from":"INSPECTED","to":"DESTROYED","trigger":"page.submitted","guard":"#submitted[\"verdict\"] == \"SCRAP\""},{"from":"STOCK_RECEIPT","to":"REPACKED","trigger":"page.submitted"},{"from":"STOCK_RECEIPT","to":"REFUND_PROCESSED","trigger":"page.submitted"}]}'),
(1,'M7.LeaveRequest',1,'PUBLISHED','{"code":"M7.LeaveRequest","version":1,"initialState":"SUBMITTED","states":[{"code":"SUBMITTED","assignedRole":"Manager"},{"code":"MGR_APPROVED","assignedRole":"HR"},{"code":"HR_VERIFIED","terminal":true},{"code":"REJECTED","terminal":true},{"code":"CANCELLED","terminal":true}],"transitions":[{"from":"SUBMITTED","to":"MGR_APPROVED","trigger":"page.submitted"},{"from":"MGR_APPROVED","to":"HR_VERIFIED","trigger":"page.submitted"},{"from":"SUBMITTED","to":"REJECTED","trigger":"page.submitted"},{"from":"SUBMITTED","to":"CANCELLED","trigger":"page.submitted"}]}'),
(1,'M7.TrainingAttendance',1,'PUBLISHED','{"code":"M7.TrainingAttendance","version":1,"initialState":"SCHEDULED","states":[{"code":"SCHEDULED","assignedRole":"HR"},{"code":"ATTENDED","assignedRole":"Trainer"},{"code":"ASSESSED","assignedRole":"Trainer"},{"code":"CERTIFIED","terminal":true},{"code":"FAILED_RETAKE","assignedRole":"HR"},{"code":"ABSENT","assignedRole":"HR"}],"transitions":[{"from":"SCHEDULED","to":"ATTENDED","trigger":"page.submitted"},{"from":"ATTENDED","to":"ASSESSED","trigger":"page.submitted"},{"from":"ASSESSED","to":"CERTIFIED","trigger":"page.submitted","guard":"#submitted[\"post_score\"] >= #submitted[\"pass_threshold\"]"},{"from":"ASSESSED","to":"FAILED_RETAKE","trigger":"page.submitted","guard":"#submitted[\"post_score\"] < #submitted[\"pass_threshold\"]"},{"from":"SCHEDULED","to":"ABSENT","trigger":"page.submitted"}]}');

-- 9. Update existing application status to match new workflow vocabulary
UPDATE m7_job_application SET current_step='Awaiting CV Screening' WHERE status='CV_SCREENING' AND current_step IS NULL;
