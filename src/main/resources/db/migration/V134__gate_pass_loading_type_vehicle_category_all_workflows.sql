-- aria:front-gate-create submits loadingType and vehicleCategory for every
-- M10 gate-pass workflow (both fields have been on this shared page since
-- before this session), and gate_pass has had loading_type/vehicle_category
-- columns to receive them for just as long. But only 3 of the 6 workflows'
-- m0_workflow_projection rows for target_table=gate_pass actually map those
-- keys (FGLoading/PackLoading/TankerOutbound) — PackUnload, ReturnStockIn,
-- and TankerInbound don't, so both fields have been silently dropped for
-- those 3 workflows this whole time. Same class of gap as V125's
-- dispatch_vehicle_detail/qc_preload_detail fix for TankerInbound: no
-- projection-config entry means WorkflowProjectionListener has nowhere to
-- write the submitted value, so it's just discarded.
UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(COALESCE(field_map_json, JSON_OBJECT()),
  JSON_OBJECT(
    'loading_type', JSON_ARRAY('loadingType', 'loading_type'),
    'vehicle_category', JSON_ARRAY('vehicleCategory', 'vehicle_category')
  ))
WHERE target_table = 'gate_pass'
  AND workflow_code IN ('M10.PackUnload', 'M10.ReturnStockIn', 'M10.TankerInbound')
  AND (JSON_EXTRACT(field_map_json, '$.loading_type') IS NULL
       OR JSON_EXTRACT(field_map_json, '$.vehicle_category') IS NULL);
