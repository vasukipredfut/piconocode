-- V386: PICO (Spokes) Module 5 -- CRM: Lead, Customer, Quotation. Requires V382-V385.
--
-- ERPNext source (apps/spokes_app/spokes_app):
--   api/pico_api.py   create_lead, send_lead_notification_email, get_leads, get_my_leads,
--                     update_lead_qualification_status, get_all_lead_tracking_history,
--                     convert_lead_to_customer, create_quotation_from_lead, create_quotation,
--                     send_quotation_email, get_quotations, get_customers, get_sales_persons, get_items
--   DocTypes          Lead Owner (company -> owner); ERPNext Lead / Customer / Quotation / Item +
--                     custom Lead City (city -> sales person) and Sales Person
--   migrate_sap_to_erp.py   NOT migrated (see module-05-crm/README.md)
--
-- What Aria already had: m8_lead + M8.Lead and m8_quote + M8.Quote (states only; NEW / CONTACTED /
-- DRAFT steps assigned to the M10 role "Dispatch"; no quote lines; nothing writes state back to
-- the tables). Aria has NO customer master (m6_sales_invoice keeps customer_name text), so
-- m8_customer is new. Items reuse m5_sku_master (SkuMaster).
--
-- Delivers:
--   m7_employee        + is_sales_person
--   m5_sku_master      + machine_id (ERPNext Item.custom_machine_id)
--   m8_lead            + company / territory / city / sales person / creator / qualification /
--                        customer link / lost reason / closed_at / erp_ref
--   m8_quote           + quote_to, customer_id, created_by, quote_date, subtotal / tax, sent_at, ...
--   new                m8_lead_city, m8_customer, m8_quote_line
--   workflows          M8.Lead v2, M8.Quote v2 (roles fixed, PICO shortcuts added)
--   projections        M8.Lead -> m8_lead, M8.Quote -> m8_quote
--   actions            pico.lead-create, pico.lead-update, pico.lead-convert, pico.quote-create, pico.my-crm
--   Java               CrmAccessValidator (who may move a lead / quote), CrmMails
--   pages              pico:lead-qualify, pico:lead-quoted, pico:lead-convert, pico:lead-lost,
--                      pico:customer-list, pico:lead-city-list, pico:quote-line-list
--
-- Tenant setting: crm.lead_owner_emp_id = m7_employee.id notified of every new lead (ERPNext Lead Owner).
--
-- Idempotent -- safe to (re)run on any database.

-- ============================================================
-- 1. COLUMNS ON EXISTING TABLES
-- ============================================================

ALTER TABLE m7_employee
  ADD COLUMN IF NOT EXISTS is_sales_person TINYINT(1) NOT NULL DEFAULT 0
    COMMENT '1 = may be assigned leads / appears in the sales person list (ERPNext Sales Person)';

ALTER TABLE m5_sku_master
  ADD COLUMN IF NOT EXISTS machine_id VARCHAR(64) DEFAULT NULL
    COMMENT 'Machine model id shown on quotations (ERPNext Item.custom_machine_id); NULL = not a machine';

ALTER TABLE m8_lead
  ADD COLUMN IF NOT EXISTS company_name VARCHAR(256) DEFAULT NULL COMMENT 'Organisation of the lead; lead_name is the person',
  ADD COLUMN IF NOT EXISTS territory VARCHAR(64) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS city_town VARCHAR(64) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sales_person_emp_id BIGINT DEFAULT NULL COMMENT 'm7_employee.id following the lead up; NULL = unassigned',
  ADD COLUMN IF NOT EXISTS created_by_emp_id BIGINT DEFAULT NULL COMMENT 'm7_employee.id who entered the lead; NULL = imported',
  ADD COLUMN IF NOT EXISTS qualification_status VARCHAR(16) NOT NULL DEFAULT 'UNQUALIFIED',
  ADD COLUMN IF NOT EXISTS customer_id BIGINT DEFAULT NULL COMMENT 'm8_customer.id once converted; NULL = not converted',
  ADD COLUMN IF NOT EXISTS lost_reason VARCHAR(512) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS notes VARCHAR(1024) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS closed_at DATETIME DEFAULT NULL COMMENT 'WON / LOST time; NULL = open',
  ADD COLUMN IF NOT EXISTS erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Lead name for migrated rows; NULL = created in Aria',
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_lead_sp ON m8_lead (tenant_id, sales_person_emp_id, stage);
CREATE INDEX IF NOT EXISTS idx_lead_creator ON m8_lead (tenant_id, created_by_emp_id);
CREATE INDEX IF NOT EXISTS idx_lead_wf ON m8_lead (tenant_id, workflow_instance_id);
CREATE INDEX IF NOT EXISTS idx_lead_phone ON m8_lead (tenant_id, contact_phone);

