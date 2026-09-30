-- Packing Process — checkweigher step, second stage after the barcode scan
-- (V345 / m5.pack-scan). An operator scans the barcode first; once ACCEPTED,
-- the box is weighed and m5.pack-weigh (M5PackWeighActionHandler) validates
-- the reading against SkuMaster.carton_weight_kg (+-10% tolerance, same
-- convention as com.aria.integration.conveyor.ConveyorAgentController's PLC
-- path) before the pack is treated as complete.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE m5_packing_scan_log ADD COLUMN IF NOT EXISTS weight_kg DECIMAL(8,3) DEFAULT NULL AFTER pieces_total;

UPDATE entity_field_registry SET options_json = '["WRONG_SKU","QTY_EXCEEDED","UNAUTHORIZED_BARCODE","DAMAGED_PACK","SEAL_TAMPER","WEIGHT_OUT_OF_SPEC","OTHER"]'
WHERE tenant_id = -1 AND entity_code = 'PackingScanLog' AND field_code = 'reject_reason'
  AND options_json NOT LIKE '%WEIGHT_OUT_OF_SPEC%';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'PackingScanLog', 'weight_kg', 'Weight (kg)', 'DECIMAL', 1, 1, 0, 0, 'number'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'PackingScanLog' AND field_code = 'weight_kg');

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm5.pack-weigh', 'Weigh Box', 'workflow',
       '{"type":"object","properties":{"scanLogId":{"type":"integer"},"weightKg":{"type":"number"}},"required":["scanLogId","weightKg"]}',
       1, NULL,
       'Checkweigher step of the Packing Process, always after m5.pack-scan: validates the weighed box against SkuMaster.carton_weight_kg (+-10% tolerance). Out-of-spec flips the scan log row to REJECTED/WEIGHT_OUT_OF_SPEC and reverses the actual_packs count m5.pack-scan added.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm5.pack-weigh');
