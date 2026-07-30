-- loading_progress_detail (V94) already exists as the one-row-per-gate-pass
-- detail table for aria:loading-in-progress-entry's workflows, currently
-- holding just route_id. It stays one row per (tenant_id,
-- workflow_instance_id) -- adding vehicle/gate-pass identity and a
-- cumulative qty summary here rather than turning it into a per-bay table;
-- existing consumers (Loading Dashboard's join, DispatchToleranceListener)
-- keep seeing exactly one row per instance, unchanged.
--
-- The per-bay/per-SKU breakdown itself (Bay 1 = 100, Bay 2 = 200, Bay 3 =
-- 500) lives in loading_progress_detail_bay (V179) -- the child table that
-- can hold several rows for one instance. total_quantity/processed_quantity
-- here are a rollup kept in sync by M10StartBayActionHandler and
-- M10BayCompleteActionHandler so other pages/reports can read the running
-- total without joining the child table.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE loading_progress_detail
  ADD COLUMN IF NOT EXISTS vehicle_no VARCHAR(32) DEFAULT NULL AFTER route_id,
  ADD COLUMN IF NOT EXISTS gate_pass_no VARCHAR(64) DEFAULT NULL AFTER vehicle_no,
  ADD COLUMN IF NOT EXISTS total_quantity DECIMAL(12,3) NOT NULL DEFAULT 0 AFTER gate_pass_no,
  ADD COLUMN IF NOT EXISTS processed_quantity DECIMAL(12,3) NOT NULL DEFAULT 0 AFTER total_quantity;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'LoadingProgressDetail' b,'vehicle_no' c,'Vehicle' d,'STRING' e,1 f,1 g,1 h,0 i,'text' j UNION ALL
  SELECT -1,'LoadingProgressDetail','gate_pass_no','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'LoadingProgressDetail','total_quantity','Total Qty','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'LoadingProgressDetail','processed_quantity','Processed Qty','DECIMAL',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'LoadingProgressDetail' AND field_code = x.c);
