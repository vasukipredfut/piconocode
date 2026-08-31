package com.aria.conveyor.service;

import java.math.BigDecimal;

/**
 * Pure quantity/status math for a {@code dispatch_task} scan increment —
 * extracted from {@link DispatchTaskService} so the target-vs-processed and
 * auto-complete boundary logic is unit-testable with no database.
 */
public final class DispatchTaskMath {

    private DispatchTaskMath() {
    }

    public record Result(BigDecimal newProcessedQty, String newStatus) {
    }

    /**
     * @param processedQty current processed quantity (before this scan)
     * @param incrementQty quantity this scan adds (normally 1, but not assumed)
     * @param targetQty    the task's target quantity
     * @param currentStatus the task's status before this scan (only Open tasks accept scans;
     *                      callers are expected to have already checked this, but the math itself is
     *                      status-agnostic — it just says what the resulting status WOULD be)
     * @return the new processed quantity and status ({@code Completed} once processed >= target,
     *         otherwise unchanged)
     */
    public static Result apply(BigDecimal processedQty, BigDecimal incrementQty, BigDecimal targetQty, String currentStatus) {
        BigDecimal newQty = processedQty.add(incrementQty);
        String newStatus = newQty.compareTo(targetQty) >= 0 ? "Completed" : currentStatus;
        return new Result(newQty, newStatus);
    }
}
