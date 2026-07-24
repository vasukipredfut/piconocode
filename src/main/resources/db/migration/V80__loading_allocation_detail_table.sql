-- V80 — Loading Supervisor Assign (aria:loading-allocation): everything this
-- page captures (supervisor, loading bay, OrderApp order/customer/qty) now
-- lives ONLY in its own table — clean cut, no gate_pass duplicate. 4 of
-- these 7 fields (supervisor_name, loading_bay, reference_no, ordered_qty)
-- pre-existed as gate_pass columns and are read by other consumers:
--   - aria:pmt-weight-diff-check reads ordered_qty + reference_no via
--     onChangeFill
--   - Loading Dashboard (aria:loading-dashboard) lists loading_bay +
--     reference_no
--   - DispatchToleranceListener.java reads ordered_qty directly by SQL
-- onChangeFill's data source is GenericEntityController.getRow()/
-- getRowByField() (a single-table GatePass fetch) — fixed there with a
-- standing LEFT JOIN to this table (and qc_preload_detail /
-- gate_pass_weighment) so those bindings keep resolving with no gate_pass
-- duplicate. The Dashboard and DispatchToleranceListener are fixed in this
-- same change (dashboard body below; listener joins loading_allocation_detail
-- directly). customer_name/customer_route/ordered_cases never existed on
-- gate_pass at all — those simply move, nothing to preserve there.
--
-- Only M10.FGLoading and M10.PackLoading use this page (LOADING_ALLOCATED
-- state) — M10.TankerOutbound has no such step.

-- 1. New table.
CREATE TABLE IF NOT EXISTS loading_allocation_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  supervisor_name VARCHAR(128) DEFAULT NULL,
  loading_bay VARCHAR(16) DEFAULT NULL,
  reference_no VARCHAR(64) DEFAULT NULL,
  customer_name VARCHAR(255) DEFAULT NULL,
  customer_route VARCHAR(64) DEFAULT NULL,
  ordered_cases DECIMAL(12,3) DEFAULT NULL,
  ordered_qty DECIMAL(12,3) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_lad_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register the entity.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'LoadingAllocationDetail', 'Loading Allocation Detail', 'loading_allocation_detail',
       'Loading-supervisor / bay / OrderApp-order capture per gate pass (aria:loading-allocation).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='LoadingAllocationDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'LoadingAllocationDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING',1,1,0,'text'
      UNION ALL SELECT 'supervisor_name','Supervisor Name','STRING',1,1,1,'text'
      UNION ALL SELECT 'loading_bay','Loading Bay','STRING',1,1,1,'text'
      UNION ALL SELECT 'reference_no','OrderApp Order #','STRING',1,1,1,'text'
      UNION ALL SELECT 'customer_name','Customer','STRING',1,1,1,'text'
      UNION ALL SELECT 'customer_route','Route','STRING',1,1,1,'text'
      UNION ALL SELECT 'ordered_cases','Ordered Cases','DECIMAL',0,1,0,'number'
      UNION ALL SELECT 'ordered_qty','Ordered Wt (kg)','DECIMAL',0,1,0,'number') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LoadingAllocationDetail' AND field_code=c.fc);

-- 3. Project every field this page captures into the new table.
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, terminal_states, terminal_ts_column, field_map_json, tolerance_pct)
SELECT -1, w.code, 'loading_allocation_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', NULL, NULL, NULL, NULL,
       '{"supervisor_name": ["supervisor_name", "supervisorName"], "loading_bay": ["loadingBay", "loading_bay"], "reference_no": ["orderRef", "order_ref", "reference_no"], "customer_name": ["customerName", "customer_name"], "customer_route": ["orderRoute", "customer_route"], "ordered_cases": ["orderedCases", "ordered_cases"], "ordered_qty": ["orderedQty", "ordered_qty", "orderedWeightKg"]}',
       NULL
FROM (SELECT 'M10.FGLoading' code UNION ALL SELECT 'M10.PackLoading') w
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=-1 AND workflow_code=w.code AND target_table='loading_allocation_detail');

-- 4. Stop duplicating those fields into gate_pass (state_columns keep
--    status/current_step updating as before). JSON_REMOVE on a path that
--    was never set (e.g. reference_no/ordered_qty on FGLoading) is a no-op.
UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json,
      '$.supervisor_name', '$.loading_bay', '$.reference_no', '$.ordered_qty')
WHERE target_table='gate_pass' AND workflow_code IN ('M10.FGLoading','M10.PackLoading');

-- 5. Loading Dashboard showed Bay + Order ID straight off gate_pass — join
--    the new table so those columns keep working (list pages compile through
--    DataSourceQueryService, which supports joins — confirmed via
--    DataSourceController's "Renderer's ListView POSTs here" contract and the
--    absence of any other list/search endpoint in GenericEntityController).
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json='{"kind": "list", "schemaVersion": 2, "dataSource": {"type": "datasource", "spec": {"type": "entity", "entity": "GatePass", "joins": [{"entity": "LoadingAllocationDetail", "as": "lad", "on": "gate_pass.workflow_instance_id = lad.workflow_instance_id"}], "filter": {"field": "status", "op": "in", "value": ["LOADING_IN_PROGRESS", "DISPATCH_LOAD_ENTRY"]}, "select": ["id", "gp_number", "vehicle_no", "flow_variant_code", "processed_qty", "remaining_qty", "status", "route_id", "lad.loading_bay as loading_bay", "lad.reference_no as reference_no"]}}, "header": {"title": "Bay-wise Loading"}, "columns": [{"field": "loading_bay", "label": "Bay"}, {"field": "vehicle_no", "label": "Vehicle"}, {"field": "gp_number", "label": "GP #"}, {"field": "reference_no", "label": "Order ID"}, {"field": "flow_variant_code", "label": "Flow", "render": "badge"}, {"field": "processed_qty", "label": "Processed Qty"}, {"field": "remaining_qty", "label": "Remaining Qty"}, {"field": "status", "label": "Status", "render": "badge"}], "filters": [{"field": "lad.loading_bay", "label": "Bay", "type": "text"}], "pagination": {"pageSize": 25}, "rowActions": [{"label": "📦 Order", "action": "page.open", "page": "aria:loading-order", "params": {"route": "row.route_id"}}]}'
WHERE pd.code='aria:loading-dashboard' AND pv.body_json NOT LIKE '%LoadingAllocationDetail%';
