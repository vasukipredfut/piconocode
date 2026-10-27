-- V389: PICO (Spokes) Module 8 -- preparation for loading ERPNext data. Requires V382-V388.
--
-- 1. erp_ref (ERPNext document name) on the tables that lacked it, and UNIQUE (tenant_id, erp_ref)
--    everywhere migrate.py upserts by it -- the load is idempotent and re-runnable (delta cut-over).
--    NULL erp_ref (rows created in Aria) never collide: UNIQUE allows many NULLs.
-- 2. Roles the PICO modules use (role is a global table).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE m7_employee
  ADD COLUMN IF NOT EXISTS erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Employee name (HR-EMP-xxxxx) for migrated rows; NULL = created in Aria';
ALTER TABLE m7_shift_type
  ADD COLUMN IF NOT EXISTS erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Shift Type name; NULL = created in Aria';
ALTER TABLE m7_employee_salary
  ADD COLUMN IF NOT EXISTS erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Salary Structure Assignment name; NULL = created in Aria';
ALTER TABLE m8_lead_city
  ADD COLUMN IF NOT EXISTS erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Lead City name; NULL = created in Aria';

CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_employee        ON m7_employee (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_shift_type      ON m7_shift_type (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_employee_salary ON m7_employee_salary (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m8_lead_city       ON m8_lead_city (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_holiday_list    ON m7_holiday_list (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_checkin         ON m7_employee_checkin (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_overtime        ON m7_overtime_entry (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_leave_type      ON m7_leave_type (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_leave_alloc     ON m7_leave_allocation (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_leave_request   ON m7_leave_request (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_payroll_run     ON m7_payroll_run (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_exp_type        ON m7_expense_claim_type (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_exp_claim       ON m7_expense_claim (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m7_announcement    ON m7_announcement (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m8_lead            ON m8_lead (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m8_quote           ON m8_quote (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m8_customer        ON m8_customer (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m9_install_report  ON m9_installation_report (tenant_id, erp_ref);
CREATE UNIQUE INDEX IF NOT EXISTS uq_erp_m9_ticket          ON m9_service_ticket (tenant_id, erp_ref);

-- Roles referenced by the PICO validators / actions (Modules 5-7). Employee = plain app user.
INSERT INTO role (name, landing_route) SELECT 'Sales', NULL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'Sales');
INSERT INTO role (name, landing_route) SELECT 'Service', NULL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'Service');
INSERT INTO role (name, landing_route) SELECT 'Employee', NULL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'Employee');
