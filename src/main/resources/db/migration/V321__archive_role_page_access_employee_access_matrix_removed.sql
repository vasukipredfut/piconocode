-- Business decision (Dhivya, 2026-09-09): "Role Page Permission" (the
-- generic-entity-list screen, V213) is the ONLY permission-management
-- screen going forward -- confirmed to already cover create/update/delete
-- of role_page_permission via the generic /api/m0/entities/RolePagePermission
-- CRUD, so EmployeeAccessMatrixController (roles dropdown, role-permission
-- upsert/delete, role-pages listing, my-permissions self-service) was
-- deleted outright as fully redundant.
--
-- aria:role-page-access (V215) backed exactly the endpoints that
-- controller exposed (/api/m0/access-matrix/roles, /role-pages,
-- /role-permission) -- with that controller gone, this page would be a
-- dead screen (every button 404s). Archived rather than hard-deleted, same
-- convention as V320's aria:employee-access-matrix.

UPDATE page_definition
SET status = 'ARCHIVED', updated_at = NOW()
WHERE tenant_id = -1 AND code = 'aria:role-page-access' AND status <> 'ARCHIVED';
