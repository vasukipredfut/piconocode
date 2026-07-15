package com.aria.m10.print;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Print template endpoints — return printable HTML for the documents
 * Pico generates on paper today: Gate Pass, GRN, Dispatch Note.
 *
 * <p>The HTML carries an inline {@code @media print} stylesheet so the
 * browser's "Print to PDF" dialog produces a clean single-page voucher.
 * Frontend fetches with the JWT in the auth header, then injects into
 * an {@code about:blank} new tab + calls {@code window.print()}.
 *
 * <p>Endpoints:
 * <pre>
 *   GET /api/m10/print/gate-pass/{id}   text/html  printable gate pass
 *   GET /api/m10/print/grn/{id}         text/html  printable GRN voucher
 *   GET /api/m10/print/dispatch/{id}    text/html  printable dispatch note
 * </pre>
 */
@RestController
@RequestMapping("/api/m10/print")
public class PrintController {

    private final JdbcTemplate jdbc;

    public PrintController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping(value = "/gate-pass/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> gatePass(@PathVariable long id) {
        Map<String, Object> gp;
        try {
            gp = jdbc.queryForMap("SELECT * FROM gate_pass WHERE id=?", id);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(notFound("Gate Pass", id));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHead("Gate Pass " + s(gp.get("gp_number"))));
        sb.append("<body>");
        sb.append("<div class='voucher'>");
        sb.append("<div class='header'>")
          .append("<div><h1>PICO POWER PROJECT</h1><small>Coimbatore · TN</small></div>")
          .append("<div class='gp-no'><div class='lbl'>GATE PASS</div><div class='num'>")
          .append(s(gp.get("gp_number"))).append("</div></div>")
          .append("</div>");
        sb.append("<div class='qr-placeholder'>QR: ").append(s(gp.get("gp_number"))).append("</div>");
        sb.append("<table class='kv'>");
        kvRow(sb, "Flow", friendlyFlow(s(gp.get("flow_variant_code"))));
        kvRow(sb, "Vehicle Number", s(gp.get("vehicle_no")));
        kvRow(sb, "Vehicle Type", s(gp.get("vehicle_type")));
        kvRow(sb, "Driver Name", s(gp.get("driver_name")));
        kvRow(sb, "Driver Phone", s(gp.get("driver_phone")));
        kvRow(sb, "From / To", s(gp.get("source_party")));
        kvRow(sb, "Reference (PO/SO/RN)", s(gp.get("reference_no")));
        kvRow(sb, "Status", s(gp.get("status")));
        kvRow(sb, "Current Step", friendlyStep(s(gp.get("current_step"))));
        kvRow(sb, "Created", s(gp.get("created_at")));
        kvRow(sb, "Closed", s(gp.get("closed_at")));
        sb.append("</table>");
        if (gp.get("w1_weight_kg") != null || gp.get("w2_weight_kg") != null || gp.get("w3_weight_kg") != null) {
            sb.append("<h3>Weighment</h3><table class='wt'>");
            sb.append("<tr><th>W1 (Loaded)</th><th>W2 (Empty)</th><th>W3 (Recheck)</th><th>Total Unloaded</th></tr>");
            sb.append("<tr>")
              .append("<td>").append(num(gp.get("w1_weight_kg"))).append(" kg</td>")
              .append("<td>").append(num(gp.get("w2_weight_kg"))).append(" kg</td>")
              .append("<td>").append(num(gp.get("w3_weight_kg"))).append(" kg</td>")
              .append("<td>").append(num(gp.get("total_unloading_qty"))).append(" kg</td>")
              .append("</tr></table>");
        }
        sb.append("<div class='sig'>")
          .append("<div class='box'><div>Security</div><div class='line'>&nbsp;</div></div>")
          .append("<div class='box'><div>Dispatch Officer</div><div class='line'>&nbsp;</div></div>")
          .append("<div class='box'><div>Manager / Approver</div><div class='line'>&nbsp;</div></div>")
          .append("</div>");
        sb.append("<div class='footer'>Printed from EARMS at ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</div>");
        sb.append("</div></body></html>");
        return ResponseEntity.ok(sb.toString());
    }

