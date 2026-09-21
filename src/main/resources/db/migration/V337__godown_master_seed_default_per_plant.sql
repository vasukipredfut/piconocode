-- Every plant gets a default Godown named after itself (godown_code = the
-- plant's own plant_code), so a plant with no detailed sub-location
-- breakdown yet (Perundurai/Erode/Veppampalayam) still has somewhere to
-- post stock. SR0001 already has 5 real sub-locations (V334) -- this just
-- adds one more, named after the plant itself, alongside them.
--
-- Idempotent -- safe to (re)run on any database.

INSERT INTO m5_plant_godown (tenant_id, plant_id, godown_code, godown_name)
SELECT 1, p.id, p.plant_code, p.plant_name
FROM m5_plant p
WHERE p.tenant_id = 1
ON DUPLICATE KEY UPDATE godown_name = VALUES(godown_name);
