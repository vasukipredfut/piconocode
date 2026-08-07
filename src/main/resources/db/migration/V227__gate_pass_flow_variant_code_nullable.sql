-- Same reasoning as V226 (gp_number): flow_variant_code is normally set by
-- WorkflowProjectionListener's variant_column on the first workflow
-- transition (INSERT branch only). The plain "save" Gate Pass path has no
-- workflow instance yet, so nothing to derive a variant code from — leave it
-- NULL until GatePassDispatchLinkListener backfills it once the workflow
-- actually starts (V224/V225/GatePassDispatchLinkListener.java).
ALTER TABLE gate_pass MODIFY flow_variant_code VARCHAR(64) NULL;
