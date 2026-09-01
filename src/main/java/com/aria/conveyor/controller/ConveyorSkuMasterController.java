package com.aria.conveyor.controller;

import com.aria.conveyor.service.SkuMasterLookupService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code GET /api/conveyor/skumaster?barcode=...} — tokenless (permit-all, same
 * {@code /api/conveyor/**} convention as {@link ConveyorScanController}/{@link
 * ConveyorHeartbeatController}) barcode→SKU lookup the device itself can call, so it can
 * identify the right SKU — and from that, the right Dispatch Task — locally, BEFORE deciding
 * whether to submit a scan at all. Same resolution {@code POST /api/conveyor/scan} already
 * runs server-side as its own authoritative check ({@link SkuMasterLookupService}); this
 * endpoint exists purely so the device's local qty pre-check
 * ({@code raspberry-pi-client/conveyor_client.py}'s {@code resolve_sku}) can work per-SKU
 * instead of only when exactly one Dispatch Task is Open on the whole bay.
 * {@code skuDescription}/{@code boxQty} ride along for device-side display/logging only.
 */
@RestController
public class ConveyorSkuMasterController {

    private final SkuMasterLookupService lookup;

    public ConveyorSkuMasterController(SkuMasterLookupService lookup) {
        this.lookup = lookup;
    }

    @GetMapping("/api/conveyor/skumaster")
    @Operation(summary = "Resolve a scanned barcode to its SKU master row (sku_code, "
            + "skuDescription, boxQty, status). found:false when no m5_sku_master row matches "
            + "barcode or sku_code.")
    public Map<String, Object> resolve(@RequestParam String barcode) {
        return lookup.resolveByBarcode(barcode)
                .<Map<String, Object>>map(row -> {
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("found", true);
                    out.put("skuCode", row.get("sku_code"));
                    out.put("skuDescription", row.get("sku_name"));
                    out.put("boxQty", row.get("pack_pieces_per_case"));
                    out.put("status", row.get("status"));
                    return out;
                })
                .orElseGet(() -> Map.of("found", false));
    }
}
