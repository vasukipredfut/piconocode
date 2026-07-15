-- V71: Fix the 62 widget-gap rows the Field Atlas generator flagged.
-- Three patterns:
--   (a) entity-ref fields with no options_json (assignee_id, assigned_by,
--       default_vehicle_id, default_driver_id, requested_for_dept_id ...)
--   (b) select fields with no options_json (status, vehicle_type,
--       ownership, party_kind, frequency, esign_status ...)
--   (c) ENUM data type but NULL widget_kind
-- Each fix is targeted to the field's actual usage; not a blanket downgrade.

-- ===========================================================================
-- (a) entity-ref source spec — bulk fix for assignee_id / assigned_by
-- ===========================================================================
UPDATE entity_field_registry
SET options_json = JSON_OBJECT('entity','Employee','valueField','id','labelField','employeename')
WHERE field_code IN ('assignee_id','assigned_by','approved_by','reviewed_by','requested_by','rejected_by','created_by','updated_by')
  AND widget_kind='entity-ref'
  AND (options_json IS NULL OR options_json='');

-- Driver → default_vehicle_id picker over Vehicle master
UPDATE entity_field_registry
SET options_json = JSON_OBJECT('entity','Vehicle','valueField','id','labelField','vehicle_no')
WHERE field_code='default_vehicle_id' AND widget_kind='entity-ref'
  AND (options_json IS NULL OR options_json='');

-- Vehicle → default_driver_id picker over Driver master
UPDATE entity_field_registry
SET options_json = JSON_OBJECT('entity','Driver','valueField','id','labelField','name')
WHERE field_code='default_driver_id' AND widget_kind='entity-ref'
  AND (options_json IS NULL OR options_json='');

-- MR → requested_for_dept_id picker over Department master
UPDATE entity_field_registry
SET options_json = JSON_OBJECT('entity','Department','valueField','id','labelField','departmentname')
WHERE field_code='requested_for_dept_id' AND widget_kind='entity-ref'
  AND (options_json IS NULL OR options_json='');

-- ===========================================================================
-- (b) select-with-no-options — give each a sensible workflow state list
-- ===========================================================================

-- GatePass.status (workflow-driven; broad set covering all 6 vehicle flows)
UPDATE entity_field_registry
SET options_json = JSON_ARRAY('DRAFT','IN_PROGRESS','CLOSED','REJECTED','CANCELLED')
WHERE entity_code='GatePass' AND field_code='status' AND widget_kind='select';

-- GatePass.vehicle_type (from PICO sheet 1 enumeration)
UPDATE entity_field_registry
SET options_json = JSON_ARRAY('TANKER','CONTAINER','OPEN','MINI','LCV')
WHERE entity_code='GatePass' AND field_code='vehicle_type' AND widget_kind='select';

-- GatePass.source_party_type
UPDATE entity_field_registry
SET options_json = JSON_ARRAY('VENDOR','CUSTOMER','INTER_UNIT','THIRD_PARTY','RETURN')
WHERE entity_code='GatePass' AND field_code='source_party_type' AND widget_kind='select';

-- BillOfMaterials.status
UPDATE entity_field_registry
SET options_json = JSON_ARRAY('DRAFT','ACTIVE','SUPERSEDED','DEPRECATED')
WHERE entity_code='BillOfMaterials' AND field_code='status' AND widget_kind='select';

-- DispatchEvent.status (same set as gate_pass since they share the flow)
UPDATE entity_field_registry
SET options_json = JSON_ARRAY('DRAFT','IN_PROGRESS','CLOSED','REJECTED','CANCELLED')
WHERE entity_code='DispatchEvent' AND field_code='status' AND widget_kind='select';

-- Document.esign_status
UPDATE entity_field_registry
SET options_json = JSON_ARRAY('PENDING','SENT','SIGNED','REJECTED','EXPIRED')
WHERE entity_code='Document' AND field_code='esign_status' AND widget_kind='select';

-- KycRecord.party_kind
UPDATE entity_field_registry
SET options_json = JSON_ARRAY('VENDOR','CUSTOMER','EMPLOYEE','DRIVER','TRANSPORTER','CONTRACTOR')
WHERE entity_code='KycRecord' AND field_code='party_kind' AND widget_kind='select';

-- QualityPlan.frequency
UPDATE entity_field_registry
SET options_json = JSON_ARRAY('EVERY_BATCH','EVERY_SHIFT','DAILY','WEEKLY','MONTHLY','RANDOM_AQL')
WHERE entity_code='QualityPlan' AND field_code='frequency' AND widget_kind='select';

-- ===========================================================================
-- Cover the LONG TAIL: any remaining select-with-no-options for a `status`
-- field — default to a generic workflow lifecycle so the dropdown isn't
-- empty. Specific entities can override later.
-- ===========================================================================
UPDATE entity_field_registry
SET options_json = JSON_ARRAY('DRAFT','SUBMITTED','APPROVED','REJECTED','CANCELLED','COMPLETED','ON_HOLD')
WHERE field_code='status' AND widget_kind='select'
  AND (options_json IS NULL OR options_json='');

-- ===========================================================================
-- (c) ENUM data type with NULL widget — promote to select
-- ===========================================================================
UPDATE entity_field_registry
SET widget_kind='select',
    options_json = JSON_ARRAY('DRAFT','PUBLISHED','SUPERSEDED','DEPRECATED')
WHERE entity_code='PageDefinition' AND field_code='status' AND widget_kind IS NULL;

-- Generic ENUM fallback — anything still NULL widget for ENUM type
UPDATE entity_field_registry
SET widget_kind='select'
WHERE data_type='ENUM' AND (widget_kind IS NULL OR widget_kind='');
