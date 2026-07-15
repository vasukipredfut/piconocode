package com.aria.m11.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.model.IntegrationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Email-IMAP inbound connector — polls a mailbox for new vendor/customer
 * email and surfaces parsed content as Inbound events that other M11
 * pipelines can route.
 *
 * <p>Operations (outbound side — manual poll/test):
 * <ul>
 *   <li>{@code poll} — payload {folder} → returns the latest N messages</li>
 *   <li>{@code markRead} — payload {uid} → flags message as read</li>
 * </ul>
 *
 * <p>Inbound: real impl would schedule a JavaMail / Jakarta Mail poller every
 * N minutes and emit InboundResult for each new email. dryRun returns
 * synthetic emails so the Email-to-Lead / Email-to-Ticket pipelines can be
 * smoke-tested.
 *
 * <p>Config: {@code { "host":"imap.gmail.com", "port":993, "username":"...",
 *   "folder":"INBOX", "pollIntervalMin":5, "dryRun":true }}.
 */
@Component
public class EmailImapInboundAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(EmailImapInboundAdapter.class);
    public static final String CODE = "EMAIL_IMAP_INBOUND";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override public String connectorCode() { return CODE; }
    @Override public boolean supportsInbound() { return true; }
    @Override public boolean supportsOutbound() { return true; }

    @Override
    public PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        try {
            Map<String, Object> config = readConfig(instance);
            if (Boolean.TRUE.equals(config.get("dryRun"))) {
                return PingResult.builder().ok(true).message("dryRun mode").build();
            }
            return PingResult.builder().ok(false).message(
                    "Live IMAP probe unwired; flip dryRun=true (host: " +
                            config.getOrDefault("host", "?") + ")").build();
        } catch (Exception e) {
            return PingResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public OutboundResult invoke(IntegrationInstance instance, String operation,
                                  Map<String, Object> payload, Map<String, Object> secrets) {
        try {
            Map<String, Object> config = readConfig(instance);
            boolean dryRun = Boolean.TRUE.equals(config.get("dryRun"));
            String op = operation == null ? "" : operation.trim();
            return switch (op) {
                case "poll" -> poll(payload, config, dryRun);
                case "markRead" -> markRead(payload, config, dryRun);
                default -> OutboundResult.failed("unknown_op",
                        "Email-IMAP supports poll and markRead; got: " + operation);
            };
        } catch (Exception e) {
            log.warn("Email IMAP invoke failed: {}", e.toString());
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private OutboundResult poll(Map<String, Object> p, Map<String, Object> config, boolean dryRun) throws IOException {
        String folder = String.valueOf(p.getOrDefault("folder", config.getOrDefault("folder", "INBOX")));
        if (dryRun) {
            log.info("Email-IMAP DRY-RUN poll folder={}", folder);
            return OutboundResult.builder().ok(true).status(200)
                    .body(mapper.writeValueAsString(Map.of(
                            "folder", folder,
                            "messages", List.of(
                                    Map.of("uid", 1001, "from", "vendor@testro.com",
                                            "subject", "Invoice INV-99876 attached",
                                            "snippet", "Please find attached our invoice for the recent dispatch...",
                                            "hasAttachment", true,
                                            "suggestedRoute", "create-vendor-invoice"),
                                    Map.of("uid", 1002, "from", "purchasing@cfp.in",
                                            "subject", "Inquiry — 500kg sunflower oil",
                                            "snippet", "We would like to enquire about pricing for 500kg...",
                                            "hasAttachment", false,
                                            "suggestedRoute", "create-lead")),
                            "dryRun", true)))
                    .build();
        }
        return OutboundResult.failed("not_implemented_live", "Live IMAP poll unwired; flip dryRun=true");
    }

    private OutboundResult markRead(Map<String, Object> p, Map<String, Object> config, boolean dryRun) throws IOException {
        Object uid = p.get("uid");
        if (uid == null) return OutboundResult.failed("bad_request", "payload.uid required");
        if (dryRun) {
            log.info("Email-IMAP DRY-RUN markRead uid={}", uid);
            return OutboundResult.builder().ok(true).status(200)
                    .body(mapper.writeValueAsString(Map.of("uid", uid, "marked", "READ", "dryRun", true)))
                    .build();
        }
        return OutboundResult.failed("not_implemented_live", "Live IMAP markRead unwired; flip dryRun=true");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws IOException {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return new HashMap<>();
        return mapper.readValue(json, Map.class);
    }
}
