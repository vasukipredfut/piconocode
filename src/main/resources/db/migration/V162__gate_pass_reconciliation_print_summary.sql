-- V162 -- gate_pass: add a print-friendly reconciliation summary
-- (invoice_qty, w1/w2 weight, computed unloaded qty + difference, GRN
-- status/number) for M10.TankerInbound/M10.PackUnload, and extend the
-- existing 'gate-pass' print template so the "Print Copy" button added in
-- V161 actually shows this screen's own details, not just generic vehicle
-- info.
--
-- Why gate_pass and not a dedicated template on a satellite table:
-- PrintController.doRender() only ever does a plain
-- `SELECT * FROM target_table WHERE id=?` -- no joins, no computed columns.
-- A document that needs invoice_qty (dispatch_vehicle_detail), w1/w2
-- (weighment_capture_detail) and GRN status (unload_reconciliation_detail)
-- together has nowhere to read them from except a single denormalised row --
-- gate_pass is that row for every other M10 print template already
-- (it already carries total_unloading_qty/qc_status/variance_reason as
-- cross-cutting summary columns for other workflows, same pattern). Also:
-- gate_pass.id is the only id reliably known at page-load time on
-- aria:reconciliation (the satellite tables' own rows may not exist yet on
-- a first-time submission), so it's the only id the Print Copy button can
-- safely use regardless of when it's clicked.
--
-- Values are computed server-side (ReconciliationDecisionListener.java,
-- same change) rather than trusting a client-computed form field, since
-- w1/w2 weight can't be read reliably off the submission bag anyway (both
-- weighments reuse the same pageCode, so the bag only ever holds the latest
-- one) -- they're read back from weighment_capture_detail directly.

-- invoice_qty already exists on gate_pass from an earlier migration (this
-- ADD COLUMN IF NOT EXISTS is a no-op for it). w1_weight_kg/w2_weight_kg
-- previously existed on gate_pass too but were dropped by
-- V84__gate_pass_drop_moved_columns.sql (moved to weighment_capture_detail),
-- so they're genuinely new again here -- declared DECIMAL(12,3) to match
-- invoice_qty/qty_difference_kg's precision, same as this table's other
-- weight/qty columns.
ALTER TABLE gate_pass
  ADD COLUMN IF NOT EXISTS invoice_qty DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS w1_weight_kg DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS w2_weight_kg DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS qty_difference_kg DECIMAL(12,3) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS grn_status VARCHAR(24) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS grn_number VARCHAR(64) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'GatePass',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,0,c.wk
FROM (SELECT 'invoice_qty' fc,'Invoice Qty' lbl,'DECIMAL' dt,1 filt,1 sort,0 srch,'number' wk
      UNION ALL SELECT 'w1_weight_kg','W1 (kg)','DECIMAL',1,1,0,'number'
      UNION ALL SELECT 'w2_weight_kg','W2 (kg)','DECIMAL',1,1,0,'number'
      UNION ALL SELECT 'qty_difference_kg','Qty Difference (kg)','DECIMAL',1,1,0,'number'
      UNION ALL SELECT 'grn_status','GRN Status','STRING',1,1,1,'select'
      UNION ALL SELECT 'grn_number','GRN Number','STRING',1,1,1,'text') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code=c.fc);

-- Extend the 'gate-pass' print template: add Invoice Qty / GRN Status / GRN
-- Number as key/value rows, and add "Difference" to the existing Weighment
-- section's headers/cells (which, as a side effect of w1/w2 now actually
-- being populated, starts rendering for the first time for these two
-- workflows -- it was previously dead since gate_pass never had those
-- columns at all).
UPDATE print_template
SET spec_json = '{"documentLabel":"GATE PASS","numberColumn":"gp_number","showQr":true,"rows":[{"label":"Flow","column":"flow_variant_code","map":{"M10.TankerInbound":"Tanker Unloading (RM)","M10.PackUnload":"Container Unloading (Packing)","M10.ReturnStockIn":"Return Stock Unloading","M10.TankerOutbound":"Tanker Loading","M10.PackLoading":"Packing Material Loading","M10.FGLoading":"FG / Container Loading"}},{"label":"Vehicle Number","column":"vehicle_no"},{"label":"Vehicle Type","column":"vehicle_type"},{"label":"Driver Name","column":"driver_name"},{"label":"Driver Phone","column":"driver_phone"},{"label":"From / To","column":"source_party"},{"label":"Reference (PO/SO/RN)","column":"reference_no"},{"label":"Invoice Qty (kg)","column":"invoice_qty","num":true},{"label":"GRN Status","column":"grn_status"},{"label":"GRN Number","column":"grn_number"},{"label":"Status","column":"status"},{"label":"Current Step","column":"current_step","map":{"GATE_PASS_CREATED":"Gate Pass Created","PARKING_IN":"At Parking","DISPATCH_QUEUED":"Queued for QC","QC_APPROVED":"QC Approved","QC_REJECTED":"QC Rejected","WEIGHMENT_LOADED":"W1 — Loaded","UNLOADING_DONE":"Unloading Done","WEIGHMENT_EMPTY":"W2 — Empty","WEIGHMENT_RECHECK":"W3 — Re-check","CLOSED":"Closed"}},{"label":"Created","column":"created_at"},{"label":"Closed","column":"closed_at"}],"sections":[{"heading":"Weighment","showIfAny":["w1_weight_kg","w2_weight_kg","w3_weight_kg"],"table":{"headers":["W1 (Loaded)","W2 (Empty)","W3 (Recheck)","Unloaded","Difference"],"cells":[{"column":"w1_weight_kg","num":true,"suffix":" kg"},{"column":"w2_weight_kg","num":true,"suffix":" kg"},{"column":"w3_weight_kg","num":true,"suffix":" kg"},{"column":"total_unloading_qty","num":true,"suffix":" kg"},{"column":"qty_difference_kg","num":true,"suffix":" kg"}]}}],"signatures":["Security","Dispatch Officer","Manager / Approver"]}'
WHERE code = 'gate-pass' AND spec_json NOT LIKE '%invoice_qty%';
