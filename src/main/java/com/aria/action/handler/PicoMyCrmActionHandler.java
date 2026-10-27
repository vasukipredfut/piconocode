package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.crm.CrmAccess;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.my-crm -- CRM reads for the mobile app. Leads and quotes are scoped by CrmAccess (CRM admins
 * see all; others the leads they are on); customers / items / sales persons need a sales user.
 * <ul>
 *   <li>{@code leads}         -- optional {@code stage}, {@code search} (get_leads, get_my_leads)</li>
 *   <li>{@code lead}          -- {@code lead_id} with its customer and quotes</li>
 *   <li>{@code tracking}      -- leads with conversion + quotation flow, optional {@code stage},
 *                                {@code from_date} / {@code to_date} (get_all_lead_tracking_history)</li>
 *   <li>{@code quotes}        -- optional {@code search} (get_quotations)</li>
 *   <li>{@code quote}         -- {@code quote_id} with lines</li>
 *   <li>{@code customers}     -- optional {@code search} (get_customers)</li>
 *   <li>{@code sales-persons} -- active sales persons (get_sales_persons)</li>
 *   <li>{@code items}         -- active items with price / GST / machine id (get_items)</li>
 *   <li>{@code cities}        -- lead cities with their default sales person</li>
 * </ul>
 */
@Component
public class PicoMyCrmActionHandler implements ActionHandler {

    public static final String CODE = "pico.my-crm";

