package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * M10.TankerInbound's aria:manager-qty-approval -> manager_qty_approval_detail. The page is submitted from WEIGHMENT_2
 * (tolerance routing) as well as QTY_PENDING_MANAGER (manager decision); both must be captured, with the invoice /
 * unloading quantities, their difference and the tolerance figures the page shows.
 */
class ManagerQtyApprovalDecisionListenerTest {

    private static final String PAGE = "aria:manager-qty-approval";
    private JdbcTemplate jdbc;
    private ManagerQtyApprovalDecisionListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        listener = new ManagerQtyApprovalDecisionListener(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private static WorkflowInstance inst(String code, String state) {
        return WorkflowInstance.builder().id(1542L).workflowCode(code).currentState(state).build();
    }

    private static Map<String, Object> bag(Map<String, Object> page) {
        Map<String, Object> b = new HashMap<>();
        b.put(PAGE, page);
        return b;
    }

    private Object[] updateCall() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update")).findFirst().orElseThrow().getArguments();
    }

    private static Map<String, Object> fullPage() {
        Map<String, Object> page = new HashMap<>();
        page.put("invoice_qty_display", "1000");
        page.put("unloading_qty_display", "985");
        page.put("qty_difference_display", "15");
        page.put("expected_weight_kg", "1000");
        page.put("loaded_weight_kg", "985");
        page.put("pmt_variance_pct", "1.5");
        page.put("withinTolerance", "NO");
        page.put("variance_reason", "short supply");
        page.put("status", "APPROVED");
        return page;
    }

    @Test
    void firstPassFromWeighment2_isCaptured_withDifferenceAndToleranceFigures() {
        listener.onTransition(inst("M10.TankerInbound", "QTY_PENDING_MANAGER"), "WEIGHMENT_2", bag(fullPage()));

        Object[] a = updateCall();
        assertThat((String) a[0]).contains("INSERT INTO manager_qty_approval_detail")
                .contains("invoice_qty", "unloading_qty", "qty_difference", "expected_weight_kg", "loaded_weight_kg",
                        "pmt_variance_pct", "pmt_within_tolerance", "manager_qty_reason", "manager_decision");
        assertThat(List.of(a)).contains("1000", "985", "15", "1.5", "NO", "short supply", "APPROVED");
    }

    @Test
    void managerDecisionFromQtyPendingManager_stillCaptured() {
        listener.onTransition(inst("M10.TankerInbound", "DOC_VERIFICATION_PENDING"), "QTY_PENDING_MANAGER", bag(fullPage()));

        assertThat((String) updateCall()[0]).contains("qty_difference", "manager_decision");
    }

    @Test
    void fieldsThePageDidNotSend_areNotWritten() {
        Map<String, Object> page = new HashMap<>(Map.of("status", "APPROVED"));
        listener.onTransition(inst("M10.TankerInbound", "DOC_VERIFICATION_PENDING"), "WEIGHMENT_2", bag(page));

        assertThat((String) updateCall()[0]).contains("manager_decision")
                .doesNotContain("qty_difference").doesNotContain("expected_weight_kg").doesNotContain("pmt_within_tolerance");
    }

    // ---- the actual difference in kg, saved in pmt_variance_pct (not a percentage) ----

    @Test
    void inbound_varianceWorkedOutAsLoadedMinusExpected_inTheSameField_whenThePageSendsNone() {
        Map<String, Object> page = fullPage();
        page.remove("pmt_variance_pct");
        listener.onTransition(inst("M10.TankerInbound", "QTY_PENDING_MANAGER"), "WEIGHMENT_2", bag(page));

        Object[] a = updateCall();
        assertThat((String) a[0]).contains("pmt_variance_pct").doesNotContain("pmt_variance_kg");
        assertThat(List.of(a)).anyMatch(o -> o instanceof java.math.BigDecimal b && b.compareTo(new java.math.BigDecimal("-15")) == 0);
    }

    @Test
    void inbound_varianceSentByThePage_isSavedAsItIs() {
        Map<String, Object> page = fullPage();
        page.put("pmt_variance_pct", "-14.5");
        listener.onTransition(inst("M10.TankerInbound", "QTY_PENDING_MANAGER"), "WEIGHMENT_2", bag(page));

        assertThat(List.of(updateCall())).contains("-14.5");
    }

    @Test
    void inbound_noExpectedOrLoaded_noVariance() {
        Map<String, Object> page = new HashMap<>(Map.of("status", "APPROVED", "loaded_weight_kg", "985"));
        listener.onTransition(inst("M10.TankerInbound", "DOC_VERIFICATION_PENDING"), "WEIGHMENT_2", bag(page));

        assertThat((String) updateCall()[0]).doesNotContain("pmt_variance_pct");
    }

    @Test
    void outbound_varianceIsTheActualDifferenceInKg_inThePmtVariancePctField() {
        Map<String, Object> page = new HashMap<>(Map.of("status", "APPROVED", "expected_weight_kg", "20000",
                "loaded_weight_kg", "20080", "withinTolerance", "YES"));
        new PmtSharedReasonListener(jdbc).onTransition(inst("M10.TankerOutbound", "WEIGHT_DIFF_OK"), "WEIGHMENT_2", bag(page));

        Object[] a = updateCall();
        assertThat((String) a[0]).contains("pmt_variance_pct").doesNotContain("pmt_variance_kg");
        assertThat(List.of(a)).anyMatch(o -> o instanceof java.math.BigDecimal b && b.compareTo(new java.math.BigDecimal("80")) == 0);
    }

    @Test
    void otherWorkflowOrOtherState_doesNothing() {
        listener.onTransition(inst("M10.TankerOutbound", "QTY_PENDING_MANAGER"), "WEIGHMENT_2", bag(fullPage()));
        listener.onTransition(inst("M10.TankerInbound", "WEIGHMENT_1"), "QC_APPROVED", bag(fullPage()));
        listener.onTransition(inst("M10.TankerInbound", "QTY_RECONCILED"), "DOC_VERIFICATION_PENDING", bag(fullPage()));
        verifyNoInteractions(jdbc);
    }

    @Test
    void noPageEntryInTheBag_writesNothing() {
        listener.onTransition(inst("M10.TankerInbound", "DOC_VERIFICATION_PENDING"), "WEIGHMENT_2", new HashMap<>());
        verifyNoInteractions(jdbc);
    }
}
