package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.fieldservice.ServiceTicketService;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.ticket-create -- staff-entered service ticket (ERPNext Issue from the desk / phone / mail).
 * Customers raise their own through PublicSupportController.
 *
 * <p>args: {@code customer_id}, {@code subject} (required); {@code description}, {@code issue_kind},
 * {@code priority} (P1-P4, default P3), {@code contact_name}, {@code contact_mobile},
 * {@code contact_email} (default: the customer's), {@code site_address}, {@code source}
 * (APP | PHONE | EMAIL, default APP).
 */
@Component
public class PicoTicketCreateActionHandler implements ActionHandler {

    public static final String CODE = "pico.ticket-create";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ServiceTicketService tickets;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> a = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        if (a.get("customer_id") == null) throw new IllegalArgumentException("customer_id is required");
        long customerId = PicoLeadCreateActionHandler.id(a.get("customer_id"), "customer_id");
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT customer_name, contact_name, mobile_no, email_id, address FROM m8_customer WHERE tenant_id=? AND id=? AND is_active=1",
                TenantContext.get(), customerId);
        if (rows.isEmpty()) return ActionResult.error("Customer not found");
        Map<String, Object> c = rows.get(0);

        String priority = PicoLeadCreateActionHandler.text(a, "priority", 2, false);
        if (priority != null && !priority.matches("P[1-4]")) throw new IllegalArgumentException("priority must be P1, P2, P3 or P4");
        String source = PicoLeadCreateActionHandler.text(a, "source", 8, false);
        if (source != null && !source.matches("APP|PHONE|EMAIL")) throw new IllegalArgumentException("source must be APP, PHONE or EMAIL");
        String description = a.get("description") == null ? null : a.get("description").toString().trim();
        if (description != null && description.length() > 10000) throw new IllegalArgumentException("description must be at most 10000 characters");

        Map<String, Object> f = new HashMap<>();
        f.put("customer_id", customerId);
        f.put("customer_name", c.get("customer_name"));
        f.put("subject", PicoLeadCreateActionHandler.text(a, "subject", 256, true));
        f.put("description", description);
        f.put("issue_kind", PicoLeadCreateActionHandler.text(a, "issue_kind", 32, false));
        f.put("priority", priority == null ? "P3" : priority);
        f.put("contact_name", orDefault(PicoLeadCreateActionHandler.text(a, "contact_name", 128, false), c.get("contact_name")));
        f.put("contact_mobile", orDefault(PicoLeadCreateActionHandler.text(a, "contact_mobile", 32, false), c.get("mobile_no")));
        f.put("contact_email", orDefault(PicoLeadCreateActionHandler.text(a, "contact_email", 128, false), c.get("email_id")));
        String site = PicoLeadCreateActionHandler.text(a, "site_address", 256, false);
        Object addr = c.get("address");
        f.put("site_address", site != null ? site : addr == null ? null : addr.toString().substring(0, Math.min(256, addr.toString().length())));

        Map<String, Object> created = tickets.create(f, source == null ? "APP" : source, ((Number) emp.get("id")).longValue());
        return ActionResult.builder().ok(true).message("Ticket " + created.get("ticket_no") + " created")
                .recordId(created.get("ticket_id")).data(created).build();
    }

    private static Object orDefault(Object v, Object fallback) {
        return v != null ? v : fallback;
    }
}
