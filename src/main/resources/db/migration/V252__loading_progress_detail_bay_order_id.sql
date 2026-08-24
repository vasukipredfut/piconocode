-- loading_progress_detail_bay was keyed by (route_id, sku_code) alone --
-- one physical column for whatever identifier the loading step happened to
-- use. In practice different loading steps group SKU lines by different
-- identifiers: sometimes route_id, sometimes order_id (both live on
-- m5_order_dispatch_detail, VARCHAR(32)/VARCHAR(64) respectively). A single
-- overloaded column can't tell which identifier a given row was opened
-- with, so a closing step (Bay Complete / m10.tanker-weighment-capture)
-- that only knows the OTHER identifier would silently fail to match it.
--
-- Fix: give order_id its own column, populated alongside route_id at
-- open time (m10.start-bay, QcPreloadTankAllocationListener) and matched
-- alongside route_id at close time (m10.bay-complete,
-- m10.tanker-weighment-capture) -- NULL-safe on both sides (same
-- "((x IS NULL AND ? IS NULL) OR x = ?)" pattern already used for
-- m5_stock_balance.lot_no elsewhere in this codebase), so existing rows
-- opened before this change (order_id NULL) still match correctly when the
-- closing call also omits order_id.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE loading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS order_id VARCHAR(32) DEFAULT NULL AFTER route_id;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'LoadingProgressDetailBay', 'order_id', 'Order Id', 'STRING', 1, 1, 1, 0, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'LoadingProgressDetailBay' AND field_code = 'order_id');
