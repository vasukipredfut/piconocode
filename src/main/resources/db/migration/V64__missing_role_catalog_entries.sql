-- V64__missing_role_catalog_entries.sql
-- Every workflow_definition assignedRole and entity_registry.write_role token
-- that has no matching `role` row. V55__role_catalog_seed.sql only seeded 9
-- names (TenantAdmin, FrontGate, ParkingSecurity, Dispatch, QC, WeighmentOfficer,
-- UnloadingSupervisor, Manager, Owner); a full scan of every workflow_definition
-- body_json (V2 base + V47/V56/V57 overrides) across all 49 workflow codes
-- turned up 37 distinct assignedRole values, so 30 have been missing since V55.
--
-- This matters for the same reason V55 documents: employee_details.roles_id ->
-- role.id -> role.name is how a logged-in user's role gets resolved to a name
-- string for entity_registry.write_role / workflow assigned_role comparisons
-- (see currentRoleName() in GenericEntityController, OrderDispatchDetailImportController,
-- OnboardingController). A user whose roles_id should point at e.g. BillingOfficer
-- or PMTTeam has had no row to point at -- the same 403 failure mode V55 fixed
-- for the original 9, just for everything after it.
--
-- No explicit ids: V55 hardcoded ids (1, 1001-1009) because those matched
-- pre-existing employee_details.roles_id references in production. None of
-- these 30 names have ever had a role row, so nothing already points at a
-- specific id for them -- let AUTO_INCREMENT assign one rather than risk
-- colliding with whatever a live deployment already holds in this id range.
-- Idempotent: INSERT ... WHERE NOT EXISTS, matched by name (no unique key on
-- `role.name`, so this guard is what keeps a re-run from duplicating rows).
INSERT INTO role (name, landing_route)
SELECT x.name, x.landing_route FROM (
  SELECT 'Accountant'          AS name, NULL AS landing_route UNION ALL
  SELECT 'BillingOfficer',            NULL UNION ALL
  SELECT 'Buyer',                     NULL UNION ALL
  SELECT 'CEO',                       NULL UNION ALL
  SELECT 'CFO',                       NULL UNION ALL
  SELECT 'Candidate',                 NULL UNION ALL
  SELECT 'Driver',                    NULL UNION ALL
  SELECT 'EHS',                       NULL UNION ALL
  SELECT 'Employee',                  NULL UNION ALL
  SELECT 'GateKeeper',                NULL UNION ALL
  SELECT 'HR',                        NULL UNION ALL
  SELECT 'HiringManager',             NULL UNION ALL
  SELECT 'InterviewPanel',            NULL UNION ALL
  SELECT 'LoadSupervisor',            NULL UNION ALL
  SELECT 'LoadingSupervisor',         NULL UNION ALL
  SELECT 'MaintenanceLead',           NULL UNION ALL
  SELECT 'MaintenanceTech',           NULL UNION ALL
  SELECT 'PMTTeam',                   NULL UNION ALL
  SELECT 'PackingOp',                 NULL UNION ALL
  SELECT 'Planning',                  NULL UNION ALL
  SELECT 'PlantManager',              NULL UNION ALL
  SELECT 'Purchase',                  NULL UNION ALL
  SELECT 'Sales',                     NULL UNION ALL
  SELECT 'StockOwner',                NULL UNION ALL
  SELECT 'StoreKeeper',               NULL UNION ALL
  SELECT 'Supervisor',                NULL UNION ALL
  SELECT 'Trainer',                   NULL UNION ALL
  SELECT 'UnloadSupervisor',          NULL UNION ALL
  SELECT 'Vendor',                    NULL UNION ALL
  SELECT 'WeighbridgeOp',             NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM role r WHERE r.name = x.name);
