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

    public GenericEntityController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
                    "SELECT field_code, display_label, data_type, filterable, sortable, " +
                            "searchable, required, widget_kind, options_json, " +
                            "visible_when, required_when, computed_from " +
                            "FROM entity_field_registry " +
                            "WHERE entity_code=? ORDER BY id", code);
        } catch (Exception e) {
            try {
                fields = jdbc.queryForList(
                        "SELECT field_code, display_label, data_type, filterable, sortable, " +
                                "searchable, required, widget_kind, options_json " +
                                "FROM entity_field_registry " +
                                "WHERE entity_code=? ORDER BY id", code);
            } catch (Exception e2) {
                fields = jdbc.queryForList(
                        "SELECT field_code, display_label, data_type, filterable, sortable, " +
                                "searchable, required FROM entity_field_registry " +
                                "WHERE entity_code=? ORDER BY id", code);
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

    @GetMapping("/{code}/{id}")
    public ResponseEntity<?> getRow(@PathVariable String code, @PathVariable long id) {
        Map<String, Object> reg = lookupRegistry(code);
        ResponseEntity<?> denied = checkRole(reg, "read");
        if (denied != null) return denied;
        String table = (String) reg.get("table_name");
        assertSafe(table);
        try {
            Map<String, Object> row = jdbc.queryForMap("SELECT * FROM " + table + " WHERE id=?", id);
            maskSecretsInPlace(code, row);
            return ResponseEntity.ok(row);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "Row not found: id=" + id + " in " + table));
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
                "SELECT field_code FROM entity_field_registry WHERE entity_code=?",
                String.class, code).stream().collect(java.util.stream.Collectors.toSet());
        Map<String, Object> safe = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : body.entrySet()) {
            if (!SAFE_IDENT.matcher(e.getKey()).matches()) continue;
            if ((declared.contains(e.getKey()) || e.getKey().equals("tenant_id"))
                    && tableHasColumn(table, e.getKey())) {
                safe.put(e.getKey(), e.getValue());
            }
        }
        if (safe.isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "No valid fields in body (expected any of " + declared + ")"));
        }
        if (!safe.containsKey("tenant_id") && tableHasColumn(table, "tenant_id")) {
            safe.put("tenant_id", TenantContext.get());
        }
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
            result.put("inserted", safe);
            if (lastId != null && lastId > 0) result.put("id", lastId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                Map.of("ok", false, "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
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
                "SELECT field_code FROM entity_field_registry WHERE entity_code=?",
                String.class, code).stream().collect(java.util.stream.Collectors.toSet());
        Map<String, Object> safe = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : body.entrySet()) {
            if (!SAFE_IDENT.matcher(e.getKey()).matches()) continue;
            if (!declared.contains(e.getKey())) continue;
            if (e.getKey().equals("id")) continue;
            safe.put(e.getKey(), e.getValue());
        }
        encryptSecretsInPlace(code, safe);
        if (safe.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "no updatable fields"));

        Map<String, Object> before = null;
        try { before = jdbc.queryForMap("SELECT * FROM " + table + " WHERE id=?", id); } catch (Exception ignored) {}

        StringBuilder sets = new StringBuilder();
        for (String k : safe.keySet()) {
            if (sets.length() > 0) sets.append(", ");
            sets.append(k).append("=?");
        }
        List<Object> args = new ArrayList<>(safe.values());
        args.add(id);
        String sql = "UPDATE " + table + " SET " + sets + " WHERE id=?";
        try {
            int n = jdbc.update(sql, args.toArray());
            writeAudit("UPDATE", code, id, before, safe);
            return ResponseEntity.ok(Map.of("ok", n > 0, "updated", n, "id", id));
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
        try { before = jdbc.queryForMap("SELECT * FROM " + table + " WHERE id=?", id); } catch (Exception ignored) {}
        try {
            int n = jdbc.update("DELETE FROM " + table + " WHERE id=?", id);
            writeAudit("DELETE", code, id, before, null);
            return ResponseEntity.ok(Map.of("ok", n > 0, "deleted", n, "id", id));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                Map.of("ok", false, "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    // ---- RBAC --------------------------------------------------------------

    /** Returns a 403 ResponseEntity when the current user can't perform the
     *  given access ("read"/"write") on the entity, else null. */
    private ResponseEntity<?> checkRole(Map<String, Object> reg, String access) {
        String allowed = (String) reg.get("write".equals(access) ? "write_role" : "read_role");
        // If the column is null or "*" → no restriction beyond authentication.
        if (allowed == null || allowed.trim().isEmpty() || "*".equals(allowed.trim())) return null;
        String currentName = currentRoleName();
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
            jdbc.update(
                "INSERT INTO m0_audit_log (tenant_id, ts, actor_id, action_kind, entity_code, entity_id, message, payload_json) " +
                "VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?)",
                TenantContext.get(), currentUserId(), kind, entityCode, entityId,
                message, afterJson != null ? afterJson : beforeJson);
        } catch (Exception ignored) {
            // Audit-log writes must never break the mutation that triggered them.
            // The page returns ok and downstream consumers can compare DB to
            // payload if the audit row is missing.
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

        if ("PageDefinition".equalsIgnoreCase(code) && "category".equalsIgnoreCase(type)) {
            return jdbc.queryForList(
                "SELECT DISTINCT category AS value, category AS label FROM " + table +
                " WHERE category IS NOT NULL ORDER BY category");
        }

        String valueColumn = (String) reg.get("value_column");
        String displayColumn = (String) reg.get("display_column");
        if (valueColumn == null || displayColumn == null) {
            return List.of();
        }
        assertSafe(valueColumn);
        assertSafe(displayColumn);

        return jdbc.queryForList(
            "SELECT " + valueColumn + " AS value, " + displayColumn + " AS label " +
            "FROM " + table + " ORDER BY " + displayColumn);
    }

    private Map<String, Object> lookupRegistryWithLookup(String code) {
        if (!SAFE_IDENT.matcher(code).matches()) {
            throw new IllegalArgumentException("Bad entity code: " + code);
        }
        try {
            return jdbc.queryForMap(
                "SELECT code, display_name, table_name, value_column, display_column " +
                "FROM entity_registry WHERE code=?", code);
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
                    "FROM entity_registry WHERE code=?", code);
            } catch (Exception eNew) {
                return jdbc.queryForMap(
                    "SELECT code, display_name, table_name FROM entity_registry WHERE code=?", code);
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
                    "SELECT lifecycle_kind, workflow_code FROM entity_registry WHERE code=?", code);
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
            jdbc.update("UPDATE " + table + " SET " + sets + " WHERE id=?", args.toArray());
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
