-- V22 — "Order from Gate Pass": the Loading step can Fetch Order for a vehicle's
-- route and see it as the master/detail template the customer approved
-- (aria:load-verification / MasterDetailList): one card per Order, expanding to
-- its SKU lines. Sources from m5_order_dispatch_detail (order lines), grouped
-- into an order-header view so the card = Order and the lines = SKUs.
--
-- Also seeds one route's worth of real edible-oil order lines + attaches a GP to
-- that route, so the order actually renders end-to-end.

-- 1. Order-header view (one row per order): card master.
CREATE OR REPLACE VIEW v_order_header AS
SELECT order_id            AS id,
       order_id,
       MIN(tenant_id)      AS tenant_id,
       MIN(route_id)       AS route_id,
       MIN(customer_email) AS customer,
       COUNT(*)            AS line_count,
       SUM(order_qty)      AS order_qty,
       SUM(dispatch_qty)   AS dispatch_qty,
       SUM(remaining_qty)  AS remaining_qty
FROM m5_order_dispatch_detail
GROUP BY order_id;

-- 2. Register OrderHeader as an entity + a few display fields.
INSERT INTO entity_registry (code, display_name, table_name)
SELECT 'OrderHeader','Order (header)','v_order_header'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='OrderHeader');

INSERT INTO entity_field_registry (id,tenant_id,entity_code,field_code,display_label,data_type,filterable,sortable,searchable,required,widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x) + ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'OrderHeader',c.fc,c.lbl,c.dt,1,1,0,0,'text'
FROM (SELECT 'order_id' fc,'Order ID' lbl,'STRING' dt
      UNION ALL SELECT 'route_id','Route','STRING'
      UNION ALL SELECT 'customer','Customer','STRING'
      UNION ALL SELECT 'line_count','SKU Lines','INT'
      UNION ALL SELECT 'order_qty','Order Qty','INT'
      UNION ALL SELECT 'dispatch_qty','Dispatch Qty','INT'
      UNION ALL SELECT 'remaining_qty','Remaining','INT') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='OrderHeader' AND field_code=c.fc);

-- 3. Seed order lines for route R-SUN-01 (edible-oil SKUs), tenant 1. Idempotent.
INSERT INTO m5_order_dispatch_detail
  (tenant_id, order_id, route_id, customer_email, sku_code, sku_desc, sku_group,
   order_qty, dispatch_qty, remaining_qty, line_order_qty, line_dispatch_qty, line_remaining_qty, status, order_rate)
SELECT * FROM (
  SELECT 1 AS a1,'ORD-SUN-5001' AS a2,'R-SUN-01' AS a3,'pico@sunraja.in' AS a4,'SFO-1L-PCH' AS a5,'Sunflower Oil 1L Pouch' AS a6,'SUNFLOWER' AS a7,480 AS a8,0 AS a9,480 AS a10,480 AS a11,0 AS a12,480 AS a13,'PENDING' AS a14,142.00 AS a15 UNION ALL
  SELECT 1,'ORD-SUN-5001','R-SUN-01','pico@sunraja.in','SFO-5L-CAN','Sunflower Oil 5L Can','SUNFLOWER',120,0,120,120,0,120,'PENDING',690.00 UNION ALL
  SELECT 1,'ORD-SUN-5001','R-SUN-01','pico@sunraja.in','SFO-15L-TIN','Sunflower Oil 15L Tin','SUNFLOWER',40,0,40,40,0,40,'PENDING',2050.00 UNION ALL
  SELECT 1,'ORD-SUN-5002','R-SUN-01','marimuthu@stores.in','GNO-1L-PCH','Groundnut Oil 1L Pouch','GROUNDNUT',300,0,300,300,0,300,'PENDING',210.00 UNION ALL
  SELECT 1,'ORD-SUN-5002','R-SUN-01','marimuthu@stores.in','GNO-5L-CAN','Groundnut Oil 5L Can','GROUNDNUT',90,0,90,90,0,90,'PENDING',1020.00
) s
WHERE NOT EXISTS (SELECT 1 FROM m5_order_dispatch_detail WHERE order_id IN ('ORD-SUN-5001','ORD-SUN-5002'));

-- 4. Attach an FG-loading gate pass to that route so "Fetch Order" has data.
UPDATE gate_pass SET route_id='R-SUN-01'
WHERE gp_number='GP-1-95' AND (route_id IS NULL OR route_id='');

-- 5. The order-from-GP page (MasterDetailList: Order card → SKU lines).
INSERT INTO page_definition (tenant_id, code, title, category, description, status)
SELECT -1,'aria:loading-order','Loading Order','M10',
  'The order for a vehicle''s route — one card per order, expanding to its SKU lines. Reached from the Loading step via Fetch Order.','PUBLISHED'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE code='aria:loading-order');

INSERT INTO page_version (page_id, version, body_json, status, published_at)
SELECT pd.id, 1, JSON_OBJECT(
  'kind','layout','schemaVersion',2,
  'layout', JSON_ARRAY(JSON_OBJECT(
    'kind','MasterDetailList',
    'title','Loading Order',
    'subtitle','Fetch by route — each order expands to its SKU lines. Pick an offer per line, then Start Loading.',
    'dataSource', JSON_OBJECT('type','entity','entity','OrderHeader',
       'filter', JSON_OBJECT('field','route_id','op','eq','value','route:route')),
    'card', JSON_OBJECT('titleField','order_id','titlePrefix','Order ','statusField','route_id','selectable',true,
       'summary', JSON_ARRAY(
         JSON_OBJECT('field','customer','label','Customer'),
         JSON_OBJECT('field','line_count','label','SKU Lines'),
         JSON_OBJECT('field','order_qty','label','Order Qty'),
         JSON_OBJECT('field','dispatch_qty','label','Dispatched'))),
    'lines', JSON_OBJECT('entity','OrderDispatchDetail','fkField','order_id',
       'columns', JSON_ARRAY(
         JSON_OBJECT('field','sku_code','label','SKU Code'),
         JSON_OBJECT('field','sku_desc','label','SKU Name'),
         JSON_OBJECT('field','order_qty','label','Order Qty'),
         JSON_OBJECT('field','dispatch_qty','label','Dispatch Qty'),
         JSON_OBJECT('field','remaining_qty','label','Remaining')))
  ))
), 'PUBLISHED', NOW()
FROM page_definition pd WHERE pd.code='aria:loading-order'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id=pd.id);

UPDATE page_definition pd
SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
WHERE pd.code='aria:loading-order';

-- 6. Wire the Loading step to open it: "Fetch Order →" row action carrying the GP's route.
UPDATE page_version pv
JOIN page_definition pd ON pd.id=pv.page_id
SET pv.body_json = JSON_ARRAY_APPEND(
  pv.body_json, '$.rowActions',
  JSON_OBJECT('label','📦 Fetch Order','action','page.open','page','aria:loading-order',
              'params', JSON_OBJECT('route','row.route_id'),'color','primary'))
WHERE pd.code='aria:loading-in-progress'
  AND JSON_EXTRACT(pv.body_json,'$.rowActions') IS NOT NULL
  AND JSON_SEARCH(pv.body_json,'one','aria:loading-order') IS NULL;
