-- aria:qc-approval (M10.TankerInbound) Sample Oil Test: the QC marks the sample Approved or Rebate and, for a Rebate, enters the
-- Value next to it (the page's sampleDecision widget). Both are saved on the sample vehicle's own row (qc_sample_vehicle, V373) by
-- QcSampleVehicleActionHandler (m10.qc-sample-vehicle): sample_decision (APPROVED / REBATE) and rebate_value (only while REBATE;
-- text, the page sends it from a text box -- same as unloading_progress_detail_bay.rebate_value, V377).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS qc_sample_vehicle ADD COLUMN IF NOT EXISTS sample_decision VARCHAR(16) NULL COMMENT 'APPROVED / REBATE';
ALTER TABLE IF EXISTS qc_sample_vehicle ADD COLUMN IF NOT EXISTS rebate_value VARCHAR(64) NULL COMMENT 'Value entered for a REBATE';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'QcSampleVehicle' b,'sample_decision' c,'Approve / Rebate' d,'STRING' e,1 f,1 g,0 h,0 i,'text' j UNION ALL
  SELECT -1,'QcSampleVehicle','rebate_value','Rebate Value','STRING',0,1,1,0,'text'
) x
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = x.b)
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);
