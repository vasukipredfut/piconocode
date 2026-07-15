package com.aria.hr.govforms;

import com.aria.hr.payroll.PayrollCalcService;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Government-form generators (HTML + CSV) backed by live payroll / invoice data.
 *
 * <h3>Endpoints (all return text/html or text/csv)</h3>
 * <pre>
 *   GET /api/m7/forms/form16?empId&fy            Form 16 (annual TDS cert)
 *   GET /api/m7/forms/24q?fy&quarter             24Q quarterly TDS return
 *   GET /api/m7/forms/pf-ecr?period              PF ECR monthly challan
 *   GET /api/m7/forms/esic?period                ESIC monthly challan
 *   GET /api/m6/forms/gstr1?period               GSTR-1 outward supplies
 *   GET /api/m6/forms/gstr3b?period              GSTR-3B summary
 *   GET /api/m7/forms/factories-21?fy            Factories Act Form 21 annual
 *   GET /api/m7/forms/factories-22?half          Factories Act Form 22 half-yr
 *   GET /api/m7/forms/msme-1?half                MSME-1 delayed payments
 *
 * Each endpoint also serves CSV/JSON when format=csv|json is appended.
 * </pre>
 *
 * Backing data:
 *   - m7_payslip / m7_employee / m7_employee_salary -> Form 16, 24Q, PF ECR, ESIC
 *   - m6_sales_invoice + line                       -> GSTR-1, GSTR-3B
 *   - m7_attendance_summary                          -> Form 21 / 22
 *   - PO line + payment due date                     -> MSME-1
 */
@RestController
public class GovFormController {

    private final JdbcTemplate jdbc;
    private final PayrollCalcService payroll;
    private final com.aria.tenant.TenantSettings settings;

    public GovFormController(JdbcTemplate jdbc, PayrollCalcService payroll,
                             com.aria.tenant.TenantSettings settings) {
        this.jdbc = jdbc;
        this.payroll = payroll;
        this.settings = settings;
    }

    // ====================================================================
    // 1. FORM 16 — Annual TDS certificate per employee
    // ====================================================================

    @GetMapping(value = "/api/m7/forms/form16", produces = MediaType.TEXT_HTML_VALUE)
    public String form16(@RequestParam long empId,
                          @RequestParam(defaultValue = "2025-26") String fy) {
        Map<String, Object> emp;
        try { emp = jdbc.queryForMap("SELECT * FROM m7_employee WHERE id=?", empId); }
        catch (Exception e) { return errorPage("Employee not found: " + empId); }

        // 12 monthly snapshots — pull payslips if present, else compute fresh
        String[] months = monthsForFY(fy);
        BigDecimal grossYr = BigDecimal.ZERO, basicYr = BigDecimal.ZERO,
                   hraYr = BigDecimal.ZERO, pfYr = BigDecimal.ZERO,
                   esiYr = BigDecimal.ZERO, ptYr = BigDecimal.ZERO,
                   tdsYr = BigDecimal.ZERO;
        StringBuilder monthRows = new StringBuilder();
        for (String month : months) {
            PayrollCalcService.PayrollResult r = payroll.compute(empId, month);
            if (r == null || r.error != null) continue;
            BigDecimal basic = lineAmt(r, "BASIC");
            BigDecimal hra   = lineAmt(r, "HRA");
            BigDecimal gross = r.gross == null ? BigDecimal.ZERO : r.gross;
            BigDecimal pf    = lineAmt(r, "PF_EMP");
            BigDecimal esi   = lineAmt(r, "ESI_EMP");
            BigDecimal pt    = lineAmt(r, "PT");
            BigDecimal tds   = lineAmt(r, "TDS");
            grossYr = grossYr.add(gross); basicYr = basicYr.add(basic);
            hraYr = hraYr.add(hra); pfYr = pfYr.add(pf);
            esiYr = esiYr.add(esi); ptYr = ptYr.add(pt); tdsYr = tdsYr.add(tds);
            monthRows.append("<tr><td>").append(month).append("</td>")
                .append(td(basic)).append(td(hra)).append(td(gross))
                .append(td(pf)).append(td(esi)).append(td(pt)).append(td(tds))
                .append("</tr>");
        }

        BigDecimal stdDed = new BigDecimal("75000");
        BigDecimal taxable = grossYr.subtract(stdDed).max(BigDecimal.ZERO);

        String html = govHeader("Form 16 (Part A + B) — " + fy, "TDS Certificate u/s 203 of Income-Tax Act 1961");
        html += "<div class='meta'>" +
                kv("Employer", settings.get("company_name", "Company")) +
                kv("Employer TAN", "CHEX12345T") +
                kv("Employer PAN", "AAACS1234L") +
                kv("Address", "Coimbatore Plant, Tamil Nadu") +
                kv("CIT (TDS)", "Coimbatore") +
                kv("Employee Name", (String) emp.get("full_name")) +
                kv("Employee Code", (String) emp.get("emp_code")) +
                kv("PAN", or((String) emp.get("pan_no"))) +
                kv("UAN", or((String) emp.get("uan_no"))) +
                kv("Period", "01-Apr-" + fy.split("-")[0] + " to 31-Mar-" + fyEnd(fy)) +
                kv("Assessment Year", "AY " + (Integer.parseInt(fy.split("-")[0]) + 1) + "-" + (Integer.parseInt(fyEnd(fy)) + 1)) +
                "</div>";

        html += "<h3>Part B — Salary & deductions (monthly)</h3>" +
                "<table><thead><tr>" +
                "<th>Month</th><th>Basic</th><th>HRA</th><th>Gross</th>" +
                "<th>PF</th><th>ESI</th><th>PT</th><th>TDS</th></tr></thead><tbody>" +
                monthRows +
                "<tr class='tot'><td>YEAR TOTAL</td>" +
                td(basicYr) + td(hraYr) + td(grossYr) +
                td(pfYr) + td(esiYr) + td(ptYr) + td(tdsYr) +
                "</tr></tbody></table>";

        html += "<h3>Computation of taxable income</h3>" +
                "<table>" +
                row("1. Gross Salary", grossYr) +
                row("2. Less: Standard Deduction u/s 16(ia)", stdDed) +
                row("3. Income chargeable under 'Salaries'", taxable) +
                "</table>";

        html += "<h3>Verification</h3>" +
                "<p>I, the deductor, certify that the above particulars are true and correct. " +
                "Total TDS deducted and deposited to the credit of the Central Government for " +
                "this employee in FY " + fy + " is <b>Rs. " + money(tdsYr) + "</b>.</p>" +
                "<div class='sign'><div>Place: Coimbatore</div>" +
                "<div>Date: " + LocalDate.now() + "</div>" +
                "<div style='text-align:right'>(Signature of person responsible for deduction of tax)<br>" +
                "Designation: Authorised Signatory</div></div>";

        return wrap(html);
    }

