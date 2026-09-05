# M10.PackUnload — Unloading-Bay `dispatch_task` Wiring: E2E API Trace

Live capture verifying the new conveyor `dispatch_task` writes added to the
M10.PackUnload receiving side: `m10.start-unloading-bay` (opens `type='UnLoading'`
dispatch_task rows, gated by `scanning_status`/`conveyor_mode`) and
`m10.unloading-bay-complete` (force-closes them). Run directly against
`POST /api/m0/actions/{code}/invoke` (`com.aria.action.ActionController`),
bypassing the frontend. Companion to `FGLOADING-E2E-API-TRACE.md` (loading
side) and `DISPATCH-TASK-E2E-LIVE-CAPTURE-2026-08-31.md`.

- Instances: **1902**, **1903** (workflow code `M10.PackUnload`, workflowVersion 52)
- Auth user: `2003` (`Dispatch` role, password `Test@1234`)
- Captured: 2026-09-05, against the local dev backend (`localhost:8080`) backed by
  the shared dev MariaDB (`earms` schema)
- Test data created: vehicle `TN86J9001`, gate pass `GP-1-1902`, synthetic SKUs
  `SKU-TEST-01`..`SKU-TEST-04` (no `m5_sku_master` row needed — `scanning_status`/
  `conveyor_mode` ride in the request payload per line, not looked up server-side)

Neither handler gates on `workflow_instance.current_state` — both only read it
once to stamp `unloading_progress_detail.current_step`/`flow_variant_code` — so
no workflow transitions were needed to reach a valid test state; a freshly
created `DRAFT` instance is enough.

## Auth

**Request**
```
POST /api/auth/signin
{"username":"2003","password":"Test@1234"}
```

**Response** `200`
```json
{"accessToken":"<redacted>","tokenType":"Bearer","id":2003,"username":"2003","roles":["1003"],"roleName":"Dispatch"}
```

## Setup — create test instance 1902

**Request**
```
POST /api/workflow/instances
{"workflowCode":"M10.PackUnload"}
```

**Response** `200`
```json
{"workflowVersion":52,"workflowCode":"M10.PackUnload","assignedRole":"","currentState":"DRAFT","instanceId":1902}
```

## Step 1 — `m10.start-unloading-bay`: two lines, mixed conveyor gating

Line 1 (`SKU-TEST-01`) is `BARCODE`/`CONVEYOR` — eligible for a `dispatch_task`.
Line 2 (`SKU-TEST-02`) is `WITHOUT_BARCODE`/`WITHOUT_CONVEYOR` — should be
skipped.

**Request**
```
POST /api/m0/actions/m10.start-unloading-bay/invoke
{"instanceId":1902,"formData":{"vehicle_no":"TN86J9001","gpNumber":"GP-1-1902","bay_name":"Bay3","order_sku_line_items":[
  {"route_id":"ROU-TEST-01","order_id":"ORD-TEST-01","sku_code":"SKU-TEST-01","sku_desc":"Test SKU One","quantity":20,"unloading_qty":0,"scanning_status":"BARCODE","conveyor_mode":"CONVEYOR"},
  {"route_id":"ROU-TEST-01","order_id":"ORD-TEST-02","sku_code":"SKU-TEST-02","sku_desc":"Test SKU Two","quantity":15,"unloading_qty":0,"scanning_status":"WITHOUT_BARCODE","conveyor_mode":"WITHOUT_CONVEYOR"}
]}}
```

**Response** `200`
```json
{"ok":true,"message":"unload bay 'Bay3' started with 2 SKU line(s), total qty 35","recordId":null,"nextPageState":null,"data":{"dispatchTasksSkipped":1,"bayName":"Bay3","totalQuantity":35,"lineCount":2}}
```

### DB verification — `unloading_progress_detail_bay` (both lines opened, `IN_PROGRESS`)

**Request**
```
POST /api/m0/datasource/query
{"spec":{"type":"entity","entity":"UnloadingProgressDetailBay","filter":{"field":"workflow_instance_id","op":"eq","value":1902}},"page":0,"size":20}
```

**Response** `200` (truncated to key fields)
```json
{"content":[
  {"id":246,"route_id":"ROU-TEST-01","order_id":"ORD-TEST-01","sku_code":"SKU-TEST-01","bay_name":"Bay3","quantity":20.000,"processed_quantity":0.000,"status":"IN_PROGRESS"},
  {"id":247,"route_id":"ROU-TEST-01","order_id":"ORD-TEST-02","sku_code":"SKU-TEST-02","bay_name":"Bay3","quantity":15.000,"processed_quantity":0.000,"status":"IN_PROGRESS"}
],"totalElements":2}
```

### DB verification — `dispatch_task` (only the conveyor-eligible line got one)

