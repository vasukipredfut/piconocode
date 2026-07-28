-- aria:dispatch-vehicle-details: add a distinct "Oil Type" field for
-- M10.ReturnStockIn, separate from the existing "Oil RM Details" (oilRmItem)
-- field — per explicit request, the spec's "oil type" is its own field, not
-- a duplicate label for oilRmItem.

ALTER TABLE dispatch_vehicle_detail
  ADD COLUMN IF NOT EXISTS oil_type VARCHAR(64) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+1,
       -1, 'DispatchVehicleDetail', 'oil_type', 'Oil Type', 'STRING', 1, 1, 1, 0, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='oil_type');

UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(COALESCE(field_map_json, JSON_OBJECT()),
  JSON_OBJECT('oil_type', JSON_ARRAY('oilType', 'oil_type')))
WHERE target_table = 'dispatch_vehicle_detail' AND workflow_code = 'M10.ReturnStockIn'
  AND JSON_EXTRACT(field_map_json, '$.oil_type') IS NULL;

-- Page field, ReturnStockIn-only, placed alongside the other ReturnStockIn-
-- only fields (invoiceReturnCount/boxWeightKg) right before the Save button.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = REPLACE(pv.body_json,
     '{"kind": "FormField", "type": "number", "name": "boxWeightKg", "label": "Box Weight (kg)", "visibleWhen": "#form.flowVariantCode == \'M10.ReturnStockIn\'"}',
     '{"kind": "FormField", "type": "number", "name": "boxWeightKg", "label": "Box Weight (kg)", "visibleWhen": "#form.flowVariantCode == \'M10.ReturnStockIn\'"}, {"kind": "FormField", "type": "text", "name": "oilType", "label": "Oil Type", "visibleWhen": "#form.flowVariantCode == \'M10.ReturnStockIn\'"}')
 WHERE pd.code = 'aria:dispatch-vehicle-details' AND pv.status = 'PUBLISHED'
   AND pv.body_json NOT LIKE '%"oilType"%';
