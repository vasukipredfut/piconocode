-- SKU Master: scanning_status + conveyor_mode. Distinct from the existing
-- boolean `conveyor_status` (V239, still in use elsewhere) -- these two new
-- columns carry the richer, text-valued classification from the business's
-- SKU master spreadsheet ("BARCODE"/"WITHOUT BARCODE" scanning status;
-- "CONVEYOR" / "CONVEYOR WITH MANUAL SELECT OPTION" / "WITHOUT CONVEYOR
-- (MANUAL ENTRY OPTION)" conveyor handling mode). Left NULL until V277 (or a
-- later bulk-update) populates them per sku_code.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS scanning_status VARCHAR(32) DEFAULT NULL COMMENT 'BARCODE | WITHOUT_BARCODE';
ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS conveyor_mode VARCHAR(32) DEFAULT NULL COMMENT 'CONVEYOR | CONVEYOR_MANUAL_SELECT | WITHOUT_CONVEYOR';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'SkuMaster', 'scanning_status', 'Scanning Status', 'STRING', 1, 1, 0, 0, 'select'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'SkuMaster' AND field_code = 'scanning_status');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'SkuMaster', 'conveyor_mode', 'Conveyor Mode', 'STRING', 1, 1, 0, 0, 'select'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'SkuMaster' AND field_code = 'conveyor_mode');
