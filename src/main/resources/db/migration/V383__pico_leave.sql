-- V383: PICO (Spokes) Module 2 -- Leave, migrated from ERPNext / HRMS. Requires V382.
--
-- ERPNext source (apps/spokes_app/spokes_app):
--   api/pico_api.py   apply_leave, approve_reject_leave, cancel_leave_application(s),
--                     delete_leave_application, whos_on_leave, get_employee_leave_history,
--                     get_leave_balance, get_leave_types, get_pending_leaves_by_employee,
--                     get_pending_leaves_for_approver
--   overrides/leave_application.py   on_update / on_update_after_submit: approval mails,
--                     cancellation of an approved leave, "revised half day" leave
--   HRMS DocTypes     Leave Application, Leave Type, Leave Allocation
--
-- Delivers:
--   m7_employee       + leave_approver_id
--   m7_leave_request  + half-day, approver, remarks, cancellation-request columns
--   entities          LeaveType, LeaveAllocation; LeaveRequest fields registered
--   workflows         M7.LeaveRequest v2   PENDING -> APPROVED | REJECTED | WITHDRAWN
--                     M7.LeaveCancellation REQUESTED -> CANCELLED | DECLINED (on an APPROVED leave)
--   projections       both workflows -> m7_leave_request (status / cancel_status columns)
--   actions           pico.leave-apply, pico.leave-cancel, pico.my-leave
--   Java              LeaveApproverValidator (who may decide), LeaveDecisionListener (mails,
--                     cancellation effects), PicoMailer (SMTP_EMAIL integration, after commit)
--   pages             pico:leave-withdraw, pico:leave-cancel-decide, pico:leave-type-list,
--                     pico:leave-allocation-list, pico:leave-cancel-queue
--
-- Why a second workflow for cancellation instead of APPROVED -> CANCELLED inside M7.LeaveRequest:
-- APPROVED stays terminal, so an approved leave does not sit in an open instance owned by nobody,
-- and v2 stays compatible with v1 (every v1 state still exists, so open v1 instances auto-adopt v2).
-- v1 instances that are already CLOSED at APPROVED can still be cancelled: cancellation is its own
-- instance and only needs the m7_leave_request row.
--
-- Idempotent -- safe to (re)run on any database.

-- ============================================================
-- 1. EMPLOYEE MASTER -- leave approver
-- ============================================================

ALTER TABLE m7_employee
  ADD COLUMN IF NOT EXISTS leave_approver_id BIGINT DEFAULT NULL
    COMMENT 'm7_employee.id who approves this employee''s leave; NULL = use reports_to_emp_id';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT -1, 'EmployeeMaster', 'leave_approver_id', 'Leave Approver', 'RELATION', 1, 0, 0, 0, 'entity-ref', 'EmployeeMaster',
       '{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'EmployeeMaster' AND field_code = 'leave_approver_id');

-- ============================================================
-- 2. LEAVE REQUEST -- new columns
-- ============================================================
-- status / current_step are written by the M7.LeaveRequest projection (section 6);
-- cancel_status by the M7.LeaveCancellation projection.

