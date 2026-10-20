-- aria:gate-pass-closure-form (M10.TankerInbound): new checkbox "Driver Received Copy" (driver_received_copy).
-- It is saved in gate_pass_closure (the closure page's own table) by the generic WorkflowProjectionListener through the
-- M10.TankerInbound gate_pass_closure projection row (V120). The column is VARCHAR so it stores the checkbox exactly as the
-- page sends it ("true" / "false", or 1 / 0), whichever the UI uses.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS gate_pass_closure
  ADD COLUMN IF NOT EXISTS driver_received_copy VARCHAR(8) NULL COMMENT 'Driver Received Copy checkbox (Tanker Inbound)';

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'GatePassClosure', 'driver_received_copy', 'Driver Received Copy', 'STRING', 1, 1, 0, 0, 'checkbox'
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'GatePassClosure')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'GatePassClosure' AND field_code = 'driver_received_copy');

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json, '$.driver_received_copy', JSON_ARRAY('driver_received_copy', 'driverReceivedCopy'))
WHERE target_table = 'gate_pass_closure'
  AND workflow_code = 'M10.TankerInbound'
  AND field_map_json IS NOT NULL;
