-- V183__gate_pass_slip_created_at_all_flows.sql
-- Adds the gate pass creation timestamp to every flow-variant slip printed
-- from GET /api/m0/slip-fields/{flow} (see V15__flow_variant_slip.sql for how
-- the QrCode primitive resolves this map) — not just M10.FGLoading.
-- `created_at` is the gate_pass table's own creation-timestamp column
-- (V1__baseline.sql). Appended as the last line on each flow's slip, one row
-- per flow_variant_code currently in slip_field_map (TankerInbound,
-- TankerOutbound, FGLoading, PackLoading, PackUnload, IntTransferIn/Out,
-- ReturnStockIn, ThirdPartyIn/Out, EwayBill) — new flow variants added later
-- will need their own row the same way the rest of their fields do.
INSERT INTO slip_field_map (tenant_id, flow_variant_code, sort_order, field, label, status)
SELECT -1, flow_variant_code, MAX(sort_order) + 1, 'created_at', 'CreatedDateAndTime', 'ACTIVE'
FROM slip_field_map
WHERE tenant_id = -1 AND field <> 'created_at'
GROUP BY flow_variant_code
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order), label = VALUES(label), status = VALUES(status);
