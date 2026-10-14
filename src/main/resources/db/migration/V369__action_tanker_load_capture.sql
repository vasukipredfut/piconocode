-- Action code for M10.TankerInbound's reject / "load again" loading (loadingInvoices rows from aria:qc-approval),
-- backend: TankerLoadCaptureActionHandler. ActionDispatcher needs an implemented=1 action_registry row
-- before it will invoke the handler (same reason as V251 / V360).
-- Idempotent -- safe to (re)run on any database.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.tanker-load-capture', 'Save Load & Update Stock', 'workflow',
       '{"type":"object","properties":{},"note":"formData.tankerLoadItems (array of {invoice_no,sku_code,order_id?,tank_location?|tank_locations?,load_wgt}) is required and read from the page form, not args. order_sku_line_items / tankerSkuWeights are accepted as aliases."}',
       1, NULL,
       'Tank-wise load of the invoice rows (loading_progress_detail_bay, IN_PROGRESS, invoice_no set) that M10.TankerInbound''s QC approval seeds from loadingInvoices: splits load_wgt across the SKU''s open tank rows by allocation, takes the stock out of that tank''s bin (m5_stock_movement kind CONSUME, ref_type TANKER_LOAD) and closes the row (DONE). Rejects a load larger than the tank holds. Does not change workflow state.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.tanker-load-capture');
