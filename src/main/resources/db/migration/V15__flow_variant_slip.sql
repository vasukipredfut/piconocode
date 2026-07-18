-- V15 — flow-variant-aware gate-pass slip.
--
-- The slip (QrCode primitive) previously printed a FIXED detail set for every
-- gate pass — Vehicle / Driver / Phone / Flow / Status — regardless of whether
-- the pass was a tanker inbound, an FG outbound, or an internal transfer. The
-- driver's "Lorry Details" should reflect the flow: an oil-tanker-in slip wants
-- the RM item + tank + supplier; an FG-out slip wants the invoice + loading bay.
--
-- This is substrate data, not a code fork: slip_field_map holds, per flow
-- variant, the ordered (field,label) lines to print. Product rows live at
-- tenant_id = -1; a tenant can override a variant by inserting its own rows
-- (resolved tenant-first, exactly like page_category). The QrCode primitive
-- reads GET /api/m0/slip-fields/{flow} and renders those lines. It works for
-- any entity with a variant discriminator — invoices, batches — not just gate
-- passes, so it stays a generic capability.

CREATE TABLE IF NOT EXISTS slip_field_map (
  id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id         BIGINT       NOT NULL,
  flow_variant_code VARCHAR(64)  NOT NULL,
  sort_order        INT          NOT NULL DEFAULT 0,
  field             VARCHAR(64)  NOT NULL,
  label             VARCHAR(96)  NOT NULL,
  status            VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_slip_field (tenant_id, flow_variant_code, field)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- Backfill the column onto an already-created table (re-run safety).
ALTER TABLE slip_field_map
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

INSERT INTO slip_field_map (tenant_id, flow_variant_code, sort_order, field, label, status)
VALUES
  (-1,'M10.TankerInbound',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.TankerInbound',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.TankerInbound',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.TankerInbound',3,'oil_rm_item','Oil / RM','ACTIVE'),
  (-1,'M10.TankerInbound',4,'source_party','Supplier','ACTIVE'),
  (-1,'M10.TankerInbound',5,'ordered_qty','Ordered Qty','ACTIVE'),
  (-1,'M10.TankerInbound',6,'tanker_no','Tanker No','ACTIVE'),
  (-1,'M10.TankerInbound',7,'loading_tank_location','Tank','ACTIVE'),
  (-1,'M10.TankerInbound',8,'status','Status','ACTIVE'),
  (-1,'M10.TankerOutbound',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.TankerOutbound',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.TankerOutbound',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.TankerOutbound',3,'oil_rm_item','Oil / RM','ACTIVE'),
  (-1,'M10.TankerOutbound',4,'invoice_no','Invoice','ACTIVE'),
  (-1,'M10.TankerOutbound',5,'invoice_qty','Invoice Qty','ACTIVE'),
  (-1,'M10.TankerOutbound',6,'broker_name','Broker','ACTIVE'),
  (-1,'M10.TankerOutbound',7,'transport_name','Transport','ACTIVE'),
  (-1,'M10.TankerOutbound',8,'tanker_no','Tanker No','ACTIVE'),
  (-1,'M10.TankerOutbound',9,'status','Status','ACTIVE'),
  (-1,'M10.FGLoading',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.FGLoading',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.FGLoading',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.FGLoading',3,'loading_bay','Loading Bay','ACTIVE'),
  (-1,'M10.FGLoading',4,'loading_supervisor','Supervisor','ACTIVE'),
  (-1,'M10.FGLoading',5,'invoice_no','Invoice','ACTIVE'),
  (-1,'M10.FGLoading',6,'ordered_qty','Ordered Qty','ACTIVE'),
  (-1,'M10.FGLoading',7,'transport_name','Transport','ACTIVE'),
  (-1,'M10.FGLoading',8,'status','Status','ACTIVE'),
  (-1,'M10.PackLoading',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.PackLoading',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.PackLoading',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.PackLoading',3,'loading_supervisor','Supervisor','ACTIVE'),
  (-1,'M10.PackLoading',4,'loading_bay','Loading Bay','ACTIVE'),
  (-1,'M10.PackLoading',5,'ordered_qty','Ordered Qty','ACTIVE'),
  (-1,'M10.PackLoading',6,'transport_name','Transport','ACTIVE'),
  (-1,'M10.PackLoading',7,'status','Status','ACTIVE'),
  (-1,'M10.PackUnload',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.PackUnload',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.PackUnload',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.PackUnload',3,'source_party','Supplier','ACTIVE'),
  (-1,'M10.PackUnload',4,'oil_rm_item','Item','ACTIVE'),
  (-1,'M10.PackUnload',5,'ordered_qty','Received Qty','ACTIVE'),
  (-1,'M10.PackUnload',6,'supervisor_name','Supervisor','ACTIVE'),
  (-1,'M10.PackUnload',7,'status','Status','ACTIVE'),
  (-1,'M10.IntTransferIn',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.IntTransferIn',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.IntTransferIn',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.IntTransferIn',3,'source_party','From Unit','ACTIVE'),
  (-1,'M10.IntTransferIn',4,'reference_no','Ref No','ACTIVE'),
  (-1,'M10.IntTransferIn',5,'ordered_qty','Qty','ACTIVE'),
  (-1,'M10.IntTransferIn',6,'status','Status','ACTIVE'),
  (-1,'M10.IntTransferOut',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.IntTransferOut',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.IntTransferOut',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.IntTransferOut',3,'source_party','To Unit','ACTIVE'),
  (-1,'M10.IntTransferOut',4,'reference_no','Ref No','ACTIVE'),
  (-1,'M10.IntTransferOut',5,'ordered_qty','Qty','ACTIVE'),
  (-1,'M10.IntTransferOut',6,'transport_name','Transport','ACTIVE'),
  (-1,'M10.IntTransferOut',7,'status','Status','ACTIVE'),
  (-1,'M10.ReturnStockIn',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.ReturnStockIn',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.ReturnStockIn',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.ReturnStockIn',3,'source_party','Returned By','ACTIVE'),
  (-1,'M10.ReturnStockIn',4,'reference_no','Ref No','ACTIVE'),
  (-1,'M10.ReturnStockIn',5,'oil_rm_item','Item','ACTIVE'),
  (-1,'M10.ReturnStockIn',6,'ordered_qty','Qty','ACTIVE'),
  (-1,'M10.ReturnStockIn',7,'status','Status','ACTIVE'),
  (-1,'M10.ThirdPartyIn',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.ThirdPartyIn',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.ThirdPartyIn',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.ThirdPartyIn',3,'source_party','Party','ACTIVE'),
  (-1,'M10.ThirdPartyIn',4,'transport_name','Transport','ACTIVE'),
  (-1,'M10.ThirdPartyIn',5,'ordered_qty','Qty','ACTIVE'),
  (-1,'M10.ThirdPartyIn',6,'status','Status','ACTIVE'),
  (-1,'M10.ThirdPartyOut',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.ThirdPartyOut',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.ThirdPartyOut',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.ThirdPartyOut',3,'source_party','Party','ACTIVE'),
  (-1,'M10.ThirdPartyOut',4,'transport_name','Transport','ACTIVE'),
  (-1,'M10.ThirdPartyOut',5,'ordered_qty','Qty','ACTIVE'),
  (-1,'M10.ThirdPartyOut',6,'status','Status','ACTIVE'),
  (-1,'M10.EwayBill',0,'vehicle_no','Vehicle','ACTIVE'),
  (-1,'M10.EwayBill',1,'driver_name','Driver','ACTIVE'),
  (-1,'M10.EwayBill',2,'driver_phone','Phone','ACTIVE'),
  (-1,'M10.EwayBill',3,'invoice_no','Invoice','ACTIVE'),
  (-1,'M10.EwayBill',4,'invoice_qty','Invoice Qty','ACTIVE'),
  (-1,'M10.EwayBill',5,'transport_name','Transport','ACTIVE'),
  (-1,'M10.EwayBill',6,'status','Status','ACTIVE')
ON DUPLICATE KEY UPDATE
  sort_order=VALUES(sort_order), label=VALUES(label), status=VALUES(status);

-- Point the shipped gate-pass slip at the flow-variant map (flowField discriminator).
-- Keeps the static details[] as a fallback for records with no mapped variant.
UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_SET(
  pv.body_json,
  '$.layout[0].flowField', 'flow_variant_code'
)
WHERE pd.code = 'aria:gate-pass-slip'
  AND JSON_EXTRACT(pv.body_json, '$.layout[0].kind') = 'QrCode';

-- One-click "QR Slip" row action on the gate-pass list → opens the flow-aware
-- slip for that pass, ready to print. Idempotent: only appended if absent.
UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_ARRAY_APPEND(
  pv.body_json, '$.rowActions',
  JSON_OBJECT('label', '🏷 QR Slip', 'action', 'page.open',
              'page', 'aria:gate-pass-slip',
              'params', JSON_OBJECT('id', 'row.id'))
)
WHERE pd.code = 'aria:recent-gate-passes'
  AND JSON_EXTRACT(pv.body_json, '$.rowActions') IS NOT NULL
  AND JSON_SEARCH(pv.body_json, 'one', 'aria:gate-pass-slip') IS NULL;
