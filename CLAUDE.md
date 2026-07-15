# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

- Spring Boot 3.3.5 · Java 21 (jakarta.*) · Hibernate 6 · MariaDB 11 (utf8mb4, ROW_FORMAT=DYNAMIC)
- Lombok 1.18.38 (don't downgrade — versions ≤ 1.18.34 break javac on JDK 21+ during `mvn compile`)
- Flyway-style numbered migrations under `src/main/resources/db/migration/`
- Primary auth: JWT via `Authorization: Bearer <token>` — handled by `com.aria.config.AuthTokenFilter`. **Header-only — no `?token=` URL fallback.**
- Optional integrations: Redis, SQL Server (legacy `WMS_VVV` for read-only), Tomcat 10

## Build + Run

```bash
mvn -o clean package -DskipTests          # offline build (uses ~/.m2)
java -jar target/aria-0.0.1-SNAPSHOT.jar # boots on :8080

# Restart pattern after code change (used dozens of times in this repo):
kill $(ps aux | grep 'aria-0.0.1-SNAPSHOT.jar' | grep -v grep | awk '{print $2}' | head -1) 2>/dev/null
mvn -q -o package -DskipTests
nohup java -jar target/aria-0.0.1-SNAPSHOT.jar > /tmp/bk.log 2>&1 &
until curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/m0/datasource/health 2>&1 | grep -qE "200|401"; do sleep 2; done
```

Health: `GET /api/m0/datasource/health` returns 200 once Spring + Hibernate are warm. Cold start ~50 s.

## Tests

```bash
mvn -q -o test                            # all (≈ 224 tests)
mvn -o test -Dtest=PayrollMathTest        # single class
mvn -o test -Dtest=GstMathTest,PayrollMathTest
```

Notes:
- **`mvn -q test` swallows failure output** — drop the `-q` when investigating a red test.
- Mockito inline-mocks (`@MockBean`, `mock(JdbcTemplate.class)`) break on JDK 25 surefire forks with `Could not modify all classes`. Prefer pure-arithmetic tests (`PayrollMathTest`, `GstMathTest`) over Mockito-on-`JdbcTemplate`. Live wiring is covered by the API smoke tests run from the frontend's `tests/` Playwright specs.

## Database

```yaml
host:     127.0.0.1:3306
schema:   earms
user:     earms_app
password: earms_local_dev
```

**Fresh-machine bootstrap** (one-time, before first backend boot):
```bash
cd src/main/resources/db/bootstrap
./setup-db.sh                                  # creates earms schema + earms_app user + loads V1
# or non-interactive:
MARIADB_ROOT_PASSWORD=... ./setup-db.sh
```
Falls back to running `00_create_schema_and_user.sql` as root if you'd rather do it by hand. After bootstrap the backend's `M0PageSchemaInitialiser` picks up newer V19/V21/V22/... migrations on the next start.

⚠️ Older `earmsbackend/sql_migrations/quick-setup.sh` is for **PostgreSQL idayam_erp** (prior prod). Do NOT run it for local dev. Same goes for the older `DATABASE_SETUP_GUIDE.md`.

- **Do not touch other MariaDB schemas** or the prod PostgreSQL listed in `application.properties`.
- 50+ numbered migrations (`V1__baseline.sql` through `V31__gl_ar_rfq_jobwork_boiler_alerts.sql`). Use the next free V-number for new migrations.
- Many migrations have an idempotent reapply hook in `M0PageSchemaInitialiser` — when adding a critical fix migration, add a matching probe so dev DBs pick it up on next boot.
- MariaDB-specific gotcha: `GENERATED ALWAYS AS (... CURRENT_DATE ...) STORED` is rejected as non-deterministic; use `VIRTUAL` for those, OR maintain the column from app code.
- Existing CHECK constraint on `page_version.body_json` is `json_valid(body_json)` — `INSERT`s with bad JSON fail loudly. Strip embedded `\n` from string literals before inserting.

## Package Layout

```
com.aria/
├── AriaApplication.java          # @SpringBootApplication entrypoint
├── config/                          # WebSecurityConfig, JwtUtils, AuthTokenFilter, beans
├── controller/                      # legacy mixed-domain controllers (M1-M4-ish)
├── dao/                             # legacy repositories
├── m0/                              # PAGE SUBSTRATE (the heart of the platform)
│   ├── page/                        # PageDefinitionController, PageVersion service
│   ├── designer/                    # DesignerController — no-code field + page authoring
│   ├── entity/                      # GenericEntityController — universal CRUD
│   ├── entity_registry/             # Entity registry CRUD
│   ├── datasource/                  # /api/m0/datasource/query — generic SQL builder
│   ├── action/                      # Schema-V2 action registry
│   ├── document/                    # DocumentUploadController (multipart upload)
│   ├── reminder/                    # ComplianceReminderController
│   └── pivot/, savedview/, audit/   # supporting primitives
├── m5/                              # MES (production / stock / packing / dispatch)
├── m6/                              # Finance — GL, AR, posting, GST forms
│   ├── posting/                     # GlAutoPostingService — auto-post sales/purch/payroll to GL
│   ├── invoice/                     # GstCalcController — intra/inter-state GST math
│   └── gst/                         # Gstr1JsonExportController — GSTN portal JSON
├── m7/                              # HR + Payroll + Statutory
│   ├── payroll/                     # PayrollCalcService (PF/ESI/PT/TDS math), PayslipController
│   └── govforms/                    # 21 government forms (Form 16, 24Q, ECR, GSTR-1/3B, Form 11-22, TB, P&L, BS)
├── m10/                             # Dispatch — gate pass, weighbridge, parking, lane
├── m11/                             # Integration hub (66 connector types + 9 adapters)
├── model/                           # JPA entities (legacy domain)
├── security/                        # JWT auth + TenantContext filter
└── service/                         # UserDetailsServiceImpl + other shared services
```

## Two Architectural Ideas to Internalize

### 1. Page-as-data (M0 substrate)
Every UI page is `page_definition` + `page_version.body_json`. The frontend `Renderer` interprets it. New pages = SQL or `POST /api/m0/designer/pages`. Never `React.createElement` a new screen unless it's a true admin tool (Plant Manager Dashboard, Payroll Console, etc. — listed in `earmsfrontend/CLAUDE.md`).

### 2. Generic entity CRUD via registry
- `entity_registry` row → entity code + display name + table name.
- `entity_field_registry` rows → field code + label + data_type + widget_kind + options_json + required/sortable/filterable/searchable flags.
- `GenericEntityController` reads these and does safe CRUD with column-name allowlist + `SAFE_IDENT` regex on every identifier.
- `DesignerController` extends this: `POST /api/m0/designer/entities/{code}/fields` does atomic `ALTER TABLE ADD COLUMN` + registry insert.

End-to-end add of a new entity:

```sql
-- 1. table + registry rows
CREATE TABLE m5_my_thing (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, ...);
INSERT INTO entity_registry (code, display_name, table_name) VALUES
  ('MyThing','My Thing','m5_my_thing');
```

```bash
# 2. register fields via Designer API (or insert into entity_field_registry directly)
curl -X POST -H "Authorization: Bearer $JWT" -H 'content-type: application/json' \
  -d '{"field_code":"name","display_label":"Name","data_type":"STRING","widget_kind":"text","required":true}' \
  http://localhost:8080/api/m0/designer/entities/MyThing/fields

# 3. stamp the list page (default 18 columns auto-derived from entity_field_registry)
curl -X POST -H "Authorization: Bearer $JWT" -H 'content-type: application/json' \
  -d '{"code":"pico:my-thing-list","title":"My Things","category":"M5","entity":"MyThing","kind":"list"}' \
  http://localhost:8080/api/m0/designer/pages
```

## Auth + Auth Failures You'll Hit

- `Authorization: Bearer <jwt>` is the **only** way into protected endpoints. URL `?token=` is NOT accepted (was tried in Round 18, reverted).
- For HTML-rendering endpoints (Form 16, payslip, P&L), the frontend fetches with the auth header in its authenticated tab, then `document.write`s the body into a fresh `about:blank` new tab. See `earmsfrontend/src/pages/Reports/ReportsHub.js` and `PayrollConsole.viewPayslip`.

## Government Forms

`com.aria.m7.govforms.GovFormController` + `ExtendedFormsController` together expose 21 form endpoints. All are GET, all serve `text/html` (with CSS for `window.print()`) except:
- `/api/m7/forms/pf-ecr` → `text/csv` (EPFO pipe-delimited upload format).
- `/api/m6/gstr1/json` → `application/json` (GSTN Offline Tool format).

## Code Hygiene Specific to This Repo

- Field naming must match column name. Many bugs traced to `field_code` in `entity_field_registry` set to camelCase while the DB column was snake_case → both modal-view and modal-edit silently failed. V21/V21_1 fixed Driver/Vehicle/TankLocation/Vendor.
- `SAFE_IDENT = ^[A-Za-z0-9_]+$` is checked against EVERY table + column name before string-concat into SQL in `GenericEntityController` and `DesignerController`. Don't lift this guard.
- `M0PageSchemaInitialiser` runs idempotent SQL fixes at startup. Add a probe + script call when shipping a critical schema fix that dev DBs need.

## Documentation in this Folder

`API_ENDPOINTS_REFERENCE.md`, `ARCHITECTURE_DIAGRAMS.md`, `CONTROLLER_DETAILED_DOCUMENTATION.md`, `DATABASE_SETUP_GUIDE.md`, `DEVELOPER_SETUP_GUIDE.md`, `HANDOVER_DOCUMENT.md` and the `USER_MANUALS/` series are older (May 2025) and not always current; treat them as supplementary, not authoritative. The live truth is the code + the `docs/requirements/modules/` files in the parent directory.
