# Module 5: CRM (Lead, Customer, Quotation)

Requires Modules 1–4. It uses `PicoSelf` and `CurrentRole` (Module 1) and `TenantMailService` / `PicoLeaveRules` (Module 2).

| File | Goes to (Aria repo) |
|---|---|
| `V386__pico_crm.sql` | `src/main/resources/db/migration/` (renumber if V386 is taken) |
| `java/com/aria/crm/CrmAccess.java` | `src/main/java/com/aria/crm/` |
| `java/com/aria/crm/CrmAccessValidator.java` | same |
| `java/com/aria/crm/CrmMails.java` | same |
| `java/com/aria/action/handler/PicoLeadCreateActionHandler.java` | `src/main/java/com/aria/action/handler/` |
| `java/com/aria/action/handler/PicoLeadUpdateActionHandler.java` | same |
| `java/com/aria/action/handler/PicoLeadConvertActionHandler.java` | same |
| `java/com/aria/action/handler/PicoQuoteCreateActionHandler.java` | same |
| `java/com/aria/action/handler/PicoMyCrmActionHandler.java` | same |

No existing Aria Java file is replaced.

## What Aria had vs. what this adds

| | Aria before | This module |
|---|---|---|
| Lead | `m8_lead` + `M8.Lead` ladder NEW → CONTACTED → QUALIFIED → QUOTED → WON/LOST; NEW/CONTACTED owned by role `Dispatch`; state never written back to the table | v2: same states and pages, role `Sales`, PICO shortcuts (qualify / quoted / convert / lost from any open stage); projection writes `stage`/`status`/`closed_at`; company, city, sales person, creator, qualification and customer link added |
| Customer | **none** (invoices store `customer_name` text) | `m8_customer` (entity `Customer`), created by conversion or manually (no SAP system; `sap_customer_code` unused) |
| Quotation | `m8_quote` header only; DRAFT owned by `Dispatch` | v2 roles; `m8_quote_line` with GST; projection writes `status`/`sent_at`/`decided_at` |
| Items | `m5_sku_master` | reused; `machine_id` added (ERPNext `custom_machine_id`) |
| Lead City / Lead Owner | none | `m8_lead_city` (city → default sales person); tenant setting `crm.lead_owner_emp_id` |

**Access** (`CrmAccess` + `CrmAccessValidator`). ERPNext's CRM APIs were all `allow_guest=True`.
- **Manager / TenantAdmin:** all leads, quotes and customers.
- **Sales users** (role `Sales` or `m7_employee.is_sales_person=1`): customers and items, plus the leads / quotes they are on (sales person, creator or lead owner).
- **Any employee:** can create a lead and sees the leads they created.
- **Transitions:** only people on the lead / quote, or a Manager, can fire them.
- The generic API read for `Lead` / `Quote` is now `Sales,Manager,TenantAdmin` (was `*`).

## Mobile app: old → new calls

| ERPNext `pico_api.*` | Aria |
|---|---|
| `create_lead` | `pico.lead-create` `{"lead_name":"..","company_name":"..","mobile_no":"..","email_id":"..","territory":"..","city_town":"Coimbatore","sales_person_emp_id":12}` |
| `update_lead_qualification_status` | `pico.lead-update` `{"lead_id":5,"qualification_status":"QUALIFIED"}`; also `stage: CONTACTED / LOST` (+`lost_reason`), `next_action_on`, `notes` |
| `convert_lead_to_customer` | `pico.lead-convert` `{"lead_id":5,"gstin":"..","address":".."}` |
| `create_quotation_from_lead` | `pico.quote-create` `{"lead_id":5,"items":[{"sku_code":"PM-200","qty":1,"rate":150000}],"valid_until":"2026-11-30"}` |
| `create_quotation` | `pico.quote-create` `{"customer_id":9,"items":[...]}` (`item_code` accepted as alias of `sku_code`) |
| quotation accepted / rejected *(new)* | `POST /api/workflow/transition` `{"instanceId":<quote workflow_instance_id>,"pageCode":"aria:quote-outcome","formData":{"outcome":"ACCEPTED"}}` |
| `get_leads`, `get_my_leads` | `pico.my-crm` `{"view":"leads","stage":"NEW","search":".."}` |
| `get_all_lead_tracking_history` | `pico.my-crm` `{"view":"tracking","from_date":"..","to_date":".."}`; one lead: `{"view":"lead","lead_id":5}` |
| `get_quotations` | `pico.my-crm` `{"view":"quotes"}`; one with lines: `{"view":"quote","quote_id":3}` |
| `get_customers` | `pico.my-crm` `{"view":"customers","search":".."}` |
| `get_sales_persons` | `pico.my-crm` `{"view":"sales-persons"}` |
| `get_items` | `pico.my-crm` `{"view":"items","search":".."}` |

Behaviour changes:
- **Stages:** lead stages are `NEW / CONTACTED / QUALIFIED / QUOTED / WON / LOST` (ERPNext "Converted" = `WON`). Qualification is `UNQUALIFIED / IN_PROCESS / QUALIFIED`.
- **Quote pricing:** the default rate is the item's `sale_price`. ERPNext's `get_items` returned `valuation_rate` (cost) as `item_price`. A rate sent by the app still overrides.
- **Quote lifecycle:** a quotation is SENT on creation (ERPNext submitted it at once), and the party is mailed if an email is on file. Accept / reject is new.
- **Duplicates:** `pico.lead-create` returns `open_leads_with_same_mobile` so the app can warn about duplicates.

## Not migrated: `migrate_sap_to_erp.py`

It reads `kna1` from a local MySQL `sap_dummy` database as `root` with a hard-coded password, so it is a test stub, not a live SAP integration. **Confirmed: PICO has no real SAP system**, so nothing replaces it. Customers are mastered in Aria. `sap_customer_code` / `source='SAP'` stay unused, and would only matter if SAP ever arrives. Rotate that MySQL root password if it is used anywhere.

## Setup after deploy
1. Create role `Sales` (or set `is_sales_person=1` on the sales employees).
2. Fill `pico:lead-city-list` (city → sales person).
3. Set `crm.lead_owner_emp_id` in `m0_tenant_setting` (the ERPNext Lead Owner).
4. Load items into `m5_sku_master` with `sale_price`, `gst_rate_pct` and `machine_id`. Note that `brand`, `product_family` and `carton_weight_kg` are NOT NULL there (FMCG fields): use e.g. brand `PICO`, product_family = machine type, carton weight 0.
5. Load customers from ERPNext `tabCustomer` into `m8_customer` (`source='IMPORT'`, `erp_ref`).

## Known limits
- GST is a single `gst_rate_pct` per line. There is no CGST/SGST vs IGST split by place of supply (Aria's `GstCalcController` does that for invoices; wire it in when quotes become invoices).
- `M8.Quote` EXPIRED is a `system.schedule` transition, but nothing in Aria fires it yet.

## Verified
- All 34 JSON literals in the SQL parse.
- Modules 1–5 compile together against Aria's `target/classes`. Modules 1–4 still compile alone.

## Not verified
- The SQL has not been run.
- No lead, quote or conversion has gone through the workflows on a running backend.
