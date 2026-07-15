-- V49: Master pages depth — Driver, Vehicle, Weighbridge, Gate, Lane,
-- Parking Bay, Vendor. Add the standard ERP fields each needs and update
-- the list pages so users see them.
-- Cross-ref: Tally Prime Master + SAP Fleet Management + Marg Vendor Master
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- 1. DRIVER MASTER — add Aadhaar (full), PAN, photo, default-vehicle FK,
--    salary type, current address, joining-date
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_col;
DELIMITER $$
CREATE PROCEDURE add_col(IN _tbl VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
                          IN _col VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
                          IN _ddl VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema=DATABASE() AND table_name=_tbl AND column_name=_col) THEN
    SET @s = CONCAT('ALTER TABLE `', _tbl, '` ADD COLUMN ', _ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;

CALL add_col('dispatch_driver','aadhaar_full','aadhaar_full VARCHAR(20) NULL');
CALL add_col('dispatch_driver','pan_no','pan_no VARCHAR(16) NULL');
CALL add_col('dispatch_driver','photo_url','photo_url VARCHAR(512) NULL');
CALL add_col('dispatch_driver','default_vehicle_id','default_vehicle_id BIGINT NULL');
CALL add_col('dispatch_driver','dl_class','dl_class VARCHAR(16) NULL');
CALL add_col('dispatch_driver','date_of_joining','date_of_joining DATE NULL');
CALL add_col('dispatch_driver','salary_type','salary_type VARCHAR(16) NULL');
CALL add_col('dispatch_driver','salary_amount','salary_amount DECIMAL(10,2) NULL');
CALL add_col('dispatch_driver','current_address','current_address VARCHAR(512) NULL');
CALL add_col('dispatch_driver','status','status VARCHAR(16) NOT NULL DEFAULT ''ACTIVE''');

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('Driver','name','Name','STRING',1,1,1,1,'text',NULL),
  ('Driver','dl_number','DL Number','STRING',1,1,1,1,'text',NULL),
  ('Driver','dl_expiry','DL Expiry','DATE',1,1,0,0,'date',NULL),
  ('Driver','dl_class','DL Class','STRING',1,1,0,0,'select','["LMV","HMV","HTV","MCWG","Trans-Hazardous","Other"]'),
  ('Driver','primary_mobile','Mobile','STRING',1,1,1,1,'text',NULL),
  ('Driver','alt_mobile','Alt Mobile','STRING',0,1,1,0,'text',NULL),
  ('Driver','emergency_contact_name','Emergency Contact','STRING',0,0,1,0,'text',NULL),
  ('Driver','aadhaar_full','Aadhaar #','STRING',0,1,1,0,'text',NULL),
  ('Driver','pan_no','PAN','STRING',0,1,1,0,'text',NULL),
  ('Driver','default_vehicle_id','Default Vehicle','LONG',1,1,0,0,'entity-ref',NULL),
  ('Driver','date_of_joining','Joined On','DATE',0,1,0,0,'date',NULL),
  ('Driver','salary_type','Salary Type','STRING',1,1,0,0,'select','["MONTHLY","PER_TRIP","PER_KM","DAILY"]'),
  ('Driver','salary_amount','Salary Amount (Rs)','DECIMAL',0,1,0,0,'currency',NULL),
  ('Driver','current_address','Current Address','TEXT',0,0,1,0,'textarea',NULL),
  ('Driver','status','Status','STRING',1,1,0,0,'select','["ACTIVE","BLOCKED","INACTIVE","ON_LEAVE"]'),
  ('Driver','shift_pref','Shift Preference','STRING',1,1,0,0,'select','["DAY","NIGHT","ANY"]');

-- Page: Driver Master rich columns
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','Driver','select',JSON_ARRAY('id','name','primary_mobile','dl_number','dl_expiry','dl_class','default_vehicle_id','salary_type','status')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','id','label','#','sortable',true),
    JSON_OBJECT('field','name','label','Driver Name','sortable',true,'render','link','linkTo','page:pico:driver-detail?id={id}'),
    JSON_OBJECT('field','primary_mobile','label','Mobile','sortable',true),
    JSON_OBJECT('field','dl_number','label','DL #','sortable',true),
    JSON_OBJECT('field','dl_expiry','label','DL Expires','render','date','sortable',true),
    JSON_OBJECT('field','dl_class','label','DL Class','sortable',true),
    JSON_OBJECT('field','default_vehicle_id','label','Default Vehicle','sortable',true),
    JSON_OBJECT('field','salary_type','label','Salary Type','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('MONTHLY','blue','PER_TRIP','green','PER_KM','purple','DAILY','amber')),
    JSON_OBJECT('field','status','label','Status','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('ACTIVE','green','BLOCKED','red','INACTIVE','grey','ON_LEAVE','amber'))
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('ACTIVE','BLOCKED','INACTIVE','ON_LEAVE')),
    JSON_OBJECT('field','dl_class','label','DL Class','type','multi-select','options',JSON_ARRAY('LMV','HMV','HTV','MCWG','Trans-Hazardous')),
    JSON_OBJECT('field','_q','label','Search Name/Mobile/DL','type','text-contains','applyTo',JSON_ARRAY('name','primary_mobile','dl_number'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code='pico:driver-master';
UPDATE page_definition SET description='Driver master per Tally / Marg ERP — name, mobile, DL+expiry+class, default vehicle, salary type, status. Mobile + DL required.' WHERE code='pico:driver-master';

-- ---------------------------------------------------------------------------
-- 2. VEHICLE MASTER — add make, model, year, FC, permit, GPS device
-- ---------------------------------------------------------------------------
CALL add_col('dispatch_vehicle','make','make VARCHAR(64) NULL');
CALL add_col('dispatch_vehicle','model','model VARCHAR(64) NULL');
CALL add_col('dispatch_vehicle','year_of_mfg','year_of_mfg INT NULL');
CALL add_col('dispatch_vehicle','fc_expiry','fc_expiry DATE NULL');
CALL add_col('dispatch_vehicle','permit_no','permit_no VARCHAR(64) NULL');
CALL add_col('dispatch_vehicle','permit_expiry','permit_expiry DATE NULL');
CALL add_col('dispatch_vehicle','insurance_policy_no','insurance_policy_no VARCHAR(64) NULL');
CALL add_col('dispatch_vehicle','insurance_sum_insured','insurance_sum_insured DECIMAL(12,2) NULL');
CALL add_col('dispatch_vehicle','gps_device_id','gps_device_id VARCHAR(64) NULL');
CALL add_col('dispatch_vehicle','default_driver_id','default_driver_id BIGINT NULL');
CALL add_col('dispatch_vehicle','wheel_count','wheel_count INT NULL');

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('Vehicle','registration_no','Reg #','STRING',1,1,1,1,'text',NULL),
  ('Vehicle','vehicle_type','Type','STRING',1,1,0,1,'select','["TANKER","CONTAINER","OPEN","MINI","LCV","TRAILER"]'),
  ('Vehicle','ownership','Ownership','STRING',1,1,0,0,'select','["OWN","HIRED"]'),
  ('Vehicle','make','Make','STRING',1,1,1,0,'text',NULL),
  ('Vehicle','model','Model','STRING',1,1,1,0,'text',NULL),
  ('Vehicle','year_of_mfg','Year','INT',1,1,0,0,'number',NULL),
  ('Vehicle','wheel_count','Wheels','INT',1,1,0,0,'select','["4","6","10","12","14","16","18","22"]'),
  ('Vehicle','tare_weight_kg','Tare (kg)','DECIMAL',0,1,0,0,'number',NULL),
  ('Vehicle','capacity_kg','Capacity (kg)','DECIMAL',0,1,0,0,'number',NULL),
  ('Vehicle','transporter_name','Transporter / Hirer','STRING',1,1,1,0,'text',NULL),
  ('Vehicle','rc_expiry','RC Expiry','DATE',1,1,0,0,'date',NULL),
  ('Vehicle','insurance_expiry','Insurance Expiry','DATE',1,1,0,0,'date',NULL),
  ('Vehicle','insurance_policy_no','Insurance Policy #','STRING',0,1,1,0,'text',NULL),
  ('Vehicle','insurance_sum_insured','Sum Insured (Rs)','DECIMAL',0,1,0,0,'currency',NULL),
  ('Vehicle','fc_expiry','FC Expiry','DATE',1,1,0,0,'date',NULL),
  ('Vehicle','permit_no','Permit #','STRING',0,1,1,0,'text',NULL),
  ('Vehicle','permit_expiry','Permit Expiry','DATE',1,1,0,0,'date',NULL),
  ('Vehicle','puc_expiry','PUC Expiry','DATE',1,1,0,0,'date',NULL),
  ('Vehicle','gps_device_id','GPS Device #','STRING',0,1,1,0,'text',NULL),
  ('Vehicle','default_driver_id','Default Driver','LONG',1,1,0,0,'entity-ref',NULL),
  ('Vehicle','active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL);

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','Vehicle','select',JSON_ARRAY('id','registration_no','vehicle_type','ownership','make','model','wheel_count','capacity_kg','tare_weight_kg','rc_expiry','insurance_expiry','fc_expiry','permit_expiry','puc_expiry','active')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','registration_no','label','Reg #','sortable',true),
    JSON_OBJECT('field','vehicle_type','label','Type','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('TANKER','blue','CONTAINER','blue','OPEN','grey','MINI','grey','LCV','green','TRAILER','purple')),
    JSON_OBJECT('field','ownership','label','Owner','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('OWN','green','HIRED','amber')),
    JSON_OBJECT('field','make','label','Make','sortable',true),
    JSON_OBJECT('field','model','label','Model','sortable',true),
    JSON_OBJECT('field','wheel_count','label','Wheels','render','number'),
    JSON_OBJECT('field','capacity_kg','label','Capacity kg','render','number'),
    JSON_OBJECT('field','tare_weight_kg','label','Tare kg','render','number'),
    JSON_OBJECT('field','rc_expiry','label','RC','render','date'),
    JSON_OBJECT('field','insurance_expiry','label','Insurance','render','date'),
    JSON_OBJECT('field','fc_expiry','label','FC','render','date'),
    JSON_OBJECT('field','permit_expiry','label','Permit','render','date'),
    JSON_OBJECT('field','puc_expiry','label','PUC','render','date'),
    JSON_OBJECT('field','active','label','Active','render','badge','colorMap',JSON_OBJECT('1','green','0','red'))
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','vehicle_type','label','Type','type','multi-select','options',JSON_ARRAY('TANKER','CONTAINER','OPEN','MINI','LCV','TRAILER')),
    JSON_OBJECT('field','ownership','label','Ownership','type','multi-select','options',JSON_ARRAY('OWN','HIRED')),
    JSON_OBJECT('field','active','label','Active Only','type','bool-toggle'),
    JSON_OBJECT('field','_q','label','Search Reg #/Make','type','text-contains','applyTo',JSON_ARRAY('registration_no','make','model'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code='pico:vehicle-master';
UPDATE page_definition SET description='Vehicle master per SAP Fleet — registration, type, make/model/year, tare+capacity, RC/Insurance/FC/Permit/PUC expiries, GPS device. Expiry traffic-lights flag renewals.' WHERE code='pico:vehicle-master';

-- ---------------------------------------------------------------------------
-- 3. WEIGHBRIDGE MASTER — calibration, TCP feed, operator
-- ---------------------------------------------------------------------------
CALL add_col('dispatch_weighbridge','name','name VARCHAR(128) NULL');
CALL add_col('dispatch_weighbridge','location','location VARCHAR(128) NULL');
CALL add_col('dispatch_weighbridge','make','make VARCHAR(64) NULL');
CALL add_col('dispatch_weighbridge','model','model VARCHAR(64) NULL');
CALL add_col('dispatch_weighbridge','serial_no','serial_no VARCHAR(64) NULL');
CALL add_col('dispatch_weighbridge','tcp_ip','tcp_ip VARCHAR(32) NULL');
CALL add_col('dispatch_weighbridge','tcp_port','tcp_port INT NULL');
CALL add_col('dispatch_weighbridge','calibrated_on','calibrated_on DATE NULL');
CALL add_col('dispatch_weighbridge','calibration_cert_no','calibration_cert_no VARCHAR(64) NULL');
CALL add_col('dispatch_weighbridge','next_calibration_due','next_calibration_due DATE NULL');
CALL add_col('dispatch_weighbridge','default_operator_id','default_operator_id BIGINT NULL');

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('Weighbridge','code','Code','STRING',1,1,1,1,'text',NULL),
  ('Weighbridge','name','Name','STRING',1,1,1,0,'text',NULL),
  ('Weighbridge','location','Location','STRING',1,1,1,0,'text',NULL),
  ('Weighbridge','capacity_kg','Capacity (kg)','DECIMAL',0,1,0,1,'number',NULL),
  ('Weighbridge','tolerance_kg','Tolerance (kg)','INT',0,1,0,0,'number',NULL),
  ('Weighbridge','protocol','Protocol','STRING',1,1,0,0,'select','["RS232","RS485","TCP","MODBUS"]'),
  ('Weighbridge','tcp_ip','TCP IP','STRING',0,0,1,0,'text',NULL),
  ('Weighbridge','tcp_port','TCP Port','INT',0,1,0,0,'number',NULL),
  ('Weighbridge','make','Make','STRING',1,1,1,0,'text',NULL),
  ('Weighbridge','model','Model','STRING',0,1,1,0,'text',NULL),
  ('Weighbridge','serial_no','Serial #','STRING',0,1,1,0,'text',NULL),
  ('Weighbridge','calibrated_on','Calibrated On','DATE',1,1,0,0,'date',NULL),
  ('Weighbridge','calibration_cert_no','Calib. Cert #','STRING',0,0,1,0,'text',NULL),
  ('Weighbridge','next_calibration_due','Next Calibration','DATE',1,1,0,0,'date',NULL),
  ('Weighbridge','active','Active','BOOLEAN',1,1,0,0,'checkbox',NULL);

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','Weighbridge','select',JSON_ARRAY('id','code','name','location','capacity_kg','protocol','tcp_ip','tcp_port','calibrated_on','next_calibration_due','active')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','code','label','Code','sortable',true),
    JSON_OBJECT('field','name','label','Name','sortable',true),
    JSON_OBJECT('field','location','label','Location','sortable',true),
    JSON_OBJECT('field','capacity_kg','label','Capacity kg','render','number'),
    JSON_OBJECT('field','protocol','label','Protocol','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('TCP','green','MODBUS','green','RS232','grey','RS485','grey')),
    JSON_OBJECT('field','tcp_ip','label','TCP IP'),
    JSON_OBJECT('field','tcp_port','label','Port','render','number'),
    JSON_OBJECT('field','calibrated_on','label','Calibrated','render','date','sortable',true),
    JSON_OBJECT('field','next_calibration_due','label','Next Calib.','render','date','sortable',true),
    JSON_OBJECT('field','active','label','Active','render','badge','colorMap',JSON_OBJECT('1','green','0','red'))
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','protocol','label','Protocol','type','multi-select','options',JSON_ARRAY('RS232','RS485','TCP','MODBUS')),
    JSON_OBJECT('field','active','label','Active Only','type','bool-toggle')
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code='pico:weighbridge-master';
UPDATE page_definition SET description='Weighbridge master — code, capacity, make/model/serial, TCP IP for live feed, calibration date + next due. Calibration expiry triggers compliance alert.' WHERE code='pico:weighbridge-master';

-- ---------------------------------------------------------------------------
-- 4. VENDOR MASTER (m0_vendor) — GST/PAN/bank/credit/payment terms
-- ---------------------------------------------------------------------------
CALL add_col('m0_vendor','vendor_code','vendor_code VARCHAR(32) NULL');
CALL add_col('m0_vendor','vendor_name','vendor_name VARCHAR(255) NULL');
CALL add_col('m0_vendor','gstin','gstin VARCHAR(16) NULL');
CALL add_col('m0_vendor','pan','pan VARCHAR(16) NULL');
CALL add_col('m0_vendor','bank_account','bank_account VARCHAR(32) NULL');
CALL add_col('m0_vendor','bank_ifsc','bank_ifsc VARCHAR(16) NULL');
CALL add_col('m0_vendor','payment_terms_days','payment_terms_days INT NOT NULL DEFAULT 30');
CALL add_col('m0_vendor','credit_limit','credit_limit DECIMAL(12,2) NULL');
CALL add_col('m0_vendor','contact_person','contact_person VARCHAR(128) NULL');
CALL add_col('m0_vendor','contact_phone','contact_phone VARCHAR(16) NULL');
CALL add_col('m0_vendor','contact_email','contact_email VARCHAR(128) NULL');
CALL add_col('m0_vendor','address','address VARCHAR(512) NULL');
CALL add_col('m0_vendor','msme_regn','msme_regn VARCHAR(64) NULL');
CALL add_col('m0_vendor','default_item_category','default_item_category VARCHAR(32) NULL');

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('Vendor','vendor_code','Vendor Code','STRING',1,1,1,1,'text',NULL),
  ('Vendor','vendor_name','Vendor Name','STRING',1,1,1,1,'text',NULL),
  ('Vendor','gstin','GSTIN','STRING',1,1,1,0,'text',NULL),
  ('Vendor','pan','PAN','STRING',0,1,1,0,'text',NULL),
  ('Vendor','bank_account','Bank A/c','STRING',0,1,1,0,'text',NULL),
  ('Vendor','bank_ifsc','IFSC','STRING',0,1,1,0,'text',NULL),
  ('Vendor','payment_terms_days','Pay Terms (days)','INT',1,1,0,0,'number',NULL),
  ('Vendor','credit_limit','Credit Limit (Rs)','DECIMAL',0,1,0,0,'currency',NULL),
  ('Vendor','contact_person','Contact','STRING',0,1,1,0,'text',NULL),
  ('Vendor','contact_phone','Phone','STRING',1,1,1,0,'text',NULL),
  ('Vendor','contact_email','Email','STRING',0,1,1,0,'email',NULL),
  ('Vendor','address','Address','TEXT',0,0,1,0,'textarea',NULL),
  ('Vendor','msme_regn','MSME Regn','STRING',1,1,1,0,'text',NULL),
  ('Vendor','default_item_category','Default Item Cat','STRING',1,1,0,0,'select','["RM_OIL","RM_OTHER","PACK_CARTON","PACK_BOTTLE","PACK_SMALL","SERVICE","OTHER"]');

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','Vendor','select',JSON_ARRAY('id','vendor_code','vendor_name','gstin','pan','payment_terms_days','credit_limit','contact_person','contact_phone','default_item_category','msme_regn')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','vendor_code','label','Code','sortable',true),
    JSON_OBJECT('field','vendor_name','label','Vendor Name','sortable',true),
    JSON_OBJECT('field','gstin','label','GSTIN','sortable',true),
    JSON_OBJECT('field','pan','label','PAN'),
    JSON_OBJECT('field','default_item_category','label','Category','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('RM_OIL','blue','RM_OTHER','blue','PACK_CARTON','green','PACK_BOTTLE','green','PACK_SMALL','green','SERVICE','purple','OTHER','grey')),
    JSON_OBJECT('field','payment_terms_days','label','Pay Terms','render','number'),
    JSON_OBJECT('field','credit_limit','label','Credit Limit Rs','render','number'),
    JSON_OBJECT('field','contact_person','label','Contact'),
    JSON_OBJECT('field','contact_phone','label','Phone'),
    JSON_OBJECT('field','msme_regn','label','MSME')
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','default_item_category','label','Category','type','multi-select','options',JSON_ARRAY('RM_OIL','RM_OTHER','PACK_CARTON','PACK_BOTTLE','PACK_SMALL','SERVICE','OTHER')),
    JSON_OBJECT('field','_q','label','Search Code/Name/GSTIN','type','text-contains','applyTo',JSON_ARRAY('vendor_code','vendor_name','gstin'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code='pico:vendor-list';
UPDATE page_definition SET description='Vendor master per Tally Prime — code, name, GSTIN+PAN, bank a/c+IFSC, pay terms, credit limit, default item category, MSME regn. Used in PO, payments, GST.' WHERE code='pico:vendor-list';

DROP PROCEDURE add_col;
