---
description: Add a new page to EARMS as a page_definition + page_version row (JSON body_json). NO React component, NO route in src/routes/index.js. Use this whenever a customer needs a new form, list, kanban, or dashboard.
---

# New page — the substrate way

## The one rule

A page in EARMS is **TWO rows**: one `page_definition` + one `page_version` with a JSON `body_json`. The React `Renderer` interprets `body_json`. There is NO React component per page.

## The wrong way (do NOT do this)

```js
// ❌ DO NOT WRITE THIS
// src/pages/customer/GatePassClosurePage.jsx
export default function GatePassClosurePage() {
  const [gateNo, setGateNo] = useState("");
  const [signature, setSignature] = useState(null);
  return (
    <Panel>
      <TextField ... />
      <SignaturePad ... />
      <Button onClick={submit}>Close</Button>
    </Panel>
  );
}

// src/routes/index.js
{ path: "/gate-pass-closure", element: <GatePassClosurePage /> }
```

Every hand-coded page is one that cannot be edited by the customer's admin, cannot be A/B tested, cannot be role-filtered, cannot be re-arranged without a deploy. Use the substrate.

## The right way (do THIS)

### Step 1 — pick the kind

- `list` — table of rows from an entity (uses `ListView`)
- `form` — capture fields, submit to a workflow (uses `Form` + `FormField` + `WorkflowActionButton`)
- `layout` — dashboard / free-form composition (uses `Grid`, `Section`, `KPI`, `LineChart`, etc.)
- `kanban` — swimlane board (uses `KanbanView`)
- `detail` — single-row detail with sections (uses `DetailView`)

Pick one. Do NOT invent new kinds without shipping a new primitive first (see `docs/architecture/CODE-REVIEW-develope-2026-07-11.md` on the substrate design gate).

### Step 2 — for kind=list, use the one-shot designer API

```bash
JWT=$(curl -s -X POST http://localhost:8080/api/auth/signin \
  -H 'Content-Type: application/json' \
  -d '{"username":"1001","password":"Test@1234"}' | jq -r .accessToken)

curl -X POST http://localhost:8080/api/m0/designer/pages \
  -H "Authorization: Bearer $JWT" -H 'Content-Type: application/json' \
  -d '{
    "code":   "pico:parking-log-list",
    "title":  "Parking Log",
    "category":"M10",
    "entity": "ParkingLog",
    "kind":   "list"
  }'
```

Done. Open `/configurable/pico:parking-log-list` and it renders. Columns come from `entity_field_registry`, `+ New` button opens the `EntityFormModal`, filters + sort + CSV export all work.

### Step 3 — for kind=form or kind=layout, write the SQL

```sql
-- V84__pico_return_qc_entry_page.sql
-- Return-stock QC entry form.
-- Idempotent.

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'pico:return-qc-entry', 'Return Stock QC Entry',
       'QC captures observations for returned stock before decision.', 'M10', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition
                  WHERE tenant_id=1 AND code='pico:return-qc-entry');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion', 2,
    'kind', 'layout',
    'layout', JSON_ARRAY(
      JSON_OBJECT('kind','Section','props', JSON_OBJECT('title','Return QC observations'),
        'fields', JSON_ARRAY(
          JSON_OBJECT('kind','FormField', 'name','batchCode',     'label','Batch Code',    'type','text',   'required',TRUE),
          JSON_OBJECT('kind','FormField', 'name','sampleWeight',  'label','Sample Weight', 'type','number', 'required',TRUE),
          JSON_OBJECT('kind','FormField', 'name','observation',   'label','Observation',   'type','textarea','required',TRUE),
          JSON_OBJECT('kind','FormField', 'name','photoRef',      'label','Photo',         'type','file',   'required',FALSE),
          JSON_OBJECT('kind','WorkflowActionButton','label','Submit for Decision',
                     'action','workflow.transition','variant','primary')
        )
      )
    )
  ),
  'V84 — return QC entry page', CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='pico:return-qc-entry'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id=p.id AND v.version=1);

UPDATE page_definition p
  JOIN page_version v ON v.page_id=p.id AND v.version=1
  SET p.current_version_id = v.id
WHERE p.tenant_id=1 AND p.code='pico:return-qc-entry' AND p.current_version_id IS NULL;
```

### Step 4 — probe hook (same as new-entity, new-workflow)

Add to `M0PageSchemaInitialiser`. Copy the V80/V81 pattern.

### Step 5 — verify

Open `http://localhost:3000/configurable/pico:return-qc-entry` in a logged-in browser. All fields render, the submit button is present, submission triggers `workflow.transition` on whatever workflow is bound to this page code via `page_workflow_binding`.

## Available primitives

Check `page_element_catalog` for the full list. As of V80 the substrate ships:

**Layout**: `Grid`, `Section`, `Tabs`, `Markdown`, `KanbanView`, `WorkflowStepper`
**Display**: `KPI`, `LineChart`, `DataTable`, `ListView`, `DetailView`, `PivotTable`, `WorkflowQueueList`
**Input**: `FormField` (text/textarea/number/currency/select/multiselect/checkbox/date/datetime/entity-ref/email/password/file), `Form`, `SignaturePad`, `QrScan`, `OtpVerify`
**Cell renderers (inside ListView column.render)**: `sparkline`, `progress`, `pill`, `delta`
**Action**: `WorkflowActionButton`
**Device**: `WeighbridgeLive`, `TelemetryGauge`, `PushNotification`

## Field-level rules (V78 feature)

Any FormField can carry three optional rule strings:

```json
{
  "kind":"FormField",
  "name":"rejectReason",
  "label":"Reason (if rejected)",
  "type":"text",
  "visibleWhen":  "#form.decision == 'REJECTED'",
  "requiredWhen": "#form.decision == 'REJECTED'"
}
```

