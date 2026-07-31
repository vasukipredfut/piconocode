-- V182 — Loading Progress screen (aria:loading-in-progress, page 29): schema
-- support for Loading Start Time / Loading Finish Time / Loading Supervisor
-- Name, all displayed on the existing page via a new "Loading Timeline"
-- ListView (added in V183, no new page).
--
-- supervisor_name is a plain form-field mirror: extends the field_map_json
-- already projecting route_id (V94) into loading_progress_detail, no new
-- Java needed for it.
--
-- loading_start_time / loading_finish_time need two INDEPENDENTLY named
-- timestamp columns stamped on two DIFFERENT states (LOADING_IN_PROGRESS /
-- LOADING_COMPLETED). WorkflowProjectionListener.java's existing
-- terminal_ts_column mechanism is a single column under a single
-- terminal_states condition (confirmed by reading the class) -- it cannot
-- do this. Rather than add a whole new bespoke listener (the SealingTimestamp
-- Listener precedent, written for a harder problem needing workflow_event
-- history lookups), this migration adds one new generic column,
-- state_ts_columns_json, to the SAME m0_workflow_projection config table --
-- WorkflowProjectionListener.java is extended (not replaced) in this same
-- change to read it: a JSON map of {state: column}, stamping NOW() into the
-- mapped column when the arriving state is a key in the map. Purely
-- additive -- every other workflow's existing terminal_ts_column config is
-- untouched.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE loading_progress_detail
  ADD COLUMN IF NOT EXISTS supervisor_name VARCHAR(128) DEFAULT NULL AFTER gate_pass_no,
  ADD COLUMN IF NOT EXISTS loading_start_time TIMESTAMP NULL DEFAULT NULL AFTER processed_quantity,
  ADD COLUMN IF NOT EXISTS loading_finish_time TIMESTAMP NULL DEFAULT NULL AFTER loading_start_time;

ALTER TABLE m0_workflow_projection
  ADD COLUMN IF NOT EXISTS state_ts_columns_json TEXT DEFAULT NULL AFTER terminal_ts_column;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'LoadingProgressDetail' b,'supervisor_name' c,'Supervisor' d,'STRING' e,1 f,1 g,1 h,0 i,'text' j UNION ALL
  SELECT -1,'LoadingProgressDetail','loading_start_time','Loading Start Time','DATETIME',0,1,0,0,'datetime' UNION ALL
  SELECT -1,'LoadingProgressDetail','loading_finish_time','Loading Finish Time','DATETIME',0,1,0,0,'datetime'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'LoadingProgressDetail' AND field_code = x.c);

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.supervisor_name', JSON_ARRAY('supervisor_name', 'supervisor_name')),
    state_ts_columns_json = '{"LOADING_IN_PROGRESS":"loading_start_time","LOADING_COMPLETED":"loading_finish_time"}'
WHERE target_table = 'loading_progress_detail'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerOutbound')
  AND (state_ts_columns_json IS NULL OR state_ts_columns_json = '');
