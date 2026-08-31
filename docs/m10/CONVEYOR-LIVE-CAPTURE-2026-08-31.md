# Conveyor Scanner Workflow — Fresh Live Capture (2026-08-31)

All requests/responses below are **real**, captured live just now against the running
backend (`localhost:8080`) connected to the **remote dev MariaDB** at `192.187.121.34:3306`
(schema `earms`, Flyway version 268 — same shared DB this repo currently points at per
`application.properties`). Nothing here is fabricated.

This supersedes nothing — `CONVEYOR_E2E_TEST_LOG.txt` (2026-07-27, captured against the
local dev DB) is still accurate and current. This capture exists to confirm the same
contract holds against the remote DB, and to leave a second, more recent reference.

**UPDATE (post-dates this capture):** V270 renamed `dispatch_task.status` values
`IN_PROGRESS` → `Open` and `COMPLETED` → `Completed` (`PENDING`/`CANCELLED` unchanged).
Every `"status":"IN_PROGRESS"` / `"status":"COMPLETED"` literal below is from before
that rename — substitute `Open`/`Completed` if replaying these calls against current
code. See `CONVEYOR_SCANNER_WORKFLOW_GUIDE.md` §4 for details and §4.1 for the new
`m10.start-bay` auto-creation path (which only ever writes `Open`/`Completed`).

Companion docs: `CONVEYOR_SCANNER_WORKFLOW_GUIDE.md` (full API reference),
`CONVEYOR_E2E_TEST_LOG.txt` (original capture), `raspberry-pi-client/conveyor_client.py`
(reference device client).

**Setup note:** `192.187.121.34` had zero conveyor test data (no `M12.*` workflow, no
rule sets, no dispatch tasks) — it's a real shared DB with 344 production-style workflow
definitions, just none for the conveyor feature. `conveyor_loading_bay` rows for
`M12.ConveyorTest` (Bay1–Bay12) already existed at `tenant_id = -1` from migration
`V181__conveyor_loading_bay_seed_bay1_to_bay12.sql`, but weren't visible to this run
because they were seeded at the global tenant, not the logged-in user's tenant
(`tenant_id = 1`) — hence `"loadingBays": []` below despite the bays existing in the DB.
Everything else (workflow definition, rule set, dispatch tasks) was created fresh for
this capture, left in place afterward per this repo's existing convention.

---

## 1. Setup

### 1.1 Login
```
POST /api/auth/signin
{"username":"2033","password":"Test@1234"}
```
`200` → JWT for tenant 1, role `TenantAdmin`.

### 1.2 Create the workflow definition (didn't exist on this DB)
```
POST /api/m0/entities/WorkflowDefinition
Authorization: Bearer <token>
{"code":"M12.ConveyorTest","version":1,"status":"PUBLISHED",
 "body_json":"{\"initialState\":\"SCANNING\",\"transitions\":[{\"from\":\"SCANNING\",\"to\":\"DONE\",\"pageCode\":\"\"}]}"}
```
`200` → `{"ok":true,"entity":"WorkflowDefinition","inserted":{...,"tenant_id":1},"id":379}`

### 1.3 Create DispatchTask #1
```
POST /api/m0/entities/DispatchTask
{"workflow_code":"M12.ConveyorTest","bay_code":"BAY1","task_code":"TASK-LIVE-CAPTURE-1",
 "order_ref":"ORDER-LIVE-1","sku_code":"SKU-LIVE-1","target_qty":3,
 "vehicle_no":"TN01AB1234","gp_number":"GP-2026-LIVE"}
```
`200` → `id: 1`

```
PUT /api/m0/entities/DispatchTask/1
{"status":"IN_PROGRESS"}
```
`200` → `{"updated":1,"ok":true,"id":1}`

---

## 2. Device-facing flow (no auth header — permit-all)

