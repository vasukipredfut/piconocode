package com.aria.ai;

import com.aria.m0.workflow.engine.WorkflowTransitionListener;
import com.aria.m0.workflow.model.WorkflowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Bridges the existing {@link WorkflowTransitionListener} substrate to the AI dispatcher.
 *
 * <p>On every workflow transition, look up any {@code ai_agent_binding} rows for
 * {@code trigger_kind='workflow.transition'} matching this workflow+state, and
 * invoke each bound agent via {@link AiAgentDispatcher}.
 *
 * <p>This is what makes an AI agent bindable to a workflow through a single SQL row —
 * no Java change needed to add a new binding.
 */
@Component
public class WorkflowAgentBridge implements WorkflowTransitionListener {

    private final AiAgentDispatcher dispatcher;
    private final JdbcTemplate jdbc;

    @Autowired
    public WorkflowAgentBridge(AiAgentDispatcher dispatcher, JdbcTemplate jdbc) {
        this.dispatcher = dispatcher;
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState,
                             Map<String, Object> submittedDataBag) {
        String triggerRef = instance.getWorkflowCode() + ":" + instance.getCurrentState();
        try {
            for (Map<String, Object> binding : jdbc.queryForList(
                    "SELECT b.agent_id, a.code AS agent_code " +
                    "FROM ai_agent_binding b JOIN ai_agent a ON a.id=b.agent_id " +
                    "WHERE b.tenant_id=? AND b.trigger_kind='workflow.transition' " +
                    "  AND b.trigger_ref=? AND b.active=TRUE AND a.status='PUBLISHED'",
                    instance.getTenantId(), triggerRef)) {

                String code = (String) binding.get("agent_code");
                dispatcher.invoke(code, submittedDataBag,
                                  instance.getId(), null,
                                  "workflow." + triggerRef);
            }
        } catch (Exception e) {
            // Fail-open — an AI dispatch failure must not break workflow transitions.
        }
    }
}
