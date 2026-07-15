-- =====================================================================
-- EARMS local-dev MariaDB bootstrap.
--
-- ONE-TIME setup on a fresh MariaDB instance. Creates the `earms` schema
-- and the `earms_app` user with privileges scoped to that schema only.
--
-- The Flyway-style numbered migrations under src/main/resources/db/migration/
-- (V1__baseline.sql ... V31__gl_ar_rfq_jobwork_boiler_alerts.sql) run after
-- this bootstrap — most automatically on backend startup via the
-- M0PageSchemaInitialiser idempotent reapply hooks; the baseline V1 you
-- apply manually below.
--
-- Run as a MariaDB superuser:
--
--   mysql -h 127.0.0.1 -P 3306 -uroot -p < 00_create_schema_and_user.sql
--
-- After this, load the baseline:
--
--   mysql -h 127.0.0.1 -uearms_app -pearms_local_dev earms \
--         < ../migration/V1__baseline.sql
--
-- ...or just boot the backend — the initialiser applies migrations on demand.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS earms
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_uca1400_ai_ci;

-- MariaDB 10.5+ uses CREATE USER IF NOT EXISTS; the password matches the
-- value in application.properties (psql.datasource.password). Change BOTH
-- here and in application.properties if you want a different password for
-- your local dev environment.
CREATE USER IF NOT EXISTS 'earms_app'@'127.0.0.1'
  IDENTIFIED BY 'earms_local_dev';
CREATE USER IF NOT EXISTS 'earms_app'@'localhost'
  IDENTIFIED BY 'earms_local_dev';

-- Scoped grants: this account can do ANYTHING inside `earms` (DDL +
-- DML + temp tables for the GENERATED column workarounds) but cannot
-- touch any other schema on the host. The 'never touch other DBs' rule
-- from the project conventions is enforced at the SQL grant level.
GRANT ALL PRIVILEGES ON earms.* TO 'earms_app'@'127.0.0.1';
GRANT ALL PRIVILEGES ON earms.* TO 'earms_app'@'localhost';

-- Required so the app can read `information_schema` for the DESCRIBE-table
-- reflection the Designer endpoints do (column existence checks before
-- ALTER TABLE ADD COLUMN).
GRANT PROCESS ON *.* TO 'earms_app'@'127.0.0.1';
GRANT PROCESS ON *.* TO 'earms_app'@'localhost';

FLUSH PRIVILEGES;

SELECT 'Bootstrap complete — earms schema + earms_app user ready' AS status;
