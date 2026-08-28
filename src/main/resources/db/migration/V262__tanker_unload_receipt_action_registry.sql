-- m10.tanker-unload-receipt (M10TankerUnloadReceiptActionHandler) was never
-- actually INSERTed into action_registry by any migration -- V258 only
-- UPDATEs its description, assuming the row already existed (it was added by
-- hand directly on whichever DB this was first built against). Any other
-- environment's action_registry has no row for this code at all, so
-- ActionDispatcher.dispatch() throws ActionUnknownException -> 422
-- unknown_action before the handler is ever reached, however correct the
-- handler itself is.
--
-- Idempotent -- safe to (re)run on any database.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.tanker-unload-receipt', 'Save Weight & Update Stock', 'workflow',
       '{"type":"object","properties":{},"note":"unload_receipt_items rides in formData, not here"}',
       1, NULL,
       'Closes matching unloading_progress_detail_bay row(s) (invoice_no/sku_code) with the entered weight, posts stock (m5_stock_movement kind RECEIPT into the tank location recorded in bay_name), and rolls up unloading_progress_detail. Used on the weighment-capture step following aria:qc-approval for M10.TankerInbound.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.tanker-unload-receipt');
