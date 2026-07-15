package com.aria.m11.service;

import com.aria.m11.dao.IntegrationInstanceRepository;
import com.aria.m11.dao.IntegrationInstanceVersionRepository;
import com.aria.m11.dto.IntegrationInstanceRequest;
import com.aria.m11.model.IntegrationConnectorCatalog;
import com.aria.m11.model.IntegrationInstance;
import com.aria.m11.model.IntegrationInstanceVersion;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the instance lifecycle service. Repos are mocked; the
 * structural-schema validator is exercised against a real catalog row.
 */
class IntegrationInstanceServiceTest {

    IntegrationInstanceService service;
    IntegrationInstanceRepository instances;
    IntegrationInstanceVersionRepository versions;
    IntegrationCatalogService catalog;
    IntegrationSecretService secrets;

    @BeforeEach
    void setUp() {
        instances = mock(IntegrationInstanceRepository.class);
        versions = mock(IntegrationInstanceVersionRepository.class);
        catalog = mock(IntegrationCatalogService.class);
        secrets = mock(IntegrationSecretService.class);
        service = new IntegrationInstanceService();
        injectField(service, "instances", instances);
        injectField(service, "versions", versions);
        injectField(service, "catalog", catalog);
        injectField(service, "secrets", secrets);
        TenantContext.set(1L);

        var msg91 = IntegrationConnectorCatalog.builder()
                .code("MSG91_SMS").version(1).category("COMMUNICATIONS").vendor("MSG91")
                .displayName("MSG91 SMS").configSchemaJson(
                        "{\"type\":\"object\",\"required\":[\"baseUrl\",\"senderId\"]," +
                        "\"properties\":{\"baseUrl\":{\"type\":\"string\"}," +
                        "\"senderId\":{\"type\":\"string\"}}}")
                .defaultPropsJson("{}").supportedOperationsJson("[\"PUSH\"]")
                .authMethodsJson("[\"API_KEY\"]").dependenciesJson("[]")
                .residencyConstraints("NONE").status("ACTIVE").build();
        when(catalog.findLatestByCode("MSG91_SMS")).thenReturn(Optional.of(msg91));
        when(catalog.findLatestByCode("UNKNOWN")).thenReturn(Optional.empty());
        AtomicLong ids = new AtomicLong(0);
        when(instances.save(any())).thenAnswer(a -> {
            IntegrationInstance i = a.getArgument(0);
            if (i.getId() == null) i.setId(ids.incrementAndGet());
            return i;
        });
        when(versions.save(any())).thenAnswer(a -> a.getArgument(0));
    }

    @AfterEach
    void clearTenant() { TenantContext.clear(); }

    @Test
    void create_happy_writesInstanceVersionAndSecret() {
        var req = IntegrationInstanceRequest.builder()
                .code("msg91Prod").connectorCode("MSG91_SMS").name("MSG91 Prod")
                .configJson(Map.of("baseUrl","https://x","senderId","PICO"))
                .credentials(Map.of("apiKey","abc")).build();
        when(instances.existsByTenantIdAndCode(1L, "msg91Prod")).thenReturn(false);

        var inst = service.create(req);

        assertThat(inst.getId()).isNotNull();
        assertThat(inst.getStatus()).isEqualTo("DRAFT");
        verify(versions).save(any(IntegrationInstanceVersion.class));
        verify(secrets).store(eq(inst.getId()), eq(Map.of("apiKey","abc")));
    }

