package com.aria.security;

/**
 * Per-request tenant identifier carrier.
 *
 * <p>Populated by {@link TenantContextFilter} from the JWT {@code tid} claim
 * after authentication succeeds. Cleared in the same filter's finally block
 * so a leaked value cannot bleed into the next request that uses the same
 * Tomcat worker thread.
 *
 * <p>Not a Spring bean — this is a thread-local accessed statically from
 * services and repositories. Tests that need a tenant context call
 * {@link #set(Long)} directly in a try/finally block.
 */
public final class TenantContext {

    /** Default tenant used when no JWT claim is present (e.g. permit-all endpoints). */
    public static final Long DEFAULT_TENANT_ID = 1L;

    /**
     * The global product catalogue: structure that ships to every deployment
     * (pages, workflows, entity/field definitions) and is owned by the product,
     * not by any customer.
     *
     * <p>Resolution is always "tenant row wins, else global":
     * <pre>WHERE tenant_id IN (:tid, -1) ORDER BY tenant_id DESC LIMIT 1</pre>
     * Real tenant ids are positive, so {@code DESC} puts an override ahead of
     * the global row for free.
     *
     * <p>-1 rather than 0 deliberately: {@code tenant_id} columns default to 1,
     * and 0 is exactly what a null-to-numeric coercion or a bad insert yields.
     * -1 cannot be arrived at by accident. (Verified: all 144 tenant_id columns
     * are signed.)
     *
     * <p>Global rows hold structure only — never business data, credentials or
     * branding. Those are always tenant-scoped.
     */
    public static final Long GLOBAL_TENANT_ID = -1L;

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private TenantContext() {
        // static utility
    }

    public static void set(Long tenantId) {
        CURRENT.set(tenantId);
    }

    public static Long get() {
        Long v = CURRENT.get();
        return v == null ? DEFAULT_TENANT_ID : v;
    }

    /** Returns the raw value (null when not set) — for diagnostic / test code only. */
    public static Long peek() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
