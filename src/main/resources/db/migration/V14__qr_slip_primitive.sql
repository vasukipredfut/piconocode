-- V14 — QrCode primitive + a printable gate-pass slip.
--
-- The substrate could SCAN a QR (QrScan) but never DISPLAY one, so the front
-- gate could not print the pass slip a driver carries and the exit gate scans —
-- exactly what was "not rendering". QrCode fills the gap generically: it renders
-- a scannable QR of a record's key plus its details, print-ready. This registers
-- it and stamps aria:gate-pass-slip, which reads ?id= and prints that pass.

INSERT INTO page_element_catalog (code, category, display_name, icon, description, status, config_schema_json)
VALUES ('QrCode', 'DISPLAY', 'QR Slip', 'QrCode2',
  'Renders a scannable QR of a value or a record field, with details and a Print button. For gate-pass / invoice / batch slips.',
  'AVAILABLE',
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','value','type','string','help','Static value or {{row.field}} to encode. Use this OR entity mode.'),
    JSON_OBJECT('name','entity','type','entityCode','help','Entity mode: fetch the record by ?id= and encode qrField.'),
    JSON_OBJECT('name','idParam','type','string','help','URL query param holding the record id (default "id").'),
    JSON_OBJECT('name','qrField','type','fieldCode','help','Which field becomes the QR value (e.g. gp_number).'),
    JSON_OBJECT('name','title','type','string'),
    JSON_OBJECT('name','caption','type','string'),
    JSON_OBJECT('name','size','type','number','default',180),
    JSON_OBJECT('name','details','type','array','help','[{field,label}] lines printed under the QR.'),
    JSON_OBJECT('name','print','type','boolean','default',true)
  )))
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), description=VALUES(description),
  config_schema_json=VALUES(config_schema_json), status='AVAILABLE';

-- Gate-pass slip page (open with ?id=<gate pass id>). Product row (-1).
INSERT INTO page_definition (tenant_id, code, title, category, description, status)
SELECT -1, 'aria:gate-pass-slip', 'Gate Pass Slip', 'M10',
       'Printable gate-pass slip with a scannable QR — carried by the driver, scanned at the exit gate.', 'PUBLISHED'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE code='aria:gate-pass-slip');

INSERT INTO page_version (page_id, version, body_json, status, published_at)
SELECT pd.id, 1, JSON_OBJECT(
  'kind','layout','schemaVersion',2,
  'layout', JSON_ARRAY(JSON_OBJECT(
    'kind','QrCode',
    'title','Gate Pass',
    'entity','GatePass',
    'idParam','id',
    'qrField','gp_number',
    'caption','Present this slip at the exit gate.',
    'size',190,
    'details', JSON_ARRAY(
      JSON_OBJECT('field','vehicle_no','label','Vehicle'),
      JSON_OBJECT('field','driver_name','label','Driver'),
      JSON_OBJECT('field','driver_phone','label','Phone'),
      JSON_OBJECT('field','flow_variant_code','label','Flow'),
      JSON_OBJECT('field','status','label','Status'))
  ))
), 'PUBLISHED', NOW()
FROM page_definition pd WHERE pd.code='aria:gate-pass-slip'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id=pd.id);

UPDATE page_definition pd
SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
WHERE pd.code='aria:gate-pass-slip';
