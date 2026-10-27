package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.hr.attendance.PicoAttendanceRules;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * pico.my-attendance -- the caller's own attendance data for the mobile app. Always scoped to the
 * employee resolved from the JWT, which the generic datasource query cannot guarantee.
 *
 * <p>args.view:
 * <ul>
 *   <li>{@code today}      -- today's m7_attendance row + shift (pico_api.today_attendance, get_employee_shift)</li>
 *   <li>{@code history}    -- day-by-day status for {@code year} [+ {@code month}] (attendance_history)</li>
 *   <li>{@code holidays}   -- holiday calendar for {@code year} (holiday_calendar)</li>
 *   <li>{@code checkins}   -- punches between {@code from_date} and {@code to_date} (get_employee_checkins)</li>
 *   <li>{@code last-punch} -- last IN / OUT, optionally on {@code date} (get_employee_last_punch)</li>
 * </ul>
 */
@Component
public class PicoMyAttendanceActionHandler implements ActionHandler {

    public static final String CODE = "pico.my-attendance";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PicoAttendanceRules rules;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        Long employeeId = ((Number) emp.get("id")).longValue();
        Long holidayListId = emp.get("holiday_list_id") == null ? null : ((Number) emp.get("holiday_list_id")).longValue();
        String view = PicoSelf.str(args.get("view"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("employee_id", employeeId);
        data.put("emp_code", emp.get("emp_code"));
        data.put("full_name", emp.get("full_name"));
        switch (view == null ? "today" : view) {
            case "today" -> {
                LocalDate today = LocalDate.now();
                String shift = rules.shiftFor(employeeId, (String) emp.get("shift_code"), today);
                List<Map<String, Object>> att = attendance(employeeId, today, today);
                data.put("date", today.toString());
                data.put("shift", rules.shiftType(shift));
                data.put("attendance", att.isEmpty() ? "Not Marked" : att.get(0));
            }
            case "history" -> {
                int year = intArg(args, "year", LocalDate.now().getYear());
                Integer month = args.get("month") == null ? null : intArg(args, "month", 1);
                if (month != null && (month < 1 || month > 12)) throw new IllegalArgumentException("month must be 1-12");
                data.put("year", year);
                data.put("attendance", history(employeeId, holidayListId, year, month));
            }
            case "holidays" -> {
                int year = intArg(args, "year", LocalDate.now().getYear());
                List<Map<String, Object>> out = new ArrayList<>();
                rules.holidays(holidayListId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
                        .forEach((d, desc) -> out.add(Map.of("holiday_date", d.toString(), "description", desc)));
                data.put("year", year);
                data.put("holidays", out);
            }
            case "checkins" -> {
                LocalDate from = dateArg(args, "from_date", LocalDate.now().minusDays(30));
                LocalDate to = dateArg(args, "to_date", LocalDate.now());
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT id, log_type, punch_time, latitude, longitude, punch_device, source, image_url " +
                                "FROM m7_employee_checkin WHERE tenant_id=? AND employee_id=? " +
                                "AND punch_time >= ? AND punch_time < ? ORDER BY punch_time DESC",
                        TenantContext.get(), employeeId, from, to.plusDays(1));
                data.put("count", rows.size());
                data.put("checkins", rows);
            }
            case "last-punch" -> {
                LocalDate day = args.get("date") == null ? null : dateArg(args, "date", null);
                data.put("date", day == null ? "latest" : day.toString());
                data.put("last_punch_in", lastPunch(employeeId, day, "IN"));
                data.put("last_punch_out", lastPunch(employeeId, day, "OUT"));
            }
            default -> throw new IllegalArgumentException("view must be today, history, holidays, checkins or last-punch");
        }
        return ActionResult.builder().ok(true).data(data).build();
    }

    /**
     * Same shape as ERPNext attendance_history: months up to today; a day without an attendance row
     * is ON_LEAVE / HALF_DAY_LEAVE (approved leave), else HOLIDAY, else ABSENT. Days with leave also
     * carry a {@code leave} object. Module 2 version: replaces the Module 1 file.
     */
    private List<Map<String, Object>> history(Long employeeId, Long holidayListId, int year, Integer month) {
        LocalDate today = LocalDate.now();
        int lastMonth = year == today.getYear() ? today.getMonthValue() : 12;
        List<Map<String, Object>> months = new ArrayList<>();
        for (int m = (month == null ? 1 : month); m <= (month == null ? lastMonth : month); m++) {
            YearMonth ym = YearMonth.of(year, m);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth().isAfter(today) ? today : ym.atEndOfMonth();
            if (start.isAfter(end)) continue;
            Map<LocalDate, Map<String, Object>> byDate = new LinkedHashMap<>();
            attendance(employeeId, start, end)
                    .forEach(r -> byDate.put(PicoAttendanceRules.toLocalDate(r.get("date_of")), r));
            Map<LocalDate, String> holidays = rules.holidays(holidayListId, start, end);
            Map<LocalDate, Map<String, Object>> leaves = approvedLeaves(employeeId, start, end);
            List<Map<String, Object>> days = new ArrayList<>();
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                Map<String, Object> day = new LinkedHashMap<>();
                day.put("date", d.toString());
                Map<String, Object> att = byDate.get(d);
                Map<String, Object> leave = leaves.get(d);
                if (leave != null) day.put("leave", leave);
                if (att == null && leave != null) {
                    day.put("status", Boolean.TRUE.equals(leave.get("is_half_day")) ? "HALF_DAY_LEAVE" : "ON_LEAVE");
                } else if (att != null) {
                    day.put("status", att.get("status"));
                    day.put("in_time", att.get("in_time"));
                    day.put("out_time", att.get("out_time"));
                    day.put("working_hours", att.get("worked_hours"));
                } else if (holidays.containsKey(d)) {
                    day.put("status", "HOLIDAY");
                    day.put("description", holidays.get(d));
                } else {
                    day.put("status", "ABSENT");
                }
                days.add(day);
            }
            months.add(Map.of("month", m,
                    "month_name", ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    "data", days));
        }
        return months;
    }

