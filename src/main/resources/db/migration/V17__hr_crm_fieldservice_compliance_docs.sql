-- =====================================================================
-- V17 — Factory + ERP completion: HR (Attendance/Leave/Payroll/Statutory)
--       + CRM (Lead/Quote) + Field Service + Compliance Audit Trail
--       + Document Management.
-- =====================================================================

-- ===== HR =============================================================
CREATE TABLE IF NOT EXISTS m7_attendance (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  employee_id BIGINT NOT NULL,
  shift_code VARCHAR(8) NULL,
  date_of DATE NOT NULL,
  in_time TIME NULL,
  out_time TIME NULL,
  worked_hours DECIMAL(5,2) NULL,
  ot_hours DECIMAL(5,2) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PRESENT',
  source VARCHAR(16) NOT NULL DEFAULT 'BIOMETRIC',
  UNIQUE KEY uq_att (tenant_id, employee_id, date_of),
  KEY idx_att_date (date_of, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m7_leave_request (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  lr_number VARCHAR(32) NOT NULL,
  employee_id BIGINT NOT NULL,
  leave_kind VARCHAR(16) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  days DECIMAL(4,1) NOT NULL,
  reason VARCHAR(256) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  approved_by BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_lr_number (tenant_id, lr_number)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m7_payroll_run (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  payroll_period VARCHAR(8) NOT NULL,
  employee_id BIGINT NOT NULL,
  worked_days DECIMAL(4,1) NULL,
  basic DECIMAL(12,2) NOT NULL,
  hra DECIMAL(12,2) NOT NULL DEFAULT 0,
  conveyance DECIMAL(12,2) NOT NULL DEFAULT 0,
  ot_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  gross DECIMAL(12,2) NOT NULL,
  pf_employee DECIMAL(12,2) NOT NULL DEFAULT 0,
  esi_employee DECIMAL(12,2) NOT NULL DEFAULT 0,
  tds DECIMAL(12,2) NOT NULL DEFAULT 0,
  other_deduction DECIMAL(12,2) NOT NULL DEFAULT 0,
  net_pay DECIMAL(12,2) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'COMPUTED',
  payslip_path VARCHAR(256) NULL,
  UNIQUE KEY uq_payroll (tenant_id, payroll_period, employee_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m7_statutory_return (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  return_kind VARCHAR(16) NOT NULL,
  period VARCHAR(8) NOT NULL,
  due_date DATE NOT NULL,
  filed_on DATE NULL,
  acknowledgement_no VARCHAR(64) NULL,
  amount DECIMAL(14,2) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DUE',
  UNIQUE KEY uq_stat_return (tenant_id, return_kind, period),
  KEY idx_stat_due (due_date, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_attendance (tenant_id, employee_id, shift_code, date_of, in_time, out_time, worked_hours, ot_hours, status, source) VALUES
  (1, 2003, 'A', '2026-06-08', '06:00:00','14:15:00', 8.25, 0.25, 'PRESENT','BIOMETRIC'),
  (1, 2004, 'B', '2026-06-08', '14:00:00','22:30:00', 8.50, 0.50, 'PRESENT','BIOMETRIC'),
  (1, 2001, 'A', '2026-06-08', '06:15:00','14:00:00', 7.75, 0.00, 'LATE','BIOMETRIC'),
  (1, 2002, NULL, '2026-06-08', NULL, NULL, 0.00, 0.00, 'ABSENT','MANUAL');

INSERT IGNORE INTO m7_leave_request (tenant_id, lr_number, employee_id, leave_kind, start_date, end_date, days, reason, status) VALUES
  (1,'LR-2026-001', 2003, 'CL',  '2026-06-15','2026-06-15', 1.0, 'Family event','PENDING'),
  (1,'LR-2026-002', 2004, 'SL',  '2026-06-10','2026-06-11', 2.0, 'Viral fever',  'APPROVED'),
  (1,'LR-2026-003', 2001, 'EL',  '2026-07-01','2026-07-05', 5.0, 'Vacation',     'PENDING');

INSERT IGNORE INTO m7_payroll_run (tenant_id, payroll_period, employee_id, worked_days, basic, hra, conveyance, ot_amount, gross, pf_employee, esi_employee, tds, net_pay, status) VALUES
  (1,'2026-05', 2003, 26.0, 25000.00, 10000.00, 1600.00,  500.00, 37100.00, 3000.00, 278.25,  0.00, 33821.75,'PAID'),
  (1,'2026-05', 2004, 26.0, 22000.00,  8800.00, 1600.00,  800.00, 33200.00, 2640.00, 249.00,  0.00, 30311.00,'PAID'),
  (1,'2026-05', 2001, 25.0, 35000.00, 14000.00, 1600.00,    0.00, 50600.00, 4200.00,   0.00,1500.00, 44900.00,'PAID');

INSERT IGNORE INTO m7_statutory_return (tenant_id, return_kind, period, due_date, filed_on, acknowledgement_no, amount, status) VALUES
  (1,'GSTR-1','2026-05','2026-06-11','2026-06-10','GSTR1-AB123456789', 287500.00,'FILED'),
  (1,'GSTR-3B','2026-05','2026-06-20', NULL,             NULL,               115000.00,'DUE'),
  (1,'PF',    '2026-05','2026-06-15','2026-06-14','PF-CHALLAN-9988',     11340.00,'FILED'),
  (1,'ESI',   '2026-05','2026-06-15','2026-06-14','ESI-CHALLAN-7766',      812.25,'FILED'),
  (1,'TDS',   '2026-05','2026-06-07','2026-06-06','TDS-CHALLAN-5544',     1500.00,'FILED'),
  (1,'MSME-1','2026-04-2026-09','2026-10-31', NULL,             NULL,               0.00,'DUE');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M7.LeaveRequest',1,'PUBLISHED','{"code":"M7.LeaveRequest","version":1,"initialState":"PENDING","states":[{"code":"PENDING","assignedRole":"Manager"},{"code":"APPROVED","terminal":true},{"code":"REJECTED","terminal":true}],"transitions":[{"from":"PENDING","to":"APPROVED","trigger":"page.submitted","pageCode":"pico:lr-approve","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"PENDING","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:lr-approve","guard":"#submitted[''status''] == ''REJECTED''"}]}'),
(1,'M7.PayrollRun',1,'PUBLISHED','{"code":"M7.PayrollRun","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Dispatch"},{"code":"COMPUTED","assignedRole":"Manager"},{"code":"APPROVED","assignedRole":"Dispatch"},{"code":"PAID","terminal":true}],"transitions":[{"from":"DRAFT","to":"COMPUTED","trigger":"page.submitted","pageCode":"pico:pr-compute"},{"from":"COMPUTED","to":"APPROVED","trigger":"page.submitted","pageCode":"pico:pr-approve"},{"from":"APPROVED","to":"PAID","trigger":"page.submitted","pageCode":"pico:pr-disburse"}]}');

-- ===== CRM ============================================================
CREATE TABLE IF NOT EXISTS m8_lead (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  lead_number VARCHAR(32) NOT NULL,
  lead_name VARCHAR(256) NOT NULL,
  contact_name VARCHAR(128) NULL,
  contact_phone VARCHAR(32) NULL,
  contact_email VARCHAR(128) NULL,
  source VARCHAR(32) NULL,
  estimated_value DECIMAL(14,2) NULL,
  stage VARCHAR(16) NOT NULL DEFAULT 'NEW',
  owner_id BIGINT NULL,
  next_action_on DATE NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_lead_number (tenant_id, lead_number),
  KEY idx_lead_stage (stage, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m8_quote (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  quote_number VARCHAR(32) NOT NULL,
  lead_id BIGINT NULL,
  customer_name VARCHAR(256) NOT NULL,
  total_value DECIMAL(14,2) NOT NULL,
  valid_until DATE NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_quote_number (tenant_id, quote_number)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m8_lead (tenant_id, lead_number, lead_name, contact_name, contact_phone, contact_email, source, estimated_value, stage, status) VALUES
  (1,'LD-2026-001','Annapoorna Caterers',  'R.Ravi',   '+919876543210','ravi@annapoorna.in',  'TRADE_SHOW',   500000.00,'QUALIFIED','OPEN'),
  (1,'LD-2026-002','Salem Sweets House',   'K.Kumar',  '+919812345678','k@salemsweets.in',    'REFERRAL',     250000.00,'CONTACTED','OPEN'),
  (1,'LD-2026-003','Trichy Bakery Co',     'S.Suresh', '+919811112222','suresh@trichybakery','WEBSITE',      750000.00,'NEW',     'OPEN');

INSERT IGNORE INTO m8_quote (tenant_id, quote_number, lead_id, customer_name, total_value, valid_until, status) VALUES
  (1,'QT-2026-001',(SELECT id FROM m8_lead WHERE lead_number='LD-2026-001'),'Annapoorna Caterers',460000.00,'2026-06-30','SENT'),
  (1,'QT-2026-002',(SELECT id FROM m8_lead WHERE lead_number='LD-2026-002'),'Salem Sweets House',  235000.00,'2026-06-25','DRAFT');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M8.Lead',1,'PUBLISHED','{"code":"M8.Lead","version":1,"initialState":"NEW","states":[{"code":"NEW","assignedRole":"Dispatch"},{"code":"CONTACTED","assignedRole":"Dispatch"},{"code":"QUALIFIED","assignedRole":"Manager"},{"code":"QUOTED","assignedRole":"Manager"},{"code":"WON","terminal":true},{"code":"LOST","terminal":true}],"transitions":[{"from":"NEW","to":"CONTACTED","trigger":"page.submitted","pageCode":"pico:lead-contact"},{"from":"CONTACTED","to":"QUALIFIED","trigger":"page.submitted","pageCode":"pico:lead-qualify"},{"from":"QUALIFIED","to":"QUOTED","trigger":"page.submitted","pageCode":"pico:lead-quote"},{"from":"QUOTED","to":"WON","trigger":"page.submitted","pageCode":"pico:lead-close","guard":"#submitted[''outcome''] == ''WON''"},{"from":"QUOTED","to":"LOST","trigger":"page.submitted","pageCode":"pico:lead-close","guard":"#submitted[''outcome''] == ''LOST''"}]}'),
(1,'M8.Quote',1,'PUBLISHED','{"code":"M8.Quote","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Dispatch"},{"code":"SENT","assignedRole":"Manager"},{"code":"ACCEPTED","terminal":true},{"code":"REJECTED","terminal":true},{"code":"EXPIRED","terminal":true}],"transitions":[{"from":"DRAFT","to":"SENT","trigger":"page.submitted","pageCode":"pico:quote-send"},{"from":"SENT","to":"ACCEPTED","trigger":"page.submitted","pageCode":"pico:quote-outcome","guard":"#submitted[''outcome''] == ''ACCEPTED''"},{"from":"SENT","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:quote-outcome","guard":"#submitted[''outcome''] == ''REJECTED''"}]}');

-- ===== FIELD SERVICE ===================================================
CREATE TABLE IF NOT EXISTS m9_service_ticket (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  ticket_no VARCHAR(32) NOT NULL,
  customer_name VARCHAR(256) NOT NULL,
  site_address VARCHAR(256) NULL,
  issue_kind VARCHAR(32) NULL,
  priority VARCHAR(8) NOT NULL DEFAULT 'P3',
  reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  technician_id BIGINT NULL,
  scheduled_for TIMESTAMP NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  resolution TEXT NULL,
  closed_at TIMESTAMP NULL,
  UNIQUE KEY uq_ticket_no (tenant_id, ticket_no),
  KEY idx_st_status (status, priority)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m9_service_ticket (tenant_id, ticket_no, customer_name, site_address, issue_kind, priority, status) VALUES
  (1,'ST-2026-001','Coimbatore Foods Pvt Ltd','Avinashi Rd, Coimbatore','LEAKAGE','P1','SCHEDULED'),
  (1,'ST-2026-002','Madurai Wholesalers',     'Anna Nagar, Madurai',    'WRONG_ITEM','P2','OPEN'),
  (1,'ST-2026-003','Chennai Hotels Supply Co','OMR, Chennai',          'PACKAGING','P3','IN_PROGRESS');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M9.ServiceTicket',1,'PUBLISHED','{"code":"M9.ServiceTicket","version":1,"initialState":"OPEN","states":[{"code":"OPEN","assignedRole":"Manager"},{"code":"SCHEDULED","assignedRole":"UnloadingSupervisor"},{"code":"IN_PROGRESS","assignedRole":"UnloadingSupervisor"},{"code":"AWAITING_CUSTOMER","assignedRole":"Manager"},{"code":"RESOLVED","assignedRole":"Manager"},{"code":"CLOSED","terminal":true}],"transitions":[{"from":"OPEN","to":"SCHEDULED","trigger":"page.submitted","pageCode":"pico:st-schedule"},{"from":"SCHEDULED","to":"IN_PROGRESS","trigger":"page.submitted","pageCode":"pico:st-start"},{"from":"IN_PROGRESS","to":"AWAITING_CUSTOMER","trigger":"page.submitted","pageCode":"pico:st-customer"},{"from":"AWAITING_CUSTOMER","to":"IN_PROGRESS","trigger":"page.submitted","pageCode":"pico:st-customer"},{"from":"IN_PROGRESS","to":"RESOLVED","trigger":"page.submitted","pageCode":"pico:st-resolve"},{"from":"RESOLVED","to":"CLOSED","trigger":"page.submitted","pageCode":"pico:st-close"}]}');

-- ===== COMPLIANCE / AUDIT LOG =========================================
CREATE TABLE IF NOT EXISTS m0_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actor_id BIGINT NULL,
  action_kind VARCHAR(32) NOT NULL,
  entity_code VARCHAR(64) NULL,
  entity_id BIGINT NULL,
  workflow_code VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  from_state VARCHAR(64) NULL,
  to_state VARCHAR(64) NULL,
  message VARCHAR(512) NULL,
  payload_json TEXT NULL,
  KEY idx_audit_ts (ts),
  KEY idx_audit_actor (actor_id, ts),
  KEY idx_audit_entity (entity_code, entity_id),
  KEY idx_audit_wf (workflow_code, workflow_instance_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m0_audit_log (tenant_id, ts, actor_id, action_kind, entity_code, entity_id, workflow_code, from_state, to_state, message) VALUES
  (1,'2026-06-08 09:00:00', 2001, 'workflow.transition','GatePass',         101,'M10.TankerInbound','PARKING_IN','DISPATCH_QUEUED','Gate Pass dispatched to QC'),
  (1,'2026-06-08 09:15:00', 2004, 'workflow.transition','GatePass',         101,'M10.TankerInbound','DISPATCH_QUEUED','QC_APPROVED','QC passed'),
  (1,'2026-06-08 10:30:00', 2003, 'workflow.transition','GoodsReceiptNote',  1,'M5.InwardReceipt','QC_CHECKED','STORED','GRN-2026-001 stored in T-OIL-03'),
  (1,'2026-06-08 14:00:00', NULL, 'cron.shift-rotation', NULL,             NULL, NULL,            'A','B', 'Shift auto-rotated A→B by scheduler');

-- ===== DOCUMENT MANAGEMENT ============================================
CREATE TABLE IF NOT EXISTS m0_document (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  doc_code VARCHAR(32) NOT NULL,
  doc_kind VARCHAR(32) NOT NULL,
  title VARCHAR(256) NOT NULL,
  file_name VARCHAR(256) NULL,
  mime_type VARCHAR(64) NULL,
  size_bytes BIGINT NULL,
  storage_path VARCHAR(512) NULL,
  linked_entity_code VARCHAR(64) NULL,
  linked_entity_id BIGINT NULL,
  uploaded_by BIGINT NULL,
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  retention_until DATE NULL,
  esign_status VARCHAR(16) NULL,
  esign_provider VARCHAR(32) NULL,
  UNIQUE KEY uq_doc_code (tenant_id, doc_code),
  KEY idx_doc_link (linked_entity_code, linked_entity_id),
  KEY idx_doc_kind (doc_kind)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m0_document (tenant_id, doc_code, doc_kind, title, file_name, mime_type, size_bytes, storage_path, linked_entity_code, linked_entity_id, retention_until, esign_status, esign_provider) VALUES
  (1,'DOC-2026-001','INVOICE_PDF',     'SI-2026-001.pdf',         'SI-2026-001.pdf','application/pdf', 132456, '/store/2026/06/SI-2026-001.pdf','SalesInvoice', 1,'2034-06-30','SIGNED','LEEGALITY'),
  (1,'DOC-2026-002','PO_PDF',          'PO-2026-001.pdf',         'PO-2026-001.pdf','application/pdf',  89123, '/store/2026/06/PO-2026-001.pdf','PurchaseOrder', 1,'2034-06-30','NOT_REQUIRED',NULL),
  (1,'DOC-2026-003','VENDOR_AGREEMENT','TESTRO Industries MSA.pdf','TESTRO-MSA.pdf','application/pdf', 245678, '/store/2026/06/TESTRO-MSA.pdf','Vendor',         1,'2030-06-30','SIGNED','DOCUSIGN'),
  (1,'DOC-2026-004','FSSAI_CERT',      'FSSAI Renewal 2026.pdf',  'FSSAI-2026.pdf','application/pdf',  67890, '/store/2026/06/FSSAI-2026.pdf','KycRecord',      1,'2027-04-15','SIGNED','SELF');

-- ===== ENTITY REGISTRY ================================================
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'Attendance','Attendance','m7_attendance','Per-employee day attendance.','HR' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='Attendance');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'LeaveRequest','Leave Request','m7_leave_request','Employee leave applications.','HR' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='LeaveRequest');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'PayrollRun','Payroll','m7_payroll_run','Monthly payroll calc.','HR' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='PayrollRun');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'StatutoryReturn','Statutory Return','m7_statutory_return','GSTR/PF/ESI/TDS due+filed.','HR' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='StatutoryReturn');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'Lead','Lead','m8_lead','CRM lead.','CRM' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='Lead');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'Quote','Quote','m8_quote','CRM quote.','CRM' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='Quote');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'ServiceTicket','Service Ticket','m9_service_ticket','Field-service ticket.','M9' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='ServiceTicket');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'AuditLog','Audit Log','m0_audit_log','Compliance audit trail.','M0' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='AuditLog');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'Document','Document','m0_document','DMS — uploaded files + e-sign status.','M0' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='Document');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Attendance','employee_id','Employee','LONG',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Attendance' AND field_code='employee_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Attendance','shift_code','Shift','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Attendance' AND field_code='shift_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Attendance','date_of','Date','DATE',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Attendance' AND field_code='date_of');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Attendance','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Attendance' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Attendance','worked_hours','Hours','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Attendance' AND field_code='worked_hours');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Attendance','ot_hours','OT','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Attendance' AND field_code='ot_hours');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Attendance','source','Src','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Attendance' AND field_code='source');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'LeaveRequest','lr_number','LR #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LeaveRequest' AND field_code='lr_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'LeaveRequest','employee_id','Employee','LONG',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LeaveRequest' AND field_code='employee_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'LeaveRequest','leave_kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LeaveRequest' AND field_code='leave_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'LeaveRequest','days','Days','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LeaveRequest' AND field_code='days');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'LeaveRequest','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LeaveRequest' AND field_code='status');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PayrollRun','payroll_period','Period','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PayrollRun' AND field_code='payroll_period');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PayrollRun','employee_id','Employee','LONG',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PayrollRun' AND field_code='employee_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PayrollRun','gross','Gross','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PayrollRun' AND field_code='gross');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PayrollRun','net_pay','Net','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PayrollRun' AND field_code='net_pay');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'PayrollRun','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PayrollRun' AND field_code='status');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StatutoryReturn','return_kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StatutoryReturn' AND field_code='return_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StatutoryReturn','period','Period','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StatutoryReturn' AND field_code='period');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StatutoryReturn','due_date','Due','DATE',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StatutoryReturn' AND field_code='due_date');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StatutoryReturn','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StatutoryReturn' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StatutoryReturn','amount','Amount','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StatutoryReturn' AND field_code='amount');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Lead','lead_number','Lead #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Lead' AND field_code='lead_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Lead','lead_name','Lead','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Lead' AND field_code='lead_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Lead','stage','Stage','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Lead' AND field_code='stage');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Lead','estimated_value','Value','DECIMAL',FALSE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Lead' AND field_code='estimated_value');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Lead','source','Source','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Lead' AND field_code='source');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Quote','quote_number','Quote #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Quote' AND field_code='quote_number');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Quote','customer_name','Customer','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Quote' AND field_code='customer_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Quote','total_value','Value','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Quote' AND field_code='total_value');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Quote','valid_until','Valid','DATE',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Quote' AND field_code='valid_until');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Quote','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Quote' AND field_code='status');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ServiceTicket','ticket_no','Ticket','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ServiceTicket' AND field_code='ticket_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ServiceTicket','customer_name','Customer','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ServiceTicket' AND field_code='customer_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ServiceTicket','issue_kind','Issue','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ServiceTicket' AND field_code='issue_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ServiceTicket','priority','Priority','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ServiceTicket' AND field_code='priority');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ServiceTicket','status','Status','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ServiceTicket' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'ServiceTicket','reported_at','Reported','DATETIME',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ServiceTicket' AND field_code='reported_at');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AuditLog','ts','When','DATETIME',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AuditLog' AND field_code='ts');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AuditLog','actor_id','Actor','LONG',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AuditLog' AND field_code='actor_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AuditLog','action_kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AuditLog' AND field_code='action_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AuditLog','entity_code','Entity','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AuditLog' AND field_code='entity_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AuditLog','workflow_code','Workflow','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AuditLog' AND field_code='workflow_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AuditLog','to_state','To','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AuditLog' AND field_code='to_state');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'AuditLog','message','Message','STRING',FALSE,FALSE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='AuditLog' AND field_code='message');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Document','doc_code','Doc #','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Document' AND field_code='doc_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Document','doc_kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Document' AND field_code='doc_kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Document','title','Title','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Document' AND field_code='title');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Document','linked_entity_code','Linked To','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Document' AND field_code='linked_entity_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Document','esign_status','E-Sign','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Document' AND field_code='esign_status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'Document','retention_until','Retention','DATE',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Document' AND field_code='retention_until');

-- ===== PAGES ==========================================================
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:attendance-list','Attendance','Per-day attendance.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:attendance-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:attendance-mark','Mark Attendance','Manual punch / late-mark.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:attendance-mark');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:lr-list','Leave Requests','All leave applications.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:lr-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:lr-apply','Apply Leave','Employee applies for leave.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:lr-apply');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:lr-approve','LR Approve','Manager approves / rejects.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:lr-approve');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pr-list','Payroll','Monthly payroll runs.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pr-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pr-compute','PR Compute','Compute the payroll period.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pr-compute');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pr-approve','PR Approve','Manager approves payroll.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pr-approve');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:pr-disburse','PR Disburse','Bank disbursement file.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:pr-disburse');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:stat-list','Statutory Returns','GSTR / PF / ESI / TDS.','Report','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:stat-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:lead-list','CRM Leads','Open leads.','CRM','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:lead-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:lead-create','New Lead','Log a new lead.','CRM','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:lead-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:lead-contact','Lead — Contacted','First contact made.','CRM','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:lead-contact');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:lead-qualify','Lead — Qualify','Manager qualifies lead.','CRM','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:lead-qualify');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:lead-quote','Lead — Send Quote','Generate quote.','CRM','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:lead-quote');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:lead-close','Lead — Close','WON / LOST outcome.','CRM','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:lead-close');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:quote-list','Quotes','All quotes.','CRM','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:quote-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:quote-send','Quote — Send','Send quote to customer.','CRM','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:quote-send');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:quote-outcome','Quote — Outcome','ACCEPTED / REJECTED.','CRM','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:quote-outcome');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:st-list','Service Tickets','Field service queue.','M9','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:st-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:st-create','New Service Ticket','Customer logs ticket.','M9','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:st-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:st-schedule','ST — Schedule','Manager assigns tech.','M9','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:st-schedule');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:st-start','ST — Start','Tech starts work.','M9','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:st-start');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:st-customer','ST — Customer Action','Pause for customer.','M9','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:st-customer');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:st-resolve','ST — Resolve','Mark resolved.','M9','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:st-resolve');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:st-close','ST — Close','Close ticket.','M9','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:st-close');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:audit-log','Audit Log','Compliance audit trail.','M0','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:audit-log');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:document-list','Documents','DMS with e-sign + retention.','M0','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:document-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:document-upload','Upload Document','Upload + link to entity.','M0','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:document-upload');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Attendance","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","employee_id","shift_code","date_of","in_time","out_time","worked_hours","ot_hours","status","source"]},"columns":[{"field":"date_of","label":"Date","render":"date","sticky":"left","sortable":true},{"field":"employee_id","label":"Emp"},{"field":"shift_code","label":"Shift","render":"badge"},{"field":"in_time","label":"In"},{"field":"out_time","label":"Out"},{"field":"worked_hours","label":"Hrs","render":"number","align":"right"},{"field":"ot_hours","label":"OT","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge","colorMap":{"PRESENT":"green","LATE":"amber","ABSENT":"red","HALF_DAY":"blue","WFH":"purple"}},{"field":"source","label":"Src","render":"badge"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["PRESENT","LATE","ABSENT","HALF_DAY","WFH"]}},{"field":"source","label":"Src","type":"multi-select","optionsSource":{"type":"static","options":["BIOMETRIC","MANUAL","RFID","MOBILE"]}}],"sort":{"default":[{"field":"date_of","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Attendance"}}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:attendance-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Manual Mark","fields":[{"kind":"FormField","type":"text","name":"employeeId","label":"Employee ID"},{"kind":"FormField","type":"text","name":"date","label":"Date"},{"kind":"FormField","type":"select","name":"status","label":"Status","options":[{"label":"PRESENT","value":"PRESENT"},{"label":"LATE","value":"LATE"},{"label":"ABSENT","value":"ABSENT"},{"label":"HALF_DAY","value":"HALF_DAY"},{"label":"WFH","value":"WFH"}]},{"kind":"WorkflowActionButton","label":"Save","action":"save","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:attendance-mark' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"LeaveRequest","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","lr_number","employee_id","leave_kind","days","status"]},"columns":[{"field":"lr_number","label":"LR #","sticky":"left","sortable":true},{"field":"employee_id","label":"Emp"},{"field":"leave_kind","label":"Kind","render":"badge","colorMap":{"CL":"blue","SL":"amber","EL":"green","ML":"purple","LWP":"red"}},{"field":"days","label":"Days","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge","colorMap":{"PENDING":"amber","APPROVED":"green","REJECTED":"red"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["PENDING","APPROVED","REJECTED"]}}],"sort":{"default":[{"field":"lr_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Leave Requests"}}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:lr-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Apply Leave","fields":[{"kind":"FormField","type":"select","name":"leaveKind","label":"Kind","options":[{"label":"CL","value":"CL"},{"label":"SL","value":"SL"},{"label":"EL","value":"EL"},{"label":"ML","value":"ML"},{"label":"LWP","value":"LWP"}]},{"kind":"FormField","type":"text","name":"startDate","label":"From"},{"kind":"FormField","type":"text","name":"endDate","label":"To"},{"kind":"FormField","type":"number","name":"days","label":"Days"},{"kind":"FormField","type":"text","name":"reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:lr-apply' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Approve / Reject Leave","fields":[{"kind":"FormField","type":"text","name":"reason","label":"Manager Note"},{"kind":"FormField","type":"select","name":"status","label":"Decision","options":[{"label":"APPROVED","value":"APPROVED"},{"label":"REJECTED","value":"REJECTED"}]},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:lr-approve' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"PayrollRun","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","payroll_period","employee_id","gross","net_pay","status"]},"columns":[{"field":"payroll_period","label":"Period","sticky":"left","sortable":true},{"field":"employee_id","label":"Emp"},{"field":"gross","label":"Gross","render":"number","align":"right"},{"field":"net_pay","label":"Net","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge","colorMap":{"DRAFT":"grey","COMPUTED":"blue","APPROVED":"amber","PAID":"green"}}],"sort":{"default":[{"field":"payroll_period","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Payroll"}}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pr-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Compute Payroll","fields":[{"kind":"FormField","type":"text","name":"payrollPeriod","label":"Period YYYY-MM"},{"kind":"WorkflowActionButton","label":"Compute All","action":"external.api.call","args":{"connector":"INTERNAL","operation":"payroll.compute"},"variant":"primary"},{"kind":"WorkflowActionButton","label":"Mark Computed","action":"workflow.transition"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pr-compute' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Approve Payroll","fields":[{"kind":"FormField","type":"text","name":"notes","label":"Notes"},{"kind":"WorkflowActionButton","label":"Approve","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pr-approve' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Disburse","fields":[{"kind":"FormField","type":"text","name":"bankFile","label":"Bank File Generated"},{"kind":"WorkflowActionButton","label":"Generate Bank File","action":"export.csv","args":{"format":"NEFT_BATCH"}},{"kind":"WorkflowActionButton","label":"Mark Paid","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:pr-disburse' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"StatutoryReturn","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","return_kind","period","due_date","filed_on","acknowledgement_no","amount","status"]},"columns":[{"field":"return_kind","label":"Kind","sticky":"left","render":"badge","colorMap":{"GSTR-1":"blue","GSTR-3B":"blue","PF":"green","ESI":"green","TDS":"amber","MSME-1":"purple"}},{"field":"period","label":"Period","sortable":true},{"field":"due_date","label":"Due","render":"date"},{"field":"filed_on","label":"Filed","render":"date"},{"field":"acknowledgement_no","label":"Ack #"},{"field":"amount","label":"Amount","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge","colorMap":{"DUE":"red","FILED":"green","LATE":"amber"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["DUE","FILED","LATE"]}},{"field":"return_kind","label":"Kind","type":"multi-select","optionsSource":{"type":"static","options":["GSTR-1","GSTR-3B","PF","ESI","TDS","MSME-1"]}}],"sort":{"default":[{"field":"due_date","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Statutory Returns"}}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:stat-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Lead","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","lead_number","lead_name","stage","estimated_value","source","status"]},"columns":[{"field":"lead_number","label":"#","sticky":"left","sortable":true},{"field":"lead_name","label":"Lead"},{"field":"stage","label":"Stage","render":"badge","colorMap":{"NEW":"grey","CONTACTED":"blue","QUALIFIED":"amber","QUOTED":"amber","WON":"green","LOST":"red"}},{"field":"estimated_value","label":"Est. Value","render":"number","align":"right"},{"field":"source","label":"Source","render":"badge","colorMap":{"TRADE_SHOW":"blue","REFERRAL":"green","WEBSITE":"amber","COLD_CALL":"purple","INBOUND":"green"}},{"field":"status","label":"Status","render":"badge","colorMap":{"OPEN":"green","CLOSED_WON":"green","CLOSED_LOST":"red"}}],"filters":[{"field":"stage","label":"Stage","type":"multi-select","optionsSource":{"type":"static","options":["NEW","CONTACTED","QUALIFIED","QUOTED","WON","LOST"]}}],"sort":{"default":[{"field":"lead_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"CRM Leads"}}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:lead-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New Lead","fields":[{"kind":"FormField","type":"text","name":"leadName","label":"Lead Name"},{"kind":"FormField","type":"text","name":"contactName","label":"Contact"},{"kind":"FormField","type":"text","name":"contactPhone","label":"Phone"},{"kind":"FormField","type":"text","name":"contactEmail","label":"Email"},{"kind":"FormField","type":"select","name":"source","label":"Source","options":[{"label":"TRADE_SHOW","value":"TRADE_SHOW"},{"label":"REFERRAL","value":"REFERRAL"},{"label":"WEBSITE","value":"WEBSITE"},{"label":"COLD_CALL","value":"COLD_CALL"},{"label":"INBOUND","value":"INBOUND"}]},{"kind":"FormField","type":"number","name":"estimatedValue","label":"Est. Value"},{"kind":"WorkflowActionButton","label":"Save Lead","action":"save","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:lead-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Mark Contacted","fields":[{"kind":"FormField","type":"text","name":"contactNotes","label":"Notes"},{"kind":"WorkflowActionButton","label":"Save + Next","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:lead-contact' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Qualify Lead","fields":[{"kind":"FormField","type":"text","name":"score","label":"BANT Score 0-100"},{"kind":"FormField","type":"text","name":"reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Qualify","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:lead-qualify' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Send Quote","fields":[{"kind":"FormField","type":"number","name":"totalValue","label":"Quote Value"},{"kind":"FormField","type":"text","name":"validUntil","label":"Valid Until"},{"kind":"WorkflowActionButton","label":"Generate + Send","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:lead-quote' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close Lead","fields":[{"kind":"FormField","type":"select","name":"outcome","label":"Outcome","options":[{"label":"WON","value":"WON"},{"label":"LOST","value":"LOST"}]},{"kind":"FormField","type":"text","name":"reason","label":"Reason / Notes"},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:lead-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Quote","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","quote_number","customer_name","total_value","valid_until","status"]},"columns":[{"field":"quote_number","label":"Quote #","sticky":"left","sortable":true},{"field":"customer_name","label":"Customer"},{"field":"total_value","label":"Value","render":"number","align":"right"},{"field":"valid_until","label":"Valid","render":"date"},{"field":"status","label":"Status","render":"badge","colorMap":{"DRAFT":"grey","SENT":"blue","ACCEPTED":"green","REJECTED":"red","EXPIRED":"amber"}}],"sort":{"default":[{"field":"quote_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Quotes"}}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:quote-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Send Quote","fields":[{"kind":"FormField","type":"text","name":"recipient","label":"Recipient"},{"kind":"WorkflowActionButton","label":"Send","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:quote-send' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Quote Outcome","fields":[{"kind":"FormField","type":"select","name":"outcome","label":"Outcome","options":[{"label":"ACCEPTED","value":"ACCEPTED"},{"label":"REJECTED","value":"REJECTED"}]},{"kind":"FormField","type":"text","name":"reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:quote-outcome' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ServiceTicket","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","ticket_no","customer_name","issue_kind","priority","status","reported_at"]},"columns":[{"field":"ticket_no","label":"Ticket #","sticky":"left","sortable":true},{"field":"customer_name","label":"Customer"},{"field":"issue_kind","label":"Issue","render":"badge"},{"field":"priority","label":"Pri","render":"badge","colorMap":{"P1":"red","P2":"amber","P3":"blue","P4":"grey"}},{"field":"status","label":"Status","render":"badge","colorMap":{"OPEN":"grey","SCHEDULED":"blue","IN_PROGRESS":"amber","AWAITING_CUSTOMER":"purple","RESOLVED":"green","CLOSED":"green"}},{"field":"reported_at","label":"Reported","render":"datetime"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["OPEN","SCHEDULED","IN_PROGRESS","AWAITING_CUSTOMER","RESOLVED","CLOSED"]}},{"field":"priority","label":"Priority","type":"multi-select","optionsSource":{"type":"static","options":["P1","P2","P3","P4"]}}],"sort":{"default":[{"field":"reported_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Service Tickets"}}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:st-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New Ticket","fields":[{"kind":"FormField","type":"text","name":"customerName","label":"Customer"},{"kind":"FormField","type":"text","name":"siteAddress","label":"Site"},{"kind":"FormField","type":"select","name":"issueKind","label":"Issue Kind","options":[{"label":"LEAKAGE","value":"LEAKAGE"},{"label":"WRONG_ITEM","value":"WRONG_ITEM"},{"label":"PACKAGING","value":"PACKAGING"},{"label":"QUANTITY","value":"QUANTITY"},{"label":"QUALITY","value":"QUALITY"},{"label":"OTHER","value":"OTHER"}]},{"kind":"FormField","type":"select","name":"priority","label":"Priority","options":[{"label":"P1","value":"P1"},{"label":"P2","value":"P2"},{"label":"P3","value":"P3"},{"label":"P4","value":"P4"}]},{"kind":"WorkflowActionButton","label":"Create Ticket","action":"save","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:st-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Schedule Technician","fields":[{"kind":"FormField","type":"text","name":"technicianId","label":"Technician"},{"kind":"FormField","type":"text","name":"scheduledFor","label":"Scheduled For"},{"kind":"WorkflowActionButton","label":"Schedule","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:st-schedule' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Start Work","fields":[{"kind":"FormField","type":"text","name":"startedAt","label":"Started At"},{"kind":"WorkflowActionButton","label":"Start","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:st-start' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Customer Action","fields":[{"kind":"FormField","type":"text","name":"needsFromCustomer","label":"Needs From Customer"},{"kind":"WorkflowActionButton","label":"Mark Awaiting","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:st-customer' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Resolve Ticket","fields":[{"kind":"FormField","type":"text","name":"resolution","label":"Resolution"},{"kind":"FormField","type":"text","name":"laborHours","label":"Labour Hours"},{"kind":"WorkflowActionButton","label":"Resolve","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:st-resolve' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Close Ticket","fields":[{"kind":"FormField","type":"text","name":"closingNotes","label":"Notes"},{"kind":"WorkflowActionButton","label":"Close","action":"workflow.transition","variant":"primary"}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:st-close' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"AuditLog","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","ts","actor_id","action_kind","entity_code","workflow_code","to_state","message"]},"columns":[{"field":"ts","label":"When","render":"datetime","sticky":"left","sortable":true},{"field":"actor_id","label":"Actor"},{"field":"action_kind","label":"Action","render":"badge","colorMap":{"workflow.transition":"blue","cron.shift-rotation":"amber","auth.signin":"green","entity.update":"purple"}},{"field":"entity_code","label":"Entity"},{"field":"workflow_code","label":"Workflow"},{"field":"to_state","label":"-> State"},{"field":"message","label":"Message"}],"filters":[{"field":"action_kind","label":"Action","type":"text-contains","applyTo":["action_kind"]},{"field":"entity_code","label":"Entity","type":"text-contains","applyTo":["entity_code"]}],"sort":{"default":[{"field":"ts","dir":"desc"}]},"pagination":{"kind":"server","pageSize":100},"header":{"title":"Audit Log"}}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:audit-log' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Document","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","doc_code","doc_kind","title","linked_entity_code","esign_status","retention_until"]},"columns":[{"field":"doc_code","label":"Doc #","sticky":"left","sortable":true},{"field":"title","label":"Title"},{"field":"doc_kind","label":"Kind","render":"badge"},{"field":"linked_entity_code","label":"Linked To","render":"badge"},{"field":"esign_status","label":"E-Sign","render":"badge","colorMap":{"SIGNED":"green","PENDING":"amber","NOT_REQUIRED":"grey","REJECTED":"red"}},{"field":"retention_until","label":"Retain Until","render":"date"}],"filters":[{"field":"doc_kind","label":"Kind","type":"text-contains","applyTo":["doc_kind"]},{"field":"esign_status","label":"E-Sign","type":"multi-select","optionsSource":{"type":"static","options":["SIGNED","PENDING","NOT_REQUIRED","REJECTED"]}}],"sort":{"default":[{"field":"doc_code","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Document Management"}}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:document-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Upload Document","fields":[{"kind":"FormField","type":"text","name":"title","label":"Title"},{"kind":"FormField","type":"select","name":"docKind","label":"Kind","options":[{"label":"INVOICE_PDF","value":"INVOICE_PDF"},{"label":"PO_PDF","value":"PO_PDF"},{"label":"VENDOR_AGREEMENT","value":"VENDOR_AGREEMENT"},{"label":"FSSAI_CERT","value":"FSSAI_CERT"},{"label":"GST_CERT","value":"GST_CERT"},{"label":"OTHER","value":"OTHER"}]},{"kind":"FormField","type":"text","name":"linkedEntityCode","label":"Linked Entity"},{"kind":"FormField","type":"text","name":"retentionUntil","label":"Retain Until"},{"kind":"WorkflowActionButton","label":"Upload + Save","action":"save","variant":"primary"},{"kind":"WorkflowActionButton","label":"Send for E-Sign","action":"external.api.call","args":{"connector":"LEEGALITY","operation":"sendForEsign"}}]}]}', 'V17', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:document-upload' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code IN ('pico:attendance-list','pico:attendance-mark','pico:lr-list','pico:lr-apply','pico:lr-approve','pico:pr-list','pico:pr-compute','pico:pr-approve','pico:pr-disburse','pico:stat-list','pico:lead-list','pico:lead-create','pico:lead-contact','pico:lead-qualify','pico:lead-quote','pico:lead-close','pico:quote-list','pico:quote-send','pico:quote-outcome','pico:st-list','pico:st-create','pico:st-schedule','pico:st-start','pico:st-customer','pico:st-resolve','pico:st-close','pico:audit-log','pico:document-list','pico:document-upload') AND current_version_id IS NULL;
