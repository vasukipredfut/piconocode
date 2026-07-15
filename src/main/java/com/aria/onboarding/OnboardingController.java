package com.aria.onboarding;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Single-endpoint customer onboarding API.
 *
 * <p>A developer POSTs a JSON spec describing the entities, workflows and
 * pages a new customer needs. The controller:
 * <ol>
 *   <li>Finds the next free {@code Vn} migration number by listing
 *       {@code src/main/resources/db/migration/}.</li>
 *   <li>Generates a single idempotent SQL script that creates the entity
 *       tables, populates {@code entity_registry} + {@code entity_field_registry},
 *       inserts one row per workflow into {@code workflow_definition}, and
 *       stamps {@code page_definition} + {@code page_version} + points
 *       {@code current_version_id} for every page.</li>
 *   <li>Writes the script to disk as
 *       {@code V<N>__customer_<customerCode>.sql} (kebab-code sanitised to
 *       snake_case so the filename is valid Flyway).</li>
 *   <li>Applies the script through {@link JdbcTemplate} inside a Spring
 *       transaction so a partial failure rolls back the registry
 *       inserts. (The {@code CREATE TABLE} statements are auto-committed
 *       by MariaDB but they use {@code CREATE TABLE IF NOT EXISTS} so a
 *       retry is safe.)</li>
 * </ol>
 *
 * <h3>Endpoint</h3>
 * <pre>POST /api/m0/onboarding/customer</pre>
 *
 * <h3>Security</h3>
 * Only {@code PLANT_ADMIN} or {@code PLATFORM_ADMIN} may call. The check
 * follows the same role-name lookup used by {@code GenericEntityController}.
 *
 * <h3>SQL safety</h3>
 * Every identifier that ends up concatenated into DDL/DML (customer code,
 * entity codes, table names, field codes, page codes, workflow codes) is
 * matched against {@link #SAFE_IDENT} before use. Literal string values
 * that flow into {@code VALUES(...)} clauses are single-quote escaped by
 * {@link #sqlLiteral(String)}.
 */
@RestController
@RequestMapping("/api/m0/onboarding")
public class OnboardingController {

    /** Identifier allow-list (letters, digits, underscore). */
    private static final Pattern SAFE_IDENT = Pattern.compile("^[A-Za-z0-9_]+$");
    /** Customer / page codes may additionally contain '-' and ':'. */
    private static final Pattern SAFE_CODE = Pattern.compile("^[A-Za-z0-9_\\-:.]+$");
    /** Extracts the leading number from a filename like {@code V80__foo.sql}. */
    private static final Pattern V_NUMBER = Pattern.compile("^V(\\d+)(?:_\\d+)?__.+\\.sql$");

    private static final Set<String> ALLOWED_ROLES = Set.of("PLANT_ADMIN", "PLATFORM_ADMIN");

    /** Directory where migration files live. Overridable for tests. */
    private static final String MIGRATION_DIR =
            "earmsbackend/src/main/resources/db/migration";

    private final JdbcTemplate jdbc;
    private final Map<Long, String> roleNameCache = new ConcurrentHashMap<>();

    public OnboardingController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================================================================
    // Endpoint
    // =========================================================================

    @PostMapping("/customer")
    @Transactional
    public ResponseEntity<?> onboardCustomer(@RequestBody OnboardingRequest req) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;

        if (req == null) {
            return ResponseEntity.badRequest().body(err("Missing request body"));
        }
        if (req.customerCode == null || !SAFE_CODE.matcher(req.customerCode).matches()) {
            return ResponseEntity.badRequest().body(err("Invalid customerCode: " + req.customerCode));
        }
        if (req.displayName == null || req.displayName.isBlank()) {
            return ResponseEntity.badRequest().body(err("Missing displayName"));
        }

        try {
            // 1. next free Vn
            Path migrationDir = resolveMigrationDir();
            int nextV = nextMigrationNumber(migrationDir);
            String slug = slug(req.customerCode);
            String migrationFile = "V" + nextV + "__customer_" + slug + ".sql";

            // 2. generate SQL
            String sql = buildSql(req, migrationFile);

            // 3. write to disk (best-effort — we still apply even if write fails)
            String writeError = null;
            try {
                Files.writeString(migrationDir.resolve(migrationFile),
                        sql, StandardCharsets.UTF_8);
            } catch (IOException ioe) {
                writeError = "Could not write migration file: " + ioe.getMessage();
            }

            // 4. apply
            int applied = applySql(sql);

            // 5. respond
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ok", true);
            out.put("customerCode", req.customerCode);
            out.put("migrationFile", migrationFile);
            out.put("migrationSql", sql);
            out.put("statementsApplied", applied);
            out.put("appliedAt", Instant.now().toString());
            out.put("entitiesCreated", req.entities == null ? 0 : req.entities.size());
            out.put("workflowsCreated", req.workflows == null ? 0 : req.workflows.size());
            out.put("pagesCreated", req.pages == null ? 0 : req.pages.size());
            if (writeError != null) out.put("writeWarning", writeError);
            return ResponseEntity.ok(out);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(err(iae.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(err(ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    // =========================================================================
    // SQL generation
    // =========================================================================

    private String buildSql(OnboardingRequest req, String migrationFile) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("-- ").append(migrationFile).append('\n');
        sb.append("-- Auto-generated by OnboardingController for customer: ")
                .append(req.customerCode).append(" (").append(req.displayName).append(")\n");
        sb.append("-- Generated at ").append(Instant.now().toString()).append('\n');
        sb.append("-- Idempotent: every write uses IF NOT EXISTS / INSERT IGNORE / WHERE NOT EXISTS.\n\n");

        // ---- entities ------------------------------------------------------
        if (req.entities != null) {
            for (EntitySpec e : req.entities) {
                appendEntity(sb, e);
            }
        }

        // ---- workflows -----------------------------------------------------
        if (req.workflows != null) {
            for (WorkflowSpec w : req.workflows) {
                appendWorkflow(sb, w);
            }
        }

        // ---- pages ---------------------------------------------------------
        if (req.pages != null) {
            for (PageSpec p : req.pages) {
                appendPage(sb, p);
            }
        }

        return sb.toString();
    }

    private void appendEntity(StringBuilder sb, EntitySpec e) {
        if (e.code == null || !SAFE_IDENT.matcher(e.code).matches()) {
            throw new IllegalArgumentException("Invalid entity code: " + e.code);
        }
        if (e.tableName == null || !SAFE_IDENT.matcher(e.tableName).matches()) {
            throw new IllegalArgumentException("Invalid tableName: " + e.tableName);
        }
        String display = e.displayName == null || e.displayName.isBlank() ? e.code : e.displayName;
        String category = e.category == null || e.category.isBlank() ? "Custom" : e.category;

        sb.append("-- ---- entity: ").append(e.code).append(" -----------------------------------\n");

        // CREATE TABLE with id + tenant_id + timestamps + one column per field
        sb.append("CREATE TABLE IF NOT EXISTS ").append(e.tableName).append(" (\n");
        sb.append("  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n");
        sb.append("  tenant_id BIGINT NOT NULL DEFAULT 1");
        if (e.fields != null) {
            for (FieldSpec f : e.fields) {
                validateField(f);
                sb.append(",\n  ").append(f.code).append(' ').append(sqlTypeFor(f.dataType, f.required));
            }
        }
        sb.append(",\n  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        sb.append(",\n  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n");
        sb.append(") ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;\n\n");

        // entity_registry (guarded)
        sb.append("INSERT INTO entity_registry (code, display_name, table_name)\n");
        sb.append("SELECT ").append(sqlLiteral(e.code)).append(", ")
                .append(sqlLiteral(display)).append(", ")
                .append(sqlLiteral(e.tableName)).append("\n");
        sb.append(" WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code=")
                .append(sqlLiteral(e.code)).append(");\n\n");

        // entity_field_registry (guarded per field)
        if (e.fields != null) {
            for (FieldSpec f : e.fields) {
                String label = f.label == null || f.label.isBlank() ? f.code : f.label;
                String widget = f.widgetKind == null || f.widgetKind.isBlank() ? "text" : f.widgetKind;
                String dataType = normaliseDataType(f.dataType);

                sb.append("INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, ")
                        .append("filterable, sortable, searchable, required, widget_kind)\n");
                sb.append("SELECT ").append(sqlLiteral(e.code)).append(", ")
                        .append(sqlLiteral(f.code)).append(", ")
                        .append(sqlLiteral(label)).append(", ")
                        .append(sqlLiteral(dataType)).append(", 1, 1, ")
                        .append((dataType.equals("STRING") || dataType.equals("TEXT")) ? "1" : "0")
                        .append(", ").append(f.required ? "1" : "0").append(", ")
                        .append(sqlLiteral(widget)).append("\n");
                sb.append(" WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry ")
                        .append("WHERE entity_code=").append(sqlLiteral(e.code))
                        .append(" AND field_code=").append(sqlLiteral(f.code)).append(");\n");
            }
            sb.append('\n');
        }
        // Category is a page-level concept, not entity-level — surfaced via
        // the pages that reference this entity. We include it as a comment
        // for the reader.
        sb.append("-- category hint (for pages): ").append(category).append("\n\n");
    }

    private void appendWorkflow(StringBuilder sb, WorkflowSpec w) {
        if (w.code == null || !SAFE_CODE.matcher(w.code).matches()) {
            throw new IllegalArgumentException("Invalid workflow code: " + w.code);
        }
        sb.append("-- ---- workflow: ").append(w.code).append(" -----------------------------------\n");

        String body = workflowBodyJson(w);
        sb.append("INSERT INTO workflow_definition (tenant_id, code, version, status, body_json)\n");
        sb.append("SELECT 1, ").append(sqlLiteral(w.code)).append(", 1, 'PUBLISHED', ")
                .append(sqlLiteral(body)).append("\n");
        sb.append(" WHERE NOT EXISTS (SELECT 1 FROM workflow_definition ")
                .append("WHERE tenant_id=1 AND code=").append(sqlLiteral(w.code))
                .append(" AND version=1);\n\n");
    }

    private void appendPage(StringBuilder sb, PageSpec p) {
        if (p.code == null || !SAFE_CODE.matcher(p.code).matches()) {
            throw new IllegalArgumentException("Invalid page code: " + p.code);
        }
        String title = p.title == null || p.title.isBlank() ? p.code : p.title;
        String category = p.category == null || p.category.isBlank() ? "Custom" : p.category;

        sb.append("-- ---- page: ").append(p.code).append(" -----------------------------------\n");

        // page_definition
        sb.append("INSERT INTO page_definition (tenant_id, code, title, category, status)\n");
        sb.append("SELECT 1, ").append(sqlLiteral(p.code)).append(", ")
                .append(sqlLiteral(title)).append(", ")
                .append(sqlLiteral(category)).append(", 'PUBLISHED'\n");
        sb.append(" WHERE NOT EXISTS (SELECT 1 FROM page_definition ")
                .append("WHERE tenant_id=1 AND code=").append(sqlLiteral(p.code)).append(");\n");

        // page_version
        String bodyJson = pageBodyJson(p);
        sb.append("INSERT INTO page_version (page_id, version, status, body_json, published_at)\n");
        sb.append("SELECT pd.id, 1, 'PUBLISHED', ").append(sqlLiteral(bodyJson))
                .append(", CURRENT_TIMESTAMP\n");
        sb.append(" FROM page_definition pd\n");
        sb.append(" WHERE pd.tenant_id=1 AND pd.code=").append(sqlLiteral(p.code)).append("\n");
        sb.append("   AND NOT EXISTS (SELECT 1 FROM page_version pv ")
                .append("WHERE pv.page_id=pd.id AND pv.version=1);\n");

        // point current_version_id
        sb.append("UPDATE page_definition pd\n");
        sb.append("   JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1\n");
        sb.append("   SET pd.current_version_id = pv.id\n");
        sb.append(" WHERE pd.tenant_id=1 AND pd.code=").append(sqlLiteral(p.code))
                .append(" AND pd.current_version_id IS NULL;\n\n");
    }

    // =========================================================================
    // JSON body builders (plain string concat — mirrors DesignerController)
    // =========================================================================

    private String workflowBodyJson(WorkflowSpec w) {
        StringBuilder j = new StringBuilder(256);
        j.append("{\"code\":\"").append(jsonEscape(w.code))
                .append("\",\"version\":1,\"initialState\":\"")
                .append(jsonEscape(w.initialState == null ? "DRAFT" : w.initialState))
                .append("\",\"states\":[");
        if (w.states != null) {
            for (int i = 0; i < w.states.size(); i++) {
                StateSpec s = w.states.get(i);
                if (i > 0) j.append(',');
                j.append("{\"code\":\"").append(jsonEscape(s.code)).append('"');
                if (s.assignedRole != null && !s.assignedRole.isBlank()) {
                    j.append(",\"assignedRole\":\"").append(jsonEscape(s.assignedRole)).append('"');
                }
                if (s.terminal) j.append(",\"terminal\":true");
                j.append('}');
            }
        }
        j.append("],\"transitions\":[");
        if (w.transitions != null) {
            for (int i = 0; i < w.transitions.size(); i++) {
                TransitionSpec t = w.transitions.get(i);
                if (i > 0) j.append(',');
                j.append("{\"from\":\"").append(jsonEscape(t.from))
                        .append("\",\"to\":\"").append(jsonEscape(t.to))
                        .append("\",\"trigger\":\"").append(jsonEscape(t.trigger == null ? "page.submitted" : t.trigger))
                        .append('"');
                if (t.pageCode != null && !t.pageCode.isBlank()) {
                    j.append(",\"pageCode\":\"").append(jsonEscape(t.pageCode)).append('"');
                }
                if (t.guard != null && !t.guard.isBlank()) {
                    j.append(",\"guard\":\"").append(jsonEscape(t.guard)).append('"');
                }
                j.append('}');
            }
        }
        j.append("]}");
        return j.toString();
    }

    @SuppressWarnings("unchecked")
    private String pageBodyJson(PageSpec p) {
        String kind = p.kind == null || p.kind.isBlank() ? "list" : p.kind;
        StringBuilder j = new StringBuilder(256);
        j.append("{\"schemaVersion\":2,\"kind\":\"").append(jsonEscape(kind)).append('"');
        if ("list".equalsIgnoreCase(kind)) {
            if (p.entity == null || !SAFE_IDENT.matcher(p.entity).matches()) {
                throw new IllegalArgumentException("Invalid entity on list page " + p.code + ": " + p.entity);
            }
            j.append(",\"dataSource\":{\"type\":\"entity\",\"entity\":\"")
                    .append(jsonEscape(p.entity)).append("\"}");
            j.append(",\"pageSize\":25");
        } else if ("form".equalsIgnoreCase(kind)) {
            if (p.entity == null || !SAFE_IDENT.matcher(p.entity).matches()) {
                throw new IllegalArgumentException("Invalid entity on form page " + p.code + ": " + p.entity);
            }
            j.append(",\"entity\":\"").append(jsonEscape(p.entity)).append('"');
        } else if ("layout".equalsIgnoreCase(kind)) {
            // layout is an array of child element specs — round-trip whatever
            // the caller supplied as a JSON array literal.
            String layoutJson = p.layout == null ? "[]" : toJsonArray(p.layout);
            j.append(",\"layout\":").append(layoutJson);
        }
        j.append("}");
        return j.toString();
    }

    // Best-effort JSON serialisation for a List<?> of maps/strings/numbers/booleans.
    @SuppressWarnings("unchecked")
    private String toJsonArray(List<?> items) {
        StringBuilder sb = new StringBuilder(64);
        sb.append('[');
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            appendJsonValue(sb, items.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void appendJsonValue(StringBuilder sb, Object v) {
        if (v == null) { sb.append("null"); return; }
        if (v instanceof Boolean || v instanceof Number) { sb.append(v); return; }
        if (v instanceof Map<?,?> m) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?,?> e : m.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(jsonEscape(String.valueOf(e.getKey()))).append("\":");
                appendJsonValue(sb, e.getValue());
            }
            sb.append('}');
            return;
        }
        if (v instanceof List<?> l) {
            sb.append(toJsonArray(l));
            return;
        }
        sb.append('"').append(jsonEscape(String.valueOf(v))).append('"');
    }

    // =========================================================================
    // File / migration numbering
    // =========================================================================

    /** Package-visible so tests can override for a temp dir. */
    protected Path resolveMigrationDir() {
        // Look for the directory relative to the current working directory
        // and to earmsbackend/ (developer might run from repo root or module).
        List<String> candidates = List.of(
                MIGRATION_DIR,
                "src/main/resources/db/migration",
                "../earmsbackend/src/main/resources/db/migration"
        );
        for (String c : candidates) {
            Path p = Paths.get(c).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) return p;
        }
        // Fall back to the first candidate — writing will fail cleanly with
        // a clear IOException that the caller surfaces as writeWarning.
        return Paths.get(candidates.get(0)).toAbsolutePath().normalize();
    }

    private int nextMigrationNumber(Path migrationDir) throws IOException {
        int max = 0;
        if (!Files.isDirectory(migrationDir)) return 100; // conservative starting point
        try (Stream<Path> stream = Files.list(migrationDir)) {
            for (Path f : (Iterable<Path>) stream::iterator) {
                String name = f.getFileName().toString();
                Matcher m = V_NUMBER.matcher(name);
                if (m.matches()) {
                    try { max = Math.max(max, Integer.parseInt(m.group(1))); }
                    catch (NumberFormatException ignored) {}
                }
            }
        }
        return max + 1;
    }

    // =========================================================================
    // SQL application
    // =========================================================================

    private int applySql(String sql) {
        // Split on ";\n" — matches how the migration files are written and
        // avoids splitting inside JSON string literals (JSON payloads are
        // single-quoted, contain no un-escaped semicolons at end-of-line).
        int applied = 0;
        for (String stmt : sql.split(";\\s*\\n")) {
            String s = stmt.trim();
            if (s.isEmpty() || s.startsWith("--")) continue;
            // Strip trailing comment-only lines
            String[] lines = s.split("\n");
            StringBuilder clean = new StringBuilder();
            for (String line : lines) {
                String t = line.trim();
                if (t.startsWith("--")) continue;
                if (clean.length() > 0) clean.append('\n');
                clean.append(line);
            }
            String finalStmt = clean.toString().trim();
            if (finalStmt.isEmpty()) continue;
            // trailing semicolon handling
            if (finalStmt.endsWith(";")) finalStmt = finalStmt.substring(0, finalStmt.length() - 1);
            if (finalStmt.isEmpty()) continue;
            jdbc.execute(finalStmt);
            applied++;
        }
        return applied;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private String sqlTypeFor(String dataType, boolean required) {
        String tail = required ? " NOT NULL" : " NULL";
        switch (normaliseDataType(dataType)) {
            case "INTEGER":  return "INT" + tail;
            case "LONG":     return "BIGINT" + tail;
            case "DECIMAL":  return "DECIMAL(18,3)" + tail;
            case "BOOLEAN":  return "TINYINT(1) NOT NULL DEFAULT 0";
            case "DATE":     return "DATE" + tail;
            case "DATETIME":
            case "TIMESTAMP": return "DATETIME" + tail;
            case "TEXT":     return "TEXT" + (required ? "" : " NULL");
            case "JSON":     return "JSON" + (required ? "" : " NULL");
            case "STRING":
            default:         return "VARCHAR(255)" + tail;
        }
    }

    private String normaliseDataType(String dt) {
        return (dt == null || dt.isBlank() ? "STRING" : dt.trim().toUpperCase());
    }

    private void validateField(FieldSpec f) {
        if (f.code == null || !SAFE_IDENT.matcher(f.code).matches()) {
            throw new IllegalArgumentException("Invalid field code: " + f.code);
        }
    }

    /** Escape a value for embedding inside a single-quoted SQL string literal. */
    private String sqlLiteral(String v) {
        if (v == null) return "NULL";
        return "'" + v.replace("\\", "\\\\").replace("'", "''") + "'";
    }

    /** Escape a value for embedding inside a JSON string. */
    private String jsonEscape(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Kebab / dot / colon customerCode -> snake_case slug for the filename. */
    private String slug(String code) {
        return code.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static Map<String, Object> err(String msg) {
        return Map.of("ok", false, "error", msg);
    }

    // =========================================================================
    // Role check — mirrors GenericEntityController#currentRoleName + cache
    // =========================================================================

    private ResponseEntity<?> assertRole() {
        String role = currentRoleName();
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("ok", false, "code", "RBAC_NO_USER",
                            "error", "Not authenticated"));
        }
        for (String allowed : ALLOWED_ROLES) {
            if (allowed.equalsIgnoreCase(role)) return null;
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of("ok", false, "code", "RBAC_DENIED",
                        "error", role + " is not permitted to onboard customers",
                        "allowed", ALLOWED_ROLES));
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
                return s; // already a role name
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

    // =========================================================================
    // Request DTOs — plain data holders. NOT JPA entities. Used only as the
    // shape of the JSON body Jackson deserialises for this endpoint.
    // =========================================================================

    public static class OnboardingRequest {
        public String customerCode;
        public String displayName;
        public List<EntitySpec> entities;
        public List<WorkflowSpec> workflows;
        public List<PageSpec> pages;
    }

    public static class EntitySpec {
        public String code;
        public String displayName;
        public String tableName;
        public String category;
        public List<FieldSpec> fields;
    }

    public static class FieldSpec {
        public String code;
        public String label;
        public String dataType;
        public String widgetKind;
        public boolean required;
    }

    public static class WorkflowSpec {
        public String code;
        public String initialState;
        public List<StateSpec> states;
        public List<TransitionSpec> transitions;
    }

    public static class StateSpec {
        public String code;
        public String assignedRole;
        public boolean terminal;
    }

    public static class TransitionSpec {
        public String from;
        public String to;
        public String trigger;
        public String pageCode;
        public String guard;
    }

    public static class PageSpec {
        public String code;
        public String title;
        public String category;
        public String kind;         // list | form | layout
        public String entity;       // used by list + form
        public List<Object> layout; // used by layout — array of element specs
    }
}
