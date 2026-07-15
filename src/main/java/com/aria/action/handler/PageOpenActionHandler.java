package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** {@code page.open} — server-side no-op; returns the navigation directive
 *  for the frontend to honour. Centralised so the audit trail logs the
 *  navigation alongside other actions. */
@Component
public class PageOpenActionHandler implements ActionHandler {

    @Override public String code() { return "page.open"; }

    @Override public ActionResult handle(ActionInvocation inv) {
        if (inv.getPage() == null) throw new IllegalArgumentException("page required");
        Map<String, Object> nps = new HashMap<>();
        nps.put("navigate", "page.open");
        nps.put("page", inv.getPage());
        if (inv.getParams() != null) nps.put("params", inv.getParams());
        return ActionResult.builder().ok(true).message("navigate").nextPageState(nps).build();
    }
}
