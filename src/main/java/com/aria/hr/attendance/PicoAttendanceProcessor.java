package com.aria.hr.attendance;

import com.aria.security.TenantContext;
import com.aria.security.TenantIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Nightly checkins -> m7_attendance (+ m7_overtime_entry), for every active tenant. Port of ERPNext
 * spokes_app/overrides/custom_attendance.process_attendance_for_date (cron "59 23 * * *").
 *
 * <p>Per active employee with at least one IN punch that day and no attendance row yet:
 * <ul>
 *   <li>status PRESENT, or HALF_DAY when the first IN is after the shift's full_day_before_time;
 *       always PRESENT on a holiday</li>
 *   <li>OT when the last OUT is after ot_start_time by at least min_ot_minutes:
 *       hours x hourly rate x (holiday_ot_multiplier | ot_multiplier)</li>
 * </ul>
 * Like ERPNext, employees without punches get no row (the mobile history shows them as Absent).
 * Existing attendance / OT rows are never overwritten, so re-running a date is safe.
 */
@Component
public class PicoAttendanceProcessor {

    private static final Logger log = LoggerFactory.getLogger(PicoAttendanceProcessor.class);

    private final JdbcTemplate jdbc;
    private final TenantIterator tenants;
    private final PicoAttendanceRules rules;

    @Value("${pico.attendance.processor.enabled:true}")
    private boolean enabled;

    public PicoAttendanceProcessor(JdbcTemplate jdbc, TenantIterator tenants, PicoAttendanceRules rules) {
        this.jdbc = jdbc;
        this.tenants = tenants;
        this.rules = rules;
    }

    @Scheduled(cron = "${pico.attendance.processor.cron:0 59 23 * * *}")
    public void processToday() {
        if (!enabled) return;
        LocalDate day = LocalDate.now();
        tenants.forEach(tenantId -> {
            try {
                int n = process(day);
                log.info("PicoAttendanceProcessor tenant {} {}: {} attendance rows", tenantId, day, n);
            } catch (Exception e) {
                log.warn("PicoAttendanceProcessor failed for tenant {}: {}", tenantId, e.toString());
            }
        });
    }

    /** Processes one day for TenantContext.get(); returns the number of attendance rows written. */
    public int process(LocalDate day) {
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> employees = jdbc.queryForList(
                "SELECT id, shift_code, holiday_list_id FROM m7_employee e WHERE e.tenant_id=? AND e.status='ACTIVE' " +
                        "AND NOT EXISTS (SELECT 1 FROM m7_attendance a WHERE a.tenant_id=e.tenant_id AND a.employee_id=e.id AND a.date_of=?)",
                tenantId, day);
        int written = 0;
        for (Map<String, Object> emp : employees) {
            try {
                if (processEmployee(tenantId, emp, day)) written++;
            } catch (Exception e) {
                log.warn("PicoAttendanceProcessor employee {} {}: {}", emp.get("id"), day, e.toString());
            }
        }
        return written;
    }

