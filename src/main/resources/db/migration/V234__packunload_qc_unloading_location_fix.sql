-- V234 — aria:qc-approval's "unloading_location" field (label "Unloading
-- Material / FG Stock Location", V137) never lands anywhere for
-- M10.PackUnload specifically.
--
-- Root cause: M10.PackUnload's aria:qc-approval submission is split across
-- two different projection mechanisms writing to two different tables —
-- WorkflowProjectionListener routes it to qc_preload_detail (per
-- m0_workflow_projection id=78, page_code=aria:qc-approval), while
-- QcApprovalDecisionListener (bespoke Java, see that class) separately
-- writes only status/qc_reject_reason into qc_unloading_approval. Neither
-- path's field mapping includes unloading_location for PackUnload:
--   - qc_preload_detail's field_map_json (id=78) only has
--     loading_supervisor/loading_tank_location/queue_reason.
--   - QcApprovalDecisionListener's INSERT/UPDATE is hardcoded to
--     status/qc_reject_reason only, for all three workflows it handles.
-- The other 5 workflows (FGLoading/PackLoading/ReturnStockIn/TankerInbound/
-- TankerOutbound) target qc_unloading_approval instead, whose field_map_json
-- already has unloading_location mapped (V197/V198) — so this is a
-- PackUnload-only gap. The qc_preload_detail.unloading_location column
-- already exists (V137's ALTER TABLE), it was just never mapped.

UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(
  field_map_json,
  JSON_OBJECT('unloading_location', JSON_ARRAY('unloadingLocation', 'unloading_location'))
)
WHERE target_table = 'qc_preload_detail'
  AND workflow_code = 'M10.PackUnload'
  AND page_code = 'aria:qc-approval'
  AND field_map_json IS NOT NULL
  AND JSON_VALID(field_map_json)
  AND JSON_EXTRACT(field_map_json, '$.unloading_location') IS NULL;
