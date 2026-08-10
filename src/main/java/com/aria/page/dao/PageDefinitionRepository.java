package com.aria.page.dao;

import com.aria.page.model.PageDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageDefinitionRepository extends JpaRepository<PageDefinition, Long> {

    Optional<PageDefinition> findByTenantIdAndCode(Long tenantId, String code);

    // ── global (-1) + tenant-override resolution ────────────────────────────
    // The tenant's own row wins; otherwise the global product row is served.
    // See TenantContext.GLOBAL_TENANT_ID for why -1.

    /** Candidates for one code, tenant override first (real ids are > -1). */
    @Query("SELECT p FROM PageDefinition p "
         + "WHERE p.code = :code AND p.tenantId IN (:tenantId, -1) "
         + "ORDER BY p.tenantId DESC")
    List<PageDefinition> findCodeCandidates(@Param("tenantId") Long tenantId,
                                            @Param("code") String code);

    /** Resolve a single page: tenant override if present, else the global row. */
    default Optional<PageDefinition> resolveByCode(Long tenantId, String code) {
        return findCodeCandidates(tenantId, code).stream().findFirst();
    }

    /**
     * The resolved catalogue for a tenant: every row they own, plus every global
     * row they have NOT overridden.
     *
     * <p>The NOT EXISTS clause is what stops a page appearing twice — once from
     * the tenant and once from global — which would double every overridden page
     * in the sidebar.
     *
     * <p>All filters are optional; pass null to skip one. This single query backs
     * every listing variant, because EVERY listing has to resolve. An unresolved
     * list is not a cosmetic bug: once the catalogue lives at -1, a tenant-only
     * query returns just their handful of overrides and the sidebar looks empty.
     */
    @Query("SELECT p FROM PageDefinition p "
         + "WHERE p.tenantId IN (:tenantId, -1) "
         + "  AND (:category IS NULL OR p.category = :category) "
         + "  AND (:status IS NULL OR p.status = :status) "
         + "  AND (:excludeStatus IS NULL OR p.status <> :excludeStatus) "
         + "  AND (p.tenantId = :tenantId "
         + "       OR NOT EXISTS (SELECT 1 FROM PageDefinition o "
         + "                      WHERE o.code = p.code AND o.tenantId = :tenantId))")
    Page<PageDefinition> findResolved(@Param("tenantId") Long tenantId,
                                      @Param("category") String category,
                                      @Param("status") String status,
                                      @Param("excludeStatus") String excludeStatus,
                                      Pageable pageable);

    /**
     * Same resolved catalogue as {@link #findResolved}, plus a role-level
     * visibility filter so the sidebar only lists pages the caller's role can
     * actually read (no per-employee override layer — out of scope for now).
     * A {@code role_page_permission} row with {@code can_read = 0} for this
     * role+page hides it; a missing row never hides anything (default
     * read=true, same convention as {@code EmployeeAccessMatrixController}).
     *
     * <p>Native (not JPQL) because {@code role_page_permission} isn't a JPA
     * entity; {@code SELECT p.*} maps back onto {@link PageDefinition} since
     * its {@code @Column} names match the table 1:1. SuperAdmin bypasses this
     * entirely at the caller (see {@code PageDefinitionController}) rather
     * than being special-cased in the query.
     */
    @Query(
        value = "SELECT p.* FROM page_definition p "
              + "WHERE p.tenant_id IN (:tenantId, -1) "
              + "  AND (:category IS NULL OR p.category = :category) "
              + "  AND (:status IS NULL OR p.status = :status) "
              + "  AND (:excludeStatus IS NULL OR p.status <> :excludeStatus) "
              + "  AND (p.tenant_id = :tenantId "
              + "       OR NOT EXISTS (SELECT 1 FROM page_definition o "
              + "                      WHERE o.code = p.code AND o.tenant_id = :tenantId)) "
              + "  AND COALESCE("
              + "        (SELECT rpp.can_read FROM role_page_permission rpp "
              + "         WHERE rpp.role_id = :roleId AND rpp.page_code = p.code "
              + "           AND rpp.tenant_id IN (:tenantId, -1) LIMIT 1), "
              + "        1) = 1",
        countQuery =
                "SELECT COUNT(*) FROM page_definition p "
              + "WHERE p.tenant_id IN (:tenantId, -1) "
              + "  AND (:category IS NULL OR p.category = :category) "
              + "  AND (:status IS NULL OR p.status = :status) "
              + "  AND (:excludeStatus IS NULL OR p.status <> :excludeStatus) "
              + "  AND (p.tenant_id = :tenantId "
              + "       OR NOT EXISTS (SELECT 1 FROM page_definition o "
              + "                      WHERE o.code = p.code AND o.tenant_id = :tenantId)) "
              + "  AND COALESCE("
              + "        (SELECT rpp.can_read FROM role_page_permission rpp "
              + "         WHERE rpp.role_id = :roleId AND rpp.page_code = p.code "
              + "           AND rpp.tenant_id IN (:tenantId, -1) LIMIT 1), "
              + "        1) = 1",
        nativeQuery = true)
    Page<PageDefinition> findResolvedForRole(@Param("tenantId") Long tenantId,
                                             @Param("category") String category,
                                             @Param("status") String status,
                                             @Param("excludeStatus") String excludeStatus,
                                             @Param("roleId") Long roleId,
                                             Pageable pageable);

    Page<PageDefinition> findByTenantId(Long tenantId, Pageable pageable);

    Page<PageDefinition> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);

    Page<PageDefinition> findByTenantIdAndCategory(Long tenantId, String category, Pageable pageable);

    Page<PageDefinition> findByTenantIdAndCategoryAndStatus(
            Long tenantId, String category, String status, Pageable pageable);

    /** Used by the sidebar to hide ARCHIVED pages from the default listing. */
    Page<PageDefinition> findByTenantIdAndStatusNot(Long tenantId, String status, Pageable pageable);
}
