package com.aria.conveyor;

import com.aria.conveyor.service.DispatchTaskMath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure boundary-math tests for the dispatch task quantity/auto-complete
 * logic — no DB, mirrors {@code SpelGuardEvaluatorTest}'s style.
 */
class DispatchTaskMathTest {

    @Test
    void belowTarget_stillInProgress() {
        var r = DispatchTaskMath.apply(new BigDecimal("50"), BigDecimal.ONE, new BigDecimal("100"), "Open");
        assertThat(r.newProcessedQty()).isEqualByComparingTo("51");
        assertThat(r.newStatus()).isEqualTo("Open");
    }

    @Test
    void exactlyAtTarget_completes() {
        var r = DispatchTaskMath.apply(new BigDecimal("99"), BigDecimal.ONE, new BigDecimal("100"), "Open");
        assertThat(r.newProcessedQty()).isEqualByComparingTo("100");
        assertThat(r.newStatus()).isEqualTo("Completed");
    }

    @Test
    void overshootingTarget_stillCompletes() {
        // A multi-unit scan (incrementQty > 1) can push processed past target in one step.
        var r = DispatchTaskMath.apply(new BigDecimal("98"), new BigDecimal("5"), new BigDecimal("100"), "Open");
        assertThat(r.newProcessedQty()).isEqualByComparingTo("103");
        assertThat(r.newStatus()).isEqualTo("Completed");
    }

    @Test
    void zeroTarget_completesImmediately() {
        var r = DispatchTaskMath.apply(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Open");
        assertThat(r.newStatus()).isEqualTo("Completed");
    }

    @Test
    void firstScan_fromZero() {
        var r = DispatchTaskMath.apply(BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("100"), "Open");
        assertThat(r.newProcessedQty()).isEqualByComparingTo("1");
        assertThat(r.newStatus()).isEqualTo("Open");
    }
}
