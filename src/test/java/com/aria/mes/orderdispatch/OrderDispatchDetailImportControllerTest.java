package com.aria.mes.orderdispatch;

import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link OrderDispatchDetailImportController#importCsv} —
 * focused on the multi-file-per-request behavior: several route/order
 * sheets picked and uploaded together under the same {@code file} form
 * field, each imported and reported independently.
 */
@SuppressWarnings("unchecked")
class OrderDispatchDetailImportControllerTest {

    private JdbcTemplate jdbc;
    private OrderDispatchDetailImportController controller;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        controller = new OrderDispatchDetailImportController(jdbc);
        TenantContext.set(1L);
        GrantedAuthority dispatchRole = new SimpleGrantedAuthority("dispatch");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tester", "n/a", List.of(dispatchRole)));
        // blockReplaceReason's two COUNT queries -- 0 means "not blocked, safe to replace".
        when(jdbc.queryForObject(contains("loading_progress_detail"), eq(Integer.class), any(Object[].class)))
                .thenReturn(0);
        when(jdbc.queryForObject(contains("m5_order_dispatch_detail"), eq(Integer.class), any(Object[].class)))
                .thenReturn(0);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void noFiles_errors() {
        ResponseEntity<?> r = controller.importCsv(new MultipartFile[0]);
        assertThat(r.getStatusCode().value()).isEqualTo(400);
        assertThat((Map<String, Object>) r.getBody()).containsEntry("error", "no file");
    }

    @Test
    void singleFile_stillWorks_backwardCompatible() {
        MultipartFile f1 = csvFile("route1.csv", csvRow("ROU1", "10", "TN01AA1111"));

        ResponseEntity<?> r = controller.importCsv(new MultipartFile[]{f1});

        Map<String, Object> body = (Map<String, Object>) r.getBody();
        assertThat(body).containsEntry("ok", true).containsEntry("filesProcessed", 1)
                .containsEntry("rowsImported", 1).containsEntry("rowsFailed", 0);
        verify(jdbc, times(1)).update(startsWith("INSERT INTO m5_order_dispatch_detail"), any(Object[].class));
    }

    @Test
    void twoFiles_bothImported_perFileBreakdownGiven() {
        MultipartFile f1 = csvFile("route1.csv", csvRow("ROU1", "10", "TN01AA1111"));
        MultipartFile f2 = csvFile("route2.csv", csvRow("ROU2", "20", "TN01AA2222"));

        ResponseEntity<?> r = controller.importCsv(new MultipartFile[]{f1, f2});

        Map<String, Object> body = (Map<String, Object>) r.getBody();
        assertThat(body).containsEntry("ok", true).containsEntry("filesProcessed", 2)
                .containsEntry("rowsImported", 2).containsEntry("rowsFailed", 0);
        List<Map<String, Object>> perFile = (List<Map<String, Object>>) (List<?>) body.get("perFile");
        assertThat(perFile).hasSize(2);
        assertThat(perFile.get(0)).containsEntry("file", "route1.csv").containsEntry("rowsImported", 1);
        assertThat(perFile.get(1)).containsEntry("file", "route2.csv").containsEntry("rowsImported", 1);
        verify(jdbc, times(2)).update(startsWith("INSERT INTO m5_order_dispatch_detail"), any(Object[].class));
    }

    @Test
    void oneFileBad_otherGood_isolatedPerFile() {
        // Missing assign_qty (column 26) on its only data row -> that file's
        // row fails, but the other file must still import cleanly.
        MultipartFile bad = csvFile("bad.csv", csvRow("ROU1", null, "TN01AA1111"));
        MultipartFile good = csvFile("good.csv", csvRow("ROU2", "20", "TN01AA2222"));

        ResponseEntity<?> r = controller.importCsv(new MultipartFile[]{bad, good});

        Map<String, Object> body = (Map<String, Object>) r.getBody();
        assertThat(body).containsEntry("ok", true).containsEntry("filesProcessed", 2)
                .containsEntry("rowsImported", 1).containsEntry("rowsFailed", 1);
        List<Map<String, Object>> perFile = (List<Map<String, Object>>) (List<?>) body.get("perFile");
        assertThat(perFile.get(0)).containsEntry("file", "bad.csv").containsEntry("rowsImported", 0).containsEntry("rowsFailed", 1);
        assertThat(perFile.get(1)).containsEntry("file", "good.csv").containsEntry("rowsImported", 1).containsEntry("rowsFailed", 0);
    }

