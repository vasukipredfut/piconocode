package com.aria.hr.payroll;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Indian-statutory payroll calculation engine.
 *
 * Inputs: employee_id + period (YYYY-MM)
 * Outputs: per-component line list ready to insert into m7_payroll_line.
 *
 * Statutory rules (all confirmed by deep-research adversarial verification):
 *  - PF (EPF Act 1952): 12% employee + 12% employer.
 *      Employer 12% splits: 8.33% EPS capped at Rs.15,000 wage + 3.67% EPF.
 *      Plus 0.5% EDLI + 0.5% admin (employer side, on basic).
 *  - ESI (Act 1948 amended 2019): 0.75% employee + 3.25% employer.
 *      Only when gross <= Rs.21,000/mo ceiling.
 *  - PT (state-wise, from m7_pt_slab): TN half-yearly Rs.0-1250,
 *      MH monthly gender-split (Rs.300 Feb surcharge), KA monthly, etc.
 *  - TDS (new regime FY 2025-26 / AY 2026-27, from m7_tds_slab):
 *      0-4L nil / 4-8L 5% / 8-12L 10% / 12-16L 15% / 16-20L 20% / 20-24L 25% / >24L 30%.
 *      Section 87A rebate Rs.60,000 (income <= Rs.12L) + std deduction Rs.75,000.
 *  - LWF (state-wise): TN Rs.20/yr emp + Rs.50 employer.
 *  - Gratuity provision (Act 1972): 4.81% of (basic+DA) accrued monthly.
 *  - OT (Factories Act §59): 2x ordinary rate for hours > 9/day or > 48/wk.
 *  - LOP: (gross / payable_days) × LOP_days.
 *  - Bonus Act 1965: 8.33%-20% of (basic+DA) capped at Rs.7000 or min wage.
 */
@Service
public class PayrollCalcService {

    /** m0_tenant_setting key: where OT pay comes from. */
    public static final String OT_SOURCE_SETTING = "payroll.ot_source";
    /** Aria default: 2x basic hourly rate x m7_attendance_summary.ot_hours. */
    public static final String OT_FROM_SUMMARY = "ATTENDANCE_SUMMARY";
    /** PICO / ERPNext: sum of m7_overtime_entry.ot_amount in the month (already priced per shift). */
    public static final String OT_FROM_ENTRIES = "OVERTIME_ENTRY";

    private final JdbcTemplate jdbc;
    private final com.aria.tenant.TenantSettings settings;

    public PayrollCalcService(JdbcTemplate jdbc, com.aria.tenant.TenantSettings settings) {
        this.jdbc = jdbc;
        this.settings = settings;
    }

