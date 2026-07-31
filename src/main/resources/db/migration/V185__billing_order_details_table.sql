-- billing_order_details: child table for aria:skipped-item-allocation's
-- "Loaded SKU Details" grid (M10.PackUnload). One row per order/SKU line
-- for a given loading transaction, capturing what was actually loaded vs
-- skipped -- mirrors loading_progress_detail_bay's per-line-item shape
-- (V179) rather than a header+lines split.
--
-- Workflow-transition wiring for the page's Submit button (which state(s)
-- this should fire from/to) is intentionally NOT part of this migration --
-- pending confirmation of the intended FSM placement. Table + registry
-- only for now.
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS billing_order_details (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  gate_pass_no VARCHAR(64) DEFAULT NULL,
  order_id VARCHAR(32) DEFAULT NULL,
  rd_code VARCHAR(32) DEFAULT NULL,
  rd_desc VARCHAR(255) DEFAULT NULL,
  sku_code VARCHAR(32) DEFAULT NULL,
  sku_desc VARCHAR(255) DEFAULT NULL,
  order_qty DECIMAL(12,3) NOT NULL DEFAULT 0,
  loading_qty DECIMAL(12,3) NOT NULL DEFAULT 0,
  skipped_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_bod_instance (tenant_id, workflow_instance_id),
  KEY idx_bod_order (tenant_id, order_id),
  KEY idx_bod_vehicle (tenant_id, vehicle_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'BillingOrderDetail', 'Billing Order Detail', 'billing_order_details',
       'One row per order/SKU line captured on aria:skipped-item-allocation (M10.PackUnload) -- what was loaded vs skipped for that line.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'BillingOrderDetail');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'BillingOrderDetail' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'BillingOrderDetail','vehicle_no','Vehicle' ,'STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'BillingOrderDetail','gate_pass_no','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'BillingOrderDetail','order_id','Order Id','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'BillingOrderDetail','rd_code','RD Code','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'BillingOrderDetail','rd_desc','RD Name','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'BillingOrderDetail','sku_code','SKU Code','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'BillingOrderDetail','sku_desc','SKU Name','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'BillingOrderDetail','order_qty','Order Quantity (Boxes)','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'BillingOrderDetail','loading_qty','Loaded Quantity (Boxes)','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'BillingOrderDetail','skipped_quantity','Skipped Quantity (Boxes)','DECIMAL',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'BillingOrderDetail' AND field_code = x.c);
