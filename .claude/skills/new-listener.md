---
description: Add a business-logic side-effect that fires when a workflow transitions. Sanctioned Java hook — the ONLY way to write custom Java for a customer feature. Use this when a workflow needs to write to another table, notify someone, or call an external system on transition.
---

# New workflow listener — the sanctioned Java hook

## When to invoke this skill

- A workflow transition needs to **write to another table** (e.g. on Parking-Scan submit, insert a row into `m10_parking_log`)
- A transition needs to **send a notification** (SMS, email, push, Slack)
- A transition needs to **call an external system** (Tally, EPFO, GSTN, weighbridge)
- A transition needs to **update a summary row** (denormalize a workflow instance's state onto its parent entity)

## When NOT to invoke this skill

- You need CRUD on a new entity → `/new-entity` (data, not Java)
- You need a new UI screen → `/new-page` (data, not Java)
- You need a new state machine → `/new-workflow` (data, not Java)
- You need to call an external system as a full 2-way integration → `/new-adapter` (the M11 pattern)

## The one rule

- One listener class per side-effect
- Under `com.idayam.m<N>.workflow.*` (never in `com.idayam.dao` or `com.idayam.service`)
- Implements `com.idayam.m0.workflow.engine.WorkflowTransitionListener`
- Uses `JdbcTemplate` directly — **NEVER** create a `JpaRepository`
- ≤ 60 lines of code (if larger, split into two listeners)
- Guards workflow code + state at the top; returns early if not applicable

## The template

```java
// src/main/java/com/idayam/m<N>/workflow/<Flow>WriteThrough.java
package com.idayam.m10.workflow;

import com.idayam.m0.workflow.engine.WorkflowTransitionListener;
import com.idayam.m0.workflow.model.WorkflowInstance;
import com.idayam.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Writes a row into m10_parking_log whenever a tanker-inbound workflow
 * enters PARKING_IN state. Idempotent — reruns are safe.
 *
 * Follows the substrate-first pattern from docs/architecture/CODE-REVIEW-develope-2026-07-11.md.
 */
@Component
public class ParkingLogWriteThrough implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ParkingLogWriteThrough.class);

    // NOT a JpaRepository. Direct JdbcTemplate.
    @Autowired private JdbcTemplate jdbc;

    @Override
    public void onTransition(WorkflowInstance instance, String previousState,
                             Map<String, Object> submittedDataBag) {

        // Guard 1 — only care about M10 workflows
        String code = instance.getWorkflowCode();
        if (code == null || !code.startsWith("M10.")) return;

        // Guard 2 — only care about the specific state
        if (!"PARKING_IN".equals(instance.getCurrentState())) return;

        // Guard 3 — must have data submitted from the parking-scan page
        Map<String, Object> parking = (Map<String, Object>) submittedDataBag.get("pico:parking-scan");
        if (parking == null) return;

        try {
            jdbc.update(
                "INSERT INTO m10_parking_log " +
                "(tenant_id, workflow_instance_id, bay_code, parking_in_time, status) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE bay_code=VALUES(bay_code), parking_in_time=VALUES(parking_in_time)",
                TenantContext.get(),
                instance.getId(),
                parking.get("bayCode"),
                Instant.now(),
                instance.getCurrentState()
            );
        } catch (Exception e) {
            // Fail-open: a listener failure MUST NOT block the workflow transition.
            // Log + move on; the workflow engine has already advanced the state.
            log.warn("ParkingLog write-through failed for instance={}: {}",
                     instance.getId(), e.getMessage());
        }
    }
}
```

## Engineering principles

- **Single side-effect per listener.** Don't write to `m10_parking_log` AND send an SMS in the same listener. Two side-effects = two listeners.
- **Fail-open on side-effect errors.** A listener failure never blocks the workflow. Log + move on. Users hate transitions failing because "SMS gateway was slow."
- **Idempotent writes.** Use `ON DUPLICATE KEY UPDATE` or `INSERT ... WHERE NOT EXISTS`. A listener that fires twice on the same event must produce the same DB state.
- **Guard workflow code AND state.** Both. A guard-only-on-code listener fires on every transition of that workflow — usually not what you want.
- **No `@Transactional` on listener methods.** The workflow engine is already inside a transaction. Nested `@Transactional` causes surprising rollback behaviour.

## SonarQube rules that apply

Everything from `docs/architecture/ENGINEERING-STANDARDS.md` plus:

| Rule | Enforcement |
|---|---|
| **S106** | No `System.out.println` — use `Logger` |
| **S138** | Method length ≤ 60 lines (tighter than universal 80) |
| **S3776** | Cognitive complexity ≤ 10 (tighter than universal 15) |
| **S107** | ≤ 5 parameters on any method |
| **Custom** | Constructor must be public no-args OR use `@Autowired` field injection consistently |
| **Custom** | No `@Repository` / `@Entity` / `JpaRepository` imports in the file |

## Coverage requirements

Every listener ships with a matching JUnit test class:

```java
// src/test/java/com/idayam/m10/workflow/ParkingLogWriteThroughTest.java
package com.idayam.m10.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingLogWriteThroughTest {

    @Mock private JdbcTemplate jdbc;
    @InjectMocks private ParkingLogWriteThrough listener;

    @Test
    void writes_when_workflow_and_state_match() {
        var instance = mock(WorkflowInstance.class);
        when(instance.getWorkflowCode()).thenReturn("M10.TankerInbound");
        when(instance.getCurrentState()).thenReturn("PARKING_IN");
        when(instance.getId()).thenReturn(42L);

        listener.onTransition(instance, "GATE_PASS_CREATED",
            Map.of("pico:parking-scan", Map.of("bayCode", "BAY-3")));

        verify(jdbc, times(1)).update(anyString(),
            eq(1L), eq(42L), eq("BAY-3"), any(), eq("PARKING_IN"));
    }

    @Test
    void skips_when_workflow_code_does_not_match() {
        var instance = mock(WorkflowInstance.class);
        when(instance.getWorkflowCode()).thenReturn("M5.PackingRun"); // wrong

        listener.onTransition(instance, "DRAFT", Map.of());

        verifyNoInteractions(jdbc);
    }

    @Test
    void skips_when_state_does_not_match() {
        var instance = mock(WorkflowInstance.class);
        when(instance.getWorkflowCode()).thenReturn("M10.TankerInbound");
        when(instance.getCurrentState()).thenReturn("WEIGHMENT_1"); // wrong

        listener.onTransition(instance, "PARKING_IN", Map.of());

        verifyNoInteractions(jdbc);
    }

    @Test
    void skips_when_no_submitted_data_from_expected_page() {
        var instance = mock(WorkflowInstance.class);
        when(instance.getWorkflowCode()).thenReturn("M10.TankerInbound");
        when(instance.getCurrentState()).thenReturn("PARKING_IN");

        listener.onTransition(instance, "GATE_PASS_CREATED",
            Map.of("pico:something-else", Map.of("x", "y")));

        verifyNoInteractions(jdbc);
    }

    @Test
    void survives_jdbc_exception_without_throwing() {
        var instance = mock(WorkflowInstance.class);
        when(instance.getWorkflowCode()).thenReturn("M10.TankerInbound");
        when(instance.getCurrentState()).thenReturn("PARKING_IN");
        when(jdbc.update(anyString(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("DB unavailable"));

        // MUST NOT throw — listener fails open
        listener.onTransition(instance, "GATE_PASS_CREATED",
            Map.of("pico:parking-scan", Map.of("bayCode", "BAY-3")));
    }
}
```

**5 tests minimum**: happy path + workflow-mismatch + state-mismatch + missing-data + jdbc-exception-fails-open.

Plus one Playwright integration test in `earmsfrontend/tests/test-agents/`:

```javascript
test("M10 workflow — parking-scan submission writes to parking_log", async ({ request }) => {
  const tok = await login(request);
  // Create workflow instance, drive to GATE_PASS_CREATED, submit parking-scan
  // Then assert /api/m0/entities/ParkingLog returns a new row
});
```

## The commit gate

If your PR adds a listener under `com.idayam.m<N>.workflow.*` without a matching test file → `.githooks/pre-commit` should flag it. (This custom check will be added in a follow-up hook update.)

## Reference

Existing listeners to read:
- `ParkingEntryWorkflowListener` — parking log write-through (Dhivya wrote this one correctly)
- `GatePassWorkflowListener` — gate-pass state updates
- `WorkflowAgentBridge` (in `com.idayam.ai`) — bridges to the AI dispatcher

Standards: `docs/architecture/ENGINEERING-STANDARDS.md`