    /** Approved leave per date in the range: lr_number, leave_kind, is_half_day, which_half. */
    private Map<LocalDate, Map<String, Object>> approvedLeaves(Long employeeId, LocalDate from, LocalDate to) {
        Map<LocalDate, Map<String, Object>> out = new LinkedHashMap<>();
        jdbc.queryForList(
                "SELECT lr_number, leave_kind, start_date, end_date, is_half_day, which_half FROM m7_leave_request " +
                        "WHERE tenant_id=? AND employee_id=? AND status='APPROVED' AND start_date <= ? AND end_date >= ?",
                TenantContext.get(), employeeId, to, from)
            .forEach(r -> {
                LocalDate s = PicoAttendanceRules.toLocalDate(r.get("start_date"));
                LocalDate e = PicoAttendanceRules.toLocalDate(r.get("end_date"));
                Object half = r.get("is_half_day");
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("lr_number", r.get("lr_number"));
                info.put("leave_kind", r.get("leave_kind"));
                info.put("is_half_day", half instanceof Boolean b ? b : half instanceof Number n && n.intValue() != 0);
                info.put("which_half", r.get("which_half"));
                for (LocalDate d = s.isBefore(from) ? from : s; !d.isAfter(e) && !d.isAfter(to); d = d.plusDays(1)) {
                    out.put(d, info);
                }
            });
        return out;
    }

    private List<Map<String, Object>> attendance(Long employeeId, LocalDate from, LocalDate to) {
        return jdbc.queryForList(
                "SELECT date_of, status, shift_code, in_time, out_time, worked_hours, ot_hours FROM m7_attendance " +
                        "WHERE tenant_id=? AND employee_id=? AND date_of BETWEEN ? AND ? ORDER BY date_of",
                TenantContext.get(), employeeId, from, to);
    }

    private Object lastPunch(Long employeeId, LocalDate day, String logType) {
        String dayFilter = day == null ? "" : " AND punch_time >= ? AND punch_time < ?";
        Object[] params = day == null
                ? new Object[]{TenantContext.get(), employeeId, logType}
                : new Object[]{TenantContext.get(), employeeId, logType, day, day.plusDays(1)};
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, punch_time, log_type FROM m7_employee_checkin WHERE tenant_id=? AND employee_id=? AND log_type=?" +
                        dayFilter + " ORDER BY punch_time DESC LIMIT 1", params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static int intArg(Map<String, Object> args, String key, int fallback) {
        Object v = args.get(key);
        if (v == null || v.toString().isBlank()) return fallback;
        try {
            return Integer.parseInt(v.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be a number");
        }
    }

    private static LocalDate dateArg(Map<String, Object> args, String key, LocalDate fallback) {
        Object v = args.get(key);
        if (v == null || v.toString().isBlank()) return fallback;
        try {
            return LocalDate.parse(v.toString().trim().substring(0, 10));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(key + " must be YYYY-MM-DD");
        }
    }
}
