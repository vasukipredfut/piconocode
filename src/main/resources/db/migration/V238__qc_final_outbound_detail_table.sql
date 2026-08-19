-- QC Final Outbound (aria:qc-final-out) submissions -- the QC officer's
-- decision + notes on the loaded tanker's sample before it exits -- today
-- only reach workflow_instance.submitted_data_json (raw, per-instance JSON
-- blob, not a queryable column) plus the resulting FSM state, which already
-- lands on gate_pass.status/current_step via the existing page-code-agnostic
-- gate_pass projection row for M10.TankerOutbound (state_columns
-- 'status,current_step', page_code NULL -- applies to every page in this
-- workflow, unchanged by this migration).
--
-- This adds a dedicated satellite table -- same one-row-per-instance shape
-- as qc_preload_detail/qc_unloading_approval -- so qc_notes and the raw
-- APPROVED/REJECTED decision are queryable/reportable on their own instead
-- of being buried in the JSON blob.
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS qc_final_outbound_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  gp_number VARCHAR(32) DEFAULT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  qc_notes TEXT DEFAULT NULL,
  status VARCHAR(16) DEFAULT NULL COMMENT 'APPROVED/REJECTED -- the QC decision itself',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_qfod_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'QcFinalOutboundDetail', 'QC Final Outbound Detail', 'qc_final_outbound_detail',
       'QC officer''s final sample sign-off before tanker exit -- notes + decision, captured on aria:qc-final-out.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'QcFinalOutboundDetail');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'QcFinalOutboundDetail' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'QcFinalOutboundDetail','gp_number','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcFinalOutboundDetail','vehicle_no','Vehicle No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcFinalOutboundDetail','qc_notes','QC Notes','STRING',0,0,1,0,'textarea' UNION ALL
  SELECT -1,'QcFinalOutboundDetail','status','QC Decision','STRING',1,1,0,1,'text'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'QcFinalOutboundDetail' AND field_code = x.c);

-- Scoped to page_code='aria:qc-final-out' -- both the APPROVED and REJECTED
-- transitions in M10.TankerOutbound already declare this pageCode, so
-- WorkflowProjectionListener resolves it correctly and this row never fires
-- off some other page's submission.
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, page_code, field_map_json)
SELECT -1, 'M10.TankerOutbound', 'qc_final_outbound_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  'aria:qc-final-out',
  '{"gp_number": ["gpNumber", "gp_number"], "vehicle_no": ["vehicleNo", "vehicle_no"], "qc_notes": ["qc_notes", "qcNotes"], "status": ["status"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.TankerOutbound' AND target_table = 'qc_final_outbound_detail'
);
