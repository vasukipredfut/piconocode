---
description: Which skill should I invoke for this task? Walk through the decision tree in ≤ 60 seconds. Use this when you're unsure whether your requirement is data (SQL migration), a listener, an adapter, a primitive, or a substrate extension.
---

# Decision tree — which skill to invoke

Start here whenever you're unsure. Answer each question truthfully; the tree lands you on the exactly-one-correct skill.

## The tree

```
Question 1 — Is this a NEW BUSINESS DATA CONCEPT (a new "thing" the customer tracks)?
├── YES → Is it a state machine (has states, transitions, roles)?
│         ├── YES → /new-workflow
│         └── NO  → /new-entity
└── NO  → continue

Question 2 — Is this a NEW USER-FACING SCREEN?
├── YES → Can you express it with existing primitives in body_json?
│         (Grid, Section, KPI, LineChart, Tabs, KanbanView, WorkflowStepper,
│          Form, ListView, DetailView, LiveGauge, cell renderers)
│         ├── YES → /new-page
│         └── NO  → Is a new PAGE-RENDER PRIMITIVE genuinely needed?
│                   ├── YES → /new-primitive  (frontend skill)
│                   └── NO  → back to Q2 — try harder to compose from existing
└── NO  → continue

Question 3 — Does a WORKFLOW TRANSITION need to trigger a side-effect
             (write to another table, notify, log, denormalize a state)?
├── YES → /new-listener
└── NO  → continue

Question 4 — Do you need to talk to an EXTERNAL SYSTEM
             (Slack, WhatsApp, Tally, EPFO, GSTN, weighbridge, banking, ...)?
├── YES → /new-adapter
└── NO  → continue

Question 5 — Is the SUBSTRATE ITSELF missing a capability?
├── Column on entity_registry / page_definition / workflow_definition?
├── A whole new substrate table?
├── An engine feature (workflow, page renderer, generic entity controller)?
├── A new page-render primitive that unlocks many future flows?
├── YES → /substrate-extension  (RFC required first)
└── NO  → continue

Question 6 — Are you sure this is an EARMS problem at all?
Sometimes the answer is: the customer's expectation is out of scope for an ERP,
or the requirement is a data-entry hygiene issue, not a code change.
```

## Cheat sheet — the 3-second version

- **Data** (new "thing") → `/new-entity` or `/new-workflow`
- **UI screen** (assembly of existing primitives) → `/new-page`
- **UI shape** (something no primitive can render) → `/new-primitive` (frontend)
- **Side-effect on transition** → `/new-listener`
- **External system integration** → `/new-adapter`
- **Substrate itself needs a new capability** → `/substrate-extension`
- **Just checking what should trigger** → `/substrate-check` before commit
- **Multi-artifact customer onboarding** → `/customer-flow`

## Anti-cheat — when the answer is "none of the above"

If none of the 6 skills fit, the honest possibilities are:

1. **The requirement is out of scope.** Not every customer request is an ERP feature. Bring it back to Arun to decide scope.
2. **The requirement is a bug in existing code.** Then it's not a "new feature" — it's a bug fix, which follows the normal fix pattern.
3. **You haven't actually tried composition.** Go back to Question 2 and honestly try to combine existing primitives.
4. **The requirement genuinely requires a substrate extension, but you don't want to write the RFC.** Do the RFC. It takes 30 minutes and prevents wrong-shape code.

## Real examples — the tree applied

### "Add a return-stock inbound workflow for the customer"
- Q1: NEW business concept? YES.
- Has states, transitions, roles? YES.
- **→ `/new-workflow`**

### "Customer wants a page showing their approval inbox with kanban board"
- Q1: New data concept? NO — it's a view over existing workflow instances.
- Q2: New screen? YES.
- Existing primitives? `KanbanView` (shipped in V80) + `WorkflowInstance` entity + a body_json binding covers it.
- **→ `/new-page`**

### "On tanker-inbound QC approval, send SMS to the fleet manager"
- Q1: New data? NO.
- Q2: New screen? NO.
- Q3: Transition side-effect (notification)? YES.
- Q4: External system? Actually YES too — SMS gateway.
- **Both apply**: `/new-listener` for the trigger, invoke the existing `Msg91SmsAdapter` (M11). No new adapter needed.

### "Customer wants a live boiler pressure gauge on the dashboard"
- Q1: New data? YES — sensor readings are a new entity.
- **First** → `/new-entity` for `Sensor` + `SensorReading` tables (already scaffolded in V83)
- Q2: New screen? Existing `KPI` shows static; no primitive shows live-tick.
- **Then** → `/new-primitive` for `LiveGauge`
- **Then** → `/new-page` composing `LiveGauge` on a dashboard body_json
- Total: 3 skills. Each does its part.

### "Integrate with the customer's Tally installation for CoA sync"
- Q1: Data concept? Not yet — Tally maps to existing entities.
- Q4: External system? YES.
- **→ `/new-adapter`** — build a Tally sync adapter. Existing `TallyZplNicAdapter` is a partial reference.

### "The entity_registry doesn't have a way to say 'this entity is read-only for role X'"
- Q1-Q4: No.
- Q5: Substrate missing a column? YES.
- **→ `/substrate-extension`** — RFC + new column + backward compat + tests.

## The commit gate remembers your choice

Whatever skill you invoke, the pre-commit hook enforces the rules for that path. If you claimed you were doing `/new-entity` but committed a `@Entity` class, the hook catches it.

## Reference

- All 7 skills live in `.claude/skills/` — Claude Code auto-lists them.
- Standards: `docs/architecture/ENGINEERING-STANDARDS.md`
- PDLC lifecycle: `docs/architecture/AI-PDLC.md`
- Anti-pattern catalogue: `docs/architecture/CODE-REVIEW-develope-2026-07-11.md`
