-- aria:qc-final-out: add "QC Sample Test" -- lets the QC officer attach the
-- sample test evidence (photos/lab report) for the loaded tanker before
-- Pass/Reject, same JSON-array-of-files shape as dispatch_vehicle_detail's
-- po_attachment (V246) -- a sample can have more than one supporting file.
-- WorkflowProjectionListener gets a matching branch (same pluckList() +
-- JSON-serialise path as po_attachment/oil_rm_item) so the array persists
-- correctly instead of falling through the generic scalar pluck().

ALTER TABLE qc_final_outbound_detail
  ADD COLUMN IF NOT EXISTS qc_sample_test TEXT DEFAULT NULL COMMENT 'JSON array of attached file URLs/refs';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1,'QcFinalOutboundDetail','qc_sample_test','QC Sample Test','JSON',0,0,0,0,'file'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'QcFinalOutboundDetail' AND field_code = 'qc_sample_test');

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.qc_sample_test', JSON_ARRAY('qc_sample_test', 'qcSampleTest'))
WHERE target_table = 'qc_final_outbound_detail'
  AND page_code = 'aria:qc-final-out';

-- Page JSON (aria:qc-final-out body_json) is pushed separately via the
-- Designer API, not here.
