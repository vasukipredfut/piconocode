package com.aria.fieldservice;

import com.aria.integration.adapter.OutboundResult;
import com.aria.integration.service.IntegrationOutboundCaller;
import com.aria.integration.service.TenantMailService;
import com.aria.security.TenantContext;
import com.aria.tenant.TenantSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Customer one-time code for installation reports (ERPNext create_installation_report /
 * send_otp_email / send_otp_sms / verify_installation_report_otp), hardened:
 * <ul>
 *   <li>6 digits from SecureRandom (ERPNext: 4 digits, random.randint)</li>
 *   <li>only SHA-256(salt:code:reportId) is stored (ERPNext: the code in two plain fields)</li>
 *   <li>expires after service.otp_ttl_minutes (default 30); 5 wrong attempts burn the code;
 *       at most 5 sends per report</li>
 *   <li>email via TenantMailService.sendNow(sensitive); SMS via the tenant's ACTIVE MSG91_SMS
 *       instance and DLT template service.otp_sms_template_id (ERPNext called Fast2SMS with a key
 *       hard-coded in the source)</li>
 * </ul>
 * Verification compares hashes in constant time.
 */
@Component
public class InstallationOtpService {

    public enum Outcome { OK, WRONG, EXPIRED, LOCKED, NO_CODE }

    static final int MAX_ATTEMPTS = 5;
    static final int MAX_SENDS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Logger log = LoggerFactory.getLogger(InstallationOtpService.class);

    private final JdbcTemplate jdbc;
    private final TenantMailService mail;
    private final IntegrationOutboundCaller outbound;
    private final TenantSettings settings;

    public InstallationOtpService(JdbcTemplate jdbc, TenantMailService mail, IntegrationOutboundCaller outbound, TenantSettings settings) {
        this.jdbc = jdbc;
        this.mail = mail;
        this.outbound = outbound;
        this.settings = settings;
    }

    /**
     * Issues a fresh code for the report and sends it. Returns the masked destinations it went to
     * (empty = nothing was delivered). Throws IllegalStateException when the send limit is reached.
     */
    public List<String> issue(Map<String, Object> report) {
        long id = ((Number) report.get("id")).longValue();
        int sends = ((Number) report.get("otp_send_count")).intValue();
        if (sends >= MAX_SENDS) throw new IllegalStateException("The code was already sent " + MAX_SENDS + " times; cancel and re-create the report");

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = HexFormat.of().formatHex(saltBytes);
        int ttl = ttlMinutes();
        LocalDateTime now = LocalDateTime.now();

        List<String> sentTo = new ArrayList<>();
        String email = (String) report.get("email");
        if (email != null && !email.isBlank() && mail.sendNow(email,
                "OTP Verification - " + report.get("report_number"), body(report, code, ttl), true)) {
            sentTo.add(maskEmail(email));
        }
        String mobile = (String) report.get("contact_number");
        if (mobile != null && !mobile.isBlank() && sms(mobile, code, (String) report.get("report_number"))) {
            sentTo.add(maskMobile(mobile));
        }
        if (sentTo.isEmpty()) return sentTo; // keep the previous code (if any) valid: nothing reached the customer

        jdbc.update("UPDATE m9_installation_report SET otp_hash=?, otp_salt=?, otp_sent_at=?, otp_expires_at=?, otp_attempts=0, " +
                        "otp_send_count=otp_send_count+1, otp_sent_to=? WHERE tenant_id=? AND id=?",
                hash(salt, code, id), salt, Timestamp.valueOf(now), Timestamp.valueOf(now.plusMinutes(ttl)),
                String.join(", ", sentTo), TenantContext.get(), id);
        return sentTo;
    }

