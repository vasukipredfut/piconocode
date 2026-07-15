package com.aria.page.dao;

import com.aria.page.model.PageVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageVersionRepository extends JpaRepository<PageVersion, Long> {

    Optional<PageVersion> findFirstByPageIdAndStatusOrderByVersionDesc(Long pageId, String status);

    Optional<PageVersion> findByPageIdAndVersion(Long pageId, Integer version);

    List<PageVersion> findByPageIdOrderByVersionDesc(Long pageId);

    Optional<PageVersion> findFirstByPageIdOrderByVersionDesc(Long pageId);
}
