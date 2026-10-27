package com.aria.fieldservice;

import com.aria.hr.leave.PicoLeaveRules;
import com.aria.integration.service.TenantMailService;
import com.aria.security.TenantContext;
import com.aria.tenant.TenantSettings;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Ticket creation shared by the two entry points that cannot share an engine page: the staff
 * action pico.ticket-create and the unauthenticated PublicSupportController. Inserts
 * m9_service_ticket (OPEN), starts M9.ServiceTicket, and for portal tickets mails
 * service.notify_emp_id. Not @Transactional: the engine has its own transaction manager; if the
 * instance cannot start the ticket row is removed again.
 */
@Component
public class ServiceTicketService {

    private final JdbcTemplate jdbc;
    private final WorkflowEngineService workflowEngine;
    private final TenantMailService mail;
    private final TenantSettings settings;
    private final PicoLeaveRules people;

    public ServiceTicketService(JdbcTemplate jdbc, WorkflowEngineService workflowEngine, TenantMailService mail,
                                TenantSettings settings, PicoLeaveRules people) {
        this.jdbc = jdbc;
        this.workflowEngine = workflowEngine;
        this.mail = mail;
        this.settings = settings;
        this.people = people;
    }

    /** Fields: customer_id, customer_name, subject, description, issue_kind, priority, contact_*, site_address. */
    public Map<String, Object> create(Map<String, Object> f, String source, Long createdByEmpId) {
        Long tenantId = TenantContext.get();
        Object[] values = {tenantId, "TMP-" + UUID.randomUUID().toString().substring(0, 24), f.get("customer_name"), f.get("site_address"),
                f.get("issue_kind"), f.getOrDefault("priority", "P3"), f.get("subject"), f.get("description"), f.get("customer_id"),
                f.get("contact_name"), f.get("contact_mobile"), f.get("contact_email"), source, createdByEmpId};
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO m9_service_ticket (tenant_id, ticket_no, customer_name, site_address, issue_kind, priority, subject, description, " +
                            "customer_id, contact_name, contact_mobile, contact_email, source, created_by_emp_id, status, current_step) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,'OPEN','OPEN')",
                    Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, keys);
        long id = keys.getKey().longValue();
        String number = String.format("TKT-%d-%05d", LocalDate.now().getYear(), id);
        WorkflowInstance inst;
        try {
            inst = workflowEngine.startInstance("M9.ServiceTicket", Map.of("businessRef", "ServiceTicket#" + id));
        } catch (RuntimeException e) {
            jdbc.update("DELETE FROM m9_service_ticket WHERE tenant_id=? AND id=?", tenantId, id);
            throw e;
        }
        jdbc.update("UPDATE m9_service_ticket SET ticket_no=?, workflow_instance_id=? WHERE tenant_id=? AND id=?", number, inst.getId(), tenantId, id);

        if ("PORTAL".equals(source)) {
            String notify = settings.get("service.notify_emp_id", null);
            if (notify != null && notify.trim().matches("\\d+")) {
                long empId = Long.parseLong(notify.trim());
                mail.send(people.emailOf(empId), "New Support Ticket - " + number,
                        "<p>A customer raised a support ticket from the portal.</p><p><b>Ticket:</b> " + number + "<br><b>Customer:</b> "
                                + esc(f.get("customer_name")) + "<br><b>Mobile:</b> " + esc(f.get("contact_mobile")) + "<br><b>Subject:</b> "
                                + esc(f.get("subject")) + "</p><p>" + esc(f.get("description")).replace("\n", "<br>") + "</p>");
            }
        }
        return Map.of("ticket_id", id, "ticket_no", number, "status", "OPEN", "workflow_instance_id", inst.getId());
    }

    public void comment(long ticketId, Long authorEmpId, String authorName, String comment, boolean internal) {
        jdbc.update("INSERT INTO m9_ticket_comment (tenant_id, ticket_id, author_emp_id, author_name, comment, is_internal) VALUES (?,?,?,?,?,?)",
                TenantContext.get(), ticketId, authorEmpId, authorName, comment, internal ? 1 : 0);
    }

    private static String esc(Object o) {
        return o == null ? "" : HtmlUtils.htmlEscape(o.toString());
    }
}
