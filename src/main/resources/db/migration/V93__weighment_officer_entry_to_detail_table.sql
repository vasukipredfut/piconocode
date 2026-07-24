-- V93 — bug fix (found while continuing the gate_pass column cleanup) +
-- Group 1 of that cleanup (weight_officer / weight_entry_at).
--
-- BUG FIX: V47's tenant_id=1 per-tenant override rows for target_table=
-- 'gate_pass' (all 6 M10 workflow codes) still carry field_map_json keys for
-- columns V84/V85 already dropped off gate_pass (w1_weight_kg, w2_weight_kg,
-- w3_weight_kg, loading_supervisor, loading_tank_location, qc_reject_reason,
-- supervisor_name, loading_bay, reference_no, ordered_qty, sealing_no,
-- sealing_supervisor, seal_lock_status, sealing_status, sealing_started_at,
-- sealing_finished_at). WorkflowProjectionListener prefers a tenant_id=1 row
-- over tenant_id=-1 for the same target_table (ORDER BY tenant_id DESC +
-- putIfAbsent), so for tenant_id=1, any transition whose submitted-data bag
-- contains one of those stale keys makes the gate_pass UPDATE/INSERT reference
-- a column that no longer exists -> jdbc throws "Unknown column" ->
-- onTransition's per-config try/catch swallows it (logs a warning) -> the
-- WHOLE gate_pass row, INCLUDING status/current_step sync, silently stops
-- updating for that transition. Since WorkflowInstance.submittedDataJson
-- accumulates every page's fields for the instance's entire lifetime, once
-- weighment-capture or sealing-process has fired once on an instance, every
-- later transition carries the stale keys and keeps failing this way. Cleaning
-- these out now since the same field_map_json blobs are being edited anyway
-- for Group 1 below.
--
-- GROUP 1: weight_officer / weight_entry_at are captured on
-- aria:weighment-capture — same page/step as w1/w2/w3_weight_kg (V81/V83) —
-- so they move into weighment_capture_detail alongside those columns, same
-- reasoning.

-- 1. Extend the satellite table.
ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS weight_officer  VARCHAR(128) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS weight_entry_at TIMESTAMP NULL DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'WeighmentCaptureDetail',c.fc,c.lbl,c.dt,1,1,1,0,c.wk
FROM (SELECT 'weight_officer' fc,'Weighment Officer' lbl,'STRING' dt,'text' wk
      UNION ALL SELECT 'weight_entry_at','Weight Entry Time','DATETIME','datetime') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='WeighmentCaptureDetail' AND field_code=c.fc);

-- 2. Add the 2 keys to the existing weighment_capture_detail projection rows.
UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json,
      '$.weight_officer', JSON_ARRAY('weightOfficer','weight_officer'),
      '$.weight_entry_at', JSON_ARRAY('weightEntryAt','weight_entry_at'))
WHERE target_table='weighment_capture_detail'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerInbound','M10.TankerOutbound');

-- 3. Stop duplicating weight_officer/weight_entry_at (and the bug-fix stale
--    keys above) into gate_pass, across every tenant_id row for the 6 M10
--    dispatch workflows. JSON_REMOVE ignores paths that don't exist in a
--    given row, so this is safe to apply across tenant_id IN (-1,1) at once.
UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json,
      '$.weight_officer', '$.weight_entry_at',
      '$.w1_weight_kg', '$.w2_weight_kg', '$.w3_weight_kg',
      '$.loading_supervisor', '$.loading_tank_location', '$.qc_reject_reason',
      '$.supervisor_name', '$.loading_bay', '$.reference_no', '$.ordered_qty',
      '$.sealing_no', '$.sealing_supervisor', '$.seal_lock_status',
      '$.sealing_status', '$.sealing_started_at', '$.sealing_finished_at')
WHERE target_table='gate_pass'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.PackUnload',
                         'M10.ReturnStockIn','M10.TankerInbound','M10.TankerOutbound');

-- 4. Drop the now-dead columns off gate_pass.
ALTER TABLE gate_pass
  DROP COLUMN IF EXISTS weight_officer,
  DROP COLUMN IF EXISTS weight_entry_at;

DELETE FROM entity_field_registry
WHERE entity_code='GatePass' AND field_code IN ('weight_officer','weight_entry_at');