**Request**
```
POST /api/m0/datasource/query
{"spec":{"type":"entity","entity":"DispatchTask","filter":{"field":"workflow_instance_id","op":"eq","value":1902}},"page":0,"size":20}
```

**Response** `200`
```json
{"content":[
  {"id":52,"workflow_code":"M12.ConveyorTest","type":"UnLoading","workflow_instance_id":1902,"bay_code":"Bay3","route_id":"ROU-TEST-01","order_id":"ORD-TEST-01","task_code":"DT-1902-Bay3-SKU-TEST-01-1788591644675","order_ref":"ORD-TEST-01","sku_code":"SKU-TEST-01","gp_number":"GP-1-1902","target_qty":20.000,"processed_qty":0.000,"status":"Open"}
],"totalElements":1}
```

**Confirmed: exactly one `dispatch_task` row, `type='UnLoading'`, stamped with the same fixed `M12.ConveyorTest` workflow code the loading side uses — `SKU-TEST-02` (`WITHOUT_CONVEYOR`) correctly got none.**

## Step 2 — `m10.unloading-bay-complete`: close both lines

**Request**
```
POST /api/m0/actions/m10.unloading-bay-complete/invoke
{"instanceId":1902,"formData":{"order_sku_line_items":[
  {"route_id":"ROU-TEST-01","order_id":"ORD-TEST-01","sku_code":"SKU-TEST-01","unloading_qty":20},
  {"route_id":"ROU-TEST-01","order_id":"ORD-TEST-02","sku_code":"SKU-TEST-02","unloading_qty":15}
]}}
```

**Response** `200`
```json
{"ok":true,"message":"2 unload bay line(s) closed, received 35 total into stock","recordId":null,"nextPageState":null,"data":{"totalReceived":35,"closed":[{"routeId":"ROU-TEST-01","processedQuantity":20,"skuCode":"SKU-TEST-01"},{"routeId":"ROU-TEST-01","processedQuantity":15,"skuCode":"SKU-TEST-02"}]}}
```

