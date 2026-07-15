package com.aria.integration.m0;

import com.aria._base.EarmsContainersExtension;
import com.aria.m0.action.ActionDispatcher;
import com.aria.m0.action.ActionInvocation;
import com.aria.m0.action.ActionResult;
import com.aria.m0.datasource.DataSourceSpec;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * IT: invoke each of the 7 implemented actions end-to-end via the
 * dispatcher against the real DB. The 23 pending actions return
 * implementation_pending; we sample-check one.
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = M0SubstrateTestApp.class)
@ActiveProfiles("test")
class ActionDispatchIT {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", EarmsContainersExtension::mariadbJdbcUrl);
        r.add("spring.datasource.username", EarmsContainersExtension::mariadbUsername);
        r.add("spring.datasource.password", EarmsContainersExtension::mariadbPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MariaDBDialect");
        r.add("spring.redis.host", EarmsContainersExtension::redisHost);
        r.add("spring.redis.port", EarmsContainersExtension::redisPort);
        r.add("spring.data.redis.host", EarmsContainersExtension::redisHost);
        r.add("spring.data.redis.port", EarmsContainersExtension::redisPort);
    }

    @Autowired private ActionDispatcher dispatcher;

    @BeforeEach
    void setTenant() { TenantContext.set(1L); }

    @AfterEach
    void clearTenant() { TenantContext.clear(); }

    @Test
    @DisplayName("save: inserts SavedView row, returns generated id")
    void saveAction() {
        ActionResult r = dispatcher.dispatch("save", ActionInvocation.builder()
                .entity("SavedView")
                .formData(Map.of(
                        "name", "it-save-1",
                        "scope", "TENANT",
                        "page_slug", "it:dispatch",
                        "body_json", "{}"))
                .build());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getRecordId()).isNotNull();
    }

    @Test
    @DisplayName("save.and.close: emits page-back navigate directive")
    void saveAndClose() {
        ActionResult r = dispatcher.dispatch("save.and.close", ActionInvocation.builder()
                .entity("SavedView")
                .formData(Map.of("name", "it-save-close", "scope", "USER",
                        "page_slug", "it:dispatch", "body_json", "{}"))
                .build());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getNextPageState()).containsEntry("navigate", "page.back");
    }

    @Test
    @DisplayName("save.and.new: emits clearForm directive")
    void saveAndNew() {
        ActionResult r = dispatcher.dispatch("save.and.new", ActionInvocation.builder()
                .entity("SavedView")
                .formData(Map.of("name", "it-save-new", "scope", "USER",
                        "page_slug", "it:dispatch", "body_json", "{}"))
                .build());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getNextPageState()).containsEntry("clearForm", true);
    }

    @Test
    @DisplayName("delete: soft-deletes SavedView (no status col → hard delete)")
    void deleteAction() {
        // First create a row so we have something to delete.
        ActionResult create = dispatcher.dispatch("save", ActionInvocation.builder()
                .entity("SavedView")
                .formData(Map.of("name", "it-del", "scope", "USER",
                        "page_slug", "it:dispatch", "body_json", "{}"))
                .build());
        Long id = ((Number) create.getRecordId()).longValue();
        ActionResult del = dispatcher.dispatch("delete", ActionInvocation.builder()
                .entity("SavedView").recordId(id).build());
        assertThat(del.isOk()).isTrue();
    }

    @Test
    @DisplayName("page.open: returns navigate=page.open with payload")
    void pageOpenAction() {
        ActionResult r = dispatcher.dispatch("page.open", ActionInvocation.builder()
                .page("pico:vendor-detail").params(Map.of("id", 1)).build());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getNextPageState()).containsEntry("page", "pico:vendor-detail");
    }

    @Test
    @DisplayName("export.csv: returns CSV text + row count via DataSource compiler")
    void exportCsvAction() {
        ActionResult r = dispatcher.dispatch("export.csv", ActionInvocation.builder()
                .spec(DataSourceSpec.builder()
                        .type("entity").entity("Vendor")
                        .select(java.util.List.of("name", "status")).build())
                .build());
        assertThat(r.isOk()).isTrue();
        String csv = (String) r.getData().get("csv");
        assertThat(csv).contains("name,status");
        assertThat(((Number) r.getData().get("rowCount")).intValue()).isGreaterThanOrEqualTo(20);
    }

    @Test
    @DisplayName("workflow.transition: dispatcher routes to engine; missing instance → 422")
    void workflowTransition_missingInstance() {
        // We don't seed a live workflow instance in this IT (Round 1F's
        // WorkflowEngineIT covers the happy path). Confirm the handler is
        // wired and validates required params: a missing instanceId / pageCode
        // yields 422.
        assertThatThrownBy(() -> dispatcher.dispatch("workflow.transition",
                ActionInvocation.builder().build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("implementation_pending action returns ActionPendingException")
    void pendingAction() {
        // V7_16 flipped 14 actions (incl. 'print') to implemented=true. Use
        // export.pdf which is still genuinely unimplemented (POI/iText
        // generators deferred — Round 1G ships export.csv).
        assertThatThrownBy(() -> dispatcher.dispatch("export.pdf", new ActionInvocation()))
                .isInstanceOf(ActionDispatcher.ActionPendingException.class);
    }
}
