package com.aria.integration.service;

import com.aria.integration.dao.IntegrationConnectorCatalogRepository;
import com.aria.integration.model.IntegrationConnectorCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class IntegrationCatalogServiceTest {

    IntegrationCatalogService svc;
    IntegrationConnectorCatalogRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        repo = mock(IntegrationConnectorCatalogRepository.class);
        svc = new IntegrationCatalogService();
        var f = IntegrationCatalogService.class.getDeclaredField("repo");
        f.setAccessible(true); f.set(svc, repo);
    }

    @Test
    void listAll_delegates() {
        when(repo.findAllByOrderByCategoryAscVendorAscCodeAsc()).thenReturn(List.of(
                IntegrationConnectorCatalog.builder().code("A").version(1).build(),
                IntegrationConnectorCatalog.builder().code("B").version(1).build()));
        assertThat(svc.listAll()).hasSize(2);
    }

    @Test
    void listByCategory_delegates() {
        when(repo.findByCategoryOrderByVendorAscCodeAsc("COMMUNICATIONS"))
                .thenReturn(List.of(IntegrationConnectorCatalog.builder().code("MSG91_SMS").version(1).build()));
        assertThat(svc.listByCategory("COMMUNICATIONS")).extracting("code").containsExactly("MSG91_SMS");
    }

    @Test
    void findLatestByCode_propagatesEmpty() {
        when(repo.findAllVersionsByCode("X")).thenReturn(List.of());
        assertThat(svc.findLatestByCode("X")).isEmpty();
    }

    @Test
    void findLatestByCode_returnsTop() {
        when(repo.findAllVersionsByCode("X")).thenReturn(List.of(
                IntegrationConnectorCatalog.builder().code("X").version(2).build(),
                IntegrationConnectorCatalog.builder().code("X").version(1).build()));
        Optional<IntegrationConnectorCatalog> top = svc.findLatestByCode("X");
        assertThat(top).isPresent();
        assertThat(top.get().getVersion()).isEqualTo(2);
    }
}
