-- V75 — re-run V72's auto-register logic across EVERY entity in
-- entity_registry (V72 was scoped to a hardcoded list of 17).
--
-- Field-level test agent surfaced ~10 NOT NULL columns still missing
-- from entity_field_registry — across AmcContract, BillOfMaterials,
-- AssetDepreciation, EnergyReading, IntegrationInstance, LeaveRequest,
-- PayrollRun, etc. The cleanest fix is to remove the entity-allowlist
-- and let the same INSERT IGNORE seed every entity.
--
-- INSERT IGNORE makes this idempotent — already-registered rows are
-- skipped by the (entity_code, field_code) uniq.

INSERT IGNORE INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, filterable, sortable,
   searchable, required, widget_kind)
SELECT
  e.code,
  CONVERT(c.column_name USING utf8mb4) COLLATE utf8mb4_unicode_ci,
  CONCAT(UPPER(SUBSTRING(c.column_name,1,1)),
         REPLACE(SUBSTRING(c.column_name,2), '_', ' ')),
  CASE
    WHEN c.data_type = 'tinyint'                                THEN 'BOOLEAN'
    WHEN c.data_type IN ('bigint','int','smallint','mediumint') THEN 'LONG'
    WHEN c.data_type IN ('decimal','numeric','float','double')  THEN 'DECIMAL'
    WHEN c.data_type = 'date'                                   THEN 'DATE'
    WHEN c.data_type IN ('datetime','timestamp')                THEN 'DATETIME'
    WHEN c.data_type IN ('text','longtext','mediumtext')        THEN 'TEXT'
    WHEN c.data_type = 'json'                                   THEN 'JSON'
    ELSE 'STRING'
  END,
  IF(c.column_name IN ('id','tenant_id','created_at','updated_at'), 0, 1),
  1,
  IF(c.data_type IN ('varchar','text','longtext','mediumtext'), 1, 0),
  IF(c.is_nullable = 'NO' AND c.column_default IS NULL
       AND c.extra NOT LIKE '%auto_increment%'
       AND c.extra NOT LIKE '%GENERATED%'
       AND c.column_name NOT IN ('id','tenant_id','created_at','updated_at'), 1, 0),
  CASE
    WHEN c.data_type = 'tinyint'                                THEN 'checkbox'
    WHEN c.data_type IN ('bigint','int','smallint','mediumint','decimal','numeric','float','double') THEN 'number'
    WHEN c.data_type = 'date'                                   THEN 'date'
    WHEN c.data_type IN ('datetime','timestamp')                THEN 'datetime'
    WHEN c.data_type IN ('text','longtext','mediumtext','json') THEN 'textarea'
    WHEN c.character_maximum_length IS NOT NULL
         AND c.character_maximum_length > 255                   THEN 'textarea'
    ELSE 'text'
  END
FROM information_schema.columns c
JOIN entity_registry e
  ON CONVERT(e.table_name USING utf8mb4) COLLATE utf8mb4_unicode_ci
   = CONVERT(c.table_name USING utf8mb4) COLLATE utf8mb4_unicode_ci
WHERE c.table_schema = DATABASE()
  AND c.extra NOT LIKE '%GENERATED%'
  AND CONVERT(c.column_name USING utf8mb4) COLLATE utf8mb4_unicode_ci
        NOT IN ('id','tenant_id','created_at','updated_at','created_by','updated_by');
