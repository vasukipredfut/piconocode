package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.crm.CrmAccess;
import com.aria.crm.CrmMails;
import com.aria.security.CurrentUser;
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
import java.util.Set;
import java.util.UUID;

/**
 * pico.quote-create -- replaces ERPNext create_quotation_from_lead, create_quotation and
 * send_quotation_email.
 *
 * <p>args: exactly one of {@code lead_id} / {@code customer_id}; {@code items} =
 * [{sku_code (or item_code), qty, rate?}, ...] (max 100); {@code valid_until}, {@code notes}.
 * Items come from m5_sku_master (active); rate defaults to its sale_price (ERPNext used the item's
 * valuation rate, i.e. cost); GST from its gst_rate_pct. The quote is created, its M8.Quote
 * instance started and sent (DRAFT -> SENT via aria:quote-send, ERPNext submitted it at once);
 * an open lead moves to QUOTED via pico:lead-quoted; the party is mailed when an email is on file.
 * Sales users only (role Sales / is_sales_person / Manager / TenantAdmin), and only for leads /
 * customers they may see.
 */
@Component
public class PicoQuoteCreateActionHandler implements ActionHandler {

    public static final String CODE = "pico.quote-create";
    private static final int MAX_ITEMS = 100;
    private static final Set<String> QUOTABLE_LEAD = Set.of("NEW", "CONTACTED", "QUALIFIED");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private CrmAccess access;
    @Autowired private CrmMails mails;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        boolean byLead = args.get("lead_id") != null;
        if (byLead == (args.get("customer_id") != null)) throw new IllegalArgumentException("Give exactly one of lead_id or customer_id");
        if (!(args.get("items") instanceof List<?> rawItems) || rawItems.isEmpty()) throw new IllegalArgumentException("items are required");
        if (rawItems.size() > MAX_ITEMS) throw new IllegalArgumentException("At most " + MAX_ITEMS + " items per quotation");
        LocalDate validUntil = null;
        if (args.get("valid_until") != null && !args.get("valid_until").toString().isBlank()) {
            try {
                validUntil = LocalDate.parse(args.get("valid_until").toString().trim().substring(0, 10));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("valid_until must be YYYY-MM-DD");
            }
            if (validUntil.isBefore(LocalDate.now())) throw new IllegalArgumentException("valid_until is in the past");
        }
        String notes = PicoLeadCreateActionHandler.text(args, "notes", 1024, false);

        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        long employeeId = ((Number) emp.get("id")).longValue();
        if (!access.isSalesUser(employeeId)) return ActionResult.error("Quotations can be created by sales users only");
        Long tenantId = TenantContext.get();

        Map<String, Object> lead = null;
        Long customerId = null;
        String partyName, partyEmail;
        if (byLead) {
            lead = access.lead(PicoLeadCreateActionHandler.id(args.get("lead_id"), "lead_id"));
            if (lead == null || !access.canSeeLead(lead, employeeId)) return ActionResult.error("Lead not found");
            if ("LOST".equals(lead.get("stage"))) return ActionResult.error("Lead is LOST");
            partyName = lead.get("company_name") != null ? (String) lead.get("company_name") : (String) lead.get("lead_name");
            partyEmail = (String) lead.get("contact_email");
        } else {
            customerId = PicoLeadCreateActionHandler.id(args.get("customer_id"), "customer_id");
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT customer_name, email_id FROM m8_customer WHERE tenant_id=? AND id=? AND is_active=1", tenantId, customerId);
            if (rows.isEmpty()) return ActionResult.error("Customer not found");
            partyName = (String) rows.get(0).get("customer_name");
            partyEmail = (String) rows.get(0).get("email_id");
        }

        List<Object[]> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO, tax = BigDecimal.ZERO;
        for (int i = 0; i < rawItems.size(); i++) {
            if (!(rawItems.get(i) instanceof Map<?, ?> m)) throw new IllegalArgumentException("items must be objects");
            Map<String, Object> it = (Map<String, Object>) m;
            String where = "item " + (i + 1) + ": ";
            String sku = PicoSelf.str(it.get("sku_code") != null ? it.get("sku_code") : it.get("item_code"));
            if (sku == null || sku.isEmpty()) throw new IllegalArgumentException(where + "sku_code is required");
            BigDecimal qty = positive(it.get("qty"), where + "qty");
            List<Map<String, Object>> skus = jdbc.queryForList(
                    "SELECT sku_code, CONCAT_WS(' ', brand, product_family, offer_variant) AS item_name, machine_id, uom_primary, sale_price, gst_rate_pct " +
                            "FROM m5_sku_master WHERE tenant_id=? AND sku_code=? AND status='ACTIVE'", tenantId, sku);
            if (skus.isEmpty()) return ActionResult.error(where + "Unknown or inactive item " + sku);
            Map<String, Object> s = skus.get(0);
            BigDecimal rate = it.get("rate") == null || it.get("rate").toString().isBlank()
                    ? (BigDecimal) s.get("sale_price") : nonNegative(it.get("rate"), where + "rate");
            if (rate == null) return ActionResult.error(where + "Item " + sku + " has no sale price; send a rate");
            BigDecimal gst = s.get("gst_rate_pct") == null ? BigDecimal.ZERO : (BigDecimal) s.get("gst_rate_pct");
            BigDecimal amount = qty.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTax = amount.multiply(gst).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            lines.add(new Object[]{sku, s.get("item_name"), s.get("machine_id"), qty, s.get("uom_primary"), rate, gst, amount, lineTax});
            subtotal = subtotal.add(amount);
            tax = tax.add(lineTax);
        }
        BigDecimal total = subtotal.add(tax);
        LocalDate today = LocalDate.now();

