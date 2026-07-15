-- =====================================================================
-- V22 — Consolidate split / mis-labelled page categories.
-- Sidebar showed duplicate "M10 — Dispatch" groups because both `M10`
-- (7 pages from Round 1F seed) and `M10.Dispatch` (10 pages from Round 5)
-- existed as separate category strings.
-- =====================================================================

-- Collapse all M10.* variants into a single M10 group
UPDATE page_definition SET category='M10' WHERE category LIKE 'M10.%';

-- Vendor pages mislabelled "M3" -> move to "Master"
UPDATE page_definition SET category='Master' WHERE category='M3' AND code LIKE 'pico:vendor%';

-- "Persona" was the original M10 manager-inbox seed; the newer one is in
-- "Dashboard". Move both to a single "Dashboard" group.
UPDATE page_definition SET category='Dashboard' WHERE category='Persona';

-- Keep Reports / HR / CRM / M5 / M6 / M9 / M0 / M11 as-is.
