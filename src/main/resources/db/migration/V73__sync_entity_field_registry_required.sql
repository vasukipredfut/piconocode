-- V73 — sync entity_field_registry.required with the actual column
-- nullability in INFORMATION_SCHEMA.
--
-- Root cause:
--   field-level test agent revealed 26 entities where POST fails with
--   "Field 'X' doesn't have a default value" because the registry said
--   required=0 but the table column is NOT NULL with no default.
--
-- Fix:
--   For every entity in entity_registry whose table exists, copy
--   columns.is_nullable into entity_field_registry.required so the
--   no-code form validation matches the DB constraint.
--
-- Also: where a column is NOT NULL AND has no default, AND the field
-- is not yet in entity_field_registry, that's a separate gap fixed by
-- V72 (auto-register). Here we only sync the required flag.

-- 1. NOT NULL with no DEFAULT in the DB → required=1 in registry
UPDATE entity_field_registry efr
JOIN entity_registry er ON er.code COLLATE utf8mb4_unicode_ci
                        = efr.entity_code COLLATE utf8mb4_unicode_ci
JOIN INFORMATION_SCHEMA.COLUMNS c
       ON CONVERT(c.TABLE_SCHEMA USING utf8mb4) COLLATE utf8mb4_unicode_ci = DATABASE()
      AND CONVERT(c.TABLE_NAME   USING utf8mb4) COLLATE utf8mb4_unicode_ci
              = er.table_name COLLATE utf8mb4_unicode_ci
      AND CONVERT(c.COLUMN_NAME  USING utf8mb4) COLLATE utf8mb4_unicode_ci
              = efr.field_code  COLLATE utf8mb4_unicode_ci
SET    efr.required = 1
WHERE  c.IS_NULLABLE = 'NO'
  AND  c.COLUMN_DEFAULT IS NULL
  AND  c.EXTRA NOT LIKE '%auto_increment%'
  AND  c.COLUMN_NAME NOT IN ('id','tenant_id','created_at','updated_at','created_by','updated_by')
  AND  efr.required = 0;

-- 2. Nullable in the DB → required=0 in registry (relax over-strict flags)
UPDATE entity_field_registry efr
JOIN entity_registry er ON er.code COLLATE utf8mb4_unicode_ci
                        = efr.entity_code COLLATE utf8mb4_unicode_ci
JOIN INFORMATION_SCHEMA.COLUMNS c
       ON CONVERT(c.TABLE_SCHEMA USING utf8mb4) COLLATE utf8mb4_unicode_ci = DATABASE()
      AND CONVERT(c.TABLE_NAME   USING utf8mb4) COLLATE utf8mb4_unicode_ci
              = er.table_name COLLATE utf8mb4_unicode_ci
      AND CONVERT(c.COLUMN_NAME  USING utf8mb4) COLLATE utf8mb4_unicode_ci
              = efr.field_code  COLLATE utf8mb4_unicode_ci
SET    efr.required = 0
WHERE  c.IS_NULLABLE = 'YES'
  AND  efr.required = 1;
