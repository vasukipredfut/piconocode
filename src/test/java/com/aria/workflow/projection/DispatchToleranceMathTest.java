package com.aria.workflow.projection;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Locks the PMT tolerance boundary — the "money control". Loaded = |W1 − W2|;
 * expected = Σ(order_qty × SKU unit_weight_kg). Verdict YES ⇒ within tolerance
 * (bills straight through); NO ⇒ over ⇒ camera + manager. Pure arithmetic, so
 * it runs without the Mockito-on-JdbcTemplate fragility this repo avoids.
 */
class DispatchToleranceMathTest {

    private static final BigDecimal TOL = new BigDecimal("0.400"); // 0.4% = 4 kg / ton

    private static String verdict(String loaded, String expected) {
        return DispatchToleranceListener.verdict(new BigDecimal(loaded), new BigDecimal(expected), TOL);
    }

    @Test
    void exactMatchIsWithin() {
        assertEquals("YES", verdict("2315.5", "2315.5"));
        assertEquals("0.000", DispatchToleranceListener
                .variancePct(new BigDecimal("2315.5"), new BigDecimal("2315.5")).toPlainString());
    }

    @Test
    void justInsideTolerancePasses() {
        // 2315.5 × 0.4% = 9.262 kg headroom; 9 kg over ⇒ 0.389% ⇒ within.
        assertEquals("YES", verdict("2324.5", "2315.5"));
    }

    @Test
    void justOutsideToleranceBlocks() {
        // 10 kg over ⇒ 0.432% ⇒ over tolerance.
        assertEquals("NO", verdict("2325.5", "2315.5"));
    }

    @Test
    void underloadOverToleranceAlsoBlocks() {
        // Direction-agnostic: 100 kg short is just as much a block as 100 kg over.
        assertEquals("NO", verdict("2215.5", "2315.5"));
    }

    @Test
    void grosslyOverBlocks() {
        // The walk's over-tolerance truck: loaded 3000 vs expected 2315.5 ⇒ 29.562%.
        assertEquals("NO", verdict("3000", "2315.5"));
        assertEquals("29.562", DispatchToleranceListener
                .variancePct(new BigDecimal("3000"), new BigDecimal("2315.5")).toPlainString());
    }

    @Test
    void boundaryIsInclusive() {
        // Variance exactly equal to tolerance counts as within (≤, not <).
        // 100 expected, 0.4 over ⇒ exactly 0.4%.
        assertEquals("YES", verdict("100.4", "100"));
        assertEquals("NO", verdict("100.5", "100"));
    }
}
