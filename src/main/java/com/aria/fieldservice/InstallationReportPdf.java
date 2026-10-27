package com.aria.fieldservice;

import com.aria.security.TenantContext;
import com.aria.tenant.TenantSettings;
import com.aria.utils.Htmltopdf;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Printable installation report (ERPNext print format "Installation Report",
 * download_installation_report_pdf). Plain tables only, so Flying Saucer (Htmltopdf) renders it
 * the same as a browser. Verified reports carry the customer-verification stamp.
 */
@Component
public class InstallationReportPdf {

    private final JdbcTemplate jdbc;
    private final Htmltopdf htmlToPdf;
    private final TenantSettings settings;

    public InstallationReportPdf(JdbcTemplate jdbc, Htmltopdf htmlToPdf, TenantSettings settings) {
        this.jdbc = jdbc;
        this.htmlToPdf = htmlToPdf;
        this.settings = settings;
    }

    /** PDF bytes, or null when rendering failed. */
    public byte[] pdf(long reportId) {
        return htmlToPdf.generatePdfFromBase64(Base64.getEncoder().encodeToString(html(reportId).getBytes(StandardCharsets.UTF_8)));
    }

    public String html(long reportId) {
        Map<String, Object> r = jdbc.queryForMap(
                "SELECT r.*, c.customer_name, c.customer_code, e.full_name AS engineer FROM m9_installation_report r " +
                        "JOIN m8_customer c ON c.tenant_id = r.tenant_id AND c.id = r.customer_id " +
                        "JOIN m7_employee e ON e.tenant_id = r.tenant_id AND e.id = r.engineer_emp_id " +
                        "WHERE r.tenant_id=? AND r.id=?", TenantContext.get(), reportId);
        String company = settings.get("company_name", "PICO Automation");
        String warranty = switch (String.valueOf(r.get("warranty_kind"))) {
            case "UW" -> "Under Warranty";
            case "AMC" -> "AMC";
            case "OW" -> "Out of Warranty";
            case "O" -> "Others";
            default -> "-";
        };
        StringBuilder h = new StringBuilder();
        h.append("<html><head><meta charset='utf-8'/><style>")
         .append("body{font-family:Helvetica,Arial,sans-serif;font-size:11px;color:#111}")
         .append("h1{font-size:16px;margin:0}table{width:100%;border-collapse:collapse;margin-top:8px}")
         .append("td,th{border:1px solid #999;padding:5px;vertical-align:top;text-align:left}th{background:#eee;width:28%}")
         .append(".stamp{margin-top:12px;padding:8px;border:2px solid ").append("VERIFIED".equals(r.get("status")) ? "#1b5e20" : "#b71c1c").append("}")
         .append("</style></head><body>")
         .append("<h1>").append(esc(company)).append(" - Installation / Service Report</h1>")
         .append("<table>")
         .append(row("Report No", r.get("report_number"))).append(row("Doc No", r.get("doc_no")))
         .append(row("Date", r.get("report_date"))).append(row("Customer", r.get("customer_name") + " (" + r.get("customer_code") + ")"))
         .append(row("Address", r.get("address"))).append(row("Contact Person", r.get("contact_person_name")))
         .append(row("Contact Number", r.get("contact_number"))).append(row("Email", r.get("email")))
         .append(row("Warranty", warranty)).append(row("Call Received", r.get("call_receipt_date")))
         .append(row("Nature of Call", r.get("nature_of_call")))
         .append(row("Visit", r.get("call_date_from") + " to " + r.get("call_date_to")))
         .append(row("Machine Type", r.get("machine_type"))).append(row("Machine Model", r.get("machine_model")))
         .append(row("Primary Communication", r.get("primary_communication")))
         .append(row("Pre-work Observation", r.get("pre_work_observation")))
         .append(row("Action Summary", r.get("action_summary")))
         .append(row("Customer Remarks", r.get("remarks_by_customer")))
         .append(row("Work Done Nature", yes(r.get("work_done_nature")))).append(row("System Operation Explained", yes(r.get("system_operation"))))
         .append(row("Suggestions for Trouble", yes(r.get("suggestions_for_trouble")))).append(row("Purpose / Reason", yes(r.get("purpose_reason"))))
         .append(row("Service Engineer", r.get("engineer")))
         .append("</table><div class='stamp'>");
        if ("VERIFIED".equals(r.get("status"))) {
            h.append("<b>Verified by customer</b> with a one-time code sent to ").append(esc(r.get("otp_sent_to")))
             .append(" on ").append(esc(r.get("otp_verified_at")));
        } else {
            h.append("<b>").append(esc(r.get("status"))).append("</b> - not yet verified by the customer");
        }
        return h.append("</div></body></html>").toString();
    }

    private static String row(String label, Object value) {
        return "<tr><th>" + esc(label) + "</th><td>" + esc(value).replace("\n", "<br/>") + "</td></tr>";
    }

    private static String yes(Object o) {
        boolean b = o instanceof Boolean x ? x : o instanceof Number n && n.intValue() != 0;
        return b ? "Yes" : "No";
    }

    private static String esc(Object o) {
        return o == null || "null".equals(String.valueOf(o)) ? "" : HtmlUtils.htmlEscape(o.toString());
    }
}
