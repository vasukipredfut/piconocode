-- aria:loading-allocation's supervisor_name is submitted at
-- QC_APPROVED -> SUPERVISOR_ALLOCATED for M10.ReturnStockIn (V131/V133), but
-- (per the gap V135 explicitly flagged and deliberately left alone, since it
-- matched TankerInbound's own pre-existing behavior) loading_allocation_detail
-- was only ever projected for M10.FGLoading/M10.PackLoading — the allocated
-- supervisor's name was never persisted for ReturnStockIn. Per direct
-- request, fixing it for ReturnStockIn specifically (mirrors V99's mapping;
-- TankerInbound's own identical gap is left as-is, out of scope here).
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, terminal_states, terminal_ts_column, field_map_json, tolerance_pct)
SELECT -1, 'M10.ReturnStockIn', 'loading_allocation_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', NULL, NULL, NULL, NULL,
       '{"supervisor_name": ["supervisor_name", "supervisorName"]}',
       NULL
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.ReturnStockIn' AND target_table = 'loading_allocation_detail'
);
