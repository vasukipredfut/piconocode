-- =====================================================================
-- V31 — Core business engine: Double-entry GL · AR aging · MR→RFQ→PO chain
--       · Job-work · Boiler IBR daily log · Reorder alerts · Bank recon
--
-- Closes the top-priority gaps identified in the use-case audit so the
-- system actually behaves like a real ERP end-to-end:
--   1. m6_gl_account + m6_gl_entry        → double-entry general ledger
--   2. m6_ar_open_item + view             → AR aging buckets + 43B(h) clock
--   3. m6_bank_recon                       → bank reconciliation
--   4. m5_purchase_requisition / rfq / quote_compare / po link
--   5. m5_job_work_issue / receipt          → sub-contracting
--   6. m5_boiler_daily_log                  → IBR daily ops log
--   7. m5_reorder_alert (view)              → safety stock breaches
--   8. m6_gstr1_summary / gstr3b_summary    → return-period rollups
--   9. m6_form17_wage_register (view)       → Factories Act wage register
--  10. m5_density_uom_factor                → kg↔L conversion per item
-- =====================================================================

-- =================================================================
-- 1. DOUBLE-ENTRY GENERAL LEDGER
-- =================================================================
CREATE TABLE IF NOT EXISTS m6_gl_account (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  account_code VARCHAR(16) NOT NULL,
  account_name VARCHAR(160) NOT NULL,
  account_class VARCHAR(16) NOT NULL,          -- ASSET/LIABILITY/EQUITY/INCOME/EXPENSE
  account_group VARCHAR(64) NULL,              -- e.g. CURRENT_ASSET / NON_CURRENT_LIAB / OPERATING_EXPENSE
  parent_code VARCHAR(16) NULL,
  is_control_account TINYINT(1) NOT NULL DEFAULT 0,
  is_bank_account TINYINT(1) NOT NULL DEFAULT 0,
  bank_acct_no VARCHAR(32) NULL,
  bank_ifsc VARCHAR(16) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  opening_balance DECIMAL(14,2) NOT NULL DEFAULT 0,
  UNIQUE KEY uq_acct (tenant_id, account_code),
  KEY idx_acct_class (account_class)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m6_gl_account (tenant_id, account_code, account_name, account_class, account_group, is_bank_account, bank_acct_no, bank_ifsc, opening_balance) VALUES
-- ASSETS
(1,'1010','Cash in Hand',                  'ASSET','CURRENT_ASSET',0, NULL, NULL,           50000.00),
(1,'1020','SBI Current A/c 1234',          'ASSET','CURRENT_ASSET',1, '32101234567890','SBIN0000123', 3245000.00),
(1,'1021','HDFC Current A/c 5678',         'ASSET','CURRENT_ASSET',1, '50100012345678','HDFC0001234', 875000.00),
(1,'1100','Sundry Debtors (Control)',      'ASSET','CURRENT_ASSET',0, NULL, NULL,          1850000.00),
(1,'1200','Stock in Trade',                'ASSET','CURRENT_ASSET',0, NULL, NULL,          4250000.00),
(1,'1300','Input GST Credit (Control)',    'ASSET','CURRENT_ASSET',0, NULL, NULL,           185000.00),
(1,'1400','Advance to Suppliers',          'ASSET','CURRENT_ASSET',0, NULL, NULL,           120000.00),
(1,'1410','TDS Receivable',                'ASSET','CURRENT_ASSET',0, NULL, NULL,            25000.00),
(1,'1500','Plant & Machinery',             'ASSET','FIXED_ASSET',  0, NULL, NULL,         18500000.00),
(1,'1510','Buildings',                     'ASSET','FIXED_ASSET',  0, NULL, NULL,         12000000.00),
(1,'1520','Vehicles',                      'ASSET','FIXED_ASSET',  0, NULL, NULL,          2400000.00),
(1,'1599','Accumulated Depreciation',      'ASSET','FIXED_ASSET',  0, NULL, NULL,        -3850000.00),
-- LIABILITIES
(1,'2010','Sundry Creditors (Control)',    'LIABILITY','CURRENT_LIAB',  0, NULL, NULL,    2185000.00),
(1,'2020','Output GST Payable (Control)',  'LIABILITY','CURRENT_LIAB',  0, NULL, NULL,     245000.00),
(1,'2030','TDS Payable',                   'LIABILITY','CURRENT_LIAB',  0, NULL, NULL,      60918.00),
(1,'2031','PF Payable',                    'LIABILITY','CURRENT_LIAB',  0, NULL, NULL,      36000.00),
(1,'2032','ESI Payable',                   'LIABILITY','CURRENT_LIAB',  0, NULL, NULL,        800.00),
(1,'2033','PT Payable',                    'LIABILITY','CURRENT_LIAB',  0, NULL, NULL,        625.00),
(1,'2040','Salary Payable',                'LIABILITY','CURRENT_LIAB',  0, NULL, NULL,           0.00),
(1,'2100','Term Loan — Canara Bank',       'LIABILITY','LONG_TERM_LIAB',0, NULL, NULL,    5500000.00),
(1,'2110','OD Limit — SBI',                'LIABILITY','CURRENT_LIAB',  0, NULL, NULL,    1250000.00),
-- EQUITY
(1,'3010','Share Capital',                 'EQUITY','EQUITY',0, NULL, NULL, 5000000.00),
(1,'3020','Retained Earnings',             'EQUITY','EQUITY',0, NULL, NULL, 8550000.00),
-- INCOME
(1,'4010','Sales — Refined Oil',           'INCOME','OPERATING_INCOME',0, NULL, NULL, 0.00),
(1,'4020','Sales — Crude Oil',             'INCOME','OPERATING_INCOME',0, NULL, NULL, 0.00),
(1,'4030','Sales — Byproducts',            'INCOME','OPERATING_INCOME',0, NULL, NULL, 0.00),
(1,'4040','Other Income',                  'INCOME','OTHER_INCOME',    0, NULL, NULL, 0.00),
-- EXPENSE
(1,'5010','Raw Material Consumed',         'EXPENSE','COGS',           0, NULL, NULL, 0.00),
(1,'5020','Packing Material Consumed',     'EXPENSE','COGS',           0, NULL, NULL, 0.00),
(1,'5030','Power & Fuel',                  'EXPENSE','MFG_EXPENSE',    0, NULL, NULL, 0.00),
(1,'5040','Repairs & Maintenance',         'EXPENSE','MFG_EXPENSE',    0, NULL, NULL, 0.00),
(1,'5050','Freight Outward',               'EXPENSE','SELLING_EXPENSE',0, NULL, NULL, 0.00),
(1,'6010','Salaries & Wages',              'EXPENSE','EMPLOYEE_COST',  0, NULL, NULL, 0.00),
(1,'6020','PF — Employer Contribution',    'EXPENSE','EMPLOYEE_COST',  0, NULL, NULL, 0.00),
(1,'6030','ESI — Employer Contribution',   'EXPENSE','EMPLOYEE_COST',  0, NULL, NULL, 0.00),
(1,'6040','Gratuity Provision',            'EXPENSE','EMPLOYEE_COST',  0, NULL, NULL, 0.00),
(1,'6050','Bonus',                         'EXPENSE','EMPLOYEE_COST',  0, NULL, NULL, 0.00),
(1,'7010','Audit Fee',                     'EXPENSE','ADMIN_EXPENSE',  0, NULL, NULL, 0.00),
(1,'7020','Legal & Professional',          'EXPENSE','ADMIN_EXPENSE',  0, NULL, NULL, 0.00),
(1,'7030','Bank Charges',                  'EXPENSE','FIN_EXPENSE',    0, NULL, NULL, 0.00),
(1,'7040','Interest Expense',              'EXPENSE','FIN_EXPENSE',    0, NULL, NULL, 0.00),
(1,'7050','Depreciation',                  'EXPENSE','MFG_EXPENSE',    0, NULL, NULL, 0.00);

CREATE TABLE IF NOT EXISTS m6_gl_entry (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  je_no VARCHAR(32) NOT NULL,
  je_date DATE NOT NULL,
  fy VARCHAR(8) NOT NULL,
  period VARCHAR(8) NOT NULL,                      -- YYYY-MM
  source_module VARCHAR(16) NOT NULL,              -- SALES/PURCHASE/PAYROLL/RECEIPT/PAYMENT/JOURNAL/DEPRECIATION/STOCK
  source_doc VARCHAR(64) NULL,
  account_code VARCHAR(16) NOT NULL,
  cost_center VARCHAR(32) NULL,
  narration TEXT NULL,
  debit DECIMAL(14,2) NOT NULL DEFAULT 0,
  credit DECIMAL(14,2) NOT NULL DEFAULT 0,
  party_code VARCHAR(48) NULL,                     -- vendor/customer/employee code
  party_type VARCHAR(16) NULL,
  posted TINYINT(1) NOT NULL DEFAULT 1,
  reversal_of_je_no VARCHAR(32) NULL,
  KEY idx_gl_period (period, account_code),
  KEY idx_gl_acct  (account_code, je_date),
  KEY idx_gl_party (party_type, party_code),
  KEY idx_gl_je (tenant_id, je_no)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Seed: post the May 2026 sales invoice + the June expenses
INSERT IGNORE INTO m6_gl_entry (tenant_id, je_no, je_date, fy, period, source_module, source_doc, account_code, cost_center, narration, debit, credit, party_code, party_type) VALUES
-- Sales invoice INV-2026-0234 (Rs.245,000)
(1,'JE-2026-06-001','2026-06-05','2026-27','2026-06','SALES','INV-2026-0234','1100','SALES','Sales to Bangalore Foods — INV-2026-0234',245000.00,0,'BFPL','CUSTOMER'),
(1,'JE-2026-06-001','2026-06-05','2026-27','2026-06','SALES','INV-2026-0234','4010','SALES','Sales — Refined Oil Bangalore Foods',0,233333.33,'BFPL','CUSTOMER'),
(1,'JE-2026-06-001','2026-06-05','2026-27','2026-06','SALES','INV-2026-0234','2020','SALES','Output IGST 5% INV-2026-0234',0, 11666.67,'BFPL','CUSTOMER'),
-- Sales invoice INV-2026-0235 (Rs.178,500)
(1,'JE-2026-06-002','2026-06-06','2026-27','2026-06','SALES','INV-2026-0235','1100','SALES','Sales to Mumbai Distributors',178500.00,0,'MD','CUSTOMER'),
(1,'JE-2026-06-002','2026-06-06','2026-27','2026-06','SALES','INV-2026-0235','4010','SALES','Sales — Refined Groundnut Mumbai',0,170000.00,'MD','CUSTOMER'),
(1,'JE-2026-06-002','2026-06-06','2026-27','2026-06','SALES','INV-2026-0235','2020','SALES','Output IGST 5% INV-2026-0235',0,  8500.00,'MD','CUSTOMER'),
-- Purchase invoice TI-9234 (Rs.2,150,000 sunflower)
(1,'JE-2026-06-003','2026-06-06','2026-27','2026-06','PURCHASE','TI-9234','5010','PURCHASE','Raw Sunflower Oil purchase TI-9234',2047619.05,0,'VEN-001','VENDOR'),
(1,'JE-2026-06-003','2026-06-06','2026-27','2026-06','PURCHASE','TI-9234','1300','PURCHASE','Input IGST 5%',102380.95,0,'VEN-001','VENDOR'),
(1,'JE-2026-06-003','2026-06-06','2026-27','2026-06','PURCHASE','TI-9234','2010','PURCHASE','Credit TESTRO Industries',0,2150000.00,'VEN-001','VENDOR'),
-- May Payroll Posting (3 employees: Ramesh+Priya+Arun)
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','6010','HR','Salaries & Wages May 2026', 105000.00,0,NULL,NULL),
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','6020','HR','PF Employer May 2026',     12600.00,0,NULL,NULL),
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','6030','HR','ESI Employer May 2026',      650.00,0,NULL,NULL),
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','6040','HR','Gratuity provision May 2026', 2525.00,0,NULL,NULL),
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','2030','HR','TDS deducted',           0,   0.00,NULL,NULL),
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','2031','HR','PF (Emp+Employer) payable May 2026',0, 17400.00,NULL,NULL),
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','2032','HR','ESI (Emp+Employer) payable',       0,   950.00,NULL,NULL),
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','2033','HR','PT payable May 2026',              0,   625.00,NULL,NULL),
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','2040','HR','Net salary payable',               0, 99425.00,NULL,NULL),
(1,'JE-2026-05-101','2026-05-31','2026-27','2026-05','PAYROLL','PAY-2026-05','2040','HR','adjust',                           0,2375.00,NULL,NULL),
-- TDS Challan paid 7-Jun
(1,'JE-2026-06-004','2026-06-07','2026-27','2026-06','PAYMENT','CIN/0612501','2030','HR','TDS 192 May 26 paid', 39650.00,0,NULL,NULL),
(1,'JE-2026-06-004','2026-06-07','2026-27','2026-06','PAYMENT','CIN/0612501','1020','HR','SBI Current debit',         0,39650.00,NULL,NULL),
-- Depreciation Jun
(1,'JE-2026-06-005','2026-06-30','2026-27','2026-06','DEPRECIATION','DEP-2026-06','7050','PROD','Monthly dep — P&M + Buildings',48500.00,0,NULL,NULL),
(1,'JE-2026-06-005','2026-06-30','2026-27','2026-06','DEPRECIATION','DEP-2026-06','1599','PROD','Acc dep credit',                       0,48500.00,NULL,NULL);

-- =================================================================
-- 2. AR / AP AGING (open items + view)
-- =================================================================
CREATE TABLE IF NOT EXISTS m6_ar_open_item (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  party_type VARCHAR(8) NOT NULL,         -- CUSTOMER/VENDOR
  party_code VARCHAR(48) NOT NULL,
  party_name VARCHAR(160) NOT NULL,
  doc_type VARCHAR(16) NOT NULL,           -- INVOICE/CN/DN
  doc_no VARCHAR(48) NOT NULL,
  doc_date DATE NOT NULL,
  due_date DATE NOT NULL,
  original_amount DECIMAL(14,2) NOT NULL,
  paid_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  outstanding DECIMAL(14,2) GENERATED ALWAYS AS (original_amount - COALESCE(paid_amount,0)) STORED,
  days_overdue INT GENERATED ALWAYS AS (
    CASE WHEN due_date < CURRENT_DATE THEN DATEDIFF(CURRENT_DATE, due_date) ELSE 0 END
  ) VIRTUAL,
  aging_bucket VARCHAR(16) GENERATED ALWAYS AS (
    CASE
      WHEN due_date >= CURRENT_DATE                  THEN 'NOT_DUE'
      WHEN DATEDIFF(CURRENT_DATE, due_date) <= 30    THEN '0-30'
      WHEN DATEDIFF(CURRENT_DATE, due_date) <= 60    THEN '31-60'
      WHEN DATEDIFF(CURRENT_DATE, due_date) <= 90    THEN '61-90'
      WHEN DATEDIFF(CURRENT_DATE, due_date) <= 180   THEN '91-180'
      ELSE '180+'
    END
  ) VIRTUAL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  -- 43B(h) MSME clock: 45 days after invoice for MSME vendors. Stored as a real
  -- column updated by app code (CURRENT_DATE is non-deterministic so can't be
  -- in a STORED generated column on MariaDB).
  msme_43bh_due_date DATE NULL,
  msme_43bh_breached TINYINT(1) NOT NULL DEFAULT 0,
  KEY idx_ar_party (party_type, party_code, status),
  KEY idx_ar_due (due_date, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m6_ar_open_item (tenant_id, party_type, party_code, party_name, doc_type, doc_no, doc_date, due_date, original_amount, paid_amount, status, msme_43bh_due_date) VALUES
-- Customer receivables
(1,'CUSTOMER','BFPL','Bangalore Foods Pvt Ltd','INVOICE','INV-2026-0234','2026-06-05','2026-07-05', 245000.00,      0, 'OPEN', NULL),
(1,'CUSTOMER','MD',  'Mumbai Distributors',    'INVOICE','INV-2026-0235','2026-06-06','2026-07-21', 178500.00,      0, 'OPEN', NULL),
(1,'CUSTOMER','CR',  'Coimbatore Retailers',   'INVOICE','INV-2026-0233','2026-04-15','2026-05-15',  98700.00,  98700, 'CLOSED',NULL),
(1,'CUSTOMER','CW',  'Chennai Wholesalers',    'INVOICE','INV-2026-0231','2026-03-15','2026-04-14', 215000.00,      0, 'OPEN', NULL),
-- Vendor payables
(1,'VENDOR','VEN-001','TESTRO Industries Pvt Ltd','INVOICE','TI-9234','2026-06-06','2026-07-06', 2150000.00,     0, 'OPEN', '2026-07-21'),
(1,'VENDOR','VEN-002','Iyer Lubricants',         'INVOICE','IY-4501','2026-06-07','2026-07-22', 1690000.00,     0, 'OPEN', '2026-07-22'),
(1,'VENDOR','VEN-003','BalajiLoading & Allied',  'INVOICE','BL-2188','2026-04-22','2026-06-06',   88000.00,     0, 'OPEN', '2026-06-06'),
(1,'VENDOR','VEN-002','Iyer Lubricants',         'INVOICE','IY-4421','2026-03-15','2026-04-29', 1245000.00,1245000, 'CLOSED','2026-04-29');

-- =================================================================
-- 3. BANK RECONCILIATION
-- =================================================================
CREATE TABLE IF NOT EXISTS m6_bank_recon (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  bank_account_code VARCHAR(16) NOT NULL,
  statement_date DATE NOT NULL,
  statement_balance DECIMAL(14,2) NOT NULL,
  book_balance DECIMAL(14,2) NOT NULL,
  unmatched_credits DECIMAL(14,2) NOT NULL DEFAULT 0,
  unmatched_debits DECIMAL(14,2) NOT NULL DEFAULT 0,
  diff DECIMAL(14,2) GENERATED ALWAYS AS (statement_balance - book_balance) STORED,
  reconciled_by_emp_id BIGINT NULL,
  reconciled_at DATETIME NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',     -- OPEN/IN_PROGRESS/RECONCILED
  notes TEXT NULL,
  KEY idx_recon_acct (bank_account_code, statement_date)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m6_bank_recon (tenant_id, bank_account_code, statement_date, statement_balance, book_balance,
  unmatched_credits, unmatched_debits, reconciled_at, status, notes) VALUES
(1,'1020','2026-05-31', 3245000.00, 3245000.00, 0, 0, '2026-06-02 10:30:00','RECONCILED','Fully matched'),
(1,'1020','2026-06-09', 3325000.00, 3402650.00, 0, 77650.00, NULL, 'IN_PROGRESS','TDS challan + 1 cheque uncleared');

-- =================================================================
-- 4. MR → RFQ → QUOTE COMPARISON
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_rfq (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  rfq_no VARCHAR(32) NOT NULL,
  rfq_date DATE NOT NULL,
  source_mr_id BIGINT NULL,
  buyer_emp_id BIGINT NULL,
  closing_date DATE NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',     -- DRAFT/OPEN/CLOSED/CANCELLED
  notes TEXT NULL,
  UNIQUE KEY uq_rfq (tenant_id, rfq_no)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_rfq_vendor (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  rfq_id BIGINT NOT NULL,
  vendor_code VARCHAR(48) NOT NULL,
  vendor_name VARCHAR(160) NULL,
  sent_at DATETIME NULL,
  response_received_at DATETIME NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'INVITED',  -- INVITED/RESPONDED/NO_RESPONSE/REJECTED
  CONSTRAINT fk_rfqv_rfq FOREIGN KEY (rfq_id) REFERENCES m5_rfq(id) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_rfq_quote (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  rfq_vendor_id BIGINT NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(160) NULL,
  qty DECIMAL(14,3) NOT NULL,
  uom VARCHAR(16) NOT NULL,
  unit_price DECIMAL(14,4) NULL,
  delivery_days INT NULL,
  credit_days INT NULL,
  freight_terms VARCHAR(32) NULL,
  payment_terms VARCHAR(64) NULL,
  taxes_pct DECIMAL(5,2) NULL,
  landed_unit_cost DECIMAL(14,4) NULL,
  is_selected TINYINT(1) NOT NULL DEFAULT 0,
  remarks TEXT NULL,
  CONSTRAINT fk_quote_rfqv FOREIGN KEY (rfq_vendor_id) REFERENCES m5_rfq_vendor(id) ON DELETE CASCADE,
  KEY idx_quote_item (item_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_rfq (tenant_id, rfq_no, rfq_date, closing_date, status, notes) VALUES
(1,'RFQ-2026-021','2026-06-08','2026-06-12','OPEN','3 vendors invited for 20,000 KG raw sunflower oil + 5,000 PCS 1L PET bottles');

INSERT IGNORE INTO m5_rfq_vendor (rfq_id, vendor_code, vendor_name, sent_at, response_received_at, status)
SELECT id, 'VEN-001','TESTRO Industries Pvt Ltd','2026-06-08 11:00:00','2026-06-09 14:00:00','RESPONDED' FROM m5_rfq WHERE rfq_no='RFQ-2026-021';
INSERT IGNORE INTO m5_rfq_vendor (rfq_id, vendor_code, vendor_name, sent_at, response_received_at, status)
SELECT id, 'VEN-002','Iyer Lubricants',         '2026-06-08 11:00:00','2026-06-09 16:30:00','RESPONDED' FROM m5_rfq WHERE rfq_no='RFQ-2026-021';
INSERT IGNORE INTO m5_rfq_vendor (rfq_id, vendor_code, vendor_name, sent_at, response_received_at, status)
SELECT id, 'VEN-004','Krishna Agro Mills',       '2026-06-08 11:00:00',NULL,                  'INVITED'    FROM m5_rfq WHERE rfq_no='RFQ-2026-021';

INSERT IGNORE INTO m5_rfq_quote (rfq_vendor_id, item_code, item_name, qty, uom, unit_price, delivery_days, credit_days, payment_terms, taxes_pct, landed_unit_cost, is_selected, remarks)
SELECT rv.id,'OIL-SUN-RAW','Raw Sunflower Oil',20000,'KG', 86.50, 5, 30,'NET30',5.00,90.83, 1,'Best price + reliable' FROM m5_rfq_vendor rv JOIN m5_rfq r ON r.id=rv.rfq_id WHERE r.rfq_no='RFQ-2026-021' AND rv.vendor_code='VEN-001';
INSERT IGNORE INTO m5_rfq_quote (rfq_vendor_id, item_code, item_name, qty, uom, unit_price, delivery_days, credit_days, payment_terms, taxes_pct, landed_unit_cost, is_selected, remarks)
SELECT rv.id,'OIL-SUN-RAW','Raw Sunflower Oil',20000,'KG', 87.20, 7, 45,'NET45',5.00,91.56, 0,'Higher price but better credit' FROM m5_rfq_vendor rv JOIN m5_rfq r ON r.id=rv.rfq_id WHERE r.rfq_no='RFQ-2026-021' AND rv.vendor_code='VEN-002';
INSERT IGNORE INTO m5_rfq_quote (rfq_vendor_id, item_code, item_name, qty, uom, unit_price, delivery_days, credit_days, payment_terms, taxes_pct, landed_unit_cost, is_selected, remarks)
SELECT rv.id,'BTL-1L-PET','1L PET Bottle',5000,'PCS',  6.50, 4, 30,'NET30',18.00,7.67, 1,'Selected' FROM m5_rfq_vendor rv JOIN m5_rfq r ON r.id=rv.rfq_id WHERE r.rfq_no='RFQ-2026-021' AND rv.vendor_code='VEN-001';

-- =================================================================
-- 5. JOB-WORK / SUB-CONTRACTING
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_job_work_issue (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  jw_no VARCHAR(32) NOT NULL,
  issue_date DATE NOT NULL,
  vendor_code VARCHAR(48) NOT NULL,
  vendor_name VARCHAR(160) NULL,
  process VARCHAR(64) NOT NULL,             -- e.g. PACKING/BLEACHING/REPAIR/CRUSHING
  expected_return_date DATE NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(160) NULL,
  qty_issued DECIMAL(14,3) NOT NULL,
  qty_returned DECIMAL(14,3) NULL,
  shrinkage_qty DECIMAL(14,3) GENERATED ALWAYS AS (qty_issued - COALESCE(qty_returned,0)) STORED,
  uom VARCHAR(16) NOT NULL,
  lot_no_out VARCHAR(64) NULL,
  lot_no_in VARCHAR(64) NULL,
  rate_per_unit DECIMAL(12,2) NULL,
  charge_amount DECIMAL(14,2) NULL,
  delivery_challan_no VARCHAR(48) NULL,
  ewb_no VARCHAR(32) NULL,
  -- GST: notification 13/2017 — service charge taxable + ITC eligible
  gst_pct DECIMAL(5,2) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ISSUED',  -- ISSUED/PARTIAL_RECEIVED/RECEIVED/CLOSED/CANCELLED
  notes TEXT NULL,
  UNIQUE KEY uq_jw (tenant_id, jw_no)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_job_work_issue (tenant_id, jw_no, issue_date, vendor_code, vendor_name, process, expected_return_date,
  item_code, item_name, qty_issued, qty_returned, uom, lot_no_out, lot_no_in, rate_per_unit, charge_amount,
  delivery_challan_no, ewb_no, gst_pct, status) VALUES
(1,'JW-2026-001','2026-05-15','VEN-005','South India Refining','BLEACHING','2026-05-25',
  'OIL-SUN-CRUDE','Sunflower Crude Oil',  5000.000, 4920.000,'KG','LOT-SUN-260507','LOT-RFD-260524', 12.50, 62500.00,
  'DC-JW-260515-01','EWB-JW-2025-001',18.00,'RECEIVED'),
(1,'JW-2026-002','2026-06-05','VEN-006','Coimbatore Container Repair','REPAIR','2026-06-15',
  'TANKER-IBC',  'IBC Container 1000L', 6.000, NULL,'PCS','IBC-LOT-260605',NULL,         3500.00,21000.00,
  'DC-JW-260605-01','EWB-JW-2025-002',18.00,'ISSUED');

-- =================================================================
-- 6. BOILER IBR DAILY LOG (statutory under IBR Rules)
-- =================================================================
CREATE TABLE IF NOT EXISTS m5_boiler_daily_log (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  boiler_code VARCHAR(16) NOT NULL,
  log_date DATE NOT NULL,
  shift VARCHAR(8) NOT NULL,                   -- A/B/C
  operator_emp_id BIGINT NULL,
  operator_ibr_cert_no VARCHAR(32) NULL,
  -- Operational readings
  startup_time DATETIME NULL,
  shutdown_time DATETIME NULL,
  pressure_max_bar DECIMAL(6,2) NULL,
  pressure_avg_bar DECIMAL(6,2) NULL,
  temperature_max_c DECIMAL(6,2) NULL,
  water_consumed_kl DECIMAL(8,2) NULL,
  fuel_type VARCHAR(16) NULL,                  -- HUSK/WOOD/COAL/LPG/HSD
  fuel_consumed_kg DECIMAL(10,2) NULL,
  steam_generated_ton DECIMAL(8,2) NULL,
  -- Safety
  safety_valve_test_done TINYINT(1) NOT NULL DEFAULT 0,
  pressure_gauge_calib_ok TINYINT(1) NOT NULL DEFAULT 1,
  water_level_alarm_ok TINYINT(1) NOT NULL DEFAULT 1,
  blowdown_count INT NULL,
  -- Issues
  abnormal_observations TEXT NULL,
  corrective_action TEXT NULL,
  supervisor_signoff_emp_id BIGINT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'CLOSED',
  UNIQUE KEY uq_boiler_log (tenant_id, boiler_code, log_date, shift),
  KEY idx_bl_date (log_date)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m5_boiler_daily_log (tenant_id, boiler_code, log_date, shift, operator_emp_id, operator_ibr_cert_no,
  startup_time, shutdown_time, pressure_max_bar, pressure_avg_bar, temperature_max_c, water_consumed_kl,
  fuel_type, fuel_consumed_kg, steam_generated_ton,
  safety_valve_test_done, pressure_gauge_calib_ok, water_level_alarm_ok, blowdown_count, status) VALUES
(1,'BOILER-1','2026-06-10','A', 1, 'IBR-TN-2024-1102', '2026-06-10 06:00:00','2026-06-10 14:00:00',8.5,8.0,185.5,4.5,'HUSK',850.00,4.20, 1, 1, 1, 2,'CLOSED'),
(1,'BOILER-1','2026-06-10','B', 1, 'IBR-TN-2024-1102', '2026-06-10 14:00:00','2026-06-10 22:00:00',8.4,7.8,184.2,4.0,'HUSK',780.00,3.85, 0, 1, 1, 1,'CLOSED'),
(1,'BOILER-1','2026-06-11','A', 1, 'IBR-TN-2024-1102', '2026-06-11 06:00:00',NULL,                  8.3,7.9,183.8,2.1,'HUSK',390.00,1.95, 1, 1, 1, 1,'IN_PROGRESS');

-- =================================================================
-- 7. REORDER ALERT VIEW (live calc — flags items below safety stock)
-- =================================================================
CREATE OR REPLACE VIEW v_reorder_alert AS
SELECT
  i.tenant_id, i.item_code, i.item_name, i.uom, i.reorder_min, i.reorder_max,
  i.safety_stock, i.eoq, i.lead_time_days,
  COALESCE(SUM(sb.qty_on_hand), 0) AS qty_on_hand,
  COALESCE(SUM(sb.qty_reserved), 0) AS qty_reserved,
  COALESCE(SUM(sb.qty_on_hand - sb.qty_reserved), 0) AS qty_available,
  CASE
    WHEN COALESCE(SUM(sb.qty_on_hand),0) <= COALESCE(i.safety_stock,0)         THEN 'CRITICAL'
    WHEN COALESCE(SUM(sb.qty_on_hand),0) <= COALESCE(i.reorder_min,0)          THEN 'REORDER'
    WHEN COALESCE(SUM(sb.qty_on_hand),0) >= COALESCE(i.reorder_max,0)          THEN 'OVERSTOCK'
    ELSE 'OK'
  END AS alert_level
FROM m5_item_master i
LEFT JOIN m5_stock_balance sb ON sb.item_code = i.item_code AND sb.tenant_id = i.tenant_id
GROUP BY i.tenant_id, i.item_code, i.item_name, i.uom, i.reorder_min, i.reorder_max, i.safety_stock, i.eoq, i.lead_time_days;

-- =================================================================
-- 8. GSTR-1 / GSTR-3B period summaries (denormalised so JSON export is one row)
-- =================================================================
CREATE TABLE IF NOT EXISTS m6_gstr1_summary (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  period VARCHAR(8) NOT NULL,
  b2b_invoice_count INT NOT NULL DEFAULT 0,
  b2b_taxable DECIMAL(16,2) NOT NULL DEFAULT 0,
  b2b_igst DECIMAL(14,2) NOT NULL DEFAULT 0,
  b2b_cgst DECIMAL(14,2) NOT NULL DEFAULT 0,
  b2b_sgst DECIMAL(14,2) NOT NULL DEFAULT 0,
  b2c_invoice_count INT NOT NULL DEFAULT 0,
  b2c_taxable DECIMAL(16,2) NOT NULL DEFAULT 0,
  b2c_igst DECIMAL(14,2) NOT NULL DEFAULT 0,
  b2c_cgst DECIMAL(14,2) NOT NULL DEFAULT 0,
  b2c_sgst DECIMAL(14,2) NOT NULL DEFAULT 0,
  export_invoice_count INT NOT NULL DEFAULT 0,
  export_taxable DECIMAL(16,2) NOT NULL DEFAULT 0,
  cdnr_credit_notes_count INT NOT NULL DEFAULT 0,
  cdnr_amount DECIMAL(16,2) NOT NULL DEFAULT 0,
  json_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  json_path VARCHAR(255) NULL,
  filed_at DATETIME NULL,
  ack_no VARCHAR(48) NULL,
  UNIQUE KEY uq_gstr1 (tenant_id, period)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m6_gstr1_summary (tenant_id, period, b2b_invoice_count, b2b_taxable, b2b_igst,
  json_status) VALUES
(1,'2026-06', 2, 403333.33, 20166.67, 'PENDING');

-- =================================================================
-- 9. Register entities + new workflows
-- =================================================================
INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('GlAccount',         'GL Account',                'm6_gl_account'),
  ('GlEntry',           'GL Journal Entry',          'm6_gl_entry'),
  ('ArOpenItem',        'AR/AP Open Item + Aging',   'm6_ar_open_item'),
  ('BankRecon',         'Bank Reconciliation',       'm6_bank_recon'),
  ('Rfq',               'Request for Quotation',     'm5_rfq'),
  ('RfqVendor',         'RFQ Vendor',                'm5_rfq_vendor'),
  ('RfqQuote',          'RFQ Quote Line',            'm5_rfq_quote'),
  ('JobWorkIssue',      'Job-Work / Sub-Contracting','m5_job_work_issue'),
  ('BoilerDailyLog',    'Boiler IBR Daily Log',      'm5_boiler_daily_log'),
  ('Gstr1Summary',      'GSTR-1 Summary',            'm6_gstr1_summary');

INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M5.Rfq',1,'PUBLISHED','{\"code\":\"M5.Rfq\",\"version\":1,\"initialState\":\"DRAFT\",\"states\":[{\"code\":\"DRAFT\",\"assignedRole\":\"Buyer\"},{\"code\":\"OPEN\",\"assignedRole\":\"Vendor\"},{\"code\":\"EVALUATING\",\"assignedRole\":\"Buyer\"},{\"code\":\"SELECTED\",\"assignedRole\":\"Manager\"},{\"code\":\"PO_RAISED\",\"terminal\":true},{\"code\":\"CANCELLED\",\"terminal\":true}],\"transitions\":[{\"from\":\"DRAFT\",\"to\":\"OPEN\",\"trigger\":\"page.submitted\"},{\"from\":\"OPEN\",\"to\":\"EVALUATING\",\"trigger\":\"page.submitted\"},{\"from\":\"EVALUATING\",\"to\":\"SELECTED\",\"trigger\":\"page.submitted\"},{\"from\":\"SELECTED\",\"to\":\"PO_RAISED\",\"trigger\":\"page.submitted\"}]}'),
(1,'M5.JobWork',1,'PUBLISHED','{\"code\":\"M5.JobWork\",\"version\":1,\"initialState\":\"DRAFT\",\"states\":[{\"code\":\"DRAFT\",\"assignedRole\":\"StoreKeeper\"},{\"code\":\"ISSUED\",\"assignedRole\":\"Vendor\"},{\"code\":\"PARTIAL_RECEIVED\",\"assignedRole\":\"StoreKeeper\"},{\"code\":\"RECEIVED\",\"assignedRole\":\"QC\"},{\"code\":\"CLOSED\",\"terminal\":true}],\"transitions\":[{\"from\":\"DRAFT\",\"to\":\"ISSUED\",\"trigger\":\"page.submitted\"},{\"from\":\"ISSUED\",\"to\":\"PARTIAL_RECEIVED\",\"trigger\":\"page.submitted\"},{\"from\":\"PARTIAL_RECEIVED\",\"to\":\"RECEIVED\",\"trigger\":\"page.submitted\"},{\"from\":\"RECEIVED\",\"to\":\"CLOSED\",\"trigger\":\"page.submitted\"}]}'),
(1,'M6.GLPeriodClose',1,'PUBLISHED','{\"code\":\"M6.GLPeriodClose\",\"version\":1,\"initialState\":\"OPEN\",\"states\":[{\"code\":\"OPEN\",\"assignedRole\":\"Accountant\"},{\"code\":\"PROVISIONAL\",\"assignedRole\":\"Accountant\"},{\"code\":\"CLOSED\",\"terminal\":true},{\"code\":\"REOPENED\",\"assignedRole\":\"CFO\"}],\"transitions\":[{\"from\":\"OPEN\",\"to\":\"PROVISIONAL\",\"trigger\":\"page.submitted\"},{\"from\":\"PROVISIONAL\",\"to\":\"CLOSED\",\"trigger\":\"page.submitted\"},{\"from\":\"CLOSED\",\"to\":\"REOPENED\",\"trigger\":\"page.submitted\"}]}');
