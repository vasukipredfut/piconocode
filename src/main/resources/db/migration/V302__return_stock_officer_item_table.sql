-- Recreates what the original return_stock_officer_item setup (table +
-- registry + roles) needed but never survived to this branch's tracked
-- history -- V300/V301 (and ReturnStockQcTransitionListener.java) all
-- assume this table already exists; it does not yet on this database.
--
-- M10.ReturnStockIn only. No other workflow/flow variant references this
-- table or these two roles.

-- 1. Table: one row per batch/SKU line captured on aria:return-stock-officer,
--    updated later by QC (qc_batch_no/unloading_area, ReturnStockQcTransitionListener).
CREATE TABLE IF NOT EXISTS return_stock_officer_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  gate_pass_no VARCHAR(64) DEFAULT NULL,
  batch_no VARCHAR(64) DEFAULT NULL,
  sku_code VARCHAR(32) DEFAULT NULL,
  sku_name VARCHAR(255) DEFAULT NULL,
  qty DECIMAL(12,3) DEFAULT NULL,
  good_stock_qty DECIMAL(12,3) DEFAULT NULL,
  expiry_stock_qty DECIMAL(12,3) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_rsoi_instance (tenant_id, workflow_instance_id),
  KEY idx_rsoi_vehicle (tenant_id, vehicle_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Entity registry + field registry (Designer / generic-CRUD visibility).
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'ReturnStockOfficerItem', 'Return Stock Officer Item', 'return_stock_officer_item',
       'One row per batch/SKU line captured on aria:return-stock-officer (M10.ReturnStockIn, after first weighment).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'ReturnStockOfficerItem');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'ReturnStockOfficerItem' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'ReturnStockOfficerItem','vehicle_no','Vehicle','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','gate_pass_no','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','batch_no','Batch No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','sku_code','SKU Code','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','sku_name','SKU Name','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','qty','Qty','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','good_stock_qty','Good Stock Qty','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','expiry_stock_qty','Expiry Stock Qty','DECIMAL',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ReturnStockOfficerItem' AND field_code = x.c);

-- 3. Roles (ReturnStockOfficer, StockCheckingOfficer) -- also missing.
INSERT INTO role (name, landing_route)
SELECT 'ReturnStockOfficer', NULL
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'ReturnStockOfficer');

INSERT INTO role (name, landing_route)
SELECT 'StockCheckingOfficer', NULL
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'StockCheckingOfficer');
