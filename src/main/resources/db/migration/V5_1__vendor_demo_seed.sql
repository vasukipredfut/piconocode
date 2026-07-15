-- =====================================================================
-- M0 — Vendor / State / City demo seed (Round 1G)
-- =====================================================================
-- The legacy `vendor` table does not exist in the local dev schema
-- (only `vendor_sister_concern_map` and DTOs survive), so V5_1 creates
-- a Schema V2-shaped `m0_vendor` table that becomes the canonical Vendor
-- entity referenced by entity_registry. State and City already exist in
-- the legacy schema (different shape — `stateid` not `state_id`, no
-- `tenant_id`); we leave them untouched and seed rows only if missing.
-- The Schema V2 list-view spec joins city to state via the legacy
-- columns (`city.stateid = state.id`).
--
-- Idempotent: CREATE IF NOT EXISTS + INSERT IGNORE.
-- =====================================================================

CREATE TABLE IF NOT EXISTS m0_vendor (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  name VARCHAR(256) NOT NULL,
  gstin VARCHAR(16) NULL,
  city_id BIGINT NULL,
  state_id BIGINT NULL,
  is_msme BOOLEAN NOT NULL DEFAULT FALSE,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_m0_vendor_tenant (tenant_id, status),
  KEY idx_m0_vendor_city (city_id),
  KEY idx_m0_vendor_state (state_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Test-DB safety: prod schema (V1 baseline) defines state + city, but the
-- Testcontainers MariaDB used in IT only has the M0 subset. Recreate the
-- minimum columns the inserts below touch. CREATE IF NOT EXISTS so prod
-- (where the tables already exist with their full legacy column set) is
-- a no-op. Columns match the prod schema observed: id, countryid,
-- gst_state_code, name on state; id, countryid, district, districtid,
-- name, pincode, poname, state, stateid on city.
CREATE TABLE IF NOT EXISTS state (
  id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  countryid BIGINT(20) NULL,
  gst_state_code VARCHAR(255) NULL,
  name VARCHAR(255) NULL
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS city (
  id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  countryid BIGINT(20) NULL,
  district VARCHAR(255) NULL,
  districtid BIGINT(20) NULL,
  name VARCHAR(255) NULL,
  pincode INT(11) NULL,
  poname VARCHAR(255) NULL,
  state VARCHAR(255) NULL,
  stateid BIGINT(20) NULL
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Seed 3 states (idempotent on name).
INSERT INTO state (name, gst_state_code, countryid)
SELECT 'Tamil Nadu', '33', 1 WHERE NOT EXISTS (SELECT 1 FROM state WHERE name='Tamil Nadu');
INSERT INTO state (name, gst_state_code, countryid)
SELECT 'Karnataka', '29', 1 WHERE NOT EXISTS (SELECT 1 FROM state WHERE name='Karnataka');
INSERT INTO state (name, gst_state_code, countryid)
SELECT 'Andhra Pradesh', '28', 1 WHERE NOT EXISTS (SELECT 1 FROM state WHERE name='Andhra Pradesh');

-- Seed 12 cities (4 per state). NULL-safe inserts via NOT EXISTS.
-- Tamil Nadu cities
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Chennai', (SELECT id FROM state WHERE name='Tamil Nadu' LIMIT 1), 1, 'Tamil Nadu'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Chennai' AND stateid=(SELECT id FROM state WHERE name='Tamil Nadu' LIMIT 1));
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Coimbatore', (SELECT id FROM state WHERE name='Tamil Nadu' LIMIT 1), 1, 'Tamil Nadu'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Coimbatore' AND stateid=(SELECT id FROM state WHERE name='Tamil Nadu' LIMIT 1));
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Madurai', (SELECT id FROM state WHERE name='Tamil Nadu' LIMIT 1), 1, 'Tamil Nadu'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Madurai' AND stateid=(SELECT id FROM state WHERE name='Tamil Nadu' LIMIT 1));
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Tiruchirappalli', (SELECT id FROM state WHERE name='Tamil Nadu' LIMIT 1), 1, 'Tamil Nadu'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Tiruchirappalli' AND stateid=(SELECT id FROM state WHERE name='Tamil Nadu' LIMIT 1));
-- Karnataka cities
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Bengaluru', (SELECT id FROM state WHERE name='Karnataka' LIMIT 1), 1, 'Karnataka'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Bengaluru' AND stateid=(SELECT id FROM state WHERE name='Karnataka' LIMIT 1));
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Mysuru', (SELECT id FROM state WHERE name='Karnataka' LIMIT 1), 1, 'Karnataka'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Mysuru' AND stateid=(SELECT id FROM state WHERE name='Karnataka' LIMIT 1));
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Mangaluru', (SELECT id FROM state WHERE name='Karnataka' LIMIT 1), 1, 'Karnataka'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Mangaluru' AND stateid=(SELECT id FROM state WHERE name='Karnataka' LIMIT 1));
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Hubli', (SELECT id FROM state WHERE name='Karnataka' LIMIT 1), 1, 'Karnataka'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Hubli' AND stateid=(SELECT id FROM state WHERE name='Karnataka' LIMIT 1));
-- Andhra Pradesh cities
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Hyderabad', (SELECT id FROM state WHERE name='Andhra Pradesh' LIMIT 1), 1, 'Andhra Pradesh'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Hyderabad' AND stateid=(SELECT id FROM state WHERE name='Andhra Pradesh' LIMIT 1));
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Vijayawada', (SELECT id FROM state WHERE name='Andhra Pradesh' LIMIT 1), 1, 'Andhra Pradesh'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Vijayawada' AND stateid=(SELECT id FROM state WHERE name='Andhra Pradesh' LIMIT 1));
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Visakhapatnam', (SELECT id FROM state WHERE name='Andhra Pradesh' LIMIT 1), 1, 'Andhra Pradesh'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Visakhapatnam' AND stateid=(SELECT id FROM state WHERE name='Andhra Pradesh' LIMIT 1));
INSERT INTO city (name, stateid, countryid, state)
SELECT 'Tirupati', (SELECT id FROM state WHERE name='Andhra Pradesh' LIMIT 1), 1, 'Andhra Pradesh'
WHERE NOT EXISTS (SELECT 1 FROM city WHERE name='Tirupati' AND stateid=(SELECT id FROM state WHERE name='Andhra Pradesh' LIMIT 1));

