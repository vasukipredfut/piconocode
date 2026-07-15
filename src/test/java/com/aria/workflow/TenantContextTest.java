package com.aria.workflow;

import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the ThreadLocal-based tenant carrier.
 */
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void defaultWhenUnset() {
        TenantContext.clear();
        assertThat(TenantContext.get()).isEqualTo(TenantContext.DEFAULT_TENANT_ID);
        assertThat(TenantContext.peek()).isNull();
    }

    @Test
    void setReturnsValueOnSameThread() {
        TenantContext.set(42L);
        assertThat(TenantContext.get()).isEqualTo(42L);
        assertThat(TenantContext.peek()).isEqualTo(42L);
    }

    @Test
    void clearWipesValue() {
        TenantContext.set(7L);
        TenantContext.clear();
        assertThat(TenantContext.peek()).isNull();
    }
}
