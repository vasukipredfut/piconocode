-- V42: Deep workflow step pages to match real ERP behavior:
--   * Weighment Capture (per SAP TM "Vehicle Weighment") — W1/W2/W3 selector,
--     vehicle context, gross/tare/net display, photo capture
--   * QC Approval (per SAP QM Quality Plan) — sample param checklist with
--     auto-pass tolerances, sample photo, qc officer
--   * Supervisor Unloading (per SAP WM Inbound Putaway) — tank picker, qty,
--     start/finish stamps, seal yes/no
--   * Parking Scan (per SAP TM "Yard Management") — bay grid + assign/release
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) WEIGHMENT CAPTURE
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:weighment-capture','title','Weighment Capture','status','published','version',1,
  'roles', JSON_ARRAY('WeighmentOfficer','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-context','props',JSON_OBJECT('title','Vehicle & Weighbridge Context','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-ctx','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass #','type','text','required',true,
                'placeholder','Scan QR or type GP-1-105','helperText','From the active queue assigned to your weighbridge'),
              JSON_OBJECT('name','weighbridge_code','label','Weighbridge','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','WB-1 (Main Gate, 60T)','value','WB-1'),
                  JSON_OBJECT('label','WB-2 (Dispatch, 40T)','value','WB-2'))),
              JSON_OBJECT('name','vehicle_no','label','Vehicle Number (auto-fill from GP)','type','text','required',false,'readOnly',true),
              JSON_OBJECT('name','driver_name','label','Driver (auto-fill from GP)','type','text','required',false,'readOnly',true))))
        )),
      JSON_OBJECT('type','Section','id','sec-wstep','props',JSON_OBJECT('title','Weighment Sequence','span',12,
        'subtitle','W1 = vehicle loaded on entry  ·  W2 = vehicle empty after unload  ·  W3 = re-verify empty (only if W2 vs W3 diff > threshold)'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-w','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','w_step','label','Which Weighment?','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','W1 — Loaded weight (on entry)','value','W1'),
                  JSON_OBJECT('label','W2 — Empty weight (after unload)','value','W2'),
                  JSON_OBJECT('label','W3 — Re-verify empty (only on diff)','value','W3'))),
              JSON_OBJECT('name','live_reading_display','label','Live Weighbridge Reading','type','text','readOnly',true,
                'placeholder','Press Capture to read from controller','helperText','TCP feed from weighbridge controller — locked while stabilising'),
              JSON_OBJECT('name','gross_kg','label','Gross (kg)','type','number','required',true,
                'helperText','Total reading from weighbridge'),
              JSON_OBJECT('name','tare_kg','label','Tare (kg) — only for W2/W3','type','number','required',false),
              JSON_OBJECT('name','net_kg','label','Net (kg) — auto = Gross - Tare','type','number','readOnly',true),
              JSON_OBJECT('name','unwanted_material_check','label','Unwanted material removed? (no water cans, no spares)','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Yes — visually verified','value','YES'),
                  JSON_OBJECT('label','No — manager approval required','value','NO'))),
              JSON_OBJECT('name','seal_lock_status','label','Seal lock status (only for outbound W1)','type','select',
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Sealed','value','SEALED'),
                  JSON_OBJECT('label','Not Sealed','value','NOT_SEALED'),
                  JSON_OBJECT('label','N/A','value','NA'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-photo','props',JSON_OBJECT('title','Photo Evidence (weighbridge display)','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-photo','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','wb_display_photo','label','Weighbridge Display Photo','type','file','accept','image/*'))))
        )),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-capture',
            'props', JSON_OBJECT('action','workflow.transition','label','Capture & Save Weighment','variant','primary'))))
    ))
) WHERE pd.code='pico:weighment-capture';

-- ---------------------------------------------------------------------------
-- (b) QC APPROVAL
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:qc-approval','title','QC Approval','status','published','version',1,
  'roles', JSON_ARRAY('QC','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-ctx','props',JSON_OBJECT('title','Sample Identification','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-qcid','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass # (auto-fill from inbox)','type','text','required',true),
              JSON_OBJECT('name','sample_taken_from','label','Sample Taken From','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Tanker manhole (top)','value','TOP'),
                  JSON_OBJECT('label','Tanker drain (bottom)','value','BOTTOM'),
                  JSON_OBJECT('label','Container bag','value','BAG'),
                  JSON_OBJECT('label','3-position composite','value','COMPOSITE'))),
              JSON_OBJECT('name','sample_jar_id','label','Sample Jar ID','type','text','required',true,
                'placeholder','SAMPLE-2026-0617'),
              JSON_OBJECT('name','qc_officer_id','label','QC Officer (auto-fill from session)','type','text','readOnly',true))))
        )),
      JSON_OBJECT('type','Section','id','sec-params','props',JSON_OBJECT('title','Quality Parameters','span',12,
        'subtitle','Auto-pass tolerances based on Pico SOP. Any out-of-spec triggers manager review.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-qp','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','density_kgl','label','Density (kg/L) — spec 0.910–0.930 for groundnut','type','number','required',true,
                'helperText','Use digital hydrometer at 25°C'),
              JSON_OBJECT('name','ffa_pct','label','Free Fatty Acid (%) — spec ≤ 0.20','type','number','required',true,
                'helperText','Titration with KOH, 0.1N standard'),
              JSON_OBJECT('name','colour_rating','label','Colour (visual vs reference)','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Golden / Pale yellow — Pass','value','PASS_GOLDEN'),
                  JSON_OBJECT('label','Light orange — Pass','value','PASS_ORANGE'),
                  JSON_OBJECT('label','Dark brown — Fail','value','FAIL_DARK'),
                  JSON_OBJECT('label','Cloudy — Fail','value','FAIL_CLOUDY'))),
              JSON_OBJECT('name','temperature_c','label','Temperature (°C) — spec ≤ 35','type','number','required',true),
              JSON_OBJECT('name','moisture_pct','label','Moisture (%) — spec ≤ 0.10','type','number','required',false),
              JSON_OBJECT('name','foreign_matter','label','Foreign matter visible?','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','None','value','NONE'),
                  JSON_OBJECT('label','Trace (acceptable)','value','TRACE'),
                  JSON_OBJECT('label','Significant — Reject','value','SIGNIFICANT'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-photo','props',JSON_OBJECT('title','Sample Photo + Seal Number','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-qph','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','sample_photo','label','Sample Jar Photo','type','file','accept','image/*'),
              JSON_OBJECT('name','seal_unlock_no','label','Seal Unlock # (recorded by QC)','type','text','required',true,
                'placeholder','SL-2026-XXXX'),
              JSON_OBJECT('name','qc_notes','label','Notes (optional)','type','textarea','rows',3))))
        )),
      JSON_OBJECT('type','Section','id','sec-decide','props',JSON_OBJECT('title','Decision','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-dec','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','status','label','QC Decision','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ APPROVED — sample passes all parameters','value','APPROVED'),
                  JSON_OBJECT('label','✗ REJECTED — see notes','value','REJECTED'))))),
            'props',JSON_OBJECT()),
          JSON_OBJECT('type','WorkflowActionButton','id','btn-qc',
            'props', JSON_OBJECT('action','workflow.transition','label','Submit QC Decision','variant','primary'))))
    ))
) WHERE pd.code='pico:qc-approval';

-- ---------------------------------------------------------------------------
-- (c) SUPERVISOR UNLOADING
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:supervisor-unloading','title','Unloading Supervisor','status','published','version',1,
  'roles', JSON_ARRAY('UnloadingSupervisor','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-tank','props',JSON_OBJECT('title','Tank Allocation','span',12,
        'subtitle','Pick the destination tank based on current free capacity & oil grade'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-tank','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass # (from your queue)','type','text','required',true),
              JSON_OBJECT('name','destination_tank','label','Destination Tank','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','TANK-1 — Groundnut Raw (free: 12,400 L of 25,000 L)','value','TANK-1'),
                  JSON_OBJECT('label','TANK-2 — Gingelly Raw (free: 8,200 L of 20,000 L)','value','TANK-2'),
                  JSON_OBJECT('label','TANK-3 — Sunflower Raw (free: 18,500 L of 25,000 L)','value','TANK-3'),
                  JSON_OBJECT('label','TANK-4 — Mustard Raw (free: 21,000 L of 25,000 L)','value','TANK-4'))),
              JSON_OBJECT('name','tank_grade_check','label','Grade compatibility check','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ Same grade as tank — direct fill','value','SAME_GRADE'),
                  JSON_OBJECT('label','⚠ Different grade — tank cleaning required','value','DIFFERENT_GRADE'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-unload','props',JSON_OBJECT('title','Unloading Operation','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-u','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','seal_unlock_no','label','Seal Unlock # (from QC handoff)','type','text','required',true),
              JSON_OBJECT('name','unloading_start_ts','label','Unloading Start Time','type','datetime-local','required',true),
              JSON_OBJECT('name','unloading_finish_ts','label','Unloading Finish Time','type','datetime-local','required',true),
              JSON_OBJECT('name','qty_unloaded_kg','label','Quantity Unloaded (kg)','type','number','required',true,
                'helperText','Verified via dipstick + flow-meter cross-check'),
              JSON_OBJECT('name','tank_level_before','label','Tank level before (L)','type','number'),
              JSON_OBJECT('name','tank_level_after','label','Tank level after (L)','type','number'),
              JSON_OBJECT('name','supervisor_id','label','Supervisor (auto-fill)','type','text','readOnly',true),
              JSON_OBJECT('name','sample_taken','label','Final-line sample taken?','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Yes — labelled with GP#','value','YES'),
                  JSON_OBJECT('label','No — not required','value','NO'))),
              JSON_OBJECT('name','notes','label','Notes (any deviation, spillage, delay)','type','textarea','rows',3))))
        )),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-finish',
            'props', JSON_OBJECT('action','workflow.transition','label','Close Unloading — Mark Done','variant','primary'))))
    ))
) WHERE pd.code='pico:supervisor-unloading';

-- ---------------------------------------------------------------------------
-- (d) PARKING SCAN
-- ---------------------------------------------------------------------------
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
