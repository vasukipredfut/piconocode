-- =====================================================================
-- M10 — Minimal dispatch DDL (Round 2 — Pico go-live substrate)
-- =====================================================================
-- Adds the customer-facing gate_pass + companion weighment/qc/unloading/
-- event tables. The M0 workflow_instance/workflow_event remain the source
-- of truth for *workflow* state, while gate_pass holds the BUSINESS record
-- that operators see — gp_number, status, vehicle_no, vendor, totals.
-- WorkflowEngineService writes to BOTH on transition (the engine is the
-- only writer of gate_pass.status; humans use the API).
--
-- All CREATE TABLE statements are IF NOT EXISTS — re-running this script
-- is a cheap no-op. The schema initialiser probes for `gate_pass` and only
-- runs the script when missing.
-- =====================================================================

-- ----- gate_pass ------------------------------------------------------
CREATE TABLE IF NOT EXISTS gate_pass (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gp_number VARCHAR(32) NOT NULL,
  flow_variant_code VARCHAR(64) NOT NULL,
  vehicle_no VARCHAR(32) NOT NULL,
  driver_name VARCHAR(128) NULL,
  driver_phone VARCHAR(16) NULL,
  vendor_id BIGINT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  closed_at TIMESTAMP NULL,
  total_unloading_qty DECIMAL(12,3) NULL,
  manager_overrides_count INT NOT NULL DEFAULT 0,
  UNIQUE KEY uq_gp_tenant_number (tenant_id, gp_number),
  KEY idx_gp_status (tenant_id, status, current_step),
  KEY idx_gp_wf_inst (workflow_instance_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- gate_pass_event ------------------------------------------------
-- A persistent business-side audit of every transition mirrored from
-- workflow_event. Workflow_event is the engine's audit; this is the
-- customer's gate-pass timeline (rendered on the dispatch UI without
-- having to traverse the workflow tables).
CREATE TABLE IF NOT EXISTS gate_pass_event (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gate_pass_id BIGINT NOT NULL,
  ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  kind VARCHAR(32) NOT NULL,
  from_status VARCHAR(32) NULL,
  to_status VARCHAR(32) NULL,
  page_code VARCHAR(128) NULL,
  actor_user_id BIGINT NULL,
  payload_json LONGTEXT NULL,
  KEY idx_gpe_gp_ts (gate_pass_id, ts),
  KEY idx_gpe_tenant_ts (tenant_id, ts)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- gate_pass_weighment -------------------------------------------
CREATE TABLE IF NOT EXISTS gate_pass_weighment (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gate_pass_id BIGINT NOT NULL,
  weighbridge_code VARCHAR(16) NOT NULL,
  reading_no INT NOT NULL,                    -- 1=loaded, 2=empty, 3=verify
  weight_kg DECIMAL(12,3) NOT NULL,
  captured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  captured_by BIGINT NULL,
  source VARCHAR(16) NOT NULL DEFAULT 'MANUAL',
  UNIQUE KEY uq_gpw_gp_reading (gate_pass_id, reading_no),
  KEY idx_gpw_wb (tenant_id, weighbridge_code, captured_at)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- gate_pass_qc ---------------------------------------------------
CREATE TABLE IF NOT EXISTS gate_pass_qc (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gate_pass_id BIGINT NOT NULL,
  sample_id VARCHAR(64) NULL,
  verdict VARCHAR(16) NULL,                   -- PASS / FAIL / CONDITIONAL
  parameters_json LONGTEXT NULL,
  drawn_by BIGINT NULL,
  drawn_at TIMESTAMP NULL,
  decided_by BIGINT NULL,
  decided_at TIMESTAMP NULL,
  remarks TEXT NULL,
  KEY idx_gpqc_gp (gate_pass_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- gate_pass_unloading -------------------------------------------
CREATE TABLE IF NOT EXISTS gate_pass_unloading (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gate_pass_id BIGINT NOT NULL,
  bay_code VARCHAR(16) NULL,
  tank_code VARCHAR(16) NULL,
  item_code VARCHAR(64) NULL,
  batch_no VARCHAR(64) NULL,
  qty_kg DECIMAL(12,3) NULL,
  started_at TIMESTAMP NULL,
  closed_at TIMESTAMP NULL,
  supervisor_id BIGINT NULL,
  KEY idx_gpu_gp (gate_pass_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- dispatch_gate --------------------------------------------------
CREATE TABLE IF NOT EXISTS dispatch_gate (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(16) NOT NULL,
  name VARCHAR(64) NOT NULL,
  gate_type VARCHAR(16) NOT NULL,              -- FRONT/PARKING/WB/EXIT
  scanner_workerless TINYINT(1) NOT NULL DEFAULT 1,
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uq_gate_tenant_code (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- dispatch_lane --------------------------------------------------
CREATE TABLE IF NOT EXISTS dispatch_lane (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gate_code VARCHAR(16) NOT NULL,
  code VARCHAR(16) NOT NULL,
  direction VARCHAR(8) NOT NULL DEFAULT 'IN',
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uq_lane_tenant_code (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- dispatch_parking_bay ------------------------------------------
CREATE TABLE IF NOT EXISTS dispatch_parking_bay (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gate_code VARCHAR(16) NOT NULL,
  code VARCHAR(16) NOT NULL,
  capacity INT NOT NULL DEFAULT 1,
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uq_bay_tenant_code (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- dispatch_tank_location ----------------------------------------
CREATE TABLE IF NOT EXISTS dispatch_tank_location (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(16) NOT NULL,
  tank_capacity_kl DECIMAL(8,2) NOT NULL,
  current_oil_grade VARCHAR(32) NOT NULL DEFAULT 'EMPTY',
  min_headspace_kl DECIMAL(6,2) NOT NULL DEFAULT 5,
  batch_density DECIMAL(6,4) NULL,
  last_cleaned_on DATE NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uq_tank_tenant_code (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- dispatch_vehicle ----------------------------------------------
CREATE TABLE IF NOT EXISTS dispatch_vehicle (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  registration_no VARCHAR(32) NOT NULL,
  vehicle_type VARCHAR(32) NOT NULL DEFAULT 'TANKER',
  tanker_compartments INT NULL,
  tare_weight_kg DECIMAL(10,2) NULL,
  capacity_kg DECIMAL(10,2) NULL,
  ownership VARCHAR(8) NOT NULL DEFAULT 'HIRED',
  transporter_name VARCHAR(128) NULL,
  rc_expiry DATE NULL,
  insurance_expiry DATE NULL,
  puc_expiry DATE NULL,
  tank_cleaning_cert_no VARCHAR(64) NULL,
  tank_cleaning_cert_expiry DATE NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uq_veh_tenant_reg (tenant_id, registration_no)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- dispatch_driver -----------------------------------------------
CREATE TABLE IF NOT EXISTS dispatch_driver (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  name VARCHAR(128) NOT NULL,
  dl_number VARCHAR(32) NOT NULL,
  dl_expiry DATE NULL,
  primary_mobile VARCHAR(16) NOT NULL,
  alt_mobile VARCHAR(16) NULL,
  preferred_language VARCHAR(4) NOT NULL DEFAULT 'ta',
  aadhaar_last4 VARCHAR(4) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uq_drv_tenant_dl (tenant_id, dl_number)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- dispatch_weighbridge ------------------------------------------
CREATE TABLE IF NOT EXISTS dispatch_weighbridge (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(16) NOT NULL,
  capacity_kg DECIMAL(10,0) NOT NULL,
  protocol VARCHAR(16) NOT NULL DEFAULT 'RS232',
  frame VARCHAR(16) NULL,
  device VARCHAR(64) NULL,
  tolerance_kg INT NOT NULL DEFAULT 20,
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uq_wb_tenant_code (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- tenant_form_layout (for the formLayouts bundle section) --------
CREATE TABLE IF NOT EXISTS tenant_form_layout (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  entity_code VARCHAR(128) NOT NULL,
  code VARCHAR(128) NOT NULL,
  label VARCHAR(256) NULL,
  device VARCHAR(64) NULL,
  body_json LONGTEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_fl_tenant_code (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- tenant_role (Pico roles + permission bundle) -------------------
-- Distinct from the legacy `role` table (referenced by employee_details).
-- TenantConfigBundleService writes to this; role linkage to login users
-- continues via the `role` table for back-compat in Round 2.
CREATE TABLE IF NOT EXISTS tenant_role (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(64) NOT NULL,
  label VARCHAR(128) NOT NULL,
  permissions_json LONGTEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_trole_tenant_code (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- notification_template (for the notificationTemplates section) -
CREATE TABLE IF NOT EXISTS notification_template (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(64) NOT NULL,
  channel VARCHAR(16) NOT NULL,                -- sms / push / email
  language VARCHAR(8) NOT NULL DEFAULT 'en-IN',
  subject VARCHAR(256) NULL,
  body TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_nt_tenant_code_lang (tenant_id, code, language)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- ALTER tenant_list_view: add UNIQUE on (tenant_id, page_slug, name) -
-- The original V5 schema lacks a UNIQUE constraint, so TenantConfigBundleService
-- has no way to upsert idempotently. The natural key is the triple
-- (tenant_id, page_slug, name) — uniqueness on (tenant_id, name) alone
-- would block multiple USER-scope views named the same on different pages
-- (legitimate per the M0 saved-view design).
ALTER TABLE tenant_list_view
    ADD UNIQUE INDEX IF NOT EXISTS uq_tlv_tenant_page_name (tenant_id, page_slug, name);
