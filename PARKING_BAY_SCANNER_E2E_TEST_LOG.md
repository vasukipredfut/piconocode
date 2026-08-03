# ParkingBayScanner — End-to-End Test Log

All requests/responses below are **real**, captured live against a running instance of this backend (`localhost:8080`, MariaDB dev DB) during a full verification pass. Nothing here is fabricated — every JSON block is an actual API call/response from that test run. Test rows created purely for this run (`GatePass.gp_number = GP-TEST-PARKING-1`) were deleted afterward; the ids/timestamps below are exactly what the live run produced.

Companion doc: [`PARKING_BAY_SCANNER_GUIDE.md`](PARKING_BAY_SCANNER_GUIDE.md) (full API reference + concepts).

## 1. Setup — create a test GatePass (needs a JWT)

```
POST /api/m0/entities/GatePass
Authorization: Bearer <token>
Content-Type: application/json

{
  "gp_number": "GP-TEST-PARKING-1",
  "flow_variant_code": "M10.TankerInbound",
  "vehicle_no": "TN01ZZ9999"
}
```

Response (200):
```json
{
  "ok": true,
  "entity": "GatePass",
  "inserted": {
    "gp_number": "GP-TEST-PARKING-1",
    "flow_variant_code": "M10.TankerInbound",
    "vehicle_no": "TN01ZZ9999",
    "tenant_id": 1
  },
  "id": 516
}
```

## 2. Device-facing flow — no auth header at all (permit-all route)

### 2.1 Scan #1 — no open entry for this gate pass yet → IN

```
POST /api/parking/scan

{ "gpNumber": "GP-TEST-PARKING-1" }
```

Response (200):
```json
{
  "outcome": "IN",
  "reason": null,
  "gpNumber": "GP-TEST-PARKING-1",
  "parkingBayEntry": {
    "id": 1,
    "tenant_id": 1,
    "gate_pass_no": "GP-TEST-PARKING-1",
    "in_time": "2026-08-03T01:42:29.000+00:00",
    "out_time": null,
    "created_by": null,
    "created_at": "2026-08-03T01:42:29.000+00:00",
    "vehicle_no": "TN01ZZ9999",
    "flow_variant_code": "M10.TankerInbound",
    "loading_type": null
  }
}
```

`vehicle_no`/`flow_variant_code` were resolved server-side from the `gate_pass` row just created above — the device never sent them.

### 2.2 Scan #2 — same gate pass, open entry exists → OUT

```
POST /api/parking/scan

{ "gpNumber": "GP-TEST-PARKING-1" }
```

Response (200):
```json
{
  "outcome": "OUT",
  "reason": null,
  "gpNumber": "GP-TEST-PARKING-1",
  "parkingBayEntry": {
    "id": 1,
    "tenant_id": 1,
    "gate_pass_no": "GP-TEST-PARKING-1",
    "in_time": "2026-08-03T01:42:29.000+00:00",
    "out_time": "2026-08-03T01:42:34.000+00:00",
    "created_by": null,
    "created_at": "2026-08-03T01:42:29.000+00:00",
    "vehicle_no": "TN01ZZ9999",
    "flow_variant_code": "M10.TankerInbound",
    "loading_type": null
  }
}
```

Same row (`id: 1`) — `out_time` now set, everything else unchanged.

### 2.3 Scan #3 — unknown gate pass → REJECT

```
POST /api/parking/scan

{ "gpNumber": "GP-DOES-NOT-EXIST" }
```

Response (200):
```json
{ "outcome": "REJECT", "reason": "GATE_PASS_NOT_FOUND", "gpNumber": "GP-DOES-NOT-EXIST", "parkingBayEntry": null }
```

## 3. Audit trail — verified directly against `m0_audit_log`

```sql
SELECT id, ts, actor_id, action_kind, entity_code, entity_id, vehicle_no, gp_number, message
FROM m0_audit_log WHERE gp_number = 'GP-TEST-PARKING-1' ORDER BY id;
```

