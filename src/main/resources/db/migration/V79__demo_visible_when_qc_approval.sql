-- V79 — demo wiring for V78 field-rule columns.
--
-- Puts real rules on a real entity so the substrate is exercised the
-- moment V78 lands. Chose WorkflowInstance because pico:qc-approval
-- writes to `submittedData.reject_reason` — an obvious "show only when
-- decision is REJECTED" case a plant manager would recognise.

-- reject_reason on WorkflowInstance is a TEXT field that only makes
-- sense when the QC officer rejected the batch.
INSERT IGNORE INTO entity_field_registry
    (entity_code, field_code, display_label, data_type, widget_kind,
     required, filterable, sortable, searchable,
     visible_when, required_when)
VALUES
    ('WorkflowInstance', 'reject_reason', 'Reject reason', 'TEXT', 'textarea',
     0, 0, 0, 1,
     '#form.decision == ''REJECTED''',
     '#form.decision == ''REJECTED''');

-- If the row already exists (from V72 auto-register), stamp the rules
-- onto it. Idempotent thanks to the exact-match WHERE.
UPDATE entity_field_registry
   SET visible_when  = '#form.decision == ''REJECTED''',
       required_when = '#form.decision == ''REJECTED'''
 WHERE entity_code = 'WorkflowInstance'
   AND field_code  = 'reject_reason'
   AND (visible_when IS NULL OR required_when IS NULL);

-- Also demo `computed_from` on a WeighmentEntry.net_kg if the entity/
-- column exists — safe INSERT IGNORE + guarded UPDATE.
INSERT IGNORE INTO entity_field_registry
    (entity_code, field_code, display_label, data_type, widget_kind,
     required, filterable, sortable, searchable, computed_from)
SELECT 'WeighmentEntry', 'net_kg', 'Net kg', 'DECIMAL', 'number',
       0, 1, 1, 0, '#form.gross_kg - #form.tare_kg'
  FROM dual
 WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code='WeighmentEntry');

UPDATE entity_field_registry
   SET computed_from = '#form.gross_kg - #form.tare_kg'
 WHERE entity_code = 'WeighmentEntry'
   AND field_code  = 'net_kg'
   AND computed_from IS NULL;
