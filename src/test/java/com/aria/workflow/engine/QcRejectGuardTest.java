package com.aria.workflow.engine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * M10.TankerOutbound's QC-sample-reject path reuses WEIGHMENT_2 / WEIGHT_DIFF_PENDING_MANAGER and tells the two
 * flows apart in the transition guards via the accumulated submission bag
 * ({@code #instance.submittedData['aria:qc-final-out']['status']}). Evaluated with the real {@link SpelGuardEvaluator}.
 */
class QcRejectGuardTest {

    static final String Q = "#instance.submittedData['aria:qc-final-out'] != null && #instance.submittedData['aria:qc-final-out']['status'] == 'REJECTED'";
    static final String NQ = "(#instance.submittedData['aria:qc-final-out'] == null || #instance.submittedData['aria:qc-final-out']['status'] != 'REJECTED')";

    /** Same shape as the engine's private InstanceViewImpl. */
    private static class View implements SpelGuardEvaluator.InstanceView {
        private final Map<String, Object> bag;
        View(Map<String, Object> bag) { this.bag = bag; }
        @Override public String getCurrentState() { return "WEIGHMENT_2"; }
        @Override public Map<String, Object> getSubmittedData() { return bag; }
    }

    private final SpelGuardEvaluator evaluator = new SpelGuardEvaluator();

    private boolean allowed(String guard, Map<String, Object> submitted, Map<String, Object> bag) {
        SpelGuardEvaluator.Outcome o = evaluator.evaluate(guard, submitted, new View(bag), 1L, 1L);
        assertThat(o.errored()).as("guard must not error: " + o.reason).isFalse();
        return o.allowed();
    }

    private static Map<String, Object> bagWithQc(String status) {
        Map<String, Object> bag = new HashMap<>();
        if (status != null) bag.put("aria:qc-final-out", Map.of("status", status));
        return bag;
    }

    @Test
    void qcRejected_detected_normalFlowNot() {
        Map<String, Object> yes = Map.of("withinTolerance", "YES");
        assertThat(allowed("#submitted['withinTolerance'] == 'YES' && (" + Q + ")", yes, bagWithQc("REJECTED"))).isTrue();
        assertThat(allowed("#submitted['withinTolerance'] == 'YES' && " + NQ, yes, bagWithQc("REJECTED"))).isFalse();

        assertThat(allowed("#submitted['withinTolerance'] == 'YES' && (" + Q + ")", yes, bagWithQc("APPROVED"))).isFalse();
        assertThat(allowed("#submitted['withinTolerance'] == 'YES' && " + NQ, yes, bagWithQc("APPROVED"))).isTrue();
    }

    @Test
    void reloadSkipsFirstWeighment_decidedByWhetherWeighmentCaptureWasEverSubmitted() {
        String first = "#submitted['status'] == 'APPROVED' && #instance.submittedData['aria:weighment-capture'] == null";
        String reload = "#submitted['status'] == 'APPROVED' && #instance.submittedData['aria:weighment-capture'] != null";
        Map<String, Object> approved = Map.of("status", "APPROVED");

        Map<String, Object> firstTime = new HashMap<>();                       // no weighment yet
        assertThat(allowed(first, approved, firstTime)).isTrue();
        assertThat(allowed(reload, approved, firstTime)).isFalse();

        Map<String, Object> afterLoad = new HashMap<>();                       // W1 captured in the earlier cycle
        afterLoad.put("aria:weighment-capture", Map.of("w1_weight_kg", "15000"));
        assertThat(allowed(first, approved, afterLoad)).isFalse();
        assertThat(allowed(reload, approved, afterLoad)).isTrue();

        assertThat(allowed(reload, Map.of("status", "REJECTED"), afterLoad)).isFalse();
    }

    @Test
    void qcSampleReject_splitsByHasLoadedStock() {
        String toPreload = "#submitted['status'] == 'REJECTED' && #submitted['has_loaded_stock'] == 'true'";
        String toWeight = "#submitted['status'] == 'REJECTED' && #submitted['has_loaded_stock'] != 'true'";
        Map<String, Object> loaded = Map.of("status", "REJECTED", "has_loaded_stock", "true");
        Map<String, Object> notLoaded = Map.of("status", "REJECTED", "has_loaded_stock", "false");
        Map<String, Object> flagMissing = Map.of("status", "REJECTED");
        Map<String, Object> pass = Map.of("status", "APPROVED", "has_loaded_stock", "true");
        assertThat(allowed(toPreload, loaded, new HashMap<>())).isTrue();
        assertThat(allowed(toWeight, loaded, new HashMap<>())).isFalse();
        assertThat(allowed(toPreload, notLoaded, new HashMap<>())).isFalse();
        assertThat(allowed(toWeight, notLoaded, new HashMap<>())).isTrue();
        // flag forgotten by the UI -> safe default: the weight-2 path, never a silent skip of both
        assertThat(allowed(toPreload, flagMissing, new HashMap<>())).isFalse();
        assertThat(allowed(toWeight, flagMissing, new HashMap<>())).isTrue();
        // a QC pass never takes either reject path
        assertThat(allowed(toPreload, pass, new HashMap<>())).isFalse();
        assertThat(allowed(toWeight, pass, new HashMap<>())).isFalse();
    }

    @Test
    void instanceThatNeverHadQcFinalOut_isNormalFlow_noError() {
        Map<String, Object> yes = Map.of("withinTolerance", "YES");
        assertThat(allowed(Q, yes, bagWithQc(null))).isFalse();
        assertThat(allowed(NQ, yes, bagWithQc(null))).isTrue();
        assertThat(allowed(NQ, yes, new HashMap<>())).isTrue();
    }

    @Test
    void managerRejectedWeight_routesByQcDecision() {
        Map<String, Object> rejected = Map.of("status", "REJECTED");
        assertThat(allowed("#submitted['status'] == 'REJECTED' && (" + Q + ")", rejected, bagWithQc("REJECTED"))).isTrue();
        assertThat(allowed("#submitted['status'] == 'REJECTED' && " + NQ, rejected, bagWithQc("REJECTED"))).isFalse();
        assertThat(allowed("#submitted['status'] == 'REJECTED' && " + NQ, rejected, bagWithQc("APPROVED"))).isTrue();
    }
}
