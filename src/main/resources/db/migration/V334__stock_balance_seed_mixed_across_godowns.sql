-- Seed data: Plant #1 ("Main Plant") + its 5 Godowns (from the business's
-- own sub-location list), then a mixed/varied stock quantity for every
-- existing SKU across all 5 Godowns -- demo/seed data, not a real physical
-- count.
--
-- lot_no is set to '' (not NULL) on every seeded row so the uq_balance
-- unique key (tenant_id, item_code, lot_no, bin_code) actually treats
-- reseeded rows as duplicates on a re-run -- two NULLs are never equal in
-- MariaDB's unique-key semantics, which would otherwise let this insert the
-- same rows again every time this migration re-applies.
--
-- Idempotent -- safe to (re)run on any database (INSERT IGNORE below).

INSERT INTO m5_plant (id, tenant_id, plant_code, plant_name)
SELECT 1, 1, 'SR0001', 'Main Plant'
WHERE NOT EXISTS (SELECT 1 FROM m5_plant WHERE id = 1);

INSERT INTO m5_plant_godown (tenant_id, plant_id, godown_code, godown_name)
SELECT 1, 1, x.code, x.name FROM (
  SELECT 'SRG0001' AS code, 'POUCH-GODOWN' AS name
  UNION ALL SELECT 'SRG0002', 'PET/JAR-GODOWN'
  UNION ALL SELECT 'SRG0003', 'OLD TIN-GODOWN'
  UNION ALL SELECT 'SRG0004', 'NEW TIN-GODOWN'
  UNION ALL SELECT 'SRG0005', 'REWORK LOCATION'
) x
WHERE NOT EXISTS (
  SELECT 1 FROM m5_plant_godown g WHERE g.tenant_id = 1 AND g.plant_id = 1 AND g.godown_code = x.code
);

INSERT IGNORE INTO m5_stock_balance
  (tenant_id, item_code, item_name, lot_no, bin_code, warehouse, plant_id, uom, qty_on_hand)
SELECT 1, s.sku_code, s.sku_name, '', g.godown_code, g.godown_code, g.plant_id,
       COALESCE(s.uom_primary, 'PCS'), FLOOR(RAND() * 50000) + 1
FROM m5_sku_master s
CROSS JOIN m5_plant_godown g
WHERE s.tenant_id = 1 AND g.tenant_id = 1 AND g.plant_id = 1;
