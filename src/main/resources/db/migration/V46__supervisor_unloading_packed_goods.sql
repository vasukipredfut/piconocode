-- V46: Rebuild Supervisor Unloading for Pico's real ops:
--   * Pico processes/packages — they don't pump bulk oil
--   * Inbound: bottled/packed RM, packing material (cartons/bottles/caps),
--     return stock (FG returns)
--   * Supervisor flow: count cases, verify item code, allocate to bin,
--     damage check, confirm GRN-ready
-- Cross-ref: Marg ERP FMCG distributor receipt; Zoho Inventory inbound
-- shipment; Tally Prime Stock Inward; SAP MM Goods Receipt
-- ===========================================================================
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:supervisor-unloading','title','Unloading Supervisor — Goods Receipt','status','published','version',1,
  'roles', JSON_ARRAY('UnloadingSupervisor','StoreKeeper','Manager','TenantAdmin'),
  'dataSources', JSON_ARRAY(),'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      -- ---------- 1. Vehicle context ----------
      JSON_OBJECT('type','Section','id','sec-ctx','props',JSON_OBJECT('title','Vehicle & Reference','span',12,
        'subtitle','Auto-fills from the gate pass assigned to your queue. Confirm before counting goods.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-ctx','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','gp_number','label','Gate Pass # (from queue)','type','text','required',true,'readOnly',true),
              JSON_OBJECT('name','vehicle_no','label','Vehicle','type','text','readOnly',true),
              JSON_OBJECT('name','flow_kind','label','Receipt Type','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Raw Material — bottled / canned / drum (inbound from vendor)','value','RM_INBOUND'),
                  JSON_OBJECT('label','Packing Material — cartons / bottles / caps / labels (inbound)','value','PACKING_INBOUND'),
                  JSON_OBJECT('label','Return Stock — FG returned by customer','value','RETURN_INBOUND'),
                  JSON_OBJECT('label','Inter-Unit Transfer In','value','INTER_UNIT_IN'))),
              JSON_OBJECT('name','reference_no','label','Reference (PO# / GRN# / RN#)','type','text','required',true))))
        )),
      -- ---------- 2. Goods receipt (case count + item) ----------
      JSON_OBJECT('type','Section','id','sec-goods','props',JSON_OBJECT('title','Goods Receipt — Count by Item','span',12,
        'subtitle','Verify item code, count cases physically as you unload. Repeat the block for each item in the truck.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-goods','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','item_code','label','Item / SKU Code','type','text','required',true,
                'placeholder','GIN-1L-IDH','helperText','Scan barcode or pick from SKU master'),
              JSON_OBJECT('name','item_description','label','Item Name (auto-fill)','type','text','readOnly',true),
              JSON_OBJECT('name','uom','label','UOM','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Cases','value','CASE'),
                  JSON_OBJECT('label','Bottles','value','BOTTLE'),
                  JSON_OBJECT('label','Bundles','value','BUNDLE'),
                  JSON_OBJECT('label','Pallets','value','PALLET'),
                  JSON_OBJECT('label','Bags','value','BAG'),
                  JSON_OBJECT('label','Pieces','value','NOS'),
                  JSON_OBJECT('label','Drums','value','DRUM'),
                  JSON_OBJECT('label','Kilograms','value','KG'),
                  JSON_OBJECT('label','Litres','value','L'))),
              JSON_OBJECT('name','received_qty','label','Quantity Received','type','number','required',true,
                'helperText','Counted physically as cases / bottles came off the truck'),
              JSON_OBJECT('name','invoice_qty','label','Invoice Qty (from PO / vendor invoice)','type','number',
                'helperText','System flags variance if Received ≠ Invoice'),
              JSON_OBJECT('name','batch_no','label','Batch / Lot Number','type','text',
                'placeholder','B26-0421-A'),
              JSON_OBJECT('name','mfg_date','label','Manufacturing Date','type','date'),
              JSON_OBJECT('name','expiry_date','label','Expiry / Best-Before Date','type','date'))))
        )),
      -- ---------- 3. Bin allocation ----------
      JSON_OBJECT('type','Section','id','sec-bin','props',JSON_OBJECT('title','Bin / Godown Allocation','span',12,
        'subtitle','Pick the destination bin/godown. Stock balance updates immediately on confirmation.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-bin','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','godown_code','label','Godown','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','G1 — Main Stores (free 62%)','value','G1'),
                  JSON_OBJECT('label','G2 — Packing Material Stores (free 41%)','value','G2'),
                  JSON_OBJECT('label','G3 — Returns / Quarantine (free 88%)','value','G3'),
                  JSON_OBJECT('label','G4 — FG Stores (free 35%)','value','G4'))),
              JSON_OBJECT('name','bin_code','label','Bin / Rack','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','A-01 (free)','value','A-01'),
                  JSON_OBJECT('label','A-02 (partial — 30%)','value','A-02'),
                  JSON_OBJECT('label','A-03 (free)','value','A-03'),
                  JSON_OBJECT('label','B-01 (full — assign next)','value','B-01'),
                  JSON_OBJECT('label','B-02 (free)','value','B-02'),
                  JSON_OBJECT('label','C-01 (free)','value','C-01'),
                  JSON_OBJECT('label','C-02 (free)','value','C-02'))),
              JSON_OBJECT('name','stack_pattern','label','Stacking Pattern','type','select',
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Standard pallet (4 cases / layer × 5 layers)','value','STD_PALLET'),
                  JSON_OBJECT('label','Bin stack (max 8 layers)','value','BIN_STACK'),
                  JSON_OBJECT('label','Single layer (fragile)','value','SINGLE'))),
              JSON_OBJECT('name','storage_condition','label','Storage Condition','type','select',
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Ambient','value','AMBIENT'),
                  JSON_OBJECT('label','Cool & Dry','value','COOL_DRY'),
                  JSON_OBJECT('label','Quarantine — QC pending','value','QUARANTINE'))))))
        )),
      -- ---------- 4. Damage / Spoilage check ----------
      JSON_OBJECT('type','Section','id','sec-damage','props',JSON_OBJECT('title','Damage / Short-Receipt Check','span',12,
        'subtitle','Capture damaged or short items separately. They route to QC for vendor debit-note.'),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-dmg','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','damaged_qty','label','Damaged Quantity','type','number',
                'helperText','Cases / bottles found damaged on unload. Photo evidence required.'),
              JSON_OBJECT('name','damage_reason','label','Damage Reason','type','select',
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','Transport damage (crushed / dented)','value','TRANSPORT'),
                  JSON_OBJECT('label','Packing leak','value','LEAK'),
                  JSON_OBJECT('label','Seal broken','value','SEAL_BROKEN'),
                  JSON_OBJECT('label','Expired before receipt','value','EXPIRED'),
                  JSON_OBJECT('label','Other (see notes)','value','OTHER'))),
              JSON_OBJECT('name','short_qty','label','Short Quantity (vs invoice)','type','number',
                'helperText','Triggers debit-note workflow against vendor'),
              JSON_OBJECT('name','damage_photo','label','Damage / Short-Receipt Photo','type','file','accept','image/*'),
              JSON_OBJECT('name','notes','label','Notes','type','textarea','rows',3))))
        )),
      -- ---------- 5. Confirm ----------
      JSON_OBJECT('type','Section','id','sec-confirm','props',JSON_OBJECT('title','Confirm Storage Complete','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','Form','id','form-cf','props',JSON_OBJECT('submitLabel',''),
            'bindings', JSON_OBJECT('fields', JSON_ARRAY(
              JSON_OBJECT('name','supervisor_id','label','Supervisor (auto-fill)','type','text','readOnly',true),
              JSON_OBJECT('name','start_ts','label','Unloading Start','type','datetime-local'),
              JSON_OBJECT('name','finish_ts','label','Unloading Finish','type','datetime-local'),
              JSON_OBJECT('name','seal_unlock_no','label','Seal # broken on unload','type','text'),
              JSON_OBJECT('name','grn_to_create','label','Create GRN now?','type','select','required',true,
                'options',JSON_ARRAY(
                  JSON_OBJECT('label','✓ Yes — auto-create GRN against PO','value','YES'),
                  JSON_OBJECT('label','Later — supervisor will create from GRN page','value','NO'))))),
            'props',JSON_OBJECT()),
          JSON_OBJECT('type','WorkflowActionButton','id','btn-fin',
            'props', JSON_OBJECT('action','workflow.transition','label','Close Unloading — Stock Updated','variant','primary'))))
    ))
)
WHERE pd.code='pico:supervisor-unloading';

-- Update description for the page-guide banner
UPDATE page_definition SET description='Packed-goods / Raw-material receipt at the godown. Count by item, allocate to bin, capture damages, confirm. Not for bulk oil tank pumping.' WHERE code='pico:supervisor-unloading';
