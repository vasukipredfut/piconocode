package com.aria.entity_registry.dao;

import com.aria.entity_registry.model.EntityFieldRegistryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntityFieldRegistryRepository extends JpaRepository<EntityFieldRegistryEntry, Long> {
    List<EntityFieldRegistryEntry> findAllByEntityCodeOrderByFieldCodeAsc(String entityCode);
}
