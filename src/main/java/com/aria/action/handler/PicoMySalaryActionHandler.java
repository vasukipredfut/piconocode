package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import com.aria.utils.Htmltopdf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.my-salary -- the caller's own salary data for the mobile app. Only APPROVED / PAID payslips
 * are visible (ERPNext showed submitted slips only). ERPNext served all of this with
 * allow_guest=True keyed by employee_number, so anyone could read anyone's salary and bank account.
 *
 * <p>args.view:
 * <ul>
 *   <li>{@code slips}      -- payslips, optional {@code year} (get_salary_slips)</li>
 *   <li>{@code slip}       -- one {@code period}: totals + earnings / deductions (get_salary_slips with salary_slip)</li>
 *   <li>{@code slip-pdf}   -- one {@code period} as base64 PDF from the stored snapshot (download_salary_slip_pdf)</li>
 *   <li>{@code profile}    -- employment, statutory and bank details, bank / PAN masked (get_employee_salary_profile)</li>
 *   <li>{@code structure}  -- current salary card (get_employee_salary_structure)</li>
 *   <li>{@code components} -- payslip components of the tenant (get_employee_salary_components)</li>
 * </ul>
 */
@Component
public class PicoMySalaryActionHandler implements ActionHandler {

    public static final String CODE = "pico.my-salary";

    /** m7_employee_salary monthly columns -> m7_salary_component / payslip codes. */
    private static final Map<String, String> MONTHLY = new LinkedHashMap<>();
    static {
        MONTHLY.put("basic_monthly", "BASIC");
        MONTHLY.put("da_monthly", "DA");
        MONTHLY.put("hra_monthly", "HRA");
        MONTHLY.put("conveyance_monthly", "CONVEYANCE");
        MONTHLY.put("medical_monthly", "MEDICAL_ALLOW");
        MONTHLY.put("special_monthly", "SPECIAL");
        MONTHLY.put("lta_monthly", "LTA");
        MONTHLY.put("cea_monthly", "CEA");
        MONTHLY.put("hostel_monthly", "HOSTEL_ALLOW");
        MONTHLY.put("cca_monthly", "CCA");
        MONTHLY.put("shift_monthly", "SHIFT_ALLOW");
        MONTHLY.put("bonus_monthly", "BONUS");
        MONTHLY.put("incentive_monthly", "INCENTIVE");
    }

