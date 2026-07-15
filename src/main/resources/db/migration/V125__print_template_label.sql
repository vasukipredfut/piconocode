-- V125 — label/ZPL output mode for the print feature.
--
-- Some documents (e.g. gate pass) print as multiple full-size labels with a
-- real scannable QR on a label printer, not an A4 voucher. A print_template can
-- now be output_kind='label': label_zpl is a ZPL II template ({{col}} +
-- {{company}} substituted; QR via the printer's native ^BQ command), printed
-- `copies` times (^PQ), and sent to the printer configured in Integrations
-- (printer_integration_code -> an M9_LABEL_PRINTER_ZPL integration_instance).

ALTER TABLE print_template
    ADD COLUMN output_kind VARCHAR(16) NOT NULL DEFAULT 'html' AFTER name,
    ADD COLUMN label_zpl   TEXT        NULL                    AFTER spec_json,
    ADD COLUMN copies       INT        NOT NULL DEFAULT 1       AFTER label_zpl;
