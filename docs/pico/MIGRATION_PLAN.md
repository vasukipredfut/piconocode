# Spokes (ERPNext) → Aria migration plan

Source: `apps/` in this repo (ERPNext v15 + HRMS + custom apps `spokes_app`, `custom_theme`, `customer_support`).
Target: Aria backend (`C:\PMRGOLD\ariabackend`), built the substrate way: `entity_registry`,
`workflow_definition`, `page_definition` and listeners/action handlers. No per-noun `@Entity`,
repository, controller or DTO.

The mobile app moves off the 59 `spokes_app.api.pico_api.*` endpoints and onto Aria's generic APIs:

| Need | Aria API |
|---|---|
| Login / refresh / logout | `POST /api/auth/signin`, `/api/auth/refreshToken`, `/api/auth/signout` |
| Read lists (masters, history) | `POST /api/m0/datasource/query` |
| Read / create / update one record | `GET/POST/PUT /api/m0/entities/{code}[/{id}]` |
| Approvals (leave, expense, quotation, installation) | `POST /api/workflow/transition` |
| Business actions that need server-side identity or rules | `POST /api/m0/actions/{code}/invoke` |
| Files (selfies, receipts, profile photo) | `POST /api/m0/document-vault/upload` |

> Only `spokes_app/spokes_app/` is migrated. `spokes_app/spokes_app/spokes_app/{hooks.py,api,...}`
> and `spokes_app/customer_support/` are stray, older copies. They are not installed on the server.

---

## Phase 0: Foundation (do before any module)

| # | Item | Decision / action |
|---|---|---|
| 0.1 | **Two employee masters in Aria.** Login runs against legacy `employee_details` (`CurrentUser.id()`), but payroll and HR use `m7_employee`. | Keep `m7_employee` as the HR master. Add `m7_employee.user_id` → `employee_details.id`. Every PICO table stores `employee_id` = `m7_employee.id`. *(Done in V382.)* |
| 0.2 | **Self-service data scope.** `datasource/query` `user:` bindings come from the client, so a mobile user could read other employees' rows. | Short term: HR entities get `read_role = Manager,TenantAdmin`, and employees read their own data through `pico.*` actions that resolve identity from the JWT. Long term: write a `/substrate-extension` RFC for an "own rows" read scope on `entity_registry`. |
| 0.3 | ERPNext `Company` → Aria **tenant** | One tenant per company. Map Company Branding to tenant settings (see Module 7). |
| 0.4 | ERPNext roles → Aria roles | HR Manager → `Manager`, System Manager → `TenantAdmin`, Leave/Expense Approver → `Approver` (create if missing), Sales User → `Sales`, Service engineer → `Service`. Confirm the list with the client. |
| 0.5 | Password reset by OTP (`send_reset_otp` / `verify_reset_otp` / `reset_password`) | ✅ Done in Module 7: `PasswordResetController` (`/api/auth/password-reset/*`). |
| 0.7 | **Secrets committed in the ERPNext repo** | Rotate: RazorpayX key + secret (`salary_payment_processor.py`), Fast2SMS key (`pico_api.send_otp_sms`), the SAP dummy-DB root password (`migrate_sap_to_erp.py`), and the Twilio credentials if the site config was shared. |
| 0.8 | **Uploads served publicly with any file type** (Aria) | Fixed by `UploadGuard` + `uploads-hardening.patch` (Module 7). Apply before the app sends selfies / receipts. |
| 0.6 | Flyway numbering | Files here start at **V382** (Aria `main` ends at V380). Renumber to the next free V before merging if `main` has moved. |

---

## Modules (in dependency order)

### Module 1: Attendance & Check-in ✅ drafted → `module-01-attendance/`
- **ERPNext source:** `pico_api.employee_checkin`, `employee_checkout`, `get_employee_checkins`, `get_employee_last_punch`, `today_attendance`, `attendance_history`, `holiday_calendar`, `get_employee_shift`; `overrides/custom_attendance.py` (23:59 cron); DocTypes `HR Attendance Settings`, `HR Setting Custom`, `OverTime Entry`; HRMS `Employee Checkin`, `Shift Type`, `Shift Assignment`, `Holiday List`.
- **Aria:**
  - Reuse `m7_attendance`.
  - New entities: `HolidayList`, `HolidayDate`, `ShiftType` (shift + OT rules, which merges HR Attendance Settings), `ShiftAssignment`, `EmployeeCheckin`, `OvertimeEntry`.
  - Actions: `pico.employee-punch`, `pico.my-attendance`.
  - Scheduled job: `PicoAttendanceProcessor`.
  - 6 list pages.
- No workflow: none of these have approval states.

