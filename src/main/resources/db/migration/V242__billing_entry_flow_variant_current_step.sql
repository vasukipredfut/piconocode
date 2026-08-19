-- billing_entry: add flow_variant_code + current_step, same convention as
-- every other per-instance satellite table (qc_final_outbound_detail,
-- pmt_weight_diff_detail, etc). Wiring these via state_columns/variant_column
-- on the existing m0_workflow_projection rows means WorkflowProjectionListener
-- stamps them on every transition -- no extra Java code needed.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE billing_entry
  ADD COLUMN IF NOT EXISTS flow_variant_code VARCHAR(64) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS current_step VARCHAR(64) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'BillingEntry' b,'flow_variant_code' c,'Flow Variant' d,'STRING' e,1 f,1 g,1 h,0 i,'text' j UNION ALL
  SELECT -1,'BillingEntry','current_step','Current Step','STRING',1,1,0,0,'text'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'BillingEntry' AND field_code = x.c);

UPDATE m0_workflow_projection
SET state_columns = 'current_step', variant_column = 'flow_variant_code'
WHERE target_table = 'billing_entry';
