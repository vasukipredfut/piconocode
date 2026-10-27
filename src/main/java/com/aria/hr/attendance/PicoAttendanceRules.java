package com.aria.hr.attendance;

import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Shift + holiday lookups shared by PicoAttendanceProcessor and the pico.my-attendance action
 * (ERPNext: custom_attendance.get_employee_shift / check_holiday, pico_api.holiday_calendar).
 * Every query is scoped to TenantContext.get().
 */
@Component
public class PicoAttendanceRules {

    private final JdbcTemplate jdbc;

    public PicoAttendanceRules(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Active shift assignment covering the day, else the employee's default shift_code; null if neither. */
    public String shiftFor(Long employeeId, String defaultShift, LocalDate day) {
        List<String> rows = jdbc.queryForList(
                "SELECT shift_code FROM m7_shift_assignment WHERE tenant_id=? AND employee_id=? AND status='ACTIVE' " +
                        "AND start_date <= ? AND (end_date IS NULL OR end_date >= ?) ORDER BY start_date DESC LIMIT 1",
                String.class, TenantContext.get(), employeeId, day, day);
        return rows.isEmpty() ? defaultShift : rows.get(0);
    }

    /** The shift_type row (timings + OT rules), or null when the code is unknown / inactive. */
    public Map<String, Object> shiftType(String shiftCode) {
        if (shiftCode == null) return null;
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM m7_shift_type WHERE tenant_id=? AND shift_code=? AND is_active=1",
                TenantContext.get(), shiftCode);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Holidays of a list between two dates (inclusive), date -> description; weekly offs included. */
    public Map<LocalDate, String> holidays(Long holidayListId, LocalDate from, LocalDate to) {
        Map<LocalDate, String> out = new TreeMap<>();
        if (holidayListId == null) return out;
        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT weekly_off FROM m7_holiday_list WHERE tenant_id=? AND id=? AND is_active=1",
                TenantContext.get(), holidayListId);
        if (list.isEmpty()) return out;
        String weeklyOff = (String) list.get(0).get("weekly_off");
        if (weeklyOff != null) {
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                if (d.getDayOfWeek().name().equals(weeklyOff)) out.put(d, "Weekly Off");
            }
        }
        jdbc.queryForList(
                "SELECT holiday_date, description FROM m7_holiday WHERE tenant_id=? AND holiday_list_id=? " +
                        "AND holiday_date BETWEEN ? AND ? ORDER BY holiday_date",
                TenantContext.get(), holidayListId, from, to)
            .forEach(r -> out.put(toLocalDate(r.get("holiday_date")), (String) r.get("description")));
        return out;
    }

    /** DATE columns come back as java.sql.Date or LocalDate depending on driver settings. */
    public static LocalDate toLocalDate(Object o) {
        if (o instanceof LocalDate d) return d;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        return o == null ? null : LocalDate.parse(o.toString().substring(0, 10));
    }

    public boolean isHoliday(Long holidayListId, LocalDate day) {
        return !holidays(holidayListId, day, day).isEmpty();
    }
}
