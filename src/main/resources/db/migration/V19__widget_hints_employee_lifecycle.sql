-- =====================================================================
-- V19 — Widget hints on entity_field_registry + Employee lifecycle:
--        Designation master, Department alias, Candidate (interview /
--        offer / onboarding workflow), Employee with proper dropdowns.
-- =====================================================================

-- 1. Schema extension --------------------------------------------------
ALTER TABLE entity_field_registry
  ADD COLUMN IF NOT EXISTS widget_kind VARCHAR(32) NULL
    COMMENT 'text|number|date|datetime|textarea|select|multiselect|entity-ref|checkbox|currency|chip';
ALTER TABLE entity_field_registry
  ADD COLUMN IF NOT EXISTS options_json TEXT NULL
    COMMENT 'For select: ["A","B"] or [{label,value}]. For entity-ref: {entity, valueField, labelField}';

-- 2. Designation master ------------------------------------------------
CREATE TABLE IF NOT EXISTS m7_designation (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(32) NOT NULL,
  title VARCHAR(128) NOT NULL,
  band VARCHAR(8) NULL,
  reports_to_code VARCHAR(32) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uq_desig (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_designation (tenant_id, code, title, band) VALUES
  (1,'GM','General Manager','M3'),
  (1,'PM','Plant Manager','M2'),
  (1,'OM','Operations Manager','M2'),
  (1,'DM','Dispatch Manager','M1'),
  (1,'QM','Quality Manager','M1'),
  (1,'SUP','Supervisor','S2'),
  (1,'OPR','Operator','W2'),
  (1,'TEC','Technician','W3'),
  (1,'GUR','Security Guard','W1'),
  (1,'DRV','Driver','W2'),
  (1,'ACC','Accountant','S2'),
  (1,'CLR','Clerk','S1');

-- 3. Candidate (recruitment) entity ------------------------------------
CREATE TABLE IF NOT EXISTS m7_candidate (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  candidate_no VARCHAR(32) NOT NULL,
  full_name VARCHAR(256) NOT NULL,
  contact_phone VARCHAR(32) NULL,
  contact_email VARCHAR(128) NULL,
  applied_designation_code VARCHAR(32) NULL,
  applied_dept_id BIGINT NULL,
  source VARCHAR(32) NULL,
  experience_years DECIMAL(4,1) NULL,
  current_salary DECIMAL(12,2) NULL,
  expected_salary DECIMAL(12,2) NULL,
  resume_doc_id BIGINT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'NEW',
  current_step VARCHAR(64) NULL,
  workflow_instance_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  hired_employee_id BIGINT NULL,
  UNIQUE KEY uq_cand_no (tenant_id, candidate_no),
  KEY idx_cand_status (tenant_id, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_candidate (tenant_id, candidate_no, full_name, contact_phone, contact_email, applied_designation_code, source, experience_years, expected_salary, status) VALUES
  (1,'CAN-2026-001','Suresh Iyer',     '+919876543201','suresh@example.com','OPR','REFERRAL', 4.0,  28000.00,'INTERVIEWED'),
  (1,'CAN-2026-002','Lakshmi Krishnan','+919876543202','lakshmi@example.com','ACC','LINKEDIN', 6.0,  42000.00,'OFFERED'),
  (1,'CAN-2026-003','Raja Sekar',      '+919876543203','raja@example.com',  'TEC','WALK_IN',   2.0,  22000.00,'NEW'),
  (1,'CAN-2026-004','Anitha Murugan',  '+919876543204','anitha@example.com','CLR','NAUKRI',    8.0,  35000.00,'ONBOARDED');

-- 4. M7.Recruitment workflow -------------------------------------------
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M7.Recruitment',1,'PUBLISHED','{"code":"M7.Recruitment","version":1,"initialState":"NEW","states":[{"code":"NEW","assignedRole":"Manager"},{"code":"SHORTLISTED","assignedRole":"Manager"},{"code":"INTERVIEWED","assignedRole":"Manager"},{"code":"OFFERED","assignedRole":"Dispatch"},{"code":"ONBOARDED","terminal":true},{"code":"REJECTED","terminal":true},{"code":"DECLINED","terminal":true}],"transitions":[{"from":"NEW","to":"SHORTLISTED","trigger":"page.submitted","pageCode":"pico:cand-screen","guard":"#submitted[''decision''] == ''SHORTLIST''"},{"from":"NEW","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:cand-screen","guard":"#submitted[''decision''] == ''REJECT''"},{"from":"SHORTLISTED","to":"INTERVIEWED","trigger":"page.submitted","pageCode":"pico:cand-interview"},{"from":"INTERVIEWED","to":"OFFERED","trigger":"page.submitted","pageCode":"pico:cand-offer","guard":"#submitted[''decision''] == ''OFFER''"},{"from":"INTERVIEWED","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:cand-offer","guard":"#submitted[''decision''] == ''REJECT''"},{"from":"OFFERED","to":"ONBOARDED","trigger":"page.submitted","pageCode":"pico:cand-onboard","guard":"#submitted[''status''] == ''ACCEPTED''"},{"from":"OFFERED","to":"DECLINED","trigger":"page.submitted","pageCode":"pico:cand-onboard","guard":"#submitted[''status''] == ''DECLINED''"}]}');

-- 5. Entity registry rows ---------------------------------------------
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'Designation','Designation','m7_designation','Job-title master.','HR' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='Designation');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'Candidate','Candidate','m7_candidate','Recruitment pipeline.','HR' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='Candidate');

-- Designation fields
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Designation','code','Code','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Designation' AND field_code='code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Designation','title','Title','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Designation' AND field_code='title');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'Designation','band','Band','STRING',TRUE,TRUE,FALSE,FALSE,'select','["M3","M2","M1","S2","S1","W3","W2","W1"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Designation' AND field_code='band');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Designation','active','Active','BOOLEAN',TRUE,TRUE,FALSE,FALSE,'checkbox' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Designation' AND field_code='active');

-- Candidate fields with widgets
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Candidate','candidate_no','Candidate #','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='candidate_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Candidate','full_name','Name','STRING',TRUE,TRUE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='full_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Candidate','contact_phone','Phone','STRING',TRUE,FALSE,TRUE,TRUE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='contact_phone');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Candidate','contact_email','Email','STRING',TRUE,FALSE,TRUE,FALSE,'text' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='contact_email');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'Candidate','applied_designation_code','Designation','STRING',TRUE,TRUE,FALSE,FALSE,'entity-ref','{"entity":"Designation","valueField":"code","labelField":"title"}' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='applied_designation_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'Candidate','applied_dept_id','Department','LONG',TRUE,TRUE,FALSE,FALSE,'entity-ref','{"entity":"Department","valueField":"id","labelField":"name"}' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='applied_dept_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'Candidate','source','Source','STRING',TRUE,TRUE,FALSE,FALSE,'select','["REFERRAL","LINKEDIN","NAUKRI","WALK_IN","CAMPUS","CONSULTANT"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='source');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Candidate','experience_years','Exp (yrs)','DECIMAL',FALSE,TRUE,FALSE,FALSE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='experience_years');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Candidate','expected_salary','Expected ₹','DECIMAL',FALSE,TRUE,FALSE,FALSE,'number' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='expected_salary');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) SELECT 'Candidate','status','Status','STRING',TRUE,TRUE,FALSE,TRUE,'select','["NEW","SHORTLISTED","INTERVIEWED","OFFERED","ONBOARDED","REJECTED","DECLINED"]' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) SELECT 'Candidate','notes','Notes','STRING',FALSE,FALSE,TRUE,FALSE,'textarea' WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Candidate' AND field_code='notes');

-- 6. UPGRADE existing Employee fields with widgets ---------------------
UPDATE entity_field_registry SET widget_kind='text'      WHERE entity_code='Employee' AND field_code IN ('employeename','username','email','phoneno');
UPDATE entity_field_registry SET widget_kind='entity-ref',
       options_json='{"entity":"Role","valueField":"id","labelField":"name"}'
  WHERE entity_code='Employee' AND field_code='roles_id';
UPDATE entity_field_registry SET widget_kind='entity-ref',
       options_json='{"entity":"Department","valueField":"id","labelField":"name"}'
  WHERE entity_code='Employee' AND field_code='dept_id';
UPDATE entity_field_registry SET widget_kind='select',
       options_json='["Staff","Worker","Contract","Apprentice","Trainee"]'
  WHERE entity_code='Employee' AND field_code='employeetype';
UPDATE entity_field_registry SET widget_kind='select',
       options_json='[{"label":"Active","value":"A"},{"label":"Inactive","value":"I"},{"label":"On Leave","value":"L"}]'
  WHERE entity_code='Employee' AND field_code='status';

-- 7. UPGRADE Driver / Vehicle / Vendor with sensible widgets -----------
UPDATE entity_field_registry SET widget_kind='text'     WHERE entity_code='Driver'   AND field_code IN ('name','dl_number','primary_mobile','alt_mobile','aadhaar_last4');
UPDATE entity_field_registry SET widget_kind='date'     WHERE entity_code='Driver'   AND field_code='dl_expiry';
UPDATE entity_field_registry SET widget_kind='select',
       options_json='["ta","te","kn","ml","hi","en"]'
  WHERE entity_code='Driver' AND field_code='preferred_language';
UPDATE entity_field_registry SET widget_kind='checkbox' WHERE entity_code='Driver'   AND field_code='active';

UPDATE entity_field_registry SET widget_kind='text'     WHERE entity_code='Vehicle'  AND field_code IN ('registration_no','vehicle_type','transporter_name','tank_cleaning_cert_no');
UPDATE entity_field_registry SET widget_kind='number'   WHERE entity_code='Vehicle'  AND field_code IN ('capacity_kg','tanker_compartments');
UPDATE entity_field_registry SET widget_kind='date'     WHERE entity_code='Vehicle'  AND field_code IN ('rc_expiry','insurance_expiry','puc_expiry','tank_cleaning_cert_expiry');
UPDATE entity_field_registry SET widget_kind='select',
       options_json='["OWNED","HIRED","LEASED"]'
  WHERE entity_code='Vehicle' AND field_code='ownership';
UPDATE entity_field_registry SET widget_kind='checkbox' WHERE entity_code='Vehicle'  AND field_code='active';

UPDATE entity_field_registry SET widget_kind='text'     WHERE entity_code='Vendor'   AND field_code IN ('legalname','tradename','gstin','pan','contactPerson','contactPhone','contactEmail');
UPDATE entity_field_registry SET widget_kind='checkbox' WHERE entity_code='Vendor'   AND field_code IN ('is_msme','active');
UPDATE entity_field_registry SET widget_kind='select',
       options_json='["ACTIVE","INACTIVE","HOLD","BLACKLISTED"]'
  WHERE entity_code='Vendor' AND field_code='status';

-- 8. Pages for Candidate lifecycle -------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:cand-list','Candidates','Recruitment pipeline.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:cand-list');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:cand-create','New Candidate','Log a new candidate.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:cand-create');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:cand-screen','Screen Candidate','Resume + phone screen.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:cand-screen');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:cand-interview','Interview','Capture interview feedback.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:cand-interview');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:cand-offer','Offer / Reject','Manager makes the call.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:cand-offer');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:cand-onboard','Onboard','Hire + create employee row.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:cand-onboard');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:designation-master','Designations','Job-title master.','HR','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:designation-master');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Candidate","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","candidate_no","full_name","contact_phone","applied_designation_code","source","experience_years","expected_salary","status"]},"columns":[{"field":"candidate_no","label":"#","sticky":"left","sortable":true},{"field":"full_name","label":"Name"},{"field":"contact_phone","label":"Phone"},{"field":"applied_designation_code","label":"Desig","render":"badge"},{"field":"source","label":"Src","render":"badge","colorMap":{"REFERRAL":"green","LINKEDIN":"blue","NAUKRI":"purple","WALK_IN":"amber","CAMPUS":"green","CONSULTANT":"grey"}},{"field":"experience_years","label":"Exp"},{"field":"expected_salary","label":"₹ Expected","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge","colorMap":{"NEW":"grey","SHORTLISTED":"blue","INTERVIEWED":"amber","OFFERED":"amber","ONBOARDED":"green","REJECTED":"red","DECLINED":"red"}}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["NEW","SHORTLISTED","INTERVIEWED","OFFERED","ONBOARDED","REJECTED","DECLINED"]}},{"field":"source","label":"Source","type":"multi-select","optionsSource":{"type":"static","options":["REFERRAL","LINKEDIN","NAUKRI","WALK_IN","CAMPUS","CONSULTANT"]}}],"sort":{"default":[{"field":"candidate_no","dir":"desc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Candidates"}}', 'V19', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:cand-list' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Screening","fields":[{"kind":"FormField","type":"textarea","name":"screeningNotes","label":"Resume Notes"},{"kind":"FormField","type":"text","name":"phoneScreenSummary","label":"Phone Screen Summary"},{"kind":"FormField","type":"select","name":"decision","label":"Decision","options":[{"label":"SHORTLIST","value":"SHORTLIST"},{"label":"REJECT","value":"REJECT"}]},{"kind":"WorkflowActionButton","label":"Submit Screening","action":"workflow.transition","variant":"primary"}]}]}', 'V19', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:cand-screen' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Interview Feedback","fields":[{"kind":"FormField","type":"text","name":"interviewerId","label":"Interviewer"},{"kind":"FormField","type":"text","name":"technicalRating","label":"Technical Rating (1-5)"},{"kind":"FormField","type":"text","name":"communicationRating","label":"Communication (1-5)"},{"kind":"FormField","type":"text","name":"cultureFitRating","label":"Culture Fit (1-5)"},{"kind":"FormField","type":"textarea","name":"strengths","label":"Strengths"},{"kind":"FormField","type":"textarea","name":"concerns","label":"Concerns"},{"kind":"WorkflowActionButton","label":"Save Interview Feedback","action":"workflow.transition","variant":"primary"}]}]}', 'V19', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:cand-interview' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Offer / Reject Decision","fields":[{"kind":"FormField","type":"text","name":"offeredSalary","label":"Offered Salary"},{"kind":"FormField","type":"text","name":"joiningDate","label":"Joining Date"},{"kind":"FormField","type":"textarea","name":"offerNotes","label":"Notes"},{"kind":"FormField","type":"select","name":"decision","label":"Decision","options":[{"label":"OFFER","value":"OFFER"},{"label":"REJECT","value":"REJECT"}]},{"kind":"WorkflowActionButton","label":"Submit Decision","action":"workflow.transition","variant":"primary"}]}]}', 'V19', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:cand-offer' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Onboarding — Convert to Employee","fields":[{"kind":"FormField","type":"text","name":"actualJoiningDate","label":"Actual Joining Date"},{"kind":"FormField","type":"text","name":"reportingTo","label":"Reports To"},{"kind":"FormField","type":"text","name":"shiftCode","label":"Initial Shift"},{"kind":"FormField","type":"select","name":"status","label":"Candidate Status","options":[{"label":"Accepted joining","value":"ACCEPTED"},{"label":"Declined offer","value":"DECLINED"}]},{"kind":"WorkflowActionButton","label":"Confirm Onboarding","action":"workflow.transition","variant":"primary"}]}]}', 'V19', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:cand-onboard' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"New Candidate","fields":[{"kind":"FormField","type":"text","name":"fullName","label":"Full Name"},{"kind":"FormField","type":"text","name":"contactPhone","label":"Phone"},{"kind":"FormField","type":"text","name":"contactEmail","label":"Email"},{"kind":"FormField","type":"select","name":"appliedDesignationCode","label":"Applied Designation","options":[{"label":"Operator","value":"OPR"},{"label":"Technician","value":"TEC"},{"label":"Supervisor","value":"SUP"},{"label":"Accountant","value":"ACC"},{"label":"Clerk","value":"CLR"},{"label":"Security Guard","value":"GUR"},{"label":"Driver","value":"DRV"}]},{"kind":"FormField","type":"select","name":"source","label":"Source","options":[{"label":"REFERRAL","value":"REFERRAL"},{"label":"LINKEDIN","value":"LINKEDIN"},{"label":"NAUKRI","value":"NAUKRI"},{"label":"WALK_IN","value":"WALK_IN"},{"label":"CAMPUS","value":"CAMPUS"},{"label":"CONSULTANT","value":"CONSULTANT"}]},{"kind":"FormField","type":"number","name":"experienceYears","label":"Experience (years)"},{"kind":"FormField","type":"number","name":"expectedSalary","label":"Expected Salary"},{"kind":"WorkflowActionButton","label":"Save Candidate","action":"save","variant":"primary"}]}]}', 'V19', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:cand-create' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Designation","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","code","title","band","active"]},"columns":[{"field":"code","label":"Code","sticky":"left","sortable":true},{"field":"title","label":"Title"},{"field":"band","label":"Band","render":"badge","colorMap":{"M3":"red","M2":"amber","M1":"amber","S2":"blue","S1":"blue","W3":"green","W2":"green","W1":"grey"}},{"field":"active","label":"Active","render":"badge","colorMap":{"1":"green","0":"red"}}],"sort":{"default":[{"field":"band","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Designations"}}', 'V19', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:designation-master' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code IN ('pico:cand-list','pico:cand-create','pico:cand-screen','pico:cand-interview','pico:cand-offer','pico:cand-onboard','pico:designation-master') AND current_version_id IS NULL;
