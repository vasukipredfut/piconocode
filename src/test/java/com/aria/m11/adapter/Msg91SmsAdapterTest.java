package com.aria.m11.adapter;

import com.aria.m11.model.IntegrationInstance;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Msg91SmsAdapter}. Exercises the dry-run path
 * (no real HTTP) for happy + error cases; full network paths are covered
 * by IT against a stub server later.
 */
class Msg91SmsAdapterTest {

    private final Msg91SmsAdapter adapter = new Msg91SmsAdapter();

    @Test
    void connectorCode_isMsg91Sms() {
        assertThat(adapter.connectorCode()).isEqualTo("MSG91_SMS");
        assertThat(adapter.supportsOutbound()).isTrue();
    }

    @Test
    void sendSms_dryRun_emits_ok() {
        IntegrationInstance inst = instanceWith("{\"dryRun\":true,\"senderId\":\"PICOPL\"}");
        OutboundResult r = adapter.invoke(inst, "sendSms",
                Map.of("to", "+919876543210", "templateId", "T-1", "variables", Map.of("name", "Suresh")),
                Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(r.getBody()).contains("dryRun");
    }

    @Test
    void sendOtp_dryRun_emits_ok_without_otp_in_log_body() {
        IntegrationInstance inst = instanceWith("{\"dryRun\":true}");
        OutboundResult r = adapter.invoke(inst, "sendOtp",
                Map.of("to", "+919876543210", "otp", "123456"), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).doesNotContain("123456");
    }

    @Test
    void unknownOperation_returns422_likeError() {
        IntegrationInstance inst = instanceWith("{\"dryRun\":true}");
        OutboundResult r = adapter.invoke(inst, "wishWell", Map.of(), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("unknown_op");
    }

    @Test
    void missingTo_returnsBadRequest() {
        IntegrationInstance inst = instanceWith("{\"dryRun\":true}");
        OutboundResult r = adapter.invoke(inst, "sendSms", Map.of("templateId", "T-1"), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("bad_request");
        assertThat(r.getErrorMessage()).contains("to");
    }

    @Test
    void ping_dryRun_isOk_withoutNetwork() {
        IntegrationInstance inst = instanceWith("{\"dryRun\":true}");
        PingResult r = adapter.ping(inst, Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getMessage()).contains("dryRun");
    }

    private IntegrationInstance instanceWith(String configJson) {
        IntegrationInstance i = new IntegrationInstance();
        i.setConfigJson(configJson);
        i.setTenantId(1L);
        i.setConnectorCode("MSG91_SMS");
        i.setCode("test-msg91");
        return i;
    }
}
