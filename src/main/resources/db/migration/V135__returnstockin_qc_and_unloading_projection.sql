-- Audit of every page in M10.ReturnStockIn's flow against its target table
-- turned up 2 more missing m0_workflow_projection rows (same class of gap
-- V125 already fixed for TankerInbound's dispatch_vehicle_detail/
-- qc_preload_detail): aria:qc-approval and aria:supervisor-unloading data was
-- being silently dropped for ReturnStockIn — no config row meant
-- WorkflowProjectionListener had nowhere to write it.
--
-- qc_preload_detail: same field_map_json as M10.TankerInbound
-- (loading_supervisor / oil_sample_recovered — those 2 page fields are
-- currently visibleWhen-gated to TankerInbound only on aria:qc-approval, so
-- this mapping is inert for ReturnStockIn today until/unless the page is
-- extended to show them there too; added now for structural completeness so
-- the gap doesn't need rediscovering later). qc_decision itself is handled
-- by QcApprovalDecisionListener (broadened alongside this migration to cover
-- both workflow codes), not by this generic field_map_json.
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.ReturnStockIn', 'qc_preload_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  '{"loading_supervisor": ["unloadingSupervisorName", "unloading_supervisor_name"], "oil_sample_recovered": ["oilSampleRecovered", "oil_sample_recovered"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.ReturnStockIn' AND target_table = 'qc_preload_detail'
);

-- tanker_unloading_detail: aria:supervisor-unloading's Confirm section
-- (supervisor_id/start_ts/finish_ts/unload_seal_unlock_no) is live for
-- ReturnStockIn already (its RETURN_INBOUND flow_kind section isn't gated
-- off) — same field_map_json as TankerInbound, table name is generic despite
-- its "tanker_" prefix (keyed by workflow_instance_id + flow_variant_code,
-- shared across flow kinds).
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.ReturnStockIn', 'tanker_unloading_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  '{"unloading_supervisor_id": ["supervisor_id"], "unloading_start_ts": ["start_ts"], "unloading_finish_ts": ["finish_ts"], "unloading_seal_unlock_no": ["unload_seal_unlock_no"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.ReturnStockIn' AND target_table = 'tanker_unloading_detail'
);

-- NOTE (pre-existing gap, not fixed here — matches M10.TankerInbound's own
-- current behavior exactly, not scope creep beyond it): aria:loading-
-- allocation's supervisor_name has no m0_workflow_projection row for EITHER
-- workflow — loading_allocation_detail is only wired for FGLoading/
-- PackLoading. Supervisor allocation still transitions the workflow
-- correctly; only the supervisor_name value itself isn't persisted anywhere
-- for TankerInbound or ReturnStockIn today.
