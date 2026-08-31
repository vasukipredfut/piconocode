# M10 Start Bay / Bay Complete ↔ Conveyor Dispatch Task — Full Live E2E Capture

Live capture of the complete feature built in this session: `m10.start-bay` auto-creating a
conveyor `dispatch_task` row, a real barcode scan against it, and `m10.bay-complete`
force-closing it. All requests/responses below are **real**, captured against the running
backend (`localhost:8080`) connected to the shared remote MariaDB at `192.187.121.34:3306`,
schema at Flyway version 271 (includes V269 route_id/order_id, V270 status rename to
Open/Completed, V271 type column).

Companion docs: `CONVEYOR_SCANNER_WORKFLOW_GUIDE.md` §4.1 (design reference for this
feature), `CONVEYOR_E2E_TEST_LOG.txt` / `docs/m10/CONVEYOR-LIVE-CAPTURE-2026-08-31.md`
(bare conveyor scan/heartbeat flow, no M10 integration).

## Setup note — why the instance skips straight to LOADING_IN_PROGRESS

`M10.FGLoading` is currently at workflow version 125 on this DB — a real, deeply-evolved
business process (gate pass → vehicle entry → order-sheet upload → loading allocation →
first weighment → ... → gate-pass closure), none of which this feature touches or tests.
Rather than drive ~5 unrelated upstream transitions through their now-heavily-computed page
schemas, the test instance's `current_state` was fast-forwarded directly to
`LOADING_IN_PROGRESS` with one `UPDATE workflow_instance` — the single state
`m10.start-bay`/`m10.bay-complete` actually care about. Everything downstream of that point
(the action invocations, the FSM transition they drive, the dispatch_task lifecycle) is the
real code path, unmodified.

## Step 1 — Create a real workflow instance

```
POST /api/workflow/instances
{"workflowCode":"M10.FGLoading"}
```
**Response `200`:** `{"workflowVersion":125,"instanceId":1838,"currentState":"DRAFT","assignedRole":"","workflowCode":"M10.FGLoading"}`

**Test-seed only** (raw SQL, not part of the feature under test):
```sql
UPDATE workflow_instance SET current_state = 'LOADING_IN_PROGRESS'
WHERE id = 1838 AND tenant_id = 1 AND current_state = 'DRAFT';
-- 1 row updated
```

## Step 2 — `POST /api/m0/actions/m10.start-bay/invoke` (the real "Start Bay" button)

**Request:**
```json
{
  "instanceId": 1838,
  "pageCode": "aria:loading-in-progress-entry",
  "formData": {
    "vehicle_no": "TN99ZZ9999",
    "gpNumber": "GP-1-1838",
    "bay_name": "Bay3",
    "order_sku_line_items": [
      {"route_id": "ROU-E2E-DT", "sku_code": "SKU-E2E-DT", "sku_desc": "E2E Dispatch Task Test SKU", "order_qty": 5}
    ]
  }
}
```
**Response `200`:**
```json
{"ok":true,"message":"bay 'Bay3' started with 1 SKU line(s), total qty 5, workflow state BAY_ASSIGNED","recordId":null,"nextPageState":null,
 "data":{"transitioned":true,"fromState":"LOADING_IN_PROGRESS","toState":"BAY_ASSIGNED","bayName":"Bay3","totalQuantity":5,"lineCount":1}}
```
Real FSM transition fired (`LOADING_IN_PROGRESS` → `BAY_ASSIGNED`), same as every other Start Bay call.

**Verification — the auto-created `dispatch_task`:**
```
POST /api/m0/datasource/query
{"spec":{"entity":"DispatchTask","filter":{"field":"workflow_instance_id","op":"eq","value":1838}}}
```
```json
{"id":3,"tenant_id":1,"workflow_code":"M12.ConveyorTest","type":"Loading","workflow_instance_id":1838,
 "bay_code":"Bay3","route_id":"ROU-E2E-DT","order_id":null,
 "task_code":"DT-1838-Bay3-SKU-E2E-DT-1788165494339","order_ref":null,"sku_code":"SKU-E2E-DT",
 "vehicle_no":null,"gp_number":"GP-1-1838","target_qty":5.000,"processed_qty":0.000,"rejected_qty":0.000,
 "status":"Open","rule_set_id":null,"started_at":null,"completed_at":null}
```
Confirms every field from CONVEYOR_SCANNER_WORKFLOW_GUIDE.md §4.1's table: `workflow_code` is the fixed
`M12.ConveyorTest` constant (not `M10.FGLoading`), `type='Loading'`, `status='Open'`, `route_id`/`order_id`
from the SKU line, `gp_number` from `formData.gpNumber`, synthetic `task_code`.

## Step 3 — Real barcode scan against the auto-created task (tokenless conveyor endpoint)

