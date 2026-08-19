-- SKU Master: conveyor_status flag, defaulting TRUE for every existing and
-- future row. Idempotent -- safe to (re)run on any database.

ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS conveyor_status TINYINT(1) NOT NULL DEFAULT 1;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'SkuMaster', 'conveyor_status', 'Conveyor Status', 'BOOLEAN', 1, 1, 0, 0, 'checkbox'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'SkuMaster' AND field_code = 'conveyor_status');
