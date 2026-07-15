package com.aria.m11.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.model.IntegrationInstance;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * SMTP outbound email adapter — pluggable into any SMTP provider
 * (Gmail, Office 365, SES, SendGrid SMTP, Zoho Mail, custom relay).
 *
 * <p>Operations:
 * <ul>
 *   <li>{@code sendEmail} — payload {to, subject, body, html?, cc?, bcc?, replyTo?}</li>
 * </ul>
 *
 * <p>Config (matches catalog config_schema_json):
 * <pre>{@code
 * {
 *   "host": "smtp.gmail.com",          // required (non-dryRun)
 *   "port": 587,                       // default 587
 *   "useTls": true,                    // STARTTLS, default true
 *   "useSsl": false,                   // SMTPS, default false
 *   "from": "noreply@picopower.in",    // required
 *   "fromName": "Pico Power Project",  // optional display name
 *   "dryRun": true                     // log only, default false
 * }
 * }</pre>
 *
 * <p>Secret: {@code password} (and optionally {@code username} if different
 * from {@code from}). Retrieved from {@code secrets["password"]}.
 *
 * <p>Resilience: dryRun mode logs the payload and returns success without a
 * network call — lets the workflow drive end-to-end without burning real
 * email quota.
 */
@Component
public class SmtpEmailAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailAdapter.class);
    public static final String CODE = "SMTP_EMAIL";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override public String connectorCode() { return CODE; }
    @Override public boolean supportsOutbound() { return true; }

    @Override
    public PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        long t0 = System.nanoTime();
        try {
            Map<String, Object> cfg = readConfig(instance);
            boolean dryRun = Boolean.TRUE.equals(cfg.get("dryRun"));
            if (dryRun) {
                return PingResult.builder().ok(true)
                        .message("dryRun mode — skipping SMTP connection")
                        .latencyMs((System.nanoTime() - t0) / 1_000_000).build();
            }
            String host = (String) cfg.get("host");
            if (host == null || host.isBlank()) return PingResult.fail("host not configured");
            String pw = secrets == null ? null : (String) secrets.get("password");
            if (pw == null || pw.isBlank()) return PingResult.fail("password secret missing");
            JavaMailSenderImpl ms = buildSender(cfg, secrets);
            ms.testConnection();   // throws on auth/connect failure
            return PingResult.builder().ok(true).message("connected to " + host + ":" + ms.getPort())
                    .latencyMs((System.nanoTime() - t0) / 1_000_000).build();
        } catch (Exception e) {
            return PingResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public OutboundResult invoke(IntegrationInstance instance, String operation,
                                  Map<String, Object> payload, Map<String, Object> secrets) {
        if (!"sendEmail".equals(operation)) {
            return OutboundResult.failed("unsupported_operation", "Unsupported: " + operation);
        }
        Map<String, Object> cfg = readConfig(instance);
        boolean dryRun = Boolean.TRUE.equals(cfg.get("dryRun"));
        String to = String.valueOf(payload.get("to"));
        String subject = String.valueOf(payload.getOrDefault("subject", "(no subject)"));
        String body = String.valueOf(payload.getOrDefault("body", ""));
        boolean isHtml = Boolean.TRUE.equals(payload.get("html"));

        if (dryRun) {
            log.info("SmtpEmailAdapter [dryRun] to={} subject={} bodyLen={}", to, subject, body.length());
            return OutboundResult.success(200, java.util.Map.of("X-Dry-Run","true"), "{\"ok\":true,\"dryRun\":true}");
        }

        try {
            JavaMailSenderImpl ms = buildSender(cfg, secrets);
            MimeMessage msg = ms.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            String from = (String) cfg.get("from");
            String fromName = (String) cfg.get("fromName");
            if (fromName != null && !fromName.isBlank()) {
                h.setFrom(from, fromName);
            } else {
                h.setFrom(from);
            }
            h.setTo(to.split("[,;\\s]+"));
            String cc = (String) payload.get("cc");
            if (cc != null && !cc.isBlank()) h.setCc(cc.split("[,;\\s]+"));
            String bcc = (String) payload.get("bcc");
            if (bcc != null && !bcc.isBlank()) h.setBcc(bcc.split("[,;\\s]+"));
            String replyTo = (String) payload.get("replyTo");
            if (replyTo != null && !replyTo.isBlank()) h.setReplyTo(replyTo);
            h.setSubject(subject);
            h.setText(body, isHtml);

            ms.send(msg);
            log.info("SmtpEmailAdapter sent to={} subject={}", to, subject);
            return OutboundResult.success(200, java.util.Map.of(),
                "{\"messageId\":\"" + escape(String.valueOf(msg.getMessageID())) + "\",\"to\":\"" + escape(to) + "\"}");
        } catch (org.springframework.mail.MailSendException mse) {
            log.warn("SmtpEmailAdapter transient send failure: {}", mse.toString());
            return OutboundResult.retryable("smtp_transient", mse.getMessage());
        } catch (Exception e) {
            log.warn("SmtpEmailAdapter send failed: {}", e.toString());
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }

    // ---- helpers -----------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) {
        try {
            String json = instance == null ? null : instance.getConfigJson();
            if (json == null || json.isBlank()) return Map.of();
            return mapper.readValue(json, Map.class);
        } catch (IOException e) {
            log.warn("SmtpEmailAdapter cfg parse failed: {}", e.getMessage());
            return Map.of();
        }
    }

    private JavaMailSenderImpl buildSender(Map<String, Object> cfg, Map<String, Object> secrets) {
        JavaMailSenderImpl s = new JavaMailSenderImpl();
        s.setHost((String) cfg.get("host"));
        Object portObj = cfg.get("port");
        int port = 587;
        if (portObj instanceof Number n) port = n.intValue();
        else if (portObj instanceof String ps) port = Integer.parseInt(ps);
        s.setPort(port);
        String username = (String) (secrets != null && secrets.get("username") != null
                ? secrets.get("username") : cfg.get("from"));
        String password = (String) (secrets != null ? secrets.get("password") : null);
        if (username != null) s.setUsername(username);
        if (password != null) s.setPassword(password);

        Properties props = s.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if (Boolean.TRUE.equals(cfg.getOrDefault("useTls", true))) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        if (Boolean.TRUE.equals(cfg.get("useSsl"))) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        props.put("mail.smtp.timeout", "8000");
        props.put("mail.smtp.connectiontimeout", "8000");
        return s;
    }
}
