-- Payroll Module: pure page aliasing, no new tables/entities.
-- aria's existing payroll engine (EmployeeSalary, PayrollRun, Payslip,
-- SalaryComponent) already covers everything ERPNext's Salary
-- Component/Structure/Structure Assignment/Payroll Entry/Salary Slip do
-- (and more, on the statutory side) -- these pages just surface it under a
-- "Payroll Module" category to match the PICO Automation sidebar grouping.
-- Existing pages (aria:salary-component, aria:pr-list, aria:payslip-list...)
-- are untouched.

-- Salary Component
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:pym-salary-component-list', 'Salary Component', 'Reusable pay components (Payroll Module).', 'Payroll Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:pym-salary-component-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "SalaryComponent", "select": ["code", "name", "component_type", "calc_method", "taxable", "pf_eligible", "esi_eligible"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Component", "type": "text-contains", "applyTo": ["code", "name"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-salary-component-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-salary-component-list' AND pd.current_version_id IS NULL;

-- Salary Structure (+ Structure Assignment, which aria merges into the same
-- effective-dated EmployeeSalary card -- both ERPNext sidebar entries point
-- here; see plan notes on this simplification).
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:pym-salary-structure-list', 'Salary Structure', 'Per-employee effective-dated salary card (Payroll Module). Also covers ERPNext''s separate Salary Structure Assignment -- aria keeps both in one record.', 'Payroll Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:pym-salary-structure-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "EmployeeSalary", "select": ["emp_id", "effective_from", "effective_to", "ctc_annual", "basic_monthly", "hra_monthly"]}, "columns": [], "filters": [{"field": "emp_id", "label": "Employee", "type": "equals", "applyTo": ["emp_id"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-salary-structure-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-salary-structure-list' AND pd.current_version_id IS NULL;

-- Payroll Entry
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:pym-payroll-entry-list', 'Payroll Entry', 'Per-employee payroll run rows (Payroll Module).', 'Payroll Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:pym-payroll-entry-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "PayrollRun", "select": ["payroll_period", "employee_id", "gross", "net_pay", "status"]}, "columns": [], "filters": [{"field": "payroll_period", "label": "Period", "type": "equals", "applyTo": ["payroll_period"]}, {"field": "status", "label": "Status", "type": "equals", "applyTo": ["status"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-payroll-entry-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-payroll-entry-list' AND pd.current_version_id IS NULL;

-- Salary Slip
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:pym-salary-slip-list', 'Salary Slip', 'Generated payslips (Payroll Module).', 'Payroll Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:pym-salary-slip-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "Payslip", "select": ["emp_id", "payroll_period", "gross", "total_deduct", "net_pay", "status", "generated_at"]}, "columns": [], "filters": [{"field": "emp_id", "label": "Employee", "type": "equals", "applyTo": ["emp_id"]}, {"field": "payroll_period", "label": "Period", "type": "equals", "applyTo": ["payroll_period"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-salary-slip-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-salary-slip-list' AND pd.current_version_id IS NULL;
