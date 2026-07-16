package com.aria.print;

import com.aria.security.TenantContext;
import com.aria.tenant.TenantSettings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Print — a first-class, config-driven feature. A printable document is a
 * {@code print_template} row (DATA): target table (+ optional child table), a
 * JSON spec describing the voucher, and an optional printer integration. ANY
 * page or workflow prints via {@code GET /api/print/{code}/{id}}; there is no
 * per-document Java. Replaces the old hardcoded m10 gate-pass/GRN controller.
 *
 * <p>The physical printer is configured once in Integrations (M11) — a template
 * with {@code printer_integration_code} routes rendered output to that device
 * via {@code POST /api/print/{code}/{id}/send}; otherwise the browser prints the
 * returned HTML.
 */
@RestController
public class PrintController {

    private static final Pattern SAFE_IDENT = Pattern.compile("^[A-Za-z0-9_]+$");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JdbcTemplate jdbc;
    private final TenantSettings settings;

    // Optional — routes label output to a configured printer (M11). When absent
    // (e.g. unit tests), /send returns the rendered ZPL instead of printing.
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.aria.integration.service.IntegrationOutboundCaller outboundCaller;

    public PrintController(JdbcTemplate jdbc, TenantSettings settings) {
        this.jdbc = jdbc;
        this.settings = settings;
    }

    // Canonical generic endpoint.
    @GetMapping(value = "/api/print/{code}/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> render(@PathVariable String code, @PathVariable long id) {
        return doRender(code, id);
    }

    // Back-compat: the frontend action fires /api/m10/print/{kind}/{id}.
    @GetMapping(value = "/api/m10/print/{code}/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> renderLegacy(@PathVariable String code, @PathVariable long id) {
        return doRender(code, id);
    }

