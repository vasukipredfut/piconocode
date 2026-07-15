package com.aria.workflow;

import com.aria.workflow.engine.SpelGuardEvaluator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the SpEL guard evaluator. The key safety properties:
 *   - empty / null guard => ALLOW (no transition is gated).
 *   - reflective access via T(...) is BLOCKED (SimpleEvaluationContext refuses).
 *   - reachable variables: #submitted, #instance.submittedData, #user, #tenant.
 */
class SpelGuardEvaluatorTest {

    private final SpelGuardEvaluator evaluator = new SpelGuardEvaluator();

    private SpelGuardEvaluator.InstanceView view(String state, Map<String, Object> bag) {
        return new SpelGuardEvaluator.InstanceView() {
            @Override public String getCurrentState() { return state; }
            @Override public Map<String, Object> getSubmittedData() { return bag; }
        };
    }

    @Test
    void nullGuardAllows() {
        var r = evaluator.evaluate(null, Map.of(), view("X", Map.of()), 1L, 1L);
        assertThat(r.allowed()).isTrue();
    }

    @Test
    void simpleEqualityOnSubmittedTrue() {
        var r = evaluator.evaluate("#submitted['status'] == 'APPROVED'",
                Map.of("status", "APPROVED"), view("X", Map.of()), 1L, 1L);
        assertThat(r.allowed()).isTrue();
    }

    @Test
    void simpleEqualityOnSubmittedFalse() {
        var r = evaluator.evaluate("#submitted['status'] == 'APPROVED'",
                Map.of("status", "REJECTED"), view("X", Map.of()), 1L, 1L);
        assertThat(r.allowed()).isFalse();
        assertThat(r.errored()).isFalse();
    }

    @Test
    void instanceSubmittedDataAccess() {
        Map<String, Object> bag = Map.of("pico:qc-approval", Map.of("status", "APPROVED"));
        var r = evaluator.evaluate("#instance.submittedData['pico:qc-approval']['status'] == 'APPROVED'",
                Map.of(), view("DISPATCH_QUEUED", bag), 1L, 1L);
        assertThat(r.allowed()).isTrue();
    }

    @Test
    void tenantVariableAvailable() {
        var r = evaluator.evaluate("#tenant == 2",
                Map.of(), view("X", Map.of()), 1L, 2L);
        assertThat(r.allowed()).isTrue();
    }

    @Test
    void typeReferenceBlocked() {
        // T(...) on SimpleEvaluationContext throws SpelEvaluationException -> ERROR.
        var r = evaluator.evaluate("T(java.lang.Runtime).getRuntime() != null",
                Map.of(), view("X", Map.of()), 1L, 1L);
        assertThat(r.errored()).isTrue();
    }

    @Test
    void constructorBlocked() {
        var r = evaluator.evaluate("new java.lang.ProcessBuilder('ls').start() != null",
                Map.of(), view("X", Map.of()), 1L, 1L);
        assertThat(r.errored()).isTrue();
    }

    @Test
    void parseErrorReturnsError() {
        var r = evaluator.evaluate("not a real spel expression ((( !!! ",
                Map.of(), view("X", Map.of()), 1L, 1L);
        assertThat(r.errored()).isTrue();
    }

    @Test
    void nonBooleanResultDenies() {
        var r = evaluator.evaluate("42",
                Map.of(), view("X", Map.of()), 1L, 1L);
        assertThat(r.allowed()).isFalse();
        assertThat(r.errored()).isFalse();
    }
}
