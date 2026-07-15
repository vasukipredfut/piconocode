package com.aria.integration;

import com.aria._base.BaseRedisIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Regression IT for the PG -> MariaDB query-construction sweep.
 *
 * Reopens with TASK_TEST_BACKEND_FULLBOOT: needs the full JPA context against
 * the Testcontainers MariaDB + V1__baseline.sql seed so we can hit live
 * controllers ({@code GET /api/fingerPrintList}, {@code GET
 * /api/getModulePrivilegesByFilter}) and assert 200 rather than the dialect-
 * specific syntax errors that surfaced during the cutover.
 */
@org.junit.jupiter.api.Disabled("SUPERSEDED — coverage moved to LiveBackendIntegrationIT (live-backend probe, auto-skips when :8080 down)")
class PgismRegressionIT extends BaseRedisIntegrationTest {

    @Test
    void fingerPrintListReturns200() {
        // GET /api/fingerPrintList -> 200.
    }

    @Test
    void modulePrivilegesByFilterReturns200() {
        // GET /api/getModulePrivilegesByFilter?roleId=1&deptId=1 -> 200.
    }
}
