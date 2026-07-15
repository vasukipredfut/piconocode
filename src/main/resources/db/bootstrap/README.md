# DB Bootstrap

One-time setup for a fresh MariaDB instance before the backend will boot.

```bash
cd src/main/resources/db/bootstrap
chmod +x setup-db.sh
./setup-db.sh                                    # interactive — asks for root pw
# OR:
MARIADB_ROOT_PASSWORD=mypassword ./setup-db.sh   # non-interactive
```

What this does:

1. Connects as `root` and runs `00_create_schema_and_user.sql` to:
   - `CREATE DATABASE earms` (utf8mb4)
   - `CREATE USER 'earms_app'@'127.0.0.1'` + `localhost` (password `earms_local_dev`)
   - Grant `ALL` on `earms.*` only — **scoped, can't touch other schemas**
   - Grant `PROCESS` so the app's `information_schema` reads work for the Designer endpoints

2. Verifies `earms_app` can log in.

3. If the `earms` schema is empty, loads `V1__baseline.sql`.

After that, `mvn -o package -DskipTests && java -jar target/aria-0.0.1-SNAPSHOT.jar` will boot and the `M0PageSchemaInitialiser` will apply newer Vx migrations on demand.

## Files

| File | Purpose |
|---|---|
| `00_create_schema_and_user.sql` | The actual DDL — run as root. Idempotent (`IF NOT EXISTS`). |
| `setup-db.sh` | Helper that runs the SQL + loads V1 + verifies login. |
| `README.md` | This file. |

## Why not in `docker-compose.yml`?

`docker-compose.yml` declares a hard rule that the stack does NOT start a MariaDB container — the backend connects to the host MariaDB at `host.docker.internal:3306`. Adding a MariaDB service to docker-compose would conflict with that rule, so the bootstrap stays as a host-side shell script.

## Stale Files in this Repo

The older `earmsbackend/sql_migrations/quick-setup.sh` is for **PostgreSQL** `aria_erp` (the prior prod database). Do NOT run it for local dev — the local dev DB is MariaDB `earms`. Same goes for `earmsbackend/DATABASE_SETUP_GUIDE.md` (PostgreSQL).