```
(6890, 2026-08-03 07:12:14, 2033, 'CREATE',      'GatePass', 516, 'TN01ZZ9999', 'GP-TEST-PARKING-1', 'CREATE GatePass#516')
(6891, 2026-08-03 07:12:30, NULL, 'PARKING_IN',  'GatePass', 516, 'TN01ZZ9999', 'GP-TEST-PARKING-1', 'Parking bay IN via gate pass scan')
(6892, 2026-08-03 07:12:34, NULL, 'PARKING_OUT', 'GatePass', 516, 'TN01ZZ9999', 'GP-TEST-PARKING-1', 'Parking bay OUT via gate pass scan')
```

First row is `GenericEntityController`'s own audit write from step 1 (creating the `GatePass`, `actor_id=2033` from the JWT). The next two are `ParkingBayScanService`'s writes for scans #1/#2 — `actor_id=NULL` since the parking scan endpoint is tokenless (no authenticated principal for a device to attribute the action to).

## 4. Re-scan after OUT opens a NEW row, and error handling for a missing/blank gpNumber

A second `GatePass` (`GP-TEST-PARKING-2`, id 517) was created the same way as step 1, then scanned three times in a row plus two deliberately-bad requests.

### 4.1 Scan #1 → IN (row id 2)
```json
{"outcome":"IN","reason":null,"gpNumber":"GP-TEST-PARKING-2","parkingBayEntry":{"id":2,"tenant_id":1,"gate_pass_no":"GP-TEST-PARKING-2","in_time":"2026-08-03T01:59:19.000+00:00","out_time":null,"created_by":null,"created_at":"2026-08-03T01:59:19.000+00:00","vehicle_no":"KA05CD5678","flow_variant_code":"M10.PackLoading","loading_type":null}}
```

### 4.2 Scan #2 → OUT (same row id 2, out_time set)
```json
{"outcome":"OUT","reason":null,"gpNumber":"GP-TEST-PARKING-2","parkingBayEntry":{"id":2,"tenant_id":1,"gate_pass_no":"GP-TEST-PARKING-2","in_time":"2026-08-03T01:59:19.000+00:00","out_time":"2026-08-03T01:59:23.000+00:00","created_by":null,"created_at":"2026-08-03T01:59:19.000+00:00","vehicle_no":"KA05CD5678","flow_variant_code":"M10.PackLoading","loading_type":null}}
```

### 4.3 Scan #3 → IN again — a brand NEW row (id 3), not a reuse of id 2
```json
{"outcome":"IN","reason":null,"gpNumber":"GP-TEST-PARKING-2","parkingBayEntry":{"id":3,"tenant_id":1,"gate_pass_no":"GP-TEST-PARKING-2","in_time":"2026-08-03T01:59:27.000+00:00","out_time":null,"created_by":null,"created_at":"2026-08-03T01:59:27.000+00:00","vehicle_no":"KA05CD5678","flow_variant_code":"M10.PackLoading","loading_type":null}}
```
Confirms `dispatch_parking_bay_entry` genuinely accumulates one row per visit over time (no unique constraint on `gate_pass_no` — see the guide's data-written section) rather than upserting a single row per gate pass.

### 4.4 Missing `gpNumber` → ERROR, no DB writes
```
POST /api/parking/scan
{}
```
```json
{"outcome":"ERROR","reason":"gpNumber is required","gpNumber":null,"parkingBayEntry":null}
```

### 4.5 Blank `gpNumber` → same ERROR
```
POST /api/parking/scan
{"gpNumber":""}
```
```json
{"outcome":"ERROR","reason":"gpNumber is required","gpNumber":"","parkingBayEntry":null}
```

## 5. Endpoint summary

```
Setup (needs Authorization: Bearer <jwt>):
  POST /api/auth/signin
  POST /api/m0/entities/GatePass

Device-facing (permit-all, NO auth header):
  POST /api/parking/scan
```
