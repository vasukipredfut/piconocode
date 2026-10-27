package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.hr.expense.ExpenseMails;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * pico.expense-submit -- replaces ERPNext create_expense_claim (+ save_base64_receipt,
 * send_expense_approval_email).
 *
 * <p>args: {@code lines} = [{expense_date, expense_type, amount, description, receipt_url}, ...]
 * (max 50), or ERPNext's single-line shape ({@code expense_date, expense_type, amount,
 * description, receipt_url}) at the top level; optional {@code remarks}.
 * Receipts are uploaded first through /api/m0/document-vault/upload (multipart) and passed as
 * {@code receipt_url}; ERPNext's base64 {@code receipt} is not accepted.
 *
 * <p>Checks per line: type active, amount &gt; 0 and within the type's max_amount, receipt present
 * when the type requires it, date not in the future. Then inserts claim + lines (SUBMITTED),
 * starts M7.ExpenseClaim and mails the approver. Not @Transactional (engine has its own
 * transaction manager); if the instance cannot start, the claim is deleted again.
 */
@Component
public class PicoExpenseSubmitActionHandler implements ActionHandler {

    public static final String CODE = "pico.expense-submit";
    private static final int MAX_LINES = 50;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ExpenseMails mails;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        List<Map<String, Object>> rawLines;
        Object l = args.get("lines");
        if (l instanceof List<?> list && !list.isEmpty()) {
            rawLines = new ArrayList<>();
            for (Object o : list) {
                if (!(o instanceof Map)) throw new IllegalArgumentException("lines must be objects");
                rawLines.add((Map<String, Object>) o);
            }
        } else {
            rawLines = List.of(args);
        }
        if (rawLines.size() > MAX_LINES) throw new IllegalArgumentException("At most " + MAX_LINES + " lines per claim");
        String remarks = PicoSelf.str(args.get("remarks"));
        if (remarks != null && remarks.length() > 512) throw new IllegalArgumentException("remarks must be at most 512 characters");

        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        long employeeId = ((Number) emp.get("id")).longValue();
        Long tenantId = TenantContext.get();

