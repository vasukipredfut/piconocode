# Building a flow as substrate — team playbook

**When a use-case like the M10 dispatch flow comes in, you almost never write code.** You add entity **fields**, register them, author **page bodies**, and draw the **workflow**. All data. This doc is the copy-paste recipe for each kind of change, in **both** forms: raw **SQL** (migration) and the **API** (Page/Workflow Designer + Designer endpoints).

Worked example for everything below: the M10 outbound dispatch flow, shipped as migrations `V127` (front-gate form), `V129` (dispatch/QC/weight fields), `V130` (order-line fields), `V131` (loading page). The consolidated runnable version is [`M10-OUTBOUND-DISPATCH.sql`](./M10-OUTBOUND-DISPATCH.sql).

---

## The one rule: do you need code?

| The change is… | Where it lands |
|---|---|
| A field, a column on a list, a form input, a dropdown, a filter | **Data** — entity field + registry row |
| Show/hide a field by variant/state | **Data** — `visible_when` (SpEL) |
| A page (list / form / dashboard / step) | **Data** — a `page_version.body_json` |
| A workflow, its states, transitions, guards, approvers | **Data** — `workflow_definition` |
| State → denormalised row (e.g. gate_pass mirror) | **Data** — `m0_workflow_projection` |
| Print a voucher / label | **Data** — `print_template` (routes to an M11 printer) |
| **A genuinely new *browser interaction*** — camera-decode, live device feed, canvas signature | **Code** — a generic primitive (`page_element_catalog` row + React), never customer-scoped |

**Not code, even though it looks like it:** a QR/barcode **scanner** is a keyboard-wedge device — it types the scanned value into the focused field. "Scan QR / Label" is a plain text `FormField`. No camera, no primitive.

The 9-step M10 outbound flow → substrate, end to end:

| Step | Built from (existing primitives) |
|---|---|
| ① Front Gate — Create Gate Pass | `Form` + `WorkflowActionButton` (start `M10.*`) · Print Gate Pass = `print_template` |
| ② Parking Bay In / ⑤ Out — Scan QR | `FormField` (scanner wedge) + `WorkflowActionButton` |
| ③ Dispatch Entry (3 variants) | `Form` with `visible_when` variant sections |
| ④ QC Verify (approve/reject + reason) | `WorkflowActionButton` (transition + guard) · reason via `visible_when` |
| ⑥ First Weight | `Form` (or `WeighbridgeLive` if you read a live bridge) |
| ⑦ Loading (Route ID → Fetch → Offer → Start) | `ListView` (route filter + inline-edit Offer) + `WorkflowActionButton` — see `V131` |
| ⑧ Bay Dashboard | `KanbanView` grouped by `loading_bay` + progress cell renderers |
| ⑨ Loading Progress + Stop | `ListView` + `rowAction` (transition) + `EntityFormModal` (Dispatch Qty) |

---

## Recipe 1 — Add a field to an entity

**SQL**
```sql
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS transport_name VARCHAR(128) NULL;

INSERT INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'GatePass','transport_name','Transport Name','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry
                    WHERE entity_code='GatePass' AND field_code='transport_name');
```

**API** (Designer does the ALTER + registry row atomically)
```bash
curl -X POST -H "Authorization: Bearer $JWT" -H 'content-type: application/json' \
  -d '{"field_code":"transport_name","display_label":"Transport Name","data_type":"STRING","widget_kind":"text"}' \
  http://localhost:8080/api/m0/designer/entities/GatePass/fields
```
`data_type`: STRING · DECIMAL · INTEGER · DATE · DATETIME · BOOLEAN.
`widget_kind`: text · textarea · number · currency · select · multiselect · checkbox · date · datetime · entity-ref · email · password · tel.

---

## Recipe 2 — Variant / conditional field (show only when…)

Set `visible_when` (SpEL over the submitted form bag `#form.<field>`):

```sql
-- Broker only for the Tanker-Outbound variant
UPDATE entity_field_registry
   SET visible_when = '#form.flow_variant_code == ''M10.TankerOutbound'''
 WHERE entity_code='GatePass' AND field_code='broker_name';

-- Reject reason only when the QC decision is REJECTED
UPDATE entity_field_registry
   SET visible_when = '#form.qc_status == ''REJECTED'''
 WHERE entity_code='GatePass' AND field_code='qc_reject_reason';
```
Also available: `required_when` (same syntax) and `computed_from`. In the API, set them via the Field Designer, or through the **UI Rule Builder** (`/designer` → field → Rules).

---

## Recipe 3 — Author a LIST page (with a filter + editable column)

A list is `{kind:'list', dataSource, columns, filters, rowActions}`. Clear `columns` (or omit) and ListView auto-derives up to 8 from the registry. Filters give you "fetch by X":

```sql
UPDATE page_version pv
SET pv.body_json = JSON_OBJECT(
  'kind','list', 'title','Order Details',
  'dataSource', JSON_OBJECT('type','entity','entity','SalesOrderLine'),
  'filters', JSON_ARRAY(JSON_OBJECT('field','route_id','label','Route ID',
                                    'type','text-contains','applyTo',JSON_ARRAY('route_id'))),
  'columns', JSON_ARRAY(
     JSON_OBJECT('field','item_name','label','SKU Name'),
     JSON_OBJECT('field','qty_ordered','label','Qty','render','number'),
     JSON_OBJECT('field','offer','label','Offer')),   -- click a cell/row to edit -> PUT saves
  'pageSize', 25)
WHERE pv.page_id=(SELECT id FROM page_definition WHERE code='pico:loading-order-sheet')
  AND pv.status='PUBLISHED';
```
- Filter types: `text-contains` (with `applyTo`) · `multi-select` · `date-range` · `range` · `entity-picker` · `bool-toggle`.
- **Editing a cell** (the "Offer Selection → Update"): inline-edit is built in — hover a cell → pencil → save `PUT /api/m0/entities/{code}/{id}`. A row click opens the full `EntityFormModal`.

