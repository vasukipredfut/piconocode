package com.aria.m0.entity_registry.dao;

import com.aria.m0.entity_registry.model.EntityRegistryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntityRegistryRepository extends JpaRepository<EntityRegistryEntry, String> {
    List<EntityRegistryEntry> findAllByOrderByCodeAsc();
}
