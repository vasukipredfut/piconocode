package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.employee-punch -- mobile punch IN / OUT (replaces ERPNext pico_api.employee_checkin and
 * employee_checkout). Writes one m7_employee_checkin row; attendance itself is derived from these
 * rows by PicoAttendanceProcessor at 23:59.
 *
 * <p>args: {@code log_type} (IN|OUT, required), {@code latitude}, {@code longitude},
 * {@code punch_device}, {@code image_url} (from /api/m0/document-vault/upload).
 * The employee comes from the JWT and the punch time from the server clock.
 *
 * <p>Approved leave covering today blocks the punch, as in ERPNext: a full day always; a
 * FIRST_HALF leave before 14:00; a SECOND_HALF leave from 14:00.
 *
 * <p>Module 2 version: replaces the Module 1 file (needs V383's is_half_day / which_half).
 */
@Component
public class PicoEmployeePunchActionHandler implements ActionHandler {

    public static final String CODE = "pico.employee-punch";
    /** ERPNext's lunch split for half-day leave (pico_api.employee_checkin used hour 14). */
    private static final int HALF_DAY_SPLIT_HOUR = 14;

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return CODE; }

    @Override
    @Transactional
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        String logType = PicoSelf.str(args.get("log_type"));
        if (!"IN".equals(logType) && !"OUT".equals(logType)) {
            throw new IllegalArgumentException("log_type must be IN or OUT");
        }

        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        Long tenantId = TenantContext.get();
        Long employeeId = ((Number) emp.get("id")).longValue();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        String blocked = leaveBlock(tenantId, employeeId, now);
        if (blocked != null) {
            return ActionResult.builder().ok(false)
                    .message(blocked)
                    .data(Map.of("reason", "LEAVE_APPROVED"))
                    .build();
        }

        Object[] values = {
                tenantId, employeeId, logType, Timestamp.valueOf(now),
                decimal(args.get("latitude")), decimal(args.get("longitude")),
                PicoSelf.str(args.get("punch_device")), PicoSelf.str(args.get("image_url")),
                CurrentUser.id()
        };
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO m7_employee_checkin (tenant_id, employee_id, log_type, punch_time, latitude, longitude, " +
                            "punch_device, image_url, source, punched_by_user_id) VALUES (?,?,?,?,?,?,?,?,'MOBILE',?)",
                    Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, keys);
        Long checkinId = keys.getKey() == null ? null : keys.getKey().longValue();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("employee_id", employeeId);
        data.put("emp_code", emp.get("emp_code"));
        data.put("full_name", emp.get("full_name"));
        data.put("log_type", logType);
        data.put("punch_time", now.toString());
        data.put("last_punch_in", lastPunch(tenantId, employeeId, today, "IN"));
        data.put("last_punch_out", lastPunch(tenantId, employeeId, today, "OUT"));
        return ActionResult.builder().ok(true)
                .message("IN".equals(logType) ? "Checkin recorded" : "Punch OUT recorded")
                .recordId(checkinId)
                .data(data)
                .build();
    }

    /** Why an approved leave blocks a punch right now, or null. */
    private String leaveBlock(Long tenantId, Long employeeId, LocalDateTime now) {
        List<Map<String, Object>> leaves = jdbc.queryForList(
                "SELECT is_half_day, which_half FROM m7_leave_request WHERE tenant_id=? AND employee_id=? " +
                        "AND status='APPROVED' AND ? BETWEEN start_date AND end_date",
                tenantId, employeeId, now.toLocalDate());
        boolean afternoon = now.getHour() >= HALF_DAY_SPLIT_HOUR;
        for (Map<String, Object> l : leaves) {
            Object half = l.get("is_half_day");
            boolean halfDay = half instanceof Boolean b ? b : half instanceof Number n && n.intValue() != 0;
            if (!halfDay) {
                return "For date " + now.toLocalDate() + " leave is already approved. Cancel the leave before punching.";
            }
            if ("FIRST_HALF".equals(l.get("which_half")) && !afternoon) {
                return "You are on First Half Leave. Punch allowed only after 2:00 PM.";
            }
            if ("SECOND_HALF".equals(l.get("which_half")) && afternoon) {
                return "You are on Second Half Leave. Punch not allowed after 2:00 PM.";
            }
        }
        return null;
    }

    private Object lastPunch(Long tenantId, Long employeeId, LocalDate day, String logType) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, punch_time FROM m7_employee_checkin WHERE tenant_id=? AND employee_id=? AND log_type=? " +
                        "AND punch_time >= ? AND punch_time < ? ORDER BY punch_time DESC LIMIT 1",
                tenantId, employeeId, logType, day, day.plusDays(1));
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static BigDecimal decimal(Object o) {
        if (o == null || o.toString().isBlank()) return null;
        try {
            return new BigDecimal(o.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("latitude / longitude must be numbers");
        }
    }
}
