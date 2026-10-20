-- Tanker Outbound reject flow: a vehicle loaded from several locations may be unloaded from only one of them, or only in
-- part. The loading rows (one per location) kept their loaded quantity and only flipped to REVERSED on a full unload, so a
-- partly unloaded location still read as fully loaded. unloaded_quantity counts what has come back, per loading row, so the
-- screen can show loaded / unloaded / still on the vehicle (processed_quantity - unloaded_quantity). processed_quantity itself
-- is never changed: it stays the audit record of what was loaded. The parent keeps the total over its rows.
-- Maintained by TankerUnloadCaptureActionHandler (m10.tanker-unload-capture).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS loading_progress_detail_bay ADD COLUMN IF NOT EXISTS unloaded_quantity DECIMAL(12,3) NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS loading_progress_detail     ADD COLUMN IF NOT EXISTS unloaded_quantity DECIMAL(12,3) NOT NULL DEFAULT 0;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'LoadingProgressDetailBay' b,'unloaded_quantity' c,'Unloaded Qty' d,'DECIMAL' e,0 f,1 g,0 h,0 i,'number' j UNION ALL
  SELECT -1,'LoadingProgressDetail','unloaded_quantity','Unloaded Qty','DECIMAL',0,1,0,0,'number'
) x
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = x.b)
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- existing reject-unloads: rebuild the figure from the RETURN movements already posted (per instance + origin tank)
UPDATE loading_progress_detail_bay b
JOIN (SELECT ref_id AS instance_id, item_code, from_bin, SUM(qty) AS qty FROM m5_stock_movement
      WHERE kind = 'RETURN' AND ref_type = 'TANKER_UNLOAD' AND from_bin IS NOT NULL GROUP BY ref_id, item_code, from_bin) m
  ON m.instance_id = b.workflow_instance_id AND m.item_code = b.sku_code COLLATE utf8mb4_uca1400_ai_ci AND m.from_bin = b.bay_name COLLATE utf8mb4_uca1400_ai_ci
SET b.unloaded_quantity = LEAST(m.qty, COALESCE(b.processed_quantity, 0))
WHERE b.unloaded_quantity = 0 AND b.status IN ('DONE','UNLOAD_IN_PROGRESS','REVERSED');

UPDATE loading_progress_detail p
SET p.unloaded_quantity = (SELECT COALESCE(SUM(b.unloaded_quantity), 0) FROM loading_progress_detail_bay b
                           WHERE b.tenant_id = p.tenant_id AND b.workflow_instance_id = p.workflow_instance_id)
WHERE p.unloaded_quantity = 0;
