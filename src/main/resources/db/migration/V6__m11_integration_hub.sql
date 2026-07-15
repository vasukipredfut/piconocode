-- V6 — M11 Integration Hub substrate (Round 1H.1)
--
-- Creates the 7 core M11 tables per docs/requirements/modules/M11-integration-hub.md.
-- All tenant-scoped (tenant_id BIGINT NOT NULL DEFAULT 1) so M0 SaaS isolation
-- semantics carry through unchanged. InnoDB ROW_FORMAT=DYNAMIC utf8mb4 throughout.
-- Every CREATE TABLE uses IF NOT EXISTS — the M0PageSchemaInitialiser replays
-- this script idempotently on every boot.

-- ===========================================================================
-- 1) integration_connector_catalog  — the registry of connector codes (seeded
--    by V6_1 with 66 rows). Composite PK (code, version) so a tenant can pin
--    an instance to a specific version while platform engineers publish v2+.
-- ===========================================================================
CREATE TABLE IF NOT EXISTS integration_connector_catalog (
  code                  VARCHAR(64)  NOT NULL,
  version               INT          NOT NULL DEFAULT 1,
  category              VARCHAR(32)  NOT NULL,
  vendor                VARCHAR(64)  NOT NULL,
  display_name          VARCHAR(160) NOT NULL,
  description           VARCHAR(512) NULL,
  config_schema_json    JSON         NOT NULL,
  default_props_json    JSON         NOT NULL,
  supported_operations  JSON         NOT NULL,
  auth_methods          JSON         NOT NULL,
  dependencies          JSON         NOT NULL,
  pii_paths_json        JSON         NULL,
  residency_constraints VARCHAR(32)  NOT NULL DEFAULT 'NONE',
  docs_url              VARCHAR(255) NULL,
  status                VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (code, version),
  KEY idx_iccat_category (category, status),
  KEY idx_iccat_vendor   (vendor)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 2) integration_instance  — one row per tenant-registered external system.
--    Lifecycle: DRAFT → ENROLLED → ACTIVE → DEGRADED → DOWN → DISABLED → RETIRED.
-- ===========================================================================
CREATE TABLE IF NOT EXISTS integration_instance (
  id                     BIGINT       NOT NULL AUTO_INCREMENT,
  tenant_id              BIGINT       NOT NULL DEFAULT 1,
  code                   VARCHAR(48)  NOT NULL,
  connector_code         VARCHAR(64)  NOT NULL,
  connector_version      INT          NOT NULL DEFAULT 1,
  name                   VARCHAR(160) NOT NULL,
  description            VARCHAR(512) NULL,
  status                 VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',
  status_reason          VARCHAR(255) NULL,
  current_version        INT          NOT NULL DEFAULT 1,
  config_json            JSON         NOT NULL,
  mapping_json           JSON         NULL,
  mapped_workflow_codes  JSON         NULL,
  data_residency_flag    VARCHAR(32)  NOT NULL DEFAULT 'ANY',
  ip_allowlist_cidr      JSON         NULL,
  rate_limit_rps         INT          NULL,
  last_health_at         TIMESTAMP    NULL,
  last_event_at          TIMESTAMP    NULL,
  error_count_24h        INT          NOT NULL DEFAULT 0,
  custom_fields          JSON         NULL,
  optimistic_lock        INT          NOT NULL DEFAULT 0,
  created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by             BIGINT       NULL,
  updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_instance_tenant_code (tenant_id, code),
  KEY idx_instance_status (tenant_id, status, last_health_at),
  KEY idx_instance_conn   (connector_code, connector_version)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 3) integration_instance_secret  — AES-256-GCM envelope-encrypted secret blob.
