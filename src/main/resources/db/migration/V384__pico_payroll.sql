-- V384: PICO (Spokes) Module 3 -- Overtime -> Payroll, salary self-service. Requires V382, V383.
--
-- ERPNext source (apps/spokes_app/spokes_app):
--   overrides/salary.py        Salary Slip before_save: sum OverTime Entry.ot_amount of the period
--                              into an "Overtime" earning
--   api/pico_api.py            get_salary_slips, download_salary_slip_pdf, get_employee_salary_profile,
--                              get_employee_salary_structure, get_employee_salary_components
--   salary_payment_processor.py  RazorpayX payouts -- NOT migrated (see module-03-payroll/README.md)
--
-- What Aria already had, and what this changes:
--   PayrollCalcService computed one employee / month on request, but nothing ran payroll for a
--   month, nothing filled m7_attendance_summary, nothing wrote m7_payroll_run / m7_payroll_line,
--   and M7.PayrollRun's DRAFT / APPROVED steps were assigned to the M10 role "Dispatch".
--
-- Delivers:
--   m7_payroll_run     + lop / payable days, deductions total, approval / payment stamps, pay_reference
--   m7_payroll_line    + tenant_id
--   m7_attendance_summary + computed_at
--   workflow           M7.PayrollRun v2 (same states / pages, roles fixed)
--   projection         M7.PayrollRun -> m7_payroll_run
--   actions            pico.payroll-run, pico.payroll-decide (HR), pico.my-salary (employee)
--   Java               PicoAttendanceSummaryBuilder, PayrollRunValidator, PayrollStatusListener,
--                      PayrollCalcService / PayslipController / PayrollController (tenant-scoped,
--                      role-checked, OT from m7_overtime_entry when payroll.ot_source=OVERTIME_ENTRY)
--   page               pico:payroll-period-list
--
-- Tenant settings read by the Java (m0_tenant_setting; unset = default):
--   payroll.ot_source       ATTENDANCE_SUMMARY (default, Aria's 2x-basic rule on summary ot_hours)
--                           | OVERTIME_ENTRY (PICO: sum of m7_overtime_entry.ot_amount, like ERPNext)
--   payroll.absent_is_lop   true (default: a working day without attendance or leave is LOP) | false
--
-- Idempotent -- safe to (re)run on any database.

-- ============================================================
-- 1. COLUMNS
-- ============================================================

ALTER TABLE m7_payroll_run
  ADD COLUMN IF NOT EXISTS payable_days DECIMAL(5,2) DEFAULT NULL COMMENT 'From m7_attendance_summary at compute time; NULL = not computed by pico.payroll-run',
  ADD COLUMN IF NOT EXISTS lop_days DECIMAL(5,2) DEFAULT NULL COMMENT 'From m7_attendance_summary at compute time; NULL = not computed by pico.payroll-run',
  ADD COLUMN IF NOT EXISTS total_deduct DECIMAL(12,2) DEFAULT NULL COMMENT 'Employee-side deductions; NULL = not computed by pico.payroll-run',
  ADD COLUMN IF NOT EXISTS computed_at DATETIME DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS computed_by BIGINT DEFAULT NULL COMMENT 'employee_details.id; NULL = not computed by pico.payroll-run',
  ADD COLUMN IF NOT EXISTS approved_at DATETIME DEFAULT NULL COMMENT 'Stamped by the projection on APPROVED; NULL = not approved',
  ADD COLUMN IF NOT EXISTS paid_at DATETIME DEFAULT NULL COMMENT 'Stamped by the projection on PAID; NULL = not paid',
  ADD COLUMN IF NOT EXISTS pay_reference VARCHAR(64) DEFAULT NULL COMMENT 'Bank UTR / batch reference entered when marking PAID; NULL = not paid',
  ADD COLUMN IF NOT EXISTS erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Salary Slip name for migrated rows; NULL = computed in Aria';

CREATE INDEX IF NOT EXISTS idx_pr_wf ON m7_payroll_run (tenant_id, workflow_instance_id);
CREATE INDEX IF NOT EXISTS idx_pr_period_status ON m7_payroll_run (tenant_id, payroll_period, status);

ALTER TABLE m7_payroll_line
  ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1 AFTER id;

ALTER TABLE m7_attendance_summary
  ADD COLUMN IF NOT EXISTS computed_at DATETIME DEFAULT NULL COMMENT 'Last rebuild by PicoAttendanceSummaryBuilder; NULL = entered by hand';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT * FROM (
  SELECT -1 a,'PayrollRun' b,'payable_days' c,'Payable Days' d,'DECIMAL' e,0 f,1 g,0 h,0 i,'number' j,NULL k,NULL l UNION ALL
  SELECT -1,'PayrollRun','lop_days','LOP Days','DECIMAL',0,1,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'PayrollRun','total_deduct','Total Deductions','DECIMAL',0,1,0,0,'currency',NULL,NULL UNION ALL
  SELECT -1,'PayrollRun','computed_at','Computed At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'PayrollRun','approved_at','Approved At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'PayrollRun','paid_at','Paid At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'PayrollRun','pay_reference','Payment Reference','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'PayrollRun','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- Salary data is not for every logged-in user: the generic API was readable by all ('*').
UPDATE entity_registry SET read_role = 'Manager,TenantAdmin'
WHERE code IN ('PayrollRun','PayrollLine','Payslip','EmployeeSalary','AttendanceSummary')
  AND (read_role IS NULL OR read_role = '*');

-- ============================================================
-- 2. WORKFLOW -- M7.PayrollRun v2
-- ============================================================
-- Same states, pages and transitions as v1, so every open v1 instance auto-adopts v2. Only the
-- roles change: v1 gave DRAFT and APPROVED to "Dispatch" (an M10 gate role). PayrollRunValidator
-- enforces who may fire each transition.

INSERT INTO workflow_definition (tenant_id, code, display_name, version, status, body_json)
SELECT -1, 'M7.PayrollRun', 'Payroll', 2, 'PUBLISHED',
  '{"code":"M7.PayrollRun","version":2,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"TenantAdmin"},{"code":"COMPUTED","assignedRole":"Manager"},{"code":"APPROVED","assignedRole":"TenantAdmin"},{"code":"PAID","terminal":true}],"transitions":[{"from":"DRAFT","to":"COMPUTED","trigger":"page.submitted","pageCode":"aria:pr-compute"},{"from":"COMPUTED","to":"APPROVED","trigger":"page.submitted","pageCode":"aria:pr-approve"},{"from":"APPROVED","to":"PAID","trigger":"page.submitted","pageCode":"aria:pr-disburse"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M7.PayrollRun' AND version = 2);

-- ============================================================
-- 3. PROJECTION -- M7.PayrollRun -> m7_payroll_run
-- ============================================================

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, terminal_states, terminal_ts_column, state_ts_columns_json, field_map_json)
SELECT -1, 'M7.PayrollRun', 'm7_payroll_run', 'workflow_instance_id', 'status,current_step',
       'PAID', 'paid_at', '{"APPROVED": "approved_at"}', '{"pay_reference": ["pay_reference", "utr"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M7.PayrollRun' AND target_table = 'm7_payroll_run');

