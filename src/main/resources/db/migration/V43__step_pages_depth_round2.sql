-- V43: Round-2 step pages with ERP-grade depth.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- (a) Dispatch Vehicle Entry  (per SAP TM "Inbound Delivery Inspection")
-- ---------------------------------------------------------------------------
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:dispatch-vehicle-entry','title','Dispatch Vehicle Entry','status','published','version',1,
  'roles', JSON_ARRAY('Dispatch','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-po','props',JSON_OBJECT('title','PO / Reference Match','span',12,
        'subtitle','For inbound: match vehicle to an open PO. Without PO → manager approval required.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-po','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass # (auto from front-gate)','type','text','required',true,'readOnly',true),
              JSON_OBJECT('name','po_number','label','Open PO # to match (leave blank if none)','type','text',
                'placeholder','PO-2026-0421','helperText','Match validates: vendor, item, qty, dispatch date'),
              JSON_OBJECT('name','po_status','label','PO Match Status','type','select','readOnly',true,
                'options',JSON_ARRAY('MATCHED','PARTIAL','NO_MATCH','NO_PO')),
              JSON_OBJECT('name','no_po_reason','label','No-PO reason (if no_po)','type','textarea','rows',2,
                'helperText','Triggers manager approval before vehicle proceeds'))))
        )),
      JSON_OBJECT('type','Section','id','sec-vend','props',JSON_OBJECT('title','Vendor / Customer Verification','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-vend','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','party_name','label','Source Party Name (Vendor or Customer)','type','text','required',true),
              JSON_OBJECT('name','party_invoice_no','label','Vendor Invoice / Customer DC No.','type','text'),
              JSON_OBJECT('name','party_invoice_qty','label','Invoice Qty (kg)','type','number'),
              JSON_OBJECT('name','oil_grade','label','Oil Grade / Item','type','select',
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Groundnut Raw (Single Filtered)','value','GND_RAW'),
                  JSON_OBJECT('label','Gingelly Raw','value','GIN_RAW'),
                  JSON_OBJECT('label','Sunflower Refined','value','SNF_REFINED'),
                  JSON_OBJECT('label','Coconut Raw','value','CNT_RAW'),
                  JSON_OBJECT('label','Mustard Raw','value','MST_RAW'),
                  JSON_OBJECT('label','Rice Bran Raw','value','RCB_RAW'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-dfo','props',JSON_OBJECT('title','Dispatch Officer Stamp','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-dfo','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','dispatch_officer_id','label','Dispatch Officer (auto-fill)','type','text','readOnly',true),
              JSON_OBJECT('name','entry_remarks','label','Remarks (optional)','type','textarea','rows',2),
              JSON_OBJECT('name','status','label','Decision','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ APPROVED — proceed to QC queue','value','APPROVED'),
                  JSON_OBJECT('label','⚠ NEEDS MANAGER (no PO / vendor mismatch)','value','NEEDS_MANAGER'),
                  JSON_OBJECT('label','✗ REJECTED — vehicle to be turned back','value','REJECTED'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-q',
            'props', JSON_OBJECT('action','workflow.transition','label','Queue Vehicle for QC','variant','primary'))))
    ))
) WHERE pd.code='pico:dispatch-vehicle-entry';

-- ---------------------------------------------------------------------------
-- (b) Reconciliation (Order vs Unloaded)  (per Tally Prime "Variance Posting")
-- ---------------------------------------------------------------------------
-- Currently a list page. Add a reconciliation FORM page if not exists.
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:reconciliation-form', 'Reconciliation Decision', 'Order vs Unloaded variance — within tolerance proceeds, outside requires manager', 'M10', 'PUBLISHED', NOW(), NOW());

INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:reconciliation-form','title','Reconciliation Decision','status','published','version',1,
  'roles', JSON_ARRAY('Dispatch','ParkingSecurity','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-context','props',JSON_OBJECT('title','Comparison','span',12,
        'subtitle','System computes the delta; within ±0.25% auto-passes, beyond requires manager.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-rec','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass #','type','text','required',true,'readOnly',true),
              JSON_OBJECT('name','ordered_qty','label','Ordered Qty (kg) — from OrderApp / PO','type','number','readOnly',true),
              JSON_OBJECT('name','unloaded_qty','label','Unloaded Qty (kg) — from supervisor','type','number','readOnly',true),
              JSON_OBJECT('name','delta_kg','label','Delta (kg) — auto','type','number','readOnly',true),
              JSON_OBJECT('name','delta_pct','label','Delta % — auto','type','number','readOnly',true),
              JSON_OBJECT('name','within_tolerance','label','Within Tolerance?','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','YES — proceed to final weighment','value','YES'),
                  JSON_OBJECT('label','NO — route to manager for approval','value','NO'))),
              JSON_OBJECT('name','reason','label','Reason / Notes','type','textarea','rows',3))))
        )),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-rec',
            'props', JSON_OBJECT('action','workflow.transition','label','Submit Reconciliation','variant','primary'))))
    ))
)
FROM page_definition pd WHERE pd.code='pico:reconciliation-form' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:reconciliation-form' AND current_version_id IS NULL;

-- ---------------------------------------------------------------------------
-- (c) Gate Pass Closure (per SAP TM "Outbound Confirmation")
-- ---------------------------------------------------------------------------
-- Page already exists as inbox-style; add a Closure FORM page for the actual checks
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:gate-pass-closure-form', 'Gate Pass Closure — Final Checks', 'Final all-checks summary before vehicle exits the gate', 'M10', 'PUBLISHED', NOW(), NOW());

INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:gate-pass-closure-form','title','Gate Pass Closure — Final Checks','status','published','version',1,
  'roles', JSON_ARRAY('FrontGate','Dispatch','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-summary','props',JSON_OBJECT('title','Vehicle Summary (auto-fill from workflow)','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-sum','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass #','type','text','readOnly',true),
              JSON_OBJECT('name','vehicle_no','label','Vehicle','type','text','readOnly',true),
              JSON_OBJECT('name','flow_summary','label','Flow Summary','type','text','readOnly',true,
                'helperText','e.g. Tanker Unloading · 9,120 kg into TANK-1 · W2-W3 diff 0 kg · Sealed'),
              JSON_OBJECT('name','total_time_min','label','Total Time on Site (minutes)','type','number','readOnly',true))))
        )),
      JSON_OBJECT('type','Section','id','sec-checks','props',JSON_OBJECT('title','Final Gate Checks','span',12,
        'subtitle','All checks must be ✓ or status will be HOLD'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-chk','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','seal_verified','label','Seal verified at gate','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ Number matches (system-generated)','value','MATCHED'),
                  JSON_OBJECT('label','✗ Mismatch — hard block','value','MISMATCH'),
                  JSON_OBJECT('label','N/A (no seal flow)','value','NA'))),
              JSON_OBJECT('name','qc_clearance','label','QC clearance','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ Approved','value','APPROVED'),
                  JSON_OBJECT('label','✗ Rejected','value','REJECTED'))),
              JSON_OBJECT('name','weighment_verified','label','Weighment verified (W1/W2/W3)','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ All within tolerance','value','OK'),
                  JSON_OBJECT('label','⚠ Manager-approved variance','value','MGR_APPROVED'),
                  JSON_OBJECT('label','✗ Pending','value','PENDING'))),
              JSON_OBJECT('name','order_recon_done','label','Order vs Unloaded reconciled','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ Yes — within tolerance','value','OK'),
                  JSON_OBJECT('label','⚠ Manager-approved discrepancy','value','MGR_APPROVED'),
                  JSON_OBJECT('label','✗ No','value','NO'))),
              JSON_OBJECT('name','within_tolerance','label','All checks clear?','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','YES — release vehicle','value','YES'),
                  JSON_OBJECT('label','NO — route to manager','value','NO'))))))
        )),
      JSON_OBJECT('type','Section','id','sec-submit','props',JSON_OBJECT('span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-close',
            'props', JSON_OBJECT('action','workflow.transition','label','Close Gate Pass — Release Vehicle','variant','primary'))))
    ))
)
FROM page_definition pd WHERE pd.code='pico:gate-pass-closure-form' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:gate-pass-closure-form' AND current_version_id IS NULL;

-- ---------------------------------------------------------------------------
-- (d) Inject page-guide descriptions on the major list pages so the new
--     ListView header subtitle has content. (Substrate: page_definition.description
--     is auto-shown as italic subtitle under the title.)
-- ---------------------------------------------------------------------------
UPDATE page_definition SET description='Tanker Unloading 14-step tracker — pick a gate pass to see every step from gate-in to closure, with W1/W2/W3 values and operator IDs.' WHERE code='pico:tanker-tracker';
UPDATE page_definition SET description='Brand × Family × Pack × Offer-variant catalogue — used by Dispatch + Packing to validate barcode scans, lookup carton weights, and price orders.' WHERE code='pico:sku-master';
UPDATE page_definition SET description='Customer orders synced or uploaded from OrderApp — Dispatch reconciles loaded qty against this list before billing is unlocked.' WHERE code='pico:orderapp-dump';
UPDATE page_definition SET description='Every conveyor barcode scan — accepted (with offer + bundle count) or rejected (with reason). Operator and bay tracked for audit.' WHERE code='pico:packing-scan-log';
UPDATE page_definition SET description='Seal lifecycle: Issued → Applied → Verified → (Broken/Tampered). Mismatch at gate is a hard block — manager override required.' WHERE code='pico:seal-log';
UPDATE page_definition SET description='Diesel fill-up record per vehicle per visit. SKIPPED + COMPLETED + PENDING audited.' WHERE code='pico:diesel-log';
UPDATE page_definition SET description='Immutable change history. Every entity create / update / delete writes here with actor + payload.' WHERE code='pico:audit-log';
UPDATE page_definition SET description='Approve / Reject vehicles flagged for manager attention — W2 vs W3 mismatch, missing PO, quantity variance > 0.25%, return-stock disputes.' WHERE code='pico:manager-inbox';
UPDATE page_definition SET description='Today at Pico Power — live counts for every flow + Jump-to navigation across the platform.' WHERE code='pico:ops-dashboard';
UPDATE page_definition SET description='All gate passes — search by vehicle, driver, or GP number; click any row for the full audit trail.' WHERE code='pico:recent-gate-passes';
UPDATE page_definition SET description='Active driver master — name, phone, licence, vehicle assignment, status.' WHERE code='pico:driver-master';
UPDATE page_definition SET description='Gates configured for the plant — link gate code to flow type + active weighbridge.' WHERE code='pico:gate-master';
UPDATE page_definition SET description='Weighbridge inventory — capacity, calibration date, TCP feed address, last-reading timestamp.' WHERE code='pico:weighbridge-master';
UPDATE page_definition SET description='Parking bay grid — assigned vehicles + dwell time. Configurable capacity per bay.' WHERE code='pico:parking-bay-master';
