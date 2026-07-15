---
description: The substrate itself is missing a capability that a customer flow needs. Ship the capability as platform code so every future flow can use it. Use this only when new-entity + new-workflow + new-page + new-listener + new-adapter genuinely cannot express the requirement.
---

# Substrate extension — when the platform itself needs to grow

## The gate — answer these BEFORE invoking this skill

Every question below must be a "no" for you to legitimately need a substrate extension. If ANY answer is "yes", use the linked skill instead.

1. Can the requirement be expressed as `entity_registry` + `entity_field_registry` rows? → **use `/new-entity`**
2. Can it be a `workflow_definition` JSON body? → **use `/new-workflow`**
3. Can it be a `page_definition` + `page_version.body_json`? → **use `/new-page`**
4. Can it be a `WorkflowTransitionListener` implementation? → **use `/new-listener`**
5. Is it an integration with an external system? → **use `/new-adapter`**
6. Can it be composed from existing primitives (`Grid` + `Section` + `KPI` + `LineChart` + `Tabs` + `KanbanView` + `WorkflowStepper` + `Form` + `ListView`)? → **use `/new-page` with composition**
7. Is the "missing capability" actually just an entity that hasn't been registered yet? → **use `/new-entity`**

If any of the above answer "yes", **STOP — you don't need a substrate extension**. Use the appropriate skill instead.

## What genuinely IS a substrate extension

Only these five categories:

### A) A new page-render primitive

Existing primitives can't express what the customer needs. Real examples that would have justified this path:

- `LiveGauge` — a real-time SVG needle bound to a WebSocket stream. `KPI` shows a static number; nothing shows a live gauge.
- `GanttChart` — a swimlane timeline. `Kanban` shows lanes but not durations.
- `MapView` — vehicle-tracker map. No primitive renders a map.

Path: `/new-primitive` skill on `earmsfrontend`.

### B) A new column on a substrate table

Existing columns don't hold the metadata a new feature needs. Real examples:

- `entity_registry.value_column` + `display_column` (added by Dhivya in V7_20) — needed for the generic dropdown-options endpoint.
- `entity_field_registry.visible_when` + `required_when` + `computed_from` (added by Arun in V78) — needed for field-level conditional rules.

Path: SQL migration + probe hook in `M0PageSchemaInitialiser`. See how V7_20 / V78 did it.

### C) A new table under `m0_*` (platform-scope)

You need a whole new substrate concept. Real examples:

- `page_workflow_binding` (V4_1) — needed to bind pages to workflow states with role assignment.
- `ai_agent` (V83, shipped today) — needed because agents are first-class substrate entities.
- `tenant_ai_credential` (V84, shipped today) — needed because customers register their own AI keys.

Path: full SQL migration + `entity_registry` registration + `page_definition` stamping + probe hook.

### D) A new engine capability

The workflow engine, page renderer, or generic entity controller genuinely lacks a feature. Real examples:

- HTTP 409 + `reason` on guard rejection (shipped as fix #11) — WorkflowEngine used to return 200 + `{transitioned:false}` on guard rejection.
- `GET /api/workflow/instances/{id}/available` (shipped as fix #13) — needed so the UI could gate workflow buttons.
- Field-rule evaluator in `EntityFormModal` — needed to consume the V78 columns.

Path: Java changes under `com.idayam.m0.*` ONLY.

### E) A new trigger kind for `ai_agent_binding`

The dispatcher supports `workflow.transition` + `page.action` + `cron`. If you need `sensor.threshold` or `github.pr.opened`, the binding needs a new trigger + a new bridge class.

Path: Java under `com.idayam.ai.bridges.*`.

## The RFC-style proposal doc

Before writing code, write `docs/architecture/RFCS/RFC-<slug>.md`:

```markdown
# RFC: <Short title>

- Status: PROPOSED / ACCEPTED / REJECTED
- Author: <name>
- Date: YYYY-MM-DD
- Reviewers: Arun (required)

## Problem
One paragraph — what customer flow needs this? What can't be expressed today?

## Alternatives considered
Which of the 7 gate questions did you answer, and why doesn't each one work?
Show that you actually tried composition before proposing extension.

## Proposal
The exact substrate change:
- new column? new table? new primitive? new engine method?
- SQL migration Vn body
- Java class + interface signatures
- Frontend changes if any

## Impact
- Which existing pages/workflows/entities are affected?
- Any migration risk?
- Any backward-compat issues?

## Coverage plan
Explicit list of tests to be written per the ENGINEERING-STANDARDS.md coverage requirements.

## Rollback plan
If this ships and it's wrong, how do we get back?
```

**Do not write the code until the RFC is ACCEPTED.** For substrate extensions Arun approves; write the RFC first, get the LGTM in a Slack thread or PR comment, then implement.

## Engineering principles for substrate extensions

Everything from `docs/architecture/ENGINEERING-STANDARDS.md`, plus:

- **Backward compatibility.** Every existing entity, workflow, page must still work after the extension. New column on `entity_registry`? It's nullable with a sensible default.
- **Substrate primitives, not custom.** If your extension adds a table, it's registered in `entity_registry` immediately so the substrate renders CRUD pages for it automatically.
- **Documentation update.** New extension = new section in `docs/architecture/REARCH.md` describing what the substrate now supports.
- **CLAUDE.md update.** If the extension is significant, add a note to `earmsbackend/CLAUDE.md` so future Claude sessions know it exists.
- **Skill update.** If the extension changes how developers should think, update the relevant existing skill (e.g. `/new-entity` should mention the new registry column).

## SonarQube rules that apply

Same as `docs/architecture/ENGINEERING-STANDARDS.md` universal + Java-specific set. Substrate code is held to the strictest standard because it's read by every other code path.

- **S138** method length ≤ 60 (tighter than 80)
- **S3776** cognitive complexity ≤ 10 (tighter than 15)
- **S110** inheritance depth ≤ 3 (tighter than 5) — substrate is composition-first
- **S107** ≤ 4 params — substrate methods must be simple

## Coverage requirements — this is where the bar goes UP

Substrate extensions must ship with:

### 1. Unit tests — 100% line coverage

Not 80%. **100%** on every substrate class. Substrate code runs for every customer flow — untested branches will bite.

### 2. Integration test that proves the OLD behaviour still works

Every substrate extension has a "before" behaviour and an "after" behaviour. Write a test that exercises a page/workflow/entity created BEFORE the extension and asserts it still renders correctly.

### 3. Integration test that proves the NEW behaviour

The extension is worthless if nothing exercises it. Ship a demo page + Playwright walk that uses the new capability end-to-end.

### 4. RFC doc + reasoning archived

The RFC document itself is a coverage artifact — future developers who see the extension need to understand WHY it was added. No RFC → no merge.

### 5. Update `docs/architecture/REARCH.md`

Add a section listing the extension + a one-sentence "when to use this."

## Reference — real substrate extensions to read as models

- **V78** (field rules) — Arun's field-level `visible_when` / `required_when` / `computed_from`. Extension was ONE column added to `entity_field_registry` + a Jest evaluator + `EntityFormModal` integration. Enabled dozens of conditional-field customer flows without ever touching Java per-form.
- **V80** (rich page kit — Tabs, KanbanView, WorkflowStepper, cell renderers) — Arun's 4 new primitives. Each demonstrated the pattern: substrate primitive + `page_element_catalog` row + demo page + Playwright spec.
- **V83** (AI agent substrate — shipped today) — 6 tables + Java interface + dispatcher. Enabled every AI feature to be substrate-registered instead of bespoke.

Standards: `docs/architecture/ENGINEERING-STANDARDS.md`
