package com.aria.redis;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RedisStartupCacheClearerTest {

    @Test
    void clearsRedisWhenEnabled() throws Exception {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        RedisServerCommands serverCommands = mock(RedisServerCommands.class);

        when(factory.getConnection()).thenReturn(connection);
        when(connection.serverCommands()).thenReturn(serverCommands);

        RedisStartupCacheClearer clearer = new RedisStartupCacheClearer(factory, true);
        clearer.clearIfEnabled();

        verify(factory).getConnection();
        verify(serverCommands).flushAll();
    }

    @Test
    void skipsClearingWhenDisabled() throws Exception {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisStartupCacheClearer clearer = new RedisStartupCacheClearer(factory, false);

        clearer.clearIfEnabled();

        verifyNoInteractions(factory);
    }
}
