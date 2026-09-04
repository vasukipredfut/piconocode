package com.aria.entity;

import com.aria.integration.secrets.TenantSecretCodec;
import com.aria.security.TenantContext;
import com.aria.auth.AuthUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Generic CRUD endpoints driven by entity_registry + entity_field_registry.
 *
 * <p>RBAC: entity_registry now carries {@code read_role} / {@code write_role}
 * as comma-separated role-name lists, or {@code *} for "any authenticated".
 * Every endpoint checks the current user's role name against the allowed set
 * and returns 403 (with a clear JSON body) on mismatch.
 *
 * <p>Audit: every mutation (POST/PUT/DELETE) writes a row to
 * {@code m0_audit_log} with actor_id, entity_code, entity_id, action_kind,
 * before-state, and after-state.  Reads are NOT audited (volume).
 *
 * <p>SQL injection prevention: every column / table name is matched against
 * {@code [A-Za-z0-9_]+} before being concatenated into a SQL string.
 * Values are always passed as bound parameters.
 */
@RestController
@RequestMapping("/api/m0/entities")
public class GenericEntityController {

    private static final Pattern SAFE_IDENT = Pattern.compile("^[A-Za-z0-9_]+$");
    private final JdbcTemplate jdbc;
    private final Map<Long, String> roleNameCache = new ConcurrentHashMap<>();

    // GatePass single-row reads (getRow/getRowByField — the data source behind
    // page-JSON onChangeFill) are the only consumer that ever saw the fields now
    // split out into qc_preload_detail / loading_allocation_detail /
    // weighment_capture_detail / sealing_process_detail / gate_pass_closure
    // (V79/V80/V83/V85/V100-V102; each drop of the now-dead gate_pass columns
    // removed the naming collision, so a plain select of the satellite columns
    // is enough — except gate_pass_closure.closed_at, which coexists with
    // gate_pass.closed_at (different meaning: lifecycle close vs. checklist
    // close) and must stay aliased below.).
    private static final String GATE_PASS_SATELLITE_JOIN = GatePassSatelliteJoin.JOIN;
    private static final String GATE_PASS_SATELLITE_SELECT = GatePassSatelliteJoin.SELECT;

    // Optional — old test wiring constructs this controller without the codec.
    // When absent, secret fields pass through unmodified (pre-encryption
    // behaviour).
    @Autowired(required = false)
    private TenantSecretCodec secretCodec;

    // Optional — when present, creating a WORKFLOW-class entity row (per
    // entity_registry.lifecycle_kind) auto-starts its bound workflow instance so
    // the transactional lifecycle is engine-driven, not advanced by raw field
    // writes. DEFINED masters are never touched.
    @Autowired(required = false)
    private com.aria.workflow.engine.WorkflowEngineService workflowEngine;

    private final PasswordEncoder passwordEncoder;