    @GetMapping(value = "/grn/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> grn(@PathVariable long id) {
        Map<String, Object> g;
        try {
            g = jdbc.queryForMap("SELECT * FROM goods_receipt_note WHERE id=?", id);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(notFound("GRN", id));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHead("GRN " + s(g.get("grn_number"))));
        sb.append("<body><div class='voucher'>");
        sb.append("<div class='header'><div><h1>PICO POWER PROJECT</h1><small>Coimbatore · TN · GSTIN 33XXXXX0000Z1</small></div>")
          .append("<div class='gp-no'><div class='lbl'>GOODS RECEIPT NOTE</div><div class='num'>")
          .append(s(g.get("grn_number"))).append("</div></div></div>");
        sb.append("<table class='kv'>");
        kvRow(sb, "PO Number", s(g.get("po_number")));
        kvRow(sb, "Vendor", s(g.get("vendor_name")));
        kvRow(sb, "Invoice #", s(g.get("invoice_number")));
        kvRow(sb, "Invoice Date", s(g.get("invoice_date")));
        kvRow(sb, "Invoice Value (Rs)", num(g.get("invoice_value")));
        kvRow(sb, "Vehicle", s(g.get("vehicle_no")));
        kvRow(sb, "Transporter", s(g.get("transporter_name")));
        kvRow(sb, "E-Way Bill #", s(g.get("ewb_no")));
        kvRow(sb, "Source", s(g.get("source_type")));
        kvRow(sb, "Status", s(g.get("status")));
        kvRow(sb, "Step", s(g.get("current_step")));
        kvRow(sb, "Received", s(g.get("received_at")));
        sb.append("</table>");
        // Try to load lines
        try {
            var lines = jdbc.queryForList("SELECT * FROM goods_receipt_line WHERE goods_receipt_note_id=?", id);
            if (!lines.isEmpty()) {
                sb.append("<h3>Line Items</h3><table class='lines'>");
                sb.append("<tr><th>#</th><th>Item</th><th>Qty</th><th>UOM</th><th>Batch</th><th>Bin</th></tr>");
                int i = 1;
                for (Map<String,Object> line : lines) {
                    sb.append("<tr>")
                      .append("<td>").append(i++).append("</td>")
                      .append("<td>").append(s(line.get("item_code"))).append("</td>")
                      .append("<td>").append(num(line.get("qty"))).append("</td>")
                      .append("<td>").append(s(line.get("uom"))).append("</td>")
                      .append("<td>").append(s(line.get("batch_no"))).append("</td>")
                      .append("<td>").append(s(line.get("bin_code"))).append("</td>")
                      .append("</tr>");
                }
                sb.append("</table>");
            }
        } catch (Exception ignored) {}
        sb.append("<div class='sig'>")
          .append("<div class='box'><div>Stores</div><div class='line'>&nbsp;</div></div>")
          .append("<div class='box'><div>QC Officer</div><div class='line'>&nbsp;</div></div>")
          .append("<div class='box'><div>Manager</div><div class='line'>&nbsp;</div></div>")
          .append("</div>");
        sb.append("<div class='footer'>Printed from EARMS at ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</div>");
        sb.append("</div></body></html>");
        return ResponseEntity.ok(sb.toString());
    }

    // ---- helpers -----------------------------------------------------------

