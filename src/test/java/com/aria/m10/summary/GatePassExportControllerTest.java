package com.aria.m10.summary;

import com.aria.security.TenantContext;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Unit test for {@link GatePassExportController}: the Excel file with every part of the matching gate passes. */
class GatePassExportControllerTest {

    private JdbcTemplate jdbc;
    private GatePassExportController controller;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        controller = new GatePassExportController(jdbc);
        TenantContext.set(1L);
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
        when(jdbc.queryForList(anyString())).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    private void gatePasses(Map<String, Object>... rows) {
        when(jdbc.queryForList(startsWith("SELECT gp.id"), any(Object[].class))).thenReturn(List.of(rows));
    }

    private Workbook open(ResponseEntity<?> r) throws Exception {
        assertThat(r.getStatusCode().value()).isEqualTo(200);
        return new XSSFWorkbook(new ByteArrayInputStream((byte[]) r.getBody()));
    }

    private static String cell(Sheet s, int row, int col) {
        var c = s.getRow(row).getCell(col);
        if (c == null) return null;
        return c.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC ? String.valueOf(c.getNumericCellValue()) : c.getStringCellValue();
    }

    @Test
    void needsAGatePassVehicleOrTransport_aDateRangeAloneIsRefused() {
        ResponseEntity<?> r = controller.export(null, " ", null, "2026-10-01", "2026-10-20");
        assertThat(r.getStatusCode().value()).isEqualTo(400);
        verifyNoInteractions(jdbc);
    }

