package com.aria.integration.action;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.integration.adapter.OutboundResult;
import com.aria.integration.dao.IntegrationInstanceRepository;
import com.aria.integration.model.IntegrationInstance;
import com.aria.integration.service.IntegrationOutboundCaller;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implements the {@code external.api.call} action wired in {@code action_registry}.
 *
 * <p>Workflow YAML invocation example:
 * <pre>
 * actions:
 *   - external.api.call:
 *       instance: "msg91Prod"
 *       op: "POST /sendmessage"
 *       payload:
 *         body: { mobile: "...", text: "..." }
 * </pre>
 *
 * <p>The ActionInvocation envelope already supports `formData`,
 * `params`, `context` — we pull instance/op/payload from any of those for
 * flexibility (page-driven call also possible via `context`).
 */
@Component
public class ExternalApiCallActionHandler implements ActionHandler {

    @Autowired private IntegrationOutboundCaller caller;
    @Autowired private IntegrationInstanceRepository instances;

    @Override public String code() { return "external.api.call"; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> source = inv.getFormData();
        if (source == null || source.isEmpty()) source = inv.getParams();
        if (source == null) source = Map.of();

        String instanceCode = asString(source.get("instance"));
        String op = asString(source.get("op"));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = source.get("payload") instanceof Map<?,?> m
                ? (Map<String, Object>) m : Map.of();

        if (instanceCode == null) {
            throw new IllegalArgumentException("external.api.call: 'instance' (code) required");
        }
        if (op == null) {
            throw new IllegalArgumentException("external.api.call: 'op' (METHOD /path) required");
        }
        Optional<IntegrationInstance> instOpt =
                instances.findByTenantIdAndCode(TenantContext.get(), instanceCode);
        if (instOpt.isEmpty()) {
            throw new IllegalArgumentException("external.api.call: unknown instance '" + instanceCode + "'");
        }
        OutboundResult r = caller.invoke(instOpt.get().getId(), op, payload);
        Map<String, Object> data = new HashMap<>();
        data.put("ok", r.isOk());
        data.put("status", r.getStatus());
        data.put("body", r.getBody());
        data.put("errorCode", r.getErrorCode());
        data.put("errorMessage", r.getErrorMessage());
        return ActionResult.builder()
                .ok(r.isOk())
                .message(r.isOk() ? "external_call_ok" : "external_call_failed")
                .data(data)
                .build();
    }

    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