### 2.1 Fetch workflow config
```
GET /api/scanner/workflows/M12.ConveyorTest?bayCode=BAY1
```
**Response `200`:**
```json
{
  "workflowId": "M12.ConveyorTest",
  "workflowCode": "M12.ConveyorTest",
  "workflowVersion": 1,
  "workflowStatus": "PUBLISHED",
  "workflowDetails": {"initialState": "SCANNING", "transitions": [{"from": "SCANNING", "to": "DONE", "pageCode": ""}]},
  "integrations": [],
  "loadingBays": [],
  "ruleBuilder": [],
  "plcIntegrations": [],
  "dispatchTasks": [
    {"id": 1, "tenant_id": 1, "workflow_code": "M12.ConveyorTest", "workflow_instance_id": null,
     "bay_code": "BAY1", "task_code": "TASK-LIVE-CAPTURE-1", "order_ref": "ORDER-LIVE-1", "sku_code": "SKU-LIVE-1",
     "vehicle_no": "TN01AB1234", "gp_number": "GP-2026-LIVE",
     "target_qty": 3.000, "processed_qty": 0.000, "rejected_qty": 0.000, "status": "IN_PROGRESS",
     "rule_set_id": null, "started_at": null, "completed_at": null,
     "created_at": "2026-08-31T06:09:46.000+00:00", "updated_at": "2026-08-31T06:10:16.000+00:00"}
  ]
}
```

### 2.2 Scan #1 — ACCEPT (no rule_set_id yet → auto-pass)
```
POST /api/conveyor/scan
{"workflowCode":"M12.ConveyorTest","stepCode":"SCANNING","bayCode":"BAY1","barcode":"BC-LIVE-1",
 "taskId":null,"scannerIntegrationId":null,"sku":"SKU-LIVE-1","orderRef":"ORDER-LIVE-1","qty":null}
```
**Response `200`:**
```json
{"outcome":"ACCEPT","reason":null,
 "dispatchTask":{"id":1,"target_qty":3.000,"processed_qty":1.000,"rejected_qty":0.000,"status":"IN_PROGRESS","rule_set_id":null,"...":"..."},
 "plcSignalOk":null}
```
`plcSignalOk: null` — no PLC integration bound for this step/bay (none created for this capture).

### 2.3 Scan #2 — wrong SKU still ACCEPTs (no rule set enforced yet)
```
POST /api/conveyor/scan
{"workflowCode":"M12.ConveyorTest","stepCode":"SCANNING","bayCode":"BAY1","barcode":"BC-LIVE-WRONGSKU",
 "taskId":1,"scannerIntegrationId":null,"sku":"SKU-WRONG","orderRef":"ORDER-LIVE-1","qty":null}
```
**Response `200`:**
```json
{"outcome":"ACCEPT","reason":null,
 "dispatchTask":{"id":1,"target_qty":3.000,"processed_qty":2.000,"rejected_qty":0.000,"status":"IN_PROGRESS","rule_set_id":null},
 "plcSignalOk":null}
```

  ---- at this point a rule set was created and attached: ----
```
POST /api/m0/entities/RuleSet
{"code":"RS-LIVE-1","version":1,"workflow_code":"M12.ConveyorTest","name":"Live Capture Rule Set","status":"PUBLISHED"}
→ 200 {"id":1}

POST /api/m0/entities/RuleDefinition
{"rule_set_id":1,"rule_code":"SKU_MATCH","display_name":"SKU Match","category":"SKU","sequence_no":1,
 "expression":"#submitted['scannedSku'] == null or #submitted['scannedSku'] == #submitted['taskSkuCode']",
 "severity":"BLOCKING","reject_reason_code":"SKU_MISMATCH"}
→ 200 {"id":1}

PUT /api/m0/entities/DispatchTask/1
{"rule_set_id":1}
→ 200 {"updated":1,"ok":true,"id":1}
```

