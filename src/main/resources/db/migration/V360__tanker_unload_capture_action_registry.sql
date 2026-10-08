-- Action code for M10.TankerOutbound's reject-flow unload (aria:weighment-capture, UNLOADING_WEIGHMENT),
-- backend: TankerUnloadCaptureActionHandler. ActionDispatcher needs an implemented=1 action_registry row
-- before it will invoke the handler (same reason as V251 for m10.tanker-weighment-capture).
-- Idempotent -- safe to (re)run on any database.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.tanker-unload-capture', 'Save Unload & Return Stock', 'workflow',
       '{"type":"object","properties":{},"note":"formData.tankerUnloadItems (array of {route_id,order_id,sku_code,tank_location,unloading_qty,final?}) is required and read from the page form, not args. order_sku_line_items is accepted as an alias."}',
       1, NULL,
       'Tank-wise reverse of m10.tanker-weighment-capture: splits unloading_qty across the SKU''s open unload rows by what each tank gave, returns the stock into that tank''s bin (m5_stock_movement kind RETURN, ref_type TANKER_UNLOAD), closes the unload row and sets the tank''s loading_progress_detail_bay row REVERSED once the tank has taken back everything (or final=true). Rejects an unload larger than what the tank gave. Does not change workflow state. Used by aria:weighment-capture (M10.TankerOutbound, UNLOADING_WEIGHMENT).'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.tanker-unload-capture');
