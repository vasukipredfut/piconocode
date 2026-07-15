package com.aria.integration.m0;

import com.aria._base.EarmsContainersExtension;
import com.aria.m0.savedview.SavedViewService;
import com.aria.m0.savedview.model.SavedView;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** IT: tenant_list_view CRUD with scope visibility. */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = M0SubstrateTestApp.class)
@ActiveProfiles("test")
class SavedViewIT {

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

    @Autowired private SavedViewService svc;

    @BeforeEach
    void setTenant() { TenantContext.set(1L); }

    @AfterEach
    void clearTenant() { TenantContext.clear(); }

    @Test
    @DisplayName("create + list TENANT-scope view visible to any user")
    void tenantScopeVisible() {
        SavedView v = svc.create("it:sv-tenant", "All Active", "TENANT", null,
                "{\"filters\":[]}", 100L);
        assertThat(v.getId()).isNotNull();
        // Different user reading TENANT-scope still sees it.
        List<SavedView> seen = svc.listVisible("it:sv-tenant", 999L);
        assertThat(seen).extracting(SavedView::getId).contains(v.getId());
    }

    @Test
    @DisplayName("USER-scope view only visible to its creator")
    void userScopeIsolated() {
        SavedView mine = svc.create("it:sv-user", "Mine", "USER", null, "{}", 100L);
        // Same user sees it.
        List<SavedView> mineSees = svc.listVisible("it:sv-user", 100L);
        assertThat(mineSees).extracting(SavedView::getId).contains(mine.getId());
        // Other user does not.
        List<SavedView> otherSees = svc.listVisible("it:sv-user", 200L);
        assertThat(otherSees).extracting(SavedView::getId).doesNotContain(mine.getId());
    }

    @Test
    @DisplayName("update + delete by USER-scope creator round-trips")
    void updateDeleteByCreator() {
        SavedView v = svc.create("it:sv-upd", "v1", "USER", null, "{\"x\":1}", 100L);
        SavedView updated = svc.update(v.getId(), "v2", "{\"x\":2}", 100L);
        assertThat(updated.getName()).isEqualTo("v2");
        assertThat(updated.getBodyJson()).contains("\"x\":2");
        svc.delete(v.getId(), 100L);
        assertThat(svc.findById(v.getId())).isEmpty();
    }

    @Test
    @DisplayName("update USER-scope by non-creator → 422")
    void updateUserScopeByOther_rejected() {
        SavedView v = svc.create("it:sv-other", "v1", "USER", null, "{}", 100L);
        assertThatThrownBy(() -> svc.update(v.getId(), "hack", null, 999L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
