-- M10.ReturnStockIn: durable record of what was submitted on
-- aria:unloading-progress's qcAllocatedLocations list once the instance
-- reaches UNLOADING_COMPLETED -- one row per BOX/PC line (see the page's
-- own dataSource.splitRows: a location with both a box_qty and a pc_qty
-- submits as two separate lines, discriminated by line_type).
--
-- Replaces the earlier approach of posting this data into
-- m5_stock_movement/m5_stock_balance (removed) -- this table is a
-- purpose-built log for this one flow instead of feeding the generic MES
-- stock ledger, keyed by workflow_instance_id like every other
-- M10.ReturnStockIn child table (return_stock_officer_item,
-- qc_processed_location).

CREATE TABLE IF NOT EXISTS return_stock_leakage_section (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  gate_pass_no VARCHAR(64) DEFAULT NULL,
  batch_no VARCHAR(64) DEFAULT NULL,
  sku_code VARCHAR(32) DEFAULT NULL,
  sku_name VARCHAR(255) DEFAULT NULL,
  processed_area VARCHAR(64) DEFAULT NULL,
  line_type VARCHAR(8) DEFAULT NULL,
  scan_method VARCHAR(16) DEFAULT NULL,
  unloading_qty DECIMAL(12,3) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_rsls_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'ReturnStockLeakageSection', 'Return Stock Leakage Section', 'return_stock_leakage_section',
       'One row per BOX/PC unloading line confirmed on aria:unloading-progress for a M10.ReturnStockIn instance, written once the instance reaches UNLOADING_COMPLETED.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'ReturnStockLeakageSection');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'ReturnStockLeakageSection' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'ReturnStockLeakageSection','vehicle_no','Vehicle No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockLeakageSection','gate_pass_no','Gate Pass No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockLeakageSection','batch_no','Batch No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockLeakageSection','sku_code','SKU Code','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockLeakageSection','sku_name','SKU Name','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockLeakageSection','processed_area','Processed Area','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'ReturnStockLeakageSection','line_type','Line Type','STRING',1,1,0,0,'select' UNION ALL
  SELECT -1,'ReturnStockLeakageSection','scan_method','Scan Method','STRING',1,1,0,0,'select' UNION ALL
  SELECT -1,'ReturnStockLeakageSection','unloading_qty','Unloading Qty','DECIMAL',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ReturnStockLeakageSection' AND field_code = x.c);
