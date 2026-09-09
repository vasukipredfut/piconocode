-- aria:role-page-permission (V213/V318, the generic-entity-list screen --
-- confirmed 2026-09-09 to be the screen actually in day-to-day use) is
-- missing "Delete", even though role_page_permission.can_delete has existed
-- since V215 and is already fully wired on the "Role Page Access" screen.
-- Nav visibility (role_page_visibility) is deliberately NOT added here --
-- confirmed not needed: this tenant already restricts the sidebar via
-- GET /api/m0/pages' role-scoped filter (PageDefinitionRepository#
-- findResolvedForRole, keyed off role_page_permission.can_read), so a
-- separate visibility mechanism would be redundant.

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'RolePagePermission', 'can_delete', 'Delete', 'BOOLEAN', 1, 1, 0, 0, 'checkbox'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'RolePagePermission' AND field_code = 'can_delete');

UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"RolePagePermission","filter":{"and":[{"field":"tenant_id","op":"eq","value":"ctx:tenantId"}]},"select":["id","role_id","page_code","can_read","can_save","can_edit","can_approve","can_delete","updated_at"]},"columns":[{"field":"role_id","label":"Role"},{"field":"page_code","label":"Page"},{"field":"can_read","label":"View"},{"field":"can_save","label":"Save (create new)"},{"field":"can_edit","label":"Edit (update existing)"},{"field":"can_approve","label":"Approve"},{"field":"can_delete","label":"Delete"},{"field":"updated_at","label":"Last Updated","render":"datetime"}],"filters":[{"field":"role_id","label":"Role","type":"select","optionsSource":{"type":"datasource","spec":{"type":"entity","entity":"Role","select":["id","name"],"sort":[{"field":"name","dir":"asc"}]},"valueField":"id","labelField":"name"}},{"field":"page_code","label":"Page","type":"text-contains","applyTo":["page_code"]}],"sort":{"default":[{"field":"updated_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Role Page Permission"}}'
WHERE pd.code = 'aria:role-page-permission';