**Application log** (confirms the force-close ran once, only for the SKU that actually had a `dispatch_task`; `SKU-TEST-02`'s zero-match case logged nothing, per the "zero matching rows is not an error" design):
```
action.unloading-bay-complete.start tenantId=1 instanceId=1902 lineCount=2
action.unloading-bay-complete.stock-receipt tenantId=1 instanceId=1902 routeId=ROU-TEST-01 skuCode=SKU-TEST-01 binCode=Bay3 qty=20 uom=EA
action.unloading-bay-complete.dispatch-task-closed tenantId=1 instanceId=1902 routeId=ROU-TEST-01 skuCode=SKU-TEST-01 count=1
action.unloading-bay-complete.stock-receipt tenantId=1 instanceId=1902 routeId=ROU-TEST-01 skuCode=SKU-TEST-02 binCode=Bay3 qty=15 uom=EA
action.unloading-bay-complete.done tenantId=1 instanceId=1902 linesClosed=2 totalReceived=35
```

### DB verification — `dispatch_task` force-closed

**Request**
```
POST /api/m0/datasource/query
{"spec":{"type":"entity","entity":"DispatchTask","filter":{"field":"workflow_instance_id","op":"eq","value":1902}},"page":0,"size":20}
```

**Response** `200`
```json
{"content":[
  {"id":52,"type":"UnLoading","sku_code":"SKU-TEST-01","status":"Completed","completed_at":"2026-09-05T07:01:40.000+00:00"}
],"totalElements":1}
```

### DB verification — bay rows `DONE`, parent rolled up, stock received

```json
// UnloadingProgressDetailBay (both rows, filtered to instance 1902)
[{"id":246,"sku_code":"SKU-TEST-01","quantity":20.000,"processed_quantity":20.000,"status":"DONE"},
 {"id":247,"sku_code":"SKU-TEST-02","quantity":15.000,"processed_quantity":15.000,"status":"DONE"}]

// UnloadingProgressDetail (parent, id 69)
{"workflow_instance_id":1902,"vehicle_no":"TN86J9001","gate_pass_no":"GP-1-1902","total_quantity":35.000,"processed_quantity":35.000,"status":"DONE"}

// StockBalance — both SKUs received into Bay3's own bin (bin_code = bay_name)
[{"item_code":"SKU-TEST-01","bin_code":"Bay3","uom":"EA","qty_on_hand":20.000},
 {"item_code":"SKU-TEST-02","bin_code":"Bay3","uom":"EA","qty_on_hand":15.000}]
```

**Full round trip confirmed: dispatch_task opened → closed, unload bay rows opened → DONE, parent rolled up to DONE, RECEIPT stock posted for both SKUs.**

## Error-path captures (instance 1903)

**Setup**
```
POST /api/workflow/instances {"workflowCode":"M10.PackUnload"}
→ 200 {"instanceId":1903,"currentState":"DRAFT", ...}
```

### Missing `scanning_status`

**Request**
```
POST /api/m0/actions/m10.start-unloading-bay/invoke
{"instanceId":1903,"formData":{"bay_name":"Bay4","order_sku_line_items":[
  {"route_id":"ROU-TEST-02","sku_code":"SKU-TEST-03","quantity":10,"conveyor_mode":"CONVEYOR"}
]}}
```

**Response** `200`
```json
{"ok":false,"message":"order_sku_line_items[0] (SKU-TEST-03) is missing scanning_status","recordId":null,"nextPageState":null,"data":null}
```

### `bay_name` out of range

**Request**
```
POST /api/m0/actions/m10.start-unloading-bay/invoke
{"instanceId":1903,"formData":{"bay_name":"Bay13","order_sku_line_items":[
  {"route_id":"ROU-TEST-02","sku_code":"SKU-TEST-03","quantity":10,"scanning_status":"BARCODE","conveyor_mode":"CONVEYOR"}
]}}
```

**Response** `200`
```json
{"ok":false,"message":"formData.bay_name must be one of Bay1..Bay12, got: Bay13","recordId":null,"nextPageState":null,"data":null}
```

### Happy-path Start Bay on 1903 (needed for the next two error cases)

**Request**
```
POST /api/m0/actions/m10.start-unloading-bay/invoke
{"instanceId":1903,"formData":{"bay_name":"Bay4","order_sku_line_items":[
  {"route_id":"ROU-TEST-02","sku_code":"SKU-TEST-03","quantity":10,"scanning_status":"BARCODE","conveyor_mode":"CONVEYOR"}
]}}
```

**Response** `200`
```json
{"ok":true,"message":"unload bay 'Bay4' started with 1 SKU line(s), total qty 10","recordId":null,"nextPageState":null,"data":{"dispatchTasksSkipped":0,"bayName":"Bay4","totalQuantity":10,"lineCount":1}}
```

### Already-open unload bay — second Start Bay refused before Bay Complete

**Request**
```
POST /api/m0/actions/m10.start-unloading-bay/invoke
{"instanceId":1903,"formData":{"bay_name":"Bay5","order_sku_line_items":[
  {"route_id":"ROU-TEST-02","sku_code":"SKU-TEST-04","quantity":5,"scanning_status":"BARCODE","conveyor_mode":"CONVEYOR"}
]}}
```

**Response** `200`
```json
{"ok":false,"message":"this vehicle already has an open unload bay — close it with Bay Complete before starting another","recordId":null,"nextPageState":null,"data":null}
```

### `unloading-bay-complete` with no matching open row

**Request**
```
POST /api/m0/actions/m10.unloading-bay-complete/invoke
{"instanceId":1903,"formData":{"order_sku_line_items":[
  {"route_id":"ROU-TEST-02","sku_code":"SKU-DOES-NOT-EXIST","unloading_qty":5}
]}}
```

**Response** `200`
```json
{"ok":false,"message":"no open unload bay row found for route_id=ROU-TEST-02 order_id=null sku_code=SKU-DOES-NOT-EXIST","recordId":null,"nextPageState":null,"data":null}
```

### Cleanup — close 1903's real open bay

**Request**
```
POST /api/m0/actions/m10.unloading-bay-complete/invoke
{"instanceId":1903,"formData":{"order_sku_line_items":[
  {"route_id":"ROU-TEST-02","sku_code":"SKU-TEST-03","unloading_qty":10}
]}}
```

**Response** `200`
```json
{"ok":true,"message":"1 unload bay line(s) closed, received 10 total into stock","recordId":null,"nextPageState":null,"data":{"totalReceived":10,"closed":[{"routeId":"ROU-TEST-02","processedQuantity":10,"skuCode":"SKU-TEST-03"}]}}
```

## Summary

| Scenario | Result |
|---|---|
| Conveyor-eligible line (`BARCODE`/`CONVEYOR`) | `dispatch_task` opened, `type='UnLoading'` |
| Non-conveyor line (`WITHOUT_BARCODE`/`WITHOUT_CONVEYOR`) | No `dispatch_task` opened; `dispatchTasksSkipped` counts it |
| Bay Complete on a line with an open `dispatch_task` | Force-closed (`status='Completed'`, `completed_at` stamped) |
| Bay Complete on a line with no `dispatch_task` | Succeeds silently, zero rows affected, no error |
| Missing `scanning_status`/`conveyor_mode` | Rejected before any row is written |
| Second Start Bay while one is still open | Rejected — must Bay Complete first |
| Bay Complete against a SKU with no open row | Rejected with a specific route/order/sku error |

All scenarios ran against the live dev backend (not mocks) and match the unit
test expectations in `M10StartUnloadingBayActionHandlerTest` and
`M10UnloadingBayCompleteActionHandlerTest`.
