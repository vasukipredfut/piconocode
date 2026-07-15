-- =====================================================================
-- M0 — Workflow persona seeding (employee_details + role)
-- =====================================================================
-- Conditional on the legacy employee_details + role tables existing
-- (the initialiser guards this — see M0PageSchemaInitialiser). Adds a
-- tenant_id column to employee_details (so the JWT can carry tid), and
-- inserts 4 persona users + role rows so the Pico Tanker-Inbound E2E
-- can log in as FrontGate / ParkingSecurity / Dispatch / QC.
-- =====================================================================

ALTER TABLE employee_details
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE employee_details
    ADD INDEX IF NOT EXISTS idx_emp_tenant (tenant_id);

INSERT INTO role (id, name) VALUES
  (1001, 'FrontGate'),
  (1002, 'ParkingSecurity'),
  (1003, 'Dispatch'),
  (1004, 'QC')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO employee_details (id, username, password, active, roles_id, tenant_id, employeename, status, branch_id)
VALUES
  (2001, '2001', '$2a$10$pivDYxR5RH2IOTau7oWBB.LQC7nf6BrsWcWxfFAXTrBoNnmWqhy9G', 1, 1001, 1, 'Pico FrontGate Guard', 'A', 8),
  (2002, '2002', '$2a$10$pivDYxR5RH2IOTau7oWBB.LQC7nf6BrsWcWxfFAXTrBoNnmWqhy9G', 1, 1002, 1, 'Pico Parking Security', 'A', 8),
  (2003, '2003', '$2a$10$pivDYxR5RH2IOTau7oWBB.LQC7nf6BrsWcWxfFAXTrBoNnmWqhy9G', 1, 1003, 1, 'Pico Dispatch Officer', 'A', 8),
  (2004, '2004', '$2a$10$pivDYxR5RH2IOTau7oWBB.LQC7nf6BrsWcWxfFAXTrBoNnmWqhy9G', 1, 1004, 1, 'Pico QC Officer', 'A', 8)
ON DUPLICATE KEY UPDATE
    username = VALUES(username), password = VALUES(password), roles_id = VALUES(roles_id),
    status = VALUES(status), branch_id = VALUES(branch_id), active = VALUES(active);
