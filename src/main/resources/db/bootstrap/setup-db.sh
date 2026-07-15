#!/bin/bash
# =====================================================================
# EARMS — Local MariaDB bootstrap helper.
#
# Usage:
#   ./setup-db.sh                              # interactive
#   MARIADB_ROOT_PASSWORD=... ./setup-db.sh     # non-interactive
#
# What it does:
#   1. Confirms a MariaDB instance is reachable on 127.0.0.1:3306
#   2. Runs 00_create_schema_and_user.sql as root (creates `earms` schema
#      + `earms_app` user with schema-scoped grants)
#   3. Loads V1__baseline.sql as earms_app so subsequent backend boots
#      can apply newer migrations idempotently
#   4. Prints next steps
#
# After this, just `mvn -o package -DskipTests && java -jar ...` from the
# earmsbackend directory.
# =====================================================================

set -e

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'

cd "$(dirname "$0")"
BOOTSTRAP_SQL="00_create_schema_and_user.sql"
BASELINE_SQL="../migration/V1__baseline.sql"

echo -e "${BLUE}=====================================================${NC}"
echo -e "${BLUE} EARMS — Local MariaDB Bootstrap${NC}"
echo -e "${BLUE}=====================================================${NC}"

# Step 1: client check
echo -e "${YELLOW}Step 1: Checking MariaDB client...${NC}"
if ! command -v mysql >/dev/null 2>&1; then
  echo -e "${RED}MariaDB / MySQL client not found.${NC}"
  echo "Install via: brew install mariadb     # macOS"
  echo "         or: apt install mariadb-client  # Debian/Ubuntu"
  exit 1
fi
echo -e "${GREEN}✓ mysql client found ($(mysql --version | head -1))${NC}"

# Step 2: get root password
if [ -z "${MARIADB_ROOT_PASSWORD:-}" ]; then
  read -rsp "MariaDB root password (will not echo): " MARIADB_ROOT_PASSWORD
  echo
fi

# Step 3: confirm root can connect
echo -e "${YELLOW}Step 2: Testing root connection to 127.0.0.1:3306...${NC}"
if ! MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mysql -h 127.0.0.1 -uroot -e "SELECT 1" >/dev/null 2>&1; then
  echo -e "${RED}Cannot connect to MariaDB as root. Check the password and that the daemon is running.${NC}"
  echo "  brew services start mariadb        # macOS"
  echo "  sudo systemctl start mariadb        # Linux"
  exit 1
fi
echo -e "${GREEN}✓ root connection OK${NC}"

# Step 4: run schema + user bootstrap
echo -e "${YELLOW}Step 3: Creating earms schema + earms_app user...${NC}"
MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mysql -h 127.0.0.1 -uroot < "$BOOTSTRAP_SQL"
echo -e "${GREEN}✓ schema + user ready${NC}"

# Step 5: confirm earms_app login
echo -e "${YELLOW}Step 4: Verifying earms_app login...${NC}"
if MYSQL_PWD=earms_local_dev mysql -h 127.0.0.1 -uearms_app -e "USE earms; SELECT 'login ok' AS status" 2>/dev/null; then
  echo -e "${GREEN}✓ earms_app can log in${NC}"
else
  echo -e "${RED}earms_app login failed.${NC}"
  exit 1
fi

# Step 6: load baseline if earms is empty
echo -e "${YELLOW}Step 5: Checking if V1 baseline needs loading...${NC}"
TABLE_COUNT=$(MYSQL_PWD=earms_local_dev mysql -h 127.0.0.1 -uearms_app earms -Nse \
  "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='earms'")
if [ "$TABLE_COUNT" = "0" ]; then
  if [ -f "$BASELINE_SQL" ]; then
    echo -e "${YELLOW}  Loading V1__baseline.sql ($(wc -l < "$BASELINE_SQL") lines)...${NC}"
    MYSQL_PWD=earms_local_dev mysql -h 127.0.0.1 -uearms_app earms < "$BASELINE_SQL"
    TABLE_COUNT_NEW=$(MYSQL_PWD=earms_local_dev mysql -h 127.0.0.1 -uearms_app earms -Nse \
      "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='earms'")
    echo -e "${GREEN}✓ baseline loaded — $TABLE_COUNT_NEW tables now in earms${NC}"
  else
    echo -e "${RED}  ${BASELINE_SQL} not found — skipping baseline load${NC}"
  fi
else
  echo -e "${GREEN}✓ earms already has $TABLE_COUNT tables — skipping baseline${NC}"
fi

echo
echo -e "${GREEN}=====================================================${NC}"
echo -e "${GREEN} Bootstrap complete${NC}"
echo -e "${GREEN}=====================================================${NC}"
echo
echo "Connection settings (already match application.properties):"
echo "  url:       jdbc:mariadb://127.0.0.1:3306/earms"
echo "  username:  earms_app"
echo "  password:  earms_local_dev"
echo
echo "Next steps from earmsbackend/ :"
echo "  mvn -o package -DskipTests"
echo "  java -jar target/aria-0.0.1-SNAPSHOT.jar"
echo
echo "The backend's M0PageSchemaInitialiser will apply newer migrations"
echo "(V19, V21, V22, ...) idempotently on startup if their probe conditions"
echo "match. For full freshness, you can also run any V*.sql manually:"
echo "  mysql -h 127.0.0.1 -uearms_app -pearms_local_dev earms < ../migration/V31__*.sql"
