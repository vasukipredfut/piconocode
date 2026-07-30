-- bay_name on loading_progress_detail_bay (V179) is a fixed set of 12
-- physical bays -- "Bay1" through "Bay12" -- not free text. Enforced at the
-- DB level too (defense in depth alongside M10StartBayActionHandler's own
-- check), same pattern as the existing json_valid(body_json) CHECK on
-- page_version.
--
-- Idempotent -- MariaDB has no "ADD CONSTRAINT IF NOT EXISTS" for CHECK, so
-- this guards via information_schema instead.

SET @constraint_exists = (
  SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'loading_progress_detail_bay'
    AND CONSTRAINT_NAME = 'chk_lpdb_bay_name'
);

-- CONCAT (not ||) -- MariaDB's default sql_mode treats || as logical OR,
-- not string concatenation (PIPES_AS_CONCAT is opt-in, not assumed here).
SET @sql = IF(@constraint_exists = 0,
  CONCAT(
    'ALTER TABLE loading_progress_detail_bay ADD CONSTRAINT chk_lpdb_bay_name ',
    'CHECK (bay_name IN (''Bay1'',''Bay2'',''Bay3'',''Bay4'',''Bay5'',''Bay6'',''Bay7'',''Bay8'',''Bay9'',''Bay10'',''Bay11'',''Bay12''))'),
  'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
