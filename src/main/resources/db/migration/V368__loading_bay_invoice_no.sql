-- M10.TankerInbound "load again" (aria:qc-approval, loadingInvoices): the vehicle that just unloaded is loaded again against
-- invoice lines, which have an invoice_no but no route_id. TankerUnloadInvoiceTransitionListener writes them to
-- loading_progress_detail_bay (status IN_PROGRESS), so the table needs the invoice_no unloading_progress_detail_bay
-- already has (V258). rd_code / vendor_name already exist on this table (V343).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS loading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS invoice_no VARCHAR(64) NULL AFTER order_id;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'LoadingProgressDetailBay', 'invoice_no', 'Invoice No', 'STRING', 1, 1, 1, 0, 'text'
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'LoadingProgressDetailBay')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'LoadingProgressDetailBay' AND field_code = 'invoice_no');