**API** — stamp a fresh list page:
```bash
curl -X POST -H "Authorization: Bearer $JWT" -H 'content-type: application/json' \
  -d '{"code":"pico:order-lines","title":"Order Details","category":"M10","entity":"SalesOrderLine","kind":"list"}' \
  http://localhost:8080/api/m0/designer/pages
# then edit body via  PUT /api/m0/pages/{code}   (draft) + POST /api/m0/pages/{code}/publish
```

---

## Recipe 4 — Author a FORM / workflow-step page

A layout tree of `Section → Form(fields) → WorkflowActionButton`. The button collects every `input[name]` in scope and fires the transition (auto-starts the instance if the page is the entry point). Example = the front-gate intake (`V127`):

```sql
-- body (abbreviated): Grid > Section > Form(bindings.fields:[{name:'vehicleNo',...}]) > WorkflowActionButton
--   WorkflowActionButton props: {action:'workflow.transition', workflowCode:'M10.TankerInbound',
--                                targetState:'GATE_PASS_CREATED', trigger:'page.submitted'}
```
Field names are the workflow **data-bag** keys; the projection maps them to columns (Recipe 6). See `V127__front_gate_create_form.sql` for the full JSON.

**API**: same `POST /api/m0/designer/pages` then `PUT` the body — or just draw it in **Page Designer** (`/designer`).

---

## Recipe 5 — Workflow: states, transitions, guards

Draw it in **Workflow Designer** (`/workflow-designer`) — it writes a `workflow_definition` row. Or SQL:
```sql
-- body_json = {code, version, initialState, states:[{code}], transitions:[{from,to,page,guard,role}]}
-- guard is SpEL, e.g.  #form.qc_status == 'APPROVED'
-- reject branch: transition from QC_VERIFY to DISPATCH_VEHICLE_ENTRY (vehicle re-appears for correction)
```
A page binds to a workflow automatically when its `WorkflowActionButton` names a `workflowCode`, or via `GET /api/m0/pages/{code}/workflow`.

---

## Recipe 6 — Projection: mirror workflow state onto a row

`m0_workflow_projection` makes any workflow write a denormalised row (e.g. `gate_pass`) on every transition — state columns, generated number, field-map, terminal timestamp. All config:
```sql
UPDATE m0_workflow_projection
   SET field_map_json = '{"vehicle_no":["vehicleNo"],"broker_name":["brokerName","broker_name"], ... }'
 WHERE tenant_id=1 AND workflow_code IN ('M10.TankerOutbound','M10.PackLoading','M10.FGLoading');
```
`field_map_json` = `{ "<column>": ["<possible form-bag keys>"] }`. First non-blank wins.

---

## Recipe 7 — Print a Gate Pass / label

`print_template` row (target table + JSON voucher spec, or `output_kind='label'` + ZPL). Rendered by `GET /api/print/{code}/{id}`; `POST /api/print/{code}/{id}/send` routes ZPL to the printer configured as an **M11 integration**. Invoke from any page/workflow. (Gate-pass + GRN templates already seeded.)

---

## Gotchas (bit us — save yourself the hour)

1. **Redis page cache.** The GET caches published bodies at `m0:page:<tenant>:<code>` (1h). Editing `page_version.body_json` by SQL does **not** invalidate it. On a running instance:
   `redis-cli --scan --pattern 'm0:page:*' | xargs redis-cli DEL`  (or use publish/rollback, or restart). Fresh DBs have no cache — clean.
2. **Multiple PUBLISHED versions.** The GET serves `status='PUBLISHED'` and a page can have more than one. Update by `page_id ... AND status='PUBLISHED'`, **not** just `current_version_id`.
3. **Field name = column name.** Keep the form field `name` matching the column (or add it to the projection field-map). camelCase-vs-snake_case mismatches make modal view/edit silently no-op.
4. **Don't seed customer rows in migrations.** Fields/pages/workflows = yes. A customer's actual orders/vehicles = onboarding API or their own data load, never a `V*.sql`.
5. **`JSON_VALID` CHECK** on `page_version.body_json` — bad JSON fails the insert loudly. Strip literal newlines from string values.

---

## The two ways in, summarised

| | SQL (migration) | API / Designer |
|---|---|---|
| Add field | `ALTER` + `entity_field_registry` insert | `POST /api/m0/designer/entities/{code}/fields` |
| Stamp page | `INSERT/UPDATE page_version` | `POST /api/m0/designer/pages` then `PUT /api/m0/pages/{code}` + publish |
| Conditional field | set `visible_when` | Field Designer / Rule Builder (`/designer`) |
| Workflow | `INSERT workflow_definition` | Workflow Designer (`/workflow-designer`) |
| Projection | `UPDATE m0_workflow_projection` | (SQL / onboarding) |

Use SQL when you want it version-controlled as a migration (ships to every tenant on deploy). Use the API/Designer for live, per-tenant authoring by an admin — same result, different door.
