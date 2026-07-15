---
description: Walk the developer through adding a new business entity to EARMS the substrate way — SQL migration + entity_registry + entity_field_registry rows. NO @Entity, NO JpaRepository, NO per-entity controller. Use this whenever a customer needs a new table.
---

# New entity — the substrate way

## The one rule

A new business entity in EARMS is **THREE things**, all data:

1. `CREATE TABLE m<module>_<name>` migration
2. `INSERT INTO entity_registry` row (one)
3. `INSERT INTO entity_field_registry` rows (one per column)

**No Java class. No repository interface. No controller. No DTO.** The generic substrate handles all of that.

## The wrong way (do NOT do this)

```java
// ❌ DO NOT WRITE THIS
@Entity
@Table(name = "m10_dispatch_entry")
public class DispatchEntry {
    @Id @GeneratedValue private Long id;
    @Column private String gatePassNo;
    // ...
}

@Repository
public interface DispatchEntryRepository extends JpaRepository<DispatchEntry, Long> {}

@RestController
@RequestMapping("/api/m10/dispatch-entry")
public class DispatchEntryController {
    // ...
}
```

Every line above is a strategy violation. The develope-branch review (`docs/architecture/CODE-REVIEW-develope-2026-07-11.md`) catches exactly this pattern. If you write it, `substrate-check` will reject the commit.

## The right way (do THIS)

### Step 1 — pick the next free Vn migration

```bash
ls src/main/resources/db/migration/ | sort -V | tail -3
```

Whatever you see (e.g. `V81__...sql`), your file becomes `V82__<snake_case_reason>.sql`.

### Step 2 — write the migration

```sql
-- V82__m10_dispatch_entities.sql
-- Adds DispatchEntry + ParkingLog for the customer loading flow.
-- Idempotent — safe to reapply.

CREATE TABLE IF NOT EXISTS m10_dispatch_entry (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    workflow_instance_id BIGINT NULL,
    gate_pass_no VARCHAR(64) NOT NULL,
    dispatch_type VARCHAR(32) NOT NULL,       -- TANKER | CONTAINER | RETURN_STOCK
    vehicle_no VARCHAR(32) NOT NULL,
    driver_name VARCHAR(128) NULL,
    driver_contact VARCHAR(32) NULL,
    vendor_id VARCHAR(64) NULL,
    broker_id VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_gp_no (gate_pass_no),
    KEY idx_wf_instance (workflow_instance_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Register the entity so GenericEntityController serves /api/m0/entities/DispatchEntry
INSERT INTO entity_registry (code, display_name, table_name, description, category)
SELECT 'DispatchEntry', 'Dispatch Entry', 'm10_dispatch_entry',
       'Loading dispatch capture at front-gate stage 2', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='DispatchEntry');

-- Register every column that operators need to see, filter, or edit
INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, filterable, sortable, searchable, options_json)
SELECT 'DispatchEntry', 'gate_pass_no', 'Gate Pass No.', 'STRING', 'text',
       TRUE, TRUE, TRUE, TRUE, NULL
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='DispatchEntry' AND field_code='gate_pass_no');

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind,
   required, filterable, sortable, searchable, options_json)
SELECT 'DispatchEntry', 'dispatch_type', 'Type', 'STRING', 'select',
       TRUE, TRUE, TRUE, FALSE,
       JSON_ARRAY('TANKER','CONTAINER','RETURN_STOCK')
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                  WHERE entity_code='DispatchEntry' AND field_code='dispatch_type');

-- ... one INSERT per column
```

### Step 3 — add a probe to M0PageSchemaInitialiser (idempotent reapply)

Open `src/main/java/com/idayam/m0/page/service/M0PageSchemaInitialiser.java`, find the block of V78/V79/V80/V81 probes, and add yours:

```java
// V82 — DispatchEntry + ParkingLog for M10 loading flow
try {
    Integer hasV82 = jdbc.queryForObject(
            "SELECT COUNT(*) FROM entity_registry WHERE code='DispatchEntry'",
            Integer.class);
    if (hasV82 == null || hasV82 == 0) {
        log.info("V82 entity registry absent; applying V82");
        try { runScript(c, "db/migration/V82__m10_dispatch_entities.sql"); }
        catch (RuntimeException e) { log.warn("V82 application failed: {}", e.getMessage()); }
    }
} catch (Exception probe) { log.warn("V82 probe failed: {}", probe.getMessage()); }
```

