package com.aria.fieldservice;

import com.aria.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Customer support portal without login (replaces customer_support.api.create_issue /
 * get_customer_issues and www/support.html). Needs {@code "/api/public/support/**"} in
 * WebSecurityConfig's permitAll list (see module-06-service/README.md).
 *
 * <ul>
 *   <li>{@code POST /api/public/support/{tenantCode}/tickets} {customer_name, mobile, subject,
 *       description, email?}: the mobile must be the registered mobile of an active m8_customer
 *       of that tenant (ERPNext only needed an existing customer NAME). Returns the ticket number.</li>
 *   <li>{@code GET /api/public/support/{tenantCode}/tickets/{ticketNo}?mobile=}: status and public
 *       comments of ONE ticket, only when the mobile matches the ticket's. ERPNext listed every issue
 *       of any customer whose name you typed.</li>
 * </ul>
 * Tenant: the permit-all filter would default to tenant 1, so the tenant is resolved here from the
 * ACTIVE tenant.code in the URL and set on TenantContext for the request. Abuse: 10 requests per
 * IP per 10 minutes (in-memory, per node); fronting with a CAPTCHA is recommended.
 */
@RestController
@RequestMapping("/api/public/support/{tenantCode}")
public class PublicSupportController {

    private static final int WINDOW_MS = 10 * 60 * 1000;
    private static final int MAX_PER_WINDOW = 10;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    private final JdbcTemplate jdbc;
    private final ServiceTicketService tickets;

    public PublicSupportController(JdbcTemplate jdbc, ServiceTicketService tickets) {
        this.jdbc = jdbc;
        this.tickets = tickets;
    }

    @PostMapping("/tickets")
    public ResponseEntity<Map<String, Object>> create(@PathVariable String tenantCode, @RequestBody Map<String, Object> body,
                                                      HttpServletRequest req) {
        if (limited(req)) return error(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later");
        Long tenantId = tenant(tenantCode);
        if (tenantId == null) return error(HttpStatus.NOT_FOUND, "Unknown company");
        String name = text(body, "customer_name", 256), mobile = text(body, "mobile", 32), subject = text(body, "subject", 256);
        String description = text(body, "description", 5000), email = text(body, "email", 128);
        if (name == null || mobile == null || subject == null || description == null) {
            return error(HttpStatus.BAD_REQUEST, "All fields are required");
        }
        Long previous = TenantContext.peek();
        try {
            TenantContext.set(tenantId);
            Map<String, Object> customer = customerByMobile(mobile);
            if (customer == null) return error(HttpStatus.NOT_FOUND, "We could not find a customer with this mobile number. Please contact us.");
            Map<String, Object> f = new HashMap<>();
            f.put("customer_id", customer.get("id"));
            f.put("customer_name", customer.get("customer_name"));
            f.put("contact_name", name);
            f.put("contact_mobile", mobile);
            f.put("contact_email", email != null ? email : customer.get("email_id"));
            f.put("subject", subject);
            f.put("description", description);
            f.put("priority", "P3");
            Map<String, Object> created = tickets.create(f, "PORTAL", null);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Issue Created Successfully : " + created.get("ticket_no"),
                    "ticket_no", created.get("ticket_no")));
        } finally {
            if (previous == null) TenantContext.clear(); else TenantContext.set(previous);
        }
    }

    @GetMapping("/tickets/{ticketNo}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String tenantCode, @PathVariable String ticketNo,
                                                      @RequestParam String mobile, HttpServletRequest req) {
        if (limited(req)) return error(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later");
        Long tenantId = tenant(tenantCode);
        if (tenantId == null) return error(HttpStatus.NOT_FOUND, "Unknown company");
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, ticket_no, subject, status, reported_at, closed_at, resolution, contact_mobile FROM m9_service_ticket WHERE tenant_id=? AND ticket_no=?",
                tenantId, ticketNo);
        if (rows.isEmpty() || !sameMobile((String) rows.get(0).get("contact_mobile"), mobile)) {
            return error(HttpStatus.NOT_FOUND, "Ticket not found");
        }
        Map<String, Object> t = new LinkedHashMap<>(rows.get(0));
        t.remove("contact_mobile");
        t.put("updates", jdbc.queryForList(
                "SELECT author_name, comment, created_at FROM m9_ticket_comment WHERE tenant_id=? AND ticket_id=? AND is_internal=0 ORDER BY created_at",
                tenantId, t.remove("id")));
        return ResponseEntity.ok(Map.of("status", "success", "ticket", t));
    }

    private Long tenant(String code) {
        if (code == null || !code.matches("[A-Za-z0-9_-]{1,64}")) return null;
        return jdbc.queryForList("SELECT id FROM tenant WHERE code=? AND status='ACTIVE'", Long.class, code).stream().findFirst().orElse(null);
    }

    private Map<String, Object> customerByMobile(String mobile) {
        String last10 = last10(mobile);
        if (last10 == null) return null;
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, customer_name, email_id FROM m8_customer WHERE tenant_id=? AND is_active=1 " +
                        "AND RIGHT(REGEXP_REPLACE(mobile_no, '[^0-9]', ''), 10) = ?", TenantContext.get(), last10);
        return rows.size() == 1 ? rows.get(0) : null; // ambiguous mobile: refuse rather than guess
    }

    private static boolean sameMobile(String a, String b) {
        String x = last10(a), y = last10(b);
        return x != null && x.equals(y);
    }

    private static String last10(String mobile) {
        if (mobile == null) return null;
        String d = mobile.replaceAll("\\D", "");
        return d.length() < 10 ? null : d.substring(d.length() - 10);
    }

    private boolean limited(HttpServletRequest req) {
        long now = System.currentTimeMillis();
        Deque<Long> q = hits.computeIfAbsent(req.getRemoteAddr(), k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) q.pollFirst();
            if (q.size() >= MAX_PER_WINDOW) return true;
            q.addLast(now);
        }
        if (hits.size() > 10_000) hits.entrySet().removeIf(e -> e.getValue().isEmpty());
        return false;
    }

    private static String text(Map<String, Object> body, String key, int max) {
        Object v = body == null ? null : body.get(key);
        String s = v == null ? null : v.toString().trim();
        if (s == null || s.isEmpty()) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", "error", "message", message));
    }
}
