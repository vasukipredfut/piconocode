-- V91 — Manager Weight Approval (aria:manager-weight-approval): dedicated
-- table. Same pattern as V90 (manager_qty_approval_detail) — both fields
-- captured via PmtSharedReasonListener.java, gated on previousState==
-- 'WEIGHMENT_3' (confirmed unique to this page across all 3 workflows —
-- no other transition in M10.FGLoading/PackLoading/TankerOutbound
-- originates from WEIGHMENT_3), NOT the generic WorkflowProjectionListener
-- field_map:
--   weight_decision       <- "status" (Approve/Reject submitValues) — same
--                            shared name as qc-preload-approval,
--                            qc-final-out, manager-qty-approval.
--   weight_variance_reason <- "variance_reason" ("Weight-difference reason")
--                            — same shared name as pmt-weight-diff-check,
--                            pmt-camera-verification, manager-qty-approval,
--                            weight-diff-entry, reconciliation.
-- gate_pass's shared variance_reason column and every other page using
-- either key are untouched.
--
-- Used by all 3: M10.FGLoading, M10.PackLoading, M10.TankerOutbound.

-- 1. New table.
CREATE TABLE IF NOT EXISTS manager_weight_approval_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  weight_decision VARCHAR(16) DEFAULT NULL,
  weight_variance_reason VARCHAR(512) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_mwad_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register the entity.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'ManagerWeightApprovalDetail', 'Manager Weight Approval Detail', 'manager_weight_approval_detail',
       'Manager approve/reject decision + reason for weight-variance gate passes (aria:manager-weight-approval).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='ManagerWeightApprovalDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'ManagerWeightApprovalDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING',1,1,0,'text'
      UNION ALL SELECT 'weight_decision','Decision','STRING',1,1,0,'text'
      UNION ALL SELECT 'weight_variance_reason','Weight Difference Reason','STRING',0,0,1,'textarea') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ManagerWeightApprovalDetail' AND field_code=c.fc);
