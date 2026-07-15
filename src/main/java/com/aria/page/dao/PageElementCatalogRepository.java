package com.aria.page.dao;

import com.aria.page.model.PageElementCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageElementCatalogRepository extends JpaRepository<PageElementCatalog, String> {
}
