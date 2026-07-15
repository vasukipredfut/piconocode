package com.aria.m11.service;

import com.aria.m11.adapter.IntegrationAdapter;
import com.aria.m11.adapter.OutboundResult;
import com.aria.m11.dao.IntegrationEventLogRepository;
import com.aria.m11.model.IntegrationInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Pure-unit tests for the retry + circuit-breaker logic — adapter is a
 * stub returning scripted outcomes, sleeper is a fake clock collector.
 */
class IntegrationOutboundCallerTest {

    IntegrationOutboundCaller caller;
    IntegrationInstanceService instances;
    IntegrationSecretService secrets;
    IntegrationEventLogRepository events;
    ScriptedAdapter adapter;
    IntegrationInstance inst;
    AtomicLong now;
    List<Long> sleeps;

    @BeforeEach
    void setUp() {
        events = Mockito.mock(IntegrationEventLogRepository.class);
        instances = Mockito.mock(IntegrationInstanceService.class);
        secrets = Mockito.mock(IntegrationSecretService.class);
        adapter = new ScriptedAdapter();
        caller = new IntegrationOutboundCaller();
        // Hand-wire @Autowired collaborators via reflection-free package access
        // through Spring would normally do this — for the unit test we set them
        // through the setters / fields we own.
        injectField(caller, "events", events);
        injectField(caller, "instances", instances);
        injectField(caller, "secretService", secrets);
        injectField(caller, "adapters", List.of(adapter));
        caller.index();

        inst = IntegrationInstance.builder()
                .id(7L).tenantId(1L).code("x").connectorCode("STUB").connectorVersion(1)
                .name("n").status("ACTIVE").currentVersion(1)
                .configJson("{\"retryProfile\":\"fast\"}")
                .dataResidencyFlag("ANY").errorCount24h(0).build();
        when(instances.require(7L)).thenReturn(inst);
        when(secrets.loadOrEmpty(7L)).thenReturn(Map.of());

        now = new AtomicLong(1_000_000L);
        sleeps = new ArrayList<>();
        caller.setClock(now::get);
        caller.setSleeper(sleeps::add);
    }

    @Test
    void success_firstAttempt_noRetry_noSleep() {
        adapter.results.add(OutboundResult.success(200, Map.of(), "OK"));
        var r = caller.invoke(7L, "GET /x", Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(adapter.calls).isEqualTo(1);
        assertThat(sleeps).isEmpty();
    }

    @Test
    void retryable_then_success_sleepsOnce() {
        adapter.results.add(OutboundResult.retryable("transport","x"));
        adapter.results.add(OutboundResult.success(200, Map.of(), "OK"));
        var r = caller.invoke(7L, "GET /x", Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(adapter.calls).isEqualTo(2);
        assertThat(sleeps).hasSize(1);
    }

    @Test
    void retryable_exhaust_returnsLastFailure() {
        for (int i = 0; i < 4; i++) adapter.results.add(OutboundResult.retryable("t","x"));
        var r = caller.invoke(7L, "GET /x", Map.of());
        assertThat(r.isOk()).isFalse();
        // fast = 4 attempts
        assertThat(adapter.calls).isEqualTo(4);
        // 3 backoffs between 4 attempts
        assertThat(sleeps).hasSize(3);
    }

    @Test
    void terminalFailure_noRetry() {
        adapter.results.add(OutboundResult.failed("http_404","not found"));
        var r = caller.invoke(7L, "GET /x", Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(adapter.calls).isEqualTo(1);
        assertThat(sleeps).isEmpty();
    }

    @Test
    void circuitOpens_afterFiveConsecutiveFailures_andFailsFast() {
        // Push 20 retryable results to exhaust attempts across two invocations.
        for (int i = 0; i < 20; i++) adapter.results.add(OutboundResult.retryable("t","x"));
        // First invocation: 4 attempts (fast profile), 4 failures recorded.
        var r1 = caller.invoke(7L, "GET /x", Map.of());
        assertThat(r1.isOk()).isFalse();
        // Second invocation: 5th-consecutive failure occurs on the first attempt;
        // remaining attempts run since the breaker only opens on/after the 5th
        // recordFailure call. We then immediately try a third invocation —
        // breaker should be OPEN and the call short-circuits.
        var r2 = caller.invoke(7L, "GET /x", Map.of());
        assertThat(r2.isOk()).isFalse();
        // Third invocation: should be CB_OPEN.
        int callsBefore = adapter.calls;
        var r3 = caller.invoke(7L, "GET /x", Map.of());
        assertThat(r3.isOk()).isFalse();
        assertThat(r3.getErrorCode()).isEqualTo("cb_open");
        assertThat(adapter.calls).isEqualTo(callsBefore); // no new adapter calls
    }

    @Test
    void circuitHalfOpens_afterCooldown() {
        // Open the breaker.
        for (int i = 0; i < 20; i++) adapter.results.add(OutboundResult.retryable("t","x"));
        caller.invoke(7L, "GET /x", Map.of());
        caller.invoke(7L, "GET /x", Map.of());

        // Advance the clock past cooldown and queue a success.
        now.addAndGet(IntegrationOutboundCaller.CircuitBreaker.COOLDOWN_MS + 1);
        adapter.results.clear();
        adapter.results.add(OutboundResult.success(200, Map.of(), "good"));
        int callsBefore = adapter.calls;
        var r = caller.invoke(7L, "GET /x", Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(adapter.calls).isEqualTo(callsBefore + 1);
    }

    private static class ScriptedAdapter implements IntegrationAdapter {
        final List<OutboundResult> results = new ArrayList<>();
        int calls = 0;
        @Override public String connectorCode() { return "STUB"; }
        @Override public boolean supportsOutbound() { return true; }
        @Override public OutboundResult invoke(IntegrationInstance i, String op,
                                                Map<String,Object> p, Map<String,Object> s) {
            calls++;
            if (results.isEmpty()) return OutboundResult.failed("no_script","");
            return results.remove(0);
        }
    }

    private static void injectField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
