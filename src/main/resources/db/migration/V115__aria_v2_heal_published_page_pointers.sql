-- V115 — Aria v2: heal pages that have a PUBLISHED version but a stale definition
--
-- 14 workflow step-pages (M5.QualityTest, M5.MaintenanceOrder, M5.KycOnboarding,
-- M5.Subscription: qt-*/mo-*/kyc-*/sub-*) were re-published (a page_version row
-- with status=PUBLISHED exists) but their page_definition still reads status=
-- ARCHIVED with current_version_id pointing at the OLD archived version. The
-- renderer therefore refuses to serve them, so those workflow transitions had a
-- dead page reference even though live content existed.
--
-- Repoint each such definition at its latest PUBLISHED version and mark it
-- PUBLISHED. The guard only matches pages that genuinely have a published
-- version, so deliberately-archived pages (whose versions are all archived) are
-- untouched. Idempotent. Pure substrate data.
UPDATE page_definition pd
JOIN (
    SELECT page_id, MAX(id) AS pub_version_id
    FROM page_version
    WHERE status = 'PUBLISHED'
    GROUP BY page_id
) v ON v.page_id = pd.id
SET pd.current_version_id = v.pub_version_id,
    pd.status = 'PUBLISHED'
WHERE pd.status <> 'PUBLISHED'
   OR pd.current_version_id IS NULL
   OR (SELECT status FROM page_version pcv WHERE pcv.id = pd.current_version_id) <> 'PUBLISHED';
