-- Sales Module: build the missing Customer master (confirmed to not exist
-- anywhere in aria -- every Sales Order/Invoice just carries customer_name
-- as free text), and extend Lead with the 4 columns the real "Lead Report"
-- needs that aria's Lead doesn't have yet. Both are additive; m8_lead's
-- existing columns/rows/pages are untouched.

-- ===================== Customer =====================
CREATE TABLE IF NOT EXISTS m8_customer (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  customer_name VARCHAR(128) NOT NULL,
  customer_group VARCHAR(64) DEFAULT NULL,
  territory VARCHAR(64) DEFAULT NULL,
  customer_type VARCHAR(16) DEFAULT NULL,
  mobile_no VARCHAR(20) DEFAULT NULL,
  email_id VARCHAR(128) DEFAULT NULL,
  disabled TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'Customer', 'Customer', 'm8_customer', 'Customer master referenced by Sales/Service documents.', 'CRM', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'Customer');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'Customer',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk
FROM (SELECT 1 ord,'customer_name' fc,'Customer Name' lbl,'STRING' dt,1 filt,1 sort,1 srch,1 req,'text' wk
      UNION ALL SELECT 2,'customer_group','Customer Group','STRING',1,1,1,0,'text'
      UNION ALL SELECT 3,'territory','Territory','STRING',1,1,1,0,'text'
      UNION ALL SELECT 4,'customer_type','Customer Type','STRING',1,1,0,0,'select'
      UNION ALL SELECT 5,'mobile_no','Mobile No','STRING',0,0,1,0,'text'
      UNION ALL SELECT 6,'email_id','Email','STRING',0,0,1,0,'text'
      UNION ALL SELECT 7,'disabled','Disabled','BOOLEAN',1,1,0,0,'checkbox') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Customer' AND field_code=c.fc);

UPDATE entity_field_registry SET options_json = '["Company","Individual","Partnership"]' WHERE entity_code='Customer' AND field_code='customer_type' AND options_json IS NULL;

-- ===================== Lead: add the 4 columns "Lead Report" needs =====================
ALTER TABLE m8_lead
  ADD COLUMN IF NOT EXISTS company_name VARCHAR(128) DEFAULT NULL AFTER lead_name,
  ADD COLUMN IF NOT EXISTS city VARCHAR(64) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS state VARCHAR(64) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS country VARCHAR(64) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'Lead',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk
FROM (SELECT 1 ord,'company_name' fc,'Organization Name' lbl,'STRING' dt,1 filt,1 sort,1 srch,0 req,'text' wk
      UNION ALL SELECT 2,'city','City','STRING',1,1,1,0,'text'
      UNION ALL SELECT 3,'state','State','STRING',1,1,0,0,'text'
      UNION ALL SELECT 4,'country','Country','STRING',1,1,0,0,'text') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Lead' AND field_code=c.fc);

-- ===================== Sales Module pages =====================
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:slm-customer-list', 'Customer Report', 'Customer master (Sales Module).', 'Sales Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:slm-customer-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "Customer", "select": ["customer_name", "customer_group", "territory", "customer_type", "mobile_no", "email_id", "disabled"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Customer", "type": "text-contains", "applyTo": ["customer_name", "mobile_no", "email_id"]}, {"field": "customer_group", "label": "Customer Group", "type": "equals", "applyTo": ["customer_group"]}, {"field": "territory", "label": "Territory", "type": "equals", "applyTo": ["territory"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:slm-customer-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:slm-customer-list' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:slm-lead-list', 'Lead Report', 'Leads (Sales Module).', 'Sales Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:slm-lead-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "Lead", "select": ["lead_name", "status", "owner_id", "contact_email", "contact_phone", "company_name", "city", "state", "country"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Lead", "type": "text-contains", "applyTo": ["lead_name", "contact_email", "contact_phone"]}, {"field": "status", "label": "Status", "type": "equals", "applyTo": ["status"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:slm-lead-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:slm-lead-list' AND pd.current_version_id IS NULL;
