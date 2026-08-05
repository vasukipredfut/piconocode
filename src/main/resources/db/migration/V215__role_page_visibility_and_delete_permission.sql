-- Role-based page visibility + delete permission
--
-- Two additions on top of V195__role_and_employee_page_permission.sql:
--
-- 1. can_delete on role_page_permission / employee_page_permission, alongside
--    the existing can_read/can_write/can_approve. Same resolution order as
--    those (employee override > role default > hardcoded default, which for
--    delete is false — deletion is closed by default, not open like read).
--
-- 2. role_page_visibility: which pages a role's users see in navigation at
--    all. Presence-based allow-list, kept as its own table rather than a
--    column on role_page_permission so it can't disturb the existing
--    "missing row = open read" convention those tables already rely on:
--      - a role with ZERO rows here is unrestricted (sees every published
--        page) — preserves today's behaviour for every role until an admin
--        opts a role into the allow-list.
--      - a role with ANY rows here is restricted to exactly those pages.
--    Resolved by com.aria.access.PageVisibilityController.

ALTER TABLE role_page_permission
  ADD COLUMN IF NOT EXISTS can_delete TINYINT(1) NOT NULL DEFAULT 0 AFTER can_approve;

ALTER TABLE employee_page_permission
  ADD COLUMN IF NOT EXISTS can_delete TINYINT(1) NOT NULL DEFAULT 0 AFTER can_approve;

CREATE TABLE IF NOT EXISTS role_page_visibility (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT -1,
  role_id BIGINT NOT NULL,
  page_code VARCHAR(128) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT DEFAULT NULL,
  UNIQUE KEY uk_role_page_visibility (tenant_id, role_id, page_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- Employee <-> Role list page (backs com.aria.access.EmployeeRoleListController)
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:employee-role-list', 'Employees & Roles',
  'Every employee with their assigned login role, department and status.',
  'HR', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:employee-role-list');

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED',
  '{"kind":"employeeRoleList","title":"Employees & Roles","endpoints":{"list":"/api/m0/employee-roles"},"filters":[{"field":"_q","label":"Search","type":"text-contains"},{"field":"roleId","label":"Role","type":"select"},{"field":"status","label":"Status","type":"select"}],"columns":[{"field":"employeeName","label":"Name"},{"field":"username","label":"Username"},{"field":"roleName","label":"Role"},{"field":"deptName","label":"Department"},{"field":"employeeType","label":"Type"},{"field":"status","label":"Status"}],"pageSize":50}',
  NOW()
FROM page_definition pd
WHERE pd.tenant_id = -1 AND pd.code = 'aria:employee-role-list'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'aria:employee-role-list' AND pd.current_version_id IS NULL;

-- Role Page Access admin page (backs the role-visibility + can_delete endpoints
-- added to com.aria.access.EmployeeAccessMatrixController)
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:role-page-access', 'Role Page Access',
  'Pick which pages a role can see in navigation, and set Read/Write/Approve/Delete for each.',
  'HR', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:role-page-access');

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED',
  '{"kind":"rolePageAccess","title":"Role Page Access","endpoints":{"roles":"/api/m0/access-matrix/roles","pages":"/api/m0/access-matrix/role-visibility","visibilitySave":"/api/m0/access-matrix/role-visibility","permissionSave":"/api/m0/access-matrix/role-permission"},"filters":[{"field":"roleId","label":"Role","type":"role-picker"},{"field":"category","label":"Module","type":"select"},{"field":"_q","label":"Search Page","type":"text-contains"}],"columns":[{"field":"category","label":"Module"},{"field":"title","label":"Page"},{"field":"visible","label":"Visible in Nav","widget":"checkbox"},{"field":"read","label":"Read","widget":"checkbox"},{"field":"write","label":"Write","widget":"checkbox"},{"field":"approve","label":"Approve","widget":"checkbox"},{"field":"delete","label":"Delete","widget":"checkbox"}],"pageSize":200}',
  NOW()
FROM page_definition pd
WHERE pd.tenant_id = -1 AND pd.code = 'aria:role-page-access'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'aria:role-page-access' AND pd.current_version_id IS NULL;
