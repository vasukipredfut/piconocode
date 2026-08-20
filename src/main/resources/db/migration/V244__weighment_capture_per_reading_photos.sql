-- aria:weighment-capture's "Photo Evidence" section has one wb_display_photo
-- + one vehicle_photo field shared across all 3 weighments (weight_seq
-- FIRST/SECOND/THIRD) -- whichever reading was captured last silently
-- overwrote the previous reading's photo in submitted_data_json, and neither
-- ever had a weighment_capture_detail column anyway (same gap as V243's
-- notes / unwanted_material_removed). Splitting into w1_/w2_/w3_ columns
-- mirrors the existing w1_weight_kg/w2_weight_kg/w3_weight_kg columns on
-- this same table.
--
-- No page JSON change needed: the page keeps submitting the same
-- wb_display_photo/vehicle_photo field names for every weighment.
-- WorkflowProjectionListener.project() (see the isWeighmentCaptureDetail
-- branch added alongside this migration) reads THIS submission's weight_seq
-- value and routes to the matching w1_/w2_/w3_ column itself.

ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS w1_wb_display_photo TEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS w1_vehicle_photo    TEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS w2_wb_display_photo TEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS w2_vehicle_photo    TEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS w3_wb_display_photo TEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS w3_vehicle_photo    TEXT DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'WeighmentCaptureDetail',c.fc,c.lbl,'STRING',0,0,0,0,'file'
FROM (SELECT 'w1_wb_display_photo' fc,'First Weighment — Weighbridge Display Photo' lbl
      UNION ALL SELECT 'w1_vehicle_photo','First Weighment — Vehicle on Bridge Photo'
      UNION ALL SELECT 'w2_wb_display_photo','Second Weighment — Weighbridge Display Photo'
      UNION ALL SELECT 'w2_vehicle_photo','Second Weighment — Vehicle on Bridge Photo'
      UNION ALL SELECT 'w3_wb_display_photo','Third Weighment — Weighbridge Display Photo'
      UNION ALL SELECT 'w3_vehicle_photo','Third Weighment — Vehicle on Bridge Photo') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='WeighmentCaptureDetail' AND field_code=c.fc);

-- Field-map keys stay the literal page field names (wb_display_photo /
-- vehicle_photo) -- WorkflowProjectionListener's isWeighmentCaptureDetail
-- branch intercepts these two keys specifically and resolves the actual
-- w1_/w2_/w3_ target column from weight_seq at write time, instead of the
-- generic fallback writing to a column literally named "wb_display_photo".
UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json,
      '$.wb_display_photo', JSON_ARRAY('wbDisplayPhoto','wb_display_photo'),
      '$.vehicle_photo',    JSON_ARRAY('vehiclePhoto','vehicle_photo'))
WHERE target_table='weighment_capture_detail'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.PackUnload',
                         'M10.ReturnStockIn','M10.TankerInbound','M10.TankerOutbound');
