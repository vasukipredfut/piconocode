# Module 4: Expense Claim

Requires Modules 1–3. It uses `PicoSelf` and `CurrentRole` (Module 1) and `TenantMailService` / `PicoLeaveRules` (Module 2).

| File | Goes to (Aria repo) |
|---|---|
| `V385__pico_expense_claim.sql` | `src/main/resources/db/migration/` (renumber if V385 is taken) |
| `java/com/aria/hr/expense/ExpenseMails.java` | `src/main/java/com/aria/hr/expense/` |
| `java/com/aria/hr/expense/ExpenseApproverValidator.java` | same |
| `java/com/aria/hr/expense/ExpenseDecisionListener.java` | same |
| `java/com/aria/action/handler/PicoExpenseSubmitActionHandler.java` | `src/main/java/com/aria/action/handler/` |
| `java/com/aria/action/handler/PicoExpenseWithdrawActionHandler.java` | same |
| `java/com/aria/action/handler/PicoMyExpenseActionHandler.java` | same |

No existing Aria file is replaced. Aria had no employee expense claim; the legacy `expenses` / `cexpense` / `expense_category` tables are Idayam company-cost masters and are not used.

## Flow

```
pico.expense-submit ──► m7_expense_claim + lines (SUBMITTED) ──► M7.ExpenseClaim
   SUBMITTED ──pico:expense-decide, decision=APPROVED──► APPROVED ──pico:expense-pay (pay_reference)──► PAID
             ──pico:expense-decide, decision=REJECTED──► REJECTED
             ──pico:expense-withdraw─────────────────► WITHDRAWN
```

- **State → table:** the `m0_workflow_projection` row writes `status`, `current_step`, `decided_at`, `paid_at`, `approver_remarks` and `pay_reference`.
- **Who can decide:** `ExpenseApproverValidator`:
  - Approve / reject: only the claim's approver (`expense_approver_id`, falling back to `reports_to_emp_id`), never the claimant. TenantAdmin can override.
  - Withdraw: only the claimant.
  - PAID: only TenantAdmin, and a `pay_reference` is required.
- **On APPROVED:** `ExpenseDecisionListener` sets every line's `sanctioned_amount = amount` and the claim's `total_sanctioned` (as ERPNext's approve did). The employee is mailed on approve, reject and reimburse.
- **GL (optional):** with `expense.gl_posting=true`, approval posts Dr each type's `default_account` / Cr `expense.payable_account`, and payment posts Dr payable / Cr `expense.bank_account` into `m6_gl_entry`, tenant-scoped. Every claim type then needs a `default_account`; a type without one is not posted.

## Mobile app: old → new calls

| ERPNext `pico_api.*` | Aria |
|---|---|
| `create_expense_claim` | Upload each receipt first: `POST /api/m0/document-vault/upload` (multipart `file`) → `url`. Then `pico.expense-submit` `{"expense_type":"TRAVEL","amount":450,"expense_date":"2026-10-20","description":"..","receipt_url":"/uploads/.."}` or several lines: `{"lines":[{...},{...}],"remarks":".."}` |
| `approve_reject_expense_claim` | `POST /api/workflow/transition` `{"instanceId":<workflow_instance_id>,"pageCode":"pico:expense-decide","formData":{"decision":"APPROVED","remarks":".."}}` |
| `get_pending_expenses_for_approver` | `pico.my-expense` `{"view":"approvals"}`. Each item carries the `decide` call |
| `get_employee_expenses` | `pico.my-expense` `{"view":"claims","status":"APPROVED"}`; one claim with lines: `{"view":"claim","claim_id":7}` |
| `get_company_expenses` | `pico.my-expense` `{"view":"company"}` (Manager / TenantAdmin only) |
| `get_company_expense_claim_types` | `pico.my-expense` `{"view":"types"}` |
| *(new)* withdraw | `pico.expense-withdraw` `{"claim_id":7,"reason":".."}` |
| *(new)* reimburse (finance) | `POST /api/workflow/transition` `{"instanceId":..,"pageCode":"pico:expense-pay","formData":{"pay_reference":"UTR123","pay_remarks":".."}}` |

Behaviour changes:
- Receipts are uploaded as files, not sent as base64 in the JSON body.
- `payable_account` is no longer sent by the app. GL accounts come from the claim type and tenant settings.
- Status values: `SUBMITTED` (was Draft / Open), `APPROVED`, `REJECTED`, `WITHDRAWN`, `PAID`.
- `get_company_expenses` was readable by anyone. It is now Manager / TenantAdmin only.

## Setup after deploy
1. Create claim types: `pico:expense-type-list` (codes ≤ 32 chars). Set `max_amount`, `requires_receipt` and `default_account` if GL is used.
2. Set `expense_approver_id` (or `reports_to_emp_id`) on each employee.
3. Optional GL:
   ```sql
   INSERT INTO m0_tenant_setting (tenant_id, setting_key, setting_value) VALUES
     (<pico_tenant_id>, 'expense.gl_posting', 'true'),
     (<pico_tenant_id>, 'expense.payable_account', '<reimbursements payable code>'),
     (<pico_tenant_id>, 'expense.bank_account', '<bank code>')
   ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);
   ```

## Verified
- All 17 JSON literals in the SQL parse.
- Modules 1 + 2 + 3 + 4 compile together against Aria's `target/classes`. Each earlier prefix (1, 1–2, 1–3) still compiles alone.

## Not verified
- The SQL has not been run.
- No claim has gone through the workflow on a running backend (projection, validator 422s, mails after commit, GL lines).
- The document-vault upload has not been checked from the mobile app.
