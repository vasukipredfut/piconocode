-- V122 — generic per-tenant key/value settings.
--
-- Holds tenant-scoped configuration that is DATA, not code: company name,
-- factory name/licence for statutory forms, etc. Replaces values that had
-- leaked as hardcoded strings into m6/m7 shared services (customization rule:
-- customer differences live in rows, not Java). Deliberately customer-AGNOSTIC
-- — no seed data here; tenants populate it via onboarding / the admin UI.

CREATE TABLE IF NOT EXISTS m0_tenant_setting (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    setting_key   VARCHAR(128) NOT NULL,
    setting_value TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_tenant_setting (tenant_id, setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- Make it manageable through the substrate (Page Designer / generic CRUD).
INSERT INTO entity_registry (code, display_name, table_name, category, lifecycle_kind)
  SELECT 'TenantSetting', 'Tenant Setting', 'm0_tenant_setting', 'M0', 'DEFINED'
  WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='TenantSetting' OR table_name='m0_tenant_setting');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'TenantSetting','setting_key','Key','STRING',1,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='TenantSetting' AND field_code='setting_key');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'TenantSetting','setting_value','Value','TEXT',0,0,1,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='TenantSetting' AND field_code='setting_value');
