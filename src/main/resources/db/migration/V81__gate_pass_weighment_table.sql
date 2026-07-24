-- V81 — Weighment Capture (aria:weighment-capture): w1/w2/w3_weight_kg get
-- their own dedicated table, gate_pass_weighment — clean cut, no gate_pass
-- duplicate.
--
-- These 3 columns are read back from gate_pass by five other consumers:
--   - aria:weighment-capture itself (previous-weight reference display)
--   - aria:pmt-weight-diff-check, aria:pmt-camera-verification (w1, w2)
--   - aria:manager-weight-approval, aria:weight-diff-entry (w2, w3)
--     — all four via onChangeFill (GenericEntityController.getRow()/
--     getRowByField(), fixed with a standing LEFT JOIN to this table)
--   - DispatchToleranceListener.java (direct SELECT — PMT tolerance verdict,
--     fixed in this same change to LEFT JOIN gate_pass_weighment)
-- No page-JSON edits needed — the onChangeFill bindings (e.g.
-- "w1_weight_kg": "w1_weight_kg") keep resolving because the joined row
-- still carries those keys.
--
-- Applies to all 4 workflows that use aria:weighment-capture: M10.FGLoading,
-- M10.PackLoading, M10.TankerInbound, M10.TankerOutbound.

-- 1. New table.
CREATE TABLE IF NOT EXISTS gate_pass_weighment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  w1_weight_kg DECIMAL(10,3) DEFAULT NULL,
  w2_weight_kg DECIMAL(10,3) DEFAULT NULL,
  w3_weight_kg DECIMAL(10,3) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_gpw_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register the entity.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'GatePassWeighment', 'Gate Pass Weighment', 'gate_pass_weighment',
       'W1/W2/W3 weighbridge readings per gate pass (aria:weighment-capture).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='GatePassWeighment');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'GatePassWeighment',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING',1,1,0,'text'
      UNION ALL SELECT 'w1_weight_kg','First Weight (kg)','DECIMAL',0,1,0,'number'
      UNION ALL SELECT 'w2_weight_kg','Second Weight (kg)','DECIMAL',0,1,0,'number'
      UNION ALL SELECT 'w3_weight_kg','Third Weight (kg)','DECIMAL',0,1,0,'number') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code=c.fc);

-- 3. Project the weight fields into the new table.
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, terminal_states, terminal_ts_column, field_map_json, tolerance_pct)
SELECT -1, w.code, 'gate_pass_weighment', 'workflow_instance_id', 'current_step', 'flow_variant_code', NULL, NULL, NULL, NULL,
       '{"w1_weight_kg": ["w1_weight_kg", "w1WeightKg", "grossKg"], "w2_weight_kg": ["w2_weight_kg", "w2WeightKg"], "w3_weight_kg": ["w3_weight_kg", "w3WeightKg"]}',
       NULL
FROM (SELECT 'M10.FGLoading' code UNION ALL SELECT 'M10.PackLoading' UNION ALL SELECT 'M10.TankerInbound' UNION ALL SELECT 'M10.TankerOutbound') w
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=-1 AND workflow_code=w.code AND target_table='gate_pass_weighment');

-- 4. Stop duplicating w1/w2/w3_weight_kg into gate_pass (state_columns keep
--    status/current_step updating as before).
UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json, '$.w1_weight_kg', '$.w2_weight_kg', '$.w3_weight_kg')
WHERE target_table='gate_pass' AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerInbound','M10.TankerOutbound');
