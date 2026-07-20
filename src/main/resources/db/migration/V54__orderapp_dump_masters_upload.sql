-- V52__orderapp_dump_masters_upload.sql
-- Wire the Masters "Import Order Dispatch Detail" page to actually upload the
-- daily OrderApp CSV: replace the inert file field with a FileImportButton that
-- POSTs the file to POST /api/m5/order-dispatch-detail/import (the real 35-col
-- importer). The frontend FileImportButton primitive backs this kind.
UPDATE page_version SET body_json = '{"kind": "layout", "schemaVersion": 2, "layout": [{"kind": "Section", "title": "Import Order Dispatch Detail", "description": "Upload the daily OrderApp order-vs-dispatch CSV/TSV export (35 columns). It imports into m5_order_dispatch_detail via the OrderApp importer — the loading-allocation order picker and PMT tolerance read from there.", "children": [{"kind": "FileImportButton", "label": "Upload & Import Order Dump", "endpoint": "m5/order-dispatch-detail/import", "fileField": "file", "accept": ".csv,.tsv,.txt", "onDonePage": "pico:order-dispatch-detail"}]}]}'
 WHERE id = (SELECT current_version_id FROM page_definition WHERE code='pico:order-dispatch-detail-upload');
