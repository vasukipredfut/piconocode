package com.aria.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.integration.adapter.IntegrationAdapter;
import com.aria.integration.adapter.OutboundResult;
import com.aria.integration.adapter.PingResult;
import com.aria.integration.dao.IntegrationEventLogRepository;
import com.aria.integration.model.IntegrationEventLog;
import com.aria.integration.model.IntegrationInstance;
import com.aria.security.TenantContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Wraps an {@link IntegrationAdapter} with:
 * <ul>
 *   <li>retry profile (fast / standard / slow) with exponential backoff + jitter,</li>
 *   <li>per-instance circuit breaker (open after 5 consecutive failures, 60s cooldown,
 *       half-open re-test),</li>
 *   <li>append-only audit to {@code integration_event_log}.</li>
 * </ul>
 *
 * <p>Used by the workflow {@code external.api.call} action and by the
 * {@code POST /api/m11/instances/{id}/test} endpoint.
 */
@Service
public class IntegrationOutboundCaller {

    private static final Logger log = LoggerFactory.getLogger(IntegrationOutboundCaller.class);

    @Autowired private List<IntegrationAdapter> adapters;
    @Autowired private IntegrationSecretService secretService;
    @Autowired private IntegrationInstanceService instances;
    @Autowired private IntegrationEventLogRepository events;
    private final ObjectMapper json = new ObjectMapper();

    private final Map<String, IntegrationAdapter> byCode = new ConcurrentHashMap<>();
    private final Map<Long, CircuitBreaker> breakers = new ConcurrentHashMap<>();
    /** Test-injectable sleeper for retry backoff. */
    private Sleeper sleeper = Sleeper.DEFAULT;
    /** Test-injectable clock for breaker cooldown. */
    private java.util.function.LongSupplier clock = System::currentTimeMillis;

    @PostConstruct
    public void index() {
        for (IntegrationAdapter a : adapters) byCode.put(a.connectorCode(), a);
        log.info("IntegrationOutboundCaller indexed {} adapter(s): {}", byCode.size(), byCode.keySet());
    }

    // For tests.
    public void setSleeper(Sleeper s) { this.sleeper = s == null ? Sleeper.DEFAULT : s; }
    public void setClock(java.util.function.LongSupplier c) { this.clock = c == null ? System::currentTimeMillis : c; }

    /** Look up the adapter for a connector code (or throw). */
    public IntegrationAdapter requireAdapter(String connectorCode) {
        IntegrationAdapter a = byCode.get(connectorCode);
        if (a == null) {
            throw new AdapterMissingException("No adapter registered for connector: " + connectorCode);
        }
        return a;
    }

