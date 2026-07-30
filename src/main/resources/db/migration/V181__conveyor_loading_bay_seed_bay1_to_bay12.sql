-- Seeds conveyor_loading_bay (V103, M12 Pi-conveyor scanning -- NOT the
-- M10 truck-dock LoadingPoint master, and NOT loading_progress_detail_bay
-- from this same session's M10 Start Bay/Bay Complete work; see V103's own
-- "do not conflate the two" comment) with the 12 physical bays, Bay1..Bay12.
--
-- tenant_id = -1 -- the global/product-catalog tenant convention used
-- throughout this repo for shared master data (see V2__global_catalog.sql,
-- V37's LoadingPoint seed, V75's RouteSkuSummary registration).
--
-- workflow_code = 'M12.ConveyorTest' -- the conveyor-line workflow this bay
-- config belongs to (same value already used as the Postman env default in
-- postman/conveyor_scanner.postman_environment.json). If a different real
-- M12 workflow_code exists, re-run this with that value instead/as well --
-- the UNIQUE KEY (tenant_id, workflow_code, bay_code) allows several
-- workflows to each have their own Bay1..Bay12 set.
--
-- Idempotent -- unique key on (tenant_id, workflow_code, bay_code) ->
-- INSERT IGNORE skips rows already present, safe to (re)run.

INSERT IGNORE INTO conveyor_loading_bay (tenant_id, workflow_code, bay_code, display_name, active)
SELECT -1, 'M12.ConveyorTest', CONCAT('Bay', n), CONCAT('Bay ', n), 1
FROM (
  SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
  SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
  SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
) bays;
