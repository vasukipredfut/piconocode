package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.datasource.DataSourceQueryService;
import com.aria.datasource.DataSourceSpec;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.*;

/** {@code export.csv} — fetches up to 10 000 rows via the DataSource
 *  compiler and returns CSV text (base64-friendly) for the frontend to
 *  trigger a download. Page size honoured per request; default 1 000. */
@Component
public class ExportCsvActionHandler implements ActionHandler {

    private static final int MAX_ROWS = 10_000;

    @Autowired private DataSourceQueryService ds;

    @Override public String code() { return "export.csv"; }

    @Override public ActionResult handle(ActionInvocation inv) {
        DataSourceSpec spec = inv.getSpec();
        if (spec == null) throw new IllegalArgumentException("spec required for export.csv");
        DataSourceSpec.Response r = ds.query(spec, 0, MAX_ROWS,
                inv.getContext() == null ? Map.of() : inv.getContext());
        List<Map<String, Object>> rows = r.getContent();
        if (rows.isEmpty()) {
            return ActionResult.builder().ok(true).message("empty")
                    .data(Map.of("csv", "", "rowCount", 0))
                    .build();
        }
        // Compute headers — use the order of keys in the first row.
        List<String> headers = new ArrayList<>(rows.get(0).keySet());
        StringWriter sw = new StringWriter();
        try (CSVPrinter p = new CSVPrinter(sw,
                CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])))) {
            for (Map<String, Object> row : rows) {
                List<Object> values = new ArrayList<>(headers.size());
                for (String h : headers) values.add(row.get(h));
                p.printRecord(values);
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("CSV writer failed", e);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("csv", sw.toString());
        data.put("rowCount", rows.size());
        data.put("filename", "export-" + System.currentTimeMillis() + ".csv");
        return ActionResult.builder().ok(true).message("exported").data(data).build();
    }
}
