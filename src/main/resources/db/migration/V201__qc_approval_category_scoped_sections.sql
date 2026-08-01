-- aria:qc-approval's "Oil Parameters" / "Packing-Material Parameters" /
-- "FG / Return Inspection" sections have only ever had subtitle TEXT saying
-- "only if item category = ..." / "Skip if not ..." -- no actual visibleWhen
-- condition has ever gated them (confirmed against every migration that's
-- touched this page: V2, V10, V26, V58, V125, V137). Every QC officer sees
-- all 3 sections and all their fields regardless of the item_category they
-- picked, and (being plain optional fields with no required flag) submitting
-- none of them still passes -- so the page silently accepted an oil-category
-- receipt with zero oil readings recorded, or a packing receipt with the
-- FG/Return checklist visible instead. Reported live against the PackUnload
-- QC step (packing material): Oil Parameters showed up needing no input,
-- Packing-Material Parameters was correctly relevant but not required, and
-- FG/Return Inspection had no reason to be there at all.
--
-- Adding real per-field visibleWhen/requiredWhen gating, matching the exact
-- category grouping each section's own title already documented:
--   Oil Parameters               -> RM_OIL or FG
--   Packing-Material Parameters  -> PACK_CARTON / PACK_BOTTLE / PACK_SMALL
--   FG / Return Inspection       -> FG or RETURN
-- RM_OTHER has no section of its own (matches the page's pre-existing
-- design -- only oil/packing/FG-return were ever documented as scoped).
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '[{"name": "density_kgl", "label": "Density (kg/L) — spec 0.910–0.930 for groundnut", "type": "number"}, {"name": "ffa_pct", "label": "Free Fatty Acid (%) — spec ≤ 0.20", "type": "number"}, {"name": "colour_rating", "label": "Colour vs reference", "type": "select", "options": [{"label": "Golden / Pale yellow — Pass", "value": "PASS_GOLDEN"}, {"label": "Light orange — Pass", "value": "PASS_ORANGE"}, {"label": "Dark brown — Fail", "value": "FAIL_DARK"}, {"label": "Cloudy — Fail", "value": "FAIL_CLOUDY"}]}, {"name": "temperature_c", "label": "Temperature (°C) — spec ≤ 35", "type": "number"}]',
  '[{"name": "density_kgl", "label": "Density (kg/L) — spec 0.910–0.930 for groundnut", "type": "number", "visibleWhen": "#form.item_category == ''RM_OIL'' || #form.item_category == ''FG''", "requiredWhen": "#form.item_category == ''RM_OIL'' || #form.item_category == ''FG''"}, {"name": "ffa_pct", "label": "Free Fatty Acid (%) — spec ≤ 0.20", "type": "number", "visibleWhen": "#form.item_category == ''RM_OIL'' || #form.item_category == ''FG''", "requiredWhen": "#form.item_category == ''RM_OIL'' || #form.item_category == ''FG''"}, {"name": "colour_rating", "label": "Colour vs reference", "type": "select", "options": [{"label": "Golden / Pale yellow — Pass", "value": "PASS_GOLDEN"}, {"label": "Light orange — Pass", "value": "PASS_ORANGE"}, {"label": "Dark brown — Fail", "value": "FAIL_DARK"}, {"label": "Cloudy — Fail", "value": "FAIL_CLOUDY"}], "visibleWhen": "#form.item_category == ''RM_OIL'' || #form.item_category == ''FG''", "requiredWhen": "#form.item_category == ''RM_OIL'' || #form.item_category == ''FG''"}, {"name": "temperature_c", "label": "Temperature (°C) — spec ≤ 35", "type": "number", "visibleWhen": "#form.item_category == ''RM_OIL'' || #form.item_category == ''FG''", "requiredWhen": "#form.item_category == ''RM_OIL'' || #form.item_category == ''FG''"}]'
)
WHERE pd.code = 'aria:qc-approval' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%[{"name": "density_kgl", "label": "Density (kg/L) — spec 0.910–0.930 for groundnut", "type": "number"}, {"name": "ffa_pct", "label": "Free Fatty Acid (%) — spec ≤ 0.20", "type": "number"}, {"name": "colour_rating", "label": "Colour vs reference", "type": "select", "options": [{"label": "Golden / Pale yellow — Pass", "value": "PASS_GOLDEN"}, {"label": "Light orange — Pass", "value": "PASS_ORANGE"}, {"label": "Dark brown — Fail", "value": "FAIL_DARK"}, {"label": "Cloudy — Fail", "value": "FAIL_CLOUDY"}]}, {"name": "temperature_c", "label": "Temperature (°C) — spec ≤ 35", "type": "number"}]%';

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '[{"name": "print_quality", "label": "Print quality (labels / cartons)", "type": "select", "options": ["PASS", "PASS_MINOR", "FAIL_PRINT"]}, {"name": "dimensional_check", "label": "Dimensions match spec? (±2 mm)", "type": "select", "options": ["PASS", "MARGINAL", "FAIL"]}, {"name": "bottle_integrity", "label": "Bottle integrity — burst pressure / cap fit", "type": "select", "options": ["PASS", "PARTIAL", "FAIL_CRACK", "FAIL_CAP_FIT"]}, {"name": "carton_strength", "label": "Carton strength — drop test", "type": "select", "options": ["PASS", "MARGINAL", "FAIL"]}]',
  '[{"name": "print_quality", "label": "Print quality (labels / cartons)", "type": "select", "options": ["PASS", "PASS_MINOR", "FAIL_PRINT"], "visibleWhen": "#form.item_category == ''PACK_CARTON'' || #form.item_category == ''PACK_BOTTLE'' || #form.item_category == ''PACK_SMALL''", "requiredWhen": "#form.item_category == ''PACK_CARTON'' || #form.item_category == ''PACK_BOTTLE'' || #form.item_category == ''PACK_SMALL''"}, {"name": "dimensional_check", "label": "Dimensions match spec? (±2 mm)", "type": "select", "options": ["PASS", "MARGINAL", "FAIL"], "visibleWhen": "#form.item_category == ''PACK_CARTON'' || #form.item_category == ''PACK_BOTTLE'' || #form.item_category == ''PACK_SMALL''", "requiredWhen": "#form.item_category == ''PACK_CARTON'' || #form.item_category == ''PACK_BOTTLE'' || #form.item_category == ''PACK_SMALL''"}, {"name": "bottle_integrity", "label": "Bottle integrity — burst pressure / cap fit", "type": "select", "options": ["PASS", "PARTIAL", "FAIL_CRACK", "FAIL_CAP_FIT"], "visibleWhen": "#form.item_category == ''PACK_CARTON'' || #form.item_category == ''PACK_BOTTLE'' || #form.item_category == ''PACK_SMALL''", "requiredWhen": "#form.item_category == ''PACK_CARTON'' || #form.item_category == ''PACK_BOTTLE'' || #form.item_category == ''PACK_SMALL''"}, {"name": "carton_strength", "label": "Carton strength — drop test", "type": "select", "options": ["PASS", "MARGINAL", "FAIL"], "visibleWhen": "#form.item_category == ''PACK_CARTON'' || #form.item_category == ''PACK_BOTTLE'' || #form.item_category == ''PACK_SMALL''", "requiredWhen": "#form.item_category == ''PACK_CARTON'' || #form.item_category == ''PACK_BOTTLE'' || #form.item_category == ''PACK_SMALL''"}]'
)
WHERE pd.code = 'aria:qc-approval' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%[{"name": "print_quality", "label": "Print quality (labels / cartons)", "type": "select", "options": ["PASS", "PASS_MINOR", "FAIL_PRINT"]}, {"name": "dimensional_check", "label": "Dimensions match spec? (±2 mm)", "type": "select", "options": ["PASS", "MARGINAL", "FAIL"]}, {"name": "bottle_integrity", "label": "Bottle integrity — burst pressure / cap fit", "type": "select", "options": ["PASS", "PARTIAL", "FAIL_CRACK", "FAIL_CAP_FIT"]}, {"name": "carton_strength", "label": "Carton strength — drop test", "type": "select", "options": ["PASS", "MARGINAL", "FAIL"]}]%';

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '[{"name": "seal_intact", "label": "Seal intact?", "type": "select", "options": ["YES", "BROKEN", "MISSING"]}, {"name": "label_match", "label": "Label matches SKU master?", "type": "select", "options": ["YES", "BATCH_MISMATCH", "EXPIRY_MISMATCH", "MRP_MISMATCH"]}, {"name": "batch_expiry_check", "label": "Within shelf life?", "type": "select", "options": ["YES", "NEAR_EXPIRY", "EXPIRED"]}]',
  '[{"name": "seal_intact", "label": "Seal intact?", "type": "select", "options": ["YES", "BROKEN", "MISSING"], "visibleWhen": "#form.item_category == ''FG'' || #form.item_category == ''RETURN''", "requiredWhen": "#form.item_category == ''FG'' || #form.item_category == ''RETURN''"}, {"name": "label_match", "label": "Label matches SKU master?", "type": "select", "options": ["YES", "BATCH_MISMATCH", "EXPIRY_MISMATCH", "MRP_MISMATCH"], "visibleWhen": "#form.item_category == ''FG'' || #form.item_category == ''RETURN''", "requiredWhen": "#form.item_category == ''FG'' || #form.item_category == ''RETURN''"}, {"name": "batch_expiry_check", "label": "Within shelf life?", "type": "select", "options": ["YES", "NEAR_EXPIRY", "EXPIRED"], "visibleWhen": "#form.item_category == ''FG'' || #form.item_category == ''RETURN''", "requiredWhen": "#form.item_category == ''FG'' || #form.item_category == ''RETURN''"}]'
)
WHERE pd.code = 'aria:qc-approval' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%[{"name": "seal_intact", "label": "Seal intact?", "type": "select", "options": ["YES", "BROKEN", "MISSING"]}, {"name": "label_match", "label": "Label matches SKU master?", "type": "select", "options": ["YES", "BATCH_MISMATCH", "EXPIRY_MISMATCH", "MRP_MISMATCH"]}, {"name": "batch_expiry_check", "label": "Within shelf life?", "type": "select", "options": ["YES", "NEAR_EXPIRY", "EXPIRED"]}]%';
