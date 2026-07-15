-- V62: Agent device topology. Different conveyor lines have different
-- hardware combinations:
--   * Line A: PLC only (PLC reads barcode + weight + drives diverter)
--   * Line B: PLC for diverter + external barcode scanner (TCP) + external
--              checkweigher (TCP / serial / Modbus separate from PLC)
--   * Line C: barcode scanner only (no checkweigher) — just confirm SKU
--   * Line D: checkweigher only (weight gating, no barcode at scanner)
--
-- The agent pulls its `devices` JSON from EARMS, knows which to connect to,
-- and runs the step sequence the acceptance scope demands.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) Agent gets a `devices` JSON column — replaces the flat plc_host/port
--     fields. Old fields kept for backward compat; if devices_json is set
--     it wins.
-- ---------------------------------------------------------------------------
ALTER TABLE m11_agent
  ADD COLUMN IF NOT EXISTS devices_json LONGTEXT NULL AFTER plc_timeout_ms;

-- Seed the demo agent with the 3-device topology so we can test
UPDATE m11_agent
SET devices_json = JSON_OBJECT(
  'plc', JSON_OBJECT(
    'mode','MODBUS_TCP', 'host','192.168.1.50', 'port',502,
    'unit_id',1, 'timeout_ms',2000,
    'registers', JSON_OBJECT('box_ready_flag',100, 'speed_setpoint',150, 'bay_code',140),
    'coils', JSON_OBJECT('divert_reject',200, 'halt_line',201, 'accept_done',202)),
  'barcode_scanner', JSON_OBJECT(
    'mode','PLC',                    -- PLC | TCP | SERIAL | OFF
    'plc_register_start',110, 'plc_register_count',20,
    -- TCP fallback (when mode=TCP):
    'host','192.168.1.51', 'port',9100, 'eol','\\r\\n'),
  'checkweigher', JSON_OBJECT(
    'mode','PLC',                    -- PLC | TCP | MODBUS_TCP | SERIAL | OFF
    'plc_register_hi',130, 'plc_register_lo',131,
    -- TCP fallback (when mode=TCP):
    'host','192.168.1.52', 'port',9101, 'format','ASCII_FLOAT',
    -- Modbus separate (when mode=MODBUS_TCP):
    'modbus_host','192.168.1.52', 'modbus_port',502, 'modbus_unit',2, 'modbus_register_hi',0, 'modbus_register_lo',1)
), config_version = config_version + 1
WHERE agent_id='pico-line-1-agent';

-- ---------------------------------------------------------------------------
-- (b) Acceptance scope gets a `flow_steps` array — what the agent must
--     verify before accepting a box. Order matters: barcode → weight → cap.
-- ---------------------------------------------------------------------------
ALTER TABLE m11_agent_acceptance_scope
  ADD COLUMN IF NOT EXISTS flow_steps_json TEXT NULL AFTER weight_max_kg;

-- Seed our demo scope: full check (barcode match + weight check + qty cap)
UPDATE m11_agent_acceptance_scope
SET flow_steps_json = JSON_ARRAY(
  'REQUIRE_BARCODE_MATCH',     -- compare scanned barcode against expected_barcode/expected_sku
  'REQUIRE_WEIGHT_IN_BAND',    -- compare weight against weight_min_kg / weight_max_kg
  'REQUIRE_QTY_CAP_NOT_HIT'    -- compare current_pieces against max_pieces
)
WHERE agent_id='pico-line-1-agent' AND scope_kind='PACKING_RUN';

-- Add a second demo scope on the SAME agent — barcode-only (no weight),
-- as if the line had no checkweigher (e.g. cap-and-bottle inspection only)
INSERT INTO m11_agent_acceptance_scope
  (tenant_id, agent_id, scope_kind, scope_ref_id, expected_sku, expected_offer,
   max_pieces, current_pieces, weight_min_kg, weight_max_kg, flow_steps_json,
   active, ttl_s, notes)
VALUES
  (1, 'pico-line-1-agent', 'PACKING_RUN', 2, 'AGB-IDH-100ST', 'STANDARD',
   200, 0, NULL, NULL,
   JSON_ARRAY('REQUIRE_BARCODE_MATCH','REQUIRE_QTY_CAP_NOT_HIT'),
   1, 60,
   'Demo Line 2: 200 cartons of Agarbatti — no weight check, just barcode + count.');

-- ---------------------------------------------------------------------------
-- (c) Expose flow_steps in entity_field_registry so admins can edit it
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO entity_field_registry
  (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  ('Agent','devices_json','Devices (JSON)','TEXT',0,0,1,0,'textarea',NULL),
  ('AgentAcceptanceScope','flow_steps_json','Flow Steps (JSON)','TEXT',0,0,1,0,'textarea',NULL);
