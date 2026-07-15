package com.aria.m0.pivot;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Server-side aggregation endpoint backing the PivotTable primitive.
 *
 * <p>Endpoint:
 * <pre>
 * POST /api/m0/pivot/query
 *   body: {
 *     baseEntity: "StockBalance",
 *     rowFields: ["item_code"],
 *     colFields: ["bin_code"],
 *     measureField: "qty_on_hand",
 *     aggKind: "SUM" (SUM | COUNT | AVG | MIN | MAX),
 *     filter: { and: [{field, op, value}, ...] }   // optional
 *   }
 *   -> { rows: [...], cols: [...], cells: {"rowKey|colKey": value}, grand: number }
 * </pre>
 *
 * <p>Looks up entity_registry to get the actual table name, builds a
 * <code>SELECT rowFields, colFields, AGG(measure) FROM table WHERE ... GROUP BY ...</code>
 * query and rearranges to a row×col cell map.
 */
@RestController
@RequestMapping("/api/m0/pivot")
public class PivotController {

    private final JdbcTemplate jdbc;
    private static final Set<String> ALLOWED_AGGS = Set.of("SUM", "COUNT", "AVG", "MIN", "MAX");

    public PivotController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/query")
    public Map<String, Object> query(@RequestBody Map<String, Object> req) {
        String entity = String.valueOf(req.get("baseEntity"));
        List<String> rowFields = listOf(req.get("rowFields"));
        List<String> colFields = listOf(req.get("colFields"));
        String measure = String.valueOf(req.getOrDefault("measureField", "id"));
        String aggKind = String.valueOf(req.getOrDefault("aggKind", "SUM")).toUpperCase();
        if (!ALLOWED_AGGS.contains(aggKind)) aggKind = "SUM";
        long tenantId = req.containsKey("tenantId") ? ((Number) req.get("tenantId")).longValue() : 1L;

        String table;
        try {
            table = jdbc.queryForObject(
                    "SELECT table_name FROM entity_registry WHERE code=?",
                    String.class, entity);
        } catch (Exception e) {
            return Map.of("error", "Unknown entity " + entity);
        }
        if (!table.matches("[a-zA-Z0-9_]+")) {
            return Map.of("error", "Bad table name");
        }
        // Sanitize field names (allow alpha + underscore + DATE(...) wrapper)
        List<String> safeRowFields = rowFields.stream().map(this::sanitizeField).collect(Collectors.toList());
        List<String> safeColFields = colFields.stream().map(this::sanitizeField).collect(Collectors.toList());
        String safeMeasure = sanitizeField(measure);

        List<String> groupCols = new ArrayList<>();
        groupCols.addAll(safeRowFields);
        groupCols.addAll(safeColFields);
        if (groupCols.isEmpty()) groupCols.add("1");

        String selectExpr = String.join(", ", groupCols) + ", " +
                (aggKind.equals("COUNT") ? "COUNT(*)" : aggKind + "(" + safeMeasure + ")") +
                " AS measure_value";
        String groupBy = String.join(", ", groupCols);
        String sql = "SELECT " + selectExpr + " FROM " + table +
                " WHERE tenant_id=?" +
                " GROUP BY " + groupBy +
                " ORDER BY " + groupBy +
                " LIMIT 1000";

        List<Map<String, Object>> raw = jdbc.queryForList(sql, tenantId);

        Set<String> rowKeys = new LinkedHashSet<>();
        Set<String> colKeys = new LinkedHashSet<>();
        Map<String, Object> cells = new LinkedHashMap<>();
        double grand = 0;
        for (Map<String, Object> r : raw) {
            String rk = safeRowFields.stream()
                    .map(f -> String.valueOf(r.get(stripWrappers(f))))
                    .collect(Collectors.joining("|"));
            String ck = safeColFields.stream()
                    .map(f -> String.valueOf(r.get(stripWrappers(f))))
                    .collect(Collectors.joining("|"));
            if (rk.isEmpty()) rk = "(all)";
            if (ck.isEmpty()) ck = "(measure)";
            rowKeys.add(rk);
            colKeys.add(ck);
            Object v = r.get("measure_value");
            cells.put(rk + "|" + ck, v);
            if (v instanceof Number) grand += ((Number) v).doubleValue();
        }
        return Map.of(
                "rowFields", rowFields,
                "colFields", colFields,
                "rows", new ArrayList<>(rowKeys),
                "cols", new ArrayList<>(colKeys),
                "cells", cells,
                "grand", grand,
                "raw", raw);
    }

    @SuppressWarnings("unchecked")
    private List<String> listOf(Object o) {
        if (o == null) return List.of();
        if (o instanceof List<?>) return (List<String>) o;
        return List.of(String.valueOf(o));
    }

    /** Allow word characters + DATE() wrapper; strip anything else. */
    private String sanitizeField(String f) {
        if (f == null) return "id";
        f = f.trim();
        if (f.matches("(?i)DATE\\(\\s*\\w+\\s*\\)")) return f.toUpperCase().replace(" ", "");
        return f.replaceAll("[^a-zA-Z0-9_]", "");
    }

    private String stripWrappers(String f) {
        // For "DATE(ts)" the result map column is "DATE(ts)" — keep as-is.
        return f;
    }
}
