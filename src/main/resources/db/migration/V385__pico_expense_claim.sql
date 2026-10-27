-- V385: PICO (Spokes) Module 4 -- Expense Claim, migrated from ERPNext / HRMS. Requires V382-V384.
--
-- ERPNext source (apps/spokes_app/spokes_app/api/pico_api.py):
--   create_expense_claim, save_base64_receipt, send_expense_approval_email,
--   approve_reject_expense_claim, get_employee_expenses, get_company_expenses,
--   get_company_expense_claim_types, get_pending_expenses_for_approver
--   HRMS DocTypes: Expense Claim, Expense Claim Detail, Expense Claim Type (+ Account)
--
-- Aria had no employee expense claim (the legacy expenses / cexpense / expense_category tables are
-- Idayam company-cost masters, unrelated), so this is a new entity + workflow.
--
-- Delivers:
--   m7_employee        + expense_approver_id
--   entities           ExpenseClaimType, ExpenseClaim, ExpenseClaimLine
--   workflow           M7.ExpenseClaim  SUBMITTED -> APPROVED -> PAID ; SUBMITTED -> REJECTED | WITHDRAWN
--   projection         M7.ExpenseClaim -> m7_expense_claim
--   actions            pico.expense-submit, pico.expense-withdraw, pico.my-expense
--   Java               ExpenseApproverValidator, ExpenseDecisionListener (sanction, mails, optional GL)
--   pages              pico:expense-decide, pico:expense-withdraw, pico:expense-pay,
--                      pico:expense-claim-list, pico:expense-type-list
--
-- Tenant settings (m0_tenant_setting; unset = default):
--   expense.gl_posting        false | true: post Dr expense / Cr payable on APPROVED and
--                             Dr payable / Cr bank on PAID into m6_gl_entry
--   expense.payable_account   GL code of "employee reimbursements payable" (required when posting)
--   expense.bank_account      GL code of the bank paying reimbursements (required when posting)
--
-- Idempotent -- safe to (re)run on any database.

-- ============================================================
-- 1. EMPLOYEE MASTER -- expense approver
-- ============================================================

ALTER TABLE m7_employee
  ADD COLUMN IF NOT EXISTS expense_approver_id BIGINT DEFAULT NULL
    COMMENT 'm7_employee.id who approves this employee''s expense claims; NULL = use reports_to_emp_id';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT -1, 'EmployeeMaster', 'expense_approver_id', 'Expense Approver', 'RELATION', 1, 0, 0, 0, 'entity-ref', 'EmployeeMaster',
       '{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'EmployeeMaster' AND field_code = 'expense_approver_id');

-- ============================================================
-- 2. TABLES
-- ============================================================

