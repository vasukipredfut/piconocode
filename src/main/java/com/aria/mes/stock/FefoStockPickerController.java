package com.aria.mes.stock;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST entry-point for {@link FefoStockPickerService}. Used by the M5 MR/JC/
 * FGDispatch forms to populate the "suggested lot" field client-side before
 * a workflow transition fires.
 *
 * <p>Endpoint:
 * <pre>
 * GET /api/m5/stock/fefo-pick?itemCode=OIL-SUN-RAW&qty=5000&bin=T-OIL-03
 *   -> { picks: [{itemCode, lotNo, binCode, qty, uom}], residual: 0 }
 * </pre>
 */
@RestController
@RequestMapping("/api/m5/stock")
public class FefoStockPickerController {

    private final FefoStockPickerService picker;

    public FefoStockPickerController(FefoStockPickerService picker) {
        this.picker = picker;
    }

    @GetMapping("/fefo-pick")
    public Map<String, Object> fefoPick(
            @RequestParam String itemCode,
            @RequestParam BigDecimal qty,
            @RequestParam(required = false) String bin,
            @RequestParam(defaultValue = "1") long tenantId) {
        List<FefoStockPickerService.FefoPick> picks = picker.pick(tenantId, itemCode, bin, qty);
        BigDecimal residual = picker.residualQty(qty, picks);
        return Map.of(
                "itemCode", itemCode,
                "requestedQty", qty,
                "picks", picks,
                "residual", residual,
                "fullyCovered", residual.compareTo(BigDecimal.ZERO) == 0);
    }
}
