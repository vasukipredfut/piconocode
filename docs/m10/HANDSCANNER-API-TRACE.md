# M10 Hand Scanner — `POST /api/handscanner/scan-gate-pass` API Trace

Live capture of the hand-scanner endpoint (`com.aria.m10.handscanner.controller.HandScannerController`
+ `HandScannerService`), run directly against the local dev backend (`localhost:8080`),
backed by the shared dev MariaDB (`earms` schema). No auth header — this endpoint is permit-all
(tokenless device), see `WebSecurityConfig`'s `/api/handscanner/**` entry.

Request shape is always the same:
```
POST /api/handscanner/scan-gate-pass
Content-Type: application/json

{"gpNumber": "GP-1-1493"}
```

This is the ONE call `raspberry-pi-client/hand_scanner.py` makes per QR scan — all orchestration
(gate pass lookup → workflow instance lookup → order/SKU dispatch lookup → bay assignment attempt)
happens server-side in this single round trip.

## Case 1 — happy-path lookups, bay assignment attempted but rejected by real business validation

Gate pass `GP-1-1493` / vehicle `TN28AM6489`, a genuine live instance (id `1493`) sitting at
`LOADING_IN_PROGRESS` at capture time (found via `GET /api/workflow/queue-for-page?pageCode=aria:loading-in-progress-entry`).

**Request**
```json
{"gpNumber": "GP-1-1493"}
```

**Response** `200`
```json
{
  "gatePass": {
    "id": 459, "gp_number": "GP-1-1493", "vehicle_no": "TN28AM6489",
    "status": "LOADING_IN_PROGRESS", "current_step": "LOADING_IN_PROGRESS",
    "workflow_instance_id": 1493, "flow_variant_code": "M10.FGLoading",
    "driver_name": "Durai", "driver_phone": "9876543456", "...": "(full gate_pass row)"
  },
  "workflowInstance": {
    "id": 1493, "workflow_code": "M10.FGLoading", "current_state": "LOADING_IN_PROGRESS",
    "status": "OPEN", "assigned_role": "WeighmentOfficer", "business_ref": "TN28AM6489"
  },
  "orderDispatchDetails": [
    { "id": 279, "order_id": "ORD107126", "sku_code": "SKU1436", "sku_desc": "REF SUNFLOWER OIL",
      "route_id": "ROU028934", "order_qty": 12000, "remaining_qty": 0, "status": "Dispatched",
      "vehicle_no": "TN28AM6489", "...": "..." },
    { "id": 280, "order_id": "ORD107520", "sku_code": "SKU1365", "sku_desc": "SUPER OLIEN OIL",
      "route_id": "ROU029019", "order_qty": 6000, "remaining_qty": 0, "status": "Assigned All",
      "vehicle_no": "TN28AM6489", "...": "..." }
  ],
  "bayAssignment": {
    "attempted": true,
    "bayName": "Bay5",
    "ok": false,
    "message": "order_sku_line_items[0] (SKU1436) needs order_qty > 0",
    "data": null
  }
}
```

**Reading this result**: every lookup succeeded — gate pass, workflow instance, 11 historical
`m5_order_dispatch_detail` rows for this vehicle (multiple CSV re-imports of the same two order
lines over time — a pre-existing data-quality quirk of that table, not something this endpoint
introduces). `pickFreeBay()` correctly found `Bay5` free (Bay1-4 were occupied by other live
instances at capture time — real production data, not a fixture). The bay-start was then
**correctly rejected**, because it went through the exact same validation as the real "Start Bay"
button (`ActionDispatcher.dispatch("m10.start-bay", ...)` → `M10StartBayActionHandler`) — this
vehicle's order lines have `remaining_qty: 0` (already fully dispatched historically), so
`order_qty > 0` fails, same as it would for a human clicking "Start Bay" on this same data. This
confirms the endpoint reuses production validation rather than bypassing it.

## Case 2 — gate pass exists, workflow not at LOADING_IN_PROGRESS (bay assignment not attempted)

Gate pass `GP-1-1624` — the same one from `docs/m10/FGLOADING-E2E-API-TRACE.md`, now sitting at
its terminal state.

**Request**
```json
{"gpNumber": "GP-1-1624"}
```

**Response** `200` (trimmed)
```json
{
  "gatePass": { "status": "GATE_PASS_OUT_CLOSED", "...": "..." },
  "workflowInstance": {
    "id": 1624, "workflow_code": "M10.FGLoading",
    "current_state": "GATE_PASS_OUT_CLOSED", "status": "CLOSED",
    "assigned_role": null, "business_ref": null
  },
  "orderDispatchDetails": [],
  "bayAssignment": {
    "attempted": false,
    "reason": "workflow is not ready for bay assignment (current state: GATE_PASS_OUT_CLOSED, expected LOADING_IN_PROGRESS)"
  }
}
```

## Case 3 — unknown gp_number

**Request**
```json
{"gpNumber": "GP-DOES-NOT-EXIST"}
```

**Response** `200`
```json
{"error": "gate pass not found: GP-DOES-NOT-EXIST"}
```

## Case 4 — missing gpNumber

**Request**
```json
{}
```

**Response** `200`
```json
{"error": "gpNumber is required"}
```

## Not yet exercised live (needs matching test data)

- **`workflowInstance: null`** — a gate pass whose `workflow_instance_id` is still NULL (normal
  right after plain gate-pass creation, before a workflow instance starts — see
  `WorkflowEngineService.plainGatePassQueueItems`). `bayAssignment.reason` would read
  *"gate pass has no linked workflow instance yet"*.
- **`bayAssignment.ok: true`** — needs a vehicle at `LOADING_IN_PROGRESS` whose
  `m5_order_dispatch_detail` rows still have `remaining_qty > 0` (or `order_qty` if
  `remaining_qty` is absent) and a free bay. None of the currently-open `M10.FGLoading` instances
  in the dev DB had remaining quantity at capture time — every one checked had already been fully
  dispatched historically.
- **No free bay** (`"no free bay available (Bay1..Bay12 all occupied)"`) — would need all 12 bays
  simultaneously open, unlikely in the current dev data.

## How to test this yourself

1. Build + run the backend (see root `CLAUDE.md` "Build + Run").
2. Sign in as any user with a Dispatch-adjacent role to get a bearer token (only needed for the
   lookups below — `/api/handscanner/**` itself takes no auth):
   ```
   POST /api/auth/signin  {"username":"2003","password":"Test@1234"}
   ```
3. Find a **currently live** `M10.FGLoading` instance at `LOADING_IN_PROGRESS` (these change as
   people work the dev DB, so re-run this rather than reusing the ids/gp numbers above):
   ```
   GET /api/workflow/queue-for-page?pageCode=aria:loading-in-progress-entry
   Authorization: Bearer <token>
   ```
   Take any row's `gpNumber`.
4. Call the endpoint under test with no auth header at all:
   ```
   POST /api/handscanner/scan-gate-pass
   Content-Type: application/json

   {"gpNumber": "<gpNumber from step 3>"}
   ```
5. To exercise the Pi client itself instead of raw `curl`: copy
   `raspberry-pi-client/hand_scanner_config.example.json` to `hand_scanner_config.json`, drop
   `scannerIp`/`scannerPort` to run in stdin dev-mode, then:
   ```
   python raspberry-pi-client/hand_scanner.py
   ```
   and type a `gpNumber` at the terminal — it POSTs it and logs the parsed result the same way
   `log_scan_result()` does.