    private static final String LEAD_COLUMNS =
            "l.id, l.lead_number, l.lead_name, l.company_name, l.contact_phone, l.contact_email, l.territory, l.city_town, " +
            "l.stage, l.qualification_status, l.source, l.estimated_value, l.next_action_on, l.notes, l.lost_reason, l.customer_id, " +
            "l.sales_person_emp_id, sp.full_name AS sales_person, l.created_by_emp_id, cb.full_name AS created_by, l.created_at, l.closed_at, " +
            "l.workflow_instance_id";
    private static final String LEAD_FROM =
            " FROM m8_lead l LEFT JOIN m7_employee sp ON sp.tenant_id = l.tenant_id AND sp.id = l.sales_person_emp_id " +
            "LEFT JOIN m7_employee cb ON cb.tenant_id = l.tenant_id AND cb.id = l.created_by_emp_id ";
    private static final String QUOTE_COLUMNS =
            "q.id, q.quote_number, q.quote_to, q.lead_id, q.customer_id, q.customer_name, q.quote_date, q.valid_until, q.subtotal, " +
            "q.tax_total, q.total_value, q.status, q.sent_at, q.decided_at, q.emailed_to, q.created_by_emp_id, " +
            "cb.full_name AS created_by, q.workflow_instance_id";
    private static final String QUOTE_FROM = " FROM m8_quote q LEFT JOIN m7_employee cb ON cb.tenant_id = q.tenant_id AND cb.id = q.created_by_emp_id ";
    /** The leads a non-admin may see (CrmAccess.isOnLead), as SQL on alias l; 3 x employee id. */
    private static final String MY_LEADS = " AND (l.sales_person_emp_id=? OR l.created_by_emp_id=? OR l.owner_id=?)";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private CrmAccess access;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        long me = ((Number) emp.get("id")).longValue();
        Long tenantId = TenantContext.get();
        boolean admin = access.isAdmin();
        String view = PicoSelf.str(args.get("view"));
        String search = PicoLeadCreateActionHandler.text(args, "search", 64, false);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("employee_id", me);
        data.put("is_crm_admin", admin);
        switch (view == null ? "leads" : view) {
            case "leads", "tracking" -> {
                List<Object> p = new ArrayList<>(List.of(tenantId));
                StringBuilder where = new StringBuilder("WHERE l.tenant_id=?");
                if (!admin) { where.append(MY_LEADS); p.add(me); p.add(me); p.add(me); }
                String stage = PicoLeadCreateActionHandler.text(args, "stage", 16, false);
                if (stage != null) { where.append(" AND l.stage=?"); p.add(stage.toUpperCase()); }
                if (search != null) {
                    where.append(" AND (l.lead_name LIKE ? OR l.company_name LIKE ? OR l.contact_phone LIKE ? OR l.lead_number LIKE ?)");
                    String like = "%" + search + "%";
                    p.add(like); p.add(like); p.add(like); p.add(like);
                }
                LocalDate from = date(args, "from_date"), to = date(args, "to_date");
                if (from != null) { where.append(" AND l.created_at >= ?"); p.add(from); }
                if (to != null) { where.append(" AND l.created_at < ?"); p.add(to.plusDays(1)); }
                List<Map<String, Object>> leads = jdbc.queryForList("SELECT " + LEAD_COLUMNS + LEAD_FROM + where +
                        " ORDER BY l.created_at DESC LIMIT 500", p.toArray());
                if ("tracking".equals(view)) {
                    List<Map<String, Object>> out = new ArrayList<>();
                    for (Map<String, Object> l : leads) out.add(tracking(tenantId, l));
                    data.put("count", out.size());
                    data.put("tracking", out);
                } else {
                    data.put("count", leads.size());
                    data.put("leads", leads);
                }
            }
            case "lead" -> {
                Map<String, Object> raw = access.lead(args.get("lead_id") == null ? null : PicoLeadCreateActionHandler.id(args.get("lead_id"), "lead_id"));
                if (raw == null || !access.canSeeLead(raw, me)) return ActionResult.error("Lead not found");
                Map<String, Object> lead = jdbc.queryForMap("SELECT " + LEAD_COLUMNS + LEAD_FROM + "WHERE l.tenant_id=? AND l.id=?", tenantId, raw.get("id"));
                data.put("lead", tracking(tenantId, lead));
            }
            case "quotes" -> {
                List<Object> p = new ArrayList<>(List.of(tenantId));
                StringBuilder where = new StringBuilder("WHERE q.tenant_id=?");
                if (!admin) {
                    where.append(" AND (q.created_by_emp_id=? OR q.lead_id IN (SELECT l.id FROM m8_lead l WHERE l.tenant_id=q.tenant_id")
                         .append(MY_LEADS).append(") OR q.customer_id IN (SELECT c.id FROM m8_customer c WHERE c.tenant_id=q.tenant_id AND c.sales_person_emp_id=?))");
                    p.add(me); p.add(me); p.add(me); p.add(me); p.add(me);
                }
                if (search != null) { where.append(" AND (q.customer_name LIKE ? OR q.quote_number LIKE ?)"); p.add("%" + search + "%"); p.add("%" + search + "%"); }
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + QUOTE_COLUMNS + QUOTE_FROM + where +
                        " ORDER BY q.created_at DESC LIMIT 500", p.toArray());
                data.put("count", rows.size());
                data.put("quotes", rows);
            }
            case "quote" -> {
                Map<String, Object> raw = access.quote(args.get("quote_id") == null ? null : PicoLeadCreateActionHandler.id(args.get("quote_id"), "quote_id"));
                if (raw == null || !access.canSeeQuote(raw, me)) return ActionResult.error("Quotation not found");
                Map<String, Object> quote = new LinkedHashMap<>(jdbc.queryForMap("SELECT " + QUOTE_COLUMNS + QUOTE_FROM +
                        "WHERE q.tenant_id=? AND q.id=?", tenantId, raw.get("id")));
                quote.put("lines", jdbc.queryForList("SELECT sku_code, item_name, machine_id, qty, uom, rate, gst_rate_pct, amount, tax_amount " +
                        "FROM m8_quote_line WHERE tenant_id=? AND quote_id=? ORDER BY id", tenantId, raw.get("id")));
                data.put("quote", quote);
            }
            case "customers" -> {
                if (!access.isSalesUser(me)) return ActionResult.error("Customers are visible to sales users only");
                List<Object> p = new ArrayList<>(List.of(tenantId));
                String where = "WHERE tenant_id=? AND is_active=1";
                if (search != null) { where += " AND (customer_name LIKE ? OR customer_code LIKE ? OR mobile_no LIKE ?)"; p.add("%" + search + "%"); p.add("%" + search + "%"); p.add("%" + search + "%"); }
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT id, customer_code, customer_name, customer_type, contact_name, mobile_no, " +
                        "email_id, gstin, territory, city_town, sales_person_emp_id, lead_id, source, created_at FROM m8_customer " + where +
                        " ORDER BY customer_name LIMIT 500", p.toArray());
                data.put("count", rows.size());
                data.put("customers", rows);
            }
            case "sales-persons" -> {
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT id, emp_code, full_name, designation, department FROM m7_employee " +
                        "WHERE tenant_id=? AND status='ACTIVE' AND is_sales_person=1 ORDER BY full_name", tenantId);
                data.put("count", rows.size());
                data.put("sales_persons", rows);
            }
            case "items" -> {
                if (!access.isSalesUser(me)) return ActionResult.error("Items are visible to sales users only");
                List<Object> p = new ArrayList<>(List.of(tenantId));
                String where = "WHERE tenant_id=? AND status='ACTIVE'";
                if (search != null) { where += " AND (sku_code LIKE ? OR brand LIKE ? OR product_family LIKE ? OR machine_id LIKE ?)"; for (int i = 0; i < 4; i++) p.add("%" + search + "%"); }
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT sku_code, CONCAT_WS(' ', brand, product_family, offer_variant) AS item_name, " +
                        "machine_id, uom_primary AS uom, sale_price AS item_price, gst_rate_pct, hsn_code FROM m5_sku_master " + where +
                        " ORDER BY item_name LIMIT 1000", p.toArray());
                data.put("count", rows.size());
                data.put("items", rows);
            }
            case "cities" -> {
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT c.city_name, c.sales_person_emp_id, e.full_name AS sales_person " +
                        "FROM m8_lead_city c LEFT JOIN m7_employee e ON e.tenant_id = c.tenant_id AND e.id = c.sales_person_emp_id " +
                        "WHERE c.tenant_id=? AND c.is_active=1 ORDER BY c.city_name", tenantId);
                data.put("count", rows.size());
                data.put("cities", rows);
            }
            default -> throw new IllegalArgumentException("view must be leads, lead, tracking, quotes, quote, customers, sales-persons, items or cities");
        }
        return ActionResult.builder().ok(true).data(data).build();
    }

    /** ERPNext get_all_lead_tracking_history shape: lead + customer + quotations (direct and via the customer). */
    private Map<String, Object> tracking(Long tenantId, Map<String, Object> lead) {
        Map<String, Object> out = new LinkedHashMap<>(lead);
        Map<String, Object> customer = null;
        if (lead.get("customer_id") != null) {
            customer = jdbc.queryForList("SELECT id, customer_code, customer_name, created_at FROM m8_customer WHERE tenant_id=? AND id=?",
                    tenantId, lead.get("customer_id")).stream().findFirst().orElse(null);
        }
        out.put("converted_to_customer", customer != null);
        out.put("customer", customer);
        List<Map<String, Object>> quotes = new ArrayList<>();
        for (Map<String, Object> q : jdbc.queryForList("SELECT " + QUOTE_COLUMNS + QUOTE_FROM +
                "WHERE q.tenant_id=? AND (q.lead_id=? OR (q.customer_id IS NOT NULL AND q.customer_id=?)) ORDER BY q.created_at",
                tenantId, lead.get("id"), lead.get("customer_id"))) {
            Map<String, Object> item = new LinkedHashMap<>(q);
            item.put("flow", "CUSTOMER".equals(q.get("quote_to")) ? "Lead → Customer → Quotation" : "Lead → Quotation");
            quotes.add(item);
        }
        out.put("quotation_created", !quotes.isEmpty());
        out.put("quotation_flow", quotes);
        return out;
    }

    private static LocalDate date(Map<String, Object> args, String key) {
        Object v = args.get(key);
        if (v == null || v.toString().isBlank()) return null;
        try {
            return LocalDate.parse(v.toString().trim().substring(0, 10));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(key + " must be YYYY-MM-DD");
        }
    }
}
