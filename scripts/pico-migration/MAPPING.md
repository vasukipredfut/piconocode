# ERPNext → Aria data mapping (migrate.py)

- **Keys and re-runs:** every target row gets `erp_ref` = the ERPNext document name. Tables without one use a natural key (see the Key column). Re-runs update rows in place.
- **Company filter:** source rows are limited to `erp_company` wherever the DocType has a `company` column.
- **Custom fields:** custom fields that are missing on a site are read as NULL. Columns are looked up in `information_schema` at run time.

| Phase | ERPNext | Aria | Key | Notes |
|---|---|---|---|---|
| masters | Holiday List (+ Holiday) | `m7_holiday_list` (+ `m7_holiday`) | erp_ref | Weekly-off rows are **not** copied; Aria derives them from `weekly_off`. Holidays are replaced per list. |
| | Shift Type + HR Attendance Settings | `m7_shift_type` | erp_ref | `shift_code` = 8-char slug of the name (fixed after the first load). OT / half-day rules come from the HR Attendance Settings row of that shift. |
| | Leave Type | `m7_leave_type` | erp_ref | `type_code` = 16-char slug. `is_lwp`, `include_holiday`, `allow_negative`, `max_leaves_allowed`. |
| | Expense Claim Type | `m7_expense_claim_type` | erp_ref | `default_account` is **not** mapped (ERPNext account names ≠ Aria GL codes); set by hand if `expense.gl_posting` is used. |
| | Notifications (custom) | `m7_announcement` | erp_ref | HTML stripped to text. |
| | Company Branding.logo | setting `branding.logo_url` | — | File copied in the files phase. |
| people | Employee (+ User, Has Role) | `employee_details` (login) + `m7_employee` | username / erp_ref | Login: username = ERPNext user id (email), role by `role_map` (System Manager → TenantAdmin, HR → Manager, Sales → Sales, Maintenance → Service, else Employee). **Passwords are not migrated**: users set one with `/api/auth/password-reset`. |
| | Employee.status | `m7_employee.status` | | Active → ACTIVE, Left → LEFT, Inactive / Suspended → INACTIVE. |
| | Employee links | `reports_to_emp_id`, `leave_approver_id`, `expense_approver_id`, `holiday_list_id`, `shift_code` | | Second pass. Approvers are users (emails) and resolve via the migrated login; unresolved ones are logged. |
| | Sales Person.employee | `m7_employee.is_sales_person = 1` | | |
| | Shift Assignment (submitted) | `m7_shift_assignment` | employee + start + shift | |
| | Lead Owner | setting `crm.lead_owner_emp_id` | | |
| | Lead City (custom) | `m8_lead_city` | erp_ref | |
| salary | Salary Structure Assignment (latest submitted) | `m7_employee_salary` | erp_ref | `basic_monthly = base`, `ctc_annual = base×12 + variable`. **HR must split** into DA / HRA / allowances (flagged in `notes`) before the first Aria payroll. |
| attendance | Attendance (submitted) | `m7_attendance` | employee + date | Present / Absent / Half Day / On Leave / Work From Home → PRESENT / ABSENT / HALF_DAY / ON_LEAVE / WFH; `source = IMPORT`. |
| | Employee Checkin | `m7_employee_checkin` | erp_ref | IN / OUT only; `attach_image` → `image_url` (files phase). |
| | OverTime Entry (custom) | `m7_overtime_entry` | erp_ref | Rejected → CANCELLED, else OPEN. Months already paid in ERPNext: mark PAID by hand, or the first Aria payroll would pay them again (see CUTOVER.md). |
| leave | Leave Allocation (submitted) | `m7_leave_allocation` | erp_ref | `allocated_days = total_leaves_allocated`. |
| | Leave Application | `m7_leave_request` | erp_ref | Open → PENDING (**open workflow instance**), Approved (submitted) → APPROVED, Rejected → REJECTED, Cancelled → CANCELLED, "Cancellation Inprocess" → APPROVED + `cancel_status = REQUESTED` (open `M7.LeaveCancellation` instance). Half day + which half, revised half-day request. |
| payroll | Salary Slip (submitted) + Salary Detail | `m7_payroll_run` (PAID) + `m7_payroll_line` + `m7_payslip` (PAID) | erp_ref / employee + period | One slip per employee and month (later duplicates are skipped and logged). Statistical / not-in-total components are dropped. `html_snapshot` is a generated printable slip, so `pico.my-salary` slip-pdf works for history. |
| expense | Expense Claim + Detail | `m7_expense_claim` + lines | erp_ref | Draft → SUBMITTED (**open instance**), Approved → APPROVED (**open instance**, awaiting payment), Approved + paid → PAID, Rejected → REJECTED, cancelled → WITHDRAWN. `receipt` → `receipt_url` (files phase). |
| crm | Item | `m5_sku_master` | sku_code | `brand` = Item.brand or `default_brand`, `product_family` = item group, `offer_variant` = item name, `sale_price` = standard_rate, `purchase_price` = valuation_rate, `machine_id` = custom_machine_id; GST = `default_gst_pct` on insert. |
| | Customer | `m8_customer` | erp_ref | `customer_code` = 32-char slug of the ERPNext name; `source = IMPORT`; custom city / sales person; lead link. |
| | Lead | `m8_lead` | erp_ref | Lead / Open → NEW, Replied → CONTACTED, Opportunity / Interested → QUALIFIED, Quotation → QUOTED, Converted → WON, Do Not Contact / Lost Quotation → LOST. Open stages get an open `M8.Lead` instance. `lead_created_by` (a name) matches a unique employee name. |
| | Quotation + Item | `m8_quote` + `m8_quote_line` | erp_ref | Draft → DRAFT, Open / Replied → SENT, Ordered → ACCEPTED, Lost → REJECTED, Expired → EXPIRED (cancelled ones skipped). ERPNext taxes are header-level, so lines carry 0 GST and the header keeps `tax_total`. |
| service | Installation Report (custom) | `m9_installation_report` | erp_ref | verified → VERIFIED (`otp_verified_at`), else OTP_PENDING (**the plain-text ERPNext OTP is not copied; engineers must resend**), cancelled → CANCELLED. uw / amc / ow / o → `warranty_kind`. |
| | Issue + Comment | `m9_service_ticket` + `m9_ticket_comment` | erp_ref | Open → OPEN, Replied / On Hold → AWAITING_CUSTOMER, Resolved → RESOLVED, Closed → CLOSED; Urgent / High / Medium / Low → P1–P4. Comments imported as internal. |
| files | `/files/…`, `/private/files/…` in photo / selfie / receipt / logo columns | `/uploads/erp/erp-<sha1>.<ext>` | — | Copied from `erp_files_dir`. Only JPG / PNG / WEBP / PDF (Aria's UploadGuard list, by magic bytes); other files are cleared and logged. |

## Not migrated (by design)

| ERPNext | Why |
|---|---|
| User passwords | ERPNext hashes can't be used by Aria's BCrypt login. Users reset theirs with `/api/auth/password-reset`. |
| OTPs (installation, password reset) | Stored in plain text in ERPNext. Open installation reports need a resend. |
| GL / journal entries, Payroll Entry, Salary Payment Processor | Finance history stays in ERPNext (read-only archive); Aria posts from the first Aria payroll / claim. |
| Salary Structure formulas | Aria salary cards hold amounts, not formulas (see the salary row above). |
| Fuel Attendar, desk customisations, workspaces | Not part of the PICO app (see Module 7). |
