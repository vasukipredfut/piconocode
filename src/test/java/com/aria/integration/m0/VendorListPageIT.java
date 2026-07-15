package com.aria.integration.m0;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria._base.EarmsContainersExtension;
import com.aria.m0.page.service.PageRendererService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IT: GET /api/m0/pages/pico:vendor-list returns the v2 body json with
 * kind=list and the joined columns/filters payload. We exercise via the
 * renderer service (the controller wires to the same path).
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = M0SubstrateTestApp.class)
@ActiveProfiles("test")
class VendorListPageIT {

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

    @Autowired private PageRendererService renderer;

    @BeforeEach
    void setTenant() { TenantContext.set(1L); }

    @AfterEach
    void clearTenant() { TenantContext.clear(); }

    @Test
    @DisplayName("pico:vendor-list page exists with Schema V2 list body")
    void vendorListPagePresent() throws Exception {
        Optional<?> served = renderer.loadPublishedFor(1L, "pico:vendor-list");
        assertThat(served).isPresent();
        // bodyJson should contain kind=list and the joined select array.
        ObjectMapper json = new ObjectMapper();
        com.aria.m0.page.dto.PageResponse resp = (com.aria.m0.page.dto.PageResponse) served.get();
        String body = resp.getBodyJson();
        JsonNode tree = json.readTree(body);
        assertThat(tree.get("kind").asText()).isEqualTo("list");
        assertThat(tree.get("schemaVersion").asInt()).isEqualTo(2);
        assertThat(tree.get("dataSource").get("entity").asText()).isEqualTo("Vendor");
        assertThat(tree.get("columns").isArray()).isTrue();
        assertThat(tree.get("filters").isArray()).isTrue();
        // Cascading filter: city.dependsOn references stateName.
        boolean cascading = false;
        for (JsonNode f : tree.get("filters")) {
            if ("cityName".equals(f.get("field").asText()) && f.has("dependsOn")) {
                cascading = true;
                break;
            }
        }
        assertThat(cascading).as("city filter must declare dependsOn stateName").isTrue();
    }

    @Test
    @DisplayName("pico:vendor-detail page exists with kind=detail + tabs")
    void vendorDetailPagePresent() throws Exception {
        Optional<?> served = renderer.loadPublishedFor(1L, "pico:vendor-detail");
        assertThat(served).isPresent();
        ObjectMapper json = new ObjectMapper();
        JsonNode tree = json.readTree(
                ((com.aria.m0.page.dto.PageResponse) served.get()).getBodyJson());
        assertThat(tree.get("kind").asText()).isEqualTo("detail");
        assertThat(tree.get("tabs").isArray()).isTrue();
        assertThat(tree.get("tabs").size()).isGreaterThanOrEqualTo(2);
        assertThat(tree.get("actionToolbar").isArray()).isTrue();
    }
}
