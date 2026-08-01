-- Expand qc_unloading_approval (added in V192 for the 3 unloading fields)
-- to hold EVERY field the aria:qc-approval page captures: sample
-- identification, oil parameters, packing-material parameters, FG/return
-- inspection, photo + seal, and the QC decision itself. None of these were
-- projected anywhere before this -- they only ever survived in
-- workflow_instance.submitted_data_json, so the QC decision (APPROVED/
-- REJECTED) and every test measurement were unqueryable outside that JSON
-- blob. qc_reject_reason was the one exception, previously split off into
-- qc_preload_detail for 4 of the 6 flows -- moved here so the whole QC
-- Approval submission lands in exactly one table, regardless of which
-- flow_variant_code it was submitted under.

ALTER TABLE qc_unloading_approval
  ADD COLUMN IF NOT EXISTS item_category      VARCHAR(16)  DEFAULT NULL COMMENT 'RM_OIL/RM_OTHER/PACK_CARTON/PACK_BOTTLE/PACK_SMALL/FG/RETURN',
  ADD COLUMN IF NOT EXISTS sample_jar_id       VARCHAR(64)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS qc_officer_id       VARCHAR(128) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS density_kgl         DECIMAL(6,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS ffa_pct             DECIMAL(6,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS colour_rating       VARCHAR(32)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS temperature_c       DECIMAL(5,2) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS print_quality       VARCHAR(32)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS dimensional_check   VARCHAR(32)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS bottle_integrity    VARCHAR(32)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS carton_strength     VARCHAR(32)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS seal_intact         VARCHAR(16)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS label_match         VARCHAR(32)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS batch_expiry_check  VARCHAR(16)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sample_photo        TEXT         DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS seal_unlock_no      VARCHAR(32)  DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS qc_notes            TEXT         DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS status              VARCHAR(16)  DEFAULT NULL COMMENT 'APPROVED/REJECTED -- the QC decision itself',
  ADD COLUMN IF NOT EXISTS qc_reject_reason    TEXT         DEFAULT NULL;

-- Full field map, same shape for every flow this page serves -- fields that
-- don't apply to a given flow_variant (e.g. unloading_* outside TankerInbound/
-- ReturnStockIn) simply arrive absent from formData and stay NULL.
SET @qc_full_map = '{
  "item_category": ["item_category", "itemCategory"],
  "sample_jar_id": ["sample_jar_id", "sampleJarId"],
  "qc_officer_id": ["qc_officer_id", "qcOfficerId"],
  "density_kgl": ["density_kgl", "densityKgl"],
  "ffa_pct": ["ffa_pct", "ffaPct"],
  "colour_rating": ["colour_rating", "colourRating"],
  "temperature_c": ["temperature_c", "temperatureC"],
  "print_quality": ["print_quality", "printQuality"],
  "dimensional_check": ["dimensional_check", "dimensionalCheck"],
  "bottle_integrity": ["bottle_integrity", "bottleIntegrity"],
  "carton_strength": ["carton_strength", "cartonStrength"],
  "seal_intact": ["seal_intact", "sealIntact"],
  "label_match": ["label_match", "labelMatch"],
  "batch_expiry_check": ["batch_expiry_check", "batchExpiryCheck"],
  "sample_photo": ["sample_photo", "samplePhoto"],
  "seal_unlock_no": ["seal_unlock_no", "sealUnlockNo"],
  "unloading_supervisor_id": ["unloadingSupervisorName", "unloading_supervisor_name"],
  "unloading_location": ["unloadingLocation", "unloading_location"],
  "oil_sample_recovered": ["oilSampleRecovered", "oil_sample_recovered"],
  "qc_notes": ["qc_notes", "qcNotes"],
  "status": ["status"],
  "qc_reject_reason": ["qcRejectReason", "qc_reject_reason"]
}';

UPDATE m0_workflow_projection
SET field_map_json = @qc_full_map
WHERE tenant_id = -1 AND target_table = 'qc_unloading_approval'
  AND workflow_code IN ('M10.TankerInbound', 'M10.ReturnStockIn');

INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, field_map_json, terminal_states)
SELECT -1, w.workflow_code, 'qc_unloading_approval', @qc_full_map,
  (SELECT terminal_states FROM m0_workflow_projection p2
    WHERE p2.tenant_id = -1 AND p2.workflow_code = w.workflow_code AND p2.target_table = 'gate_pass' LIMIT 1)
FROM (
  SELECT 'M10.FGLoading' workflow_code UNION ALL
  SELECT 'M10.PackLoading' UNION ALL
  SELECT 'M10.PackUnload' UNION ALL
  SELECT 'M10.TankerOutbound'
) w
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = w.workflow_code AND target_table = 'qc_unloading_approval'
);

-- qc_reject_reason now lands in qc_unloading_approval for every flow --
-- strip it back out of qc_preload_detail so it saves in one place only.
-- loading_supervisor / loading_tank_location stay: those are fed by a
-- different page, not aria:qc-approval.
UPDATE m0_workflow_projection
SET field_map_json = '{"loading_supervisor": ["loadingSupervisor", "loading_supervisor"], "loading_tank_location": ["loadingTankLocation", "loading_tank_location"]}'
WHERE tenant_id = -1 AND target_table = 'qc_preload_detail'
  AND workflow_code IN ('M10.FGLoading', 'M10.PackLoading', 'M10.PackUnload', 'M10.TankerOutbound');
