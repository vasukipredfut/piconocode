-- V160 wired M10.PackUnload's QC step to aria:qc-preload-approval (a
-- pre-LOADING readiness screen used by outbound dispatch flows: loading
-- supervisor + tank/loading location) instead of aria:qc-approval (the
-- item-category-aware receiving QC screen used by M10.TankerInbound /
-- M10.ReturnStockIn, which already has packing-material inspection fields --
-- print quality, dimensions, bottle/carton integrity -- plus Approved/
-- Rejected + reject reason). V160's own states (QC_APPROVED/QC_REJECTED,
-- not QC_PRELOAD_APPROVED/QC_PRELOAD_REJECTED) already followed the
-- TankerInbound/ReturnStockIn convention -- the pageCode just didn't match.
--
-- V160 is already applied (this dev DB's flyway_schema_history confirms
-- it), so the fix has to be a new migration, not an edit to V160 -- same
-- approach V172 used for a prior page-code correction.

-- 1. Route M10.PackUnload's QC step to the correct screen.
UPDATE workflow_definition
SET body_json = REPLACE(body_json, '"pageCode":"aria:qc-preload-approval"', '"pageCode":"aria:qc-approval"')
WHERE code = 'M10.PackUnload' AND status = 'PUBLISHED'
  AND body_json LIKE '%"pageCode":"aria:qc-preload-approval"%';

-- 2. aria:qc-approval's "Unloading Material / FG Stock Location" field is
-- currently gated to item_category == 'RETURN' only (added in V137 for
-- ReturnStockIn). M10.PackUnload needs it too (packing material / FG stock
-- location), regardless of item_category, so add flow_variant_code ==
-- 'M10.PackUnload' as an alternate condition. TankerInbound/ReturnStockIn's
-- existing RETURN-only behavior is unchanged (OR'd, not replaced).
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '"visibleWhen": "#form.item_category == ''RETURN''", "requiredWhen": "#form.item_category == ''RETURN''"',
  '"visibleWhen": "#form.item_category == ''RETURN'' || #form.flow_variant_code == ''M10.PackUnload''", "requiredWhen": "#form.item_category == ''RETURN'' || #form.flow_variant_code == ''M10.PackUnload''"'
)
WHERE pd.code = 'aria:qc-approval' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%"visibleWhen": "#form.item_category == ''RETURN''", "requiredWhen": "#form.item_category == ''RETURN''"%';

-- 3. V160's qc_preload_detail projection row for M10.PackUnload mapped
-- loading_supervisor/loading_tank_location -- fields that only ever existed
-- on aria:qc-preload-approval, never submitted by aria:qc-approval. Drop
-- those two dead keys; qc_reject_reason stays (its submitted field name
-- already matches the column name on both pages, so the mapping is still
-- valid). qc_decision/unloading_location are captured by
-- QcApprovalDecisionListener, not this generic field_map_json -- same as
-- for M10.TankerInbound/M10.ReturnStockIn.
UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json, '$.loading_supervisor', '$.loading_tank_location')
WHERE tenant_id = -1 AND workflow_code = 'M10.PackUnload' AND target_table = 'qc_preload_detail'
  AND JSON_CONTAINS_PATH(field_map_json, 'one', '$.loading_supervisor', '$.loading_tank_location');
