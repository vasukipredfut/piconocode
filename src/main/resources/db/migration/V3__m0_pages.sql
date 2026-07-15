-- =====================================================================
-- M0 — Page-Definition Substrate (Round 1F)
-- =====================================================================
-- Lays the DB foundation for the no-code configurable-pages capability.
-- Pages live as rows in `page_definition` (versioned via `page_version`,
-- bound to workflow steps via `page_workflow_binding`). The 14 first-
-- party Renderer primitives are catalogued in `page_element_catalog`
-- so the Builder palette can populate without code.
--
-- Schema-level notes:
--   - All tables InnoDB / utf8mb4 / ROW_FORMAT=DYNAMIC — matches existing
--     conventions (`MasterField.java` etc).
--   - `body_json` / `config_schema_json` / `field_map_json` use the JSON
--     type (Hibernate 6 maps via @JdbcTypeCode(SqlTypes.JSON), matching
--     the OrderRepository / pgism migration already on file).
--   - `tenant_id` is denormalised onto the binding rows for hot-path
--     lookup; multitenant scoping is enforced at the service layer for
--     now (`// TODO M0-tenant-context (Round 1H)`).
-- =====================================================================

CREATE TABLE IF NOT EXISTS page_definition (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  code VARCHAR(128) NOT NULL,
  title VARCHAR(256) NOT NULL,
  description TEXT NULL,
  category VARCHAR(64) NULL,
  current_version_id BIGINT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_page_tenant_code (tenant_id, code),
  KEY idx_page_tenant_status (tenant_id, status, category)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS page_version (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  page_id BIGINT NOT NULL,
  version INT NOT NULL,
  status VARCHAR(16) NOT NULL,
  body_json JSON NOT NULL,
  rationale TEXT NULL,
  published_at TIMESTAMP NULL,
  published_by BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_page_version (page_id, version),
  CONSTRAINT fk_pv_page FOREIGN KEY (page_id) REFERENCES page_definition(id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS page_workflow_binding (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  page_code VARCHAR(128) NOT NULL,
  workflow_code VARCHAR(128) NOT NULL,
  step_code VARCHAR(128) NOT NULL,
  role ENUM('INPUT','OUTPUT','BOTH') NOT NULL DEFAULT 'INPUT',
  field_map_json JSON NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_pwb (tenant_id, page_code, workflow_code, step_code, role),
  KEY idx_pwb_workflow (tenant_id, workflow_code, step_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS page_element_catalog (
  code VARCHAR(64) NOT NULL PRIMARY KEY,
  category VARCHAR(32) NOT NULL,
  config_schema_json JSON NULL,
  default_props_json JSON NULL,
  description TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Seed the 14 first-party primitives. Builder reads this to render its
-- palette; Renderer ignores it (the JS registry is the runtime authority).
INSERT INTO page_element_catalog (code, category, description) VALUES
  ('Grid','LAYOUT','Responsive grid container'),
  ('Section','LAYOUT','Card section with title'),
  ('KPI','DISPLAY','Single-number KPI tile'),
  ('FormField','INPUT','Generic input bound to a data source'),
  ('Form','INPUT','Composes multiple FormFields with submit'),
  ('DataTable','DISPLAY','Server-paginated table'),
  ('LineChart','CHART','Time-series line chart'),
  ('TelemetryGauge','DEVICE','Live gauge bound to a stream'),
  ('QrScan','DEVICE','Camera-based QR scan with manual fallback'),
  ('WeighbridgeLive','DEVICE','Live-weight tile bound to M9 weighbridge worker'),
  ('OtpVerify','ACTION','OTP send/verify pair bound to /api/m10/otp/*'),
  ('WorkflowActionButton','ACTION','Emits a workflow transition'),
  ('WorkflowQueueList','DISPLAY','Persona work-queue from /api/m10/queues/{persona}'),
  ('SignaturePad','INPUT','Captures a signature as base64 PNG');
