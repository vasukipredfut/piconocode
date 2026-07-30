-- Action codes for aria:loading-in-progress-entry's Start Bay / Bay
-- Complete buttons (M10StartBayActionHandler / M10BayCompleteActionHandler).
-- ActionDispatcher requires an implemented=1 action_registry row before it
-- will invoke a matching ActionHandler bean at all -- without this, POST
-- /api/m0/actions/m10.start-bay/invoke (and m10.bay-complete) would 422 with
-- unknown_action regardless of the Java handler existing (mirrors V169's
-- registration of entity.bulk-update-field the same way).
--
-- Both actions take no args at all -- everything (bay_name, the SKU line
-- items) rides inside formData, the page's existing form state submitted
-- whole per bay session.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.start-bay', 'Start Bay', 'workflow',
       '{"type":"object","properties":{},"note":"formData.bay_name (string) and formData.order_sku_line_items (array of {route_id,sku_code,sku_desc,order_qty}) are required and read from the page form, not args."}',
       1, NULL,
       'Opens one loading_progress_detail_bay row per line in formData.order_sku_line_items under formData.bay_name, rolls their combined quantity into loading_progress_detail.total_quantity, and (on the first bay for this instance) drives LOADING_IN_PROGRESS -> BAY_ASSIGNED. Used by aria:loading-in-progress-entry Start Bay.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.start-bay');

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.bay-complete', 'Bay Complete', 'workflow',
       '{"type":"object","properties":{},"note":"formData.order_sku_line_items (array of {route_id,sku_code,loading_qty}) is required and read from the page form, not args."}',
       1, NULL,
       'Closes every open loading_progress_detail_bay row matching (route_id, sku_code) from formData.order_sku_line_items, recording processed quantity per line and rolling the total into loading_progress_detail.processed_quantity. Does not change workflow state. Used by aria:loading-in-progress-entry Bay Complete.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.bay-complete');