    @Autowired private JdbcTemplate jdbc;
    @Autowired private Htmltopdf htmlToPdf;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Map<String, Object> self = PicoSelf.employee(jdbc);
        if (self == null) return ActionResult.error("Employee not linked to this user");
        long empId = ((Number) self.get("id")).longValue();
        Long tenantId = TenantContext.get();
        String view = PicoSelf.str(args.get("view"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("employee_id", empId);
        data.put("emp_code", self.get("emp_code"));
        data.put("full_name", self.get("full_name"));
        switch (view == null ? "slips" : view) {
            case "slips" -> {
                Object year = args.get("year");
                String yearFilter = year == null || year.toString().isBlank() ? "" : " AND p.payroll_period LIKE ?";
                List<Object> params = new ArrayList<>(List.of(tenantId, empId));
                if (!yearFilter.isEmpty()) params.add(year(year) + "-%");
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT p.payroll_period, p.gross, p.total_deduct, p.net_pay, p.status, r.payable_days, r.lop_days, " +
                                "r.ot_amount, r.paid_at, r.pay_reference FROM m7_payslip p LEFT JOIN m7_payroll_run r " +
                                "ON r.tenant_id = p.tenant_id AND r.employee_id = p.emp_id AND r.payroll_period = p.payroll_period " +
                                "WHERE p.tenant_id=? AND p.emp_id=? AND p.status IN ('APPROVED','PAID')" + yearFilter +
                                " ORDER BY p.payroll_period DESC", params.toArray());
                data.put("count", rows.size());
                data.put("salary_slips", rows);
            }
            case "slip" -> {
                String period = period(args);
                Map<String, Object> slip = slip(tenantId, empId, period);
                if (slip == null) return ActionResult.error("No approved payslip for " + period);
                List<Map<String, Object>> earnings = new ArrayList<>(), deductions = new ArrayList<>();
                for (Map<String, Object> l : jdbc.queryForList(
                        "SELECT l.component_code, l.component_type, l.amount, c.name AS component_name FROM m7_payroll_line l " +
                                "JOIN m7_payroll_run r ON r.id = l.run_id AND r.tenant_id = l.tenant_id " +
                                "LEFT JOIN m7_salary_component c ON c.tenant_id = l.tenant_id AND c.code = l.component_code " +
                                "WHERE l.tenant_id=? AND r.employee_id=? AND r.payroll_period=? ORDER BY l.id",
                        tenantId, empId, period)) {
                    if ("EARNING".equals(l.get("component_type"))) earnings.add(l);
                    else if ("DEDUCTION".equals(l.get("component_type"))) deductions.add(l);
                }
                slip.remove("html_snapshot");
                slip.put("earnings", earnings);
                slip.put("deductions", deductions);
                data.put("salary_slip", slip);
            }
            case "slip-pdf" -> {
                String period = period(args);
                Map<String, Object> slip = slip(tenantId, empId, period);
                if (slip == null) return ActionResult.error("No approved payslip for " + period);
                String html = (String) slip.get("html_snapshot");
                if (html == null || html.isBlank()) return ActionResult.error("Payslip for " + period + " has no stored document");
                byte[] pdf = htmlToPdf.generatePdfFromBase64(
                        Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8)));
                if (pdf == null) return ActionResult.error("Could not render the payslip PDF");
                data.put("filename", "Payslip-" + self.get("emp_code") + "-" + period + ".pdf");
                data.put("content_type", "application/pdf");
                data.put("pdf_base64", Base64.getEncoder().encodeToString(pdf));
            }
            case "profile" -> {
                Map<String, Object> e = jdbc.queryForMap(
                        "SELECT e.emp_code, e.full_name, e.department, e.designation, e.grade, e.employment_type, e.work_location, " +
                                "e.date_of_joining, e.shift_code, e.photo_url, e.email_official, e.mobile_primary, e.uan_no, e.pf_no, e.esi_no, " +
                                "e.pan_no, e.bank_name, e.bank_account_no, e.bank_ifsc, m.full_name AS reporting_manager, " +
                                "a.full_name AS leave_approver, h.list_name AS holiday_list " +
                                "FROM m7_employee e LEFT JOIN m7_employee m ON m.tenant_id = e.tenant_id AND m.id = e.reports_to_emp_id " +
                                "LEFT JOIN m7_employee a ON a.tenant_id = e.tenant_id AND a.id = COALESCE(e.leave_approver_id, e.reports_to_emp_id) " +
                                "LEFT JOIN m7_holiday_list h ON h.tenant_id = e.tenant_id AND h.id = e.holiday_list_id " +
                                "WHERE e.tenant_id=? AND e.id=?", tenantId, empId);
                Map<String, Object> profile = new LinkedHashMap<>(e);
                profile.put("pan_no", mask((String) e.get("pan_no")));
                profile.put("bank_account_no", mask((String) e.get("bank_account_no")));
                Map<String, Object> card = salaryCard(tenantId, empId);
                profile.put("ctc_annual", card == null ? null : card.get("ctc_annual"));
                data.put("employee_profile", profile);
            }
            case "structure" -> {
                Map<String, Object> card = salaryCard(tenantId, empId);
                if (card == null) return ActionResult.error("Salary structure not assigned");
                List<Map<String, Object>> earnings = new ArrayList<>();
                BigDecimal monthly = BigDecimal.ZERO;
                for (Map.Entry<String, String> m : MONTHLY.entrySet()) {
                    BigDecimal v = (BigDecimal) card.get(m.getKey());
                    if (v == null || v.signum() == 0) continue;
                    earnings.add(Map.of("component_code", m.getValue(), "amount", v));
                    monthly = monthly.add(v);
                }
                Map<String, Object> structure = new LinkedHashMap<>();
                structure.put("effective_from", card.get("effective_from"));
                structure.put("ctc_annual", card.get("ctc_annual"));
                structure.put("monthly_gross", monthly);
                structure.put("earnings", earnings);
                structure.put("yearly", Map.of(
                        "medical_reimb", card.get("medical_reimb_yearly"), "fuel_reimb", card.get("fuel_reimb_yearly"),
                        "telephone_reimb", card.get("telephone_reimb_yearly"), "variable_pay", card.get("variable_pay_yearly")));
                data.put("salary_structure", structure);
            }
            case "components" -> {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT code, name, component_type, calc_method, taxable, pf_eligible, esi_eligible FROM m7_salary_component " +
                                "WHERE tenant_id=? AND show_on_payslip=1 ORDER BY sort_order, code", tenantId);
                data.put("count", rows.size());
                data.put("components", rows);
            }
            default -> throw new IllegalArgumentException("view must be slips, slip, slip-pdf, profile, structure or components");
        }
        return ActionResult.builder().ok(true).data(data).build();
    }

    private Map<String, Object> slip(Long tenantId, long empId, String period) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT p.payroll_period, p.gross, p.total_deduct, p.net_pay, p.status, p.html_snapshot, r.payable_days, r.lop_days, " +
                        "r.ot_amount, r.paid_at, r.pay_reference FROM m7_payslip p LEFT JOIN m7_payroll_run r " +
                        "ON r.tenant_id = p.tenant_id AND r.employee_id = p.emp_id AND r.payroll_period = p.payroll_period " +
                        "WHERE p.tenant_id=? AND p.emp_id=? AND p.payroll_period=? AND p.status IN ('APPROVED','PAID')",
                tenantId, empId, period);
        return rows.isEmpty() ? null : new LinkedHashMap<>(rows.get(0));
    }

    private Map<String, Object> salaryCard(Long tenantId, long empId) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM m7_employee_salary WHERE tenant_id=? AND emp_id=? AND effective_from <= ? " +
                        "AND (effective_to IS NULL OR effective_to >= ?) ORDER BY effective_from DESC LIMIT 1",
                tenantId, empId, today, today);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Keeps the last 4 characters: a phone screen or its cache is not the place for full PAN / account numbers. */
    private static String mask(String s) {
        if (s == null || s.length() <= 4) return s;
        return "*".repeat(s.length() - 4) + s.substring(s.length() - 4);
    }

    private static String period(Map<String, Object> args) {
        String p = PicoSelf.str(args.get("period"));
        if (p == null || !p.matches("\\d{4}-(0[1-9]|1[0-2])")) throw new IllegalArgumentException("period must be YYYY-MM");
        return p;
    }

    private static int year(Object v) {
        try {
            return Integer.parseInt(v.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("year must be a number");
        }
    }
}
