package com.aria.integration.adapter;

import com.aria.integration.model.IntegrationInstance;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * TWILIO_WHATSAPP connector -- outbound WhatsApp text through the Twilio Messages API. Replaces
 * the direct Twilio calls in ERPNext spokes_app/attendance_report.py (sandbox sender and the
 * recipient number hard-coded in source).
 *
 * <p>config: {@code accountSid}, {@code from} ("whatsapp:+E164" approved sender), {@code dryRun}.
 * secrets: {@code authToken}. Operation {@code sendMessage}: payload {to, body}; {@code to} is an
 * E.164 number with or without the "whatsapp:" prefix; body is capped at Twilio's 1600 characters.
 */
@Component
public class TwilioWhatsappAdapter implements IntegrationAdapter {

    public static final String CODE = "TWILIO_WHATSAPP";
    public static final int MAX_BODY = 1600;
    private static final Logger log = LoggerFactory.getLogger(TwilioWhatsappAdapter.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override public String connectorCode() { return CODE; }
    @Override public boolean supportsOutbound() { return true; }

    @Override
    public PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        Map<String, Object> cfg = config(instance);
        if (cfg.get("accountSid") == null || cfg.get("from") == null) return PingResult.fail("accountSid and from are required");
        if (secrets == null || secrets.get("authToken") == null) return PingResult.fail("authToken secret missing");
        long t0 = System.currentTimeMillis();
        try {
            ResponseEntity<String> r = RestClient.builder().build().get()
                    .uri("https://api.twilio.com/2010-04-01/Accounts/" + cfg.get("accountSid") + ".json")
                    .header(HttpHeaders.AUTHORIZATION, basic(cfg, secrets))
                    .retrieve().toEntity(String.class);
            return r.getStatusCode().is2xxSuccessful() ? PingResult.ok(System.currentTimeMillis() - t0)
                    : PingResult.fail("HTTP " + r.getStatusCode().value());
        } catch (Exception e) {
            return PingResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public OutboundResult invoke(IntegrationInstance instance, String operation, Map<String, Object> payload, Map<String, Object> secrets) {
        if (!"sendMessage".equals(operation)) return OutboundResult.failed("unsupported_operation", "Unsupported: " + operation);
        Map<String, Object> cfg = config(instance);
        Object to = payload.get("to");
        Object body = payload.get("body");
        if (to == null || body == null) return OutboundResult.failed("bad_request", "payload.to and payload.body required");
        String toAddr = to.toString().startsWith("whatsapp:") ? to.toString() : "whatsapp:" + to;
        String text = body.toString().length() > MAX_BODY ? body.toString().substring(0, MAX_BODY) : body.toString();
        if (Boolean.TRUE.equals(cfg.get("dryRun"))) {
            log.info("TWILIO_WHATSAPP [dryRun] to={} chars={}", toAddr, text.length());
            return OutboundResult.success(200, Map.of("X-Dry-Run", "true"), "{\"dryRun\":true}");
        }
        if (cfg.get("accountSid") == null || cfg.get("from") == null) return OutboundResult.failed("config", "accountSid and from are required");
        if (secrets == null || secrets.get("authToken") == null) return OutboundResult.failed("config", "authToken secret missing");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("From", cfg.get("from").toString().startsWith("whatsapp:") ? cfg.get("from").toString() : "whatsapp:" + cfg.get("from"));
        form.add("To", toAddr);
        form.add("Body", text);
        try {
            ResponseEntity<String> r = RestClient.builder().build().post()
                    .uri("https://api.twilio.com/2010-04-01/Accounts/" + cfg.get("accountSid") + "/Messages.json")
                    .header(HttpHeaders.AUTHORIZATION, basic(cfg, secrets))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve().toEntity(String.class);
            return OutboundResult.builder().ok(r.getStatusCode().is2xxSuccessful()).status(r.getStatusCode().value()).body(r.getBody()).build();
        } catch (RestClientResponseException server) {
            HttpStatusCode sc = server.getStatusCode();
            return OutboundResult.builder().ok(false).status(sc.value()).errorCode("http_" + sc.value())
                    .errorMessage(server.getResponseBodyAsString()).retryable(sc.value() == 429 || sc.is5xxServerError()).build();
        } catch (Exception e) {
            return OutboundResult.retryable(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private static String basic(Map<String, Object> cfg, Map<String, Object> secrets) {
        String raw = cfg.get("accountSid") + ":" + secrets.get("authToken");
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> config(IntegrationInstance instance) {
        try {
            String json = instance == null ? null : instance.getConfigJson();
            return json == null || json.isBlank() ? new HashMap<>() : mapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("TWILIO_WHATSAPP config parse failed: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
