-- Billing page (aria:billing-entry) has a multi-file "Billing Check List" upload (name="billing_check_list", next to the
-- "Billing Attachments" billing_documents of V356). The page submits an array of uploaded-file references
-- ({url, doc_no, sha256, size_kb}); it is stored as a JSON array in billing_entry.billing_check_list by
-- WorkflowProjectionListener (billing_entry branch, same as billing_documents).
-- Also exposed on a plain GatePass read (GatePassSatelliteJoin.SELECT, be.billing_check_list).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE billing_entry
  ADD COLUMN IF NOT EXISTS billing_check_list TEXT DEFAULT NULL COMMENT 'JSON array of uploaded files [{url,doc_no,sha256,size_kb}]';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'BillingEntry', 'billing_check_list', 'Billing Check List', 'JSON', 0, 0, 0, 0, 'file'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'BillingEntry' AND field_code = 'billing_check_list');

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.billing_check_list', JSON_ARRAY('billing_check_list', 'billingCheckList'))
WHERE target_table = 'billing_entry'
  AND field_map_json IS NOT NULL
  AND JSON_EXTRACT(field_map_json, '$.billing_check_list') IS NULL;
