-- V12 — category display metadata as SUBSTRATE, not hardcoded frontend.
--
-- Sidebar group labels (M0/M5/M10…) were baked into the React shell's
-- CATEGORY_META. That is the anti-pattern the platform exists to avoid: renaming
-- a group, reordering it, or giving M3 a label needed a code change + redeploy,
-- and every tenant was stuck with the same wording. This moves it to data:
-- page_category is resolved per tenant like every other substrate row
-- (WHERE tenant_id IN (tid,-1) ORDER BY tenant_id DESC), so an admin can rename
-- "Dispatch & Gate" for their factory alone, and the labels are plain English
-- by default instead of internal module codes.
--
-- `icon` is a NAME, not markup — the frontend maps it to a MUI component through
-- a small stable registry (JSX can't live in a DB row).

CREATE TABLE IF NOT EXISTS page_category (
  id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id  BIGINT       NOT NULL DEFAULT -1,
  code       VARCHAR(32)  NOT NULL,            -- the page_definition.category value
  label      VARCHAR(80)  NOT NULL,            -- what a human reads in the sidebar
  icon       VARCHAR(48),                      -- MUI icon name, mapped on the frontend
  sort_order INT          NOT NULL DEFAULT 99,
  status     VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY u_tenant_code (tenant_id, code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Product defaults (tenant_id = -1). Plain-English labels; the codes stay codes.
INSERT INTO page_category (tenant_id, code, label, icon, sort_order) VALUES
  (-1, 'Dashboard', 'Dashboards',             'Dashboard',       1),
  (-1, 'M10',       'Dispatch & Gate',        'LocalShipping',   2),
  (-1, 'M5',        'Inventory & Production',  'Inventory2',      3),
  (-1, 'Master',    'Master Data',             'Storefront',      4),
  (-1, 'M6',        'Finance & Accounts',      'AccountBalance',  5),
  (-1, 'HR',        'HR & Payroll',            'People',          6),
  (-1, 'CRM',       'Sales & CRM',             'Handshake',       7),
  (-1, 'M9',        'Field Service',           'Build',           8),
  (-1, 'Report',    'Reports',                 'Assessment',      9),
  (-1, 'M3',        'MSME Compliance',         'Assessment',     10),
  (-1, 'M0',        'Settings & Admin',        'Settings',       11),
  (-1, 'M11',       'Integrations',            'Settings',       12),
  (-1, 'AI',        'AI Assistant',            'Settings',       13)
ON DUPLICATE KEY UPDATE label=VALUES(label), icon=VALUES(icon), sort_order=VALUES(sort_order);
