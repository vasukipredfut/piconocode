-- V79 — QC Pre-Load Approval: stop writing loading_supervisor /
-- loading_tank_location / qc_reject_reason into gate_pass. Give them their
-- own dedicated table (qc_preload_detail — same pattern as
-- dispatch_vehicle_detail: one row per workflow_instance_id, projected via
-- m0_workflow_projection, no bespoke Java) — clean cut, no gate_pass
-- duplicate, same convention as V80/V81. onChangeFill reads of these 3
-- fields (none currently — no other page binds them) stay safe either way
-- thanks to GenericEntityController's standing LEFT JOIN to this table.

-- 1. New table.
CREATE TABLE IF NOT EXISTS qc_preload_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  loading_supervisor VARCHAR(128) DEFAULT NULL,
  loading_tank_location VARCHAR(64) DEFAULT NULL,
  qc_reject_reason VARCHAR(255) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_qpd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register the entity (same convention as DispatchVehicleDetail).
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'QcPreloadDetail', 'QC Pre-Load Detail', 'qc_preload_detail',
       'QC pre-load supervisor / tank-location / reject-reason capture per gate pass.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='QcPreloadDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'QcPreloadDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING',1,1,0,'text'
      UNION ALL SELECT 'loading_supervisor','Loading Supervisor','STRING',1,1,1,'text'
      UNION ALL SELECT 'loading_tank_location','Loading Location','STRING',1,1,1,'text'
      UNION ALL SELECT 'qc_reject_reason','QC Reject Reason','STRING',0,0,1,'textarea') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='QcPreloadDetail' AND field_code=c.fc);

-- 3. Project the QC pre-load fields into the new table (one config per
--    workflow — same 3 flows that share aria:qc-preload-approval).
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, terminal_states, terminal_ts_column, field_map_json, tolerance_pct)
SELECT -1, w.code, 'qc_preload_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', NULL, NULL, NULL, NULL,
       '{"loading_supervisor": ["loadingSupervisor", "loading_supervisor"], "loading_tank_location": ["loadingTankLocation", "loading_tank_location"], "qc_reject_reason": ["qcRejectReason", "qc_reject_reason"]}',
       NULL
FROM (SELECT 'M10.FGLoading' code UNION ALL SELECT 'M10.PackLoading' UNION ALL SELECT 'M10.TankerOutbound') w
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=-1 AND workflow_code=w.code AND target_table='qc_preload_detail');

-- 4. Stop duplicating those same 3 fields into gate_pass — it now only picks
--    up status/current_step (state_columns) on this step, same as before for
--    every other field this page doesn't own.
UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json, '$.loading_supervisor', '$.loading_tank_location', '$.qc_reject_reason')
WHERE target_table='gate_pass' AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerOutbound');