CREATE TABLE IF NOT EXISTS m7_expense_claim_type (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  type_code VARCHAR(32) NOT NULL,
  type_name VARCHAR(128) NOT NULL,
  default_account VARCHAR(16) DEFAULT NULL COMMENT 'm6_gl_entry.account_code debited on approval; NULL = no GL posting for this type',
  max_amount DECIMAL(12,2) DEFAULT NULL COMMENT 'Per-line cap; NULL = no cap',
  requires_receipt TINYINT(1) NOT NULL DEFAULT 0,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Expense Claim Type name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m7ect_code (tenant_id, type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- status / current_step / decided_at / paid_at / approver_remarks / pay_reference are written by
-- the M7.ExpenseClaim projection; totals by pico.expense-submit and ExpenseDecisionListener.
CREATE TABLE IF NOT EXISTS m7_expense_claim (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  claim_number VARCHAR(32) NOT NULL,
  employee_id BIGINT NOT NULL,
  approver_emp_id BIGINT DEFAULT NULL COMMENT 'm7_employee.id resolved at submit; NULL = any Manager may decide',
  posting_date DATE NOT NULL,
  total_claimed DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  total_sanctioned DECIMAL(12,2) DEFAULT NULL COMMENT 'Set on APPROVED; NULL = not decided yet',
  remarks VARCHAR(512) DEFAULT NULL COMMENT 'Employee''s note',
  status VARCHAR(16) NOT NULL DEFAULT 'SUBMITTED',
  current_step VARCHAR(64) DEFAULT NULL,
  workflow_instance_id BIGINT DEFAULT NULL,
  approver_remarks VARCHAR(512) DEFAULT NULL,
  decided_at DATETIME DEFAULT NULL COMMENT 'APPROVED / REJECTED / WITHDRAWN time; NULL = still pending',
  paid_at DATETIME DEFAULT NULL COMMENT 'NULL = not reimbursed',
  pay_reference VARCHAR(64) DEFAULT NULL COMMENT 'Bank UTR / voucher of the reimbursement; NULL = not reimbursed',
  erp_ref VARCHAR(140) DEFAULT NULL COMMENT 'ERPNext Expense Claim name for migrated rows; NULL = created in Aria',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_m7ec_number (tenant_id, claim_number),
  KEY idx_m7ec_emp (tenant_id, employee_id, status),
  KEY idx_m7ec_approver (tenant_id, approver_emp_id, status),
  KEY idx_m7ec_wf (tenant_id, workflow_instance_id),
  CONSTRAINT fk_m7ec_emp FOREIGN KEY (employee_id) REFERENCES m7_employee (id),
  CONSTRAINT chk_m7ec_status CHECK (status IN ('SUBMITTED','APPROVED','REJECTED','WITHDRAWN','PAID'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS m7_expense_claim_line (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  claim_id BIGINT NOT NULL,
  expense_date DATE NOT NULL,
  type_code VARCHAR(32) NOT NULL COMMENT 'm7_expense_claim_type.type_code',
  description VARCHAR(512) DEFAULT NULL,
  amount DECIMAL(12,2) NOT NULL,
  sanctioned_amount DECIMAL(12,2) DEFAULT NULL COMMENT 'Set on APPROVED; NULL = not decided yet',
  receipt_url VARCHAR(512) DEFAULT NULL COMMENT 'From /api/m0/document-vault/upload; NULL = no receipt',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_m7ecl_claim (claim_id),
  CONSTRAINT fk_m7ecl_claim FOREIGN KEY (claim_id) REFERENCES m7_expense_claim (id) ON DELETE CASCADE,
  CONSTRAINT chk_m7ecl_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- ============================================================
-- 3. ENTITY REGISTRY
-- ============================================================
-- Claims are written only through pico.expense-submit (employee from the JWT) and the workflow, so
-- the generic API is read-only for HR / finance.

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, read_role, write_role, value_column, display_column, lifecycle_kind, workflow_code)
SELECT * FROM (
  SELECT -1 a,'ExpenseClaimType' b,'Expense Claim Type' c,'m7_expense_claim_type' d,'Kinds of reimbursable expense with GL account and caps (ERPNext Expense Claim Type).' e,'HR' f,'*' g,'Manager,TenantAdmin' h,'type_code' i,'type_name' j,'DEFINED' k,NULL l UNION ALL
  SELECT -1,'ExpenseClaim','Expense Claim','m7_expense_claim','Employee reimbursement claim (ERPNext Expense Claim).','HR','Manager,TenantAdmin','TenantAdmin',NULL,NULL,'WORKFLOW','M7.ExpenseClaim' UNION ALL
  SELECT -1,'ExpenseClaimLine','Expense Claim Line','m7_expense_claim_line','One expense inside a claim (ERPNext Expense Claim Detail).','HR','Manager,TenantAdmin','TenantAdmin',NULL,NULL,'DEFINED',NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = x.b);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT * FROM (
  -- ExpenseClaimType
  SELECT -1 a,'ExpenseClaimType' b,'type_code' c,'Code' d,'STRING' e,1 f,1 g,1 h,1 i,'text' j,NULL k,NULL l UNION ALL
  SELECT -1,'ExpenseClaimType','type_name','Name','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaimType','default_account','GL Account','STRING',1,1,0,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaimType','max_amount','Max per Line','DECIMAL',0,1,0,0,'currency',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaimType','requires_receipt','Receipt Required','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaimType','is_active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaimType','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- ExpenseClaim
  SELECT -1,'ExpenseClaim','claim_number','Claim #','STRING',1,1,1,1,'text',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaim','employee_id','Employee','RELATION',1,1,0,1,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'ExpenseClaim','approver_emp_id','Approver','RELATION',1,1,0,0,'entity-ref','EmployeeMaster','{"entity":"EmployeeMaster","valueField":"id","labelField":"full_name"}' UNION ALL
  SELECT -1,'ExpenseClaim','posting_date','Date','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaim','total_claimed','Claimed','DECIMAL',0,1,0,1,'currency',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaim','total_sanctioned','Sanctioned','DECIMAL',0,1,0,0,'currency',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaim','remarks','Employee Remarks','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaim','status','Status','STRING',1,1,0,0,'select',NULL,'["SUBMITTED","APPROVED","REJECTED","WITHDRAWN","PAID"]' UNION ALL
  SELECT -1,'ExpenseClaim','approver_remarks','Approver Remarks','STRING',0,0,1,0,'textarea',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaim','decided_at','Decided At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaim','paid_at','Paid At','DATETIME',1,1,0,0,'datetime',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaim','pay_reference','Payment Reference','STRING',1,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaim','erp_ref','ERPNext Ref','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  -- ExpenseClaimLine
  SELECT -1,'ExpenseClaimLine','claim_id','Claim','RELATION',1,1,0,1,'entity-ref','ExpenseClaim','{"entity":"ExpenseClaim","valueField":"id","labelField":"claim_number"}' UNION ALL
  SELECT -1,'ExpenseClaimLine','expense_date','Expense Date','DATE',1,1,0,1,'date',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaimLine','type_code','Type','RELATION',1,1,0,1,'entity-ref','ExpenseClaimType','{"entity":"ExpenseClaimType","valueField":"type_code","labelField":"type_name"}' UNION ALL
  SELECT -1,'ExpenseClaimLine','description','Description','STRING',0,0,1,0,'text',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaimLine','amount','Amount','DECIMAL',0,1,0,1,'currency',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaimLine','sanctioned_amount','Sanctioned','DECIMAL',0,1,0,0,'currency',NULL,NULL UNION ALL
  SELECT -1,'ExpenseClaimLine','receipt_url','Receipt','STRING',0,0,0,0,'text',NULL,NULL
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- ============================================================
-- 4. WORKFLOW + PROJECTION
-- ============================================================
-- Approve / reject read #submitted['decision'] on pico:expense-decide; payment is its own page so
-- the approver's remarks (remarks) and finance's note (pay_remarks) never overwrite each other.

INSERT INTO workflow_definition (tenant_id, code, display_name, version, status, body_json)
SELECT -1, 'M7.ExpenseClaim', 'Expense Claim', 1, 'PUBLISHED',
  '{"code":"M7.ExpenseClaim","version":1,"initialState":"SUBMITTED","states":[{"code":"SUBMITTED","assignedRole":"Manager"},{"code":"APPROVED","assignedRole":"TenantAdmin"},{"code":"REJECTED","terminal":true},{"code":"WITHDRAWN","terminal":true},{"code":"PAID","terminal":true}],"transitions":[{"from":"SUBMITTED","to":"APPROVED","trigger":"page.submitted","pageCode":"pico:expense-decide","guard":"#submitted[''decision''] == ''APPROVED''"},{"from":"SUBMITTED","to":"REJECTED","trigger":"page.submitted","pageCode":"pico:expense-decide","guard":"#submitted[''decision''] == ''REJECTED''"},{"from":"SUBMITTED","to":"WITHDRAWN","trigger":"page.submitted","pageCode":"pico:expense-withdraw"},{"from":"APPROVED","to":"PAID","trigger":"page.submitted","pageCode":"pico:expense-pay"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M7.ExpenseClaim' AND version = 1);

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, terminal_states, terminal_ts_column, state_ts_columns_json, field_map_json)
SELECT -1, 'M7.ExpenseClaim', 'm7_expense_claim', 'workflow_instance_id', 'status,current_step',
       'PAID', 'paid_at', '{"APPROVED": "decided_at", "REJECTED": "decided_at", "WITHDRAWN": "decided_at"}',
       '{"approver_remarks": ["remarks"], "pay_reference": ["pay_reference", "utr"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M7.ExpenseClaim' AND target_table = 'm7_expense_claim');

-- ============================================================
-- 5. ACTIONS
-- ============================================================

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT * FROM (
  SELECT 'pico.expense-submit' a, 'Submit Expense Claim' b, 'hr' c,
         '{"type":"object","properties":{"lines":{"type":"array","items":{"type":"object","properties":{"expense_date":{"type":"string","format":"date"},"expense_type":{"type":"string"},"amount":{"type":"number"},"description":{"type":"string"},"receipt_url":{"type":"string"}},"required":["expense_type","amount"]}},"expense_date":{"type":"string"},"expense_type":{"type":"string"},"amount":{"type":"number"},"description":{"type":"string"},"receipt_url":{"type":"string"},"remarks":{"type":"string"}},"note":"Read from args. Either lines[] or the single-line fields (ERPNext create_expense_claim shape). Employee and approver come from the JWT / employee master."}' d,
         1 e, NULL f,
         'Creates m7_expense_claim + lines (SUBMITTED), starts M7.ExpenseClaim and mails the approver (replaces create_expense_claim, save_base64_receipt, send_expense_approval_email).' g
  UNION ALL
  SELECT 'pico.expense-withdraw', 'Withdraw Expense Claim', 'hr',
         '{"type":"object","properties":{"claim_id":{"type":"integer"},"reason":{"type":"string"}},"required":["claim_id"],"note":"Read from args. Only the claim''s own employee, only while SUBMITTED."}',
         1, NULL,
         'SUBMITTED claim -> WITHDRAWN via pico:expense-withdraw.'
  UNION ALL
  SELECT 'pico.my-expense', 'My Expenses', 'hr',
         '{"type":"object","properties":{"view":{"enum":["claims","claim","types","approvals","company"]},"status":{"type":"string"},"claim_id":{"type":"integer"}},"note":"Read from args. claims / claim: own claims (claim also for its approver and HR); approvals: claims waiting for the caller; company: all claims, Manager / TenantAdmin only."}',
         1, NULL,
         'Expense reads for the mobile app (replaces get_employee_expenses, get_pending_expenses_for_approver, get_company_expenses, get_company_expense_claim_types).'
) x
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = x.a);

-- ============================================================
-- 6. PAGES
-- ============================================================

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT * FROM (
  SELECT -1 a,'pico:expense-decide' b,'Expense Claim Decision' c,'Approver approves or rejects an expense claim.' d,'HR' e,'PUBLISHED' f,NOW() g,NOW() h UNION ALL
  SELECT -1,'pico:expense-withdraw','Withdraw Expense Claim','Employee withdraws a claim that is still pending.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:expense-pay','Reimburse Expense Claim','Finance records the reimbursement of an approved claim.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:expense-claim-list','Expense Claims','All employee expense claims.','HR','PUBLISHED',NOW(),NOW() UNION ALL
  SELECT -1,'pico:expense-type-list','Expense Claim Types','Reimbursable expense kinds, GL accounts and caps.','HR','PUBLISHED',NOW(),NOW()
) x
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = x.b);

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED', b.body, NOW()
FROM page_definition pd
JOIN (
  SELECT 'pico:expense-decide' code,
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Expense Claim Decision","fields":[{"kind":"FormField","type":"select","name":"decision","label":"Decision","options":[{"label":"Approve","value":"APPROVED"},{"label":"Reject","value":"REJECTED"}]},{"kind":"FormField","type":"textarea","name":"remarks","label":"Remarks"},{"kind":"WorkflowActionButton","label":"Submit","action":"workflow.transition","variant":"primary"}]}]}' body
  UNION ALL
  SELECT 'pico:expense-withdraw',
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Withdraw Expense Claim","fields":[{"kind":"FormField","type":"textarea","name":"reason","label":"Reason"},{"kind":"WorkflowActionButton","label":"Withdraw","action":"workflow.transition","variant":"danger"}]}]}'
  UNION ALL
  SELECT 'pico:expense-pay',
    '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Reimbursement","fields":[{"kind":"FormField","type":"text","name":"pay_reference","label":"Bank UTR / Voucher No"},{"kind":"FormField","type":"textarea","name":"pay_remarks","label":"Notes"},{"kind":"WorkflowActionButton","label":"Mark Paid","action":"workflow.transition","variant":"primary"}]}]}'
  UNION ALL
  SELECT 'pico:expense-claim-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ExpenseClaim","joins":[{"entity":"EmployeeMaster","as":"e","on":"m7_expense_claim.employee_id = e.id"}],"select":["m7_expense_claim.id as id","m7_expense_claim.claim_number as claim_number","m7_expense_claim.posting_date as posting_date","e.emp_code as emp_code","e.full_name as full_name","m7_expense_claim.total_claimed as total_claimed","m7_expense_claim.total_sanctioned as total_sanctioned","m7_expense_claim.status as status","m7_expense_claim.decided_at as decided_at","m7_expense_claim.pay_reference as pay_reference","m7_expense_claim.workflow_instance_id as workflow_instance_id"]},"columns":[{"field":"claim_number","label":"Claim #","sticky":"left"},{"field":"posting_date","label":"Date","render":"date","sortable":true},{"field":"emp_code","label":"Emp Code"},{"field":"full_name","label":"Employee"},{"field":"total_claimed","label":"Claimed","render":"number","align":"right"},{"field":"total_sanctioned","label":"Sanctioned","render":"number","align":"right"},{"field":"status","label":"Status","render":"badge","colorMap":{"SUBMITTED":"amber","APPROVED":"blue","REJECTED":"red","WITHDRAWN":"gray","PAID":"green"}},{"field":"decided_at","label":"Decided","render":"datetime"},{"field":"pay_reference","label":"Pay Ref"}],"filters":[{"field":"status","label":"Status","type":"multi-select","optionsSource":{"type":"static","options":["SUBMITTED","APPROVED","REJECTED","WITHDRAWN","PAID"]}}],"sort":{"default":[{"field":"posting_date","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Expense Claims"}}'
  UNION ALL
  SELECT 'pico:expense-type-list',
    '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ExpenseClaimType","select":["id","type_code","type_name","default_account","max_amount","requires_receipt","is_active"]},"columns":[{"field":"type_code","label":"Code","render":"badge","sticky":"left"},{"field":"type_name","label":"Name"},{"field":"default_account","label":"GL Account"},{"field":"max_amount","label":"Max / Line","render":"number","align":"right"},{"field":"requires_receipt","label":"Receipt","render":"badge"},{"field":"is_active","label":"Active","render":"badge"}],"sort":{"default":[{"field":"type_name","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Expense Claim Types"}}'
) b ON b.code = pd.code
WHERE pd.tenant_id = -1
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id AND pv.version = 1);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1
  AND pd.code IN ('pico:expense-decide','pico:expense-withdraw','pico:expense-pay','pico:expense-claim-list','pico:expense-type-list')
  AND pd.current_version_id IS NULL;

INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
SELECT * FROM (
  SELECT -1 a,'pico:expense-decide' b,'M7.ExpenseClaim' c,'SUBMITTED' d,'INPUT' e UNION ALL
  SELECT -1,'pico:expense-withdraw','M7.ExpenseClaim','SUBMITTED','INPUT' UNION ALL
  SELECT -1,'pico:expense-pay','M7.ExpenseClaim','APPROVED','INPUT'
) x
WHERE NOT EXISTS (SELECT 1 FROM page_workflow_binding WHERE tenant_id = x.a AND page_code = x.b AND workflow_code = x.c AND step_code = x.d);
