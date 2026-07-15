package com.aria.m0.workflow;

/**
 * Thrown by {@link PageDataReferenceResolver} when a workflow expression
 * references a page field that was never submitted (or whose page binding
 * is OUTPUT-only and no INPUT step has run yet).
 */
public class PageDataMissingException extends RuntimeException {
    public PageDataMissingException(String msg) { super(msg); }
}
