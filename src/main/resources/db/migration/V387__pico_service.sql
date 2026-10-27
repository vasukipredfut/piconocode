-- V387: PICO (Spokes) Module 6 -- Service: Installation Report (customer OTP) + support tickets.
-- Requires V382-V386 (m8_customer from V386).
--
-- ERPNext source (apps/):
--   spokes_app api/pico_api.py   create_installation_report, send_otp_email, send_otp_sms (Fast2SMS),
--                                verify_installation_report_otp, get_installation_report,
--                                get_all_installation_reports, get_employee_installation_reports,
--                                download_installation_report_pdf, get_issues
--   spokes_app DocType           Installation Report (submittable, OTP fields)
--   customer_support             api.create_issue / get_customer_issues + www/support.html (public form)
--
-- What changes vs ERPNext:
--   OTP     6 digits from SecureRandom (was 4, random.randint), stored only as a salted SHA-256
--           (was plaintext in two fields), expires (service.otp_ttl_minutes, default 30), max 5 wrong
--           attempts then a resend is required (was unlimited -> 10 000 guesses brute-forced it).
--   Tickets Aria's m9_service_ticket + M9.ServiceTicket replace ERPNext Issue; subject / description /
--           customer / contact / source columns and a comments table are added.
--   Portal  public create + status check needs the customer's registered mobile (ERPNext accepted any
--           existing customer NAME and listed that customer's issues to anyone).
--
-- Delivers:
--   tables     m9_installation_report (new), m9_ticket_comment (new), m9_service_ticket (+ columns)
--   workflows  M9.InstallationReport  OTP_PENDING -> VERIFIED | CANCELLED
--              M9.ServiceTicket v2    v1 states / pages kept; roles fixed (Service / Manager);
--                                     OPEN -> CLOSED via pico:ticket-close (spam / duplicate)
--   projections both workflows -> their tables
--   actions    pico.installation-create, pico.installation-otp, pico.ticket-create,
--              pico.ticket-comment, pico.my-service
--   Java       ServiceAccessValidator, InstallationOtpService, InstallationReportPdf,
--              ServiceTicketService, PublicSupportController (/api/public/support/{tenantCode}/...)
--   pages      pico:installation-verify, pico:installation-cancel, pico:ticket-close,
--              pico:installation-report-list, pico:service-ticket-list
--
-- Tenant settings: service.otp_ttl_minutes (30), service.otp_sms_template_id (MSG91 DLT template;
-- unset = email only), service.notify_emp_id (m7_employee.id mailed for portal tickets).
--
-- Idempotent -- safe to (re)run on any database.

-- ============================================================
-- 1. TABLES
-- ============================================================

