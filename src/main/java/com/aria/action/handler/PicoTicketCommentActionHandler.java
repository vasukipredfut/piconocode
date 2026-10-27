package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.fieldservice.ServiceAccessValidator;
import com.aria.fieldservice.ServiceTicketService;
import com.aria.security.CurrentRole;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * pico.ticket-comment -- adds a comment to a ticket (ERPNext Issue comments). Allowed for the
 * ticket's technician, its creator, role Service, and Manager / TenantAdmin.
 * args: {@code ticket_id}, {@code comment} (required), {@code is_internal} (staff-only, hidden on the portal).
 */
@Component
public class PicoTicketCommentActionHandler implements ActionHandler {

    public static final String CODE = "pico.ticket-comment";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ServiceTicketService tickets;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> a = inv.getArgs() == null ? Map.of() : inv.getArgs();
        if (a.get("ticket_id") == null) throw new IllegalArgumentException("ticket_id is required");
        long ticketId = PicoLeadCreateActionHandler.id(a.get("ticket_id"), "ticket_id");
        String comment = a.get("comment") == null ? null : a.get("comment").toString().trim();
        if (comment == null || comment.isEmpty()) throw new IllegalArgumentException("comment is required");
        if (comment.length() > 5000) throw new IllegalArgumentException("comment must be at most 5000 characters");
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        long me = ((Number) emp.get("id")).longValue();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT technician_id, created_by_emp_id FROM m9_service_ticket WHERE tenant_id=? AND id=?", TenantContext.get(), ticketId);
        if (rows.isEmpty()) return ActionResult.error("Ticket not found");
        boolean onTicket = same(rows.get(0).get("technician_id"), me) || same(rows.get(0).get("created_by_emp_id"), me);
        if (!onTicket && !CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN, ServiceAccessValidator.SERVICE_ROLE)) {
            return ActionResult.error("Ticket not found");
        }
        Object internal = a.get("is_internal");
        tickets.comment(ticketId, me, (String) emp.get("full_name"), comment,
                Boolean.TRUE.equals(internal) || "true".equalsIgnoreCase(String.valueOf(internal)));
        return ActionResult.builder().ok(true).message("Comment added").recordId(ticketId).build();
    }

    private static boolean same(Object a, long b) {
        return a instanceof Number n && n.longValue() == b;
    }
}
