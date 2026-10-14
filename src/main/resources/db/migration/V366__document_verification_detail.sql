-- aria:document-verification (M10.TankerInbound: manager approved -> document verification -> Weighment 3).
-- What the officer types on it (GRN number, verification remarks, the "Documents Verified" decision, the
-- skip-priority reason) only ever reached workflow_instance.submitted_data_json. It gets its own one-row-per-instance
-- table, filled by the generic WorkflowProjectionListener via a page_code-scoped m0_workflow_projection row (so it
-- only fires for THIS page and takes the values from THIS page's own submission), plus the page-submitter stamping
-- (V363 / V364): submitted_by_id (login) and performed_by_name.
--
-- The read-only fields on the page (sample_jar_id, sample_photo, the PO attachments, the unloading grid) are shown from
-- other tables and are deliberately not saved here.
--
-- NOTE: the projection only fires for a transition whose workflow-definition entry declares
-- "pageCode":"aria:document-verification" -- make sure M10.TankerInbound's transition for this page carries it.
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS document_verification_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) DEFAULT NULL,
  current_step VARCHAR(64) DEFAULT NULL,
  gp_number VARCHAR(32) DEFAULT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  grn_number VARCHAR(64) DEFAULT NULL,
  doc_verification_remark TEXT DEFAULT NULL,
  documents_verified VARCHAR(8) DEFAULT NULL COMMENT 'YES when the officer sent the vehicle on to Weighment 3',
  queue_reason VARCHAR(255) DEFAULT NULL,
  submitted_by_id BIGINT DEFAULT NULL,
  performed_by_name VARCHAR(128) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_dvd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'DocumentVerificationDetail', 'Document Verification', 'document_verification_detail',
       'GRN number, remarks and the verified decision captured on aria:document-verification (Tanker Inbound, before Weighment 3).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'DocumentVerificationDetail');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'DocumentVerificationDetail' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'DocumentVerificationDetail','gp_number','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DocumentVerificationDetail','vehicle_no','Vehicle No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DocumentVerificationDetail','grn_number','GRN Number','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DocumentVerificationDetail','doc_verification_remark','Document Verification Remarks','STRING',0,0,1,0,'textarea' UNION ALL
  SELECT -1,'DocumentVerificationDetail','documents_verified','Documents Verified','STRING',1,1,0,0,'text' UNION ALL
  SELECT -1,'DocumentVerificationDetail','queue_reason','Reason for Skipping Priority','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'DocumentVerificationDetail','submitted_by_id','Submitted By (user id)','LONG',1,1,0,0,'number' UNION ALL
  SELECT -1,'DocumentVerificationDetail','performed_by_name','Performed By','STRING',1,1,1,0,'text'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, page_code, field_map_json)
SELECT -1, 'M10.TankerInbound', 'document_verification_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  'aria:document-verification',
  '{"gp_number": ["gpNumber", "gp_number"], "vehicle_no": ["vehicleNo", "vehicle_no"], "grn_number": ["grn_number", "grnNumber"], "doc_verification_remark": ["doc_verification_remark", "docVerificationRemark"], "documents_verified": ["documents_verified", "documentsVerified"], "queue_reason": ["queue_reason", "queueReason"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection
                  WHERE tenant_id = -1 AND workflow_code = 'M10.TankerInbound' AND target_table = 'document_verification_detail');

INSERT IGNORE INTO page_submitter_map (page_code, table_name, key_column, user_column, name_column)
VALUES ('aria:document-verification', 'document_verification_detail', 'workflow_instance_id', 'submitted_by_id', 'performed_by_name');
