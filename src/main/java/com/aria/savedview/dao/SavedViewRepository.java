package com.aria.savedview.dao;

import com.aria.savedview.model.SavedView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SavedViewRepository extends JpaRepository<SavedView, Long> {

    List<SavedView> findAllByTenantIdAndPageSlug(Long tenantId, String pageSlug);

    @Query("SELECT v FROM SavedView v WHERE v.tenantId = :tid AND v.pageSlug = :slug "
            + "AND (v.scope = 'TENANT' OR (v.scope = 'USER' AND v.createdBy = :userId))")
    List<SavedView> findVisible(Long tid, String slug, Long userId);

    Optional<SavedView> findByIdAndTenantId(Long id, Long tenantId);
}
