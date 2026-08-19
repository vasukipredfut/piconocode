package com.aria.mes.orderdispatch;

import com.aria.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
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
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * new rows for the route are inserted. Deleted-row count is reported as
 * {@code rowsReplaced}.
 *
 * <p>The replace is blocked entirely — no delete, no insert, every row for
 * that {@code route_id} in the file rejected into {@code errors} — when
 * either of these holds (see {@link #blockReplaceReason}):
 * <ul>
 *   <li>Loading has already started for the route: a row exists in either
 *   {@code loading_progress_detail} (written at WEIGHMENT_1 -&gt;
 *   LOADING_IN_PROGRESS, before any bay is started) or
 *   {@code loading_progress_detail_bay} (written once "Start Bay" runs,
 *   -&gt; BAY_ASSIGNED) for this {@code route_id}.</li>
 *   <li>The route already has rows in {@code m5_order_dispatch_detail} but
 *   none are still {@code vehicle_status = 'active'} — the vehicle has
 *   already gated out, so there's nothing live left to replace.</li>
 * </ul>
 * A brand-new route (no existing rows at all) is never blocked by the
 * second rule — it's a first-time import, not a replace.
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

    // ---- printed truck/route order-sheet PDF (see parseTruckOrderSheetRows) ----
    private static final DateTimeFormatter TRUCK_SHEET_DATE =
        DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH);
    private static final Pattern TRUCK_HEADER = Pattern.compile("Truck No:\\s*(\\S+)\\s*\\(([^)]+)\\)");
    private static final Pattern DRIVER_HEADER = Pattern.compile("Driver Details:\\s*(\\S*)\\s*/\\s*(\\S*)");
    private static final Pattern ORDER_START = Pattern.compile("ORD(\\d+)\\s*/\\s*(.+)");
    private static final Pattern ORDER_GST_LINE = Pattern.compile(
        "GST:(\\S*)\\s+Contact No:\\s*(\\S*)\\s+Created:\\s*(\\d{2}/\\d{2}/\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\s*[AP]M)");
    private static final Pattern ORDER_LINES_LINE = Pattern.compile(
        "Lines:\\s*\\d+\\s+Qty:\\s*(\\d+)\\s+Wgt:\\s*([\\d.]+)\\s+Amt:\\s*[\\d.]+\\s+Submited:\\s*" +
        "(\\d{2}/\\d{2}/\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\s*[AP]M)");
    // No leading line-number anchor: that "No" column index isn't stored anywhere
    // downstream, and OCR unreliably misreads it (e.g. "06"->"0 O", "07"->"o7",
    // "01"->"1" with a dropped leading zero) — anchoring on it caused a failed
    // match to either silently drop the whole line or merge it into the
    // previous row's description, losing real qty/rate data. Detecting a line
    // item purely by "ends in qty, rate, weight" is robust to garbage
    // (checkbox-glyph noise, a mangled index) in whatever precedes it.
    private static final Pattern SKU_LINE = Pattern.compile("^(.+?)\\s+(\\d+)\\s+([\\d.]+)\\s+([\\d.]+)$");

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
        Map<String, String> blockedRoutes = new HashMap<>();
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
                if (routeId != null) {
                    if (!clearedRoutes.contains(routeId) && !blockedRoutes.containsKey(routeId)) {
                        String blockReason = blockReplaceReason(tenantId, routeId);
                        if (blockReason != null) {
                            blockedRoutes.put(routeId, blockReason);
                        } else {
                            clearedRoutes.add(routeId);
                            replaced += jdbc.update(
                                "DELETE FROM m5_order_dispatch_detail " +
                                "WHERE tenant_id = ? AND route_id = ? AND vehicle_status = 'active'",
                                tenantId, routeId);
                        }
                    }
                    String blockReason = blockedRoutes.get(routeId);
                    if (blockReason != null) {
                        throw new IllegalArgumentException("Route " + routeId + " cannot be replaced — " + blockReason);
                    }
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
     * PDF has no native table structure. Two shapes are supported:
     * <ul>
     *   <li>A print-to-PDF of the flat 35-column export (the original,
     *   narrower case): rows are recovered by splitting extracted text on
     *   runs of 2+ spaces — see the class javadoc for when this does and
     *   doesn't work.</li>
     *   <li>The printed per-truck route/order sheet (Truck No / Driver
     *   Details header, then one block per order with GST/Contact/Created/
     *   Submitted and a No/Check/SKU/L-Qty/Rate/Weight line-item table) —
     *   detected by the presence of "Truck No:" + "Scheduled On:" and
     *   handed to {@link #parseTruckOrderSheetRows}.</li>
     * </ul>
     */
    private List<List<String>> parsePdfRows(MultipartFile file) throws IOException {
        String text;
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            text = stripper.getText(doc);
            if (text.isBlank()) {
                // No text layer at all — some report/print-driver pipelines draw
                // every glyph as a raw vector path (no font, no Tj/TJ operators),
                // which reads identically to a scanned page from an extraction
                // standpoint. Fall back to OCR of the rendered page images.
                text = ocrText(doc);
            }
            // Some report engines use U+00A0 (non-breaking space) rather than a
            // plain space for layout gaps (e.g. right-aligning "Scheduled On:").
            // Java's \s is ASCII-only, so left as-is this silently breaks every
            // regex boundary that spans one — normalize every Unicode space
            // separator to a plain space so \s/\s* behave as intended everywhere
            // below (parseTruckOrderSheetRows and the flat-format line splitter).
            text = text.replaceAll("\\p{Zs}", " ");
        }
        if (text.contains("Truck No:") && text.contains("Scheduled On:")) {
            return parseTruckOrderSheetRows(text);
        }
        List<List<String>> rows = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            if (line.isBlank()) continue;
            String[] cols = line.trim().split("\\s{2,}");
            rows.add(Arrays.asList(cols));
        }
        return rows;
    }

    // Common tessdata locations, checked in order, when TESSDATA_PREFIX isn't
    // set — covers the Windows UB-Mannheim installer (this dev machine) and
    // the package-manager install locations on Debian/Ubuntu and RHEL/Amazon
    // Linux (production is Linux), so a fresh deploy works without also
    // having to remember to set an env var.
    private static final String[] TESSDATA_FALLBACK_PATHS = {
        "/usr/share/tesseract-ocr/5/tessdata",
        "/usr/share/tesseract-ocr/4.00/tessdata",
        "/usr/share/tessdata",
        "/usr/local/share/tessdata",
        "C:/Program Files/Tesseract-OCR/tessdata",
    };

    /**
     * Renders each page to a high-DPI image and runs Tesseract over it.
     * Requires a system Tesseract install — this is the one part of the
     * importer that depends on something outside the JVM/Maven build. The
     * tessdata directory is resolved from {@code TESSDATA_PREFIX} if set,
     * otherwise the first existing path in {@link #TESSDATA_FALLBACK_PATHS}.
     *
     * <p>OCR can misread digits in the rate/qty/weight columns. The only
     * safety net here is the existing required-assign_qty check in
     * {@link #importCsv}, which catches a misread that lands on that one
     * column (blank/zero/non-numeric) — a misread that still parses as some
     * other valid-looking number is not caught. Treat OCR-sourced imports as
     * needing a human glance at the result.
     */
    private String ocrText(PDDocument doc) throws IOException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(resolveTessdataPath());
        PDFRenderer renderer = new PDFRenderer(doc);
        StringBuilder all = new StringBuilder();
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            BufferedImage image = renderer.renderImageWithDPI(i, 300, ImageType.RGB);
            try {
                all.append(tesseract.doOCR(image)).append('\n');
            } catch (TesseractException e) {
                throw new IOException("OCR failed on page " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return all.toString();
    }

    private static String resolveTessdataPath() throws IOException {
        String envPath = System.getenv("TESSDATA_PREFIX");
        if (envPath != null && !envPath.isBlank()) return envPath;
        for (String candidate : TESSDATA_FALLBACK_PATHS) {
            if (new java.io.File(candidate).isDirectory()) return candidate;
        }
        throw new IOException("Tesseract tessdata directory not found — install tesseract-ocr "
            + "(e.g. 'apt install tesseract-ocr') or set the TESSDATA_PREFIX environment variable");
    }

    /**
     * Parses the printed per-truck route/order sheet into the same
     * 35-column row shape {@link #insertRow} expects, so validation and
     * insert are unchanged regardless of source format. Fields with no
     * direct column match — GST No, Contact No, the order block's Amt
     * total, and the blank Supervisor/Load Weight/Reason boxes (hand-filled
     * after printing, and always empty on the uploaded copy) — are read but
     * intentionally not persisted, since {@code m5_order_dispatch_detail}
     * has no column for them and they aren't consumed by the loading/
     * dispatch workflow.
     *
     * <p>Mapping: Truck No's prefix -&gt; vehicle_no, its parenthesised code
     * -&gt; route_id; Driver Details' "name / phone" -&gt; driver_name /
     * driver_phone; each order block's "ORD..." code -&gt; order_id, its
     * customer/destination text -&gt; rd_desc, Created/Submited -&gt;
     * created_on / submitted_on, its "Qty: NNN" -&gt; order_qty and
     * "Wgt: NNN.NN" -&gt; dispatch_wgt (both order-level totals, repeated
     * on every one of that order's SKU-line rows, same denormalization the
     * flat OrderApp export already uses per V7's column-mapping rationale);
     * each line item's SKU Description -&gt; sku_desc, Rate -&gt;
     * order_rate, L/Qty -&gt; assign_qty (the field {@link #importCsv}
     * already requires non-null/non-zero, since it's literally the
     * quantity to load for that line).
     *
     * <p>The header block (Truck No/Driver Details/Total Orders row) and the
     * "No Check SKU Description..." column header repeat on every page —
     * skipped as noise rather than mistaken for order data or a wrapped
     * description line.
     */
    private List<List<String>> parseTruckOrderSheetRows(String text) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(new String[35])); // synthetic header row, skipped by importCsv

        Matcher truckM = TRUCK_HEADER.matcher(text);
        String vehicleNo = null;
        String routeId = null;
        if (truckM.find()) {
            vehicleNo = truckM.group(1);
            routeId = truckM.group(2);
        }
        Matcher driverM = DRIVER_HEADER.matcher(text);
        String driverName = null;
        String driverPhone = null;
        if (driverM.find()) {
            driverName = driverM.group(1);
            driverPhone = driverM.group(2);
        }

        String orderId = null;
        StringBuilder rdDesc = null;
        String createdOn = null;
        String submittedOn = null;
        String orderQty = null;
        String orderWgt = null;
        List<String> lastRow = null;

        for (String raw : text.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.contains("Truck No:") || line.contains("Driver Details:")
                    || line.contains("Total Orders:") || line.contains("SKU Description")) {
                continue; // repeats on every page — not order data
            }

            Matcher os = ORDER_START.matcher(line);
            if (os.find()) {
                orderId = "ORD" + os.group(1);
                rdDesc = new StringBuilder(os.group(2).trim());
                createdOn = null;
                submittedOn = null;
                orderQty = null;
                orderWgt = null;
                lastRow = null;
                continue;
            }
            Matcher gst = ORDER_GST_LINE.matcher(line);
            if (gst.find()) {
                createdOn = toSourceDate(gst.group(3));
                continue;
            }
            Matcher lin = ORDER_LINES_LINE.matcher(line);
            if (lin.find()) {
                orderQty = lin.group(1);
                orderWgt = lin.group(2);
                submittedOn = toSourceDate(lin.group(3));
                continue;
            }
            if (line.startsWith("Total")) {
                lastRow = null; // end of this order's line items
                continue;
            }
            Matcher sku = SKU_LINE.matcher(line);
            if (sku.find() && orderId != null) {
                String[] cols = new String[35];
                cols[1] = orderId;
                cols[3] = rdDesc == null ? null : squeeze(rdDesc.toString());
                cols[4] = createdOn;
                cols[5] = submittedOn;
                cols[7] = orderQty; // order_qty: the order block's own "Qty: NNN" total, repeated per SKU line
                cols[15] = stripLeadingNoise(squeeze(sku.group(1)));
                cols[19] = sku.group(3);
                cols[24] = routeId;
                cols[26] = sku.group(2);
                cols[28] = vehicleNo;
                cols[29] = driverName;
                cols[30] = driverPhone;
                cols[33] = orderWgt; // dispatch_wgt: the order block's own "Wgt: NNN.NN" total, repeated per SKU line
                lastRow = Arrays.asList(cols);
                rows.add(lastRow);
                continue;
            }
            // wrapped continuation text: order description (before its GST line) or a SKU description
            if (rdDesc != null && createdOn == null) {
                rdDesc.append(' ').append(line);
            } else if (lastRow != null && lastRow.get(15) != null) {
                lastRow.set(15, squeeze(lastRow.get(15) + " " + line));
            }
        }

        // The printed sheet only gives a free-text SKU Description, never a code —
        // resolve sku_code by fuzzy-matching it against m5_sku_master.sku_name (see
        // bestSkuMatch). Done once against the final, fully-assembled descriptions
        // (after any wrapped-continuation text has been appended above), not
        // per-row during the scan. When a match is found, sku_desc is replaced with
        // the master's own clean name — strictly better than the OCR'd text, which
        // can still carry a stray glyph beyond what stripLeadingNoise catches, or
        // (rarely) have OCR-scrambled word order that the master name doesn't.
        List<String[]> skuCandidates = loadActiveSkuMaster();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.get(15) != null) {
                String[] match = bestSkuMatch(row.get(15), skuCandidates);
                if (match != null) {
                    row.set(14, match[0]);
                    row.set(15, cleanMasterName(match[1]));
                }
            }
        }
        return rows;
    }

    /**
     * Drops leading tokens that are pure-digit (the printed "No" column index) or
     * length <=2 (checkbox-glyph OCR noise, e.g. "Oj", "0 O", "[J") — but never past
     * the first token that looks like a real word, so the actual description is
     * left untouched. Every real SKU description in the master catalog starts with
     * a word of length >=3 (REF/DAILY/RSFO/GULAS/...), so this is safe in practice;
     * always leaves at least one token even if everything looks like noise.
     */
    private static String stripLeadingNoise(String desc) {
        if (desc == null || desc.isEmpty()) return desc;
        String[] tokens = desc.split(" ");
        int start = 0;
        while (start < tokens.length - 1 && isNoiseToken(tokens[start])) {
            start++;
        }
        return String.join(" ", Arrays.asList(tokens).subList(start, tokens.length));
    }

    private static boolean isNoiseToken(String token) {
        if (token.isEmpty()) return true;
        if (token.length() <= 2) return true;
        for (int i = 0; i < token.length(); i++) {
            if (!Character.isDigit(token.charAt(i))) return false;
        }
        return true; // all digits, any length — the printed index, never part of a real description
    }

    /** {@code "SKU1461 (REF SUNFLOWER OIL 15 LTR TIN (ROYAL))"} -&gt; {@code "REF SUNFLOWER OIL 15 LTR TIN (ROYAL)"}. */
    private static String cleanMasterName(String skuName) {
        if (skuName == null) return null;
        String s = skuName.replaceFirst("(?i)^\\s*SKU\\S*\\s*", "").trim();
        if (s.startsWith("(") && s.endsWith(")")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    private List<String[]> loadActiveSkuMaster() {
        return jdbc.query(
            "SELECT sku_code, sku_name FROM m5_sku_master WHERE tenant_id = ? AND status = 'ACTIVE'",
            (rs, i) -> new String[]{rs.getString("sku_code"), rs.getString("sku_name")},
            TenantContext.get());
    }

    // Calibrated against the 20 real SKU lines from ATTUR LINE.pdf: every genuine
    // match scored >=0.714, while the 3 lines with no real master entry (an unseeded
    // candy product, an unseeded empty-carton SKU, and one whose only overlap with
    // an unrelated product was generic packaging words like "500g POUCH - 10Kg BAG")
    // topped out at 0.556. 0.65 sits in the gap. Lower this only after re-checking
    // against real data — false positives here mislabel a dispatch line's product.
    private static final double SKU_MATCH_MIN_SCORE = 0.65;

    /**
     * Fuzzy-matches an OCR'd SKU description against {@code m5_sku_master.sku_name}
     * (e.g. {@code "SKU1461 (REF SUNFLOWER OIL 15 LTR TIN (ROYAL))"}) by token-set
     * (Jaccard) overlap, since exact matching never survives OCR noise (garbled
     * leading index/checkbox glyphs) or the sheet's own minor wording differences
     * (e.g. missing parens around "ROYAL"). Returns null rather than guess when
     * nothing clears {@link #SKU_MATCH_MIN_SCORE} — a wrong sku_code silently
     * mislabels the dispatch line, which is worse than leaving it unresolved.
     */
    private static String[] bestSkuMatch(String skuDesc, List<String[]> candidates) {
        Set<String> target = skuNameTokens(skuDesc);
        if (target.isEmpty()) return null;
        String[] best = null;
        double bestScore = 0;
        for (String[] candidate : candidates) {
            // sku_name embeds its own code as a prefix, e.g. "SKU1461 (...)" — strip
            // it so it doesn't count as a spurious token match against anything.
            String name = candidate[1] == null ? "" : candidate[1].replaceFirst("(?i)^\\s*SKU\\S*\\s*", "");
            Set<String> nameTokens = skuNameTokens(name);
            if (nameTokens.isEmpty()) continue;
            Set<String> intersection = new HashSet<>(nameTokens);
            intersection.retainAll(target);
            if (intersection.size() < 2) continue; // require at least 2 shared real words
            Set<String> union = new HashSet<>(nameTokens);
            union.addAll(target);
            double score = (double) intersection.size() / union.size();
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return bestScore >= SKU_MATCH_MIN_SCORE ? best : null;
    }

    /** Upper-cases, strips punctuation, and drops single-character tokens (checkbox/OCR noise). */
    private static Set<String> skuNameTokens(String raw) {
        if (raw == null) return Set.of();
        Set<String> out = new HashSet<>();
        for (String w : raw.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").split(" ")) {
            if (w.length() >= 2) out.add(w);
        }
        return out;
    }

    /** Collapses runs of internal whitespace (e.g. wide PDF column gaps) to a single space. */
    private static String squeeze(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }

    /** {@code dd/MM/yyyy hh:mm:ss a} (as printed) -&gt; {@code dd-MM-yyyy/hh:mm:ss/a} (as {@link #dateVal} expects). */
    private static String toSourceDate(String printed) {
        if (printed == null) return null;
        try {
            LocalDateTime dt = LocalDateTime.parse(printed.trim().replaceAll("\\s+", " "), TRUCK_SHEET_DATE);
            return SOURCE_DATE.format(dt);
        } catch (Exception e) {
            return null;
        }
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

    /**
     * Returns null if the route is safe to replace (new route, or has an
     * existing {@code vehicle_status = 'active'} row and loading hasn't
     * started), otherwise a human-readable reason the replace is blocked.
     * See the class javadoc for the two block conditions.
     */
    private String blockReplaceReason(Long tenantId, String routeId) {
        // loading_progress_detail (parent, one row per workflow instance) is
        // written at WEIGHMENT_1 -> LOADING_IN_PROGRESS, before "Start Bay" is
        // even clicked; loading_progress_detail_bay (child) only gets rows once
        // Start Bay runs (-> BAY_ASSIGNED). Checking only the child table would
        // miss a route that's already LOADING_IN_PROGRESS but not yet bay-assigned.
        Integer loadingStarted = jdbc.queryForObject(
            "SELECT " +
            "(SELECT COUNT(1) FROM loading_progress_detail WHERE tenant_id = ? AND route_id = ?) + " +
            "(SELECT COUNT(1) FROM loading_progress_detail_bay WHERE tenant_id = ? AND route_id = ?)",
            Integer.class, tenantId, routeId, tenantId, routeId);
        if (loadingStarted != null && loadingStarted > 0) {
            return "loading has already started for this route";
        }
        Integer existingRows = jdbc.queryForObject(
            "SELECT COUNT(1) FROM m5_order_dispatch_detail WHERE tenant_id = ? AND route_id = ?",
            Integer.class, tenantId, routeId);
        if (existingRows != null && existingRows > 0) {
            Integer activeRows = jdbc.queryForObject(
                "SELECT COUNT(1) FROM m5_order_dispatch_detail WHERE tenant_id = ? AND route_id = ? AND vehicle_status = 'active'",
                Integer.class, tenantId, routeId);
            if (activeRows == null || activeRows == 0) {
                return "vehicle for this route is no longer active (already gated out)";
            }
        }
        return null;
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