-- Seed 20 vendors distributed across cities. Idempotent on (tenant_id, name).
INSERT INTO m0_vendor (tenant_id, name, gstin, city_id, state_id, is_msme, status)
SELECT 1, v.name, v.gstin, c.id, s.id, v.is_msme, v.status FROM (
  SELECT 'TESTRA Logistics'        AS name, '33ABCDE1234F1Z5' AS gstin, 'Chennai'        AS city_name, 'Tamil Nadu'     AS state_name, TRUE  AS is_msme, 'ACTIVE'  AS status UNION ALL
  SELECT 'TESTRO Industries',      '33ABCDE2345F1Z6', 'Chennai',        'Tamil Nadu',     TRUE,  'ACTIVE'  UNION ALL
  SELECT 'Iyer Lubricants',        '33BCDEF3456G1Z7', 'Coimbatore',     'Tamil Nadu',     FALSE, 'ACTIVE'  UNION ALL
  SELECT 'Kovai Trading',          '33CDEFG4567H1Z8', 'Coimbatore',     'Tamil Nadu',     TRUE,  'BLOCKED' UNION ALL
  SELECT 'Madurai Mills',          '33DEFGH5678I1Z9', 'Madurai',        'Tamil Nadu',     FALSE, 'ACTIVE'  UNION ALL
  SELECT 'Trichy Castings',        '33EFGHI6789J1Z0', 'Tiruchirappalli','Tamil Nadu',     TRUE,  'PENDING' UNION ALL
  SELECT 'TESTUR Hardware',        '33FGHIJ7890K1Z1', 'Madurai',        'Tamil Nadu',     FALSE, 'ACTIVE'  UNION ALL

  SELECT 'Bengaluru Bearings',     '29GHIJK8901L1Z2', 'Bengaluru',      'Karnataka',      TRUE,  'ACTIVE'  UNION ALL
  SELECT 'Whitefield Plastics',    '29HIJKL9012M1Z3', 'Bengaluru',      'Karnataka',      FALSE, 'ACTIVE'  UNION ALL
  SELECT 'Mysuru Silks',           '29IJKLM0123N1Z4', 'Mysuru',         'Karnataka',      TRUE,  'ACTIVE'  UNION ALL
  SELECT 'Mangalore Marine',       '29JKLMN1234O1Z5', 'Mangaluru',      'Karnataka',      FALSE, 'BLOCKED' UNION ALL
  SELECT 'Hubli Steel',            '29KLMNO2345P1Z6', 'Hubli',          'Karnataka',      TRUE,  'ACTIVE'  UNION ALL
  SELECT 'Karnataka Chemicals',    '29LMNOP3456Q1Z7', 'Bengaluru',      'Karnataka',      FALSE, 'PENDING' UNION ALL

  SELECT 'Hyderabad Heavy',        '28MNOPQ4567R1Z8', 'Hyderabad',      'Andhra Pradesh', TRUE,  'ACTIVE'  UNION ALL
  SELECT 'Vijaya Vendors',         '28NOPQR5678S1Z9', 'Vijayawada',     'Andhra Pradesh', FALSE, 'ACTIVE'  UNION ALL
  SELECT 'Vizag Logistics',        '28OPQRS6789T1Z0', 'Visakhapatnam',  'Andhra Pradesh', TRUE,  'ACTIVE'  UNION ALL
  SELECT 'Tirupati Traders',       '28PQRST7890U1Z1', 'Tirupati',       'Andhra Pradesh', FALSE, 'BLOCKED' UNION ALL
  SELECT 'AP Agri Solutions',      '28QRSTU8901V1Z2', 'Vijayawada',     'Andhra Pradesh', TRUE,  'PENDING' UNION ALL
  SELECT 'Coastal Components',     '28RSTUV9012W1Z3', 'Visakhapatnam',  'Andhra Pradesh', FALSE, 'ACTIVE'  UNION ALL
  SELECT 'Andhra Allied',          '28STUVW0123X1Z4', 'Tirupati',       'Andhra Pradesh', TRUE,  'ACTIVE'
) v
JOIN city c   ON c.name = v.city_name AND c.stateid = (SELECT id FROM state WHERE name=v.state_name LIMIT 1)
JOIN state s  ON s.name = v.state_name
WHERE NOT EXISTS (SELECT 1 FROM m0_vendor WHERE tenant_id=1 AND name=v.name);
