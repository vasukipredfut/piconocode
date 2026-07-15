-- =====================================================================
-- V20 — Bulk widget assignment across entity_field_registry.
-- 39/345 fields had widget_kind set (11%); the EntityFormModal rendered
-- everything-as-TextField for the other 89%. This sets sensible defaults
-- based on data_type + field_code patterns.
-- =====================================================================

-- 1. By data_type ------------------------------------------------------
UPDATE entity_field_registry SET widget_kind='date'     WHERE widget_kind IS NULL AND data_type='DATE';
UPDATE entity_field_registry SET widget_kind='datetime' WHERE widget_kind IS NULL AND data_type='DATETIME';
UPDATE entity_field_registry SET widget_kind='checkbox' WHERE widget_kind IS NULL AND data_type='BOOLEAN';
UPDATE entity_field_registry SET widget_kind='number'   WHERE widget_kind IS NULL AND data_type IN ('DECIMAL','LONG','INTEGER','NUMBER');
UPDATE entity_field_registry SET widget_kind='text'     WHERE widget_kind IS NULL AND data_type='STRING';

-- 2. By field_code pattern --------------------------------------------
-- Notes / remarks / description / reason -> textarea
UPDATE entity_field_registry SET widget_kind='textarea'
  WHERE field_code IN ('notes','description','remarks','reason','comments','closing_notes',
                       'work_notes','inspection_notes','closingNotes','qc_notes','message',
                       'narration','justification','feedback');
-- Status-shaped enum columns (data_type STRING, name = "status") — most have options known
UPDATE entity_field_registry SET widget_kind='select'
  WHERE field_code='status' AND data_type='STRING';
UPDATE entity_field_registry SET widget_kind='select'
  WHERE field_code IN ('action_kind','source','source_type','return_kind','meter_kind','kind',
                       'priority','cover_kind','method','direction','mode','frequency',
                       'feed_mode','outcome','decision','leave_kind','party_kind','esign_status',
                       'gate_type','protocol','employmenttype','employeetype','vehicle_type',
                       'ownership')
  AND data_type='STRING';

-- 3. Status options by entity (best-effort, harmless when no options match) -
UPDATE entity_field_registry SET options_json='[{"label":"Draft","value":"DRAFT"},{"label":"Submitted","value":"SUBMITTED"},{"label":"Approved","value":"APPROVED"},{"label":"Issued","value":"ISSUED"},{"label":"Closed","value":"CLOSED"},{"label":"Rejected","value":"REJECTED"}]'
  WHERE entity_code='MaterialRequisition' AND field_code='status';
UPDATE entity_field_registry SET options_json='["DRAFT","ITEMS_LOGGED","QC_CHECKED","STORED","CLOSED","QC_REJECTED"]'
  WHERE entity_code='GoodsReceiptNote' AND field_code='status';
UPDATE entity_field_registry SET options_json='["DRAFT","APPROVED","SENT_TO_VENDOR","RECEIVED","CLOSED","CANCELLED"]'
  WHERE entity_code='PurchaseOrder' AND field_code='status';
UPDATE entity_field_registry SET options_json='["DRAFT","CONFIRMED","IN_PRODUCTION","READY_TO_DISPATCH","DISPATCHED","INVOICED","CLOSED","CANCELLED"]'
  WHERE entity_code='SalesOrder' AND field_code='status';
UPDATE entity_field_registry SET options_json='["DRAFT","FINALIZED","E_INVOICED","SENT","PARTIAL","PAID","UNPAID","CANCELLED"]'
  WHERE entity_code='SalesInvoice' AND field_code='status';
UPDATE entity_field_registry SET options_json='["DRAFT","MATCHED","APPROVED","PARTIAL","PAID","UNPAID","DISPUTED"]'
  WHERE entity_code='PurchaseInvoice' AND field_code='status';
UPDATE entity_field_registry SET options_json='["DRAFT","ACTIVE","PM_DUE","EXPIRED","TERMINATED"]'
  WHERE entity_code='AmcContract' AND field_code='status';
UPDATE entity_field_registry SET options_json='["ACTIVE","PAUSED","CANCELLED","EXPIRED"]'
  WHERE entity_code='Subscription' AND field_code='status';
UPDATE entity_field_registry SET options_json='["PLANNED","IN_PROGRESS","COMPLETED","CANCELLED"]'
  WHERE entity_code='ProductionOrder' AND field_code='status';
UPDATE entity_field_registry SET options_json='["OPEN","CONSUMED","OUTPUT_DECLARED","QC_PASSED","CLOSED","QC_REJECTED"]'
  WHERE entity_code='JobCard' AND field_code='status';
UPDATE entity_field_registry SET options_json='["OPEN","RUNNING","PACKED","SPOIL","CLOSED"]'
  WHERE entity_code='PackingRun' AND field_code='status';