This lets dev DBs pick up the change on next backend boot without a manual Flyway command.

### Step 4 — stamp the list page (one command, no code)

```bash
JWT=$(curl -s -X POST http://localhost:8080/api/auth/signin \
  -H 'Content-Type: application/json' \
  -d '{"username":"1001","password":"Test@1234"}' | jq -r .accessToken)

curl -X POST http://localhost:8080/api/m0/designer/pages \
  -H "Authorization: Bearer $JWT" -H 'Content-Type: application/json' \
  -d '{
    "code":"pico:dispatch-entry-list",
    "title":"Dispatch Entries",
    "category":"M10",
    "entity":"DispatchEntry",
    "kind":"list"
  }'
```

That's the whole entity onboarding. Open `http://localhost:3000/configurable/pico:dispatch-entry-list` — the ListView renders, the columns come from `entity_field_registry`, the `+ New` button opens `EntityFormModal` (also driven by the registry), row-click opens detail modal with all fields, inline-edit works, filters + sort + CSV export all work — **zero React code, zero Java code**.

### Step 5 — verify

Run the test agents from the frontend:

```bash
cd earmsfrontend
npx playwright test tests/test-agents/field-level.spec.js --project="Desktop Chromium" --reporter=line
```

Should pass with the new entity discoverable.

## What business rules go where

If your new entity needs behaviour beyond CRUD (e.g. "on parking-scan submit, upsert ParkingLog"), the answer is a **`WorkflowTransitionListener`** implementation:

```java
package com.idayam.m10.workflow;

@Component
public class ParkingLogWriteThrough implements WorkflowTransitionListener {
    @Autowired private JdbcTemplate jdbc;

    @Override
    public void onTransition(WorkflowInstance instance, String previousState,
                             Map<String, Object> submittedDataBag) {
        String code = instance.getWorkflowCode();
        if (code == null || !code.startsWith("M10.")) return;

        Map<String,Object> parking = (Map<String,Object>) submittedDataBag.get("pico:parking-scan");
        if (parking == null) return;

        // Use JdbcTemplate directly — do NOT create a JpaRepository
        jdbc.update(
          "INSERT INTO m10_parking_log (tenant_id, workflow_instance_id, bay_code, parking_in_time, status) " +
          "VALUES (?, ?, ?, NOW(), ?)",
          TenantContext.get(), instance.getId(), parking.get("bayCode"), instance.getCurrentState());
    }
}
```

That's the ONLY Java you write for an entity's business logic. No entity class, no repo, no controller, no DTO. Just one listener, ~40 lines, using `JdbcTemplate`.

## Engineering principles for entities

### Table design

- **Single responsibility** — one entity per business object. A "gate pass with driver info embedded" is TWO entities (GatePass + Driver) linked by FK, not one denormalized table.
- **Every business table has `tenant_id`** (except reference/lookup tables shared globally). Multi-tenancy is a first-class constraint, not a filter you remember to add.
- **Every business table has `created_at` + `updated_at`** with `DEFAULT CURRENT_TIMESTAMP` + `ON UPDATE CURRENT_TIMESTAMP`. Auditability by default.
- **Foreign keys have matching indexes.** MariaDB does not auto-index FKs; missing indexes destroy list performance at scale.
- **Nullable columns are documented** — every `NULL` column has a comment explaining what NULL means for that business object.
- **No `TEXT` where `VARCHAR(N)` fits.** TEXT columns can't be indexed cleanly; use VARCHAR with a sensible cap.
- **Boolean columns are named `is_<state>` or `has_<thing>`** — never bare `active` or `enabled` without qualification.
- **Enum columns use `VARCHAR(32)` + a `CHECK` constraint** — not MySQL `ENUM` type (poor migration ergonomics).

### Field naming

- `snake_case` in DB, always. `field_code` in registry maps to column name 1:1.
- Never mix `driverPhone` and `driver_phone` in the same entity. Pick snake_case, be consistent.
- Column names ≤ 64 chars (MariaDB limit) but prefer ≤ 40 for readability.

### SonarQube rules that apply to the generated migration