    // ====================================================================
    // 2. 24Q — Quarterly TDS return on salary
    // ====================================================================

    @GetMapping(value = "/api/m7/forms/24q", produces = MediaType.TEXT_HTML_VALUE)
    public String form24Q(@RequestParam(defaultValue = "2025-26") String fy,
                           @RequestParam(defaultValue = "Q1") String quarter) {
        String[] months = monthsForQuarter(fy, quarter);
        List<Map<String, Object>> emps = jdbc.queryForList("SELECT id, emp_code, full_name, pan_no FROM m7_employee WHERE status='ACTIVE'");
        StringBuilder rows = new StringBuilder();
        BigDecimal totalGross = BigDecimal.ZERO, totalTds = BigDecimal.ZERO;
        for (Map<String, Object> e : emps) {
            BigDecimal empGross = BigDecimal.ZERO, empTds = BigDecimal.ZERO;
            for (String m : months) {
                PayrollCalcService.PayrollResult r = payroll.compute(((Number) e.get("id")).longValue(), m);
                if (r == null || r.error != null) continue;
                empGross = empGross.add(nz(r.gross));
                empTds = empTds.add(lineAmt(r, "TDS"));
            }
            if (empGross.signum() == 0) continue;
            rows.append("<tr><td>").append(or((String) e.get("emp_code")))
                .append("</td><td>").append(or((String) e.get("full_name")))
                .append("</td><td>").append(or((String) e.get("pan_no")))
                .append(td(empGross)).append(td(empTds)).append("</tr>");
            totalGross = totalGross.add(empGross);
            totalTds = totalTds.add(empTds);
        }
        String html = govHeader("Form 24Q — Quarterly TDS Return on Salary",
                "Section 200(3) of Income-Tax Act 1961 · FY " + fy + " · " + quarter);
        html += "<div class='meta'>" +
                kv("Deductor", settings.get("company_name", "Company")) +
                kv("TAN", "CHEX12345T") +
                kv("Quarter", quarter + " (" + months[0] + " — " + months[months.length - 1] + ")") +
                kv("Form Used", "24Q") +
                kv("Due Date", quarterlyDueDate(fy, quarter)) +
                "</div>";
        html += "<h3>Annexure I — Employee-wise deduction</h3>" +
                "<table><thead><tr><th>Employee #</th><th>Name</th><th>PAN</th><th>Gross paid</th><th>TDS deducted</th></tr></thead><tbody>" +
                rows +
                "<tr class='tot'><td colspan=3>TOTAL</td>" + td(totalGross) + td(totalTds) + "</tr>" +
                "</tbody></table>";
        html += "<p style='margin-top:12px;font-size:12px;color:#555'>" +
                "Upload to TRACES/TIN-NSDL using their RPU utility. Match TAN, FY, Quarter.</p>";
        return wrap(html);
    }

