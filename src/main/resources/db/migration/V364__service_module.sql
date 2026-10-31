-- Service Module: ERPNext's pinned doctypes here are "Installation Report"
-- (a custom doctype, not stock ERPNext -- read directly from the
-- custom_theme Frappe app's installation_report.json) and "Notifications"
-- (a simple internal notice board). aria's existing ServiceTicket/
-- AmcContract (category M9) stay exactly as they are -- not part of this
-- screenshot's Service module, out of scope per the approved plan.

-- ===================== InstallationReport =====================
CREATE TABLE IF NOT EXISTS m9_installation_report (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  doc_no VARCHAR(32) DEFAULT NULL,
  date DATE NOT NULL,
  company VARCHAR(64) NOT NULL,
  employee_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  nature_of_call VARCHAR(128) DEFAULT NULL,
  machine_type VARCHAR(64) DEFAULT NULL,
  machine_model VARCHAR(64) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_installation_report_employee (employee_id),
  KEY idx_installation_report_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'InstallationReport', 'Installation Report', 'm9_installation_report', 'Field-service visit record (matches the custom ERPNext doctype 1:1).', 'M9', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'InstallationReport');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'InstallationReport',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk,c.rel
FROM (SELECT 1 ord,'doc_no' fc,'Doc No' lbl,'STRING' dt,1 filt,1 sort,1 srch,0 req,'text' wk,NULL rel
      UNION ALL SELECT 2,'date','Date','DATE',0,1,0,1,'date',NULL
      UNION ALL SELECT 3,'company','Company','STRING',1,1,0,1,'text',NULL
      UNION ALL SELECT 4,'employee_id','Employee','LONG',1,1,0,1,'entity-ref','Employee'
      UNION ALL SELECT 5,'customer_id','Customer','LONG',1,1,0,1,'entity-ref','Customer'
      UNION ALL SELECT 6,'nature_of_call','Nature Of Call','STRING',0,0,1,0,'text',NULL
      UNION ALL SELECT 7,'machine_type','Machine Type','STRING',1,0,1,0,'text',NULL
      UNION ALL SELECT 8,'machine_model','Machine Model','STRING',0,0,1,0,'text',NULL) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='InstallationReport' AND field_code=c.fc);

-- ===================== Notifications =====================
CREATE TABLE IF NOT EXISTS m9_notification_board (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  title VARCHAR(128) NOT NULL,
  company VARCHAR(64) DEFAULT NULL,
  description TEXT,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'Notifications', 'Notifications', 'm9_notification_board', 'Internal notice board (matches the custom ERPNext doctype 1:1).', 'M9', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'Notifications');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'Notifications',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk
FROM (SELECT 1 ord,'title' fc,'Title' lbl,'STRING' dt,1 filt,1 sort,1 srch,1 req,'text' wk
      UNION ALL SELECT 2,'company','Company','STRING',1,1,0,0,'text'
      UNION ALL SELECT 3,'description','Description','TEXT',0,0,1,0,'textarea'
      UNION ALL SELECT 4,'is_active','Is Active','BOOLEAN',1,1,0,0,'checkbox') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Notifications' AND field_code=c.fc);

-- ===================== Service Module pages =====================
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:svm-installation-report-list', 'Installation Reports', 'Field-service visit reports (Service Module).', 'Service Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:svm-installation-report-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "InstallationReport", "select": ["doc_no", "date", "employee_id", "customer_id", "nature_of_call", "machine_type", "machine_model", "company"]}, "columns": [], "filters": [{"field": "date", "label": "From/To", "type": "date-range", "applyTo": ["date"]}, {"field": "employee_id", "label": "Employee", "type": "equals", "applyTo": ["employee_id"]}, {"field": "customer_id", "label": "Customer", "type": "equals", "applyTo": ["customer_id"]}, {"field": "company", "label": "Company", "type": "equals", "applyTo": ["company"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:svm-installation-report-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:svm-installation-report-list' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:svm-notifications-list', 'Notifications List', 'Internal notice board (Service Module).', 'Service Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:svm-notifications-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "Notifications", "select": ["title", "company", "description", "is_active"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Notifications", "type": "text-contains", "applyTo": ["title", "description"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:svm-notifications-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:svm-notifications-list' AND pd.current_version_id IS NULL;
