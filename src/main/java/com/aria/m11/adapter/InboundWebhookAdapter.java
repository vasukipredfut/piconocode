package com.aria.m11.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.model.IntegrationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Generic webhook receiver for connector code {@code INBOUND_WEBHOOK}.
 *
 * <p>Config:
 * <ul>
 *   <li>{@code signatureScheme} — currently supports {@code hmac-sha256-body}
 *       and {@code none}. Defaults to none.</li>
 *   <li>{@code signatureHeader} — header carrying the signature (default
 *       {@code X-Signature}). Optional prefix {@code sha256=} is accepted.</li>
 *   <li>{@code allowUnverified} — when true, request is accepted even when
 *       the signature is missing/invalid; {@code signatureOk=false} is
 *       reported in the result so the workflow can decide.</li>
 * </ul>
 *
 * <p>Secret payload (decrypted by {@link com.aria.m11.service.IntegrationSecretService}):
 * map with key {@code hmacSecret} (string).
 */
@Component
public class InboundWebhookAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(InboundWebhookAdapter.class);
    public static final String CODE = "INBOUND_WEBHOOK";
    private final ObjectMapper mapper = new ObjectMapper();

    @Override public String connectorCode() { return CODE; }
    @Override public boolean supportsInbound() { return true; }

    @Override
    public PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        // Inbound-only — "ping" is just config sanity.
        Map<String, Object> cfg = readConfig(instance);
        String scheme = String.valueOf(cfg.getOrDefault("signatureScheme", "none"));
        if (!"none".equalsIgnoreCase(scheme) && !"hmac-sha256-body".equalsIgnoreCase(scheme)) {
            return PingResult.fail("unsupported signatureScheme: " + scheme);
        }
        return PingResult.ok(0L);
    }

    @Override
    public InboundResult ingest(IntegrationInstance instance, Map<String, String> headers,
                                 byte[] body, Map<String, Object> secrets) {
        Map<String, Object> cfg = readConfig(instance);
        String scheme = String.valueOf(cfg.getOrDefault("signatureScheme", "none")).toLowerCase(Locale.ROOT);
        String sigHeader = String.valueOf(cfg.getOrDefault("signatureHeader", "X-Signature"));
        boolean allowUnverified = Boolean.TRUE.equals(cfg.get("allowUnverified"));

        boolean sigOk;
        if ("none".equals(scheme)) {
            sigOk = true;
        } else if ("hmac-sha256-body".equals(scheme)) {
            String secret = secrets == null ? null : String.valueOf(secrets.getOrDefault("hmacSecret", ""));
            String headerSig = headerLookup(headers, sigHeader);
            sigOk = verifyHmacSha256(body, secret, headerSig);
            if (!sigOk && !allowUnverified) {
                return InboundResult.rejected("hmac_invalid");
            }
        } else {
            return InboundResult.rejected("unsupported_scheme:" + scheme);
        }

        Map<String, Object> parsed = parseBody(body);
        return InboundResult.accepted(sigOk, parsed);
    }

    /** Constant-time HMAC-SHA256 check. Accepts {@code sha256=<hex>} or {@code <hex>}. */
    public static boolean verifyHmacSha256(byte[] body, String secret, String headerSig) {
        if (secret == null || secret.isEmpty() || headerSig == null || headerSig.isEmpty()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(body == null ? new byte[0] : body);
            String computedHex = HexFormat.of().formatHex(computed);
            String expected = headerSig.toLowerCase(Locale.ROOT).replaceFirst("^sha256=", "").trim();
            return MessageDigestConstantTime.equalsHex(expected, computedHex);
        } catch (Exception e) {
            log.warn("HMAC verify failed: {}", e.getMessage());
            return false;
        }
    }

    private static String headerLookup(Map<String, String> headers, String name) {
        if (headers == null || name == null) return null;
        for (var e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) return e.getValue();
        }
        return null;
    }

    private Map<String, Object> readConfig(IntegrationInstance instance) {
        try {
            JsonNode n = mapper.readTree(instance.getConfigJson() == null ? "{}" : instance.getConfigJson());
            return mapper.convertValue(n, Map.class);
        } catch (Exception e) {
            log.warn("INBOUND_WEBHOOK: bad config JSON for instance {}: {}",
                    instance.getId(), e.getMessage());
            return Map.of();
        }
    }

    private Map<String, Object> parseBody(byte[] body) {
        if (body == null || body.length == 0) return Map.of();
        try {
            JsonNode n = mapper.readTree(body);
            if (n.isObject()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> out = mapper.convertValue(n, Map.class);
                return out;
            }
            return Map.of("_raw", n.toString());
        } catch (Exception e) {
            return Map.of("_raw", new String(body, StandardCharsets.UTF_8));
        }
    }

    /** Constant-time string-equality on lowercase hex strings. */
    private static final class MessageDigestConstantTime {
        static boolean equalsHex(String a, String b) {
            if (a == null || b == null) return false;
            byte[] aa = a.getBytes(StandardCharsets.US_ASCII);
            byte[] bb = b.getBytes(StandardCharsets.US_ASCII);
            if (aa.length != bb.length) return false;
            int r = 0;
            for (int i = 0; i < aa.length; i++) r |= (aa[i] ^ bb[i]);
            return r == 0;
        }
    }
}
