package com.aria.workflow.projection;

import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BillingOrderDetailTransitionListener}. Mock the
 * JdbcTemplate; capture every SQL the listener fires and assert via
 * substring — same convention as StockMovementListenerTest.
 */
class BillingOrderDetailTransitionListenerTest {

    private JdbcTemplate jdbc;
    private BillingOrderDetailTransitionListener listener;

    private void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), ArgumentMatchers.<Object[]>any())).thenReturn(1);
        listener = new BillingOrderDetailTransitionListener(jdbc);
    }

    @Test
    void loadedSkuDetails_present_writesOneRowPerLinePlusAudit() {
        setUp();
        WorkflowInstance wi = wi(1567L, "M10.FGLoading");
        Map<String, Object> pageData = Map.of(
                "vehicle_no", "TN86J8079", "gpNumber", "GP-1-1567",
                "loaded_sku_details", List.of(
                        Map.of("order_id", "ORD1", "sku_code", "SKU1", "order_qty", 65, "loading_qty", 50, "skipped_quantity", 15),
                        Map.of("order_id", "ORD2", "sku_code", "SKU2", "order_qty", 10, "loading_qty", 10, "skipped_quantity", 0)));

        listener.onTransition(wi, "SKIPPED_ITEM_ALLOCATION", Map.of("aria:skipped-item-allocation", pageData));

        List<String> sql = capturedSql();
        assertThat(sql).filteredOn(s -> s.contains("INSERT INTO billing_order_details")).hasSize(2);
        assertThat(sql).anyMatch(s -> s.contains("INSERT INTO m0_audit_log"));

        verify(jdbc).update(anyString(), eq(1L), eq(1567L), eq("TN86J8079"), eq("GP-1-1567"),
                eq("ORD1"), isNull(), isNull(), eq("SKU1"), isNull(),
                any(java.math.BigDecimal.class), any(java.math.BigDecimal.class), any(java.math.BigDecimal.class));
    }

    @Test
    void noPageDataForThisPage_doesNothing() {
        setUp();
        WorkflowInstance wi = wi(1L, "M10.FGLoading");
        listener.onTransition(wi, "SOME_STATE", Map.of("some-other-page", Map.of("x", 1)));
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingLoadedSkuDetailsKey_doesNothing() {
        setUp();
        WorkflowInstance wi = wi(1L, "M10.FGLoading");
        listener.onTransition(wi, "SOME_STATE",
                Map.of("aria:skipped-item-allocation", Map.of("vehicle_no", "TN01AB1234")));
        verifyNoInteractions(jdbc);
    }

    @Test
    void emptyLoadedSkuDetails_doesNothing() {
        setUp();
        WorkflowInstance wi = wi(1L, "M10.FGLoading");
        listener.onTransition(wi, "SOME_STATE",
                Map.of("aria:skipped-item-allocation", Map.of("loaded_sku_details", List.of())));
        verifyNoInteractions(jdbc);
    }

    @Test
    void nullBag_doesNothing() {
        setUp();
        WorkflowInstance wi = wi(1L, "M10.FGLoading");
        listener.onTransition(wi, "SOME_STATE", null);
        verifyNoInteractions(jdbc);
    }

    @Test
    void oneLineInsertFails_otherLineStillWritten_andAuditReflectsPartialCount() {
        setUp();
        when(jdbc.update(contains("billing_order_details"), any(), any(), any(), any(), eq("BAD"),
                any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        WorkflowInstance wi = wi(2L, "M10.FGLoading");
        Map<String, Object> pageData = Map.of(
                "vehicle_no", "TN01AB1234",
                "loaded_sku_details", List.of(
                        Map.of("order_id", "BAD", "sku_code", "SKU1", "order_qty", 5, "loading_qty", 5, "skipped_quantity", 0),
                        Map.of("order_id", "GOOD", "sku_code", "SKU2", "order_qty", 5, "loading_qty", 5, "skipped_quantity", 0)));

        listener.onTransition(wi, "SKIPPED_ITEM_ALLOCATION", Map.of("aria:skipped-item-allocation", pageData));

        List<String> sql = capturedSql();
        assertThat(sql).filteredOn(s -> s.contains("INSERT INTO billing_order_details")).hasSize(2); // both attempted
        assertThat(sql).anyMatch(s -> s.contains("INSERT INTO m0_audit_log"));
    }

    // ---- helpers -----------------------------------------------------------

    private static WorkflowInstance wi(Long id, String workflowCode) {
        WorkflowInstance i = new WorkflowInstance();
        i.setId(id);
        i.setTenantId(1L);
        i.setWorkflowCode(workflowCode);
        i.setCurrentState("PMT_CHECK_OK");
        return i;
    }

    private List<String> capturedSql() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .map(i -> i.getArgument(0))
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
