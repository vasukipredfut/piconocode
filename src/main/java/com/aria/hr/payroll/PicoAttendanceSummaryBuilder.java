package com.aria.hr.payroll;

import com.aria.hr.attendance.PicoAttendanceRules;
import com.aria.security.TenantContext;
import com.aria.tenant.TenantSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds m7_attendance_summary (PayrollCalcService's input) for one employee and month from
 * m7_attendance (Module 1), approved m7_leave_request (Module 2) and the employee's holiday list.
 * Nothing in Aria filled this table before, so payroll always used its default of 26 paid days.
 *
 * <p>Each calendar day is classified once:
 * <ul>
 *   <li>before date_of_joining / after separation_date -> LOP</li>
 *   <li>weekly off / holiday of the employee's list    -> weekly_off_days / holiday_days (paid, even
 *       when worked: holiday work is paid through OT at the holiday multiplier)</li>
 *   <li>otherwise, in halves: attendance PRESENT = 1, HALF_DAY = 0.5 present; approved paid leave
 *       fills the missing half/day as paid_leave, approved LWP leave as lop; anything still missing
 *       is LOP when payroll.absent_is_lop=true (default), else counted as present</li>
 * </ul>
 * payable_days is a generated column (present + paid_leave + holiday + weekly_off).
 * ot_hours = sum of m7_overtime_entry.ot_hours (used only when payroll.ot_source=ATTENDANCE_SUMMARY).
 */
@Component
public class PicoAttendanceSummaryBuilder {

    public static final String ABSENT_IS_LOP_SETTING = "payroll.absent_is_lop";

    private final JdbcTemplate jdbc;
    private final PicoAttendanceRules attendanceRules;
    private final TenantSettings settings;

    public PicoAttendanceSummaryBuilder(JdbcTemplate jdbc, PicoAttendanceRules attendanceRules, TenantSettings settings) {
        this.jdbc = jdbc;
        this.attendanceRules = attendanceRules;
        this.settings = settings;
    }

    /** Rebuilds (upserts) the summary row; returns the values written. */
    public Map<String, Object> build(Map<String, Object> emp, YearMonth period) {
        Long tenantId = TenantContext.get();
        long empId = ((Number) emp.get("id")).longValue();
        LocalDate from = period.atDay(1);
        LocalDate to = period.atEndOfMonth();
        LocalDate joined = PicoAttendanceRules.toLocalDate(emp.get("date_of_joining"));
        LocalDate left = PicoAttendanceRules.toLocalDate(emp.get("separation_date"));
        Long holidayListId = emp.get("holiday_list_id") == null ? null : ((Number) emp.get("holiday_list_id")).longValue();
        boolean absentIsLop = !"false".equalsIgnoreCase(settings.get(ABSENT_IS_LOP_SETTING, "true"));

        Map<LocalDate, String> attendance = new HashMap<>();
        int missingPunch = 0;
        for (Map<String, Object> r : jdbc.queryForList(
                "SELECT date_of, status, out_time FROM m7_attendance WHERE tenant_id=? AND employee_id=? AND date_of BETWEEN ? AND ?",
                tenantId, empId, from, to)) {
            attendance.put(PicoAttendanceRules.toLocalDate(r.get("date_of")), (String) r.get("status"));
            if (r.get("out_time") == null) missingPunch++;
        }
        // date -> {paidHalves, lwpHalves} from approved leave (a half day leave = 1 half, full = 2)
        Map<LocalDate, int[]> leave = new HashMap<>();
        for (Map<String, Object> r : jdbc.queryForList(
                "SELECT lr.start_date, lr.end_date, lr.is_half_day, COALESCE(t.is_lwp, lr.leave_kind = 'LWP') AS is_lwp " +
                        "FROM m7_leave_request lr LEFT JOIN m7_leave_type t ON t.tenant_id = lr.tenant_id AND t.type_code = lr.leave_kind " +
                        "WHERE lr.tenant_id=? AND lr.employee_id=? AND lr.status='APPROVED' AND lr.start_date <= ? AND lr.end_date >= ?",
                tenantId, empId, to, from)) {
            LocalDate s = PicoAttendanceRules.toLocalDate(r.get("start_date"));
            LocalDate e = PicoAttendanceRules.toLocalDate(r.get("end_date"));
            int halves = truthy(r.get("is_half_day")) ? 1 : 2;
            boolean lwp = truthy(r.get("is_lwp"));
            for (LocalDate d = s.isBefore(from) ? from : s; !d.isAfter(e) && !d.isAfter(to); d = d.plusDays(1)) {
                int[] h = leave.computeIfAbsent(d, k -> new int[2]);
                h[lwp ? 1 : 0] += halves;
            }
        }
        Map<LocalDate, String> holidays = attendanceRules.holidays(holidayListId, from, to);

        int presentHalves = 0, paidLeaveHalves = 0, lopHalves = 0, holidayDays = 0, weeklyOffDays = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            if ((joined != null && d.isBefore(joined)) || (left != null && d.isAfter(left))) {
                lopHalves += 2;
                continue;
            }
            String holiday = holidays.get(d);
            if (holiday != null) {
                if ("Weekly Off".equals(holiday)) weeklyOffDays++; else holidayDays++;
                continue;
            }
            String status = attendance.get(d);
            int present = "PRESENT".equals(status) || "WFH".equals(status) || "LATE".equals(status) ? 2
                    : "HALF_DAY".equals(status) ? 1 : 0;
            int[] l = leave.getOrDefault(d, new int[2]);
            int remaining = 2 - present;
            int paid = Math.min(l[0], remaining);
            remaining -= paid;
            int lwp = Math.min(l[1], remaining);
            remaining -= lwp;
            presentHalves += present + (absentIsLop ? 0 : remaining);
            paidLeaveHalves += paid;
            lopHalves += lwp + (absentIsLop ? remaining : 0);
        }

