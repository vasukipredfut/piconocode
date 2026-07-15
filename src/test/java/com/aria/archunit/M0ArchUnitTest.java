package com.aria.archunit;

import com.aria.workflow.engine.WorkflowDriven;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaFieldAccess;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Schema V2 §10.6 Rules G + H — enforced as ArchUnit tests so any future
 * PR that violates them fails CI.
 *
 * <h2>Rule G</h2>
 * Fields annotated {@link com.aria.workflow.engine.WorkflowDriven}
 * may only be written from classes inside the workflow engine package
 * {@code com.aria.workflow.engine}. (Read access from anywhere is
 * fine.) This guarantees the engine is the SOLE state mutator of
 * {@code WorkflowInstance.currentState} and {@code status}.
 *
 * <h2>Rule H</h2>
 * Method names matching the regex
 * {@code (?i)^(approve|reject|cancel|submit|close|finalize|publish)\\w*$}
 * are banned on {@code @Service}-annotated classes that live OUTSIDE
 * {@code com.aria.workflow.engine}. Catches the
 * "let me just add an approveGatePass() to the service layer" anti-pattern
 * (Schema V2 §1.3 table row 6).
 *
 * <p>The scan is restricted to {@code com.aria.**} so legacy
 * non-workflow service classes outside M0 are unaffected — Rule H targets
 * the new substrate, not the historical codebase that's frozen behind
 * CUSTOMIZATION-POLICY.
 */
class M0ArchUnitTest {

    private static final String ENGINE_PKG = "com.aria.workflow.engine";

    private static final java.util.regex.Pattern BANNED_NAMES =
            java.util.regex.Pattern.compile("(?i)^(approve|reject|cancel|submit|close|finalize|publish)\\w*$");

    // The substrate (formerly com.aria.m0.*) was promoted to capability-named
    // top-level packages (workflow/page/entity/action/...). Import that set.
    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.aria.workflow", "com.aria.page", "com.aria.entity",
                    "com.aria.entity_registry", "com.aria.action", "com.aria.datasource",
                    "com.aria.designer", "com.aria.savedview", "com.aria.pivot",
                    "com.aria.tenant", "com.aria.onboarding", "com.aria.audit",
                    "com.aria.document", "com.aria.reminder");

    @Test
    void ruleG_workflowDrivenFields_onlyMutatedByEnginePackage() {
        Set<JavaField> wfFields = classes.stream()
                .flatMap(c -> c.getFields().stream())
                .filter(f -> f.isAnnotatedWith(WorkflowDriven.class))
                .collect(java.util.stream.Collectors.toSet());
        assertThat(wfFields)
                .as("@WorkflowDriven should be applied to at least one entity field")
                .isNotEmpty();

        for (JavaField f : wfFields) {
            Set<JavaFieldAccess> writes = f.getAccessesToSelf().stream()
                    .filter(a -> a.getAccessType() == JavaFieldAccess.AccessType.SET)
                    .collect(java.util.stream.Collectors.toSet());
            for (JavaFieldAccess w : writes) {
                String pkg = w.getOrigin().getOwner().getPackageName();
                // Lombok-generated setters live on the entity class itself
                // (com.aria.workflow.model) and the engine package
                // invokes them — that's the legitimate path. We allow:
                //   - any class in com.aria.workflow.engine.*
                //   - the entity's own constructor / lifecycle hooks
                //     (same package as the field declaration)
                boolean okEngine = pkg.startsWith(ENGINE_PKG);
                boolean okSelf = pkg.equals(f.getOwner().getPackageName());
                assertThat(okEngine || okSelf)
                        .as("Field %s.%s is @WorkflowDriven but is mutated from %s",
                                f.getOwner().getName(), f.getName(), w.getOrigin().getFullName())
                        .isTrue();
            }
        }
    }

    /**
     * Allow-list of @Service classes where a "workflow-shaped" verb has a
     * legitimate non-workflow meaning (publish a page version, etc.).
     * Adding to this list requires PR review per CUSTOMIZATION-POLICY.
     */
    private static final Set<String> RULE_H_ALLOW = Set.of(
            "com.aria.page.service.PageDefinitionService" // publish() = page-version publish
    );

    @Test
    void ruleH_workflowVerbsBannedOnServicesOutsideEngine() {
        for (JavaClass c : classes) {
            if (!c.isAnnotatedWith(org.springframework.stereotype.Service.class)) continue;
            if (c.getPackageName().startsWith(ENGINE_PKG)) continue; // engine is the legitimate home
            if (RULE_H_ALLOW.contains(c.getName())) continue;
            for (JavaMethod m : c.getMethods()) {
                if (BANNED_NAMES.matcher(m.getName()).matches()) {
                    throw new AssertionError(String.format(
                            "Rule H violation: method %s on @Service %s — workflow verbs must live in %s",
                            m.getName(), c.getName(), ENGINE_PKG));
                }
            }
        }
    }
}
