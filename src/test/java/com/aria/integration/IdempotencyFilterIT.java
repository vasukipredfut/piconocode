package com.aria.integration;

import com.aria._base.BaseRedisIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * End-to-end Idempotency-Key filter test.
 *
 * Reopens with TASK_TEST_BACKEND_FULLBOOT: needs the full AriaApplication
 * context so the {@code IdempotencyFilter} sits in the real servlet chain in
 * front of {@code POST /api/saveHsn}. The Redis-only slim context cannot
 * exercise the filter end-to-end because the controller is not in scope.
 *
 * The service-level guarantees this would re-prove are already covered
 * unit-side ({@code IdempotencyServiceTest}) and at the Redis-primitive level
 * ({@code IdempotencyServiceIT}).
 */
@org.junit.jupiter.api.Disabled("SUPERSEDED — coverage moved to LiveBackendIntegrationIT (live-backend probe, auto-skips when :8080 down)")
class IdempotencyFilterIT extends BaseRedisIntegrationTest {

    @Test
    void replaySecondPostWithSameKey() {
        // POST /api/saveHsn twice with the same Idempotency-Key ->
        //   first response stored, second carries X-Idempotency-Replay: true.
    }
}