ALTER TABLE m7_leave_request
  ADD COLUMN IF NOT EXISTS is_half_day TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS which_half VARCHAR(16) DEFAULT NULL
    COMMENT 'FIRST_HALF | SECOND_HALF when is_half_day=1; NULL = full day(s)',
  ADD COLUMN IF NOT EXISTS approver_emp_id BIGINT DEFAULT NULL
    COMMENT 'm7_employee.id resolved at apply time; NULL = legacy row, any Manager may decide',
  ADD COLUMN IF NOT EXISTS approver_remarks VARCHAR(512) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS decided_at DATETIME DEFAULT NULL
    COMMENT 'When the leave reached APPROVED / REJECTED / WITHDRAWN; NULL = still pending',
  ADD COLUMN IF NOT EXISTS cancel_instance_id BIGINT DEFAULT NULL
    COMMENT 'workflow_instance.id of the latest M7.LeaveCancellation; NULL = never requested',
  ADD COLUMN IF NOT EXISTS cancel_status VARCHAR(16) DEFAULT NULL
    COMMENT 'REQUESTED | CANCELLED | DECLINED; NULL = no cancellation requested',
  ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(512) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS cancel_requested_at DATETIME DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS cancel_decided_at DATETIME DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS revised_half_day TINYINT(1) NOT NULL DEFAULT 0
    COMMENT '1 = employee asked to keep a half day instead of cancelling fully',
  ADD COLUMN IF NOT EXISTS revised_which_half VARCHAR(16) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS revised_half_day_date DATE DEFAULT NULL
    COMMENT 'Date of the half day to keep; NULL = start_date',
  ADD COLUMN IF NOT EXISTS revised_from_id BIGINT DEFAULT NULL
    COMMENT 'm7_leave_request.id this half-day leave replaced on cancellation; NULL = applied normally',
  ADD COLUMN IF NOT EXISTS erp_ref VARCHAR(140) DEFAULT NULL
    COMMENT 'ERPNext Leave Application name for migrated rows; NULL = created in Aria',
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_lr_emp_dates ON m7_leave_request (tenant_id, employee_id, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_lr_approver ON m7_leave_request (tenant_id, approver_emp_id, status);
CREATE INDEX IF NOT EXISTS idx_lr_wf ON m7_leave_request (tenant_id, workflow_instance_id);
CREATE INDEX IF NOT EXISTS idx_lr_cancel_wf ON m7_leave_request (tenant_id, cancel_instance_id);

-- ============================================================
-- 3. LEAVE TYPE + ALLOCATION
-- ============================================================

-- type_code is what m7_leave_request.leave_kind (VARCHAR(16)) stores.
CREATE TABLE IF NOT EXISTS m7_leave_type (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  type_code VARCHAR(16) NOT NULL,
  type_name VARCHAR(64) NOT NULL,
  max_days_per_year DECIMAL(5,1) DEFAULT NULL COMMENT 'Informational cap shown to employees; NULL = no cap',
  is_lwp TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 = leave without pay: no allocation / balance check',
  is_compensatory TINYINT(1) NOT NULL DEFAULT 0,
  include_holidays TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 = holidays inside the range count as leave days',
  allow_negative TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 = may apply beyond the remaining balance',
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Leave Type name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m7lt_code (tenant_id, type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS m7_leave_allocation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  employee_id BIGINT NOT NULL,
  leave_kind VARCHAR(16) NOT NULL COMMENT 'm7_leave_type.type_code',
  from_date DATE NOT NULL,
  to_date DATE NOT NULL,
  allocated_days DECIMAL(5,1) NOT NULL,
  notes VARCHAR(255) DEFAULT NULL,
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Leave Allocation name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m7la_emp_kind_from (tenant_id, employee_id, leave_kind, from_date),
  KEY idx_m7la_emp (employee_id),
  CONSTRAINT fk_m7la_emp FOREIGN KEY (employee_id) REFERENCES m7_employee (id) ON DELETE CASCADE,
  CONSTRAINT chk_m7la_dates CHECK (to_date >= from_date),
  CONSTRAINT chk_m7la_days CHECK (allocated_days >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- ============================================================
-- 4. ENTITY REGISTRY
-- ============================================================

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, read_role, write_role, value_column, display_column, lifecycle_kind)
SELECT * FROM (
  SELECT -1 a,'LeaveType' b,'Leave Type' c,'m7_leave_type' d,'Leave kinds with balance / holiday rules (ERPNext Leave Type).' e,'HR' f,'*' g,'Manager,TenantAdmin' h,'type_code' i,'type_name' j,'DEFINED' k UNION ALL
  SELECT -1,'LeaveAllocation','Leave Allocation','m7_leave_allocation','Days of a leave kind granted to an employee for a period (ERPNext Leave Allocation).','HR','Manager,TenantAdmin','Manager,TenantAdmin',NULL,NULL,'DEFINED'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = x.b);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT * FROM (
  -- LeaveType
  SELECT -1 a,'LeaveType' b,'type_code' c,'Code' d,'STRING' e,1 f,1 g,1 h,1 i,'text' j,NULL k,NULL l UNION ALL
  SELECT -1,'LeaveType','type_name','Name','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'LeaveType','max_days_per_year','Max Days / Year','DECIMAL',0,1,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'LeaveType','is_lwp','Without Pay','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'LeaveType','is_compensatory','Compensatory','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'LeaveType','include_holidays','Count Holidays','BOOLEAN',0,0,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'LeaveType','allow_negative','Allow Negative Balance','BOOLEAN',0,0,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'LeaveType','is_active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'LeaveType','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- LeaveAllocation
  SELECT -1,'LeaveAllocation','employee_id','Employee','RELATION',1,1,0,1,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'LeaveAllocation','leave_kind','Leave Type','RELATION',1,1,0,1,'entity-ref','LeaveType','{"entity":"LeaveType","valueField":"type_code","labelField":"type_name"}' UNION ALL
  SELECT -1,'LeaveAllocation','from_date','From','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'LeaveAllocation','to_date','To','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'LeaveAllocation','allocated_days','Days','DECIMAL',0,1,0,1,'number',NULL,NULL UNION ALL
  SELECT -1,'LeaveAllocation','notes','Notes','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'LeaveAllocation','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- LeaveRequest (existing entity on m7_leave_request; new + previously unregistered columns)
  SELECT -1,'LeaveRequest','lr_number','Leave #','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','employee_id','Employee','RELATION',1,1,0,1,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'LeaveRequest','leave_kind','Leave Type','RELATION',1,1,0,1,'entity-ref','LeaveType','{"entity":"LeaveType","valueField":"type_code","labelField":"type_name"}' UNION ALL
  SELECT -1,'LeaveRequest','start_date','From','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','end_date','To','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','days','Days','DECIMAL',0,1,0,1,'number',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','reason','Reason','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','status','Status','STRING',1,1,0,0,'select',NULL,'["PENDING","APPROVED","REJECTED","WITHDRAWN","CANCELLED"]' UNION ALL
  SELECT -1,'LeaveRequest','is_half_day','Half Day','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','which_half','Which Half','STRING',1,0,0,0,'select',NULL,'["FIRST_HALF","SECOND_HALF"]' UNION ALL
  SELECT -1,'LeaveRequest','approver_emp_id','Approver','RELATION',1,1,0,0,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'LeaveRequest','approver_remarks','Approver Remarks','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','decided_at','Decided At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','cancel_status','Cancellation','STRING',1,1,0,0,'select',NULL,'["REQUESTED","CANCELLED","DECLINED"]' UNION ALL
  SELECT -1,'LeaveRequest','cancel_reason','Cancellation Reason','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','cancel_requested_at','Cancellation Requested','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','revised_half_day','Keep Half Day','BOOLEAN',1,0,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','revised_which_half','Keep Which Half','STRING',0,0,0,0,'select',NULL,'["FIRST_HALF","SECOND_HALF"]' UNION ALL
  SELECT -1,'LeaveRequest','revised_half_day_date','Keep Half Day On','DATE',0,0,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','revised_from_id','Revised From','LONG',1,0,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'LeaveRequest','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- ============================================================
-- 5. WORKFLOWS
-- ============================================================
-- M7.LeaveRequest v2 keeps v1's states, page (aria:lr-approve) and guard field (status), so the
-- existing web approve page keeps working; it adds WITHDRAWN (employee cancels before a decision,
-- which replaces ERPNext's draft cancel and delete_leave_application).

INSERT INTO workflow_definition (tenant_id, code, display_name, version, status, body_json)
SELECT -1, 'M7.LeaveRequest', 'Leave Request', 2, 'PUBLISHED',
  '{"code":"M7.LeaveRequest","version":2,"initialState":"PENDING","states":[{"code":"PENDING","assignedRole":"Manager"},{"code":"APPROVED","terminal":true},{"code":"REJECTED","terminal":true},{"code":"WITHDRAWN","terminal":true}],"transitions":[{"from":"PENDING","to":"APPROVED","trigger":"page.submitted","pageCode":"aria:lr-approve","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"PENDING","to":"REJECTED","trigger":"page.submitted","pageCode":"aria:lr-approve","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"PENDING","to":"WITHDRAWN","trigger":"page.submitted","pageCode":"pico:leave-withdraw"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M7.LeaveRequest' AND version = 2);

INSERT INTO workflow_definition (tenant_id, code, display_name, version, status, body_json)
SELECT -1, 'M7.LeaveCancellation', 'Leave Cancellation', 1, 'PUBLISHED',
  '{"code":"M7.LeaveCancellation","version":1,"initialState":"REQUESTED","states":[{"code":"REQUESTED","assignedRole":"Manager"},{"code":"CANCELLED","terminal":true},{"code":"DECLINED","terminal":true}],"transitions":[{"from":"REQUESTED","to":"CANCELLED","trigger":"page.submitted","pageCode":"pico:leave-cancel-decide","guard":"#submitted[''decision''] == ''CANCELLED''"},{"from":"REQUESTED","to":"DECLINED","trigger":"page.submitted","pageCode":"pico:leave-cancel-decide","guard":"#submitted[''decision''] == ''DECLINED''"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M7.LeaveCancellation' AND version = 1);

-- ============================================================
-- 6. PROJECTIONS (workflow state -> m7_leave_request)
-- ============================================================
-- Both rows always find the m7_leave_request row: pico.leave-apply / pico.leave-cancel write
-- workflow_instance_id / cancel_instance_id right after starting the instance.

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, terminal_states, terminal_ts_column, field_map_json)
SELECT -1, 'M7.LeaveRequest', 'm7_leave_request', 'workflow_instance_id', 'status,current_step',
       'APPROVED,REJECTED,WITHDRAWN', 'decided_at', '{"approver_remarks": ["remarks", "approver_remarks"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M7.LeaveRequest' AND target_table = 'm7_leave_request');

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, terminal_states, terminal_ts_column, field_map_json)
SELECT -1, 'M7.LeaveCancellation', 'm7_leave_request', 'cancel_instance_id', 'cancel_status',
       'CANCELLED,DECLINED', 'cancel_decided_at', '{"approver_remarks": ["remarks", "approver_remarks"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M7.LeaveCancellation' AND target_table = 'm7_leave_request');