ALTER TABLE m8_quote
  ADD COLUMN IF NOT EXISTS quote_to VARCHAR(16) NOT NULL DEFAULT 'LEAD' COMMENT 'LEAD | CUSTOMER',
  ADD COLUMN IF NOT EXISTS customer_id BIGINT DEFAULT NULL COMMENT 'm8_customer.id when quote_to=CUSTOMER; NULL = quoted to a lead',
  ADD COLUMN IF NOT EXISTS created_by_emp_id BIGINT DEFAULT NULL COMMENT 'm7_employee.id; NULL = imported',
  ADD COLUMN IF NOT EXISTS quote_date DATE DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS subtotal DECIMAL(14,2) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS tax_total DECIMAL(14,2) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS notes VARCHAR(1024) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sent_at DATETIME DEFAULT NULL COMMENT 'Stamped by the projection on SENT',
  ADD COLUMN IF NOT EXISTS decided_at DATETIME DEFAULT NULL COMMENT 'ACCEPTED / REJECTED / EXPIRED time; NULL = open',
  ADD COLUMN IF NOT EXISTS emailed_to VARCHAR(256) DEFAULT NULL COMMENT 'Address the quotation mail went to; NULL = no email on file',
  ADD COLUMN IF NOT EXISTS erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Quotation name for migrated rows; NULL = created in Aria',
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_quote_lead ON m8_quote (tenant_id, lead_id);
CREATE INDEX IF NOT EXISTS idx_quote_customer ON m8_quote (tenant_id, customer_id);
CREATE INDEX IF NOT EXISTS idx_quote_wf ON m8_quote (tenant_id, workflow_instance_id);

-- ============================================================
-- 2. NEW TABLES
-- ============================================================

