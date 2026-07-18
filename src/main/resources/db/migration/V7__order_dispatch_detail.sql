-- =====================================================================
-- V7 — OrderDispatchDetail: a new, flat, append-only entity for the
-- OrderApp export dump (Email/OrderId/RD/.../Reason, 35 columns) the
-- customer uploads for order-vs-dispatch reconciliation reporting.
--
-- Deliberately a NEW entity, not an extension of OrderAppDump — that's a
-- narrower order-header entity (order_ref/customer/sku/qty) used by its
-- own reconcile flow. This dump has order-level AND per-SKU-line columns
-- in one row (the source export repeats Order Qty/Cancel Qty/Remaining
-- Qty/Dispatch Qty once as the order-level total and again as the
-- line-level value) — kept as separate columns here: order_qty/
-- cancel_qty/remaining_qty/dispatch_qty for the header total,
-- line_order_qty/line_cancel_qty/line_remaining_qty/line_dispatch_qty
-- for the per-SKU value.
--
-- One flat table (not header+lines split) per explicit choice: simpler
-- import, one row per (order, SKU) export line, some order-level values
-- repeated across a multi-SKU order's rows — accepted denormalization
-- for a reporting/reconciliation dump, not master data.
--
-- Append-only: no upsert key. Re-importing the same file creates
-- duplicate rows — acceptable for a point-in-time reconciliation
-- snapshot, not enforced here since the source export has no stable
-- per-row id.
--
-- Real CSV parsing + bulk insert lives in
-- com.aria.mes.orderdispatch.OrderDispatchDetailImportController
-- (POST /api/m5/order-dispatch-detail/import, multipart file). Column
-- order in the controller is POSITIONAL, matching the source export
-- exactly, because "Order Qty"/"Cancel Qty"/"Remaining Qty"/"Dispatch
-- Qty" each appear twice with the SAME header text (once for the order
-- total, once for the SKU line) — name-based mapping cannot disambiguate
-- these, only column position can. The same controller also serves a
-- self-contained authenticated upload page at
-- GET /api/m5/order-dispatch-detail/import-page (see class javadoc for
-- why a raw HTML+JS page is used instead of the page-JSON Renderer:
-- there is no Renderer convention in this repo for a file FormField to
-- POST multipart to a custom endpoint, and header-only JWT auth means a
-- plain page.open button click has no auth context to hand it).
--
-- Product-catalog rows (entity_registry / entity_field_registry /
-- page_definition) are seeded at tenant_id = -1 per the V2 convention —
-- shared, copy-on-write via DesignerController, not tenant-owned. The
-- underlying business-data table (m5_order_dispatch_detail) is ordinary
-- tenant data, tenant_id defaulting to 1 per row like any other M5 table.
-- =====================================================================

CREATE TABLE IF NOT EXISTS m5_order_dispatch_detail (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  customer_email VARCHAR(128) NULL,
  order_id VARCHAR(32) NULL,
  rd_code VARCHAR(32) NULL,
  rd_desc VARCHAR(255) NULL,
  created_on DATETIME NULL,
  submitted_on DATETIME NULL,
  updated_on DATETIME NULL,
  order_qty INT NULL,
  cancel_qty INT NULL,
  dispatch_qty INT NULL,
  remaining_qty INT NULL,
  status VARCHAR(32) NULL,
  detained TINYINT(1) NULL,
  sku_group VARCHAR(64) NULL,
  sku_code VARCHAR(32) NULL,
  sku_desc VARCHAR(255) NULL,
  sku_active_on DATETIME NULL,
  staged_on DATETIME NULL,
  staged_rate DECIMAL(12,3) NULL,
  order_rate DECIMAL(12,3) NULL,
  stock_qty INT NULL,
  line_order_qty INT NULL,
  line_cancel_qty INT NULL,
  line_remaining_qty INT NULL,
  route_id VARCHAR(32) NULL,
  line_dispatch_qty INT NULL,
  assign_qty INT NULL,
  dispatched_on DATETIME NULL,
  vehicle_no VARCHAR(32) NULL,
  driver_name VARCHAR(128) NULL,
  driver_phone VARCHAR(16) NULL,
  dispatch_by VARCHAR(64) NULL,
  load_wgt DECIMAL(12,3) NULL,
  dispatch_wgt DECIMAL(12,3) NULL,
  reason VARCHAR(255) NULL,
  source_file VARCHAR(255) NULL,
  imported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX ix_odd_order (tenant_id, order_id),
  INDEX ix_odd_sku (tenant_id, sku_code),
  INDEX ix_odd_route (tenant_id, route_id),
  INDEX ix_odd_vehicle (tenant_id, vehicle_no),
  INDEX ix_odd_status (tenant_id, status)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------- registry

INSERT INTO entity_registry
  (tenant_id, code, display_name, table_name, description, category, created_at,
   read_role, write_role, approver_role, value_column, display_column, lifecycle_kind, workflow_code)
VALUES
  (-1, 'OrderDispatchDetail', 'Order Dispatch Detail', 'm5_order_dispatch_detail',
   'OrderApp order-vs-dispatch export — one row per order+SKU line, imported from CSV.',
   'M5', CURRENT_TIMESTAMP, '*', 'Dispatch,Manager,TenantAdmin', NULL, NULL, NULL, 'LEDGER', NULL)
ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name), table_name = VALUES(table_name),
  description = VALUES(description), category = VALUES(category),
  read_role = VALUES(read_role), write_role = VALUES(write_role), lifecycle_kind = VALUES(lifecycle_kind);

