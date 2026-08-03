-- Editable admin UI for role_page_permission (V195). The tables + the
-- read/write/approve API (EmployeeAccessMatrixController's /role-permission,
-- /employee-permission) already existed, but nothing exposed a page a
-- TenantAdmin could actually pick a Role + Page and check Read/Write/Approve
-- boxes on -- aria:employee-access-matrix (V191) is a read-only report and
-- never wires those PUT endpoints to any UI at all.
--
-- Rather than build a bespoke frontend for this, this follows the standard
-- "Generic entity CRUD via registry" pattern already used throughout this
-- app (see CLAUDE.md): register role_page_permission as a normal entity,
-- register its columns, stamp a plain "kind":"list" page. GenericEntityController
-- then gives Create/Edit/Delete for free (its own edit modal is built from
-- entity_field_registry, same as every other admin screen in this app) --
-- no frontend code change needed.
--
-- Kept to the 3 flags that already exist (can_read/can_write/can_approve --
-- there is no can_create/can_update/can_delete split, and none is being
-- added here). Page-level enforcement (actually filtering nav/pages or
-- gating CRUD buttons elsewhere by this data) is explicitly NOT part of
-- this change -- today this is a "grant catalogue" UI only, same scope as
-- the existing employee-access-matrix report.

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, read_role, write_role)
SELECT -1, 'RolePagePermission', 'Role Page Permission', 'role_page_permission',
  'Per-role Read/Write/Approve grant for one page. Missing row for a (role, page) pair means the hardcoded default: read=true, write=false, approve=false. Resolution order (see EmployeeAccessMatrixController): employee_page_permission override > this > default.',
  'Admin', 'TenantAdmin', 'TenantAdmin'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'RolePagePermission');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json, hidden)
SELECT (SELECT COALESCE(MAX(id), 0) FROM entity_field_registry x) + ROW_NUMBER() OVER (ORDER BY c.ord),
       -1, 'RolePagePermission', c.field_code, c.display_label, c.data_type, c.filterable, c.sortable, c.searchable, c.required, c.widget_kind, c.options_json, c.hidden
FROM (
  SELECT 1 ord, 'role_id' field_code, 'Role' display_label, 'LONG' data_type, 1 filterable, 1 sortable, 0 searchable, 1 required,
         'entity-ref' widget_kind, '{"entity":"Role","valueField":"id","labelField":"name"}' options_json, 0 hidden
  UNION ALL SELECT 2, 'page_code', 'Page', 'STRING', 1, 1, 1, 1,
         'entity-ref', '{"entity":"PageDefinition","valueField":"code","labelField":"title"}', 0
  UNION ALL SELECT 3, 'can_read', 'View', 'BOOLEAN', 1, 1, 0, 0, 'checkbox', NULL, 0
  UNION ALL SELECT 4, 'can_write', 'Save / Update', 'BOOLEAN', 1, 1, 0, 0, 'checkbox', NULL, 0
  UNION ALL SELECT 5, 'can_approve', 'Approve', 'BOOLEAN', 1, 1, 0, 0, 'checkbox', NULL, 0
  UNION ALL SELECT 6, 'updated_at', 'Last Updated', 'DATETIME', 0, 1, 0, 0, 'datetime', NULL, 0
) c
WHERE NOT EXISTS (
  SELECT 1 FROM entity_field_registry WHERE entity_code = 'RolePagePermission' AND field_code = c.field_code
);

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:role-page-permission', 'Role Page Permission',
  'Grant a role View / Save-Update / Approve access to a specific page. One row per (role, page); no row means the default (View only).',
  'Admin', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:role-page-permission');

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED',
  '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"RolePagePermission","filter":{"and":[{"field":"tenant_id","op":"eq","value":"ctx:tenantId"}]},"select":["id","role_id","page_code","can_read","can_write","can_approve","updated_at"]},"columns":[{"field":"role_id","label":"Role"},{"field":"page_code","label":"Page"},{"field":"can_read","label":"View"},{"field":"can_write","label":"Save / Update"},{"field":"can_approve","label":"Approve"},{"field":"updated_at","label":"Last Updated","render":"datetime"}],"filters":[{"field":"role_id","label":"Role","type":"select","optionsSource":{"type":"datasource","spec":{"type":"entity","entity":"Role","select":["id","name"],"sort":[{"field":"name","dir":"asc"}]},"valueField":"id","labelField":"name"}},{"field":"page_code","label":"Page","type":"text-contains","applyTo":["page_code"]}],"sort":{"default":[{"field":"updated_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Role Page Permission"}}',
  NOW()
FROM page_definition pd
WHERE pd.tenant_id = -1 AND pd.code = 'aria:role-page-permission'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'aria:role-page-permission' AND pd.current_version_id IS NULL;
