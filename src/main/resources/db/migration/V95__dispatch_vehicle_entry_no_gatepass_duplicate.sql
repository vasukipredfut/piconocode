-- V95 — aria:dispatch-vehicle-details (PARKING_IN_1 -> DISPATCH_VEHICLE_ENTRY):
-- stop duplicating its own fields into gate_pass. dispatch_vehicle_detail
-- (V36 default projection) already captures everything this page submits.
--
-- vehicle_no / driver_name / driver_phone stay on gate_pass — core identity
-- fields every other M10 page reads back via onChangeFill on the GatePass
-- entity (PMT check, sealing, billing, closure-form, etc.). Not moving these.
--
-- broker_name / invoice_qty / oil_rm_item / tanker_no / transport_name: no
-- other page reads these off gate_pass (confirmed: no onChangeFill target
-- anywhere uses these names) — safe to drop the duplicate write entirely.
--
-- invoice_no: NOT safe to fully remove — aria:billing-entry has its own
-- required "Invoice Number" input field under the exact same name (a
-- different, later invoice — sales/dispatch, vs this page's vendor/purchase
-- invoice), submitted at a different transition (PMT_CHECK_OK ->
-- BILLING_DONE), and billing-entry has no dedicated table of its own yet, so
-- gate_pass.invoice_no is still its only save location. Both writes share one
-- gate_pass column today (last-submit-wins, same class of bug as
-- variance_reason). Fixed the same way: WorkflowProjectionListener.java skips
-- the 'invoice_no' key for gate_pass specifically when previousState ==
-- 'PARKING_IN_1' (dispatch-vehicle-details' own transition, confirmed the
-- only pageCode firing from that state across all 3 M10 workflows that use
-- it) — billing-entry's own transition (PMT_CHECK_OK) is untouched, so its
-- invoice_no keeps landing in gate_pass as before.

UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json,
      '$.broker_name', '$.invoice_qty', '$.oil_rm_item', '$.tanker_no', '$.transport_name')
WHERE target_table='gate_pass'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.PackUnload',
                         'M10.ReturnStockIn','M10.TankerInbound','M10.TankerOutbound');
