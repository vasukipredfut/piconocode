-- Closes the gap noted after V296: aria:unloading-progress's
-- unloading_start_time/unloading_finish_time had no working
-- m0_workflow_projection mapping, so they only ever survived in
-- workflow_instance.submitted_data_json.
--
-- A row for (M10.ReturnStockIn, tanker_unloading_detail) already existed
-- from before V296's page swap -- still page_code='aria:supervisor-unloading'
-- (the page V296 replaced) and still keyed on that old page's field names
-- ("start_ts"/"finish_ts"). Repoint it at the new page and add the new
-- page's actual field names as additional aliases -- no new table needed,
-- tanker_unloading_detail already has the right columns.
UPDATE m0_workflow_projection
SET page_code = 'aria:unloading-progress',
    field_map_json = JSON_MERGE_PATCH(field_map_json, JSON_OBJECT(
      'unloading_start_ts', JSON_ARRAY('start_ts', 'unloading_start_time', 'unloadingStartTime'),
      'unloading_finish_ts', JSON_ARRAY('finish_ts', 'unloading_finish_time', 'unloadingFinishTime')
    ))
WHERE tenant_id = -1 AND workflow_code = 'M10.ReturnStockIn' AND target_table = 'tanker_unloading_detail';
