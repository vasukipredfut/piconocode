-- aria:loading-in-progress's route_id picker declares "distinct":true on its
-- OrderDispatchDetail datasource spec, but DataSourceSpec/DataSourceQueryService
-- has no such property -- it's silently ignored, so the query returns one row
-- per matching order_dispatch_detail row (duplicate route_ids), not one per
-- distinct route. The only dedup the query engine actually implements is
-- spec.groupBy (DataSourceQueryService.queryGrouped: "one row per distinct
-- combination of the given fields"), so swap to that.
--
-- Done as a targeted string replace rather than a full body_json rewrite:
-- this page has had multiple content revisions (Start Bay/Bay Complete
-- actions, KPIs, ListViews vary by version) and "distinct" appears exactly
-- once, tied unambiguously to route_id -- a full-body overwrite risks
-- clobbering whichever revision is actually live wherever this runs.

UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = REPLACE(pv.body_json, '"distinct":true', '"groupBy":["route_id"]')
WHERE pd.code = 'aria:loading-in-progress' AND pv.id = pd.current_version_id
  AND pv.body_json LIKE '%"distinct":true%';
