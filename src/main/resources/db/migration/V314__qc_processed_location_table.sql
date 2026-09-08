-- QC Approval (M10.ReturnStockIn only): "Processed Location" allocation
-- entries -- QC Officer splits one Return Stock Officer batch/SKU row's
-- Box/PC across one or more processed areas (e.g. Pouch/Repack,
-- ETP/Cutting). One row per allocation, child of ONE
-- return_stock_officer_item row (Option A -- nested per batch, not a flat
-- per-vehicle list), mirroring return_stock_officer_item's own shape
-- (V302) and reasoning.
--
-- diff_box/diff_pc are deliberately NOT stored here or on
-- return_stock_officer_item -- same convention as variance_kg on
-- aria:weighment-capture (V296): computed client-side from
-- return_stock_officer_item.box/pc vs. SUM of this batch's
-- qc_processed_location rows, not persisted. Only diff_reason (actual user
-- input, added to return_stock_officer_item below) is real data.
--
-- M10.ReturnStockIn only -- no other workflow/flow variant references this
-- table.

CREATE TABLE IF NOT EXISTS qc_processed_location (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  return_stock_officer_item_id BIGINT NOT NULL,
  processed_area VARCHAR(64) DEFAULT NULL,
  processed_type VARCHAR(16) DEFAULT NULL,
  box_qty DECIMAL(12,3) DEFAULT NULL,
  pc_qty DECIMAL(12,3) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_qpl_instance (tenant_id, workflow_instance_id),
  KEY idx_qpl_item (return_stock_officer_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'QcProcessedLocation', 'QC Processed Location', 'qc_processed_location',
       'One row per QC-allocated processed area/type + Box/PC split of a single Return Stock Officer batch/SKU line (M10.ReturnStockIn).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'QcProcessedLocation');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'QcProcessedLocation' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'QcProcessedLocation','return_stock_officer_item_id','Return Stock Officer Item','LONG',1,1,0,1,'number' UNION ALL
  SELECT -1,'QcProcessedLocation','processed_area','Processed Area','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcProcessedLocation','processed_type','Processed Type','STRING',1,1,0,0,'select' UNION ALL
  SELECT -1,'QcProcessedLocation','box_qty','Box Qty','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'QcProcessedLocation','pc_qty','PC Qty','DECIMAL',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'QcProcessedLocation' AND field_code = x.c);

-- diff_reason lives on the PARENT batch row (one reason per batch, not per
-- allocation) -- required only when the computed Box/PC diff for that batch
-- is non-zero (frontend-enforced, per the poOverrideReason requiredWhen
-- pattern already used elsewhere on this page).
ALTER TABLE return_stock_officer_item
  ADD COLUMN IF NOT EXISTS diff_reason VARCHAR(255) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1,'ReturnStockOfficerItem','diff_reason','Diff Reason','STRING',0,0,1,0,'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ReturnStockOfficerItem' AND field_code = 'diff_reason');
