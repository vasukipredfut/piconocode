-- loading_progress_detail_bay.bay_name (V179) was CHECK-restricted to the 12
-- physical bays "Bay1".."Bay12" (V180), enforced identically in
-- M10StartBayActionHandler. For M10.TankerOutbound there is no bay --
-- instead the operator picks a tank from m10_tanker_location (V217, codes
-- like 'ROT-1', 'F7', 'GNP-2') and that code is what gets saved into this
-- same bay_name column. A CHECK constraint can't express "Bay1..Bay12 OR any
-- code that exists in m10_tanker_location" (no subqueries in MariaDB CHECK),
-- so the enum-style guarantee is dropped here and moves entirely into the
-- handler: bay_name (when given) must still be Bay1..Bay12; the tank-code
-- path is only taken when bay_name is absent, per M10StartBayActionHandler.
--
-- Idempotent -- MariaDB has no "DROP CONSTRAINT IF EXISTS" for CHECK before
-- 10.5.2 semantics are guaranteed here, so this guards via information_schema
-- same as V180 did for the ADD.

SET @constraint_exists = (
  SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'loading_progress_detail_bay'
    AND CONSTRAINT_NAME = 'chk_lpdb_bay_name'
);

SET @sql = IF(@constraint_exists > 0,
  'ALTER TABLE loading_progress_detail_bay DROP CONSTRAINT chk_lpdb_bay_name',
  'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
