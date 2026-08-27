-- balance_qty on loading_progress_detail_bay (V179): a plain stored column,
-- NOT computed on read -- M10BayCompleteActionHandler sets it in the same
-- UPDATE that closes a bay row (quantity - processed_quantity), exactly the
-- same "only written at Bay Complete, NULL while IN_PROGRESS" lifecycle
-- processed_quantity already has. Lets a datasource query filter directly
-- on it ({"field":"balance_qty","op":"gt","value":10}) with a plain
-- literal-value comparison -- no generated-column/virtual-column involved.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE loading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS balance_qty DECIMAL(12,3) DEFAULT NULL AFTER processed_quantity;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'LoadingProgressDetailBay', 'balance_qty', 'Balance Qty', 'DECIMAL', 1, 1, 0, 0, 'number'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'LoadingProgressDetailBay' AND field_code = 'balance_qty');
