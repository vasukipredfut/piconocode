---
description: Draft a new workflow definition as a workflow_definition row (state machine + transitions + SpEL guards). NO switch statements in Java, NO per-workflow controller, NO state-machine library. Use this whenever a customer flow needs a new state machine.
---

# New workflow — the substrate way

## The one rule

A workflow in EARMS is **ONE row** in `workflow_definition` — a JSON body with states + transitions + SpEL guards. The `WorkflowEngine` interprets it. There is NO per-workflow Java class.

## The wrong way (do NOT do this)

```java
// ❌ DO NOT WRITE THIS
@Service
public class LoadingFlowService {
    public void transition(Long id, String action) {
        Instance i = repo.findById(id).orElseThrow();
        switch (i.getState()) {
            case "GATE_PASS_CREATED":
                if ("scan-parking".equals(action)) { i.setState("PARKING_IN_1"); }
                break;
            case "PARKING_IN_1":
                // ...
        }
    }
}
```

Every switch statement over states is a workflow written in the wrong place. The substrate already has the state machine — this code duplicates it, drifts from the workflow_definition row, and can't be edited by the visual workflow designer.

## The right way (do THIS)

### Step 1 — draft the state list on paper

Every state has:
- `code` — SCREAMING_SNAKE_CASE (e.g. `GATE_PASS_CREATED`)
- `assignedRole` — who owns work in this state (or omit if system-driven)
- `terminal: true` — mark end states

### Step 2 — draft transitions

Every transition has:
- `from` — source state code
- `to` — target state code
- `trigger` — usually `page.submitted` (default), or `workflow.transition` (button action), or `system.schedule` (cron)
- `pageCode` — the page whose submission drives this transition
- `guard` — optional SpEL predicate. Reads submitted form data via `#submitted['fieldName']`

### Step 3 — write the migration

```sql
-- V83__m10_dispatch_return_flow.sql
-- New M10 workflow — customer return-stock inbound flow.
-- Idempotent.

INSERT INTO workflow_definition (tenant_id, code, version, status, body_json, initial_state)
SELECT 1, 'M10.ReturnStockInbound', 1, 'PUBLISHED',
  JSON_OBJECT(
    'code', 'M10.ReturnStockInbound',
    'version', 1,
    'initialState', 'DRAFT',
    'states', JSON_ARRAY(
      JSON_OBJECT('code','DRAFT'),
      JSON_OBJECT('code','GATE_PASS_CREATED', 'assignedRole','FrontGate'),
      JSON_OBJECT('code','PARKING_IN', 'assignedRole','Dispatch'),
      JSON_OBJECT('code','QC_PENDING', 'assignedRole','QC'),
      JSON_OBJECT('code','QC_APPROVED', 'assignedRole','Warehouse'),
      JSON_OBJECT('code','QC_REJECTED', 'terminal', TRUE),
      JSON_OBJECT('code','UNLOADED', 'assignedRole','Dispatch'),
      JSON_OBJECT('code','GATE_PASS_OUT_CLOSED', 'terminal', TRUE)
    ),
    'transitions', JSON_ARRAY(
      JSON_OBJECT('from','DRAFT', 'to','GATE_PASS_CREATED',
                  'trigger','page.submitted', 'pageCode','pico:front-gate-create'),
      JSON_OBJECT('from','GATE_PASS_CREATED', 'to','PARKING_IN',
                  'trigger','page.submitted', 'pageCode','pico:parking-scan'),
      JSON_OBJECT('from','PARKING_IN', 'to','QC_PENDING',
                  'trigger','page.submitted', 'pageCode','pico:return-qc-entry'),
      JSON_OBJECT('from','QC_PENDING', 'to','QC_APPROVED',
                  'trigger','page.submitted', 'pageCode','pico:return-qc-decision',
                  'guard', '#submitted[''decision''] == ''APPROVED'''),
      JSON_OBJECT('from','QC_PENDING', 'to','QC_REJECTED',
                  'trigger','page.submitted', 'pageCode','pico:return-qc-decision',
                  'guard', '#submitted[''decision''] == ''REJECTED'''),
      JSON_OBJECT('from','QC_APPROVED', 'to','UNLOADED',
                  'trigger','page.submitted', 'pageCode','pico:return-unload'),
      JSON_OBJECT('from','UNLOADED', 'to','GATE_PASS_OUT_CLOSED',
                  'trigger','page.submitted', 'pageCode','pico:gate-pass-out-closure')
    )
  ),
  'DRAFT'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition
                  WHERE tenant_id=1 AND code='M10.ReturnStockInbound' AND version=1);
```

