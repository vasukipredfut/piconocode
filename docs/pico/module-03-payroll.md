# Module 3: Overtime → Payroll, Salary

Requires Modules 1 and 2.

| File | Goes to (Aria repo) |
|---|---|
| `V384__pico_payroll.sql` | `src/main/resources/db/migration/` (renumber if V384 is taken) |
| `java/com/aria/hr/payroll/PayrollCalcService.java` | `src/main/java/com/aria/hr/payroll/`. **Replaces the existing file** |
| `java/com/aria/hr/payroll/PayslipController.java` | same. **Replaces the existing file** |
| `java/com/aria/hr/payroll/PayrollController.java` | same. **Replaces the existing file** |
| `java/com/aria/hr/payroll/PicoAttendanceSummaryBuilder.java` | same |
| `java/com/aria/hr/payroll/PayrollRunValidator.java` | same |
| `java/com/aria/hr/payroll/PayrollStatusListener.java` | same |
| `java/com/aria/action/handler/PicoPayrollRunActionHandler.java` | `src/main/java/com/aria/action/handler/` |
| `java/com/aria/action/handler/PicoPayrollDecideActionHandler.java` | same |
| `java/com/aria/action/handler/PicoMySalaryActionHandler.java` | same |

## What was missing in Aria

Aria had a payroll calculator (`PayrollCalcService`: PF / ESI / PT / TDS / LOP for one employee and month) but no payroll process:

- Nothing ran payroll for a month.
- Nothing filled `m7_attendance_summary`, so every payslip used the default of 26 paid days and 0 LOP.
- Nothing wrote `m7_payroll_run` / `m7_payroll_line`. The `M7.PayrollRun` workflow existed, but its DRAFT / APPROVED steps were assigned to the M10 gate role `Dispatch`.
- `PayrollCalcService` read `m7_employee` / `m7_employee_salary` / `m7_attendance_summary` without `tenant_id`.
- `PayslipController` wrote every payslip as tenant 1.
- `/api/m7/payroll/preview` and `/api/m7/payslip/*` accepted any `empId` from any logged-in user (salary, PAN, bank account). The generic API also let any user read `PayrollRun` / `Payslip` / `EmployeeSalary` (`read_role='*'`).

## Flow

```
pico.payroll-run {period}
  per employee with a salary card:
    PicoAttendanceSummaryBuilder → m7_attendance_summary (attendance + approved leave + holidays)
    PayrollCalcService.compute   → OT from m7_overtime_entry (payroll.ot_source=OVERTIME_ENTRY)
    m7_payroll_run + m7_payroll_line + m7_payslip (HTML snapshot)
    M7.PayrollRun: DRAFT ──aria:pr-compute──► COMPUTED
pico.payroll-decide {period, decision: APPROVE}            COMPUTED ──aria:pr-approve──► APPROVED  → payslip visible to employee
pico.payroll-decide {period, decision: PAID, pay_reference} APPROVED ──aria:pr-disburse─► PAID      → OT entries PAID + payslip_id
```

- Recompute is safe. COMPUTED rows are recomputed in place, and APPROVED / PAID rows (and their payslips) are never overwritten.
- `PayrollRunValidator`: only Manager / TenantAdmin may compute and approve, and only TenantAdmin may mark PAID.
- **OT, ERPNext parity:** the OT earning is the sum of the month's `m7_overtime_entry.ot_amount` (already priced per shift and holiday by `PicoAttendanceProcessor`). It is added after LOP, so absent days don't prorate it. Other tenants keep Aria's original 2x-basic rule (`payroll.ot_source` unset).

## Tenant settings for PICO

```sql
INSERT INTO m0_tenant_setting (tenant_id, setting_key, setting_value) VALUES
  (<pico_tenant_id>, 'payroll.ot_source', 'OVERTIME_ENTRY'),
  (<pico_tenant_id>, 'payroll.absent_is_lop', 'true')
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);
```

Also make sure the tenant has `m7_pf_setting`, `m7_esi_setting`, `m7_pt_slab` (state TN), `m7_salary_component` rows and a salary card (`m7_employee_salary`) per employee. Without PF/ESI settings the run reports a clear error per employee (it used to throw).

## Mobile app: old → new calls

| ERPNext `pico_api.*` | Aria `pico.my-salary` args |
|---|---|
| `get_salary_slips` (list) | `{"view":"slips","year":2026}` |
| `get_salary_slips` (with `salary_slip`) | `{"view":"slip","period":"2026-10"}`: totals + earnings / deductions |
| `download_salary_slip_pdf` | `{"view":"slip-pdf","period":"2026-10"}`: `data.pdf_base64` + `filename` (decode in the app) |
| `get_employee_salary_profile` | `{"view":"profile"}` |
| `get_employee_salary_structure` | `{"view":"structure"}` |
| `get_employee_salary_components` | `{"view":"components"}` |

Behaviour changes:
- Payslips are identified by period (`YYYY-MM`), not an ERPNext slip name.
- Only APPROVED / PAID slips are visible.
- PAN and bank account numbers are masked (last 4 digits).
- The PDF comes back as base64 JSON instead of a binary download.

## Not migrated: `salary_payment_processor.py` (RazorpayX payouts)

- It has **RazorpayX test API key + secret hard-coded in the source** (and now in the `piconocode` git repo). Rotate them in the Razorpay dashboard, whether or not this module ships.
- It sends every payout contact to one hard-coded email / phone, uses a test account number, and calls `make_salary_slip`, which it never imports. It cannot have worked as written.

In Aria, PAID is recorded with the bank's reference (`pay_reference`) after HR pays through the bank. If automatic payouts are wanted, build it as an M11 adapter (`/new-adapter`) with keys in `IntegrationSecretService` (decision for the client).

## Open points for the client
1. **Absent days:** ERPNext's default payroll deducts only leave without pay, not unmarked absence. Aria deducts absence when `payroll.absent_is_lop=true`. Which did PICO use?
2. **LOP formula:** Aria's existing formula is `gross / payable_days × lop_days`. The common Indian practice is `gross / calendar days × lop_days`. Unchanged here; confirm before the first parallel run.
3. **TDS:** the `m7_tds_slab` lookup is not tenant-scoped (unchanged; statutory slabs are global). Duplicate slab rows across tenants would double-count.
4. **GL posting:** `GlAutoPostingService.postPayroll` reads `m7_payslip` without `tenant_id` (existing; not changed here, fix with Module 4 / finance).

## Verified
- All 7 JSON literals in the SQL parse.
- Modules 1 + 2 + 3 compile together against Aria's `target/classes` (Spring 6.1.14). Modules 1 and 1 + 2 still compile on their own.

## Not verified
- The SQL has not been run.
- No payroll has been computed on a real database (summary classification, statutory math with PICO data, PDF rendering of the snapshot by Flying Saucer).
- Aria's own tests have not been run against the replaced `PayrollCalcService`.
