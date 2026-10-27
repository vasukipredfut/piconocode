package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.hr.attendance.PicoAttendanceRules;
import com.aria.hr.leave.LeaveMails;
import com.aria.hr.leave.PicoLeaveRules;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * pico.leave-apply -- replaces ERPNext pico_api.apply_leave.
 *
 * <p>args: {@code leave_kind}, {@code from_date}, {@code to_date} (YYYY-MM-DD, required),
 * {@code is_half_day}, {@code which_half} (FIRST_HALF|SECOND_HALF), {@code reason}.
 * The employee comes from the JWT and the approver from the employee master.
 *
 * <p>Checks ERPNext's Leave Application did natively: dates, half day on a single date,
 * overlap with another open leave, approver configured, remaining balance (unless the type is
 * LWP or allows negative). Then inserts the m7_leave_request row (PENDING), starts
 * M7.LeaveRequest, links the instance, and mails the approver.
 *
 * <p>Not @Transactional, like GenericEntityController.insert: the workflow engine runs on its own
 * transaction manager. If starting the instance fails, the inserted row is deleted again.
 */
@Component
public class PicoLeaveApplyActionHandler implements ActionHandler {

    public static final String CODE = "pico.leave-apply";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PicoLeaveRules rules;
    @Autowired private LeaveMails mails;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        String kind = PicoSelf.str(args.get("leave_kind"));
        LocalDate from = date(args, "from_date");
        LocalDate to = date(args, "to_date");
        boolean halfDay = PicoLeaveRules.toBool(args.get("is_half_day"));
        String whichHalf = PicoSelf.str(args.get("which_half"));
        String reason = PicoSelf.str(args.get("reason"));
        if (kind == null || kind.isEmpty()) throw new IllegalArgumentException("leave_kind is required");
        if (to.isBefore(from)) throw new IllegalArgumentException("to_date must not be before from_date");
        if (halfDay) {
            if (!from.equals(to)) throw new IllegalArgumentException("A half day leave must have from_date = to_date");
            if (!"FIRST_HALF".equals(whichHalf) && !"SECOND_HALF".equals(whichHalf)) {
                throw new IllegalArgumentException("which_half must be FIRST_HALF or SECOND_HALF for a half day");
            }
        } else {
            whichHalf = null;
        }
        if (reason != null && reason.length() > 256) throw new IllegalArgumentException("reason must be at most 256 characters");

        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        Long employeeId = ((Number) emp.get("id")).longValue();

        Map<String, Object> type = rules.leaveType(kind);
        if (type == null) return ActionResult.error("Unknown or inactive leave type " + kind);
        Map<String, Object> approver = rules.approverOf(employeeId);
        if (approver == null) return ActionResult.error("Leave approver not configured for employee");

        Long holidayListId = emp.get("holiday_list_id") == null ? null : ((Number) emp.get("holiday_list_id")).longValue();
        BigDecimal days = rules.leaveDays(holidayListId, from, to, halfDay,
                PicoLeaveRules.toBool(type.get("include_holidays")));
        if (days.signum() == 0) return ActionResult.error("The selected dates are all holidays");

        Map<String, Object> clash = rules.overlapping(employeeId, from, to, halfDay, whichHalf);
        if (clash != null) {
            return ActionResult.builder().ok(false)
                    .message("Leave " + clash.get("lr_number") + " (" + clash.get("status") + ") already covers "
                            + clash.get("start_date") + " to " + clash.get("end_date"))
                    .data(Map.of("reason", "OVERLAP", "leave_id", clash.get("id")))
                    .build();
        }

        if (!PicoLeaveRules.toBool(type.get("is_lwp")) && !PicoLeaveRules.toBool(type.get("allow_negative"))) {
            Map<String, Object> alloc = rules.allocation(employeeId, kind, from);
            if (alloc == null) return ActionResult.error("No " + kind + " allocation covers " + from);
            BigDecimal[] up = rules.usedAndPending(employeeId, kind,
                    PicoAttendanceRules.toLocalDate(alloc.get("from_date")), PicoAttendanceRules.toLocalDate(alloc.get("to_date")));
            BigDecimal left = ((BigDecimal) alloc.get("allocated_days")).subtract(up[0]).subtract(up[1]);
            if (left.compareTo(days) < 0) {
                return ActionResult.builder().ok(false)
                        .message("Insufficient " + kind + " balance: " + left + " day(s) left (pending leave counted), " + days + " requested")
                        .data(Map.of("reason", "INSUFFICIENT_BALANCE", "available", left, "requested", days))
                        .build();
            }
        }

        Long tenantId = TenantContext.get();
        Long leaveId = insert(tenantId, employeeId, kind, from, to, days, reason, halfDay, whichHalf,
                ((Number) approver.get("id")).longValue());
        String lrNumber = String.format("LR-%d-%05d", from.getYear(), leaveId);
        WorkflowInstance inst;
        try {
            inst = workflowEngine.startInstance(PicoLeaveRules.WF_LEAVE, Map.of("businessRef", "LeaveRequest#" + leaveId));
        } catch (RuntimeException e) {
            jdbc.update("DELETE FROM m7_leave_request WHERE tenant_id=? AND id=?", tenantId, leaveId);
            throw e;
        }
        jdbc.update("UPDATE m7_leave_request SET lr_number=?, workflow_instance_id=?, current_step=? WHERE tenant_id=? AND id=?",
                lrNumber, inst.getId(), inst.getCurrentState(), tenantId, leaveId);

        Map<String, Object> leave = rules.leave(leaveId);
        mails.approvalRequested(leave);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("leave_id", leaveId);
        data.put("lr_number", lrNumber);
        data.put("days", days);
        data.put("status", "PENDING");
        data.put("workflow_instance_id", inst.getId());
        data.put("leave_approver", approver.get("full_name"));
        return ActionResult.builder().ok(true)
                .message("Leave application submitted successfully")
                .recordId(leaveId)
                .data(data)
                .build();
    }

    private Long insert(Long tenantId, Long employeeId, String kind, LocalDate from, LocalDate to, BigDecimal days,
                        String reason, boolean halfDay, String whichHalf, Long approverId) {
        // lr_number is NOT NULL + unique: a placeholder until the id is known.
        Object[] values = {tenantId, "TMP-" + UUID.randomUUID().toString().substring(0, 24), employeeId, kind, from, to, days,
                reason, halfDay ? 1 : 0, whichHalf, approverId};
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO m7_leave_request (tenant_id, lr_number, employee_id, leave_kind, start_date, end_date, days, " +
                            "reason, is_half_day, which_half, approver_emp_id, status, current_step) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,'PENDING','PENDING')",
                    Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }

    private static LocalDate date(Map<String, Object> args, String key) {
        String v = PicoSelf.str(args.get(key));
        if (v == null || v.isEmpty()) throw new IllegalArgumentException(key + " is required");
        try {
            return LocalDate.parse(v.length() > 10 ? v.substring(0, 10) : v);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(key + " must be YYYY-MM-DD");
        }
    }
}
