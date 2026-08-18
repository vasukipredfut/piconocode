package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import com.aria.workflow.dto.WorkflowTransitionResult;
import com.aria.workflow.engine.WorkflowEngineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link M10StartBayActionHandler}: "Bay" here is a named
 * session over one or more SKU line items (formData.order_sku_line_items),
 * not a physical LoadingPoint. Covers input validation, the sequential
 * one-open-bay-per-instance rule, and the "only the FIRST bay drives the
 * LOADING_IN_PROGRESS -> BAY_ASSIGNED transition" rule.
 *
 * <p>NOT covered here: the branch where {@code engine.handlePageSubmission}
 * throws and the handler calls {@code TransactionAspectSupport
 * .currentTransactionStatus().setRollbackOnly()}. That call requires a live
 * Spring-managed {@code @Transactional} proxy context (it throws {@code
 * NoTransactionException} when invoked outside one) — not reproducible by
 * calling {@code handle(...)} directly the way every other test here does.
 * That behavior needs a real running app to exercise (manual/API smoke
 * test), per CLAUDE.md's note that live wiring is covered outside unit
 * tests.
 */
class M10StartBayActionHandlerTest {

    private JdbcTemplate jdbc;
    private WorkflowEngineService engine;
    private M10StartBayActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        engine = mock(WorkflowEngineService.class);
        handler = new M10StartBayActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        ReflectionTestUtils.setField(handler, "engine", engine);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isM10StartBay() {
        assertThat(handler.code()).isEqualTo("m10.start-bay");
    }