### Step 4 — bind the workflow to a starting page

```sql
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, role, step_code)
VALUES
  (1, 'pico:front-gate-create',   'M10.ReturnStockInbound', 'INPUT',    'GATE_PASS_CREATE'),
  (1, 'pico:parking-scan',        'M10.ReturnStockInbound', 'INPUT',    'PARKING_IN'),
  (1, 'pico:return-qc-entry',     'M10.ReturnStockInbound', 'INPUT',    'QC_PENDING'),
  (1, 'pico:return-qc-decision',  'M10.ReturnStockInbound', 'APPROVER', 'QC_PENDING'),
  (1, 'pico:return-unload',       'M10.ReturnStockInbound', 'INPUT',    'UNLOADED'),
  (1, 'pico:gate-pass-out-closure','M10.ReturnStockInbound','INPUT',    'GATE_PASS_OUT_CLOSED');
```

### Step 5 — probe hook (same pattern as new-entity skill)

Add a probe to `M0PageSchemaInitialiser`:

```java
try {
    Integer hasV83 = jdbc.queryForObject(
            "SELECT COUNT(*) FROM workflow_definition WHERE code='M10.ReturnStockInbound'",
            Integer.class);
    if (hasV83 == null || hasV83 == 0) {
        log.info("V83 return-stock workflow absent; applying V83");
        try { runScript(c, "db/migration/V83__m10_dispatch_return_flow.sql"); }
        catch (RuntimeException e) { log.warn("V83 application failed: {}", e.getMessage()); }
    }
} catch (Exception probe) { log.warn("V83 probe failed: {}", probe.getMessage()); }
```

### Step 6 — verify in the visual designer

Open `http://localhost:3000/workflow-designer`, filter to `M10.ReturnStockInbound`. The states + transitions render as a React Flow graph. If the flow doesn't look right, edit the SQL and reapply.

## Guard syntax quick-ref

All guards are Spring Expression Language (SpEL) evaluated against the submitted form data:

```
#submitted['decision'] == 'APPROVED'
#submitted['decision'] == 'REJECTED'
#submitted['withinTolerance'] == 'YES'
#submitted['weight_delta'] > 0.02
#submitted['status'] == 'PASS' && #submitted['temperature'] < 85
#submitted['qty'] >= 100 || #submitted['override'] == true
```

Escaping in SQL: single-quotes double up. `'APPROVED'` inside a JSON string in a SQL statement becomes `''APPROVED''`.

## When workflow logic needs Java

If a transition needs to write to an entity, notify by SMS, call a REST API, etc., the ONLY sanctioned place is a `WorkflowTransitionListener` implementation. Not a per-workflow service:

```java
@Component
public class ReturnStockAuditListener implements WorkflowTransitionListener {
    @Override
    public void onTransition(WorkflowInstance i, String prev, Map<String,Object> bag) {
        if (!"M10.ReturnStockInbound".equals(i.getWorkflowCode())) return;
        if (!"QC_REJECTED".equals(i.getCurrentState())) return;
        // ~20 lines: pluck bag, insert audit row, notify vendor
    }
}
```

One `if`-guard on workflow-code + state, direct `JdbcTemplate` writes, done. No repository, no service class.

## Engineering principles for workflows

### State machine purity

- **States are nouns.** `GATE_PASS_CREATED`, `QC_PENDING`, `APPROVED`. Not verbs. Not questions.
- **Every non-terminal state has an `assignedRole`.** If nobody owns work at this state, why does the state exist?
- **Every path leads to a terminal state.** No orphan states unreachable from `initialState`. No states without an outgoing transition (unless `terminal: true`).
- **Terminal states are truly terminal.** Add `"terminal": true`. Terminal states may not have outgoing transitions.
- **State code length ≤ 32 chars.** Enforced by `workflow_instance.current_state VARCHAR(64)`. Prefer ≤ 20 for readability.
- **Cyclic transitions are allowed but rare** — e.g. `RELOAD_REQUIRED → WEIGHMENT_2` in PackLoading. Every cycle must have a way out (or a hard rework counter to prevent infinite loops).

### Transition + guard design

