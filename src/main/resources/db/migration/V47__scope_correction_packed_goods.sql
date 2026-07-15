-- V47: Packed-goods + RM scope correction for Pico Power Project.
-- Pico's actual ops: packed RM in, packing material in, packed FG out, returns in.
-- (Bulk-oil tanker pumping is NOT part of the current scope.)
-- Adjust: weighment-capture (truck gross/tare not oil W1/W2/W3), dispatch
-- vehicle entry (Item Category not Oil Grade), QC (multi-item-type
-- parameters), tanker-tracker (rename to Vehicle Tracker).
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) Weighment Capture — gross + tare per vehicle, not oil-tank sequence
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:weighment-capture','title','Weighment Capture','status','published','version',1,
  'roles', JSON_ARRAY('WeighmentOfficer','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-ctx','props',JSON_OBJECT('title','Vehicle & Weighbridge','span',12,
        'subtitle','One weighment per direction. Loaded weight on entry, empty on exit; difference = quantity received/dispatched.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-ctx','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass # (from queue)','type','text','required',true,'readOnly',true),
              JSON_OBJECT('name','vehicle_no','label','Vehicle (auto-fill)','type','text','readOnly',true),
              JSON_OBJECT('name','weighbridge_code','label','Weighbridge','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','WB-1 (Main Gate, 60T)','value','WB-1'),
                  JSON_OBJECT('label','WB-2 (Dispatch, 40T)','value','WB-2'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-w','props',JSON_OBJECT('title','Reading','span',12,
        'subtitle','Live feed from the weighbridge controller. Press Capture to lock the reading.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-w','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','direction','label','Weighment','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Loaded (entry — full)','value','LOADED'),
                  JSON_OBJECT('label','Empty (exit — tare check)','value','EMPTY'),
                  JSON_OBJECT('label','Re-check (only if discrepancy)','value','RECHECK'))),
              JSON_OBJECT('name','live_reading_display','label','Live Reading','type','text','readOnly',true,
                'helperText','Locked while stabilising — wait for the green tick on the controller'),
              JSON_OBJECT('name','gross_kg','label','Gross Weight (kg)','type','number','required',true),
              JSON_OBJECT('name','tare_kg','label','Tare (kg) — from vehicle master or this empty reading','type','number'),
              JSON_OBJECT('name','net_kg','label','Net (kg) — auto Gross − Tare','type','number','readOnly',true),
              JSON_OBJECT('name','expected_kg','label','Expected (kg) — from PO / SO','type','number','readOnly',true),
              JSON_OBJECT('name','variance_kg','label','Variance (kg) — auto','type','number','readOnly',true),
              JSON_OBJECT('name','within_tolerance','label','Within Tolerance?','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','YES — proceed','value','YES'),
                  JSON_OBJECT('label','NO — route to manager','value','NO'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-photo','props',JSON_OBJECT('title','Photo Evidence','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-photo','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','wb_display_photo','label','Weighbridge Display Photo','type','file','accept','image/*'),
              JSON_OBJECT('name','vehicle_photo','label','Vehicle on Bridge Photo','type','file','accept','image/*'),
              JSON_OBJECT('name','notes','label','Notes (any anomaly)','type','textarea','rows',2))))
        )),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-cap',
            'props', JSON_OBJECT('action','workflow.transition','label','Capture & Save Weighment','variant','primary'))))
    ))
) WHERE pd.code='pico:weighment-capture';

UPDATE page_definition SET description='Truck weighment — loaded on entry, empty on exit. Variance routes to manager. One reading per direction (not oil W1/W2/W3 sequence).' WHERE code='pico:weighment-capture';

-- ---------------------------------------------------------------------------
-- (b) Dispatch Vehicle Entry — Item Category instead of Oil Grade
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:dispatch-vehicle-entry','title','Dispatch Vehicle Entry','status','published','version',1,
  'roles', JSON_ARRAY('Dispatch','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-po','props',JSON_OBJECT('title','Reference Match','span',12,
        'subtitle','For inbound: match to open PO / GRN draft. For outbound: match to SO / customer order. No reference → manager approval.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-po','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass # (auto)','type','text','required',true,'readOnly',true),
              JSON_OBJECT('name','reference_no','label','PO / SO / RN Reference #','type','text',
                'helperText','Match validates: party, item, qty, dispatch date'),
              JSON_OBJECT('name','reference_status','label','Match Status','type','select','readOnly',true,
                'options',JSON_ARRAY('MATCHED','PARTIAL','NO_MATCH','NO_REF')),
              JSON_OBJECT('name','no_ref_reason','label','No-reference reason (if none)','type','textarea','rows',2,
                'helperText','Triggers manager approval before vehicle proceeds'))))
        )),
      JSON_OBJECT('type','Section','id','sec-party','props',JSON_OBJECT('title','Source / Destination Party','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-party','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','party_name','label','Party Name (Vendor / Customer)','type','text','required',true),
              JSON_OBJECT('name','party_doc_no','label','Vendor Invoice / Customer DC No.','type','text'),
              JSON_OBJECT('name','party_qty','label','Document Quantity','type','number'),
              JSON_OBJECT('name','item_category','label','Item Category','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Raw Material — bulk oil (drum / can / bottled)','value','RM_OIL'),
                  JSON_OBJECT('label','Raw Material — jaggery / agarbatti raw','value','RM_OTHER'),
                  JSON_OBJECT('label','Packing Material — cartons / cases','value','PACK_CARTON'),
                  JSON_OBJECT('label','Packing Material — bottles / cans','value','PACK_BOTTLE'),
                  JSON_OBJECT('label','Packing Material — caps / labels / inner liners','value','PACK_SMALL'),
                  JSON_OBJECT('label','Finished Goods — packed for dispatch','value','FG'),
                  JSON_OBJECT('label','Return Stock — FG / packing returned','value','RETURN'),
                  JSON_OBJECT('label','Inter-Unit Transfer','value','INTERUNIT'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-dfo','props',JSON_OBJECT('title','Dispatch Officer Decision','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-dfo','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','dispatch_officer_id','label','Officer (auto-fill)','type','text','readOnly',true),
              JSON_OBJECT('name','entry_remarks','label','Remarks (optional)','type','textarea','rows',2),
              JSON_OBJECT('name','status','label','Decision','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ APPROVED — proceed to QC queue','value','APPROVED'),
                  JSON_OBJECT('label','⚠ NEEDS MANAGER (no ref / party mismatch)','value','NEEDS_MANAGER'),
                  JSON_OBJECT('label','✗ REJECTED — vehicle to be turned back','value','REJECTED'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-q',
            'props', JSON_OBJECT('action','workflow.transition','label','Queue Vehicle for QC','variant','primary'))))
    ))
) WHERE pd.code='pico:dispatch-vehicle-entry';

UPDATE page_definition SET description='Vehicle entry review — match to PO/SO/RN, identify item category (RM oil / packing carton / FG / return / inter-unit), decide approve / needs-manager / reject.' WHERE code='pico:dispatch-vehicle-entry';

-- ---------------------------------------------------------------------------
-- (c) QC Approval — branches by item category, not just oil
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:qc-approval','title','QC Approval','status','published','version',1,
  'roles', JSON_ARRAY('QC','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-id','props',JSON_OBJECT('title','Sample Identification','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-id','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass # (from inbox)','type','text','required',true),
              JSON_OBJECT('name','item_category','label','Item Category (auto-fill from dispatch entry)','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','RM Oil — bottled / canned / drum','value','RM_OIL'),
                  JSON_OBJECT('label','RM Other — jaggery / agarbatti raw','value','RM_OTHER'),
                  JSON_OBJECT('label','Packing — cartons / cases','value','PACK_CARTON'),
                  JSON_OBJECT('label','Packing — bottles / cans','value','PACK_BOTTLE'),
                  JSON_OBJECT('label','Packing — caps / labels','value','PACK_SMALL'),
                  JSON_OBJECT('label','FG — packed for dispatch','value','FG'),
                  JSON_OBJECT('label','Return Stock','value','RETURN'))),
              JSON_OBJECT('name','sample_jar_id','label','Sample / Sample Lot ID','type','text','required',true,
                'placeholder','SAMPLE-2026-0617'),
              JSON_OBJECT('name','qc_officer_id','label','QC Officer (auto-fill)','type','text','readOnly',true))))
        )),
      JSON_OBJECT('type','Section','id','sec-oil','props',JSON_OBJECT('title','Oil Parameters (only if item category = RM Oil / FG)','span',12,
        'subtitle','Skip this section if the receipt is packing material / return / non-oil.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-oil','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','density_kgl','label','Density (kg/L) — spec 0.910–0.930 for groundnut','type','number'),
              JSON_OBJECT('name','ffa_pct','label','Free Fatty Acid (%) — spec ≤ 0.20','type','number'),
              JSON_OBJECT('name','colour_rating','label','Colour vs reference','type','select',
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Golden / Pale yellow — Pass','value','PASS_GOLDEN'),
                  JSON_OBJECT('label','Light orange — Pass','value','PASS_ORANGE'),
                  JSON_OBJECT('label','Dark brown — Fail','value','FAIL_DARK'),
                  JSON_OBJECT('label','Cloudy — Fail','value','FAIL_CLOUDY'))),
              JSON_OBJECT('name','temperature_c','label','Temperature (°C) — spec ≤ 35','type','number'))))
        )),
      JSON_OBJECT('type','Section','id','sec-pack','props',JSON_OBJECT('title','Packing-Material Parameters (only if Packing)','span',12,
        'subtitle','Skip if not packing material.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-pack','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','print_quality','label','Print quality (labels / cartons)','type','select',
                'options',JSON_ARRAY('PASS','PASS_MINOR','FAIL_PRINT')),
              JSON_OBJECT('name','dimensional_check','label','Dimensions match spec? (±2 mm)','type','select',
                'options',JSON_ARRAY('PASS','MARGINAL','FAIL')),
              JSON_OBJECT('name','bottle_integrity','label','Bottle integrity — burst pressure / cap fit','type','select',
                'options',JSON_ARRAY('PASS','PARTIAL','FAIL_CRACK','FAIL_CAP_FIT')),
              JSON_OBJECT('name','carton_strength','label','Carton strength — drop test','type','select',
                'options',JSON_ARRAY('PASS','MARGINAL','FAIL')))))
        )),
      JSON_OBJECT('type','Section','id','sec-fg','props',JSON_OBJECT('title','FG / Return Inspection (only if FG / Return)','span',12,
        'subtitle','Skip if not FG or return.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-fg','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','seal_intact','label','Seal intact?','type','select',
                'options',JSON_ARRAY('YES','BROKEN','MISSING')),
              JSON_OBJECT('name','label_match','label','Label matches SKU master?','type','select',
                'options',JSON_ARRAY('YES','BATCH_MISMATCH','EXPIRY_MISMATCH','MRP_MISMATCH')),
              JSON_OBJECT('name','batch_expiry_check','label','Within shelf life?','type','select',
                'options',JSON_ARRAY('YES','NEAR_EXPIRY','EXPIRED')))))
        )),
      JSON_OBJECT('type','Section','id','sec-photo','props',JSON_OBJECT('title','Photo + Seal','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-ph','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','sample_photo','label','Sample Photo','type','file','accept','image/*'),
              JSON_OBJECT('name','seal_unlock_no','label','Seal Unlock #','type','text'),
              JSON_OBJECT('name','qc_notes','label','Notes','type','textarea','rows',3))))
        )),
      JSON_OBJECT('type','Section','id','sec-dec','props',JSON_OBJECT('title','Decision','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-dec','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','status','label','QC Decision','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ APPROVED — all parameters pass','value','APPROVED'),
                  JSON_OBJECT('label','✓ APPROVED with notes (managed exception)','value','APPROVED_NOTES'),
                  JSON_OBJECT('label','✗ REJECTED — see notes / debit vendor','value','REJECTED'))))),
            'props',JSON_OBJECT()),
          JSON_OBJECT('type','WorkflowActionButton','id','btn-qc',
            'props', JSON_OBJECT('action','workflow.transition','label','Submit QC Decision','variant','primary'))))
    ))
) WHERE pd.code='pico:qc-approval';

UPDATE page_definition SET description='Item-category-aware QC. Oil receipts get density/FFA/colour, packing material gets print/dimensions/integrity, FG/returns get seal/label/expiry checks.' WHERE code='pico:qc-approval';

-- ---------------------------------------------------------------------------
-- (d) Rename Tanker Tracker → Vehicle Tracker (covers all 6 flows)
-- ---------------------------------------------------------------------------
UPDATE page_definition SET title='Vehicle Tracker', description='All active gate passes across the 6 flows (RM in, packing in, return in, FG out, packing out, inter-unit). Click any row for the 14-step trail.' WHERE code='pico:tanker-tracker';

-- Loosen the dataSource filter — show ALL flows, not just TankerInbound
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_REMOVE(pv.body_json, '$.dataSource.filter')
WHERE pd.code='pico:tanker-tracker';

-- Drop the flow-variant filter that was hard-coded to TankerInbound only;
-- the page already has a "Flow" multi-select filter that user can use.
