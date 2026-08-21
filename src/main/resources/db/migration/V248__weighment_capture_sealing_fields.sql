-- aria:weighment-capture's "Reading" section has 3 sealing fields --
-- "Sealing Status" (select name="seal_lock_status", values AUTO_LOCK/
-- SEAL_NO_UPDATE), "Sealing No" (name="sealing_no"), and "Sealing
-- Supervisor" (name="sealing_supervisor") -- shown for every flow variant
-- when (weight_seq=SECOND && loading_type=LOADING) or (weight_seq=FIRST &&
-- loading_type=UN_LOADING). None of the 3 ever reached weighment_capture_
-- detail:
--   - sealing_no / sealing_supervisor: no column at all on this table (they
--     were dropped off gate_pass by V85 and never re-homed here).
--   - seal_lock_status: a same-named column already exists on this table
--     (V125), but it's reserved for TankerInbound's UNRELATED YES/NO field
--     ("Seal Lock Status", name="seal_lock_status_yn") -- V125's own comment
--     flags this page's AUTO_LOCK/SEAL_NO_UPDATE dropdown as landing nowhere.
-- Add 3 new columns (avoiding the seal_lock_status name collision with the
-- TankerInbound YES/NO field) and alias the actual submitted keys into them,
-- for every workflow code sharing this page/table.

ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS sealing_no         VARCHAR(64)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sealing_supervisor VARCHAR(128) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sealing_status      VARCHAR(24)  DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'WeighmentCaptureDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk,c.opts
FROM (SELECT 'sealing_no' fc,'Sealing No' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk,NULL opts
      UNION ALL SELECT 'sealing_supervisor','Sealing Supervisor','STRING',1,1,1,'text',NULL
      UNION ALL SELECT 'sealing_status','Sealing Status','STRING',1,1,0,'select','["AUTO_LOCK","SEAL_NO_UPDATE"]') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='WeighmentCaptureDetail' AND field_code=c.fc);

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json,
      '$.sealing_no', JSON_ARRAY('sealingNo','sealing_no'),
      '$.sealing_supervisor', JSON_ARRAY('sealingSupervisor','sealing_supervisor'),
      '$.sealing_status', JSON_ARRAY('seal_lock_status','sealLockStatus'))
WHERE target_table='weighment_capture_detail'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.PackUnload',
                         'M10.ReturnStockIn','M10.TankerInbound','M10.TankerOutbound');