```
POST /api/conveyor/scan
{"workflowCode":"M12.ConveyorTest","stepCode":"SCANNING","bayCode":"Bay3","barcode":"BC-E2E-DT-1",
 "taskId":null,"scannerIntegrationId":null,"sku":"SKU-E2E-DT","orderRef":null,"qty":2}
```
**Response `200`:**
```json
{"outcome":"ACCEPT","reason":null,
 "dispatchTask":{"id":3,"target_qty":5.000,"processed_qty":2.000,"status":"Open","...":"..."},
 "plcSignalOk":null}
```
The scanner resolved the task purely by `(workflow_code=M12.ConveyorTest, bay_code=Bay3)` — it has no
knowledge of the M10 workflow instance at all, exactly as designed (the two systems only share the
`dispatch_task` row). `plcSignalOk: null` since no PLC integration is bound for this bay.

## Step 4 — Stock setup (prerequisite for Bay Complete's FEFO deduction, unrelated to this feature)

```
POST /api/m0/entities/StockBalance
{"item_code":"SKU-E2E-DT","bin_code":"BIN-E2E-DT","qty_on_hand":100,"uom":"KG"}
```
`200` → `{"ok":true,"id":242}`

## Step 5 — `POST /api/m0/actions/m10.bay-complete/invoke` (the real "Bay Complete" button)

**Request:**
```json
{
  "instanceId": 1838,
  "formData": {
    "order_sku_line_items": [
      {"route_id": "ROU-E2E-DT", "sku_code": "SKU-E2E-DT", "loading_qty": 5, "balance_qty": 0}
    ]
  }
}
```
**Response `200`:**
```json
{"ok":true,"message":"1 bay line(s) closed, processed 5 total — pick another bay or press Completed",
 "data":{"totalProcessed":5,"closed":[{"routeId":"ROU-E2E-DT","processedQuantity":5,"skuCode":"SKU-E2E-DT"}]}}
```
Stock deducted (FEFO, one `m5_stock_movement` CONSUME row against the seeded lot), `loading_progress_detail_bay`
row closed. Note the operator submitted `loading_qty: 5` (the full planned amount) even though the scanner had
only actually counted `2`.

**Verification — the force-closed `dispatch_task`:**
```json
{"id":3,"workflow_code":"M12.ConveyorTest","type":"Loading","bay_code":"Bay3",
 "target_qty":5.000,"processed_qty":2.000,"rejected_qty":0.000,
 "status":"Completed","completed_at":"2026-08-31T08:39:40.000+00:00"}
```
**`status` is `Completed` and `completed_at` is stamped, even though `processed_qty` (2) never reached
`target_qty` (5)** — exactly the documented behavior: Bay Complete force-closes regardless of scan count,
and never overwrites `processed_qty` with the operator's `loading_qty`. This is the gap flagged earlier in
this session, now closed and proven live.

## Step 6 — Scan against the now-completed task is rejected

```
POST /api/conveyor/scan
{"workflowCode":"M12.ConveyorTest","stepCode":"SCANNING","bayCode":"Bay3","barcode":"BC-E2E-DT-2",
 "sku":"SKU-E2E-DT","qty":1}
```
**Response `200`:** `{"outcome":"REJECT","reason":"NO_ACTIVE_TASK","dispatchTask":null,"plcSignalOk":null}`

Confirms `DispatchTaskService.listActiveForBay`'s `status = 'Open'` filter correctly excludes the
now-`Completed` task — a scanner physically still sitting at Bay3 after the operator closes the bay
can no longer accept scans against the old task.

## Result

Every step of the feature built across this session — schema (V269/V270/V271), `M10StartBayActionHandler`'s
`dispatch_task` auto-creation, and `M10BayCompleteActionHandler`'s force-close — is verified working
end-to-end against the real action endpoints, the real FSM, and the real (tokenless) conveyor scan endpoint,
on the live shared dev database.

## Migration note (found and fixed during this capture)

The first attempt at V270 failed on this DB with:
```
Column 'status' has duplicated value 'COMPLETED' in ENUM
```
`dispatch_task` uses `COLLATE=utf8mb4_uca1400_ai_ci` (case-insensitive) — a single `ENUM(...)` definition
cannot declare both `'COMPLETED'` and `'Completed'` at once, since they're the same value under that
collation. The widen/narrow pattern used successfully for other columns (V210, V269) doesn't work for a
same-word-different-case rename. Fixed by routing the rename through a temporary `VARCHAR` shadow column
(reading/writing string values never triggers the enum-definition-uniqueness check) — see the comment in
`V270__dispatch_task_status_open_completed.sql` for the corrected approach. The failed migration attempt's
`flyway_schema_history` row was deleted (equivalent to `flyway repair`) before retrying; the failed `ALTER`
had not partially applied (single-statement DDL), so no data was affected.
