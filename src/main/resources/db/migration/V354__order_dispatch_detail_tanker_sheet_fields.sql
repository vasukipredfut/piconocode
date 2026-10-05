-- Tanker-flow order sheet carries six extra per-line fields beyond the 35-column
-- OrderApp export: PO Execution, Payment Type, Loading Type, Delivery Date &
-- Time, Remark, Tax. They are read POSITIONALLY from columns 35..40 of the
-- uploaded sheet by OrderDispatchDetailImportController (all optional -- an
-- older 35-column sheet simply leaves them NULL).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE m5_order_dispatch_detail
  ADD COLUMN IF NOT EXISTS po_execution VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS payment_type VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS loading_type VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS delivery_datetime DATETIME NULL,
  ADD COLUMN IF NOT EXISTS remark VARCHAR(500) NULL,
  ADD COLUMN IF NOT EXISTS tax DECIMAL(12,3) NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'OrderDispatchDetail' b,'po_execution' c,'PO Execution' d,'STRING' e,1 f,1 g,1 h,0 i,'text' j UNION ALL
  SELECT -1,'OrderDispatchDetail','payment_type','Payment Type','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'OrderDispatchDetail','loading_type','Loading Type','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'OrderDispatchDetail','delivery_datetime','Delivery Date & Time','DATETIME',1,1,0,0,'datetime' UNION ALL
  SELECT -1,'OrderDispatchDetail','remark','Remark','STRING',0,0,1,0,'textarea' UNION ALL
  SELECT -1,'OrderDispatchDetail','tax','Tax','DECIMAL',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'OrderDispatchDetail' AND field_code = x.c);