    /** Checks a code; counts wrong attempts. Does not change the workflow. */
    public Outcome verify(Map<String, Object> report, String code) {
        long id = ((Number) report.get("id")).longValue();
        String stored = (String) report.get("otp_hash");
        if (stored == null) return Outcome.NO_CODE;
        Object exp = report.get("otp_expires_at");
        LocalDateTime expires = exp instanceof LocalDateTime l ? l : ((Timestamp) exp).toLocalDateTime();
        if (LocalDateTime.now().isAfter(expires)) return Outcome.EXPIRED;
        // Reserve the attempt atomically before comparing, so parallel guesses cannot exceed the limit.
        int reserved = jdbc.update("UPDATE m9_installation_report SET otp_attempts=otp_attempts+1 " +
                "WHERE tenant_id=? AND id=? AND otp_hash=? AND otp_attempts < ?", TenantContext.get(), id, stored, MAX_ATTEMPTS);
        if (reserved == 0) return Outcome.LOCKED;
        String candidate = code == null ? "" : code.trim();
        boolean match = candidate.matches("\\d{6}") && MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.US_ASCII),
                hash((String) report.get("otp_salt"), candidate, id).getBytes(StandardCharsets.US_ASCII));
        if (!match) {
            Integer used = jdbc.queryForObject("SELECT otp_attempts FROM m9_installation_report WHERE tenant_id=? AND id=?",
                    Integer.class, TenantContext.get(), id);
            return used != null && used >= MAX_ATTEMPTS ? Outcome.LOCKED : Outcome.WRONG;
        }
        return Outcome.OK;
    }

    public int ttlMinutes() {
        try {
            return Math.max(5, Math.min(1440, Integer.parseInt(settings.get("service.otp_ttl_minutes", "30").trim())));
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    private boolean sms(String mobile, String code, String reportNumber) {
        String template = settings.get("service.otp_sms_template_id", null);
        if (template == null) return false;
        List<Long> ids = jdbc.queryForList(
                "SELECT id FROM integration_instance WHERE tenant_id=? AND connector_code='MSG91_SMS' AND status='ACTIVE' ORDER BY id LIMIT 1",
                Long.class, TenantContext.get());
        if (ids.isEmpty()) return false;
        try {
            OutboundResult r = outbound.invoke(ids.get(0), "sendSms", Map.of(
                    "to", digits(mobile), "templateId", template, "sensitive", true,
                    "variables", Map.of("otp", code, "doc", reportNumber)));
            if (!r.isOk()) log.warn("OTP SMS for {} failed: {} {}", reportNumber, r.getErrorCode(), r.getErrorMessage());
            return r.isOk();
        } catch (Exception e) {
            log.warn("OTP SMS for {} failed: {}", reportNumber, e.toString());
            return false;
        }
    }

    private static String body(Map<String, Object> report, String code, int ttl) {
        return "<p>Dear Customer,</p><p>Your OTP for Installation Report verification is: <b style='font-size:18px'>" + code + "</b></p>"
                + "<p>Document Number: " + HtmlUtils.htmlEscape(String.valueOf(report.get("report_number")))
                + (report.get("doc_no") == null ? "" : " (" + HtmlUtils.htmlEscape(report.get("doc_no").toString()) + ")") + "</p>"
                + "<p>It is valid for " + ttl + " minutes. Please share it only with the service engineer at your site, "
                + "and only once the work is complete.</p><p>Regards,<br>PICO Automation</p>";
    }

    static String hash(String salt, String code, long id) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest((salt + ":" + code + ":" + id).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static String digits(String mobile) {
        String d = mobile.replaceAll("\\D", "");
        return d.length() == 10 ? "91" + d : d;
    }

    static String maskEmail(String email) {
        int at = email.indexOf('@');
        return at <= 1 ? "***" + email.substring(Math.max(at, 0)) : email.charAt(0) + "***" + email.substring(at);
    }

    static String maskMobile(String mobile) {
        String d = mobile.replaceAll("\\D", "");
        return d.length() <= 4 ? "****" : "******" + d.substring(d.length() - 4);
    }
}
