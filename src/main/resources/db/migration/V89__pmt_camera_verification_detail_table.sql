-- V89 — PMT Camera Verification (aria:pmt-camera-verification): dedicated
-- table for everything this page captures.
--
-- camera_photo and issue_identified were NEVER mapped anywhere before this
-- (confirmed: no m0_workflow_projection field_map_json references either
-- name, no gate_pass column of either name) — moving them here is a pure
-- addition, nothing to preserve/break.
--
-- variance_reason is the same shared field name used by manager-qty-approval,
-- pmt-weight-diff-check, weight-diff-entry, reconciliation and
-- manager-weight-approval, all writing gate_pass.variance_reason (whichever
-- submits last wins — pre-existing, unrelated to this change). It is
-- captured here too, under a distinctly-named column
-- (camera_variance_reason) — but NOT via the generic field_map below.
-- WorkflowProjectionListener applies a target_table's field_map to EVERY
-- transition of a workflow_code, not just the page that "owns" the table —
-- mapping 'variance_reason' here would also pull in manager-qty-approval's,
-- pmt-weight-diff-check's etc. reasons from unrelated states.
-- PmtSharedReasonListener.java (new) captures it correctly instead, gated on
-- previousState=='PMT_CAMERA_VERIFICATION' so only this page's own
-- submission lands here. camera_photo/issue_identified stay on the generic
-- path below since those two names aren't shared with any other page.
--
-- Only M10.FGLoading and M10.PackLoading use aria:pmt-camera-verification.

-- 1. New table.
CREATE TABLE IF NOT EXISTS pmt_camera_verification_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  camera_photo VARCHAR(512) DEFAULT NULL,
  issue_identified VARCHAR(16) DEFAULT NULL,
  camera_variance_reason VARCHAR(512) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pcvd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register the entity.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'PmtCameraVerificationDetail', 'PMT Camera Verification Detail', 'pmt_camera_verification_detail',
       'Camera evidence + issue-found decision per gate pass (aria:pmt-camera-verification).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='PmtCameraVerificationDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'PmtCameraVerificationDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING',1,1,0,'text'
      UNION ALL SELECT 'camera_photo','Camera Photo','STRING',0,0,0,'text'
      UNION ALL SELECT 'issue_identified','Issue Identified','STRING',1,1,0,'text'
      UNION ALL SELECT 'camera_variance_reason','Variance Reason','STRING',0,0,1,'textarea') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PmtCameraVerificationDetail' AND field_code=c.fc);

-- 3. Project this page's fields into the new table.
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, terminal_states, terminal_ts_column, field_map_json, tolerance_pct)
SELECT -1, w.code, 'pmt_camera_verification_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', NULL, NULL, NULL, NULL,
       '{"camera_photo": ["camera_photo", "cameraPhoto"], "issue_identified": ["issue_identified", "issueIdentified"]}',
       NULL
FROM (SELECT 'M10.FGLoading' code UNION ALL SELECT 'M10.PackLoading') w
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=-1 AND workflow_code=w.code AND target_table='pmt_camera_verification_detail');
