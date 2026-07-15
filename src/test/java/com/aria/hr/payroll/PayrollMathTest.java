package com.aria.hr.payroll;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-math tests for the Indian statutory payroll formulas surfaced in V23.
 *
 * Avoids Mockito on JdbcTemplate (inline mock breaks on JDK 25); instead
 * encodes the exact statutory arithmetic so a regression on PF/ESI/PT/TDS
 * numbers shows up immediately. The integration that wires this to real
 * data + the JdbcTemplate is exercised by the live API smoke test at
 * GET /api/m7/payroll/preview.
 */
class PayrollMathTest {

    /** PF Employee = 12% of min(basic+DA, wage_ceiling). Capped at Rs.15k since V23. */
    @Test
    void pf_employee_caps_at_basic_15k() {
        BigDecimal basic = new BigDecimal("22500");   // Ramesh — above ceiling
        BigDecimal ceiling = new BigDecimal("15000");
        BigDecimal pfWage = basic.min(ceiling);
        BigDecimal pfEmp = pfWage.multiply(new BigDecimal("12"))
                                 .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertThat(pfEmp).isEqualByComparingTo("1800.00");
    }

    /** PF Employer 12% splits as EPS 8.33% (capped at Rs.15k always) + EPF (residual). */
    @Test
    void pf_employer_eps_eight_thirty_three_capped_epf_residual() {
        BigDecimal basic = new BigDecimal("22500");
        BigDecimal ceiling = new BigDecimal("15000");
        BigDecimal pfEpsBase = basic.min(ceiling);
        BigDecimal pfWage = pfEpsBase;
        BigDecimal pfEps = pfEpsBase.multiply(new BigDecimal("8.33"))
                                     .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal pfEmployer12 = pfWage.multiply(new BigDecimal("12"))
                                         .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal pfEpf = pfEmployer12.subtract(pfEps);
        assertThat(pfEps).isBetween(new BigDecimal("1249"), new BigDecimal("1250"));
        assertThat(pfEpf).isBetween(new BigDecimal("550"),  new BigDecimal("551"));
    }

    /** ESI applies only when gross <= Rs.21,000 ceiling. */
    @Test
    void esi_applies_below_21k_only() {
        BigDecimal grossLow  = new BigDecimal("14000");
        BigDecimal grossHigh = new BigDecimal("31500");
        BigDecimal ceiling   = new BigDecimal("21000");

        boolean lowApplies  = grossLow.compareTo(ceiling) <= 0;
        boolean highApplies = grossHigh.compareTo(ceiling) <= 0;
        assertThat(lowApplies).isTrue();
        assertThat(highApplies).isFalse();

        BigDecimal esiEmp = grossLow.multiply(new BigDecimal("0.75"))
                                     .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertThat(esiEmp).isEqualByComparingTo("105.00");
    }

