-- Billing page (aria:billing-entry) gets a multi-file document upload. The page
-- submits an array of uploaded-file references ({url, doc_no, sha256, size_kb}),
-- stored as a JSON array in billing_entry.billing_documents by
-- WorkflowProjectionListener (billing_entry branch), same array-of-files idea as
-- dispatch_vehicle_detail.po_attachment (V246) but keeping real JSON objects.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE billing_entry
  ADD COLUMN IF NOT EXISTS billing_documents TEXT DEFAULT NULL COMMENT 'JSON array of uploaded files [{url,doc_no,sha256,size_kb}]';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'BillingEntry', 'billing_documents', 'Billing Documents', 'JSON', 0, 0, 0, 0, 'file'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'BillingEntry' AND field_code = 'billing_documents');

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.billing_documents', JSON_ARRAY('billing_documents', 'billingDocuments'))
WHERE target_table = 'billing_entry'
  AND field_map_json IS NOT NULL
  AND JSON_EXTRACT(field_map_json, '$.billing_documents') IS NULL;