-- ============================================================
-- 4. ACTIONS
-- ============================================================
-- requires_role is documentation only: ActionDispatcher does not enforce it, the handlers do.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT * FROM (
  SELECT 'pico.payroll-run' a, 'Run Payroll' b, 'hr' c,
         '{"type":"object","properties":{"period":{"type":"string","pattern":"^[0-9]{4}-[0-9]{2}$"},"employee_ids":{"type":"array","items":{"type":"integer"}},"allow_open_period":{"type":"boolean"}},"required":["period"],"note":"Read from args."}' d,
         1 e, 'Manager,TenantAdmin' f,
         'For every active employee with a salary card (or employee_ids): rebuilds m7_attendance_summary from attendance / leave / holidays, computes PayrollCalcService, upserts m7_payroll_run + m7_payroll_line + m7_payslip, and moves the M7.PayrollRun instance to COMPUTED. Rows already APPROVED / PAID are skipped.' g
  UNION ALL
  SELECT 'pico.payroll-decide', 'Approve / Pay Payroll', 'hr',
         '{"type":"object","properties":{"period":{"type":"string","pattern":"^[0-9]{4}-[0-9]{2}$"},"decision":{"enum":["APPROVE","PAID"]},"employee_ids":{"type":"array","items":{"type":"integer"}},"pay_reference":{"type":"string"}},"required":["period","decision"],"note":"Read from args."}',
         1, 'Manager,TenantAdmin',
         'Bulk M7.PayrollRun transitions for a period: APPROVE moves COMPUTED rows to APPROVED (aria:pr-approve), PAID moves APPROVED rows to PAID (aria:pr-disburse) with pay_reference.'
  UNION ALL
  SELECT 'pico.my-salary', 'My Salary', 'hr',
         '{"type":"object","properties":{"view":{"enum":["slips","slip","slip-pdf","profile","structure","components"]},"year":{"type":"integer"},"period":{"type":"string","pattern":"^[0-9]{4}-[0-9]{2}$"}},"note":"Read from args. Always scoped to the caller; only APPROVED / PAID payslips are visible."}',
         1, NULL,
         'Self-service salary reads for the mobile app (replaces get_salary_slips, download_salary_slip_pdf, get_employee_salary_profile, get_employee_salary_structure, get_employee_salary_components).'
) x
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = x.a);

