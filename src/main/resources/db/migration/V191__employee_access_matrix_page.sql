-- Employee Access Matrix
--
-- Read-only report page: for a chosen employee (employee_details — the real
-- login/identity table, roles_id -> role.id), shows Read/Write/Approve for
-- every screen in entity_registry, derived from that entity's
-- read_role/write_role/approver_role columns against the employee's role
-- name. No new tables — everything is computed at request time by
-- com.aria.access.EmployeeAccessMatrixController from data that already
-- exists (employee_details, role, entity_registry). Same custom-kind +
-- endpoints-block shape as aria:audit-tracker-groupby (V72), since this data
-- shape (a join + per-row CSV membership test) can't be expressed as a
-- generic dataSource:{type:"entity"} page.

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:employee-access-matrix', 'Employee Access Matrix',
  'Pick an employee to see their assigned role and Read/Write/Approve access across every registered screen/entity.',
  'HR', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:employee-access-matrix');

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED',
  '{"kind":"employeeAccessMatrix","title":"Employee Access Matrix","endpoints":{"employees":"/api/m0/access-matrix/employees","matrix":"/api/m0/access-matrix"},"filters":[{"field":"employeeId","label":"Employee","type":"employee-picker"},{"field":"category","label":"Module","type":"select"},{"field":"_q","label":"Search Screen","type":"text-contains"},{"field":"onlyGranted","label":"Only screens with access","type":"boolean"}],"columns":[{"field":"category","label":"Module"},{"field":"displayName","label":"Screen"},{"field":"read","label":"Read"},{"field":"write","label":"Write"},{"field":"approve","label":"Approve"}],"pageSize":100}',
  NOW()
FROM page_definition pd
WHERE pd.tenant_id = -1 AND pd.code = 'aria:employee-access-matrix'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'aria:employee-access-matrix' AND pd.current_version_id IS NULL;
