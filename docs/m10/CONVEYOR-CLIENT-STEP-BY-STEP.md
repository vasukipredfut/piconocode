# `conveyor_client.py` — Complete Step-by-Step Walkthrough

Reference: [`raspberry-pi-client/conveyor_client.py`](../../raspberry-pi-client/conveyor_client.py).
Full API contract: [`CONVEYOR_SCANNER_WORKFLOW_GUIDE.md`](../../CONVEYOR_SCANNER_WORKFLOW_GUIDE.md).
Real captured request/response examples for every call below: `docs/m10/CONVEYOR-SKU-RESOLUTION-E2E-CAPTURE-2026-08-31.md`.

## 1. Entry point

```
python conveyor_client.py [config_path]
```
Calls `main()`. No arg → `config.json` next to the script.

## 2. Startup (`main()`)

| Step | What happens |
|---|---|
| `resolve_paths()` | Config path from the first CLI arg, or `config.json` by default. Log filename derives from the config's stem (`config_bay2.json` → `conveyor_client.config_bay2.log`) so a second process with a second config never interleaves log writes with the first. |
| `configure_logging()` | One logger, two handlers: console (stdout) + a 5MB rotating file (5 backups). |
| `load_config()` | Plain `json.load` — `baseUrl`, `workflowCode`, `bayCode`, `deviceId`, optional `stepCode`/`scannerIntegrationId`/`scannerIp`/`scannerPort`. |

## 3. Supervisor loop (`main()`)

```python
while True:
    try:
        run(config)
        return          # clean stop: stdin EOF (dev/testing)
    except KeyboardInterrupt:
        return           # clean stop: Ctrl+C
    except Exception:
        log.exception(...)
        time.sleep(CRASH_RESTART_DELAY_S)   # 5s, then retry run() from scratch
```
This is the device's self-healing layer — a backend that's down at boot, a workflow not yet
published, a bug in a barcode transport, anything else: logged, and the whole cycle restarts
after 5 seconds instead of the process dying. Nothing else in this deployment restarts it.

## 4. One operating cycle (`run()`)

Constructs a fresh `ConveyorClient(config)`, then:

### 4a. Fetch workflow config
```
GET /api/scanner/workflows/{workflowCode}?bayCode={bayCode}
```
Tokenless. `404` → `RuntimeError` (workflow not published) → bubbles to the supervisor, retried
in 5s. Response includes `integrations`, `loadingBays`, `ruleBuilder`, `dispatchTasks` — scoped
to this one bay.

### 4b. Startup diagnostic only
`track_tasks_from_workflow(workflow)` caches every `Open` dispatch task from the response,
keyed by `sku_code`, purely so `has_tracked_task()` can log a one-time warning if the bay has
**zero** Open tasks at boot. **This snapshot is not consulted per-scan anymore** — see step 5b.

### 4c. Start the heartbeat thread
Daemon thread, `POST /api/conveyor/heartbeat` every 5s, forever, independent of scan activity.
A heartbeat failure is logged and swallowed — never crashes the thread.

### 4d. Choose the barcode source
`scannerIp` set in config → `ScannerSocketReader` (raw TCP, newline-framed, auto-reconnecting).
Otherwise → `read_barcode_stdin()` (dev/testing, or a USB scanner enumerating as a keyboard).

### 4e. Main scan loop
```
loop:
  barcode = read_barcode()              # blocks until one arrives
  if barcode is None: break             # stdin EOF only
  if barcode is "": continue
  result = client.submit_scan(barcode)  # <- step 5, the real work
  log ACCEPT (processed_qty/target_qty) or REJECT (reason)
```
A `submit_scan` failure (network error after all retries) is caught, logged, and the loop
continues to the next barcode.

### 4f. Shutdown (`finally`)
Closes the scanner socket if open, stops the heartbeat thread, fires one final heartbeat with
`workflowStatus=STOPPED`/`deviceStatus=OFFLINE` so the backend doesn't have to wait for its own
staleness sweep to notice the device went away.

## 5. `submit_scan(barcode, ...)` — the three-call chain

Every scan now makes **three** backend round-trips before returning, in this exact order:

```
1. GET  /api/conveyor/skumaster?barcode=...
2. GET  /api/conveyor/dispatchdetails?workflowCode=&bayCode=&skuCode=&status=Open
3. POST /api/conveyor/scan
```

### 5a. Resolve the SKU — `resolve_sku(barcode)`
```
GET /api/conveyor/skumaster?barcode={barcode}
```
Tokenless. Real response for a known barcode:
```json
{"found": true, "skuCode": "SKU1463", "skuDescription": "SKU1463 (REF SUNFLOWER OIL 1 LTR BOX)", "boxQty": 12, "status": "ACTIVE"}
```
- `found: false` → **local REJECT**, `reason: "UNKNOWN_BARCODE"`, **no further call at all**.
- `found: true` but `status != "ACTIVE"` → **local REJECT**, `reason: "SKU_INACTIVE"`, same — no further call.
- Otherwise: `skuCode` is captured, `skuDescription`/`boxQty` logged for visibility (not used
  in any decision).

