-- M5.PackingRun had no m0_workflow_projection row at all, so a plain
-- workflow.transition (page.submitted from aria:pack-start / aria:pack-finish
-- / aria:pack-spoil / aria:pack-close) moved workflow_instance.current_state
-- but never touched m5_packing_run.status/current_step, and never stamped
-- start/finish timestamps -- unlike every M10 gate_pass-driven workflow,
-- which already has this wired (see V36/V190).
--
-- key_column='workflow_instance_id' (already a column on m5_packing_run,
-- set when the row's workflow starts via GenericEntityController.startWorkflowFor
-- -- same pattern as gate_pass). No variant_column/number_column: PackingRun
-- has neither a flow_variant_code column nor an auto-generated pr_number
-- pattern (pr_number is set at row creation, not derived here).
--
-- state_ts_columns_json reuses the existing shift_start/shift_end DATETIME
-- columns as the Packing Process spec's "Packing Start Time"/"Packing
-- Finish Time" auto-entry fields -- stamped automatically the moment the
-- workflow enters RUNNING/PACKED, same convention as loading_start_time/
-- loading_finish_time on loading_progress_detail (V190).
--
-- field_map_json covers the manual-entry fields the aria:pack-start /
-- aria:pack-finish forms submit (operator, machine, shift, QC, notes) --
-- page_code stays NULL so it applies to every M5.PackingRun transition,
-- matching gate_pass's own convention (any submitted field not present on a
-- given page's payload is simply skipped by pluck()).
--
-- Idempotent -- safe to (re)run on any database.

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column,
   number_column, number_pattern, terminal_states, terminal_ts_column, state_ts_columns_json,
   field_map_json, tolerance_pct, page_code)
SELECT -1, 'M5.PackingRun', 'm5_packing_run', 'workflow_instance_id', 'status,current_step', NULL,
       NULL, NULL, 'CLOSED', NULL,
       '{"RUNNING":"shift_start","PACKED":"shift_end"}',
       '{"operator_emp_id":["operator_emp_id","operatorEmpId"],"shift_code":["shift_code","shiftCode"],"packing_line":["packing_line","packingLine"],"machine_code":["machine_code","machineCode"],"pack_size":["pack_size","packSize"],"pack_uom":["pack_uom","packUom"],"qc_status":["qc_status","qcStatus"],"avg_pack_weight_g":["avg_pack_weight_g","avgPackWeightG"],"downtime_min":["downtime_min","downtimeMin"],"downtime_reason":["downtime_reason","downtimeReason"],"spoil_reason":["spoil_reason","spoilReason"],"notes":["notes"]}',
       NULL, NULL
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M5.PackingRun' AND target_table = 'm5_packing_run'
);
