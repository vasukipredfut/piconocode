-- Rename page_definition.code 'aria:order-dispatch-detail' -> 'aria:order-dispatch-data'.
-- Deliberately NOT touching 'aria:order-dispatch-detail-upload' (a separate
-- page, left as-is) or the underlying OrderDispatchDetail entity/table/Java
-- controller (com.aria.mes.orderdispatch.OrderDispatchDetailImportController,
-- m5_order_dispatch_detail) — this is a page-code-only rename, not a module
-- rename.
--
-- V70__order_dispatch_detail_fix_pico_refs.sql already established that the
-- upload page (id 417) holds a self-reference to this page's code inside its
-- own body_json (onDonePage-style navigation target), so that reference must
-- be updated in lockstep or in-app navigation breaks. Using the same
-- exact-quoted-string REPLACE technique V70 used — matching
-- '"aria:order-dispatch-detail"' with its closing quote so
-- '"aria:order-dispatch-detail-upload"' is never touched.

UPDATE page_definition
   SET code = 'aria:order-dispatch-data'
 WHERE code = 'aria:order-dispatch-detail';

UPDATE page_version
   SET body_json = REPLACE(body_json,
         '"aria:order-dispatch-detail"',
         '"aria:order-dispatch-data"')
 WHERE body_json LIKE '%"aria:order-dispatch-detail"%';
