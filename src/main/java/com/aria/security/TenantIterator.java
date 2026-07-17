package com.aria.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs a unit of work once per tenant, with {@link TenantContext} set for each.
 *
 * <h3>Why background jobs need this</h3>
 * {@link TenantContextFilter} populates the tenant from the JWT — but only on an
 * HTTP request. A {@code @Scheduled} job has no request, so it has no tenant, and
 * {@code TenantContext.get()} falls back to the default. That is why schedulers
 * ended up with {@code WHERE tenant_id=1} hardcoded: not carelessness, but a
 * single-tenant assumption with nowhere else to go.
 *
 * <p>The failure mode is the quiet kind. Add a second firm and their shift
 * rotation, reminder sweep or reconciliation simply never runs. Nothing throws.
 * No row appears. The job reports success every time — it is faithfully doing
 * the first tenant's work forever.
 *
 * <p>So: a scheduled job in a multi-tenant product must iterate tenants.
 *
 * <pre>
 *   tenants.forEach(tenantId -&gt; rotateShiftsForCurrentTenant());
 * </pre>
 *
 * The body then reads {@code TenantContext.get()} like any request-scoped code,
 * and one tenant's failure never stops the rest.
 *
 * <h3>Where the tenant list comes from</h3>
 * The {@code tenant} registry, filtered to ACTIVE. A SUSPENDED tenant must not
 * have work done on its behalf — that is what suspension means.
 *
 * <p>Global structure ({@link TenantContext#GLOBAL_TENANT_ID}) is never a
 * tenant: it is a scope that owns no business data, so there is nothing for a
 * job to act on. The {@code id > 0} guard keeps it out even if a row ever
 * appears there.
 */
@Component
public class TenantIterator {

    private static final Logger log = LoggerFactory.getLogger(TenantIterator.class);

    private final JdbcTemplate jdbc;

    public TenantIterator(@Qualifier("psqlDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /** Every ACTIVE tenant. Never includes the global (-1) scope. */
    public List<Long> activeTenants() {
        try {
            return jdbc.queryForList(
                    "SELECT id FROM tenant WHERE status = 'ACTIVE' AND id > 0 ORDER BY id",
                    Long.class);
        } catch (RuntimeException e) {
            // Returning the default tenant here would quietly resume the exact
            // single-tenant behaviour this class exists to remove, so say so
            // loudly rather than look like it worked.
            log.error("Cannot read the tenant registry ({}); scheduled work is SKIPPED this cycle "
                    + "rather than silently running for one tenant only", e.getMessage());
            return List.of();
        }
    }

    /**
     * Run {@code job} once per tenant with the context set, restoring it after.
     *
     * <p>One tenant's failure is logged and skipped rather than aborting the
     * sweep: a scheduled job that dies on tenant 2 would silently stop serving
     * tenants 3..n, and the only symptom would be work that quietly stops
     * happening for some customers and not others.
     */
    public void forEach(Consumer<Long> job) {
        Long previous = TenantContext.peek();
        try {
            for (Long tenantId : activeTenants()) {
                try {
                    TenantContext.set(tenantId);
                    job.accept(tenantId);
                } catch (RuntimeException ex) {
                    log.error("Scheduled work failed for tenant {} — continuing with the rest", tenantId, ex);
                }
            }
        } finally {
            if (previous == null) TenantContext.clear();
            else TenantContext.set(previous);
        }
    }
}
