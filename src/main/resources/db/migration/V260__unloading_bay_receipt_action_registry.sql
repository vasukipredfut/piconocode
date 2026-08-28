-- Registers the two new M10.PackUnload "Start Bay" / "Bay Complete" action
-- codes (M10StartUnloadingBayActionHandler / M10UnloadingBayCompleteActionHandler).
-- Sibling of the loading side's m10.start-bay/m10.bay-complete (CONSUME/BAY)
-- and the Reject Flow Unload side's m10.start-unload-bay/m10.unload-bay-complete
-- (RETURN/UNLOAD_BAY) -- this pair is the receiving direction: an incoming
-- vehicle's cargo is received INTO stock, kind=RECEIPT, ref_type=BAY (matching
-- the loading side's own ref_type, not a reversal of anything).
--
-- Idempotent -- safe to (re)run on any database.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.start-unloading-bay', 'Start Bay', 'workflow',
       '{"type":"object","properties":{},"note":"bay_name + order_sku_line_items ride in formData, not here"}',
       1, NULL,
       'Opens one unloading_progress_detail_bay row per SKU line for M10.PackUnload''s unloading-mark-in-progress step. No prior loaded row required (unlike m10.start-unload-bay) -- this is fresh receiving, not a reversal.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.start-unloading-bay');

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.unloading-bay-complete', 'Bay Complete', 'workflow',
       '{"type":"object","properties":{},"note":"order_sku_line_items rides in formData, not here"}',
       1, NULL,
       'Closes unloading_progress_detail_bay row(s) opened by m10.start-unloading-bay and posts a RECEIPT stock movement (ref_type=BAY) per line -- qty_on_hand increment into the bay''s bin, inserting the balance row if it does not yet exist.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.unloading-bay-complete');
