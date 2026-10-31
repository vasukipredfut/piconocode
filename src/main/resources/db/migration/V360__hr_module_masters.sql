-- HR Module: masters pinned in the PICO Automation ERPNext workspace
-- (Shift Type, Leave Type, Holiday List, Leave Allocation) plus an
-- Employee Checkin log table, so the "Employee Checkin Report" /
-- "Leave Application Report" pages have something real to read.
-- Additive only: existing HR pages/entities are untouched.

-- ===================== ShiftType =====================
CREATE TABLE IF NOT EXISTS m7_shift_type (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_shift_type_tenant_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'ShiftType', 'Shift Type', 'm7_shift_type', 'Named work shift with start/end time.', 'HR', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'ShiftType');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'ShiftType',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk
FROM (SELECT 1 ord,'code' fc,'Code' lbl,'STRING' dt,1 filt,1 sort,1 srch,1 req,'text' wk
      UNION ALL SELECT 2,'name','Name','STRING',1,1,1,1,'text'
      UNION ALL SELECT 3,'start_time','Start Time','TIME',0,1,0,1,'time'
      UNION ALL SELECT 4,'end_time','End Time','TIME',0,1,0,1,'time'
      UNION ALL SELECT 5,'active','Active','BOOLEAN',1,1,0,0,'checkbox') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ShiftType' AND field_code=c.fc);

-- ===================== LeaveType =====================
CREATE TABLE IF NOT EXISTS m7_leave_type (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  is_lwp TINYINT(1) NOT NULL DEFAULT 0,
  max_leaves_allowed DECIMAL(6,2) DEFAULT NULL,
  is_carry_forward TINYINT(1) NOT NULL DEFAULT 0,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_leave_type_tenant_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'LeaveType', 'Leave Type', 'm7_leave_type', 'Configurable leave type (casual, sick, earned, LWP...).', 'HR', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'LeaveType');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'LeaveType',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk
FROM (SELECT 1 ord,'code' fc,'Code' lbl,'STRING' dt,1 filt,1 sort,1 srch,1 req,'text' wk
      UNION ALL SELECT 2,'name','Name','STRING',1,1,1,1,'text'
      UNION ALL SELECT 3,'is_lwp','Is Leave Without Pay','BOOLEAN',1,1,0,0,'checkbox'
      UNION ALL SELECT 4,'max_leaves_allowed','Max Leaves Allowed','DECIMAL',0,1,0,0,'number'
      UNION ALL SELECT 5,'is_carry_forward','Is Carry Forward','BOOLEAN',1,1,0,0,'checkbox'
      UNION ALL SELECT 6,'active','Active','BOOLEAN',1,1,0,0,'checkbox') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LeaveType' AND field_code=c.fc);

-- ===================== HolidayList (+ child dates) =====================
CREATE TABLE IF NOT EXISTS m7_holiday_list (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  from_date DATE NOT NULL,
  to_date DATE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_holiday_list_tenant_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS m7_holiday_list_date (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  holiday_list_id BIGINT NOT NULL,
  holiday_date DATE NOT NULL,
  description VARCHAR(128) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_holiday_list_date_parent (holiday_list_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'HolidayList', 'Holiday List', 'm7_holiday_list', 'Named holiday calendar for a date range.', 'HR', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'HolidayList');

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'HolidayListDate', 'Holiday', 'm7_holiday_list_date', 'A single holiday date within a Holiday List.', 'HR', 'CHILD'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'HolidayListDate');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'HolidayList',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk
FROM (SELECT 1 ord,'code' fc,'Code' lbl,'STRING' dt,1 filt,1 sort,1 srch,1 req,'text' wk
      UNION ALL SELECT 2,'name','Holiday List Name','STRING',1,1,1,1,'text'
      UNION ALL SELECT 3,'from_date','From Date','DATE',0,1,0,1,'date'
      UNION ALL SELECT 4,'to_date','To Date','DATE',0,1,0,1,'date') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='HolidayList' AND field_code=c.fc);

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'HolidayListDate',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk,c.rel
FROM (SELECT 1 ord,'holiday_list_id' fc,'Holiday List' lbl,'LONG' dt,1 filt,1 sort,0 srch,1 req,'entity-ref' wk,'HolidayList' rel
      UNION ALL SELECT 2,'holiday_date','Date','DATE',0,1,0,1,'date',NULL
      UNION ALL SELECT 3,'description','Description','STRING',0,0,1,0,'text',NULL) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='HolidayListDate' AND field_code=c.fc);