INSERT INTO entity_field_registry
  (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
VALUES
  (-1, 'OrderDispatchDetail', 'order_id', 'Order Id', 'STRING', 1, 1, 1, 0),
  (-1, 'OrderDispatchDetail', 'customer_email', 'Email', 'STRING', 1, 0, 1, 0),
  (-1, 'OrderDispatchDetail', 'rd_code', 'RD', 'STRING', 1, 1, 1, 0),
  (-1, 'OrderDispatchDetail', 'rd_desc', 'RD Desc', 'STRING', 1, 1, 1, 0),
  (-1, 'OrderDispatchDetail', 'created_on', 'Created On', 'DATETIME', 1, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'submitted_on', 'Submitted On', 'DATETIME', 1, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'updated_on', 'Updated On', 'DATETIME', 1, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'order_qty', 'Order Qty', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'cancel_qty', 'Cancel Qty', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'dispatch_qty', 'Dispatch Qty', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'remaining_qty', 'Remaining Qty', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'status', 'Status', 'STRING', 1, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'detained', 'Detained', 'BOOLEAN', 1, 0, 0, 0),
  (-1, 'OrderDispatchDetail', 'sku_group', 'SKU Group', 'STRING', 1, 1, 1, 0),
  (-1, 'OrderDispatchDetail', 'sku_code', 'SKU', 'STRING', 1, 1, 1, 0),
  (-1, 'OrderDispatchDetail', 'sku_desc', 'SKU Desc', 'STRING', 1, 0, 1, 0),
  (-1, 'OrderDispatchDetail', 'staged_on', 'Staged On', 'DATETIME', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'staged_rate', 'Staged Rate', 'DECIMAL', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'order_rate', 'Order Rate', 'DECIMAL', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'stock_qty', 'Stock', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'line_order_qty', 'Line Order Qty', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'line_cancel_qty', 'Line Cancel Qty', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'line_remaining_qty', 'Line Remaining Qty', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'route_id', 'Route Id', 'STRING', 1, 1, 1, 0),
  (-1, 'OrderDispatchDetail', 'line_dispatch_qty', 'Line Dispatch Qty', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'assign_qty', 'Assign Qty', 'INT', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'dispatched_on', 'Dispatched On', 'DATETIME', 1, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'vehicle_no', 'Vehicle No', 'STRING', 1, 1, 1, 0),
  (-1, 'OrderDispatchDetail', 'driver_name', 'Driver Name', 'STRING', 1, 0, 1, 0),
  (-1, 'OrderDispatchDetail', 'driver_phone', 'Driver Phone', 'STRING', 0, 0, 1, 0),
  (-1, 'OrderDispatchDetail', 'dispatch_by', 'Dispatch By', 'STRING', 1, 1, 1, 0),
  (-1, 'OrderDispatchDetail', 'load_wgt', 'Load Wgt', 'DECIMAL', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'dispatch_wgt', 'Dispatch Wgt', 'DECIMAL', 0, 1, 0, 0),
  (-1, 'OrderDispatchDetail', 'reason', 'Reason', 'STRING', 0, 0, 1, 0)
ON DUPLICATE KEY UPDATE
  display_label = VALUES(display_label), data_type = VALUES(data_type),
  filterable = VALUES(filterable), sortable = VALUES(sortable),
  searchable = VALUES(searchable), required = VALUES(required);

-- ---------------------------------------------------------------- list page

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
VALUES (-1, 'pico:order-dispatch-detail', 'Order Dispatch Detail',
        'Order-vs-dispatch reconciliation export, imported from CSV — one row per order+SKU line.',
        'M5', 'PUBLISHED')
ON DUPLICATE KEY UPDATE
  title = VALUES(title), description = VALUES(description),
  category = VALUES(category), status = VALUES(status);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED', JSON_OBJECT(
    'kind', 'list',
    'schemaVersion', 2,
    'dataSource', JSON_OBJECT('type', 'entity', 'entity', 'OrderDispatchDetail'),
    'pageSize', 50,
    'defaultSort', JSON_OBJECT('field', 'id', 'dir', 'desc'),
    'columns', JSON_ARRAY(
      JSON_OBJECT('field', 'order_id', 'label', 'Order Id', 'sortable', true),
      JSON_OBJECT('field', 'rd_code', 'label', 'RD', 'sortable', true),
      JSON_OBJECT('field', 'rd_desc', 'label', 'RD Desc', 'sortable', true),
      JSON_OBJECT('field', 'sku_code', 'label', 'SKU', 'sortable', true),
      JSON_OBJECT('field', 'sku_desc', 'label', 'SKU Desc'),
      JSON_OBJECT('field', 'status', 'label', 'Status', 'sortable', true),
      JSON_OBJECT('field', 'detained', 'label', 'Detained', 'render', 'badge'),
      JSON_OBJECT('field', 'line_order_qty', 'label', 'Order Qty', 'render', 'number'),
      JSON_OBJECT('field', 'line_cancel_qty', 'label', 'Cancel Qty', 'render', 'number'),
      JSON_OBJECT('field', 'line_dispatch_qty', 'label', 'Dispatch Qty', 'render', 'number'),
      JSON_OBJECT('field', 'line_remaining_qty', 'label', 'Remaining Qty', 'render', 'number'),
      JSON_OBJECT('field', 'route_id', 'label', 'Route Id', 'sortable', true),
      JSON_OBJECT('field', 'vehicle_no', 'label', 'Vehicle No', 'sortable', true),
      JSON_OBJECT('field', 'driver_name', 'label', 'Driver Name'),
      JSON_OBJECT('field', 'dispatched_on', 'label', 'Dispatched On', 'render', 'datetime', 'sortable', true),
      JSON_OBJECT('field', 'load_wgt', 'label', 'Load Wgt', 'render', 'number'),
      JSON_OBJECT('field', 'dispatch_wgt', 'label', 'Dispatch Wgt', 'render', 'number')
    ),
    'filters', JSON_ARRAY(
      JSON_OBJECT('field', 'status', 'label', 'Status', 'type', 'multi-select',
                  'options', JSON_ARRAY('Dispatched', 'Assigned All', 'Pending')),
      JSON_OBJECT('field', '_q', 'label', 'Search Order/RD/SKU/Vehicle', 'type', 'text-contains',
                  'applyTo', JSON_ARRAY('order_id', 'rd_desc', 'sku_code', 'vehicle_no'))
    ),
    -- Header button + landing page shape as supplied by the frontend team's
    -- sample (matches their pico:qc-preload-approval-style convention:
    -- 'action':'page.open' navigating to another page_definition code).
    -- This migration ships that exact page-JSON shape; wiring it to also
    -- perform the real multipart POST to
    -- /api/m5/order-dispatch-detail/import (see
    -- OrderDispatchDetailImportController) is frontend work outside this
    -- migration's reach — until then this button opens the landing page
    -- below, whose submit is a safe page.open no-op back to the list.
    'header', JSON_OBJECT('actions', JSON_ARRAY(
      JSON_OBJECT('label', 'Import CSV', 'action', 'page.open', 'page', 'pico:order-dispatch-detail-upload')
    ))
  ), 'V7', CURRENT_TIMESTAMP
