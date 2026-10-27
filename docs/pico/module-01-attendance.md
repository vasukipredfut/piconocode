# Module 1: Attendance & Check-in

| File | Goes to (Aria repo) |
|---|---|
| `V382__pico_attendance_checkin.sql` | `src/main/resources/db/migration/` (renumber if V382 is taken) |
| `java/com/aria/action/handler/PicoSelf.java` | `src/main/java/com/aria/action/handler/` |
| `java/com/aria/action/handler/PicoEmployeePunchActionHandler.java` | same |
| `java/com/aria/action/handler/PicoMyAttendanceActionHandler.java` | same |
| `java/com/aria/hr/attendance/PicoAttendanceRules.java` | `src/main/java/com/aria/hr/attendance/` |
| `java/com/aria/hr/attendance/PicoAttendanceProcessor.java` | same |
| `java/com/aria/security/CurrentRole.java` | `src/main/java/com/aria/security/` (role-name helper used by Modules 2+) |

Optional properties: `pico.attendance.processor.enabled` (default `true`) and
`pico.attendance.processor.cron` (default `0 59 23 * * *`).

## Mobile app: old → new calls

All calls carry `Authorization: Bearer <accessToken>` from `POST /api/auth/signin`.
Action calls are `POST /api/m0/actions/{code}/invoke` with body `{"args": {...}}`.
The response is `{ok, message, recordId, data}`.

| ERPNext `pico_api.*` | Aria |
|---|---|
| `employee_checkin` | `pico.employee-punch`, args `{"log_type":"IN","latitude":..,"longitude":..,"punch_device":"..","image_url":".."}` |
| `employee_checkout` | `pico.employee-punch`, args `{"log_type":"OUT", ...}` |
| selfie `image` (base64 in body) | first `POST /api/m0/document-vault/upload` (multipart), then pass the returned URL as `image_url` |
| `today_attendance`, `get_employee_shift` | `pico.my-attendance`, args `{"view":"today"}` |
| `attendance_history` | `pico.my-attendance`, args `{"view":"history","year":2026,"month":10}` (omit `month` for the whole year) |
| `holiday_calendar` | `pico.my-attendance`, args `{"view":"holidays","year":2026}` |
| `get_employee_checkins` | `pico.my-attendance`, args `{"view":"checkins","from_date":"2026-10-01","to_date":"2026-10-26"}` |
| `get_employee_last_punch` | `pico.my-attendance`, args `{"view":"last-punch","date":"2026-10-26"}` (omit `date` for latest) |

Behaviour changes the app must handle:
- `employee_number` is no longer sent. The employee comes from the login (`m7_employee.user_id`).
- `time` is no longer accepted. The punch uses the server clock, so a phone with the wrong time cannot backdate a punch.
- An approved leave returns `ok:false` with `data.reason = "LEAVE_APPROVED"` (ERPNext returned HTTP 409). Half-day leave rules return in Module 2.
- Status values are upper-case: `PRESENT`, `HALF_DAY`, `HOLIDAY`, `ABSENT`.

## Setup after deploy
1. Create shift types: `pico:shift-type-list` (one per ERPNext Shift Type, with that company's HR Attendance Settings).
2. Create holiday lists and holidays: `pico:holiday-list`, `pico:holiday-date-list`.
3. On each employee (`EmployeeMaster`), set `user_id` (their login), `holiday_list_id` and `shift_code`.
4. Optional: back-load ERPNext history with `POST /api/m0/entities/EmployeeCheckin/bulk` using `source = "IMPORT"` and `erp_ref` = the ERPNext checkin name.

## Verified
- All 20 JSON literals in the SQL (page bodies, `options_json`, action params) parse.
- The Java compiles against Aria's `target/classes` with Spring 6.1.14.

## Not verified
- The SQL has not been run against MariaDB. Run it on a dev copy of `earms` first.
- The pages have not been opened in the frontend, and there are no Playwright specs yet (Aria's customer-flow skill requires entity API, page render and e2e specs).
