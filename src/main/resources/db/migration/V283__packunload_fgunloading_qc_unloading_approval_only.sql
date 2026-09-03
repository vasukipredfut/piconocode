-- M10.PackUnload and M10.FGUnloading are unload flows -- their aria:qc-approval
-- submission should land exclusively in qc_unloading_approval, matching
-- M10.TankerInbound/M10.ReturnStockIn. qc_preload_detail is the pre-load QC
-- step's table (aria:qc-preload-approval, used by the loading flows:
-- PackLoading/FGLoading/TankerOutbound) and should not also be fed by an
-- unload flow's QC approval page.
--
-- M10.PackUnload today (V216) has NO m0_workflow_projection row at all for
-- qc_unloading_approval -- V216 deleted it, reasoning the page never carried
-- the oil-chemistry fields that table's original field_map_json expected.
-- That reasoning missed that the SAME page also carries generic fields
-- (qc_officer_id/seal_unlock_no/qc_notes/queue_reason/time_taken/
-- unloading_location) that legitimately belong in qc_unloading_approval too --
-- those fields were only ever reaching qc_preload_detail partially (V233/
-- V234/V265) or nowhere at all (qc_officer_id/seal_unlock_no/qc_notes had no
-- column anywhere). Re-adding the full field map here (same shape V278 used
-- for FGUnloading) fixes that gap.
--
-- variant_column is set this time (unlike V198/V278's rows) so
-- WorkflowProjectionListener -- which always runs before
-- QcApprovalDecisionListener (see @Order on both classes) -- stamps
-- flow_variant_code on its own INSERT; QcApprovalDecisionListener's own
-- INSERT branch (which used to be the only writer, since no generic row
-- existed for PackUnload) now always finds the row already present and takes
-- its UPDATE branch instead, so this is the only place flow_variant_code gets
-- set going forward for this workflow+table.
INSERT INTO m0_workflow_projection
    (tenant_id, workflow_code, target_table, page_code, key_column, state_columns, variant_column, terminal_states, field_map_json)
SELECT -1, 'M10.PackUnload', 'qc_unloading_approval', 'aria:qc-approval', 'workflow_instance_id', 'current_step', 'flow_variant_code',
    (SELECT terminal_states FROM m0_workflow_projection p2
      WHERE p2.tenant_id = -1 AND p2.workflow_code = 'M10.PackUnload' AND p2.target_table = 'gate_pass' LIMIT 1),
    '{"item_category": ["item_category", "itemCategory"], "sample_jar_id": ["sample_jar_id", "sampleJarId"], "qc_officer_id": ["qc_officer_id", "qcOfficerId"], "density_kgl": ["density_kgl", "densityKgl"], "ffa_pct": ["ffa_pct", "ffaPct"], "colour_rating": ["colour_rating", "colourRating"], "temperature_c": ["temperature_c", "temperatureC"], "print_quality": ["print_quality", "printQuality"], "dimensional_check": ["dimensional_check", "dimensionalCheck"], "bottle_integrity": ["bottle_integrity", "bottleIntegrity"], "carton_strength": ["carton_strength", "cartonStrength"], "seal_intact": ["seal_intact", "sealIntact"], "label_match": ["label_match", "labelMatch"], "batch_expiry_check": ["batch_expiry_check", "batchExpiryCheck"], "sample_photo": ["sample_photo", "samplePhoto"], "seal_unlock_no": ["seal_unlock_no", "sealUnlockNo"], "unloading_supervisor_id": ["unloadingSupervisorName", "unloading_supervisor_name"], "unloading_location": ["unloadingLocation", "unloading_location"], "oil_sample_recovered": ["oilSampleRecovered", "oil_sample_recovered"], "qc_notes": ["qc_notes", "qcNotes"], "qc_reject_reason": ["qcRejectReason", "qc_reject_reason"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (
    SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.PackUnload' AND target_table = 'qc_unloading_approval'
);

-- Stop PackUnload's aria:qc-approval from also feeding qc_preload_detail
-- (its only row there, added by V216 with the loading_supervisor/
-- loading_tank_location/queue_reason/unloading_location/time_taken map --
-- superseded entirely by the row above).
DELETE FROM m0_workflow_projection
  WHERE workflow_code = 'M10.PackUnload' AND target_table = 'qc_preload_detail' AND page_code = 'aria:qc-approval';

-- Same cleanup for M10.FGUnloading: V278 already gave it a full-map
-- qc_unloading_approval row, but also (by cloning PackUnload's pre-fix
-- config verbatim) a redundant qc_preload_detail row carrying
-- queue_reason/unloading_location/time_taken. Remove the duplicate so
-- FGUnloading's QC approval submission lands in exactly one table too.
DELETE FROM m0_workflow_projection
  WHERE workflow_code = 'M10.FGUnloading' AND target_table = 'qc_preload_detail' AND page_code = 'aria:qc-approval';
