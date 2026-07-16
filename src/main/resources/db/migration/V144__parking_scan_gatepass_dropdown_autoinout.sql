-- =====================================================================
-- V143 — pico:parking-scan: replace the free-text/Direction combo (V137)
-- with a live GatePass dropdown + auto-fill, and drop manual Direction
-- in favour of automatic IN/OUT toggling.
--
-- HISTORY, so nobody re-discovers this the hard way: V135 first tried an
-- entity-backed dropdown ("optionsSource":{"type":"entity",...}) with
-- onChangeFill on this exact page; V137 reverted gp_number to plain text
-- because live testing (on V136's near-identical Flow Variant field)
-- showed that key rendered nothing on this Grid/Form component at the
-- time. Reapplying it now per explicit confirmation from the user that
-- the frontend Renderer has since been updated to support it — this
-- migration was NOT re-verified against the frontend in this session (no
-- frontend repo available here). If gp_number renders empty again, the
-- fix is frontend-side (Renderer support for optionsSource/onChangeFill
-- on FormField), not this migration.
--
-- What changed vs V137:
--   1) gp_number -> gpNumber, now a "select" with optionsSource entity
--      GatePass (same shape V136 already uses for its WorkflowDefinition
--      dropdown), plus a QrScan component (registered DEVICE component,
--      V3) offering a "Tap to Scan" camera capture into the same field.
--   2) onChangeFill maps the selected GatePass row's vehicle_no /
--      flow_variant_code / loading_type / workflow_instance_id onto
--      disabled sibling fields — vehicleNo/flowVariantCode/loadingType
--      shown read-only for officer confirmation, instanceId used by the
--      Confirm button (below) to target the right workflow instance,
--      the same "{{form.x}}" templating V136 already uses for
--      workflowCode, applied here to instanceId (not previously
--      confirmed for this specific prop).
--   3) Direction select REMOVED. IN vs OUT is no longer a manual choice:
--      pico:parking-scan is bound (V7_14) to FOUR transitions on
--      M10.TankerInbound (GATE_PASS_CREATED->PARKING_IN,
--      QC_APPROVED->PARKING_OUT_AFTER_QC,
--      WEIGHMENT_EMPTY->PARKING_IN_AFTER_UNLOAD,
--      QTY_RECONCILED->PARKING_OUT_FINAL) — the engine already knows
--      which ONE applies from the instance's current state, so Confirm
--      just submits gpNumber + instanceId. ParkingBayEntryTransitionListener
--      (updated alongside this migration, no longer scoped to just
--      M10.TankerInbound — this page is reused by all 6 M10 gate-pass
--      workflows) now decides IN vs OUT itself: an open (out_time IS
--      NULL) dispatch_parking_bay_entry row for the gate pass means this
--      scan closes it out (OUT); no open row means this scan opens a new
--      one (IN). First scan = IN, next = OUT, next = IN, alternating —
--      no separate field to get wrong.
--   4) Available Bays section (bay_code/dwell_threshold_min/notes)
--      dropped per this request — scan/select + confirm only.
-- =====================================================================

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:parking-scan','title','Parking-Bay Scan','status','published','version',1,
  'roles', JSON_ARRAY('ParkingSecurity','Dispatch','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-scan','props',JSON_OBJECT('title','Scan / Select Gate-Pass','span',12,
          'subtitle','Tap Scan to use the camera, or pick the Gate Pass # from the list — vehicle, flow and loading context fill in automatically.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','QrScan','id','scan-gp','props',JSON_OBJECT('label','Tap to Scan','targetField','gpNumber','manualFallback',true)),
          JSON_OBJECT('type','Form','id','form-scan','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gpNumber','label','Gate Pass #','type','select','required',true,
                'placeholder','Scan above or select Gate Pass #',
                'optionsSource',JSON_OBJECT('type','entity','entity','GatePass','lookupType','gpNumbers','valueField','gp_number','labelField','gp_number'),
                'onChangeFill',JSON_OBJECT('vehicleNo','vehicle_no','flowVariantCode','flow_variant_code','loadingType','loading_type','instanceId','workflow_instance_id')),
              JSON_OBJECT('name','vehicleNo','label','Vehicle Number','type','text','disabled',true),
              JSON_OBJECT('name','flowVariantCode','label','Flow Variant','type','text','disabled',true),
              JSON_OBJECT('name','loadingType','label','Loading Type','type','text','disabled',true),
              JSON_OBJECT('name','instanceId','label','Workflow Instance','type','text','disabled',true)
            ))))
        ),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-confirm',
            'props', JSON_OBJECT('action','workflow.transition','label','Confirm','instanceId','{{form.instanceId}}','variant','primary'))))
    ))
) WHERE pd.code='pico:parking-scan';
