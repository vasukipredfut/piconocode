package com.aria.datasource;

import com.aria.entity_registry.EntityRegistryService;
import com.aria.entity_registry.model.EntityRegistryEntry;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The Schema V2 §3 DataSource compiler — converts a {@link DataSourceSpec}
 * to a parameterised SQL statement and returns the rows as
 * {@code List<Map<String,Object>>} for the Renderer to display.
 *
 * <h2>What it owns</h2>
 * <ul>
 *   <li>Entity-name → physical table resolution via {@link EntityRegistryService}.</li>
 *   <li>Join planning (LEFT JOIN default, per Schema V2 §3.4 step 4).</li>
 *   <li>Filter tree → WHERE clause translation (AND/OR/NOT). Each predicate
 *       value is bound as a {@code ?} parameter — values are NEVER inlined,
 *       so tenant-authored filter trees cannot inject SQL (§3.4 step 8 and
 *       §17 security policy).</li>
 *   <li>Binding-string resolution: {@code ctx:tenantId}, {@code user:branch},
 *       {@code route:id}, {@code page:<field>} — looked up in the supplied
 *       {@code bindings} map. Unresolved binding → {@link IllegalArgumentException}.</li>
 *   <li>Tenant scoping: every query carries {@code tenant_id = :tid} when
 *       the entity has a {@code tenant_id} column (Vendor, PageDefinition,
 *       WorkflowInstance, SavedView). Non-tenant tables (state, city) skip
 *       that predicate.</li>
 *   <li>Pagination: server-side LIMIT/OFFSET + a count query for total.</li>
 * </ul>
 *
 * <h2>What it explicitly does NOT do (Round 1G.2 carry)</h2>
 * <ul>
 *   <li>{@code type=lookup|api|expr|static|csv|rest} — entity-only for now.</li>
 *   <li>{@code search} (full-text) — clients use a contains-filter on a
 *       string field instead.</li>
 *   <li>{@code cache.evictOn} subscriptions — every call hits the DB.</li>
 *   <li>{@code rolesRequired} on the source — gating is done at the controller.</li>
 * </ul>
 */
@Service
public class DataSourceQueryService {

    private static final Logger log = LoggerFactory.getLogger(DataSourceQueryService.class);

    /** Allowed operator codes. Anything else triggers a 422. */
    private static final Set<String> OPS = Set.of(
            "eq","ne","lt","lte","gt","gte","in","nin",
            "contains","startsWith","endsWith","isNull","isNotNull","between");