    /** Send a label document to its configured printer integration (or render ZPL if none). */
    @PostMapping(value = {"/api/print/{code}/{id}/send", "/api/m10/print/{code}/{id}/send"})
    public ResponseEntity<?> send(@PathVariable String code, @PathVariable long id) {
        Long tenantId = TenantContext.get();
        Map<String, Object> tpl;
        try {
            tpl = jdbc.queryForMap("SELECT * FROM print_template WHERE code=? AND tenant_id=?", code, tenantId);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "no template '" + code + "'"));
        }
        Map<String, Object> row;
        try {
            row = jdbc.queryForMap("SELECT * FROM " + ident((String) tpl.get("target_table")) + " WHERE id=?", id);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", code + " #" + id + " not found"));
        }
        String zpl = renderZpl(tpl, row);
        String printer = (String) tpl.get("printer_integration_code");
        if (printer == null || printer.isBlank() || outboundCaller == null) {
            return ResponseEntity.ok(Map.of("ok", true, "printed", false, "zpl", zpl,
                    "note", printer == null ? "no printer configured" : "printer routing unavailable"));
        }
        try {
            Long instanceId = jdbc.queryForObject(
                    "SELECT id FROM integration_instance WHERE code=? AND tenant_id=?",
                    Long.class, printer, tenantId);
            Object result = outboundCaller.invoke(instanceId, "printZpl", Map.of("zpl", zpl));
            return ResponseEntity.ok(Map.of("ok", true, "printed", true, "printer", printer, "result", result));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("ok", false, "error", e.getMessage(), "zpl", zpl));
        }
    }

    private ResponseEntity<String> doRender(String code, long id) {
        Long tenantId = TenantContext.get();
        Map<String, Object> tpl;
        try {
            tpl = jdbc.queryForMap(
                    "SELECT * FROM print_template WHERE code=? AND tenant_id=?", code, tenantId);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(errorPage("No print template '" + code + "'"));
        }
        String table = ident((String) tpl.get("target_table"));
        Map<String, Object> row;
        try {
            row = jdbc.queryForMap("SELECT * FROM " + table + " WHERE id=?", id);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(errorPage(code + " #" + id + " not found"));
        }
        // Label documents preview as their ZPL (real QR via ^BQ; sent to a
        // printer through POST .../send). HTML documents render the voucher.
        if ("label".equalsIgnoreCase(s(tpl.get("output_kind")))) {
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .body(renderZpl(tpl, row));
        }
        List<Map<String, Object>> lines = List.of();
        String childTable = (String) tpl.get("child_table");
        String childFk = (String) tpl.get("child_fk");
        if (childTable != null && childFk != null) {
            try {
                lines = jdbc.queryForList(
                        "SELECT * FROM " + ident(childTable) + " WHERE " + ident(childFk) + "=?", id);
            } catch (Exception ignored) { }
        }
        try {
            JsonNode spec = MAPPER.readTree((String) tpl.get("spec_json"));
            return ResponseEntity.ok(renderVoucher(spec, row, lines));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorPage("Bad template spec: " + e.getMessage()));
        }
    }

    // ---- rendering ---------------------------------------------------------

    private String renderVoucher(JsonNode spec, Map<String, Object> row, List<Map<String, Object>> lines) {
        String docLabel = spec.path("documentLabel").asText("DOCUMENT");
        String numberCol = spec.path("numberColumn").asText(null);
        String number = numberCol == null ? "" : s(row.get(numberCol));
        StringBuilder sb = new StringBuilder();
        sb.append(head(docLabel + " " + number));
        sb.append("<body><div class='voucher'>");
        sb.append("<div class='header'><div><h1>")
          .append(esc(settings.get("company_name", "Company").toUpperCase()))
          .append("</h1><small>").append(esc(settings.get("company_tagline", "")))
          .append("</small></div><div class='gp-no'><div class='lbl'>").append(esc(docLabel))
          .append("</div><div class='num'>").append(esc(number)).append("</div></div></div>");

        if (spec.path("showQr").asBoolean(false) && !number.isBlank()) {
            String dataUri = qrDataUri(number, 160);
            if (dataUri != null)
                sb.append("<div class='qr-box'><img class='qr-img' src='").append(dataUri)
                  .append("' alt='QR ").append(esc(number)).append("'></div>");
            else
                sb.append("<div class='qr-placeholder'>QR: ").append(esc(number)).append("</div>");
        }

        // key/value rows
        sb.append("<table class='kv'>");
        for (JsonNode r : spec.path("rows")) {
            String label = r.path("label").asText("");
            String col = r.path("column").asText("");
            String v = r.path("num").asBoolean(false) ? num(row.get(col)) : mapValue(r, s(row.get(col)));
            if (v == null || v.isBlank() || "—".equals(v)) continue;
            sb.append("<tr><td>").append(esc(label)).append("</td><td>").append(esc(v)).append("</td></tr>");
        }
        sb.append("</table>");

        // conditional sections (a heading + one-row table)
        for (JsonNode sec : spec.path("sections")) {
            boolean show = true;
            if (sec.has("showIfAny")) {
                show = false;
                for (JsonNode c : sec.get("showIfAny")) if (row.get(c.asText()) != null) { show = true; break; }
            }
            if (!show) continue;
            sb.append("<h3>").append(esc(sec.path("heading").asText(""))).append("</h3>");
            JsonNode tbl = sec.path("table");
            sb.append("<table class='wt'><tr>");
            for (JsonNode h : tbl.path("headers")) sb.append("<th>").append(esc(h.asText())).append("</th>");
            sb.append("</tr><tr>");
            for (JsonNode c : tbl.path("cells")) {
                String v = c.path("num").asBoolean(false) ? num(row.get(c.path("column").asText()))
                                                          : s(row.get(c.path("column").asText()));
                sb.append("<td>").append(esc(v)).append(esc(c.path("suffix").asText(""))).append("</td>");
            }
            sb.append("</tr></table>");
        }

        // line items from the child table
        JsonNode li = spec.path("lineItems");
        if (!li.isMissingNode() && !lines.isEmpty()) {
            sb.append("<h3>").append(esc(li.path("heading").asText("Line Items"))).append("</h3>");
            sb.append("<table class='lines'><tr><th>#</th>");
            for (JsonNode colDef : li.path("columns")) sb.append("<th>").append(esc(colDef.path("label").asText())).append("</th>");
            sb.append("</tr>");
            int i = 1;
            for (Map<String, Object> line : lines) {
                sb.append("<tr><td>").append(i++).append("</td>");
                for (JsonNode colDef : li.path("columns")) {
                    String v = colDef.path("num").asBoolean(false) ? num(line.get(colDef.path("column").asText()))
                                                                  : s(line.get(colDef.path("column").asText()));
                    sb.append("<td>").append(esc(v)).append("</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        // signature boxes
        if (spec.has("signatures")) {
            sb.append("<div class='sig'>");
            for (JsonNode sig : spec.get("signatures"))
                sb.append("<div class='box'><div>").append(esc(sig.asText()))
                  .append("</div><div class='line'>&nbsp;</div></div>");
            sb.append("</div>");
        }
        sb.append("<div class='footer'>Printed from EARMS at ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
          .append("</div></div></body></html>");
        return sb.toString();
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    /** Render a ZPL label template: {{company}}, {{copies}}, and {{column}} from the row. */
    private String renderZpl(Map<String, Object> tpl, Map<String, Object> row) {
        String zpl = s(tpl.get("label_zpl"));
        if (zpl.isBlank()) return "";
        int copies = tpl.get("copies") == null ? 1 : ((Number) tpl.get("copies")).intValue();
        Matcher m = PLACEHOLDER.matcher(zpl);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String val = switch (key) {
                case "company" -> settings.get("company_name", "Company");
                case "copies"  -> String.valueOf(copies);
                default        -> s(row.get(key));
            };
            m.appendReplacement(out, Matcher.quoteReplacement(zplEsc(val)));
        }
        m.appendTail(out);
        return out.toString();
    }

    /** Strip ZPL control chars (^ ~) from substituted field values. */
    private static String zplEsc(String s) {
        return s == null ? "" : s.replace("^", " ").replace("~", " ");
    }

    /** Real, scannable QR (PNG data URI) for {@code content}, or null if encoding fails. */
    private static String qrDataUri(String content, int size) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < size; x++)
                for (int y = 0; y < size; y++)
                    img.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            return null;
        }
    }

    private static String mapValue(JsonNode rowSpec, String raw) {
        JsonNode map = rowSpec.path("map");
        if (map.isObject() && raw != null && map.has(raw)) return map.get(raw).asText();
        return raw;
    }

    private static String head(String title) {
        return "<!doctype html><html><head><meta charset='utf-8'><title>" + esc(title) + "</title><style>" +
               "body{font-family:-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;margin:0;padding:24px;color:#222;background:#fafafa}" +
               ".voucher{max-width:800px;margin:0 auto;background:white;padding:32px;border:1px solid #ddd;border-radius:6px}" +
               ".header{display:flex;justify-content:space-between;align-items:start;border-bottom:3px solid #0d47a1;padding-bottom:12px;margin-bottom:16px}" +
               ".header h1{margin:0;font-size:22px;color:#0d47a1;letter-spacing:1px}.header small{color:#777;font-size:12px}" +
               ".gp-no{text-align:right}.gp-no .lbl{font-size:11px;color:#888;letter-spacing:2px}.gp-no .num{font-size:24px;font-weight:700;color:#0d47a1}" +
               ".qr-placeholder{display:inline-block;border:2px dashed #aaa;padding:18px 22px;margin:8px 0;font-family:monospace;font-size:13px;color:#666;background:#f5f5f5}" +
               ".qr-box{margin:8px 0}.qr-img{width:160px;height:160px;image-rendering:pixelated}" +
               "table{width:100%;border-collapse:collapse;margin:12px 0}" +
               "table.kv td{padding:6px 8px;border-bottom:1px dotted #ddd;font-size:13px}table.kv td:first-child{font-weight:600;color:#555;width:40%}" +
               "table.wt th,table.lines th{background:#f0f4f8;text-align:left;padding:8px;border-bottom:1px solid #ccc;font-size:11px;text-transform:uppercase;letter-spacing:0.5px;color:#555}" +
               "table.wt td,table.lines td{padding:8px;border-bottom:1px solid #eee;font-size:13px}" +
               "h3{font-size:14px;color:#0d47a1;margin-top:24px;border-bottom:1px solid #ddd;padding-bottom:4px}" +
               ".sig{display:flex;gap:24px;margin-top:48px}.sig .box{flex:1;font-size:11px;color:#666}.sig .line{border-bottom:1px solid #999;margin-top:36px;height:1px}" +
               ".footer{margin-top:32px;text-align:center;font-size:10px;color:#aaa;border-top:1px solid #eee;padding-top:12px}" +
               "@media print{body{background:white;padding:0}.voucher{border:none;box-shadow:none;max-width:100%;padding:16px}}@page{size:A4;margin:12mm}" +
               "</style></head>";
    }

    private static String errorPage(String msg) {
        return "<html><body style='font-family:sans-serif;padding:32px'><h2>" + esc(msg) + "</h2></body></html>";
    }

    private static String ident(String s) {
        if (s == null || !SAFE_IDENT.matcher(s).matches()) throw new IllegalArgumentException("unsafe identifier: " + s);
        return s;
    }
    private static String s(Object o) { return o == null ? "" : String.valueOf(o); }
    private static String num(Object o) {
        if (o == null) return "—";
        try { return String.format("%,.2f", ((Number) o).doubleValue()); } catch (Exception e) { return String.valueOf(o); }
    }
    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
