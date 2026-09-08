-- M10.ReturnStockIn's Return Stock Officer grid (aria:return-stock-officer,
-- RepeatingGroup "return_stock_items") gets a revised column set per
-- confirmed business requirement: Batch No / Packing Month / SKU Name /
-- Stock Type / Box / PC / Remarks. Qty/Good Stock Qty/Expiry Stock Qty drop
-- off the VISIBLE table but their columns are kept (not deleted) since
-- QC Approval's own "Return Stock Details" display (V303/
-- ReturnStockQcTransitionListener) may still reference them -- removing the
-- columns outright risks breaking that read path.
--
-- M10.ReturnStockIn only -- no other workflow references this table.

ALTER TABLE return_stock_officer_item
  ADD COLUMN IF NOT EXISTS packing_month VARCHAR(32) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS stock_type VARCHAR(16) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS box DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pc DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS remarks VARCHAR(500) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'ReturnStockOfficerItem' b,'packing_month' c,'Packing Month' d,'STRING' e,1 f,1 g,0 h,0 i,'text' j UNION ALL
  SELECT -1,'ReturnStockOfficerItem','stock_type','Stock Type','STRING',1,1,0,0,'select' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','box','Box','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','pc','PC','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'ReturnStockOfficerItem','remarks','Remarks','STRING',0,0,1,0,'textarea'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ReturnStockOfficerItem' AND field_code = x.c);
