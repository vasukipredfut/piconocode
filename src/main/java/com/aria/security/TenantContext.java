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
