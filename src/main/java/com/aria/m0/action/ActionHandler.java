package com.aria.m0.action;

/**
 * Contract every action implementation must satisfy. One implementation
 * per action code; the {@link ActionDispatcher} routes by
 * {@link #code()} return value.
 */
public interface ActionHandler {

    /** The action's catalogue code (must match {@code action_registry.code}). */
    String code();

    /** Execute the action. May throw {@link IllegalArgumentException} for
     *  invalid payloads (mapped to 422 in the controller). */
    ActionResult handle(ActionInvocation inv);
}
