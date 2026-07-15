package com.aria.m6.invoice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-math tests for GST computation under the
 * intra-state (CGST+SGST) vs inter-state (IGST) split.
 *
 * Live wiring exercised by API smoke test at /api/m6/gst/compute-invoice.
 */
class GstMathTest {

    @Test
    void intra_state_splits_cgst_sgst_each_half_of_total() {
        BigDecimal taxable = new BigDecimal("135000");
        // 5% GST -> 2.5% CGST + 2.5% SGST
        BigDecimal cgst = taxable.multiply(new BigDecimal("2.5"))
                                  .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal sgst = taxable.multiply(new BigDecimal("2.5"))
                                  .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertThat(cgst).isEqualByComparingTo("3375.00");
        assertThat(sgst).isEqualByComparingTo("3375.00");
        assertThat(cgst.add(sgst)).isEqualByComparingTo("6750.00");   // full 5%
    }

    @Test
    void inter_state_goes_to_igst_only() {
        BigDecimal taxable = new BigDecimal("264600");   // after 2% discount on 270000
        BigDecimal igst = taxable.multiply(new BigDecimal("5.00"))
                                  .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertThat(igst).isEqualByComparingTo("13230.00");
    }

    @Test
    void cgst_plus_sgst_equals_igst_for_same_taxable() {
        // Sanity: the total GST burden is the same; only the split differs.
        BigDecimal taxable = new BigDecimal("100000");
        BigDecimal cgst = taxable.multiply(new BigDecimal("9.00"))
                                  .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal sgst = taxable.multiply(new BigDecimal("9.00"))
                                  .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal igst = taxable.multiply(new BigDecimal("18.00"))
                                  .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertThat(cgst.add(sgst)).isEqualByComparingTo(igst);
    }

    @Test
    void discount_applied_to_taxable_base_not_full_amount() {
        BigDecimal qty  = new BigDecimal("2000");
        BigDecimal rate = new BigDecimal("135.00");
        BigDecimal amount = qty.multiply(rate);
        BigDecimal discPct = new BigDecimal("2.00");
        BigDecimal discAmt = amount.multiply(discPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal taxable = amount.subtract(discAmt);
        assertThat(amount).isEqualByComparingTo("270000.00");
        assertThat(discAmt).isEqualByComparingTo("5400.00");
        assertThat(taxable).isEqualByComparingTo("264600.00");
    }

    @Test
    void cess_applied_on_taxable_separately() {
        // E.g. aerated drink at 28% GST + 12% cess
        BigDecimal taxable = new BigDecimal("10000");
        BigDecimal cess = taxable.multiply(new BigDecimal("12.00"))
                                  .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertThat(cess).isEqualByComparingTo("1200.00");
    }
}
