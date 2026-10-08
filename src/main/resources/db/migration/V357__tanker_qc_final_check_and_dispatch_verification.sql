-- M10.TankerOutbound's two post-billing pages built in the page designer:
--   aria:qc-officer-final-check        (BILLING_DONE -> QC_FINAL_CHECKED):  qc_final_files (multi-file), qc_final_remarks, status
--   aria:dispatch-officer-verification (QC_FINAL_CHECKED -> exit weighment): dispatch_verify_remarks, status
-- Their entries only ever reached workflow_instance.submitted_data_json. Each page gets its own
-- one-row-per-instance satellite table (same shape as qc_final_outbound_detail, V238), filled by the
-- generic WorkflowProjectionListener via a page_code-scoped m0_workflow_projection row.
--
-- qc_final_files holds a JSON array of uploaded-file objects ({url,doc_no,sha256,size_kb}) -- see the
-- billing_documents branch in WorkflowProjectionListener. It is exposed on a plain GatePass read
-- (GatePassSatelliteJoin) so the Dispatch Officer page's "QC Officer Uploaded Documents" list can show it.
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS qc_officer_final_check_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  gp_number VARCHAR(32) DEFAULT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  qc_final_files TEXT DEFAULT NULL COMMENT 'JSON array of uploaded files [{url,doc_no,sha256,size_kb}]',
  qc_final_remarks TEXT DEFAULT NULL,
  status VARCHAR(16) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_qofc_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS dispatch_officer_verification_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  gp_number VARCHAR(32) DEFAULT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  dispatch_verify_remarks TEXT DEFAULT NULL,
  status VARCHAR(16) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_dov_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'QcOfficerFinalCheckDetail', 'QC Officer Final Check', 'qc_officer_final_check_detail',
       'QC officer''s final check on the billed tanker (documents + remarks), captured on aria:qc-officer-final-check.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'QcOfficerFinalCheckDetail');

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'DispatchOfficerVerificationDetail', 'Dispatch Officer Verification', 'dispatch_officer_verification_detail',
       'Dispatch officer''s final verification remarks before exit weighment, captured on aria:dispatch-officer-verification.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'DispatchOfficerVerificationDetail');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'QcOfficerFinalCheckDetail' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'QcOfficerFinalCheckDetail','gp_number','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcOfficerFinalCheckDetail','vehicle_no','Vehicle No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcOfficerFinalCheckDetail','qc_final_files','Final Check Photos / Documents','JSON',0,0,0,0,'file' UNION ALL
  SELECT -1,'QcOfficerFinalCheckDetail','qc_final_remarks','Remarks','STRING',0,0,1,0,'textarea' UNION ALL
  SELECT -1,'QcOfficerFinalCheckDetail','status','Decision','STRING',1,1,0,0,'text' UNION ALL
  SELECT -1,'DispatchOfficerVerificationDetail','workflow_instance_id','Workflow Instance','LONG',1,1,0,1,'number' UNION ALL
  SELECT -1,'DispatchOfficerVerificationDetail','gp_number','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchOfficerVerificationDetail','vehicle_no','Vehicle No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DispatchOfficerVerificationDetail','dispatch_verify_remarks','Remarks','STRING',0,0,1,0,'textarea' UNION ALL
  SELECT -1,'DispatchOfficerVerificationDetail','status','Decision','STRING',1,1,0,0,'text'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, page_code, field_map_json)
SELECT -1, 'M10.TankerOutbound', 'qc_officer_final_check_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  'aria:qc-officer-final-check',
  '{"gp_number": ["gpNumber", "gp_number"], "vehicle_no": ["vehicleNo", "vehicle_no"], "qc_final_files": ["qc_final_files", "qcFinalFiles"], "qc_final_remarks": ["qc_final_remarks", "qcFinalRemarks"], "status": ["status"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.TankerOutbound' AND target_table = 'qc_officer_final_check_detail');

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, page_code, field_map_json)
SELECT -1, 'M10.TankerOutbound', 'dispatch_officer_verification_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  'aria:dispatch-officer-verification',
  '{"gp_number": ["gpNumber", "gp_number"], "vehicle_no": ["vehicleNo", "vehicle_no"], "dispatch_verify_remarks": ["dispatch_verify_remarks", "dispatchVerifyRemarks"], "status": ["status"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.TankerOutbound' AND target_table = 'dispatch_officer_verification_detail');
