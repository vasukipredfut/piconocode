package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.CurrentRole;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.my-expense -- expense reads for the mobile app.
 * <ul>
 *   <li>{@code claims}    -- own claims, optional {@code status} (get_employee_expenses)</li>
 *   <li>{@code claim}     -- one {@code claim_id} with lines; visible to its employee, its approver
 *                            and Manager / TenantAdmin (get_employee_expenses with expense_claim)</li>
 *   <li>{@code types}     -- active expense claim types (get_company_expense_claim_types)</li>
 *   <li>{@code approvals} -- SUBMITTED claims waiting for the caller, each with the transition call
 *                            (get_pending_expenses_for_approver)</li>
 *   <li>{@code company}   -- every claim of the tenant, optional {@code status}; Manager /
 *                            TenantAdmin only. ERPNext's get_company_expenses showed them to anyone.</li>
 * </ul>
 */
@Component
public class PicoMyExpenseActionHandler implements ActionHandler {

    public static final String CODE = "pico.my-expense";

    private static final String CLAIM_COLUMNS =
            "c.id, c.claim_number, c.posting_date, c.total_claimed, c.total_sanctioned, c.status, c.remarks, " +
            "c.approver_remarks, c.decided_at, c.paid_at, c.pay_reference, c.workflow_instance_id, " +
            "e.emp_code, e.full_name AS employee_name, a.full_name AS approver_name";
    private static final String CLAIM_FROM =
            " FROM m7_expense_claim c JOIN m7_employee e ON e.tenant_id = c.tenant_id AND e.id = c.employee_id " +
            "LEFT JOIN m7_employee a ON a.tenant_id = c.tenant_id AND a.id = c.approver_emp_id ";

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        long employeeId = ((Number) emp.get("id")).longValue();
        Long tenantId = TenantContext.get();
        String view = PicoSelf.str(args.get("view"));
        String status = PicoSelf.str(args.get("status"));
        boolean hr = CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("employee_id", employeeId);
        data.put("emp_code", emp.get("emp_code"));
        data.put("full_name", emp.get("full_name"));
        switch (view == null ? "claims" : view) {
            case "claims" -> {
                List<Map<String, Object>> rows = claims("WHERE c.tenant_id=? AND c.employee_id=?", status, tenantId, employeeId);
                data.put("count", rows.size());
                data.put("expense_claims", rows);
            }
            case "claim" -> {
                long claimId;
                try {
                    claimId = Long.parseLong(String.valueOf(args.get("claim_id")).trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("claim_id is required");
                }
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + CLAIM_COLUMNS + ", c.employee_id, c.approver_emp_id" +
                        CLAIM_FROM + "WHERE c.tenant_id=? AND c.id=?", tenantId, claimId);
                if (rows.isEmpty()) return ActionResult.error("Expense claim not found");
                Map<String, Object> claim = new LinkedHashMap<>(rows.get(0));
                boolean mine = ((Number) claim.get("employee_id")).longValue() == employeeId;
                boolean approver = claim.get("approver_emp_id") != null && ((Number) claim.get("approver_emp_id")).longValue() == employeeId;
                if (!mine && !approver && !hr) return ActionResult.error("Expense claim not found");
                claim.put("lines", jdbc.queryForList(
                        "SELECT l.id, l.expense_date, l.type_code, t.type_name, l.description, l.amount, l.sanctioned_amount, l.receipt_url " +
                                "FROM m7_expense_claim_line l LEFT JOIN m7_expense_claim_type t ON t.tenant_id = l.tenant_id AND t.type_code = l.type_code " +
                                "WHERE l.tenant_id=? AND l.claim_id=? ORDER BY l.id", tenantId, claimId));
                data.put("expense_claim", claim);
            }
            case "types" -> {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT type_code, type_name, max_amount, requires_receipt FROM m7_expense_claim_type " +
                                "WHERE tenant_id=? AND is_active=1 ORDER BY type_name", tenantId);
                data.put("count", rows.size());
                data.put("expense_types", rows);
            }
            case "approvals" -> {
                List<Map<String, Object>> out = new ArrayList<>();
                for (Map<String, Object> r : claims("WHERE c.tenant_id=? AND c.approver_emp_id=?", "SUBMITTED", tenantId, employeeId)) {
                    Map<String, Object> item = new LinkedHashMap<>(r);
                    Map<String, Object> decide = new LinkedHashMap<>();
                    decide.put("instanceId", r.get("workflow_instance_id"));
                    decide.put("pageCode", "pico:expense-decide");
                    decide.put("field", "decision");
                    decide.put("values", List.of("APPROVED", "REJECTED"));
                    item.put("decide", decide);
                    out.add(item);
                }
                data.put("count", out.size());
                data.put("pending_expenses", out);
            }
            case "company" -> {
                if (!hr) return ActionResult.error("Company expenses are visible to Manager / TenantAdmin only");
                List<Map<String, Object>> rows = claims("WHERE c.tenant_id=?", status, tenantId);
                data.put("count", rows.size());
                data.put("expense_claims", rows);
            }
            default -> throw new IllegalArgumentException("view must be claims, claim, types, approvals or company");
        }
        return ActionResult.builder().ok(true).data(data).build();
    }

    private List<Map<String, Object>> claims(String where, String status, Object... params) {
        List<Object> p = new ArrayList<>(List.of(params));
        String statusFilter = "";
        if (status != null && !status.isEmpty()) {
            statusFilter = " AND c.status=?";
            p.add(status.toUpperCase());
        }
        return jdbc.queryForList("SELECT " + CLAIM_COLUMNS + CLAIM_FROM + where + statusFilter +
                " ORDER BY c.created_at DESC LIMIT 500", p.toArray());
    }
}