- **One trigger per transition.** Prefer `page.submitted` (default). Use `workflow.transition` when action is a button click without a form. Use `system.schedule` only for cron-driven expiries.
- **Every branching transition needs a guard.** If two transitions leave the same state on the same trigger, both need guards that are mutually exclusive.
- **Guards are single-expression SpEL.** Not multi-line. Not `if/else`. If your logic needs `if/else`, that's a workflow state you didn't model.
- **Guards read submitted form data only.** `#submitted['fieldName']`. No cross-instance queries in guards.
- **Same-value comparisons use `==`** not `!=` where possible — mutually-exclusive `== 'APPROVED'` and `== 'REJECTED'` is clearer than `!= 'APPROVED'`.
- **No magic strings in guards.** If a status code is used ≥ 3 times, it's an enum in `entity_field_registry.options_json`, not a scattered literal.

### Workflow code naming

- `<MODULE>.<Flow>` — e.g. `M10.TankerInbound`, `M5.PackingRun`, `ACME.SupplierOnboarding`.
- Module prefix must match an existing package (`M0` through `M11`) OR a customer prefix (`ACME`, `PICO` etc.) — never a bare name.

### SonarQube rules that apply

| Rule | Enforcement |
|---|---|
| **S1192** | Same state code / role name used ≥ 3 times → extract as JSON variable inside migration |
| **SM03** | The `INSERT INTO workflow_definition` uses `WHERE NOT EXISTS` |
| **S3776** | If any transition's `guard` has cognitive complexity > 3 (multiple `&&`, `\|\|`, nested comparisons), extract logic into a state |
| **Custom** | Every non-terminal state must have ≥ 1 outgoing transition |
| **Custom** | Every transition's `pageCode` must resolve to a `page_definition.code` on the same tenant |

## Coverage requirements — every new workflow must ship with

### 1. Backend transition coverage test — every happy path

`earmsfrontend/tests/test-agents/workflow-<workflow-code>-api.spec.js`:

```javascript
const { test, expect } = require("@playwright/test");
const BASE = "http://localhost:8080";
async function login(api) { /* as in new-entity skill */ }
const hdrs = (tok) => ({ Authorization: `Bearer ${tok}`, "Content-Type": "application/json" });

test("<WorkflowCode> — happy path drives from initial to first terminal state", async ({ request }) => {
  const tok = await login(request);
  const create = await request.post(`${BASE}/api/workflow/instances`, {
    headers: hdrs(tok),
    data: { workflowCode: "<WorkflowCode>" },
  });
  expect(create.ok()).toBe(true);
  const id = (await create.json()).id;

  // Every transition on the happy path — one call per transition
  const transitions = [
    { toState: "<STATE_1>", submitted: { /* fields */ } },
    { toState: "<STATE_2>", submitted: { /* fields */ } },
    // ... every step
  ];
  for (const t of transitions) {
    const r = await request.post(`${BASE}/api/workflow/transition`, {
      headers: hdrs(tok),
      data: { instanceId: id, toState: t.toState, submitted: t.submitted },
    });
    expect(r.ok()).toBe(true);
    const j = await r.json();
    expect(j.transitioned).toBe(true);
    expect(j.currentState).toBe(t.toState);
  }
});
```

### 2. Guard-rejection test — for every branching transition

```javascript
test("<WorkflowCode> — guard on <STATE_X> rejects with 409 + reason", async ({ request }) => {
  const tok = await login(request);
  // ... drive instance to STATE_X
  const r = await request.post(`${BASE}/api/workflow/transition`, {
    headers: hdrs(tok),
    data: { instanceId: id, toState: "<REJECTED_STATE>", submitted: { decision: "APPROVED" /* wrong for this transition */ } },
  });
  expect(r.status()).toBe(409);
  const j = await r.json();
  expect(j.reason).toContain("guard");
});
```

### 3. UI walk — one Playwright spec per role that touches the workflow

`earmsfrontend/tests/test-agents/workflow-<workflow-code>-ui-<role>.spec.js`:

Walk from the sidebar to each page in the workflow, fill the form, submit, verify state advances.

### 4. Coverage matrix increment

After committing, `python3 scripts/coverage-matrix.py`. The workflow appears in the "Workflow test coverage" section with 100% pages-tested. If any page in the workflow has no test, the workflow shows < 100%.

## Reference

Workflows already following this pattern (read them if unsure):
- `M10.TankerInbound` — 15-state tanker flow, full guards
- `M10.EwayBill` — 6-state flow with terminal states
- `M10.PackLoading` — 24-state loading flow (Dhivya's V78 file — this one is textbook)
- `M5.PackingRun` — production flow with cross-workflow triggers

Standards: `docs/architecture/ENGINEERING-STANDARDS.md`
Coverage matrix: `scripts/coverage-matrix.py`
