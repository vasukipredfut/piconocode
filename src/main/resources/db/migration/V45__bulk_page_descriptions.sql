-- V45: Bulk-inject ERP-style page-guide descriptions across every category.
-- The substrate ListView header now displays page_definition.description as
-- an italic subtitle under the title. Per Zoho / SAP / Tally convention,
-- every screen should answer "what / who / when" in one line.
-- ===========================================================================

-- ---- M5: MES / Production / Dispatch ----
UPDATE page_definition SET description='Active production orders. Plant Manager + Supervisor — schedule, approve job cards, track WIP.' WHERE code='pico:production-order-list' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Sales orders pending dispatch. Dispatch officer reconciles loaded qty against this list before billing.' WHERE code='pico:sales-order-list' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Active packing runs. Packing supervisor — start/stop, capture spoilage, close to FG stock.' WHERE code='pico:packing-run-list' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Goods Receipt Notes pending storage. Store keeper — receive against PO, capture batch, allocate bin.' WHERE code IN ('pico:grn-list','pico:grn-store','pico:grn-from-po') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Material requisitions from departments. Store manager approves; bin stock deducts on issue.' WHERE code IN ('pico:mr-list','pico:mr-approve','pico:mr-issue') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Job cards open on the shop floor. Operator captures consumption, supervisor closes.' WHERE code IN ('pico:jc-list','pico:job-card-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Live stock balance per item × bin × batch. Updated on every movement (RECEIPT/ISSUE/CONSUME/SPOIL/ADJUST).' WHERE code IN ('pico:stock-list','pico:stock-balance') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Immutable ledger of every stock movement. Used by Auditor + Finance for period close.' WHERE code IN ('pico:stock-movement-list','pico:stock-movements') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Quality plans per item × stage. QC Officer references sampling AQL + parameter spec.' WHERE code IN ('pico:qp-list','pico:quality-plan-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='QC test results captured against each plan. Pass/fail per parameter; photos attached.' WHERE code IN ('pico:qt-list','pico:quality-test-list','pico:qc-test-result-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Outbound dispatches awaiting truck loading. Dispatch officer + Bay supervisor.' WHERE code IN ('pico:dsp-list','pico:dsp-confirm-load','pico:dsp-pick-bay','pico:dsp-start-loading') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Plant Transfers in-progress between units / godowns. Source + Destination both confirm.' WHERE code IN ('pico:plant-transfer-list','pico:plant-transfer-line-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Return Notes from customers (FG or packing material). QC + Stores process per Pico SOP.' WHERE code IN ('pico:rn-list','pico:return-note-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Vendor master — KYC, GST, payment terms, default UOM, default bin.' WHERE code IN ('pico:vendor-list','pico:vendor-master') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Item master — HSN, GST rate, UOM conversion, shelf life, batch & FEFO rules, reorder point.' WHERE code IN ('pico:item-master','pico:item-master-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Bill of Materials per finished good. Used for production planning + cost roll-up.' WHERE code IN ('pico:bom-list','pico:bom-create') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Active subscriptions / AMC contracts. Renewal alerts go to account owner.' WHERE code IN ('pico:sub-list','pico:amc-list','pico:amc-create') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Asset register — equipment / vehicles / machines with depreciation schedule and maintenance plan.' WHERE code IN ('pico:asset-list','pico:asset-master') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Energy meter readings per asset. Used for energy intensity (kWh / MT) KPI.' WHERE code IN ('pico:energy-list','pico:energy-capture') AND (description IS NULL OR description='');
UPDATE page_definition SET description='KYC compliance per vendor / customer. Triggers PO/SO block until cleared.' WHERE code IN ('pico:kyc-list','pico:kyc-docs') AND (description IS NULL OR description='');

-- ---- M10: Dispatch / Yard ----
UPDATE page_definition SET description='Loading in progress on each bay — operator can see remaining cases, expected weight, scan progress.' WHERE code='pico:loading-in-progress' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Vehicle entry queue — Dispatch officer reviews PO match before pushing to QC.' WHERE code='pico:dispatch-vehicle-entry' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Final QC for outbound vehicles before exit-stamping.' WHERE code='pico:qc-final-out' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Manager-only approval queue for vehicles flagged at any step. Variance, missing PO, weight diff.' WHERE code='pico:manager-approval-inbox' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Workflow activity stream across all gate passes — transitions, role assignments, actor.' WHERE code='pico:workflow-activity' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Vehicle master — registration, type, ownership, default driver, RC + insurance + permit expiry.' WHERE code='pico:vehicle-master' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Tank location master — capacity, current grade, calibration date.' WHERE code IN ('pico:tank-master','pico:tank-status','pico:tank-detail') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Lane / route master — gate to gate routing inside the plant.' WHERE code='pico:lane-master' AND (description IS NULL OR description='');

-- ---- M6: Finance ----
UPDATE page_definition SET description='Sales invoices issued. Tally bidirectional sync pushes these as Sales Vouchers.' WHERE code IN ('pico:si-list','pico:sales-invoice-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Purchase invoices received. Posted to GL via auto-posting service.' WHERE code IN ('pico:pi-list','pico:purchase-invoice-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Payment vouchers — out to vendors, in from customers. Approval required above threshold.' WHERE code IN ('pico:pay-list','pico:payment-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='General Ledger accounts. Used for posting + period close.' WHERE code IN ('pico:gl-list','pico:gl-account-list','pico:m6-gl-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Vendor KYC compliance — PAN + GST + bank a/c verification.' WHERE code='pico:vendor-kyc-list' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Day book — all transactions for a single day. Used by accountant for daily reconciliation.' WHERE code='pico:daybook-list' AND (description IS NULL OR description='');

-- ---- M7: HR / Payroll ----
UPDATE page_definition SET description='Active employee master — identity, contact, employment, statutory IDs (PF, ESIC, UAN, PAN, PT).' WHERE code IN ('pico:emp-list','pico:employee-list','pico:emp-master') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Daily attendance roster — source (biometric / RFID / manual), exception types, regularisation workflow.' WHERE code IN ('pico:att-list','pico:attendance-list','pico:attendance-mark') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Leave applications pending approval. Manager + HR review per leave policy.' WHERE code IN ('pico:lr-list','pico:leave-request-list','pico:lr-apply') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Payroll run — compute → review → post → generate payslips → push to bank file.' WHERE code IN ('pico:pr-list','pico:payroll-run-list','pico:payroll-run') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Individual payslips with PF/ESI/PT/TDS breakdown.' WHERE code IN ('pico:payslip-list','pico:payslips') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Statutory returns calendar — PF ECR, ESIC, TDS 24Q/26Q, PT, LWF, GSTR-1/3B, MSME-1, Form 21/22.' WHERE code IN ('pico:stat-return-catalog','pico:statutory-return-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Statutory return filings — actual submissions per period with ACK + portal link.' WHERE code IN ('pico:stat-return-filing','pico:statutory-return-filing-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Open job postings — pipeline + candidates + interview rounds.' WHERE code IN ('pico:job-opening-list','pico:cand-list','pico:candidate-list','pico:cand-create','pico:cand-interview') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Annual appraisal cycles. Forms + ratings + increment letters.' WHERE code IN ('pico:appraisal-cycle-list','pico:appraisal-form-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Active training programs + attendance.' WHERE code IN ('pico:training-list','pico:training-program-list','pico:training-attendance-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='POSH committee + complaint register. Auto-route to designated members.' WHERE code IN ('pico:posh-committee','pico:posh-incident-list') AND (description IS NULL OR description='');

-- ---- CRM ----
UPDATE page_definition SET description='Leads in the pipeline — BANT-scored. Owner + stage + last activity.' WHERE code IN ('pico:lead-list','pico:lead-create') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Active quotes. Validity, conversion to SO, discount approval if beyond authority.' WHERE code IN ('pico:quote-list') AND (description IS NULL OR description='');
UPDATE page_definition SET description='Open service tickets — priority P1-P4 with SLA timer. Tech assignment + parts consumption.' WHERE code IN ('pico:st-list','pico:service-ticket-list') AND (description IS NULL OR description='');

-- ---- Reports ----
UPDATE page_definition SET description='Day-end stock close — FIFO recompute, opening + receipts + issues + closing per item.' WHERE code='pico:rpt-day-end' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Receivables aging — 0-30 / 31-60 / 61-90 / 90+ buckets per customer.' WHERE code='pico:rpt-ar-aging' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Payables aging — 0-30 / 31-60 / 61-90 / 90+ buckets per vendor.' WHERE code='pico:rpt-ap-aging' AND (description IS NULL OR description='');

-- ---- M0 admin ----
UPDATE page_definition SET description='Document vault — uploaded files attached to any entity. Tagged + versioned + access-controlled.' WHERE code IN ('pico:document-list','pico:document-upload','pico:document-vault') AND (description IS NULL OR description='');
UPDATE page_definition SET description='System roles + their default permissions. Used by RBAC layer to gate every read / write / approve.' WHERE code='pico:role-master' AND (description IS NULL OR description='');
UPDATE page_definition SET description='Departments + their head + active employees. Used in MR approval routing.' WHERE code='pico:department-master' AND (description IS NULL OR description='');

-- ---- M11 Integrations ----
UPDATE page_definition SET description='Active third-party integrations — Tally, GSTN, EPFO, ESIC, SMS, payment gateway, weighbridge controllers.' WHERE code IN ('pico:m11-integrations-list','m0:integration-console') AND (description IS NULL OR description='');
