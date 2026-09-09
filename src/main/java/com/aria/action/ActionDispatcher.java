package com.aria.action;

import com.aria.access.PagePermissionResolver;
import com.aria.action.dao.ActionRegistryRepository;
import com.aria.action.model.ActionRegistryEntry;
import com.aria.security.EntityWriteGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @Autowired private EntityWriteGuard writeGuard;
    @Autowired private PagePermissionResolver pageGuard;

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
        // entity_registry.write_role enforcement. GenericEntityController
        // already checks this for its own /api/m0/entities/{code} CRUD, but
        // every entity-mutating ActionHandler (save, gatepass.create, delete,
        // entity.update-field, ...) reaches the same tables through this
        // dispatcher instead, and was found to skip the check entirely — a
        // role excluded from an entity's write_role could still write it via
        // any page whose button fires one of these actions. Only actions
        // that actually name an entity are checked; workflow.transition and
        // other entity-less actions are unaffected here.
        String entityCode = resolveEntityCode(invocation);
        if (entityCode != null) {
            ResponseEntity<?> denied = writeGuard.check(entityCode, "write");
            if (denied != null) {
                throw new ActionForbiddenException(bodyOf(denied));
            }
        }
        // role_page_permission enforcement (role-based only, no per-employee
        // override -- see PagePermissionResolver). Covers workflow.transition
        // (the "Save"/"Submit" button on nearly every workflow-driven page in
        // this app) — the only entity-less write action, so entity_registry
        // can't gate it. See PagePermissionResolver for why "no grant
        // configured yet" resolves to allowed rather than denied: the table
        // starts out empty in every environment.
        if (invocation != null && invocation.getPageCode() != null) {
            ResponseEntity<?> denied = pageGuard.check(invocation.getPageCode(), pageAccessFor(code, invocation));
            if (denied != null) {
                throw new ActionForbiddenException(bodyOf(denied));
            }
        }
        return h.handle(invocation);
    }

    /** "save" (creating a brand-new record) vs "edit" (updating an existing
     *  one) for the page-level permission check. workflow.transition always
     *  operates on an existing {@code instanceId} (required by
     *  WorkflowTransitionActionHandler), so it's always an edit of that
     *  instance's state. Anything else that carries a pageCode is judged the
     *  same way GenericEntityController/EntityWriter already distinguish
     *  create from update: no recordId yet means this is the first save. */
    private String pageAccessFor(String code, ActionInvocation inv) {
        if ("workflow.transition".equals(code)) return "edit";
        return inv.getRecordId() == null ? "save" : "edit";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> bodyOf(ResponseEntity<?> denied) {
        return (Map<String, Object>) denied.getBody();
    }

    /** The entity code an invocation targets, or null when the action isn't
     *  entity-scoped. Mirrors how EntityUpdateFieldActionHandler/
     *  EntityBulkUpdateFieldActionHandler themselves resolve it (args.entity
     *  wins over the top-level field) so the guard checks whatever the
     *  handler will actually write to. */
    private String resolveEntityCode(ActionInvocation inv) {
        if (inv == null) return null;
        if (inv.getArgs() != null && inv.getArgs().get("entity") != null) {
            return String.valueOf(inv.getArgs().get("entity"));
        }
        return inv.getEntity();
    }

    public Map<String, ActionHandler> handlersByCode() { return Map.copyOf(byCode); }

    public static class ActionUnknownException extends RuntimeException {
        public ActionUnknownException(String m) { super(m); }
    }
    public static class ActionPendingException extends RuntimeException {
        public ActionPendingException(String m) { super(m); }
    }
    public static class ActionForbiddenException extends RuntimeException {
        private final Map<String, Object> body;
        public ActionForbiddenException(Map<String, Object> body) {
            super(body == null ? "forbidden" : String.valueOf(body.get("error")));
            this.body = body;
        }
        public Map<String, Object> getBody() { return body; }
    }
}