-- ERPNext "Lead City": a lead from this city goes to this sales person unless one is chosen.
CREATE TABLE IF NOT EXISTS m8_lead_city (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  city_name VARCHAR(64) NOT NULL,
  sales_person_emp_id BIGINT DEFAULT NULL COMMENT 'm7_employee.id; NULL = no automatic assignment',
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m8lc_city (tenant_id, city_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS m8_customer (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  customer_code VARCHAR(32) NOT NULL,
  customer_name VARCHAR(256) NOT NULL,
  customer_type VARCHAR(16) NOT NULL DEFAULT 'COMPANY',
  contact_name VARCHAR(128) DEFAULT NULL,
  mobile_no VARCHAR(32) DEFAULT NULL,
  email_id VARCHAR(128) DEFAULT NULL,
  gstin VARCHAR(16) DEFAULT NULL,
  address VARCHAR(512) DEFAULT NULL,
  territory VARCHAR(64) DEFAULT NULL,
  city_town VARCHAR(64) DEFAULT NULL,
  sales_person_emp_id BIGINT DEFAULT NULL COMMENT 'm7_employee.id; NULL = unassigned',
  lead_id BIGINT DEFAULT NULL COMMENT 'm8_lead.id it was converted from; NULL = created directly / imported',
  created_by_emp_id BIGINT DEFAULT NULL COMMENT 'm7_employee.id; NULL = imported',
  source VARCHAR(16) NOT NULL DEFAULT 'MANUAL',
  sap_customer_code VARCHAR(32) DEFAULT NULL COMMENT 'SAP KNA1 customer number for synced rows; NULL = not in SAP',
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Customer name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m8c_code (tenant_id, customer_code),
  UNIQUE KEY uq_m8c_lead (tenant_id, lead_id),
  KEY idx_m8c_name (tenant_id, customer_name),
  KEY idx_m8c_sp (tenant_id, sales_person_emp_id),
  CONSTRAINT chk_m8c_type CHECK (customer_type IN ('COMPANY','INDIVIDUAL')),
  CONSTRAINT chk_m8c_source CHECK (source IN ('LEAD','MANUAL','SAP','IMPORT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS m8_quote_line (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  quote_id BIGINT NOT NULL,
  sku_code VARCHAR(64) NOT NULL COMMENT 'm5_sku_master.sku_code',
  item_name VARCHAR(256) DEFAULT NULL COMMENT 'Snapshot at quoting time',
  machine_id VARCHAR(64) DEFAULT NULL COMMENT 'Snapshot of m5_sku_master.machine_id',
  qty DECIMAL(12,3) NOT NULL,
  uom VARCHAR(16) DEFAULT NULL,
  rate DECIMAL(14,2) NOT NULL,
  gst_rate_pct DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  amount DECIMAL(14,2) NOT NULL COMMENT 'qty x rate',
  tax_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_m8ql_quote (quote_id),
  CONSTRAINT fk_m8ql_quote FOREIGN KEY (quote_id) REFERENCES m8_quote (id) ON DELETE CASCADE,
  CONSTRAINT chk_m8ql_qty CHECK (qty > 0),
  CONSTRAINT chk_m8ql_rate CHECK (rate >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- ============================================================
-- 3. ENTITY REGISTRY
-- ============================================================
-- ERPNext served leads, quotations and customers to anyone (allow_guest=True). Lead / Quote were
-- readable by every logged-in user here too ('*'); restrict them to sales + management.

UPDATE entity_registry SET read_role = 'Sales,Manager,TenantAdmin'
WHERE code IN ('Lead','Quote') AND (read_role IS NULL OR read_role = '*');

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, read_role, write_role, value_column, display_column, lifecycle_kind)
SELECT * FROM (
  SELECT -1 a,'Customer' b,'Customer' c,'m8_customer' d,'Customer master (Aria had none; ERPNext Customer).' e,'CRM' f,'Sales,Manager,TenantAdmin' g,'Sales,Manager,TenantAdmin' h,'id' i,'customer_name' j,'DEFINED' k UNION ALL
  SELECT -1,'LeadCity','Lead City','m8_lead_city','City -> default sales person for new leads (ERPNext Lead City).','CRM','Sales,Manager,TenantAdmin','Manager,TenantAdmin','city_name','city_name','DEFINED' UNION ALL
  SELECT -1,'QuoteLine','Quote Line','m8_quote_line','Item line of a quotation.','CRM','Sales,Manager,TenantAdmin','TenantAdmin',NULL,NULL,'DEFINED'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = x.b);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT * FROM (
  SELECT -1 a,'EmployeeMaster' b,'is_sales_person' c,'Sales Person' d,'BOOLEAN' e,1 f,1 g,0 h,0 i,'checkbox' j,NULL k,NULL l UNION ALL
  SELECT -1,'SkuMaster','machine_id','Machine ID','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  -- Lead (existing + new columns)
  SELECT -1,'Lead','lead_number','Lead #','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Lead','lead_name','Lead Name','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'Lead','company_name','Company','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Lead','contact_phone','Mobile','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Lead','contact_email','Email','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Lead','source','Source','STRING',1,1,0,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Lead','estimated_value','Estimated Value','DECIMAL',0,1,0,0,'currency',NULL,NULL UNION ALL
  SELECT -1,'Lead','territory','Territory','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Lead','city_town','City / Town','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Lead','stage','Stage','STRING',1,1,0,0,'select',NULL,'["NEW","CONTACTED","QUALIFIED","QUOTED","WON","LOST"]' UNION ALL
  SELECT -1,'Lead','qualification_status','Qualification','STRING',1,1,0,0,'select',NULL,'["UNQUALIFIED","IN_PROCESS","QUALIFIED"]' UNION ALL
  SELECT -1,'Lead','sales_person_emp_id','Sales Person','RELATION',1,1,0,0,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'Lead','created_by_emp_id','Created By','RELATION',1,1,0,0,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'Lead','customer_id','Customer','RELATION',1,0,0,0,'entity-ref','Customer','{"entity":"Customer","valueField":"id","labelField":"customer_name"}' UNION ALL
  SELECT -1,'Lead','next_action_on','Next Action','DATE',1,1,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'Lead','lost_reason','Lost Reason','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'Lead','notes','Notes','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'Lead','closed_at','Closed At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'Lead','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- Quote (existing + new columns)
  SELECT -1,'Quote','quote_number','Quote #','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Quote','quote_to','Quote To','STRING',1,1,0,1,'select',NULL,'["LEAD","CUSTOMER"]' UNION ALL
  SELECT -1,'Quote','lead_id','Lead','RELATION',1,0,0,0,'entity-ref','Lead','{"entity":"Lead","valueField":"id","labelField":"lead_name"}' UNION ALL
  SELECT -1,'Quote','customer_id','Customer','RELATION',1,0,0,0,'entity-ref','Customer','{"entity":"Customer","valueField":"id","labelField":"customer_name"}' UNION ALL
  SELECT -1,'Quote','customer_name','Party Name','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'Quote','quote_date','Date','DATE',1,1,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'Quote','valid_until','Valid Until','DATE',1,1,0,0,'date',NULL,NULL UNION ALL
  SELECT -1,'Quote','subtotal','Subtotal','DECIMAL',0,1,0,0,'currency',NULL,NULL UNION ALL
  SELECT -1,'Quote','tax_total','Tax','DECIMAL',0,1,0,0,'currency',NULL,NULL UNION ALL
  SELECT -1,'Quote','total_value','Grand Total','DECIMAL',0,1,0,1,'currency',NULL,NULL UNION ALL
  SELECT -1,'Quote','status','Status','STRING',1,1,0,0,'select',NULL,'["DRAFT","SENT","ACCEPTED","REJECTED","EXPIRED"]' UNION ALL
  SELECT -1,'Quote','created_by_emp_id','Created By','RELATION',1,1,0,0,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'Quote','sent_at','Sent At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'Quote','emailed_to','Emailed To','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Quote','notes','Notes','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'Quote','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- Customer
  SELECT -1,'Customer','customer_code','Code','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'Customer','customer_name','Name','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'Customer','customer_type','Type','STRING',1,1,0,1,'select',NULL,'["COMPANY","INDIVIDUAL"]' UNION ALL
  SELECT -1,'Customer','contact_name','Contact','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Customer','mobile_no','Mobile','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Customer','email_id','Email','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Customer','gstin','GSTIN','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Customer','address','Address','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'Customer','territory','Territory','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Customer','city_town','City / Town','STRING',1,1,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Customer','sales_person_emp_id','Sales Person','RELATION',1,1,0,0,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'Customer','lead_id','From Lead','RELATION',1,0,0,0,'entity-ref','Lead','{"entity":"Lead","valueField":"id","labelField":"lead_name"}' UNION ALL
  SELECT -1,'Customer','created_by_emp_id','Created By','RELATION',1,1,0,0,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'Customer','source','Source','STRING',1,1,0,1,'select',NULL,'["LEAD","MANUAL","SAP","IMPORT"]' UNION ALL
  SELECT -1,'Customer','sap_customer_code','SAP Code','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'Customer','is_active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'Customer','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- LeadCity
  SELECT -1,'LeadCity','city_name','City','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'LeadCity','sales_person_emp_id','Sales Person','RELATION',1,1,0,0,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'LeadCity','is_active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  -- QuoteLine
  SELECT -1,'QuoteLine','quote_id','Quote','RELATION',1,1,0,1,'entity-ref','Quote','{"entity":"Quote","valueField":"id","labelField":"quote_number"}' UNION ALL
  SELECT -1,'QuoteLine','sku_code','Item','RELATION',1,1,1,1,'entity-ref','SkuMaster','{"entity":"SkuMaster","valueField":"sku_code","labelField":"sku_code"}' UNION ALL
  SELECT -1,'QuoteLine','item_name','Item Name','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'QuoteLine','machine_id','Machine ID','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'QuoteLine','qty','Qty','DECIMAL',0,1,0,1,'number',NULL,NULL UNION ALL
  SELECT -1,'QuoteLine','uom','UOM','STRING',0,0,0,0,'text',NULL,NULL UNION ALL
  SELECT -1,'QuoteLine','rate','Rate','DECIMAL',0,1,0,1,'currency',NULL,NULL UNION ALL
  SELECT -1,'QuoteLine','gst_rate_pct','GST %','DECIMAL',0,1,0,0,'number',NULL,NULL UNION ALL
  SELECT -1,'QuoteLine','amount','Amount','DECIMAL',0,1,0,1,'currency',NULL,NULL UNION ALL
  SELECT -1,'QuoteLine','tax_amount','Tax','DECIMAL',0,1,0,0,'currency',NULL,NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- ============================================================
-- 4. WORKFLOWS (v2: every v1 state, page and transition kept; roles fixed; PICO shortcuts added)
-- ============================================================
-- ERPNext leads are not a strict ladder: a lead can be quoted or converted from any open state.
--   pico:lead-qualify  NEW / CONTACTED -> QUALIFIED   (fired by pico.lead-update qualification=QUALIFIED)
--   pico:lead-quoted   NEW / CONTACTED / QUALIFIED -> QUOTED (fired by pico.quote-create)
--   pico:lead-convert  any open state -> WON          (fired by pico.lead-convert)
--   pico:lead-lost     any open state -> LOST         (fired by pico.lead-update stage=LOST)

INSERT INTO workflow_definition (tenant_id, code, display_name, version, status, body_json)
SELECT -1, 'M8.Lead', 'Lead', 2, 'PUBLISHED',
  '{"code":"M8.Lead","version":2,"initialState":"NEW","states":[{"code":"NEW","assignedRole":"Sales"},{"code":"CONTACTED","assignedRole":"Sales"},{"code":"QUALIFIED","assignedRole":"Sales"},{"code":"QUOTED","assignedRole":"Sales"},{"code":"WON","terminal":true},{"code":"LOST","terminal":true}],"transitions":[{"from":"NEW","to":"CONTACTED","trigger":"page.submitted","pageCode":"aria:lead-contact"},{"from":"CONTACTED","to":"QUALIFIED","trigger":"page.submitted","pageCode":"aria:lead-qualify"},{"from":"QUALIFIED","to":"QUOTED","trigger":"page.submitted","pageCode":"aria:lead-quote"},{"from":"QUOTED","to":"WON","trigger":"page.submitted","pageCode":"aria:lead-close","guard":"#submitted[''outcome''] == ''WON''"},{"from":"QUOTED","to":"LOST","trigger":"page.submitted","pageCode":"aria:lead-close","guard":"#submitted[''outcome''] == ''LOST''"},{"from":"NEW","to":"QUALIFIED","trigger":"page.submitted","pageCode":"pico:lead-qualify"},{"from":"CONTACTED","to":"QUALIFIED","trigger":"page.submitted","pageCode":"pico:lead-qualify"},{"from":"NEW","to":"QUOTED","trigger":"page.submitted","pageCode":"pico:lead-quoted"},{"from":"CONTACTED","to":"QUOTED","trigger":"page.submitted","pageCode":"pico:lead-quoted"},{"from":"QUALIFIED","to":"QUOTED","trigger":"page.submitted","pageCode":"pico:lead-quoted"},{"from":"NEW","to":"WON","trigger":"page.submitted","pageCode":"pico:lead-convert"},{"from":"CONTACTED","to":"WON","trigger":"page.submitted","pageCode":"pico:lead-convert"},{"from":"QUALIFIED","to":"WON","trigger":"page.submitted","pageCode":"pico:lead-convert"},{"from":"QUOTED","to":"WON","trigger":"page.submitted","pageCode":"pico:lead-convert"},{"from":"NEW","to":"LOST","trigger":"page.submitted","pageCode":"pico:lead-lost"},{"from":"CONTACTED","to":"LOST","trigger":"page.submitted","pageCode":"pico:lead-lost"},{"from":"QUALIFIED","to":"LOST","trigger":"page.submitted","pageCode":"pico:lead-lost"},{"from":"QUOTED","to":"LOST","trigger":"page.submitted","pageCode":"pico:lead-lost"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M8.Lead' AND version = 2);

INSERT INTO workflow_definition (tenant_id, code, display_name, version, status, body_json)
SELECT -1, 'M8.Quote', 'Quote', 2, 'PUBLISHED',
  '{"code":"M8.Quote","version":2,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Sales"},{"code":"SENT","assignedRole":"Sales"},{"code":"ACCEPTED","terminal":true},{"code":"REJECTED","terminal":true},{"code":"EXPIRED","terminal":true}],"transitions":[{"from":"DRAFT","to":"SENT","trigger":"page.submitted","pageCode":"aria:quote-send"},{"from":"SENT","to":"ACCEPTED","trigger":"page.submitted","pageCode":"aria:quote-outcome","guard":"#submitted[''outcome''] == ''ACCEPTED''"},{"from":"SENT","to":"REJECTED","trigger":"page.submitted","pageCode":"aria:quote-outcome","guard":"#submitted[''outcome''] == ''REJECTED''"},{"from":"SENT","to":"EXPIRED","trigger":"system.schedule"},{"from":"DRAFT","to":"EXPIRED","trigger":"system.schedule"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M8.Quote' AND version = 2);

-- ============================================================
-- 5. PROJECTIONS
-- ============================================================

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, terminal_states, terminal_ts_column, field_map_json)
SELECT -1, 'M8.Lead', 'm8_lead', 'workflow_instance_id', 'stage,status,current_step', 'WON,LOST', 'closed_at',
       '{"lost_reason": ["lost_reason"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M8.Lead' AND target_table = 'm8_lead');

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, terminal_states, terminal_ts_column, state_ts_columns_json)
SELECT -1, 'M8.Quote', 'm8_quote', 'workflow_instance_id', 'status,current_step', 'ACCEPTED,REJECTED,EXPIRED', 'decided_at',
       '{"SENT": "sent_at"}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M8.Quote' AND target_table = 'm8_quote');