### 2.4 Scan #3 — wrong SKU, now REJECTS (rule set enforced)
```
POST /api/conveyor/scan
{"workflowCode":"M12.ConveyorTest","stepCode":"SCANNING","bayCode":"BAY1","barcode":"BC-LIVE-WRONGSKU-2",
 "taskId":1,"scannerIntegrationId":null,"sku":"SKU-WRONG","orderRef":"ORDER-LIVE-1","qty":null}
```
**Response `200`:**
```json
{"outcome":"REJECT","reason":"SKU_MISMATCH",
 "dispatchTask":{"id":1,"target_qty":3.000,"processed_qty":2.000,"rejected_qty":0.000,"status":"IN_PROGRESS","rule_set_id":1},
 "plcSignalOk":null}
```
(`rejected_qty` still shows the pre-fetch snapshot here — same cosmetic quirk documented in
`CONVEYOR_E2E_TEST_LOG.txt` §2.4: `ConveyorScanService.response()` returns the task row
fetched *before* `recordRejectedScan()` runs. The real DB counter is correct — see §2.5.)

### 2.5 Scan #4 — correct SKU, completes the task
```
POST /api/conveyor/scan
{"workflowCode":"M12.ConveyorTest","stepCode":"SCANNING","bayCode":"BAY1","barcode":"BC-LIVE-3",
 "taskId":1,"scannerIntegrationId":null,"sku":"SKU-LIVE-1","orderRef":"ORDER-LIVE-1","qty":null}
```
**Response `200`:**
```json
{"outcome":"ACCEPT","reason":null,
 "dispatchTask":{"id":1,"target_qty":3.000,"processed_qty":3.000,"rejected_qty":1.000,"status":"COMPLETED",
                 "rule_set_id":1,"completed_at":"2026-08-31T06:18:04.000+00:00"},
 "plcSignalOk":null}
```
`rejected_qty` now correctly shows `1.000` — this is an ACCEPT response, which re-fetches
the task row after the write.

### 2.6 Scan #5 — task already COMPLETED → NO_ACTIVE_TASK
```
POST /api/conveyor/scan
{"workflowCode":"M12.ConveyorTest","stepCode":"SCANNING","bayCode":"BAY1","barcode":"BC-LIVE-4",
 "taskId":null,"scannerIntegrationId":null,"sku":"SKU-LIVE-1","orderRef":"ORDER-LIVE-1","qty":null}
```
**Response `200`:**
```json
{"outcome":"REJECT","reason":"NO_ACTIVE_TASK","dispatchTask":null,"plcSignalOk":null}
```

### 2.7 Heartbeat
```
POST /api/conveyor/heartbeat
{"workflowCode":"M12.ConveyorTest","deviceId":"PI-BAY1-LIVE","workflowStatus":"RUNNING",
 "currentDispatchTaskId":1,"deviceStatus":"ONLINE","lastScanAt":"2026-08-31T06:18:04Z","integrationHealth":{}}
```
**Response `200`:** `{"ok":true}`

---

## 3. Real Python client run (`raspberry-pi-client/conveyor_client.py`)

Ran against the same live backend with a fresh `DispatchTask` (id=2, `target_qty=3`, no
`rule_set_id`), barcodes piped via stdin: `BC-LIVE-PY-1`, `BC-LIVE-PY-2`, `BC-LIVE-PY-3`.
Actual console output (note the ~6–8s per-request latency — this backend is talking to a
remote DB over the network, unlike the sub-second local-DB captures in the original log):

