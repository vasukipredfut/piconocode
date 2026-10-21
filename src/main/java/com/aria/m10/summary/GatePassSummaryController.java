package com.aria.m10.summary;

import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code GET /api/m10/gate-pass/{gpNumber}/summary} -- everything a gate pass went through, in one read-only response:
 * the gate pass and its workflow instance, the timeline (who moved it, when, through which page), what each page saved
 * (one section per page table), the tank rows of loading and unloading, the sample vehicles and the stock movements.
 *
 * <p>Read-only: nothing here writes. Each section is read on its own, so a table that does not exist on a given database
 * (a migration not applied yet) is listed under {@code unavailable} instead of failing the whole summary. A section is an
 * empty list when the gate pass never reached that page.
 *
 * <p>The summary carries a {@code journey}: the workflow events in order, each with who did it, when, the minutes since the step
 * before and -- through {@code page_submitter_map}, the page -> table map -- what that page saved (its {@code entry}). So one gate
 * pass reads as a story from creation to gate close. {@code GET /api/m10/gate-pass/history} does the same for every gate pass of
 * a vehicle or a transport (a few per page).
 *
 * <p>{@code GET /api/m10/gate-pass/search} finds the gate passes to open: by gate pass number, by vehicle number (whole or
 * part, dashes / spaces ignored), by transport name (whole or part), each optionally within a date range (the day the gate pass
 * was created). Newest first, paged. Also lists the distinct vehicles behind the result, with how many gate passes each had and
 * the last visit -- "every vehicle of this transport". Open one with {@code /{gpNumber}/summary}.
 *
 * <p>Every query is tenant-scoped and keyed on the gate pass's {@code workflow_instance_id}; table names come from the
 * fixed list below, never from the request.
 */
@RestController
@RequestMapping("/api/m10/gate-pass")
public class GatePassSummaryController {

    private static final Logger log = LoggerFactory.getLogger(GatePassSummaryController.class);

    /** Section name in the response -> table that holds it (one row, or a few, per workflow instance). */
    private static final Map<String, String> SECTIONS = new LinkedHashMap<>();
    static {
        SECTIONS.put("dispatchVehicle", "dispatch_vehicle_detail");
        SECTIONS.put("loadingAllocation", "loading_allocation_detail");
        SECTIONS.put("qcPreload", "qc_preload_detail");
        SECTIONS.put("weighment", "weighment_capture_detail");
        SECTIONS.put("qcFinalOutbound", "qc_final_outbound_detail");
        SECTIONS.put("managerQty", "manager_qty_approval_detail");
        SECTIONS.put("managerWeight", "manager_weight_approval_detail");
        SECTIONS.put("billing", "billing_entry");
        SECTIONS.put("qcOfficerFinalCheck", "qc_officer_final_check_detail");
        SECTIONS.put("dispatchOfficerVerification", "dispatch_officer_verification_detail");
        SECTIONS.put("documentVerification", "document_verification_detail");
        SECTIONS.put("closure", "gate_pass_closure");
        SECTIONS.put("loadingProgress", "loading_progress_detail");
        SECTIONS.put("loadingBays", "loading_progress_detail_bay");
        SECTIONS.put("unloadingProgress", "unloading_progress_detail");
        SECTIONS.put("unloadingBays", "unloading_progress_detail_bay");
        SECTIONS.put("sampleVehicles", "qc_sample_vehicle");
    }

    /** The stock movements the loading / unloading / sample flows write with the workflow instance id as {@code ref_id}. */
    private static final String MOVEMENT_REF_TYPES = "'TANKER','TANKER_UNLOAD','TANKER_LOAD','QC_SAMPLE','BAY','UNLOAD_BAY'";

    private final JdbcTemplate jdbc;

