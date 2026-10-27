-- V388: PICO (Spokes) Module 7 -- reports, branding / profile, announcements, password reset,
-- WhatsApp attendance digest. Requires V382-V387.
--
-- ERPNext source (apps/):
--   custom_theme   6 script reports, Company Branding, Theme Preference, My Preferences page,
--                  desk CSS / JS, boot-session hooks
--   spokes_app     api/pico_api.py: login / validate_token / refresh_token, send_reset_otp /
--                  verify_reset_otp / reset_password, user_profile / update_user_profile,
--                  company_branding, get_notifications ("Notifications" DocType);
--                  attendance_report.py (Twilio WhatsApp); user.py (company-scoped users)
--
-- Where each piece lands:
--   reports            Customer / Checkin / Expense / Installation reports = list pages of Modules 1-6;
--                      Lead and Leave reports added here (pico:lead-report, pico:leave-report)
--   Theme Preference   already in Aria: /api/account/preferences (user_ui_preference) -- nothing new
--   Company Branding   tenant settings company_name (existing) + branding.logo_url (new key)
--   profile            /api/account/profile[/photo] (existing) + pico.my-profile for m7_employee fields
--   Notifications      m7_announcement (new) + pico.my-notifications (also pending approval counts)
--   password reset     /api/auth/password-reset/* (PasswordResetController), m0_password_reset (new)
--   WhatsApp           TWILIO_WHATSAPP connector + adapter, PicoAttendanceDigest (daily) and
--                      pico.whatsapp-attendance-report (on demand)
--   login / tokens     Aria JWT (/api/auth/signin, /refreshToken) -- nothing new
--   user.py            Aria tenants already isolate users per company -- nothing new
--   desk CSS / JS      dropped (Aria has its own frontend)
--
-- Tenant settings: branding.logo_url, whatsapp.attendance_report_to (comma-separated E.164 numbers),
-- whatsapp.attendance_report_enabled (true / false, default false).
--
-- Idempotent -- safe to (re)run on any database.

-- ============================================================
-- 1. ANNOUNCEMENTS (ERPNext custom DocType "Notifications": title, description, is_active)
-- ============================================================

CREATE TABLE IF NOT EXISTS m7_announcement (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  title VARCHAR(200) NOT NULL,
  body TEXT NOT NULL COMMENT 'Plain text; the app renders it as text, never as HTML',
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  publish_from DATE DEFAULT NULL COMMENT 'NULL = immediately',
  publish_until DATE DEFAULT NULL COMMENT 'NULL = until deactivated',
  created_by_user_id BIGINT DEFAULT NULL COMMENT 'employee_details.id; NULL = imported',
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Notifications name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_m7an_active (tenant_id, is_active, publish_from),
  CONSTRAINT chk_m7an_dates CHECK (publish_until IS NULL OR publish_from IS NULL OR publish_until >= publish_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, read_role, write_role, value_column, display_column, lifecycle_kind)
SELECT -1, 'Announcement', 'Announcement', 'm7_announcement', 'Company notices shown in the mobile app (ERPNext "Notifications").', 'HR',
       '*', 'Manager,TenantAdmin', 'id', 'title', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'Announcement');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT * FROM (
  SELECT -1 a,'Announcement' b,'title' c,'Title' d,'STRING' e,1 f,1 g,1 h,1 i,'text' j,NULL k,NULL l UNION ALL
  SELECT -1,'Announcement','body','Message','TEXT',0,0,1,1,'textarea',NULL,NULL UNION ALL
  SELECT -1,'Announcement','is_active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'Announcement','publish_from','Show From','DATE',1,1,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'Announcement','publish_until','Show Until','DATE',1,1,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'Announcement','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- ============================================================
-- 2. PASSWORD RESET (replaces send_reset_otp / verify_reset_otp / reset_password)
-- ============================================================
-- ERPNext: 4-digit code in plain text on User, unlimited guesses, and reset_password only checked
-- an "otp_verified" flag per email -- a brute-forced code reset any account. Here one row per
-- request, code stored only as salted SHA-256, 15-minute expiry, 5 attempts, and the new password
-- is set in the SAME call that proves the code (no separate "verified" state to abuse).

CREATE TABLE IF NOT EXISTS m0_password_reset (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL COMMENT 'employee_details.id',
  code_hash CHAR(64) NOT NULL COMMENT 'SHA-256(salt:code:id), hex',
  code_salt CHAR(32) NOT NULL,
  expires_at DATETIME NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  used_at DATETIME DEFAULT NULL COMMENT 'Password changed with this code; NULL = unused',
  requested_ip VARCHAR(64) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_m0pr_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
-- Deliberately not in entity_registry: reset codes are never readable through the generic API.

-- ============================================================
-- 3. WHATSAPP CONNECTOR (Twilio) -- replaces attendance_report.py's direct Twilio calls
-- ============================================================

INSERT INTO integration_connector_catalog
  (code, version, category, vendor, display_name, description, config_schema_json, default_props_json,
   supported_operations, auth_methods, dependencies, pii_paths_json, residency_constraints, docs_url, status)
SELECT 'TWILIO_WHATSAPP', 1, 'MESSAGING', 'Twilio', 'Twilio WhatsApp',
       'Outbound WhatsApp text messages through the Twilio Messages API (sendMessage {to, body}).',
       '{"type":"object","properties":{"accountSid":{"type":"string"},"from":{"type":"string","description":"whatsapp:+E164 sender approved in Twilio"},"dryRun":{"type":"boolean"}},"required":["accountSid","from"]}',
       '{"dryRun":false}',
       '["sendMessage"]',
       '["BASIC"]',
       '[]',
       '["payload.to","payload.body"]',
       'NONE', 'https://www.twilio.com/docs/whatsapp/api', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM integration_connector_catalog WHERE code = 'TWILIO_WHATSAPP' AND version = 1);

-- ============================================================
-- 4. ACTIONS
-- ============================================================

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT * FROM (
  SELECT 'pico.my-profile' a, 'My Profile' b, 'hr' c,
         '{"type":"object","properties":{"op":{"enum":["get","update","branding"]},"mobile_primary":{"type":"string"},"email_personal":{"type":"string"},"emergency_contact_name":{"type":"string"},"emergency_contact_phone":{"type":"string"},"emergency_contact_relation":{"type":"string"},"photo_url":{"type":"string"}},"note":"Read from args. Own record only. HR-owned fields (designation, DOB, gender, bank, PAN) are read-only here."}' d,
         1 e, NULL f,
         'Profile (login + employee master) and company branding for the app (replaces user_profile, update_user_profile, company_branding). Photo upload stays on /api/account/profile/photo.' g
  UNION ALL
  SELECT 'pico.my-notifications', 'My Notifications', 'hr',
         '{"type":"object","properties":{"announcement_id":{"type":"integer"}},"note":"Read from args."}',
         1, NULL,
         'Active announcements of the tenant plus counts of approvals waiting for the caller (replaces get_notifications).'
  UNION ALL
  SELECT 'pico.whatsapp-attendance-report', 'Send Attendance on WhatsApp', 'hr',
         '{"type":"object","properties":{"date":{"type":"string","format":"date"},"to":{"type":"array","items":{"type":"string"}}},"note":"Read from args. Manager / TenantAdmin. Defaults: yesterday, whatsapp.attendance_report_to."}',
         1, 'Manager,TenantAdmin',
         'Sends the day''s attendance summary as WhatsApp text through the TWILIO_WHATSAPP integration (replaces send_attendance_report_to_whatsapp).'
) x
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = x.a);

-- ============================================================
-- 5. PAGES -- the two ERPNext reports Modules 1-6 did not already cover, + announcements
-- ============================================================

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT * FROM (
  SELECT -1 a,'pico:lead-report' b,'Lead Report' c,'Leads with owner, sales person and status (ERPNext custom_theme Lead Report).' d,'CRM' e,'PUBLISHED' f,NOW() g,NOW() h UNION ALL
  SELECT -1,'pico:leave-report','Leave Application Report','Leave applications with department and status (ERPNext custom_theme Leave Application Report).','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:announcement-list','Announcements','Company notices shown in the mobile app.','HR','PUBLISHED',NOW(),NOW()
) x
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = x.b);

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', b.body, NOW()
FROM page_definition pd
JOIN (
  SELECT 'pico:lead-report' code,
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Lead","joins":[{"entity":"EmployeeMaster","as":"sp","on":"m8_lead.sales_person_emp_id = sp.id"},{"entity":"EmployeeMaster","as":"ow","on":"m8_lead.owner_id = ow.id"}],"select":["m8_lead.id as id","m8_lead.lead_number as lead_number","m8_lead.lead_name as lead_name","m8_lead.company_name as company_name","m8_lead.stage as stage","m8_lead.qualification_status as qualification_status","ow.full_name as lead_owner","sp.full_name as sales_person","m8_lead.contact_email as contact_email","m8_lead.contact_phone as contact_phone","m8_lead.territory as territory","m8_lead.city_town as city_town","m8_lead.created_at as created_at"]},"columns":[{"field":"lead_number","label":"Lead","sticky":"left"},{"field":"lead_name","label":"Lead Name"},{"field":"company_name","label":"Company"},{"field":"stage","label":"Status","render":"badge"},{"field":"qualification_status","label":"Qualification","render":"badge"},{"field":"lead_owner","label":"Lead Owner"},{"field":"sales_person","label":"Sales Person"},{"field":"contact_email","label":"Email"},{"field":"contact_phone","label":"Mobile"},{"field":"territory","label":"Territory"},{"field":"city_town","label":"City"},{"field":"created_at","label":"Created","render":"datetime","sortable":true}],"filters":[{"field":"stage","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["NEW","CONTACTED","QUALIFIED","QUOTED","WON","LOST"]}},{"field":"qualification_status","label":"Qualification","type":"multi-select","optionsSource":{"type":"static","options":["UNQUALIFIED","IN_PROCESS","QUALIFIED"]}}],"sort":{"default":[{"field":"created_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":100},"header":{"title":"Lead Report"}}' body
  UNION ALL
  SELECT 'pico:leave-report',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"LeaveRequest","joins":[{"entity":"EmployeeMaster","as":"e","on":"m7_leave_request.employee_id = e.id"}],"select":["m7_leave_request.id as id","m7_leave_request.lr_number as lr_number","e.emp_code as emp_code","e.full_name as full_name","e.department as department","m7_leave_request.leave_kind as leave_kind","m7_leave_request.start_date as start_date","m7_leave_request.end_date as end_date","m7_leave_request.days as days","m7_leave_request.is_half_day as is_half_day","m7_leave_request.status as status","m7_leave_request.cancel_status as cancel_status"]},"columns":[{"field":"lr_number","label":"Leave #","sticky":"left"},{"field":"emp_code","label":"Emp Code"},{"field":"full_name","label":"Employee"},{"field":"department","label":"Department"},{"field":"leave_kind","label":"Type","render":"badge"},{"field":"start_date","label":"From","render":"date","sortable":true},{"field":"end_date","label":"To","render":"date"},{"field":"days","label":"Days","render":"number","align":"right"},{"field":"is_half_day","label":"Half Day","render":"badge"},{"field":"status","label":"Status","render":"badge","colorMap":{"PENDING":"amber","APPROVED":"green","REJECTED":"red","WITHDRAWN":"gray","CANCELLED":"gray"}},{"field":"cancel_status","label":"Cancellation","render":"badge"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["PENDING","APPROVED","REJECTED","WITHDRAWN","CANCELLED"]}}],"sort":{"default":[{"field":"start_date","dir":"desc"}]},"pagination":{"kind":"server","pageSize":100},"header":{"title":"Leave Application Report"}}'
  UNION ALL
  SELECT 'pico:announcement-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Announcement","select":["id","title","is_active","publish_from","publish_until","created_at"]},"columns":[{"field":"title","label":"Title","sticky":"left"},{"field":"is_active","label":"Active","render":"badge"},{"field":"publish_from","label":"From","render":"date"},{"field":"publish_until","label":"Until","render":"date"},{"field":"created_at","label":"Created","render":"datetime","sortable":true}],"sort":{"default":[{"field":"created_at","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Announcements"}}'
) b ON b.code = pd.code
WHERE pd.tenant_id = -1
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id AND pv.version = 1);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1
  AND pd.code IN ('pico:lead-report','pico:leave-report','pico:announcement-list')
  AND pd.current_version_id IS NULL;