        BigDecimal otHours = jdbc.queryForObject(
                "SELECT COALESCE(SUM(ot_hours),0) FROM m7_overtime_entry WHERE tenant_id=? AND employee_id=? " +
                        "AND status <> 'CANCELLED' AND attendance_date BETWEEN ? AND ?",
                BigDecimal.class, tenantId, empId, from, to);

        Map<String, Object> out = new HashMap<>();
        out.put("total_days", period.lengthOfMonth());
        out.put("present_days", halves(presentHalves));
        out.put("paid_leave_days", halves(paidLeaveHalves));
        out.put("lop_days", halves(lopHalves));
        out.put("holiday_days", holidayDays);
        out.put("weekly_off_days", weeklyOffDays);
        out.put("ot_hours", otHours);
        out.put("missing_punch_count", missingPunch);
        jdbc.update(
                "INSERT INTO m7_attendance_summary (tenant_id, emp_id, period, total_days, present_days, weekly_off_days, holiday_days, " +
                        "paid_leave_days, lop_days, ot_hours, missing_punch_count, computed_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,NOW()) " +
                        "ON DUPLICATE KEY UPDATE total_days=VALUES(total_days), present_days=VALUES(present_days), " +
                        "weekly_off_days=VALUES(weekly_off_days), holiday_days=VALUES(holiday_days), paid_leave_days=VALUES(paid_leave_days), " +
                        "lop_days=VALUES(lop_days), ot_hours=VALUES(ot_hours), missing_punch_count=VALUES(missing_punch_count), computed_at=NOW()",
                tenantId, empId, period.toString(), period.lengthOfMonth(), out.get("present_days"), weeklyOffDays, holidayDays,
                out.get("paid_leave_days"), out.get("lop_days"), otHours, missingPunch);
        return out;
    }

    private static BigDecimal halves(int n) {
        return BigDecimal.valueOf(n).divide(BigDecimal.valueOf(2));
    }

    private static boolean truthy(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        return o != null && ("1".equals(o.toString()) || "true".equalsIgnoreCase(o.toString()));
    }

    /** Active employees of the current tenant (optionally limited to ids) that have a salary card for the month. */
    public List<Map<String, Object>> payrollEmployees(YearMonth period, List<Long> onlyIds) {
        String filter = onlyIds == null || onlyIds.isEmpty() ? ""
                : " AND e.id IN (" + String.join(",", onlyIds.stream().map(String::valueOf).toList()) + ")";
        return jdbc.queryForList(
                "SELECT e.id, e.emp_code, e.full_name, e.date_of_joining, e.separation_date, e.holiday_list_id FROM m7_employee e " +
                        "WHERE e.tenant_id=? AND (e.status='ACTIVE' OR e.separation_date >= ?)" + filter +
                        " AND EXISTS (SELECT 1 FROM m7_employee_salary s WHERE s.tenant_id=e.tenant_id AND s.emp_id=e.id " +
                        "AND s.effective_from <= ? AND (s.effective_to IS NULL OR s.effective_to >= ?)) ORDER BY e.emp_code",
                TenantContext.get(), period.atDay(1), period.atEndOfMonth(), period.atDay(1));
    }
}