-- ===================== LeaveAllocation =====================
CREATE TABLE IF NOT EXISTS m7_leave_allocation (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  employee_id BIGINT NOT NULL,
  leave_type_id BIGINT NOT NULL,
  from_date DATE NOT NULL,
  to_date DATE NOT NULL,
  new_leaves_allocated DECIMAL(6,2) NOT NULL DEFAULT 0,
  total_leaves_allocated DECIMAL(6,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_leave_allocation_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'LeaveAllocation', 'Leave Allocation', 'm7_leave_allocation', 'Leave days allocated to an employee for a period.', 'HR', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'LeaveAllocation');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'LeaveAllocation',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk,c.rel
FROM (SELECT 1 ord,'employee_id' fc,'Employee' lbl,'LONG' dt,1 filt,1 sort,0 srch,1 req,'entity-ref' wk,'Employee' rel
      UNION ALL SELECT 2,'leave_type_id','Leave Type','LONG',1,1,0,1,'entity-ref','LeaveType'
      UNION ALL SELECT 3,'from_date','From Date','DATE',0,1,0,1,'date',NULL
      UNION ALL SELECT 4,'to_date','To Date','DATE',0,1,0,1,'date',NULL
      UNION ALL SELECT 5,'new_leaves_allocated','New Leaves Allocated','DECIMAL',0,1,0,0,'number',NULL
      UNION ALL SELECT 6,'total_leaves_allocated','Total Leaves Allocated','DECIMAL',0,1,0,0,'number',NULL) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LeaveAllocation' AND field_code=c.fc);

-- ===================== EmployeeCheckin (powers "Employee Checkin Report") =====================
CREATE TABLE IF NOT EXISTS m7_employee_checkin (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  employee_id BIGINT NOT NULL,
  log_type VARCHAR(8) NOT NULL,
  time DATETIME NOT NULL,
  shift_type_id BIGINT DEFAULT NULL,
  device_id VARCHAR(64) DEFAULT NULL,
  latitude DECIMAL(10,6) DEFAULT NULL,
  longitude DECIMAL(10,6) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_employee_checkin_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'EmployeeCheckin', 'Employee Checkin', 'm7_employee_checkin', 'Individual in/out punch log, with optional geolocation.', 'HR', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'EmployeeCheckin');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'EmployeeCheckin',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk,c.rel
FROM (SELECT 1 ord,'employee_id' fc,'Employee' lbl,'LONG' dt,1 filt,1 sort,0 srch,1 req,'entity-ref' wk,'Employee' rel
      UNION ALL SELECT 2,'log_type','Log Type','STRING',1,1,0,1,'select',NULL
      UNION ALL SELECT 3,'time','Time','DATETIME',0,1,0,1,'datetime',NULL
      UNION ALL SELECT 4,'shift_type_id','Shift','LONG',1,1,0,0,'entity-ref','ShiftType'
      UNION ALL SELECT 5,'device_id','Device ID','STRING',0,0,1,0,'text',NULL
      UNION ALL SELECT 6,'latitude','Latitude','DECIMAL',0,0,0,0,'number',NULL
      UNION ALL SELECT 7,'longitude','Longitude','DECIMAL',0,0,0,0,'number',NULL) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='EmployeeCheckin' AND field_code=c.fc);

UPDATE entity_field_registry SET options_json = '["IN","OUT"]' WHERE entity_code='EmployeeCheckin' AND field_code='log_type' AND options_json IS NULL;

-- ===================== HR Module pages =====================
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:hrm-employee-list', 'Employees', 'Employee master (HR Module).', 'HR Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:hrm-employee-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "Employee", "select": ["employeename", "status", "dept_id", "desi_id", "gender", "joiningdate", "phoneno", "email"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Employee", "type": "text-contains", "applyTo": ["employeename", "email", "phoneno"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-employee-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-employee-list' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:hrm-shift-type-list', 'Shift Type', 'Shift timing master (HR Module).', 'HR Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:hrm-shift-type-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "ShiftType", "select": ["code", "name", "start_time", "end_time", "active"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Shift", "type": "text-contains", "applyTo": ["code", "name"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-shift-type-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-shift-type-list' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:hrm-leave-type-list', 'Leave Type', 'Leave type master (HR Module).', 'HR Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:hrm-leave-type-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "LeaveType", "select": ["code", "name", "is_lwp", "max_leaves_allowed", "is_carry_forward", "active"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Leave Type", "type": "text-contains", "applyTo": ["code", "name"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-leave-type-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-leave-type-list' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:hrm-holiday-list-list', 'Holiday List', 'Holiday calendars (HR Module).', 'HR Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:hrm-holiday-list-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "HolidayList", "select": ["code", "name", "from_date", "to_date"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Holiday List", "type": "text-contains", "applyTo": ["code", "name"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-holiday-list-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-holiday-list-list' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:hrm-leave-allocation-list', 'Leave Allocation', 'Leave days allocated per employee (HR Module).', 'HR Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:hrm-leave-allocation-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "LeaveAllocation", "select": ["employee_id", "leave_type_id", "from_date", "to_date", "new_leaves_allocated", "total_leaves_allocated"]}, "columns": [], "filters": [{"field": "employee_id", "label": "Employee", "type": "equals", "applyTo": ["employee_id"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-leave-allocation-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-leave-allocation-list' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:hrm-employee-checkin-report', 'Employee Checkin Report', 'Raw in/out punch log (HR Module).', 'HR Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:hrm-employee-checkin-report');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "EmployeeCheckin", "select": ["employee_id", "log_type", "time", "shift_type_id", "device_id"]}, "columns": [], "filters": [{"field": "employee_id", "label": "Employee", "type": "equals", "applyTo": ["employee_id"]}, {"field": "time", "label": "From/To", "type": "date-range", "applyTo": ["time"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-employee-checkin-report' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-employee-checkin-report' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:hrm-leave-application-report', 'Leave Application Report', 'Leave requests across employees (HR Module).', 'HR Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:hrm-leave-application-report');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "LeaveRequest", "select": ["employee_id", "leave_kind", "start_date", "end_date", "days", "status"]}, "columns": [], "filters": [{"field": "employee_id", "label": "Employee", "type": "equals", "applyTo": ["employee_id"]}, {"field": "status", "label": "Status", "type": "equals", "applyTo": ["status"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-leave-application-report' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-leave-application-report' AND pd.current_version_id IS NULL;
