package com.aria.m11.service;

import com.aria.m11.dao.IntegrationConnectorCatalogRepository;
import com.aria.m11.model.IntegrationConnectorCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Read-side service for the M11 connector catalog. Backed by the
 * {@link IntegrationConnectorCatalogRepository} JPA repo and Spring's
 * {@code @Cacheable} (Redis under the hood per {@code earms.cache.ttl.default-seconds}).
 *
 * <p>Round 1H.1 keeps writes platform-engineer-only via migrations; this
 * service only exposes reads.
 */
@Service
public class IntegrationCatalogService {

    public static final String CACHE_ALL = "m11.catalog.all";
    public static final String CACHE_BY_CAT = "m11.catalog.byCategory";
    public static final String CACHE_BY_CODE = "m11.catalog.byCode";

    @Autowired private IntegrationConnectorCatalogRepository repo;

    @Cacheable(CACHE_ALL)
    public List<IntegrationConnectorCatalog> listAll() {
        return repo.findAllByOrderByCategoryAscVendorAscCodeAsc();
    }

    @Cacheable(value = CACHE_BY_CAT, key = "#category")
    public List<IntegrationConnectorCatalog> listByCategory(String category) {
        return repo.findByCategoryOrderByVendorAscCodeAsc(category);
    }

    @Cacheable(value = CACHE_BY_CODE, key = "#code")
    public Optional<IntegrationConnectorCatalog> findLatestByCode(String code) {
        // Call the underlying query directly instead of repo.findLatestByCode().
        // Mockito mocks don't execute interface default methods by default —
        // calling the @Query method keeps the service unit-testable without
        // needing CALLS_REAL_METHODS or @Spy in every test.
        List<IntegrationConnectorCatalog> all = repo.findAllVersionsByCode(code);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }
}
