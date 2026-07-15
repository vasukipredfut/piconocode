package com.aria.m0.action;

import com.aria.m0.action.dao.ActionRegistryRepository;
import com.aria.m0.action.model.ActionRegistryEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST surface for Schema V2 §9 action dispatch.
 *
 * <p>Two routes:
 * <ul>
 *   <li>{@code GET /api/m0/actions} — list the catalogue (implemented +
 *       implementation_pending). Used by the Builder palette.</li>
 *   <li>{@code POST /api/m0/actions/{code}/invoke} — dispatch via the
 *       registered {@link ActionHandler}.</li>
 * </ul>
 */
@RestController("m0ActionController")
@Tag(name = "M0 Actions", description = "Schema V2 action catalogue + dispatch (Round 1G)")
public class ActionController {

    @Autowired private ActionDispatcher dispatcher;
    @Autowired private ActionRegistryRepository registry;

    @GetMapping("/api/m0/actions")
    @Operation(summary = "List all registered actions (catalogue).")
    public ResponseEntity<List<ActionRegistryEntry>> list() {
        return ResponseEntity.ok(registry.findAll());
    }

    @PostMapping("/api/m0/actions/{code}/invoke")
    @Operation(summary = "Invoke an action by code.")
    public ResponseEntity<ActionResult> invoke(@PathVariable String code,
                                               @RequestBody(required = false) ActionInvocation inv) {
        ActionResult r = dispatcher.dispatch(code, inv == null ? new ActionInvocation() : inv);
        return ResponseEntity.ok(r);
    }

    @ExceptionHandler(ActionDispatcher.ActionUnknownException.class)
    public ResponseEntity<Map<String, Object>> unknown(ActionDispatcher.ActionUnknownException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "unknown_action", "message", e.getMessage()));
    }

    @ExceptionHandler(ActionDispatcher.ActionPendingException.class)
    public ResponseEntity<Map<String, Object>> pending(ActionDispatcher.ActionPendingException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "implementation_pending", "message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> bad(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "bad_request", "message", e.getMessage()));
    }
}