    @Test
    void sameRouteAcrossTwoFiles_secondFileReusesFirstFilesDecision() {
        // Both files carry route ROU1 for the SAME vehicle -- blockReplaceReason
        // should only be queried once (shared clearedRoutes across the request),
        // and the DELETE for that route should only fire once too.
        MultipartFile f1 = csvFile("route1a.csv", csvRow("ROU1", "10", "TN01AA1111"));
        MultipartFile f2 = csvFile("route1b.csv", csvRow("ROU1", "15", "TN01AA1111"));
        when(jdbc.update(startsWith("DELETE FROM m5_order_dispatch_detail"), any(Object[].class))).thenReturn(3);

        ResponseEntity<?> r = controller.importCsv(new MultipartFile[]{f1, f2});

        Map<String, Object> body = (Map<String, Object>) r.getBody();
        assertThat(body).containsEntry("rowsImported", 2).containsEntry("rowsReplaced", 3);
        verify(jdbc, times(1)).update(startsWith("DELETE FROM m5_order_dispatch_detail"), any(Object[].class));
    }

    @Test
    void tankerExtraColumns_35to40_areImported() {
        String[] hdr = new String[41];
        String[] row = new String[41];
        for (int i = 0; i < 41; i++) { hdr[i] = "col" + i; row[i] = ""; }
        row[24] = "ROU1"; row[26] = "10"; row[28] = "TN01AA1111";
        row[35] = "PO-EXEC"; row[36] = "CREDIT"; row[37] = "TANKER"; row[38] = "05/10/2026 14:30"; row[39] = "handle with care"; row[40] = "18.5";
        MultipartFile f = new MockMultipartFile("file", "tanker.csv", "text/csv",
                (String.join(",", hdr) + "\n" + String.join(",", row) + "\n").getBytes(StandardCharsets.UTF_8));

        ResponseEntity<?> r = controller.importCsv(new MultipartFile[]{f});

        assertThat((Map<String, Object>) r.getBody()).containsEntry("rowsImported", 1).containsEntry("rowsFailed", 0);
        Object[] args = insertArgs();
        assertThat(args).hasSize(45);
        assertThat(args[39]).isEqualTo("PO-EXEC");
        assertThat(args[40]).isEqualTo("CREDIT");
        assertThat(args[41]).isEqualTo("TANKER");
        assertThat(args[42]).isEqualTo(java.time.LocalDateTime.of(2026, 10, 5, 14, 30));
        assertThat(args[43]).isEqualTo("handle with care");
        assertThat(args[44]).isEqualTo(new java.math.BigDecimal("18.5"));
    }

