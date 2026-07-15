#!/usr/bin/env bash
# =====================================================================
# EARMS Backend — One-command developer setup for macOS / Linux / WSL.
#
# What this does (idempotent — safe to re-run):
#   1. Verifies Docker is installed and running
#   2. Starts a MariaDB 11 container (port 3306) if one isn't already running
#   3. Runs the schema + user bootstrap inside MariaDB
#   4. Loads V1__baseline.sql if the earms schema is empty
#   5. Builds the Spring Boot jar (`mvn package` or dockerized fallback)
#   6. Prints the start command + login credentials
#
# Usage:
#   ./setup-dev.sh                  # interactive
#   ./setup-dev.sh --yes            # non-interactive (CI / fresh box)
#   ./setup-dev.sh --no-mariadb     # skip the container, use host MariaDB
#   ./setup-dev.sh --skip-build     # bootstrap DB only, don't compile
#
# Frontend lives in the sibling repo earmsfrontend/ — clone it next to this
# one and run its setup-dev.sh to get React going.
# =====================================================================
set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'

ASSUME_YES=false; SKIP_MARIA=false; SKIP_BUILD=false
for arg in "$@"; do
  case "$arg" in
    -y|--yes)         ASSUME_YES=true ;;
    --no-mariadb)     SKIP_MARIA=true ;;
    --skip-build)     SKIP_BUILD=true ;;
    -h|--help)
      sed -n '2,22p' "$0"; exit 0 ;;
  esac
done

cd "$(dirname "$0")"
BACKEND_DIR="$(pwd)"

prompt_yn() { local q="$1"; $ASSUME_YES && return 0; read -rp "$q [Y/n] " ans; [[ -z "$ans" || "$ans" =~ ^[Yy] ]]; }
step() { echo -e "\n${BLUE}━━━ $1 ━━━${NC}"; }
ok()   { echo -e "${GREEN}✓ $1${NC}"; }
warn() { echo -e "${YELLOW}⚠ $1${NC}"; }
fail() { echo -e "${RED}✗ $1${NC}"; exit 1; }

# 1. Prereqs
step "Step 1: Verifying prerequisites"
command -v docker >/dev/null 2>&1     || fail "docker not found — install Docker Desktop / docker engine"
docker info >/dev/null 2>&1            || fail "docker daemon not running — start Docker Desktop"
ok "docker $(docker --version | awk '{print $3}' | tr -d ,)"

HAS_MVN=false; command -v mvn >/dev/null 2>&1 && HAS_MVN=true
$HAS_MVN && ok "maven $(mvn -v 2>/dev/null | head -1 | awk '{print $3}')" || warn "maven not on host — will use dockerized build"

# 2. MariaDB
step "Step 2: MariaDB container"
if $SKIP_MARIA; then
  ok "Skipped (--no-mariadb) — using host MariaDB at 127.0.0.1:3306"
else
  if docker ps --format '{{.Names}}' | grep -q '^earms-mariadb$'; then
    ok "earms-mariadb container already running"
  elif docker ps -a --format '{{.Names}}' | grep -q '^earms-mariadb$'; then
    docker start earms-mariadb >/dev/null
    ok "earms-mariadb started"
  else
    if (echo > /dev/tcp/127.0.0.1/3306) >/dev/null 2>&1; then
      warn "Port 3306 already in use on the host."
      if prompt_yn "Use the existing host service instead of starting a container?"; then
        SKIP_MARIA=true
      else
        fail "Free port 3306 and re-run, or pass --no-mariadb"
      fi
    fi
    if ! $SKIP_MARIA; then
      docker run -d --name earms-mariadb --restart unless-stopped -p 3306:3306 \
        -e MARIADB_ROOT_PASSWORD=root_local_dev \
        -v earms-mariadb-data:/var/lib/mysql \
        mariadb:11 >/dev/null
      echo -n "Waiting for MariaDB"
      for i in {1..40}; do
        if docker exec earms-mariadb mysqladmin -uroot -proot_local_dev ping >/dev/null 2>&1; then echo; ok "MariaDB ready"; break; fi
        echo -n "."; sleep 2; [ "$i" = 40 ] && fail "MariaDB did not become ready in 80 s"
      done
    fi
  fi
fi

# 3. Bootstrap
step "Step 3: Bootstrap earms schema + earms_app user"
BOOTSTRAP_SQL="$BACKEND_DIR/src/main/resources/db/bootstrap/00_create_schema_and_user.sql"
[ -f "$BOOTSTRAP_SQL" ] || fail "Missing $BOOTSTRAP_SQL"
if $SKIP_MARIA; then
  if [ -z "${MARIADB_ROOT_PASSWORD:-}" ]; then read -rsp "Host MariaDB root password: " MARIADB_ROOT_PASSWORD; echo; fi
  MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mysql -h 127.0.0.1 -uroot < "$BOOTSTRAP_SQL"
else
  docker exec -i earms-mariadb mysql -uroot -proot_local_dev < "$BOOTSTRAP_SQL"
fi
ok "earms schema + earms_app user ready"

# 4. Baseline if empty
BASELINE_SQL="$BACKEND_DIR/src/main/resources/db/migration/V1__baseline.sql"
if [ -f "$BASELINE_SQL" ]; then
  if $SKIP_MARIA; then
    TABLES=$(MYSQL_PWD=earms_local_dev mysql -h 127.0.0.1 -uearms_app earms -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='earms'")
  else
    TABLES=$(docker exec earms-mariadb mysql -uearms_app -pearms_local_dev earms -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='earms'")
  fi
  if [ "$TABLES" = "0" ]; then
    echo "Loading V1__baseline.sql..."
    if $SKIP_MARIA; then
      MYSQL_PWD=earms_local_dev mysql -h 127.0.0.1 -uearms_app earms < "$BASELINE_SQL"
    else
      docker exec -i earms-mariadb mysql -uearms_app -pearms_local_dev earms < "$BASELINE_SQL"
    fi
    ok "Baseline loaded"
  else
    ok "$TABLES tables already in earms (skipping baseline)"
  fi
fi

# 5. Build
step "Step 4: Build backend"
if $SKIP_BUILD; then
  ok "Skipped (--skip-build)"
elif $HAS_MVN; then
  mvn -o package -DskipTests -q
  ok "target/idayam-0.0.1-SNAPSHOT.jar ($(du -h target/idayam-0.0.1-SNAPSHOT.jar 2>/dev/null | awk '{print $1}'))"
else
  docker run --rm -v "$BACKEND_DIR:/work" -v earms-m2:/root/.m2 -w /work \
    maven:3.9-eclipse-temurin-21 mvn -o package -DskipTests -q
  ok "JAR built in container"
fi

step "Setup complete"
cat <<'EOF'

╭────────────────────────────────────────────────────────────╮
│  Start the backend:                                        │
│    java -jar target/idayam-0.0.1-SNAPSHOT.jar              │
│    # Cold start ~50 s; warm at /api/m0/datasource/health   │
│                                                            │
│  Default login: 1001 / Test@1234  (Plant Admin)            │
│  Personas:      2001-2004 (same password, different roles) │
│                                                            │
│  Frontend:                                                 │
│    cd ../earmsfrontend && ./setup-dev.sh && npm start      │
│                                                            │
│  Stop MariaDB container:                                   │
│    docker stop earms-mariadb                               │
│  Wipe + start fresh:                                       │
│    docker rm -f earms-mariadb                              │
│    docker volume rm earms-mariadb-data                     │
╰────────────────────────────────────────────────────────────╯

EOF
