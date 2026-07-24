-- V83 — fix a real name collision introduced by V81. `gate_pass_weighment`
-- was NOT a free name: an entity code 'GatePassWeighment' + a table of that
-- name already existed (a per-reading weighbridge event log — columns
-- gate_pass_id, weighbridge_code, reading_no, weight_kg, captured_at — no
-- workflow_instance_id, no w1/w2/w3_weight_kg). V81's
-- `CREATE TABLE IF NOT EXISTS gate_pass_weighment` therefore silently did
-- nothing, and its `entity_registry` insert (guarded on the code already
-- existing) also no-op'd — but its m0_workflow_projection INSERT had no such
-- guard against the pre-existing table, so it went ahead and pointed 4
-- workflows' w1/w2/w3_weight_kg mapping at a table with none of those
-- columns. Confirmed live: qc_preload_detail and loading_allocation_detail
-- (V79/V80) landed with the intended schema — this collision is isolated to
-- gate_pass_weighment.
--
-- Fix: create the real table under a name that doesn't collide
-- (weighment_capture_detail), then repoint the 4 existing projection rows at
-- it instead of deleting/reinserting (keeps their history/ids stable).

-- 1. The real table, correctly named.
CREATE TABLE IF NOT EXISTS weighment_capture_detail (
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
  UNIQUE KEY uq_wcd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register it under a non-colliding entity code.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'WeighmentCaptureDetail', 'Weighment Capture Detail', 'weighment_capture_detail',
       'W1/W2/W3 weighbridge readings per gate pass (aria:weighment-capture).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='WeighmentCaptureDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'WeighmentCaptureDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING',1,1,0,'text'
      UNION ALL SELECT 'w1_weight_kg','First Weight (kg)','DECIMAL',0,1,0,'number'
      UNION ALL SELECT 'w2_weight_kg','Second Weight (kg)','DECIMAL',0,1,0,'number'
      UNION ALL SELECT 'w3_weight_kg','Third Weight (kg)','DECIMAL',0,1,0,'number') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='WeighmentCaptureDetail' AND field_code=c.fc);

-- 3. Repoint the 4 projection rows V81 aimed at the colliding table.
UPDATE m0_workflow_projection
SET target_table = 'weighment_capture_detail'
WHERE target_table = 'gate_pass_weighment'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerInbound','M10.TankerOutbound');
