-- Business decision (Dhivya, 2026-09-09): role-based permission only, no
-- per-employee override, no separate nav-visibility mechanism -- the
-- sidebar is already governed by GET /api/m0/pages' own role-scoped filter
-- (PageDefinitionRepository#findResolvedForRole, keyed off
-- role_page_permission.can_read). Both employee_page_permission (V195) and
-- role_page_visibility (V215) are confirmed unused; the Java side
-- (EmployeeAccessMatrixController, PageVisibilityController) was trimmed to
-- match in the same change as this migration.
--
-- aria:employee-access-matrix (V191) backed exactly the endpoints removed
-- here (GET /employees, GET /api/m0/access-matrix with employeeId) --
-- archived rather than hard-deleted, same convention PageDefinitionService
-- already uses for retired pages (status='ARCHIVED' hides it from listings
-- without losing the row/history).
--
-- aria:role-page-access (V215) keeps working -- its "Visible in Nav" column
-- and visibilitySave endpoint are dropped from its body_json, and its data
-- endpoint moves from the now-removed GET /role-visibility to the new
-- GET /role-pages (EmployeeAccessMatrixController#rolePages).

DROP TABLE IF EXISTS employee_page_permission;
DROP TABLE IF EXISTS role_page_visibility;

UPDATE page_definition
SET status = 'ARCHIVED', updated_at = NOW()
WHERE tenant_id = -1 AND code = 'aria:employee-access-matrix' AND status <> 'ARCHIVED';

UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind":"rolePageAccess","title":"Role Page Access","endpoints":{"roles":"/api/m0/access-matrix/roles","pages":"/api/m0/access-matrix/role-pages","permissionSave":"/api/m0/access-matrix/role-permission"},"filters":[{"field":"roleId","label":"Role","type":"role-picker"},{"field":"category","label":"Module","type":"select"},{"field":"_q","label":"Search Page","type":"text-contains"}],"columns":[{"field":"category","label":"Module"},{"field":"title","label":"Page"},{"field":"read","label":"Read","widget":"checkbox"},{"field":"save","label":"Save","widget":"checkbox"},{"field":"edit","label":"Edit","widget":"checkbox"},{"field":"approve","label":"Approve","widget":"checkbox"},{"field":"delete","label":"Delete","widget":"checkbox"}],"pageSize":200}'
WHERE pd.code = 'aria:role-page-access';
