-- V329 cloned M10.StockInward's m0_workflow_projection rows from
-- M10.FGUnloading, but missed two things FGUnloading's REAL config (V278)
-- actually has:
--
-- 1. tolerance_pct=0.400 on the gate_pass row -- DispatchToleranceListener
--    only computes expected_weight_kg/loaded_weight_kg/pmt_variance_pct/
--    pmt_within_tolerance (into manager_qty_approval_detail, the table
--    aria:manager-qty-approval reads) when this column is set on a
--    workflow's gate_pass projection row. Without it, that page's data was
--    never computed for M10.StockInward at all.
-- 2. The qc_preload_detail row for aria:qc-approval -- present in
--    FGUnloading's config, absent from V329's clone.
--
-- Idempotent -- safe to (re)run on any database.

UPDATE m0_workflow_projection
  SET tolerance_pct = 0.400
  WHERE tenant_id = -1 AND workflow_code = 'M10.StockInward' AND target_table = 'gate_pass'
    AND tolerance_pct IS NULL;

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, page_code, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.StockInward', 'qc_preload_detail', 'aria:qc-approval', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  '{"loading_supervisor": ["loadingSupervisor", "loading_supervisor"], "loading_tank_location": ["loadingTankLocation", "loading_tank_location"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "unloading_location": ["unloadingLocation", "unloading_location"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.StockInward' AND target_table = 'qc_preload_detail'
);
