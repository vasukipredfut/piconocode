package com.aria.crm;

import com.aria.security.CurrentRole;
import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * CRM visibility, replacing ERPNext's role-profile checks (get_leads: "Sales Manager" sees leads
 * where sales_person = self, "Super Admin" sees all, others nothing):
 * <ul>
 *   <li>CRM admin = Manager / TenantAdmin (/ SuperAdmin): every lead, quote, customer</li>
 *   <li>sales user = role Sales or m7_employee.is_sales_person = 1: customers, items, and the leads
 *       / quotes they are on (sales person, creator or lead owner)</li>
 *   <li>any employee may create a lead (ERPNext lead_created_by) and sees the leads they created</li>
 * </ul>
 */
@Component
public class CrmAccess {

    public static final String SALES_ROLE = "Sales";

    private final JdbcTemplate jdbc;

    public CrmAccess(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isAdmin() {
        return CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN);
    }

    public boolean isSalesUser(long employeeId) {
        if (CurrentRole.isAny(jdbc, SALES_ROLE, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN)) return true;
        List<Integer> rows = jdbc.queryForList("SELECT is_sales_person FROM m7_employee WHERE tenant_id=? AND id=?",
                Integer.class, TenantContext.get(), employeeId);
        return !rows.isEmpty() && rows.get(0) != null && rows.get(0) == 1;
    }

    /** True when the employee is the lead's sales person, creator or owner. */
    public boolean isOnLead(Map<String, Object> lead, long employeeId) {
        return same(lead.get("sales_person_emp_id"), employeeId) || same(lead.get("created_by_emp_id"), employeeId)
                || same(lead.get("owner_id"), employeeId);
    }

    public boolean canSeeLead(Map<String, Object> lead, long employeeId) {
        return isAdmin() || isOnLead(lead, employeeId);
    }

    /** Quote visible to its creator, to whoever is on its lead / customer, and to CRM admins. */
    public boolean canSeeQuote(Map<String, Object> quote, long employeeId) {
        if (isAdmin() || same(quote.get("created_by_emp_id"), employeeId)) return true;
        Map<String, Object> lead = lead(quote.get("lead_id"));
        if (lead != null && isOnLead(lead, employeeId)) return true;
        if (quote.get("customer_id") != null) {
            List<Long> sp = jdbc.queryForList("SELECT sales_person_emp_id FROM m8_customer WHERE tenant_id=? AND id=?",
                    Long.class, TenantContext.get(), quote.get("customer_id"));
            return !sp.isEmpty() && same(sp.get(0), employeeId);
        }
        return false;
    }

    public Map<String, Object> lead(Object id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM m8_lead WHERE tenant_id=? AND id=?", TenantContext.get(), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> quote(Object id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM m8_quote WHERE tenant_id=? AND id=?", TenantContext.get(), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    static boolean same(Object a, long b) {
        return a instanceof Number n && n.longValue() == b;
    }
}
