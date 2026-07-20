-- V53__role_catalog_seed.sql
-- Seed the canonical product role catalogue.
--
-- The `role` table (global, no tenant_id — one catalogue per deployment) is
-- what AuthController's landing-route lookup and every controller's
-- currentRoleName() resolve against: employee_details.roles_id -> role.id ->
-- role.name, then name is matched against entity_registry.write_role /
-- workflow assigned_role tokens. On a clean migration-chain rebuild this table
-- was left EMPTY (identity historically arrived via a data dump that seeded
-- employee_details but not role), so role-id -> name never resolved and EVERY
-- role-gated write returned 403 "Not authenticated" — including the OrderApp
-- daily-dump Masters upload (POST /api/m5/order-dispatch-detail/import).
--
-- These names are generic product roles (no customer specifics); the ids are
-- the deployment's role-id convention that employee_details already references.
-- Idempotent: ON DUPLICATE KEY UPDATE lets a later identity dump override.
INSERT INTO role (id, name, landing_route) VALUES
  (1,    'TenantAdmin',         NULL),
  (1001, 'FrontGate',           '/m10/front-gate'),
  (1002, 'ParkingSecurity',     '/m10/parking'),
  (1003, 'Dispatch',            '/m10/dispatch'),
  (1004, 'QC',                  '/m10/qc'),
  (1005, 'WeighmentOfficer',    NULL),
  (1006, 'UnloadingSupervisor', NULL),
  (1007, 'Manager',             NULL),
  (1008, 'Owner',               NULL),
  (1009, 'TenantAdmin',         NULL)
ON DUPLICATE KEY UPDATE name = VALUES(name), landing_route = VALUES(landing_route);
