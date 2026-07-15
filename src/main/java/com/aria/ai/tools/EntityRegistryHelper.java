package com.aria.ai.tools;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Shared safety layer for every {@link AgentTool} that touches the DB.
 *
 * <p>Enforces the same invariants as {@code GenericEntityController}:
 * <ul>
 *   <li>Every identifier that ends up in SQL matches {@code SAFE_IDENT}.</li>
 *   <li>Every entity code resolves via {@code entity_registry}.</li>
 *   <li>Every column referenced in a filter exists in
 *       {@code entity_field_registry} for that entity.</li>
 * </ul>
 * Caches the field allowlist per entity — cheap after the first hit and
 * cleared on schema changes via {@link #clearCache()}.
 */
@Service
public class EntityRegistryHelper {

    public static final Pattern SAFE_IDENT = Pattern.compile("^[A-Za-z0-9_]+$");

    private final JdbcTemplate jdbc;
    private final Map<String, Set<String>> fieldCache = new ConcurrentHashMap<>();

    public EntityRegistryHelper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Whether the string is safe to string-concat into SQL. */
    public boolean safeIdent(String s) {
        return s != null && SAFE_IDENT.matcher(s).matches();
    }

    /** All entities visible in the registry. Ordered alphabetically. */
    public List<Map<String, Object>> listEntities() {
        return jdbc.queryForList(
                "SELECT code, display_name, table_name FROM entity_registry ORDER BY code");
    }

    /** Registry row for one code — {@code null} if unknown. */
    public Map<String, Object> lookup(String code) {
        if (!safeIdent(code)) return null;
        try {
            return jdbc.queryForMap(
                    "SELECT code, display_name, table_name, read_role, write_role " +
                    "FROM entity_registry WHERE code=?", code);
        } catch (Exception e) {
            return null;
        }
    }

    /** Full field-registry rows for one entity — for {@code entity.schema} tool. */
    public List<Map<String, Object>> fields(String code) {
        if (!safeIdent(code)) return List.of();
        try {
            return jdbc.queryForList(
                    "SELECT field_code, display_label, data_type, widget_kind, " +
                    "filterable, sortable, searchable, required " +
                    "FROM entity_field_registry WHERE entity_code=? ORDER BY id", code);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Allowlist of column names for one entity — cached. */
    public Set<String> allowedColumns(String code) {
        return fieldCache.computeIfAbsent(code, k -> {
            try {
                return jdbc.queryForList(
                        "SELECT field_code FROM entity_field_registry WHERE entity_code=?",
                        String.class, k).stream()
                        .filter(this::safeIdent)
                        .collect(Collectors.toUnmodifiableSet());
            } catch (Exception e) {
                return Set.of();
            }
        });
    }

    /** Force-clear the cache — call after DDL changes. */
    public void clearCache() {
        fieldCache.clear();
    }

    /**
     * Whether the physical table has a {@code tenant_id} column.
     * Some legacy tables don't; tools skip tenant scoping for those.
     */
    public boolean hasTenantId(String tableName) {
        if (!safeIdent(tableName)) return false;
        try {
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema=DATABASE() AND table_name=? AND column_name='tenant_id'",
                    Integer.class, tableName);
            return n != null && n > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Shallow-clone helper — the LLM output can arrive as immutable maps. */
    public static Map<String, Object> mutable(Map<String, Object> src) {
        return src == null ? new LinkedHashMap<>() : new LinkedHashMap<>(src);
    }
}
