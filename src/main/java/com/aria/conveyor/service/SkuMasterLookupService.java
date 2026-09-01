package com.aria.conveyor.service;

import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves a scanned barcode to its authoritative SKU via {@code m5_sku_master} — shared by
 * {@link ConveyorScanService} (the server-side check every {@code POST /api/conveyor/scan}
 * call goes through) and {@code ConveyorSkuMasterController}'s {@code GET
 * /api/conveyor/skumaster} (a tokenless lookup the device itself can call, so it can pick the
 * right Dispatch Task locally when several are concurrently Open on one bay — see
 * {@code raspberry-pi-client/conveyor_client.py}'s {@code resolve_sku}).
 */
@Service
public class SkuMasterLookupService {

    private final JdbcTemplate jdbc;

    public SkuMasterLookupService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Matches on EITHER {@code barcode} OR {@code sku_code} — some scanner configurations emit
     * the SKU code directly rather than the printed barcode (same convention as the older
     * {@code ConveyorAgentController}'s equivalent lookup). {@code barcode} isn't a unique key
     * on {@code m5_sku_master}, so ties are broken deterministically by {@code id}. Returns
     * {@code Optional.empty()} for no match at all — a match with a non-ACTIVE {@code status}
     * is still returned; callers decide what to do with that (see {@link ConveyorScanService}'s
     * {@code SKU_INACTIVE} rejection).
     *
     * <p>{@code sku_name} (the SKU's description) and {@code pack_pieces_per_case} (pieces per
     * box/case) ride along purely for device-side display/logging — neither is read anywhere
     * in the scan/rule-evaluation flow itself, same tier as {@code dispatch_task.vehicle_no}.
     */
    public Optional<Map<String, Object>> resolveByBarcode(String barcode) {
        long tenantId = TenantContext.get();
        List<Map<String, Object>> matches = jdbc.queryForList(
                "SELECT sku_code, sku_name, pack_pieces_per_case, status FROM m5_sku_master "
                        + "WHERE tenant_id = ? AND (barcode = ? OR sku_code = ?) ORDER BY id",
                tenantId, barcode, barcode);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }
}