    public GenericEntityController(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Only {@code Employee} (table {@code employee_details}) has a
     * registered {@code password} field today, and that same column is
     * read straight off the row by {@code AuthUserDetailsService} for
     * login -- Spring Security's {@code DaoAuthenticationProvider} always
     * compares a submitted password against it via the app's
     * {@code BCryptPasswordEncoder} bean.
     *
     * <p>{@code entity_field_registry.password} is {@code hidden=1} (a
     * pre-existing guard against the generic column-passthrough writing a
     * raw plaintext value nothing ever hashed), so the normal body-to-
     * {@code safe} copy in {@link #insert} never carries it — read the raw
     * value straight off {@code body} instead of {@code safe}. Hash
     * whatever was submitted; if the caller left it out or blank, default
     * to a known password ("changeme") the new hire can log in with and be
     * told to change.
     */
    private void applyDefaultPasswordIfNeeded(String table, Map<String, Object> body, Map<String, Object> safe) {
        if (!tableHasColumn(table, "password")) return;
        Object raw = body == null ? null : body.get("password");
        String plain = (raw == null || raw.toString().isBlank()) ? "changeme" : raw.toString();
        safe.put("password", passwordEncoder.encode(plain));
    }

    // ------------------------------------------------------------------------
    // Schema endpoint — no RBAC: any authenticated user may read the shape.
    // ------------------------------------------------------------------------
    @GetMapping("/{code}/schema")
    public Map<String, Object> schema(@PathVariable String code) {
        Map<String, Object> reg = lookupRegistry(code);
        List<Map<String, Object>> fields;
        try {
            // Fully-loaded row (includes V78 rule columns). Silently falls
            // back for pre-V78 dev DBs that haven't run the migration yet.
            fields = jdbc.queryForList(
                    "SELECT f.field_code, f.display_label, f.data_type, f.filterable, f.sortable, " +
                            "f.searchable, f.required, f.widget_kind, f.options_json, " +
                            "f.visible_when, f.required_when, f.computed_from " +
                            "FROM entity_field_registry f" +
                            FIELD_RESOLUTION + "ORDER BY f.id", fieldArgs(code));
        } catch (Exception e) {
            try {
                fields = jdbc.queryForList(
                        "SELECT f.field_code, f.display_label, f.data_type, f.filterable, f.sortable, " +
                                "f.searchable, f.required, f.widget_kind, f.options_json " +
                                "FROM entity_field_registry f" +
                                FIELD_RESOLUTION + "ORDER BY f.id", fieldArgs(code));
            } catch (Exception e2) {
                fields = jdbc.queryForList(
                        "SELECT f.field_code, f.display_label, f.data_type, f.filterable, f.sortable, " +
                                "f.searchable, f.required FROM entity_field_registry f" +
                                FIELD_RESOLUTION + "ORDER BY f.id", fieldArgs(code));
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("displayName", reg.get("display_name"));
        out.put("table", reg.get("table_name"));
        out.put("readRole", reg.get("read_role"));
        out.put("writeRole", reg.get("write_role"));
        out.put("approverRole", reg.get("approver_role"));
        out.put("fields", fields);
        return out;
    }

    // ════════════════════════════════════════════════════════════════════
    // Tenancy. Every registry-driven query is scoped by the caller's tenant —
    // no allowlist to keep in sync, no table exempt by omission.
    //
    // READ  : tenant_id IN (tid, -1)  — your rows plus the inherited global
    //         product catalogue. Business tables hold no -1 rows, so this
    //         collapses to "= tid" for them.
    // WRITE : tenant_id = tid, strictly. A tenant must never update or delete
    //         a global (-1) row through generic CRUD; that would let one firm
    //         mutate the product for every other firm. Customising a global
    //         page forks a tenant copy instead.
    // INSERT: tenant_id is forced from TenantContext and any client-supplied
    //         value is discarded — otherwise a caller can plant rows into
    //         another firm by naming its id.
    //
    // Tables with no tenant_id column (pure catalogues) get no predicate.
    // ════════════════════════════════════════════════════════════════════

    /** " AND tenant_id IN (?, -1)" for reads, or "" when the table is not tenant-scoped. */
    private String readScope(String table) {
        return tableHasColumn(table, "tenant_id") ? " AND tenant_id IN (?, -1)" : "";
    }

    /**
     * WHERE clause resolving an entity's fields for the current tenant:
     * inherited product fields (-1) plus this tenant's own, each appearing once.
     *
     * <p>Three behaviours fall out of one clause:
     * <ul>
     *   <li><b>ADD</b> — a tenant row with a new field_code simply unions in.</li>
     *   <li><b>MODIFY</b> — a tenant row with the SAME field_code suppresses the
     *       -1 row via NOT EXISTS, so the override wins.</li>
     *   <li><b>REMOVE</b> — a tenant row with hidden=1 suppresses the -1 row the
     *       same way, and is then dropped by {@code f.hidden = 0}. Net: the field
     *       disappears for this tenant only. You cannot DELETE the shared -1 row
     *       without removing the field for every other tenant, so removal has to
     *       be a tombstone.</li>
     * </ul>
     *
     * <p>Binds: entity_code, tenantId, tenantId, tenantId — see {@link #fieldArgs}.
     */
    private static final String FIELD_RESOLUTION =
            " WHERE f.entity_code = ? " +
            "   AND f.tenant_id IN (?, -1) " +
            "   AND f.hidden = 0 " +
            "   AND (f.tenant_id = ? " +
            "        OR NOT EXISTS (SELECT 1 FROM entity_field_registry o " +
            "                       WHERE o.entity_code = f.entity_code " +
            "                         AND o.field_code  = f.field_code " +
            "                         AND o.tenant_id   = ?)) ";

    private Object[] fieldArgs(String entityCode) {
        Long t = TenantContext.get();
        return new Object[]{ entityCode, t, t, t };
    }

    /** " AND tenant_id = ?" for writes, or "" when the table is not tenant-scoped. */
    private String writeScope(String table) {
        return tableHasColumn(table, "tenant_id") ? " AND tenant_id = ?" : "";
    }

    /** Args for the predicate above — empty when the table is not tenant-scoped. */
    private Object[] scopeArgs(String table, Object... leading) {
        List<Object> a = new ArrayList<>(Arrays.asList(leading));
        if (tableHasColumn(table, "tenant_id")) a.add(TenantContext.get());
        return a.toArray();
    }

    @GetMapping("/{code}/{id}")
    public ResponseEntity<?> getRow(@PathVariable String code, @PathVariable long id) {
        Map<String, Object> reg = lookupRegistry(code);
        ResponseEntity<?> denied = checkRole(reg, "read");
        if (denied != null) return denied;
        String table = (String) reg.get("table_name");
        assertSafe(table);
        try {
            Map<String, Object> row;
            if ("gate_pass".equals(table)) {
                row = jdbc.queryForMap(
                        "SELECT " + GATE_PASS_SATELLITE_SELECT + " FROM gate_pass gp" + GATE_PASS_SATELLITE_JOIN +
                                " WHERE gp.id=? AND gp.tenant_id IN (?, -1)",
                        id, TenantContext.get());
            } else {
                row = jdbc.queryForMap(
                        "SELECT * FROM " + table + " WHERE id=?" + readScope(table),
                        scopeArgs(table, id));
            }
            maskSecretsInPlace(code, row);
            return ResponseEntity.ok(row);
        } catch (Exception e) {
            // A row belonging to another tenant is reported as not-found rather
            // than forbidden: "exists but not yours" is itself a disclosure.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "Row not found: id=" + id + " in " + table));
        }
    }
    @GetMapping("/{code}/by-field/{fieldName}/{value}")
    public ResponseEntity<?> getRowByField(@PathVariable String code,
                                           @PathVariable String fieldName,
                                           @PathVariable String value) {
        Map<String, Object> reg = lookupRegistry(code);
        ResponseEntity<?> denied = checkRole(reg, "read");
        if (denied != null) return denied;
        String table = (String) reg.get("table_name");
        assertSafe(table);
        assertSafe(fieldName);
        try {
            Map<String, Object> row;
            if ("gate_pass".equals(table)) {
                row = jdbc.queryForMap(
                    "SELECT " + GATE_PASS_SATELLITE_SELECT + " FROM gate_pass gp" + GATE_PASS_SATELLITE_JOIN +
                            " WHERE gp." + fieldName + "=?", value);
            } else {
                row = jdbc.queryForMap(
                    "SELECT * FROM " + table + " WHERE " + fieldName + "=?", value);
            }
            maskSecretsInPlace(code, row);
            return ResponseEntity.ok(row);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "Row not found: " + fieldName + "=" + value + " in " + table));
        }
    }

    /** Rewrite any secret columns in a query result to the mask value in-place. */
    private void maskSecretsInPlace(String code, Map<String, Object> row) {
        if (secretCodec == null || row == null) return;
        for (String k : new ArrayList<>(row.keySet())) {
            if (secretCodec.isSecretField(code, k)) row.put(k, secretCodec.mask(row.get(k)));
        }
    }

    /** Encrypt any secret columns present in the write payload before it hits the DB. */
    private void encryptSecretsInPlace(String code, Map<String, Object> safe) {
        if (secretCodec == null || safe == null) return;
        for (String k : new ArrayList<>(safe.keySet())) {
            if (!secretCodec.isSecretField(code, k)) continue;
            Object v = safe.get(k);
            if (v instanceof String s) {
                // Skip if the row still holds the mask from a round-tripped GET —
                // that means the customer edited other fields and never touched
                // the secret; keep whatever's already in the DB.
                if (TenantSecretCodec.MASK.equals(s)) {
                    safe.remove(k);
                    continue;
                }
                safe.put(k, secretCodec.encryptIfPlaintext(s));
            }
        }
    }

    @PostMapping("/{code}")
    public ResponseEntity<?> insert(@PathVariable String code, @RequestBody Map<String, Object> body) {
        Map<String, Object> reg = lookupRegistry(code);
        ResponseEntity<?> denied = checkRole(reg, "write");
        if (denied != null) return denied;
        String table = (String) reg.get("table_name");
        assertSafe(table);

        Set<String> declared = jdbc.queryForList(
                "SELECT f.field_code FROM entity_field_registry f" + FIELD_RESOLUTION,
                String.class, fieldArgs(code)).stream().collect(java.util.stream.Collectors.toSet());
        Map<String, Object> safe = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : body.entrySet()) {
            if (!SAFE_IDENT.matcher(e.getKey()).matches()) continue;
            // tenant_id is NEVER taken from the body: accepting it let a caller
            // plant a row into another firm simply by naming its id. It is
            // stamped from the authenticated context below instead.
            if (e.getKey().equals("tenant_id")) continue;
            if (declared.contains(e.getKey()) && tableHasColumn(table, e.getKey())) {
                safe.put(e.getKey(), e.getValue());
            }
        }
        if (safe.isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "No valid fields in body (expected any of " + declared + ")"));
        }
        if (tableHasColumn(table, "tenant_id")) {
            safe.put("tenant_id", TenantContext.get());
        }
        applyDefaultPasswordIfNeeded(table, body, safe);
        encryptSecretsInPlace(code, safe);

