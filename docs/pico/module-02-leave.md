# Module 2: Leave

Requires Module 1 (V382 and its Java).

| File | Goes to (Aria repo) |
|---|---|
| `V383__pico_leave.sql` | `src/main/resources/db/migration/` (renumber if V383 is taken) |
| `java/com/aria/action/handler/PicoLeaveApplyActionHandler.java` | `src/main/java/com/aria/action/handler/` |
| `java/com/aria/action/handler/PicoLeaveCancelActionHandler.java` | same |
| `java/com/aria/action/handler/PicoMyLeaveActionHandler.java` | same |
| `java/com/aria/action/handler/PicoEmployeePunchActionHandler.java` | same. **Replaces the Module 1 file** (adds half-day leave rules). |
| `java/com/aria/action/handler/PicoMyAttendanceActionHandler.java` | same. **Replaces the Module 1 file** (history shows `ON_LEAVE` / `HALF_DAY_LEAVE`). |
| `java/com/aria/hr/leave/PicoLeaveRules.java` | `src/main/java/com/aria/hr/leave/` |
| `java/com/aria/hr/leave/LeaveMails.java` | same |
| `java/com/aria/hr/leave/LeaveApproverValidator.java` | same |
| `java/com/aria/hr/leave/LeaveDecisionListener.java` | same |
| `java/com/aria/integration/service/TenantMailService.java` | `src/main/java/com/aria/integration/service/` |

`LeaveApproverValidator` uses `com.aria.security.CurrentRole` from Module 1.

## How it fits Aria

```
pico.leave-apply ──► m7_leave_request (PENDING) ──► M7.LeaveRequest v2
                                                   PENDING ──aria:lr-approve, status=APPROVED──► APPROVED
                                                           ──aria:lr-approve, status=REJECTED──► REJECTED
                                                           ──pico:leave-withdraw──────────────► WITHDRAWN
pico.leave-cancel (APPROVED leave) ──► M7.LeaveCancellation
                                                   REQUESTED ──pico:leave-cancel-decide, decision=CANCELLED──► CANCELLED
                                                             ──pico:leave-cancel-decide, decision=DECLINED───► DECLINED
```

- **State → table:** two `m0_workflow_projection` rows write `status` / `current_step` / `decided_at` and `cancel_status` / `cancel_decided_at` (plus `approver_remarks`). No Java writes workflow state.
- **Who can decide:** `LeaveApproverValidator` (a `WorkflowTransitionValidator`) only lets the leave's approver decide. Only the employee can withdraw. TenantAdmin / SuperAdmin can always act. Without it, the engine would accept any logged-in user's transition. Approving also re-checks overlap.
- **Side-effects:** `LeaveDecisionListener` sends the mails and, on CANCELLED, marks the leave CANCELLED. When the employee asked to keep a half day, it also inserts that half day as APPROVED (`LR-…-H`, `revised_from_id`), as ERPNext did.
- **Mail:** `TenantMailService` uses the tenant's ACTIVE `SMTP_EMAIL` integration (M11) and sends after commit. With no SMTP instance it logs and skips. Recipients come from `m7_employee.email_official`, then `email_personal`, then the login account's `email`.
- **Approver:** `m7_employee.leave_approver_id`, falling back to `reports_to_emp_id`. Fixed on the leave (`approver_emp_id`) at apply time.

## Mobile app: old → new calls

Action calls are `POST /api/m0/actions/{code}/invoke` with `{"args": {...}}`. Decisions use `POST /api/workflow/transition`.

