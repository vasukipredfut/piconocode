# Module 6: Service (Installation Report + customer OTP, Support tickets)

Requires Modules 1–5. It needs `m8_customer` (Module 5), `TenantMailService` (Module 2, **updated in this module**: `sendNow`) and `PicoSelf` / `CurrentRole` (Module 1).

| File | Goes to (Aria repo) |
|---|---|
| `V387__pico_service.sql` | `src/main/resources/db/migration/` (renumber if V387 is taken) |
| `java/com/aria/fieldservice/InstallationOtpService.java` | `src/main/java/com/aria/fieldservice/` |
| `java/com/aria/fieldservice/InstallationReportPdf.java` | same |
| `java/com/aria/fieldservice/ServiceTicketService.java` | same |
| `java/com/aria/fieldservice/ServiceAccessValidator.java` | same |
| `java/com/aria/fieldservice/PublicSupportController.java` | same |
| `java/com/aria/action/handler/PicoInstallationCreateActionHandler.java` | `src/main/java/com/aria/action/handler/` |
| `java/com/aria/action/handler/PicoInstallationOtpActionHandler.java` | same |
| `java/com/aria/action/handler/PicoTicketCreateActionHandler.java` | same |
| `java/com/aria/action/handler/PicoTicketCommentActionHandler.java` | same |
| `java/com/aria/action/handler/PicoMyServiceActionHandler.java` | same |
| `../module-02-leave/java/com/aria/integration/service/TenantMailService.java` | copy the current version (adds `sendNow`) |
| `IntegrationOutboundCaller.redact.patch` | apply to `integration/service/IntegrationOutboundCaller.java` |

Two edits to existing Aria files:
1. `WebSecurityConfig`: add `"/api/public/support/**"` to the `permitAll()` list (next to `"/api/integrations/inbound/**"`).
2. `IntegrationOutboundCaller.redact`: apply the patch. Otherwise the OTP is written in plain text to `integration_event_log`.

## ⚠️ Rotate now
`pico_api.send_otp_sms` has a **Fast2SMS API key hard-coded** in the source (now also in the `piconocode` git repo). Revoke it in Fast2SMS whether or not this module ships. Aria sends SMS through the tenant's `MSG91_SMS` integration, with keys in the integration secret store.

## Installation Report

```
pico.installation-create ──► m9_installation_report (OTP_PENDING) + code to customer email / SMS
pico.installation-otp {op: verify, otp} ──► otp_verified_at stamped ──pico:installation-verify──► VERIFIED
pico.installation-otp {op: resend}      ──► new code (max 5 sends)
POST /api/workflow/transition pico:installation-cancel ──► CANCELLED (engineer / Manager)
```

| | ERPNext | Aria |
|---|---|---|
| Code | 4 digits, `random.randint` | 6 digits, `SecureRandom` |
| Storage | plain text in `otp` **and** `otp_to_be_verified` | salted SHA-256 only (`otp_hash`, `otp_salt`) |
| Expiry | none | `service.otp_ttl_minutes` (default 30) |
| Wrong tries | unlimited, guest API (10 000 codes → brute-forceable) | 5 per code, reserved atomically; then resend |
| Verify API | `allow_guest=True`, any report by name | engineer of the report or Manager, logged in |
| Skipping the OTP | n/a | `ServiceAccessValidator` refuses VERIFIED unless the OTP action stamped `otp_verified_at`, so a direct transition call cannot skip it |
| SMS | Fast2SMS, key in source | MSG91 integration + DLT template `service.otp_sms_template_id` (variables `otp`, `doc`); unset = email only |
| Logs | n/a | payload marked `sensitive`; redact patch masks body / variables / `otp` keys |

- ERPNext's `uw / amc / ow / o` checkboxes become one `warranty_kind` (`UW / AMC / OW / O`). The old field names are still accepted by the action.
- The PDF is Aria's own table layout (`InstallationReportPdf`, Flying Saucer), not the ERPNext print format. Verified reports carry a "verified by customer" stamp. Compare it with the client's paper format before go-live.
- The salt sits next to the hash, so someone with database read access could still brute-force a 6-digit code within its 30 minutes. Acceptable for this use; a server-side pepper would close it.

