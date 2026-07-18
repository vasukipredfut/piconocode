-- V18 — page templates: pre-built page bodies the Page Builder can start from.
--
-- "Start from a blank shape" made every new page begin from nothing. Templates
-- give the author a running start with real visual/UX variations to pick from —
-- Standard Table vs Kanban vs Master/Detail for a list; Single-column vs
-- Two-section for a form; a tabbed detail; a KPI dashboard; a printable QR slip.
--
-- Substrate data on the product row (tenant_id = -1). A tenant can add its own
-- templates (its rows resolve first, same pattern as page_category). Selecting a
-- template just loads its body_json into the builder — nothing is stamped until
-- the author saves, so templates are safe to browse.

CREATE TABLE IF NOT EXISTS page_template (
  id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id    BIGINT       NOT NULL,
  code         VARCHAR(64)  NOT NULL,
  name         VARCHAR(96)  NOT NULL,
  group_label  VARCHAR(48)  NOT NULL,
  description  VARCHAR(255) NOT NULL,
  shape        VARCHAR(24)  NOT NULL,
  sort_order   INT          NOT NULL DEFAULT 0,
  status       VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  body_json    LONGTEXT     NOT NULL,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_page_template (tenant_id, code),
  CONSTRAINT chk_page_template_body CHECK (json_valid(body_json))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO page_template
  (tenant_id, code, name, group_label, description, shape, sort_order, status, body_json)
VALUES
(-1,'tmpl:list-standard','Standard Table','List','A sortable table with a search box, a status filter and View / Edit row actions.','list',1,'ACTIVE',
  '{"kind": "list", "schemaVersion": 2, "dataSource": {"type": "entity", "entity": ""}, "header": {"title": "Records"}, "columns": [], "filters": [{"field": "status", "label": "Status", "type": "select", "options": []}], "sort": [{"field": "id", "dir": "desc"}], "pagination": {"pageSize": 25}, "rowActions": [{"label": "View", "action": "page.open"}, {"label": "Edit", "action": "page.open"}]}'),
(-1,'tmpl:list-kanban','Kanban Board','List','Records as cards in lanes, grouped by a status field — drag between stages.','kanban',2,'ACTIVE',
  '{"kind": "kanban", "schemaVersion": 2, "dataSource": {"type": "entity", "entity": ""}, "groupBy": "status", "cardTitle": "{{row.id}}", "columns": ["DRAFT", "IN_PROGRESS", "DONE"]}'),
(-1,'tmpl:list-masterdetail','Master / Detail','List','Expandable master rows, each opening its own nested table of line items.','layout',3,'ACTIVE',
  '{"kind": "layout", "schemaVersion": 2, "layout": [{"kind": "MasterDetailList", "master": {"entity": "", "titleField": "id", "subtitleField": "status"}, "detail": {"entity": "", "foreignKey": ""}}]}'),
(-1,'tmpl:form-single','Single Column Form','Form','One clean column of fields with a Save button — the simplest capture form.','layout',4,'ACTIVE',
  '{"kind": "layout", "schemaVersion": 2, "layout": [{"kind": "Section", "title": "Details", "children": [{"kind": "Form", "bindings": {"fields": [{"name": "name", "label": "Name", "type": "text", "required": true}, {"name": "description", "label": "Description", "type": "textarea"}, {"name": "status", "label": "Status", "type": "select", "options": [{"label": "Draft", "value": "DRAFT"}, {"label": "Active", "value": "ACTIVE"}]}]}, "children": [{"kind": "WorkflowActionButton", "label": "Save", "action": "save", "color": "primary"}]}]}]}'),
(-1,'tmpl:form-two-section','Two-Section Form','Form','Two side-by-side sections (e.g. Identity and Contact) for longer forms.','layout',5,'ACTIVE',
  '{"kind": "layout", "schemaVersion": 2, "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Identity", "span": 6, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "name", "label": "Name", "type": "text", "required": true}, {"name": "code", "label": "Code", "type": "text"}]}}]}, {"kind": "Section", "title": "Contact", "span": 6, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "phone", "label": "Phone", "type": "tel"}, {"name": "email", "label": "Email", "type": "email"}]}, "children": [{"kind": "WorkflowActionButton", "label": "Save", "action": "save", "color": "primary"}]}]}]}]}'),
(-1,'tmpl:detail-tabbed','Tabbed Record','Detail','One record shown across tabs (Summary, History) with a header and status badge.','detail',6,'ACTIVE',
  '{"kind": "detail", "schemaVersion": 2, "entity": "", "idFrom": "route:id", "dataSource": {"type": "entity", "entity": "", "filter": {"and": [{"field": "id", "op": "eq", "value": "route:id"}]}}, "header": {"title": "{{row.id}}", "subtitle": "", "statusBadge": {"field": "row.status"}}, "layout": [{"kind": "tabs", "tabs": [{"label": "Summary", "body": [{"kind": "section", "title": "Details", "fields": []}]}, {"label": "History", "body": [{"kind": "section", "title": "Timeline", "fields": []}]}]}]}'),
(-1,'tmpl:dashboard-kpi','KPI Dashboard','Dashboard','A row of four KPI tiles over a records table — the classic morning view.','layout',7,'ACTIVE',
  '{"kind": "layout", "schemaVersion": 2, "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "KPI", "span": 3, "title": "Total", "value": 0}, {"kind": "KPI", "span": 3, "title": "Open", "value": 0}, {"kind": "KPI", "span": 3, "title": "Closed", "value": 0}, {"kind": "KPI", "span": 3, "title": "Today", "value": 0}]}]}'),
(-1,'tmpl:slip-qr','Printable QR Slip','Print','A scannable QR of a record\'s key with its details and a Print button.','layout',8,'ACTIVE',
  '{"kind": "layout", "schemaVersion": 2, "layout": [{"kind": "QrCode", "title": "Slip", "entity": "", "idParam": "id", "qrField": "", "caption": "Present this slip at the gate.", "size": 180, "print": true, "details": [{"field": "id", "label": "ID"}, {"field": "status", "label": "Status"}]}]}')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), group_label=VALUES(group_label), description=VALUES(description),
  shape=VALUES(shape), sort_order=VALUES(sort_order), status=VALUES(status), body_json=VALUES(body_json);