    @Test
    void missingInstanceId_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().pageCode("aria:loading-in-progress-entry").build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("instanceId required");
        verifyNoInteractions(jdbc, engine);
    }

    @Test
    void missingPageCode_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().instanceId(1495L).build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("pageCode required");
        verifyNoInteractions(jdbc, engine);
    }

    @Test
    void missingBayNameAndTankLocation_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of("order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("formData.bay_name or formData.loading_tank_location is required");
        verifyNoInteractions(jdbc, engine);
    }

    @Test
    void tankLocation_usedInPlaceOfBayName_skipsBay1To12Check() {
        stubOpenBayCount(0);
        stubCurrentState("BAY_ASSIGNED");
        stubParentId(1L);

        // "ROT-1" (an m10_tanker_location code, M10.TankerOutbound) would fail
        // the Bay1..Bay12 check if it arrived as bay_name — it's accepted
        // unchecked when it arrives as loading_tank_location instead, since
        // bay_name itself is absent.
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "loading_tank_location", "ROT-1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("bayName", "ROT-1");
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(1L), eq(1495L), eq("ROU028912"), eq("SKU1215"), eq("SKU1215 desc"), eq("ROT-1"), eq(new BigDecimal("45")));
    }

    @Test
    void bayNamePresent_stillEnforcesBay1To12EvenWithTankLocationAlsoGiven() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "ROT-1", "loading_tank_location", "ROT-1",
                "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("must be one of Bay1..Bay12").contains("ROT-1");
        verifyNoInteractions(jdbc, engine);
    }

    @Test
    void bayNameOutOfRange_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay13", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("must be one of Bay1..Bay12").contains("Bay13");
        verifyNoInteractions(jdbc, engine);
    }

    @Test
    void bayNameZero_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay0", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("must be one of Bay1..Bay12");
    }

    @Test
    void bayNameWrongCase_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("must be one of Bay1..Bay12");
    }

    @Test
    void bayNameWithSpace_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay 1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("must be one of Bay1..Bay12");
    }

    @Test
    void bayNameBoundaries_bay1AndBay12BothValid() {
        stubOpenBayCount(0);
        stubCurrentState("BAY_ASSIGNED");
        stubParentId(1L);

        ActionResult r1 = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));
        assertThat(r1.isOk()).isTrue();

        ActionResult r12 = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay12", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));
        assertThat(r12.isOk()).isTrue();
    }

    @Test
    void missingLineItemsArray_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1")));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("order_sku_line_items must be a non-empty array");
    }

    @Test
    void emptyLineItemsArray_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1", "order_sku_line_items", List.of())));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("non-empty array");
    }

    @Test
    void lineItemMissingSkuCode_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("order_qty", 45);
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1", "order_sku_line_items", List.of(badLine))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("missing route_id or sku_code");
    }

    @Test
    void lineItemZeroQty_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 0)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("SKU1215").contains("order_qty > 0");
    }

    @Test
    void lineItemNonNumericQty_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("sku_code", "SKU1215");
        badLine.put("order_qty", "not-a-number");
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1", "order_sku_line_items", List.of(badLine))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("order_qty must be a number");
    }

    @Test
    void alreadyOpenBayForInstance_refusesToStartAnother() {
        stubOpenBayCount(1);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay2", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already has an open bay");
        verifyNoInteractions(engine);
        verify(jdbc, never()).queryForObject(contains("current_state"), eq(String.class), any(Object[].class));
    }

    @Test
    void firstBay_fromLoadingInProgress_firesTransitionAndOpensRows() {
        stubOpenBayCount(0);
        stubCurrentState("LOADING_IN_PROGRESS");
        stubParentId(777L);
        when(engine.handlePageSubmission(eq(1495L), eq("aria:loading-in-progress-entry"), anyMap(), any()))
                .thenReturn(WorkflowTransitionResult.builder()
                        .instanceId(1495L).previousState("LOADING_IN_PROGRESS").newState("BAY_ASSIGNED")
                        .transitioned(true).build());

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "vehicle_no", "TN86J8070", "gpNumber", "GP-1-1495", "bay_name", "Bay1",
                "order_sku_line_items", List.of(
                        line("ROU028912", "SKU1215", 45),
                        line("ROU028912", "SKU1461", 250)))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("fromState", "LOADING_IN_PROGRESS");
        assertThat(r.getData()).containsEntry("toState", "BAY_ASSIGNED");
        assertThat(r.getData()).containsEntry("transitioned", true);
        assertThat(r.getData()).containsEntry("lineCount", 2);
        assertThat(r.getData()).containsEntry("totalQuantity", new BigDecimal("295"));

        // The engine is called exactly once for the whole bay, with the
        // guard flag set, not once per line item.
        verify(engine, times(1)).handlePageSubmission(eq(1495L), eq("aria:loading-in-progress-entry"),
                argThat(fd -> "start".equals(fd.get("bayAction"))), any());

        // One loading_progress_detail_bay row per line item.
        verify(jdbc, times(2)).update(contains("INSERT INTO loading_progress_detail_bay"), any(Object[].class));
        // Parent rollup upserted once with the combined total (295). Matcher
        // is anchored with the opening "(" so it can't also match the
        // "_bay" child-table INSERT (which starts with the same prefix).
        verify(jdbc).update(startsWith("INSERT INTO loading_progress_detail ("),
                eq(1L), eq(1495L), eq("TN86J8070"), eq("GP-1-1495"), eq(new BigDecimal("295")));
    }

    @Test
    void secondBay_alreadyBayAssigned_doesNotCallEngineAgain() {
        stubOpenBayCount(0);
        stubCurrentState("BAY_ASSIGNED");
        stubParentId(777L);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay2", "order_sku_line_items", List.of(line("ROU028912", "SKU9999", 200)))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("fromState", "BAY_ASSIGNED");
        assertThat(r.getData()).containsEntry("toState", "BAY_ASSIGNED");
        assertThat(r.getData()).containsEntry("transitioned", false);
        verifyNoInteractions(engine);
    }

    @Test
    void wrongWorkflowState_refusesToStart() {
        stubOpenBayCount(0);
        stubCurrentState("WEIGHMENT_2");

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("cannot start a bay from workflow state WEIGHMENT_2");
        verifyNoInteractions(engine);
        verify(jdbc, never()).update(contains("loading_progress_detail_bay"), any(Object[].class));
    }

    private ActionInvocation baseInvocation(Map<String, Object> extraFormData) {
        Map<String, Object> formData = new LinkedHashMap<>();
        formData.put("route_id", "ROU028912");
        formData.putAll(extraFormData);
        return ActionInvocation.builder()
                .instanceId(1495L)
                .pageCode("aria:loading-in-progress-entry")
                .formData(formData)
                .build();
    }

    private Map<String, Object> line(String routeId, String skuCode, int orderQty) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("route_id", routeId);
        line.put("sku_code", skuCode);
        line.put("sku_desc", skuCode + " desc");
        line.put("order_qty", orderQty);
        return line;
    }

    private void stubOpenBayCount(int count) {
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenReturn(count);
    }

    private void stubCurrentState(String state) {
        when(jdbc.queryForObject(contains("current_state"), eq(String.class), any(Object[].class)))
                .thenReturn(state);
    }

    private void stubParentId(Long id) {
        when(jdbc.queryForObject(contains("SELECT id FROM loading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(id);
    }
}