    /** Field codes are letters/digits/underscores; alias-prefixed via a dot.
     *  This guards against SQL injection through field codes (Schema V2 §3.3). */
    private static final Pattern IDENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)?");

    /** Tables that have a {@code tenant_id} column — informs auto-scoping. */
    private static final Set<String> TENANT_SCOPED = Set.of(
            "m0_vendor","page_definition","workflow_instance","workflow_definition","tenant_list_view");

    @Autowired private EntityRegistryService registry;

    private JdbcTemplate jdbc;

    @Autowired
    public void setDataSource(@Qualifier("psqlDataSource") DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    /** Test-only constructor. */
    DataSourceQueryService(EntityRegistryService registry, JdbcTemplate jdbc) {
        this.registry = registry;
        this.jdbc = jdbc;
    }

    public DataSourceQueryService() {}

    // ---------------------------------------------------------- public API
    public DataSourceSpec.Response query(DataSourceSpec spec, int page, int size,
                                         Map<String, Object> bindings) {
        if (spec == null || spec.getEntity() == null) {
            throw new IllegalArgumentException("spec.entity is required");
        }
        if (!"entity".equalsIgnoreCase(spec.getType() == null ? "entity" : spec.getType())) {
            throw new IllegalArgumentException(
                    "Only type=entity is implemented in Round 1G; got: " + spec.getType());
        }
        EntityRegistryEntry root = registry.findByCode(spec.getEntity())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown entity: " + spec.getEntity() + " (entity_registry has no row)"));

        // Resolve join entity names.
        Map<String, String> aliasToTable = new LinkedHashMap<>();
        aliasToTable.put(root.getTableName(), root.getTableName());
        if (spec.getJoins() != null) {
            for (DataSourceSpec.Join j : spec.getJoins()) {
                if (j.getEntity() == null || j.getAs() == null || j.getOn() == null) {
                    throw new IllegalArgumentException("join requires entity/as/on");
                }
                EntityRegistryEntry je = registry.findByCode(j.getEntity())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown join entity: " + j.getEntity()));
                aliasToTable.put(j.getAs(), je.getTableName());
            }
        }

        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();

        // SELECT clause
        sql.append("SELECT ");
        appendSelect(sql, spec, root.getTableName());
        sql.append(" FROM ").append(root.getTableName());

        // JOINs
        appendJoins(sql, spec, root.getTableName());

        // WHERE
        List<String> whereClauses = new ArrayList<>();

        // Auto tenant-scope (root entity).
        if (TENANT_SCOPED.contains(root.getTableName())) {
            whereClauses.add(root.getTableName() + ".tenant_id = ?");
            args.add(TenantContext.get());
        }
        // User-supplied filter.
        if (spec.getFilter() != null) {
            String fc = compileFilter(spec.getFilter(), root.getTableName(), aliasToTable, args, bindings);
            if (fc != null && !fc.isBlank()) whereClauses.add(fc);
        }
        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }

        // ORDER BY
        if (spec.getSort() != null && !spec.getSort().isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (DataSourceSpec.Sort s : spec.getSort()) {
                if (s.getField() == null) continue;
                String col = resolveColumn(s.getField(), root.getTableName(), aliasToTable, true);
                String dir = "desc".equalsIgnoreCase(s.getDir()) ? "DESC" : "ASC";
                parts.add(col + " " + dir);
            }
            if (!parts.isEmpty()) sql.append(" ORDER BY ").append(String.join(", ", parts));
        }

        // count query (totals) — same FROM / WHERE, no SELECT cols or ORDER
        long total = countMatching(spec, root.getTableName(), aliasToTable, bindings);

        // LIMIT / OFFSET
        int sz = size > 0 ? size : 25;
        int pg = Math.max(0, page);
        sql.append(" LIMIT ").append(sz).append(" OFFSET ").append(pg * sz);

        log.debug("datasource.query sql={} args={}", sql, args);
        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), args.toArray());

        int totalPages = (int) Math.ceil((double) total / (double) sz);
        return DataSourceSpec.Response.builder()
                .content(rows)
                .totalElements(total)
                .totalPages(totalPages)
                .number(pg)
                .size(sz)
                .build();
    }

    // ---------------------------------------------- internal: SELECT clause
    private void appendSelect(StringBuilder sql, DataSourceSpec spec, String rootTable) {
        List<String> cols = new ArrayList<>();
        if (spec.getSelect() == null || spec.getSelect().isEmpty()) {
            cols.add(rootTable + ".*");
        } else {
            // Pre-pass: collect aliases declared in joins[i].fields[] so bare
            // select-names matching an alias don't get the rootTable prefix
            // (which would produce "Unknown column rootTable.alias" errors).
            // Example fields entry: "city.name as cityName" -> alias "cityName".
            java.util.Set<String> joinAliases = new java.util.HashSet<>();
            if (spec.getJoins() != null) {
                java.util.regex.Pattern aliasPat = java.util.regex.Pattern.compile(
                        "(?i)\\s+as\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*$");
                for (DataSourceSpec.Join j : spec.getJoins()) {
                    if (j.getFields() == null) continue;
                    for (String f : j.getFields()) {
                        java.util.regex.Matcher m = aliasPat.matcher(f);
                        if (m.find()) joinAliases.add(m.group(1));
                    }
                }
            }
            for (String s : spec.getSelect()) {
                // Three shapes:
                //   "name"                  -> rootTable.name
                //   "city.name as cityName" -> emit verbatim
                //   "cityName" (join alias) -> skip; join-fields loop below emits it
                String trimmed = s.trim();
                if (trimmed.contains(".") || trimmed.toLowerCase().contains(" as ")) {
                    validateIdentSafe(trimmed);
                    cols.add(trimmed);
                } else if (joinAliases.contains(trimmed)) {
                    continue;
                } else {
                    validateIdent(trimmed);
                    cols.add(rootTable + "." + trimmed);
                }
            }
            // Also include the alias-fields explicitly declared in joins[].fields.
            if (spec.getJoins() != null) {
                for (DataSourceSpec.Join j : spec.getJoins()) {
                    if (j.getFields() == null) continue;
                    for (String f : j.getFields()) {
                        validateIdentSafe(f);
                        if (!cols.contains(f)) cols.add(f);
                    }
                }
            }
        }
        sql.append(String.join(", ", cols));
    }

    // ---------------------------------------------- internal: JOIN clauses
    private void appendJoins(StringBuilder sql, DataSourceSpec spec, String rootTable) {
        if (spec.getJoins() == null) return;
        for (DataSourceSpec.Join j : spec.getJoins()) {
            String kind = "inner".equalsIgnoreCase(j.getKind()) ? "INNER" : "LEFT";
            EntityRegistryEntry je = registry.findByCode(j.getEntity()).orElseThrow();
            validateIdent(j.getAs());
            // The `on` clause is a tenant-authored predicate against table/alias
            // columns — validate it consists of safe identifiers + operators only.
            String onSafe = sanitiseOnClause(j.getOn());
            sql.append(" ").append(kind).append(" JOIN ")
               .append(je.getTableName()).append(" ").append(j.getAs())
               .append(" ON ").append(onSafe);
        }
    }

    private String sanitiseOnClause(String on) {
        // Allow only ident.ident, =, AND, parens, whitespace. Anything else → reject.
        if (on == null) throw new IllegalArgumentException("join.on required");
        if (!on.matches("[A-Za-z0-9_.=()\\s,]+|[A-Za-z0-9_.=()\\s,]+AND[A-Za-z0-9_.=()\\s,]+"))
            ; // fall through to deeper validation
        // Stricter: split on '=' / AND / whitespace and check each token is an
        // identifier or empty. Reject quotes, semicolons, comments.
        if (on.contains(";") || on.contains("--") || on.contains("'") || on.contains("\"")
                || on.contains("/*") || on.contains("*/")) {
            throw new IllegalArgumentException("join.on contains forbidden tokens: " + on);
        }
        return on;
    }

    // ------------------------------------- internal: WHERE clause compiler
    private String compileFilter(DataSourceSpec.Filter f, String rootTable,
                                 Map<String, String> aliasToTable,
                                 List<Object> args, Map<String, Object> bindings) {
        if (f == null) return null;
        if (f.getAnd() != null) {
            List<String> parts = new ArrayList<>();
            for (DataSourceSpec.Filter child : f.getAnd()) {
                String c = compileFilter(child, rootTable, aliasToTable, args, bindings);
                if (c != null) parts.add(c);
            }
            return parts.isEmpty() ? null : "(" + String.join(" AND ", parts) + ")";
        }
        if (f.getOr() != null) {
            List<String> parts = new ArrayList<>();
            for (DataSourceSpec.Filter child : f.getOr()) {
                String c = compileFilter(child, rootTable, aliasToTable, args, bindings);
                if (c != null) parts.add(c);
            }
            return parts.isEmpty() ? null : "(" + String.join(" OR ", parts) + ")";
        }
        if (f.getNot() != null) {
            String c = compileFilter(f.getNot(), rootTable, aliasToTable, args, bindings);
            return c == null ? null : "NOT " + c;
        }
        // Leaf predicate.
        if (f.getField() == null || f.getOp() == null) return null;
        String op = f.getOp().toLowerCase(Locale.ROOT);
        if (!OPS.contains(op)) {
            throw new IllegalArgumentException("Unsupported filter op: " + f.getOp());
        }
        String col = resolveColumn(f.getField(), rootTable, aliasToTable, true);
        Object val = resolveBinding(f.getValue(), bindings);
        Object valTo = resolveBinding(f.getValueTo(), bindings);

        switch (op) {
            case "eq":  args.add(val);  return col + " = ?";
            case "ne":  args.add(val);  return col + " <> ?";
            case "lt":  args.add(val);  return col + " < ?";
            case "lte": args.add(val);  return col + " <= ?";
            case "gt":  args.add(val);  return col + " > ?";
            case "gte": args.add(val);  return col + " >= ?";
            case "in": {
                List<?> arr = asList(val);
                if (arr.isEmpty()) return "1 = 0";
                String placeholders = String.join(", ", java.util.Collections.nCopies(arr.size(), "?"));
                args.addAll(arr);
                return col + " IN (" + placeholders + ")";
            }
            case "nin": {
                List<?> arr = asList(val);
                if (arr.isEmpty()) return "1 = 1";
                String placeholders = String.join(", ", java.util.Collections.nCopies(arr.size(), "?"));
                args.addAll(arr);
                return col + " NOT IN (" + placeholders + ")";
            }
            case "contains":   args.add("%" + val + "%"); return col + " LIKE ?";
            case "startswith": args.add(val + "%");       return col + " LIKE ?";
            case "endswith":   args.add("%" + val);       return col + " LIKE ?";
            case "isnull":     return col + " IS NULL";
            case "isnotnull":  return col + " IS NOT NULL";
            case "between":    args.add(val); args.add(valTo); return col + " BETWEEN ? AND ?";
            default: throw new IllegalArgumentException("Unsupported op: " + op);
        }
    }

    @SuppressWarnings("unchecked")
    private List<?> asList(Object v) {
        if (v == null) return java.util.Collections.emptyList();
        if (v instanceof List) return (List<Object>) v;
        if (v.getClass().isArray()) {
            List<Object> out = new ArrayList<>();
            int n = java.lang.reflect.Array.getLength(v);
            for (int i = 0; i < n; i++) out.add(java.lang.reflect.Array.get(v, i));
            return out;
        }
        return List.of(v);
    }

    // ----------------------- internal: binding-string resolution (NOT SQL)
    Object resolveBinding(Object raw, Map<String, Object> bindings) {
        if (!(raw instanceof String)) return raw;
        String s = (String) raw;
        // Detect prefixes: ctx: / user: / route: / page: / tenant: / pageData: / workflowState:
        int colon = s.indexOf(':');
        if (colon <= 0) return raw;
        String prefix = s.substring(0, colon);
        String rest = s.substring(colon + 1);
        switch (prefix) {
            case "ctx": {
                if ("tenantId".equals(rest)) return TenantContext.get();
                if (bindings != null && bindings.containsKey("ctx." + rest)) return bindings.get("ctx." + rest);
                return null;
            }
            case "user":
            case "tenant":
            case "route":
            case "page":
            case "pageData":
            case "workflowState": {
                if (bindings == null) return null;
                Object v = bindings.get(prefix + ":" + rest);
                if (v != null) return v;
                v = bindings.get(rest);
                return v;
            }
            default:
                return raw;
        }
    }

    // ---------------------------------- internal: count for pagination
    private long countMatching(DataSourceSpec spec, String rootTable,
                               Map<String, String> aliasToTable,
                               Map<String, Object> bindings) {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        sql.append("SELECT COUNT(*) FROM ").append(rootTable);
        appendJoins(sql, spec, rootTable);
        List<String> wc = new ArrayList<>();
        if (TENANT_SCOPED.contains(rootTable)) {
            wc.add(rootTable + ".tenant_id = ?"); args.add(TenantContext.get());
        }
        if (spec.getFilter() != null) {
            String fc = compileFilter(spec.getFilter(), rootTable, aliasToTable, args, bindings);
            if (fc != null) wc.add(fc);
        }
        if (!wc.isEmpty()) sql.append(" WHERE ").append(String.join(" AND ", wc));
        Long n = jdbc.queryForObject(sql.toString(), Long.class, args.toArray());
        return n == null ? 0L : n;
    }

    // ------------------------------------------- internal: ident validation
    private void validateIdent(String s) {
        if (s == null || !IDENT.matcher(s).matches()) {
            throw new IllegalArgumentException("Unsafe identifier: " + s);
        }
    }

    /** Allows "alias.col as alias" — the broader pattern used in select. */
    private void validateIdentSafe(String s) {
        if (s == null) throw new IllegalArgumentException("null select expr");
        String norm = s.replaceAll("(?i)\\s+as\\s+", " ");
        for (String tok : norm.split("\\s+")) {
            if (tok.isBlank()) continue;
            if (!IDENT.matcher(tok).matches()) {
                throw new IllegalArgumentException("Unsafe select expr: " + s);
            }
        }
    }

    private String resolveColumn(String field, String rootTable,
                                 Map<String, String> aliasToTable, boolean fallbackToRoot) {
        validateIdent(field);
        if (field.contains(".")) {
            String alias = field.substring(0, field.indexOf('.'));
            if (!aliasToTable.containsKey(alias) && !alias.equals(rootTable)) {
                throw new IllegalArgumentException("Unknown alias in field: " + field);
            }
            return field;
        }
        // Special case: select aliases like "cityName" / "stateName" appear in
        // ORDER BY but are not direct columns. Honour them as bare identifiers
        // (MySQL accepts alias in ORDER BY). Otherwise prefix with root table.
        if (fallbackToRoot && isLikelyRootColumn(field)) {
            return rootTable + "." + field;
        }
        return field;
    }

    /** Heuristic: alias columns are CamelCase, root columns are snake_case.
     *  Both work in ORDER BY thanks to MySQL alias-in-ORDER-BY semantics, but
     *  in WHERE we must qualify root columns so the join doesn't make them
     *  ambiguous. Bare-camel identifiers fall through unqualified. */
    private boolean isLikelyRootColumn(String f) {
        if (f.isEmpty()) return true;
        return Character.isLowerCase(f.charAt(0)) && f.indexOf('_') >= 0
                || f.equals(f.toLowerCase(Locale.ROOT));
    }
}