CREATE TABLE IF NOT EXISTS m9_installation_report (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  report_number VARCHAR(32) NOT NULL,
  doc_no VARCHAR(64) DEFAULT NULL COMMENT 'Paper / customer document number typed by the engineer',
  report_date DATE NOT NULL,
  engineer_emp_id BIGINT NOT NULL COMMENT 'm7_employee.id of the service engineer',
  customer_id BIGINT NOT NULL COMMENT 'm8_customer.id',
  ticket_id BIGINT DEFAULT NULL COMMENT 'm9_service_ticket.id this visit served; NULL = not ticket-driven',
  address VARCHAR(512) DEFAULT NULL,
  contact_person_name VARCHAR(128) DEFAULT NULL,
  contact_number VARCHAR(32) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  warranty_kind VARCHAR(8) DEFAULT NULL COMMENT 'UW under warranty | AMC | OW out of warranty | O other (ERPNext uw/amc/ow/o checkboxes); NULL = not stated',
  call_receipt_date DATE DEFAULT NULL,
  nature_of_call VARCHAR(140) DEFAULT NULL,
  call_date_from DATE DEFAULT NULL,
  call_date_to DATE DEFAULT NULL,
  machine_type VARCHAR(140) DEFAULT NULL,
  machine_model VARCHAR(140) DEFAULT NULL,
  primary_communication TEXT DEFAULT NULL COMMENT 'ERPNext brief_of_primary_communication_with_or_by_customer',
  pre_work_observation TEXT DEFAULT NULL,
  action_summary TEXT DEFAULT NULL,
  remarks_by_customer TEXT DEFAULT NULL,
  work_done_nature TINYINT(1) NOT NULL DEFAULT 0,
  system_operation TINYINT(1) NOT NULL DEFAULT 0,
  suggestions_for_trouble TINYINT(1) NOT NULL DEFAULT 0,
  purpose_reason TINYINT(1) NOT NULL DEFAULT 0,
  otp_hash CHAR(64) DEFAULT NULL COMMENT 'SHA-256(salt:otp:id), hex; NULL = no OTP issued. The OTP itself is never stored',
  otp_salt CHAR(32) DEFAULT NULL,
  otp_sent_at DATETIME DEFAULT NULL,
  otp_expires_at DATETIME DEFAULT NULL,
  otp_attempts INT NOT NULL DEFAULT 0 COMMENT 'Wrong attempts against the current OTP',
  otp_send_count INT NOT NULL DEFAULT 0,
  otp_sent_to VARCHAR(256) DEFAULT NULL COMMENT 'Masked destinations of the last send',
  otp_verified_at DATETIME DEFAULT NULL COMMENT 'Set by pico.installation-otp just before the VERIFIED transition; NULL = not verified',
  status VARCHAR(16) NOT NULL DEFAULT 'OTP_PENDING',
  current_step VARCHAR(64) DEFAULT NULL,
  workflow_instance_id BIGINT DEFAULT NULL,
  cancel_reason VARCHAR(512) DEFAULT NULL,
  closed_at DATETIME DEFAULT NULL COMMENT 'VERIFIED / CANCELLED time; NULL = open',
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Installation Report name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m9ir_number (tenant_id, report_number),
  KEY idx_m9ir_engineer (tenant_id, engineer_emp_id, status),
  KEY idx_m9ir_customer (tenant_id, customer_id),
  KEY idx_m9ir_wf (tenant_id, workflow_instance_id),
  CONSTRAINT fk_m9ir_customer FOREIGN KEY (customer_id) REFERENCES m8_customer (id),
  CONSTRAINT fk_m9ir_engineer FOREIGN KEY (engineer_emp_id) REFERENCES m7_employee (id),
  CONSTRAINT chk_m9ir_status CHECK (status IN ('OTP_PENDING','VERIFIED','CANCELLED')),
  CONSTRAINT chk_m9ir_warranty CHECK (warranty_kind IS NULL OR warranty_kind IN ('UW','AMC','OW','O'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

ALTER TABLE m9_service_ticket
  ADD COLUMN IF NOT EXISTS subject VARCHAR(256) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS description TEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS customer_id BIGINT DEFAULT NULL COMMENT 'm8_customer.id; NULL = legacy row with customer_name only',
  ADD COLUMN IF NOT EXISTS contact_name VARCHAR(128) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS contact_mobile VARCHAR(32) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS contact_email VARCHAR(128) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS source VARCHAR(16) NOT NULL DEFAULT 'APP' COMMENT 'PORTAL | APP | PHONE | EMAIL | IMPORT',
  ADD COLUMN IF NOT EXISTS created_by_emp_id BIGINT DEFAULT NULL COMMENT 'm7_employee.id; NULL = customer portal / import',
  ADD COLUMN IF NOT EXISTS erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Issue name for migrated rows; NULL = created in Aria',
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_st_customer ON m9_service_ticket (tenant_id, customer_id);
CREATE INDEX IF NOT EXISTS idx_st_tech ON m9_service_ticket (tenant_id, technician_id, status);
CREATE INDEX IF NOT EXISTS idx_st_wf ON m9_service_ticket (tenant_id, workflow_instance_id);

CREATE TABLE IF NOT EXISTS m9_ticket_comment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  ticket_id BIGINT NOT NULL,
  author_emp_id BIGINT DEFAULT NULL COMMENT 'm7_employee.id; NULL = the customer (portal)',
  author_name VARCHAR(128) NOT NULL,
  comment TEXT NOT NULL,
  is_internal TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 = staff-only, never shown on the portal',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_m9tc_ticket (tenant_id, ticket_id, created_at),
  CONSTRAINT fk_m9tc_ticket FOREIGN KEY (ticket_id) REFERENCES m9_service_ticket (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- ============================================================
-- 2. ENTITY REGISTRY
-- ============================================================
-- Installation reports carry customer contact data and OTP hashes: HR/service management only
-- through the generic API, never writable there (the OTP columns must only change via the action).

UPDATE entity_registry SET read_role = 'Service,Manager,TenantAdmin'
WHERE code = 'ServiceTicket' AND (read_role IS NULL OR read_role = '*');

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, read_role, write_role, value_column, display_column, lifecycle_kind, workflow_code)
SELECT * FROM (
  SELECT -1 a,'InstallationReport' b,'Installation Report' c,'m9_installation_report' d,'Service visit report verified by a customer OTP (ERPNext Installation Report).' e,'M9' f,'Manager,TenantAdmin' g,'SuperAdmin' h,'id' i,'report_number' j,'WORKFLOW' k,'M9.InstallationReport' l UNION ALL
  SELECT -1,'TicketComment','Ticket Comment','m9_ticket_comment','Comment / activity on a service ticket.','M9','Service,Manager,TenantAdmin','SuperAdmin',NULL,NULL,'DEFINED',NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = x.b);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT * FROM (
  -- InstallationReport (OTP hash / salt deliberately not registered)
  SELECT -1 a,'InstallationReport' b,'report_number' c,'Report #' d,'STRING' e,1 f,1 g,1 h,1 i,'text' j,NULL k,NULL l UNION ALL
  SELECT -1,'InstallationReport','doc_no','Doc No','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','report_date','Date','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','engineer_emp_id','Engineer','RELATION',1,1,0,1,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'InstallationReport','customer_id','Customer','RELATION',1,1,0,1,'entity-ref','Customer','{"entity":"Customer","valueField":"id","labelField":"customer_name"}' UNION ALL
  SELECT -1,'InstallationReport','ticket_id','Ticket','RELATION',1,0,0,0,'entity-ref','ServiceTicket','{"entity":"ServiceTicket","valueField":"id","labelField":"ticket_no"}' UNION ALL
  SELECT -1,'InstallationReport','address','Address','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','contact_person_name','Contact Person','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','contact_number','Contact Number','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','email','Email','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','warranty_kind','Warranty','STRING',1,1,0,0,'select',NULL,'["UW","AMC","OW","O"]' UNION ALL
  SELECT -1,'InstallationReport','call_receipt_date','Call Received','DATE',1,1,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','nature_of_call','Nature of Call','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','call_date_from','Visit From','DATE',1,1,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','call_date_to','Visit To','DATE',1,1,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','machine_type','Machine Type','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','machine_model','Machine Model','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','primary_communication','Primary Communication','TEXT',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','pre_work_observation','Pre-work Observation','TEXT',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','action_summary','Action Summary','TEXT',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','remarks_by_customer','Customer Remarks','TEXT',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','work_done_nature','Work Done Nature','BOOLEAN',0,0,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','system_operation','System Operation','BOOLEAN',0,0,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','suggestions_for_trouble','Suggestions for Trouble','BOOLEAN',0,0,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','purpose_reason','Purpose / Reason','BOOLEAN',0,0,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','otp_sent_at','OTP Sent','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','otp_sent_to','OTP Sent To','STRING',0,0,0,0,'text',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','otp_verified_at','Customer Verified','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','status','Status','STRING',1,1,0,0,'select',NULL,'["OTP_PENDING","VERIFIED","CANCELLED"]' UNION ALL
  SELECT -1,'InstallationReport','cancel_reason','Cancel Reason','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','closed_at','Closed At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'InstallationReport','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- ServiceTicket (existing + new columns)
  SELECT -1,'ServiceTicket','ticket_no','Ticket #','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','subject','Subject','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','description','Description','TEXT',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','customer_id','Customer','RELATION',1,1,0,0,'entity-ref','Customer','{"entity":"Customer","valueField":"id","labelField":"customer_name"}' UNION ALL
  SELECT -1,'ServiceTicket','customer_name','Customer Name','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','contact_name','Contact','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','contact_mobile','Contact Mobile','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','contact_email','Contact Email','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','issue_kind','Issue Type','STRING',1,1,0,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','priority','Priority','STRING',1,1,0,0,'select',NULL,'["P1","P2","P3","P4"]' UNION ALL
  SELECT -1,'ServiceTicket','technician_id','Technician','RELATION',1,1,0,0,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'ServiceTicket','scheduled_for','Scheduled For','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','status','Status','STRING',1,1,0,0,'select',NULL,'["OPEN","SCHEDULED","IN_PROGRESS","AWAITING_CUSTOMER","RESOLVED","CLOSED"]' UNION ALL
  SELECT -1,'ServiceTicket','source','Source','STRING',1,1,0,0,'select',NULL,'["PORTAL","APP","PHONE","EMAIL","IMPORT"]' UNION ALL
  SELECT -1,'ServiceTicket','resolution','Resolution','TEXT',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','closed_at','Closed At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'ServiceTicket','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- TicketComment
  SELECT -1,'TicketComment','ticket_id','Ticket','RELATION',1,1,0,1,'entity-ref','ServiceTicket','{"entity":"ServiceTicket","valueField":"id","labelField":"ticket_no"}' UNION ALL
  SELECT -1,'TicketComment','author_name','By','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'TicketComment','comment','Comment','TEXT',0,0,1,1,'textarea',NULL,NULL UNION ALL
  SELECT -1,'TicketComment','is_internal','Internal','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- ============================================================
-- 3. WORKFLOWS
-- ============================================================
-- VERIFIED has no guard here on purpose: guards cannot read the DB, so the OTP check lives in
-- pico.installation-otp, and ServiceAccessValidator refuses VERIFIED unless that action stamped
-- otp_verified_at first -- a direct POST /api/workflow/transition cannot skip the OTP.

INSERT INTO workflow_definition (tenant_id, code, display_name, version, status, body_json)
SELECT -1, 'M9.InstallationReport', 'Installation Report', 1, 'PUBLISHED',
  '{"code":"M9.InstallationReport","version":1,"initialState":"OTP_PENDING","states":[{"code":"OTP_PENDING","assignedRole":"Service"},{"code":"VERIFIED","terminal":true},{"code":"CANCELLED","terminal":true}],"transitions":[{"from":"OTP_PENDING","to":"VERIFIED","trigger":"page.submitted","pageCode":"pico:installation-verify"},{"from":"OTP_PENDING","to":"CANCELLED","trigger":"page.submitted","pageCode":"pico:installation-cancel"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M9.InstallationReport' AND version = 1);

INSERT INTO workflow_definition (tenant_id, code, display_name, version, status, body_json)
SELECT -1, 'M9.ServiceTicket', 'Service Ticket', 2, 'PUBLISHED',
  '{"code":"M9.ServiceTicket","version":2,"initialState":"OPEN","states":[{"code":"OPEN","assignedRole":"Manager"},{"code":"SCHEDULED","assignedRole":"Service"},{"code":"IN_PROGRESS","assignedRole":"Service"},{"code":"AWAITING_CUSTOMER","assignedRole":"Service"},{"code":"RESOLVED","assignedRole":"Manager"},{"code":"CLOSED","terminal":true}],"transitions":[{"from":"OPEN","to":"SCHEDULED","trigger":"page.submitted","pageCode":"aria:st-schedule"},{"from":"SCHEDULED","to":"IN_PROGRESS","trigger":"page.submitted","pageCode":"aria:st-start"},{"from":"IN_PROGRESS","to":"AWAITING_CUSTOMER","trigger":"page.submitted","pageCode":"aria:st-customer"},{"from":"AWAITING_CUSTOMER","to":"IN_PROGRESS","trigger":"page.submitted","pageCode":"aria:st-customer"},{"from":"IN_PROGRESS","to":"RESOLVED","trigger":"page.submitted","pageCode":"aria:st-resolve"},{"from":"RESOLVED","to":"CLOSED","trigger":"page.submitted","pageCode":"aria:st-close"},{"from":"OPEN","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:ticket-close"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M9.ServiceTicket' AND version = 2);

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, terminal_states, terminal_ts_column, field_map_json)
SELECT -1, 'M9.InstallationReport', 'm9_installation_report', 'workflow_instance_id', 'status,current_step', 'VERIFIED,CANCELLED', 'closed_at',
       '{"cancel_reason": ["cancel_reason", "reason"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M9.InstallationReport' AND target_table = 'm9_installation_report');

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, terminal_states, terminal_ts_column, field_map_json)
SELECT -1, 'M9.ServiceTicket', 'm9_service_ticket', 'workflow_instance_id', 'status,current_step', 'CLOSED', 'closed_at',
       '{"resolution": ["resolution"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M9.ServiceTicket' AND target_table = 'm9_service_ticket');

