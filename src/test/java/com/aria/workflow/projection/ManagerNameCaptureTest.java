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

/** manager_name (V358) from aria:manager-qty-approval lands on manager_qty_approval_detail for tanker flows. */
class ManagerNameCaptureTest {

    private static final String PAGE = "aria:manager-qty-approval";
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private static WorkflowInstance inst(String code, String state) {
        return WorkflowInstance.builder().id(2019L).workflowCode(code).currentState(state).build();
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

    @Test
    void tankerOutbound_managerNameSaved() {
        Map<String, Object> page = new HashMap<>(Map.of("status", "APPROVED", "manager_name", "Ravi Kumar",
                "manager_contact", "9876543210"));
        new PmtSharedReasonListener(jdbc).onTransition(inst("M10.TankerOutbound", "WEIGHMENT_2"), "WEIGHMENT_2", bag(page));

        Object[] a = updateCall();
        assertThat((String) a[0]).contains("INSERT INTO manager_qty_approval_detail").contains("manager_name", "manager_contact");
        assertThat(List.of(a)).contains("Ravi Kumar", "APPROVED", "9876543210");
    }

    @Test
    void tankerOutbound_noManagerName_columnNotWritten() {
        Map<String, Object> page = new HashMap<>(Map.of("status", "APPROVED"));
        new PmtSharedReasonListener(jdbc).onTransition(inst("M10.TankerOutbound", "WEIGHMENT_2"), "WEIGHMENT_2", bag(page));

        assertThat((String) updateCall()[0]).doesNotContain("manager_name");
    }

    @Test
    void tankerInbound_managerNameSaved() {
        Map<String, Object> page = new HashMap<>(Map.of("status", "APPROVED", "managerName", "Meena", "manager_contact_no", "9000000001"));
        new ManagerQtyApprovalDecisionListener(jdbc)
                .onTransition(inst("M10.TankerInbound", "QTY_APPROVED"), "QTY_PENDING_MANAGER", bag(page));

        Object[] a = updateCall();
        assertThat((String) a[0]).contains("manager_name");
        assertThat(List.of(a)).contains("Meena", "9000000001");
    }
}
