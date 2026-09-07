-- QC Approval (M10.ReturnStockIn only) needs to record, per Return Stock
-- Officer batch/SKU line: a QC-assigned Batch No (distinct from the Return
-- Stock Officer's own batch_no — never overwritten) and a stock-wise
-- Unloading Area. Neither existed on return_stock_officer_item (V288's
-- CREATE TABLE only had the Return Stock Officer's own 5 capture columns).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE return_stock_officer_item
  ADD COLUMN IF NOT EXISTS qc_batch_no VARCHAR(64) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS unloading_area VARCHAR(16) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'ReturnStockOfficerItem' b,'qc_batch_no' c,'QC Batch No' d,'STRING' e,0 f,1 g,0 h,0 i,'text' j UNION ALL
  SELECT -1,'ReturnStockOfficerItem','unloading_area','Stock-wise Unloading Area','STRING',1,1,0,0,'select'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ReturnStockOfficerItem' AND field_code = x.c);
