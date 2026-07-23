-- The Group By summary page's "View History" row action opened the detail
-- page but never passed the clicked row's groupKey — so a frontend had no
-- way to know which vehicle/gate-pass/status/employee/date to filter the
-- detail view by. Fixes the navigation contract end-to-end:
--
--   1. aria:audit-tracker-groupby's rowAction now passes the whole
--      groupKey object ("row.groupKey" — same row-templating convention
--      already used elsewhere, e.g. V20's {"id":"row.id"}).
--   2. aria:audit-tracker-detail's contract now documents how to turn that
--      groupKey into a datasource/query filter: for EACH key in groupKey,
--      resolve its source_column via GET /api/m0/audit-tracker/dimensions
--      (code -> source_column), emit one {field,op:eq,value} leaf, and AND
--      them together — this supports both single- and multi-field Group By
--      (e.g. VEHICLE_NO alone, or VEHICLE_NO + DATE together).

UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_SET(pv.body_json,
    '$.rowActions', JSON_ARRAY(JSON_OBJECT(
      'label', 'View History', 'action', 'page.open', 'page', 'aria:audit-tracker-detail',
      'params', JSON_OBJECT('groupKey', 'row.groupKey')
    )))
WHERE pd.code = 'aria:audit-tracker-groupby';

UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_SET(pv.body_json,
    '$.receivesParams', JSON_ARRAY('groupKey'),
    '$.buildFilterFromGroupKey', JSON_OBJECT(
      'description', 'For each {dimensionCode: value} entry in the received groupKey param, resolve dimensionCode to its column via GET /api/m0/audit-tracker/dimensions (match on code, use source_column), then AND one eq-leaf per entry together as the datasource/query filter.',
      'dimensionsEndpoint', '/api/m0/audit-tracker/dimensions',
      'example_singleDimension', JSON_OBJECT(
        'receivedGroupKey', JSON_OBJECT('VEHICLE_NO', 'TN32AS2100'),
        'resultingFilter', JSON_OBJECT('and', JSON_ARRAY(JSON_OBJECT('field', 'vehicle_no', 'op', 'eq', 'value', 'TN32AS2100')))
      ),
      'example_multiDimension', JSON_OBJECT(
        'receivedGroupKey', JSON_OBJECT('VEHICLE_NO', 'TN32AS2100', 'DATE', '2026-07-22'),
        'resultingFilter', JSON_OBJECT('and', JSON_ARRAY(
          JSON_OBJECT('field', 'vehicle_no', 'op', 'eq', 'value', 'TN32AS2100'),
          JSON_OBJECT('field', 'ts_date', 'op', 'eq', 'value', '2026-07-22')
        ))
      )
    ))
WHERE pd.code = 'aria:audit-tracker-detail';
