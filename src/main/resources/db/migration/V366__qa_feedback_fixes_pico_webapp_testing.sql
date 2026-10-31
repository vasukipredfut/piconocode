-- Fixes against the QA doc "picowebapptesting.docx" (testing of an earlier
-- build of these same HR/Payroll/Expense/Sales/Service modules):
--   - Employee had no Shift / Holiday List link fields at all.
--   - Leave approver / Expense approver rendered as a bare number, not a
--     user picker.
--   - Holiday List had no way to flag a day as a weekly off vs a named
--     holiday (on top of the free-text HolidayListDate rows already added
--     in V360, which do let festival/other holidays be entered).
--   - Attendance had no dedicated read-only report page under HR Module
--     (Employee Checkin Report / Leave Application Report already got one
--     in V360 -- Attendance was missed).
--   - "Salary Structure Assignment" was only ever aliased onto the same
--     page as "Salary Structure" (V361); ERPNext's sidebar expects it as
--     its own distinct screen.
--   - There was no "User" screen under HR Module at all. aria has no
--     separate User entity -- login credentials (username/password) live
--     directly on the Employee row (confirmed via
--     com.aria.auth.AuthUserDetailsService reading employee_details) -- so
--     this ships as a read-only Employee-as-user list, not a new entity.

-- ===================== Employee: Shift + Holiday List links =====================
ALTER TABLE employee_details
  ADD COLUMN IF NOT EXISTS shift_type_id BIGINT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS holiday_list_id BIGINT DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'Employee',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk,c.rel
FROM (SELECT 1 ord,'shift_type_id' fc,'Shift' lbl,'LONG' dt,1 filt,1 sort,0 srch,0 req,'entity-ref' wk,'ShiftType' rel
      UNION ALL SELECT 2,'holiday_list_id','Holiday List','LONG',1,1,0,0,'entity-ref','HolidayList') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Employee' AND field_code=c.fc);

-- ===================== Leave approver / Expense approver -> user picker =====================
-- No separate "User" entity exists in aria (login = Employee row, see note
-- above), so "list of users" resolves to the Employee list -- same pattern
-- LeaveRequest.assignee_id/assigned_by already use.
UPDATE entity_field_registry SET widget_kind = 'entity-ref', relation_entity = 'Employee'
WHERE entity_code = 'LeaveRequest' AND field_code = 'approved_by';

UPDATE entity_field_registry SET widget_kind = 'entity-ref', relation_entity = 'Employee'
WHERE entity_code = 'ExpenseClaim' AND field_code = 'approved_by';

-- ===================== Holiday List: weekly_off =====================
ALTER TABLE m7_holiday_list ADD COLUMN IF NOT EXISTS weekly_off VARCHAR(16) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0)+1 FROM entity_field_registry x),
       -1,'HolidayList','weekly_off','Weekly Off','STRING',1,1,0,0,'select'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='HolidayList' AND field_code='weekly_off');

UPDATE entity_field_registry SET options_json = '["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"]'
WHERE entity_code='HolidayList' AND field_code='weekly_off' AND options_json IS NULL;

-- ===================== Update the HR Module list page to surface the new fields =====================
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind": "list", "dataSource": {"entity": "Employee", "select": ["employeename", "status", "dept_id", "desi_id", "shift_type_id", "holiday_list_id", "gender", "joiningdate", "phoneno", "email"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Employee", "type": "text-contains", "applyTo": ["employeename", "email", "phoneno"]}], "pagination": {"pageSize": 50}}'
WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-employee-list';

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind": "list", "dataSource": {"entity": "HolidayList", "select": ["code", "name", "from_date", "to_date", "weekly_off"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Holiday List", "type": "text-contains", "applyTo": ["code", "name"]}], "pagination": {"pageSize": 50}}'
WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-holiday-list-list';

-- ===================== New: Attendance Report (HR Module) =====================
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:hrm-attendance-report', 'Attendance Report', 'Read-only attendance report (HR Module), mirrors ERPNext''s Shift Attendance Report.', 'HR Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:hrm-attendance-report');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "Attendance", "select": ["employee_id", "date_of", "status", "shift_code", "in_time", "out_time", "worked_hours"]}, "columns": [], "filters": [{"field": "employee_id", "label": "Employee", "type": "equals", "applyTo": ["employee_id"]}, {"field": "date_of", "label": "From/To", "type": "date-range", "applyTo": ["date_of"]}, {"field": "status", "label": "Status", "type": "equals", "applyTo": ["status"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-attendance-report' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-attendance-report' AND pd.current_version_id IS NULL;

-- ===================== New: Salary Structure Assignment, as its own screen (Payroll Module) =====================
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:pym-salary-structure-assignment-list', 'Salary Structure Assignment', 'Which salary card is effective for which employee, from when (Payroll Module). Same underlying record as Salary Structure -- aria keeps structure+assignment as one effective-dated card; this view emphasizes the assignment side.', 'Payroll Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:pym-salary-structure-assignment-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "EmployeeSalary", "select": ["emp_id", "effective_from", "effective_to", "ctc_annual"]}, "columns": [], "filters": [{"field": "emp_id", "label": "Employee", "type": "equals", "applyTo": ["emp_id"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-salary-structure-assignment-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:pym-salary-structure-assignment-list' AND pd.current_version_id IS NULL;

-- ===================== New: "User" screen (HR Module) -- read-only, Employee-as-login-identity =====================
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:hrm-user-list', 'User', 'Login-capable employees (HR Module). aria has no separate User entity -- an Employee IS the login identity (username/password live on the Employee row) -- so this lists Employee records with their login/role fields.', 'HR Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:hrm-user-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "Employee", "select": ["employeename", "username", "email", "roles_id", "dept_id", "status"]}, "columns": [], "filters": [{"field": "_q", "label": "Search User", "type": "text-contains", "applyTo": ["employeename", "username", "email"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-user-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-user-list' AND pd.current_version_id IS NULL;