-- ============================================================
-- 6. ACTIONS
-- ============================================================

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT * FROM (
  SELECT 'pico.lead-create' a, 'Create Lead' b, 'crm' c,
         '{"type":"object","properties":{"lead_name":{"type":"string"},"company_name":{"type":"string"},"email_id":{"type":"string"},"mobile_no":{"type":"string"},"territory":{"type":"string"},"city_town":{"type":"string"},"sales_person_emp_id":{"type":"integer"},"source":{"type":"string"},"estimated_value":{"type":"number"},"notes":{"type":"string"}},"required":["lead_name"],"note":"Read from args. Creator from the JWT; sales person from args, else the city''s (m8_lead_city)."}' d,
         1 e, NULL f,
         'Creates m8_lead (NEW), starts M8.Lead, mails the lead owner (crm.lead_owner_emp_id) and the sales person (replaces create_lead, send_lead_notification_email).' g
  UNION ALL
  SELECT 'pico.lead-update', 'Update Lead', 'crm',
         '{"type":"object","properties":{"lead_id":{"type":"integer"},"qualification_status":{"enum":["UNQUALIFIED","IN_PROCESS","QUALIFIED"]},"stage":{"enum":["CONTACTED","LOST"]},"lost_reason":{"type":"string"},"next_action_on":{"type":"string","format":"date"},"notes":{"type":"string"},"sales_person_emp_id":{"type":"integer"}},"required":["lead_id"],"note":"Read from args."}',
         1, NULL,
         'Updates qualification / next action / notes / sales person; QUALIFIED moves an early lead to QUALIFIED; stage CONTACTED / LOST fires those transitions (replaces update_lead_qualification_status).'
  UNION ALL
  SELECT 'pico.lead-convert', 'Convert Lead to Customer', 'crm',
         '{"type":"object","properties":{"lead_id":{"type":"integer"},"customer_type":{"enum":["COMPANY","INDIVIDUAL"]},"gstin":{"type":"string"},"address":{"type":"string"}},"required":["lead_id"],"note":"Read from args."}',
         1, NULL,
         'Creates m8_customer from the lead, links it and moves the lead to WON (replaces convert_lead_to_customer).'
  UNION ALL
  SELECT 'pico.quote-create', 'Create Quotation', 'crm',
         '{"type":"object","properties":{"lead_id":{"type":"integer"},"customer_id":{"type":"integer"},"items":{"type":"array","items":{"type":"object","properties":{"sku_code":{"type":"string"},"qty":{"type":"number"},"rate":{"type":"number"}},"required":["sku_code","qty"]}},"valid_until":{"type":"string","format":"date"},"notes":{"type":"string"}},"required":["items"],"note":"Read from args. Exactly one of lead_id / customer_id. rate defaults to m5_sku_master.sale_price; item_code is accepted as an alias of sku_code."}',
         1, NULL,
         'Creates m8_quote + lines with GST, starts M8.Quote and sends it (SENT), moves an open lead to QUOTED and mails the party (replaces create_quotation_from_lead, create_quotation, send_quotation_email).'
  UNION ALL
  SELECT 'pico.my-crm', 'My CRM', 'crm',
         '{"type":"object","properties":{"view":{"enum":["leads","lead","tracking","quotes","quote","customers","sales-persons","items","cities"]},"lead_id":{"type":"integer"},"quote_id":{"type":"integer"},"stage":{"type":"string"},"search":{"type":"string"},"from_date":{"type":"string"},"to_date":{"type":"string"}},"note":"Read from args. Sales users see their own leads / quotes; Manager / TenantAdmin see all."}',
         1, NULL,
         'CRM reads for the mobile app (replaces get_leads, get_my_leads, get_all_lead_tracking_history, get_quotations, get_customers, get_sales_persons, get_items).'
) x
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = x.a);

