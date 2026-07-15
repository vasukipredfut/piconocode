-- =====================================================================
-- V24 — Tables for:
--   1. Integration field mapping (our entity.field -> SAP/Tally/NIC field)
--   2. Eway bill (M10 outbound) — full schema + workflow
--   3. Statutory return filing rows (companion to m7_statutory_return_catalog)
-- =====================================================================

-- 1. Integration field mapping
CREATE TABLE IF NOT EXISTS integration_field_mapping (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  instance_code VARCHAR(48) NOT NULL,
  our_entity VARCHAR(64) NOT NULL,
  our_field VARCHAR(64) NOT NULL,
  their_field VARCHAR(128) NOT NULL,
  transform_expr VARCHAR(255) NULL,                      -- e.g. "upper", "trim", "x*100"
  direction VARCHAR(8) NOT NULL DEFAULT 'OUT',           -- IN / OUT / BOTH
  KEY idx_ifm_inst (instance_code, our_entity)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Seed some sample mappings for the Tally + SAP + NIC instances if they exist
INSERT IGNORE INTO integration_field_mapping (instance_code, our_entity, our_field, their_field, direction) VALUES
('TALLY_PROD',  'SalesInvoice', 'invoice_no',   'VOUCHERNUMBER',     'OUT'),
('TALLY_PROD',  'SalesInvoice', 'customer_name','PARTYLEDGERNAME',   'OUT'),
('TALLY_PROD',  'SalesInvoice', 'amount',       'AMOUNT',            'OUT'),
('TALLY_PROD',  'SalesInvoice', 'invoice_date', 'DATE',              'OUT'),
('SAP_PROD',    'Item',         'item_code',    'Product',           'BOTH'),
('SAP_PROD',    'Item',         'item_name',    'ProductDescription','BOTH'),
('SAP_PROD',    'Item',         'uom',          'BaseUnit',          'BOTH'),
('SAP_PROD',    'Item',         'hsn_code',     'HSNCode',           'BOTH'),
('NIC_PROD',    'SalesInvoice', 'invoice_no',   'DocNo',             'OUT'),
('NIC_PROD',    'SalesInvoice', 'gstin_buyer',  'BuyerGstin',        'OUT'),
('NIC_PROD',    'SalesInvoice', 'total_value',  'TotInvVal',         'OUT'),
('MSG91_PROD',  'GatePass',     'driver_phone', 'mobile',            'OUT');

-- 2. Eway bill
CREATE TABLE IF NOT EXISTS m10_eway_bill (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  ewb_number VARCHAR(32) NOT NULL,
  source_doc_type VARCHAR(16) NOT NULL,                 -- INVOICE / DELIVERY_CHALLAN / BILL_OF_SUPPLY
  source_doc_no VARCHAR(64) NULL,
  source_doc_date DATE NULL,
  supply_type VARCHAR(8) NOT NULL,                       -- OUTWARD / INWARD
  sub_supply_type VARCHAR(16) NOT NULL,                 -- SUPPLY / EXPORT / JOB_WORK
  doc_value DECIMAL(14,2) NOT NULL,
  from_gstin VARCHAR(16) NULL,
  from_address VARCHAR(255) NULL,
  from_pincode VARCHAR(10) NULL,
  from_state_code VARCHAR(8) NULL,
  to_gstin VARCHAR(16) NULL,
  to_address VARCHAR(255) NULL,
  to_pincode VARCHAR(10) NULL,
  to_state_code VARCHAR(8) NULL,
  transport_mode VARCHAR(8) NULL,                        -- ROAD / RAIL / AIR / SHIP
  vehicle_no VARCHAR(16) NULL,
  transporter_id VARCHAR(20) NULL,
  transporter_name VARCHAR(64) NULL,
  distance_km INT NULL,
  irn VARCHAR(64) NULL,                                  -- if linked to e-invoice
  ewb_status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',       -- DRAFT/GENERATED/CANCELLED/EXPIRED
  generated_at TIMESTAMP NULL,
  valid_upto TIMESTAMP NULL,
  cancelled_at TIMESTAMP NULL,
  cancel_reason VARCHAR(128) NULL,
  notes TEXT NULL,
  workflow_instance_id BIGINT NULL,
  current_step VARCHAR(64) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_ewb_number (tenant_id, ewb_number),
  KEY idx_ewb_status (tenant_id, ewb_status, generated_at)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m10_eway_bill (tenant_id, ewb_number, source_doc_type, source_doc_no, source_doc_date,
  supply_type, sub_supply_type, doc_value, from_gstin, from_state_code, to_gstin, to_state_code,
  transport_mode, vehicle_no, distance_km, ewb_status, generated_at, valid_upto) VALUES
(1,'EWB-2026-001','INVOICE','INV-2026-0234','2026-06-05','OUTWARD','SUPPLY', 245000.00,
 '33AAACS1234L1ZA','33','29AAACX1234L1ZB','29','ROAD','TN37BC0011', 412, 'GENERATED','2026-06-05 09:30:00','2026-06-09 09:30:00'),
(1,'EWB-2026-002','INVOICE','INV-2026-0235','2026-06-06','OUTWARD','SUPPLY', 178500.00,
 '33AAACS1234L1ZA','33','27AAACY1234L1ZC','27','ROAD','TN37BC0012', 1240,'GENERATED','2026-06-06 14:15:00','2026-06-13 14:15:00'),
(1,'EWB-2026-003','INVOICE','INV-2026-0236','2026-06-08','OUTWARD','SUPPLY',  98700.00,
 '33AAACS1234L1ZA','33','33AAACZ1234L1ZD','33','ROAD','TN37BC0013', 122, 'DRAFT', NULL, NULL);

-- 3. Eway workflow
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
(1,'M10.EwayBill',1,'PUBLISHED','{"code":"M10.EwayBill","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT","assignedRole":"Dispatch"},{"code":"SUBMITTED","assignedRole":"Manager"},{"code":"GENERATED","assignedRole":"Dispatch"},{"code":"CANCELLED","terminal":true},{"code":"EXPIRED","terminal":true}],"transitions":[{"from":"DRAFT","to":"SUBMITTED","trigger":"page.submitted","pageCode":"pico:eway-create"},{"from":"SUBMITTED","to":"GENERATED","trigger":"workflow.transition","pageCode":"pico:eway-create"},{"from":"GENERATED","to":"CANCELLED","trigger":"page.submitted","pageCode":"pico:eway-cancel"},{"from":"GENERATED","to":"EXPIRED","trigger":"system.schedule"}]}');

-- 4. Register Eway entity + create list page (via raw insert; the page-definition
--    initialiser will pick up body_json from page_version)
INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('EwayBill','E-Way Bill','m10_eway_bill'),
  ('IntegrationFieldMapping','Integration Field Mapping','integration_field_mapping');

-- 5. Statutory return filing rows companion (one row per period instance of a return)
CREATE TABLE IF NOT EXISTS m7_statutory_return_filing (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  return_code VARCHAR(32) NOT NULL,                      -- FK to catalog
  period VARCHAR(8) NOT NULL,                             -- YYYY-MM or YYYY-Q1 / FY 2025-26
  due_date DATE NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',         -- PENDING/FILED/LATE/N/A
  filed_at TIMESTAMP NULL,
  filed_by VARCHAR(64) NULL,
  ack_no VARCHAR(64) NULL,
  amount DECIMAL(14,2) NULL,
  challan_no VARCHAR(64) NULL,
  notes TEXT NULL,
  UNIQUE KEY uq_filing (tenant_id, return_code, period),
  KEY idx_filing_due (due_date, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO m7_statutory_return_filing (tenant_id, return_code, period, due_date, status) VALUES
(1,'PF_ECR','2026-05','2026-06-15','PENDING'),
(1,'ESI_CHALLAN','2026-05','2026-06-15','PENDING'),
(1,'GSTR1','2026-05','2026-06-11','PENDING'),
(1,'GSTR3B','2026-05','2026-06-20','PENDING'),
(1,'TDS_24Q','2026-Q1','2026-07-31','PENDING'),
(1,'PT_RETURN','2026-04','2026-05-15','FILED'),
(1,'MSME_1','2026-H1','2026-04-30','FILED');

INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES
  ('StatutoryReturnFiling','Statutory Return Filing','m7_statutory_return_filing');
