-- V74 — aria:weighment-capture: weighbridge_code no longer a WB-1/WB-2
-- select, always prefilled read-only to "WB-1 (Main Gate, 60T)".

UPDATE page_version
   SET body_json = REPLACE(body_json,
     '{"name": "weighbridge_code", "label": "Weighbridge", "type": "select", "required": true, "options": [{"label": "WB-1 (Main Gate, 60T)", "value": "WB-1"}, {"label": "WB-2 (Dispatch, 40T)", "value": "WB-2"}]}',
     '{"name": "weighbridge_code", "label": "Weighbridge", "type": "text", "required": true, "readOnly": true, "value": "WB-1 (Main Gate, 60T)"}')
 WHERE id = (SELECT id FROM (
     SELECT pv.id FROM page_version pv JOIN page_definition pd ON pd.id = pv.page_id
      WHERE pd.code = 'aria:weighment-capture' AND pv.status = 'PUBLISHED'
   ) t);
   
   -- V74— aria:weighment-capture: show prior captured weights as read-only
-- reference while capturing a later one.
--   weight_seq = SECOND -> also shows w1_prev_display (First Weight, read-only)
--   weight_seq = THIRD  -> also shows w1_prev_display AND w2_prev_display
-- Both prefill from the GatePass row's own w1_weight_kg/w2_weight_kg columns
-- via onChangeFill when gp_number is picked (values already persisted there
-- by the earlier weighment submissions' workflow projection).

UPDATE page_version
   SET body_json = REPLACE(body_json,
     '{"kind": "FormField", "name": "w1_weight_kg", "label": "First Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == ''FIRST''"}, {"kind": "FormField", "name": "w2_weight_kg", "label": "Second Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == ''SECOND''"}, {"kind": "FormField", "name": "w3_weight_kg", "label": "Third Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == ''THIRD''"}',
     '{"kind": "FormField", "name": "w1_weight_kg", "label": "First Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == ''FIRST''"}, {"kind": "FormField", "name": "w1_prev_display", "label": "First Weight (kg)", "type": "number", "readOnly": true, "visibleWhen": "#form.weight_seq == ''SECOND'' || #form.weight_seq == ''THIRD''"}, {"kind": "FormField", "name": "w2_weight_kg", "label": "Second Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == ''SECOND''"}, {"kind": "FormField", "name": "w2_prev_display", "label": "Second Weight (kg)", "type": "number", "readOnly": true, "visibleWhen": "#form.weight_seq == ''THIRD''"}, {"kind": "FormField", "name": "w3_weight_kg", "label": "Third Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == ''THIRD''"}')
 WHERE id = (SELECT id FROM (
     SELECT pv.id FROM page_version pv JOIN page_definition pd ON pd.id = pv.page_id
      WHERE pd.code = 'aria:weighment-capture' AND pv.status = 'PUBLISHED'
   ) t);

UPDATE page_version
   SET body_json = REPLACE(body_json,
     '"onChangeFill": {"vehicle_no": "vehicle_no", "instanceId": "workflow_instance_id"}}',
     '"onChangeFill": {"vehicle_no": "vehicle_no", "instanceId": "workflow_instance_id", "w1_prev_display": "w1_weight_kg", "w2_prev_display": "w2_weight_kg"}}')
 WHERE id = (SELECT id FROM (
     SELECT pv.id FROM page_version pv JOIN page_definition pd ON pd.id = pv.page_id
      WHERE pd.code = 'aria:weighment-capture' AND pv.status = 'PUBLISHED'
   ) t);
   
   -- V74 — aria:loading-in-progress: route_id field becomes multiselect, and
-- scoped by vehicle_no (same "pageData:" binding-prefix approach as the
-- orderRef filter on aria:loading-allocation, V73). Same caveat applies:
-- filtering only takes effect live if the frontend Renderer sends the
-- current form's vehicle_no under bindings key "pageData:vehicle_no" (or
-- plain "vehicle_no") when querying this field's options.

UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
   SET pv.body_json = REPLACE(pv.body_json,
     '{"name": "route_id", "label": "Route", "type": "select", "optionsSource": {"entity": "OrderDispatchDetail", "valueField": "route_id", "labelField": "route_id"}}',
     '{"name": "route_id", "label": "Route", "type": "multiselect", "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "OrderDispatchDetail", "filter": {"field": "vehicle_no", "op": "eq", "value": "pageData:vehicle_no"}, "select": ["route_id"], "sort": [{"field": "route_id", "dir": "asc"}]}, "valueField": "route_id", "labelField": "route_id"}}')
 WHERE pd.code = 'aria:loading-in-progress' AND pv.status = 'PUBLISHED';
