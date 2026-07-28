-- aria:manager-qty-approval (shared with FGLoading/PackLoading/TankerOutbound
-- PMT-tolerance steps; M10.TankerInbound reaches it from QTY_PENDING_MANAGER):
-- "Order ID" (reference_no) is an OrderApp dispatch-order reference — doesn't
-- apply to TankerInbound (raw-material inbound, no OrderApp order), hidden
-- there. TankerInbound-only additions: Invoice Qty (joined from
-- dispatch_vehicle_detail) and W1/W2 (joined from weighment_capture_detail,
-- same join pattern as aria:reconciliation) with Unloading Qty and Difference
-- computed client-side (computedFrom, same convention as V97/aria:reconciliation),
-- a GRN Status dropdown, and a Print Copy button. Every other field/flow stays
-- unchanged.
--
-- Print Copy: "action":"browser.print" is a best-effort declarative hint —
-- there's no existing print pattern anywhere else in this page-builder DSL to
-- follow, so this only works once the frontend Renderer adds handling for
-- that action (window.print()). Flagging rather than claiming it works today.
UPDATE page_version pv
  JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = '{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Qty Difference — Manager Approval", "span": 12, "subtitle": "The system stopped these vehicles: loaded weight (W1-W2) differs from the OrderApp qty beyond the 0.4% tolerance. Pick the gate pass showing PMT_CHECK_PENDING_MANAGER, review the variance, add a reason, then RELEASE to billing or SEND BACK to reload.", "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (scan / pick / type)", "type": "select", "required": true, "freeSolo": true, "scannable": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "GatePass", "joins": [{"entity": "WorkflowInstance", "as": "wi", "on": "gate_pass.workflow_instance_id = wi.id"}, {"entity": "DispatchVehicleDetail", "as": "dvd", "on": "dvd.workflow_instance_id = gate_pass.workflow_instance_id", "fields": ["dvd.invoice_qty as invoiceQtyVal"]}, {"entity": "WeighmentCaptureDetail", "as": "wcd2", "on": "wcd2.workflow_instance_id = gate_pass.workflow_instance_id", "fields": ["wcd2.w1_weight_kg as w1WeightKgVal", "wcd2.w2_weight_kg as w2WeightKgVal"]}], "filter": {"and": [{"field": "wi.assigned_role", "op": "eq", "value": "Manager"}, {"field": "wi.status", "op": "eq", "value": "OPEN"}]}, "select": ["id", "gp_number", "flow_variant_code"], "sort": [{"field": "gp_number", "dir": "desc"}]}, "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "instanceId": "workflow_instance_id", "reference_no": "reference_no", "expected_weight_kg": "expected_weight_kg", "loaded_weight_kg": "loaded_weight_kg", "pmt_variance_pct": "pmt_variance_pct", "withinTolerance": "pmt_within_tolerance", "flow_variant_code": "flow_variant_code", "invoice_qty_display": "invoiceQtyVal", "w1_weight_kg_display": "w1WeightKgVal", "w2_weight_kg_display": "w2WeightKgVal"}}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "flow_variant_code", "label": "Flow Variant", "type": "text", "readOnly": true}, {"name": "reference_no", "label": "Order ID", "type": "text", "readOnly": true, "visibleWhen": "#form.flow_variant_code != \'M10.TankerInbound\'"}, {"name": "expected_weight_kg", "label": "Expected Weight (kg)", "type": "text", "readOnly": true}, {"name": "loaded_weight_kg", "label": "Loaded (W1 - W2) kg", "type": "text", "readOnly": true}, {"name": "pmt_variance_pct", "label": "Variance %", "type": "text", "readOnly": true}, {"name": "withinTolerance", "label": "System Verdict", "type": "text", "readOnly": true}, {"name": "invoice_qty_display", "label": "Invoice Qty", "type": "number", "readOnly": true, "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'"}, {"name": "w1_weight_kg_display", "label": "W1 (kg)", "type": "number", "readOnly": true, "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'"}, {"name": "w2_weight_kg_display", "label": "W2 (kg)", "type": "number", "readOnly": true, "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'"}, {"name": "unloading_qty_display", "label": "Unloading Qty (W1 − W2)", "type": "number", "readOnly": true, "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'", "computedFrom": "#form.w1_weight_kg_display - #form.w2_weight_kg_display"}, {"name": "qty_difference_display", "label": "Difference (Invoice − Unloading)", "type": "number", "readOnly": true, "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'", "computedFrom": "#form.invoice_qty_display - #form.unloading_qty_display"}, {"name": "grn_status", "label": "GRN Status", "type": "select", "options": [{"label": "GRN Created", "value": "GRN_CREATED"}, {"label": "GRN Pending", "value": "GRN_PENDING"}], "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'", "requiredWhen": "#form.flow_variant_code == \'M10.TankerInbound\'"}, {"name": "variance_reason", "label": "Manager Reason (required to release or reject)", "type": "textarea"}]}}, {"kind": "Stack", "direction": "row", "gap": 12, "children": [{"kind": "Button", "label": "🖨 Print Copy", "action": "browser.print", "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'"}]}, {"kind": "Stack", "direction": "row", "gap": 12, "children": [{"kind": "WorkflowActionButton", "label": "✓ Approve & Release to Billing", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CHECK_OK", "color": "primary", "submitValues": {"status": "APPROVED"}}, {"kind": "WorkflowActionButton", "label": "✗ Reject — Send Back to Reload", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "RELOAD_REQUIRED", "color": "error", "submitValues": {"status": "REJECTED"}}]}]}]}]}'
 WHERE pd.code = 'aria:manager-qty-approval' AND pv.status = 'PUBLISHED';

