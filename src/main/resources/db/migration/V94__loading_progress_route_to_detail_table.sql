-- V94 — Group 2 (partial) of gate_pass column cleanup: route_id.
--
-- route_id is operator-picked on aria:loading-in-progress (V74 made it a
-- multiselect of the vehicle's OrderDispatchDetail routes) when "Start
-- Loading" fires WEIGHMENT_1 -> LOADING_IN_PROGRESS. Only M10.FGLoading,
-- M10.PackLoading and M10.TankerOutbound use this step (confirmed: their
-- gate_pass field_map_json carries route_id; PackUnload/ReturnStockIn/
-- TankerInbound don't).
--
-- DispatchToleranceListener.java reads gate_pass.route_id directly (SQL join)
-- for the expected-weight-by-route fallback basis — fixed alongside this
-- migration to read loading_progress_detail.route_id instead.
--
-- processed_qty/remaining_qty/dispatch_qty (same original gate_pass block as
-- route_id) are NOT moved here — no page-JSON or Java code in this backend
-- writes them (only display references: list "columns"/"select" arrays and
-- onChangeFill reads), so their actual write path is unconfirmed from this
-- repo alone. Left in place pending confirmation.

-- 1. New table.
CREATE TABLE IF NOT EXISTS loading_progress_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  route_id VARCHAR(64) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_lpd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register the entity.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'LoadingProgressDetail', 'Loading Progress Detail', 'loading_progress_detail',
       'Route selection per gate pass (aria:loading-in-progress).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='LoadingProgressDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'LoadingProgressDetail',c.fc,c.lbl,c.dt,1,1,1,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING','text'
      UNION ALL SELECT 'route_id','Route ID','STRING','text') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LoadingProgressDetail' AND field_code=c.fc);

-- 3. Project route_id into the new table for the 3 workflows that use it.
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, terminal_states, terminal_ts_column, field_map_json, tolerance_pct)
SELECT -1, w.code, 'loading_progress_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', NULL, NULL, NULL, NULL,
       '{"route_id": ["routeId", "route_id"]}',
       NULL
FROM (SELECT 'M10.FGLoading' code UNION ALL SELECT 'M10.PackLoading' UNION ALL SELECT 'M10.TankerOutbound') w
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=-1 AND workflow_code=w.code AND target_table='loading_progress_detail');

-- 4. Stop duplicating route_id into gate_pass (all tenant_id rows).
UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json, '$.route_id')
WHERE target_table='gate_pass'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerOutbound');

-- 5. Drop the now-dead column off gate_pass.
ALTER TABLE gate_pass DROP COLUMN IF EXISTS route_id;

DELETE FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='route_id';

-- 6. Loading Dashboard's explicit "select" referenced bare route_id (resolves
--    to gate_pass.route_id in DataSourceQueryService.appendSelect — bare
--    columns always qualify against the root table, never a join alias)
--    alongside its existing lad. join. Re-point it at the new table's join.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json='{"kind": "list", "schemaVersion": 2, "dataSource": {"type": "datasource", "spec": {"type": "entity", "entity": "GatePass", "joins": [{"entity": "LoadingAllocationDetail", "as": "lad", "on": "gate_pass.workflow_instance_id = lad.workflow_instance_id"}, {"entity": "LoadingProgressDetail", "as": "lpd", "on": "gate_pass.workflow_instance_id = lpd.workflow_instance_id"}], "filter": {"field": "status", "op": "in", "value": ["LOADING_IN_PROGRESS", "DISPATCH_LOAD_ENTRY"]}, "select": ["id", "gp_number", "vehicle_no", "flow_variant_code", "processed_qty", "remaining_qty", "status", "lad.loading_bay as loading_bay", "lad.reference_no as reference_no", "lpd.route_id as route_id"]}}, "header": {"title": "Bay-wise Loading"}, "columns": [{"field": "loading_bay", "label": "Bay"}, {"field": "vehicle_no", "label": "Vehicle"}, {"field": "gp_number", "label": "GP #"}, {"field": "reference_no", "label": "Order ID"}, {"field": "flow_variant_code", "label": "Flow", "render": "badge"}, {"field": "processed_qty", "label": "Processed Qty"}, {"field": "remaining_qty", "label": "Remaining Qty"}, {"field": "status", "label": "Status", "render": "badge"}], "filters": [{"field": "lad.loading_bay", "label": "Bay", "type": "text"}], "pagination": {"pageSize": 25}, "rowActions": [{"label": "📦 Order", "action": "page.open", "page": "aria:loading-order", "params": {"route": "row.route_id"}}]}'
WHERE pd.code='aria:loading-dashboard' AND pv.body_json LIKE '%"route_id", "lad.loading_bay%';