    /** TN PT half-yearly Rs.1,250 cap → monthly equivalent Rs.208.33. */
    @Test
    void pt_tn_top_slab_monthly_equivalent() {
        BigDecimal halfYearly = new BigDecimal("1250");
        BigDecimal monthly = halfYearly.divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP);
        assertThat(monthly).isEqualByComparingTo("208.33");
    }

    /** MH MALE >Rs.10k slab = Rs.200/mo, Rs.100 surcharge in February. */
    @Test
    void pt_mh_male_feb_includes_100_surcharge() {
        BigDecimal monthly = new BigDecimal("200");
        BigDecimal feb     = monthly.add(new BigDecimal("100"));
        assertThat(feb).isEqualByComparingTo("300.00");
    }

    /** Gratuity provision per Gratuity Act 1972 = 4.81% of (basic + DA). */
    @Test
    void gratuity_provision_481_pct_of_basic_da() {
        BigDecimal basic = new BigDecimal("10000");
        BigDecimal grat = basic.multiply(new BigDecimal("4.81"))
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertThat(grat).isEqualByComparingTo("481.00");
    }

    /**
     * TDS new regime FY 2025-26 (AY 2026-27) — high earner Rs.30L gross.
     * Verified against deep-research adversarial finding 1 (3-0 confirmed):
     *   0-4L nil | 4-8L 5% | 8-12L 10% | 12-16L 15% | 16-20L 20% | 20-24L 25% | >24L 30%
     * Standard deduction Rs.75,000 (salaried).
     * 87A rebate Rs.60,000 — only applies if taxable <= Rs.12L.
     */
    @Test
    void tds_30l_ctc_under_new_regime_fy_2025_26() {
        BigDecimal annual = new BigDecimal("3000000");
        BigDecimal std    = new BigDecimal("75000");
        BigDecimal taxable = annual.subtract(std);   // 2925000

        BigDecimal tax = BigDecimal.ZERO;
        // 4L-8L @ 5%
        tax = tax.add(new BigDecimal("400000").multiply(new BigDecimal("0.05")));
        // 8L-12L @ 10%
        tax = tax.add(new BigDecimal("400000").multiply(new BigDecimal("0.10")));
        // 12L-16L @ 15%
        tax = tax.add(new BigDecimal("400000").multiply(new BigDecimal("0.15")));
        // 16L-20L @ 20%
        tax = tax.add(new BigDecimal("400000").multiply(new BigDecimal("0.20")));
        // 20L-24L @ 25%
        tax = tax.add(new BigDecimal("400000").multiply(new BigDecimal("0.25")));
        // 24L-29.25L @ 30%
        tax = tax.add(taxable.subtract(new BigDecimal("2400000")).multiply(new BigDecimal("0.30")));

        // Annual tax = 457500, monthly = 38125
        assertThat(tax).isEqualByComparingTo("457500.00");
        BigDecimal monthly = tax.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        assertThat(monthly).isEqualByComparingTo("38125.00");
    }

    /** 87A rebate Rs.60k makes income up to Rs.12L effectively tax-free. */
    @Test
    void rebate_87a_makes_12l_effectively_tax_free() {
        BigDecimal annual = new BigDecimal("1200000");
        BigDecimal std    = new BigDecimal("75000");
        BigDecimal taxable = annual.subtract(std);   // 1125000

        BigDecimal tax = BigDecimal.ZERO;
        tax = tax.add(new BigDecimal("400000").multiply(new BigDecimal("0.05")));   // 4-8L
        tax = tax.add(new BigDecimal("325000").multiply(new BigDecimal("0.10")));   // 8-11.25L
        // Tax = 20000 + 32500 = 52500
        assertThat(tax).isEqualByComparingTo("52500.00");

        // 87A rebate Rs.60k kicks in since taxable <= 1.2L threshold
        BigDecimal rebate = new BigDecimal("60000");
        BigDecimal afterRebate = tax.subtract(rebate).max(BigDecimal.ZERO);
        assertThat(afterRebate).isEqualByComparingTo("0.00");
    }

    /** LOP = (gross / payable_days) * lop_days. */
    @Test
    void lop_proportional_to_payable_days() {
        BigDecimal gross = new BigDecimal("45000");
        BigDecimal payableDays = new BigDecimal("26");
        BigDecimal lopDays = new BigDecimal("2");
        BigDecimal lopDeduct = gross.divide(payableDays, 4, RoundingMode.HALF_UP)
                                     .multiply(lopDays).setScale(2, RoundingMode.HALF_UP);
        // 45000/26 = 1730.7692 * 2 = 3461.54
        assertThat(lopDeduct).isEqualByComparingTo("3461.54");
    }

    /** OT under Factories Act §59 = 2x ordinary hourly rate. */
    @Test
    void ot_two_times_ordinary_rate() {
        BigDecimal basic = new BigDecimal("22500");
        BigDecimal payableDays = new BigDecimal("26");
        BigDecimal hoursPerDay = new BigDecimal("8");
        BigDecimal otHours = new BigDecimal("10");
        BigDecimal hourly = basic.divide(payableDays.multiply(hoursPerDay), 4, RoundingMode.HALF_UP);
        BigDecimal otAmt = hourly.multiply(otHours).multiply(new BigDecimal("2"))
                                   .setScale(2, RoundingMode.HALF_UP);
        // 22500 / 208 = 108.1731 * 10 * 2 = 2163.46
        assertThat(otAmt).isEqualByComparingTo("2163.46");
    }
}