`computedFrom` also works for auto-derived values (e.g. `net_kg = gross_kg - tare_kg`).

## When a page genuinely needs to be hand-coded

The whitelist of hand-coded React pages is in `earmsfrontend/CLAUDE.md#B) Hand-coded admin tools`:

- Page Designer, Workflow Designer, Integration Console
- Reports Hub, Payroll Console
- Plant Manager / Owner / Compliance Dashboards

Everything else is `body_json`. If you think your page belongs on the whitelist, that's a substrate design decision — write a proposal in `docs/architecture/` first.

## Engineering principles for pages

### Single-purpose per page

- **One job per page.** A page is either a list, a form, a layout dashboard, or a kanban — never a hybrid. If you feel the urge to combine list + form on one page, that's a two-page workflow.
- **Page title matches business language.** "Drivers" not "Driver Master". "New Gate Pass" not "Create GatePass".
- **≤ 40 fields per form page.** Above that, split into sections via the `Tabs` primitive (auto-enabled in EntityFormModal at ≥ 4 sections) or split into a multi-step workflow.
- **Page category matches sidebar layout.** `M10` maps to Dispatch section; `HR` to HR; `Master` to Masters. Wrong category = page won't be findable.

### Body_json hygiene

- **Every FormField has `name` + `label`.** Missing labels break accessibility and screen readers.
- **Every FormField `type` is one of the 12 supported widget kinds.** No custom type strings without a matching primitive.
- **`workflow.transition` action buttons declare `label`.** No bare button that reads "OK" or "Submit".
- **No inline SVG or raw HTML in body_json.** If you need a diagram, ship a primitive.
- **No hardcoded strings that vary per tenant.** Use `entity_field_registry.options_json` for dropdown values so they're editable per tenant.

### SonarQube rules that apply

| Rule | Enforcement |
|---|---|
| **SM03** | Every `INSERT INTO page_definition` / `page_version` uses `WHERE NOT EXISTS` |
| **S1192** | Field labels used ≥ 3 times → extract to a variable in the migration |
| **JSON validity** | `page_version.body_json` must pass `JSON_VALID()` — the DB CHECK constraint enforces this at write time |
| **Custom** | Every page code follows `<customer>:<domain>-<action>` format |
| **Custom** | Every workflow-bound page has ≥ 1 row in `page_workflow_binding` |
| **Custom** | Every page's `current_version_id` is not NULL after the migration |

## Coverage requirements — every new page must ship with

### 1. Backend load test — page renders + body_json is valid

`earmsfrontend/tests/test-agents/page-<page-code>-api.spec.js`:

```javascript
const { test, expect } = require("@playwright/test");
const BASE = "http://localhost:8080";

test("<page-code> — loads valid body_json", async ({ request }) => {
  const login = await request.post(`${BASE}/api/auth/signin`, {
    data: { username: "1001", password: "Test@1234" },
  });
  const tok = (await login.json()).accessToken;

  const r = await request.get(`${BASE}/api/m0/pages/${encodeURIComponent("<page-code>")}`,
    { headers: { Authorization: `Bearer ${tok}` } });
  expect(r.ok()).toBe(true);
  const body = await r.json();
  expect(body.bodyJson).toBeTruthy();
  const parsed = JSON.parse(body.bodyJson);
  expect(parsed.kind).toBeTruthy();  // one of list|form|layout|kanban|detail
});
```

### 2. Playwright UI test — page navigates + primitives render + interactives clickable

`earmsfrontend/tests/test-agents/page-<page-code>-ui.spec.js`:

```javascript
test("<page-code> — renders + buttons clickable", async ({ page, request }) => {
  // ... login boilerplate

  await page.goto(`http://localhost:3000/configurable/${encodeURIComponent("<page-code>")}`);

  // Assert the appropriate primitive rendered
  await page.waitForSelector('[data-renderer-root="true"]', { timeout: 15000 });

  // Assert no error banner
  const errBanner = await page.locator('[role="alert"][data-loader-state]').count();
  expect(errBanner).toBe(0);

  // Click every visible button (submit / action / navigation) and assert no console errors
  const buttons = page.locator('button:visible, [role="button"]:visible');
  const n = await buttons.count();
  for (let i = 0; i < Math.min(n, 5); i++) {
    // ... click + assert observable effect
  }
});
```

### 3. Assert page appears in existing `qa-agent.spec.js`

`tests/qa-agent.spec.js` walks every page in the app. After your PR:

```bash
cd earmsfrontend
npx playwright test tests/qa-agent.spec.js --project="Desktop Chromium" --timeout=1800000
cat tmp/qa-report.md | grep "<page-code>"
```

The new page should NOT appear in the "issues" section of the report. Categories to avoid: `schema-error`, `list-empty`, `few-columns`, `modal-coverage-poor`, `form-no-inputs`, `kind-unknown`.

### 4. Coverage matrix increment

`python3 scripts/coverage-matrix.py`. The page's parent entity should now show `page_count >= 1`. If the page is workflow-bound, the workflow entry should show +1 pages tested.

## Reference

Pages already following the pattern (read them if unsure):
- `pico:dispatch-kanban` (V80) — kanban board
- `pico:workflow-stepper-demo` (V80) — layout with stepper primitive
- `pico:front-gate-create` — form + workflow-bound
- `pico:driver-master` — list stamped by designer
- `pico:plant-manager-dashboard` — kind=layout with KPIs + charts

Standards: `docs/architecture/ENGINEERING-STANDARDS.md`
Coverage matrix: `scripts/coverage-matrix.py`
