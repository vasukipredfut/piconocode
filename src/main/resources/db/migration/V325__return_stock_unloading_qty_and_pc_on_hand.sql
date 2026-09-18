-- m5_stock_balance only ever tracked ONE generic on-hand quantity
-- (qty_on_hand, unit given by the row's own `uom`) — there was no
-- piece-count on-hand column at all, so M10.ReturnStockIn's "Unloading
-- Progress" step (aria:unloading-progress) has nowhere to post a confirmed
-- piece-count quantity alongside the confirmed box quantity. Adds
-- pc_qty_on_hand so both survive.
--
-- (Scan Method / Unloading Qty entered per QC-allocated location on that
-- page are NOT persisted back onto qc_processed_location — the ListView is
-- localEditOnly and those values only ever flow through the one-shot
-- workflow submission bag that ReturnStockUnloadingStockPostingListener
-- reads at UNLOADING_COMPLETED, so no column is needed for them there.)

ALTER TABLE m5_stock_balance
  ADD COLUMN IF NOT EXISTS pc_qty_on_hand DECIMAL(14,3) NOT NULL DEFAULT 0.000;

-- entity_field_registry row so this field is Designer/entity-CRUD aware
-- like every other field on StockBalance.
INSERT INTO entity_field_registry
    (tenant_id, entity_code, field_code, display_label, data_type, widget_kind, required, sortable, filterable, searchable)
SELECT -1, 'StockBalance', 'pc_qty_on_hand', 'PC Qty On Hand', 'DECIMAL', 'number', 0, 1, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM entity_field_registry WHERE entity_code = 'StockBalance' AND field_code = 'pc_qty_on_hand'
);
