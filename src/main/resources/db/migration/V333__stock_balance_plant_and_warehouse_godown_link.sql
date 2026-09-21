-- Stock Balance: link `warehouse` to the new Godown master (V332) instead of
-- free text, and add `plant_id` (no default -- pick the Plant explicitly each
-- time, per business decision).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE m5_stock_balance ADD COLUMN IF NOT EXISTS plant_id BIGINT DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT -1, 'StockBalance', 'plant_id', 'Plant', 'RELATION', 1, 1, 0, 0, 'entity-ref', 'Plant', '{"entity":"Plant","valueField":"id","labelField":"plant_name"}'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'StockBalance' AND field_code = 'plant_id');

-- warehouse already exists as a plain-text field (V2) -- repoint it at the
-- Godown master (a plant's sub-locations, e.g. SRG0001 / POUCH-GODOWN)
-- instead of free text, storing the Godown's short code in this VARCHAR(32)
-- column (matches existing short-code values like "MAIN-WH", V298).
UPDATE entity_field_registry
  SET widget_kind = 'entity-ref', relation_entity = 'PlantGodown',
      options_json = '{"entity":"PlantGodown","valueField":"godown_code","labelField":"godown_name"}'
  WHERE entity_code = 'StockBalance' AND field_code = 'warehouse';
