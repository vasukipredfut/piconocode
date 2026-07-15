---
description: End-to-end scaffolder for a new customer flow. Takes a natural-language process description (from a customer interview, doc, or transcript) and drafts the full package — entity migrations, workflow definition, page bodies, role bindings, listener stubs. This is the canonical customer-onboarding entry point.
---

# Customer flow — one-shot scaffolder

Use this when a customer describes a process ("we scan tanker at gate → weigh → QC → offload → close gate pass") and you need to convert it into an EARMS PR.

## What you produce

A single migration file (or a numbered pair if entity + workflow), plus optional listener stub, that when applied gives the customer a working end-to-end flow:

1. **Entities** — every domain noun (`GatePass`, `WeighmentEntry`, `QcApproval`, etc.) as `entity_registry` + `entity_field_registry` rows
2. **Workflow** — every process shape as a `workflow_definition.body_json` state machine with SpEL guards
3. **Pages** — every user-facing step as `page_definition` + `page_version` rows
4. **Bindings** — `page_workflow_binding` rows tying each page to the workflow state + role it serves
5. **Listener (optional)** — a `WorkflowTransitionListener` implementation IF write-through to another entity is required
6. **Probe hook** — `M0PageSchemaInitialiser` block so the change picks up automatically

## The interview questions to ask

Before writing SQL, extract these from the customer:

### Nouns (become entities)
- What "things" does the process track? (gate pass, batch, order, sample, invoice, driver, ...)
- Which of them already exist in EARMS? (check `entity_registry`)
- What data does each new noun need? (list fields with type + required flag)

### Verbs (become states + transitions)
- What are the steps in order? (each step = one state)
- Who does each step? (role assignment)
- What decisions branch the flow? (each branch = a guard condition)
- What ends the flow? (terminal states)

### Screens (become pages)
- For each step, what does the operator SEE? (list of instances waiting for them = list page)
- For each step, what does the operator FILL IN? (form page)
- Are there any dashboards? (layout page)

### Side-effects (become listeners)
- On any transition, do we need to write to another table? (write-through listener)
- On any transition, do we need to notify someone? (notify listener)
- On any transition, do we need to call an external system? (adapter through M11)

## The output template

Once you have answers to all four sections above, produce ONE migration file following this template:

```sql
-- V<N>__<customer>_<flow>_scaffold.sql
-- Customer: <name>
-- Interview date: <date>
-- Process source: <link to interview doc>
--
-- Delivers:
--   entities:  <list>
--   workflow:  <code>
--   pages:     <list>
--   bindings:  <count>
--   listener:  <required Java file, if any>

-- ============================================================
-- 1. ENTITIES
-- ============================================================

CREATE TABLE IF NOT EXISTS m<M>_<noun> (...) ENGINE=InnoDB ...;

INSERT INTO entity_registry (code, ...) SELECT ... WHERE NOT EXISTS (...);
INSERT INTO entity_field_registry (...) SELECT ... WHERE NOT EXISTS (...);
-- Repeat for each noun

-- ============================================================
-- 2. WORKFLOW
-- ============================================================

INSERT INTO workflow_definition (tenant_id, code, version, status, body_json, initial_state)
SELECT 1, '<Module>.<Flow>', 1, 'PUBLISHED',
  JSON_OBJECT(
    'code', '<Module>.<Flow>',
    'version', 1,
    'initialState', 'DRAFT',
    'states',      JSON_ARRAY(...),
    'transitions', JSON_ARRAY(...)
  ),
  'DRAFT'
WHERE NOT EXISTS (...);

-- ============================================================
-- 3. PAGES
-- ============================================================

INSERT INTO page_definition (tenant_id, code, title, ...) SELECT ... WHERE NOT EXISTS (...);
INSERT INTO page_version (page_id, version, status, body_json, ...) SELECT ... WHERE NOT EXISTS (...);
UPDATE page_definition p JOIN page_version v ... SET p.current_version_id = v.id ...;
-- Repeat for each page

-- ============================================================
-- 4. BINDINGS
-- ============================================================

INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, role, step_code) VALUES
  (1, 'pico:step-1-page', '<Module>.<Flow>', 'INPUT',    'STATE_1'),
  (1, 'pico:step-2-page', '<Module>.<Flow>', 'APPROVER', 'STATE_2'),
  ...
;
```

## For the optional listener

