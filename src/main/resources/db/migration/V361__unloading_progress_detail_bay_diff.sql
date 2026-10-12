-- aria:qc-approval's "Unloading Invoices" grid (M10.TankerInbound) has per-line "Diff" (diff_box) and
-- "Diff Reason" inputs that were only ever kept inside workflow_instance.submitted_data_json.
-- TankerUnloadInvoiceTransitionListener now persists them onto every unloading_progress_detail_bay row it
-- seeds for that invoice/SKU line (one line may span several tank rows; each carries the same diff).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE unloading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS diff_qty DECIMAL(12,3) NULL,
  ADD COLUMN IF NOT EXISTS diff_reason VARCHAR(255) NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1, 'UnloadingProgressDetailBay', c.fc, c.lbl, c.dt, 0, 0, c.srch, 0, c.wk
FROM (SELECT 'diff_qty' fc, 'Diff' lbl, 'DECIMAL' dt, 0 srch, 'number' wk
      UNION ALL SELECT 'diff_reason', 'Diff Reason', 'STRING', 1, 'text') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'UnloadingProgressDetailBay' AND field_code = c.fc);