-- ============================================================
-- 5. PAGE -- payroll status per period
-- ============================================================

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'pico:payroll-period-list', 'Payroll by Employee', 'Computed / approved / paid payroll per employee and month.', 'HR', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'pico:payroll-period-list');

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED',
  '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"PayrollRun","joins":[{"entity":"EmployeeMaster","as":"e","on":"m7_payroll_run.employee_id = e.id"}],"select":["m7_payroll_run.id as id","m7_payroll_run.payroll_period as payroll_period","e.emp_code as emp_code","e.full_name as full_name","m7_payroll_run.payable_days as payable_days","m7_payroll_run.lop_days as lop_days","m7_payroll_run.gross as gross","m7_payroll_run.ot_amount as ot_amount","m7_payroll_run.total_deduct as total_deduct","m7_payroll_run.net_pay as net_pay","m7_payroll_run.status as status","m7_payroll_run.pay_reference as pay_reference","m7_payroll_run.computed_at as computed_at"]},"columns":[{"field":"payroll_period","label":"Period","render":"badge","sticky":"left","sortable":true},{"field":"emp_code","label":"Emp Code"},{"field":"full_name","label":"Employee"},{"field":"payable_days","label":"Payable","render":"number","align":"right"},{"field":"lop_days","label":"LOP","render":"number","align":"right"},{"field":"gross","label":"Gross","render":"number","align":"right"},{"field":"ot_amount","label":"OT","render":"number","align":"right"},{"field":"total_deduct","label":"Deductions","render":"number","align":"right"},{"field":"net_pay","label":"Net Pay","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge","colorMap":{"DRAFT":"amber","COMPUTED":"blue","APPROVED":"purple","PAID":"green"}},{"field":"pay_reference","label":"Pay Ref"},{"field":"computed_at","label":"Computed","render":"datetime"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["DRAFT","COMPUTED","APPROVED","PAID"]}}],"sort":{"default":[{"field":"payroll_period","dir":"desc"}]},"pagination":{"kind":"server","pageSize":100},"header":{"title":"Payroll by Employee"}}',
  NOW()
FROM page_definition pd
WHERE pd.tenant_id = -1 AND pd.code = 'pico:payroll-period-list'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id AND pv.version = 1);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'pico:payroll-period-list' AND pd.current_version_id IS NULL;
