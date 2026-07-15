package com.aria.m11.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.model.IntegrationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic outbound REST adapter for connector code {@code OUTBOUND_REST}.
 *
 * <p>Config (from {@code config_json}):
 * <ul>
 *   <li>{@code baseUrl} (required) — e.g. {@code https://api.example.com}.</li>
 *   <li>{@code defaultHeaders} (optional object) — applied to every call.</li>
 *   <li>{@code retryProfile} — informational; the supervisor honours it.</li>
 * </ul>
 *
 * <p>Operation string: {@code "METHOD /path/template"} — e.g.
 * {@code "POST /payments/{id}"}. Path tokens {@code {name}} are substituted
 * from the {@code payload.pathParams} map. Body comes from
 * {@code payload.body} (any JSON value). Per-call headers in
 * {@code payload.headers}.
 */
@Component
public class OutboundRestAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(OutboundRestAdapter.class);
    public static final String CODE = "OUTBOUND_REST";
    private final ObjectMapper mapper = new ObjectMapper();

    @Override public String connectorCode() { return CODE; }
    @Override public boolean supportsOutbound() { return true; }

    @Override
    public PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        long t0 = System.nanoTime();
        try {
            Map<String, Object> config = readConfig(instance);
            String baseUrl = stringOr(config.get("baseUrl"), null);
            if (baseUrl == null || baseUrl.isBlank()) {
                return PingResult.fail("baseUrl missing in config");
            }
            RestClient client = buildClient(baseUrl, mapHeaders(config.get("defaultHeaders")));
            // HEAD on root — best-effort reachability check. Many servers
            // reply 200/4xx; treat anything other than network exceptions
            // as reachable.
            try {
                ResponseEntity<String> resp = client.method(HttpMethod.HEAD).uri("/")
                        .retrieve().toEntity(String.class);
                long ms = (System.nanoTime() - t0) / 1_000_000;
                return PingResult.builder().ok(true).message("HTTP " + resp.getStatusCode().value())
                        .httpStatus(resp.getStatusCode().value()).latencyMs(ms).build();
            } catch (RestClientResponseException server) {
                long ms = (System.nanoTime() - t0) / 1_000_000;
                return PingResult.builder().ok(true)
                        .message("HTTP " + server.getStatusCode().value())
                        .httpStatus(server.getStatusCode().value()).latencyMs(ms).build();
            }
        } catch (Exception e) {
            return PingResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public OutboundResult invoke(IntegrationInstance instance, String operation,
                                  Map<String, Object> payload, Map<String, Object> secrets) {
        if (operation == null || operation.isBlank()) {
            return OutboundResult.failed("bad_request", "operation required");
        }
        String[] parts = operation.trim().split("\\s+", 2);
        if (parts.length != 2) {
            return OutboundResult.failed("bad_request",
                    "operation must be 'METHOD /path', got: " + operation);
        }
        HttpMethod method;
        try { method = HttpMethod.valueOf(parts[0].toUpperCase()); }
        catch (IllegalArgumentException e) {
            return OutboundResult.failed("bad_request", "unknown HTTP method: " + parts[0]);
        }
        String pathTemplate = parts[1];
        Map<String, Object> config = readConfig(instance);
        String baseUrl = stringOr(config.get("baseUrl"), null);
        if (baseUrl == null || baseUrl.isBlank()) {
            return OutboundResult.failed("config_invalid", "baseUrl missing");
        }
        // Resolve path tokens.
        Map<String, Object> pathParams = mapPayload(payload, "pathParams");
        String resolvedPath = substitutePath(pathTemplate, pathParams);
        URI uri;
        try { uri = URI.create(baseUrl.replaceAll("/$", "") + resolvedPath); }
        catch (IllegalArgumentException e) {
            return OutboundResult.failed("config_invalid", "bad uri: " + e.getMessage());
        }

        // Headers — defaults from config + per-call overrides.
        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, Object> defaultHeaders = mapHeaders(config.get("defaultHeaders"));
        defaultHeaders.forEach((k, v) -> headers.put(k, String.valueOf(v)));
        Map<String, Object> callHeaders = mapPayload(payload, "headers");
        callHeaders.forEach((k, v) -> headers.put(k, String.valueOf(v)));
        // Apply API_KEY secret if present.
        Object apiKey = secrets == null ? null : secrets.get("apiKey");
        if (apiKey != null) {
            headers.putIfAbsent("Authorization", "Bearer " + apiKey);
        }

        Object body = payload == null ? null : payload.get("body");

        RestClient client = buildClient(baseUrl, defaultHeaders);
        try {
            RestClient.RequestBodySpec spec = client.method(method).uri(uri);
            headers.forEach(spec::header);
            ResponseEntity<String> resp = (body == null)
                    ? spec.retrieve().toEntity(String.class)
                    : spec.contentType(MediaType.APPLICATION_JSON)
                          .body(writeJson(body)).retrieve().toEntity(String.class);
            Map<String, String> respHeaders = new HashMap<>();
            for (Map.Entry<String, java.util.List<String>> h : resp.getHeaders().entrySet()) {
                respHeaders.put(h.getKey(), String.join(",", h.getValue()));
            }
            return OutboundResult.success(resp.getStatusCode().value(), respHeaders, resp.getBody());
        } catch (RestClientResponseException remote) {
            // 4xx — terminal; 5xx — retryable
            int s = remote.getStatusCode().value();
            String code = "http_" + s;
            String msg = "HTTP " + s + ": " + remote.getResponseBodyAsString();
            if (s >= 500) return OutboundResult.retryable(code, msg);
            return OutboundResult.failed(code, msg);
        } catch (Exception transport) {
            return OutboundResult.retryable("transport_error",
                    transport.getClass().getSimpleName() + ": " + transport.getMessage());
        }
    }

    private RestClient buildClient(String baseUrl, Map<String, Object> defaultHeaders) {
        RestClient.Builder b = RestClient.builder().baseUrl(baseUrl);
        if (defaultHeaders != null) {
            for (Map.Entry<String, Object> e : defaultHeaders.entrySet()) {
                b.defaultHeader(e.getKey(), String.valueOf(e.getValue()));
            }
        }
        b.defaultHeader(HttpHeaders.USER_AGENT, "earms-m11/1.0");
        return b.build();
    }

    private Map<String, Object> readConfig(IntegrationInstance instance) {
        try {
            JsonNode n = mapper.readTree(instance.getConfigJson() == null ? "{}" : instance.getConfigJson());
            return mapper.convertValue(n, Map.class);
        } catch (Exception e) {
            log.warn("OUTBOUND_REST: bad config JSON for instance {}: {}", instance.getId(), e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapHeaders(Object o) {
        if (!(o instanceof Map<?,?> m)) return new LinkedHashMap<>();
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        for (var e : m.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        return out;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapPayload(Map<String,Object> payload, String key) {
        if (payload == null) return Map.of();
        Object o = payload.get(key);
        if (!(o instanceof Map<?,?> m)) return Map.of();
        return (Map<String, Object>) m;
    }

    private static String substitutePath(String template, Map<String, Object> params) {
        if (params == null || params.isEmpty()) return template;
        String out = template;
        for (var e : params.entrySet()) {
            out = out.replace("{" + e.getKey() + "}",
                    e.getValue() == null ? "" : String.valueOf(e.getValue()));
        }
        return out;
    }

    private String writeJson(Object body) {
        if (body instanceof String s) return s;
        try { return mapper.writeValueAsString(body); }
        catch (Exception e) { throw new IllegalArgumentException("body not serialisable", e); }
    }

    private static String stringOr(Object v, String fallback) {
        return v == null ? fallback : String.valueOf(v);
    }

    // Exposed for tests — kept package-private.
    static Duration defaultTimeout() { return Duration.ofSeconds(10); }
}
