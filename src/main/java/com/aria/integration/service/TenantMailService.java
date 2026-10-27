package com.aria.integration.service;

import com.aria.integration.adapter.OutboundResult;
import com.aria.integration.adapter.SmtpEmailAdapter;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

/**
 * Sends an HTML mail through the tenant's ACTIVE SMTP_EMAIL integration instance (M11), so
 * credentials, retries, circuit breaker and the integration_event_log audit all come from the
 * integration hub. Replaces ERPNext frappe.sendmail for the PICO flows.
 *
 * <p>Inside a transaction the mail goes out after commit: a rolled-back transition must not have
 * already told anyone it happened, and SMTP retries must not hold row locks. A tenant without an
 * SMTP instance just logs and skips -- mail is never allowed to fail the business action.
 */
@Service
public class TenantMailService {

    private static final Logger log = LoggerFactory.getLogger(TenantMailService.class);

    private final JdbcTemplate jdbc;
    private final IntegrationOutboundCaller outbound;

    public TenantMailService(JdbcTemplate jdbc, IntegrationOutboundCaller outbound) {
        this.jdbc = jdbc;
        this.outbound = outbound;
    }

    public void send(String to, String subject, String html) {
        if (to == null || to.isBlank()) {
            log.info("mail skipped (no recipient): {}", subject);
            return;
        }
        Long tenantId = TenantContext.get();
        Runnable job = () -> deliver(tenantId, to, subject, html, false);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { job.run(); }
            });
        } else {
            job.run();
        }
    }

    /**
     * Sends right away and reports whether the SMTP instance accepted it (one-time codes, where the
     * caller must tell the user if nothing went out). {@code sensitive=true} marks the payload so the
     * integration event log redacts the body (see IntegrationOutboundCaller.redact).
     */
    public boolean sendNow(String to, String subject, String html, boolean sensitive) {
        if (to == null || to.isBlank()) return false;
        return deliver(TenantContext.get(), to, subject, html, sensitive);
    }

    private boolean deliver(Long tenantId, String to, String subject, String html, boolean sensitive) {
        Long previous = TenantContext.peek();
        try {
            TenantContext.set(tenantId);
            List<Long> ids = jdbc.queryForList(
                    "SELECT id FROM integration_instance WHERE tenant_id=? AND connector_code=? AND status='ACTIVE' ORDER BY id LIMIT 1",
                    Long.class, tenantId, SmtpEmailAdapter.CODE);
            if (ids.isEmpty()) {
                log.info("mail skipped (tenant {} has no ACTIVE {} integration): {}", tenantId, SmtpEmailAdapter.CODE, subject);
                return false;
            }
            OutboundResult r = outbound.invoke(ids.get(0), "sendEmail",
                    Map.of("to", to, "subject", subject, "body", html, "html", true, "sensitive", sensitive));
            if (!r.isOk()) log.warn("mail to {} failed: {} {}", to, r.getErrorCode(), r.getErrorMessage());
            return r.isOk();
        } catch (Exception e) {
            log.warn("mail to {} failed: {}", to, e.toString());
            return false;
        } finally {
            if (previous == null) TenantContext.clear(); else TenantContext.set(previous);
        }
    }
}
