package com.aria.workflow.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for entity fields whose value is owned by the workflow engine.
 * Reads anywhere are fine; writes are confined to
 * {@code com.aria.workflow.engine} by ArchUnit Rule G (see
 * {@code M0ArchUnitTest}).
 *
 * <p>Applied to {@code WorkflowInstance.currentState} +
 * {@code WorkflowInstance.status} in Round 1G — these are the two
 * fields the engine alone may mutate.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WorkflowDriven {
    String reason() default "owned by workflow engine";
}