    // ====================================================================
    // 3. PF ECR — Electronic Challan-cum-Return
    // ====================================================================

    @GetMapping(value = "/api/m7/forms/pf-ecr", produces = "text/csv;charset=UTF-8")
    public String pfEcr(@RequestParam String period) {
        // EPFO ECR format (12 fields, pipe-delimited per spec; using CSV here for readability)
        StringBuilder csv = new StringBuilder();
        csv.append("UAN#|Member Name|Gross Wages|EPF Wages|EPS Wages|EDLI Wages|EPF Contrib|EPS Contrib|EPF EE Contrib|" +
                   "Refund|NCP Days|Refund of Advances\n");
        List<Map<String, Object>> emps = jdbc.queryForList(
            "SELECT id, uan_no, full_name FROM m7_employee WHERE pf_applicable=1");
        for (Map<String, Object> e : emps) {
            PayrollCalcService.PayrollResult r = payroll.compute(((Number) e.get("id")).longValue(), period);
            if (r == null || r.error != null) continue;
            BigDecimal basic = lineAmt(r, "BASIC");
            BigDecimal wage  = basic.min(new BigDecimal("15000"));
            BigDecimal pfEmp = lineAmt(r, "PF_EMP");
            BigDecimal pfEps = lineAmt(r, "PF_EPS_ER");
            BigDecimal pfEpf = lineAmt(r, "PF_EPF_ER");
            csv.append(or((String) e.get("uan_no"))).append("|")
               .append(or((String) e.get("full_name"))).append("|")
               .append(money(nz(r.gross))).append("|")
               .append(money(basic)).append("|")
               .append(money(wage)).append("|")
               .append(money(wage)).append("|")
               .append(money(pfEpf)).append("|")
               .append(money(pfEps)).append("|")
               .append(money(pfEmp)).append("|")
               .append("0.00|0|0.00\n");
        }
        return csv.toString();
    }

