-- V58__test_users_per_role.sql
-- One employee_details login per row in the `role` catalogue (V55__role_catalog_seed.sql),
-- for QA/dev testing of role-gated pages and workflow queues (FrontGate, Dispatch,
-- QC, etc.) without needing a real employee account per role.
--
-- Username = the new row's own auto-increment id (can't be known before insert,
-- so this is a 2-step INSERT + UPDATE ... WHERE username IS NULL).
-- Password hash below is BCrypt("Test@1234") generated with the project's own
-- BCryptPasswordEncoder (spring-security-crypto) — dev/QA credential only,
-- never reuse this password for a real account.
--
-- Idempotent: the INSERT only fires for role rows that don't already have a
-- matching test-user row (matched on the employeename marker, which embeds the
-- role id so the two rows sharing name='TenantAdmin' — id 1 and id 1009 — don't
-- collide); the UPDATE only ever touches rows still missing a username.
INSERT INTO employee_details (employeename, password, roles_id, status)
SELECT CONCAT('Test User - ', r.name, ' (role ', r.id, ')'),
       '$2a$10$WNm7mggKUwyI/r7T64pI/uoR.dxSIyqn1Y4HB463u0hAhDRgta4um',
       r.id,
       'A'
FROM role r
WHERE NOT EXISTS (
  SELECT 1 FROM employee_details e
  WHERE e.employeename = CONCAT('Test User - ', r.name, ' (role ', r.id, ')')
);

UPDATE employee_details
SET username = CAST(id AS CHAR)
WHERE username IS NULL
  AND employeename LIKE 'Test User - %(role %)';
