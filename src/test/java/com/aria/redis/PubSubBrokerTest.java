package com.aria.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link PubSubBroker#publish(String, Object)}.
 *
 * The {@code @PostConstruct} boot sanity check is exercised by {@code PubSubIT}
 * against the live Testcontainers Redis. Here we just verify that publish()
 * delegates to {@code convertAndSend} on the JSON template — the only thing
 * the unit-level surface owns.
 */
@ExtendWith(MockitoExtension.class)
class PubSubBrokerTest {

    @Mock
    private RedisTemplate<String, Object> redisJsonTemplate;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @InjectMocks
    private PubSubBroker broker;

    @Test
    @DisplayName("publish(): delegates to convertAndSend on the json template")
    void publishDelegatesToTemplate() {
        broker.publish("dev:ping", "hello");
        verify(redisJsonTemplate).convertAndSend("dev:ping", "hello");
    }
}
