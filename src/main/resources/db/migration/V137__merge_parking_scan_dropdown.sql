-- =====================================================================
-- V137 — merge the Direction-driven parking log into the EXISTING
-- pico:parking-scan page (V42), instead of the separate
-- pico:dispatch-parking-scan page V135 created. Removes that page.
--
-- pico:parking-scan is wired to M10.TankerInbound's GATE_PASS_CREATED ->
-- PARKING_IN transition (V4 page_workflow_binding) and already has a
-- Direction (IN/OUT) select + bay-assignment section — unchanged.
-- gp_number stays plain text (scan or type), same as the original V42
-- design — NOT an entity-backed dropdown. An earlier draft of this
-- migration tried "optionsSource":{"type":"entity",...} on this Form
-- field; live testing on V136's near-identical Flow Variant field proved
-- that key silently renders nothing on this Grid/Form component (no
-- confirmed precedent anywhere in this repo's migrations for a *dynamic*
-- options source on this component — only static "options" arrays work).
-- Gate passes are created constantly, so a static list isn't an option
-- here the way it was for Flow Variant's fixed 6 codes.
--
-- Vehicle/flow-variant/loading-type context is looked up SERVER-SIDE by
-- gp_number instead (com.aria.workflow.projection.
-- ParkingBayEntryTransitionListener queries gate_pass directly) — more
-- reliable than depending on the frontend to auto-fill or the officer to
-- retype values that are already stored on the gate pass record.
--
-- The actual dispatch_parking_bay_entry write (in_time on IN, out_time on
-- OUT) is NOT done via the generic WorkflowProjectionListener — Direction
-- makes it genuinely conditional, which that listener can't express. See
-- ParkingBayEntryTransitionListener (new, separate bean —
-- WorkflowEngineService invokes every WorkflowTransitionListener bean, so
-- this runs alongside WorkflowProjectionListener without touching it).
-- =====================================================================

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:parking-scan','title','Parking-Bay Scan','status','published','version',1,
  'roles', JSON_ARRAY('ParkingSecurity','Dispatch','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-scan','props',JSON_OBJECT('title','Scan / Type Gate-Pass QR','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-scan','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass #','type','text','required',true,
                'placeholder','Scan QR or type GP-1-105'),
              JSON_OBJECT('name','direction','label','Direction','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','IN — vehicle entering bay','value','IN'),
                  JSON_OBJECT('label','OUT — vehicle leaving bay','value','OUT'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-bay','props',JSON_OBJECT('title','Available Bays','span',12,
        'subtitle','Live occupancy. Click a free bay to assign; click an occupied one to release.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-bay','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','bay_code','label','Parking Bay','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','BAY-1 — Free','value','BAY-1'),
                  JSON_OBJECT('label','BAY-2 — Occupied (TN-43-AB-9421 since 09:15) ⏱ 47 min','value','BAY-2'),
                  JSON_OBJECT('label','BAY-3 — Free','value','BAY-3'),
                  JSON_OBJECT('label','BAY-4 — Occupied (TN-46-CJ-6701 since 08:42) ⏱ 80 min ⚠','value','BAY-4'),
                  JSON_OBJECT('label','BAY-5 — Reserved (FG outbound)','value','BAY-5'),
                  JSON_OBJECT('label','BAY-6 — Free','value','BAY-6'))),
              JSON_OBJECT('name','dwell_threshold_min','label','Dwell alert threshold (minutes)','type','number','readOnly',true),
              JSON_OBJECT('name','notes','label','Notes (optional)','type','text'))))
        )),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-assign',
            'props', JSON_OBJECT('action','workflow.transition','label','Assign / Release Bay','variant','primary'))))
    ))
) WHERE pd.code='pico:parking-scan';

-- Remove the page V135 created — superseded by the merge above.
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:dispatch-parking-scan';
DELETE FROM page_definition WHERE code='pico:dispatch-parking-scan';
