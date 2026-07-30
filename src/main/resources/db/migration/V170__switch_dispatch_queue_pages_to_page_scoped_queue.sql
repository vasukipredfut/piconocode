-- Switch 8 M10 pages' Queue widget from the persona-wide queue endpoint
-- (/api/workflow/queue/{persona} -- mixes every state that shares a role,
-- e.g. GATE_PASS_CREATED + QC_APPROVED + WEIGHMENT_2 all share role=Dispatch
-- on M10.TankerInbound) to the page-scoped endpoint
-- (/api/workflow/queue-for-page?pageCode=... -- only the state(s) THIS
-- page's own transitions act on).
--
-- In-place edit of the existing page_version row (not a new version) --
-- unlike workflow_definition, nothing pins a running page to a specific
-- page_version.id, so there is no in-flight-instance risk here; scoped by
-- page_version.id (not code/body LIKE) so each UPDATE touches exactly the
-- row already inspected. REPLACE() is a no-op on rerun once the old URL
-- substring is gone, so this file is naturally idempotent.

UPDATE page_version SET body_json = REPLACE(body_json,
  '/api/workflow/queue/Dispatch',
  '/api/workflow/queue-for-page?pageCode=aria:billing-entry')
WHERE id = 757;

UPDATE page_version SET body_json = REPLACE(body_json,
  '/api/workflow/queue/Dispatch',
  '/api/workflow/queue-for-page?pageCode=aria:dispatch-vehicle-details')
WHERE id = 743;

UPDATE page_version SET body_json = REPLACE(body_json,
  '/api/workflow/queue/Dispatch',
  '/api/workflow/queue-for-page?pageCode=aria:loading-allocation')
WHERE id = 758;

UPDATE page_version SET body_json = REPLACE(body_json,
  '/api/workflow/queue/PMT',
  '/api/workflow/queue-for-page?pageCode=aria:pmt-camera-verification')
WHERE id = 761;

UPDATE page_version SET body_json = REPLACE(body_json,
  '/api/workflow/queue/PMT',
  '/api/workflow/queue-for-page?pageCode=aria:pmt-weight-diff-check')
WHERE id = 760;

UPDATE page_version SET body_json = REPLACE(body_json,
  '/api/workflow/queue/QC',
  '/api/workflow/queue-for-page?pageCode=aria:qc-preload-approval')
WHERE id = 755;

UPDATE page_version SET body_json = REPLACE(body_json,
  '/api/workflow/queue/Dispatch',
  '/api/workflow/queue-for-page?pageCode=aria:sealing-process')
WHERE id = 759;

UPDATE page_version SET body_json = REPLACE(body_json,
  '/api/workflow/queue/PMT',
  '/api/workflow/queue-for-page?pageCode=aria:weight-diff-entry')
WHERE id = 756;