--    `kid` is the envelope key id; `ciphertext` is base64(nonce || ct || tag);
--    `encoded_key_ref` records which KEK was used (for rotation visibility).
-- ===========================================================================
CREATE TABLE IF NOT EXISTS integration_instance_secret (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  instance_id     BIGINT       NOT NULL,
  kid             VARCHAR(64)  NOT NULL,
  ciphertext      LONGTEXT     NOT NULL,
  encoded_key_ref VARCHAR(128) NULL,
  rotates_at      TIMESTAMP    NULL,
  created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_isecret_instance (instance_id, kid),
  CONSTRAINT fk_isecret_instance FOREIGN KEY (instance_id)
       REFERENCES integration_instance (id) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 4) integration_instance_version  — every config publish writes a new row.
--    `status` mirrors lifecycle (`DRAFT`, `PUBLISHED`, `ROLLED_BACK`).
-- ===========================================================================
CREATE TABLE IF NOT EXISTS integration_instance_version (
  id            BIGINT      NOT NULL AUTO_INCREMENT,
  instance_id   BIGINT      NOT NULL,
  version       INT         NOT NULL,
  config_json   JSON        NOT NULL,
  mapping_json  JSON        NULL,
  rationale     TEXT        NULL,
  published_at  TIMESTAMP   NULL,
  published_by  BIGINT      NULL,
  status        VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_iiver_instance_version (instance_id, version),
  CONSTRAINT fk_iiver_instance FOREIGN KEY (instance_id)
       REFERENCES integration_instance (id) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 5) integration_event_log  — append-only event ledger. PII-redacted
--    request/response are written to `redacted_json` by the redactor; the
--    raw payloads stay in request_json/response_json for the trusted audit
--    role only (retention applies — 18 months by default).
-- ===========================================================================
CREATE TABLE IF NOT EXISTS integration_event_log (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  tenant_id     BIGINT       NOT NULL DEFAULT 1,
  instance_id   BIGINT       NOT NULL,
  direction     VARCHAR(16)  NOT NULL,
  ts            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status        VARCHAR(16)  NOT NULL,
  request_json  JSON         NULL,
  response_json JSON         NULL,
  error_code    VARCHAR(64)  NULL,
  latency_ms    INT          NULL,
  redacted_json JSON         NULL,
  PRIMARY KEY (id),
  KEY idx_ielog_tenant_inst_ts (tenant_id, instance_id, ts),
  KEY idx_ielog_status (status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 6) integration_inbound_token  — opaque path-token that gates a webhook
--    URL. `token_hash` is SHA-256(token); the plaintext is shown once on
--    creation and never persisted.
-- ===========================================================================
CREATE TABLE IF NOT EXISTS integration_inbound_token (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  instance_id  BIGINT       NOT NULL,
  token_hash   VARCHAR(128) NOT NULL,
  scope_json   JSON         NULL,
  valid_from   TIMESTAMP    NULL,
  valid_to     TIMESTAMP    NULL,
  last_used_at TIMESTAMP    NULL,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_iitok_hash (token_hash),
  KEY idx_iitok_instance (instance_id),
  CONSTRAINT fk_iitok_instance FOREIGN KEY (instance_id)
       REFERENCES integration_instance (id) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- 7) integration_schedule  — cron-driven pull / batch / run-now triggers.
-- ===========================================================================
CREATE TABLE IF NOT EXISTS integration_schedule (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  instance_id   BIGINT       NOT NULL,
  cron          VARCHAR(64)  NOT NULL,
  timezone      VARCHAR(64)  NOT NULL DEFAULT 'Asia/Kolkata',
  enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
  last_fired_at TIMESTAMP    NULL,
  next_fire_at  TIMESTAMP    NULL,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_ischd_instance (instance_id),
  KEY idx_ischd_next_fire (enabled, next_fire_at),
  CONSTRAINT fk_ischd_instance FOREIGN KEY (instance_id)
       REFERENCES integration_instance (id) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- Mark the workflow `external.api.call` action as IMPLEMENTED — the
-- ExternalApiCallActionHandler bean ships with V6 and the dispatcher will
-- now route to it. (Idempotent UPDATE; no-op if the row is absent.)
-- ===========================================================================
UPDATE action_registry
   SET implemented = TRUE,
       description = 'Invoke an M11 integration instance: external.api.call(instance=<code>, op=<method:/path>, payload={...}).'
 WHERE code = 'external.api.call';
