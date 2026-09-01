#!/bin/bash
# =====================================================================
# EARMS — MariaDB backup helper (any server, not just local dev).
#
# Usage:
#   ./backup-db.sh                                    # prompts for everything
#   DB_HOST=192.187.121.34 DB_USER=earms_app DB_PASS=*** DB_NAME=earms ./backup-db.sh
#
# What it does:
#   1. Confirms the target MariaDB instance is reachable
#   2. Dumps the full schema (structure + data + routines/triggers) via
#      mysqldump --single-transaction (consistent snapshot, no table locks)
#   3. Writes to ./backups/<db>_<host>_<timestamp>.sql
#
# Why this matters before a deploy: this project applies schema changes
# via Flyway-style numbered migrations under src/main/resources/db/migration/.
# When you build + start the backend jar against a NEW server's database
# (pointing application.properties / env vars at that server), the app
# automatically runs every migration that server's flyway_schema_history
# doesn't already have -- you do NOT need to manually copy/run individual
# V*.sql files. Take a backup here FIRST so a restore point exists before
# that automatic migration run touches the target database.
# =====================================================================

set -e

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'

echo -e "${BLUE}=====================================================${NC}"
echo -e "${BLUE} EARMS — MariaDB Backup${NC}"
echo -e "${BLUE}=====================================================${NC}"

if ! command -v mysqldump >/dev/null 2>&1; then
  echo -e "${RED}mysqldump not found.${NC}"
  echo "Install via: brew install mariadb        # macOS"
  echo "         or: apt install mariadb-client  # Debian/Ubuntu"
  exit 1
fi

DB_HOST="${DB_HOST:-}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-}"
DB_PASS="${DB_PASS:-}"
DB_NAME="${DB_NAME:-}"

[ -z "$DB_HOST" ] && read -rp "DB host (e.g. 192.187.121.34): " DB_HOST
[ -z "$DB_USER" ] && read -rp "DB user: " DB_USER
[ -z "$DB_PASS" ] && read -rsp "DB password (will not echo): " DB_PASS && echo
[ -z "$DB_NAME" ] && read -rp "DB schema name (e.g. earms): " DB_NAME

echo -e "${YELLOW}Testing connection to ${DB_HOST}:${DB_PORT}...${NC}"
if ! MYSQL_PWD="$DB_PASS" mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" -e "SELECT 1" >/dev/null 2>&1; then
  echo -e "${RED}Cannot connect. Check host/port/user/password/schema.${NC}"
  exit 1
fi
echo -e "${GREEN}✓ connection OK${NC}"

mkdir -p "$(dirname "$0")/backups"
STAMP=$(date +%Y%m%d_%H%M%S)
OUT_FILE="$(dirname "$0")/backups/${DB_NAME}_${DB_HOST}_${STAMP}.sql"

echo -e "${YELLOW}Dumping ${DB_NAME}@${DB_HOST} -> ${OUT_FILE} ...${NC}"
MYSQL_PWD="$DB_PASS" mysqldump -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" \
  --single-transaction --routines --triggers --events \
  "$DB_NAME" > "$OUT_FILE"

echo -e "${GREEN}✓ backup complete: ${OUT_FILE} ($(du -h "$OUT_FILE" | cut -f1))${NC}"
echo
echo "To restore this backup later:"
echo "  mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p $DB_NAME < \"$OUT_FILE\""
