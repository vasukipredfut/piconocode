# PICO cut-over: ERPNext → Aria

Assumption: the mobile app gets a new release that talks to Aria (Modules 1–7 mapping tables). Old app builds keep pointing at ERPNext until they are forced to update.

## T-14 days: rehearsal on a copy
1. Restore a fresh ERPNext DB backup and an Aria `earms` copy on a test server.
2. Deploy the Aria build with Modules 1–7 Java and V382–V389; apply the two patches (`IntegrationOutboundCaller.redact`, uploads hardening); add the `WebSecurityConfig` permitAll line (Module 6).
3. `python migrate.py --config migrate.ini --dry-run` → fix every error and read every warning in `migrate.log`.
4. `python migrate.py --config migrate.ini` → `python migrate.py --config migrate.ini --reconcile`. Every DIFF must be explained by a logged skip.
5. Business checks with HR / sales / service users on the test server:
   - 5 employees: profile, today's attendance, history, leave balance, payslip PDF of last month.
   - Pending leave and expense approvals appear for the right approver and can be approved.
   - Open leads / quotes / tickets are in the right stage and can move on.
6. **Parallel payroll:** run `pico.payroll-run` for the last closed month and compare employee by employee with ERPNext's slips. Expected differences: the LOP formula and the salary card split (open questions 6–7 in MIGRATION_PLAN.md). Sign-off by HR is the go / no-go gate.
7. Time the full load; it sets the freeze window below.

## T-7 days: prepare production
- Rotate the secrets listed in MIGRATION_PLAN.md Phase 0.7 (Razorpay, Fast2SMS, SAP DB, Twilio).
- Create Aria integrations: SMTP_EMAIL (ACTIVE), MSG91_SMS + DLT OTP template (optional), TWILIO_WHATSAPP (optional).
- Tenant settings: `payroll.ot_source=OVERTIME_ENTRY`, `payroll.absent_is_lop`, `crm.lead_owner_emp_id` (loaded), `branding.logo_url` (loaded), `service.*`, `whatsapp.*`, `company_name`.
- PF / ESI / PT / TDS settings (`m7_pf_setting`, `m7_esi_setting`, `m7_pt_slab` TN, `m7_tds_slab`) and salary components for the PICO tenant.
- **Initial load into production Aria** (full run). Employees can be told "the new app opens on cut-over day".
- HR splits each migrated salary card (`notes` starts with "Migrated from ERPNext"): basic / DA / HRA / allowances.

## Cut-over day (T)
| Time | Step |
|---|---|
| T-1h | Announce the freeze. Note the timestamp `SINCE` = start of the freeze minus 1 hour (overlap is harmless; the load is an upsert). |
| T | ERPNext read-only: disable the mobile API (`bench --site … set-config maintenance_mode 1`, or block `/api/method/spokes_app.*` at the proxy) and stop the scheduler (`bench --site … disable-scheduler`), so the 23:59 attendance job (and the dummy 18:00 SAP job) don't run. |
| T+5m | Final backup of the ERPNext DB and `sites/<site>/{public,private}/files`. |
| T+10m | Delta load: `python migrate.py --config migrate.ini --since "<SINCE>"`, then `--phases files` (copies new selfies / receipts). |
| T+30m | `--reconcile`; spot-check today's punches and the pending approvals. |
| T+45m | Switch: release the Aria app build / flip the app's base URL; send the "set your password" mail (users run `/api/auth/password-reset/request`; nobody has an Aria password yet). |
| T+1h | Enable `PicoAttendanceProcessor` (23:59) and `PicoAttendanceDigest` (00:30) on Aria; confirm the first punches arrive. |

## After go-live
- **Day 1, 23:59:** check `m7_attendance` rows were written by `PicoAttendanceProcessor`.
- **First Aria payroll month:**
  - Set to PAID every `m7_overtime_entry` that ERPNext already paid (if the cut-over is mid-month, those dated before the last ERPNext slip), so it isn't paid twice:
    ```sql
    UPDATE m7_overtime_entry SET status='PAID' WHERE tenant_id=<t> AND erp_ref IS NOT NULL AND attendance_date <= '<last ERPNext slip end_date>';
    ```
  - Run payroll and approve with HR side-by-side.
- **Keep ERPNext read-only for 90 days** as the archive for GL history and as the rollback path. Then export and decommission.

## Rollback (before payroll is run in Aria)
1. Point the app back to ERPNext (previous app build / base URL); re-enable the ERPNext API and scheduler.
2. Anything entered in Aria after the switch (punches, leave, claims, leads) has to be re-keyed in ERPNext. Export it with `SELECT … WHERE erp_ref IS NULL AND created_at >= '<switch time>'` per table.
3. Aria data stays in place; fix the issue, then repeat the cut-over (the load is an upsert).

After the first Aria payroll is approved, rollback means re-keying payroll in ERPNext: the decision point is the HR sign-off of the parallel run.
