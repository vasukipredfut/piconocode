package com.aria.reminder;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 *  GET /api/m0/reminders/today           — everything due in the next 7 days
 *  GET /api/m0/reminders/this-week       — within 30 days
 *  GET /api/m0/reminders/this-quarter    — within 90 days
 *  GET /api/m0/reminders/by-category     — grouped buckets
 *
 * Pulls from m5_license_renewal, m7_statutory_return_filing, m5_maintenance_schedule,
 * m5_accident_log (open CAPA), m6_ar_open_item (43B(h) approaching), etc.
 */
@RestController
@RequestMapping("/api/m0/reminders")
public class ComplianceReminderController {

    private final JdbcTemplate jdbc;

    public ComplianceReminderController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/by-category")
    public Map<String, Object> byCategory() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("licences", licencesDue(60));
        out.put("statutoryReturns", statReturnsDue(30));
        out.put("maintenance", maintenanceDue(7));
        out.put("openAccidents", openAccidents());
        out.put("msme43bhBreaches", msmeBreaches());
        out.put("statutoryOverdue", statReturnsOverdue());
        return out;
    }

    @GetMapping("/today")
    public Map<String, Object> today() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("licences", licencesDue(7));
        out.put("statutoryReturns", statReturnsDue(7));
        out.put("maintenance", maintenanceDue(0));
        return out;
    }

    @GetMapping("/this-week")
    public Map<String, Object> thisWeek() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("licences", licencesDue(30));
        out.put("statutoryReturns", statReturnsDue(14));
        out.put("maintenance", maintenanceDue(7));
        return out;
    }

    @GetMapping("/this-quarter")
    public Map<String, Object> thisQuarter() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("licences", licencesDue(90));
        out.put("statutoryReturns", statReturnsDue(45));
        out.put("maintenance", maintenanceDue(30));
        return out;
    }

    // ====== helpers ======
    private List<Map<String, Object>> licencesDue(int withinDays) {
        return jdbc.queryForList(
            "SELECT licence_code, licence_type, licence_no, issuing_authority, " +
            "valid_to, DATEDIFF(valid_to, CURRENT_DATE) AS days_to_expiry, status " +
            "FROM m5_license_renewal " +
            "WHERE status='ACTIVE' AND DATEDIFF(valid_to, CURRENT_DATE) <= ? " +
            "ORDER BY valid_to", withinDays);
    }

    private List<Map<String, Object>> statReturnsDue(int withinDays) {
        return jdbc.queryForList(
            "SELECT return_code, period, due_date, status, " +
            "DATEDIFF(due_date, CURRENT_DATE) AS days_to_due " +
            "FROM m7_statutory_return_filing " +
            "WHERE status NOT IN ('FILED','N_A') AND DATEDIFF(due_date, CURRENT_DATE) <= ? " +
            "ORDER BY due_date", withinDays);
    }

    private List<Map<String, Object>> statReturnsOverdue() {
        return jdbc.queryForList(
            "SELECT return_code, period, due_date, status, " +
            "DATEDIFF(CURRENT_DATE, due_date) AS days_overdue " +
            "FROM m7_statutory_return_filing " +
            "WHERE status NOT IN ('FILED','N_A') AND due_date < CURRENT_DATE " +
            "ORDER BY due_date");
    }

    private List<Map<String, Object>> maintenanceDue(int withinDays) {
        try {
            return jdbc.queryForList(
                "SELECT asset_code, asset_name, maintenance_type, task_description, next_due_date, " +
                "DATEDIFF(next_due_date, CURRENT_DATE) AS days_to_due " +
                "FROM m5_maintenance_schedule " +
                "WHERE status='ACTIVE' AND DATEDIFF(next_due_date, CURRENT_DATE) <= ? " +
                "ORDER BY next_due_date", withinDays);
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private List<Map<String, Object>> openAccidents() {
        try {
            return jdbc.queryForList(
                "SELECT accident_no, occurred_at, injured_name, severity, status, " +
                "DATEDIFF(CURRENT_DATE, occurred_at) AS days_open " +
                "FROM m5_accident_log WHERE status NOT IN ('CLOSED') ORDER BY occurred_at");
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private List<Map<String, Object>> msmeBreaches() {
        try {
            return jdbc.queryForList(
                "SELECT party_code, party_name, doc_no, doc_date, due_date, outstanding, " +
                "msme_43bh_due_date, DATEDIFF(CURRENT_DATE, msme_43bh_due_date) AS days_breached " +
                "FROM m6_ar_open_item " +
                "WHERE party_type='VENDOR' AND msme_43bh_breached=1 ORDER BY msme_43bh_due_date");
        } catch (Exception e) { return Collections.emptyList(); }
    }
}
