package com.aria.m11.dao;

import com.aria.m11.model.IntegrationConnectorCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationConnectorCatalogRepository
        extends JpaRepository<IntegrationConnectorCatalog, IntegrationConnectorCatalog.Key> {

    List<IntegrationConnectorCatalog> findAllByOrderByCategoryAscVendorAscCodeAsc();

    List<IntegrationConnectorCatalog> findByCategoryOrderByVendorAscCodeAsc(String category);

    @Query("SELECT c FROM IntegrationConnectorCatalog c WHERE c.code = :code " +
            "ORDER BY c.version DESC")
    List<IntegrationConnectorCatalog> findAllVersionsByCode(@Param("code") String code);

    default Optional<IntegrationConnectorCatalog> findLatestByCode(String code) {
        List<IntegrationConnectorCatalog> all = findAllVersionsByCode(code);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }
}
