-- V77 — aria:gate-pass-closure-form: "All checks clear?" (within_tolerance)
-- auto-computes from the 5 check dropdowns instead of manual pick.
--   ANY of them at a "bad" value (seal MISMATCH, invoice NO, QC REJECTED,
--   weighment PENDING, recon NO) -> "NO — route to manager"
--   ALL of them clear (MATCHED/NA, YES, APPROVED, OK/MGR_APPROVED,
--   OK/MGR_APPROVED) -> "YES — release vehicle"
-- Uses the same "computedFrom" SpEL convention as visibleWhen/requiredWhen
-- elsewhere in this app. CAVEAT: unverified whether the frontend Form
-- primitive live-recomputes a field from computedFrom on every keystroke of
-- its dependencies for a non-entity-bound page like this one — test in
-- browser; if it doesn't auto-fill, the frontend needs that wiring.

UPDATE page_version
   SET body_json = REPLACE(body_json,
     '{"name": "within_tolerance", "label": "All checks clear?", "type": "select", "required": true, "options": [{"label": "YES — release vehicle", "value": "YES"}, {"label": "NO — route to manager", "value": "NO"}]}',
     '{"name": "within_tolerance", "label": "All checks clear?", "type": "select", "required": true, "computedFrom": "(#form.seal_verified == ''MATCHED'' || #form.seal_verified == ''NA'') && #form.invoice_count_verified == ''YES'' && #form.qc_clearance == ''APPROVED'' && (#form.weighment_verified == ''OK'' || #form.weighment_verified == ''MGR_APPROVED'') && (#form.order_recon_done == ''OK'' || #form.order_recon_done == ''MGR_APPROVED'') ? ''YES'' : ''NO''", "options": [{"label": "YES — release vehicle", "value": "YES"}, {"label": "NO — route to manager", "value": "NO"}]}')
 WHERE id = (SELECT id FROM (
     SELECT pv.id FROM page_version pv JOIN page_definition pd ON pd.id = pv.page_id
      WHERE pd.code = 'aria:gate-pass-closure-form' AND pv.status = 'PUBLISHED'
   ) t);
