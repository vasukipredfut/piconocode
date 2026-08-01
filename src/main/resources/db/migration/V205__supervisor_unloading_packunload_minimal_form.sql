-- aria:supervisor-unloading gates its entire item-count / batch-expiry /
-- bin-allocation / damage-capture / GRN section on a single condition:
-- "#form.flow_variant_code != 'M10.TankerInbound'" -- meaning it shows for
-- every OTHER flow, including M10.PackUnload. Per the process spec,
-- PackUnload's "Unloading Process" step is meant to be exactly what
-- TankerInbound already gets: just Start Time / Finish Time / Supervisor
-- Name (start_ts / finish_ts / supervisor_id, all already unconditionally
-- visible, no visibleWhen gate) -- reported live: PackUnload was showing
-- the full goods-receipt form (item/SKU, UOM, quantities, batch/expiry,
-- godown/bin, damage capture, GRN creation) with several fields required,
-- none of which the process calls for at this step.
--
-- All 30 occurrences of the guard condition share the exact same literal
-- text (verified: this string appears nowhere else in the page, e.g. not
-- inside any label), so a single blanket REPLACE across the whole
-- body_json correctly extends both visibleWhen and requiredWhen on every
-- affected field (flow_kind, reference_no, item_code, item_description,
-- uom, received_qty, invoice_qty, qty_difference_display, qty_diff_reason,
-- batch_no, mfg_date, expiry_date, godown_code, bin_code, stack_pattern,
-- storage_condition, damaged_qty, damage_reason, short_qty, damage_photo,
-- notes, grn_to_create) in one pass.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '#form.flow_variant_code != ''M10.TankerInbound''',
  '#form.flow_variant_code != ''M10.TankerInbound'' && #form.flow_variant_code != ''M10.PackUnload'''
)
WHERE pd.code = 'aria:supervisor-unloading' AND pv.status = 'PUBLISHED'
  AND pv.body_json LIKE '%#form.flow_variant_code != ''M10.TankerInbound''%'
  AND pv.body_json NOT LIKE '%#form.flow_variant_code != ''M10.TankerInbound'' && #form.flow_variant_code != ''M10.PackUnload''%';
