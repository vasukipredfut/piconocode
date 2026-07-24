-- V90 — Manager Qty Approval (aria:manager-qty-approval): dedicated table.
--
-- Two fields captured, both via PmtSharedReasonListener.java (NOT the
-- generic WorkflowProjectionListener field_map — see below):
--   manager_qty_reason  <- "variance_reason" (shared name with
--                          pmt-weight-diff-check, pmt-camera-verification,
--                          weight-diff-entry, reconciliation and
--                          manager-weight-approval, all writing
--                          gate_pass.variance_reason — untouched by this).
--   manager_decision    <- "status" (this page's Approve/Reject
--                          submitValues — ALSO shared: aria:qc-preload-
--                          approval, aria:qc-final-out and
--                          aria:manager-weight-approval submit "status"
--                          too, inside these same 3 workflow codes).
--
-- No m0_workflow_projection row for either: WorkflowProjectionListener
-- applies a target_table's field_map to EVERY transition of a
-- workflow_code, not just the page that "owns" the table — a generic
-- mapping for either shared key would let those OTHER pages' values
-- (submitted at unrelated states) land here too. PmtSharedReasonListener is
-- gated on previousState IN ('PMT_CHECK_PENDING_MANAGER',
-- 'WEIGHT_DIFF_PENDING_MANAGER') instead, so only aria:manager-qty-approval's
-- own submission is ever captured into this table.
--
-- Used by all 3: M10.FGLoading, M10.PackLoading, M10.TankerOutbound.

-- 1. New table.
CREATE TABLE IF NOT EXISTS manager_qty_approval_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  manager_decision VARCHAR(16) DEFAULT NULL,
  manager_qty_reason VARCHAR(512) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_mqad_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 2. Register the entity.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'ManagerQtyApprovalDetail', 'Manager Qty Approval Detail', 'manager_qty_approval_detail',
       'Manager approve/reject decision + reason for qty-variance gate passes (aria:manager-qty-approval).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='ManagerQtyApprovalDetail');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'ManagerQtyApprovalDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'flow_variant_code' fc,'Flow Variant' lbl,'STRING' dt,1 filt,1 sort,1 srch,'text' wk
      UNION ALL SELECT 'current_step','Current Step','STRING',1,1,0,'text'
      UNION ALL SELECT 'manager_decision','Decision','STRING',1,1,0,'text'
      UNION ALL SELECT 'manager_qty_reason','Manager Reason','STRING',0,0,1,'textarea') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ManagerQtyApprovalDetail' AND field_code=c.fc);
