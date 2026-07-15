package com.aria.finance.invoice;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * GST / HSN computation helpers for the invoice flow.
 *
 *   GET  /api/m6/gst/hsn/{hsn}                    look up CGST/SGST/IGST/CESS for an HSN
 *   POST /api/m6/gst/compute-invoice              compute line-level + invoice-level GST
 *
 * Logic: intra-state (supplier_state == place_of_supply) -> CGST + SGST split.
 *        inter-state                                       -> IGST only.
 */
@RestController
@RequestMapping("/api/m6/gst")
public class GstCalcController {

    private final JdbcTemplate jdbc;

    public GstCalcController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/hsn/{hsn}")
    public Map<String, Object> lookupHsn(@PathVariable String hsn) {
        try {
            return jdbc.queryForMap(
                "SELECT hsn_code, description, chapter, cgst_pct, sgst_pct, igst_pct, cess_pct " +
                "FROM m5_hsn_master WHERE hsn_code=? ORDER BY effective_from DESC LIMIT 1", hsn);
        } catch (Exception e) {
            return Map.of("error", "HSN not found: " + hsn);
        }
    }

    /**
     * Body shape:
     *   {
     *     supplier_state: "33",         // TN
     *     place_of_supply: "29",        // KA => inter-state IGST
     *     lines: [
     *       {item_code, hsn_code, qty, rate, discount_pct}
     *     ]
     *   }
     */
    @PostMapping("/compute-invoice")
    public Map<String, Object> computeInvoice(@RequestBody Map<String, Object> body) {
        String supState = String.valueOf(body.getOrDefault("supplier_state", ""));
        String posState = String.valueOf(body.getOrDefault("place_of_supply", ""));
        boolean interState = !supState.isEmpty() && !posState.isEmpty() && !supState.equals(posState);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) body.getOrDefault("lines", Collections.emptyList());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO, totalSgst = BigDecimal.ZERO,
                   totalIgst = BigDecimal.ZERO, totalCess = BigDecimal.ZERO;

        List<Map<String, Object>> out = new ArrayList<>();
        int lineNo = 0;
        for (Map<String, Object> line : lines) {
            lineNo++;
            String hsn = String.valueOf(line.getOrDefault("hsn_code", ""));
            BigDecimal qty = bd(line.get("qty"));
            BigDecimal rate = bd(line.get("rate"));
            BigDecimal discPct = bd(line.getOrDefault("discount_pct", "0"));
            BigDecimal amount = qty.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discAmt = amount.multiply(discPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal taxable = amount.subtract(discAmt);

            // Lookup HSN rates
            Map<String, Object> hsnRow = null;
            try {
                hsnRow = jdbc.queryForMap(
                    "SELECT cgst_pct, sgst_pct, igst_pct, cess_pct FROM m5_hsn_master " +
                    "WHERE hsn_code=? ORDER BY effective_from DESC LIMIT 1", hsn);
            } catch (Exception ignored) {}

            BigDecimal cgstPct = hsnRow == null ? BigDecimal.ZERO : bd(hsnRow.get("cgst_pct"));
            BigDecimal sgstPct = hsnRow == null ? BigDecimal.ZERO : bd(hsnRow.get("sgst_pct"));
            BigDecimal igstPct = hsnRow == null ? BigDecimal.ZERO : bd(hsnRow.get("igst_pct"));
            BigDecimal cessPct = hsnRow == null ? BigDecimal.ZERO : bd(hsnRow.get("cess_pct"));

            BigDecimal cgst = BigDecimal.ZERO, sgst = BigDecimal.ZERO, igst = BigDecimal.ZERO;
            if (interState) {
                igst = taxable.multiply(igstPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            } else {
                cgst = taxable.multiply(cgstPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                sgst = taxable.multiply(sgstPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
            BigDecimal cess = taxable.multiply(cessPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = taxable.add(cgst).add(sgst).add(igst).add(cess);

            Map<String, Object> lr = new LinkedHashMap<>();
            lr.put("line_no", lineNo);
            lr.put("item_code", line.get("item_code"));
            lr.put("hsn_code", hsn);
            lr.put("qty", qty);
            lr.put("rate", rate);
            lr.put("amount", amount);
            lr.put("discount_amount", discAmt);
            lr.put("taxable_amount", taxable);
            lr.put("cgst_pct", cgstPct); lr.put("cgst_amount", cgst);
            lr.put("sgst_pct", sgstPct); lr.put("sgst_amount", sgst);
            lr.put("igst_pct", igstPct); lr.put("igst_amount", igst);
            lr.put("cess_pct", cessPct); lr.put("cess_amount", cess);
            lr.put("total_amount", lineTotal);
            out.add(lr);

            subtotal = subtotal.add(taxable);
            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            totalIgst = totalIgst.add(igst);
            totalCess = totalCess.add(cess);
        }

        BigDecimal totalTax = totalCgst.add(totalSgst).add(totalIgst).add(totalCess);
        BigDecimal grandTotal = subtotal.add(totalTax);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("interState", interState);
        r.put("supplier_state", supState);
        r.put("place_of_supply", posState);
        r.put("subtotal", subtotal);
        r.put("totalCgst", totalCgst);
        r.put("totalSgst", totalSgst);
        r.put("totalIgst", totalIgst);
        r.put("totalCess", totalCess);
        r.put("totalTax", totalTax);
        r.put("grandTotal", grandTotal);
        r.put("lines", out);
        return r;
    }

    private BigDecimal bd(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }
}
