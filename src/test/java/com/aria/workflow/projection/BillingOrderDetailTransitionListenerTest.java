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
 *
 * <p>V253 changed the listener to resolve the actual triggering page per
 * transition (a DB lookup against {@code workflow_definition.body_json})
 * instead of a hard-coded page key — every test here stubs that lookup to
 * return a transitions[] array matching the (previousState, currentState)
 * pair under test, mirroring the fix's own rationale: a real
 * {@code aria:billing-entry} submission carried {@code loaded_sku_details},
 * not {@code aria:skipped-item-allocation}.
 */
class BillingOrderDetailTransitionListenerTest {

    private JdbcTemplate jdbc;
    private BillingOrderDetailTransitionListener listener;

    private void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), ArgumentMatchers.<Object[]>any())).thenReturn(1);
        listener = new BillingOrderDetailTransitionListener(jdbc);
    }

    /** Stubs the resolveTriggeringPageCode lookup to resolve to pageCode for (from -> to). */
    private void stubTriggeringPage(String from, String to, String pageCode) {
        String bodyJson = "{\"transitions\":[{\"from\":\"" + from + "\",\"to\":\"" + to + "\",\"pageCode\":\"" + pageCode + "\"}]}";
        when(jdbc.queryForObject(eq("SELECT body_json FROM workflow_definition WHERE tenant_id=? AND code=? AND version=?"),
                eq(String.class), any(), any(), any()))
                .thenReturn(bodyJson);
    }

    /** No transitions[] entry matches — resolveTriggeringPageCode returns null. */
    private void stubNoMatchingTransition() {
        when(jdbc.queryForObject(eq("SELECT body_json FROM workflow_definition WHERE tenant_id=? AND code=? AND version=?"),
                eq(String.class), any(), any(), any()))
                .thenReturn("{\"transitions\":[]}");
    }

    @Test
    void loadedSkuDetails_underSkippedItemAllocationPage_writesOneRowPerLinePlusAudit() {
        setUp();
        stubTriggeringPage("SKIPPED_ITEM_ALLOCATION", "PMT_CHECK_OK", "aria:skipped-item-allocation");
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
                any(java.math.BigDecimal.class), any(java.math.BigDecimal.class), any(java.math.BigDecimal.class), isNull());
    }

    @Test
    void loadedSkuDetails_underADifferentTriggeringPage_stillWrites() {
        // Confirmed live: a real workflow.transition payload (pageCode
        // aria:billing-entry, instance 1776) carried loaded_sku_details
        // under ITS OWN page key, not aria:skipped-item-allocation — this
        // is the exact scenario the fix targets.
        setUp();
        stubTriggeringPage("WEIGHMENT_2", "PMT_CHECK_OK", "aria:billing-entry");
        WorkflowInstance wi = wi(1776L, "M10.FGLoading");
        Map<String, Object> pageData = Map.of(
                "vehicle_no", "TN86H0989", "gpNumber", "GP-1-118",
                "loaded_sku_details", List.of(
                        Map.of("order_id", "ORD30350983", "sku_code", "SKU9269", "order_qty", 120, "loading_qty", 70, "skipped_quantity", 50)));

        listener.onTransition(wi, "WEIGHMENT_2", Map.of("aria:billing-entry", pageData));

        List<String> sql = capturedSql();
        assertThat(sql).filteredOn(s -> s.contains("INSERT INTO billing_order_details")).hasSize(1);
        verify(jdbc).update(anyString(), eq(1L), eq(1776L), eq("TN86H0989"), eq("GP-1-118"),
                eq("ORD30350983"), isNull(), isNull(), eq("SKU9269"), isNull(),
                any(java.math.BigDecimal.class), any(java.math.BigDecimal.class), any(java.math.BigDecimal.class), isNull());
    }

    @Test
    void insertUsesOnDuplicateKeyUpdate_forIdempotentResubmission() {
        setUp();
        stubTriggeringPage("SKIPPED_ITEM_ALLOCATION", "PMT_CHECK_OK", "aria:skipped-item-allocation");
        WorkflowInstance wi = wi(1567L, "M10.FGLoading");
        Map<String, Object> pageData = Map.of(
                "loaded_sku_details", List.of(Map.of("order_id", "ORD1", "sku_code", "SKU1", "order_qty", 65, "loading_qty", 50, "skipped_quantity", 15)));

        listener.onTransition(wi, "SKIPPED_ITEM_ALLOCATION", Map.of("aria:skipped-item-allocation", pageData));

        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO billing_order_details")
                && s.contains("ON DUPLICATE KEY UPDATE") && s.contains("skipped_quantity = VALUES(skipped_quantity)"));
    }

    @Test
    void reasonField_savedForSkippedLine() {
        setUp();
        stubTriggeringPage("SKIPPED_ITEM_ALLOCATION", "PMT_CHECK_OK", "aria:skipped-item-allocation");
        WorkflowInstance wi = wi(1567L, "M10.FGLoading");
        Map<String, Object> pageData = Map.of(
                "loaded_sku_details", List.of(Map.of("order_id", "ORD1", "sku_code", "SKU1",
                        "order_qty", 65, "loading_qty", 50, "skipped_quantity", 15, "reason", "Vehicle capacity full")));

        listener.onTransition(wi, "SKIPPED_ITEM_ALLOCATION", Map.of("aria:skipped-item-allocation", pageData));

        verify(jdbc).update(anyString(), eq(1L), eq(1567L), isNull(), isNull(),
                eq("ORD1"), isNull(), isNull(), eq("SKU1"), isNull(),
                any(java.math.BigDecimal.class), any(java.math.BigDecimal.class), any(java.math.BigDecimal.class),
                eq("Vehicle capacity full"));
    }

    @Test
    void noTransitionMatch_doesNothing() {
        setUp();
        stubNoMatchingTransition();
        WorkflowInstance wi = wi(1L, "M10.FGLoading");
        listener.onTransition(wi, "SOME_STATE", Map.of("some-other-page", Map.of("loaded_sku_details", List.of(Map.of("sku_code", "X")))));
        verify(jdbc, never()).update(anyString(), ArgumentMatchers.<Object[]>any());
    }

    @Test
    void resolvedPageHasNoBagEntry_doesNothing() {
        setUp();
        stubTriggeringPage("SOME_STATE", "PMT_CHECK_OK", "aria:some-page-with-no-bag-entry");
        WorkflowInstance wi = wi(1L, "M10.FGLoading");
        listener.onTransition(wi, "SOME_STATE", Map.of("some-other-page", Map.of("x", 1)));
        verify(jdbc, never()).update(anyString(), ArgumentMatchers.<Object[]>any());
    }

    @Test
    void missingLoadedSkuDetailsKey_doesNothing() {
        setUp();
        stubTriggeringPage("SOME_STATE", "PMT_CHECK_OK", "aria:skipped-item-allocation");
        WorkflowInstance wi = wi(1L, "M10.FGLoading");
        listener.onTransition(wi, "SOME_STATE",
                Map.of("aria:skipped-item-allocation", Map.of("vehicle_no", "TN01AB1234")));
        verify(jdbc, never()).update(anyString(), ArgumentMatchers.<Object[]>any());
    }

    @Test
    void emptyLoadedSkuDetails_doesNothing() {
        setUp();
        stubTriggeringPage("SOME_STATE", "PMT_CHECK_OK", "aria:skipped-item-allocation");
        WorkflowInstance wi = wi(1L, "M10.FGLoading");
        listener.onTransition(wi, "SOME_STATE",
                Map.of("aria:skipped-item-allocation", Map.of("loaded_sku_details", List.of())));
        verify(jdbc, never()).update(anyString(), ArgumentMatchers.<Object[]>any());
    }

    @Test
    void billingTableRateRows_writesDirectlyFromSubmittedRow_noOrderDispatchDetailLookup() {
        setUp();
        stubTriggeringPage("SEALING_DONE", "PMT_CHECK_OK", "aria:billing-entry");
        WorkflowInstance wi = wi(1776L, "M10.FGLoading");
        Map<String, Object> pageData = Map.of(
                "vehicle_no", "TN86H0989", "gpNumber", "GP-1-118",
                "billing_table", List.of(
                        Map.of("order_id", "ORD1", "sku_code", "SKU1", "rd_code", "RD1", "rd_desc", "RD One",
                                "sku_desc", "SKU One", "order_rate", 100, "gst_rate_pct", 18, "loaded_qty", 40,
                                "invoice_no", "INV1")));

        listener.onTransition(wi, "SEALING_DONE", Map.of("aria:billing-entry", pageData));

        // No m5_order_dispatch_detail/SkuMaster/LoadingProgressDetailBay lookup — row data alone drives the write.
        verify(jdbc, never()).queryForList(contains("FROM m5_order_dispatch_detail"), ArgumentMatchers.<Object[]>any());
        List<String> sql = capturedSql();
        assertThat(sql).anyMatch(s -> s.contains("INSERT INTO billing_order_details")
                && s.contains("net_rate") && s.contains("billed_qty"));
        verify(jdbc).update(anyString(), eq(1L), eq(1776L), eq("TN86H0989"), eq("GP-1-118"),
                eq("ORD1"), eq("RD1"), eq("RD One"), eq("SKU1"), eq("SKU One"),
                eq(java.math.BigDecimal.valueOf(40.0)),
                eq(java.math.BigDecimal.valueOf(100.0)), eq(java.math.BigDecimal.valueOf(18.0)),
                eq(new java.math.BigDecimal("18.000")), eq(new java.math.BigDecimal("82.000")),
                eq(java.math.BigDecimal.valueOf(40.0)), eq("INV1"));
    }

    @Test
    void billingTableRateRows_loadedQtyKey_takesPriorityOverLineDispatchQtyAlias() {
        setUp();
        stubTriggeringPage("SEALING_DONE", "PMT_CHECK_OK", "aria:billing-entry");
        WorkflowInstance wi = wi(1776L, "M10.FGLoading");
        Map<String, Object> pageData = Map.of(
                "vehicle_no", "TN86H0989",
                "billing_table", List.of(
                        Map.of("order_id", "ORD1", "sku_code", "SKU1", "order_rate", 100, "gst_rate_pct", 18,
                                "loaded_qty", 55, "line_dispatch_qty", 40)));

        listener.onTransition(wi, "SEALING_DONE", Map.of("aria:billing-entry", pageData));

        verify(jdbc).update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                eq(java.math.BigDecimal.valueOf(55.0)), any(), any(), any(), any(),
                eq(java.math.BigDecimal.valueOf(55.0)), any());
    }

    @Test
    void billingTableRateRows_fallsBackToLineDispatchQty_whenLoadedQtyAbsent() {
        setUp();
        stubTriggeringPage("SEALING_DONE", "PMT_CHECK_OK", "aria:billing-entry");
        WorkflowInstance wi = wi(1776L, "M10.FGLoading");
        Map<String, Object> pageData = Map.of(
                "vehicle_no", "TN86H0989",
                "billing_table", List.of(
                        Map.of("order_id", "ORD1", "sku_code", "SKU1", "order_rate", 100, "gst_rate_pct", 18,
                                "line_dispatch_qty", 40)));

        listener.onTransition(wi, "SEALING_DONE", Map.of("aria:billing-entry", pageData));

        verify(jdbc).update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                eq(java.math.BigDecimal.valueOf(40.0)), any(), any(), any(), any(),
                eq(java.math.BigDecimal.valueOf(40.0)), any());
    }

    @Test
    void billingTableRateRows_skippedWhenVehicleNoMissing() {
        setUp();
        stubTriggeringPage("SEALING_DONE", "PMT_CHECK_OK", "aria:billing-entry");
        WorkflowInstance wi = wi(1776L, "M10.FGLoading");

        listener.onTransition(wi, "SEALING_DONE", Map.of("aria:billing-entry", Map.of()));

        verify(jdbc, never()).queryForList(anyString(), ArgumentMatchers.<Object[]>any());
        verify(jdbc, never()).update(anyString(), ArgumentMatchers.<Object[]>any());
    }

    @Test
    void nullBag_doesNothing() {
        setUp();
        WorkflowInstance wi = wi(1L, "M10.FGLoading");
        listener.onTransition(wi, "SOME_STATE", null);
        // Returns before ever resolving the triggering page — no DB interaction at all.
        verifyNoInteractions(jdbc);
    }

    @Test
    void oneLineInsertFails_otherLineStillWritten_andAuditReflectsPartialCount() {
        setUp();
        stubTriggeringPage("SKIPPED_ITEM_ALLOCATION", "PMT_CHECK_OK", "aria:skipped-item-allocation");
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
