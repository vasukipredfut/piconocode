package com.aria.m0.action;

import com.aria.m0.action.dao.ActionRegistryRepository;
import com.aria.m0.action.model.ActionRegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central dispatch table for {@link ActionHandler}s. On startup it
 * collects every {@link ActionHandler} bean from the Spring context and
 * indexes by {@code handler.code()}. At request time it consults the
 * {@code action_registry} table:
 * <ul>
 *   <li>row missing → 422 {@code unknown_action}</li>
 *   <li>row present + implemented=false → 422 {@code implementation_pending}</li>
 *   <li>row present + implemented=true + no handler bean → 500 (config bug)</li>
 *   <li>row present + handler bean present → handler.handle(invocation)</li>
 * </ul>
 */
@Service
public class ActionDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ActionDispatcher.class);

    @Autowired private ActionRegistryRepository registry;
    @Autowired private List<ActionHandler> handlers;

    private final Map<String, ActionHandler> byCode = new HashMap<>();

    @PostConstruct
    public void index() {
        for (ActionHandler h : handlers) {
            ActionHandler prior = byCode.put(h.code(), h);
            if (prior != null) {
                log.warn("Multiple ActionHandlers for code={} ({} and {}) — last wins",
                        h.code(), prior.getClass().getSimpleName(), h.getClass().getSimpleName());
            }
        }
        log.info("ActionDispatcher indexed {} handlers: {}", byCode.size(), byCode.keySet());
    }

    public ActionResult dispatch(String code, ActionInvocation invocation) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("action code required");
        }
        Optional<ActionRegistryEntry> rowOpt = registry.findById(code);
        if (rowOpt.isEmpty()) {
            throw new ActionUnknownException("unknown_action: " + code);
        }
        ActionRegistryEntry row = rowOpt.get();
        if (!row.isImplemented()) {
            throw new ActionPendingException("implementation_pending: " + code
                    + " is in the catalogue but not yet wired (Round 1G.2)");
        }
        ActionHandler h = byCode.get(code);
        if (h == null) {
            throw new IllegalStateException(
                    "action_registry says implemented=true but no ActionHandler bean for code=" + code);
        }
        return h.handle(invocation);
    }

    public Map<String, ActionHandler> handlersByCode() { return Map.copyOf(byCode); }

    public static class ActionUnknownException extends RuntimeException {
        public ActionUnknownException(String m) { super(m); }
    }
    public static class ActionPendingException extends RuntimeException {
        public ActionPendingException(String m) { super(m); }
    }
}
