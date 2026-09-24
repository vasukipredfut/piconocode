-- Registers M10.StockInward's own dedicated "Start Bay" / "Bay Complete"
-- action codes (M10StockInwardStartBayActionHandler /
-- M10StockInwardBayCompleteActionHandler), kept separate from the shared
-- M10.PackUnload pair (m10.start-unloading-bay / m10.unloading-bay-complete)
-- per explicit business request -- this flow's stock movement is shaped
-- differently: kind=STOCK_INWARD, to_bin=Godown (not bay_name),
-- from_bin=vendor code (rd_code, off the order sheet).
--
-- Idempotent -- safe to (re)run on any database.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.stockinward-start-bay', 'Start Bay', 'workflow',
       '{"type":"object","properties":{},"note":"bay_name + godown_code + plant_code + order_sku_line_items ride in formData, not here"}',
       1, NULL,
       'M10.StockInward''s own Start Bay -- opens one unloading_progress_detail_bay row per SKU line, capturing rd_code/vendor_name (order-sheet vendor code) and godown_code/plant_code alongside it.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.stockinward-start-bay');

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.stockinward-bay-complete', 'Bay Complete', 'workflow',
       '{"type":"object","properties":{},"note":"order_sku_line_items rides in formData, not here"}',
       1, NULL,
       'M10.StockInward''s own Bay Complete -- closes unloading_progress_detail_bay row(s) and posts a STOCK_INWARD stock movement (ref_type=BAY) per line: to_bin=Godown, from_bin=vendor code (rd_code).'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.stockinward-bay-complete');
