package com.aria.integration.m0;

import com.aria._base.EarmsContainersExtension;
import com.aria.entity_registry.EntityRegistryService;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IT: GET /api/m0/entity-registry — list entities + nested fields, sourced
 * from the V5 seed.
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = M0SubstrateTestApp.class)
@ActiveProfiles("test")
class EntityRegistryIT {

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

    @Autowired private EntityRegistryService svc;

    @BeforeEach
    void setTenant() { TenantContext.set(1L); }

    @AfterEach
    void clearTenant() { TenantContext.clear(); }

    @Test
    @DisplayName("V5 seed exposes the 8 catalogued entities")
    void seededEntities() {
        List<Map<String, Object>> rows = svc.listAllWithFields();
        List<String> codes = rows.stream().map(r -> (String) r.get("code")).toList();
        assertThat(codes).contains("Vendor", "City", "State", "PageDefinition", "PageVersion",
                "WorkflowDefinition", "WorkflowInstance", "SavedView");
    }

    @Test
    @DisplayName("Vendor entity carries its registered fields")
    void vendorFieldsNested() {
        List<Map<String, Object>> rows = svc.listAllWithFields();
        Map<String, Object> vendor = rows.stream()
                .filter(r -> "Vendor".equals(r.get("code"))).findFirst().orElseThrow();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) vendor.get("fields");
        List<String> fieldCodes = fields.stream().map(f -> (String) f.get("fieldCode")).toList();
        assertThat(fieldCodes).contains("name", "gstin", "city_id", "state_id", "is_msme", "status");
    }

    @Test
    @DisplayName("findByCode returns Vendor → m0_vendor table mapping")
    void findVendorByCode() {
        assertThat(svc.findByCode("Vendor")).isPresent();
        assertThat(svc.findByCode("Vendor").get().getTableName()).isEqualTo("m0_vendor");
        assertThat(svc.findByCode("UnknownEntity")).isEmpty();
    }
}
