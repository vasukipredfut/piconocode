-- V382: PICO (Spokes) Module 1 -- Attendance & Check-in, migrated from ERPNext / HRMS.
--
-- ERPNext source (apps/spokes_app/spokes_app):
--   api/pico_api.py   employee_checkin, employee_checkout, get_employee_checkins,
--                     get_employee_last_punch, today_attendance, attendance_history,
--                     holiday_calendar, get_employee_shift
--   overrides/custom_attendance.py   23:59 cron: checkins -> Attendance (+ OverTime Entry)
--   DocTypes          HR Attendance Settings, HR Setting Custom, OverTime Entry
--   HRMS DocTypes     Employee Checkin, Shift Type, Shift Assignment, Holiday List / Holiday
--
-- Delivers:
--   m7_employee       + user_id (login link) + holiday_list_id
--   entities          HolidayList, HolidayDate, ShiftType, ShiftAssignment, EmployeeCheckin, OvertimeEntry
--   actions           pico.employee-punch (PicoEmployeePunchActionHandler)
--                     pico.my-attendance  (PicoMyAttendanceActionHandler)
--   pages             pico:holiday-list, pico:holiday-date-list, pico:shift-type-list,
--                     pico:shift-assignment-list, pico:checkin-list, pico:overtime-list
--   scheduled job     PicoAttendanceProcessor (no SQL here)
--   Attendance itself reuses m7_attendance / entity Attendance / page aria:attendance-list.
--
-- ERPNext "HR Attendance Settings" (per company + shift) is folded into ShiftType: one Aria tenant
-- is one company, so the OT / half-day rules simply live on the shift they apply to.
--
-- No workflow: none of these records have approval states (ERPNext auto-submits them).
-- Idempotent -- safe to (re)run on any database.

-- ============================================================
-- 1. EMPLOYEE MASTER -- link to login account + holiday list
-- ============================================================

ALTER TABLE m7_employee
  ADD COLUMN IF NOT EXISTS user_id BIGINT DEFAULT NULL
    COMMENT 'employee_details.id of this employee''s login (CurrentUser.id()); NULL = employee has no app login',
  ADD COLUMN IF NOT EXISTS holiday_list_id BIGINT DEFAULT NULL
    COMMENT 'm7_holiday_list.id; NULL = no holiday list, every day is a working day';

