-- Expense Module: aria has no Expense Claim equivalent at all today (see
-- CLAUDE.md-adjacent comparison work) -- this is net-new, not a port.
-- Kept intentionally simple (flat claim, one expense type per claim, plain
-- status field) per "core business fields only"; NOT wired into aria's
-- workflow engine in this pass -- see plan notes.

-- ===================== ExpenseClaimType =====================
CREATE TABLE IF NOT EXISTS m7_expense_claim_type (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_expense_claim_type_tenant_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'ExpenseClaimType', 'Expense Claim Type', 'm7_expense_claim_type', 'Expense category master (Travel, Meals, ...).', 'Expense', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'ExpenseClaimType');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'ExpenseClaimType',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk
FROM (SELECT 1 ord,'code' fc,'Code' lbl,'STRING' dt,1 filt,1 sort,1 srch,1 req,'text' wk
      UNION ALL SELECT 2,'name','Name','STRING',1,1,1,1,'text'
      UNION ALL SELECT 3,'description','Description','STRING',0,0,1,0,'text'
      UNION ALL SELECT 4,'active','Active','BOOLEAN',1,1,0,0,'checkbox') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ExpenseClaimType' AND field_code=c.fc);

-- ===================== ExpenseClaim (powers "Expense Claim Report") =====================
CREATE TABLE IF NOT EXISTS m7_expense_claim (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  employee_id BIGINT NOT NULL,
  posting_date DATE NOT NULL,
  expense_type_id BIGINT NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  sanctioned_amount DECIMAL(14,2) DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  approved_by BIGINT DEFAULT NULL,
  company VARCHAR(64) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_expense_claim_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, lifecycle_kind)
SELECT -1, 'ExpenseClaim', 'Expense Claim', 'm7_expense_claim', 'Employee-initiated reimbursement claim.', 'Expense', 'DEFINED'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE tenant_id = -1 AND code = 'ExpenseClaim');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'ExpenseClaim',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk,c.rel
FROM (SELECT 1 ord,'employee_id' fc,'Employee' lbl,'LONG' dt,1 filt,1 sort,0 srch,1 req,'entity-ref' wk,'Employee' rel
      UNION ALL SELECT 2,'posting_date','Posting Date','DATE',0,1,0,1,'date',NULL
      UNION ALL SELECT 3,'expense_type_id','Expense Type','LONG',1,1,0,1,'entity-ref','ExpenseClaimType'
      UNION ALL SELECT 4,'description','Description','STRING',0,0,1,0,'text',NULL
      UNION ALL SELECT 5,'amount','Amount','DECIMAL',0,1,0,1,'currency',NULL
      UNION ALL SELECT 6,'sanctioned_amount','Sanctioned Amount','DECIMAL',0,1,0,0,'currency',NULL
      UNION ALL SELECT 7,'status','Status','STRING',1,1,0,1,'select',NULL
      UNION ALL SELECT 8,'approved_by','Approved By','LONG',0,0,0,0,'number',NULL
      UNION ALL SELECT 9,'company','Company','STRING',1,1,0,0,'text',NULL) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ExpenseClaim' AND field_code=c.fc);

UPDATE entity_field_registry SET options_json = '["DRAFT","SUBMITTED","APPROVED","REJECTED","PAID"]' WHERE entity_code='ExpenseClaim' AND field_code='status' AND options_json IS NULL;

-- ===================== Expense Module pages =====================
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:exm-expense-claim-type-list', 'Expense Claim Type', 'Expense category master (Expense Module).', 'Expense Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:exm-expense-claim-type-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "ExpenseClaimType", "select": ["code", "name", "description", "active"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Expense Type", "type": "text-contains", "applyTo": ["code", "name"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:exm-expense-claim-type-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:exm-expense-claim-type-list' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:exm-expense-claim-list', 'Expense Claims', 'Employee expense claims (Expense Module).', 'Expense Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:exm-expense-claim-list');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "ExpenseClaim", "select": ["employee_id", "posting_date", "expense_type_id", "amount", "sanctioned_amount", "status"]}, "columns": [], "filters": [{"field": "employee_id", "label": "Employee", "type": "equals", "applyTo": ["employee_id"]}, {"field": "status", "label": "Status", "type": "equals", "applyTo": ["status"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:exm-expense-claim-list' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:exm-expense-claim-list' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:exm-expense-claim-report', 'Expense Claim Report', 'Expense claims across employees (Expense Module).', 'Expense Module', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:exm-expense-claim-report');
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', '{"kind": "list", "dataSource": {"entity": "ExpenseClaim", "select": ["employee_id", "expense_type_id", "posting_date", "status", "amount", "sanctioned_amount", "company"]}, "columns": [], "filters": [{"field": "posting_date", "label": "From/To", "type": "date-range", "applyTo": ["posting_date"]}, {"field": "employee_id", "label": "Employee", "type": "equals", "applyTo": ["employee_id"]}, {"field": "company", "label": "Company", "type": "equals", "applyTo": ["company"]}, {"field": "status", "label": "Status", "type": "equals", "applyTo": ["status"]}], "pagination": {"pageSize": 50}}', NOW()
FROM page_definition pd WHERE pd.tenant_id = -1 AND pd.code = 'aria:exm-expense-claim-report' AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
UPDATE page_definition pd JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id WHERE pd.tenant_id = -1 AND pd.code = 'aria:exm-expense-claim-report' AND pd.current_version_id IS NULL;
