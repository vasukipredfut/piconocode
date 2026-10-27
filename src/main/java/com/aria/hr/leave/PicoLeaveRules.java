package com.aria.hr.leave;

import com.aria.hr.attendance.PicoAttendanceRules;
import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Leave rules shared by the pico.leave-* actions, LeaveApproverValidator and LeaveDecisionListener
 * (what ERPNext's Leave Application controller did natively: day count, overlap, balance, approver).
 * Every query is scoped to TenantContext.get(). "Open" leave = PENDING or APPROVED.
 */
@Component
public class PicoLeaveRules {

    public static final String WF_LEAVE = "M7.LeaveRequest";
    public static final String WF_CANCEL = "M7.LeaveCancellation";

    private final JdbcTemplate jdbc;
    private final PicoAttendanceRules attendance;

    public PicoLeaveRules(JdbcTemplate jdbc, PicoAttendanceRules attendance) {
        this.jdbc = jdbc;
        this.attendance = attendance;
    }

    /** Active leave type row, or null. */
    public Map<String, Object> leaveType(String kind) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM m7_leave_type WHERE tenant_id=? AND type_code=? AND is_active=1",
                TenantContext.get(), kind);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 0.5 for a half day; otherwise calendar days, minus holidays unless the type counts them. */
    public BigDecimal leaveDays(Long holidayListId, LocalDate from, LocalDate to, boolean halfDay, boolean includeHolidays) {
        if (halfDay) return new BigDecimal("0.5");
        long days = from.datesUntil(to.plusDays(1)).count();
        if (!includeHolidays) days -= attendance.holidays(holidayListId, from, to).size();
        return BigDecimal.valueOf(Math.max(days, 0));
    }

    /**
     * The approver's m7_employee row (id, full_name, user_id): employee.leave_approver_id, else
     * reports_to_emp_id. Null when neither is set (ERPNext: "Leave approver not configured").
     */
    public Map<String, Object> approverOf(Long employeeId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT a.id, a.full_name, a.user_id FROM m7_employee e " +
                        "JOIN m7_employee a ON a.tenant_id = e.tenant_id AND a.id = COALESCE(e.leave_approver_id, e.reports_to_emp_id) " +
                        "WHERE e.tenant_id=? AND e.id=?",
                TenantContext.get(), employeeId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Official mail, else personal mail, else the login account's mail; null if none. */
    public String emailOf(Long employeeId) {
        if (employeeId == null) return null;
        List<String> rows = jdbc.queryForList(
                "SELECT COALESCE(NULLIF(e.email_official,''), NULLIF(e.email_personal,''), NULLIF(u.email,'')) " +
                        "FROM m7_employee e LEFT JOIN employee_details u ON u.id = e.user_id WHERE e.tenant_id=? AND e.id=?",
                String.class, TenantContext.get(), employeeId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Another open leave of the employee overlapping the range. Two half days on the same date
     * do not overlap when they are different halves.
     */
    public Map<String, Object> overlapping(Long employeeId, LocalDate from, LocalDate to, boolean halfDay, String whichHalf) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, lr_number, start_date, end_date, is_half_day, which_half, status FROM m7_leave_request " +
                        "WHERE tenant_id=? AND employee_id=? AND status IN ('PENDING','APPROVED') " +
                        "AND COALESCE(cancel_status,'') <> 'CANCELLED' AND start_date <= ? AND end_date >= ?",
                TenantContext.get(), employeeId, to, from);
        for (Map<String, Object> r : rows) {
            boolean otherHalf = toBool(r.get("is_half_day"));
            if (halfDay && otherHalf && whichHalf != null && !whichHalf.equals(r.get("which_half"))) continue;
            return r;
        }
        return null;
    }

    /** Allocation of the kind covering the date, or null. */
    public Map<String, Object> allocation(Long employeeId, String kind, LocalDate day) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM m7_leave_allocation WHERE tenant_id=? AND employee_id=? AND leave_kind=? " +
                        "AND from_date <= ? AND to_date >= ? ORDER BY from_date DESC LIMIT 1",
                TenantContext.get(), employeeId, kind, day, day);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Days of the kind used (APPROVED) and waiting (PENDING) inside an allocation's period. */
    public BigDecimal[] usedAndPending(Long employeeId, String kind, LocalDate from, LocalDate to) {
        Map<String, Object> r = jdbc.queryForMap(
                "SELECT COALESCE(SUM(CASE WHEN status='APPROVED' THEN days END),0) AS used, " +
                        "COALESCE(SUM(CASE WHEN status='PENDING' THEN days END),0) AS pending " +
                        "FROM m7_leave_request WHERE tenant_id=? AND employee_id=? AND leave_kind=? " +
                        "AND COALESCE(cancel_status,'') <> 'CANCELLED' AND start_date BETWEEN ? AND ?",
                TenantContext.get(), employeeId, kind, from, to);
        return new BigDecimal[]{(BigDecimal) r.get("used"), (BigDecimal) r.get("pending")};
    }

    /** One row per allocation starting in the year: allocated / used / pending / balance (ERPNext get_leave_balance). */
    public List<Map<String, Object>> balance(Long employeeId, int year) {
        List<Map<String, Object>> out = new ArrayList<>();
        List<Map<String, Object>> allocations = jdbc.queryForList(
                "SELECT a.leave_kind, t.type_name, a.from_date, a.to_date, a.allocated_days FROM m7_leave_allocation a " +
                        "LEFT JOIN m7_leave_type t ON t.tenant_id = a.tenant_id AND t.type_code = a.leave_kind " +
                        "WHERE a.tenant_id=? AND a.employee_id=? AND YEAR(a.from_date)=? ORDER BY a.leave_kind",
                TenantContext.get(), employeeId, year);
        for (Map<String, Object> a : allocations) {
            String kind = (String) a.get("leave_kind");
            LocalDate from = PicoAttendanceRules.toLocalDate(a.get("from_date"));
            LocalDate to = PicoAttendanceRules.toLocalDate(a.get("to_date"));
            BigDecimal allocated = (BigDecimal) a.get("allocated_days");
            BigDecimal[] up = usedAndPending(employeeId, kind, from, to);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("leave_kind", kind);
            row.put("type_name", a.get("type_name"));
            row.put("from_date", from.toString());
            row.put("to_date", to.toString());
            row.put("allocated_leaves", allocated);
            row.put("used_leaves", up[0]);
            row.put("pending_leaves", up[1]);
            row.put("balance_leaves", allocated.subtract(up[0]));
            out.add(row);
        }
        return out;
    }

    public Map<String, Object> leave(Long id) {
        return one("SELECT * FROM m7_leave_request WHERE tenant_id=? AND id=?", id);
    }

    public Map<String, Object> leaveByInstance(Long instanceId) {
        return one("SELECT * FROM m7_leave_request WHERE tenant_id=? AND workflow_instance_id=?", instanceId);
    }

    public Map<String, Object> leaveByCancelInstance(Long instanceId) {
        return one("SELECT * FROM m7_leave_request WHERE tenant_id=? AND cancel_instance_id=?", instanceId);
    }

    /** employee_details.id of an m7_employee, or null. */
    public Long userIdOf(Object employeeId) {
        if (employeeId == null) return null;
        List<Long> rows = jdbc.queryForList(
                "SELECT user_id FROM m7_employee WHERE tenant_id=? AND id=? AND user_id IS NOT NULL",
                Long.class, TenantContext.get(), ((Number) employeeId).longValue());
        return rows.isEmpty() ? null : rows.get(0);
    }

    public String employeeName(Object employeeId) {
        if (employeeId == null) return null;
        List<String> rows = jdbc.queryForList("SELECT full_name FROM m7_employee WHERE tenant_id=? AND id=?",
                String.class, TenantContext.get(), ((Number) employeeId).longValue());
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Map<String, Object> one(String sql, Long id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, TenantContext.get(), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static boolean toBool(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        return o != null && ("true".equalsIgnoreCase(o.toString()) || "1".equals(o.toString()));
    }
}
