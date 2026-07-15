-- V44: ERP-grade depth on master pages + inline actions on log pages.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) SKU Master — add ERP fields used by every Indian distributor app
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_sku_col;
DELIMITER $$
CREATE PROCEDURE add_sku_col(IN col VARCHAR(64), IN ddl VARCHAR(255))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema=DATABASE() AND table_name='m5_sku_master' AND column_name=col) THEN
    SET @s = CONCAT('ALTER TABLE m5_sku_master ADD COLUMN ', ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;
-- Per Tally Prime / Zoho Inventory / Marg ERP "Stock Item Master":
CALL add_sku_col('uom_primary',     'uom_primary VARCHAR(16) NOT NULL DEFAULT ''KG''');
CALL add_sku_col('uom_secondary',   'uom_secondary VARCHAR(16) NULL');
CALL add_sku_col('uom_conversion',  'uom_conversion DECIMAL(10,4) NULL COMMENT ''e.g. 1 case = 12 pcs''');
CALL add_sku_col('shelf_life_days', 'shelf_life_days INT NULL');
CALL add_sku_col('batch_tracked',   'batch_tracked TINYINT(1) NOT NULL DEFAULT 1');
CALL add_sku_col('fefo_enabled',    'fefo_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''First-Expiry-First-Out picking''');
CALL add_sku_col('reorder_point',   'reorder_point INT NULL');
CALL add_sku_col('safety_stock',    'safety_stock INT NULL');
CALL add_sku_col('abc_class',       'abc_class VARCHAR(1) NULL COMMENT ''A/B/C by volume''');
CALL add_sku_col('cess_pct',        'cess_pct DECIMAL(4,2) NOT NULL DEFAULT 0');
CALL add_sku_col('purchase_price',  'purchase_price DECIMAL(10,2) NULL');
CALL add_sku_col('sale_price',      'sale_price DECIMAL(10,2) NULL');
DROP PROCEDURE add_sku_col;

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('SkuMaster','uom_primary','Primary UOM','STRING',1,1,0,0,'select','["KG","L","ML","NOS","CASE","BUNDLE","BOX"]'),
  ('SkuMaster','uom_secondary','Secondary UOM','STRING',0,1,0,0,'select','["KG","L","ML","NOS","CASE","BUNDLE","BOX"]'),
  ('SkuMaster','uom_conversion','UOM Conversion','DECIMAL',0,1,0,0,'number',NULL),
  ('SkuMaster','shelf_life_days','Shelf Life (days)','INT',1,1,0,0,'number',NULL),
  ('SkuMaster','batch_tracked','Batch Tracked','BOOLEAN',1,1,0,0,'checkbox',NULL),
  ('SkuMaster','fefo_enabled','FEFO Picking','BOOLEAN',1,1,0,0,'checkbox',NULL),
  ('SkuMaster','reorder_point','Reorder Point','INT',1,1,0,0,'number',NULL),
  ('SkuMaster','safety_stock','Safety Stock','INT',1,1,0,0,'number',NULL),
  ('SkuMaster','abc_class','ABC Class','STRING',1,1,0,0,'select','["A","B","C"]'),
  ('SkuMaster','cess_pct','Cess %','DECIMAL',0,1,0,0,'number',NULL),
  ('SkuMaster','purchase_price','Purchase Price','DECIMAL',1,1,0,0,'currency',NULL),
  ('SkuMaster','sale_price','Sale Price','DECIMAL',1,1,0,0,'currency',NULL);

-- Backfill sensible defaults for the 872 SKUs based on product family
UPDATE m5_sku_master SET uom_primary='L', shelf_life_days=540, batch_tracked=1, fefo_enabled=1, reorder_point=50, safety_stock=20, abc_class='A' WHERE product_family IN ('Gingelly','Groundnut','Coconut','Sunflower','Rice Bran','Mustard','Pooja Oil');
UPDATE m5_sku_master SET uom_primary='KG', shelf_life_days=720, batch_tracked=1, reorder_point=30, safety_stock=10, abc_class='B' WHERE product_family='Jaggery';
UPDATE m5_sku_master SET uom_primary='NOS', shelf_life_days=1095, batch_tracked=0, reorder_point=100, safety_stock=40, abc_class='C', cess_pct=0 WHERE product_family='Agarbatti';

-- Update SKU master page-def to expose the new columns
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','SkuMaster',
    'select', JSON_ARRAY('id','sku_code','brand','product_family','pack_size_ml','uom_primary','carton_weight_kg','barcode','offer_variant','bundle_pieces','mrp_inr','sale_price','hsn_code','gst_rate_pct','cess_pct','shelf_life_days','abc_class','reorder_point','batch_tracked','fefo_enabled','status')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','sku_code','label','SKU','sortable',true),
    JSON_OBJECT('field','brand','label','Brand','sortable',true),
    JSON_OBJECT('field','product_family','label','Family','sortable',true),
    JSON_OBJECT('field','pack_size_ml','label','Pack ml','render','number'),
    JSON_OBJECT('field','uom_primary','label','UOM'),
    JSON_OBJECT('field','carton_weight_kg','label','Carton kg','render','number'),
    JSON_OBJECT('field','barcode','label','Barcode (EAN-13)','sortable',true),
    JSON_OBJECT('field','offer_variant','label','Offer','sortable',true),
    JSON_OBJECT('field','mrp_inr','label','MRP ₹','render','number'),
    JSON_OBJECT('field','sale_price','label','Sale ₹','render','number'),
    JSON_OBJECT('field','hsn_code','label','HSN'),
    JSON_OBJECT('field','gst_rate_pct','label','GST %','render','number'),
    JSON_OBJECT('field','shelf_life_days','label','Shelf days','render','number'),
    JSON_OBJECT('field','abc_class','label','ABC','sortable',true),
    JSON_OBJECT('field','reorder_point','label','Reorder Pt','render','number'),
    JSON_OBJECT('field','status','label','Status','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','brand','label','Brand','type','multi-select','options',JSON_ARRAY('Aria','Pico Pure','Pico Gold','Pico Saver')),
    JSON_OBJECT('field','product_family','label','Family','type','multi-select','options',JSON_ARRAY('Gingelly','Groundnut','Coconut','Sunflower','Rice Bran','Mustard','Jaggery','Pooja Oil','Agarbatti')),
    JSON_OBJECT('field','offer_variant','label','Offer','type','multi-select','options',JSON_ARRAY('STANDARD','BUY1GET1','10PCT_OFF','20PCT_OFF','FESTIVE_PACK','COMBO_2L_500ML')),
    JSON_OBJECT('field','abc_class','label','ABC','type','multi-select','options',JSON_ARRAY('A','B','C')),
    JSON_OBJECT('field','_q','label','Search SKU/Barcode/Brand','type','text-contains','applyTo',JSON_ARRAY('sku_code','barcode','brand'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
WHERE pd.code='pico:sku-master';

-- ---------------------------------------------------------------------------
-- (b) OrderApp Dump — add route stops + ETA + dispatch ref
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_oa_col;
DELIMITER $$
CREATE PROCEDURE add_oa_col(IN col VARCHAR(64), IN ddl VARCHAR(255))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema=DATABASE() AND table_name='m5_orderapp_dump' AND column_name=col) THEN
    SET @s = CONCAT('ALTER TABLE m5_orderapp_dump ADD COLUMN ', ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;
CALL add_oa_col('route_stops',    'route_stops INT NULL');
CALL add_oa_col('eta_at',         'eta_at DATETIME NULL');
CALL add_oa_col('dispatch_gp_id', 'dispatch_gp_id BIGINT NULL');
CALL add_oa_col('priority',       'priority VARCHAR(8) NOT NULL DEFAULT ''NORMAL''');
CALL add_oa_col('credit_status',  'credit_status VARCHAR(16) NULL');
DROP PROCEDURE add_oa_col;

INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('OrderAppDump','route_stops','Stops on Route','INT',1,1,0,0,'number',NULL),
  ('OrderAppDump','eta_at','ETA','DATETIME',1,1,0,0,'datetime',NULL),
  ('OrderAppDump','dispatch_gp_id','Dispatch GP #','LONG',1,1,0,0,'number',NULL),
  ('OrderAppDump','priority','Priority','STRING',1,1,0,0,'select','["URGENT","HIGH","NORMAL","LOW"]'),
  ('OrderAppDump','credit_status','Credit Status','STRING',1,1,0,0,'select','["WITHIN_LIMIT","NEAR_LIMIT","HOLD"]');

UPDATE m5_orderapp_dump SET route_stops = FLOOR(2 + RAND()*8), priority='NORMAL', credit_status='WITHIN_LIMIT' WHERE route_stops IS NULL;

-- ---------------------------------------------------------------------------
-- (c) Seal Log — add inline row actions for the lifecycle transitions
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','SealLog','select',JSON_ARRAY('id','gate_pass_id','seal_number','seal_kind','status','issued_at','applied_at','verified_at','broken_at','notes'), 'sort', JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','seal_number','label','Seal #','sortable',true),
    JSON_OBJECT('field','gate_pass_id','label','GP #','sortable',true),
    JSON_OBJECT('field','seal_kind','label','Kind','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('AUTO','blue','MANUAL','amber')),
    JSON_OBJECT('field','status','label','Status','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('ISSUED','grey','APPLIED','blue','VERIFIED','green','BROKEN','red','TAMPERED','red')),
    JSON_OBJECT('field','issued_at','label','Issued','render','datetime'),
    JSON_OBJECT('field','applied_at','label','Applied','render','datetime'),
    JSON_OBJECT('field','verified_at','label','Verified','render','datetime'),
    JSON_OBJECT('field','broken_at','label','Broken','render','datetime')
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('ISSUED','APPLIED','VERIFIED','BROKEN','TAMPERED')),
    JSON_OBJECT('field','seal_kind','label','Kind','type','multi-select','options',JSON_ARRAY('AUTO','MANUAL'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','Apply','action','entity.update-field','args',JSON_OBJECT('field','status','value','APPLIED')),
    JSON_OBJECT('label','Verify','action','entity.update-field','args',JSON_OBJECT('field','status','value','VERIFIED')),
    JSON_OBJECT('label','Break','action','entity.update-field','args',JSON_OBJECT('field','status','value','BROKEN')),
    JSON_OBJECT('label','Tampered','action','entity.update-field','args',JSON_OBJECT('field','status','value','TAMPERED'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
WHERE pd.code='pico:seal-log';

-- ---------------------------------------------------------------------------
-- (d) Diesel Log — add Fill Complete / Skip actions inline
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','DieselFillingLog','select',JSON_ARRAY('id','gate_pass_id','vehicle_no','diesel_qty_litres','filled_at','filled_by','reason','status'),'sort', JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','gate_pass_id','label','GP #','sortable',true),
    JSON_OBJECT('field','vehicle_no','label','Vehicle','sortable',true),
    JSON_OBJECT('field','diesel_qty_litres','label','Diesel (L)','render','number'),
    JSON_OBJECT('field','filled_at','label','Filled At','render','datetime'),
    JSON_OBJECT('field','filled_by','label','Filled By'),
    JSON_OBJECT('field','reason','label','Reason'),
    JSON_OBJECT('field','status','label','Status','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('COMPLETED','green','SKIPPED','grey','PENDING','amber'))
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('COMPLETED','SKIPPED','PENDING'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','Mark Filled','action','entity.update-field','args',JSON_OBJECT('field','status','value','COMPLETED')),
    JSON_OBJECT('label','Skip','action','entity.update-field','args',JSON_OBJECT('field','status','value','SKIPPED'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
WHERE pd.code='pico:diesel-log';

-- ---------------------------------------------------------------------------
-- (e) Packing Scan Log — add Re-scan / Reject inline actions
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','PackingScanLog','select',JSON_ARRAY('id','scanned_at','bay_code','scanned_barcode','sku_code','offer_variant','bundle_count','pieces_total','outcome','reject_reason','operator_id'), 'sort', JSON_ARRAY(JSON_OBJECT('field','id','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','scanned_at','label','When','render','datetime','sortable',true),
    JSON_OBJECT('field','bay_code','label','Bay','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('BAY-A','blue','BAY-B','blue','BAY-C','blue','BAY-D','blue')),
    JSON_OBJECT('field','scanned_barcode','label','Barcode','sortable',true),
    JSON_OBJECT('field','sku_code','label','SKU','sortable',true),
    JSON_OBJECT('field','offer_variant','label','Offer','sortable',true),
    JSON_OBJECT('field','bundle_count','label','Bundles','render','number'),
    JSON_OBJECT('field','pieces_total','label','Pieces','render','number'),
    JSON_OBJECT('field','outcome','label','Outcome','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('ACCEPTED','green','REJECTED','red')),
    JSON_OBJECT('field','reject_reason','label','Reject Reason','sortable',true),
    JSON_OBJECT('field','operator_id','label','Operator')
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','outcome','label','Outcome','type','multi-select','options',JSON_ARRAY('ACCEPTED','REJECTED')),
    JSON_OBJECT('field','bay_code','label','Bay','type','multi-select','options',JSON_ARRAY('BAY-A','BAY-B','BAY-C','BAY-D')),
    JSON_OBJECT('field','reject_reason','label','Reject Reason','type','multi-select','options',JSON_ARRAY('WRONG_SKU','QTY_EXCEEDED','UNAUTHORIZED_BARCODE','DAMAGED_PACK','SEAL_TAMPER','OTHER'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','Re-scan','action','entity.update-field','args',JSON_OBJECT('field','outcome','value','ACCEPTED')),
    JSON_OBJECT('label','Mark Wrong SKU','action','entity.update-field','args',JSON_OBJECT('field','reject_reason','value','WRONG_SKU'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code='pico:packing-scan-log';
