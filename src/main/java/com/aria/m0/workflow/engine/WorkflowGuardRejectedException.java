package com.aria.m0.workflow.engine;

/**
 * Thrown when a transition was definitionally eligible (state + page match
 * a transition row) but every candidate's SpEL guard evaluated false.
 *
 * Distinct from {@link WorkflowGuardErrorException} (guard threw) and
 * {@link WorkflowTransitionNotFoundException} (no transition rows at all).
 *
 * Maps to HTTP 409 Conflict in {@code WorkflowController} so clients
 * (UI + test agents) can't confuse it with the silent-no-op HTTP 200
 * that the engine previously returned. The {@code reason} carries the
 * guard expression(s) tested and the form keys submitted, so the caller
 * can diagnose at a glance.
 */
public class WorkflowGuardRejectedException extends RuntimeException {
    private final String currentState;
    private final String pageCode;
    private final String triedGuards;

    public WorkflowGuardRejectedException(String currentState, String pageCode,
                                          String triedGuards, String submittedFields) {
        super("All guards rejected the transition. state=" + currentState
                + " page=" + pageCode
                + " tried-guards=[" + triedGuards + "]"
                + " submitted-fields=[" + submittedFields + "]");
        this.currentState = currentState;
        this.pageCode = pageCode;
        this.triedGuards = triedGuards;
    }

    public String getCurrentState() { return currentState; }
    public String getPageCode() { return pageCode; }
    public String getTriedGuards() { return triedGuards; }
}
