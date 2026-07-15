package com.aria.workflow.engine;

public class WorkflowInstanceNotFoundException extends RuntimeException {
    public WorkflowInstanceNotFoundException(String msg) { super(msg); }
}
