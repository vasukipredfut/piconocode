-- V88 — PMT Weight Difference (aria:pmt-weight-diff-check): give its own
-- operator input (variance_reason) a dedicated table.
--
-- Every other field on this page (vehicle_no, reference_no, w1/w2_weight_kg,
-- ordered_qty, expected_weight_kg, loaded_weight_kg, pmt_variance_pct,
-- withinTolerance) is readOnly — a display mirror of gate_pass /
-- DispatchToleranceListener's computed verdict, not something this page
-- captures. variance_reason is the only true operator input here.
--
-- variance_reason is NOT unique to this page though — manager-qty-approval,
-- pmt-camera-verification, weight-diff-entry, reconciliation and
-- manager-weight-approval all reuse the exact same field name for their own
-- step's reason capture, all writing into the same gate_pass.variance_reason
-- column (whichever submits last wins — a pre-existing, unrelated
-- limitation). gate_pass's own shared column and every other page that
-- writes it are left completely untouched here.
--
-- No m0_workflow_projection row for this table: WorkflowProjectionListener
-- applies a target_table's field_map to EVERY transition of a workflow_code,
-- not just the page that "owns" the table — a generic 'variance_reason'
-- mapping here would also capture manager-qty-approval's, pmt-camera-
-- verification's etc. reasons (submitted at unrelated states) into this
-- table. PmtSharedReasonListener.java (new) captures it correctly instead,
-- gated on previousState=='PARKING_IN_2' so only this page's own submission
-- lands here.
--
-- Only M10.FGLoading and M10.PackLoading use aria:pmt-weight-diff-check
-- (M10.TankerOutbound's equivalent is aria:weight-diff-entry).

-- 1. New table.
CREATE TABLE IF NOT EXISTS pmt_weight_diff_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  pmt_variance_reason VARCHAR(512) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pwd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register the entity.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'PmtWeightDiffDetail', 'PMT Weight Diff Detail', 'pmt_weight_diff_detail',
       'PMT weight-difference variance reason per gate pass (aria:pmt-weight-diff-check).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='PmtWeightDiffDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'PmtWeightDiffDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING',1,1,0,'text'
      UNION ALL SELECT 'pmt_variance_reason','Variance Reason','STRING',0,0,1,'textarea') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='PmtWeightDiffDetail' AND field_code=c.fc);
