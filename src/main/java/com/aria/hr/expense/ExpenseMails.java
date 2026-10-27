package com.aria.hr.expense;

import com.aria.hr.leave.PicoLeaveRules;
import com.aria.integration.service.TenantMailService;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;

/**
 * Expense mails: ERPNext send_expense_approval_email (to the approver) plus the employee
 * notifications ERPNext lacked (approved / rejected / reimbursed). Values are HTML-escaped.
 * ERPNext attached the receipt files; here the receipt links are listed instead (document-vault URLs).
 */
@Component
public class ExpenseMails {

    private final TenantMailService mail;
    private final PicoLeaveRules people;

    public ExpenseMails(TenantMailService mail, PicoLeaveRules people) {
        this.mail = mail;
        this.people = people;
    }

    public void approvalRequested(Map<String, Object> claim, List<Map<String, Object>> lines) {
        StringBuilder rows = new StringBuilder();
        for (Map<String, Object> l : lines) {
            rows.append("<tr><td>").append(esc(l.get("expense_date"))).append("</td><td>").append(esc(l.get("type_code")))
                .append("</td><td>").append(esc(l.get("description"))).append("</td><td style='text-align:right'>")
                .append(esc(l.get("amount"))).append("</td><td>")
                .append(l.get("receipt_url") == null ? "" : "<a href='" + esc(l.get("receipt_url")) + "'>receipt</a>")
                .append("</td></tr>");
        }
        Object approver = claim.get("approver_emp_id");
        mail.send(people.emailOf(approver == null ? null : ((Number) approver).longValue()),
                "Expense Approval Request - " + claim.get("claim_number"),
                "<p>Dear sir/madam,</p><p>Expense claim is waiting for your approval.</p>"
                        + "<p><b>Employee:</b> " + esc(people.employeeName(claim.get("employee_id"))) + "<br>"
                        + "<b>Total Amount:</b> " + esc(claim.get("total_claimed")) + "</p>"
                        + "<table border='1' cellpadding='4' cellspacing='0'><tr><th>Date</th><th>Type</th><th>Description</th><th>Amount</th><th></th></tr>"
                        + rows + "</table>"
                        + (claim.get("remarks") == null ? "" : "<p><b>Remarks:</b> " + esc(claim.get("remarks")) + "</p>")
                        + "<p>Please review it in the Expense approvals queue.</p>");
    }

    /** To the employee: Approved / Rejected / Reimbursed. */
    public void decided(Map<String, Object> claim, String outcome) {
        mail.send(people.emailOf(((Number) claim.get("employee_id")).longValue()),
                "Expense Claim " + outcome + " - " + claim.get("claim_number"),
                "<p>Dear " + esc(people.employeeName(claim.get("employee_id"))) + ",</p>"
                        + "<p>Your expense claim <b>" + esc(claim.get("claim_number")) + "</b> has been <b>" + esc(outcome) + "</b>.</p>"
                        + "<p><b>Claimed:</b> " + esc(claim.get("total_claimed"))
                        + (claim.get("total_sanctioned") == null ? "" : "<br><b>Sanctioned:</b> " + esc(claim.get("total_sanctioned")))
                        + (claim.get("pay_reference") == null ? "" : "<br><b>Payment Reference:</b> " + esc(claim.get("pay_reference")))
                        + "</p>"
                        + (claim.get("approver_remarks") == null ? "" : "<p><b>Remarks:</b> " + esc(claim.get("approver_remarks")) + "</p>")
                        + "<p>Thanks</p>");
    }

    private static String esc(Object o) {
        return o == null ? "" : HtmlUtils.htmlEscape(o.toString());
    }
}
