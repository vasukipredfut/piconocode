-- V32 — Manager Approval Inbox becomes a hub, not a status-flipper.
-- It was a kind:list whose Approve/Reject buttons wrote gate_pass.current_step
-- directly (QTY_RECONCILED / REJECTED_BY_MANAGER) — bypassing the engine and
-- desyncing the workflow instance from the projected row. The real approvals now
-- live on aria:manager-qty-approval + aria:manager-weight-approval (Forms that
-- fire the transition). Rebuilt this page as a hub that routes to those two.
-- Idempotent.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json='{"kind": "layout", "schemaVersion": 2, "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Manager Approvals", "span": 12, "subtitle": "Vehicles the system stopped for a manager decision. Pick the queue you need — each opens the review screen with the system-computed variance and Approve / Reject actions.", "children": [{"kind": "Stack", "direction": "row", "gap": 12, "children": [{"kind": "WorkflowActionButton", "label": "⚖ Qty Difference Approval", "action": "page.open", "page": "aria:manager-qty-approval", "color": "primary"}, {"kind": "WorkflowActionButton", "label": "⚖ Weight Difference Approval", "action": "page.open", "page": "aria:manager-weight-approval", "color": "primary"}]}]}]}]}'
WHERE pd.code='aria:manager-approval-inbox' AND pv.body_json NOT LIKE '%Manager Approvals%';