    private boolean processEmployee(Long tenantId, Map<String, Object> emp, LocalDate day) {
        Long employeeId = ((Number) emp.get("id")).longValue();
        List<Map<String, Object>> punches = jdbc.queryForList(
                "SELECT log_type, punch_time FROM m7_employee_checkin WHERE tenant_id=? AND employee_id=? " +
                        "AND punch_time >= ? AND punch_time < ? ORDER BY punch_time",
                tenantId, employeeId, day, day.plusDays(1));
        LocalDateTime firstIn = null, lastOut = null;
        for (Map<String, Object> p : punches) {
            Object raw = p.get("punch_time");
            LocalDateTime t = raw instanceof LocalDateTime ldt ? ldt : ((java.sql.Timestamp) raw).toLocalDateTime();
            if ("IN".equals(p.get("log_type")) && firstIn == null) firstIn = t;
            if ("OUT".equals(p.get("log_type"))) lastOut = t;
        }
        if (firstIn == null) return false;

        String shiftCode = rules.shiftFor(employeeId, (String) emp.get("shift_code"), day);
        Map<String, Object> shift = rules.shiftType(shiftCode);
        Long holidayListId = emp.get("holiday_list_id") == null ? null : ((Number) emp.get("holiday_list_id")).longValue();
        boolean holiday = rules.isHoliday(holidayListId, day);

        String status = "PRESENT";
        LocalTime fullDayBefore = time(shift, "full_day_before_time");
        if (!holiday && fullDayBefore != null && firstIn.isAfter(day.atTime(fullDayBefore))) status = "HALF_DAY";

        BigDecimal workedHours = lastOut != null && lastOut.isAfter(firstIn)
                ? hours(Duration.between(firstIn, lastOut)) : null;
        BigDecimal otHours = otHours(shift, day, lastOut);

        int inserted = jdbc.update(
                "INSERT IGNORE INTO m7_attendance (tenant_id, employee_id, shift_code, date_of, in_time, out_time, " +
                        "worked_hours, ot_hours, status, source) VALUES (?,?,?,?,?,?,?,?,?,'MOBILE')",
                tenantId, employeeId, shiftCode, day, Time.valueOf(firstIn.toLocalTime()),
                lastOut == null ? null : Time.valueOf(lastOut.toLocalTime()), workedHours, otHours, status);

        if (otHours != null) {
            BigDecimal multiplier = (BigDecimal) shift.get(holiday ? "holiday_ot_multiplier" : "ot_multiplier");
            BigDecimal amount = otHours.multiply(hourlyRate(tenantId, employeeId, day)).multiply(multiplier)
                    .setScale(2, RoundingMode.HALF_UP);
            jdbc.update(
                    "INSERT IGNORE INTO m7_overtime_entry (tenant_id, employee_id, attendance_date, shift_code, ot_hours, " +
                            "ot_amount, is_holiday) VALUES (?,?,?,?,?,?,?)",
                    tenantId, employeeId, day, shiftCode, otHours, amount, holiday ? 1 : 0);
        }
        return inserted > 0;
    }

    /** OT hours past the shift's ot_start_time, or null when below min_ot_minutes / no OT rule / no OUT punch. */
    private static BigDecimal otHours(Map<String, Object> shift, LocalDate day, LocalDateTime lastOut) {
        LocalTime otStart = time(shift, "ot_start_time");
        if (otStart == null || lastOut == null) return null;
        LocalDateTime otFrom = day.atTime(otStart);
        if (!lastOut.isAfter(otFrom)) return null;
        Duration ot = Duration.between(otFrom, lastOut);
        int minMinutes = ((Number) shift.get("min_ot_minutes")).intValue();
        return ot.toMinutes() >= minMinutes ? hours(ot) : null;
    }

    /**
     * (basic + DA) / 30 / 8 from the salary row effective on the day. ERPNext used
     * Salary Structure Assignment.base / 30 / 8 -- confirm with the client which Aria
     * components make up "base". 0 when the employee has no salary row.
     */
    private BigDecimal hourlyRate(Long tenantId, Long employeeId, LocalDate day) {
        List<BigDecimal> rows = jdbc.queryForList(
                "SELECT basic_monthly + da_monthly FROM m7_employee_salary WHERE tenant_id=? AND emp_id=? " +
                        "AND effective_from <= ? AND (effective_to IS NULL OR effective_to >= ?) ORDER BY effective_from DESC LIMIT 1",
                BigDecimal.class, tenantId, employeeId, day, day);
        if (rows.isEmpty() || rows.get(0) == null) return BigDecimal.ZERO;
        return rows.get(0).divide(BigDecimal.valueOf(240), 6, RoundingMode.HALF_UP);
    }

    private static LocalTime time(Map<String, Object> shift, String column) {
        if (shift == null) return null;
        Object v = shift.get(column);
        if (v instanceof Time t) return t.toLocalTime();
        if (v instanceof LocalTime t) return t;
        return v == null ? null : LocalTime.parse(v.toString());
    }

    private static BigDecimal hours(Duration d) {
        return BigDecimal.valueOf(d.toMinutes()).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