    /** Adapter-test path: one-shot {@code ping()} with audit + health update. */
    public PingResult ping(Long instanceId) {
        IntegrationInstance inst = instances.require(instanceId);
        IntegrationAdapter a = requireAdapter(inst.getConnectorCode());
        Map<String, Object> secrets = secretService.loadOrEmpty(instanceId);
        long t0 = System.nanoTime();
        PingResult res;
        try {
            res = a.ping(inst, secrets);
        } catch (Exception e) {
            res = PingResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        int latency = (int) ((System.nanoTime() - t0) / 1_000_000);
        recordEvent(inst, IntegrationEventLog.DIR_OUTBOUND,
                res.isOk() ? IntegrationEventLog.STATUS_OK : IntegrationEventLog.STATUS_FAIL,
                Map.of("op", "PING"), Map.of("message", res.getMessage()),
                res.isOk() ? null : "PING_FAIL", latency);
        instances.markHealth(instanceId, res.isOk(), res.getMessage());
        return res;
    }

    /**
     * Invoke an operation with retry + circuit breaker. Returns the final
     * {@link OutboundResult}; never throws for adapter-level errors (caller
     * inspects {@code ok}).
     */
    public OutboundResult invoke(Long instanceId, String operation, Map<String, Object> payload) {
        IntegrationInstance inst = instances.require(instanceId);
        IntegrationAdapter a = requireAdapter(inst.getConnectorCode());

        CircuitBreaker cb = breakers.computeIfAbsent(instanceId, k -> new CircuitBreaker());
        if (!cb.allow(clock.getAsLong())) {
            recordEvent(inst, IntegrationEventLog.DIR_OUTBOUND, IntegrationEventLog.STATUS_CB_OPEN,
                    Map.of("op", operation), Map.of("message", "circuit_open"),
                    "CB_OPEN", 0);
            return OutboundResult.failed("cb_open", "circuit breaker open for instance " + instanceId);
        }

        RetryProfile profile = retryProfileFor(inst);
        Map<String, Object> secrets = secretService.loadOrEmpty(instanceId);

        OutboundResult last = null;
        for (int attempt = 1; attempt <= profile.maxAttempts; attempt++) {
            long t0 = System.nanoTime();
            try {
                last = a.invoke(inst, operation, payload, secrets);
            } catch (Exception e) {
                last = OutboundResult.retryable("adapter_exception",
                        e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            int latency = (int) ((System.nanoTime() - t0) / 1_000_000);
            String status = last.isOk() ? IntegrationEventLog.STATUS_OK : IntegrationEventLog.STATUS_FAIL;
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("op", operation); reqMap.put("attempt", attempt);
            reqMap.put("payload", redact(payload));
            Map<String, Object> respMap = new HashMap<>();
            respMap.put("status", last.getStatus()); respMap.put("ok", last.isOk());
            respMap.put("errorCode", last.getErrorCode());
            respMap.put("body", abbreviate(last.getBody(), 4096));
            recordEvent(inst, IntegrationEventLog.DIR_OUTBOUND, status,
                    reqMap, respMap, last.isOk() ? null : last.getErrorCode(), latency);
            if (last.isOk()) {
                cb.recordSuccess();
                return last;
            }
            cb.recordFailure(clock.getAsLong());
            if (!last.isRetryable() || attempt == profile.maxAttempts) break;
            sleeper.sleep(profile.backoffWithJitter(attempt));
        }
        return last == null ? OutboundResult.failed("no_attempt", "no attempt made") : last;
    }

    // ---------------------------------------------------------- event log
    @Transactional(value = "m11TransactionManager", propagation = Propagation.REQUIRES_NEW)
    void recordEvent(IntegrationInstance inst, String direction, String status,
                     Map<String, Object> req, Map<String, Object> resp,
                     String errorCode, int latencyMs) {
        try {
            IntegrationEventLog e = IntegrationEventLog.builder()
                    .tenantId(inst.getTenantId() == null ? TenantContext.get() : inst.getTenantId())
                    .instanceId(inst.getId()).direction(direction).status(status)
                    .requestJson(req == null ? null : json.writeValueAsString(req))
                    .responseJson(resp == null ? null : json.writeValueAsString(resp))
                    .errorCode(errorCode).latencyMs(latencyMs)
                    .redactedJson(req == null ? null : json.writeValueAsString(redact(req)))
                    .build();
            events.save(e);
        } catch (Exception ex) {
            log.warn("event_log write failed: {}", ex.getMessage());
        }
    }

    private static Map<String, Object> redact(Map<String, Object> in) {
        if (in == null) return null;
        Map<String, Object> out = new HashMap<>();
        for (var e : in.entrySet()) {
            String k = e.getKey();
            if (k == null) continue;
            String lk = k.toLowerCase();
            if (lk.contains("password") || lk.contains("secret")
                    || lk.contains("token") || lk.contains("apikey")
                    || lk.contains("authorization")) {
                out.put(k, "***");
            } else if (e.getValue() instanceof Map<?,?> m) {
                @SuppressWarnings("unchecked")
                Map<String, Object> inner = (Map<String, Object>) m;
                out.put(k, redact(inner));
            } else {
                out.put(k, e.getValue());
            }
        }
        return out;
    }

    private static String abbreviate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "…(" + s.length() + ")";
    }

    private RetryProfile retryProfileFor(IntegrationInstance inst) {
        try {
            Map<?,?> cfg = json.readValue(inst.getConfigJson() == null ? "{}" : inst.getConfigJson(), Map.class);
            Object p = cfg.get("retryProfile");
            if (p == null) return RetryProfile.STANDARD;
            return switch (String.valueOf(p).toLowerCase()) {
                case "fast" -> RetryProfile.FAST;
                case "slow" -> RetryProfile.SLOW;
                default -> RetryProfile.STANDARD;
            };
        } catch (Exception e) {
            return RetryProfile.STANDARD;
        }
    }

    // ---------------------------------------------------------- helpers
    public static class AdapterMissingException extends RuntimeException {
        public AdapterMissingException(String m) { super(m); }
    }

    /** Pluggable sleeper so tests don't really wait. */
    public interface Sleeper {
        Sleeper DEFAULT = ms -> { try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); } };
        void sleep(long ms);
    }

    /** Retry policy with exponential backoff + jitter (per spec §17). */
    public static class RetryProfile {
        public static final RetryProfile FAST     = new RetryProfile("fast", 4,   200,  2_000);
        public static final RetryProfile STANDARD = new RetryProfile("standard", 4, 500, 30_000);
        public static final RetryProfile SLOW     = new RetryProfile("slow", 6, 2_000, 60_000);
        public final String name; public final int maxAttempts;
        public final long baseMs; public final long capMs;
        public RetryProfile(String n, int a, long b, long c) {
            this.name = n; this.maxAttempts = a; this.baseMs = b; this.capMs = c;
        }
        long backoffWithJitter(int attempt) {
            long exp = baseMs * (long) Math.pow(2, attempt - 1);
            long capped = Math.min(exp, capMs);
            long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, capped / 4));
            return capped + jitter;
        }
    }

    /** Per-instance circuit breaker — opens after 5 consecutive failures
     *  and stays open for {@link #cooldownMs} before going half-open. */
    public static class CircuitBreaker {
        static final int FAIL_THRESHOLD = 5;
        static final long COOLDOWN_MS = Duration.ofSeconds(60).toMillis();
        private int consecutiveFailures = 0;
        private long openedAtMs = 0;
        private volatile State state = State.CLOSED;
        public enum State { CLOSED, OPEN, HALF_OPEN }

        public synchronized boolean allow(long nowMs) {
            if (state == State.CLOSED) return true;
            if (state == State.OPEN) {
                if (nowMs - openedAtMs >= COOLDOWN_MS) {
                    state = State.HALF_OPEN;
                    return true;
                }
                return false;
            }
            // HALF_OPEN — allow one probe at a time. Real implementation
            // would lock; we accept best-effort here for simplicity.
            return true;
        }

        public synchronized void recordSuccess() {
            consecutiveFailures = 0;
            state = State.CLOSED;
        }

        public synchronized void recordFailure(long nowMs) {
            consecutiveFailures++;
            if (state == State.HALF_OPEN || consecutiveFailures >= FAIL_THRESHOLD) {
                state = State.OPEN;
                openedAtMs = nowMs;
            }
        }

        public State state() { return state; }
        public int consecutiveFailures() { return consecutiveFailures; }
        public long cooldownMs() { return COOLDOWN_MS; }
    }
}
