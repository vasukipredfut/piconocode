package com.aria.integration;

import com.aria._base.BaseRedisIntegrationTest;
import com.aria.redis.PubSubBroker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real Redis pub/sub round-trip: subscribe, publish, assert the handler ran.
 * Covers the bus that M6 will use for WebSocket / SSE fan-out and the
 * cache-invalidation broadcast in phase 2.
 */
class PubSubIT extends BaseRedisIntegrationTest {

    @Autowired
    private PubSubBroker broker;

    @Test
    @DisplayName("subscribe() then publish() -> handler receives the payload within 2s")
    void roundTrip() throws InterruptedException {
        String channel = "dev:ping:" + System.nanoTime();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();

        broker.subscribe(channel, payload -> {
            received.set(payload);
            latch.countDown();
        });

        // Give the listener a beat to register before publishing.
        Thread.sleep(150);
        broker.publish(channel, "hello-from-IT");

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        // The JSON template encodes Strings with quotes; either form is acceptable.
        assertThat(received.get()).contains("hello-from-IT");
    }
}
