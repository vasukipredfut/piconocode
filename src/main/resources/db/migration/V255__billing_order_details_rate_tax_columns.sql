-- billing_order_details (V192/V253/V254) only ever captured the "Loaded SKU
-- Details" grid (order_qty/loading_qty/skipped_quantity/reason). The
-- aria:billing-entry page also renders a read-only "Billing Table" section
-- (rate, GST tax, net rate, billed qty per order/SKU line, sourced from
-- OrderDispatchDetail + SkuMaster + LoadingProgressDetailBay) that was never
-- persisted anywhere -- it only ever existed as a client-side computed grid.
--
-- Adding columns here so BillingOrderDetailTransitionListener can persist
-- that same data (recomputed server-side from the authoritative tables,
-- not trusted from the client) into the same per-(instance, order, sku) row
-- the skip-allocation write already uses.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE billing_order_details
  ADD COLUMN IF NOT EXISTS order_rate DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS gst_rate_pct DECIMAL(4,2) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS tax_amount DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS net_rate DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS billed_qty DECIMAL(12,3) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'BillingOrderDetail' b,'order_rate' c,'Order Rate' d,'DECIMAL' e,0 f,1 g,0 h,0 i,'number' j UNION ALL
  SELECT -1,'BillingOrderDetail','gst_rate_pct','GST %','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'BillingOrderDetail','tax_amount','Tax Amount','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'BillingOrderDetail','net_rate','Net Rate','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'BillingOrderDetail','billed_qty','Billed Quantity (Boxes)','DECIMAL',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'BillingOrderDetail' AND field_code = x.c);
