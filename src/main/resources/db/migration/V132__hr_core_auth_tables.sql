-- V132 — Core HR/auth tables: employee_details, role, department_details,
-- permissions, refresh_token.
--
-- These tables are referenced everywhere (AuthController, AuthUserDetailsService,
-- entity_registry rows for Employee/Role/Department/Permissions/RefreshToken)
-- but no migration ever created them — they were assumed to already exist from
-- an external legacy DB dump imported outside of Flyway (see comments in
-- V4_1__m0_workflow_personas.sql / V7_2__pico_role_permissions.sql: "production
-- DB does [have them], testcontainer slim DB does not"). A fresh install with
-- no legacy dump has completely broken login as a result — AuthUserDetailsService
-- queries employee_details/role directly with JdbcTemplate.
--
-- Idempotent — CREATE TABLE IF NOT EXISTS throughout. Once these exist, the
-- already-idempotent V4_1/V7_2/V7_15 probes populate role + persona employee
-- rows and register HR pages automatically on the next boot.

-- ----- department_details ----------------------------------------------
CREATE TABLE IF NOT EXISTS department_details (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    capacity VARCHAR(32) NULL,
    purpose VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- role (global persona/role master, NOT per-tenant — tenant_role is
-- the per-tenant analogue created in V7_3) ------------------------------
CREATE TABLE IF NOT EXISTS role (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    landing_route VARCHAR(128) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_role_name (name)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- employee_details (the login table — AuthUserDetailsService reads
-- id, username, password, roles_id, status directly) --------------------
CREATE TABLE IF NOT EXISTS employee_details (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    employeename VARCHAR(128) NOT NULL,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(128) NULL,
    phoneno VARCHAR(32) NULL,
    roles_id BIGINT NULL,
    dept_id BIGINT NULL,
    status VARCHAR(8) NOT NULL DEFAULT 'A',       -- auth query checks status='' OR status='A'
    employeetype VARCHAR(32) NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    branch_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_employee_username (username),
    KEY idx_emp_roles (roles_id),
    KEY idx_emp_dept (dept_id),
    KEY idx_emp_tenant (tenant_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- permissions (legacy module/sub-module/action-item RBAC grid per
-- role — shape taken from the entity_field_registry rows already seeded
-- for entity_code='Permissions' in V120) --------------------------------
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    role_name VARCHAR(64) NULL,
    module_id BIGINT NULL,
    module_name VARCHAR(64) NULL,
    sub_module_id BIGINT NULL,
    sub_module_name VARCHAR(64) NULL,
    action_items_id BIGINT NULL,
    action_items_name VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_perm_role (role_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ----- refresh_token (AuthController: insert on signin, read+delete on
-- refresh/signout) -------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_token (
    token VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    KEY idx_refresh_user (user_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;