    private static String htmlHead(String title) {
        return "<!doctype html><html><head><meta charset='utf-8'><title>" + esc(title) + "</title>" +
               "<style>" +
               "body{font-family:-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;margin:0;padding:24px;color:#222;background:#fafafa}" +
               ".voucher{max-width:800px;margin:0 auto;background:white;padding:32px;border:1px solid #ddd;border-radius:6px}" +
               ".header{display:flex;justify-content:space-between;align-items:start;border-bottom:3px solid #0d47a1;padding-bottom:12px;margin-bottom:16px}" +
               ".header h1{margin:0;font-size:22px;color:#0d47a1;letter-spacing:1px}" +
               ".header small{color:#777;font-size:12px}" +
               ".gp-no{text-align:right}" +
               ".gp-no .lbl{font-size:11px;color:#888;letter-spacing:2px}" +
               ".gp-no .num{font-size:24px;font-weight:700;color:#0d47a1}" +
               ".qr-placeholder{display:inline-block;border:2px dashed #aaa;padding:18px 22px;margin:8px 0;font-family:monospace;font-size:13px;color:#666;background:#f5f5f5}" +
               "table{width:100%;border-collapse:collapse;margin:12px 0}" +
               "table.kv td{padding:6px 8px;border-bottom:1px dotted #ddd;font-size:13px}" +
               "table.kv td:first-child{font-weight:600;color:#555;width:40%}" +
               "table.wt,table.lines{margin-top:8px}" +
               "table.wt th,table.lines th{background:#f0f4f8;text-align:left;padding:8px;border-bottom:1px solid #ccc;font-size:11px;text-transform:uppercase;letter-spacing:0.5px;color:#555}" +
               "table.wt td,table.lines td{padding:8px;border-bottom:1px solid #eee;font-size:13px}" +
               "h3{font-size:14px;color:#0d47a1;margin-top:24px;border-bottom:1px solid #ddd;padding-bottom:4px}" +
               ".sig{display:flex;gap:24px;margin-top:48px}" +
               ".sig .box{flex:1;font-size:11px;color:#666}" +
               ".sig .line{border-bottom:1px solid #999;margin-top:36px;height:1px}" +
               ".footer{margin-top:32px;text-align:center;font-size:10px;color:#aaa;border-top:1px solid #eee;padding-top:12px}" +
               "@media print{body{background:white;padding:0}.voucher{border:none;box-shadow:none;max-width:100%;padding:16px}" +
               ".no-print{display:none !important}}" +
               "@page{size:A4;margin:12mm}" +
               "</style></head>";
    }

    private static String notFound(String kind, long id) {
        return "<html><body style='font-family:sans-serif;padding:32px'>" +
               "<h2>" + kind + " #" + id + " not found</h2></body></html>";
    }

    private static void kvRow(StringBuilder sb, String k, String v) {
        if (v == null || v.isBlank()) return;
        sb.append("<tr><td>").append(esc(k)).append("</td><td>").append(esc(v)).append("</td></tr>");
    }

    private static String s(Object o) { return o == null ? "" : String.valueOf(o); }
    private static String num(Object o) {
        if (o == null) return "—";
        try { return String.format("%,.2f", ((Number)o).doubleValue()); }
        catch (Exception e) { return String.valueOf(o); }
    }
    private static String esc(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }

    private static String friendlyFlow(String code) {
        if (code == null) return "";
        return switch (code) {
            case "M10.TankerInbound" -> "Tanker Unloading (RM)";
            case "M10.PackUnload" -> "Container Unloading (Packing)";
            case "M10.ReturnStockIn" -> "Return Stock Unloading";
            case "M10.TankerOutbound" -> "Tanker Loading";
            case "M10.PackLoading" -> "Packing Material Loading";
            case "M10.FGLoading" -> "FG / Container Loading";
            default -> code;
        };
    }

    private static String friendlyStep(String code) {
        if (code == null) return "";
        return switch (code) {
            case "GATE_PASS_CREATED" -> "Gate Pass Created";
            case "PARKING_IN" -> "At Parking";
            case "DISPATCH_QUEUED" -> "Queued for QC";
            case "QC_APPROVED" -> "QC Approved";
            case "QC_REJECTED" -> "QC Rejected";
            case "WEIGHMENT_LOADED" -> "W1 — Loaded";
            case "UNLOADING_DONE" -> "Unloading Done";
            case "WEIGHMENT_EMPTY" -> "W2 — Empty";
            case "WEIGHMENT_RECHECK" -> "W3 — Re-check";
            case "CLOSED" -> "Closed";
            default -> code;
        };
    }
}
