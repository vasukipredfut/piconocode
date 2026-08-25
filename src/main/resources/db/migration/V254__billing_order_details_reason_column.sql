-- billing_order_details: add "reason" (e.g. why a line's qty was skipped/
-- partially loaded), captured per-line on the same "Loaded SKU Details"
-- grid as order_qty/loading_qty/skipped_quantity (V192), written by
-- BillingOrderDetailTransitionListener (V253's page-resolution fix).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE billing_order_details
  ADD COLUMN IF NOT EXISTS reason VARCHAR(255) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'BillingOrderDetail', 'reason', 'Reason', 'STRING', 1, 0, 1, 0, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'BillingOrderDetail' AND field_code = 'reason');
