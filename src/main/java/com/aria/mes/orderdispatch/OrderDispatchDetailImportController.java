package com.aria.mes.orderdispatch;

import com.aria.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
 * Import for the OrderApp order-vs-dispatch reconciliation export. Accepts
 * CSV/TSV, Excel (.xlsx/.xls), and PDF, all funneled through a common
 * {@code List<String>} row representation (see {@link #parseRows}) so
 * {@link #insertRow} and its column-value helpers work identically
 * regardless of source format. See {@code V7__order_dispatch_detail.sql}
 * for the full column-mapping rationale (several source headers repeat —
 * once at order level, once at SKU-line level — hence positional, not
 * name-based, column mapping).
 *
 * <p>PDF rows are recovered from extracted text by splitting on runs of 2+
 * spaces, which only works for text-based PDFs whose table export preserved
 * wide column gaps (e.g. a print-to-PDF of the same tabular export). It
 * cannot handle scanned/image PDFs (no OCR here) or PDFs with tight
 * single-space column gaps — such rows will simply fail per-row validation
 * (e.g. missing assign_qty) and show up in {@code errors}, same as any other
 * malformed row.
 *
 * <p>{@code POST /api/m5/order-dispatch-detail/import} — multipart
 * {@code file} (csv/tsv/xlsx/xls/pdf), bulk-inserts one row per data line
 * into {@code m5_order_dispatch_detail}: a point-in-time reconciliation
 * snapshot.
 * Re-uploading for a route that's already been imported doesn't duplicate
 * lines: for every distinct {@code route_id} seen in the file, the first row
 * for that route triggers a delete of that route's existing still-{@code
 * active} rows for this tenant (see {@code vehicle_status}, stamped "active"
 * on every import and flipped to "inactive" by {@link #gateOut}) before any
 * new rows for the route are inserted — rows already gated out are historical
 * and left alone. Deleted-row count is reported as {@code rowsReplaced}.
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
        List<List<String>> rows;
        try {
            rows = parseRows(file);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "error", "Could not read file: " + e.getMessage()));
        }
        if (rows.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "empty file"));

        List<String> errors = new ArrayList<>();
        Set<String> clearedRoutes = new HashSet<>();
        int imported = 0;
        int replaced = 0;

        int rowNum = 0;
        boolean headerSkipped = false;
        for (List<String> r : rows) {
            rowNum++;
            if (!headerSkipped) { headerSkipped = true; continue; }
            try {
                // assign_qty is column 26 per insertRow()'s positional mapping.
                // Validated before the route_id delete below runs, so a bad
                // row fails without first wiping that route's existing data.
                Integer assignQty = intVal(r, 26);
                if (assignQty == null || assignQty == 0) {
                    throw new IllegalArgumentException("assign_qty value required (null, empty, or 0 is not allowed)");
                }
                // route_id is column 24 per insertRow()'s positional mapping.
                String routeId = str(r, 24);
                if (routeId != null && clearedRoutes.add(routeId)) {
                    replaced += jdbc.update(
                        "DELETE FROM m5_order_dispatch_detail " +
                        "WHERE tenant_id = ? AND route_id = ? AND vehicle_status = 'active'",
                        tenantId, routeId);
                }
                insertRow(tenantId, r, sourceFile);
                imported++;
            } catch (Exception rowEx) {
                errors.add("Row " + rowNum + ": " + rowEx.getMessage());
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("rowsImported", imported);
        body.put("rowsReplaced", replaced);
        body.put("rowsFailed", errors.size());
        body.put("errors", errors);
        return ResponseEntity.ok(body);
    }

    /**
     * Dispatches to a format-specific parser by file extension and returns
     * every row (header included — the caller skips row 1) as a list of
     * column strings, so downstream code never needs to know which format
     * the file came from.
     */
    private List<List<String>> parseRows(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        String ext = (name == null) ? "" : name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        switch (ext) {
            case "xlsx":
            case "xls":
                return parseExcelRows(file);
            case "pdf":
                return parsePdfRows(file);
            case "csv":
            case "tsv":
            case "txt":
                return parseCsvRows(file);
            default:
                throw new IllegalArgumentException(
                    "Unsupported file type '" + ext + "'. Supported: csv, tsv, txt, xlsx, xls, pdf");
        }
    }

    private List<List<String>> parseCsvRows(MultipartFile file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.mark(8192);
            String firstLine = reader.readLine();
            if (firstLine == null) return rows;
            long tabs = firstLine.chars().filter(c -> c == '\t').count();
            long commas = firstLine.chars().filter(c -> c == ',').count();
            char delimiter = tabs > commas ? '\t' : ',';
            reader.reset();

            CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(delimiter)
                .withIgnoreEmptyLines(true)
                .withTrim(true);

            try (CSVParser parser = new CSVParser(reader, format)) {
                for (CSVRecord r : parser) {
                    List<String> cols = new ArrayList<>(r.size());
                    for (int i = 0; i < r.size(); i++) cols.add(r.get(i));
                    rows.add(cols);
                }
            }
        }
        return rows;
    }

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private List<List<String>> parseExcelRows(MultipartFile file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                int lastCol = row.getLastCellNum();
                if (lastCol < 0) continue;
                List<String> cols = new ArrayList<>(lastCol);
                boolean anyValue = false;
                for (int c = 0; c < lastCol; c++) {
                    String v = cellToString(row.getCell(c));
                    if (!v.isBlank()) anyValue = true;
                    cols.add(v);
                }
                if (anyValue) rows.add(cols);
            }
        }
        return rows;
    }

    private static String cellToString(Cell cell) {
        if (cell == null) return "";
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) type = cell.getCachedFormulaResultType();
        if (type == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return SOURCE_DATE.format(cell.getLocalDateTimeCellValue());
        }
        if (type == CellType.NUMERIC) {
            double d = cell.getNumericCellValue();
            if (d == Math.rint(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
            return String.valueOf(d);
        }
        if (type == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    /**
     * PDF has no native table structure, so rows are recovered by splitting
     * extracted text on runs of 2+ spaces — see the class javadoc for when
     * this does and doesn't work.
     */
    private List<List<String>> parsePdfRows(MultipartFile file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(doc);
            for (String line : text.split("\\r?\\n")) {
                if (line.isBlank()) continue;
                String[] cols = line.trim().split("\\s{2,}");
                rows.add(Arrays.asList(cols));
            }
        }
        return rows;
    }

    /**
     * {@code POST /api/m5/order-dispatch-detail/gate-out} — {@code {"vehicleNo": "..."}}.
     * Flips every "active" row for this vehicle to "inactive": once it has
     * gated out, the order data imported for it has been fully consumed.
     * Matches by {@code vehicle_no} alone (no route_id/order_id needed) —
     * there is no existing FK from a gate pass/dispatch record to this table,
     * so vehicle_no is the one field both sides already share.
     */
    @PostMapping("/gate-out")
    public ResponseEntity<?> gateOut(@RequestBody Map<String, Object> body) {
        ResponseEntity<?> denied = checkWriteRole();
        if (denied != null) return denied;
        String vehicleNo = body == null ? null : str(body.get("vehicleNo"));
        if (vehicleNo == null || vehicleNo.isBlank())
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "vehicleNo required"));

        Long tenantId = TenantContext.get();
        int updated = jdbc.update(
            "UPDATE m5_order_dispatch_detail SET vehicle_status = 'inactive' " +
            "WHERE tenant_id = ? AND vehicle_no = ? AND vehicle_status = 'active'",
            tenantId, vehicleNo);

        return ResponseEntity.ok(Map.of("ok", true, "vehicleNo", vehicleNo, "rowsUpdated", updated));
    }

    private static String str(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * vehicle_status (separate from the pre-existing status column, which
     * already carries its own Dispatched/Assigned All/Pending meaning — see
     * V7's list-page filter) is always stamped "active" on import.
     * {@link #gateOut} is what later flips it to "inactive", once the
     * vehicle it's tied to has gated out.
     */
    private void insertRow(Long tenantId, List<String> r, String sourceFile) {
        jdbc.update(
            "INSERT INTO m5_order_dispatch_detail (" +
            "tenant_id, customer_email, order_id, rd_code, rd_desc, created_on, submitted_on, updated_on, " +
            "order_qty, cancel_qty, dispatch_qty, remaining_qty, status, vehicle_status, detained, sku_group, sku_code, sku_desc, " +
            "sku_active_on, staged_on, staged_rate, order_rate, stock_qty, line_order_qty, line_cancel_qty, " +
            "line_remaining_qty, route_id, line_dispatch_qty, assign_qty, dispatched_on, vehicle_no, driver_name, " +
            "driver_phone, dispatch_by, load_wgt, dispatch_wgt, reason, source_file) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            tenantId,
            str(r, 0), str(r, 1), str(r, 2), str(r, 3),
            dateVal(r, 4), dateVal(r, 5), dateVal(r, 6),
            intVal(r, 7), intVal(r, 8), intVal(r, 9), intVal(r, 10),
            str(r, 11), "active", boolVal(r, 12), str(r, 13), str(r, 14), str(r, 15),
            dateVal(r, 16), dateVal(r, 17), decVal(r, 18), decVal(r, 19), intVal(r, 20),
            intVal(r, 21), intVal(r, 22), intVal(r, 23), str(r, 24), intVal(r, 25), intVal(r, 26),
            dateVal(r, 27), str(r, 28), str(r, 29), str(r, 30), str(r, 31),
            decVal(r, 32), decVal(r, 33), str(r, 34), sourceFile);
    }

    private static String str(List<String> r, int i) {
        if (i >= r.size()) return null;
        String v = r.get(i);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static Integer intVal(List<String> r, int i) {
        String v = str(r, i);
        if (v == null) return null;
        try { return Integer.valueOf(v.replaceAll("[,\\s]", "")); }
        catch (NumberFormatException e) { return null; }
    }

    private static BigDecimal decVal(List<String> r, int i) {
        String v = str(r, i);
        if (v == null) return null;
        try { return new BigDecimal(v.replaceAll("[,\\s]", "")); }
        catch (NumberFormatException e) { return null; }
    }

    private static Boolean boolVal(List<String> r, int i) {
        String v = str(r, i);
        if (v == null) return null;
        return Boolean.parseBoolean(v.trim());
    }

    private static LocalDateTime dateVal(List<String> r, int i) {
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
            "<p>Select the OrderApp order-vs-dispatch export (35 columns) — CSV, TSV, Excel, or PDF.</p>" +
            "<input type='file' id='f' accept='.csv,.tsv,.txt,.xlsx,.xls,.pdf'/>" +
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
        // SuperAdmin bypasses every write-role check, same convention as
        // PageDefinitionController#SUPER_ROLE and GenericEntityController#checkRole.
        if ("SuperAdmin".equalsIgnoreCase(roleName)) {
            return null;
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
