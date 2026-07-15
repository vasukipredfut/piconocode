-- =====================================================================
-- V18 — Seed sample integration_instance rows so the M11 integrations
-- list page (pico:m11-integrations-list) shows data instead of looking
-- empty. Each row pairs a tenant with one of the 9 concrete M11 adapters
-- in dryRun mode (safe — no live network calls).
-- =====================================================================

INSERT IGNORE INTO integration_instance
  (tenant_id, code, connector_code, connector_version, name, description, status, current_version, config_json, mapped_workflow_codes, data_residency_flag)
VALUES
  (1, 'pico-msg91',        'MSG91_SMS',            1, 'MSG91 SMS — driver OTP',
      'Sends transactional SMS for driver verification + delivery confirmation.',
      'ACTIVE', 1,
      '{"dryRun":true,"senderId":"PICOPL","authKey":"dev-key-9876"}',
      '["M10.TankerInbound","M5.MaterialRequisition"]', 'IN'),
  (1, 'pico-tally',         'TALLY_TDL_XML',        1, 'Tally TDL — voucher push',
      'Posts SI/PI vouchers to local Tally listener.',
      'ACTIVE', 1,
      '{"dryRun":true,"baseUrl":"http://localhost:9000","companyName":"PICO POWER PROJECT"}',
      '["M6.SalesInvoice","M6.PurchaseInvoice"]', 'IN'),
  (1, 'pico-zpl',           'M9_LABEL_PRINTER_ZPL', 1, 'ZPL Label Printer — gate-pass',
      'Prints gate-pass + weighment slips to Zebra ZD420 in dispatch shed.',
      'ACTIVE', 1,
      '{"dryRun":true,"host":"192.168.1.50","port":9100}',
      '["M10.TankerInbound","M10.TankerOutbound"]', 'IN'),
  (1, 'pico-nic-einv',      'NIC_IRP_V1.1',         1, 'NIC IRP — e-invoice IRN',
      'Generates IRN + signed QR for sales invoices via NIC sandbox.',
      'ACTIVE', 1,
      '{"dryRun":true,"baseUrl":"https://einv-apisandbox.nic.in","gstin":"33ABCDE2345F1Z6"}',
      '["M6.SalesInvoice"]', 'IN'),
  (1, 'pico-sap',           'SAP_S4_OUTBOUND',      1, 'SAP S/4 — inward pull',
      'Auto-pulls GRN drafts from SAP material-doc API.',
      'ACTIVE', 1,
      '{"dryRun":true,"baseUrl":"https://sap.example.com","plant":"PICO-CBE"}',
      '["M5.InwardReceipt"]', 'IN'),
  (1, 'pico-conveyor',      'MQTT_CONVEYOR',        1, 'Conveyor MQTT — pack line 1',
      'Publishes start/stop/setSpeed commands to packing-line PLC.',
      'ACTIVE', 1,
      '{"dryRun":true,"brokerUrl":"tcp://factory.local:1883","topicBase":"factory/conveyor"}',
      '["M5.PackingRun","M5.FGDispatch"]', 'IN'),
  (1, 'pico-modbus-wb',     'MODBUS_TCP',           1, 'Modbus — Weighbridge #1',
      'Reads gross/tare/net registers from Avery weighbridge.',
      'ACTIVE', 1,
      '{"dryRun":true,"host":"192.168.1.60","port":502,"unitId":1}',
      '["M10.TankerInbound","M10.TankerOutbound"]', 'IN'),
  (1, 'pico-email-imap',    'EMAIL_IMAP_INBOUND',   1, 'Vendor email inbox',
      'Polls vendor@picopower.in for new vendor invoices + customer enquiries.',
      'ACTIVE', 1,
      '{"dryRun":true,"host":"imap.gmail.com","port":993,"username":"vendor@picopower.in","folder":"INBOX","pollIntervalMin":5}',
      '["M6.PurchaseInvoice","M8.Lead"]', 'IN'),
  (1, 'pico-excel-import',  'EXCEL_BULK_IMPORT',    1, 'Bulk CSV/Excel import',
      'Bulk-loads master data (BOM, items, vendors) via uploaded CSV/Excel.',
      'ACTIVE', 1,
      '{}', '["M5.BillOfMaterials","M5.Vendor"]', 'IN');