UPDATE entity_field_registry SET options_json='["PLANNED","BIN_CHOSEN","BAY_ASSIGNED","LOADING","DISPATCHED","CLOSED"]'
  WHERE entity_code='FGDispatch' AND field_code='status';
UPDATE entity_field_registry SET options_json='["REQUESTED","RECEIVED","INSPECTED","DESTINATION_DECIDED","CLOSED"]'
  WHERE entity_code='ReturnNote' AND field_code='status';
UPDATE entity_field_registry SET options_json='["ACTIVE","DOWN","RETIRED"]'
  WHERE entity_code='Asset' AND field_code='status';
UPDATE entity_field_registry SET options_json='["REPORTED","ASSIGNED","IN_PROGRESS","AWAITING_PARTS","RESOLVED","CLOSED","REJECTED"]'
  WHERE entity_code='MaintenanceOrder' AND field_code='status';
UPDATE entity_field_registry SET options_json='["PENDING","DOCS_SUBMITTED","IN_REVIEW","VERIFIED","REJECTED"]'
  WHERE entity_code='KycRecord' AND field_code='status';
UPDATE entity_field_registry SET options_json='["PENDING","APPROVED","REJECTED"]'
  WHERE entity_code='LeaveRequest' AND field_code='status';
UPDATE entity_field_registry SET options_json='["DRAFT","COMPUTED","APPROVED","PAID"]'
  WHERE entity_code='PayrollRun' AND field_code='status';
UPDATE entity_field_registry SET options_json='["DUE","FILED","LATE"]'
  WHERE entity_code='StatutoryReturn' AND field_code='status';
UPDATE entity_field_registry SET options_json='["OPEN","CLOSED_WON","CLOSED_LOST"]'
  WHERE entity_code='Lead' AND field_code='status';
UPDATE entity_field_registry SET options_json='["DRAFT","SENT","ACCEPTED","REJECTED","EXPIRED"]'
  WHERE entity_code='Quote' AND field_code='status';
UPDATE entity_field_registry SET options_json='["OPEN","SCHEDULED","IN_PROGRESS","AWAITING_CUSTOMER","RESOLVED","CLOSED"]'
  WHERE entity_code='ServiceTicket' AND field_code='status';

-- 4. Specific enum field options --------------------------------------
UPDATE entity_field_registry SET options_json='["P1","P2","P3","P4"]'
  WHERE entity_code IN ('MaintenanceOrder','ServiceTicket') AND field_code='priority';
UPDATE entity_field_registry SET options_json='["A","B","C"]'
  WHERE entity_code IN ('Attendance','EnergyReading','ShiftHandover','ShiftSchedule') AND field_code='shift_code';
UPDATE entity_field_registry SET options_json='["RECEIPT","ISSUE","TRANSFER","ADJUST","CONSUME","SPOIL"]'
  WHERE entity_code='StockMovement' AND field_code='kind';
UPDATE entity_field_registry SET options_json='["VENDOR","TRANSFER","RETURN","3RD_PARTY"]'
  WHERE entity_code='GoodsReceiptNote' AND field_code='source_type';
UPDATE entity_field_registry SET options_json='["CUSTOMER","INTERNAL","VENDOR"]'
  WHERE entity_code='ReturnNote' AND field_code='return_kind';
UPDATE entity_field_registry SET options_json='["RESTOCK","SCRAP","REWORK"]'
  WHERE entity_code='ReturnNote' AND field_code='destination';
UPDATE entity_field_registry SET options_json='["ELECTRIC","WATER","STEAM","DIESEL","GAS"]'
  WHERE entity_code='EnergyReading' AND field_code='meter_kind';
UPDATE entity_field_registry SET options_json='["WEEKLY","BIWEEKLY","MONTHLY","QUARTERLY","ANNUAL"]'
  WHERE entity_code='Subscription' AND field_code='frequency';
UPDATE entity_field_registry SET options_json='["COMPREHENSIVE","PARTS_ONLY","LABOUR_ONLY"]'
  WHERE entity_code='AmcContract' AND field_code='cover_kind';
UPDATE entity_field_registry SET options_json='["STRAIGHT_LINE","WDV","UNITS"]'
  WHERE entity_code='AssetDepreciation' AND field_code='method';
UPDATE entity_field_registry SET options_json='["RECEIPT","PAYMENT"]'
  WHERE entity_code='Payment' AND field_code='direction';
UPDATE entity_field_registry SET options_json='["NEFT","RTGS","CHEQUE","CASH","UPI"]'
  WHERE entity_code='Payment' AND field_code='mode';
UPDATE entity_field_registry SET options_json='["CL","SL","EL","ML","LWP"]'
  WHERE entity_code='LeaveRequest' AND field_code='leave_kind';