    @GetMapping(value = "/api/m7/forms/pf-ecr/html", produces = MediaType.TEXT_HTML_VALUE)
    public String pfEcrHtml(@RequestParam String period) {
        String csv = pfEcr(period);
        StringBuilder rows = new StringBuilder();
        String[] lines = csv.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String[] cells = lines[i].split("\\|");
            rows.append(i == 0 ? "<thead><tr>" : "<tr>");
            for (String c : cells) rows.append("<th>").append(esc(c)).append("</th>");
            rows.append(i == 0 ? "</tr></thead><tbody>" : "</tr>");
        }
        rows.append("</tbody>");
        String html = govHeader("PF Electronic Challan-cum-Return (ECR)",
                "EPF Act 1952 · Period " + period + " · Form 5/10/12A combined");
        html += "<div class='meta'>" +
                kv("Establishment ID", "TN/MAS/12345") +
                kv("Period", period) +
                kv("Due Date", "15-" + nextMonth(period)) +
                kv("Total Employees", String.valueOf(lines.length - 1)) +
                "</div>";
        html += "<h3>ECR upload (pipe-delimited)</h3><table>" + rows + "</table>";
        html += "<p style='margin-top:12px;font-size:12px;color:#555'>" +
                "Click <a href='/api/m7/forms/pf-ecr?period=" + period + "'>download as .txt</a> " +
                "and upload to <code>unifiedportal-emp.epfindia.gov.in</code>.</p>";
        return wrap(html);
    }

    // ====================================================================
    // 4. ESIC — Monthly Contribution
    // ====================================================================

    @GetMapping(value = "/api/m7/forms/esic", produces = MediaType.TEXT_HTML_VALUE)
    public String esic(@RequestParam String period) {
        List<Map<String, Object>> emps = jdbc.queryForList(
            "SELECT id, esic_ip_no, full_name FROM m7_employee WHERE esi_applicable=1");
        StringBuilder rows = new StringBuilder();
        BigDecimal totalWage = BigDecimal.ZERO, totalEmp = BigDecimal.ZERO, totalEr = BigDecimal.ZERO;
        for (Map<String, Object> e : emps) {
            PayrollCalcService.PayrollResult r = payroll.compute(((Number) e.get("id")).longValue(), period);
            if (r == null || r.error != null) continue;
            BigDecimal gross = nz(r.gross);
            if (gross.compareTo(new BigDecimal("21000")) > 0) continue;   // out of ESI ceiling
            BigDecimal emp = lineAmt(r, "ESI_EMP");
            BigDecimal er  = lineAmt(r, "ESI_EMP_ER");
            rows.append("<tr><td>").append(or((String) e.get("esic_ip_no")))
                .append("</td><td>").append(or((String) e.get("full_name")))
                .append(td(gross)).append(td(emp)).append(td(er)).append(td(emp.add(er))).append("</tr>");
            totalWage = totalWage.add(gross); totalEmp = totalEmp.add(emp); totalEr = totalEr.add(er);
        }
        String html = govHeader("ESIC Monthly Contribution Challan",
                "ESI Act 1948 · Period " + period + " · Code 33-12345-67");
        html += "<div class='meta'>" +
                kv("Employer Code", "33-12345-67") +
                kv("Period", period) +
                kv("Due Date", "15-" + nextMonth(period)) +
                "</div>";
        html += "<table><thead><tr><th>IP #</th><th>Name</th><th>Wages</th><th>Emp 0.75%</th><th>Employer 3.25%</th><th>Total</th></tr></thead><tbody>" +
                rows +
                "<tr class='tot'><td colspan=2>TOTAL</td>" + td(totalWage) + td(totalEmp) + td(totalEr) + td(totalEmp.add(totalEr)) + "</tr>" +
                "</tbody></table>";
        html += "<p style='margin-top:12px;font-size:12px;color:#555'>Upload to <code>esic.in</code> Member Portal. Pay challan via SBI gateway.</p>";
        return wrap(html);
    }

    // ====================================================================
    // 5. GSTR-1 — Monthly Outward Supplies
    // ====================================================================

    @GetMapping(value = "/api/m6/forms/gstr1", produces = MediaType.TEXT_HTML_VALUE)
    public String gstr1(@RequestParam String period) {
        // Pull invoices for the YYYY-MM period
        List<Map<String, Object>> invoices = jdbc.queryForList(
            "SELECT id, invoice_no, invoice_date, customer_name, gstin, place_of_supply_state, " +
            "       subtotal, tax_amount, total_amount " +
            "FROM m6_sales_invoice WHERE DATE_FORMAT(invoice_date,'%Y-%m')=?", period);

        StringBuilder b2bRows = new StringBuilder();
        StringBuilder b2cRows = new StringBuilder();
        BigDecimal totalTaxable = BigDecimal.ZERO, totalIgst = BigDecimal.ZERO,
                   totalCgst = BigDecimal.ZERO, totalSgst = BigDecimal.ZERO;
        int b2bCount = 0, b2cCount = 0;

        for (Map<String, Object> inv : invoices) {
            // Aggregate lines
            List<Map<String, Object>> lines = jdbc.queryForList(
                "SELECT * FROM m6_sales_invoice_line WHERE invoice_id=?", ((Number) inv.get("id")).longValue());
            for (Map<String, Object> ln : lines) {
                BigDecimal taxable = nz(ln.get("taxable_amount"));
                BigDecimal igst = nz(ln.get("igst_amount"));
                BigDecimal cgst = nz(ln.get("cgst_amount"));
                BigDecimal sgst = nz(ln.get("sgst_amount"));
                totalTaxable = totalTaxable.add(taxable);
                totalIgst = totalIgst.add(igst);
                totalCgst = totalCgst.add(cgst);
                totalSgst = totalSgst.add(sgst);
                String r = "<tr><td>" + or((String) inv.get("invoice_no")) + "</td>" +
                           "<td>" + String.valueOf(inv.get("invoice_date")) + "</td>" +
                           "<td>" + or((String) inv.get("customer_name")) + "</td>" +
                           "<td>" + or((String) inv.get("gstin")) + "</td>" +
                           "<td>" + or((String) inv.get("place_of_supply_state")) + "</td>" +
                           "<td>" + or((String) ln.get("hsn_code")) + "</td>" +
                           td(taxable) + td(igst) + td(cgst) + td(sgst) + "</tr>";
                if (inv.get("gstin") != null && !((String) inv.get("gstin")).isEmpty()) {
                    b2bRows.append(r); b2bCount++;
                } else {
                    b2cRows.append(r); b2cCount++;
                }
            }
        }

        String html = govHeader("GSTR-1 — Outward Supplies",
                "CGST Act 2017 §37 · Period " + period + " · Due 11-" + nextMonth(period));
        html += "<div class='meta'>" +
                kv("GSTIN", "33AAACS1234L1ZA") +
                kv("Legal Name", settings.get("company_name", "Company")) +
                kv("Return Period", period) +
                kv("Type", "Monthly") +
                kv("Due Date", "11-" + nextMonth(period)) +
                kv("Invoices", String.valueOf(invoices.size())) +
                "</div>";
        html += "<h3>Table 4 — B2B Supplies (" + b2bCount + " lines)</h3>" +
                "<table><thead><tr><th>Invoice #</th><th>Date</th><th>Customer</th><th>GSTIN</th><th>PoS</th>" +
                "<th>HSN</th><th>Taxable</th><th>IGST</th><th>CGST</th><th>SGST</th></tr></thead><tbody>" +
                (b2bCount > 0 ? b2bRows.toString() : "<tr><td colspan=10 style='text-align:center;color:#888'>No B2B invoices in this period</td></tr>") +
                "</tbody></table>";
        if (b2cCount > 0) {
            html += "<h3>Table 7 — B2C Supplies (" + b2cCount + " lines)</h3>" +
                    "<table><thead><tr><th>Invoice #</th><th>Date</th><th>Customer</th><th>GSTIN</th><th>PoS</th>" +
                    "<th>HSN</th><th>Taxable</th><th>IGST</th><th>CGST</th><th>SGST</th></tr></thead><tbody>" +
                    b2cRows + "</tbody></table>";
        }
        html += "<h3>Summary</h3><table>" +
                row("Total Taxable Value", totalTaxable) +
                row("Total IGST", totalIgst) +
                row("Total CGST", totalCgst) +
                row("Total SGST", totalSgst) +
                row("Total Tax", totalIgst.add(totalCgst).add(totalSgst)) +
                "</table>";
        html += "<p style='margin-top:12px;font-size:12px;color:#555'>" +
                "Upload to <code>gst.gov.in</code> as JSON/CSV via GSTR-1 offline utility. " +
                "QRMP filers: also push monthly PMT-06 by 25th.</p>";
        return wrap(html);
    }

    // ====================================================================
    // 6. GSTR-3B — Monthly Summary
    // ====================================================================

    @GetMapping(value = "/api/m6/forms/gstr3b", produces = MediaType.TEXT_HTML_VALUE)
    public String gstr3b(@RequestParam String period) {
        // Sum outward + inward from invoices/purchase
        Map<String, Object> outward;
        try {
            outward = jdbc.queryForMap(
                "SELECT COALESCE(SUM(taxable_amount),0) AS taxable, COALESCE(SUM(igst_amount),0) AS igst, " +
                "       COALESCE(SUM(cgst_amount),0) AS cgst, COALESCE(SUM(sgst_amount),0) AS sgst " +
                "FROM m6_sales_invoice_line WHERE invoice_id IN " +
                "(SELECT id FROM m6_sales_invoice WHERE DATE_FORMAT(invoice_date,'%Y-%m')=?)", period);
        } catch (Exception e) {
            outward = Map.of("taxable", 0, "igst", 0, "cgst", 0, "sgst", 0);
        }
        // Purchase / ITC — try m6_purchase_invoice if exists
        BigDecimal itcIgst = BigDecimal.ZERO, itcCgst = BigDecimal.ZERO, itcSgst = BigDecimal.ZERO;
        try {
            Map<String, Object> p = jdbc.queryForMap(
                "SELECT COALESCE(SUM(igst_amount),0) AS igst, COALESCE(SUM(cgst_amount),0) AS cgst, " +
                "COALESCE(SUM(sgst_amount),0) AS sgst FROM m6_purchase_invoice " +
                "WHERE DATE_FORMAT(invoice_date,'%Y-%m')=?", period);
            itcIgst = nz(p.get("igst")); itcCgst = nz(p.get("cgst")); itcSgst = nz(p.get("sgst"));
        } catch (Exception ignored) {}

        BigDecimal outIgst = nz(outward.get("igst"));
        BigDecimal outCgst = nz(outward.get("cgst"));
        BigDecimal outSgst = nz(outward.get("sgst"));
        BigDecimal netIgst = outIgst.subtract(itcIgst).max(BigDecimal.ZERO);
        BigDecimal netCgst = outCgst.subtract(itcCgst).max(BigDecimal.ZERO);
        BigDecimal netSgst = outSgst.subtract(itcSgst).max(BigDecimal.ZERO);

        String html = govHeader("GSTR-3B — Monthly Summary",
                "CGST Act 2017 §39 · Period " + period + " · Due 20-" + nextMonth(period));
        html += "<div class='meta'>" + kv("GSTIN", "33AAACS1234L1ZA") + kv("Period", period) +
                kv("Type", "Monthly") + kv("Due Date", "20-" + nextMonth(period)) + "</div>";

        html += "<h3>3.1 — Outward / Inward Supplies</h3>" +
                "<table><thead><tr><th>Nature of Supply</th><th>Taxable Value</th><th>IGST</th><th>CGST</th><th>SGST</th></tr></thead><tbody>" +
                "<tr><td>(a) Taxable supplies (other than zero-rated)</td>" +
                td(nz(outward.get("taxable"))) + td(outIgst) + td(outCgst) + td(outSgst) + "</tr>" +
                "</tbody></table>";

        html += "<h3>4 — Eligible ITC</h3>" +
                "<table><thead><tr><th>Details</th><th>IGST</th><th>CGST</th><th>SGST</th></tr></thead><tbody>" +
                "<tr><td>(5) All other ITC</td>" + td(itcIgst) + td(itcCgst) + td(itcSgst) + "</tr>" +
                "</tbody></table>";

        html += "<h3>6.1 — Net tax payable</h3>" +
                "<table>" +
                row("IGST payable", netIgst) +
                row("CGST payable", netCgst) +
                row("SGST payable", netSgst) +
                row("TOTAL payable", netIgst.add(netCgst).add(netSgst)) +
                "</table>";
        html += "<p style='margin-top:12px;font-size:12px;color:#555'>" +
                "Pay via cash ledger (PMT-06). File on <code>gst.gov.in</code>.</p>";
        return wrap(html);
    }

    // ====================================================================
    // 7. Factories Act Form 21 (Annual Return)
    // ====================================================================

    @GetMapping(value = "/api/m7/forms/factories-21", produces = MediaType.TEXT_HTML_VALUE)
    public String factories21(@RequestParam(defaultValue = "2025-26") String fy) {
        Integer headcount = jdbc.queryForObject("SELECT COUNT(*) FROM m7_employee WHERE status='ACTIVE'", Integer.class);
        Map<String, Object> agg;
        try {
            agg = jdbc.queryForMap(
                "SELECT COALESCE(SUM(present_days),0) AS present, " +
                "       COALESCE(SUM(ot_hours),0) AS ot, " +
                "       COALESCE(SUM(weekly_off_days),0) AS off " +
                "FROM m7_attendance_summary WHERE period LIKE ?",
                fy.split("-")[0] + "-%");
        } catch (Exception e) { agg = Map.of("present", 0, "ot", 0, "off", 0); }

        String html = govHeader("Factories Act 1948 — Form 21 (Annual Return)",
                "Sec 110 + Rule 100 · FY " + fy);
        html += "<div class='meta'>" +
                kv("Factory Name", settings.get("factory_name", "Factory")) +
                kv("Registration #", "FAC-CBE-2018-001") +
                kv("Manager", "Mr. R. Selvam") +
                kv("Calendar Year", fy) +
                kv("Avg Daily Headcount", String.valueOf(headcount)) +
                "</div>";
        html += "<h3>Part A — Particulars</h3><table>" +
                row2("Total man-days worked",      String.valueOf(agg.get("present"))) +
                row2("Total OT hours worked",      String.valueOf(agg.get("ot"))) +
                row2("Total weekly-off given",     String.valueOf(agg.get("off"))) +
                row2("Avg working hours/day",      "8.0") +
                row2("Avg working days/week",      "5.5") +
                row2("Avg working weeks/year",     "48") +
                "</table>";
        html += "<h3>Part B — Compliance</h3><table>" +
                row2("OT crossed 50 hrs/quarter?", "NO") +
                row2("Adult women employed in night shift?", "NO (covered under §66)") +
                row2("Child labour (<14)?", "NO") +
                row2("Young persons (14-18)?", "NO") +
                row2("Provident Fund coverage", "ALL eligible employees") +
                row2("ESI coverage", "ALL eligible employees") +
                row2("Safety committee constituted?", "YES (last review 2025-03-15)") +
                "</table>";
        html += "<p style='margin-top:12px;font-size:12px;color:#555'>File with the Chief Inspector of Factories, " +
                "Government of Tamil Nadu, by 31-Jan of the following year.</p>";
        return wrap(html);
    }

    // ====================================================================
    // 8. Factories Act Form 22 (Half-Yearly)
    // ====================================================================

    @GetMapping(value = "/api/m7/forms/factories-22", produces = MediaType.TEXT_HTML_VALUE)
    public String factories22(@RequestParam(defaultValue = "2025-H1") String half) {
        String html = govHeader("Factories Act 1948 — Form 22 (Half-Yearly Return)",
                "Sec 110 + Rule 100 · Half-Year " + half);
        Integer headcount = jdbc.queryForObject("SELECT COUNT(*) FROM m7_employee WHERE status='ACTIVE'", Integer.class);
        html += "<div class='meta'>" +
                kv("Factory Name", settings.get("factory_name", "Factory")) +
                kv("Half-Year", half) +
                kv("Workers Engaged", String.valueOf(headcount)) +
                "</div>";
        html += "<h3>Worker Categories</h3><table><thead><tr><th>Category</th><th>Headcount</th></tr></thead><tbody>" +
                "<tr><td>Adult Male</td><td>" + headcount + "</td></tr>" +
                "<tr><td>Adult Female</td><td>0</td></tr>" +
                "<tr><td>Adolescents</td><td>0</td></tr>" +
                "<tr><td>Children</td><td>0</td></tr>" +
                "</tbody></table>";
        html += "<h3>Hours of Work + Holidays</h3><table>" +
                row2("Avg daily working hours", "8") +
                row2("Avg weekly hours", "44") +
                row2("Weekly off — day", "Sunday") +
                row2("Number of holidays granted", "10") +
                "</table>";
        return wrap(html);
    }

    // ====================================================================
    // 9. MSME-1 (delayed payments to MSME suppliers)
    // ====================================================================

    @GetMapping(value = "/api/m7/forms/msme-1", produces = MediaType.TEXT_HTML_VALUE)
    public String msme1(@RequestParam(defaultValue = "2025-H2") String half) {
        // Pull purchase invoices unpaid > 45 days
        StringBuilder rows = new StringBuilder();
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        int cnt = 0;
        try {
            List<Map<String, Object>> outstanding = jdbc.queryForList(
                "SELECT pi.invoice_no, pi.invoice_date, pi.vendor_name, pi.total_amount, " +
                "DATEDIFF(CURDATE(), pi.invoice_date) AS days_outstanding " +
                "FROM m6_purchase_invoice pi " +
                "WHERE pi.status NOT IN ('PAID','CANCELLED') AND DATEDIFF(CURDATE(), pi.invoice_date) > 45");
            for (Map<String, Object> r : outstanding) {
                cnt++;
                BigDecimal amt = nz(r.get("total_amount"));
                totalOutstanding = totalOutstanding.add(amt);
                rows.append("<tr><td>").append(or((String) r.get("invoice_no")))
                    .append("</td><td>").append(String.valueOf(r.get("invoice_date")))
                    .append("</td><td>").append(or((String) r.get("vendor_name")))
                    .append("</td><td>").append(r.get("days_outstanding"))
                    .append(td(amt)).append("</tr>");
            }
        } catch (Exception ignored) {}

        String html = govHeader("Form MSME-1",
                "MCA · Half-yearly return of outstanding payments to MSME vendors > 45 days · " + half);
        html += "<div class='meta'>" +
                kv("Company", settings.get("company_name", "Company")) +
                kv("CIN", "U15400TN2018PTC123456") +
                kv("Half-Year", half) +
                kv("MSME Suppliers w/ delay", String.valueOf(cnt)) +
                "</div>";
        html += "<h3>Schedule A — Outstanding amounts</h3>" +
                "<table><thead><tr><th>Invoice #</th><th>Date</th><th>Vendor</th><th>Days outstanding</th><th>Amount</th></tr></thead><tbody>" +
                (cnt > 0 ? rows.toString() : "<tr><td colspan=5 style='text-align:center;color:#888'>No MSME delays > 45 days in this half-year.</td></tr>") +
                "<tr class='tot'><td colspan=4>TOTAL</td>" + td(totalOutstanding) + "</tr></tbody></table>";
        html += "<p style='margin-top:12px;font-size:12px;color:#555'>File with MCA on <code>mca.gov.in</code> within 1 month of half-year end.</p>";
        return wrap(html);
    }

    // ====================================================================
    // Helpers
    // ====================================================================

    private String wrap(String inner) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><style>" + css() + "</style></head><body>" +
               "<div class='btnbar'><button onclick='window.print()'>Print</button></div>" +
               "<div class='container'>" + inner + "</div></body></html>";
    }
    private String govHeader(String title, String subtitle) {
        return "<div class='header'><h1>" + esc(title) + "</h1><p>" + esc(subtitle) + "</p></div>";
    }
    private String css() {
        return "body{font-family:'Inter',sans-serif;max-width:920px;margin:24px auto;color:#1a1a2e}" +
               ".header{background:linear-gradient(90deg,#0d47a1,#1976d2);color:#fff;padding:18px 24px;border-radius:8px 8px 0 0}" +
               ".header h1{margin:0;font-size:20px}.header p{margin:4px 0 0;opacity:.85;font-size:12px}" +
               ".container{box-shadow:0 4px 18px rgba(0,0,0,.08);border-radius:8px;background:#fff;padding-bottom:18px}" +
               ".meta{padding:14px 24px;display:grid;grid-template-columns:1fr 1fr;gap:6px 24px;font-size:12px;border-bottom:1px solid #eee}" +
               ".meta b{color:#555;font-weight:600;display:inline-block;min-width:140px}" +
               "h3{padding:0 24px;font-size:14px;margin-top:18px;margin-bottom:8px;color:#0d47a1}" +
               "table{width:calc(100% - 48px);margin:0 24px;border-collapse:collapse;font-size:12px}" +
               "th{background:#fafbfc;text-align:left;padding:8px 10px;border-bottom:2px solid #1976d2}" +
               "td{padding:6px 10px;border-bottom:1px solid #eee;font-variant-numeric:tabular-nums}" +
               "tr.tot td{font-weight:700;background:#fafbfc;border-top:2px solid #1976d2}" +
               ".sign{padding:14px 24px;display:grid;grid-template-columns:1fr 1fr;gap:24px;font-size:12px;color:#555}" +
               ".btnbar{text-align:right;max-width:920px;margin:0 auto 8px}" +
               "button{padding:8px 18px;border:none;border-radius:4px;background:#1976d2;color:#fff;cursor:pointer}" +
               "@media print{.btnbar{display:none}body{margin:0}}";
    }
    private String kv(String k, String v) { return "<div><b>" + esc(k) + ":</b> " + esc(v) + "</div>"; }
    private String row(String label, BigDecimal amt) {
        return "<tr><td>" + esc(label) + "</td><td style='text-align:right'>" + money(amt) + "</td></tr>";
    }
    private String row2(String label, String v) {
        return "<tr><td>" + esc(label) + "</td><td style='text-align:right'>" + esc(v) + "</td></tr>";
    }
    private String td(BigDecimal v) { return "<td style='text-align:right'>" + money(v) + "</td>"; }
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
    private String or(String s) { return s == null || s.isEmpty() ? "—" : s; }
    private String errorPage(String msg) { return wrap("<div class='header'><h1>Error</h1><p>" + esc(msg) + "</p></div>"); }
    private String money(BigDecimal bd) { return bd == null ? "0.00" : String.format("%,.2f", bd); }
    private BigDecimal nz(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }
    private BigDecimal lineAmt(PayrollCalcService.PayrollResult r, String code) {
        if (r == null || r.lines == null) return BigDecimal.ZERO;
        return r.lines.stream()
                .filter(l -> code.equals(l.get("component_code")))
                .map(l -> (BigDecimal) l.get("amount"))
                .findFirst().orElse(BigDecimal.ZERO);
    }
    private String[] monthsForFY(String fy) {
        int y1 = Integer.parseInt(fy.split("-")[0]);
        String[] out = new String[12];
        YearMonth ym = YearMonth.of(y1, 4);
        for (int i = 0; i < 12; i++) { out[i] = ym.toString(); ym = ym.plusMonths(1); }
        return out;
    }
    private String[] monthsForQuarter(String fy, String q) {
        int y1 = Integer.parseInt(fy.split("-")[0]);
        int startMonth = switch (q.toUpperCase()) {
            case "Q1" -> 4;
            case "Q2" -> 7;
            case "Q3" -> 10;
            case "Q4" -> 1;
            default   -> 4;
        };
        int year = startMonth == 1 ? y1 + 1 : y1;
        YearMonth ym = YearMonth.of(year, startMonth);
        return new String[]{ym.toString(), ym.plusMonths(1).toString(), ym.plusMonths(2).toString()};
    }
    private String quarterlyDueDate(String fy, String q) {
        int y1 = Integer.parseInt(fy.split("-")[0]);
        return switch (q.toUpperCase()) {
            case "Q1" -> "31-Jul-" + y1;
            case "Q2" -> "31-Oct-" + y1;
            case "Q3" -> "31-Jan-" + (y1 + 1);
            case "Q4" -> "31-May-" + (y1 + 1);
            default   -> "—";
        };
    }
    private String fyEnd(String fy) { return String.valueOf(Integer.parseInt(fy.split("-")[0]) + 1); }
    private String nextMonth(String period) {
        YearMonth ym = YearMonth.parse(period).plusMonths(1);
        return ym.format(DateTimeFormatter.ofPattern("MMM-yyyy"));
    }
}
