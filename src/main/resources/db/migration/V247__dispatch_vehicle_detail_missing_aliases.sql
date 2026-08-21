-- aria:dispatch-vehicle-details actually submits attachmentDocs, brokerContactNumber
-- and partyNames (not the poAttachment/brokerNumber/partyName aliases added by
-- V246), so WorkflowProjectionListener's exact-match pluck() silently dropped
-- the PO attachment, broker contact number, and party list on every save.
-- Add the actual submitted keys as additional aliases for the existing columns.

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json,
      '$.po_attachment', JSON_ARRAY_APPEND(JSON_EXTRACT(field_map_json, '$.po_attachment'), '$', 'attachmentDocs'),
      '$.broker_number', JSON_ARRAY_APPEND(JSON_EXTRACT(field_map_json, '$.broker_number'), '$', 'brokerContactNumber'),
      '$.party_name',    JSON_ARRAY_APPEND(JSON_EXTRACT(field_map_json, '$.party_name'), '$', 'partyNames'))
WHERE target_table='dispatch_vehicle_detail'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.PackUnload',
                         'M10.ReturnStockIn','M10.TankerInbound','M10.TankerOutbound')
  AND JSON_CONTAINS_PATH(field_map_json, 'one', '$.po_attachment', '$.broker_number', '$.party_name');
