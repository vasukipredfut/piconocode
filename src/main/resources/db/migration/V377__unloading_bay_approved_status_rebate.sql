-- Tanker Inbound, aria:qc-approval "Unloading Invoices": the QC marks every invoice line Approved, Rebate or Reject, and for a Rebate
-- enters the Value (the rate) next to it (the page's rebate_reason field). Neither was saved on the unload rows. They are now written
-- onto every tank row of the line by TankerUnloadInvoiceTransitionListener: approved_status, and rebate_value (only while the line is
-- a Rebate). The value is text because the page sends it from a text box.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS unloading_progress_detail_bay ADD COLUMN IF NOT EXISTS approved_status VARCHAR(16) NULL;
ALTER TABLE IF EXISTS unloading_progress_detail_bay ADD COLUMN IF NOT EXISTS rebate_value VARCHAR(64) NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'UnloadingProgressDetailBay' b,'approved_status' c,'QC Decision' d,'STRING' e,1 f,1 g,0 h,0 i,'text' j UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','rebate_value','Rebate Value','STRING',0,1,1,0,'text'
) x
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = x.b)
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);