    @Test
    void create_duplicateCode_throwsConflict() {
        when(instances.existsByTenantIdAndCode(1L, "msg91Prod")).thenReturn(true);
        var req = IntegrationInstanceRequest.builder()
                .code("msg91Prod").connectorCode("MSG91_SMS")
                .configJson(Map.of("baseUrl","x","senderId","y")).build();
        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IntegrationInstanceService.InstanceConflictException.class)
                .hasMessageContaining("M11_INSTANCE_CODE_DUPLICATE");
    }

    @Test
    void create_unknownConnector_throws422() {
        var req = IntegrationInstanceRequest.builder()
                .code("nope").connectorCode("UNKNOWN").configJson(Map.of()).build();
        when(instances.existsByTenantIdAndCode(anyLong(), any())).thenReturn(false);
        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IntegrationInstanceService.ConnectorUnknownException.class);
    }

    @Test
    void create_missingRequired_throws422() {
        // code "msg91-prod" passes the {3,48} length validator; baseUrl missing -> config error
        var req = IntegrationInstanceRequest.builder()
                .code("msg91-prod").connectorCode("MSG91_SMS")
                .configJson(Map.of("senderId","PICO")).build();
        when(instances.existsByTenantIdAndCode(anyLong(), any())).thenReturn(false);
        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IntegrationInstanceService.InvalidConfigException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void create_badCode_rejected() {
        var req = IntegrationInstanceRequest.builder()
                .code("!").connectorCode("MSG91_SMS")
                .configJson(Map.of("baseUrl","u","senderId","s")).build();
        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IntegrationInstanceService.InvalidConfigException.class)
                .hasMessageContaining("M11_INSTANCE_CODE_INVALID");
    }

    @Test
    void publish_increments_currentVersion_andWritesVersionRow() {
        var inst = IntegrationInstance.builder()
                .id(5L).tenantId(1L).code("c").connectorCode("MSG91_SMS").connectorVersion(1)
                .name("n").status("DRAFT").currentVersion(1)
                .configJson("{\"baseUrl\":\"u\",\"senderId\":\"s\"}").errorCount24h(0)
                .dataResidencyFlag("ANY").build();
        when(instances.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(inst));

        var v = service.publish(5L, "first publish", null);

        assertThat(v.getVersion()).isEqualTo(2);
        assertThat(v.getStatus()).isEqualTo("PUBLISHED");
        ArgumentCaptor<IntegrationInstance> cap = ArgumentCaptor.forClass(IntegrationInstance.class);
        verify(instances, atLeastOnce()).save(cap.capture());
        IntegrationInstance saved = cap.getValue();
        assertThat(saved.getCurrentVersion()).isEqualTo(2);
        assertThat(saved.getStatus()).isEqualTo("ENROLLED");
    }

    @Test
    void rollback_writesNewPublishedVersion_andUpdatesInstance() {
        var inst = IntegrationInstance.builder()
                .id(8L).tenantId(1L).code("c").connectorCode("MSG91_SMS").connectorVersion(1)
                .name("n").status("ACTIVE").currentVersion(3)
                .configJson("{\"baseUrl\":\"v3\",\"senderId\":\"s\"}").errorCount24h(0)
                .dataResidencyFlag("ANY").build();
        when(instances.findByIdAndTenantId(8L, 1L)).thenReturn(Optional.of(inst));
        var oldVersion = IntegrationInstanceVersion.builder()
                .id(1L).instanceId(8L).version(1)
                .configJson("{\"baseUrl\":\"v1\",\"senderId\":\"s\"}")
                .status("PUBLISHED").build();
        when(versions.findByInstanceIdAndVersion(8L, 1)).thenReturn(Optional.of(oldVersion));

        var v = service.rollback(8L, 1, 99L);

        assertThat(v.getVersion()).isEqualTo(4);
        assertThat(v.getRationale()).contains("rolled back from v1");
        assertThat(v.getConfigJson()).contains("\"v1\"");
    }

    @Test
    void enable_disable_retire_walkTheStateMachine() {
        var inst = IntegrationInstance.builder()
                .id(1L).tenantId(1L).code("c").connectorCode("MSG91_SMS").connectorVersion(1)
                .name("n").status("ENROLLED").currentVersion(1)
                .configJson("{\"baseUrl\":\"u\",\"senderId\":\"s\"}").errorCount24h(0)
                .dataResidencyFlag("ANY").build();
        when(instances.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(inst));

        assertThat(service.enable(1L).getStatus()).isEqualTo("ACTIVE");
        assertThat(service.disable(1L, "manual").getStatus()).isEqualTo("DISABLED");
        assertThat(service.retire(1L).getStatus()).isEqualTo("RETIRED");
        // After retire, enable should refuse.
        assertThatThrownBy(() -> service.enable(1L))
                .isInstanceOf(IntegrationInstanceService.InvalidLifecycleException.class);
    }

    @Test
    void listByTenant_delegates_with_tenantId() {
        when(instances.findByTenantIdOrderByCreatedAtDesc(1L)).thenReturn(java.util.List.of());
        assertThat(service.listByTenant()).isEmpty();
        verify(instances).findByTenantIdOrderByCreatedAtDesc(1L);
    }

    private static void injectField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
