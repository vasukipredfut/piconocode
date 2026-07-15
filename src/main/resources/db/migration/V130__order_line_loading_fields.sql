-- V130 — order-line fields for the M10 loading order-sheet.
--
-- The loading step (Route ID -> Fetch Order -> per-row Offer -> Start) is an
-- ordinary page: a ListView on SalesOrderLine filtered by route_id, offer
-- inline-editable, + a Start-Loading WorkflowActionButton. No new primitive.
-- This adds the fields that page needs and registers them so the generic
-- ListView/modal/filters/auto-derive pick them up. Idempotent.

ALTER TABLE m5_sales_order_line ADD COLUMN IF NOT EXISTS route_id    VARCHAR(64) NULL;
ALTER TABLE m5_sales_order_line ADD COLUMN IF NOT EXISTS sku_barcode VARCHAR(64) NULL;
ALTER TABLE m5_sales_order_line ADD COLUMN IF NOT EXISTS offer       VARCHAR(64) NULL;

-- Register the loading-relevant fields (SalesOrderLine). item_code/item_name
-- are columns but weren't registered; add them so the order table shows SKU
-- Name/Code and the filter/edit widgets know the types.
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'SalesOrderLine','route_id','Route ID','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrderLine' AND field_code='route_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'SalesOrderLine','sku_barcode','SKU Barcode','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrderLine' AND field_code='sku_barcode');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'SalesOrderLine','offer','Offer','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrderLine' AND field_code='offer');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'SalesOrderLine','item_code','SKU Code','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrderLine' AND field_code='item_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'SalesOrderLine','item_name','SKU Name','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrderLine' AND field_code='item_name');
