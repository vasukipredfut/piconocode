package com.aria.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * The single authority on which tables are tenant-scoped, and on the predicate
 * that scopes them.
 *
 * <h3>Why this exists</h3>
 * Tenant scoping used to be decided by a hand-maintained allowlist in
 * {@code DataSourceQueryService}:
 * <pre>
 *   TENANT_SCOPED = Set.of("m0_vendor","page_definition","workflow_instance",
 *                          "workflow_definition","tenant_list_view");
 * </pre>
 * That list named <b>5</b> tables. The schema has <b>146</b> carrying a
 * {@code tenant_id}. Every other one was silently unscoped — not by decision,
 * but by omission — and a table added tomorrow would be unscoped too, because
 * nothing forces anyone to update the list. An allowlist fails open, and it
 * fails quietly: the only symptom is a list that returns more rows than it
 * should, which looks like data, not like a bug.
 *
 * <p>So the list is gone. <b>The schema is the source of truth:</b> if a table
 * has a {@code tenant_id} column, it is scoped. Always. A new tenant table is
 * protected the moment it is created, with no code change and nothing to
 * remember.
 *
 * <h3>Read vs write</h3>
 * <ul>
 *   <li><b>Read</b> — {@code tenant_id IN (:tid, -1)}: your rows, plus the
 *       inherited global product catalogue. Business tables hold no -1 rows, so
 *       this collapses to {@code = :tid} for them.</li>
 *   <li><b>Write</b> — {@code tenant_id = :tid}, strictly. A tenant must never
 *       update or delete a global row; that would let one firm mutate the
 *       product for every other firm. Customising a global row forks a tenant
 *       copy instead.</li>
 * </ul>
 *
 * <p>The membership set is read once from {@code information_schema} and cached:
 * it changes only when DDL runs, and every alternative (probing per query) costs
 * a round trip on the hot path.
 */
@Component
public class TenantScope {

    /** Column whose presence means "this table belongs to someone". */
    public static final String TENANT_COLUMN = "tenant_id";

    private final JdbcTemplate jdbc;
    private volatile Set<String> scopedTables;

    // @Autowired is REQUIRED, not decorative: the test-only constructor below
    // means this class has two, and Spring only auto-selects a constructor when
    // there is exactly one. Without this it falls back to a no-arg default,
    // finds none, and the whole application fails to start.
    @Autowired
    public TenantScope(@Qualifier("psqlDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    private TenantScope(Set<String> fixedMembership) {
        this.jdbc = null;
        this.scopedTables = fixedMembership;
    }

    /**
     * Test-only: a scope with fixed membership and no database behind it.
     *
     * <p>Exists because the production constructor derives membership from
     * {@code information_schema}, which a unit test has no connection to. Tests
     * that want the real derivation should use the IT suite, where Flyway builds
     * the actual schema.
     */
    public static TenantScope forTables(String... tables) {
        Set<String> t = new HashSet<>();
        for (String s : tables) t.add(s.toLowerCase(Locale.ROOT));
        return new TenantScope(t);
    }

    /** True when the table carries a tenant_id and therefore must be scoped. */
    public boolean isScoped(String table) {
        if (table == null) return false;
        return tables().contains(table.toLowerCase(Locale.ROOT));
    }

    /**
     * Read predicate for a table, or "" when it is not tenant-scoped.
     * Bind one arg via {@link #arg()} when this returns non-empty.
     *
     * @param qualifier table name or alias to prefix the column with; null for none
     */
    public String readPredicate(String table, String qualifier) {
        if (!isScoped(table)) return "";
        String col = (qualifier == null ? "" : qualifier + ".") + TENANT_COLUMN;
        return col + " IN (?, " + TenantContext.GLOBAL_TENANT_ID + ")";
    }

    /** Write predicate — strict equality, never the global row. */
    public String writePredicate(String table, String qualifier) {
        if (!isScoped(table)) return "";
        String col = (qualifier == null ? "" : qualifier + ".") + TENANT_COLUMN;
        return col + " = ?";
    }

    /** The bind value for either predicate above. */
    public Long arg() {
        return TenantContext.get();
    }

    /** Cached membership, loaded on first use. */
    private Set<String> tables() {
        Set<String> t = scopedTables;
        if (t == null) {
            synchronized (this) {
                t = scopedTables;
                if (t == null) {
                    t = load();
                    scopedTables = t;
                }
            }
        }
        return t;
    }

    private Set<String> load() {
        Set<String> out = new HashSet<>();
        try {
            for (String name : jdbc.queryForList(
                    "SELECT TABLE_NAME FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND COLUMN_NAME = ?",
                    String.class, TENANT_COLUMN)) {
                if (name != null) out.add(name.toLowerCase(Locale.ROOT));
            }
        } catch (RuntimeException e) {
            // Fail CLOSED is not an option here (it would break every read), but
            // an empty set would fail OPEN and silently unscope everything --
            // exactly the bug this class exists to kill. Surface it instead.
            throw new IllegalStateException(
                    "Cannot determine tenant-scoped tables; refusing to serve unscoped queries", e);
        }
        return out;
    }

    /** Drop the cache after DDL that adds or removes a tenant_id column. */
    public void refresh() {
        scopedTables = null;
    }
}
