package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.hr.attendance.PicoAttendanceRules;
import com.aria.hr.leave.LeaveMails;
import com.aria.hr.leave.PicoLeaveRules;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * pico.leave-cancel -- replaces ERPNext cancel_leave_application, cancel_leave_applications and
 * delete_leave_application. Only the leave's own employee may call it.
 *
 * <p>args: {@code leave_id} (required), {@code reason}, and for an approved leave optionally
 * {@code revised_half_day} + {@code which_half} (+ {@code half_day_date}) to keep a half day.
 * <ul>
 *   <li>PENDING  -> M7.LeaveRequest transition to WITHDRAWN via pico:leave-withdraw
 *       (ERPNext cancelled the draft; delete_leave_application deleted it -- Aria keeps the row)</li>
 *   <li>APPROVED -> starts M7.LeaveCancellation (REQUESTED, owned by the approver) and mails the
 *       approver. The approver decides on pico:leave-cancel-decide; LeaveDecisionListener applies it.</li>
 * </ul>
 */
@Component
public class PicoLeaveCancelActionHandler implements ActionHandler {

    public static final String CODE = "pico.leave-cancel";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PicoLeaveRules rules;
    @Autowired private LeaveMails mails;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Object rawId = args.get("leave_id");
        if (rawId == null) throw new IllegalArgumentException("leave_id is required");
        Long leaveId;
        try {
            leaveId = Long.valueOf(rawId.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("leave_id must be a number");
        }
        String reason = PicoSelf.str(args.get("reason"));
        if (reason != null && reason.length() > 512) throw new IllegalArgumentException("reason must be at most 512 characters");

        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        Map<String, Object> leave = rules.leave(leaveId);
        if (leave == null || ((Number) leave.get("employee_id")).longValue() != ((Number) emp.get("id")).longValue()) {
            return ActionResult.error("Leave application not found");
        }

        String status = (String) leave.get("status");
        if ("PENDING".equals(status)) return withdraw(leave, reason);
        if (!"APPROVED".equals(status)) return ActionResult.error("Leave is already " + status);
        if ("REQUESTED".equals(leave.get("cancel_status"))) return ActionResult.error("Cancellation already requested");

        boolean keepHalf = PicoLeaveRules.toBool(args.get("revised_half_day"));
        String whichHalf = keepHalf ? PicoSelf.str(args.get("which_half")) : null;
        LocalDate halfDate = null;
        if (keepHalf) {
            if (!"FIRST_HALF".equals(whichHalf) && !"SECOND_HALF".equals(whichHalf)) {
                throw new IllegalArgumentException("which_half must be FIRST_HALF or SECOND_HALF");
            }
            LocalDate start = PicoAttendanceRules.toLocalDate(leave.get("start_date"));
            LocalDate end = PicoAttendanceRules.toLocalDate(leave.get("end_date"));
            String d = PicoSelf.str(args.get("half_day_date"));
            try {
                halfDate = d == null || d.isEmpty() ? start : LocalDate.parse(d.length() > 10 ? d.substring(0, 10) : d);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("half_day_date must be YYYY-MM-DD");
            }
            if (halfDate.isBefore(start) || halfDate.isAfter(end)) {
                throw new IllegalArgumentException("half_day_date must be inside the leave");
            }
        }

        WorkflowInstance inst = workflowEngine.startInstance(PicoLeaveRules.WF_CANCEL,
                Map.of("businessRef", "LeaveRequest#" + leaveId));
        jdbc.update("UPDATE m7_leave_request SET cancel_instance_id=?, cancel_status=?, cancel_reason=?, cancel_requested_at=NOW(), " +
                        "cancel_decided_at=NULL, revised_half_day=?, revised_which_half=?, revised_half_day_date=? WHERE tenant_id=? AND id=?",
                inst.getId(), inst.getCurrentState(), reason, keepHalf ? 1 : 0, whichHalf, halfDate, TenantContext.get(), leaveId);
        mails.cancellationRequested(rules.leave(leaveId));

        return ActionResult.builder().ok(true)
                .message("Cancellation request sent to approver")
                .recordId(leaveId)
                .data(Map.of("cancel_status", inst.getCurrentState(), "half_day_requested", keepHalf,
                        "cancel_instance_id", inst.getId()))
                .build();
    }

    private ActionResult withdraw(Map<String, Object> leave, String reason) {
        Object instanceId = leave.get("workflow_instance_id");
        if (instanceId == null) return ActionResult.error("Leave has no workflow instance; ask HR to withdraw it");
        Map<String, Object> form = new HashMap<>();
        form.put("reason", reason);
        try {
            workflowEngine.handlePageSubmission(((Number) instanceId).longValue(), "pico:leave-withdraw", form, CurrentUser.id());
        } catch (RuntimeException e) {
            return ActionResult.error("Could not withdraw leave: " + e.getMessage());
        }
        return ActionResult.builder().ok(true).message("Leave cancelled successfully")
                .recordId(leave.get("id")).data(Map.of("status", "WITHDRAWN")).build();
    }
}
