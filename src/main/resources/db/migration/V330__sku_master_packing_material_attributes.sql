-- SKU Master: packing-material attributes from the PMT "packing material code
-- wise details" spreadsheet (Master 1 = MASTER DATA, Master 2 = MASTER DATA (2)).
--
-- Master 1 and Master 2 carry the same underlying attribute pair twice with
-- conflicting wording -- Master 1 says "COTTON BOX" (material_name /
-- material_type), Master 2 says "CARTON BOX" (item_category / item_sub_group)
-- for the same sku_code. Per business decision, Master 1's "COTTON BOX"
-- wording is authoritative, so only Master 1 feeds material_name/material_type;
-- Master 2's item_category/item_sub_group are not stored as separate columns.
--
-- material_receive_type (Master 1) and base_uom (Master 2) were checked
-- row-by-row and differ on ~20% of SKUs (e.g. BOX vs Kg/Ltr) -- they are
-- genuinely distinct attributes (how the material is received vs. its backend
-- calc unit), so both are kept. uom (Master 2) is a third, sparser packaging
-- unit column (TIN/PCS/BAG/BOX/POUCH) and is also kept as-is.
-- PROD.LOCATION (Master 2) is 100% blank across all 839 rows -- not added.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS item_type VARCHAR(10) DEFAULT NULL COMMENT 'PM | FG | RM';
ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS material_name VARCHAR(120) DEFAULT NULL COMMENT 'e.g. SF COTTON BOX';
ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS material_type VARCHAR(80) DEFAULT NULL COMMENT 'e.g. COTTON BOX';
ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS material_receive_type VARCHAR(40) DEFAULT NULL COMMENT 'BAG | BOX | BUNDLE | SINGLE ROLL';
ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS base_uom VARCHAR(20) DEFAULT NULL COMMENT 'Backend calc unit: KG | LTR | PCS | BAG | BOX | GM | BUNDLE | SINGLE ROLL';
ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS uom VARCHAR(20) DEFAULT NULL COMMENT 'TIN | PCS | BAG | BOX | POUCH';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'SkuMaster', 'item_type', 'Item Type', 'STRING', 1, 1, 0, 0, 'select'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'SkuMaster' AND field_code = 'item_type');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'SkuMaster', 'material_name', 'Material Name', 'STRING', 1, 1, 1, 0, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'SkuMaster' AND field_code = 'material_name');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'SkuMaster', 'material_type', 'Material Type', 'STRING', 1, 1, 1, 0, 'select'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'SkuMaster' AND field_code = 'material_type');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'SkuMaster', 'material_receive_type', 'Material Receive Type', 'STRING', 1, 1, 0, 0, 'select'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'SkuMaster' AND field_code = 'material_receive_type');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'SkuMaster', 'base_uom', 'Base UOM', 'STRING', 1, 1, 0, 0, 'select'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'SkuMaster' AND field_code = 'base_uom');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'SkuMaster', 'uom', 'UOM', 'STRING', 1, 1, 0, 0, 'select'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'SkuMaster' AND field_code = 'uom');