## Support tickets

- **Model:** ERPNext Issue maps to Aria's existing `m9_service_ticket` + `M9.ServiceTicket` (v2: same states and pages; roles fixed from `UnloadingSupervisor` to `Service`; `OPEN → CLOSED` via `pico:ticket-close` for spam). Comments go to `m9_ticket_comment` (internal or public).
- **Staff:** `pico.ticket-create`, `pico.ticket-comment`. Schedule / start / resolve / close through the existing `aria:st-*` pages.
- **Technician:** set `technician_id` on the ticket (generic entity update by TenantAdmin, or the `aria:st-schedule` page). The projection does not map it from forms, because one bad value would fail the whole state write.
- **Public portal** (`PublicSupportController`, no login):

  | | ERPNext `customer_support` | Aria |
  |---|---|---|
  | Create | any existing customer **name** | the **registered mobile** of exactly one active customer of that tenant |
  | Read | `get_customer_issues?customer_name=` listed every issue of any customer, to anyone | one ticket by `ticket_no` + matching mobile; internal comments hidden |
  | Abuse | `ignore_csrf = 1`, no limits | 10 requests / IP / 10 min (in-memory, per node); add a CAPTCHA on the page |
  | Tenant | single company | `/api/public/support/{tenantCode}/…` resolves the ACTIVE `tenant.code` |

  `POST /api/public/support/PICO/tickets` `{"customer_name":"..","mobile":"98xxxxxx21","subject":"..","description":"..","email":".."}` → `ticket_no`.
  `GET /api/public/support/PICO/tickets/TKT-2026-00012?mobile=98xxxxxx21` → status + public updates.
  The portal page itself (ERPNext `www/support.html`) needs a small static page in the frontend that calls these two endpoints.
  A "customer not found" reply tells the caller whether a mobile belongs to a customer. Accepted for usability; make the message generic if that is a concern.

## Mobile app: old → new calls

| ERPNext | Aria |
|---|---|
| `create_installation_report` | `pico.installation-create` `{"customer_id":9,"email":"..","contact_number":"..","warranty_kind":"AMC","machine_type":"..",...}` (or `customer_name`) |
| `verify_installation_report_otp` | `pico.installation-otp` `{"report_id":4,"op":"verify","otp":"123456"}`; resend: `{"op":"resend"}` |
| `get_installation_report` | `pico.my-service` `{"view":"report","report_id":4}` |
| `get_all_installation_reports`, `get_employee_installation_reports` | `pico.my-service` `{"view":"reports","status":"VERIFIED"}` (own; Manager sees all) |
| `download_installation_report_pdf` | `pico.my-service` `{"view":"report-pdf","report_id":4}` → `pdf_base64` |
| `get_issues` | `pico.my-service` `{"view":"tickets"}`; one with comments: `{"view":"ticket","ticket_id":12}` |
| *(desk)* new issue / comment | `pico.ticket-create`, `pico.ticket-comment` |

## Setup after deploy
1. Create role `Service` for service engineers / coordinators.
2. Create an `MSG91_SMS` integration (ACTIVE) and a DLT-approved OTP template; set `service.otp_sms_template_id`. Or rely on email only.
3. Optional settings: `service.otp_ttl_minutes`, `service.notify_emp_id` (mailed for portal tickets).
4. Load ERPNext Installation Reports (`erp_ref`). Do **not** load their `otp` fields. Load ERPNext Issues into `m9_service_ticket` (`source='IMPORT'`).

## Verified
- All 25 JSON literals in the SQL parse.
- Modules 1–6 compile together against Aria's `target/classes` (+ Tomcat servlet API). Modules 1, 1–2 and 1–5 still compile.

## Not verified
- The SQL has not been run.
- OTP send / verify / lockout, the public endpoints (security config change, tenant resolution, rate limit) and the PDF rendering have not run against a live backend.