-- Save wiring for the TankerInbound-only additions above: aria:manager-qty-approval
-- is its own distinct page/step, so it saves to its own dedicated table —
-- manager_qty_approval_detail (V90; already exists, already used by
-- FGLoading/PackLoading/TankerOutbound via PmtSharedReasonListener, already
-- has manager_decision/manager_qty_reason columns) — NOT
-- unload_reconciliation_detail (that one belongs to aria:reconciliation only).
-- Just adding the 3 new TankerInbound-only columns it doesn't have yet.
-- "variance_reason"/"status" are NOT added to the generic field_map_json —
-- same ambiguity reasoning as ReconciliationDecisionListener: this page
-- reuses those field names with aria:reconciliation earlier in this same
-- workflow (PmtSharedReasonListener's own javadoc documents this exact
-- overlap for other workflows). ManagerQtyApprovalDecisionListener.java
-- reads aria:manager-qty-approval's own bag entry directly instead, gated on
-- previousState == QTY_PENDING_MANAGER.
--
-- NOTE (pre-existing, not fixed here): PmtSharedReasonListener ALSO reacts to
-- previousState == WEIGHT_DIFF_PENDING_MANAGER for every workflow (no
-- workflow_code guard on that case) by reading aria:manager-qty-approval's bag
-- entry into manager_qty_approval_detail — that state name is reused later in
-- M10.TankerInbound's OWN flow (WEIGHT_DIFF_PENDING_MANAGER -> aria:manager-weight-approval,
-- a different page). Once a TankerInbound instance passes QTY_PENDING_MANAGER
-- and later reaches WEIGHT_DIFF_PENDING_MANAGER, PmtSharedReasonListener will
-- re-fire and re-write manager_qty_approval_detail with the STALE
-- aria:manager-qty-approval values from the earlier step — harmless
-- (re-writes the same values, doesn't corrupt anything), but worth knowing.
-- Fixing it means touching PmtSharedReasonListener itself, shared by 3 other
-- workflows — out of scope unless asked.
ALTER TABLE manager_qty_approval_detail
  ADD COLUMN IF NOT EXISTS invoice_qty DECIMAL(12,3) DEFAULT NULL AFTER manager_qty_reason,
  ADD COLUMN IF NOT EXISTS unloading_qty DECIMAL(12,3) DEFAULT NULL AFTER invoice_qty,
  ADD COLUMN IF NOT EXISTS grn_status VARCHAR(24) DEFAULT NULL AFTER unloading_qty;


-- aria:manager-weight-approval (shared with FGLoading/PackLoading/TankerOutbound;
-- M10.TankerInbound reaches it from WEIGHT_DIFF_PENDING_MANAGER): added
-- flow_variant_code capture (same recurring pattern as every other page) and
-- a TankerInbound-only computed Difference (W2 − W3) field so the manager
-- doesn't have to do the subtraction by eye.
UPDATE page_version pv
  JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = '{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Weight Verification — Manager Approval", "span": 12, "subtitle": "Final (third) weight captured at the exit. Pick the gate pass showing WEIGHMENT_3, confirm W3 against the second weight; if there is a difference, record the reason. APPROVE to send the vehicle to the gate, or REJECT it.", "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (scan / pick / type)", "type": "select", "required": true, "freeSolo": true, "scannable": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "GatePass", "joins": [{"entity": "WorkflowInstance", "as": "wi", "on": "gate_pass.workflow_instance_id = wi.id"}], "filter": {"and": [{"field": "wi.assigned_role", "op": "eq", "value": "Manager"}, {"field": "wi.status", "op": "eq", "value": "OPEN"}]}, "select": ["id", "gp_number", "flow_variant_code"], "sort": [{"field": "gp_number", "dir": "desc"}]}, "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "instanceId": "workflow_instance_id", "w2_weight_kg": "w2_weight_kg", "w3_weight_kg": "w3_weight_kg", "variance_reason": "variance_reason", "flow_variant_code": "flow_variant_code"}}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "flow_variant_code", "label": "Flow Variant", "type": "text", "readOnly": true}, {"name": "w2_weight_kg", "label": "Second Weight (kg)", "type": "text", "readOnly": true}, {"name": "w3_weight_kg", "label": "Third Weight (kg)", "type": "text", "readOnly": true}, {"name": "weight_difference_display", "label": "Difference (W2 − W3) kg", "type": "number", "readOnly": true, "visibleWhen": "#form.flow_variant_code == \'M10.TankerInbound\'", "computedFrom": "#form.w2_weight_kg - #form.w3_weight_kg"}, {"name": "variance_reason", "label": "Weight-difference reason (if any)", "type": "textarea"}]}}, {"kind": "Stack", "direction": "row", "gap": 12, "children": [{"kind": "WorkflowActionButton", "label": "✓ Approve — Ready for Exit", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "READY_FOR_EXIT", "color": "primary", "submitValues": {"status": "APPROVED"}}, {"kind": "WorkflowActionButton", "label": "✗ Reject", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "REJECTED_BY_MANAGER", "color": "error", "submitValues": {"status": "REJECTED"}}]}]}]}]}'
 WHERE pd.code = 'aria:manager-weight-approval' AND pv.status = 'PUBLISHED';

-- aria:manager-weight-approval saves to its own dedicated table too —
-- manager_weight_approval_detail (V91; already exists, already used by
-- FGLoading/PackLoading/TankerOutbound via PmtSharedReasonListener, already
-- has weight_decision/weight_variance_reason columns). Just adding the one
-- new TankerInbound-only column (the computed difference, snapshotted).
-- "variance_reason"/"status" NOT added to the generic field_map_json — same
-- ambiguity reasoning as the qty-approval/reconciliation listeners above.
-- ManagerWeightApprovalDecisionListener.java (new) reads
-- aria:manager-weight-approval's own bag entry directly, gated on
-- previousState == WEIGHT_DIFF_PENDING_MANAGER — this one IS scoped to
-- M10.TankerInbound only, since (per the note above) WEIGHT_DIFF_PENDING_MANAGER
-- means something different for the other 3 workflows (PmtSharedReasonListener
-- already owns that case for them, writing manager_qty_approval_detail, not
-- manager_weight_approval_detail).
ALTER TABLE manager_weight_approval_detail
  ADD COLUMN IF NOT EXISTS weight_difference DECIMAL(12,3) DEFAULT NULL AFTER weight_variance_reason;
