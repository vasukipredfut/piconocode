package com.aria.workflow.engine;

import com.aria.workflow.model.WorkflowInstance;

import java.util.Map;

/**
 * Plugin point invoked by {@link WorkflowEngineService} after a transition's
 * guard has matched but BEFORE it is persisted. Beans implementing this
 * interface are auto-discovered. Throw {@link WorkflowValidationException}
 * to reject the submission — the whole transactional method rolls back
 * (nothing commits, same as a guard rejection), and the exception surfaces
 * to the caller as a clear 422 with your message.
 *
 * <p>This is the pre-commit counterpart to {@link WorkflowTransitionListener}
 * (which only ever runs after a transition has already been applied and
 * therefore can't block it). Use this interface for checks that must stop
 * the transition outright — e.g. a business-key uniqueness check a plain
 * SpEL guard can't express (guards run in a sandboxed evaluation context
 * with no DB access).
 */
public interface WorkflowTransitionValidator {

    /**
     * @param instance  the instance BEFORE this transition (currentState still the old value)
     * @param toState   the state this transition would move to
     * @param formData  the page's submitted form data for this submission
     * @throws WorkflowValidationException to reject the transition
     */
    void validate(WorkflowInstance instance, String toState, Map<String, Object> formData);
}
