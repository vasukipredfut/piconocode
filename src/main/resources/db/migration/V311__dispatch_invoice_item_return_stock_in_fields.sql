-- M10.ReturnStockIn's "Invoice Details (Return Stock In)" grid on
-- aria:dispatch-vehicle-details gets a richer set of columns per business
-- requirement: Billing No / Billing Date / RD Code / Party Name / SKU Name /
-- SKU Code / Reason / Others Reason / Box / PC / Total PC / Billing Rate /
-- Value / Stock Received Rate / Remarks.
--
-- Confirmed simplification: "Billing No" = the EXISTING invoice_no column
-- (no new billing_no column, no new Billing master table) and "Party Name" =
-- the EXISTING vendor_name column (relabeled in the UI only) -- both already
-- exist on dispatch_invoice_item since V245 and already round-trip correctly
-- via ReturnStockOfficerItemTransitionListener's sibling,
-- DispatchInvoiceItemTransitionListener (str(line,"invoice_no","invoiceNo"),
-- str(line,"vendor_name","vendorName")). Only the genuinely new fields below
-- need columns. sku_name/sku_code both already exist too (V245) -- the UI
-- change (SKU Name as the primary dropdown, SKU Code auto-filled from it) is
-- a page-JSON/interaction change only, no new column.
--
-- This table is SHARED with M10.TankerInbound's own "invoice_items" grid
-- (same listener, same table, disambiguated by which JSON key was
-- submitted) -- all new columns are nullable so TankerInbound rows are
-- entirely unaffected (they simply never populate them).

ALTER TABLE dispatch_invoice_item
  ADD COLUMN IF NOT EXISTS billing_date DATE DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reason VARCHAR(32) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS others_reason VARCHAR(255) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS box DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pc DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS total_pc DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS billing_rate DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `value` DECIMAL(14,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS stock_received_rate DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS remarks VARCHAR(500) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'DispatchInvoiceItem' b,'billing_date' c,'Billing Date' d,'DATE' e,1 f,1 g,0 h,0 i,'date' j UNION ALL
  SELECT -1,'DispatchInvoiceItem','reason','Reason','STRING',1,1,0,0,'select' UNION ALL
  SELECT -1,'DispatchInvoiceItem','others_reason','Others Reason','STRING',0,0,1,0,'text' UNION ALL
  SELECT -1,'DispatchInvoiceItem','box','Box','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'DispatchInvoiceItem','pc','PC','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'DispatchInvoiceItem','total_pc','Total PC','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'DispatchInvoiceItem','billing_rate','Billing Rate','DECIMAL',0,1,0,0,'currency' UNION ALL
  SELECT -1,'DispatchInvoiceItem','value','Value','DECIMAL',0,1,0,0,'currency' UNION ALL
  SELECT -1,'DispatchInvoiceItem','stock_received_rate','Stock Received Rate','DECIMAL',0,1,0,0,'currency' UNION ALL
  SELECT -1,'DispatchInvoiceItem','remarks','Remarks','STRING',0,0,1,0,'textarea'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'DispatchInvoiceItem' AND field_code = x.c);