        String cols = String.join(",", safe.keySet());
        String marks = String.join(",", java.util.Collections.nCopies(safe.size(), "?"));
        String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + marks + ")";
        try {
            // Use KeyHolder so we capture the generated id on the SAME connection
            // as the INSERT — jdbc.queryForObject("SELECT LAST_INSERT_ID()") was
            // hitting a different pooled connection and getting 0/null.
            KeyHolder keyHolder = new GeneratedKeyHolder();
            Object[] values = safe.values().toArray();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
                return ps;
            }, keyHolder);
            Long lastId = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
            writeAudit("CREATE", code, lastId, null, safe);
            maybeStartWorkflow(code, table, lastId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("ok", true);
            result.put("entity", code);
            // Never echo the password hash back in the response body, even
            // hashed -- the rest of "safe" (what was actually written) is
            // still useful for the caller to confirm.
            Map<String, Object> inserted = safe;
            if (safe.containsKey("password")) {
                inserted = new LinkedHashMap<>(safe);
                inserted.remove("password");
            }
            result.put("inserted", inserted);
            if (lastId != null && lastId > 0) result.put("id", lastId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                Map.of("ok", false, "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    /**
     * Bulk upsert — for loading spreadsheet/CSV data into any registry-backed
     * entity without a bespoke migration per batch. Body: {@code {"rows": [ {...}, {...} ]}}.
     *
     * <p>Each row is filtered through the same {@code entity_field_registry} +
     * {@code SAFE_IDENT} allowlist as the single-row {@link #insert}, then
     * written with {@code INSERT ... ON DUPLICATE KEY UPDATE}: rows that hit
     * an existing unique key (e.g. SkuMaster's {@code sku_code}) update in
     * place, everything else inserts. Tables with no natural unique key just
     * always insert, same as {@link #insert} called in a loop.
     *
     * <p>Rows are applied one at a time so one bad row (unknown column, bad
     * type) doesn't abort the batch — its index and error are collected and
     * returned alongside the rows that did succeed. Capped at 2000 rows per
     * call; split larger files client-side.
     */
    @PostMapping("/{code}/bulk")
    public ResponseEntity<?> bulkUpsert(@PathVariable String code, @RequestBody Map<String, Object> body) {
        Map<String, Object> reg = lookupRegistry(code);
        ResponseEntity<?> denied = checkRole(reg, "write");
        if (denied != null) return denied;
        String table = (String) reg.get("table_name");
        assertSafe(table);

        Object rowsObj = body.get("rows");
        if (!(rowsObj instanceof List<?> rawRows) || rawRows.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Expected a non-empty \"rows\" array"));
        }
        if (rawRows.size() > 2000) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Too many rows in one call (" + rawRows.size() + " > 2000); split into batches"));
        }

        Set<String> declared = jdbc.queryForList(
                "SELECT f.field_code FROM entity_field_registry f" + FIELD_RESOLUTION,
                String.class, fieldArgs(code)).stream().collect(java.util.stream.Collectors.toSet());
        // One round-trip for the whole batch instead of tableHasColumn() per
        // field per row — against a high-latency DB link, that per-cell check
        // turned a few hundred rows into thousands of round-trips.
        Set<String> tableColumns = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                "WHERE table_schema=DATABASE() AND table_name=? AND EXTRA NOT LIKE '%GENERATED%'",
                String.class, table).stream().collect(java.util.stream.Collectors.toSet());
        boolean hasTenant = tableColumns.contains("tenant_id");

        int inserted = 0, updated = 0, unchanged = 0;
        List<Map<String, Object>> errors = new ArrayList<>();

        for (int i = 0; i < rawRows.size(); i++) {
            Object rawRow = rawRows.get(i);
            if (!(rawRow instanceof Map<?, ?> rawMap)) {
                errors.add(Map.of("index", i, "error", "Row is not an object"));
                continue;
            }
            Map<String, Object> safe = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : rawMap.entrySet()) {
                String key = String.valueOf(e.getKey());
                if (!SAFE_IDENT.matcher(key).matches()) continue;
                // Same reasoning as insert(): tenant_id is stamped from context,
                // never taken from the payload; id is never client-assigned here.
                if (key.equals("tenant_id") || key.equals("id")) continue;
                if (declared.contains(key) && tableColumns.contains(key)) {
                    safe.put(key, e.getValue());
                }
            }
            if (safe.isEmpty()) {
                errors.add(Map.of("index", i, "error", "No valid fields in row (expected any of " + declared + ")"));
                continue;
            }
            if (hasTenant) safe.put("tenant_id", TenantContext.get());
            encryptSecretsInPlace(code, safe);

            String cols = String.join(",", safe.keySet());
            String marks = String.join(",", java.util.Collections.nCopies(safe.size(), "?"));
            String updateClause = safe.keySet().stream()
                    .map(k -> k + "=VALUES(" + k + ")")
                    .collect(java.util.stream.Collectors.joining(", "));
            String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + marks + ") " +
                    "ON DUPLICATE KEY UPDATE " + updateClause;
            try {
                int affected = jdbc.update(sql, safe.values().toArray());
                // MariaDB/MySQL JDBC convention: 1 = fresh insert, 2 = existing
                // row updated, 0 = duplicate hit but every value was identical.
                if (affected == 1) inserted++;
                else if (affected == 2) updated++;
                else unchanged++;
            } catch (Exception ex) {
                errors.add(Map.of("index", i, "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
            }
        }

        writeAudit("BULK_CREATE", code, null, null, Map.of(
                "total", rawRows.size(), "inserted", inserted, "updated", updated, "failed", errors.size()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", errors.isEmpty());
        result.put("entity", code);
        result.put("total", rawRows.size());
        result.put("inserted", inserted);
        result.put("updated", updated);
        result.put("unchanged", unchanged);
        result.put("failed", errors.size());
        if (!errors.isEmpty()) result.put("errors", errors);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{code}/{id}")
    public ResponseEntity<?> update(@PathVariable String code, @PathVariable long id,
                                       @RequestBody Map<String, Object> body) {
        Map<String, Object> reg = lookupRegistry(code);
        ResponseEntity<?> denied = checkRole(reg, "write");
        if (denied != null) return denied;
        String table = (String) reg.get("table_name");
        assertSafe(table);
        Set<String> declared = jdbc.queryForList(
                "SELECT f.field_code FROM entity_field_registry f" + FIELD_RESOLUTION,
                String.class, fieldArgs(code)).stream().collect(java.util.stream.Collectors.toSet());
        Map<String, Object> safe = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : body.entrySet()) {
            if (!SAFE_IDENT.matcher(e.getKey()).matches()) continue;
            if (!declared.contains(e.getKey())) continue;
            if (e.getKey().equals("id")) continue;
            safe.put(e.getKey(), e.getValue());
        }
        encryptSecretsInPlace(code, safe);
        if (safe.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "no updatable fields"));

        // Before-image is read with the WRITE scope: if the row is not this
        // tenant's, we must not read it either -- and the update below will
        // match nothing, so the audit entry would otherwise describe a row the
        // caller never touched.
        Map<String, Object> before = null;
        try {
            before = jdbc.queryForMap("SELECT * FROM " + table + " WHERE id=?" + writeScope(table),
                    scopeArgs(table, id));
        } catch (Exception ignored) {}

        StringBuilder sets = new StringBuilder();
        for (String k : safe.keySet()) {
            if (sets.length() > 0) sets.append(", ");
            sets.append(k).append("=?");
        }
        List<Object> args = new ArrayList<>(safe.values());
        args.add(id);
        if (tableHasColumn(table, "tenant_id")) args.add(TenantContext.get());
        String sql = "UPDATE " + table + " SET " + sets + " WHERE id=?" + writeScope(table);
        try {
            int n = jdbc.update(sql, args.toArray());
            if (n == 0) {
                // Either no such row, or it belongs to another tenant. Same
                // answer either way -- distinguishing them leaks existence.
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("ok", false, "error", "Row not found: id=" + id + " in " + table));
            }
            writeAudit("UPDATE", code, id, before, safe);
            return ResponseEntity.ok(Map.of("ok", true, "updated", n, "id", id));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                Map.of("ok", false, "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{code}/{id}")
    public ResponseEntity<?> delete(@PathVariable String code, @PathVariable long id) {
        Map<String, Object> reg = lookupRegistry(code);
        ResponseEntity<?> denied = checkRole(reg, "write");
        if (denied != null) return denied;
        String table = (String) reg.get("table_name");
        assertSafe(table);
        Map<String, Object> before = null;
        try {
            before = jdbc.queryForMap("SELECT * FROM " + table + " WHERE id=?" + writeScope(table),
                    scopeArgs(table, id));
        } catch (Exception ignored) {}
        try {
            int n = jdbc.update("DELETE FROM " + table + " WHERE id=?" + writeScope(table),
                    scopeArgs(table, id));
            if (n == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("ok", false, "error", "Row not found: id=" + id + " in " + table));
            }
            writeAudit("DELETE", code, id, before, null);
            return ResponseEntity.ok(Map.of("ok", true, "deleted", n, "id", id));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                Map.of("ok", false, "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    // ---- RBAC --------------------------------------------------------------

    /** Returns a 403 ResponseEntity when the current user can't perform the
     *  given access ("read"/"write") on the entity, else null. */
    private static final String SUPER_ROLE = "SuperAdmin";

    private ResponseEntity<?> checkRole(Map<String, Object> reg, String access) {
        String allowed = (String) reg.get("write".equals(access) ? "write_role" : "read_role");
        // If the column is null or "*" → no restriction beyond authentication.
        if (allowed == null || allowed.trim().isEmpty() || "*".equals(allowed.trim())) return null;
        String currentName = currentRoleName();
        // SuperAdmin bypasses every entity_registry read_role/write_role check,
        // including entities added after this role was created.
        if (SUPER_ROLE.equalsIgnoreCase(currentName)) return null;
        if (currentName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "ok", false, "code", "RBAC_NO_USER",
                "error", "Not authenticated for " + access + " on " + reg.get("code")));
        }
        // Allow comma-separated list, trim whitespace, case-insensitive compare.
        for (String rl : allowed.split(",")) {
            if (rl.trim().equalsIgnoreCase(currentName)) return null;
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "ok", false, "code", "RBAC_DENIED",
            "error", currentName + " is not permitted to " + access + " " + reg.get("code"),
            "allowed", allowed));
    }

    private String currentRoleName() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getAuthorities() == null) return null;
        for (GrantedAuthority ga : a.getAuthorities()) {
            String s = ga.getAuthority();
            if (s == null) continue;
            // The current user-details impl stores role_id (e.g. "1007") here.
            // Look up the name from the cache; populate on miss.
            try {
                long id = Long.parseLong(s.trim());
                String name = roleNameCache.computeIfAbsent(id, this::loadRoleName);
                if (name != null) return name;
            } catch (NumberFormatException nfe) {
                // Already a name (e.g. older code paths) — pass through.
                return s;
            }
        }
        return null;
    }

    private Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return null;
        Object p = a.getPrincipal();
        if (p instanceof AuthUserDetails uid) return uid.getId();
        return null;
    }

    private String loadRoleName(Long id) {
        try {
            return jdbc.queryForObject("SELECT name FROM role WHERE id=?", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ---- Audit log ---------------------------------------------------------

    private void writeAudit(String kind, String entityCode, Long entityId,
                            Map<String, Object> before, Map<String, Object> after) {
        try {
            String beforeJson = before == null ? null : toJsonSafe(before);
            String afterJson = after == null ? null : toJsonSafe(after);
            String message = kind + " " + entityCode + (entityId != null ? "#" + entityId : "");
            Map<String, Object> gatePassFields = resolveGatePassFields(kind, entityCode, entityId, before);
            jdbc.update(
                "INSERT INTO m0_audit_log (tenant_id, ts, actor_id, action_kind, entity_code, entity_id, " +
                "vehicle_no, gp_number, message, payload_json) " +
                "VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?)",
                TenantContext.get(), currentUserId(), kind, entityCode, entityId,
                gatePassFields.get("vehicle_no"), gatePassFields.get("gp_number"),
                message, afterJson != null ? afterJson : beforeJson);
        } catch (Exception ignored) {
            // Audit-log writes must never break the mutation that triggered them.
            // The page returns ok and downstream consumers can compare DB to
            // payload if the audit row is missing.
        }
    }

    /**
     * Denormalizes vehicle_no/gp_number onto the audit row for GatePass
     * mutations, so the Audit Tracker Group-By feature can group directly on
     * m0_audit_log with no joins. DELETE reads the fields from the
     * pre-delete snapshot (the row is already gone); CREATE/UPDATE re-select
     * the current row (the write already happened by this point).
     */
    private Map<String, Object> resolveGatePassFields(String kind, String entityCode, Long entityId, Map<String, Object> before) {
        if (!"GatePass".equals(entityCode)) return Map.of();
        if ("DELETE".equals(kind)) {
            return before == null ? Map.of() : before;
        }
        if (entityId == null) return Map.of();
        try {
            return jdbc.queryForMap("SELECT vehicle_no, gp_number FROM gate_pass WHERE id = ?", entityId);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static String toJsonSafe(Map<String, Object> m) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : m.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(v);
            else sb.append("\"").append(escape(String.valueOf(v))).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    /**
     * Generic dropdown-options endpoint.
     *
     * Reads {@code value_column} + {@code display_column} from
     * {@code entity_registry} (added in V86 — cherry-picked from develope's
     * V7_21) and returns {@code [{value, label}, ...]} for any registered
     * entity. Used by the frontend Autocomplete / Select primitives so any
     * page can offer "pick from Driver / Vehicle / Employee / ..." without
     * a bespoke controller per entity.
     *
     * Special case: {@code ?type=category} on {@code PageDefinition} returns
     * the distinct category values (used for the sidebar category picker in
     * the visual page designer).
     */
    @GetMapping("/{code}/options")
    public List<Map<String, Object>> options(@PathVariable String code,
                                             @RequestParam(required = false) String type) {
        Map<String, Object> reg = lookupRegistryWithLookup(code);
        String table = (String) reg.get("table_name");
        assertSafe(table);

        // Dropdowns are scoped too. Unscoped, a picker on any tenant-owned
        // entity (Vendor, Driver, Vehicle, Employee...) listed every firm's
        // values to every other firm -- a quiet cross-tenant disclosure, since
        // nothing errors and the list simply looks longer than it should.
        if ("PageDefinition".equalsIgnoreCase(code) && "category".equalsIgnoreCase(type)) {
            return jdbc.queryForList(
                "SELECT DISTINCT category AS value, category AS label FROM " + table +
                " WHERE category IS NOT NULL" + readScope(table).replace(" AND ", " AND ") +
                " ORDER BY category",
                scopeArgs(table));
        }
     // GatePass + ?type=dispatchQueue — gate passes currently sitting in
        // a state assigned to the "Dispatch" role (PARKING_IN_1 plus
        // whatever else that role owns per workflow — PARKING_IN_2 in
        // TankerOutbound, ORDER_SHEET_UPLOADED in Packing/FG), for the
        // Dispatch Front Office vehicle-details screen's dropdown. Value
        // is workflow_instance_id (resolved via GatePass/by-instance/{id}),
        // matching what a list/inbox screen would separately get in full
        // detail from GET /api/workflow/queue/Dispatch — this is that same
        // role-scoped set, reshaped to {value,label} for a select field.
        if ("GatePass".equalsIgnoreCase(code) && "dispatchQueue".equalsIgnoreCase(type)) {
            return jdbc.queryForList(
                "SELECT gp.workflow_instance_id AS value, " +
                "CONCAT(gp.vehicle_no, ' (', wi.current_state, ')') AS label " +
                "FROM " + table + " gp " +
                "JOIN workflow_instance wi ON wi.id = gp.workflow_instance_id " +
                "WHERE wi.assigned_role = 'Dispatch' AND wi.status = 'OPEN' " +
                "ORDER BY gp.vehicle_no");
        }

        // GatePass + ?type=qcPending — gate passes with Dispatch Entry
        // completed, awaiting QC Verification. Role-scoped like
        // dispatchQueue, not state-scoped — DISPATCH_VEHICLE_ENTRY's
        // assignedRole is "Planning" for Tanker/FG and "QC" for Packing
        // (V87, unchanged — that's master-data role assignment, not a bug
        // to fix), so both roles' open queues are combined here to cover
        // all three flows. Value is workflow_instance_id, same
        // by-instance lookup convention as dispatchQueue.
        if ("GatePass".equalsIgnoreCase(code) && "qcPending".equalsIgnoreCase(type)) {
            return jdbc.queryForList(
                "SELECT gp.workflow_instance_id AS value, " +
                "CONCAT(gp.vehicle_no, ' (', wi.current_state, ')') AS label " +
                "FROM " + table + " gp " +
                "JOIN workflow_instance wi ON wi.id = gp.workflow_instance_id " +
                "WHERE wi.assigned_role IN ('Planning','QC') AND wi.status = 'OPEN' " +
                "ORDER BY gp.vehicle_no");
        }

        // GatePass + ?type=qcPreloadPending — gate passes actually awaiting
        // the QC pre-load decision handled by pico:qc-preload-approval:
        // TankerOutbound's ORDER_SHEET_UPLOADED and PackLoading's
        // DISPATCH_VEHICLE_ENTRY (both assignedRole "QC" per V87). FGLoading
        // has no QC pre-load step at all so it's deliberately excluded —
        // unlike qcPending above (which also pulls in FGLoading's
        // Planning-assigned rows for a different, broader purpose).
        if ("GatePass".equalsIgnoreCase(code) && "qcPreloadPending".equalsIgnoreCase(type)) {
            return jdbc.queryForList(
                "SELECT " +
                "gp.workflow_instance_id AS value, " +
                "gp.gp_number AS label, " +
                "gp.vehicle_no AS vehicleNo, " +
                "gp.driver_name AS driverName, " +
                "gp.flow_variant_code AS flowVariant, " +
                "wi.current_state AS currentState " +
                "FROM " + table + " gp " +
                "JOIN workflow_instance wi ON wi.id = gp.workflow_instance_id " +
                "WHERE wi.assigned_role = 'QC' " +
                "AND wi.status = 'OPEN' " +
                "ORDER BY gp.vehicle_no");
        }

        // GatePass + ?type=recentVehicles — distinct vehicle numbers already
        // seen on a gate pass (hired/vendor trucks aren't in the Vehicle
        // fleet master, so the plain value_column/display_column path below
        // would return one row per gate-pass record instead of one per
        // vehicle).
        if ("GatePass".equalsIgnoreCase(code) && "recentVehicles".equalsIgnoreCase(type)) {
            return jdbc.queryForList(
                "SELECT DISTINCT vehicle_no AS value, vehicle_no AS label FROM " + table +
                " WHERE vehicle_no IS NOT NULL ORDER BY vehicle_no");
        }

        // GatePass + ?type=gpNumbers — pick an existing gate pass by its
        // human-readable number, value is the row id (so the caller can
        // then fetch/act on the full GatePass record). Excludes gate passes
        // already closed/rejected — this same dropdown is reused at every
        // Parking In/Out touchpoint (1st and 2nd) across all three loading
        // flows, so it can't be narrowed to one specific current_step; the
        // status is appended to the label so the operator can tell which
        // stage each entry is at.
        if ("GatePass".equalsIgnoreCase(code) && "gpNumbers".equalsIgnoreCase(type)) {
            return jdbc.queryForList(
                "SELECT gp_number AS value, CONCAT(gp_number, ' (', COALESCE(status, 'DRAFT'), ')') AS label " +
                "FROM " + table +
                " WHERE gp_number IS NOT NULL AND status NOT IN (" +
                "'CLOSED','GATE_PASS_OUT_CLOSED','QC_REJECTED','QC_PRELOAD_REJECTED'," +
                "'QC_SAMPLE_REJECTED','QC_FINAL_OUT_REJECTED','REJECTED_BY_MANAGER') " +
                "ORDER BY gp_number DESC");
        }

        // WorkflowDefinition + ?type=gatePassFlows — the 6 gate-pass workflow
        // codes, published only. workflow_definition has no display_name
        // column (unlike the generic value_column/display_column path
        // below) and holds one row per version, so a plain SELECT would
        // otherwise repeat/mismatch.
        if ("WorkflowDefinition".equalsIgnoreCase(code) && "gatePassFlows".equalsIgnoreCase(type)) {
            return jdbc.queryForList(
                "SELECT DISTINCT code AS value, code AS label FROM " + table +
                " WHERE status='PUBLISHED' AND code IN " +
                "('M10.TankerInbound','M10.TankerOutbound','M10.PackLoading','M10.PackUnload','M10.FGLoading','M10.ReturnStockIn') " +
                "ORDER BY code");
        }   
        String valueColumn = (String) reg.get("value_column");
        String displayColumn = (String) reg.get("display_column");
        if (valueColumn == null || displayColumn == null) {
            return List.of();
        }
        assertSafe(valueColumn);
        assertSafe(displayColumn);

        String scope = readScope(table);
        String where = scope.isEmpty() ? "" : " WHERE " + scope.substring(" AND ".length());
        return jdbc.queryForList(
            "SELECT " + valueColumn + " AS value, " + displayColumn + " AS label " +
            "FROM " + table + where + " ORDER BY " + displayColumn,
            scopeArgs(table));
    }

    private Map<String, Object> lookupRegistryWithLookup(String code) {
        if (!SAFE_IDENT.matcher(code).matches()) {
            throw new IllegalArgumentException("Bad entity code: " + code);
        }
        try {
            return jdbc.queryForMap(
                "SELECT code, display_name, table_name, value_column, display_column " +
                "FROM entity_registry WHERE code=? AND tenant_id IN (?, -1) ORDER BY tenant_id DESC LIMIT 1", code, TenantContext.get());
        } catch (Exception e) {
            // V86 not applied yet — fall back to core lookup so /options
            // gracefully returns empty rather than 500.
            return lookupRegistry(code);
        }
    }

    // ---- internal helpers --------------------------------------------------

    private Map<String, Object> lookupRegistry(String code) {
        if (!SAFE_IDENT.matcher(code).matches()) {
            throw new IllegalArgumentException("Bad entity code: " + code);
        }
        try {
            // Try the new shape (RBAC columns); fall back to the old shape if V33
            // hasn't applied yet (e.g. fresh upgrade in flight).
            try {
                return jdbc.queryForMap(
                    "SELECT code, display_name, table_name, read_role, write_role, approver_role " +
                    "FROM entity_registry WHERE code=? AND tenant_id IN (?, -1) ORDER BY tenant_id DESC LIMIT 1", code, TenantContext.get());
            } catch (Exception eNew) {
                return jdbc.queryForMap(
                    "SELECT code, display_name, table_name FROM entity_registry WHERE code=? AND tenant_id IN (?, -1) ORDER BY tenant_id DESC LIMIT 1", code, TenantContext.get());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown entity: " + code);
        }
    }

    private void assertSafe(String ident) {
        if (ident == null || !SAFE_IDENT.matcher(ident).matches()) {
            throw new IllegalArgumentException("Unsafe identifier: " + ident);
        }
    }

    /**
     * Enforce the lifecycle invariant on create: a WORKFLOW-class entity
     * (entity_registry.lifecycle_kind='WORKFLOW') with a single bound
     * workflow_code gets a workflow instance started and its
     * workflow_instance_id + current_step stamped, so its lifecycle is
     * engine-driven from birth. DEFINED / CHILD / LEDGER entities and MULTI-
     * variant workflows (resolved at runtime by purpose, e.g. GatePass) are
     * left alone. Best-effort: a workflow-start failure never breaks the insert
     * — the backfill migration reconciles any row left without an instance.
     */
    private void maybeStartWorkflow(String code, String table, Long id) {
        if (workflowEngine == null || id == null || id <= 0) return;
        String kind, wf;
        try {
            Map<String, Object> life = jdbc.queryForMap(
                    "SELECT lifecycle_kind, workflow_code FROM entity_registry WHERE code=? AND tenant_id IN (?, -1) ORDER BY tenant_id DESC LIMIT 1", code, TenantContext.get());
            kind = (String) life.get("lifecycle_kind");
            wf = (String) life.get("workflow_code");
        } catch (Exception e) {
            return; // pre-V116 schema or no registry row — nothing to enforce
        }
        if (!"WORKFLOW".equals(kind) || wf == null || wf.isBlank() || "MULTI".equals(wf)) return;
        if (!tableHasColumn(table, "workflow_instance_id")) return;
        try {
            var inst = workflowEngine.startInstance(wf, java.util.Map.of("businessRef", code + "#" + id));
            List<Object> args = new ArrayList<>();
            StringBuilder sets = new StringBuilder("workflow_instance_id=?");
            args.add(inst.getId());
            if (tableHasColumn(table, "current_step")) { sets.append(", current_step=?"); args.add(inst.getCurrentState()); }
            else if (tableHasColumn(table, "current_state")) { sets.append(", current_state=?"); args.add(inst.getCurrentState()); }
            args.add(id);
            // Scoped even though `id` is the row we just inserted and therefore
            // always ours: keeping "no unscoped write in this class" as a rule with
            // zero exceptions makes it greppable. An exception justified by a
            // comment is how the next unscoped write gets added.
            if (tableHasColumn(table, "tenant_id")) args.add(TenantContext.get());
            jdbc.update("UPDATE " + table + " SET " + sets + " WHERE id=?" + writeScope(table),
                    args.toArray());
        } catch (Exception ex) {
            // Never break entity creation because workflow start failed.
        }
    }

    private boolean tableHasColumn(String table, String col) {
        try {
            // EXTRA is empty for normal columns; for generated columns it
            // contains 'VIRTUAL GENERATED' or 'STORED GENERATED'. We must
            // filter those out — INSERT into a generated column raises
            // "value specified for generated column has been ignored".
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                            "WHERE table_schema=DATABASE() AND table_name=? AND column_name=? " +
                            "  AND EXTRA NOT LIKE '%GENERATED%'",
                    Integer.class, table, col);
            return n != null && n > 0;
        } catch (Exception e) { return false; }
    }
}
