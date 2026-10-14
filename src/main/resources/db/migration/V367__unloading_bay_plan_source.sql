-- unloading_progress_detail_bay rows come from two different shapes:
--   * origin-tank rows (reject unload of what was loaded): bay_name = the tank the oil is taken BACK from;
--   * unload-plan rows the QC set at QC preload (QcPreloadTankAllocationListener, tankUnloadAllocations):
--     bay_name = the tank the oil GOES TO.
-- plan_source marks the second kind ('QC_PRELOAD') so m10.tanker-unload-capture reads bay_name the right way round.
-- NULL = the older / origin-tank rows (and every other flow's rows).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS unloading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS plan_source VARCHAR(16) NULL COMMENT 'QC_PRELOAD = unload plan row, bay_name is the destination tank';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'UnloadingProgressDetailBay', 'plan_source', 'Plan Source', 'STRING', 1, 1, 0, 0, 'text'
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'UnloadingProgressDetailBay')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'UnloadingProgressDetailBay' AND field_code = 'plan_source');