### Module 2: Leave ✅ drafted → `module-02-leave/`
- **ERPNext source:** `apply_leave`, `approve_reject_leave`, `cancel_leave_application(s)`, `delete_leave_application`, `whos_on_leave`, `get_employee_leave_history`, `get_leave_balance`, `get_leave_types`, `get_pending_leaves_*`; `overrides/leave_application.py` (approver/employee mails, cancellation, revised half day).
- **Aria:**
  - `M7.LeaveRequest` v2 adds `WITHDRAWN`.
  - New workflow `M7.LeaveCancellation` (`REQUESTED → CANCELLED | DECLINED`) keeps APPROVED terminal.
  - Half-day, approver and cancellation columns on `m7_leave_request`; state is written by `m0_workflow_projection`.
  - New entities: `LeaveType`, `LeaveAllocation`; balance is computed in `PicoLeaveRules`.
  - `LeaveApproverValidator` (only the approver decides), `LeaveDecisionListener` (mails, cancellation, revised half day), `TenantMailService` (M11 SMTP, after commit).
  - Actions: `pico.leave-apply`, `pico.leave-cancel`, `pico.my-leave`.
  - `pico.employee-punch` now enforces the half-day leave rules.
  - Fixes the existing Aria bug in `ExtendedFormsController.form12` (queries `emp_id` / `leave_type`, but the columns are `employee_id` / `leave_kind`).

### Module 3: Overtime → Payroll, Salary ✅ drafted → `module-03-payroll/`
- **ERPNext source:** `overrides/salary.py` (OT added into the Salary Slip), `get_salary_slips`, `download_salary_slip_pdf`, `get_employee_salary_profile/structure/components`, `salary_payment_processor.py`.
- **Aria:**
  - Payroll process that Aria lacked: `pico.payroll-run` (attendance summary → `PayrollCalcService` → run / lines / payslip → `M7.PayrollRun` COMPUTED) and `pico.payroll-decide` (bulk APPROVE / PAID).
  - `M7.PayrollRun` v2 fixes the roles; a projection writes state; `PayrollRunValidator` enforces roles; `PayrollStatusListener` updates the payslip and OT entries.
  - OT from `m7_overtime_entry` when `payroll.ot_source=OVERTIME_ENTRY`.
  - `pico.my-salary` for the app.
  - Tenant scoping and role checks added to `PayrollCalcService`, `PayslipController` and `PayrollController`.
  - RazorpayX payouts are **not** migrated: the keys are hard-coded in the source, rotate them.

### Module 4: Expense Claim ✅ drafted → `module-04-expense/`
- **ERPNext source:** `create_expense_claim`, `save_base64_receipt`, `approve_reject_expense_claim`, `get_employee_expenses`, `get_company_expenses`, `get_company_expense_claim_types`, `get_pending_expenses_for_approver`, `send_expense_approval_email`.
- **Aria:**
  - New entities: `ExpenseClaimType`, `ExpenseClaim`, `ExpenseClaimLine`.
  - Workflow `M7.ExpenseClaim`: `SUBMITTED → APPROVED → PAID`, plus `REJECTED` and `WITHDRAWN`; state is written by projection.
  - `ExpenseApproverValidator`: only the approver decides, never the claimant; PAID is TenantAdmin with a pay reference.
  - `ExpenseDecisionListener`: sanctions on approval, mails the employee, optional tenant-scoped GL posting.
  - Receipts go through document-vault.
  - Actions: `pico.expense-submit` (multi-line), `pico.expense-withdraw`, `pico.my-expense`.

### Module 5: CRM (Lead, Quotation, Customer) ✅ drafted → `module-05-crm/`
- **ERPNext source:** `create_lead`, `get_leads`, `get_my_leads`, `update_lead_qualification_status`, `get_all_lead_tracking_history`, `convert_lead_to_customer`, `create_quotation(_from_lead)`, `get_quotations`, `get_customers`, `get_sales_persons`, `get_items`, `send_lead_notification_email`, `send_quotation_email`; DocType `Lead Owner`; `migrate_sap_to_erp.migrate_customers` (18:00 cron).
- **Aria:**
  - `M8.Lead` / `M8.Quote` v2 fix the roles and add PICO shortcuts; projections write state.
  - New `m8_customer` (Aria had no customer master), `m8_quote_line` with GST, `m8_lead_city`; `m5_sku_master.machine_id`.
  - `CrmAccess` / `CrmAccessValidator` scope leads and quotes to the people on them.
  - Actions: `pico.lead-create`, `pico.lead-update`, `pico.lead-convert`, `pico.quote-create`, `pico.my-crm`.
  - SAP sync is dropped: there is no real SAP system (confirmed); the ERPNext job only read a local dummy DB. `m8_customer.sap_customer_code` / `source='SAP'` stay unused.

