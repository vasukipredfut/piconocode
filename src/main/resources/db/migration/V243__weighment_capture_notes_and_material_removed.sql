-- aria:weighment-capture captures "Notes (any anomaly)" (textarea) and
-- "Unwanted Material Removed & Checked" (YES/NO select) on the form, but
-- weighment_capture_detail (V83) never had columns for either -- so both
-- values only ever survived in workflow_instance.submitted_data_json and
-- never landed in the queryable table. Same fix shape as V93's
-- weight_officer/weight_entry_at addition to this same table.

-- 1. Extend the satellite table.
ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS notes                      TEXT         DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS unwanted_material_removed   VARCHAR(16)  DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'WeighmentCaptureDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk,c.opts
FROM (SELECT 'notes' fc,'Notes' lbl,'STRING' dt,0 filt,0 sort,1 srch,'textarea' wk,NULL opts
      UNION ALL SELECT 'unwanted_material_removed','Unwanted Material Removed & Checked','STRING',1,1,0,'select','["YES","NO"]') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='WeighmentCaptureDetail' AND field_code=c.fc);

-- 2. Add the 2 keys to every existing weighment_capture_detail projection row.
UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json,
      '$.notes', JSON_ARRAY('notes'),
      '$.unwanted_material_removed', JSON_ARRAY('unwantedMaterialRemoved','unwanted_material_removed'))
WHERE target_table='weighment_capture_detail'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.PackUnload',
                         'M10.ReturnStockIn','M10.TankerInbound','M10.TankerOutbound');
