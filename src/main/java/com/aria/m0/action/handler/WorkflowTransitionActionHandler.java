package com.aria.m0.action.handler;

import com.aria.m0.action.ActionHandler;
import com.aria.m0.action.ActionInvocation;
import com.aria.m0.action.ActionResult;
import com.aria.m0.workflow.dto.WorkflowTransitionResult;
import com.aria.m0.workflow.engine.WorkflowEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** {@code workflow.transition} — proxies to {@link WorkflowEngineService}.
 *  Keeps the workflow engine the SOLE state mutator (ArchUnit Rule G). */
@Component
public class WorkflowTransitionActionHandler implements ActionHandler {

    @Autowired private WorkflowEngineService engine;

    @Override public String code() { return "workflow.transition"; }

    @Override public ActionResult handle(ActionInvocation inv) {
        if (inv.getInstanceId() == null) throw new IllegalArgumentException("instanceId required");
        if (inv.getPageCode() == null) throw new IllegalArgumentException("pageCode required");
        WorkflowTransitionResult r = engine.handlePageSubmission(
                inv.getInstanceId(),
                inv.getPageCode(),
                inv.getFormData() == null ? Map.of() : inv.getFormData(),
                inv.getContext() == null ? null
                        : (inv.getContext().get("actorUserId") instanceof Number
                                ? ((Number) inv.getContext().get("actorUserId")).longValue()
                                : null));
        Map<String, Object> data = new HashMap<>();
        data.put("fromState", r.getPreviousState());
        data.put("toState", r.getNewState());
        data.put("instanceId", r.getInstanceId());
        data.put("transitioned", r.isTransitioned());
        data.put("assignedRole", r.getAssignedRole());
        return ActionResult.builder().ok(true).message("transitioned").data(data).build();
    }
}
