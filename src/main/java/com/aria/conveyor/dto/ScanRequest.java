package com.aria.conveyor.dto;

import java.math.BigDecimal;

/**
 * {@code POST /api/conveyor/scan} request body. {@code stepCode} identifies
 * which workflow step this scan belongs to (so the right PLC binding
 * resolves — a line can have different PLC integrations at different
 * steps); {@code taskId} is optional — if omitted, the active task for
 * {@code bayCode} is resolved automatically (matched by SKU/order ref when
 * more than one task is concurrently active on that bay).
 */
public record ScanRequest(
        String workflowCode,
        String stepCode,
        String bayCode,
        String barcode,
        Long taskId,
        Long scannerIntegrationId,
        String sku,
        String orderRef,
        BigDecimal qty) {
}
