-- V70 — V61 renamed page_definition.code from 'pico:order-dispatch-detail'
-- and 'pico:order-dispatch-detail-upload' to their 'aria:' equivalents, but
-- left stale 'pico:' self-references inside page_version.body_json (the
-- list page's "Import CSV" button target, and the upload page's
-- onDonePage). Fix both so in-app navigation between the two pages works
-- after the rename. Scoped to page_definition ids 416/417 + the current
-- (post-V61) aria: codes as a belt-and-braces guard against touching the
-- wrong rows.

UPDATE page_version pv
  JOIN page_definition pd ON pd.id = pv.page_id
   SET pv.body_json = REPLACE(pv.body_json,
         '"pico:order-dispatch-detail-upload"',
         '"aria:order-dispatch-detail-upload"')
 WHERE pd.id IN (416, 417)
   AND pd.code IN ('aria:order-dispatch-detail', 'aria:order-dispatch-detail-upload')
   AND pv.status = 'PUBLISHED';

UPDATE page_version pv
  JOIN page_definition pd ON pd.id = pv.page_id
   SET pv.body_json = REPLACE(pv.body_json,
         '"pico:order-dispatch-detail"',
         '"aria:order-dispatch-detail"')
 WHERE pd.id IN (416, 417)
   AND pd.code IN ('aria:order-dispatch-detail', 'aria:order-dispatch-detail-upload')
   AND pv.status = 'PUBLISHED';
   
   -- V70 — rename flow-variant display labels: FGLoading -> "Container Loading",
-- PackUnload -> "Container Unload". `code` values (M10.FGLoading / M10.PackUnload)
-- are unchanged — only the UI-facing display_name on m10_flow_variant.

UPDATE m10_flow_variant SET display_name = 'Container Loading' WHERE code = 'M10.FGLoading';
UPDATE m10_flow_variant SET display_name = 'Container Unload'  WHERE code = 'M10.PackUnload';
