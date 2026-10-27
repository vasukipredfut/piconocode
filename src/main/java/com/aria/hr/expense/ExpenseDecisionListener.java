package com.aria.hr.expense;

import com.aria.security.TenantContext;
import com.aria.tenant.TenantSettings;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Side-effects of M7.ExpenseClaim transitions (status columns come from the projection, which
 * runs first):
 * <ul>
 *   <li>APPROVED -> sanctioned_amount = amount on every line, total_sanctioned (as ERPNext's
 *       approve did); mail the employee; GL Dr type account / Cr reimbursements payable</li>
 *   <li>REJECTED -> mail the employee</li>
 *   <li>PAID     -> mail the employee; GL Dr reimbursements payable / Cr bank</li>
 * </ul>
 * GL posting only when the tenant sets expense.gl_posting=true with expense.payable_account and
 * expense.bank_account; lines whose type has no default_account are not posted.
 */
@Component
public class ExpenseDecisionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ExpenseDecisionListener.class);

    private final JdbcTemplate jdbc;
    private final ExpenseMails mails;
    private final TenantSettings settings;

    public ExpenseDecisionListener(JdbcTemplate jdbc, ExpenseMails mails, TenantSettings settings) {
        this.jdbc = jdbc;
        this.mails = mails;
        this.settings = settings;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!ExpenseApproverValidator.WF_EXPENSE.equals(instance.getWorkflowCode())) return;
        String state = instance.getCurrentState();
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM m7_expense_claim WHERE tenant_id=? AND workflow_instance_id=?", tenantId, instance.getId());
        if (rows.isEmpty()) return;
        Map<String, Object> claim = rows.get(0);
        Object claimId = claim.get("id");
        switch (state) {
            case "APPROVED" -> {
                jdbc.update("UPDATE m7_expense_claim_line SET sanctioned_amount = amount WHERE tenant_id=? AND claim_id=?", tenantId, claimId);
                jdbc.update("UPDATE m7_expense_claim c SET total_sanctioned = (SELECT COALESCE(SUM(sanctioned_amount),0) " +
                        "FROM m7_expense_claim_line l WHERE l.tenant_id = c.tenant_id AND l.claim_id = c.id) WHERE c.tenant_id=? AND c.id=?",
                        tenantId, claimId);
                Map<String, Object> fresh = jdbc.queryForMap("SELECT * FROM m7_expense_claim WHERE tenant_id=? AND id=?", tenantId, claimId);
                postApproval(fresh);
                mails.decided(fresh, "Approved");
            }
            case "REJECTED" -> mails.decided(claim, "Rejected");
            case "PAID" -> {
                postPayment(claim);
                mails.decided(claim, "Reimbursed");
            }
            default -> { }
        }
    }

    private boolean glOn() {
        return "true".equalsIgnoreCase(settings.get("expense.gl_posting", "false"));
    }

    private void postApproval(Map<String, Object> claim) {
        if (!glOn()) return;
        String payable = settings.get("expense.payable_account", null);
        if (payable == null) {
            log.warn("expense.gl_posting=true but expense.payable_account unset; claim {} not posted", claim.get("claim_number"));
            return;
        }
        Long tenantId = TenantContext.get();
        String doc = (String) claim.get("claim_number");
        String jeNo = "JE-EXP-" + claim.get("id");
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> r : jdbc.queryForList(
                "SELECT t.default_account, SUM(l.sanctioned_amount) AS amt FROM m7_expense_claim_line l " +
                        "JOIN m7_expense_claim_type t ON t.tenant_id = l.tenant_id AND t.type_code = l.type_code " +
                        "WHERE l.tenant_id=? AND l.claim_id=? AND t.default_account IS NOT NULL GROUP BY t.default_account",
                tenantId, claim.get("id"))) {
            BigDecimal amt = (BigDecimal) r.get("amt");
            entry(jeNo, doc, (String) r.get("default_account"), "Expense claim " + doc, amt, BigDecimal.ZERO, claim);
            total = total.add(amt);
        }
        if (total.signum() > 0) entry(jeNo, doc, payable, "Reimbursement payable " + doc, BigDecimal.ZERO, total, claim);
    }

    private void postPayment(Map<String, Object> claim) {
        if (!glOn()) return;
        String payable = settings.get("expense.payable_account", null);
        String bank = settings.get("expense.bank_account", null);
        BigDecimal amt = (BigDecimal) claim.get("total_sanctioned");
        if (payable == null || bank == null || amt == null || amt.signum() == 0) return;
        String doc = (String) claim.get("claim_number");
        String jeNo = "JE-EXPPAY-" + claim.get("id");
        entry(jeNo, doc, payable, "Reimbursed " + doc + " " + claim.get("pay_reference"), amt, BigDecimal.ZERO, claim);
        entry(jeNo, doc, bank, "Reimbursed " + doc + " " + claim.get("pay_reference"), BigDecimal.ZERO, amt, claim);
    }

    /** One m6_gl_entry line, scoped to the tenant (GlAutoPostingService.insertEntry hard-codes tenant 1). */
    private void entry(String jeNo, String doc, String account, String narration, BigDecimal debit, BigDecimal credit,
                       Map<String, Object> claim) {
        LocalDate d = LocalDate.now();
        int startYr = d.getMonthValue() >= 4 ? d.getYear() : d.getYear() - 1;
        String party = jdbc.queryForList("SELECT emp_code FROM m7_employee WHERE tenant_id=? AND id=?", String.class,
                TenantContext.get(), claim.get("employee_id")).stream().findFirst().orElse(null);
        jdbc.update("INSERT INTO m6_gl_entry (tenant_id, je_no, je_date, fy, period, source_module, source_doc, account_code, " +
                        "narration, debit, credit, party_code, party_type, posted) VALUES (?,?,?,?,?,'EXPENSE',?,?,?,?,?,?,'EMPLOYEE',1)",
                TenantContext.get(), jeNo, d, startYr + "-" + String.valueOf(startYr + 1).substring(2), d.toString().substring(0, 7),
                doc, account, narration, debit, credit, party);
    }
}