    public GatePassSummaryController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Most gate passes one call returns; ask for the next page for more. */
    private static final int MAX_PAGE_SIZE = 200;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false) String gpNumber,
            @RequestParam(required = false) String vehicleNo,
            @RequestParam(required = false) String transportName,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        boolean hasGp = gpNumber != null && !gpNumber.isBlank();
        boolean hasVehicle = vehicleNo != null && !vehicleNo.isBlank();
        boolean hasTransport = transportName != null && !transportName.isBlank();
        if (!hasGp && !hasVehicle && !hasTransport) {
            return ResponseEntity.badRequest().body(Map.of("error", "criteria_required",
                    "message", "Give a gpNumber, a vehicleNo or a transportName (a date range alone would list everything)."));
        }
        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = from == null || from.isBlank() ? null : LocalDate.parse(from.trim());
            toDate = to == null || to.isBlank() ? null : LocalDate.parse(to.trim());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "bad_date", "message", "from / to must be yyyy-MM-dd"));
        }
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            return ResponseEntity.badRequest().body(Map.of("error", "bad_range", "message", "to is before from"));
        }
        int pageNo = Math.max(page, 0);
        int pageSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        long tenantId = TenantContext.get();
        StringBuilder where = new StringBuilder(" WHERE gp.tenant_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(tenantId);
        if (hasGp) { where.append(" AND gp.gp_number = ?"); args.add(gpNumber.trim()); }
        if (hasVehicle) {
            where.append(" AND UPPER(REPLACE(REPLACE(gp.vehicle_no, '-', ''), ' ', '')) LIKE ?");
            args.add("%" + vehicleNo.trim().toUpperCase().replace("-", "").replace(" ", "") + "%");
        }
        if (hasTransport) {
            where.append(" AND COALESCE(NULLIF(gp.transport_name, ''), dvd.transport_name) LIKE ?");
            args.add("%" + transportName.trim() + "%");
        }
        if (fromDate != null) { where.append(" AND gp.created_at >= ?"); args.add(java.sql.Timestamp.valueOf(fromDate.atStartOfDay())); }
        if (toDate != null) { where.append(" AND gp.created_at < ?"); args.add(java.sql.Timestamp.valueOf(toDate.plusDays(1).atStartOfDay())); }

        String join = " FROM gate_pass gp LEFT JOIN dispatch_vehicle_detail dvd "
                + "ON dvd.workflow_instance_id = gp.workflow_instance_id AND dvd.tenant_id = gp.tenant_id";

        Map<String, Object> out = new LinkedHashMap<>();
        Long total = jdbc.queryForObject("SELECT COUNT(*)" + join + where, Long.class, args.toArray());
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(pageSize);
        pageArgs.add((long) pageNo * pageSize);
        out.put("content", jdbc.queryForList(
                "SELECT gp.id, gp.gp_number, gp.vehicle_no, gp.flow_variant_code, gp.status, gp.current_step, gp.workflow_instance_id, "
                        + "COALESCE(NULLIF(gp.transport_name, ''), dvd.transport_name) AS transport_name, "
                        + "COALESCE(NULLIF(gp.driver_name, ''), dvd.driver_name) AS driver_name, gp.created_at, gp.closed_at"
                        + join + where + " ORDER BY gp.created_at DESC, gp.id DESC LIMIT ? OFFSET ?",
                pageArgs.toArray()));
        out.put("vehicles", jdbc.queryForList(
                "SELECT gp.vehicle_no, COUNT(*) AS gate_pass_count, MAX(gp.created_at) AS last_visit" + join + where
                        + " GROUP BY gp.vehicle_no ORDER BY last_visit DESC", args.toArray()));
        out.put("totalElements", total == null ? 0L : total);
        out.put("number", pageNo);
        out.put("size", pageSize);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{gpNumber}/summary")
    public ResponseEntity<Map<String, Object>> summary(@PathVariable String gpNumber) {
        long tenantId = TenantContext.get();
        List<Map<String, Object>> gp = jdbc.queryForList(
                "SELECT * FROM gate_pass WHERE tenant_id = ? AND gp_number = ?", tenantId, gpNumber);
        if (gp.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "not_found", "message", "No gate pass " + gpNumber));
        }
        return ResponseEntity.ok(buildSummary(tenantId, gp.get(0)));
    }

    /** Most gate passes one history page carries (each one is a full summary). */
    private static final int MAX_HISTORY_SIZE = 20;

    /** The full summary of every gate pass of a vehicle / transport / gate pass number, within a date range -- a few per page. */
    @GetMapping("/history")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> history(
            @RequestParam(required = false) String gpNumber,
            @RequestParam(required = false) String vehicleNo,
            @RequestParam(required = false) String transportName,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        ResponseEntity<Map<String, Object>> found = search(gpNumber, vehicleNo, transportName, from, to, page, Math.min(Math.max(size, 1), MAX_HISTORY_SIZE));
        if (found.getStatusCode().isError() || found.getBody() == null) return found;
        long tenantId = TenantContext.get();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> row : (List<Map<String, Object>>) found.getBody().get("content")) {
            List<Map<String, Object>> gp = jdbc.queryForList(
                    "SELECT * FROM gate_pass WHERE tenant_id = ? AND gp_number = ?", tenantId, row.get("gp_number"));
            if (!gp.isEmpty()) items.add(buildSummary(tenantId, gp.get(0)));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("content", items);
        out.put("totalElements", found.getBody().get("totalElements"));
        out.put("number", found.getBody().get("number"));
        out.put("size", found.getBody().get("size"));
        return ResponseEntity.ok(out);
    }

    private Map<String, Object> buildSummary(long tenantId, Map<String, Object> gatePass) {
        Object instanceObj = gatePass.get("workflow_instance_id");
        Long instanceId = instanceObj instanceof Number n ? n.longValue() : null;

        Map<String, Object> out = new LinkedHashMap<>();
        List<String> unavailable = new ArrayList<>();
        out.put("gatePass", gatePass);

        if (instanceId == null) {
            // a gate pass saved without a flow variant has no workflow behind it yet
            out.put("workflow", null);
            out.put("timeline", List.of());
            out.put("journey", List.of());
            for (String name : SECTIONS.keySet()) out.put(name, List.of());
            out.put("stockMovements", List.of());
            out.put("unavailable", unavailable);
            return out;
        }

        out.put("workflow", first(read("workflow", unavailable,
                "SELECT id, workflow_code, workflow_version, current_state, status, assigned_role, business_ref, created_at, updated_at "
                        + "FROM workflow_instance WHERE tenant_id = ? AND id = ?", tenantId, instanceId)));
        List<Map<String, Object>> timeline = read("timeline", unavailable,
                "SELECT id, ts, kind, from_state, to_state, page_code, actor_user_id FROM workflow_event "
                        + "WHERE tenant_id = ? AND instance_id = ? ORDER BY ts, id", tenantId, instanceId);
        out.put("timeline", timeline);
        out.put("journey", journey(tenantId, instanceId, timeline, unavailable));

        for (Map.Entry<String, String> e : SECTIONS.entrySet()) {
            out.put(e.getKey(), read(e.getKey(), unavailable,
                    "SELECT * FROM " + e.getValue() + " WHERE tenant_id = ? AND workflow_instance_id = ? ORDER BY id",
                    tenantId, instanceId));
        }

        out.put("stockMovements", read("stockMovements", unavailable,
                "SELECT * FROM m5_stock_movement WHERE tenant_id = ? AND ref_id = ? AND ref_type IN (" + MOVEMENT_REF_TYPES + ") ORDER BY id",
                tenantId, instanceId));

        out.put("unavailable", unavailable);
        return out;
    }

    /** Identifiers read from the page -> table map must still look like plain names before they go into a query. */
    private static boolean safe(Object ident) {
        return ident instanceof String s && s.matches("[A-Za-z0-9_]+");
    }

    /**
     * The gate pass as a story: every workflow event in order -- when, from which state to which, through which page, by whom
     * ({@code performed_by_name} of what the page saved, else the user id), the minutes since the step before -- with what the
     * page saved ({@code entry}: its row(s) in the table {@code page_submitter_map} names for it).
     */
    private List<Map<String, Object>> journey(long tenantId, Long instanceId, List<Map<String, Object>> timeline, List<String> unavailable) {
        Map<String, String[]> pageTable = new HashMap<>();
        for (Map<String, Object> m : read("pageMap", unavailable, "SELECT page_code, table_name, key_column FROM page_submitter_map")) {
            if (m.get("page_code") != null && safe(m.get("table_name")) && safe(m.get("key_column"))) {
                pageTable.put((String) m.get("page_code"), new String[]{(String) m.get("table_name"), (String) m.get("key_column")});
            }
        }
        Map<String, List<Map<String, Object>>> entries = new HashMap<>();
        List<Map<String, Object>> steps = new ArrayList<>();
        Long previous = null;
        int n = 1;
        for (Map<String, Object> ev : timeline) {
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("step", n++);
            step.put("ts", ev.get("ts"));
            step.put("kind", ev.get("kind"));
            step.put("from_state", ev.get("from_state"));
            step.put("to_state", ev.get("to_state"));
            step.put("page_code", ev.get("page_code"));
            step.put("actor_user_id", ev.get("actor_user_id"));
            Long ms = millis(ev.get("ts"));
            step.put("minutes_since_previous", ms == null || previous == null ? null
                    : BigDecimal.valueOf(Math.max(0, ms - previous)).divide(BigDecimal.valueOf(60000), 1, java.math.RoundingMode.HALF_UP));
            if (ms != null) previous = ms;

            List<Map<String, Object>> entry = List.of();
            String[] where = ev.get("page_code") == null ? null : pageTable.get(String.valueOf(ev.get("page_code")));
            if (where != null) {
                entry = entries.computeIfAbsent(where[0] + "." + where[1], k -> read(where[0], unavailable,
                        "SELECT * FROM " + where[0] + " WHERE tenant_id = ? AND " + where[1] + " = ?", tenantId, instanceId));
            }
            step.put("performed_by_name", entry.isEmpty() ? null : entry.get(0).get("performed_by_name"));
            step.put("entry", entry);
            steps.add(step);
        }
        return steps;
    }

    private static Long millis(Object ts) {
        if (ts instanceof java.sql.Timestamp t) return t.getTime();
        if (ts instanceof java.util.Date d) return d.getTime();
        if (ts instanceof java.time.LocalDateTime l) return java.sql.Timestamp.valueOf(l).getTime();
        if (ts instanceof java.time.Instant i) return i.toEpochMilli();
        return null;
    }

    /** One section: its rows, or an empty list (and a note under {@code unavailable}) when it cannot be read. */
    private List<Map<String, Object>> read(String name, List<String> unavailable, String sql, Object... args) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
            return rows == null ? List.of() : rows;
        } catch (Exception ex) {
            log.warn("gate-pass summary: section {} unavailable: {}", name, ex.getMessage());
            unavailable.add(name);
            return List.of();
        }
    }

    private static Map<String, Object> first(List<Map<String, Object>> rows) {
        return rows.isEmpty() ? null : rows.get(0);
    }
}
