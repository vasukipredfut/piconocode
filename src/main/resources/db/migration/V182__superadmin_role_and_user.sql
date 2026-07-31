-- V182__superadmin_role_and_user.sql
-- Adds a SuperAdmin role + a single login for it.
--
-- landing_route is left NULL: per AuthController.signin() (see
-- V126__role_landing_route.sql), NULL means the frontend falls back to the
-- home dashboard, which is the desired default landing page for this account.
--
-- Full read/write access to every entity/category is granted in code, not
-- data: GenericEntityController.checkRole() now short-circuits to "allowed"
-- whenever the caller's role name is 'SuperAdmin', regardless of each
-- entity_registry row's read_role/write_role. Categories were already
-- unrestricted for any authenticated user (GET /api/m0/categories has no
-- role filtering), so no change was needed there.
--
-- Idempotent: `role.name` has no unique key, so existence is checked with
-- WHERE NOT EXISTS (same pattern as V64__missing_role_catalog_entries.sql);
-- the employee_details insert is guarded the same way on username.
INSERT INTO role (name, landing_route)
SELECT 'SuperAdmin', NULL
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'SuperAdmin');

-- Login: username 'superadmin', password 'changeme' (default, meant to be
-- rotated after first login). Hash below is BCrypt of that plaintext,
-- produced with the project's own BCryptPasswordEncoder (spring-security-crypto)
-- exactly as V60's test users were.
INSERT INTO employee_details (username, employeename, password, roles_id, status)
SELECT 'superadmin', 'Super Admin',
       '$2a$10$PY3gcR02NHGXyGbr3WbFO.SnBmEHY/OAV7Q5QcMvGKJvUjs4J8THq',
       (SELECT id FROM role WHERE name = 'SuperAdmin' LIMIT 1),
       'A'
WHERE NOT EXISTS (SELECT 1 FROM employee_details WHERE username = 'superadmin');
