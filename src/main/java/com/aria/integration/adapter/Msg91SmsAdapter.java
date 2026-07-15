package com.aria.integration.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.integration.model.IntegrationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MSG91 SMS connector — sends transactional SMS via the MSG91 v5 flow API.
 *
 * <p>Concrete first-cut implementation of {@link IntegrationAdapter} for
 * Round 1H.2. Wires to the existing M11 substrate (catalog row was seeded
 * in V6_1). Outbound only. Two operations:
 *
 * <ul>
 *   <li>{@code sendSms} — payload {to, templateId, variables}; calls MSG91
 *     {@code /api/v5/flow} with the configured {@code authKey} header.</li>
 *   <li>{@code sendOtp} — payload {to, otp}; calls {@code /api/v5/otp}
 *     (helper for the Round 1F driver-verify use case).</li>
 * </ul>
 *
 * <p>Resilience: when {@code config.dryRun=true} the adapter logs the
 * outbound payload and returns success without making a network call —
 * lets the engine drive a workflow end-to-end without real MSG91 credits.
 *
 * <p>Secret lookup: {@code authKey} read from
 * {@code secrets["authKey"]} first (stored encrypted via EnvelopeCipher),
 * then falls back to {@code config["authKey"]} for dev convenience.
 *
 * <p>Config shape (matches V6_1 catalog config_schema_json):
 * <pre>{@code
 * {
 *   "baseUrl": "https://control.msg91.com",   // optional, default below
 *   "senderId": "PICOPL",
 *   "dlt_te_id": "1107171234567",             // optional, DLT entity id
 *   "dryRun": true                            // optional, default false
 * }
 * }</pre>
 */