-- ============================================================
-- 7. ACTIONS
-- ============================================================

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT * FROM (
  SELECT 'pico.leave-apply' a, 'Apply Leave' b, 'hr' c,
         '{"type":"object","properties":{"leave_kind":{"type":"string"},"from_date":{"type":"string","format":"date"},"to_date":{"type":"string","format":"date"},"is_half_day":{"type":"boolean"},"which_half":{"enum":["FIRST_HALF","SECOND_HALF"]},"reason":{"type":"string"}},"required":["leave_kind","from_date","to_date"],"note":"Read from args. Employee and approver come from the JWT / employee master."}' d,
         1 e, NULL f,
         'Creates an m7_leave_request (PENDING), starts M7.LeaveRequest and mails the approver. Rejects overlaps, missing approver and insufficient balance (replaces pico_api.apply_leave).' g
  UNION ALL
  SELECT 'pico.leave-cancel', 'Cancel Leave', 'hr',
         '{"type":"object","properties":{"leave_id":{"type":"integer"},"reason":{"type":"string"},"revised_half_day":{"type":"boolean"},"which_half":{"enum":["FIRST_HALF","SECOND_HALF"]},"half_day_date":{"type":"string","format":"date"}},"required":["leave_id"],"note":"Read from args. Only the leave''s own employee may call it."}',
         1, NULL,
         'PENDING leave -> WITHDRAWN. APPROVED leave -> starts M7.LeaveCancellation and mails the approver, optionally asking to keep a half day (replaces cancel_leave_application(s) and delete_leave_application).'
  UNION ALL
  SELECT 'pico.my-leave', 'My Leave', 'hr',
         '{"type":"object","properties":{"view":{"enum":["history","pending","balance","types","approvals","whos-on-leave"]},"status":{"type":"string"},"year":{"type":"integer"}},"note":"Read from args. Always scoped to the caller (employee, or approver for view=approvals)."}',
         1, NULL,
         'Self-service leave reads for the mobile app (replaces get_employee_leave_history, get_pending_leaves_by_employee, get_leave_balance, get_leave_types, get_pending_leaves_for_approver, whos_on_leave).'
) x
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = x.a);