-- ============================================================
-- 4. ACTIONS
-- ============================================================

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT * FROM (
  SELECT 'pico.installation-create' a, 'Create Installation Report' b, 'service' c,
         '{"type":"object","properties":{"customer_id":{"type":"integer"},"customer_name":{"type":"string"},"ticket_id":{"type":"integer"},"doc_no":{"type":"string"},"date":{"type":"string","format":"date"},"address":{"type":"string"},"contact_person_name":{"type":"string"},"contact_number":{"type":"string"},"email":{"type":"string"},"warranty_kind":{"enum":["UW","AMC","OW","O"]},"call_receipt_date":{"type":"string"},"nature_of_call":{"type":"string"},"call_date_from":{"type":"string"},"call_date_to":{"type":"string"},"machine_type":{"type":"string"},"machine_model":{"type":"string"},"primary_communication":{"type":"string"},"pre_work_observation":{"type":"string"},"action_summary":{"type":"string"},"remarks_by_customer":{"type":"string"}},"note":"Read from args. Engineer from the JWT. customer_id, or an exact customer_name. ERPNext uw/amc/ow/o flags are accepted and mapped to warranty_kind."}' d,
         1 e, NULL f,
         'Creates m9_installation_report (OTP_PENDING), starts M9.InstallationReport and sends a one-time code to the customer email / mobile (replaces create_installation_report, send_otp_email, send_otp_sms).' g
  UNION ALL
  SELECT 'pico.installation-otp', 'Verify / Resend Installation OTP', 'service',
         '{"type":"object","properties":{"report_id":{"type":"integer"},"op":{"enum":["verify","resend"]},"otp":{"type":"string"}},"required":["report_id","op"],"note":"Read from args. The report''s engineer or a Manager."}',
         1, NULL,
         'verify: checks the code (expiry, 5 attempts), stamps otp_verified_at and moves the report to VERIFIED; resend: issues a new code (replaces verify_installation_report_otp).'
  UNION ALL
  SELECT 'pico.ticket-create', 'Create Service Ticket', 'service',
         '{"type":"object","properties":{"customer_id":{"type":"integer"},"subject":{"type":"string"},"description":{"type":"string"},"issue_kind":{"type":"string"},"priority":{"enum":["P1","P2","P3","P4"]},"contact_name":{"type":"string"},"contact_mobile":{"type":"string"},"contact_email":{"type":"string"},"source":{"enum":["APP","PHONE","EMAIL"]},"site_address":{"type":"string"}},"required":["customer_id","subject"],"note":"Read from args. Staff-entered ticket; customers use the public portal endpoint."}',
         1, NULL,
         'Creates m9_service_ticket (OPEN) and starts M9.ServiceTicket (replaces ERPNext Issue creation from the desk).'
  UNION ALL
  SELECT 'pico.ticket-comment', 'Comment on Ticket', 'service',
         '{"type":"object","properties":{"ticket_id":{"type":"integer"},"comment":{"type":"string"},"is_internal":{"type":"boolean"}},"required":["ticket_id","comment"],"note":"Read from args."}',
         1, NULL,
         'Adds an m9_ticket_comment by the caller (ERPNext Issue comments).'
  UNION ALL
  SELECT 'pico.my-service', 'My Service', 'service',
         '{"type":"object","properties":{"view":{"enum":["reports","report","report-pdf","tickets","ticket"]},"report_id":{"type":"integer"},"ticket_id":{"type":"integer"},"status":{"type":"string"},"customer_id":{"type":"integer"}},"note":"Read from args. Engineers / technicians see their own; Manager / TenantAdmin see all."}',
         1, NULL,
         'Service reads for the mobile app (replaces get_installation_report, get_all_installation_reports, get_employee_installation_reports, download_installation_report_pdf, get_issues).'
) x
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = x.a);

