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
 * Unit tests for {@link ReturnStockUnloadingStockPostingListener}. Mock the
 * JdbcTemplate; capture every SQL the listener fires and assert via
 * substring — same style as {@code StockMovementListenerTest}.
 */
class ReturnStockUnloadingStockPostingListenerTest {

    private JdbcTemplate jdbc;
    private ReturnStockUnloadingStockPostingListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), ArgumentMatchers.<Object[]>any())).thenReturn(1);
        when(jdbc.queryForList(anyString(), eq(String.class), any(), any()))
                .thenReturn(List.of("GP-1"));
        listener = new ReturnStockUnloadingStockPostingListener(jdbc);
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
    void fromQcApprovedDirectly_posts() {
        // The new direct QC_APPROVED -> UNLOADING_COMPLETED edge (V325) must
        // post identically to the legacy UNLOADING_IN_PROGRESS -> UNLOADING_COMPLETED
        // hop — checked against CURRENT state, not previousState.
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO m5_stock_movement"));
    }

    @Test
    void fromUnloadingInProgress_postsToo() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "UNLOADING_IN_PROGRESS",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO m5_stock_movement"));
    }

    @Test
    void onePosted_writesMovementAndBalanceUpdate() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        List<String> sql = capturedSql();
        assertThat(sql).anyMatch(s -> s.contains("INSERT INTO m5_stock_movement"));
        assertThat(sql).anyMatch(s -> s.contains("UPDATE m5_stock_balance") && s.contains("pc_qty_on_hand"));
    }

    @Test
    void balanceUpdateAffectsZeroRows_fallsBackToInsert() {
        when(jdbc.update(startsWith("UPDATE m5_stock_balance"), ArgumentMatchers.<Object[]>any()))
                .thenReturn(0);
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO m5_stock_balance") && s.contains("pc_qty_on_hand"));
    }

    @Test
    void twoLocations_postsTwoMovements() {
        Map<String, Object> row1 = row("SKU-A", "Item A", "B26-01", "G1", "10", "5");
        Map<String, Object> row2 = row("SKU-A", "Item A", "B26-01", "G2", "3", "1");
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(row1, row2))));
        long movementInserts = capturedSql().stream().filter(s -> s.contains("INSERT INTO m5_stock_movement")).count();
        assertThat(movementInserts).isEqualTo(2);
    }

    @Test
    void rowWithZeroQty_isSkipped_othersStillPost() {
        Map<String, Object> zeroRow = row("SKU-B", "Item B", "B26-02", "G1", "0", "0");
        Map<String, Object> goodRow = oneRow();
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(zeroRow, goodRow))));
        long movementInserts = capturedSql().stream().filter(s -> s.contains("INSERT INTO m5_stock_movement")).count();
        assertThat(movementInserts).isEqualTo(1);
    }

    @Test
    void rowMissingBin_isSkipped() {
        // A gate-pass-number lookup (queryForList) always runs once per
        // invocation regardless of row validity — only the per-row
        // movement/balance writes (update calls) are expected to be skipped.
        Map<String, Object> badRow = row("SKU-C", "Item C", "B26-03", null, "5", "2");
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(badRow))));
        assertThat(capturedSql()).isEmpty();
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
        return row("SKU-1624", "Coconut Oil 100ml", "BACT-003", "G1", "5", "20");
    }

    private static Map<String, Object> row(String skuCode, String skuName, String batchNo, String bin,
                                            String unloadingQty, String pcQty) {
        java.util.HashMap<String, Object> m = new java.util.HashMap<>();
        m.put("sku_code", skuCode);
        m.put("sku_name", skuName);
        m.put("batch_no", batchNo);
        m.put("processed_area", bin);
        m.put("unloading_qty", unloadingQty);
        m.put("pc_qty", pcQty);
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
