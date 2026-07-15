package com.aria.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Thin Redis pub/sub wrapper (REARCH ADR-007).
 *
 * Two consumers planned:
 *   - WebSocket / SSE fan-out across multi-instance backend (M6): MQTT edge writes
 *     to Redis, every backend pod subscribes and pushes to its locally-connected
 *     clients. No backend-to-backend mesh needed.
 *   - Cache-invalidation broadcasts when one node updates a master entity that
 *     another node has cached locally (defence-in-depth on top of {@code @CacheEvict}).
 *
 * For now we ship a {@link #publish(String, Object)} / {@link #subscribe(String, Consumer)}
 * facade and a {@link PostConstruct} sanity-check that proves the bus works at boot.
 */
@Component
public class PubSubBroker {

    private static final Logger log = LoggerFactory.getLogger(PubSubBroker.class);

    @Autowired
    @Qualifier("redisJsonTemplate")
    private RedisTemplate<String, Object> redisJsonTemplate;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    private RedisMessageListenerContainer container;

    @PostConstruct
    public void init() {
        this.container = new RedisMessageListenerContainer();
        this.container.setConnectionFactory(connectionFactory);
        this.container.afterPropertiesSet();
        this.container.start();

        // Round-trip sanity check: subscribe, publish, log the latency.
        try {
            String channel = "dev:ping";
            CountDownLatch latch = new CountDownLatch(1);
            long[] receivedAt = new long[1];
            long sentAt = System.nanoTime();

            MessageListener listener = (Message m, byte[] pattern) -> {
                receivedAt[0] = System.nanoTime();
                String body = new String(m.getBody(), StandardCharsets.UTF_8);
                log.info("REDIS_PUBSUB_RTT_RECEIVED channel={} payload={}", channel, body);
                latch.countDown();
            };
            container.addMessageListener(listener, new ChannelTopic(channel));

            // Give the subscription a moment to register.
            Thread.sleep(200);
            redisJsonTemplate.convertAndSend(channel, "boot-ping-" + System.currentTimeMillis());

            boolean got = latch.await(2, TimeUnit.SECONDS);
            if (got) {
                long micros = (receivedAt[0] - sentAt) / 1_000L;
                log.info("REDIS_PUBSUB_RTT_OK channel={} micros={}", channel, micros);
            } else {
                log.warn("REDIS_PUBSUB_RTT_TIMEOUT channel={} (broker reachable? subscription delivered?)", channel);
            }
            container.removeMessageListener(listener);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("REDIS_PUBSUB_RTT_FAIL", e);
        }
    }

    /**
     * Publish a JSON-serialised payload to a channel. Fire-and-forget.
     */
    public void publish(String channel, Object payload) {
        redisJsonTemplate.convertAndSend(channel, payload);
    }

    /**
     * Subscribe to a channel. The handler is invoked on the listener thread —
     * keep it short, hand heavy work to a worker.
     */
    public void subscribe(String channel, Consumer<String> handler) {
        Topic topic = new ChannelTopic(channel);
        container.addMessageListener((Message m, byte[] pattern) -> {
            handler.accept(new String(m.getBody(), StandardCharsets.UTF_8));
        }, topic);
        log.info("REDIS_PUBSUB_SUBSCRIBE channel={}", channel);
    }
}