If write-through is required, ALSO produce a Java stub:

```java
// src/main/java/com/idayam/m<M>/workflow/<Flow>WriteThrough.java
package com.idayam.m<M>.workflow;

import com.idayam.m0.workflow.engine.WorkflowTransitionListener;
import com.idayam.m0.workflow.model.WorkflowInstance;
import com.idayam.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class <Flow>WriteThrough implements WorkflowTransitionListener {
    private static final Logger log = LoggerFactory.getLogger(<Flow>WriteThrough.class);
    @Autowired private JdbcTemplate jdbc;

    @Override
    public void onTransition(WorkflowInstance i, String prev, Map<String,Object> bag) {
        if (!"<Module>.<Flow>".equals(i.getWorkflowCode())) return;
        // Guard which state you write on:
        if (!"<STATE_TO_ACT_ON>".equals(i.getCurrentState())) return;

        // Pluck submitted data from the bag — key is the pageCode that submitted it
        Map<String,Object> submitted = (Map<String,Object>) bag.get("pico:<page-code>");
        if (submitted == null) return;

        // Direct JdbcTemplate write — do NOT create a JpaRepository
        jdbc.update(
          "INSERT INTO m<M>_<table> (tenant_id, workflow_instance_id, ...) VALUES (?, ?, ...)",
          TenantContext.get(), i.getId(), submitted.get("field1"), submitted.get("field2"));
    }
}
```

**One class per side-effect, ~40 lines, JdbcTemplate direct.** Zero repositories, zero DTOs, zero controllers.

## The self-check checklist BEFORE you produce the file

- [ ] Every noun mapped to an entity_registry code?
- [ ] Every field of every entity has a data_type + widget_kind + required flag?
- [ ] Every state has a code (SCREAMING_SNAKE_CASE)?
- [ ] Every state has an assignedRole OR is a terminal state?
- [ ] Every transition has a `from`, `to`, `trigger`, and (usually) `pageCode`?
- [ ] Branching transitions have SpEL guards?
- [ ] Every page has a `code`, `title`, `category`, and `kind`?
- [ ] Every page code is in `page_workflow_binding` if it's part of the flow?
- [ ] The `WHERE NOT EXISTS (…)` guards are present on every INSERT (idempotency)?
- [ ] The `M0PageSchemaInitialiser` probe block is drafted separately?
- [ ] The Vn number does not collide with anything on `main` or the current branch?

## Anti-patterns you MUST NOT introduce

- ❌ A `@Entity` class per noun (use `entity_registry`)
- ❌ A `JpaRepository` per noun (`GenericEntityController` reads via JdbcTemplate)
- ❌ A `*Controller.java` per noun (use `/api/m0/entities/{code}`)
- ❌ A `*Dto.java` per noun (use datasource-query response)
- ❌ A `*Service.java` per noun (use workflow listener if needed)
- ❌ A new `application.properties` line with a non-127.0.0.1 IP
- ❌ A modification to any migration V1..V<current-1> — always ship a new Vn
- ❌ A `.zip` in `src/main/resources/db/migration/`

If your draft contains any of the above, restart from the interview questions.

## Engineering principles for customer flows

### Scope discipline

- **One customer per migration.** Don't bundle two customers' flows in one Vn file — they'll ship + rollback together, which is never the intent.
- **Migration file name identifies the customer.** `Vn__<customer_code>_<flow_slug>.sql`. Not `Vn__updates.sql`.
- **Every migration is idempotent.** Re-running it produces the same DB state, not a duplicate row.
- **No cross-flow globals.** If your customer flow inserts into `entity_registry`, use `WHERE NOT EXISTS`. If a global constant is used elsewhere too, extract it into a preceding V-migration.

### End-to-end coverage per flow

A customer flow is only "done" when:

- ✅ Every entity CRUD works via `/api/m0/entities/<code>` (backend test proves it)
- ✅ Every workflow's happy path drives from initial to terminal (backend test proves it)
- ✅ Every guard rejection returns 409 + reason (backend test proves it)
- ✅ Every page renders + primitives are clickable (Playwright UI test proves it)
- ✅ The full end-to-end walk through the customer's process succeeds in ≤ 5 minutes (e2e Playwright spec proves it)
- ✅ Coverage matrix shows every entity 🟢 covered

### SonarQube rules that apply

