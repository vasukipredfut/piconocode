-- Moves the 4 PMT tolerance-verdict columns (expected_weight_kg,
-- loaded_weight_kg, pmt_variance_pct, pmt_within_tolerance) off
-- pmt_weight_diff_detail (V88/V92) onto manager_qty_approval_detail (V90) --
-- this data is already computed entirely server-side by
-- DispatchToleranceListener (FGLoading/PackLoading/TankerOutbound) and
-- TankerOutbound's own branch of PmtSharedReasonListener; it doesn't need
-- its own dedicated table, and the business wants it living alongside the
-- manager's qty-approval decision instead of a separate PMT satellite.
--
-- pmt_weight_diff_detail itself is NOT dropped and keeps its own
-- pmt_variance_reason/queue_reason columns untouched -- those are the PMT
-- page's own operator-entered reason capture (PmtSharedReasonListener,
-- WEIGHMENT_2/SEALING_DONE case), a completely different concern from the
-- 4 computed columns moving here. Existing historical rows there are left
-- as-is (not backfilled/migrated) -- going forward, only manager_qty_approval_detail
-- gets new values for these 4 columns.
--
-- See DispatchToleranceListener.java / PmtSharedReasonListener.java /
-- GatePassSatelliteJoin.java for the write/read-side changes that pair with
-- this migration.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE manager_qty_approval_detail
  ADD COLUMN IF NOT EXISTS expected_weight_kg   DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS loaded_weight_kg      DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pmt_variance_pct      DECIMAL(8,3)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pmt_within_tolerance  VARCHAR(8)    DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'ManagerQtyApprovalDetail',c.fc,c.lbl,c.dt,0,1,0,0,c.wk
FROM (SELECT 'expected_weight_kg' fc,'Expected Weight (kg)' lbl,'DECIMAL' dt,'number' wk
      UNION ALL SELECT 'loaded_weight_kg','Loaded Weight W1-W2 (kg)','DECIMAL','number'
      UNION ALL SELECT 'pmt_variance_pct','PMT Variance %','DECIMAL','number'
      UNION ALL SELECT 'pmt_within_tolerance','PMT Within Tolerance','STRING','text') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ManagerQtyApprovalDetail' AND field_code=c.fc);
