# Module 8: ERPNext data migration & cut-over

| File | What |
|---|---|
| `V389__pico_migration_prep.sql` | Aria migration: `erp_ref` on the remaining tables, UNIQUE `(tenant_id, erp_ref)` for idempotent loads, roles `Sales` / `Service` / `Employee` |
| `migrate.py` | ETL: ERPNext MariaDB → Aria MariaDB, 10 phases in dependency order, `--dry-run`, `--since` delta, `--reconcile` |
| `transforms.py` | Pure value mappings (status codes, slugs, files, payslip snapshot) |
| `test_transforms.py` | 13 offline tests: `python -m unittest test_transforms -v` |
| `migrate.ini.example` | Connection + options (copy to `migrate.ini`; it is git-ignored) |
| `MAPPING.md` | Field-by-field mapping and what is deliberately not migrated |
| `CUTOVER.md` | Rehearsal, production prep, cut-over day timeline, rollback |

```bash
pip install pymysql
cp migrate.ini.example migrate.ini      # fill in both databases, tenant, company
python migrate.py --config migrate.ini --dry-run
python migrate.py --config migrate.ini
python migrate.py --config migrate.ini --reconcile
python migrate.py --config migrate.ini --since "2026-11-28 17:00"     # cut-over delta
python migrate.py --config migrate.ini --phases files                 # copy photos / selfies / receipts
```

## How it behaves
- **Reads** ERPNext through SQL with a read-only user. Columns are discovered from `information_schema`, so custom fields missing on a site come through as NULL instead of failing. Rows are filtered to `erp_company`.
- **Writes** one Aria tenant:
  - Every row is keyed by `erp_ref` (or a natural key such as employee + date), so a re-run updates instead of duplicating.
  - Child lines are replaced per parent.
  - Each phase commits on its own; a failing phase is rolled back and can be re-run.
- **Workflow state:**
  - Open documents get an open `workflow_instance` at the matching state, so approvers find them in their queues: pending leave, cancellation requests, submitted / approved-unpaid claims, open leads / quotes, unverified installation reports, open tickets.
  - Closed history gets no instance.
  - On a delta run, instances follow the source: approved in ERPNext after the first load means the instance is closed.
- **Bad rows** (duplicate business key, broken link) are rejected one by one and logged; the run continues. Every skip is a `WARNING` in `migrate.log`; `--reconcile` shows where counts differ.
- **`--dry-run`** does the whole load in one transaction and rolls it back. Constraint errors surface without changing anything.

## Verified
- The 13 transform tests pass (they caught a rounding bug: `Decimal.quantize` defaults to half-even, while Aria rounds half-up).
- `migrate.py` compiles.
- Source column names were checked against the DocType JSON in this repo (erpnext / hrms / spokes_app / custom_theme). Custom fields that only exist in the site database (e.g. `Employee.leave_approver`, `Lead.city_town`, `Customer.custom_sales_person`) are read when present.

## Not verified
- **`migrate.py` has never run against a database.** No ERPNext copy and no Aria test schema were available here.
- Run the `--dry-run` rehearsal (CUTOVER.md T-14) on copies before trusting it.
- Expect fixes for site-specific custom fields and data quality.
