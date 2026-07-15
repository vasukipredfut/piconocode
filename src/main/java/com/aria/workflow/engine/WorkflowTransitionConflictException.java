package com.aria.workflow.engine;

public class WorkflowTransitionConflictException extends RuntimeException {
    public WorkflowTransitionConflictException(String msg) { super(msg); }
}