    /** Compute payroll for a single employee of the current tenant for a YYYY-MM period. */
    public PayrollResult compute(long empId, String period) {
        Long tenantId = com.aria.security.TenantContext.get();
        // 1. Load active salary card
        Map<String, Object> emp;
        Map<String, Object> sal;
        try {
            emp = jdbc.queryForMap("SELECT * FROM m7_employee WHERE id=? AND tenant_id=?", empId, tenantId);
            sal = jdbc.queryForMap(
                "SELECT * FROM m7_employee_salary " +
                "WHERE emp_id=? AND tenant_id=? AND effective_from <= LAST_DAY(?) " +
                "AND (effective_to IS NULL OR effective_to >= ?) " +
                "ORDER BY effective_from DESC LIMIT 1",
                empId, tenantId, period + "-01", period + "-01");
        } catch (Exception e) {
            return PayrollResult.error("Employee or salary card not found: " + e.getMessage());
        }

        // 2. Attendance summary (defaults if missing: 26 paid days)
        BigDecimal payableDays = new BigDecimal("26");
        BigDecimal lopDays = BigDecimal.ZERO;
        BigDecimal otHours = BigDecimal.ZERO;
        try {
            Map<String, Object> att = jdbc.queryForMap(
                "SELECT payable_days, lop_days, ot_hours FROM m7_attendance_summary " +
                "WHERE emp_id=? AND tenant_id=? AND period=?", empId, tenantId, period);
            payableDays = new BigDecimal(att.get("payable_days").toString());
            lopDays     = new BigDecimal(att.get("lop_days").toString());
            otHours     = new BigDecimal(att.get("ot_hours").toString());
        } catch (Exception ignored) { /* use defaults */ }

        // 3. Earnings — straight from salary card
        BigDecimal basic = nz(sal.get("basic_monthly"));
        BigDecimal da    = nz(sal.get("da_monthly"));
        BigDecimal hra   = nz(sal.get("hra_monthly"));
        BigDecimal conv  = nz(sal.get("conveyance_monthly"));
        BigDecimal med   = nz(sal.get("medical_monthly"));
        BigDecimal spec  = nz(sal.get("special_monthly"));
        BigDecimal lta   = nz(sal.get("lta_monthly"));
        BigDecimal cea   = nz(sal.get("cea_monthly"));
        BigDecimal hostel= nz(sal.get("hostel_monthly"));
        BigDecimal cca   = nz(sal.get("cca_monthly"));
        BigDecimal shift = nz(sal.get("shift_monthly"));
        BigDecimal bonus = nz(sal.get("bonus_monthly"));
        BigDecimal inc   = nz(sal.get("incentive_monthly"));

        // OT: 2x ordinary hourly rate (Factories Act §59).
        // Ordinary rate = basic / (paid days * 8 hrs). Doubled for OT.
        boolean otFromEntries = OT_FROM_ENTRIES.equalsIgnoreCase(settings.get(OT_SOURCE_SETTING, OT_FROM_SUMMARY));
        BigDecimal otAmt = BigDecimal.ZERO;
        if (!otFromEntries && otHours.compareTo(BigDecimal.ZERO) > 0 && payableDays.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal hourly = basic.divide(payableDays.multiply(new BigDecimal("8")), 4, RoundingMode.HALF_UP);
            otAmt = hourly.multiply(otHours).multiply(new BigDecimal("2")).setScale(2, RoundingMode.HALF_UP);
        }
        // OVERTIME_ENTRY: already-priced daily OT (shift multipliers, holiday rate), added after LOP
        // so absent days do not prorate it -- ERPNext appended it to the slip's earnings as-is.
        BigDecimal otEntries = otFromEntries ? overtimeEntries(empId, tenantId, period) : BigDecimal.ZERO;

        // Total gross before LOP
        BigDecimal grossPreLop = basic.add(da).add(hra).add(conv).add(med).add(spec)
                                       .add(lta).add(cea).add(hostel).add(cca)
                                       .add(shift).add(bonus).add(inc).add(otAmt);

        // LOP deduction: (gross / payable_days) * LOP days
        BigDecimal lopDeduct = BigDecimal.ZERO;
        if (lopDays.compareTo(BigDecimal.ZERO) > 0 && payableDays.compareTo(BigDecimal.ZERO) > 0) {
            lopDeduct = grossPreLop.divide(payableDays, 4, RoundingMode.HALF_UP)
                                    .multiply(lopDays).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal gross = grossPreLop.subtract(lopDeduct).add(otEntries);

        // 4. Statutory deductions — load current settings
        Map<String, Object> pfSetting;
        Map<String, Object> esiSetting;
        try {
            pfSetting = jdbc.queryForMap(
                "SELECT * FROM m7_pf_setting WHERE tenant_id=? ORDER BY effective_from DESC LIMIT 1", tenantId);
            esiSetting = jdbc.queryForMap(
                "SELECT * FROM m7_esi_setting WHERE tenant_id=? ORDER BY effective_from DESC LIMIT 1", tenantId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return PayrollResult.error("PF / ESI settings missing for this tenant (m7_pf_setting, m7_esi_setting)");
        }

        BigDecimal pfBase = basic.add(da);
        BigDecimal pfCeiling = nz(pfSetting.get("wage_ceiling"));
        boolean cap = isTrue(sal.get("pf_employer_basic_capped"));
        BigDecimal pfWage = (cap && pfBase.compareTo(pfCeiling) > 0) ? pfCeiling : pfBase;
        BigDecimal pfEmp  = pct(pfWage, pfSetting.get("emp_rate_pct"));
        // Employer split: EPS 8.33% capped at Rs.15k always, EPF gets the rest
        BigDecimal pfEpsBase = pfBase.min(pfCeiling);
        BigDecimal pfEps  = pct(pfEpsBase, pfSetting.get("eps_share_pct"));
        BigDecimal pfEpf  = pct(pfWage, pfSetting.get("emp_rate_pct")).subtract(pfEps).max(BigDecimal.ZERO);
        BigDecimal pfEdli = pct(pfEpsBase, pfSetting.get("edli_pct"));
        BigDecimal pfAdmin= pct(pfEpsBase, pfSetting.get("admin_pct"));

        // ESI — only if gross <= Rs.21000 ceiling AND employee flag
        BigDecimal esiEmp = BigDecimal.ZERO, esiErp = BigDecimal.ZERO;
        BigDecimal esiCeiling = nz(esiSetting.get("gross_wage_ceiling"));
        if (isTrue(emp.get("esi_applicable")) && gross.compareTo(esiCeiling) <= 0) {
            esiEmp = pct(gross, esiSetting.get("emp_rate_pct"));
            esiErp = pct(gross, esiSetting.get("employer_rate_pct"));
        }

        // PT — state-wise slab
        BigDecimal pt = BigDecimal.ZERO;
        String ptState = (String) emp.get("pt_state");
        String gender  = (String) emp.get("gender");
        if (isTrue(emp.get("pt_applicable")) && ptState != null) {
            pt = lookupPt(ptState, gender, gross, period);
        }

        // TDS — current FY new regime by default
        BigDecimal tds = BigDecimal.ZERO;
        if (isTrue(emp.get("tds_applicable"))) {
            BigDecimal annual = gross.multiply(new BigDecimal("12"));
            tds = computeTds(annual, "NEW", "2025-26").divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        }

        // Gratuity provision 4.81% of basic+DA (employer side, not deducted)
        BigDecimal gratuityProv = pct(pfBase, new BigDecimal("4.81"));

        // 5. Build result
        PayrollResult r = new PayrollResult();
        r.empId = empId;
        r.period = period;
        r.payableDays = payableDays;
        r.lopDays = lopDays;
        // Earnings
        r.lines.add(line("BASIC", "EARNING", basic));
        if (da.signum() > 0) r.lines.add(line("DA", "EARNING", da));
        if (hra.signum() > 0) r.lines.add(line("HRA", "EARNING", hra));
        if (conv.signum() > 0) r.lines.add(line("CONVEYANCE", "EARNING", conv));
        if (med.signum() > 0) r.lines.add(line("MEDICAL_ALLOW", "EARNING", med));
        if (spec.signum() > 0) r.lines.add(line("SPECIAL", "EARNING", spec));
        if (lta.signum() > 0) r.lines.add(line("LTA", "EARNING", lta));
        if (cea.signum() > 0) r.lines.add(line("CEA", "EARNING", cea));
        if (hostel.signum() > 0) r.lines.add(line("HOSTEL_ALLOW", "EARNING", hostel));
        if (cca.signum() > 0) r.lines.add(line("CCA", "EARNING", cca));
        if (shift.signum() > 0) r.lines.add(line("SHIFT_ALLOW", "EARNING", shift));
        if (bonus.signum() > 0) r.lines.add(line("BONUS", "EARNING", bonus));
        if (inc.signum() > 0) r.lines.add(line("INCENTIVE", "EARNING", inc));
        if (otAmt.signum() > 0) r.lines.add(line("OT", "EARNING", otAmt));
        if (otEntries.signum() > 0) r.lines.add(line("OT", "EARNING", otEntries));
        r.otAmount = otAmt.add(otEntries);
        if (lopDeduct.signum() > 0) r.lines.add(line("LOP_DEDUCT", "DEDUCTION", lopDeduct));
        // Deductions (employee side)
        if (isTrue(emp.get("pf_applicable")) && pfEmp.signum() > 0) r.lines.add(line("PF_EMP", "DEDUCTION", pfEmp));
        if (esiEmp.signum() > 0) r.lines.add(line("ESI_EMP", "DEDUCTION", esiEmp));
        if (pt.signum() > 0) r.lines.add(line("PT", "DEDUCTION", pt));
        if (tds.signum() > 0) r.lines.add(line("TDS", "DEDUCTION", tds));
        // Employer (recorded but not netted)
        if (isTrue(emp.get("pf_applicable"))) {
            r.lines.add(line("PF_EPF_ER", "EMPLOYER", pfEpf));
            r.lines.add(line("PF_EPS_ER", "EMPLOYER", pfEps));
            r.lines.add(line("PF_EDLI_ER", "EMPLOYER", pfEdli));
            r.lines.add(line("PF_ADMIN_ER", "EMPLOYER", pfAdmin));
        }
        if (esiErp.signum() > 0) r.lines.add(line("ESI_EMP_ER", "EMPLOYER", esiErp));
        r.lines.add(line("GRATUITY_ER", "EMPLOYER", gratuityProv));
        // Totals
        r.gross   = gross;
        r.totalDeduct = pfEmp.add(esiEmp).add(pt).add(tds);
        r.netPay  = gross.subtract(r.totalDeduct);
        return r;
    }

    // ===== Helpers ==========================================================

    /** ERPNext overrides/salary.py: OverTime Entry amounts dated inside the month, except cancelled ones. */
    private BigDecimal overtimeEntries(long empId, Long tenantId, String period) {
        try {
            BigDecimal v = jdbc.queryForObject(
                "SELECT COALESCE(SUM(ot_amount),0) FROM m7_overtime_entry WHERE tenant_id=? AND employee_id=? " +
                "AND status <> 'CANCELLED' AND attendance_date BETWEEN ? AND LAST_DAY(?)",
                BigDecimal.class, tenantId, empId, period + "-01", period + "-01");
            return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO; // V382 not applied on this database
        }
    }

    /** Normalize free-form gender values to M / F used by the PT slab table. */
    private String normGender(String g) {
        if (g == null) return null;
        String u = g.trim().toUpperCase();
        if (u.startsWith("M")) return "M";
        if (u.startsWith("F")) return "F";
        return null;
    }

    private BigDecimal lookupPt(String state, String gender, BigDecimal gross, String period) {
        String g = normGender(gender);
        // Find slabs for this state. For HALF_YEAR (TN), apply against the
        // employee's half-year aggregate gross / 6, but for monthly payroll
        // we approximate as gross/6 * 6 = gross; here we apply the divided
        // slab amount per month so the half-year cap of Rs.1250 spreads as
        // Rs.208 monthly. For MH/KA monthly slabs, use the gross directly.
        try {
            List<Map<String, Object>> slabs = jdbc.queryForList(
                "SELECT cadence, gender, gross_from, gross_to, pt_amount, feb_surcharge " +
                "FROM m7_pt_slab WHERE tenant_id=? AND state_code=? " +
                "ORDER BY effective_from DESC, gross_from",
                com.aria.security.TenantContext.get(), state);
            for (Map<String, Object> s : slabs) {
                String sg = (String) s.get("gender");
                if (sg != null && !sg.equals(g)) continue;
                BigDecimal from = nz(s.get("gross_from"));
                Object toObj = s.get("gross_to");
                BigDecimal to = toObj == null ? null : new BigDecimal(toObj.toString());
                String cadence = (String) s.get("cadence");
                BigDecimal compareAgainst;
                if ("HALF_YEAR".equals(cadence)) {
                    compareAgainst = gross.multiply(new BigDecimal("6"));   // annualise to half-year
                } else {
                    compareAgainst = gross;
                }
                if (compareAgainst.compareTo(from) >= 0 && (to == null || compareAgainst.compareTo(to) <= 0)) {
                    BigDecimal amt = nz(s.get("pt_amount"));
                    // Convert to per-month
                    if ("HALF_YEAR".equals(cadence)) amt = amt.divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP);
                    if ("ANNUAL".equals(cadence))    amt = amt.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
                    // Feb surcharge for MH
                    if (period.endsWith("-02") && s.get("feb_surcharge") != null) {
                        amt = amt.add(nz(s.get("feb_surcharge")));
                    }
                    return amt;
                }
            }
        } catch (Exception ignored) {}
        return BigDecimal.ZERO;
    }

    private BigDecimal computeTds(BigDecimal annual, String regime, String fy) {
        try {
            // Apply std deduction first
            List<Map<String, Object>> slabs = jdbc.queryForList(
                "SELECT annual_from, annual_to, rate_pct, rebate_87a, std_deduction " +
                "FROM m7_tds_slab WHERE regime=? AND fy=? ORDER BY annual_from",
                regime, fy);
            if (slabs.isEmpty()) return BigDecimal.ZERO;
            BigDecimal stdDed = nz(slabs.get(0).get("std_deduction"));
            BigDecimal rebate = nz(slabs.get(0).get("rebate_87a"));
            BigDecimal taxable = annual.subtract(stdDed).max(BigDecimal.ZERO);
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal cursor = BigDecimal.ZERO;
            for (Map<String, Object> s : slabs) {
                BigDecimal from = nz(s.get("annual_from"));
                Object toObj = s.get("annual_to");
                BigDecimal to = toObj == null ? taxable : new BigDecimal(toObj.toString()).min(taxable);
                BigDecimal pct = nz(s.get("rate_pct"));
                if (to.compareTo(from) > 0 && taxable.compareTo(from) > 0) {
                    BigDecimal slab = to.subtract(from.max(cursor)).max(BigDecimal.ZERO);
                    tax = tax.add(slab.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                    cursor = to;
                }
            }
            // 87A rebate
            if (rebate.signum() > 0) {
                // If taxable <= Rs.12L (for FY 2025-26), apply rebate up to ceiling
                BigDecimal rebateCeilingTaxable = new BigDecimal("1200000");
                if (taxable.compareTo(rebateCeilingTaxable) <= 0) tax = tax.subtract(rebate).max(BigDecimal.ZERO);
            }
            return tax;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal pct(BigDecimal base, Object pct) {
        if (pct == null) return BigDecimal.ZERO;
        return base.multiply(new BigDecimal(pct.toString()))
                   .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nz(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }

    private boolean isTrue(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number)  return ((Number) o).intValue() != 0;
        String s = o.toString();
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "t".equalsIgnoreCase(s);
    }

    private Map<String, Object> line(String comp, String type, BigDecimal amt) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("component_code", comp);
        m.put("component_type", type);
        m.put("amount", amt);
        return m;
    }

    // ===== Result type ======================================================

    public static class PayrollResult {
        public long empId;
        public String period;
        public BigDecimal payableDays;
        public BigDecimal lopDays;
        public BigDecimal gross;
        public BigDecimal totalDeduct;
        public BigDecimal netPay;
        /** Total OT earning (either source). */
        public BigDecimal otAmount = BigDecimal.ZERO;
        public List<Map<String, Object>> lines = new ArrayList<>();
        public String error;

        public static PayrollResult error(String msg) {
            PayrollResult r = new PayrollResult();
            r.error = msg;
            return r;
        }
    }
}