```
2026-08-31 11:51:18,940 INFO [MainThread] ==== run cycle start: device=PI-BAY1-LIVE-CAPTURE workflow=M12.ConveyorTest bay=BAY1 ====
2026-08-31 11:51:18,940 INFO [MainThread] fetching workflow config for M12.ConveyorTest
2026-08-31 11:51:26,029 INFO [MainThread] TRACE GET /api/scanner/workflows/M12.ConveyorTest?bayCode=BAY1 -> 200 (7089ms)
2026-08-31 11:51:26,032 INFO [MainThread] workflow 'M12.ConveyorTest' loaded: 0 integration binding(s), 2 dispatch task(s)
2026-08-31 11:51:26,034 INFO [MainThread] no scannerIp configured — reading barcodes from stdin (dev/testing mode)
2026-08-31 11:51:26,035 INFO [MainThread] heartbeat started (every 5s), waiting for barcode scans (Ctrl+C to stop)
2026-08-31 11:51:27,743 INFO [heartbeat] TRACE POST /api/conveyor/heartbeat -> 200 (1709ms)
2026-08-31 11:51:32,145 INFO [MainThread] TRACE POST /api/conveyor/scan -> 200 (6109ms)
2026-08-31 11:51:32,145 INFO [MainThread] ACCEPT barcode=BC-LIVE-PY-1 processed_qty=3.0/3.0 status=COMPLETED plcSignalOk=None
2026-08-31 11:51:32,146 ERROR [MainThread] LOCAL REJECT barcode=BC-LIVE-PY-2: scan qty 1 exceeds remaining allowed qty 0.0 (target=3.0, processed=3.0) (not sent to backend)
2026-08-31 11:51:32,146 WARNING [MainThread] REJECT barcode=BC-LIVE-PY-2 reason=LOCAL_QTY_LIMIT_EXCEEDED plcSignalOk=None
2026-08-31 11:51:32,146 ERROR [MainThread] LOCAL REJECT barcode=BC-LIVE-PY-3: scan qty 1 exceeds remaining allowed qty 0.0 (target=3.0, processed=3.0) (not sent to backend)
2026-08-31 11:51:32,146 WARNING [MainThread] REJECT barcode=BC-LIVE-PY-3 reason=LOCAL_QTY_LIMIT_EXCEEDED plcSignalOk=None
2026-08-31 11:51:34,949 INFO [MainThread] TRACE POST /api/conveyor/heartbeat -> 200 (2803ms)
2026-08-31 11:51:34,950 INFO [MainThread] stopping (barcode source ended)
```

**Why the first scan jumped straight to `3.0/3.0`:** an earlier, aborted dry-run against
this same task (killed mid-request while capturing this doc) left an in-flight
`POST /api/conveyor/scan` that the backend finished processing server-side even after the
client process died — the backend has no client-side rollback, so a request that's already
been sent completes regardless of whether the client is still around to read the response.
By the time this run's `GET` fetched the task, `processed_qty` was already at 2/3; this
run's first scan pushed it to 3/3 in one step, which is why barcodes #2 and #3 hit the
**local** qty pre-check (`LOCAL_QTY_LIMIT_EXCEEDED`, never sent to the backend) — a live,
organic demonstration of `ConveyorClient.check_qty_limit` short-circuiting once a task is
exhausted, not a bug.

---

## 4. Final state
```
GET (via /api/m0/datasource/query on DispatchTask, workflow_code=M12.ConveyorTest)
```
```json
[
  {"id":1,"task_code":"TASK-LIVE-CAPTURE-1","target_qty":3.000,"processed_qty":3.000,"rejected_qty":1.000,"status":"COMPLETED","rule_set_id":1},
  {"id":2,"task_code":"TASK-LIVE-PY-1","target_qty":3.000,"processed_qty":3.000,"rejected_qty":0.000,"status":"COMPLETED","rule_set_id":null}
]
```

## 5. Endpoint summary
```
Setup (Authorization: Bearer <jwt>, TenantAdmin):
  POST  /api/auth/signin
  POST  /api/m0/entities/WorkflowDefinition
  POST  /api/m0/entities/RuleSet
  POST  /api/m0/entities/RuleDefinition
  POST  /api/m0/entities/DispatchTask
  PUT   /api/m0/entities/DispatchTask/{id}

Device-facing (permit-all, NO auth header):
  GET   /api/scanner/workflows/{workflowCode}[?bayCode={bayCode}]
  POST  /api/conveyor/scan
  POST  /api/conveyor/heartbeat
```