@Component
public class Msg91SmsAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(Msg91SmsAdapter.class);
    public static final String CODE = "MSG91_SMS";
    private static final String DEFAULT_BASE_URL = "https://control.msg91.com";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override public String connectorCode() { return CODE; }
    @Override public boolean supportsOutbound() { return true; }

    @Override
    public PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        long t0 = System.nanoTime();
        try {
            Map<String, Object> config = readConfig(instance);
            String baseUrl = String.valueOf(config.getOrDefault("baseUrl", DEFAULT_BASE_URL));
            boolean dryRun = Boolean.TRUE.equals(config.get("dryRun"));
            if (dryRun) {
                long ms = (System.nanoTime() - t0) / 1_000_000;
                return PingResult.builder().ok(true).message("dryRun mode — skipping network probe")
                        .latencyMs(ms).build();
            }
            String authKey = pickAuthKey(secrets, config);
            if (authKey == null || authKey.isBlank()) {
                return PingResult.fail("authKey missing (set instance secret 'authKey' or config.authKey for dev)");
            }
            RestClient client = RestClient.builder().baseUrl(baseUrl)
                    .defaultHeader("authkey", authKey).build();
            try {
                ResponseEntity<String> resp = client.get().uri("/api/v5/flow")
                        .retrieve().toEntity(String.class);
                long ms = (System.nanoTime() - t0) / 1_000_000;
                int code = resp.getStatusCode().value();
                return PingResult.builder().ok(code < 500).message("HTTP " + code)
                        .httpStatus(code).latencyMs(ms).build();
            } catch (RestClientResponseException server) {
                // MSG91 GET /flow returns 405 for non-POST; still proves reachability.
                long ms = (System.nanoTime() - t0) / 1_000_000;
                int code = server.getStatusCode().value();
                return PingResult.builder().ok(code != 401 && code != 403)
                        .message("HTTP " + code + " (auth " + (code == 401 || code == 403 ? "FAILED" : "ok") + ")")
                        .httpStatus(code).latencyMs(ms).build();
            }
        } catch (Exception e) {
            return PingResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public OutboundResult invoke(IntegrationInstance instance, String operation,
                                  Map<String, Object> payload, Map<String, Object> secrets) {
        if (operation == null || operation.isBlank()) {
            return OutboundResult.failed("bad_request", "operation required (sendSms / sendOtp)");
        }
        try {
            Map<String, Object> config = readConfig(instance);
            String baseUrl = String.valueOf(config.getOrDefault("baseUrl", DEFAULT_BASE_URL));
            boolean dryRun = Boolean.TRUE.equals(config.get("dryRun"));
            String op = operation.trim();
            return switch (op) {
                case "sendSms" -> sendSms(payload, baseUrl, secrets, config, dryRun);
                case "sendOtp" -> sendOtp(payload, baseUrl, secrets, config, dryRun);
                default -> OutboundResult.failed("unknown_op",
                        "MSG91_SMS supports sendSms and sendOtp; got: " + operation);
            };
        } catch (Exception e) {
            log.warn("MSG91 invoke failed: {}", e.toString());
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // --- internal -----------------------------------------------------------

    private OutboundResult sendSms(Map<String, Object> payload, String baseUrl,
                                   Map<String, Object> secrets, Map<String, Object> config,
                                   boolean dryRun) {
        Object to = payload.get("to");
        Object templateId = payload.get("templateId");
        if (to == null) return OutboundResult.failed("bad_request", "payload.to required");
        if (templateId == null) return OutboundResult.failed("bad_request", "payload.templateId required");
        if (dryRun) {
            log.info("MSG91 DRY-RUN sendSms to={} templateId={} variables={}",
                    to, templateId, payload.get("variables"));
            return OutboundResult.builder().ok(true).status(200).body(
                    "{\"dryRun\":true,\"to\":\"" + to + "\",\"templateId\":\"" + templateId + "\"}"
            ).build();
        }
        String authKey = pickAuthKey(secrets, config);
        if (authKey == null) return OutboundResult.failed("config", "authKey missing");
        Map<String, Object> body = new HashMap<>();
        body.put("template_id", templateId);
        body.put("short_url", "0");
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("mobiles", String.valueOf(to));
        Object vars = payload.get("variables");
        if (vars instanceof Map<?, ?> vm) recipient.putAll((Map<String, Object>) vm);
        body.put("recipients", List.of(recipient));
        return doPost(baseUrl + "/api/v5/flow", authKey, body);
    }

    private OutboundResult sendOtp(Map<String, Object> payload, String baseUrl,
                                   Map<String, Object> secrets, Map<String, Object> config,
                                   boolean dryRun) {
        Object to = payload.get("to");
        Object otp = payload.get("otp");
        if (to == null) return OutboundResult.failed("bad_request", "payload.to required");
        if (otp == null) return OutboundResult.failed("bad_request", "payload.otp required");
        if (dryRun) {
            log.info("MSG91 DRY-RUN sendOtp to={} otp=****", to);
            return OutboundResult.builder().ok(true).status(200).body(
                    "{\"dryRun\":true,\"to\":\"" + to + "\"}"
            ).build();
        }
        String authKey = pickAuthKey(secrets, config);
        if (authKey == null) return OutboundResult.failed("config", "authKey missing");
        Map<String, Object> body = new HashMap<>();
        body.put("mobile", String.valueOf(to));
        body.put("otp", String.valueOf(otp));
        return doPost(baseUrl + "/api/v5/otp", authKey, body);
    }

    private OutboundResult doPost(String url, String authKey, Map<String, Object> body) {
        RestClient client = RestClient.builder().build();
        try {
            ResponseEntity<String> resp = client.post().uri(url)
                    .header("authkey", authKey)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(body)
                    .retrieve().toEntity(String.class);
            return OutboundResult.builder().ok(resp.getStatusCode().is2xxSuccessful())
                    .status(resp.getStatusCode().value()).body(resp.getBody()).build();
        } catch (RestClientResponseException server) {
            HttpStatusCode sc = server.getStatusCode();
            return OutboundResult.builder().ok(false).status(sc.value())
                    .errorCode("http_" + sc.value())
                    .errorMessage(server.getResponseBodyAsString()).build();
        } catch (Exception e) {
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private String pickAuthKey(Map<String, Object> secrets, Map<String, Object> config) {
        if (secrets != null && secrets.get("authKey") != null) {
            return String.valueOf(secrets.get("authKey"));
        }
        Object fallback = config.get("authKey");
        return fallback == null ? null : String.valueOf(fallback);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws IOException {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return new HashMap<>();
        return mapper.readValue(json, Map.class);
    }
}
