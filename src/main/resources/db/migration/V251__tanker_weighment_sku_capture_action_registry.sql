-- Action code for aria:weighment-capture's "Save Weight & Update Stock"
-- button (M10.TankerOutbound, WEIGHMENT_2 only), backend:
-- TankerWeighmentSkuCaptureActionHandler. Same reason as V178's
-- m10.start-bay/m10.bay-complete rows: ActionDispatcher requires an
-- implemented=1 action_registry row before it will invoke a matching
-- ActionHandler bean at all -- without this, POST
-- /api/m0/actions/m10.tanker-weighment-capture/invoke would 422 with
-- unknown_action regardless of the Java handler existing.
--
-- Deliberately separate from the page's existing "Capture & Save Weighment"
-- button (workflow.transition to WEIGHMENT_2) -- same division of labour as
-- Start Bay / Bay Complete on page 428: this action saves the per-SKU
-- weighbridge reading + stock ledger without touching workflow state.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.tanker-weighment-capture', 'Save Weight & Update Stock', 'workflow',
       '{"type":"object","properties":{},"note":"formData.tankerSkuWeights (array of {route_id,sku_code,loading_qty}) is required and read from the page form, not args -- same (route_id, sku_code) match key as m10.bay-complete."}',
       1, NULL,
       'Per line (matched by route_id+sku_code): FEFO-deducts m5_stock_balance, inserts one m5_stock_movement row per lot picked (kind ISSUE, ref_type TANKER), closes the matching loading_progress_detail_bay row (IN_PROGRESS->DONE), and rolls the total into loading_progress_detail.processed_quantity. Does not touch m5_order_dispatch_detail and does not change workflow state. Used by aria:weighment-capture (M10.TankerOutbound, WEIGHMENT_2).'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.tanker-weighment-capture');
