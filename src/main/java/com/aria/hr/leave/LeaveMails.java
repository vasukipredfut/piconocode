package com.aria.hr.leave;

import com.aria.integration.service.TenantMailService;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;

/**
 * The leave mails ERPNext sent from pico_api.apply_leave / cancel_leave_application and
 * overrides/leave_application.py, same wording, delivered through TenantMailService.
 * Every interpolated value is HTML-escaped (reasons are free text typed by employees).
 */
@Component
public class LeaveMails {

    private final TenantMailService mail;
    private final PicoLeaveRules rules;

    public LeaveMails(TenantMailService mail, PicoLeaveRules rules) {
        this.mail = mail;
        this.rules = rules;
    }

    /** To the approver, after apply. */
    public void approvalRequested(Map<String, Object> leave) {
        Object approver = leave.get("approver_emp_id");
        mail.send(rules.emailOf(approver == null ? null : ((Number) approver).longValue()),
                "Leave Approval Request - " + leave.get("lr_number"),
                "<p>Dear Approver,</p>"
                        + "<p>Employee <b>" + esc(rules.employeeName(leave.get("employee_id"))) + "</b> has applied for leave.</p>"
                        + "<p><b>Leave Type:</b> " + esc(leave.get("leave_kind")) + halfDayNote(leave) + "<br>"
                        + "<b>From:</b> " + esc(leave.get("start_date")) + "<br>"
                        + "<b>To:</b> " + esc(leave.get("end_date")) + "<br>"
                        + "<b>Days:</b> " + esc(leave.get("days")) + "</p>"
                        + "<p><b>Reason:</b> " + esc(leave.get("reason")) + "</p>"
                        + "<p>Please review it in the Leave approvals queue.</p><p>Thanks</p>");
    }

    /** To the approver, after an employee asks to cancel an approved leave. */
    public void cancellationRequested(Map<String, Object> leave) {
        Object approver = leave.get("approver_emp_id");
        boolean keepHalf = PicoLeaveRules.toBool(leave.get("revised_half_day"));
        String body = "<p>Dear Approver,</p>"
                + "<p>Employee <b>" + esc(rules.employeeName(leave.get("employee_id"))) + "</b> requested cancellation of approved leave <b>"
                + esc(leave.get("lr_number")) + "</b> (" + esc(leave.get("start_date")) + " to " + esc(leave.get("end_date")) + ").</p>"
                + (keepHalf
                    ? "<p>Instead of full cancellation, employee requested <b>Half Day Leave</b>.</p>"
                      + "<p><b>Date:</b> " + esc(halfDayDate(leave)) + "<br><b>Which Half:</b> " + esc(leave.get("revised_which_half")) + "</p>"
                    : "")
                + "<p><b>Reason:</b> " + esc(leave.get("cancel_reason")) + "</p>"
                + "<p>Please review it in the Leave Cancellation Requests queue.</p><p>Thanks</p>";
        mail.send(rules.emailOf(approver == null ? null : ((Number) approver).longValue()),
                (keepHalf ? "Half Day Revision Request - " : "Leave Cancellation Request - ") + leave.get("lr_number"), body);
    }

    /** To the employee: APPROVED / REJECTED / CANCELLED / cancellation DECLINED. */
    public void decided(Map<String, Object> leave, String outcome) {
        mail.send(rules.emailOf(((Number) leave.get("employee_id")).longValue()),
                "Leave Application " + outcome,
                "<p>Dear " + esc(rules.employeeName(leave.get("employee_id"))) + ",</p>"
                        + "<p>Your leave application <b>" + esc(leave.get("lr_number")) + "</b> has been <b>" + esc(outcome) + "</b>.</p>"
                        + "<p><b>From Date:</b> " + esc(leave.get("start_date")) + "<br><b>To Date:</b> " + esc(leave.get("end_date")) + "</p>"
                        + (leave.get("approver_remarks") == null ? "" : "<p><b>Remarks:</b> " + esc(leave.get("approver_remarks")) + "</p>")
                        + "<p>Thanks</p>");
    }

    /** To the employee: the approved leave was cancelled and a half day kept in its place. */
    public void halfDayRevised(Map<String, Object> original, Map<String, Object> revised) {
        mail.send(rules.emailOf(((Number) original.get("employee_id")).longValue()),
                "Half Day Leave Approved",
                "<p>Dear " + esc(rules.employeeName(original.get("employee_id"))) + ",</p>"
                        + "<p>Your leave <b>" + esc(original.get("lr_number")) + "</b> was cancelled.</p>"
                        + "<p>Revised Half Day Leave Approved.</p>"
                        + "<p><b>Date:</b> " + esc(revised.get("start_date")) + "<br>"
                        + "<b>Which Half:</b> " + esc(revised.get("which_half")) + "<br>"
                        + "<b>New Leave:</b> " + esc(revised.get("lr_number")) + "</p>");
    }

    static Object halfDayDate(Map<String, Object> leave) {
        Object d = leave.get("revised_half_day_date");
        return d != null ? d : leave.get("start_date");
    }

    private static String halfDayNote(Map<String, Object> leave) {
        return PicoLeaveRules.toBool(leave.get("is_half_day")) ? " (half day, " + esc(leave.get("which_half")) + ")" : "";
    }

    private static String esc(Object o) {
        return o == null ? "" : HtmlUtils.htmlEscape(o.toString());
    }
}
