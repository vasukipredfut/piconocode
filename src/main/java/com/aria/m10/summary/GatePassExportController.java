package com.aria.m10.summary;

import com.aria.security.TenantContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code GET /api/m10/gate-pass/export.xlsx} -- the whole story of the gate passes of a gate pass number, a vehicle or a transport
 * (within a date range) as ONE Excel file. Sheets: {@code Info}, {@code Gate Passes} (one row each), {@code Journey} (every step: when,
 * which page, who, minutes since the step before, what the page saved), {@code Loading} and {@code Unloading} (the tank rows),
 * {@code Sample Vehicles} and {@code Stock} (the stock movements). Every sheet starts with the gate pass number, so the file filters and
 * pivots in Excel.
 *
 * <p>Filters: {@code gpNumber}, {@code vehicleNo} (whole or part, dashes / spaces / case ignored), {@code transportName} (whole or
 * part), {@code from} / {@code to} (the day the gate pass was created, yyyy-MM-dd). At least one of gate pass / vehicle / transport is
 * required. At most {@value #MAX_GATE_PASSES} gate passes (newest first); the Info sheet says when more matched.
 *
 * <p>Read-only; tenant-scoped; filter values are bind parameters. Table names come from fixed lists here, or -- for the page tables of the
 * Journey -- from {@code page_submitter_map} and only when they are plain names.
 */
@RestController
@RequestMapping("/api/m10/gate-pass")
public class GatePassExportController {

    static final int MAX_GATE_PASSES = 1000;
    private static final int IN_CHUNK = 500;
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String TRANSPORT = "COALESCE(NULLIF(gp.transport_name, ''), dvd.transport_name)";

    private static final String GATE_PASS_SELECT = "SELECT gp.id, gp.gp_number, gp.workflow_instance_id, gp.created_at, gp.closed_at, gp.vehicle_no, "
            + TRANSPORT + " AS transport_name, COALESCE(NULLIF(gp.driver_name, ''), dvd.driver_name) AS driver_name, gp.flow_variant_code, gp.status, "
            + "gp.current_step, w.w1_weight_kg, w.w2_weight_kg, w.w3_weight_kg, ABS(w.w1_weight_kg - w.w2_weight_kg) AS net_weight_kg, "
            + "lpd.processed_quantity AS loaded_qty, lpd.unloaded_quantity AS unloaded_qty, upd.processed_quantity AS unload_progress_qty, "
            + "mq.expected_weight_kg, mq.pmt_variance_pct AS variance, mq.manager_decision"
            + " FROM gate_pass gp"
            + " LEFT JOIN dispatch_vehicle_detail dvd ON dvd.workflow_instance_id = gp.workflow_instance_id AND dvd.tenant_id = gp.tenant_id"
            + " LEFT JOIN weighment_capture_detail w ON w.workflow_instance_id = gp.workflow_instance_id AND w.tenant_id = gp.tenant_id"
            + " LEFT JOIN loading_progress_detail lpd ON lpd.workflow_instance_id = gp.workflow_instance_id AND lpd.tenant_id = gp.tenant_id"
            + " LEFT JOIN unloading_progress_detail upd ON upd.workflow_instance_id = gp.workflow_instance_id AND upd.tenant_id = gp.tenant_id"
            + " LEFT JOIN manager_qty_approval_detail mq ON mq.workflow_instance_id = gp.workflow_instance_id AND mq.tenant_id = gp.tenant_id";

    private static final String[] GATE_PASS_COLS = {"gp_number", "created_at", "closed_at", "vehicle_no", "transport_name", "driver_name",
            "flow_variant_code", "status", "current_step", "w1_weight_kg", "w2_weight_kg", "w3_weight_kg", "net_weight_kg", "loaded_qty",
            "unloaded_qty", "unload_progress_qty", "expected_weight_kg", "variance", "manager_decision"};

    /** Sheet -> {table, columns of that table, order}. The gate pass number is added in front. */
    private static final Map<String, Object[]> DETAIL = new LinkedHashMap<>();
    static {
        DETAIL.put("Loading", new Object[]{"loading_progress_detail_bay", new String[]{"invoice_no", "route_id", "order_id", "sku_code", "sku_desc",
                "bay_name", "quantity", "processed_quantity", "unloaded_quantity", "status", "started_by_name", "weighed_by_name"}});
        DETAIL.put("Unloading", new Object[]{"unloading_progress_detail_bay", new String[]{"invoice_no", "route_id", "order_id", "sku_code", "sku_desc",
                "bay_name", "quantity", "processed_quantity", "loaded_back_quantity", "approved_status", "rebate_value", "diff_qty", "diff_reason",
                "plan_source", "status", "unloaded_by_name"}});
        DETAIL.put("Sample Vehicles", new Object[]{"qc_sample_vehicle", new String[]{"vehicle_no", "invoice_no", "sku_code", "sample_location",
                "sample_qty", "w1_weight_kg", "w2_weight_kg", "unloaded_qty", "status", "w1_by_name", "w2_by_name"}});
    }
    private static final String[] STOCK_COLS = {"kind", "item_code", "lot_no", "from_bin", "to_bin", "qty", "uom", "ref_type", "ref_number", "created_at"};
    private static final String MOVEMENT_REF_TYPES = "'TANKER','TANKER_UNLOAD','TANKER_LOAD','QC_SAMPLE','BAY','UNLOAD_BAY'";
    private static final Set<String> HIDDEN_ENTRY_COLS = Set.of("id", "tenant_id", "workflow_instance_id", "created_at", "updated_at", "submitted_by_id");

    private final JdbcTemplate jdbc;

    public GatePassExportController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/export.xlsx")
    public ResponseEntity<?> export(
            @RequestParam(required = false) String gpNumber,
            @RequestParam(required = false) String vehicleNo,
            @RequestParam(required = false) String transportName,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        if (blank(gpNumber) && blank(vehicleNo) && blank(transportName)) {
            return bad("criteria_required", "Give a gpNumber, a vehicleNo or a transportName (a date range alone would list everything).");
        }
        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = blank(from) ? null : LocalDate.parse(from.trim());
            toDate = blank(to) ? null : LocalDate.parse(to.trim());
        } catch (DateTimeParseException e) {
            return bad("bad_date", "from / to must be yyyy-MM-dd");
        }
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) return bad("bad_range", "to is before from");

        long tenantId = TenantContext.get();
        StringBuilder where = new StringBuilder(" WHERE gp.tenant_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(tenantId);
        if (!blank(gpNumber)) { where.append(" AND gp.gp_number = ?"); args.add(gpNumber.trim()); }
        if (!blank(vehicleNo)) {
            where.append(" AND UPPER(REPLACE(REPLACE(gp.vehicle_no, '-', ''), ' ', '')) LIKE ?");
            args.add("%" + vehicleNo.trim().toUpperCase().replace("-", "").replace(" ", "") + "%");
        }
        if (!blank(transportName)) { where.append(" AND ").append(TRANSPORT).append(" LIKE ?"); args.add("%" + transportName.trim() + "%"); }
        if (fromDate != null) { where.append(" AND gp.created_at >= ?"); args.add(Timestamp.valueOf(fromDate.atStartOfDay())); }
        if (toDate != null) { where.append(" AND gp.created_at < ?"); args.add(Timestamp.valueOf(toDate.plusDays(1).atStartOfDay())); }
        args.add(MAX_GATE_PASSES + 1);   // one more than the cap, to know whether there were more

        List<Map<String, Object>> gatePasses = jdbc.queryForList(
                GATE_PASS_SELECT + where + " ORDER BY gp.created_at DESC, gp.id DESC LIMIT ?", args.toArray());
        boolean truncated = gatePasses.size() > MAX_GATE_PASSES;
        if (truncated) gatePasses = new ArrayList<>(gatePasses.subList(0, MAX_GATE_PASSES));

        Map<Long, String> gpByInstance = new LinkedHashMap<>();
        for (Map<String, Object> g : gatePasses) {
            if (g.get("workflow_instance_id") instanceof Number n) gpByInstance.put(n.longValue(), String.valueOf(g.get("gp_number")));
        }
        List<Long> instanceIds = new ArrayList<>(gpByInstance.keySet());

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            CellStyle head = headerStyle(wb);

            Sheet info = wb.createSheet("Info");
            String[][] infoRows = {
                    {"Generated", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())},
                    {"Gate pass", nz(gpNumber)}, {"Vehicle", nz(vehicleNo)}, {"Transport", nz(transportName)},
                    {"From", nz(from)}, {"To", nz(to)},
                    {"Gate passes in this file", String.valueOf(gatePasses.size())},
                    {"More matched than the limit", truncated ? "YES -- only the newest " + MAX_GATE_PASSES + " are here, narrow the date range" : "no"}};
            for (int i = 0; i < infoRows.length; i++) {
                Row r = info.createRow(i);
                r.createCell(0).setCellValue(infoRows[i][0]);
                r.createCell(1).setCellValue(infoRows[i][1]);
            }
            info.setColumnWidth(0, 30 * 256);
            info.setColumnWidth(1, 60 * 256);

            writeTable(wb, head, "Gate Passes", GATE_PASS_COLS, gatePasses);

            writeTable(wb, head, "Journey", new String[]{"gp_number", "step", "ts", "from_state", "to_state", "page_code", "performed_by_name",
                    "actor_user_id", "minutes_since_previous", "data"}, journeyRows(tenantId, instanceIds, gpByInstance));

            for (Map.Entry<String, Object[]> d : DETAIL.entrySet()) {
                String table = (String) d.getValue()[0];
                String[] cols = (String[]) d.getValue()[1];
                writeTable(wb, head, d.getKey(), withGp(cols), byInstance(tenantId, instanceIds, gpByInstance, table, "workflow_instance_id", "id", ""));
            }
            writeTable(wb, head, "Stock", withGp(STOCK_COLS), byInstance(tenantId, instanceIds, gpByInstance, "m5_stock_movement", "ref_id", "id",
                    " AND ref_type IN (" + MOVEMENT_REF_TYPES + ")"));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gate-pass-report.xlsx\"")
                    .contentType(MediaType.parseMediaType(XLSX))
                    .body(out.toByteArray());
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "export_failed", "message", String.valueOf(e.getMessage())));
        }
    }

    private static String[] withGp(String[] cols) {
        String[] out = new String[cols.length + 1];
        out[0] = "gp_number";
        System.arraycopy(cols, 0, out, 1, cols.length);
        return out;
    }

    /** Rows of {@code table} for the instances, each carrying its gate pass number; an unreadable table gives no rows. */
    private List<Map<String, Object>> byInstance(long tenantId, List<Long> instanceIds, Map<Long, String> gpByInstance, String table,
                                                 String keyCol, String orderCol, String extraWhere) {
        List<Map<String, Object>> out = new ArrayList<>();
        try {
            for (List<Long> chunk : chunks(instanceIds)) {
                List<Object> args = new ArrayList<>();
                args.add(tenantId);
                args.addAll(chunk);
                for (Map<String, Object> row : jdbc.queryForList("SELECT * FROM " + table + " WHERE tenant_id = ? AND " + keyCol + " IN (" + marks(chunk.size())
                        + ")" + extraWhere + " ORDER BY " + keyCol + ", " + orderCol, args.toArray())) {
                    Map<String, Object> r = new LinkedHashMap<>(row);
                    r.put("gp_number", gpByInstance.get(((Number) row.get(keyCol)).longValue()));
                    out.add(r);
                }
            }
        } catch (Exception ex) {
            // a table not there yet (migration pending): its sheet is simply empty
        }
        return out;
    }

    /** Every workflow step of the gate passes, with who / when / minutes since the step before and what the page saved. */
    private List<Map<String, Object>> journeyRows(long tenantId, List<Long> instanceIds, Map<Long, String> gpByInstance) {
        List<Map<String, Object>> out = new ArrayList<>();
        try {
            Map<String, String[]> pageTable = new HashMap<>();
            for (Map<String, Object> m : jdbc.queryForList("SELECT page_code, table_name, key_column FROM page_submitter_map")) {
                if (m.get("page_code") != null && plain(m.get("table_name")) && plain(m.get("key_column"))) {
                    pageTable.put((String) m.get("page_code"), new String[]{(String) m.get("table_name"), (String) m.get("key_column")});
                }
            }
            List<Map<String, Object>> events = new ArrayList<>();
            for (List<Long> chunk : chunks(instanceIds)) {
                List<Object> args = new ArrayList<>();
                args.add(tenantId);
                args.addAll(chunk);
                events.addAll(jdbc.queryForList("SELECT instance_id, ts, from_state, to_state, page_code, actor_user_id FROM workflow_event "
                        + "WHERE tenant_id = ? AND instance_id IN (" + marks(chunk.size()) + ") ORDER BY instance_id, ts, id", args.toArray()));
            }
            // what each page saved: one read per page table, keyed by "table.instance"
            Map<String, Map<Long, Map<String, Object>>> saved = new HashMap<>();
            for (String[] tk : new LinkedHashSet<>(java.util.Arrays.asList(pageTable.values().toArray(new String[0][])).stream()
                    .map(a -> new String[]{a[0], a[1]}).toList())) {
                String key = tk[0] + "." + tk[1];
                if (saved.containsKey(key)) continue;
                Map<Long, Map<String, Object>> rows = new HashMap<>();
                try {
                    for (List<Long> chunk : chunks(instanceIds)) {
                        List<Object> args = new ArrayList<>();
                        args.add(tenantId);
                        args.addAll(chunk);
                        for (Map<String, Object> row : jdbc.queryForList("SELECT * FROM " + tk[0] + " WHERE tenant_id = ? AND " + tk[1] + " IN ("
                                + marks(chunk.size()) + ")", args.toArray())) {
                            if (row.get(tk[1]) instanceof Number n) rows.putIfAbsent(n.longValue(), row);
                        }
                    }
                } catch (Exception ex) {
                    // unreadable page table: its steps just have no data
                }
                saved.put(key, rows);
            }
            Long lastInstance = null;
            Long previousMs = null;
            int step = 0;
            for (Map<String, Object> ev : events) {
                Long instance = ((Number) ev.get("instance_id")).longValue();
                if (!instance.equals(lastInstance)) { lastInstance = instance; previousMs = null; step = 0; }
                step++;
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("gp_number", gpByInstance.get(instance));
                r.put("step", step);
                r.put("ts", ev.get("ts"));
                r.put("from_state", ev.get("from_state"));
                r.put("to_state", ev.get("to_state"));
                r.put("page_code", ev.get("page_code"));
                r.put("actor_user_id", ev.get("actor_user_id"));
                Long ms = ev.get("ts") instanceof Timestamp t ? t.getTime() : null;
                r.put("minutes_since_previous", ms == null || previousMs == null ? null
                        : BigDecimal.valueOf(Math.max(0, ms - previousMs)).divide(BigDecimal.valueOf(60000), 1, java.math.RoundingMode.HALF_UP));
                if (ms != null) previousMs = ms;
                String[] where = ev.get("page_code") == null ? null : pageTable.get(String.valueOf(ev.get("page_code")));
                Map<String, Object> entry = where == null ? null : saved.getOrDefault(where[0] + "." + where[1], Map.of()).get(instance);
                r.put("performed_by_name", entry == null ? null : entry.get("performed_by_name"));
                r.put("data", entry == null ? null : flatten(entry));
                out.add(r);
            }
        } catch (Exception ex) {
            // the journey sheet stays as far as it got
        }
        return out;
    }

    /** {@code col=value; col=value} of what a page saved: no ids / timestamps, no empty values, capped. */
    private static String flatten(Map<String, Object> entry) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : entry.entrySet()) {
            if (HIDDEN_ENTRY_COLS.contains(e.getKey()) || e.getValue() == null || String.valueOf(e.getValue()).isBlank()) continue;
            if (sb.length() > 0) sb.append("; ");
            sb.append(e.getKey()).append('=').append(e.getValue() instanceof BigDecimal b ? b.stripTrailingZeros().toPlainString() : e.getValue());
            if (sb.length() > 2000) { sb.setLength(2000); sb.append("..."); break; }
        }
        return sb.toString();
    }

    private static void writeTable(XSSFWorkbook wb, CellStyle head, String name, String[] cols, List<Map<String, Object>> rows) {
        Sheet sheet = wb.createSheet(name);
        Row h = sheet.createRow(0);
        for (int c = 0; c < cols.length; c++) {
            Cell cell = h.createCell(c);
            cell.setCellValue(cols[c]);
            cell.setCellStyle(head);
            sheet.setColumnWidth(c, ("data".equals(cols[c]) ? 80 : 18) * 256);
        }
        sheet.createFreezePane(0, 1);
        int r = 1;
        for (Map<String, Object> row : rows) {
            Row xr = sheet.createRow(r++);
            for (int c = 0; c < cols.length; c++) setCell(xr.createCell(c), row.get(cols[c]));
        }
    }

    private static void setCell(Cell cell, Object v) {
        if (v == null) return;
        if (v instanceof Number n) { cell.setCellValue(n.doubleValue()); return; }
        if (v instanceof Boolean b) { cell.setCellValue(b); return; }
        String s = v instanceof Timestamp t ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t) : String.valueOf(v);
        // a text cell that starts like a formula must not be run by Excel
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        cell.setCellValue(s.length() > 32000 ? s.substring(0, 32000) : s);
    }

    private static CellStyle headerStyle(XSSFWorkbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        CellStyle st = wb.createCellStyle();
        st.setFont(f);
        return st;
    }

    private static List<List<Long>> chunks(List<Long> ids) {
        List<List<Long>> out = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += IN_CHUNK) out.add(ids.subList(i, Math.min(ids.size(), i + IN_CHUNK)));
        return out;
    }

    private static String marks(int n) {
        return String.join(",", java.util.Collections.nCopies(n, "?"));
    }

    private static boolean plain(Object ident) { return ident instanceof String s && s.matches("[A-Za-z0-9_]+"); }
    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static String nz(String s) { return s == null ? "" : s; }

    private static ResponseEntity<Map<String, Object>> bad(String error, String message) {
        return ResponseEntity.badRequest().body(Map.of("error", error, "message", message));
    }
}