This is the device doing the same lookup `POST /api/conveyor/scan` will run server-side anyway
as its own authoritative check (`SkuMasterLookupService`) — doing it here first is what lets
step 5b target the *right* task, and catches a bad barcode one call earlier.

### 5b. Fetch the live dispatch task — `fetch_dispatch_details(resolved_sku)`
```
GET /api/conveyor/dispatchdetails?workflowCode={workflowCode}&bayCode={bayCode}&skuCode={resolved_sku}&status=Open
```
Tokenless. Real response:
```json
{"found": true, "dispatchTask": {"id": 4, "target_qty": 3.000, "processed_qty": 2.000, "status": "Open", "...": "..."}}
```
Fetched **fresh on every scan** — not from step 4b's startup snapshot — so this always reflects
whatever another device or process may have done to the task since the last workflow-config
sync. `found: false` just means no `Open` task currently matches; not an error.

### 5c. Local quantity pre-check — `check_qty_limit(task, qty)`
```python
remaining = task["target_qty"] - task["processed_qty"]
if qty > remaining:
    REJECT locally, reason="LOCAL_QTY_LIMIT_EXCEEDED", no /api/conveyor/scan call
```
If step 5b found no task (`task is None`), this is a no-op (`ok=True`) — the backend is the
fallback authority.

### 5d. Submit the scan
```json
POST /api/conveyor/scan
{
  "workflowCode": "...", "stepCode": "...", "bayCode": "...", "barcode": "...",
  "taskId": <the id from step 5b, if any>,
  "scannerIntegrationId": ..., "orderRef": null, "qty": null
}
```
**No `sku` field** — the backend re-resolves the SKU from `barcode` itself as its own
authoritative check regardless of anything the device already determined. `taskId` being
explicit means the backend never has to guess which task a scan on a multi-task bay belongs to.

Response synced back into local state: `_last_scan_at`, `_current_dispatch_task_id`; the
diagnostic per-SKU cache (step 4b) is updated if the returned task is still `Open`, or dropped
entirely if it auto-completed.

## 6. Real captured run (barcode `8900001011017` → `SKU1463`)

```
16:37:54  GET  /api/conveyor/skumaster?barcode=8900001011017        -> 200 (1424ms)
          resolved barcode=8900001011017 -> sku=SKU1463 (SKU1463 (REF SUNFLOWER OIL 1 LTR BOX)) boxQty=12
16:37:56  GET  /api/conveyor/dispatchdetails?workflowCode=M12.ConveyorTest&bayCode=Bay4&skuCode=SKU1463&status=Open -> 200 (1326ms)
16:38:03  POST /api/conveyor/scan                                   -> 200 (7356ms)
          ACCEPT barcode=8900001011017 processed_qty=3.0/3.0 status=Completed plcSignalOk=None
```
`processed_qty` went from `2.0` (what step 5b's live fetch saw) to `3.0/3.0`, and the task
auto-completed — all three calls chained correctly against the live backend.

## 7. `ScannerSocketReader` — network scanner transport

Opens a TCP connection to `scannerIp:scannerPort`, buffers bytes, yields one barcode per
`\n`-terminated line. `read_barcode()` **never returns `None`** — any socket error, EOF, or the
scanner closing the connection triggers a silent reconnect (fixed 3s delay) and it keeps
blocking, so a scanner power-cycle or brief network blip never kills the process.

## 8. `_request()` — shared HTTP layer

Every one of the calls above goes through this: capped exponential backoff (`0, 1, 2, 5, 10`
seconds) on connection errors, timeouts, or a `5xx` response. A `4xx` is **not** retried
(treated as a real answer). Every attempt is logged as `TRACE <method> <path> -> <status> (<ms>)`.

## Cost/benefit of the three-call design

| | Before (single cached snapshot) | Now (live per-scan) |
|---|---|---|
| Network calls per scan | 1 (`POST /scan`) | 3 (`GET /skumaster`, `GET /dispatchdetails`, `POST /scan`) |
| Multi-task bays (several SKUs concurrently Open on one bay) | Local pre-check skipped entirely — deferred to backend | Works correctly, per-SKU |
| Staleness | `target_qty`/`processed_qty` only as fresh as the last workflow-config sync | Always fetched fresh, right before the qty check |
| Bad barcode | Rejected inside `/api/conveyor/scan`'s own resolution | Rejected one call earlier, before touching a task at all |

Net effect unchanged from the module docstring's stated principle: the device does the bare
minimum locally (qty fast-fail, barcode transport, retry/reconnect plumbing) and defers every
real decision — SKU identity, task resolution, rule evaluation, PLC signaling — to the backend.
