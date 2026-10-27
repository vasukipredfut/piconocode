package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.hr.leave.PicoLeaveRules;
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
 * pico.my-leave -- self-service leave reads for the mobile app, always scoped to the caller.
 *
 * <p>args.view:
 * <ul>
 *   <li>{@code history}       -- own leave, newest first, optional {@code status} filter (get_employee_leave_history)</li>
 *   <li>{@code pending}       -- own PENDING leave (get_pending_leaves_by_employee)</li>
 *   <li>{@code balance}       -- allocated / used / pending / balance for {@code year} (get_leave_balance)</li>
 *   <li>{@code types}         -- active leave types (get_leave_types)</li>
 *   <li>{@code approvals}     -- leave and cancellation requests waiting for the caller as approver,
 *                                with the workflow_instance_id + pageCode to decide them
 *                                (get_pending_leaves_for_approver)</li>
 *   <li>{@code whos-on-leave} -- everyone on approved leave today (whos_on_leave)</li>
 * </ul>
 */
@Component
public class PicoMyLeaveActionHandler implements ActionHandler {

    public static final String CODE = "pico.my-leave";

    private static final String LEAVE_COLUMNS =
            "lr.id, lr.lr_number, lr.leave_kind, lr.start_date, lr.end_date, lr.days, lr.is_half_day, lr.which_half, " +
            "lr.reason, lr.status, lr.approver_remarks, lr.decided_at, lr.cancel_status, lr.cancel_reason, " +
            "lr.revised_half_day, lr.revised_which_half, lr.revised_from_id, lr.created_at";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PicoLeaveRules rules;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        Long employeeId = ((Number) emp.get("id")).longValue();
        Long tenantId = TenantContext.get();
        String view = PicoSelf.str(args.get("view"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("employee_id", employeeId);
        data.put("emp_code", emp.get("emp_code"));
        data.put("full_name", emp.get("full_name"));
        switch (view == null ? "history" : view) {
            case "history" -> {
                String status = PicoSelf.str(args.get("status"));
                List<Map<String, Object>> rows = status == null || status.isEmpty()
                        ? jdbc.queryForList("SELECT " + LEAVE_COLUMNS + " FROM m7_leave_request lr " +
                                "WHERE lr.tenant_id=? AND lr.employee_id=? ORDER BY lr.created_at DESC", tenantId, employeeId)
                        : jdbc.queryForList("SELECT " + LEAVE_COLUMNS + " FROM m7_leave_request lr " +
                                "WHERE lr.tenant_id=? AND lr.employee_id=? AND lr.status=? ORDER BY lr.created_at DESC",
                                tenantId, employeeId, status.toUpperCase());
                data.put("count", rows.size());
                data.put("leave_history", rows);
            }
            case "pending" -> {
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + LEAVE_COLUMNS + " FROM m7_leave_request lr " +
                        "WHERE lr.tenant_id=? AND lr.employee_id=? AND lr.status='PENDING' ORDER BY lr.created_at DESC", tenantId, employeeId);
                data.put("count", rows.size());
                data.put("pending_leaves", rows);
            }
            case "balance" -> {
                int year = year(args);
                data.put("year", year);
                data.put("leave_summary", rules.balance(employeeId, year));
            }
            case "types" -> {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT type_code, type_name, max_days_per_year, is_lwp, is_compensatory FROM m7_leave_type " +
                                "WHERE tenant_id=? AND is_active=1 ORDER BY type_name", tenantId);
                data.put("count", rows.size());
                data.put("leave_types", rows);
            }
            case "approvals" -> {
                List<Map<String, Object>> out = new ArrayList<>();
                jdbc.queryForList("SELECT " + LEAVE_COLUMNS + ", e.emp_code, e.full_name AS employee_name, lr.workflow_instance_id " +
                                "FROM m7_leave_request lr JOIN m7_employee e ON e.tenant_id = lr.tenant_id AND e.id = lr.employee_id " +
                                "WHERE lr.tenant_id=? AND lr.approver_emp_id=? AND lr.status='PENDING' ORDER BY lr.created_at",
                                tenantId, employeeId)
                        .forEach(r -> out.add(decision(r, "LEAVE", r.get("workflow_instance_id"), "aria:lr-approve",
                                "status", List.of("APPROVED", "REJECTED"))));
                jdbc.queryForList("SELECT " + LEAVE_COLUMNS + ", e.emp_code, e.full_name AS employee_name, lr.cancel_instance_id " +
                                "FROM m7_leave_request lr JOIN m7_employee e ON e.tenant_id = lr.tenant_id AND e.id = lr.employee_id " +
                                "WHERE lr.tenant_id=? AND lr.approver_emp_id=? AND lr.cancel_status='REQUESTED' ORDER BY lr.cancel_requested_at",
                                tenantId, employeeId)
                        .forEach(r -> out.add(decision(r, "CANCELLATION", r.get("cancel_instance_id"), "pico:leave-cancel-decide",
                                "decision", List.of("CANCELLED", "DECLINED"))));
                data.put("count", out.size());
                data.put("pending_approvals", out);
            }
            case "whos-on-leave" -> {
                LocalDate today = LocalDate.now();
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT lr.id AS leave_id, e.emp_code, e.full_name, e.department, e.designation, e.photo_url, " +
                                "lr.leave_kind, lr.start_date, lr.end_date, lr.is_half_day, lr.which_half " +
                                "FROM m7_leave_request lr JOIN m7_employee e ON e.tenant_id = lr.tenant_id AND e.id = lr.employee_id " +
                                "WHERE lr.tenant_id=? AND lr.status='APPROVED' AND ? BETWEEN lr.start_date AND lr.end_date " +
                                "ORDER BY e.full_name", tenantId, today);
                data.put("date", today.toString());
                data.put("count", rows.size());
                data.put("employees_on_leave", rows);
            }
            default -> throw new IllegalArgumentException("view must be history, pending, balance, types, approvals or whos-on-leave");
        }
        return ActionResult.builder().ok(true).data(data).build();
    }

    /** One approval item plus exactly what POST /api/workflow/transition needs to decide it. */
    private static Map<String, Object> decision(Map<String, Object> row, String kind, Object instanceId, String pageCode,
                                                String field, List<String> values) {
        Map<String, Object> item = new LinkedHashMap<>(row);
        item.put("request_kind", kind);
        Map<String, Object> decide = new LinkedHashMap<>();
        decide.put("instanceId", instanceId);
        decide.put("pageCode", pageCode);
        decide.put("field", field);
        decide.put("values", values);
        item.put("decide", decide);
        return item;
    }

    private static int year(Map<String, Object> args) {
        Object v = args.get("year");
        if (v == null || v.toString().isBlank()) return LocalDate.now().getYear();
        try {
            return Integer.parseInt(v.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("year must be a number");
        }
    }
}
