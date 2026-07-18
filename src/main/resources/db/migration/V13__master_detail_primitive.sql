-- V13 — MasterDetailList: the expandable master-detail primitive + a demo page.
--
-- The substrate had 29 primitives, none of which could render "a list of records,
-- each expanding to detail fields plus a nested line-item table" — the shape a
-- load-verification / order-dispatch screen needs. MasterDetailList is that
-- primitive: generic (orders+lines, invoices+items, GRN+lines, packing-run+scans),
-- config-driven, registered here so the Page Builder offers it to every tenant.

-- 1. Register the element in the catalog (Builder palette + config contract).
INSERT INTO page_element_catalog (code, category, display_name, icon, description, status, config_schema_json)
VALUES ('MasterDetailList', 'DISPLAY', 'Master-Detail List', 'ViewList',
  'Expandable cards over a master entity; each reveals detail fields and a nested, per-row line-item table. For order dispatch / load verification / any header+lines view.',
  'AVAILABLE',
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','dataSource','type','dataSource','required',true,'help','The master entity (orders / runs / invoices).'),
    JSON_OBJECT('name','title','type','string'),
    JSON_OBJECT('name','subtitle','type','string'),
    JSON_OBJECT('name','card','type','object','help','titleField, titlePrefix, summary:[{field,label,render}], statusField, selectable.'),
    JSON_OBJECT('name','detail','type','object','help','fields:[{field,label}] shown when a card is expanded.'),
    JSON_OBJECT('name','lines','type','object','required',true,
                'help','Child table: {entity, fkField, masterKey, columns:[{field,label,align,render}]}. render "pill" for status columns.')
  )))
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), description=VALUES(description),
  config_schema_json=VALUES(config_schema_json), status='AVAILABLE';

-- 2. Demo page — Load Verification, bound to a real master/child pair with data
--    (m5_packing_run -> m5_packing_scan_log). Product row (-1); every tenant sees it.
INSERT INTO page_definition (tenant_id, code, title, category, description, status)
SELECT -1, 'aria:load-verification', 'Load Verification', 'M10',
       'Audit and confirm outward shipment integrity before dispatch — each run expands to its scan lines.', 'PUBLISHED'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE code='aria:load-verification');

INSERT INTO page_version (page_id, version, body_json, status, published_at)
SELECT pd.id, 1, JSON_OBJECT(
  'kind','layout','schemaVersion',2,
  'layout', JSON_ARRAY(JSON_OBJECT(
    'kind','MasterDetailList',
    'title','Load Verification',
    'subtitle','Audit and confirm outward shipment integrity before vehicle dispatch.',
    'dataSource', JSON_OBJECT('type','entity','entity','PackingRun'),
    'card', JSON_OBJECT(
      'titleField','pr_number','titlePrefix','Run ','statusField','status','selectable',true,
      'summary', JSON_ARRAY(
        JSON_OBJECT('field','packaging_sku','label','SKU'),
        JSON_OBJECT('field','planned_packs','label','Planned'),
        JSON_OBJECT('field','actual_packs','label','Actual'),
        JSON_OBJECT('field','yield_pct','label','Yield %'))),
    'detail', JSON_OBJECT('fields', JSON_ARRAY(
        JSON_OBJECT('field','packing_line','label','Line'),
        JSON_OBJECT('field','machine_code','label','Machine'),
        JSON_OBJECT('field','shift_code','label','Shift'),
        JSON_OBJECT('field','qc_status','label','QC Status'),
        JSON_OBJECT('field','packed_lot_no','label','Packed Lot'))),
    'lines', JSON_OBJECT(
      'entity','PackingScanLog','fkField','packing_run_id','masterKey','id',
      'columns', JSON_ARRAY(
        JSON_OBJECT('field','scanned_barcode','label','Barcode'),
        JSON_OBJECT('field','sku_code','label','SKU'),
        JSON_OBJECT('field','offer_variant','label','Offer'),
        JSON_OBJECT('field','pieces_total','label','Pieces','align','right'),
        JSON_OBJECT('field','outcome','label','Status','render','pill'),
        JSON_OBJECT('field','reject_reason','label','Reason')))
  ))
), 'PUBLISHED', NOW()
FROM page_definition pd WHERE pd.code='aria:load-verification'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id=pd.id);

UPDATE page_definition pd
SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
WHERE pd.code='aria:load-verification';
