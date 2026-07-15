---
description: Add an integration adapter for an external system (Tally, EPFO, GSTN, weighbridge, Slack, WhatsApp, banking, etc.). Sanctioned Java — the M11 pattern. Use this when you need to speak to a system outside EARMS.
---

# New M11 adapter — the sanctioned external-integration path

## When to invoke this skill

- Push data OUT to an external system (post to Slack, submit e-way bill to GSTN, upload to EPFO)
- Pull data IN from an external system (fetch bank statement, read from weighbridge over serial, poll a REST endpoint)
- Full 2-way integration (Tally sync, WhatsApp Business API)

## When NOT to invoke this skill

- Just writing to another EARMS table on a transition → `/new-listener`
- Sending a single SMS/email via existing infrastructure → the shipped `Msg91SmsAdapter` / `SMTPMailAdapter` already exist
- Reading data from an external SQL DB → M11 already has a `LegacySqlServerAdapter` pattern

## The one rule

- One adapter class per external system
- Under `com.idayam.m11.adapter.*` (this is the ONLY package outside `m0` where `@RestController` / `@Service` are allowed for external-facing work)
- Extends `com.idayam.m11.adapter.AbstractAdapter` (or implements the appropriate direction interface)
- Registered in `integration_connector_catalog` (SQL migration)
- Credentials never in code — always via `envelope_cipher` (see existing adapters)
- ≤ 400 lines per adapter — split by direction (Outbound / Inbound) if larger

## Template

```java
// src/main/java/com/idayam/m11/adapter/AcmeCrmOutboundAdapter.java
package com.idayam.m11.adapter;

import com.idayam.m11.secrets.EnvelopeCipher;
import com.idayam.m11.service.IntegrationInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Pushes lead + deal events to Acme CRM via their REST API.
 * Credentials envelope-encrypted at rest — see EnvelopeCipher.
 */
@Service
public class AcmeCrmOutboundAdapter implements OutboundAdapter {

    private static final Logger log = LoggerFactory.getLogger(AcmeCrmOutboundAdapter.class);
    private static final String ADAPTER_CODE = "acme-crm.v1.outbound";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    @Autowired private IntegrationInstanceService instances;
    @Autowired private EnvelopeCipher cipher;

    @Override public String code() { return ADAPTER_CODE; }

    @Override
    public AdapterResult send(Long tenantId, String eventKind, Map<String, Object> payload) {
        var instance = instances.findActive(tenantId, ADAPTER_CODE);
        if (instance.isEmpty()) return AdapterResult.skipped("no active instance");

        String apiKey = cipher.decrypt(instance.get().getCredentialCiphertext());
        String endpoint = instance.get().getEndpoint();

        try {
            var response = RestClient.builder()
                .baseUrl(endpoint)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build()
                .post()
                .uri("/events/" + eventKind)
                .body(payload)
                .retrieve()
                .toEntity(Map.class);

            return AdapterResult.ok(Map.of("acme_id", response.getBody().get("id")));
        } catch (Exception e) {
            log.warn("acme-crm send failed for tenant {} kind {}: {}",
                     tenantId, eventKind, e.getMessage());
            return AdapterResult.failed(e.getMessage());
        }
    }
}
```

Plus register the adapter in `integration_connector_catalog` in a SQL migration.

## Engineering principles

- **Credentials via `EnvelopeCipher` — never hardcoded.** Rotate keys by rotating the master key, not by touching adapters.
- **Timeouts on every external call.** 15 seconds max default.
- **Fail-open on adapter errors.** Return `AdapterResult.failed(...)` — never throw.
- **Idempotency-key headers where the target supports them.**
- **Rate limits respected.** Bucket4j or Guava RateLimiter locally.
- **Retries with backoff.** 3 attempts on 5xx/network, none on 4xx.
- **PII scrubbing in logs.** Log event kind + tenant + status; never payload.

## SonarQube rules that apply

| Rule | Enforcement |
|---|---|
| **S106** | No `System.out` — use `Logger` |
| **S138** | Method length ≤ 100 lines (relaxed for adapters) |
| **S3776** | Cognitive complexity ≤ 15 |
| **S107** | ≤ 5 params — wrap config in a class |
| **S1192** | Repeated URLs / header names → `private static final String` |
| **S2647** | Hardcoded credentials → CRITICAL |
| **S4423** | Weak TLS versions → TLS 1.2 minimum |
| **Custom** | Class name ends in `Adapter` |
| **Custom** | `code()` returns a matching `integration_connector_catalog` code |

## Coverage requirements

### 1. Unit test with mocked RestClient / HttpClient

Test the 5 canonical paths:
- Happy path (2xx response)
- Skip when no active instance
- Failed on 5xx
- Failed on network timeout
- Survives credential decrypt error without throwing

### 2. WireMock integration test

Assert the adapter posts the correct method + path + headers + body against a mock endpoint.

### 3. Playwright integration via M11 outbound endpoint

Assert that an M11 outbound-call event fires this adapter + a log row lands in `integration_call_log`.

## Reference

Existing adapters to read:
- `Msg91SmsAdapter` — outbound SMS
- `OutboundRestAdapter` — generic outbound REST
- `InboundWebhookAdapter` — inbound webhook receiver
- `TallyZplNicAdapter` — Tally + ZPL + NIC composite
- `SMTPMailAdapter` — outbound email

Standards: `docs/architecture/ENGINEERING-STANDARDS.md`