FROM page_definition p WHERE p.tenant_id = -1 AND p.code = 'pico:order-dispatch-detail'
ON DUPLICATE KEY UPDATE body_json = VALUES(body_json), rationale = VALUES(rationale), published_at = VALUES(published_at);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'pico:order-dispatch-detail';

-- ---------------------------------------------------------------- upload landing page

INSERT INTO page_definition (tenant_id, code, title, description, category, status)
VALUES (-1, 'pico:order-dispatch-detail-upload', 'Import Order Dispatch Detail',
        'Upload the OrderApp order-vs-dispatch CSV export.', 'M5', 'PUBLISHED')
ON DUPLICATE KEY UPDATE
  title = VALUES(title), description = VALUES(description),
  category = VALUES(category), status = VALUES(status);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED', JSON_OBJECT(
    'kind', 'layout', 'schemaVersion', 2,
    'layout', JSON_ARRAY(
      JSON_OBJECT('kind', 'Section', 'title', 'Import Order Dispatch Detail',
        'description', CONCAT(
          'Select the OrderApp order-vs-dispatch CSV/TSV export (35 columns). ',
          'This screen currently only records the file field for later wiring — ',
          'to actually import today, open /api/m5/order-dispatch-detail/import-page ',
          'directly (same auth header as any API call), which performs the real ',
          'CSV parse + insert via OrderDispatchDetailImportController.'),
        'fields', JSON_ARRAY(
          JSON_OBJECT('kind', 'FormField', 'type', 'file', 'name', 'orderDispatchDetailFile',
                      'label', 'Order Dispatch Detail CSV', 'accept', '.csv,.tsv,.txt')
        ),
        'submit', JSON_OBJECT('label', 'Back to list', 'action', 'page.open', 'page', 'pico:order-dispatch-detail')
      )
    )
  ), 'V7', CURRENT_TIMESTAMP
FROM page_definition p WHERE p.tenant_id = -1 AND p.code = 'pico:order-dispatch-detail-upload'
ON DUPLICATE KEY UPDATE body_json = VALUES(body_json), rationale = VALUES(rationale), published_at = VALUES(published_at);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'pico:order-dispatch-detail-upload';
