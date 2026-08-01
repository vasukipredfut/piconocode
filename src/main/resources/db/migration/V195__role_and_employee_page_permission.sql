-- Role/Employee page-level permission tables
--
-- Backs the Employee Access Matrix's switch from entity-wise (entity_registry
-- read_role/write_role/approver_role) to page-wise permissions. Same
-- "missing row means default" convention as entity_registry's blank CSV
-- columns: no row for a (role|employee, page) pair means
-- read=true, write=false, approve=false. These tables therefore only need to
-- store explicit grants an Admin makes through the app — no bulk seeding.
--
-- Resolution order (see com.aria.access.EmployeeAccessMatrixController):
-- employee_page_permission (explicit override for that one employee) wins
-- outright over role_page_permission (the employee's role default), which
-- wins over the hardcoded default above.

CREATE TABLE IF NOT EXISTS role_page_permission (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT -1,
  role_id BIGINT NOT NULL,
  page_code VARCHAR(128) NOT NULL,
  can_read TINYINT(1) NOT NULL DEFAULT 1,
  can_write TINYINT(1) NOT NULL DEFAULT 0,
  can_approve TINYINT(1) NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by BIGINT DEFAULT NULL,
  UNIQUE KEY uk_role_page (tenant_id, role_id, page_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS employee_page_permission (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT -1,
  employee_id BIGINT NOT NULL,
  page_code VARCHAR(128) NOT NULL,
  can_read TINYINT(1) NOT NULL DEFAULT 1,
  can_write TINYINT(1) NOT NULL DEFAULT 0,
  can_approve TINYINT(1) NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by BIGINT DEFAULT NULL,
  UNIQUE KEY uk_employee_page (tenant_id, employee_id, page_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;
