-- Seed the 4 real plants from the business's "MAIN PLANTS" list. The first
-- row (blank PART CODE, "MAIN PLANT" section) is the same physical site as
-- the "SR0001" placeholder V334 already created -- this updates its name to
-- the real one instead of inserting a duplicate plant_code.
--
-- Idempotent -- safe to (re)run on any database.

INSERT INTO m5_plant (tenant_id, plant_code, plant_name) VALUES
  (1, 'SR0001', 'SUNRAJA - HEAD OFFICE'),
  (1, 'SUN07068', 'PRI-SUNRAJA OIL INDUSTRIES PRIVATE LIMITED'),
  (1, 'SUN05548', 'ERD SUNRAJA OIL INDUSTRIES PRIVATE LIMITED'),
  (1, 'SUN06250', 'VEPPAMPALAYAM - SUNRAJA OIL INDUSTRIES')
ON DUPLICATE KEY UPDATE plant_name = VALUES(plant_name);
