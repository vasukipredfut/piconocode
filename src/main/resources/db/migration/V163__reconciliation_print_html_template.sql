-- V163 -- Fix: the "Print Copy" button (V161) was pointed at the
-- 'gate-pass' print_template, which has output_kind='label' -- confirmed by
-- actually driving the flow end-to-end and calling the print endpoint: it
-- returns raw ZPL barcode-printer commands (~300 chars), NOT an HTML
-- voucher. PrintController.doRender() only calls renderVoucher() (the one
-- that reads spec_json's rows/sections) when output_kind is anything OTHER
-- than 'label' -- for a 'label' template it always returns
-- renderZpl(label_zpl) instead, which never even looks at spec_json. V162's
-- edit to 'gate-pass'.spec_json was therefore completely inert.
--
-- Reverting that edit ('gate-pass' stays exactly as it always was --
-- untouched by this feature, still used correctly by the ZPL label print
-- flow for the physical gate-pass barcode on every M10 workflow), and
-- instead adding a NEW, separate HTML print_template (output_kind='html',
-- matching the 'grn' template's own convention) targeting the same
-- gate_pass row/id. The Print Copy button now points at this new template.

-- ============================================================ 1. Revert the inert 'gate-pass' spec_json edit
UPDATE print_template
SET spec_json = '{"documentLabel":"GATE PASS","numberColumn":"gp_number","showQr":true,"rows":[{"label":"Flow","column":"flow_variant_code","map":{"M10.TankerInbound":"Tanker Unloading (RM)","M10.PackUnload":"Container Unloading (Packing)","M10.ReturnStockIn":"Return Stock Unloading","M10.TankerOutbound":"Tanker Loading","M10.PackLoading":"Packing Material Loading","M10.FGLoading":"FG / Container Loading"}},{"label":"Vehicle Number","column":"vehicle_no"},{"label":"Vehicle Type","column":"vehicle_type"},{"label":"Driver Name","column":"driver_name"},{"label":"Driver Phone","column":"driver_phone"},{"label":"From / To","column":"source_party"},{"label":"Reference (PO/SO/RN)","column":"reference_no"},{"label":"Status","column":"status"},{"label":"Current Step","column":"current_step","map":{"GATE_PASS_CREATED":"Gate Pass Created","PARKING_IN":"At Parking","DISPATCH_QUEUED":"Queued for QC","QC_APPROVED":"QC Approved","QC_REJECTED":"QC Rejected","WEIGHMENT_LOADED":"W1 — Loaded","UNLOADING_DONE":"Unloading Done","WEIGHMENT_EMPTY":"W2 — Empty","WEIGHMENT_RECHECK":"W3 — Re-check","CLOSED":"Closed"}},{"label":"Created","column":"created_at"},{"label":"Closed","column":"closed_at"}],"sections":[{"heading":"Weighment","showIfAny":["w1_weight_kg","w2_weight_kg","w3_weight_kg"],"table":{"headers":["W1 (Loaded)","W2 (Empty)","W3 (Recheck)","Total Unloaded"],"cells":[{"column":"w1_weight_kg","num":true,"suffix":" kg"},{"column":"w2_weight_kg","num":true,"suffix":" kg"},{"column":"w3_weight_kg","num":true,"suffix":" kg"},{"column":"total_unloading_qty","num":true,"suffix":" kg"}]}}],"signatures":["Security","Dispatch Officer","Manager / Approver"]}'
WHERE code = 'gate-pass' AND output_kind = 'label';

-- ============================================================ 2. New HTML voucher template for the reconciliation/GRN summary
INSERT INTO print_template (id, tenant_id, code, name, output_kind, target_table, child_table, child_fk, spec_json, label_zpl, copies, printer_integration_code)
SELECT (SELECT COALESCE(MAX(id),0)+1 FROM print_template x), -1, 'unload-reconciliation', 'Unload Reconciliation Summary', 'html', 'gate_pass', NULL, NULL,
  '{"documentLabel":"UNLOAD RECONCILIATION SUMMARY","numberColumn":"gp_number","showQr":true,"rows":[{"label":"Flow","column":"flow_variant_code","map":{"M10.TankerInbound":"Tanker Unloading (RM)","M10.PackUnload":"Container Unloading (Packing)"}},{"label":"Vehicle Number","column":"vehicle_no"},{"label":"Driver Name","column":"driver_name"},{"label":"Driver Phone","column":"driver_phone"},{"label":"Invoice Qty (kg)","column":"invoice_qty","num":true},{"label":"GRN Status","column":"grn_status"},{"label":"GRN Number","column":"grn_number"},{"label":"Current Step","column":"current_step"},{"label":"Created","column":"created_at"}],"sections":[{"heading":"Weighment & Reconciliation","showIfAny":["w1_weight_kg","w2_weight_kg"],"table":{"headers":["W1 (Loaded)","W2 (Empty)","Unloaded Qty","Invoice Qty","Difference"],"cells":[{"column":"w1_weight_kg","num":true,"suffix":" kg"},{"column":"w2_weight_kg","num":true,"suffix":" kg"},{"column":"total_unloading_qty","num":true,"suffix":" kg"},{"column":"invoice_qty","num":true,"suffix":" kg"},{"column":"qty_difference_kg","num":true,"suffix":" kg"}]}}],"signatures":["Dispatch Officer","Weighment Officer"]}', NULL, 1, NULL
WHERE NOT EXISTS (SELECT 1 FROM print_template WHERE code='unload-reconciliation');

-- ============================================================ 3. Point aria:reconciliation's Print Copy button at the new template
-- (Earlier revision of this migration assumed the button's emoji label had
-- been corrupted to a literal '?' -- that was a misdiagnosis caused by a
-- locale/encoding issue in how it was re-read for verification, not in what
-- was actually stored. The label's real bytes are the correct UTF-8 printer
-- emoji all along, confirmed via HEX(). Matching on that below, unchanged.)
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(pv.body_json,
  '"printCode": "gate-pass", "idFrom": "prop", "id": "{{form.gatePassId}}", "label": "🖨 Print Copy"',
  '"printCode": "unload-reconciliation", "idFrom": "prop", "id": "{{form.gatePassId}}", "label": "🖨 Print Copy"')
WHERE pd.code = 'aria:reconciliation' AND pv.body_json LIKE '%"printCode": "gate-pass"%';
