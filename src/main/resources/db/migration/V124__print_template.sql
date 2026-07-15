-- V124 — Print as a first-class, config-driven feature (com.aria.print).
--
-- A printable document is DATA now: a print_template row says which table to
-- read, an optional child table for line items, a JSON spec describing the
-- voucher (header label, key/value rows with optional value-maps + number
-- formatting, conditional sections, a line-items table, signature boxes), and
-- an OPTIONAL printer_integration_code (an M11 integration_instance — e.g. a
-- ZPL label printer) to send to instead of returning browser HTML.
--
-- Any page or workflow can invoke GET /api/print/{code}/{id} (or POST .../send).
-- Replaces the hardcoded m10 PrintController (gate-pass + GRN) with generic code.

CREATE TABLE IF NOT EXISTS print_template (
    id                       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id                BIGINT       NOT NULL,
    code                     VARCHAR(64)  NOT NULL,
    name                     VARCHAR(128) NOT NULL,
    target_table             VARCHAR(64)  NOT NULL,
    child_table              VARCHAR(64)  NULL,
    child_fk                 VARCHAR(64)  NULL,
    spec_json                TEXT         NOT NULL,
    printer_integration_code VARCHAR(64)  NULL,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_print_template (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (code, display_name, table_name, category, lifecycle_kind)
  SELECT 'PrintTemplate', 'Print Template', 'print_template', 'M0', 'DEFINED'
  WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='PrintTemplate' OR table_name='print_template');