| Rule | Enforcement |
|---|---|
| **SM01** | Every business table has `tenant_id` |
| **SM02** | Every business table has `created_at` + `updated_at` |
| **SM03** | Every `INSERT` guarded with `WHERE NOT EXISTS` |
| **SM04** | Every `CREATE TABLE` uses `IF NOT EXISTS` |
| **SM05** | All columns `snake_case` |
| **SM06** | All FK columns indexed |
| **SM07** | Every `entity_registry` row has matching `entity_field_registry` rows |
| **S1192** | No repeated string literals inside `JSON_OBJECT(...)` calls — extract to a variable if used ≥ 3 times |

## Coverage requirements — every new entity must ship with

### 1. Backend integration test (Playwright API)

`earmsfrontend/tests/test-agents/entity-<entity-code>-api.spec.js`:

```javascript
const { test, expect } = require("@playwright/test");
const BASE = "http://localhost:8080";

async function login(api) {
  const r = await api.post(`${BASE}/api/auth/signin`, {
    data: { username: "1001", password: "Test@1234" },
  });
  return (await r.json()).accessToken;
}
const hdrs = (tok) => ({ Authorization: `Bearer ${tok}`, "Content-Type": "application/json" });

test("<EntityCode> — CRUD via /api/m0/entities/<EntityCode>", async ({ request }) => {
  const tok = await login(request);

  // Schema endpoint returns registered fields
  const s = await request.get(`${BASE}/api/m0/entities/<EntityCode>/schema`, { headers: hdrs(tok) });
  expect(s.ok()).toBe(true);
  const schema = await s.json();
  expect(schema.fields.length).toBeGreaterThan(0);

  // Create
  const c = await request.post(`${BASE}/api/m0/entities/<EntityCode>`, {
    headers: hdrs(tok),
    data: { /* required fields per spec */ },
  });
  expect(c.ok()).toBe(true);
  const id = (await c.json()).id;

  // Read
  const r = await request.get(`${BASE}/api/m0/entities/<EntityCode>/${id}`, { headers: hdrs(tok) });
  expect(r.ok()).toBe(true);

  // Update
  const u = await request.put(`${BASE}/api/m0/entities/<EntityCode>/${id}`, {
    headers: hdrs(tok),
    data: { /* changed fields */ },
  });
  expect(u.ok()).toBe(true);

  // Delete
  const d = await request.delete(`${BASE}/api/m0/entities/<EntityCode>/${id}`, { headers: hdrs(tok) });
  expect(d.ok()).toBe(true);
});
```

### 2. Playwright UI test — list + form modal

`earmsfrontend/tests/test-agents/entity-<entity-code>-ui.spec.js`:

```javascript
test("<EntityCode> — list page renders + form modal opens", async ({ page, request }) => {
  const tok = await login(request);
  await page.goto("http://localhost:3000");
  await page.evaluate((t) => window.localStorage.setItem("accessToken", t), tok);

  await page.goto(`http://localhost:3000/configurable/<page-code>`);
  await page.waitForSelector('[data-primitive="ListView"]', { timeout: 15000 });

  // Add new
  await page.click('button:has-text("+ New")');
  await page.waitForSelector('[role="dialog"]');
  const inputs = await page.locator('[role="dialog"] input, [role="dialog"] textarea').count();
  expect(inputs).toBeGreaterThan(0);
});
```

### 3. Field-rule Jest test — ONLY if this entity uses `visible_when` / `required_when` / `computed_from`

Not needed for plain entities. When rules present, add tests to `earmsfrontend/src/configurable/ruleEval.test.js`.

### 4. Coverage matrix increment

After committing, run `python3 scripts/coverage-matrix.py`. The new entity should appear in the matrix. Its status should be at least `🟡 no workflow` (if there's a page but no workflow binding) or `🟢 covered` (if all three of page + workflow + test exist).

If the entity shows `🔴 no test` after your commit, you didn't ship the required specs above.

## Reference

Entities already following this pattern — read them if unsure:
- `Driver` — masters, no business logic
- `Vehicle` — masters with lookup columns
- `GatePass` — masters + workflow-driven state
- `GRN` — transactional entity with M0PageSchemaInitialiser probe

Standards: `docs/architecture/ENGINEERING-STANDARDS.md`
Coverage matrix: `scripts/coverage-matrix.py`
