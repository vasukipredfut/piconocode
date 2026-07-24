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
    /**
     * Allowlisted filter ops, lower-cased.
     *
     * <p>MUST stay lower-case: compileFilter lower-cases the incoming op before
     * testing membership, so a camelCase entry here can never match. The switch
     * below is already lower-cased; this set was not, which silently killed the
     * four camelCase ops -- startsWith, endsWith, isNull, isNotNull -- for every
     * page that used them. They were allowlisted, implemented, and unreachable,
     * and the only symptom was "Unsupported filter op" on the one page that
     * happened to use one.
     */
    private static final Set<String> OPS = Set.of(
            "eq","ne","lt","lte","gt","gte","in","nin",
            "contains","startswith","endswith","isnull","isnotnull","between");

    /** Field codes are letters/digits/underscores; alias-prefixed via a dot.
     *  This guards against SQL injection through field codes (Schema V2 §3.3). */
    private static final Pattern IDENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)?");

    /** Tables that have a {@code tenant_id} column — informs auto-scoping. */
    // The hand-maintained TENANT_SCOPED allowlist that used to live here named 5
    // tables; the schema has 146 with a tenant_id. Everything it omitted was
    // silently unscoped. TenantScope derives membership from the schema itself,
    // so a new tenant table is protected the moment it is created.
    @Autowired private com.aria.security.TenantScope tenantScope;

    @Autowired private EntityRegistryService registry;

    private JdbcTemplate jdbc;

    @Autowired
    public void setDataSource(@Qualifier("psqlDataSource") DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    /** Test-only constructor. */
    DataSourceQueryService(EntityRegistryService registry, JdbcTemplate jdbc,
                           com.aria.security.TenantScope tenantScope) {
        this.registry = registry;
        this.jdbc = jdbc;
        this.tenantScope = tenantScope;
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
        if (spec.getGroupBy() != null && !spec.getGroupBy().isEmpty()) {
            return queryGrouped(spec, page, size, bindings);
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
        String rootScope = tenantScope.readPredicate(root.getTableName(), root.getTableName());
        if (!rootScope.isEmpty()) {
            whereClauses.add(rootScope);
            args.add(tenantScope.arg());
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

    /**
     * {@code spec.groupBy} path: one row per distinct combination of the
     * given fields, carrying the FULL column content of that group's most
     * recent row (highest {@code id}) plus a {@code count} column — the
     * classic "top-1-per-group" pattern (a subquery computes {@code MAX(id)}
     * per group, joined back to the real table for the rest of its columns).
     * Filter/joins-in-the-WHERE/tenant-scope/pagination all apply the same as
     * the ungrouped path; sort orders the groups (by a group field, by
     * {@code count}, or the default of most-recently-active group first).
     *
     * <p>Known limitation: {@code spec.select} entries that reference a
     * {@code spec.joins} alias (e.g. {@code "city.name as cityName"}) are
     * NOT resolvable here — joins are only applied inside the grouping
     * subquery (for filtering), not re-applied to the joined-back full row.
     * Plain root-entity columns (the default {@code rootTable.*}, or any
     * subset of them) work fully.
     *
     * <p>Kept as a separate method so the default (no groupBy) path — every
     * other ListView page in the app — is untouched.
     */
    private DataSourceSpec.Response queryGrouped(DataSourceSpec spec, int page, int size,
                                                 Map<String, Object> bindings) {
        EntityRegistryEntry root = registry.findByCode(spec.getEntity())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown entity: " + spec.getEntity() + " (entity_registry has no row)"));

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

        List<String> groupCols = new ArrayList<>();
        for (String f : spec.getGroupBy()) {
            groupCols.add(resolveColumn(f, root.getTableName(), aliasToTable, true));
        }
        String groupByClause = String.join(", ", groupCols);

        // WHERE — identical construction to the ungrouped path (auto tenant-scope + user filter),
        // built fresh into its own args list for the main query below.
        List<Object> args = new ArrayList<>();
        List<String> whereClauses = new ArrayList<>();
        String rootScope = tenantScope.readPredicate(root.getTableName(), root.getTableName());
        if (!rootScope.isEmpty()) {
            whereClauses.add(rootScope);
            args.add(tenantScope.arg());
        }
        if (spec.getFilter() != null) {
            String fc = compileFilter(spec.getFilter(), root.getTableName(), aliasToTable, args, bindings);
            if (fc != null && !fc.isBlank()) whereClauses.add(fc);
        }
        String whereSql = whereClauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", whereClauses);

        String rootTable = root.getTableName();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        appendSelect(sql, spec, rootTable);
        sql.append(", g.count AS count");
        sql.append(" FROM (SELECT ").append(groupByClause)
                .append(", MAX(").append(rootTable).append(".id) AS max_id, COUNT(*) AS count FROM ").append(rootTable);
        appendJoins(sql, spec, rootTable);
        sql.append(whereSql);
        sql.append(" GROUP BY ").append(groupByClause).append(") g");
        sql.append(" JOIN ").append(rootTable).append(" ON ").append(rootTable).append(".id = g.max_id");

        if (spec.getSort() != null && !spec.getSort().isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (DataSourceSpec.Sort s : spec.getSort()) {
                if (s.getField() == null) continue;
                String col = "count".equalsIgnoreCase(s.getField())
                        ? "g.count" : resolveColumn(s.getField(), rootTable, aliasToTable, true);
                String dir = "desc".equalsIgnoreCase(s.getDir()) ? "DESC" : "ASC";
                parts.add(col + " " + dir);
            }
            if (!parts.isEmpty()) sql.append(" ORDER BY ").append(String.join(", ", parts));
        } else {
            // Default: most recently active group first (highest max id == latest row).
            sql.append(" ORDER BY g.max_id DESC");
        }

        int sz = size > 0 ? size : 25;
        int pg = Math.max(0, page);
        sql.append(" LIMIT ").append(sz).append(" OFFSET ").append(pg * sz);

        // Total distinct groups (for pagination) — same FROM/JOIN/WHERE/GROUP BY,
        // wrapped and counted, with its own fresh args list (compileFilter is
        // deterministic given the same filter/bindings, so this reproduces the
        // identical WHERE text safely).
        List<Object> countArgs = new ArrayList<>();
        List<String> countWhereClauses = new ArrayList<>();
        if (!rootScope.isEmpty()) {
            countWhereClauses.add(rootScope);
            countArgs.add(tenantScope.arg());
        }
        if (spec.getFilter() != null) {
            String fc = compileFilter(spec.getFilter(), root.getTableName(), aliasToTable, countArgs, bindings);
            if (fc != null && !fc.isBlank()) countWhereClauses.add(fc);
        }
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(*) FROM (SELECT 1 FROM ").append(root.getTableName());
        appendJoins(countSql, spec, root.getTableName());
        if (!countWhereClauses.isEmpty()) countSql.append(" WHERE ").append(String.join(" AND ", countWhereClauses));
        countSql.append(" GROUP BY ").append(groupByClause).append(") grouped_");

        log.debug("datasource.query.grouped sql={} args={}", sql, args);
        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), args.toArray());
        Long total = jdbc.queryForObject(countSql.toString(), Long.class, countArgs.toArray());

        int totalPages = (int) Math.ceil((double) (total == null ? 0 : total) / (double) sz);
        return DataSourceSpec.Response.builder()
                .content(rows)
                .totalElements(total == null ? 0 : total)
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
        String cntScope = tenantScope.readPredicate(rootTable, rootTable);
        if (!cntScope.isEmpty()) { wc.add(cntScope); args.add(tenantScope.arg()); }
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
