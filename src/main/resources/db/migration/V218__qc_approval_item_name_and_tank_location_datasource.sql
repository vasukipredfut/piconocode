-- aria:qc-approval, Tanker Inbound flow:
-- 1. New read-only "Item Name" field, auto-filled from gate_pass.oil_rm_item
--    (already captured at gate-in for M10.TankerInbound, see V15's slip
--    field #3 "Oil / RM") when the QC officer picks the gp_number -- same
--    onChangeFill mechanism item_category/flow_variant_code already use.
-- 2. unloading_tank_location switches from a hardcoded 3-tank list to a
--    datasource-driven picker against the TankerLocation entity (V217,
--    table m10_tanker_location), filtered to that item's assigned tank(s)
--    via product_name = the new item_name field.

-- Add oil_rm_item -> item_name to gp_number's onChangeFill map.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '"onChangeFill":{"item_category":"item_category","flow_variant_code":"flow_variant_code","instanceId":"workflow_instance_id"}',
  '"onChangeFill":{"item_category":"item_category","flow_variant_code":"flow_variant_code","instanceId":"workflow_instance_id","oil_rm_item":"item_name"}'
)
WHERE pd.code = 'aria:qc-approval' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%"onChangeFill":{"item_category":"item_category","flow_variant_code":"flow_variant_code","instanceId":"workflow_instance_id"}%';

-- Insert the new item_name field right after item_category.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '{"label":"Return Stock","value":"RETURN"}]},{"name":"sample_jar_id"',
  '{"label":"Return Stock","value":"RETURN"}]},{"name":"item_name","label":"Item Name (auto-fill from dispatch entry)","type":"text","readOnly":true},{"name":"sample_jar_id"'
)
WHERE pd.code = 'aria:qc-approval' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%{"label":"Return Stock","value":"RETURN"}]},{"name":"sample_jar_id"%';

-- Switch unloading_tank_location to a TankerLocation datasource query filtered by item_name.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '{"name":"unloading_tank_location","label":"Unloading Tank Location","type":"select","visibleWhen":"#form.flow_variant_code == ''M10.TankerInbound''","requiredWhen":"#form.flow_variant_code == ''M10.TankerInbound''","options":[{"label":"Tank 1","value":"TANK-1"},{"label":"Tank 2","value":"TANK-2"},{"label":"Tank 3","value":"TANK-3"}]}',
  '{"name":"unloading_tank_location","label":"Unloading Tank Location","type":"select","visibleWhen":"#form.flow_variant_code == ''M10.TankerInbound''","requiredWhen":"#form.flow_variant_code == ''M10.TankerInbound''","optionsSource":{"type":"datasource","spec":{"type":"entity","entity":"TankerLocation","select":["code","label"],"filter":{"and":[{"field":"active","op":"eq","value":1},{"field":"product_name","op":"eq","value":"#form.item_name"}]},"sort":[{"field":"code","dir":"asc"}]},"valueField":"code","labelField":"label"}}'
)
WHERE pd.code = 'aria:qc-approval' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%{"name":"unloading_tank_location","label":"Unloading Tank Location","type":"select","visibleWhen":"#form.flow_variant_code == ''M10.TankerInbound''","requiredWhen":"#form.flow_variant_code == ''M10.TankerInbound''","options":[{"label":"Tank 1","value":"TANK-1"},{"label":"Tank 2","value":"TANK-2"},{"label":"Tank 3","value":"TANK-3"}]}%';