CREATE UNIQUE INDEX IF NOT EXISTS uq_emp_user ON m7_employee (tenant_id, user_id);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT * FROM (
  SELECT -1 a,'EmployeeMaster' b,'user_id' c,'App Login' d,'RELATION' e,1 f,0 g,0 h,0 i,'entity-ref' j,'Employee' k,'{"entity":"Employee","valueField":"id","labelField":"employeename"}' l UNION ALL
  SELECT -1,'EmployeeMaster','holiday_list_id','Holiday List','RELATION',1,0,0,0,'entity-ref','HolidayList','{"entity":"HolidayList","valueField":"id","labelField":"list_name"}'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- ============================================================
-- 2. TABLES
-- ============================================================

CREATE TABLE IF NOT EXISTS m7_holiday_list (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  list_name VARCHAR(128) NOT NULL,
  from_date DATE NOT NULL,
  to_date DATE NOT NULL,
  weekly_off VARCHAR(16) DEFAULT NULL COMMENT 'Day treated as a holiday every week; NULL = no weekly off',
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Holiday List name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m7hl_name (tenant_id, list_name),
  CONSTRAINT chk_m7hl_dates CHECK (to_date >= from_date),
  CONSTRAINT chk_m7hl_weekly_off CHECK (weekly_off IS NULL OR weekly_off IN
    ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS m7_holiday (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  holiday_list_id BIGINT NOT NULL,
  holiday_date DATE NOT NULL,
  description VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m7h_list_date (tenant_id, holiday_list_id, holiday_date),
  KEY idx_m7h_list (holiday_list_id),
  CONSTRAINT fk_m7h_list FOREIGN KEY (holiday_list_id) REFERENCES m7_holiday_list (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- shift_code is VARCHAR(8) because m7_attendance.shift_code is VARCHAR(8).
CREATE TABLE IF NOT EXISTS m7_shift_type (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  shift_code VARCHAR(8) NOT NULL,
  shift_name VARCHAR(64) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  full_day_before_time TIME DEFAULT NULL COMMENT 'First IN after this time => HALF_DAY; NULL = never half day',
  ot_start_time TIME DEFAULT NULL COMMENT 'Last OUT after this time counts as OT; NULL = no OT on this shift',
  min_ot_minutes INT NOT NULL DEFAULT 0,
  ot_multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.00,
  holiday_ot_multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.00,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m7st_code (tenant_id, shift_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- Overrides m7_employee.shift_code (the default shift) from start_date onwards.
CREATE TABLE IF NOT EXISTS m7_shift_assignment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  employee_id BIGINT NOT NULL,
  shift_code VARCHAR(8) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE DEFAULT NULL COMMENT 'NULL = open-ended',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_m7sa_emp (tenant_id, employee_id, start_date),
  CONSTRAINT fk_m7sa_emp FOREIGN KEY (employee_id) REFERENCES m7_employee (id) ON DELETE CASCADE,
  CONSTRAINT chk_m7sa_status CHECK (status IN ('ACTIVE','INACTIVE')),
  CONSTRAINT chk_m7sa_dates CHECK (end_date IS NULL OR end_date >= start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- One row per punch. Written by pico.employee-punch (mobile), bulk import (biometric / ERPNext history)
-- or HR (manual correction). Read by PicoAttendanceProcessor.
CREATE TABLE IF NOT EXISTS m7_employee_checkin (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  employee_id BIGINT NOT NULL,
  log_type VARCHAR(8) NOT NULL,
  punch_time DATETIME NOT NULL,
  latitude DECIMAL(10,7) DEFAULT NULL COMMENT 'NULL = device sent no location',
  longitude DECIMAL(10,7) DEFAULT NULL COMMENT 'NULL = device sent no location',
  punch_device VARCHAR(64) DEFAULT NULL,
  image_url VARCHAR(512) DEFAULT NULL COMMENT 'Selfie uploaded via /api/m0/document-vault/upload; NULL = none',
  source VARCHAR(16) NOT NULL DEFAULT 'MOBILE',
  punched_by_user_id BIGINT DEFAULT NULL COMMENT 'employee_details.id that recorded the punch; NULL = import',
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Employee Checkin name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_m7ec_emp_time (tenant_id, employee_id, punch_time),
  KEY idx_m7ec_time (tenant_id, punch_time),
  CONSTRAINT fk_m7ec_emp FOREIGN KEY (employee_id) REFERENCES m7_employee (id) ON DELETE CASCADE,
  CONSTRAINT chk_m7ec_log_type CHECK (log_type IN ('IN','OUT')),
  CONSTRAINT chk_m7ec_source CHECK (source IN ('MOBILE','BIOMETRIC','MANUAL','RFID','IMPORT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- Written by PicoAttendanceProcessor; paid by payroll (Module 3 sets PAID + payslip_id).
CREATE TABLE IF NOT EXISTS m7_overtime_entry (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  employee_id BIGINT NOT NULL,
  attendance_date DATE NOT NULL,
  shift_code VARCHAR(8) DEFAULT NULL COMMENT 'NULL = employee had no shift that day',
  ot_hours DECIMAL(5,2) NOT NULL,
  ot_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  is_holiday TINYINT(1) NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  payslip_id BIGINT DEFAULT NULL COMMENT 'm7_payslip.id that paid this OT; NULL = not paid yet',
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext OverTime Entry name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m7ot_emp_date (tenant_id, employee_id, attendance_date),
  KEY idx_m7ot_status (tenant_id, status),
  CONSTRAINT fk_m7ot_emp FOREIGN KEY (employee_id) REFERENCES m7_employee (id) ON DELETE CASCADE,
  CONSTRAINT chk_m7ot_status CHECK (status IN ('OPEN','PAID','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- ============================================================
-- 3. ENTITY REGISTRY
-- ============================================================
-- Checkins and OT are payroll inputs: only HR reads them through the generic API. Employees read
-- their own data through pico.my-attendance, which resolves the employee from the JWT.

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, read_role, write_role, value_column, display_column, lifecycle_kind)
SELECT * FROM (
  SELECT -1 a,'HolidayList' b,'Holiday List' c,'m7_holiday_list' d,'Named holiday calendar assigned to employees (ERPNext Holiday List).' e,'HR' f,'*' g,'Manager,TenantAdmin' h,'id' i,'list_name' j,'DEFINED' k UNION ALL
  SELECT -1,'HolidayDate','Holiday','m7_holiday','One holiday date inside a holiday list.','HR','*','Manager,TenantAdmin',NULL,NULL,'DEFINED' UNION ALL
  SELECT -1,'ShiftType','Shift Type','m7_shift_type','Shift timings + half-day / OT rules (ERPNext Shift Type + HR Attendance Settings).','HR','*','Manager,TenantAdmin','shift_code','shift_name','DEFINED' UNION ALL
  SELECT -1,'ShiftAssignment','Shift Assignment','m7_shift_assignment','Dated shift override for an employee (ERPNext Shift Assignment).','HR','Manager,TenantAdmin','Manager,TenantAdmin',NULL,NULL,'DEFINED' UNION ALL
  SELECT -1,'EmployeeCheckin','Employee Checkin','m7_employee_checkin','Punch IN / OUT log from mobile, biometric or manual entry.','HR','Manager,TenantAdmin','Manager,TenantAdmin',NULL,NULL,'DEFINED' UNION ALL
  SELECT -1,'OvertimeEntry','Overtime Entry','m7_overtime_entry','Daily OT hours + amount computed from checkins; paid by payroll.','HR','Manager,TenantAdmin','Manager,TenantAdmin',NULL,NULL,'DEFINED'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = x.b);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT * FROM (
  -- HolidayList
  SELECT -1 a,'HolidayList' b,'list_name' c,'Name' d,'STRING' e,1 f,1 g,1 h,1 i,'text' j,NULL k,NULL l UNION ALL
  SELECT -1,'HolidayList','from_date','From','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'HolidayList','to_date','To','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'HolidayList','weekly_off','Weekly Off','STRING',1,1,0,0,'select',NULL,'["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"]' UNION ALL
  SELECT -1,'HolidayList','is_active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'HolidayList','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- HolidayDate
  SELECT -1,'HolidayDate','holiday_list_id','Holiday List','RELATION',1,1,0,1,'entity-ref','HolidayList','{"entity":"HolidayList","valueField":"id","labelField":"list_name"}' UNION ALL
  SELECT -1,'HolidayDate','holiday_date','Date','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'HolidayDate','description','Description','STRING',0,1,1,1,'text',NULL,NULL UNION ALL
  -- ShiftType
  SELECT -1,'ShiftType','shift_code','Code','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'ShiftType','shift_name','Name','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'ShiftType','start_time','Start','TIME',0,1,0,1,'text',NULL,NULL UNION ALL
  SELECT -1,'ShiftType','end_time','End','TIME',0,1,0,1,'text',NULL,NULL UNION ALL
  SELECT -1,'ShiftType','full_day_before_time','Full Day If IN Before','TIME',0,0,0,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ShiftType','ot_start_time','OT Starts At','TIME',0,0,0,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ShiftType','min_ot_minutes','Min OT (min)','INT',0,0,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'ShiftType','ot_multiplier','OT Multiplier','DECIMAL',0,0,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'ShiftType','holiday_ot_multiplier','Holiday OT Multiplier','DECIMAL',0,0,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'ShiftType','is_active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  -- ShiftAssignment
  SELECT -1,'ShiftAssignment','employee_id','Employee','RELATION',1,1,0,1,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'ShiftAssignment','shift_code','Shift','RELATION',1,1,0,1,'entity-ref','ShiftType','{"entity":"ShiftType","valueField":"shift_code","labelField":"shift_name"}' UNION ALL
  SELECT -1,'ShiftAssignment','start_date','From','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'ShiftAssignment','end_date','To','DATE',1,1,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'ShiftAssignment','status','Status','STRING',1,1,0,1,'select',NULL,'["ACTIVE","INACTIVE"]' UNION ALL
  -- EmployeeCheckin
  SELECT -1,'EmployeeCheckin','employee_id','Employee','RELATION',1,1,0,1,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'EmployeeCheckin','log_type','IN / OUT','STRING',1,1,0,1,'select',NULL,'["IN","OUT"]' UNION ALL
  SELECT -1,'EmployeeCheckin','punch_time','Time','DATETIME',1,1,0,1,'datetime',NULL,NULL UNION ALL
  SELECT -1,'EmployeeCheckin','latitude','Latitude','DECIMAL',0,0,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'EmployeeCheckin','longitude','Longitude','DECIMAL',0,0,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'EmployeeCheckin','punch_device','Device','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'EmployeeCheckin','image_url','Photo','STRING',0,0,0,0,'text',NULL,NULL UNION ALL
  SELECT -1,'EmployeeCheckin','source','Source','STRING',1,1,0,1,'select',NULL,'["MOBILE","BIOMETRIC","MANUAL","RFID","IMPORT"]' UNION ALL
  SELECT -1,'EmployeeCheckin','punched_by_user_id','Recorded By','RELATION',1,0,0,0,'entity-ref','Employee','{"entity":"Employee","valueField":"id","labelField":"employeename"}' UNION ALL
  SELECT -1,'EmployeeCheckin','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- OvertimeEntry
  SELECT -1,'OvertimeEntry','employee_id','Employee','RELATION',1,1,0,1,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'OvertimeEntry','attendance_date','Date','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'OvertimeEntry','shift_code','Shift','STRING',1,1,0,0,'text',NULL,NULL UNION ALL
  SELECT -1,'OvertimeEntry','ot_hours','OT Hours','DECIMAL',0,1,0,1,'number',NULL,NULL UNION ALL
  SELECT -1,'OvertimeEntry','ot_amount','OT Amount','DECIMAL',0,1,0,0,'currency',NULL,NULL UNION ALL
  SELECT -1,'OvertimeEntry','is_holiday','Holiday','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'OvertimeEntry','status','Status','STRING',1,1,0,1,'select',NULL,'["OPEN","PAID","CANCELLED"]' UNION ALL
  SELECT -1,'OvertimeEntry','payslip_id','Payslip','LONG',1,0,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'OvertimeEntry','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- ============================================================
-- 4. ACTIONS (the mobile app's write + self-read entry points)
-- ============================================================

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT * FROM (
  SELECT 'pico.employee-punch' a, 'Punch IN / OUT' b, 'hr' c,
         '{"type":"object","properties":{"log_type":{"enum":["IN","OUT"]},"latitude":{"type":"number"},"longitude":{"type":"number"},"punch_device":{"type":"string"},"image_url":{"type":"string"}},"required":["log_type"],"note":"Read from args. The employee comes from the JWT (m7_employee.user_id) and the time is the server clock; neither is accepted from the client."}' d,
         1 e, NULL f,
         'Mobile punch (replaces pico_api.employee_checkin / employee_checkout). Returns ok=false, data.reason=LEAVE_APPROVED when an approved leave covers today (ERPNext answered 409). Returns the inserted checkin + today''s last IN / OUT.' g
  UNION ALL
  SELECT 'pico.my-attendance', 'My Attendance', 'hr',
         '{"type":"object","properties":{"view":{"enum":["today","history","holidays","checkins","last-punch"]},"year":{"type":"integer"},"month":{"type":"integer"},"from_date":{"type":"string"},"to_date":{"type":"string"}},"note":"Read from args. Always scoped to the caller''s own employee."}',
         1, NULL,
         'Self-service attendance reads for the mobile app (replaces today_attendance, attendance_history, holiday_calendar, get_employee_checkins, get_employee_last_punch, get_employee_shift).'
) x
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = x.a);

-- ============================================================
-- 5. PAGES (HR admin screens; list pages get +New / edit from the registry)
-- ============================================================

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT * FROM (
  SELECT -1 a,'pico:holiday-list' b,'Holiday Lists' c,'Holiday calendars assigned to employees.' d,'HR' e,'PUBLISHED' f,NOW() g,NOW() h UNION ALL
  SELECT -1,'pico:holiday-date-list','Holidays','Holiday dates per holiday list.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:shift-type-list','Shift Types','Shift timings with half-day and OT rules.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:shift-assignment-list','Shift Assignments','Dated shift overrides per employee.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:checkin-list','Employee Checkins','Punch IN / OUT log.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:overtime-list','Overtime','Daily OT computed from checkins.','HR','PUBLISHED',NOW(),NOW()
) x
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = x.b);

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', b.body, NOW()
FROM page_definition pd
JOIN (
  SELECT 'pico:holiday-list' code,
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"HolidayList","select":["id","list_name","from_date","to_date","weekly_off","is_active"]},"columns":[{"field":"list_name","label":"Name","sticky":"left","sortable":true},{"field":"from_date","label":"From","render":"date"},{"field":"to_date","label":"To","render":"date"},{"field":"weekly_off","label":"Weekly Off","render":"badge"},{"field":"is_active","label":"Active","render":"badge"}],"sort":{"default":[{"field":"from_date","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Holiday Lists"}}' body
  UNION ALL
  SELECT 'pico:holiday-date-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"HolidayDate","joins":[{"entity":"HolidayList","as":"hl","on":"m7_holiday.holiday_list_id = hl.id"}],"select":["m7_holiday.id as id","m7_holiday.holiday_list_id as holiday_list_id","hl.list_name as list_name","m7_holiday.holiday_date as holiday_date","m7_holiday.description as description"]},"columns":[{"field":"holiday_date","label":"Date","render":"date","sticky":"left","sortable":true},{"field":"description","label":"Holiday"},{"field":"list_name","label":"Holiday List","render":"badge"}],"sort":{"default":[{"field":"holiday_date","dir":"asc"}]},"pagination":{"kind":"server","pageSize":100},"header":{"title":"Holidays"}}'
  UNION ALL
  SELECT 'pico:shift-type-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ShiftType","select":["id","shift_code","shift_name","start_time","end_time","full_day_before_time","ot_start_time","min_ot_minutes","ot_multiplier","holiday_ot_multiplier","is_active"]},"columns":[{"field":"shift_code","label":"Code","render":"badge","sticky":"left"},{"field":"shift_name","label":"Name"},{"field":"start_time","label":"Start"},{"field":"end_time","label":"End"},{"field":"full_day_before_time","label":"Full Day Before"},{"field":"ot_start_time","label":"OT From"},{"field":"min_ot_minutes","label":"Min OT","render":"number","align":"right"},{"field":"ot_multiplier","label":"OT x","render":"number","align":"right"},{"field":"holiday_ot_multiplier","label":"Holiday OT x","render":"number","align":"right"},{"field":"is_active","label":"Active","render":"badge"}],"sort":{"default":[{"field":"shift_code","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Shift Types"}}'
  UNION ALL
  SELECT 'pico:shift-assignment-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ShiftAssignment","joins":[{"entity":"EmployeeMaster","as":"e","on":"m7_shift_assignment.employee_id = e.id"}],"select":["m7_shift_assignment.id as id","m7_shift_assignment.employee_id as employee_id","e.emp_code as emp_code","e.full_name as full_name","m7_shift_assignment.shift_code as shift_code","m7_shift_assignment.start_date as start_date","m7_shift_assignment.end_date as end_date","m7_shift_assignment.status as status"]},"columns":[{"field":"emp_code","label":"Emp Code","sticky":"left"},{"field":"full_name","label":"Employee"},{"field":"shift_code","label":"Shift","render":"badge"},{"field":"start_date","label":"From","render":"date","sortable":true},{"field":"end_date","label":"To","render":"date"},{"field":"status","label":"Status","render":"badge","colorMap":{"ACTIVE":"green","INACTIVE":"red"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["ACTIVE","INACTIVE"]}}],"sort":{"default":[{"field":"start_date","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Shift Assignments"}}'
  UNION ALL
  SELECT 'pico:checkin-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"EmployeeCheckin","joins":[{"entity":"EmployeeMaster","as":"e","on":"m7_employee_checkin.employee_id = e.id"}],"select":["m7_employee_checkin.id as id","m7_employee_checkin.employee_id as employee_id","e.emp_code as emp_code","e.full_name as full_name","m7_employee_checkin.log_type as log_type","m7_employee_checkin.punch_time as punch_time","m7_employee_checkin.source as source","m7_employee_checkin.punch_device as punch_device","m7_employee_checkin.latitude as latitude","m7_employee_checkin.longitude as longitude","m7_employee_checkin.image_url as image_url"]},"columns":[{"field":"punch_time","label":"Time","render":"datetime","sticky":"left","sortable":true},{"field":"emp_code","label":"Emp Code"},{"field":"full_name","label":"Employee"},{"field":"log_type","label":"Type","render":"badge","colorMap":{"IN":"green","OUT":"blue"}},{"field":"source","label":"Source","render":"badge"},{"field":"punch_device","label":"Device"},{"field":"latitude","label":"Lat","render":"number","align":"right"},{"field":"longitude","label":"Long","render":"number","align":"right"}],"filters":[{"field":"log_type","label":"Type","type":"multi-select","optionsSource":{"type":"static","options":["IN","OUT"]}},{"field":"source","label":"Source","type":"multi-select","optionsSource":{"type":"static","options":["MOBILE","BIOMETRIC","MANUAL","RFID","IMPORT"]}}],"sort":{"default":[{"field":"punch_time","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Employee Checkins"}}'
  UNION ALL
  SELECT 'pico:overtime-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"OvertimeEntry","joins":[{"entity":"EmployeeMaster","as":"e","on":"m7_overtime_entry.employee_id = e.id"}],"select":["m7_overtime_entry.id as id","m7_overtime_entry.employee_id as employee_id","e.emp_code as emp_code","e.full_name as full_name","m7_overtime_entry.attendance_date as attendance_date","m7_overtime_entry.shift_code as shift_code","m7_overtime_entry.ot_hours as ot_hours","m7_overtime_entry.ot_amount as ot_amount","m7_overtime_entry.is_holiday as is_holiday","m7_overtime_entry.status as status"]},"columns":[{"field":"attendance_date","label":"Date","render":"date","sticky":"left","sortable":true},{"field":"emp_code","label":"Emp Code"},{"field":"full_name","label":"Employee"},{"field":"shift_code","label":"Shift","render":"badge"},{"field":"ot_hours","label":"OT Hrs","render":"number","align":"right"},{"field":"ot_amount","label":"OT Amount","render":"number","align":"right"},{"field":"is_holiday","label":"Holiday","render":"badge"},{"field":"status","label":"Status","render":"badge","colorMap":{"OPEN":"amber","PAID":"green","CANCELLED":"red"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["OPEN","PAID","CANCELLED"]}}],"sort":{"default":[{"field":"attendance_date","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Overtime"}}'
) b ON b.code = pd.code
WHERE pd.tenant_id = -1
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id AND pv.version = 1);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1
  AND pd.code IN ('pico:holiday-list','pico:holiday-date-list','pico:shift-type-list',
                  'pico:shift-assignment-list','pico:checkin-list','pico:overtime-list')
  AND pd.current_version_id IS NULL;
