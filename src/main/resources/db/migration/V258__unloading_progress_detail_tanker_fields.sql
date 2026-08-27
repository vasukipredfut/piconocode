-- Merges the M10.TankerInbound unload-invoice-receipt process (aria:qc-approval
-- "Unloading Invoices" -> m10.tanker-unload-receipt) into the SAME
-- unloading_progress_detail / unloading_progress_detail_bay tables (V256)
-- already used by the FG/PackLoading Reject Flow Unload process, instead of
-- a separate table pair -- one "unloading" concept, one place to look,
-- shared status lifecycle (IN_PROGRESS -> DONE), at the cost of some columns
-- being meaningful only for one of the two processes on any given row
-- (route_id/order_id for reject-unload rows; invoice_no/vendor_name/rd_code
-- for tanker-receipt rows). sku_name (tanker) reuses the existing sku_desc
-- column rather than duplicating it.
--
-- No new tank_location column either -- same precedent as
-- M10StartBayActionHandler's M10.TankerOutbound handling (see its class
-- javadoc): where there's no physical bay, the tank code the operator picks
-- goes into bay_name itself, not a separate column. Tanker-receipt rows
-- store their tank location in bay_name the same way.
--
-- unloading_progress_detail (parent) previously had no status of its own --
-- reject-unload instead flips the ORIGINAL loading_progress_detail row to
-- REVERSED once done. Tanker-receipt needs its own IN_PROGRESS/DONE on the
-- unloading side directly, so this adds it generically -- harmless default
-- for existing reject-unload rows too.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE unloading_progress_detail
  ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS' AFTER processed_quantity;

ALTER TABLE unloading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS invoice_no VARCHAR(64) DEFAULT NULL AFTER order_id,
  ADD COLUMN IF NOT EXISTS vendor_name VARCHAR(255) DEFAULT NULL AFTER invoice_no,
  ADD COLUMN IF NOT EXISTS rd_code VARCHAR(32) DEFAULT NULL AFTER vendor_name;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'UnloadingProgressDetail' b,'status' c,'Status' d,'STRING' e,1 f,1 g,0 h,0 i,'text' j UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','invoice_no','Invoice No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','vendor_name','Vendor','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','rd_code','RD Code','STRING',1,1,1,0,'text'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = x.b AND field_code = x.c);

UPDATE action_registry
SET description = 'Closes matching unloading_progress_detail_bay row(s) (invoice_no/sku_code) with the entered weight, posts stock (m5_stock_movement kind RECEIPT into the tank location recorded in bay_name), and rolls up unloading_progress_detail. Used on the weighment-capture step following aria:qc-approval for M10.TankerInbound.'
WHERE code = 'm10.tanker-unload-receipt';