-- ============================================================
-- 5. PAGES
-- ============================================================

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT * FROM (
  SELECT -1 a,'pico:installation-verify' b,'Installation Verified' c,'Submitted by pico.installation-otp after the customer code matched.' d,'M9' e,'PUBLISHED' f,NOW() g,NOW() h UNION ALL
  SELECT -1,'pico:installation-cancel','Cancel Installation Report','Cancel a report that was entered by mistake.','M9','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:ticket-close','Close Ticket','Close an OPEN ticket as spam / duplicate.','M9','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:installation-report-list','Installation Reports','Service visit reports and their customer verification.','M9','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:service-ticket-list','Service Tickets','Customer service tickets.','M9','PUBLISHED',NOW(),NOW()
) x
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = x.b);

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', b.body, NOW()
FROM page_definition pd
JOIN (
  SELECT 'pico:installation-verify' code,
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Installation Verified","fields":[{"kind":"FormField","type":"text","name":"note","label":"Note"}]}]}' body
  UNION ALL
  SELECT 'pico:installation-cancel',
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Cancel Installation Report","fields":[{"kind":"FormField","type":"textarea","name":"cancel_reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Cancel Report","action":"workflow.transition","variant":"danger"}]}]}'
  UNION ALL
  SELECT 'pico:ticket-close',
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close Ticket","fields":[{"kind":"FormField","type":"textarea","name":"resolution","label":"Reason (spam / duplicate / ...)"},{"kind":"WorkflowActionButton","label":"Close","action":"workflow.transition","variant":"danger"}]}]}'
  UNION ALL
  SELECT 'pico:installation-report-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"InstallationReport","joins":[{"entity":"Customer","as":"c","on":"m9_installation_report.customer_id = c.id"},{"entity":"EmployeeMaster","as":"e","on":"m9_installation_report.engineer_emp_id = e.id"}],"select":["m9_installation_report.id as id","m9_installation_report.report_number as report_number","m9_installation_report.report_date as report_date","c.customer_name as customer_name","e.full_name as engineer","m9_installation_report.machine_type as machine_type","m9_installation_report.machine_model as machine_model","m9_installation_report.nature_of_call as nature_of_call","m9_installation_report.warranty_kind as warranty_kind","m9_installation_report.status as status","m9_installation_report.otp_verified_at as otp_verified_at"]},"columns":[{"field":"report_number","label":"Report #","sticky":"left"},{"field":"report_date","label":"Date","render":"date","sortable":true},{"field":"customer_name","label":"Customer"},{"field":"engineer","label":"Engineer"},{"field":"machine_type","label":"Machine"},{"field":"machine_model","label":"Model"},{"field":"nature_of_call","label":"Nature of Call"},{"field":"warranty_kind","label":"Warranty","render":"badge"},{"field":"status","label":"Status","render":"badge","colorMap":{"OTP_PENDING":"amber","VERIFIED":"green","CANCELLED":"red"}},{"field":"otp_verified_at","label":"Verified","render":"datetime"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["OTP_PENDING","VERIFIED","CANCELLED"]}}],"sort":{"default":[{"field":"report_date","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Installation Reports"}}'
  UNION ALL
  SELECT 'pico:service-ticket-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ServiceTicket","joins":[{"entity":"EmployeeMaster","as":"t","on":"m9_service_ticket.technician_id = t.id"}],"select":["m9_service_ticket.id as id","m9_service_ticket.ticket_no as ticket_no","m9_service_ticket.reported_at as reported_at","m9_service_ticket.customer_name as customer_name","m9_service_ticket.subject as subject","m9_service_ticket.priority as priority","m9_service_ticket.source as source","t.full_name as technician","m9_service_ticket.scheduled_for as scheduled_for","m9_service_ticket.status as status","m9_service_ticket.workflow_instance_id as workflow_instance_id"]},"columns":[{"field":"ticket_no","label":"Ticket #","sticky":"left"},{"field":"reported_at","label":"Reported","render":"datetime","sortable":true},{"field":"customer_name","label":"Customer"},{"field":"subject","label":"Subject"},{"field":"priority","label":"Priority","render":"badge","colorMap":{"P1":"red","P2":"amber","P3":"blue","P4":"gray"}},{"field":"source","label":"Source","render":"badge"},{"field":"technician","label":"Technician"},{"field":"scheduled_for","label":"Scheduled","render":"datetime"},{"field":"status","label":"Status","render":"badge"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["OPEN","SCHEDULED","IN_PROGRESS","AWAITING_CUSTOMER","RESOLVED","CLOSED"]}},{"field":"priority","label":"Priority","type":"multi-select","optionsSource":{"type":"static","options":["P1","P2","P3","P4"]}}],"sort":{"default":[{"field":"reported_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Service Tickets"}}'
) b ON b.code = pd.code
WHERE pd.tenant_id = -1
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id AND pv.version = 1);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1
  AND pd.code IN ('pico:installation-verify','pico:installation-cancel','pico:ticket-close',
                  'pico:installation-report-list','pico:service-ticket-list')
  AND pd.current_version_id IS NULL;

INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
SELECT * FROM (
  SELECT -1 a,'pico:installation-verify' b,'M9.InstallationReport' c,'OTP_PENDING' d,'INPUT' e UNION ALL
  SELECT -1,'pico:installation-cancel','M9.InstallationReport','OTP_PENDING','INPUT' UNION ALL
  SELECT -1,'pico:ticket-close','M9.ServiceTicket','OPEN','INPUT'
) x
WHERE NOT EXISTS (SELECT 1 FROM page_workflow_binding WHERE tenant_id = x.a AND page_code = x.b AND workflow_code = x.c AND step_code = x.d);
