package com.aria.m11.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.adapter.InboundResult;
import com.aria.m11.adapter.IntegrationAdapter;
import com.aria.m11.dao.IntegrationEventLogRepository;
import com.aria.m11.dao.IntegrationInboundTokenRepository;
import com.aria.m11.dao.IntegrationInstanceRepository;
import com.aria.m11.model.IntegrationEventLog;
import com.aria.m11.model.IntegrationInboundToken;
import com.aria.m11.model.IntegrationInstance;
import com.aria.m0.workflow.engine.WorkflowEngineService;
import com.aria.redis.IdempotencyService;
import com.aria.security.TenantContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the {@code instanceId/token} URL pair, verifies the path token
 * (constant-time), invokes the adapter's {@code ingest()} and — when the
 * instance is configured with {@code workflowCode} in its config — fires
 * the workflow via {@link WorkflowEngineService#startInstance}.
 *
 * <p>Idempotency: same instance + same body within 1h returns {@code DUP}
 * without re-firing the workflow. Implemented via {@link IdempotencyService}.
 */
@Service
public class IntegrationInboundReceiver {

    private static final Logger log = LoggerFactory.getLogger(IntegrationInboundReceiver.class);
    public static final Duration IDEM_TTL = Duration.ofHours(1);

    @Autowired private IntegrationInstanceRepository instances;
    @Autowired private IntegrationInboundTokenRepository tokens;
    @Autowired private IntegrationEventLogRepository events;
    @Autowired private IntegrationSecretService secretService;
    @Autowired private List<IntegrationAdapter> adapterList;
    @Autowired(required = false) private IdempotencyService idempotency;
    @Autowired(required = false) private WorkflowEngineService workflowEngine;
    private final ObjectMapper json = new ObjectMapper();

    private final Map<String, IntegrationAdapter> byCode = new ConcurrentHashMap<>();

    @PostConstruct
    public void index() {
        for (IntegrationAdapter a : adapterList) byCode.put(a.connectorCode(), a);
        log.info("InboundReceiver indexed {} adapter(s)", byCode.size());
    }

    public Outcome receive(Long instanceId, String token, Map<String, String> headers, byte[] body) {
        Optional<IntegrationInstance> instOpt = instances.findById(instanceId);
        if (instOpt.isEmpty()) {
            return Outcome.notFound("instance " + instanceId + " not found");
        }
        IntegrationInstance inst = instOpt.get();
        // Tenant context for the workflow trigger.
        TenantContext.set(inst.getTenantId());

        // Token verification. We accept either an explicit row in
        // integration_inbound_token (with token_hash) or — convenient for
        // demos — a token that equals the instance code (only allowed when
        // no rows exist for the instance). Both branches log to the audit.
        boolean tokenOk;
        Optional<IntegrationInboundToken> tokenRow = tokens.findByTokenHash(sha256Hex(token));
        if (tokenRow.isPresent()) {
            tokenOk = tokenRow.get().getInstanceId().equals(instanceId);
            tokenRow.get().setLastUsedAt(Instant.now());
            try { tokens.save(tokenRow.get()); } catch (Exception ignore) {}
        } else {
            List<IntegrationInboundToken> all = tokens.findByInstanceIdOrderByCreatedAtDesc(instanceId);
            tokenOk = all.isEmpty() && constantTimeEquals(token, inst.getCode());
        }
        if (!tokenOk) {
            recordEvent(inst, IntegrationEventLog.STATUS_REJECTED,
                    Map.of("reason", "token_invalid"), null, "TOKEN_INVALID", 0);
            return Outcome.unauthorized("token_invalid");
        }

        // Idempotency.
        String bodyHash = sha256Hex(body == null ? new byte[0] : body);
        String idemKey = "m11:in:" + instanceId + ":" + bodyHash;
        if (idempotency != null && !idempotency.reserve(idemKey, IDEM_TTL)) {
            recordEvent(inst, IntegrationEventLog.STATUS_DUP,
                    Map.of("bodyHash", bodyHash), null, null, 0);
            return Outcome.duplicate();
        }

        IntegrationAdapter adapter = byCode.get(inst.getConnectorCode());
        if (adapter == null) {
            recordEvent(inst, IntegrationEventLog.STATUS_REJECTED,
                    Map.of("reason", "no_adapter"), null, "NO_ADAPTER", 0);
            return Outcome.serverError("no adapter for connector " + inst.getConnectorCode());
        }

        long t0 = System.nanoTime();
        Map<String, Object> secrets = secretService.loadOrEmpty(instanceId);
        InboundResult result;
        try {
            result = adapter.ingest(inst, headers, body, secrets);
        } catch (Exception e) {
            result = InboundResult.rejected(e.getClass().getSimpleName() + ":" + e.getMessage());
        }
        int latency = (int) ((System.nanoTime() - t0) / 1_000_000);

        if (InboundResult.STATUS_REJECTED.equals(result.getStatus())) {
            recordEvent(inst, IntegrationEventLog.STATUS_REJECTED,
                    Map.of("headers", redactHeaders(headers), "bodyHash", bodyHash),
                    Map.of("reason", result.getReason()),
                    "INGEST_REJECTED", latency);
            return Outcome.unauthorized(result.getReason() == null ? "rejected" : result.getReason());
        }

        // Optional workflow fire.
        String workflowCode = workflowCodeFor(inst);
        Long workflowInstanceId = null;
        if (workflowCode != null && workflowEngine != null) {
            try {
                Map<String, Object> ctx = new HashMap<>();
                ctx.put("payload", result.getParsedBody());
                ctx.put("headers", headers);
                ctx.put("signatureOk", result.isSignatureOk());
                ctx.put("instanceId", instanceId);
                var wi = workflowEngine.startInstance(workflowCode, ctx);
                workflowInstanceId = wi == null ? null : wi.getId();
            } catch (Exception e) {
                log.warn("workflow trigger failed for instance {} (workflow={}): {}",
                        instanceId, workflowCode, e.getMessage());
            }
        }

        recordEvent(inst, IntegrationEventLog.STATUS_ACCEPTED,
                Map.of("headers", redactHeaders(headers), "bodyHash", bodyHash,
                        "signatureOk", result.isSignatureOk()),
                Map.of("workflowCode", workflowCode, "workflowInstanceId", workflowInstanceId),
                null, latency);

        return Outcome.accepted(result.isSignatureOk(), workflowInstanceId);
    }

    private String workflowCodeFor(IntegrationInstance inst) {
        try {
            Map<?,?> cfg = json.readValue(inst.getConfigJson() == null ? "{}" : inst.getConfigJson(), Map.class);
            Object w = cfg.get("workflowCode");
            return w == null ? null : String.valueOf(w);
        } catch (Exception e) { return null; }
    }

    @Transactional(value = "m11TransactionManager", propagation = Propagation.REQUIRES_NEW)
    void recordEvent(IntegrationInstance inst, String status, Map<String,Object> req,
                     Map<String,Object> resp, String errorCode, int latencyMs) {
        try {
            IntegrationEventLog e = IntegrationEventLog.builder()
                    .tenantId(inst.getTenantId()).instanceId(inst.getId())
                    .direction(IntegrationEventLog.DIR_INBOUND).status(status)
                    .requestJson(req == null ? null : json.writeValueAsString(req))
                    .responseJson(resp == null ? null : json.writeValueAsString(resp))
                    .errorCode(errorCode).latencyMs(latencyMs)
                    .redactedJson(req == null ? null : json.writeValueAsString(req))
                    .build();
            events.save(e);
        } catch (Exception ex) {
            log.warn("inbound event_log write failed: {}", ex.getMessage());
        }
    }

    private Map<String, String> redactHeaders(Map<String, String> headers) {
        if (headers == null) return null;
        Map<String, String> out = new HashMap<>();
        for (var e : headers.entrySet()) {
            String k = e.getKey() == null ? "" : e.getKey().toLowerCase();
            if (k.contains("authorization") || k.contains("token")) {
                out.put(e.getKey(), "***");
            } else {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }

    static String sha256Hex(byte[] in) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(in == null ? new byte[0] : in));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    static String sha256Hex(String in) {
        return sha256Hex(in == null ? new byte[0] : in.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aa = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (aa.length != bb.length) return false;
        int r = 0;
        for (int i = 0; i < aa.length; i++) r |= aa[i] ^ bb[i];
        return r == 0;
    }

    /** Result envelope for the controller layer. */
    public record Outcome(int httpStatus, String body, boolean signatureOk, Long workflowInstanceId) {
        public static Outcome accepted(boolean sigOk, Long wfId) {
            return new Outcome(200, "{\"status\":\"accepted\",\"signatureOk\":" + sigOk + "}", sigOk, wfId);
        }
        public static Outcome duplicate() {
            return new Outcome(200, "{\"status\":\"duplicate\"}", true, null);
        }
        public static Outcome unauthorized(String reason) {
            return new Outcome(401, "{\"status\":\"unauthorized\",\"reason\":\"" + reason + "\"}", false, null);
        }
        public static Outcome notFound(String reason) {
            return new Outcome(404, "{\"status\":\"not_found\",\"reason\":\"" + reason + "\"}", false, null);
        }
        public static Outcome serverError(String reason) {
            return new Outcome(500, "{\"status\":\"error\",\"reason\":\"" + reason + "\"}", false, null);
        }
    }
}