Applies to the generated migration + any listener stubs:

- All SQL rules from `new-entity.md`, `new-workflow.md`, `new-page.md`
- **S1192** — repeated JSON strings extracted to variables
- **S138** — a customer flow migration ≥ 500 lines is a smell; split by concern
- **Custom** — the customer's code appears in the file name; `WHERE NOT EXISTS` guards mention the customer's entities

## Coverage requirements — every customer flow must ship with FIVE test files

For a flow like the ACME return-stock example, the PR must include:

### 1. Entity API test

`earmsfrontend/tests/test-agents/entity-AcmeSupplier-api.spec.js` — one per new entity. Uses the template from `new-entity.md`.

### 2. Workflow transition test

`earmsfrontend/tests/test-agents/workflow-ACME.SupplierOnboarding-api.spec.js` — happy path + one guard-rejection. Uses the template from `new-workflow.md`.

### 3. Page-render tests

One per page in the flow: `page-acme:supplier-list-ui.spec.js`, `page-acme:supplier-create-ui.spec.js`.

### 4. End-to-end walk test

`earmsfrontend/tests/e2e-acme-supplier-onboarding.spec.js`:

```javascript
test("ACME supplier onboarding — end-to-end walk", async ({ page, request }) => {
  const tok = await login(request);
  await page.goto("http://localhost:3000");
  await page.evaluate((t) => window.localStorage.setItem("accessToken", t), tok);

  // Step 1 — navigate to Suppliers list
  await page.goto("http://localhost:3000/configurable/acme:supplier-list");
  await page.waitForSelector('[data-primitive="ListView"]');

  // Step 2 — click "+ New"
  await page.click('button:has-text("+ New")');
  await page.waitForSelector('[role="dialog"]');

  // Step 3 — fill required fields
  await page.fill('input[name="name"]', "Test Supplier");
  await page.fill('input[name="gstin"]', "22AAAAA0000A1Z5");

  // Step 4 — submit + assert workflow state advanced
  await page.click('button:has-text("Save")');
  await page.waitForResponse(r => r.url().includes("/api/workflow/transition"));

  // Step 5 — assert row appears in list
  await page.reload();
  await page.waitForSelector('text=Test Supplier');

  // Step 6 — walk KYC → APPROVED
  // ... continue driving the workflow to terminal
});
```

### 5. Listener unit test — if a WorkflowTransitionListener was shipped

`earmsbackend/src/test/java/com/idayam/m<M>/workflow/<Flow>WriteThroughTest.java`:

```java
@ExtendWith(MockitoExtension.class)
class AcmeSupplierWriteThroughTest {
    @Mock private JdbcTemplate jdbc;
    @InjectMocks private AcmeSupplierWriteThrough listener;

    @Test
    void writes_only_when_workflow_matches() {
        WorkflowInstance i = mock(WorkflowInstance.class);
        when(i.getWorkflowCode()).thenReturn("M10.TankerInbound"); // WRONG workflow
        listener.onTransition(i, "PARKING", Map.of());
        verifyNoInteractions(jdbc); // did not write
    }

    @Test
    void writes_on_matching_workflow_and_state() {
        WorkflowInstance i = mock(WorkflowInstance.class);
        when(i.getWorkflowCode()).thenReturn("ACME.SupplierOnboarding");
        when(i.getCurrentState()).thenReturn("KYC_PENDING");
        listener.onTransition(i, "DRAFT", Map.of("acme:supplier-create", Map.of("name","Test")));
        verify(jdbc, times(1)).update(anyString(), any(), any(), any(), any());
    }
}
```

### 6. Coverage matrix must move from 0 to 100% for the flow's entities

After committing all five tests:

```bash
python3 scripts/coverage-matrix.py
```

Every entity in this customer flow should show 🟢 covered. Every workflow should show 100% pages tested. If any shows 🔴, a test is missing.

## Reference implementation

The V78 loading-workflow SQL (`M10.TankerOutbound` / `M10.PackLoading` / `M10.FGLoading`) that Dhivya wrote is the canonical example of a customer flow done right. Nine pages, three workflows, ~54 lines of very dense SQL. Read it before writing your own.

Standards: `docs/architecture/ENGINEERING-STANDARDS.md`
Coverage matrix: `scripts/coverage-matrix.py`
AI-PDLC lifecycle: `docs/architecture/AI-PDLC.md`
