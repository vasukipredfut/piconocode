package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.datasource.DataSourceQueryService;
import com.aria.datasource.DataSourceSpec;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/** Unit test for {@link ExportCsvActionHandler}: rows -> CSV text. */
class ExportCsvActionHandlerTest {

    private ExportCsvActionHandler newHandler(DataSourceQueryService ds) {
        ExportCsvActionHandler h = new ExportCsvActionHandler();
        ReflectionTestUtils.setField(h, "ds", ds);
        return h;
    }

    @Test
    void code_isExportCsv() {
        assertThat(new ExportCsvActionHandler().code()).isEqualTo("export.csv");
    }

    @Test
    void rowsCompileToCsvText() {
        DataSourceQueryService ds = mock(DataSourceQueryService.class);
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("name", "Acme");
        row1.put("city", "Chennai");
        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("name", "Beta");
        row2.put("city", "Bengaluru");
        when(ds.query(any(), anyInt(), anyInt(), any()))
                .thenReturn(DataSourceSpec.Response.builder()
                        .content(List.of(row1, row2)).totalElements(2).build());

        ExportCsvActionHandler h = newHandler(ds);
        ActionResult r = h.handle(ActionInvocation.builder()
                .spec(DataSourceSpec.builder().entity("Vendor").build())
                .build());

        assertThat(r.isOk()).isTrue();
        String csv = (String) r.getData().get("csv");
        assertThat(csv).contains("name,city");
        assertThat(csv).contains("Acme,Chennai");
        assertThat(csv).contains("Beta,Bengaluru");
        assertThat(r.getData()).containsEntry("rowCount", 2);
        assertThat(((String) r.getData().get("filename"))).startsWith("export-").endsWith(".csv");
    }

    @Test
    void emptyResult_returnsEmptyCsv() {
        DataSourceQueryService ds = mock(DataSourceQueryService.class);
        when(ds.query(any(), anyInt(), anyInt(), any()))
                .thenReturn(DataSourceSpec.Response.builder().content(List.of()).build());
        ExportCsvActionHandler h = newHandler(ds);

        ActionResult r = h.handle(ActionInvocation.builder()
                .spec(DataSourceSpec.builder().entity("Vendor").build())
                .build());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("csv", "");
        assertThat(r.getData()).containsEntry("rowCount", 0);
    }

    @Test
    void missingSpec_throws() {
        ExportCsvActionHandler h = newHandler(mock(DataSourceQueryService.class));
        assertThatThrownBy(() -> h.handle(new ActionInvocation()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spec");
    }
}
