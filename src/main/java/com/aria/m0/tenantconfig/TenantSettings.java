package com.aria.m0.tenantconfig;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.aria.security.TenantContext;

/**
 * Reads generic per-tenant settings from {@code m0_tenant_setting} for the
 * current tenant (from {@link TenantContext}). This is the substrate home for
 * values that are DATA, not code — company name, factory name/licence for
 * statutory forms, etc. — so shared m6/m7 services stay tenant-agnostic.
 */
@Service
public class TenantSettings {

    private final JdbcTemplate jdbc;

    public TenantSettings(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Value for {@code key} in the current tenant, or {@code fallback} if unset. */
    public String get(String key, String fallback) {
        Long tenantId = TenantContext.get();
        if (tenantId == null) return fallback;
        try {
            String v = jdbc.queryForObject(
                    "SELECT setting_value FROM m0_tenant_setting WHERE tenant_id=? AND setting_key=?",
                    String.class, tenantId, key);
            return (v == null || v.isBlank()) ? fallback : v;
        } catch (Exception e) {
            return fallback;
        }
    }
}
