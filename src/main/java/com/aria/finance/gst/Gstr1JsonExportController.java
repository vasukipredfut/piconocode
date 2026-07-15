package com.aria.finance.gst;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * GSTR-1 JSON export — produces the JSON payload that can be uploaded
 * directly to the GST portal via the Offline Tool.
 *
 *  GET /api/m6/gstr1/json?period=YYYY-MM
 *
 * Schema follows GSTN public spec v2.2 for the JSON template that goes
 * into the Offline Tool. We only emit the sections we populate:
 *   gstin, fp, b2b, b2cs, hsn.
 */
@RestController
@RequestMapping("/api/m6/gstr1")
public class Gstr1JsonExportController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();
    private static final String FILER_GSTIN = "33AAACS1234L1ZA";

    public Gstr1JsonExportController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/json")
    public Object exportJson(@RequestParam String period) {
        ObjectNode root = json.createObjectNode();
        root.put("gstin", FILER_GSTIN);
        // GSTN expects period as MMYYYY (no separator)
        String fp = period.substring(5) + period.substring(0, 4);
        root.put("fp", fp);
        root.put("gt", 0);
        root.put("cur_gt", 0);

        // B2B: invoices to registered customers (GSTIN present)
        List<Map<String, Object>> b2bInvs = jdbc.queryForList(
            "SELECT si.id, si.invoice_no, si.invoice_date, si.gstin, si.customer_name, " +
            "si.subtotal, si.tax_amount, si.total_amount, si.place_of_supply_state, si.irn " +
            "FROM m6_sales_invoice si " +
            "WHERE DATE_FORMAT(si.invoice_date,'%Y-%m')=? AND si.gstin IS NOT NULL AND si.gstin <> ''",
            period);

        // Group by counter-party gstin
        Map<String, List<Map<String, Object>>> byGstin = new LinkedHashMap<>();
        for (Map<String, Object> inv : b2bInvs) {
            byGstin.computeIfAbsent((String) inv.get("gstin"), k -> new ArrayList<>()).add(inv);
        }
        ArrayNode b2bArr = json.createArrayNode();
        for (Map.Entry<String, List<Map<String, Object>>> e : byGstin.entrySet()) {
            ObjectNode ctin = json.createObjectNode();
            ctin.put("ctin", e.getKey());
            ArrayNode invArr = json.createArrayNode();
            for (Map<String, Object> inv : e.getValue()) {
                ObjectNode i = json.createObjectNode();
                i.put("inum", (String) inv.get("invoice_no"));
                i.put("idt",  formatDate(inv.get("invoice_date")));
                i.put("val",  nz(inv.get("total_amount")).doubleValue());
                i.put("pos",  String.valueOf(inv.get("place_of_supply_state")));
                i.put("rchrg", "N");
                i.put("inv_typ", "R");
                if (inv.get("irn") != null) i.put("irn", (String) inv.get("irn"));
                // Lines as itms array
                ArrayNode itms = json.createArrayNode();
                List<Map<String, Object>> lines = jdbc.queryForList(
                    "SELECT line_no, hsn_code, taxable_amount, cgst_amount, sgst_amount, igst_amount, cgst_pct, sgst_pct, igst_pct " +
                    "FROM m6_sales_invoice_line WHERE invoice_id=? ORDER BY line_no",
                    ((Number) inv.get("id")).longValue());
                for (Map<String, Object> ln : lines) {
                    ObjectNode it = json.createObjectNode();
                    it.put("num", ((Number) ln.get("line_no")).intValue());
                    ObjectNode itm = json.createObjectNode();
                    BigDecimal rate = nz(ln.get("igst_pct")).max(nz(ln.get("cgst_pct")).add(nz(ln.get("sgst_pct"))));
                    itm.put("rt", rate.doubleValue());
                    itm.put("txval", nz(ln.get("taxable_amount")).doubleValue());
                    itm.put("iamt", nz(ln.get("igst_amount")).doubleValue());
                    itm.put("camt", nz(ln.get("cgst_amount")).doubleValue());
                    itm.put("samt", nz(ln.get("sgst_amount")).doubleValue());
                    itm.put("csamt", 0);
                    it.set("itm_det", itm);
                    itms.add(it);
                }
                i.set("itms", itms);
                invArr.add(i);
            }
            ctin.set("inv", invArr);
            b2bArr.add(ctin);
        }
        root.set("b2b", b2bArr);

        // HSN summary (Table 12 of GSTR-1)
        List<Map<String, Object>> hsnAgg = jdbc.queryForList(
            "SELECT sil.hsn_code, COUNT(*) AS line_count, SUM(sil.qty) AS qty, " +
            "  SUM(sil.taxable_amount) AS taxable, SUM(sil.igst_amount) AS igst, " +
            "  SUM(sil.cgst_amount) AS cgst, SUM(sil.sgst_amount) AS sgst, " +
            "  MAX(sil.uom) AS uom, MAX(GREATEST(sil.igst_pct, sil.cgst_pct + sil.sgst_pct)) AS rate " +
            "FROM m6_sales_invoice_line sil " +
            "JOIN m6_sales_invoice si ON si.id = sil.invoice_id " +
            "WHERE DATE_FORMAT(si.invoice_date,'%Y-%m')=? " +
            "GROUP BY sil.hsn_code", period);
        ObjectNode hsn = json.createObjectNode();
        ArrayNode data = json.createArrayNode();
        int hsnIdx = 0;
        for (Map<String, Object> h : hsnAgg) {
            hsnIdx++;
            ObjectNode item = json.createObjectNode();
            item.put("num", hsnIdx);
            item.put("hsn_sc", String.valueOf(h.get("hsn_code")));
            item.put("uqc", String.valueOf(h.get("uom")));
            item.put("qty", nz(h.get("qty")).doubleValue());
            item.put("rt", nz(h.get("rate")).doubleValue());
            item.put("txval", nz(h.get("taxable")).doubleValue());
            item.put("iamt", nz(h.get("igst")).doubleValue());
            item.put("camt", nz(h.get("cgst")).doubleValue());
            item.put("samt", nz(h.get("sgst")).doubleValue());
            item.put("csamt", 0);
            data.add(item);
        }
        hsn.set("data", data);
        root.set("hsn", hsn);

        // Update summary table
        try {
            int b2bCount = b2bInvs.size();
            BigDecimal taxable = BigDecimal.ZERO, igst = BigDecimal.ZERO;
            for (Map<String, Object> h : hsnAgg) {
                taxable = taxable.add(nz(h.get("taxable")));
                igst = igst.add(nz(h.get("igst")));
            }
            jdbc.update(
                "INSERT INTO m6_gstr1_summary (tenant_id, period, b2b_invoice_count, b2b_taxable, b2b_igst, json_status, json_path) " +
                "VALUES (1, ?, ?, ?, ?, 'GENERATED', ?) " +
                "ON DUPLICATE KEY UPDATE b2b_invoice_count=VALUES(b2b_invoice_count), b2b_taxable=VALUES(b2b_taxable), b2b_igst=VALUES(b2b_igst), json_status='GENERATED'",
                period, b2bCount, taxable, igst, "/api/m6/gstr1/json?period=" + period);
        } catch (Exception ignored) {}

        return root;
    }

    private BigDecimal nz(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }
    private String formatDate(Object o) {
        if (o == null) return "";
        String s = o.toString();
        // 2026-06-05 -> 05-06-2026
        if (s.length() >= 10) {
            String[] p = s.substring(0, 10).split("-");
            return p[2] + "-" + p[1] + "-" + p[0];
        }
        return s;
    }
}
