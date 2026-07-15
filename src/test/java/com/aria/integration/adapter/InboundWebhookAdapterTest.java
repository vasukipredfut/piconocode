package com.aria.integration.adapter;

import com.aria.integration.model.IntegrationInstance;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for the generic HMAC-verified webhook adapter. */
class InboundWebhookAdapterTest {

    private final InboundWebhookAdapter adapter = new InboundWebhookAdapter();

    private static IntegrationInstance instWith(String configJson) {
        return IntegrationInstance.builder()
                .id(1L).tenantId(1L).code("hooktest")
                .connectorCode(InboundWebhookAdapter.CODE).connectorVersion(1)
                .name("hook").status("ACTIVE").currentVersion(1)
                .configJson(configJson).dataResidencyFlag("ANY").errorCount24h(0)
                .build();
    }

    @Test
    void noSignatureScheme_acceptsEverything() {
        var inst = instWith("{\"signatureScheme\":\"none\"}");
        var res = adapter.ingest(inst, Map.of("Content-Type","application/json"),
                "{\"event\":\"x\"}".getBytes(), Map.of());
        assertThat(res.getStatus()).isEqualTo(InboundResult.STATUS_ACCEPTED);
        assertThat(res.isSignatureOk()).isTrue();
        assertThat(res.getParsedBody()).containsEntry("event", "x");
    }

    @Test
    void hmac_happy_accepts() throws Exception {
        String secret = "shhh-it-is-a-secret";
        byte[] body = "{\"event\":\"payment_captured\"}".getBytes();
        String sig = hmacHex(secret, body);
        var inst = instWith("{\"signatureScheme\":\"hmac-sha256-body\",\"signatureHeader\":\"X-Signature\"}");
        var res = adapter.ingest(inst, Map.of("X-Signature", sig), body,
                Map.of("hmacSecret", secret));
        assertThat(res.getStatus()).isEqualTo(InboundResult.STATUS_ACCEPTED);
        assertThat(res.isSignatureOk()).isTrue();
    }

    @Test
    void hmac_acceptsShaPrefix() throws Exception {
        String secret = "another";
        byte[] body = "[1,2,3]".getBytes();
        String sig = "sha256=" + hmacHex(secret, body);
        var inst = instWith("{\"signatureScheme\":\"hmac-sha256-body\",\"signatureHeader\":\"X-Signature\"}");
        var res = adapter.ingest(inst, Map.of("x-signature", sig), body,
                Map.of("hmacSecret", secret));
        assertThat(res.getStatus()).isEqualTo(InboundResult.STATUS_ACCEPTED);
    }

    @Test
    void hmac_tampered_rejected() throws Exception {
        String secret = "key";
        byte[] body = "{\"a\":1}".getBytes();
        String sig = hmacHex(secret, body);
        var inst = instWith("{\"signatureScheme\":\"hmac-sha256-body\",\"signatureHeader\":\"X-Signature\"}");
        // Tamper with the body after signing.
        byte[] tampered = "{\"a\":2}".getBytes();
        var res = adapter.ingest(inst, Map.of("X-Signature", sig), tampered,
                Map.of("hmacSecret", secret));
        assertThat(res.getStatus()).isEqualTo(InboundResult.STATUS_REJECTED);
    }

    @Test
    void hmac_unverifiedAllowed_passesWithFlag() throws Exception {
        var inst = instWith("{\"signatureScheme\":\"hmac-sha256-body\",\"signatureHeader\":\"X-Signature\",\"allowUnverified\":true}");
        var res = adapter.ingest(inst, Map.of(), "{}".getBytes(), Map.of("hmacSecret","k"));
        assertThat(res.getStatus()).isEqualTo(InboundResult.STATUS_ACCEPTED);
        assertThat(res.isSignatureOk()).isFalse();
    }

    @Test
    void unsupportedScheme_rejected() {
        var inst = instWith("{\"signatureScheme\":\"some-magic\"}");
        var res = adapter.ingest(inst, Map.of(), "{}".getBytes(), Map.of());
        assertThat(res.getStatus()).isEqualTo(InboundResult.STATUS_REJECTED);
        assertThat(res.getReason()).contains("unsupported_scheme");
    }

    @Test
    void supportsInbound() {
        assertThat(adapter.supportsInbound()).isTrue();
        assertThat(adapter.connectorCode()).isEqualTo(InboundWebhookAdapter.CODE);
    }

    private static String hmacHex(String secret, byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body));
    }
}
