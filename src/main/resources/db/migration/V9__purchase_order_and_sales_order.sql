-- =====================================================================
-- V9 — SUPERSEDED by V9_1 (legacy purchase_order / *_line tables already
-- exist in V1 baseline with a different schema; my CREATE TABLE IF NOT
-- EXISTS was a no-op and the seed INSERT then aborted). V9_1 uses the
-- m5_ prefix to avoid the namespace collision. This file is intentionally
-- empty — the initialiser hook for V9 has been removed and V9_1 has its
-- own hook keyed on m5_purchase_order existence.
-- =====================================================================
SELECT 'V9 superseded by V9_1' AS message;
