-- Page id=428: force-publish page_version id=1200 and demote any other
-- currently-PUBLISHED sibling versions to ARCHIVED, mirroring
-- PageDefinitionService#publish() semantics (com.aria.page.service).
--
-- Unlike the normal publish flow (which promotes the latest DRAFT), this
-- targets an already-known version id directly, so it's guarded to be a
-- no-op if 1200 doesn't belong to page 428 or is already the published one.

-- 1. Demote any other PUBLISHED version of page 428 to ARCHIVED.
UPDATE page_version
   SET status = 'ARCHIVED'
 WHERE page_id = 428
   AND id <> 1200
   AND status = 'PUBLISHED';

-- 2. Promote version 1200 to PUBLISHED.
UPDATE page_version
   SET status = 'PUBLISHED',
       published_at = COALESCE(published_at, NOW())
 WHERE id = 1200
   AND page_id = 428
   AND status <> 'PUBLISHED';

-- 3. Point the page definition at version 1200 as current.
UPDATE page_definition pd
   SET pd.current_version_id = 1200,
       pd.status = 'PUBLISHED'
 WHERE pd.id = 428
   AND EXISTS (SELECT 1 FROM page_version pv WHERE pv.id = 1200 AND pv.page_id = 428);
