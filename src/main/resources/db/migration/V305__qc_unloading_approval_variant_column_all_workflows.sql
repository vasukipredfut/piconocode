-- Extends the V283/V284 fix to every OTHER workflow that projects into
-- qc_unloading_approval. Checked live: only M10.PackUnload (V283) and
-- M10.FGUnloading (V284) ever got variant_column set on their
-- m0_workflow_projection row -- M10.FGLoading, M10.PackLoading,
-- M10.TankerInbound, M10.ReturnStockIn and M10.TankerOutbound were all still
-- NULL, so WorkflowProjectionListener's first-INSERT-per-instance stamp (see
-- project()) never fires for them and flow_variant_code stays permanently
-- NULL on the QC Approval page's own table for those five workflows'
-- submissions (confirmed live: every M10.TankerInbound/M10.ReturnStockIn row
-- in qc_unloading_approval today has flow_variant_code = NULL).
--
-- Going-forward fix only, by request -- historical rows that are already
-- NULL are intentionally left as-is (no backfill here). Already applied
-- directly against the live DB; this migration just brings that same
-- config change into the tracked ledger so a fresh environment gets it too.
UPDATE m0_workflow_projection
SET variant_column = 'flow_variant_code'
WHERE tenant_id = -1 AND target_table = 'qc_unloading_approval'
  AND workflow_code IN ('M10.FGLoading', 'M10.PackLoading', 'M10.TankerInbound',
                         'M10.ReturnStockIn', 'M10.TankerOutbound')
  AND variant_column IS NULL;