    /** Args (varargs already expanded) of the single INSERT INTO m5_order_dispatch_detail call. */
    private Object[] insertArgs() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update")
                        && i.getArguments().length > 0
                        && String.valueOf(i.getArguments()[0]).startsWith("INSERT INTO m5_order_dispatch_detail"))
                .findFirst().orElseThrow()
                .getArguments();
    }

    @Test
    void plain35ColumnSheet_leavesTankerExtrasNull() {
        MultipartFile f = csvFile("plain.csv", csvRow("ROU1", "10", "TN01AA1111"));

        controller.importCsv(new MultipartFile[]{f});

        Object[] args = insertArgs();
        assertThat(args).hasSize(45);
        for (int i = 39; i <= 44; i++) assertThat(args[i]).isNull();
    }

    // ---- deleteActive ---------------------------------------------------

    @Test
    void deleteActive_missingVehicleNo_errors() {
        ResponseEntity<?> r = controller.deleteActive(Map.of());

        assertThat(r.getStatusCode().value()).isEqualTo(400);
        assertThat((Map<String, Object>) r.getBody()).containsEntry("error", "vehicleNo required");
        verify(jdbc, never()).update(startsWith("DELETE FROM m5_order_dispatch_detail"), any(Object[].class));
    }

    @Test
    void deleteActive_noLoadingStarted_hardDeletes() {
        when(jdbc.queryForList(contains("SELECT DISTINCT route_id"), eq(String.class), eq(1L), eq("TN01AA1111")))
                .thenReturn(List.of("ROU1", "ROU2"));
        // Both routes' loading-started check -> 0 (safe).
        when(jdbc.queryForObject(contains("loading_progress_detail"), eq(Integer.class), any(Object[].class)))
                .thenReturn(0);
        when(jdbc.update(startsWith("DELETE FROM m5_order_dispatch_detail"), eq(1L), eq("TN01AA1111")))
                .thenReturn(5);

        ResponseEntity<?> r = controller.deleteActive(Map.of("vehicleNo", "TN01AA1111"));

        Map<String, Object> body = (Map<String, Object>) r.getBody();
        assertThat(body).containsEntry("ok", true).containsEntry("vehicleNo", "TN01AA1111").containsEntry("rowsDeleted", 5);
        verify(jdbc).update(eq("DELETE FROM m5_order_dispatch_detail WHERE tenant_id = ? AND vehicle_no = ? AND vehicle_status = 'active'"),
                eq(1L), eq("TN01AA1111"));
    }

    @Test
    void deleteActive_loadingAlreadyStarted_blockedNoDelete() {
        when(jdbc.queryForList(contains("SELECT DISTINCT route_id"), eq(String.class), eq(1L), eq("TN01AA1111")))
                .thenReturn(List.of("ROU1"));
        when(jdbc.queryForObject(contains("loading_progress_detail"), eq(Integer.class), any(Object[].class)))
                .thenReturn(1); // loading already started for ROU1

        ResponseEntity<?> r = controller.deleteActive(Map.of("vehicleNo", "TN01AA1111"));

        assertThat(r.getStatusCode().value()).isEqualTo(409);
        Map<String, Object> body = (Map<String, Object>) r.getBody();
        assertThat(body).containsEntry("ok", false);
        assertThat((String) body.get("error")).contains("loading has already started").contains("ROU1");
        verify(jdbc, never()).update(startsWith("DELETE FROM m5_order_dispatch_detail"), any(Object[].class));
    }

    @Test
    void deleteActive_noActiveRowsForVehicle_deletesZeroHarmlessly() {
        when(jdbc.queryForList(contains("SELECT DISTINCT route_id"), eq(String.class), eq(1L), eq("TN01AA9999")))
                .thenReturn(List.of());
        when(jdbc.update(startsWith("DELETE FROM m5_order_dispatch_detail"), eq(1L), eq("TN01AA9999")))
                .thenReturn(0);

        ResponseEntity<?> r = controller.deleteActive(Map.of("vehicleNo", "TN01AA9999"));

        Map<String, Object> body = (Map<String, Object>) r.getBody();
        assertThat(body).containsEntry("ok", true).containsEntry("rowsDeleted", 0);
    }

    private static MockMultipartFile csvFile(String name, String dataRow) {
        // Header must carry real (non-blank) text -- parseCsvRows drops any
        // all-blank row outright (see its own comment on that), which would
        // silently swallow an all-empty placeholder header here and leave
        // the real data row mistaken for the header instead.
        String[] headerCols = new String[35];
        for (int i = 0; i < 35; i++) headerCols[i] = "col" + i;
        String header = String.join(",", headerCols);
        String content = header + "\n" + dataRow + "\n";
        return new MockMultipartFile("file", name, "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    /** Builds a 35-column CSV row with route_id (col 24), assign_qty (col 26), vehicle_no (col 28) set. */
    private static String csvRow(String routeId, String assignQty, String vehicleNo) {
        String[] cols = new String[35];
        java.util.Arrays.fill(cols, "");
        cols[24] = routeId == null ? "" : routeId;
        cols[26] = assignQty == null ? "" : assignQty;
        cols[28] = vehicleNo == null ? "" : vehicleNo;
        return String.join(",", cols);
    }
}
