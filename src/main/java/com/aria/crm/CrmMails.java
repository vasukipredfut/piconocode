package com.aria.crm;

import com.aria.hr.leave.PicoLeaveRules;
import com.aria.integration.service.TenantMailService;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;

/**
 * ERPNext send_lead_notification_email and send_quotation_email, same content, delivered through
 * TenantMailService. Values are HTML-escaped (lead fields are typed by field staff).
 */
@Component
public class CrmMails {

    private final TenantMailService mail;
    private final PicoLeaveRules people;

    public CrmMails(TenantMailService mail, PicoLeaveRules people) {
        this.mail = mail;
        this.people = people;
    }

    /** To the lead owner / sales person (employee id). */
    public void newLead(Map<String, Object> lead, Object recipientEmpId) {
        if (recipientEmpId == null) return;
        mail.send(people.emailOf(((Number) recipientEmpId).longValue()),
                "New Lead Generated - " + lead.get("lead_number"),
                "<p>Dear " + esc(people.employeeName(recipientEmpId)) + ",</p><p>A new lead has been generated.</p>"
                        + "<table border='1' cellpadding='8' cellspacing='0'>"
                        + row("Lead ID", lead.get("lead_number")) + row("Lead Name", lead.get("lead_name"))
                        + row("Company Name", lead.get("company_name")) + row("Mobile Number", lead.get("contact_phone"))
                        + row("Email", lead.get("contact_email")) + row("Territory", lead.get("territory"))
                        + row("City/Town", lead.get("city_town"))
                        + row("Sales Person", people.employeeName(lead.get("sales_person_emp_id")))
                        + row("Created By", people.employeeName(lead.get("created_by_emp_id")))
                        + "</table><p>Please follow it up in Aria.</p><p>Thanks</p>");
    }

    /** To the lead / customer email on file. */
    public void quotation(Map<String, Object> quote, List<Map<String, Object>> lines, String to) {
        StringBuilder rows = new StringBuilder();
        for (Map<String, Object> l : lines) {
            rows.append("<tr><td>").append(esc(l.get("item_name") != null ? l.get("item_name") : l.get("sku_code")))
                .append("</td><td style='text-align:right'>").append(esc(l.get("qty")))
                .append("</td><td style='text-align:right'>").append(esc(l.get("rate")))
                .append("</td><td style='text-align:right'>").append(esc(l.get("gst_rate_pct"))).append("%")
                .append("</td><td style='text-align:right'>").append(esc(l.get("amount"))).append("</td></tr>");
        }
        mail.send(to, "Quotation Submitted - " + quote.get("quote_number"),
                "<p>Dear Customer,</p><p>Your quotation has been submitted successfully.</p>"
                        + "<table border='1' cellpadding='8' cellspacing='0'>"
                        + row("Quotation", quote.get("quote_number")) + row("Quotation To", quote.get("customer_name"))
                        + row("Transaction Date", quote.get("quote_date"))
                        + (quote.get("valid_until") == null ? "" : row("Valid Until", quote.get("valid_until")))
                        + "</table><br><table border='1' cellpadding='6' cellspacing='0'>"
                        + "<tr><th>Item</th><th>Qty</th><th>Rate</th><th>GST</th><th>Amount</th></tr>" + rows
                        + "<tr><td colspan='4'><b>Subtotal</b></td><td style='text-align:right'>" + esc(quote.get("subtotal")) + "</td></tr>"
                        + "<tr><td colspan='4'><b>Tax</b></td><td style='text-align:right'>" + esc(quote.get("tax_total")) + "</td></tr>"
                        + "<tr><td colspan='4'><b>Grand Total</b></td><td style='text-align:right'><b>" + esc(quote.get("total_value")) + "</b></td></tr>"
                        + "</table><p>Thanks</p>");
    }

    private static String row(String label, Object value) {
        return "<tr><td><b>" + esc(label) + "</b></td><td>" + esc(value) + "</td></tr>";
    }

    private static String esc(Object o) {
        return o == null ? "" : HtmlUtils.htmlEscape(o.toString());
    }
}