        List<Object[]> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < rawLines.size(); i++) {
            Map<String, Object> r = rawLines.get(i);
            String where = rawLines.size() > 1 ? "line " + (i + 1) + ": " : "";
            String type = PicoSelf.str(r.get("expense_type"));
            if (type == null || type.isEmpty()) throw new IllegalArgumentException(where + "expense_type is required");
            BigDecimal amount = amount(r.get("amount"), where);
            LocalDate date = date(r.get("expense_date"), today, where);
            if (date.isAfter(today)) throw new IllegalArgumentException(where + "expense_date cannot be in the future");
            String description = PicoSelf.str(r.get("description"));
            if (description != null && description.length() > 512) throw new IllegalArgumentException(where + "description must be at most 512 characters");
            String receipt = PicoSelf.str(r.get("receipt_url"));
            if (receipt != null && receipt.length() > 512) throw new IllegalArgumentException(where + "receipt_url must be at most 512 characters");

            List<Map<String, Object>> types = jdbc.queryForList(
                    "SELECT max_amount, requires_receipt FROM m7_expense_claim_type WHERE tenant_id=? AND type_code=? AND is_active=1",
                    tenantId, type);
            if (types.isEmpty()) return ActionResult.error(where + "Unknown or inactive expense type " + type);
            BigDecimal max = (BigDecimal) types.get(0).get("max_amount");
            if (max != null && amount.compareTo(max) > 0) return ActionResult.error(where + type + " is limited to " + max + " per line");
            Object req = types.get(0).get("requires_receipt");
            boolean needsReceipt = req instanceof Boolean b ? b : req instanceof Number n && n.intValue() != 0;
            if (needsReceipt && (receipt == null || receipt.isEmpty())) return ActionResult.error(where + "A receipt is required for " + type);

            lines.add(new Object[]{date, type, description, amount, receipt});
            total = total.add(amount);
        }

        Long approverId = jdbc.queryForList(
                "SELECT a.id FROM m7_employee e JOIN m7_employee a ON a.tenant_id = e.tenant_id " +
                        "AND a.id = COALESCE(e.expense_approver_id, e.reports_to_emp_id) WHERE e.tenant_id=? AND e.id=?",
                Long.class, tenantId, employeeId).stream().findFirst().orElse(null);
        if (approverId == null) return ActionResult.error("Expense Approver not configured");
        if (approverId == employeeId) return ActionResult.error("Expense approver is the employee themself; ask HR to fix the employee master");

        Long claimId = insertClaim(tenantId, employeeId, approverId, today, total, remarks);
        String number = String.format("EXP-%d-%05d", today.getYear(), claimId);
        for (Object[] line : lines) {
            jdbc.update("INSERT INTO m7_expense_claim_line (tenant_id, claim_id, expense_date, type_code, description, amount, receipt_url) " +
                    "VALUES (?,?,?,?,?,?,?)", tenantId, claimId, line[0], line[1], line[2], line[3], line[4]);
        }
        WorkflowInstance inst;
        try {
            inst = workflowEngine.startInstance("M7.ExpenseClaim", Map.of("businessRef", "ExpenseClaim#" + claimId));
        } catch (RuntimeException e) {
            jdbc.update("DELETE FROM m7_expense_claim WHERE tenant_id=? AND id=?", tenantId, claimId);
            throw e;
        }
        jdbc.update("UPDATE m7_expense_claim SET claim_number=?, workflow_instance_id=?, current_step=? WHERE tenant_id=? AND id=?",
                number, inst.getId(), inst.getCurrentState(), tenantId, claimId);

        Map<String, Object> claim = jdbc.queryForMap("SELECT * FROM m7_expense_claim WHERE tenant_id=? AND id=?", tenantId, claimId);
        mails.approvalRequested(claim, jdbc.queryForList(
                "SELECT * FROM m7_expense_claim_line WHERE tenant_id=? AND claim_id=? ORDER BY id", tenantId, claimId));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("claim_id", claimId);
        data.put("claim_number", number);
        data.put("status", inst.getCurrentState());
        data.put("total_claimed", total);
        data.put("lines", lines.size());
        data.put("workflow_instance_id", inst.getId());
        return ActionResult.builder().ok(true).message("Expense Claim created successfully")
                .recordId(claimId).data(data).build();
    }

    private Long insertClaim(Long tenantId, long employeeId, Long approverId, LocalDate date, BigDecimal total, String remarks) {
        Object[] values = {tenantId, "TMP-" + UUID.randomUUID().toString().substring(0, 24), employeeId, approverId, date, total, remarks};
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO m7_expense_claim (tenant_id, claim_number, employee_id, approver_emp_id, posting_date, total_claimed, remarks, " +
                            "status, current_step) VALUES (?,?,?,?,?,?,?,'SUBMITTED','SUBMITTED')",
                    Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }

    private static BigDecimal amount(Object raw, String where) {
        BigDecimal v;
        try {
            v = raw == null ? null : new BigDecimal(raw.toString().trim());
        } catch (NumberFormatException e) {
            v = null;
        }
        if (v == null || v.signum() <= 0) throw new IllegalArgumentException(where + "amount should be greater than 0");
        if (v.scale() > 2) v = v.setScale(2, RoundingMode.HALF_UP);
        if (v.compareTo(new BigDecimal("9999999999.99")) > 0) throw new IllegalArgumentException(where + "amount is too large");
        return v;
    }

    private static LocalDate date(Object raw, LocalDate fallback, String where) {
        String s = raw == null ? null : raw.toString().trim();
        if (s == null || s.isEmpty()) return fallback;
        try {
            return LocalDate.parse(s.length() > 10 ? s.substring(0, 10) : s);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(where + "expense_date must be YYYY-MM-DD");
        }
    }
}
