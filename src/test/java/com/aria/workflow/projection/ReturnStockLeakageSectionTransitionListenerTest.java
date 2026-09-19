package com.aria.workflow.projection;

import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReturnStockLeakageSectionTransitionListener}. Mock
 * the JdbcTemplate; capture every SQL the listener fires, same style as
 * {@code ReturnStockOfficerItemTransitionListener}'s sibling listeners.
 */
class ReturnStockLeakageSectionTransitionListenerTest {

    private JdbcTemplate jdbc;
    private ReturnStockLeakageSectionTransitionListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), ArgumentMatchers.<Object[]>any())).thenReturn(1);
        listener = new ReturnStockLeakageSectionTransitionListener(jdbc);
    }

    @Test
    void wrongWorkflow_doesNothing() {
        WorkflowInstance wi = wi("M10.PackUnload", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "UNLOADING_IN_PROGRESS",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        verifyNoInteractions(jdbc);
    }

    @Test
    void wrongState_doesNothing() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "WEIGHMENT_2");
        listener.onTransition(wi, "UNLOADING_COMPLETED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        verifyNoInteractions(jdbc);
    }

    @Test
    void noPageData_doesNothing() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED", Map.of("aria:qc-approval", Map.of("status", "APPROVED")));
        verifyNoInteractions(jdbc);
    }

    @Test
    void emptyList_doesNothing() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of())));
        verifyNoInteractions(jdbc);
    }

    @Test
    void fromQcApprovedDirectly_writesRow() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO return_stock_leakage_section"));
    }

    @Test
    void fromUnloadingInProgress_writesRowToo() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "UNLOADING_IN_PROGRESS",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO return_stock_leakage_section"));
    }

    @Test
    void deletesExistingRowsForInstanceBeforeInserting() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        assertThat(capturedSql()).anyMatch(s -> s.startsWith("DELETE FROM return_stock_leakage_section"));
    }

    @Test
    void twoLines_writesTwoRows() {
        Map<String, Object> box = row("SKU-1624", "BACT-003", "G1", "BOX", "5");
        Map<String, Object> pc = row("SKU-1624", "BACT-003", "G1", "PC", "3");
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(box, pc))));
        long inserts = capturedSql().stream().filter(s -> s.contains("INSERT INTO return_stock_leakage_section")).count();
        assertThat(inserts).isEqualTo(2);
    }

    // ---- helpers ---------------------------------------------------------
    private static WorkflowInstance wi(String code, String state) {
        WorkflowInstance i = new WorkflowInstance();
        i.setId(1L);
        i.setTenantId(1L);
        i.setWorkflowCode(code);
        i.setCurrentState(state);
        return i;
    }

    private static Map<String, Object> oneRow() {
        return row("SKU-1624", "BACT-003", "G1", "BOX", "5");
    }

    private static Map<String, Object> row(String skuCode, String batchNo, String bin, String lineType, String qty) {
        java.util.HashMap<String, Object> m = new java.util.HashMap<>();
        m.put("sku_code", skuCode);
        m.put("sku_name", "Coconut Oil 100ml");
        m.put("batch_no", batchNo);
        m.put("processed_area", bin);
        m.put("line_type", lineType);
        m.put("scan_method", "GUN_SCANNER");
        m.put("unloading_qty", qty);
        return m;
    }

    private List<String> capturedSql() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .map(i -> i.getArgument(0))
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