-- ============================================================
-- 8. PAGES
-- ============================================================

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT * FROM (
  SELECT -1 a,'pico:leave-withdraw' b,'Withdraw Leave' c,'Employee withdraws a leave that is still pending.' d,'HR' e,'PUBLISHED' f,NOW() g,NOW() h UNION ALL
  SELECT -1,'pico:leave-cancel-decide','Leave Cancellation Decision','Approver cancels an approved leave or declines the request.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:leave-cancel-queue','Leave Cancellation Requests','Open cancellation requests on approved leave.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:leave-type-list','Leave Types','Leave kinds with balance and holiday rules.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:leave-allocation-list','Leave Allocations','Leave days granted per employee and period.','HR','PUBLISHED',NOW(),NOW()
) x
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = x.b);

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', b.body, NOW()
FROM page_definition pd
JOIN (
  SELECT 'pico:leave-withdraw' code,
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Withdraw Leave","fields":[{"kind":"FormField","type":"textarea","name":"reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Withdraw","action":"workflow.transition","variant":"danger"}]}]}' body
  UNION ALL
  SELECT 'pico:leave-cancel-decide',
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Leave Cancellation Request","fields":[{"kind":"FormField","type":"select","name":"decision","label":"Decision","options":[{"label":"Cancel the leave","value":"CANCELLED"},{"label":"Keep the leave","value":"DECLINED"}]},{"kind":"FormField","type":"textarea","name":"remarks","label":"Remarks"},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}'
  UNION ALL
  SELECT 'pico:leave-cancel-queue',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"LeaveRequest","joins":[{"entity":"EmployeeMaster","as":"e","on":"m7_leave_request.employee_id = e.id"}],"filter":{"and":[{"field":"cancel_status","op":"eq","value":"REQUESTED"}]},"select":["m7_leave_request.id as id","m7_leave_request.lr_number as lr_number","e.emp_code as emp_code","e.full_name as full_name","m7_leave_request.leave_kind as leave_kind","m7_leave_request.start_date as start_date","m7_leave_request.end_date as end_date","m7_leave_request.days as days","m7_leave_request.cancel_reason as cancel_reason","m7_leave_request.revised_half_day as revised_half_day","m7_leave_request.revised_which_half as revised_which_half","m7_leave_request.cancel_requested_at as cancel_requested_at","m7_leave_request.cancel_instance_id as cancel_instance_id"]},"columns":[{"field":"lr_number","label":"Leave #","sticky":"left"},{"field":"emp_code","label":"Emp Code"},{"field":"full_name","label":"Employee"},{"field":"leave_kind","label":"Type","render":"badge"},{"field":"start_date","label":"From","render":"date"},{"field":"end_date","label":"To","render":"date"},{"field":"days","label":"Days","render":"number","align":"right"},{"field":"cancel_reason","label":"Reason"},{"field":"revised_half_day","label":"Keep Half Day","render":"badge"},{"field":"revised_which_half","label":"Half"},{"field":"cancel_requested_at","label":"Requested","render":"datetime","sortable":true}],"sort":{"default":[{"field":"cancel_requested_at","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Leave Cancellation Requests"}}'
  UNION ALL
  SELECT 'pico:leave-type-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"LeaveType","select":["id","type_code","type_name","max_days_per_year","is_lwp","is_compensatory","include_holidays","allow_negative","is_active"]},"columns":[{"field":"type_code","label":"Code","render":"badge","sticky":"left"},{"field":"type_name","label":"Name"},{"field":"max_days_per_year","label":"Max / Year","render":"number","align":"right"},{"field":"is_lwp","label":"LWP","render":"badge"},{"field":"is_compensatory","label":"Comp Off","render":"badge"},{"field":"include_holidays","label":"Count Holidays","render":"badge"},{"field":"allow_negative","label":"Negative OK","render":"badge"},{"field":"is_active","label":"Active","render":"badge"}],"sort":{"default":[{"field":"type_code","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Leave Types"}}'
  UNION ALL
  SELECT 'pico:leave-allocation-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"LeaveAllocation","joins":[{"entity":"EmployeeMaster","as":"e","on":"m7_leave_allocation.employee_id = e.id"}],"select":["m7_leave_allocation.id as id","m7_leave_allocation.employee_id as employee_id","e.emp_code as emp_code","e.full_name as full_name","m7_leave_allocation.leave_kind as leave_kind","m7_leave_allocation.from_date as from_date","m7_leave_allocation.to_date as to_date","m7_leave_allocation.allocated_days as allocated_days","m7_leave_allocation.notes as notes"]},"columns":[{"field":"emp_code","label":"Emp Code","sticky":"left"},{"field":"full_name","label":"Employee"},{"field":"leave_kind","label":"Type","render":"badge"},{"field":"from_date","label":"From","render":"date","sortable":true},{"field":"to_date","label":"To","render":"date"},{"field":"allocated_days","label":"Days","render":"number","align":"right"},{"field":"notes","label":"Notes"}],"sort":{"default":[{"field":"from_date","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Leave Allocations"}}'
) b ON b.code = pd.code
WHERE pd.tenant_id = -1
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id AND pv.version = 1);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1
  AND pd.code IN ('pico:leave-withdraw','pico:leave-cancel-decide','pico:leave-cancel-queue',
                  'pico:leave-type-list','pico:leave-allocation-list')
  AND pd.current_version_id IS NULL;

INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
SELECT * FROM (
  SELECT -1 a,'pico:leave-withdraw' b,'M7.LeaveRequest' c,'PENDING' d,'INPUT' e UNION ALL
  SELECT -1,'pico:leave-cancel-decide','M7.LeaveCancellation','REQUESTED','INPUT'
) x
WHERE NOT EXISTS (SELECT 1 FROM page_workflow_binding WHERE tenant_id = x.a AND page_code = x.b AND workflow_code = x.c AND step_code = x.d);

-- ============================================================
-- 9. EXISTING BUG -- gov-form leave numbers (ExtendedFormsController)
-- ============================================================
-- ExtendedFormsController queries m7_leave_request.emp_id / leave_type, which do not exist
-- (the columns are employee_id / leave_kind), so those numbers are always 0. Fixed in Java, see
-- module-02-leave/README.md; nothing to change in the schema.
