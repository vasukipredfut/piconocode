-- Small per-tenant sequence for gp_number generation at plain Gate Pass save
-- time (no workflow instance exists yet to derive "GP-{tenant}-{instance}"
-- from, per V226/V227). Uses the standard MariaDB
-- "INSERT ... ON DUPLICATE KEY UPDATE next_val = LAST_INSERT_ID(next_val + 1)"
-- pattern for an atomic, race-safe increment (a plain SELECT MAX(...)+1 would
-- race under concurrent saves) — see GatePassCreateActionHandler.java.
CREATE TABLE IF NOT EXISTS gate_pass_number_seq (
  tenant_id BIGINT NOT NULL PRIMARY KEY,
  next_val BIGINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Registers the "gatepass.create" action: wraps the generic EntityWriter
-- save (same mechanism as "save", V225) with GatePass-specific bookkeeping on
-- a fresh insert — stamping gp_number from the sequence above, and setting
-- current_step/status to "GATE_PASS_CREATED" so the record shows the same
-- values it always would have under the old GATE_PASS_CREATED workflow state,
-- for the interim period before a workflow instance exists.
INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'gatepass.create', 'Create Gate Pass (standalone)', 'entity',
       '{"type":"object","required":["entity","formData"],"properties":{"entity":{"const":"GatePass"},"formData":{"type":"object"}}}',
       1, NULL, 'Like save (EntityWriter.upsert against GatePass), but on a fresh insert also stamps gp_number from a per-tenant sequence and sets current_step/status to GATE_PASS_CREATED — used where no workflow instance exists yet to derive gp_number from.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'gatepass.create');
