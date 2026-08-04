package com.aria.mes.orderdispatch;

import com.aria.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real CSV import for the OrderApp order-vs-dispatch reconciliation export.
 * See {@code V7__order_dispatch_detail.sql} for the full column-mapping
 * rationale (several source headers repeat — once at order level, once at
 * SKU-line level — hence positional, not name-based, column mapping).
 *
 * <p>{@code POST /api/m5/order-dispatch-detail/import} — multipart
 * {@code file} (csv/tsv), bulk-inserts one row per data line into
 * {@code m5_order_dispatch_detail}: a point-in-time reconciliation snapshot,
 * not master data, so there's still no upsert (no row is ever updated). A
 * row is instead skipped outright — not inserted, not updated — when its
 * (order_id, route_id, sku_code) already exists for this tenant ({@link
 * #isAlreadyExists}), so re-uploading the same export doesn't keep
 * duplicating identical lines; counted separately as {@code rowsSkippedDuplicate}.
 *
 * <p>{@code GET /api/m5/order-dispatch-detail/import-page} — a
 * self-contained HTML+JS upload form. This exists because the page-JSON
 * Renderer has no convention for a file FormField to POST multipart to a
 * custom endpoint (only the generic "save" action, which persists plain
 * JSON scalar fields). Since auth here is header-only (no {@code ?token=}
 * fallback), this endpoint is itself behind the same JWT filter as any API
 * call — reaching it therefore already proves the caller's bearer token,
 * which this method echoes back into the returned page so its own upload
 * button can call {@code /import} with it. Open it the same way the
 * frontend already opens Form16/payslip: fetch with the Authorization
 * header in an authenticated tab, then {@code document.write} the body into
 * a new tab.
 */
@RestController
@RequestMapping("/api/m5/order-dispatch-detail")
public class OrderDispatchDetailImportController {

    private static final DateTimeFormatter SOURCE_DATE =
        DateTimeFormatter.ofPattern("dd-MM-yyyy/hh:mm:ss/a", Locale.ENGLISH);

    private static final Set<String> WRITE_ROLES = Set.of("dispatch", "manager", "tenantadmin");

    private final JdbcTemplate jdbc;
    private final Map<Long, String> roleNameCache = new ConcurrentHashMap<>();

    public OrderDispatchDetailImportController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file) {
        ResponseEntity<?> denied = checkWriteRole();
        if (denied != null) return denied;
        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "no file"));

        Long tenantId = TenantContext.get();
        String sourceFile = file.getOriginalFilename();
        List<String> errors = new ArrayList<>();
        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.mark(8192);
            String firstLine = reader.readLine();
            if (firstLine == null)
                return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "empty file"));
            long tabs = firstLine.chars().filter(c -> c == '\t').count();
            long commas = firstLine.chars().filter(c -> c == ',').count();
            char delimiter = tabs > commas ? '\t' : ',';
            reader.reset();

            CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(delimiter)
                .withIgnoreEmptyLines(true)
                .withTrim(true);

            try (CSVParser parser = new CSVParser(reader, format)) {
                int rowNum = 0;
                boolean headerSkipped = false;
                for (CSVRecord r : parser) {
                    rowNum++;
                    if (!headerSkipped) { headerSkipped = true; continue; }
                    try {
                        // Positional columns per insertRow(): order_id=1, sku_code=14, route_id=24.
                        if (isAlreadyExists(tenantId, str(r, 1), str(r, 24), str(r, 14))) {
                            skipped++;
                            continue;
                        }
                        insertRow(tenantId, r, sourceFile);
                        imported++;
                    } catch (Exception rowEx) {
                        errors.add("Row " + rowNum + ": " + rowEx.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "error", "Could not read file: " + e.getMessage()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("rowsImported", imported);
        body.put("rowsSkippedDuplicate", skipped);
        body.put("rowsFailed", errors.size());
        body.put("errors", errors);
        return ResponseEntity.ok(body);
    }

    /** True if a row for this (order_id, route_id, sku_code) was already imported for this tenant. */
    private boolean isAlreadyExists(Long tenantId, String orderId, String routeId, String skuCode) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM m5_order_dispatch_detail WHERE tenant_id = ?
                AND order_id = ? AND route_id = ? AND sku_code = ? """, Integer.class,
            tenantId, orderId, routeId, skuCode);
        return count != null && count > 0;
    }

    private void insertRow(Long tenantId, CSVRecord r, String sourceFile) {
        jdbc.update(
            "INSERT INTO m5_order_dispatch_detail (" +
            "tenant_id, customer_email, order_id, rd_code, rd_desc, created_on, submitted_on, updated_on, " +
            "order_qty, cancel_qty, dispatch_qty, remaining_qty, status, detained, sku_group, sku_code, sku_desc, " +
            "sku_active_on, staged_on, staged_rate, order_rate, stock_qty, line_order_qty, line_cancel_qty, " +
            "line_remaining_qty, route_id, line_dispatch_qty, assign_qty, dispatched_on, vehicle_no, driver_name, " +
            "driver_phone, dispatch_by, load_wgt, dispatch_wgt, reason, source_file) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            tenantId,
            str(r, 0), str(r, 1), str(r, 2), str(r, 3),
            dateVal(r, 4), dateVal(r, 5), dateVal(r, 6),
            intVal(r, 7), intVal(r, 8), intVal(r, 9), intVal(r, 10),
            str(r, 11), boolVal(r, 12), str(r, 13), str(r, 14), str(r, 15),
            dateVal(r, 16), dateVal(r, 17), decVal(r, 18), decVal(r, 19), intVal(r, 20),
            intVal(r, 21), intVal(r, 22), intVal(r, 23), str(r, 24), intVal(r, 25), intVal(r, 26),
            dateVal(r, 27), str(r, 28), str(r, 29), str(r, 30), str(r, 31),
            decVal(r, 32), decVal(r, 33), str(r, 34), sourceFile);
    }

    private static String str(CSVRecord r, int i) {
        if (i >= r.size()) return null;
        String v = r.get(i);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static Integer intVal(CSVRecord r, int i) {
        String v = str(r, i);
        if (v == null) return null;
        try { return Integer.valueOf(v.replaceAll("[,\\s]", "")); }
        catch (NumberFormatException e) { return null; }
    }

    private static BigDecimal decVal(CSVRecord r, int i) {
        String v = str(r, i);
        if (v == null) return null;
        try { return new BigDecimal(v.replaceAll("[,\\s]", "")); }
        catch (NumberFormatException e) { return null; }
    }

    private static Boolean boolVal(CSVRecord r, int i) {
        String v = str(r, i);
        if (v == null) return null;
        return Boolean.parseBoolean(v.trim());
    }

    private static LocalDateTime dateVal(CSVRecord r, int i) {
        String v = str(r, i);
        if (v == null) return null;
        try { return LocalDateTime.parse(v, SOURCE_DATE); }
        catch (Exception e) { return null; }
    }

    // ---- upload page --------------------------------------------------

    @GetMapping(value = "/import-page", produces = MediaType.TEXT_HTML_VALUE)
    public String importPage(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "";
        String tokenJs = token.replace("\\", "\\\\").replace("'", "\\'");
        return "<!doctype html><html><head><meta charset='utf-8'>" +
            "<title>Import Order Dispatch Detail</title>" +
            "<style>body{font-family:sans-serif;max-width:640px;margin:40px auto;padding:0 16px}" +
            "button{padding:8px 16px;font-size:14px;cursor:pointer}" +
            "#result{margin-top:16px;white-space:pre-wrap;font-family:monospace;font-size:12px}" +
            ".err{color:#b00020}.ok{color:#0a7d28}</style></head><body>" +
            "<h2>Import Order Dispatch Detail</h2>" +
            "<p>Select the OrderApp order-vs-dispatch CSV/TSV export (35 columns).</p>" +
            "<input type='file' id='f' accept='.csv,.tsv,.txt'/>" +
            "<button id='go'>Upload</button>" +
            "<div id='result'></div>" +
            "<script>" +
            "const TOKEN='" + tokenJs + "';" +
            "document.getElementById('go').onclick = async function() {" +
            "  const fileInput = document.getElementById('f');" +
            "  const out = document.getElementById('result');" +
            "  if (!fileInput.files.length) { out.textContent = 'Pick a file first.'; return; }" +
            "  const fd = new FormData(); fd.append('file', fileInput.files[0]);" +
            "  out.textContent = 'Uploading...';" +
            "  try {" +
            "    const resp = await fetch('/api/m5/order-dispatch-detail/import', {" +
            "      method: 'POST', headers: { 'Authorization': 'Bearer ' + TOKEN }, body: fd });" +
            "    const json = await resp.json();" +
            "    out.className = resp.ok ? 'ok' : 'err';" +
            "    out.textContent = JSON.stringify(json, null, 2);" +
            "  } catch (e) { out.className = 'err'; out.textContent = 'Upload failed: ' + e; }" +
            "};" +
            "</script></body></html>";
    }

    // ---- RBAC (mirrors GenericEntityController's role-name resolution) ----

    private ResponseEntity<?> checkWriteRole() {
        String roleName = currentRoleName();
        if (roleName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("ok", false, "error", "Not authenticated"));
        }
        if (!WRITE_ROLES.contains(roleName.toLowerCase(Locale.ROOT))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("ok", false, "error", "Role '" + roleName + "' cannot import Order Dispatch Detail"));
        }
        return null;
    }

    private String currentRoleName() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getAuthorities() == null) return null;
        for (GrantedAuthority ga : a.getAuthorities()) {
            String s = ga.getAuthority();
            if (s == null) continue;
            try {
                long id = Long.parseLong(s.trim());
                String name = roleNameCache.computeIfAbsent(id, this::loadRoleName);
                if (name != null) return name;
            } catch (NumberFormatException nfe) {
                return s;
            }
        }
        return null;
    }

    private String loadRoleName(Long id) {
        try {
            return jdbc.queryForObject("SELECT name FROM role WHERE id=?", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
