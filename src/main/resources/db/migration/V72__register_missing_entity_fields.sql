-- V72: Auto-register the 17 entities whose entity_registry row exists
-- but entity_field_registry has zero rows. Done as a single
-- INSERT IGNORE … SELECT … FROM information_schema with explicit
-- COLLATE overrides — MariaDB 10.10+ uses uca1400_ai_ci for INFORMATION_SCHEMA
-- and entity_registry was created with unicode_ci, so every join needs
-- normalisation.

INSERT IGNORE INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, filterable, sortable,
   searchable, required, widget_kind)
SELECT
  e.code,
  CONVERT(c.column_name USING utf8mb4) COLLATE utf8mb4_unicode_ci,
  -- Display label = "Snake Case" → "Snake Case"
  CONCAT(UPPER(SUBSTRING(c.column_name,1,1)),
         REPLACE(SUBSTRING(c.column_name,2), '_', ' ')),
  CASE
    WHEN c.data_type = 'tinyint'                                THEN 'BOOLEAN'
    WHEN c.data_type IN ('bigint','int','smallint','mediumint') THEN 'LONG'
    WHEN c.data_type IN ('decimal','numeric','float','double')  THEN 'DECIMAL'
    WHEN c.data_type = 'date'                                   THEN 'DATE'
    WHEN c.data_type IN ('datetime','timestamp')                THEN 'DATETIME'
    WHEN c.data_type IN ('text','longtext','mediumtext')        THEN 'TEXT'
    ELSE 'STRING'
  END,
  IF(c.column_name IN ('id','tenant_id','created_at','updated_at'), 0, 1),
  1,
  IF(c.data_type IN ('varchar','text','longtext','mediumtext'), 1, 0),
  IF(c.is_nullable = 'NO' AND c.column_name NOT IN ('id'), 1, 0),
  CASE
    WHEN c.data_type = 'tinyint'                                THEN 'checkbox'
    WHEN c.data_type IN ('bigint','int','smallint','mediumint','decimal','numeric','float','double') THEN 'number'
    WHEN c.data_type = 'date'                                   THEN 'date'
    WHEN c.data_type IN ('datetime','timestamp')                THEN 'datetime'
    WHEN c.data_type IN ('text','longtext','mediumtext')        THEN 'textarea'
    WHEN c.character_maximum_length IS NOT NULL
         AND c.character_maximum_length > 255                   THEN 'textarea'
    ELSE 'text'
  END
FROM information_schema.columns c
JOIN entity_registry e
  ON CONVERT(e.table_name USING utf8mb4) COLLATE utf8mb4_unicode_ci
   = CONVERT(c.table_name USING utf8mb4) COLLATE utf8mb4_unicode_ci
WHERE c.table_schema = DATABASE()
  AND CONVERT(c.column_name USING utf8mb4) COLLATE utf8mb4_unicode_ci NOT IN ('tenant_id')
  AND e.code IN (
    'AttendanceSummary','EmployeeExperience','EmployeeFamily',
    'EmployeeQualification','ESISetting','Gstr1Summary',
    'IntegrationConnectorCatalog','LWFSetting','PageVersion',
    'PayrollLine','PFSetting','PlantTransferLine','PoshCommittee',
    'PoshIncident','RfqVendor','SalesInvoiceLine','ShipmentCheckpoint')
  -- skip if already registered (uniq key on (entity_code, field_code) does most of this,
  -- but uniq isn't declared, so we anti-join)
  AND NOT EXISTS (
    SELECT 1 FROM entity_field_registry f
    WHERE CONVERT(f.entity_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = e.code
      AND CONVERT(f.field_code USING utf8mb4) COLLATE utf8mb4_unicode_ci
          = CONVERT(c.column_name USING utf8mb4) COLLATE utf8mb4_unicode_ci
  );
