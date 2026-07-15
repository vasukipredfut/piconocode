package com.aria.action.handler;

import com.aria.entity_registry.EntityRegistryService;
import com.aria.entity_registry.model.EntityFieldRegistryEntry;
import com.aria.entity_registry.model.EntityRegistryEntry;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Thin generic INSERT / UPDATE / soft-DELETE over the entity registry —
 * the engine the save / save.and.* / delete action handlers share.
 *
 * <p>Field codes are validated against {@code entity_field_registry} so the
 * tenant cannot smuggle SQL through column names. Values are bound
 * parameterised — no string concatenation of user input.
 */
@Component
public class EntityWriter {

    private static final Logger log = LoggerFactory.getLogger(EntityWriter.class);
    private static final Pattern IDENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    @Autowired private EntityRegistryService registry;
    private JdbcTemplate jdbc;

    @Autowired
    public void setDataSource(@Qualifier("psqlDataSource") DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    public EntityWriter() {}

    EntityWriter(EntityRegistryService r, JdbcTemplate j) { this.registry = r; this.jdbc = j; }

    public Long upsert(String entityCode, Long id, Map<String, Object> formData) {
        EntityRegistryEntry e = registry.findByCode(entityCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown entity: " + entityCode));
        Map<String, String> allowed = new HashMap<>();
        for (EntityFieldRegistryEntry f : registry.fieldsFor(entityCode)) {
            allowed.put(f.getFieldCode(), f.getDataType());
        }
        // Filter out fields not in registry; protect identifier safety.
        Map<String, Object> safe = new LinkedHashMap<>();
        if (formData != null) {
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                if (!IDENT.matcher(entry.getKey()).matches()) continue;
                if (!allowed.containsKey(entry.getKey())) continue;
                if ("id".equals(entry.getKey())) continue; // never write id directly
                if ("tenant_id".equals(entry.getKey())) continue; // server-controlled
                safe.put(entry.getKey(), entry.getValue());
            }
        }
        if (safe.isEmpty()) {
            throw new IllegalArgumentException("No writable fields in formData for entity " + entityCode);
        }
        String table = e.getTableName();
        // Force tenant_id on writes for tenant-scoped tables.
        boolean tenantScoped = allowed.containsKey("tenant_id");
        if (id == null) {
            // INSERT
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (");
            List<String> cols = new ArrayList<>(safe.keySet());
            if (tenantScoped) cols.add("tenant_id");
            sql.append(String.join(", ", cols)).append(") VALUES (");
            sql.append(String.join(", ", java.util.Collections.nCopies(cols.size(), "?"))).append(")");
            List<Object> args = new ArrayList<>(safe.values());
            if (tenantScoped) args.add(TenantContext.get());
            final String finalSql = sql.toString();
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(new PreparedStatementCreator() {
                @Override public PreparedStatement createPreparedStatement(Connection con) throws java.sql.SQLException {
                    PreparedStatement ps = con.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
                    for (int i = 0; i < args.size(); i++) ps.setObject(i + 1, args.get(i));
                    return ps;
                }
            }, kh);
            Number key = kh.getKey();
            return key == null ? null : key.longValue();
        } else {
            // UPDATE
            StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");
            List<String> setParts = new ArrayList<>();
            List<Object> args = new ArrayList<>();
            for (Map.Entry<String, Object> entry : safe.entrySet()) {
                setParts.add(entry.getKey() + " = ?");
                args.add(entry.getValue());
            }
            sql.append(String.join(", ", setParts));
            sql.append(" WHERE id = ?");
            args.add(id);
            if (tenantScoped) { sql.append(" AND tenant_id = ?"); args.add(TenantContext.get()); }
            int n = jdbc.update(sql.toString(), args.toArray());
            if (n == 0) throw new IllegalArgumentException("update_no_match: id=" + id);
            return id;
        }
    }

    public void softDelete(String entityCode, Long id) {
        EntityRegistryEntry e = registry.findByCode(entityCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown entity: " + entityCode));
        Map<String, String> allowed = new HashMap<>();
        for (EntityFieldRegistryEntry f : registry.fieldsFor(entityCode)) {
            allowed.put(f.getFieldCode(), f.getDataType());
        }
        boolean tenantScoped = allowed.containsKey("tenant_id");
        boolean hasStatus = allowed.containsKey("status");
        if (id == null) throw new IllegalArgumentException("recordId required for delete");
        // Prefer status = 'DELETED' if the column exists, else hard delete.
        String sql;
        List<Object> args = new ArrayList<>();
        if (hasStatus) {
            sql = "UPDATE " + e.getTableName() + " SET status = 'DELETED' WHERE id = ?";
            args.add(id);
        } else {
            sql = "DELETE FROM " + e.getTableName() + " WHERE id = ?";
            args.add(id);
        }
        if (tenantScoped) { sql += " AND tenant_id = ?"; args.add(TenantContext.get()); }
        int n = jdbc.update(sql, args.toArray());
        if (n == 0) throw new IllegalArgumentException("delete_no_match: id=" + id);
        log.info("action.delete entity={} id={} sql={}", entityCode, id, sql);
    }
}
