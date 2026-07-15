package com.aria.m10.service;

import com.aria.m0.workflow.engine.WorkflowTransitionListener;
import com.aria.m0.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mirrors the M0 workflow engine's authoritative state into the M10
 * gate_pass record on every transition — but ONLY for the tanker /
 * packed-goods gate-pass flows. Pure-document flows like EwayBill
 * hit the same workflow engine but have no vehicle context, so the
 * listener MUST skip them (NOT NULL constraint on vehicle_no would
 * blow the outer transition transaction otherwise, and the caller
 * would see a misleading 401 / 500 instead of a clean OK).
 */
@Component
public class GatePassWorkflowListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(GatePassWorkflowListener.class);

    /**
     * Workflow codes we ARE authoritative for. Everything else in the
     * M10.* namespace (EwayBill, ThirdPartyIn/Out, IntTransfer*, etc.)
     * is a document flow with no gate_pass semantics.
     */
    private static final java.util.Set<String> GATE_PASS_WORKFLOWS = java.util.Set.of(
            "M10.TankerInbound",
            "M10.TankerOutbound",
            "M10.PackLoading",
            "M10.PackUnload",
            "M10.FGLoading",
            "M10.ReturnStockIn"
    );

    @Autowired private GatePassService gatePassService;

    @Override
    public void onTransition(WorkflowInstance instance, String previousState,
                             Map<String, Object> submittedDataBag) {
        String code = instance.getWorkflowCode();
        if (code == null || !GATE_PASS_WORKFLOWS.contains(code)) return;
        try {
            gatePassService.upsertForInstance(
                    instance.getId(),
                    code,
                    instance.getCurrentState(),
                    submittedDataBag);
        } catch (Exception ex) {
            // A catch here won't rescue the outer transaction — Spring
            // has already marked it for rollback the moment the JPA
            // exception fired. Log loudly so we can investigate; the
            // caller sees the tainted-transaction bubble.
            log.warn("gate_pass write-through failed for instance={} code={}: {}",
                    instance.getId(), code, ex.getMessage());
        }
    }
}
