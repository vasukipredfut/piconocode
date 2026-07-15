package com.aria.m0.workflow.engine;

import com.aria.m0.workflow.model.WorkflowInstance;

import java.util.Map;

/**
 * Plugin point invoked by {@link WorkflowEngineService} after a successful
 * state transition. Beans implementing this interface are auto-discovered;
 * exceptions are caught and logged, never propagated (a listener cannot
 * fail a workflow transition).
 *
 * <p>The primary use case is wiring the engine's authoritative workflow
 * state into module-specific business records (e.g. {@code gate_pass.status}
 * in M10) without coupling the engine to those modules.
 */
public interface WorkflowTransitionListener {

    /**
     * Called once per transition, after the instance + event are persisted.
     *
     * @param instance         the post-transition workflow instance
     * @param previousState    the state code BEFORE this transition
     * @param submittedDataBag the full submitted-data bag (Map of
     *                         pageCode-&gt;formData) — listeners read from
     *                         it; mutations are ignored
     */
    void onTransition(WorkflowInstance instance, String previousState,
                      Map<String, Object> submittedDataBag);
}
