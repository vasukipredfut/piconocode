package com.aria.m10.parking.service;

import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lorry parking bay in/out tracking, driven by a standalone Raspberry Pi
 * scanning a gate pass QR code (see {@code raspberry-pi-client/parking_bay_scanner.py}).
 * The device only ever submits a {@code gp_number} — IN vs OUT is derived
 * server-side: no open {@code dispatch_parking_bay_entry} row for this gate
 * pass -> this scan is IN (open one); an open row exists -> this scan is OUT
 * (close it). Same toggle convention as the existing workflow-driven
 * {@code ParkingBayEntryTransitionListener}, but reachable directly by a
 * tokenless device instead of only as a side effect of a workflow transition.
 *
 * <p>Debounced: a flaky reader double/triple-firing the same QR (or an
 * operator re-scanning by mistake) would otherwise toggle the state on every
 * repeat — scan-scan on a just-arrived lorry would immediately mark it OUT
 * again. A repeat scan of the same gate pass within
 * {@code earms.parking.scan.debounce-seconds} of its last event is ignored:
 * it returns the existing state (reason {@code DUPLICATE_SCAN_IGNORED}) with
 * no new row and no new audit entry, rather than toggling.
 */
@Service
public class ParkingBayScanService {

    private final JdbcTemplate jdbc;
    private final long debounceSeconds;

    public ParkingBayScanService(JdbcTemplate jdbc,
                                  @Value("${earms.parking.scan.debounce-seconds:10}") long debounceSeconds) {
        this.jdbc = jdbc;
        this.debounceSeconds = debounceSeconds;
    }

    @Transactional("psqlTransactionManager")
    public Map<String, Object> scan(String gpNumber) {
        if (gpNumber == null || gpNumber.isBlank()) {
            return response("ERROR", "gpNumber is required", gpNumber, null);
        }
        long tenantId = TenantContext.get();

        List<Map<String, Object>> gatePass = jdbc.queryForList(
                "SELECT id, vehicle_no, flow_variant_code, loading_type FROM gate_pass "
                        + "WHERE tenant_id = ? AND gp_number = ?",
                tenantId, gpNumber);
        if (gatePass.isEmpty()) {
            return response("REJECT", "GATE_PASS_NOT_FOUND", gpNumber, null);
        }
        Map<String, Object> pass = gatePass.get(0);
        Long gatePassId = ((Number) pass.get("id")).longValue();
        String vehicleNo = (String) pass.get("vehicle_no");

        List<Map<String, Object>> recent = jdbc.queryForList(
                "SELECT id, out_time, "
                        + "TIMESTAMPDIFF(SECOND, COALESCE(out_time, in_time), CURRENT_TIMESTAMP) AS seconds_since "
                        + "FROM dispatch_parking_bay_entry WHERE tenant_id = ? AND gate_pass_no = ? "
                        + "ORDER BY id DESC LIMIT 1",
                tenantId, gpNumber);
        if (!recent.isEmpty()) {
            long secondsSince = ((Number) recent.get(0).get("seconds_since")).longValue();
            if (secondsSince < debounceSeconds) {
                Long lastId = ((Number) recent.get(0).get("id")).longValue();
                Map<String, Object> lastEntry = jdbc.queryForMap(
                        "SELECT * FROM dispatch_parking_bay_entry WHERE id = ?", lastId);
                String priorOutcome = recent.get(0).get("out_time") == null ? "IN" : "OUT";
                return response(priorOutcome, "DUPLICATE_SCAN_IGNORED", gpNumber, lastEntry);
            }
        }

        List<Map<String, Object>> open = jdbc.queryForList(
                "SELECT id FROM dispatch_parking_bay_entry WHERE tenant_id = ? AND gate_pass_no = ? "
                        + "AND out_time IS NULL ORDER BY in_time DESC LIMIT 1 FOR UPDATE",
                tenantId, gpNumber);

        String outcome;
        Map<String, Object> entry;
        if (!open.isEmpty()) {
            Long entryId = ((Number) open.get(0).get("id")).longValue();
            jdbc.update("UPDATE dispatch_parking_bay_entry SET out_time = CURRENT_TIMESTAMP WHERE id = ?", entryId);
            entry = jdbc.queryForMap("SELECT * FROM dispatch_parking_bay_entry WHERE id = ?", entryId);
            outcome = "OUT";
        } else {
            jdbc.update(
                    "INSERT INTO dispatch_parking_bay_entry "
                            + "(tenant_id, gate_pass_no, vehicle_no, flow_variant_code, loading_type, in_time) "
                            + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                    tenantId, gpNumber, vehicleNo, pass.get("flow_variant_code"), pass.get("loading_type"));
            Long entryId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            entry = jdbc.queryForMap("SELECT * FROM dispatch_parking_bay_entry WHERE id = ?", entryId);
            outcome = "IN";
        }

        jdbc.update(
                "INSERT INTO m0_audit_log (tenant_id, ts, actor_id, action_kind, entity_code, entity_id, "
                        + "vehicle_no, gp_number, message) VALUES (?, CURRENT_TIMESTAMP, NULL, ?, 'GatePass', ?, ?, ?, ?)",
                tenantId, "PARKING_" + outcome, gatePassId, vehicleNo, gpNumber,
                "Parking bay " + outcome + " via gate pass scan");

        return response(outcome, null, gpNumber, entry);
    }

    private Map<String, Object> response(String outcome, String reason, String gpNumber, Map<String, Object> entry) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("outcome", outcome);
        out.put("reason", reason);
        out.put("gpNumber", gpNumber);
        out.put("parkingBayEntry", entry);
        return out;
    }
}
