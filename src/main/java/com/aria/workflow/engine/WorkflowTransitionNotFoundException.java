package com.aria.workflow.engine;

public class WorkflowTransitionNotFoundException extends RuntimeException {
    public WorkflowTransitionNotFoundException(String msg) { super(msg); }
}