-- ============================================================
-- 7. PAGES
-- ============================================================
-- The four pico:lead-* transition pages are what the actions submit; they also work from the web.

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT * FROM (
  SELECT -1 a,'pico:lead-qualify' b,'Qualify Lead' c,'Mark a lead qualified.' d,'CRM' e,'PUBLISHED' f,NOW() g,NOW() h UNION ALL
  SELECT -1,'pico:lead-quoted','Lead Quoted','Set by quotation creation.','CRM','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:lead-convert','Convert Lead','Set by conversion to customer.','CRM','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:lead-lost','Lead Lost','Close a lead as lost.','CRM','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:customer-list','Customers','Customer master.','CRM','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:lead-city-list','Lead Cities','City to sales person assignment for new leads.','CRM','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:quote-line-list','Quotation Lines','Items quoted, per quotation.','CRM','PUBLISHED',NOW(),NOW()
) x
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = x.b);

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', b.body, NOW()
FROM page_definition pd
JOIN (
  SELECT 'pico:lead-qualify' code,
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Qualify Lead","fields":[{"kind":"FormField","type":"textarea","name":"notes","label":"Notes"},{"kind":"WorkflowActionButton","label":"Mark Qualified","action":"workflow.transition","variant":"primary"}]}]}' body
  UNION ALL
  SELECT 'pico:lead-quoted',
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Lead Quoted","fields":[{"kind":"FormField","type":"text","name":"quote_number","label":"Quotation #"},{"kind":"WorkflowActionButton","label":"Mark Quoted","action":"workflow.transition","variant":"primary"}]}]}'
  UNION ALL
  SELECT 'pico:lead-convert',
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Convert Lead","fields":[{"kind":"FormField","type":"text","name":"customer_code","label":"Customer Code"},{"kind":"WorkflowActionButton","label":"Mark Won","action":"workflow.transition","variant":"primary"}]}]}'
  UNION ALL
  SELECT 'pico:lead-lost',
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Lead Lost","fields":[{"kind":"FormField","type":"textarea","name":"lost_reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Mark Lost","action":"workflow.transition","variant":"danger"}]}]}'
  UNION ALL
  SELECT 'pico:customer-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"Customer","select":["id","customer_code","customer_name","customer_type","mobile_no","email_id","gstin","territory","city_town","source","is_active"]},"columns":[{"field":"customer_code","label":"Code","sticky":"left"},{"field":"customer_name","label":"Name","sortable":true},{"field":"customer_type","label":"Type","render":"badge"},{"field":"mobile_no","label":"Mobile"},{"field":"email_id","label":"Email"},{"field":"gstin","label":"GSTIN"},{"field":"territory","label":"Territory"},{"field":"city_town","label":"City"},{"field":"source","label":"Source","render":"badge"},{"field":"is_active","label":"Active","render":"badge"}],"filters":[{"field":"source","label":"Source","type":"multi-select","optionsSource":{"type":"static","options":["LEAD","MANUAL","SAP","IMPORT"]}}],"sort":{"default":[{"field":"customer_name","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Customers"}}'
  UNION ALL
  SELECT 'pico:lead-city-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"LeadCity","joins":[{"entity":"EmployeeMaster","as":"e","on":"m8_lead_city.sales_person_emp_id = e.id"}],"select":["m8_lead_city.id as id","m8_lead_city.city_name as city_name","m8_lead_city.sales_person_emp_id as sales_person_emp_id","e.full_name as sales_person","m8_lead_city.is_active as is_active"]},"columns":[{"field":"city_name","label":"City","sticky":"left","sortable":true},{"field":"sales_person","label":"Sales Person"},{"field":"is_active","label":"Active","render":"badge"}],"sort":{"default":[{"field":"city_name","dir":"asc"}]},"pagination":{"kind":"server","pageSize":100},"header":{"title":"Lead Cities"}}'
  UNION ALL
  SELECT 'pico:quote-line-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"QuoteLine","joins":[{"entity":"Quote","as":"q","on":"m8_quote_line.quote_id = q.id"}],"select":["m8_quote_line.id as id","q.quote_number as quote_number","q.customer_name as customer_name","q.status as status","m8_quote_line.sku_code as sku_code","m8_quote_line.item_name as item_name","m8_quote_line.machine_id as machine_id","m8_quote_line.qty as qty","m8_quote_line.rate as rate","m8_quote_line.amount as amount","m8_quote_line.tax_amount as tax_amount"]},"columns":[{"field":"quote_number","label":"Quote #","sticky":"left","sortable":true},{"field":"customer_name","label":"Party"},{"field":"status","label":"Status","render":"badge"},{"field":"sku_code","label":"Item"},{"field":"item_name","label":"Name"},{"field":"machine_id","label":"Machine"},{"field":"qty","label":"Qty","render":"number","align":"right"},{"field":"rate","label":"Rate","render":"number","align":"right"},{"field":"amount","label":"Amount","render":"number","align":"right"},{"field":"tax_amount","label":"Tax","render":"number","align":"right"}],"sort":{"default":[{"field":"quote_number","dir":"desc"}]},"pagination":{"kind":"server","pageSize":100},"header":{"title":"Quotation Lines"}}'
) b ON b.code = pd.code
WHERE pd.tenant_id = -1
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id AND pv.version = 1);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1
  AND pd.code IN ('pico:lead-qualify','pico:lead-quoted','pico:lead-convert','pico:lead-lost',
                  'pico:customer-list','pico:lead-city-list','pico:quote-line-list')
  AND pd.current_version_id IS NULL;

INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
SELECT * FROM (
  SELECT -1 a,'pico:lead-qualify' b,'M8.Lead' c,'QUALIFIED' d,'INPUT' e UNION ALL
  SELECT -1,'pico:lead-quoted','M8.Lead','QUOTED','INPUT' UNION ALL
  SELECT -1,'pico:lead-convert','M8.Lead','WON','INPUT' UNION ALL
  SELECT -1,'pico:lead-lost','M8.Lead','LOST','INPUT'
) x
WHERE NOT EXISTS (SELECT 1 FROM page_workflow_binding WHERE tenant_id = x.a AND page_code = x.b AND workflow_code = x.c AND step_code = x.d);
