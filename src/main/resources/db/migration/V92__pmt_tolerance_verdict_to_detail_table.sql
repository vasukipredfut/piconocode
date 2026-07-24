-- V92 — the remaining 4 PMT fields on aria:pmt-weight-diff-check
-- (expected_weight_kg, loaded_weight_kg, pmt_variance_pct, pmt_within_tolerance)
-- move out of gate_pass into pmt_weight_diff_detail (V88), same treatment as
-- V80/V81/V83/V85 for the other satellite fields, and the same treatment
-- variance_reason itself just got (WorkflowProjectionListener skip, this
-- session). DispatchToleranceListener.java now upserts these 4 columns into
-- pmt_weight_diff_detail instead of UPDATE-ing gate_pass; GenericEntityController's
-- GATE_PASS_SATELLITE_SELECT picks them up through the existing pwd join so no
-- page-JSON changes are needed (onChangeFill still reads the same field names
-- off the GatePass entity read path).
--
-- DispatchToleranceListener runs for all 3 tolerance-enabled workflows
-- (FGLoading, PackLoading, TankerOutbound) keyed only by workflow_instance_id
-- + tenant_id, so TankerOutbound instances (whose PMT-equivalent page is
-- aria:weight-diff-entry, not aria:pmt-weight-diff-check) also get a row here
-- for these 4 columns — harmless, same join covers them too.

-- 1. New columns on the satellite table.
ALTER TABLE pmt_weight_diff_detail
  ADD COLUMN IF NOT EXISTS expected_weight_kg   DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS loaded_weight_kg      DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pmt_variance_pct      DECIMAL(8,3)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pmt_within_tolerance  VARCHAR(8)    DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'PmtWeightDiffDetail',c.fc,c.lbl,c.dt,0,1,0,0,c.wk
FROM (SELECT 'expected_weight_kg' fc,'Expected Weight (kg)' lbl,'DECIMAL' dt,'number' wk
      UNION ALL SELECT 'loaded_weight_kg','Loaded Weight W1-W2 (kg)','DECIMAL','number'
      UNION ALL SELECT 'pmt_variance_pct','PMT Variance %','DECIMAL','number'
      UNION ALL SELECT 'pmt_within_tolerance','PMT Within Tolerance','STRING','text') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PmtWeightDiffDetail' AND field_code=c.fc);

-- 2. Drop the now-dead columns off gate_pass (nothing writes to them once
--    DispatchToleranceListener ships alongside this migration).
ALTER TABLE gate_pass
  DROP COLUMN IF EXISTS expected_weight_kg,
  DROP COLUMN IF EXISTS loaded_weight_kg,
  DROP COLUMN IF EXISTS pmt_variance_pct,
  DROP COLUMN IF EXISTS pmt_within_tolerance;

DELETE FROM entity_field_registry
WHERE entity_code='GatePass'
  AND field_code IN ('expected_weight_kg','loaded_weight_kg','pmt_variance_pct','pmt_within_tolerance');
