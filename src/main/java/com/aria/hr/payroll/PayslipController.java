package com.aria.hr.payroll;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Generate + serve payslip HTML for an employee/period.
 *
 *  GET  /api/m7/payslip/html?empId=N&period=YYYY-MM
 *      Computes payroll via PayrollCalcService and returns a printable
 *      HTML payslip. Used by the frontend to preview / print / email.
 *
 *  POST /api/m7/payslip/generate?empId=N&period=YYYY-MM
 *      Upserts an m7_payslip row marked GENERATED with the HTML snapshot.
 */
@RestController
@RequestMapping("/api/m7/payslip")
public class PayslipController {

    private final PayrollCalcService calc;
    private final JdbcTemplate jdbc;
    private final com.aria.tenant.TenantSettings settings;

    public PayslipController(PayrollCalcService calc, JdbcTemplate jdbc,
                             com.aria.tenant.TenantSettings settings) {
        this.calc = calc;
        this.jdbc = jdbc;
        this.settings = settings;
    }

    @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
    public String html(@RequestParam long empId, @RequestParam String period) {
        return render(empId, period, true);
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestParam long empId, @RequestParam String period) {
        String html = render(empId, period, false);
        PayrollCalcService.PayrollResult r = calc.compute(empId, period);
        try {
            jdbc.update(
                "INSERT INTO m7_payslip (tenant_id, emp_id, payroll_period, generated_at, " +
                "status, gross, total_deduct, net_pay, html_snapshot) " +
                "VALUES (1, ?, ?, NOW(), 'GENERATED', ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE generated_at=NOW(), status='GENERATED', " +
                "gross=VALUES(gross), total_deduct=VALUES(total_deduct), net_pay=VALUES(net_pay), html_snapshot=VALUES(html_snapshot)",
                empId, period, r.gross, r.totalDeduct, r.netPay, html);
            return Map.of("ok", true, "empId", empId, "period", period,
                          "netPay", r.netPay == null ? "0" : r.netPay.toPlainString());
        } catch (Exception e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }

    // ====================================================================
    // HTML rendering
    // ====================================================================

    private String render(long empId, String period, boolean preview) {
        PayrollCalcService.PayrollResult r = calc.compute(empId, period);
        if (r.error != null && !r.error.isEmpty()) {
            return "<h1>Payslip error</h1><p>" + esc(r.error) + "</p>";
        }
        Map<String, Object> emp;
        try { emp = jdbc.queryForMap("SELECT * FROM m7_employee WHERE id=?", empId); }
        catch (Exception ex) { return "<h1>Employee not found</h1>"; }

        StringBuilder earnings = new StringBuilder();
        StringBuilder deductions = new StringBuilder();
        BigDecimal totalE = BigDecimal.ZERO, totalD = BigDecimal.ZERO;
        for (Map<String, Object> line : r.lines) {
            String comp = (String) line.get("component_code");
            String type = (String) line.get("component_type");
            BigDecimal amt = (BigDecimal) line.get("amount");
            if ("EARNING".equals(type)) {
                earnings.append("<tr><td>").append(esc(comp)).append("</td><td class='r'>")
                        .append(formatCurrency(amt)).append("</td></tr>");
                totalE = totalE.add(amt);
            } else if ("DEDUCTION".equals(type)) {
                deductions.append("<tr><td>").append(esc(comp)).append("</td><td class='r'>")
                          .append(formatCurrency(amt)).append("</td></tr>");
                totalD = totalD.add(amt);
            }
        }

        String css =
            "body { font-family: 'Inter', sans-serif; max-width: 720px; margin: 24px auto; color: #1a1a2e; }" +
            ".header { background: linear-gradient(90deg, #0d47a1 0%, #1976d2 100%); color: #fff; " +
            "          padding: 20px 24px; border-radius: 8px 8px 0 0; }" +
            ".header h1 { margin: 0; font-size: 22px; }" +
            ".header p { margin: 4px 0 0; opacity: .9; font-size: 13px; }" +
            ".section { padding: 16px 24px; }" +
            ".meta { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 24px; font-size: 13px; }" +
            ".meta div { padding: 4px 0; }" +
            ".meta b { color: #555; font-weight: 600; }" +
            "table { width: 100%; border-collapse: collapse; font-size: 13px; margin-top: 8px; }" +
            "th { text-align: left; background: #fafbfc; padding: 8px 12px; border-bottom: 2px solid #1976d2; }" +
            "td { padding: 6px 12px; border-bottom: 1px solid #eee; }" +
            "td.r { text-align: right; font-variant-numeric: tabular-nums; }" +
            "tr.tot td { font-weight: 700; background: #fafbfc; border-top: 2px solid #1976d2; }" +
            ".pair { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; padding: 16px 24px; }" +
            ".net { background: #1b5e20; color: #fff; padding: 18px 24px; border-radius: 0 0 8px 8px; " +
            "       display: flex; justify-content: space-between; align-items: center; }" +
            ".net .v { font-size: 28px; font-weight: 700; }" +
            ".container { box-shadow: 0 4px 18px rgba(0,0,0,.08); border-radius: 8px; }" +
            ".btnbar { text-align: right; margin-top: 12px; }" +
            "button { padding: 8px 18px; border: none; border-radius: 4px; background: #1976d2; color: #fff; cursor: pointer; font-size: 14px; }" +
            "@media print { .btnbar { display: none; } body { margin: 0; } }";

        String company = settings.get("company_name", "Company");
        String employerGstin = "33AAACS1234L1ZA";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>")
            .append("<meta charset='utf-8'><title>Payslip — ").append(esc((String) emp.get("emp_code")))
            .append(" — ").append(period).append("</title>")
            .append("<style>").append(css).append("</style></head><body>");

        if (preview) {
            html.append("<div class='btnbar'>")
                .append("<button onclick='window.print()'>Print / Save as PDF</button>")
                .append("</div>");
        }

        html.append("<div class='container'>");
        html.append("<div class='header'>")
            .append("<h1>").append(esc(company)).append("</h1>")
            .append("<p>GSTIN: ").append(employerGstin).append(" · Payslip for <b>").append(period).append("</b></p>")
            .append("</div>");

        html.append("<div class='section'><div class='meta'>")
            .append("<div><b>Employee Code:</b> ").append(esc((String) emp.get("emp_code"))).append("</div>")
            .append("<div><b>UAN:</b> ").append(esc(orDash((String) emp.get("uan_no")))).append("</div>")
            .append("<div><b>Name:</b> ").append(esc((String) emp.get("full_name"))).append("</div>")
            .append("<div><b>PAN:</b> ").append(esc(orDash((String) emp.get("pan_no")))).append("</div>")
            .append("<div><b>Designation:</b> ").append(esc(orDash((String) emp.get("designation")))).append("</div>")
            .append("<div><b>Department:</b> ").append(esc(orDash((String) emp.get("department")))).append("</div>")
            .append("<div><b>Date of Joining:</b> ").append(esc(String.valueOf(emp.get("date_of_joining")))).append("</div>")
            .append("<div><b>Bank A/c:</b> ").append(esc(orDash((String) emp.get("bank_account_no"))))
            .append(" (").append(esc(orDash((String) emp.get("bank_ifsc")))).append(")</div>")
            .append("<div><b>Payable Days:</b> ").append(r.payableDays == null ? "—" : r.payableDays.toPlainString()).append("</div>")
            .append("<div><b>LOP Days:</b> ").append(r.lopDays == null ? "—" : r.lopDays.toPlainString()).append("</div>")
            .append("</div></div>");

        html.append("<div class='pair'>")
            .append("<table><thead><tr><th>Earnings</th><th class='r'>₹</th></tr></thead><tbody>")
            .append(earnings)
            .append("<tr class='tot'><td>Total Earnings</td><td class='r'>").append(formatCurrency(totalE)).append("</td></tr>")
            .append("</tbody></table>")
            .append("<table><thead><tr><th>Deductions</th><th class='r'>₹</th></tr></thead><tbody>")
            .append(deductions)
            .append("<tr class='tot'><td>Total Deductions</td><td class='r'>").append(formatCurrency(totalD)).append("</td></tr>")
            .append("</tbody></table>")
            .append("</div>");

        html.append("<div class='net'><div>Net Pay (₹)</div><div class='v'>")
            .append(formatCurrency(r.netPay)).append("</div></div>");

        html.append("</div>");   // container

        if (preview) {
            html.append("<p style='text-align:center;font-size:11px;color:#777;margin-top:18px;'>")
                .append("System-generated. Generated at ").append(Instant.now()).append(".</p>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    private String formatCurrency(BigDecimal bd) {
        if (bd == null) return "0.00";
        return String.format("%,.2f", bd);
    }
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
    private String orDash(String s) { return (s == null || s.isEmpty()) ? "—" : s; }
}