### Module 6: Service (Installation Report, Customer Support) ✅ drafted → `module-06-service/`
- **ERPNext source:** `create_installation_report`, `verify_installation_report_otp`, `send_otp_email/sms`, `get_(all_|employee_)installation_report(s)`, `download_installation_report_pdf`, `get_issues`; DocType `Installation Report` (submittable); `customer_support.api.create_issue` / `get_customer_issues` and the `/support` web form.
- **Aria:**
  - `m9_installation_report` + `M9.InstallationReport` (`OTP_PENDING → VERIFIED | CANCELLED`).
  - Hardened OTP (`InstallationOtpService`): 6 digits, hashed, expiry, 5 tries, MSG91 / email. An OTP check can't be a guard (guards have no DB access), so `ServiceAccessValidator` blocks VERIFIED without the action's stamp.
  - PDF via Flying Saucer.
  - Issues map to `m9_service_ticket` + `M9.ServiceTicket` v2, plus comments.
  - Public portal `PublicSupportController` (`/api/public/support/{tenantCode}`, mobile-verified, rate-limited) needs a `WebSecurityConfig` permitAll line.
  - `IntegrationOutboundCaller.redact` patch so OTPs never reach `integration_event_log`.
  - The Fast2SMS key hard-coded in ERPNext must be rotated.

### Module 7: Reports, Branding, Notifications ✅ drafted → `module-07-platform/`
- **ERPNext source:** `custom_theme` (6 script reports, `Company Branding`, `Theme Preference`, My Preferences page, desk CSS); `company_branding`, `user_profile`, `update_user_profile`, `get_notifications`, `send_reset_otp` / `verify_reset_otp` / `reset_password`; `attendance_report.send_attendance_report_to_whatsapp`; `user.py`; `fuel_attendant.py`.
- **Aria:**
  - Four reports were already list pages from Modules 1–6; Lead and Leave report pages are added.
  - Theme Preference already exists in Aria (`/api/account/preferences`).
  - Branding uses the `branding.logo_url` tenant setting.
  - `pico.my-profile` (contact fields only); `m7_announcement` + `pico.my-notifications`.
  - Secure password reset at `/api/auth/password-reset/*` (closes Phase 0.5).
  - `TWILIO_WHATSAPP` adapter + daily digest.
  - `UploadGuard` + patch for the stored-XSS hole in Aria's `/uploads`.
  - `fuel_attendant.py` is not migrated (unrelated, unwired); confirm with the client.

### Module 8: Data migration & cut-over ✅ drafted → `module-08-migration/`
- `migrate.py` loads straight from the ERPNext DB into the Aria DB instead of CSV through the bulk API. The bulk API can't resolve links (employee / approver / customer ids) or create workflow instances for open documents.
- 10 phases in dependency order, idempotent by `erp_ref` (V389 adds UNIQUE indexes), `--dry-run` (rolled-back transaction), `--since` delta for cut-over day, `--reconcile` counts and sums.
- Open documents get open workflow instances, so approvals continue in Aria.
- Passwords and plain-text OTPs are not migrated: users reset their password; engineers resend codes.
- `CUTOVER.md`: rehearsal, parallel payroll (go / no-go), freeze + delta + switch timeline, rollback.
- **Never run against a database yet**: the rehearsal on copies is the first real test.

---

## Open questions for the client
1. OT hourly rate: ERPNext uses `Salary Structure Assignment.base / 30 / 8`. Aria has `basic_monthly`, `da_monthly` and others. Which components make up "base"? (`PicoAttendanceProcessor` uses `basic_monthly + da_monthly`.)
2. The ERPNext nightly job never creates **Absent** rows; the mobile history infers Absent. Keep that, or have Aria write ABSENT rows?
3. What does the mobile app send in the checkin `status` field? It is stored in ERPNext but never read.
4. Salary payouts: `salary_payment_processor.py` is a RazorpayX test prototype with hard-coded keys. Should Aria automate payouts (an M11 adapter), or record PAID with the bank reference after a manual / bulk-upload transfer?
5. Leave balance: ERPNext counted approved leave from every year against the current allocation. Aria counts only the allocation's period and includes pending leave when applying. Confirm this is wanted.
6. Absent days: ERPNext's default payroll deducts only LWP, but Aria deducts unmarked absence when `payroll.absent_is_lop=true`. Which did PICO use?
7. LOP formula: Aria uses `gross / payable_days × lop_days`, while common practice is `gross / calendar days`. Confirm.
8. Quotation default price: ERPNext `get_items` showed the item's valuation rate (cost) as its price. Aria uses `sale_price`. Confirm the price list to load.
9. ~~SAP customer sync: is there a real SAP system?~~ **Answered (2026-10-26): no.** Customers are mastered in Aria (`m8_customer`: lead conversion, manual entry, one-time ERPNext import). No SAP adapter will be built.
10. `fuel_attendant.py` ("Fuel Attendar" registration with OTP): part of PICO, or another product? It is not wired in hooks and its DocType is not in the repo.
11. WhatsApp: which numbers should get the daily attendance summary, and is there an approved WhatsApp sender (the ERPNext code used the Twilio sandbox)?
