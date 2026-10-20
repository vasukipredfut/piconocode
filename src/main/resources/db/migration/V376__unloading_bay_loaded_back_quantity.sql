-- Tanker Inbound reject: oil unloaded into the tanks is taken back out and loaded on the vehicle again (aria:qc-approval "Load Again",
-- m10.tanker-load-capture). The unload rows kept saying everything was still unloaded, so a location loaded back in part still read as
-- fully unloaded. loaded_back_quantity counts what has been taken back, per unload row, so the screen can show unloaded / loaded back /
-- still in the tank (processed_quantity - loaded_back_quantity). processed_quantity is never changed. A row fully loaded back becomes
-- REVERSED. The parent keeps the total. Mirror of loading_progress_detail_bay.unloaded_quantity (V375).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS unloading_progress_detail_bay ADD COLUMN IF NOT EXISTS loaded_back_quantity DECIMAL(12,3) NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS unloading_progress_detail     ADD COLUMN IF NOT EXISTS loaded_back_quantity DECIMAL(12,3) NOT NULL DEFAULT 0;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'UnloadingProgressDetailBay' b,'loaded_back_quantity' c,'Loaded Back Qty' d,'DECIMAL' e,0 f,1 g,0 h,0 i,'number' j UNION ALL
  SELECT -1,'UnloadingProgressDetail','loaded_back_quantity','Loaded Back Qty','DECIMAL',0,1,0,0,'number'
) x
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = x.b)
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);
