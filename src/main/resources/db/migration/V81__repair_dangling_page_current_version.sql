-- V81 — Repair page_definition.current_version_id when it points to a
-- page_version.id that no longer exists.
--
-- Symptom: GET /api/m0/pages/<code> returns 401 (via /error) because
-- loadPublishedFor returns Optional.empty and controller throws
-- PageNotFoundException but the response gets rerouted through /error
-- which itself requires auth → 401 masks the real 404.
--
-- Repair strategy: for every dangling page, repoint current_version_id
-- to the newest PUBLISHED version_id for that page. If no published
-- version exists, leave the pointer NULL (the loader already treats
-- NULL as "no published version" and returns empty gracefully).
-- Idempotent — safe to reapply.

UPDATE page_definition p
LEFT JOIN page_version v ON v.id = p.current_version_id
JOIN (
    SELECT pv.page_id, pv.id AS latest_pub_id
      FROM page_version pv
      JOIN (
          SELECT page_id, MAX(version) AS max_ver
            FROM page_version WHERE status = 'PUBLISHED'
           GROUP BY page_id
      ) mv ON mv.page_id = pv.page_id AND mv.max_ver = pv.version
     WHERE pv.status = 'PUBLISHED'
) fresh ON fresh.page_id = p.id
   SET p.current_version_id = fresh.latest_pub_id
 WHERE p.current_version_id IS NOT NULL AND v.id IS NULL;

-- Null out any still-dangling pointers (page had NO published version).
UPDATE page_definition p
LEFT JOIN page_version v ON v.id = p.current_version_id
   SET p.current_version_id = NULL
 WHERE p.current_version_id IS NOT NULL AND v.id IS NULL;
