#!/bin/bash
# =====================================================================
# EARMS — MariaDB export helper.
#
# Usage:
#   ./export-db.sh                          # dumps schema+data to ../../../../backups/
#   ./export-db.sh /path/to/out.sql         # dumps to an explicit path
#   DB_HOST=127.0.0.1 ./export-db.sh        # override any connection var
#
# Env overrides (defaults match application.properties):
#   DB_HOST      default 192.187.121.34   (the live configured host — NOT 127.0.0.1)
#   DB_PORT      default 3306
#   DB_USER      default earms_app
#   DB_PASSWORD  default earms_local_dev
#   DB_NAME      default earms
#
# Produces a single .sql file with schema + data + routines/triggers/events,
# suitable for restoring with:
#   mysql -h <host> -u<user> -p<password> <db> < earms_export.sql
# =====================================================================

set -e

DB_HOST="${DB_HOST:-192.187.121.34}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-earms_app}"
DB_PASSWORD="${DB_PASSWORD:-earms_local_dev}"
DB_NAME="${DB_NAME:-earms}"

cd "$(dirname "$0")"

if [ -n "$1" ]; then
  OUT_FILE="$1"
else
  BACKUP_DIR="../../../../../backups"
  mkdir -p "$BACKUP_DIR"
  OUT_FILE="$BACKUP_DIR/earms_export_$(date +%Y%m%d_%H%M%S).sql"
fi

DUMP_BIN=""
for candidate in mariadb-dump mysqldump; do
  if command -v "$candidate" >/dev/null 2>&1; then
    DUMP_BIN="$candidate"
    break
  fi
done
if [ -z "$DUMP_BIN" ]; then
  echo "Neither mariadb-dump nor mysqldump found on PATH." >&2
  echo "Install the MariaDB client, or point PATH at e.g. 'C:/Program Files/MariaDB 12.3/bin'." >&2
  exit 1
fi

echo "Dumping ${DB_NAME}@${DB_HOST}:${DB_PORT} with ${DUMP_BIN} -> ${OUT_FILE}"

MYSQL_PWD="$DB_PASSWORD" "$DUMP_BIN" \
  --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" \
  --routines --triggers --events --single-transaction --quick \
  "$DB_NAME" > "$OUT_FILE"

echo "Done: $(du -h "$OUT_FILE" | cut -f1) written to $OUT_FILE"
