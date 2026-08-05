-- Tanker loading/unloading storage tank master (per-tenant): code + display
-- label + the oil/product it's assigned to. Backs the tank-location picker
-- on loading/unloading pages (qc_preload_detail.loading_tank_location,
-- tanker_unloading_detail's unloading location capture, etc.) -- simpler
-- than the existing TankLocation/dispatch_tank_location entity (which
-- carries capacity/density/cleaning-schedule columns this list doesn't
-- have data for), so it's its own table rather than reusing that one.

CREATE TABLE IF NOT EXISTS m10_tanker_location (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  code VARCHAR(16) NOT NULL,
  label VARCHAR(64) NOT NULL,
  product_name VARCHAR(64) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_tanker_location_tenant_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name)
SELECT -1, 'TankerLocation', 'Tanker Location', 'm10_tanker_location'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'TankerLocation');

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'TankerLocation',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk
FROM (SELECT 'code' fc,'Tank Code' lbl,'STRING' dt,1 filt,1 sort,1 srch,1 req,'text' wk
      UNION ALL SELECT 'label','Label','STRING',1,1,1,0,'text'
      UNION ALL SELECT 'product_name','Product','STRING',1,1,1,1,'text'
      UNION ALL SELECT 'active','Active','BOOLEAN',1,1,0,0,'checkbox') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='TankerLocation' AND field_code=c.fc);

-- Tank list, from the "Tanker Loading & Unloading Storage Tank Name List"
-- reference sheet -- one row per physical tank, product_name is the oil
-- grade that tank is assigned to.
INSERT INTO m10_tanker_location (tenant_id, code, label, product_name, active)
SELECT 1, t.code, t.code, t.product_name, 1
FROM (
  SELECT 'ROT-1' code, 'REF SUNFLOWER OIL' product_name UNION ALL
  SELECT 'ROT-3', 'REF SUNFLOWER OIL' UNION ALL
  SELECT 'ROT-4', 'REF SUNFLOWER OIL' UNION ALL
  SELECT 'ROT-5', 'REF SUNFLOWER OIL' UNION ALL
  SELECT 'ROT6',  'REF SUNFLOWER OIL' UNION ALL
  SELECT 'F7',    'REF SUNFLOWER OIL' UNION ALL
  SELECT 'F8',    'REF SUNFLOWER OIL' UNION ALL
  SELECT 'F9',    'REF SUNFLOWER OIL' UNION ALL
  SELECT 'P3',    'REF SUNFLOWER OIL' UNION ALL
  SELECT 'P4',    'REF SUNFLOWER OIL' UNION ALL
  SELECT 'F10',   'GINGELLY OIL' UNION ALL
  SELECT 'P6',    'GINGELLY OIL' UNION ALL
  SELECT 'F21',   'GINGELLY OIL' UNION ALL
  SELECT 'GNP-2', 'GROUNDNUT RAW OIL' UNION ALL
  SELECT 'GNP-1', 'GROUNDNUT RAW OIL' UNION ALL
  SELECT 'P1',    'GROUNDNUT RAW OIL' UNION ALL
  SELECT 'RT1',   'GROUNDNUT RAW OIL' UNION ALL
  SELECT 'RT2',   'GROUNDNUT RAW OIL' UNION ALL
  SELECT 'DOT8',  'REF RICE BRAN OIL' UNION ALL
  SELECT 'ROT-2', 'REF RICE BRAN OIL' UNION ALL
  SELECT 'P2',    'REF RICE BRAN OIL' UNION ALL
  SELECT 'RT3',   'REF RICE BRAN OIL' UNION ALL
  SELECT 'F22',   'REF RICE BRAN OIL' UNION ALL
  SELECT 'GNR-1', 'REF GROUNDNUT OIL' UNION ALL
  SELECT 'F5',    'REF GROUNDNUT OIL' UNION ALL
  SELECT 'F3',    'REF GROUNDNUT OIL' UNION ALL
  SELECT 'P5',    'REF GROUNDNUT OIL' UNION ALL
  SELECT 'COT-4', 'RBD PALM OLEIN' UNION ALL
  SELECT 'COT2',  'RBD PALM OLEIN' UNION ALL
  SELECT 'P7',    'RBD PALM OLEIN' UNION ALL
  SELECT 'PT1',   'RBD PALM OLEIN' UNION ALL
  SELECT 'PT2',   'RBD PALM OLEIN' UNION ALL
  SELECT 'P8',    'COCONUT OIL' UNION ALL
  SELECT 'F19',   'COCONUT OIL' UNION ALL
  SELECT 'F20',   'COCONUT OIL' UNION ALL
  SELECT 'COT-8', 'SUNFLOWER RAW OIL' UNION ALL
  SELECT 'COT-9', 'SUNFLOWER RAW OIL' UNION ALL
  SELECT 'COT-5', 'SUNFLOWER RAW OIL' UNION ALL
  SELECT 'COT-1', 'CRUDE GN' UNION ALL
  SELECT 'SO-1',  'SUPEROLIEN OIL' UNION ALL
  SELECT 'SO-2',  'SUPEROLIEN OIL' UNION ALL
  SELECT 'F18',   'MUSTARD OIL' UNION ALL
  SELECT 'RT-16', 'MUSTARD OIL' UNION ALL
  SELECT 'RT-17', 'MUSTARD OIL'
) t
WHERE NOT EXISTS (SELECT 1 FROM m10_tanker_location WHERE tenant_id = 1 AND code = t.code);

-- List page, same "kind":"list" shape as aria:role-master.
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:tanker-location-master', 'Tanker Locations', 'Tanker loading/unloading storage tank master.',
  'M10', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:tanker-location-master');

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED',
  '{"kind": "list", "dataSource": {"entity": "TankerLocation", "select": ["code", "label", "product_name", "active"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Tank / Product", "type": "text-contains", "applyTo": ["code", "product_name"]}], "pagination": {"pageSize": 50}}',
  NOW()
FROM page_definition pd
WHERE pd.tenant_id = -1 AND pd.code = 'aria:tanker-location-master'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'aria:tanker-location-master' AND pd.current_version_id IS NULL;
