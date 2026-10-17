-- aria:document-verification (M10.TankerInbound): new multi-file field "Verification Attachments" (verification_documents,
-- uploaded through /api/m0/document-vault/upload, docCategory DOC_VERIFICATION). The page submits an array of uploaded-file
-- references ({url, doc_no, sha256, size_kb}); like billing_entry.billing_documents (V356) and
-- qc_officer_final_check_detail.qc_final_files (V357) it is stored as a JSON array in document_verification_detail by the
-- generic WorkflowProjectionListener, through this page-scoped m0_workflow_projection row (V366).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS document_verification_detail
  ADD COLUMN IF NOT EXISTS verification_documents TEXT DEFAULT NULL COMMENT 'JSON array of uploaded files [{url,doc_no,sha256,size_kb}]';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'DocumentVerificationDetail', 'verification_documents', 'Verification Attachments', 'JSON', 0, 0, 0, 0, 'file'
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'DocumentVerificationDetail')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'DocumentVerificationDetail' AND field_code = 'verification_documents');

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.verification_documents', JSON_ARRAY('verification_documents', 'verificationDocuments'))
WHERE target_table = 'document_verification_detail'
  AND field_map_json IS NOT NULL
  AND JSON_EXTRACT(field_map_json, '$.verification_documents') IS NULL;
