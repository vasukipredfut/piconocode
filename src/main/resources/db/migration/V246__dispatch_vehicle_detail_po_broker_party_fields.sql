-- aria:dispatch-vehicle-details: add PO Number (multiple, JSON array -- same
-- shape as billing_entry.invoice_numbers/V104 and dispatch_vehicle_detail's
-- own oil_rm_item/V237), PO Attachment (MULTIPLE files -- same JSON-array
-- shape, one PO can have more than one scanned attachment), Broker Number
-- (phone, alongside the existing broker_name), and Party Name (multiple,
-- same JSON-array shape as po_numbers). Drops tanker_no -- no longer
-- captured on this page.

ALTER TABLE dispatch_vehicle_detail
  ADD COLUMN IF NOT EXISTS po_numbers     TEXT        DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS po_attachment  TEXT        DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS broker_number  VARCHAR(16) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS party_name     TEXT        DEFAULT NULL,
  DROP COLUMN IF EXISTS tanker_no;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'DispatchVehicleDetail',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'po_numbers' fc,'PO Number' lbl,'JSON' dt,0 filt,0 sort,1 srch,'repeater' wk
      UNION ALL SELECT 'po_attachment','PO Attachment','JSON',0,0,0,'file'
      UNION ALL SELECT 'broker_number','Broker Number','STRING',1,1,1,'text'
      UNION ALL SELECT 'party_name','Party Name','JSON',0,0,1,'repeater') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code=c.fc);

DELETE FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='tanker_no';

UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(
      JSON_SET(field_map_json,
        '$.po_numbers',    JSON_ARRAY('po_numbers','poNumbers'),
        '$.po_attachment', JSON_ARRAY('po_attachment','poAttachment'),
        '$.broker_number', JSON_ARRAY('broker_number','brokerNumber'),
        '$.party_name',    JSON_ARRAY('party_name','partyName')),
      '$.tanker_no')
WHERE target_table='dispatch_vehicle_detail'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.PackUnload',
                         'M10.ReturnStockIn','M10.TankerInbound','M10.TankerOutbound');
