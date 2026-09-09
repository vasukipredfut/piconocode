-- Split the single "write" flag on role_page_permission / employee_page_permission
-- into two: can_save (create a brand-new record via this page) and can_edit
-- (update an existing one). Business ask (Dhivya, 2026-09-09): a single combined
-- "write" checkbox doesn't let an admin grant Save without Edit, or vice versa.
--
-- Safe as a straight column swap (drop can_write, add can_save/can_edit) rather
-- than an additive migration -- role_page_permission/employee_page_permission
-- have zero rows in every environment (no seed migration ever inserts into
-- either), and can_write's only consumers are the enforcement code added
-- earlier this same session (ActionDispatcher/PagePermissionResolver), so
-- there is no live data or deployed behaviour to preserve.

ALTER TABLE role_page_permission
  DROP COLUMN can_write,
  ADD COLUMN can_save TINYINT(1) NOT NULL DEFAULT 0 AFTER can_read,
  ADD COLUMN can_edit TINYINT(1) NOT NULL DEFAULT 0 AFTER can_save;

ALTER TABLE employee_page_permission
  DROP COLUMN can_write,
  ADD COLUMN can_save TINYINT(1) NOT NULL DEFAULT 0 AFTER can_read,
  ADD COLUMN can_edit TINYINT(1) NOT NULL DEFAULT 0 AFTER can_save;

-- entity_field_registry: swap RolePagePermission.can_write for two fields
-- (backs the aria:role-page-permission generic-entity-list admin page, V213).
DELETE FROM entity_field_registry WHERE entity_code = 'RolePagePermission' AND field_code = 'can_write';

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json, hidden)
SELECT (SELECT COALESCE(MAX(id), 0) FROM entity_field_registry x) + ROW_NUMBER() OVER (ORDER BY c.ord),
       -1, 'RolePagePermission', c.field_code, c.display_label, c.data_type, c.filterable, c.sortable, c.searchable, c.required, c.widget_kind, c.options_json, c.hidden
FROM (
  SELECT 1 ord, 'can_save' field_code, 'Save (create new)' display_label, 'BOOLEAN' data_type, 1 filterable, 1 sortable, 0 searchable, 0 required, 'checkbox' widget_kind, NULL options_json, 0 hidden
  UNION ALL SELECT 2, 'can_edit', 'Edit (update existing)', 'BOOLEAN', 1, 1, 0, 0, 'checkbox', NULL, 0
) c
WHERE NOT EXISTS (
  SELECT 1 FROM entity_field_registry WHERE entity_code = 'RolePagePermission' AND field_code = c.field_code
);

-- aria:role-page-permission (V213, generic entity list): can_write -> can_save/can_edit.
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"RolePagePermission","filter":{"and":[{"field":"tenant_id","op":"eq","value":"ctx:tenantId"}]},"select":["id","role_id","page_code","can_read","can_save","can_edit","can_approve","updated_at"]},"columns":[{"field":"role_id","label":"Role"},{"field":"page_code","label":"Page"},{"field":"can_read","label":"View"},{"field":"can_save","label":"Save (create new)"},{"field":"can_edit","label":"Edit (update existing)"},{"field":"can_approve","label":"Approve"},{"field":"updated_at","label":"Last Updated","render":"datetime"}],"filters":[{"field":"role_id","label":"Role","type":"select","optionsSource":{"type":"datasource","spec":{"type":"entity","entity":"Role","select":["id","name"],"sort":[{"field":"name","dir":"asc"}]},"valueField":"id","labelField":"name"}},{"field":"page_code","label":"Page","type":"text-contains","applyTo":["page_code"]}],"sort":{"default":[{"field":"updated_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Role Page Permission"}}'
WHERE pd.code = 'aria:role-page-permission';

-- aria:role-page-access (V215, purpose-built rolePageAccess widget): the single
-- "write" checkbox column becomes two, "save"/"edit".
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind":"rolePageAccess","title":"Role Page Access","endpoints":{"roles":"/api/m0/access-matrix/roles","pages":"/api/m0/access-matrix/role-visibility","visibilitySave":"/api/m0/access-matrix/role-visibility","permissionSave":"/api/m0/access-matrix/role-permission"},"filters":[{"field":"roleId","label":"Role","type":"role-picker"},{"field":"category","label":"Module","type":"select"},{"field":"_q","label":"Search Page","type":"text-contains"}],"columns":[{"field":"category","label":"Module"},{"field":"title","label":"Page"},{"field":"visible","label":"Visible in Nav","widget":"checkbox"},{"field":"read","label":"Read","widget":"checkbox"},{"field":"save","label":"Save","widget":"checkbox"},{"field":"edit","label":"Edit","widget":"checkbox"},{"field":"approve","label":"Approve","widget":"checkbox"},{"field":"delete","label":"Delete","widget":"checkbox"}],"pageSize":200}'
WHERE pd.code = 'aria:role-page-access';
