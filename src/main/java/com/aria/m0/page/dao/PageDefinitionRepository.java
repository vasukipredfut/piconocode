package com.aria.m0.page.dao;

import com.aria.m0.page.model.PageDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PageDefinitionRepository extends JpaRepository<PageDefinition, Long> {

    Optional<PageDefinition> findByTenantIdAndCode(Long tenantId, String code);

    Page<PageDefinition> findByTenantId(Long tenantId, Pageable pageable);

    Page<PageDefinition> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);

    Page<PageDefinition> findByTenantIdAndCategory(Long tenantId, String category, Pageable pageable);

    Page<PageDefinition> findByTenantIdAndCategoryAndStatus(
            Long tenantId, String category, String status, Pageable pageable);

    /** Used by the sidebar to hide ARCHIVED pages from the default listing. */
    Page<PageDefinition> findByTenantIdAndStatusNot(Long tenantId, String status, Pageable pageable);
}
