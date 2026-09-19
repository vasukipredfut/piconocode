-- Links return_stock_officer_item back to the invoice it was declared
-- against on aria:dispatch-vehicle-details (dispatch_invoice_item), so
-- downstream reporting (qc_processed_location -> Unloading Details) can be
-- grouped by invoice instead of batch. Captured via a new "Invoice No"
-- column on aria:return-stock-officer's return_stock_items grid.
ALTER TABLE return_stock_officer_item
  ADD COLUMN IF NOT EXISTS invoice_no VARCHAR(64) NULL;

INSERT INTO entity_field_registry
    (tenant_id, entity_code, field_code, display_label, data_type, widget_kind, required, sortable, filterable, searchable)
SELECT -1, 'ReturnStockOfficerItem', 'invoice_no', 'Invoice No', 'STRING', 'text', 0, 1, 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM entity_field_registry WHERE entity_code = 'ReturnStockOfficerItem' AND field_code = 'invoice_no'
);
