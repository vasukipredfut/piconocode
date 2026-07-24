package com.aria.workflow.engine;

/** Thrown by a {@link WorkflowTransitionValidator} to reject a transition before it commits. */
public class WorkflowValidationException extends RuntimeException {
    public WorkflowValidationException(String msg) { super(msg); }
}
