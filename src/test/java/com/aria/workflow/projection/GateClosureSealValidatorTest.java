package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowValidationException;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GateClosureSealValidatorTest {

    private JdbcTemplate jdbc;
    private GateClosureSealValidator validator;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        validator = new GateClosureSealValidator(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private static WorkflowInstance inst(String code) {
        return WorkflowInstance.builder().id(2028L).workflowCode(code).currentState("READY_FOR_EXIT").build();
    }

    private static Map<String, Object> form(String verified, String entered, String newSeal) {
        Map<String, Object> m = new HashMap<>();
        if (verified != null) m.put("seal_verified", verified);
        if (entered != null) m.put("entered_seal_no", entered);
        if (newSeal != null) m.put("new_seal_no", newSeal);
        return m;
    }

    private void recorded(String table, String seal) {
        when(jdbc.queryForList(contains("FROM " + table), eq(String.class), any(Object[].class)))
                .thenReturn(seal == null ? List.of() : List.of(seal));
    }

    @Test
    void matchingSeal_passes_caseAndSpacesIgnored() {
        recorded("weighment_capture_detail", "SL-1001");
        validator.validate(inst("M10.TankerOutbound"), "GATE_PASS_OUT_CLOSED", form("MATCHED", "  sl-1001 ", null));
    }

    @Test
    void wrongSeal_refused() {
        recorded("weighment_capture_detail", "SL-1001");
        assertThatThrownBy(() -> validator.validate(inst("M10.TankerOutbound"), "GATE_PASS_OUT_CLOSED", form("MATCHED", "SL-9999", null)))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("SL-9999").hasMessageContaining("SL-1001");
    }

    @Test
    void recordedSealFallsBackToSealingProcess() {
        recorded("weighment_capture_detail", null);
        recorded("sealing_process_detail", "SP-77");
        validator.validate(inst("M10.TankerOutbound"), "GATE_PASS_OUT_CLOSED", form("MATCHED", "SP-77", null));
    }

    @Test
    void mismatchReported_requiresNewSealNo() {
        assertThatThrownBy(() -> validator.validate(inst("M10.TankerOutbound"), "GATE_PASS_OUT_CLOSED", form("MISMATCH", "X", null)))
                .isInstanceOf(WorkflowValidationException.class).hasMessageContaining("new seal number");
        validator.validate(inst("M10.TankerOutbound"), "GATE_PASS_OUT_CLOSED", form("MISMATCH", "X", "SL-2002"));
        verifyNoInteractions(jdbc);
    }

    @Test
    void notApplicable_skipsCheck() {
        validator.validate(inst("M10.TankerOutbound"), "GATE_PASS_OUT_CLOSED", form("NA", null, null));
        verifyNoInteractions(jdbc);
    }

    @Test
    void matchedButNothingEnteredOrNothingRecorded_refused() {
        assertThatThrownBy(() -> validator.validate(inst("M10.TankerOutbound"), "GATE_PASS_OUT_CLOSED", form("MATCHED", null, null)))
                .isInstanceOf(WorkflowValidationException.class).hasMessageContaining("Enter the seal number");
        recorded("weighment_capture_detail", null);
        recorded("sealing_process_detail", null);
        assertThatThrownBy(() -> validator.validate(inst("M10.TankerOutbound"), "GATE_PASS_OUT_CLOSED", form("MATCHED", "S1", null)))
                .isInstanceOf(WorkflowValidationException.class).hasMessageContaining("No seal number is recorded");
    }

    @Test
    void otherWorkflowOrState_orNoSealField_doNothing() {
        validator.validate(inst("M10.FGLoading"), "GATE_PASS_OUT_CLOSED", form("MATCHED", "S", null));
        validator.validate(inst("M10.TankerOutbound"), "WEIGHT_APPROVAL_PENDING", form("MATCHED", "S", null));
        validator.validate(inst("M10.TankerOutbound"), "GATE_PASS_OUT_CLOSED", new HashMap<>());
        verifyNoInteractions(jdbc);
        assertThat(true).isTrue();
    }
}
