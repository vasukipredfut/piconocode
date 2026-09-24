-- Registers M10.StockOutward's own dedicated "Start Bay" / "Bay Complete"
-- action codes (M10StockOutwardStartBayActionHandler /
-- M10StockOutwardBayCompleteActionHandler), kept separate from the shared
-- M10.FGLoading pair (m10.start-bay / m10.bay-complete) per the same
-- explicit business request as M10.StockInward's own pair (V342). This
-- flow's stock movement is shaped differently: kind=STOCK_OUTWARD,
-- ref_type=UNLOAD_BAY, from_bin=Godown (the operator-selected source
-- location), to_bin=vendor/destination code (rd_code, off the order sheet).
--
-- loading_progress_detail_bay never had rd_code/vendor_name (unlike
-- unloading_progress_detail_bay, which got them in V258) -- adding them here
-- so Start Bay can capture the destination code for Bay Complete to use.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE loading_progress_detail_bay ADD COLUMN IF NOT EXISTS rd_code VARCHAR(32) DEFAULT NULL;
ALTER TABLE loading_progress_detail_bay ADD COLUMN IF NOT EXISTS vendor_name VARCHAR(255) DEFAULT NULL;

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.stockoutward-start-bay', 'Start Bay', 'workflow',
       '{"type":"object","properties":{},"note":"bay_name + godown_code + plant_code + order_sku_line_items ride in formData, not here"}',
       1, NULL,
       'M10.StockOutward''s own Start Bay -- opens one loading_progress_detail_bay row per SKU line, capturing rd_code/vendor_name (order-sheet destination code) and godown_code/plant_code alongside it.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.stockoutward-start-bay');

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.stockoutward-bay-complete', 'Bay Complete', 'workflow',
       '{"type":"object","properties":{},"note":"order_sku_line_items rides in formData, not here"}',
       1, NULL,
       'M10.StockOutward''s own Bay Complete -- FEFO-picks and deducts stock (scoped to the row''s Godown) and posts a STOCK_OUTWARD stock movement (ref_type=UNLOAD_BAY) per line: from_bin=Godown, to_bin=destination code (rd_code).'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.stockoutward-bay-complete');
