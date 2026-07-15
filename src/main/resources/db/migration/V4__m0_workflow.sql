-- =====================================================================
-- M0 — Workflow Runtime (Round 1F-followon)
-- =====================================================================
-- Adds the three workflow tables (definition / instance / event) that the
-- engine drives during page-submission, and seeds the M10.TankerInbound
-- v1 workflow definition. Tenant + employee seed is V4_1 (conditional on
-- the legacy employee_details table existing — split out so the M0 IT
-- testcontainers stay schema-light).
-- =====================================================================

-- ----- workflow_definition --------------------------------------------
CREATE TABLE IF NOT EXISTS workflow_definition (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  code VARCHAR(128) NOT NULL,
  version INT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
  body_json JSON NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_wfd (tenant_id, code, version),
  KEY idx_wfd_code (tenant_id, code, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- workflow_instance ----------------------------------------------
CREATE TABLE IF NOT EXISTS workflow_instance (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  workflow_code VARCHAR(128) NOT NULL,
  workflow_version INT NOT NULL,
  current_state VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  business_ref VARCHAR(128) NULL,
  submitted_data_json JSON NULL,
  assigned_role VARCHAR(64) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_wi_queue (tenant_id, assigned_role, status, current_state),
  KEY idx_wi_ref (tenant_id, business_ref)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- workflow_event -------------------------------------------------
CREATE TABLE IF NOT EXISTS workflow_event (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  instance_id BIGINT NOT NULL,
  ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  kind VARCHAR(32) NOT NULL,
  from_state VARCHAR(64) NULL,
  to_state VARCHAR(64) NULL,
  page_code VARCHAR(128) NULL,
  actor_user_id BIGINT NULL,
  payload_json JSON NULL,
  CONSTRAINT fk_we_instance FOREIGN KEY (instance_id) REFERENCES workflow_instance(id),
  KEY idx_we_instance_ts (instance_id, ts)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- seed M10.TankerInbound v1 --------------------------------------
INSERT INTO workflow_definition (tenant_id, code, version, status, body_json)
SELECT 1, 'M10.TankerInbound', 1, 'PUBLISHED',
  JSON_OBJECT(
    'code','M10.TankerInbound',
    'version',1,
    'initialState','DRAFT',
    'states', JSON_ARRAY(
      JSON_OBJECT('code','DRAFT'),
      JSON_OBJECT('code','GATE_PASS_CREATED','assignedRole','ParkingSecurity'),
      JSON_OBJECT('code','PARKING_IN','assignedRole','Dispatch'),
      JSON_OBJECT('code','DISPATCH_QUEUED','assignedRole','QC'),
      JSON_OBJECT('code','QC_APPROVED','assignedRole','WeighmentOfficer'),
      JSON_OBJECT('code','QC_REJECTED','terminal',true)
    ),
    'transitions', JSON_ARRAY(
      JSON_OBJECT('from','DRAFT','to','GATE_PASS_CREATED','trigger','page.submitted','pageCode','pico:front-gate-create'),
      JSON_OBJECT('from','GATE_PASS_CREATED','to','PARKING_IN','trigger','page.submitted','pageCode','pico:parking-scan'),
      JSON_OBJECT('from','PARKING_IN','to','DISPATCH_QUEUED','trigger','page.submitted','pageCode','pico:dispatch-vehicle-entry'),
      JSON_OBJECT('from','DISPATCH_QUEUED','to','QC_APPROVED','trigger','page.submitted','pageCode','pico:qc-approval','guard','#submitted[''status''] == ''APPROVED'''),
      JSON_OBJECT('from','DISPATCH_QUEUED','to','QC_REJECTED','trigger','page.submitted','pageCode','pico:qc-approval','guard','#submitted[''status''] == ''REJECTED''')
    )
  )
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM workflow_definition WHERE tenant_id = 1 AND code = 'M10.TankerInbound' AND version = 1
);
