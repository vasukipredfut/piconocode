-- V74 — fix department field-name drift.
--
-- Root cause:
--   DepartmentDetails JPA entity uses column `name`, but
--   entity_field_registry rows (and several page_definition bodies)
--   reference `departmentname`. The lookup query crashes with
--   `Unknown column 'department_details.departmentname' in 'SELECT'`
--   whenever a page tries to render a Department dropdown.
--
-- Fix:
--   1. Rename the registry entry `departmentname` → `name`
--   2. Patch any options_json refs in OTHER entities that pointed at
--      labelField=departmentname → labelField=name
--   3. Patch page_definition body_json strings same way (best-effort
--      JSON_REPLACE; MariaDB tolerates the no-op on non-matching rows).

-- 1. Registry field rename — drop the stale row (uniq prevents UPDATE)
DELETE FROM entity_field_registry
WHERE entity_code = 'Department'
  AND field_code  = 'departmentname';

-- Re-insert as `name` ONLY if it isn't already present (V72 may have
-- auto-registered it from INFORMATION_SCHEMA).
INSERT IGNORE INTO entity_field_registry
    (tenant_id, entity_code, field_code, display_label,
     data_type, widget_kind, required, filterable, sortable, searchable,
     display_order)
SELECT 1, 'Department', 'name', 'Department Name',
       'STRING', 'text', 1, 1, 1, 1, 10
FROM   dual
WHERE  NOT EXISTS (
    SELECT 1 FROM entity_field_registry
     WHERE entity_code='Department' AND field_code='name');

-- 2. Patch cross-entity options_json refs that say labelField=departmentname
UPDATE entity_field_registry
SET    options_json = REPLACE(options_json, '"departmentname"', '"name"')
WHERE  options_json IS NOT NULL
  AND  options_json LIKE '%"departmentname"%';

-- 3. Patch page_definition body_json — both labelField and column refs
UPDATE page_version
SET    body_json = REPLACE(body_json, '"departmentname"', '"name"')
WHERE  body_json LIKE '%"departmentname"%';
