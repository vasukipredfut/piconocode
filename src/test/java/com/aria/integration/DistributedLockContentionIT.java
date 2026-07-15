package com.aria.integration;

import com.aria._base.BaseRedisIntegrationTest;
import com.aria.redis.DistributedLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end Redis lock contention test.
 *
 * 5 threads race for the same {@code lock:contention:*} key. Exactly one must
 * get the token; the other four must see Optional.empty(). The Lua CAS release
 * must succeed for the holder and report no-op for the four losers.
 *
 * This is the property that protects {@code POST /api/saveHsn} from racing
 * on the HSN code, and is the canonical Redlock guarantee we cannot fake with
 * Mockito.
 */
class DistributedLockContentionIT extends BaseRedisIntegrationTest {

    @Autowired
    private DistributedLock lock;

    @Test
    @DisplayName("5 parallel tryLock() on same key -> exactly one acquires, four lose")
    void onlyOneWinnerUnderContention() throws Exception {
        String key = "lock:contention:" + System.nanoTime();
        int threads = 5;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Optional<String>>> tasks = IntStream.range(0, threads)
                    .<Callable<Optional<String>>>mapToObj(i -> () -> lock.tryLock(key, Duration.ofSeconds(5)))
                    .collect(Collectors.toList());

            List<Future<Optional<String>>> results = pool.invokeAll(tasks, 5, TimeUnit.SECONDS);

            long winners = 0;
            String winnerToken = null;
            for (Future<Optional<String>> f : results) {
                Optional<String> r = f.get();
                if (r.isPresent()) {
                    winners++;
                    winnerToken = r.get();
                }
            }
            assertThat(winners).isEqualTo(1L);
            assertThat(winnerToken).isNotNull();
            // The winner can release; nobody else can.
            assertThat(lock.unlock(key, winnerToken)).isTrue();
            assertThat(lock.unlock(key, "some-other-token")).isFalse();
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("unlock() with a stolen token does NOT release the genuine holder (CAS guarantee)")
    void casRejectsForeignToken() {
        String key = "lock:cas:" + System.nanoTime();
        Optional<String> token = lock.tryLock(key, Duration.ofSeconds(10));
        assertThat(token).isPresent();

        // Stolen token: must be rejected, real holder's token still works.
        assertThat(lock.unlock(key, "forged-token")).isFalse();
        assertThat(lock.unlock(key, token.get())).isTrue();
    }
}
