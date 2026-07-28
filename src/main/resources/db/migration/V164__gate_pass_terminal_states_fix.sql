-- V164 -- Fix: gate_pass.closed_at (and gate_pass_closure.closed_at) never
-- got set for ANY M10 workflow's gate pass, ever -- confirmed by actually
-- driving M10.PackUnload end-to-end and checking the DB: every historical
-- row with status='GATE_PASS_OUT_CLOSED' has closed_at=NULL.
--
-- Root cause: m0_workflow_projection.terminal_states for target_table=
-- 'gate_pass' is 'EXIT_STAMPED,REJECTED,ABORTED,QC_REJECTED' for all 6 M10
-- workflows -- but none of them actually use EXIT_STAMPED, ABORTED, or bare
-- REJECTED as a state name (confirmed against each workflow_definition's own
-- states[].terminal list). The real terminal names in use are
-- GATE_PASS_OUT_CLOSED, CLOSED, QC_REJECTED, QC_PRELOAD_REJECTED,
-- QC_SAMPLE_REJECTED and REJECTED_BY_MANAGER -- so WorkflowProjectionListener
-- never recognised a real close as terminal and never stamped closed_at.
--
-- The gate_pass_closure row (added later, V119/V120) got this right for the
-- close states but is still missing REJECTED_BY_MANAGER -- added here too for
-- consistency (a manager-rejected pass is just as terminal/closed as a normal
-- exit).
UPDATE m0_workflow_projection
SET terminal_states = 'QC_REJECTED,QC_PRELOAD_REJECTED,QC_SAMPLE_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED,REJECTED_BY_MANAGER'
WHERE target_table = 'gate_pass'
  AND terminal_states = 'EXIT_STAMPED,REJECTED,ABORTED,QC_REJECTED';

UPDATE m0_workflow_projection
SET terminal_states = 'QC_REJECTED,QC_PRELOAD_REJECTED,QC_SAMPLE_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED,REJECTED_BY_MANAGER'
WHERE target_table = 'gate_pass_closure'
  AND terminal_states = 'EXIT_STAMPED,REJECTED,ABORTED,QC_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED';
