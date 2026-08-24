-- aria:qc-preload-approval's Approve/Reject buttons submit {"status":"APPROVED",
-- "qc_status":"APPROVED"} / {"status":"REJECTED","qc_status":"REJECTED"}
-- (V63) to satisfy the transition guard, but qc_preload_detail.qc_decision
-- (added V125) has never had a field_map_json entry -- it was deliberately
-- left out of the generic alias mechanism back then (V125's own comment)
-- pending a dedicated listener that, in practice, ended up writing a
-- different table (qc_unloading_approval, V197/V198) for different
-- workflows (TankerInbound/ReturnStockIn/PackUnload) -- so for TankerOutbound/
-- FGLoading/PackLoading, the decision was never persisted anywhere queryable.
-- Fixed in code (WorkflowProjectionListener's new "qc_decision" branch, using
-- the page-scoped pluck like queue_reason -- "status" is reused by unrelated
-- pages, so the plain cross-page fallback would risk the wrong page's value).

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.qc_decision', JSON_ARRAY('status', 'qc_status'))
WHERE target_table='qc_preload_detail'
  AND page_code='aria:qc-preload-approval'
  AND workflow_code IN ('M10.TankerOutbound','M10.FGLoading','M10.PackLoading');