UPDATE entity_field_registry SET options_json='["GSTR-1","GSTR-3B","PF","ESI","TDS","MSME-1"]'
  WHERE entity_code='StatutoryReturn' AND field_code='return_kind';
UPDATE entity_field_registry SET options_json='["PRESENT","LATE","ABSENT","HALF_DAY","WFH"]'
  WHERE entity_code='Attendance' AND field_code='status';
UPDATE entity_field_registry SET options_json='["BIOMETRIC","MANUAL","RFID","MOBILE"]'
  WHERE entity_code='Attendance' AND field_code='source';
UPDATE entity_field_registry SET options_json='["MANUAL","CONVEYOR","DRUM"]'
  WHERE entity_code='FGDispatch' AND field_code='feed_mode';
UPDATE entity_field_registry SET options_json='["NEW","CONTACTED","QUALIFIED","QUOTED","WON","LOST"]'
  WHERE entity_code='Lead' AND field_code='stage';
UPDATE entity_field_registry SET options_json='["TRADE_SHOW","REFERRAL","WEBSITE","COLD_CALL","INBOUND"]'
  WHERE entity_code='Lead' AND field_code='source';
UPDATE entity_field_registry SET options_json='["LEAKAGE","WRONG_ITEM","PACKAGING","QUANTITY","QUALITY","OTHER"]'
  WHERE entity_code='ServiceTicket' AND field_code='issue_kind';
UPDATE entity_field_registry SET options_json='["BREAKDOWN","PM","CALIBRATION"]'
  WHERE entity_code='MaintenanceOrder' AND field_code='kind';
UPDATE entity_field_registry SET options_json='["WEIGHBRIDGE","CONVEYOR","PUMP","TANK","BOILER","DG","SOLAR"]'
  WHERE entity_code='Asset' AND field_code='category';
UPDATE entity_field_registry SET options_json='["RS232","TCP","MODBUS"]'
  WHERE entity_code='Weighbridge' AND field_code='protocol';
UPDATE entity_field_registry SET options_json='["IN","OUT","BOTH"]'
  WHERE entity_code='Lane' AND field_code='direction';
UPDATE entity_field_registry SET options_json='["FRONT","EXIT"]'
  WHERE entity_code='DispatchGate' AND field_code='gate_type';
UPDATE entity_field_registry SET options_json='["AR","BR","CR","JV","CRC","RC","PMT","CR_NOTE","DR_NOTE","REC","PAY"]'
  WHERE entity_code='Payment' AND field_code='direction' AND options_json IS NULL;

-- 5. *_id columns are foreign keys — set entity-ref hints --------------
-- Vendor: dept_id -> Department; etc. Add the obvious ones.
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"Driver","valueField":"id","labelField":"name"}'
  WHERE entity_code='GatePass' AND field_code='driver_id';
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"Vehicle","valueField":"id","labelField":"registration_no"}'
  WHERE entity_code='GatePass' AND field_code='vehicle_id';
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"Vendor","valueField":"id","labelField":"legalname"}'
  WHERE entity_code IN ('GatePass','PurchaseOrder','GoodsReceiptNote','PurchaseInvoice','AmcContract','KycRecord')
    AND field_code IN ('vendor_id');
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"Asset","valueField":"id","labelField":"asset_code"}'
  WHERE entity_code IN ('MaintenanceOrder','AmcContract','AssetDepreciation') AND field_code='asset_id';
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"Department","valueField":"id","labelField":"name"}'
  WHERE field_code IN ('dept_id','requested_for_dept_id','applied_dept_id');
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"Employee","valueField":"id","labelField":"employeename"}'
  WHERE field_code IN ('employee_id','requested_by','approved_by','reported_by',
                       'technician_id','interviewer_id','operator_id','supervisor_id',
                       'last_actor_id','outgoing_supervisor_id','incoming_supervisor_id',
                       'uploaded_by');
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"GatePass","valueField":"id","labelField":"gp_number"}'
  WHERE field_code='gate_pass_id';
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"SalesOrder","valueField":"id","labelField":"so_number"}'
  WHERE field_code='linked_so_id' OR field_code='so_id';
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"PurchaseOrder","valueField":"id","labelField":"po_number"}'
  WHERE field_code='linked_po_id' OR field_code='po_id';
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"GoodsReceiptNote","valueField":"id","labelField":"grn_number"}'
  WHERE field_code='linked_grn_id' OR field_code='grn_id';
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"ProductionOrder","valueField":"id","labelField":"po_number"}'
  WHERE field_code='production_order_id';
UPDATE entity_field_registry SET widget_kind='entity-ref',
  options_json='{"entity":"MaterialRequisition","valueField":"id","labelField":"mr_number"}'
  WHERE field_code='mr_id';
