package com.aria.mes.quality;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quality algorithm — computes a 0-100 quality score for a stream of
 * measurements (typically weighbridge readings) using a moving-average
 * tolerance check. Scoring rule:
 * <pre>
 *   for each sample s in last N readings:
 *     deviation = |s - moving_average(window)|
 *     in-tol    = deviation <= toleranceKg
 *   score = (count(in-tol) / count) * 100
 * </pre>
 *
 * <p>Also exposes a REST endpoint that runs the algorithm against the
 * stock-movement ledger (filtering by item or bin) so the {@code pico:rpt-quality}
 * page's "Run Algorithm" action has a real backend to call.
 */
@Service
@RestController
@RequestMapping("/api/m5/reports")
public class QualityAlgorithmService {

    private final JdbcTemplate jdbc;

    public QualityAlgorithmService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Run the moving-average-tolerance algorithm. Returns score + flagged
     * sample indices.
     */
    public ScoreResult qualityScore(List<BigDecimal> series, int windowSize, BigDecimal toleranceKg) {
        if (series == null || series.isEmpty()) {
            return new ScoreResult(BigDecimal.ZERO, 0, 0, List.of());
        }
        int total = series.size();
        int flagged = 0;
        java.util.ArrayList<Integer> flaggedIdx = new java.util.ArrayList<>();
        for (int i = 0; i < total; i++) {
            int from = Math.max(0, i - windowSize + 1);
            BigDecimal avg = movingAvg(series, from, i + 1);
            BigDecimal dev = series.get(i).subtract(avg).abs();
            if (dev.compareTo(toleranceKg) > 0) {
                flagged++;
                flaggedIdx.add(i);
            }
        }
        BigDecimal score = BigDecimal.valueOf(total - flagged)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return new ScoreResult(score, total, flagged, flaggedIdx);
    }

    private BigDecimal movingAvg(List<BigDecimal> series, int from, int toExclusive) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = from; i < toExclusive; i++) sum = sum.add(series.get(i));
        return sum.divide(BigDecimal.valueOf(toExclusive - from), 4, RoundingMode.HALF_UP);
    }

    /**
     * REST: run quality algorithm against stock-movement qty values.
     */
    @GetMapping("/quality-score")
    public Map<String, Object> runQualityScore(
            @RequestParam(required = false) String itemCode,
            @RequestParam(defaultValue = "100") int sampleSize,
            @RequestParam(defaultValue = "50") BigDecimal toleranceKg,
            @RequestParam(defaultValue = "1") long tenantId) {
        String sql = "SELECT qty FROM m5_stock_movement WHERE tenant_id=?" +
                (itemCode != null ? " AND item_code=?" : "") +
                " ORDER BY ts DESC LIMIT " + sampleSize;
        List<BigDecimal> series = itemCode != null
                ? jdbc.queryForList(sql, BigDecimal.class, tenantId, itemCode)
                : jdbc.queryForList(sql, BigDecimal.class, tenantId);
        ScoreResult r = qualityScore(series, Math.min(10, sampleSize), toleranceKg);
        Map<String, Object> out = new HashMap<>();
        out.put("algorithm", "moving-avg-tolerance");
        out.put("itemCode", itemCode);
        out.put("sampleSize", series.size());
        out.put("toleranceKg", toleranceKg);
        out.put("score", r.score());
        out.put("total", r.total());
        out.put("flagged", r.flagged());
        out.put("flaggedIndices", r.flaggedIndices());
        return out;
    }

    public record ScoreResult(BigDecimal score, int total, int flagged, List<Integer> flaggedIndices) { }
}