    @Test
    void badDates_areRefused() {
        assertThat(controller.export(null, "TN1", null, "01-10-2026", null).getStatusCode().value()).isEqualTo(400);
        assertThat(controller.export(null, "TN1", null, "2026-10-20", "2026-10-01").getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void theFileHasAllTheSheets_andTheGatePassRowComesFirst() throws Exception {
        Map<String, Object> g = row("id", 1L, "gp_number", "GP-1-54", "workflow_instance_id", 2040L, "vehicle_no", "TN23J1144",
                "transport_name", "Sri Murugan", "status", "CLOSED", "w1_weight_kg", new BigDecimal("18200.000"),
                "w2_weight_kg", new BigDecimal("8200.000"), "net_weight_kg", new BigDecimal("10000.000"),
                "created_at", Timestamp.valueOf("2026-10-10 09:00:00"));
        gatePasses(g);

        ResponseEntity<?> r = controller.export(null, "TN23J1144", null, null, null);

        assertThat(r.getHeaders().getFirst("Content-Disposition")).contains("attachment").contains("gate-pass-report.xlsx");
        try (Workbook wb = open(r)) {
            List<String> names = new ArrayList<>();
            wb.forEach(s -> names.add(s.getSheetName()));
            assertThat(names).containsExactly("Info", "Gate Passes", "Journey", "Loading", "Unloading", "Sample Vehicles", "Stock");
            Sheet sheet = wb.getSheet("Gate Passes");
            assertThat(cell(sheet, 0, 0)).isEqualTo("gp_number");
            assertThat(cell(sheet, 1, 0)).isEqualTo("GP-1-54");
            assertThat(cell(sheet, 1, 1)).isEqualTo("2026-10-10 09:00:00");
            assertThat(cell(sheet, 1, 3)).isEqualTo("TN23J1144");
            assertThat(cell(sheet, 1, 12)).isEqualTo("10000.0");   // net weight is a number, not text
        }
    }

    @Test
    void theInfoSheetSaysWhatWasAskedFor_andHowMany() throws Exception {
        gatePasses(row("id", 1L, "gp_number", "GP-1", "workflow_instance_id", 5L));

        try (Workbook wb = open(controller.export(null, "TN1", "Murugan", "2026-10-01", "2026-10-31"))) {
            Sheet info = wb.getSheet("Info");
            assertThat(cell(info, 2, 1)).isEqualTo("TN1");
            assertThat(cell(info, 3, 1)).isEqualTo("Murugan");
            assertThat(cell(info, 6, 1)).isEqualTo("1");
            assertThat(cell(info, 7, 1)).isEqualTo("no");
        }
    }

    @Test
    void moreGatePassesThanTheLimit_areCut_andTheInfoSheetSaysSo() throws Exception {
        List<Map<String, Object>> many = new ArrayList<>();
        for (int i = 0; i < GatePassExportController.MAX_GATE_PASSES + 1; i++) many.add(row("id", (long) i, "gp_number", "GP-" + i));
        when(jdbc.queryForList(startsWith("SELECT gp.id"), any(Object[].class))).thenReturn(many);

        try (Workbook wb = open(controller.export(null, "TN1", null, null, null))) {
            assertThat(wb.getSheet("Gate Passes").getLastRowNum()).isEqualTo(GatePassExportController.MAX_GATE_PASSES);
            assertThat(cell(wb.getSheet("Info"), 7, 1)).startsWith("YES");
        }
    }

    @Test
    void theDetailSheets_carryTheGatePassNumberInFront() throws Exception {
        gatePasses(row("id", 1L, "gp_number", "GP-1-54", "workflow_instance_id", 2040L));
        when(jdbc.queryForList(contains("FROM loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(row("workflow_instance_id", 2040L, "bay_name", "COT-1", "processed_quantity", new BigDecimal("3000.000"),
                        "unloaded_quantity", new BigDecimal("1000.000"), "status", "DONE")));
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(row("workflow_instance_id", 2040L, "bay_name", "ROT-9", "approved_status", "REBATE", "rebate_value", "45.5")));
        when(jdbc.queryForList(contains("FROM m5_stock_movement"), any(Object[].class)))
                .thenReturn(List.of(row("ref_id", 2040L, "kind", "RETURN", "from_bin", "COT-1", "to_bin", "ROT-9", "qty", new BigDecimal("1000"))));

        try (Workbook wb = open(controller.export("GP-1-54", null, null, null, null))) {
            Sheet loading = wb.getSheet("Loading");
            assertThat(cell(loading, 0, 0)).isEqualTo("gp_number");
            assertThat(cell(loading, 1, 0)).isEqualTo("GP-1-54");
            assertThat(cell(loading, 0, 6)).isEqualTo("bay_name");
            assertThat(cell(loading, 1, 6)).isEqualTo("COT-1");
            Sheet unloading = wb.getSheet("Unloading");
            assertThat(cell(unloading, 1, 0)).isEqualTo("GP-1-54");
            assertThat(cell(unloading, 1, 6)).isEqualTo("ROT-9");
            assertThat(cell(wb.getSheet("Stock"), 1, 1)).isEqualTo("RETURN");
        }
    }

    @Test
    void theJourney_hasStepsWho_minutes_andWhatThePageSaved() throws Exception {
        gatePasses(row("id", 1L, "gp_number", "GP-1-54", "workflow_instance_id", 2040L));
        when(jdbc.queryForList(startsWith("SELECT page_code, table_name"))).thenReturn(List.of(
                row("page_code", "aria:weighment-capture", "table_name", "weighment_capture_detail", "key_column", "workflow_instance_id")));
        when(jdbc.queryForList(contains("FROM workflow_event"), any(Object[].class))).thenReturn(List.of(
                row("instance_id", 2040L, "ts", Timestamp.valueOf("2026-10-10 09:00:00"), "to_state", "DISPATCH_QUEUED"),
                row("instance_id", 2040L, "ts", Timestamp.valueOf("2026-10-10 09:38:00"), "from_state", "DISPATCH_QUEUED", "to_state", "WEIGHMENT_1",
                        "page_code", "aria:weighment-capture", "actor_user_id", 8L)));
        when(jdbc.queryForList(startsWith("SELECT * FROM weighment_capture_detail"), any(Object[].class))).thenReturn(List.of(
                row("id", 3L, "workflow_instance_id", 2040L, "w1_weight_kg", new BigDecimal("18200.000"), "weight_officer", "Suresh",
                        "performed_by_name", "Suresh", "notes", "", "created_at", Timestamp.valueOf("2026-10-10 09:38:00"))));

        try (Workbook wb = open(controller.export("GP-1-54", null, null, null, null))) {
            Sheet j = wb.getSheet("Journey");
            assertThat(cell(j, 1, 0)).isEqualTo("GP-1-54");
            assertThat(cell(j, 1, 1)).isEqualTo("1.0");
            assertThat(cell(j, 2, 1)).isEqualTo("2.0");
            assertThat(cell(j, 2, 5)).isEqualTo("aria:weighment-capture");
            assertThat(cell(j, 2, 6)).isEqualTo("Suresh");
            assertThat(cell(j, 2, 8)).isEqualTo("38.0");                 // minutes since the step before
            String data = cell(j, 2, 9);
            assertThat(data).contains("w1_weight_kg=18200").contains("weight_officer=Suresh")
                    .doesNotContain("created_at").doesNotContain("id=").doesNotContain("notes");   // no ids, timestamps or empty values
            assertThat(cell(j, 1, 9)).isEmpty();                        // a step with no page has no data
        }
    }

    @Test
    void aTableNameThatIsNotAPlainName_isNeverQueried() throws Exception {
        gatePasses(row("id", 1L, "gp_number", "GP-1", "workflow_instance_id", 5L));
        when(jdbc.queryForList(startsWith("SELECT page_code, table_name"))).thenReturn(List.of(
                row("page_code", "aria:x", "table_name", "gate_pass; DROP TABLE x", "key_column", "workflow_instance_id")));

        controller.export("GP-1", null, null, null, null);

        verify(jdbc, never()).queryForList(contains("DROP TABLE"), any(Object[].class));
    }

    @Test
    void aTableThatCannotBeRead_leavesItsSheetEmpty_andTheRestStillComes() throws Exception {
        gatePasses(row("id", 1L, "gp_number", "GP-1", "workflow_instance_id", 5L));
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenThrow(new org.springframework.jdbc.BadSqlGrammarException("x", "SELECT", new java.sql.SQLException("no column")));

        try (Workbook wb = open(controller.export("GP-1", null, null, null, null))) {
            assertThat(wb.getSheet("Unloading").getLastRowNum()).isEqualTo(0);   // header only
            assertThat(cell(wb.getSheet("Gate Passes"), 1, 0)).isEqualTo("GP-1");
        }
    }

    @Test
    void aTextCellThatLooksLikeAFormula_isKeptAsText() throws Exception {
        gatePasses(row("id", 1L, "gp_number", "GP-1", "workflow_instance_id", 5L, "vehicle_no", "=HYPERLINK(\"x\")"));

        try (Workbook wb = open(controller.export("GP-1", null, null, null, null))) {
            assertThat(cell(wb.getSheet("Gate Passes"), 1, 3)).startsWith("'=");
        }
    }

    @Test
    void filtersAreBindParameters_andTheLimitIsOneMoreThanTheCap() {
        controller.export("GP-1-54", "tn-23 j1144", "Murugan", "2026-10-01", "2026-10-20");

        verify(jdbc).queryForList(startsWith("SELECT gp.id"), eq(1L), eq("GP-1-54"), eq("%TN23J1144%"), eq("%Murugan%"),
                eq(Timestamp.valueOf("2026-10-01 00:00:00")), eq(Timestamp.valueOf("2026-10-21 00:00:00")), eq(1001));
    }

    @Test
    void itOnlyEverReads() {
        controller.export("GP-1", null, null, null, null);
        verify(jdbc, never()).update(anyString(), any(Object[].class));
        verify(jdbc, never()).update(anyString());
    }
}