| ERPNext `pico_api.*` | Aria |
|---|---|
| `apply_leave` | `pico.leave-apply`, args `{"leave_kind":"CL","from_date":"2026-11-03","to_date":"2026-11-04","reason":".."}`; half day: `"is_half_day":true,"which_half":"FIRST_HALF"` (from = to) |
| `cancel_leave_application`, `cancel_leave_applications`, `delete_leave_application` | `pico.leave-cancel`, args `{"leave_id":12,"reason":".."}`; keep a half day: `"revised_half_day":true,"which_half":"SECOND_HALF","half_day_date":"2026-11-03"` |
| `approve_reject_leave` (Approved / Rejected) | `POST /api/workflow/transition` `{"instanceId":<workflow_instance_id>,"pageCode":"aria:lr-approve","formData":{"status":"APPROVED","remarks":".."}}` |
| `approve_reject_leave` (Cancelled) | `POST /api/workflow/transition` `{"instanceId":<cancel_instance_id>,"pageCode":"pico:leave-cancel-decide","formData":{"decision":"CANCELLED","remarks":".."}}` (or `"DECLINED"`) |
| `get_pending_leaves_for_approver` | `pico.my-leave` `{"view":"approvals"}`. Each item carries `decide` = the exact `instanceId` / `pageCode` / field / values for the call above |
| `get_employee_leave_history` | `pico.my-leave` `{"view":"history","status":"APPROVED"}` (`status` optional) |
| `get_pending_leaves_by_employee` | `pico.my-leave` `{"view":"pending"}` |
| `get_leave_balance` | `pico.my-leave` `{"view":"balance","year":2026}` |
| `get_leave_types` | `pico.my-leave` `{"view":"types"}` |
| `whos_on_leave` | `pico.my-leave` `{"view":"whos-on-leave"}` |

Behaviour changes the app must handle:
- Status values are upper-case: `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN` (was draft-cancel / delete), `CANCELLED`. ERPNext's "Cancellation Inprocess" is now `status=APPROVED` + `cancel_status=REQUESTED`.
- Business errors return `ok:false` with `data.reason` set to `OVERLAP` or `INSUFFICIENT_BALANCE`.
- A guard or validator rejection on `/api/workflow/transition` returns HTTP 409 (guard) or 422 (validator) with a message.
- Withdrawn leave is kept, not deleted.

## Changes from ERPNext (intentional)
- **Balance:** ERPNext counted approved leave of every year against the current allocation. Aria counts only leave inside the allocation's period, and pending leave counts against the balance when applying.
- **Half day:** a half day must be a single date (ERPNext allowed `half_day` on a range).
- **Security:** `employee_number` and the approver are no longer taken from the request.

## Setup after deploy
1. Create leave types: `pico:leave-type-list` (codes must match `leave_kind`, max 16 chars, e.g. CL / SL / EL / LWP).
2. Create allocations: `pico:leave-allocation-list`, or bulk load from ERPNext `tabLeave Allocation`.
3. Set `leave_approver_id` (or `reports_to_emp_id`) on each employee, and their email.
4. Create an `SMTP_EMAIL` integration instance (M11) and set it ACTIVE. Use `dryRun: true` in its config while testing.

## Existing Aria bug (fix in the same PR)
`hr/govforms/ExtendedFormsController.form12` queries columns that don't exist, so Form 12's leave numbers are always 0. It also reads every tenant. Replace the two queries with:

```java
paid = jdbc.queryForObject(
    "SELECT COALESCE(SUM(days),0) FROM m7_leave_request WHERE tenant_id=? AND employee_id=? AND status='APPROVED' AND leave_kind IN ('CL','EL','SL')",
    Integer.class, TenantContext.get(), empId);
lop = jdbc.queryForObject(
    "SELECT COALESCE(SUM(days),0) FROM m7_leave_request WHERE tenant_id=? AND employee_id=? AND status='APPROVED' AND leave_kind='LWP'",
    Integer.class, TenantContext.get(), empId);
```

## Verified
- All 22 JSON literals in the SQL (workflow bodies with guards, page bodies, projection field maps, action params) parse.
- Module 1 + Module 2 Java compiles together against Aria's `target/classes` (Spring 6.1.14). Module 1 still compiles on its own.

## Not verified
- The SQL has not been run against MariaDB.
- The transitions have not been exercised on a running backend: v1 → v2 auto-resync of open instances, projection writes, validator 422s, and mail after commit.
- No Playwright specs yet (workflow happy path + guard rejection per Aria's new-workflow skill).
