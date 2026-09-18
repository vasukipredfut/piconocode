-- Return Stock In's "Unloading Progress" step (aria:unloading-progress) now
-- captures a per-location Scan Method (Gun Scanner / Manual) and a confirmed
-- Unloading Qty for each qc_processed_location row, neither of which had a
-- column to land in.
--
-- Separately, m5_stock_balance only ever tracked ONE generic on-hand
-- quantity (qty_on_hand, unit given by the row's own `uom`) — there was no
-- piece-count on-hand column at all, so a PC quantity had nowhere to post
-- alongside the box quantity. Adds pc_qty_on_hand so both survive.

ALTER TABLE qc_processed_location
  ADD COLUMN scan_method VARCHAR(16) NULL,
  ADD COLUMN unloading_qty DECIMAL(12,3) NULL;

ALTER TABLE m5_stock_balance
  ADD COLUMN pc_qty_on_hand DECIMAL(14,3) NOT NULL DEFAULT 0.000;

-- entity_field_registry rows so these fields are Designer/entity-CRUD aware
-- like every other field on these already-registered entities.
INSERT INTO entity_field_registry
    (tenant_id, entity_code, field_code, display_label, data_type, widget_kind, options_json, required, sortable, filterable, searchable)
SELECT -1, 'QcProcessedLocation', 'scan_method', 'Scan Method', 'STRING', 'select',
    '[{"label":"Gun Scanner","value":"GUN_SCANNER"},{"label":"Manual","value":"MANUAL"}]', 0, 1, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM entity_field_registry WHERE entity_code = 'QcProcessedLocation' AND field_code = 'scan_method'
);

INSERT INTO entity_field_registry
    (tenant_id, entity_code, field_code, display_label, data_type, widget_kind, required, sortable, filterable, searchable)
SELECT -1, 'QcProcessedLocation', 'unloading_qty', 'Unloading Qty', 'DECIMAL', 'number', 0, 1, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM entity_field_registry WHERE entity_code = 'QcProcessedLocation' AND field_code = 'unloading_qty'
);

INSERT INTO entity_field_registry
    (tenant_id, entity_code, field_code, display_label, data_type, widget_kind, required, sortable, filterable, searchable)
SELECT -1, 'StockBalance', 'pc_qty_on_hand', 'PC Qty On Hand', 'DECIMAL', 'number', 0, 1, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM entity_field_registry WHERE entity_code = 'StockBalance' AND field_code = 'pc_qty_on_hand'
);
