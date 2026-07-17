package com.aria.mes.shift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Auto-rotates shift supervisors based on rows in {@code m5_shift_schedule}.
 *
 * <p>Cron fires every 5 minutes by default; checks whether the current
 * wall-clock minute crosses a shift boundary (start_time within the last
 * 5 minutes). When it does, writes a row into {@code m5_shift_handover}
 * with snapshot counts of open job-cards / open dispatches and rotates
 * the supervisor pointer.
 *
 * <p>Disabled via {@code earms.m5.shifts.autoRotate.enabled=false} — also
 * disabled by default in tests/CI to avoid timing surprises.
 */
@Component
public class ShiftRotationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ShiftRotationScheduler.class);

    private final JdbcTemplate jdbc;
    private final com.aria.security.TenantIterator tenants;

    @Value("${earms.m5.shifts.autoRotate.enabled:false}")
    private boolean enabled;

    public ShiftRotationScheduler(JdbcTemplate jdbc, com.aria.security.TenantIterator tenants) {
        this.jdbc = jdbc;
        this.tenants = tenants;
    }

    /**
     * Every 5 minutes; check for shift boundary crossings — for every tenant.
     *
     * <p>A @Scheduled method has no HTTP request, so TenantContextFilter never
     * runs and there is no tenant context to inherit. This job previously
     * hardcoded {@code tenant_id=1} because that was the only thing available —
     * which meant that the moment a second factory existed, its shift handover
     * would simply never be written. No error, no row, and the job reporting
     * success every five minutes while doing the first tenant's work forever.
     */
    @Scheduled(fixedDelayString = "${earms.m5.shifts.autoRotate.intervalMs:300000}", initialDelay = 60_000)
    public void rotateIfNeeded() {
        if (!enabled) return;
        tenants.forEach(tenantId -> {
            try {
                rotate(LocalDateTime.now());
            } catch (Exception e) {
                log.warn("ShiftRotationScheduler failed for tenant {}: {}", tenantId, e.toString());
            }
        });
    }

    /** Test-friendly entrypoint. Returns true if a handover was written. */
    public boolean rotate(LocalDateTime now) {
        List<Map<String, Object>> candidates = findShiftsStartingNow(now);
        if (candidates.isEmpty()) return false;
        Map<String, Object> incoming = candidates.get(0);
        List<Map<String, Object>> previous = findPreviousShift(now, (String) incoming.get("shift_code"));
        String outgoingShift = previous.isEmpty() ? "?" : (String) previous.get(0).get("shift_code");
        Object outgoingSup = previous.isEmpty() ? null : previous.get(0).get("supervisor_id");

        Long tenantId = com.aria.security.TenantContext.get();
        Integer openJc = safeCount("SELECT COUNT(*) FROM m5_job_card WHERE tenant_id=? AND status NOT IN ('CLOSED','QC_REJECTED')", tenantId);
        Integer openDsp = safeCount("SELECT COUNT(*) FROM m5_fg_dispatch WHERE tenant_id=? AND status NOT IN ('CLOSED','DISPATCHED')", tenantId);

        jdbc.update(
                "INSERT INTO m5_shift_handover (tenant_id, handed_off_at, outgoing_shift, incoming_shift, " +
                        "outgoing_supervisor_id, incoming_supervisor_id, open_jc_count, open_dispatch_count, notes) " +
                        "VALUES (?,?,?,?,?,?,?,?,?)",
                tenantId, java.sql.Timestamp.valueOf(now), outgoingShift, incoming.get("shift_code"),
                outgoingSup, incoming.get("supervisor_id"),
                openJc == null ? 0 : openJc,
                openDsp == null ? 0 : openDsp,
                "Auto-rotated by ShiftRotationScheduler");
        log.info("Shift auto-rotated: {} -> {} (open JCs: {}, open dispatches: {})",
                outgoingShift, incoming.get("shift_code"), openJc, openDsp);
        return true;
    }

    /** Hook for tests — returns shifts whose start_time falls within last 5 minutes. */
    protected List<Map<String, Object>> findShiftsStartingNow(LocalDateTime now) {
        return jdbc.queryForList(
                "SELECT id, shift_code, start_time, supervisor_id FROM m5_shift_schedule " +
                        "WHERE tenant_id=? AND shift_date=? AND " +
                        "TIMEDIFF(?, start_time) BETWEEN '00:00:00' AND '00:05:00' " +
                        "ORDER BY start_time DESC LIMIT 1",
                com.aria.security.TenantContext.get(), now.toLocalDate(), now.toLocalTime());
    }

    /** Hook for tests — returns previous shift (the one currently ending). */
    protected List<Map<String, Object>> findPreviousShift(LocalDateTime now, String incomingShift) {
        return jdbc.queryForList(
                "SELECT shift_code, supervisor_id FROM m5_shift_schedule " +
                        "WHERE tenant_id=? AND shift_date=? AND end_time <= ? AND shift_code <> ? " +
                        "ORDER BY end_time DESC LIMIT 1",
                com.aria.security.TenantContext.get(), now.toLocalDate(), now.toLocalTime(), incomingShift);
    }

    private Integer safeCount(String sql, Object... args) {
        try { return jdbc.queryForObject(sql, Integer.class, args); }
        catch (Exception e) {
            // Swallowing here means a broken query degrades to a 0 count on the
            // handover rather than an exception — so log it, or a bind mistake
            // looks exactly like "no open job cards".
            log.warn("safeCount failed ({}): {}", sql, e.toString());
            return null;
        }
    }
}
