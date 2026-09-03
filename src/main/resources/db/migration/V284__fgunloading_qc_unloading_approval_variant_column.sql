-- M10.FGUnloading's qc_unloading_approval row (V278) never set variant_column,
-- so flow_variant_code has been left NULL for every FGUnloading QC approval
-- submission so far -- WorkflowProjectionListener only stamps variant_column
-- on its own first INSERT for an instance (see project(), insert-only-columns
-- block), and that listener always creates the row before
-- QcApprovalDecisionListener's own INSERT branch (which would set it) gets a
-- chance to run, since @Order ranks WorkflowProjectionListener first.
--
-- Same fix V283 already applied to M10.PackUnload's freshly-added row; doing
-- it here for the row FGUnloading has had since V278.
UPDATE m0_workflow_projection
SET variant_column = 'flow_variant_code'
WHERE tenant_id = -1 AND workflow_code = 'M10.FGUnloading' AND target_table = 'qc_unloading_approval'
  AND variant_column IS NULL;

-- Config only affects rows created from here on -- backfill flow_variant_code
-- on whatever FGUnloading rows already exist in qc_unloading_approval with it
-- still NULL, so historical submissions aren't left inconsistent with new ones.
UPDATE qc_unloading_approval qua
JOIN workflow_instance wi
  ON wi.id = qua.workflow_instance_id AND wi.tenant_id = qua.tenant_id
SET qua.flow_variant_code = wi.workflow_code
WHERE wi.workflow_code = 'M10.FGUnloading'
  AND (qua.flow_variant_code IS NULL OR qua.flow_variant_code = '');
