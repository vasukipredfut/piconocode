-- One vehicle (gate pass) can carry multiple invoices, each from its own
-- vendor, each invoice carrying multiple products with their own qty. That's
-- 2-level nesting (invoice -> vendor is 1:1, invoice -> products is 1:many)
-- that a single flat field can't represent -- same shape as
-- billing_order_details (V192/BillingOrderDetailTransitionListener): one
-- denormalised row per (invoice, product) line, invoice_no/vendor_name
-- repeated across every product-row belonging to the same invoice, rather
-- than a separate invoice-header table.

CREATE TABLE IF NOT EXISTS dispatch_invoice_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  gate_pass_no VARCHAR(64) DEFAULT NULL,
  invoice_no VARCHAR(64) DEFAULT NULL,
  order_id VARCHAR(32) DEFAULT NULL,
  vendor_name VARCHAR(255) DEFAULT NULL,
  rd_code VARCHAR(32) DEFAULT NULL,
  sku_code VARCHAR(32) DEFAULT NULL,
  sku_name VARCHAR(255) DEFAULT NULL,
  qty DECIMAL(12,3) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dii_instance (tenant_id, workflow_instance_id),
  KEY idx_dii_invoice (tenant_id, invoice_no),
  KEY idx_dii_vehicle (tenant_id, vehicle_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'DispatchInvoiceItem', 'Dispatch Invoice Item', 'dispatch_invoice_item',
       'One row per invoice/product line captured on aria:dispatch-vehicle-details -- a vehicle can carry multiple invoices, each from its own vendor, each with multiple SKU+qty lines.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'DispatchInvoiceItem');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'DispatchInvoiceItem' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'DispatchInvoiceItem','vehicle_no','Vehicle','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchInvoiceItem','gate_pass_no','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchInvoiceItem','invoice_no','Invoice No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchInvoiceItem','order_id','Order Id','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchInvoiceItem','vendor_name','Vendor','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchInvoiceItem','rd_code','RD Code','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchInvoiceItem','sku_code','SKU Code','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchInvoiceItem','sku_name','SKU Name','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchInvoiceItem','qty','Qty','DECIMAL',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'DispatchInvoiceItem' AND field_code = x.c);
