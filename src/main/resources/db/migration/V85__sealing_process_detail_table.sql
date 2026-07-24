-- V85 — Sealing Process (aria:sealing-process): dedicated table
-- (sealing_process_detail — same pattern as V79/V80/V83), clean cut off
-- gate_pass (same convention: drop the now-dead gate_pass columns per user
-- decision, no historical data needed).
--
-- Behaviour change: sealing_started_at / sealing_finished_at stop being
-- operator-typed fields. The page keeps ONE submission ("Sealing Done" —
-- no separate "start" step, no new workflow state). On that submission the
-- backend auto-computes both timestamps:
--   sealing_started_at = when this gate pass ENTERED the state right before
--                         SEALING_DONE (looked up from workflow_event history
--                         — the timestamp of the transition INTO
--                         previousState), i.e. "when it became available for
--                         sealing".
--   sealing_finished_at = now (the moment "Sealing Done" is submitted).
-- sealing_status is no longer a dropdown either — submitting this page always
-- means sealing is complete, so the backend stamps it 'COMPLETED' itself.
-- See the accompanying SealingTimestampListener.java (new file) for the
-- timestamp computation — WorkflowProjectionListener's generic field_map
-- can't look up workflow_event history, so this one piece needs bespoke Java,
-- same as DispatchToleranceListener's tolerance math.
--
-- Only M10.FGLoading and M10.PackLoading use aria:sealing-process
-- (M10.TankerOutbound has no such step).

-- 1. New table.
CREATE TABLE IF NOT EXISTS sealing_process_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  sealing_no VARCHAR(48) DEFAULT NULL,
  sealing_supervisor VARCHAR(96) DEFAULT NULL,
  seal_lock_status VARCHAR(24) DEFAULT NULL,
  sealing_status VARCHAR(16) DEFAULT NULL,
  sealing_started_at DATETIME DEFAULT NULL,
  sealing_finished_at DATETIME DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_spd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register the entity.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'SealingProcessDetail', 'Sealing Process Detail', 'sealing_process_detail',
       'Sealing number/supervisor/lock-status + auto-computed start/finish timestamps per gate pass.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='SealingProcessDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'SealingProcessDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING',1,1,0,'text'
      UNION ALL SELECT 'sealing_no','Sealing No','STRING',1,1,1,'text'
      UNION ALL SELECT 'sealing_supervisor','Sealing Supervisor','STRING',1,1,1,'text'
      UNION ALL SELECT 'seal_lock_status','Sealing Status','STRING',1,1,0,'text'
      UNION ALL SELECT 'sealing_status','Sealing Completion Status','STRING',1,1,0,'text'
      UNION ALL SELECT 'sealing_started_at','Sealing Start','DATETIME',1,1,0,'datetime'
      UNION ALL SELECT 'sealing_finished_at','Sealing Finish','DATETIME',1,1,0,'datetime') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SealingProcessDetail' AND field_code=c.fc);

-- 3. Project the operator-entered fields (NOT the two auto-computed
--    timestamps or sealing_status — SealingTimestampListener.java owns those).
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, terminal_states, terminal_ts_column, field_map_json, tolerance_pct)
SELECT -1, w.code, 'sealing_process_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', NULL, NULL, NULL, NULL,
       '{"sealing_no": ["sealing_no", "sealingNo"], "sealing_supervisor": ["sealing_supervisor", "sealingSupervisor"], "seal_lock_status": ["seal_lock_status", "sealLockStatus"]}',
       NULL
FROM (SELECT 'M10.FGLoading' code UNION ALL SELECT 'M10.PackLoading') w
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=-1 AND workflow_code=w.code AND target_table='sealing_process_detail');

-- 4. Stop duplicating these 6 fields into gate_pass, then drop the now-dead
--    columns (per user decision — no historical data needed).
UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json,
      '$.sealing_no', '$.sealing_supervisor', '$.seal_lock_status',
      '$.sealing_status', '$.sealing_started_at', '$.sealing_finished_at')
WHERE target_table='gate_pass' AND workflow_code IN ('M10.FGLoading','M10.PackLoading');

ALTER TABLE gate_pass
  DROP COLUMN IF EXISTS sealing_no,
  DROP COLUMN IF EXISTS sealing_supervisor,
  DROP COLUMN IF EXISTS seal_lock_status,
  DROP COLUMN IF EXISTS sealing_status,
  DROP COLUMN IF EXISTS sealing_started_at,
  DROP COLUMN IF EXISTS sealing_finished_at;

DELETE FROM entity_field_registry
WHERE entity_code='GatePass'
  AND field_code IN ('sealing_no','sealing_supervisor','seal_lock_status',
                      'sealing_status','sealing_started_at','sealing_finished_at');

-- 5. Sealing Process capture form: drop the sealing_status /
--    sealing_started_at / sealing_finished_at fields — everything else
--    (queue, gpNumber picker joined/filtered on
--    WorkflowInstance.assigned_role='LoadingSupervisor', sealing_no,
--    sealing_supervisor, seal_lock_status, the Sealing Done button) is
--    unchanged from V47.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json='{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "dataSources": [{"id": "q", "kind": "rest", "method": "GET", "url": "/api/workflow/queue/Dispatch"}], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Sealing — Queue", "span": 12, "subtitle": "Loaded vehicles awaiting sealing (packing / FG). Record the seal number.", "children": [{"kind": "WorkflowQueueList", "persona": "Dispatch", "dataSource": "q", "pageSize": 25, "cardFields": ["gpNumber", "vehicleNo", "flowVariant", "currentState"]}]}, {"kind": "Section", "title": "Capture & Submit", "span": 12, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (pick one)", "type": "select", "required": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "GatePass", "joins": [{"entity": "WorkflowInstance", "as": "wi", "on": "gate_pass.workflow_instance_id = wi.id"}], "filter": {"and": [{"field": "wi.assigned_role", "op": "eq", "value": "LoadingSupervisor"}, {"field": "wi.status", "op": "eq", "value": "OPEN"}]}, "select": ["id", "gp_number"], "sort": [{"field": "gp_number", "dir": "desc"}]}, "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "flow_variant_code": "flow_variant_code", "instanceId": "workflow_instance_id"}, "scannable": true, "freeSolo": true}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "flow_variant_code", "label": "Flow", "type": "text", "readOnly": true}, {"name": "sealing_no", "label": "Sealing No", "type": "text", "required": true}, {"name": "sealing_supervisor", "label": "Sealing Supervisor", "type": "text"}, {"name": "seal_lock_status", "label": "Sealing Status", "type": "select", "options": [{"label": "Completed", "value": "AUTO_LOCK"}, {"label": "Seal No Updated", "value": "SEAL_NO_UPDATE"}]}]}, "children": [{"kind": "WorkflowActionButton", "label": "Sealing Done", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "SEALING_DONE", "color": "primary"}]}]}]}]}'
WHERE pd.code='aria:sealing-process' AND pv.body_json LIKE '%sealing_started_at%';
