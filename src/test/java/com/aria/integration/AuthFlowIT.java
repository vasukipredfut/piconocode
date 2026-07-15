package com.aria.integration;

import com.aria._base.BaseRedisIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Auth flow end-to-end against Testcontainers MariaDB.
 *
 * Reopens with TASK_TEST_BACKEND_FULLBOOT: this test needs the full
 * {@code AriaApplication} context (UserController + JWT chain + JPA + 468
 * V1__baseline.sql tables) brought up against the Testcontainers MariaDB,
 * plus a stubbed SQL Server datasource (Hikari's initialization-fail-timeout=-1
 * already keeps it lazy). The IT base class today loads a Redis-only slim
 * context; wiring full-boot is the next test-infra task.
 *
 * When enabled, the assertions are:
 *   - POST /api/auth/signin with {"username":"1001","password":"Test@1234"} returns 200
 *     with a non-empty accessToken in the body.
 *   - Decoded JWT carries sub=1001.
 *   - Bad credentials -> 401.
 *   - Missing body -> 400 or 422.
 */
@org.junit.jupiter.api.Disabled("SUPERSEDED — coverage moved to LiveBackendIntegrationIT (live-backend probe, auto-skips when :8080 down)")
class AuthFlowIT extends BaseRedisIntegrationTest {

    @Test
    void loginWithSeededUser() {
        // Test body lands when full-boot IT base is in place.
    }

    @Test
    void badCredentialsReturn401() {
        // Test body lands when full-boot IT base is in place.
    }
}
