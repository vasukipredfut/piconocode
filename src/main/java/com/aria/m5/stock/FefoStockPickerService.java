package com.aria.m5.stock;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FEFO (first-expire-first-out) lot picker. Picks the OLDEST available lot
 * for a given item-code + bin (or across bins) to issue/consume.
 *
 * <p>Used by MR.issue + JC.consume + FGDispatch.pick-bin to suggest a lot
 * that minimizes expiry risk. The algorithm walks {@code m5_stock_balance}
 * ordered by last_movement_at ascending (proxy for receipt date) and
 * returns lots until the requested qty is covered.
 *
 * <p>Returns a {@link FefoPick} for each lot consumed plus a residual qty
 * if the request couldn't be fully covered (caller decides whether to
 * partially-issue or fail).
 */
@Service
public class FefoStockPickerService {

    private final JdbcTemplate jdbc;

    public FefoStockPickerService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Pick lots for {@code itemCode} totalling {@code requestedQty}. When
     * {@code binCode} is non-null, restrict to that bin. Returns up to
     * N picks (one per lot consumed) ordered by oldest-first.
     */
    public List<FefoPick> pick(long tenantId, String itemCode, String binCode, BigDecimal requestedQty) {
        if (itemCode == null || requestedQty == null || requestedQty.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        return pickFromRows(itemCode, findAvailableLots(tenantId, itemCode, binCode), requestedQty);
    }

    /** Hook for tests: returns raw available-lot rows ordered oldest-first. */
    protected List<Map<String, Object>> findAvailableLots(long tenantId, String itemCode, String binCode) {
        String sql = "SELECT lot_no, bin_code, uom, qty_on_hand, last_movement_at " +
                "FROM m5_stock_balance " +
                "WHERE tenant_id=? AND item_code=? AND qty_on_hand > 0" +
                (binCode != null ? " AND bin_code=?" : "") +
                " ORDER BY COALESCE(last_movement_at, '1970-01-01') ASC, id ASC";
        return binCode != null
                ? jdbc.queryForList(sql, tenantId, itemCode, binCode)
                : jdbc.queryForList(sql, tenantId, itemCode);
    }

    /** Pure-function picker: walk rows oldest-first, take qty until satisfied. */
    static List<FefoPick> pickFromRows(String itemCode, List<Map<String, Object>> rows, BigDecimal requestedQty) {
        BigDecimal remaining = requestedQty;
        List<FefoPick> picks = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal avail = (BigDecimal) r.get("qty_on_hand");
            BigDecimal pickQty = avail.compareTo(remaining) >= 0 ? remaining : avail;
            picks.add(new FefoPick(
                    itemCode,
                    (String) r.get("lot_no"),
                    (String) r.get("bin_code"),
                    pickQty,
                    (String) r.get("uom")));
            remaining = remaining.subtract(pickQty);
        }
        return picks;
    }

    /** Residual = requested − sum of picked qty. Zero or positive (under-pick). */
    public BigDecimal residualQty(BigDecimal requestedQty, List<FefoPick> picks) {
        BigDecimal picked = picks.stream()
                .map(FefoPick::qty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal residual = requestedQty.subtract(picked);
        return residual.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : residual;
    }

    public record FefoPick(String itemCode, String lotNo, String binCode,
                            BigDecimal qty, String uom) { }
}