        Object[] values = {tenantId, "TMP-" + UUID.randomUUID().toString().substring(0, 24), lead == null ? null : lead.get("id"),
                partyName, total, validUntil, byLead ? "LEAD" : "CUSTOMER", customerId, employeeId, today, subtotal, tax, notes};
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO m8_quote (tenant_id, quote_number, lead_id, customer_name, total_value, valid_until, quote_to, customer_id, " +
                            "created_by_emp_id, quote_date, subtotal, tax_total, notes, status, current_step) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,'DRAFT','DRAFT')",
                    Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, keys);
        long quoteId = keys.getKey().longValue();
        String number = String.format("QTN-%d-%05d", today.getYear(), quoteId);
        for (Object[] l : lines) {
            jdbc.update("INSERT INTO m8_quote_line (tenant_id, quote_id, sku_code, item_name, machine_id, qty, uom, rate, gst_rate_pct, amount, tax_amount) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?)", tenantId, quoteId, l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8]);
        }
        WorkflowInstance inst;
        try {
            inst = workflowEngine.startInstance("M8.Quote", Map.of("businessRef", "Quote#" + quoteId));
        } catch (RuntimeException e) {
            jdbc.update("DELETE FROM m8_quote WHERE tenant_id=? AND id=?", tenantId, quoteId);
            throw e;
        }
        jdbc.update("UPDATE m8_quote SET quote_number=?, workflow_instance_id=? WHERE tenant_id=? AND id=?", number, inst.getId(), tenantId, quoteId);
        List<String> warnings = new ArrayList<>();
        try {
            workflowEngine.handlePageSubmission(inst.getId(), "aria:quote-send", Map.of("quote_number", number), CurrentUser.id());
        } catch (RuntimeException e) {
            warnings.add("Quotation saved as DRAFT, could not mark it sent: " + e.getMessage());
        }
        if (lead != null && QUOTABLE_LEAD.contains((String) lead.get("stage")) && lead.get("workflow_instance_id") != null) {
            try {
                workflowEngine.handlePageSubmission(((Number) lead.get("workflow_instance_id")).longValue(), "pico:lead-quoted",
                        Map.of("quote_number", number), CurrentUser.id());
            } catch (RuntimeException e) {
                warnings.add("Lead stage not changed: " + e.getMessage());
            }
        }

        Map<String, Object> quote = jdbc.queryForMap("SELECT * FROM m8_quote WHERE tenant_id=? AND id=?", tenantId, quoteId);
        if (partyEmail != null && !partyEmail.isBlank()) {
            mails.quotation(quote, jdbc.queryForList("SELECT * FROM m8_quote_line WHERE tenant_id=? AND quote_id=? ORDER BY id", tenantId, quoteId), partyEmail);
            jdbc.update("UPDATE m8_quote SET emailed_to=? WHERE tenant_id=? AND id=?", partyEmail, tenantId, quoteId);
        } else {
            warnings.add("No email on file for " + partyName + "; quotation not mailed");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("quote_id", quoteId);
        data.put("quote_number", number);
        data.put("status", quote.get("status"));
        data.put("subtotal", subtotal);
        data.put("tax_total", tax);
        data.put("grand_total", total);
        data.put("warnings", warnings);
        return ActionResult.builder().ok(true).message("Quotation created successfully").recordId(quoteId).data(data).build();
    }

    private static BigDecimal positive(Object raw, String what) {
        BigDecimal v = number(raw, what);
        if (v.signum() <= 0) throw new IllegalArgumentException(what + " must be greater than 0");
        return v;
    }

    private static BigDecimal nonNegative(Object raw, String what) {
        BigDecimal v = number(raw, what).setScale(2, RoundingMode.HALF_UP);
        if (v.signum() < 0) throw new IllegalArgumentException(what + " cannot be negative");
        return v;
    }

    private static BigDecimal number(Object raw, String what) {
        try {
            return new BigDecimal(String.valueOf(raw).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(what + " must be a number");
        }
    }
}
