package com.aria.hr.leave;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Side-effects of leave decisions (ERPNext overrides/leave_application.py on_update /
 * on_update_after_submit). Runs after WorkflowProjectionListener, so status / cancel_status
 * on m7_leave_request are already written.
 * <ul>
 *   <li>M7.LeaveRequest APPROVED / REJECTED          -> mail the employee</li>
 *   <li>M7.LeaveCancellation CANCELLED               -> leave status CANCELLED; if the employee asked
 *       to keep a half day, insert that half-day leave as APPROVED (no workflow: it was decided in
 *       this same step) and mail it; else mail "Cancelled"</li>
 *   <li>M7.LeaveCancellation DECLINED                -> mail the employee (the leave stays APPROVED)</li>
 * </ul>
 */
@Component
public class LeaveDecisionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(LeaveDecisionListener.class);

    private final JdbcTemplate jdbc;
    private final PicoLeaveRules rules;
    private final LeaveMails mails;

    public LeaveDecisionListener(JdbcTemplate jdbc, PicoLeaveRules rules, LeaveMails mails) {
        this.jdbc = jdbc;
        this.rules = rules;
        this.mails = mails;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        String code = instance.getWorkflowCode();
        String state = instance.getCurrentState();
        if (PicoLeaveRules.WF_LEAVE.equals(code)) {
            if (!"APPROVED".equals(state) && !"REJECTED".equals(state)) return;
            Map<String, Object> leave = rules.leaveByInstance(instance.getId());
            if (leave != null) mails.decided(leave, "APPROVED".equals(state) ? "Approved" : "Rejected");
            return;
        }
        if (!PicoLeaveRules.WF_CANCEL.equals(code)) return;
        Map<String, Object> leave = rules.leaveByCancelInstance(instance.getId());
        if (leave == null) return;
        if ("DECLINED".equals(state)) {
            mails.decided(leave, "kept (cancellation declined)");
            return;
        }
        if (!"CANCELLED".equals(state)) return;

        Long tenantId = TenantContext.get();
        jdbc.update("UPDATE m7_leave_request SET status='CANCELLED', current_step='CANCELLED' WHERE tenant_id=? AND id=?",
                tenantId, leave.get("id"));
        if (!PicoLeaveRules.toBool(leave.get("revised_half_day"))) {
            mails.decided(leave, "Cancelled");
            return;
        }
        Object halfDate = LeaveMails.halfDayDate(leave);
        String lrNumber = leave.get("lr_number") + "-H";
        jdbc.update("INSERT INTO m7_leave_request (tenant_id, lr_number, employee_id, leave_kind, start_date, end_date, days, reason, " +
                        "status, current_step, is_half_day, which_half, approver_emp_id, approver_remarks, decided_at, revised_from_id) " +
                        "VALUES (?,?,?,?,?,?,0.5,?,'APPROVED','APPROVED',1,?,?,?,NOW(),?)",
                tenantId, lrNumber, leave.get("employee_id"), leave.get("leave_kind"), halfDate, halfDate,
                truncate((leave.get("reason") == null ? "" : leave.get("reason") + " -- ") + "Revised Half Day Leave", 256),
                leave.get("revised_which_half"), leave.get("approver_emp_id"), leave.get("approver_remarks"), leave.get("id"));
        Map<String, Object> revised = jdbc.queryForMap(
                "SELECT * FROM m7_leave_request WHERE tenant_id=? AND lr_number=?", tenantId, lrNumber);
        log.info("leave {} cancelled, half day {} kept as {}", leave.get("lr_number"), halfDate, lrNumber);
        mails.halfDayRevised(leave, revised);
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
