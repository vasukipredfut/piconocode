-- V328 cloned M10.StockOutward's m0_workflow_projection rows from
-- M10.FGLoading, but missed three things FGLoading's actual (multi-migration)
-- config has:
--
-- 1. tolerance_pct=0.400 on the gate_pass row -- same DispatchToleranceListener
--    gap as V340 fixed for M10.StockInward: without it, expected/loaded
--    weight, variance%, and the within-tolerance verdict are never computed
--    into manager_qty_approval_detail.
-- 2. state_columns='' on the gate_pass_closure row -- left NULL, the column
--    defaults to 'status' (the schema default), but gate_pass_closure has no
--    status column at all, so WorkflowProjectionListener's INSERT silently
--    fails every time (caught, only logged as a warning) -- gate pass
--    closure data was never actually saved for M10.StockOutward.
-- 3. state_ts_columns_json on the loading_progress_detail row -- without it,
--    loading_start_time/loading_finish_time never get stamped.
--
-- Idempotent -- safe to (re)run on any database.

UPDATE m0_workflow_projection
  SET tolerance_pct = 0.400
  WHERE tenant_id = -1 AND workflow_code = 'M10.StockOutward' AND target_table = 'gate_pass'
    AND tolerance_pct IS NULL;

UPDATE m0_workflow_projection
  SET state_columns = ''
  WHERE tenant_id = -1 AND workflow_code = 'M10.StockOutward' AND target_table = 'gate_pass_closure'
    AND (state_columns IS NULL OR state_columns = 'status');

UPDATE m0_workflow_projection
  SET state_ts_columns_json = '{"LOADING_IN_PROGRESS":"loading_start_time","LOADING_COMPLETED":"loading_finish_time"}'
  WHERE tenant_id = -1 AND workflow_code = 'M10.StockOutward' AND target_table = 'loading_progress_detail'
    AND (state_ts_columns_json IS NULL OR state_ts_columns_json = '');
