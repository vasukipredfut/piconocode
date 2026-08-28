-- unloading_progress_detail (V256) never carried flow_variant_code/current_step
-- -- it's written by direct JdbcTemplate INSERTs in the action handlers
-- (M10StartUnloadBayActionHandler / M10StartUnloadingBayActionHandler), not
-- through the generic WorkflowProjectionListener mechanism that auto-stamps
-- those columns elsewhere (gate_pass, weighment_capture_detail, etc.) via
-- m0_workflow_projection's variant_column/state_columns. Added directly here
-- so a datasource query against this table can filter by flow_variant_code
-- or current_step without a join back to workflow_instance.
--
-- flow_variant_code is set once at Start Bay (an instance's workflow_code
-- never changes) and preserved on conflict; current_step is refreshed at
-- both Start Bay and Bay Complete, read fresh from workflow_instance.current_state
-- each time so it doesn't go stale as the instance moves through later states.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE unloading_progress_detail
  ADD COLUMN IF NOT EXISTS flow_variant_code VARCHAR(64) DEFAULT NULL AFTER workflow_instance_id,
  ADD COLUMN IF NOT EXISTS current_step VARCHAR(64) DEFAULT NULL AFTER flow_variant_code;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1, 'UnloadingProgressDetail', c.fc, c.lbl, 'STRING', 1, 1, 1, 0, 'text'
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl UNION ALL SELECT 'current_step','Current Step') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='UnloadingProgressDetail' AND field_code=c.fc);
