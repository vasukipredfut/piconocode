-- Diff(Box) / Diff(PC) for M10.ReturnStockIn's QC Processed Locations
-- feature. Computed and persisted SERVER-SIDE by
-- QcProcessedLocationTransitionListener (not client-side): the frontend
-- expression engine (ariafrontend/src/configurable/ruleEval.js) only
-- evaluates scalar arithmetic -- no array sum/filter -- so "sum this
-- batch's QC-allocated Box/PC" can't be expressed as a computedFrom on the
-- Return Stock Details ListView. Instead the backend computes
-- diff_box/diff_pc right after writing qc_processed_location rows, and the
-- ListView simply displays them back as two more read-only columns, same
-- "write via listener, read via ListView" pattern already used for
-- qc_batch_no/unloading_area/diff_reason.
--
-- diff_reason (added V314) is the single COMMON reason field for both
-- diffs -- not split per Box/PC.

ALTER TABLE return_stock_officer_item
  ADD COLUMN IF NOT EXISTS diff_box DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS diff_pc DECIMAL(12,3) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'ReturnStockOfficerItem' b,'diff_box' c,'Diff (Box)' d,'DECIMAL' e,0 f,1 g,0 h,0 i,'number' j UNION ALL
  SELECT -1,'ReturnStockOfficerItem','diff_pc','Diff (PC)','DECIMAL',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ReturnStockOfficerItem' AND field_code = x.c);
