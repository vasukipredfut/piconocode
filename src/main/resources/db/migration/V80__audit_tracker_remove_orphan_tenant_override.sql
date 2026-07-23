-- page_definition resolution order is "tenant override wins, else global"
-- (PageDefinitionRepository.findCodeCandidates: ORDER BY tenant_id DESC).
-- A stray tenant-scoped row for aria:audit-tracker-groupby existed with
-- status='PUBLISHED' but current_version_id=NULL and zero page_version rows
-- underneath it — an invalid shape (PUBLISHED implies a version exists) that
-- was never created by any migration here. Because it outranks the correct
-- global (tenant_id=-1) row in the resolution order, GET /api/m0/pages/... for
-- that tenant returned 404 even though the real page is published and
-- correctly resolvable for every other tenant.
--
-- Scoped tightly: only removes page_definition rows for these two codes that
-- are (a) not the global tenant_id=-1 row and (b) have no page_version rows
-- at all — i.e. can't possibly be someone's real customization.
DELETE pd FROM page_definition pd
WHERE pd.code IN ('aria:audit-tracker-groupby', 'aria:audit-tracker-detail')
  AND pd.tenant_id <> -1
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);
