package com.aria.action.dao;

import com.aria.action.model.ActionRegistryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionRegistryRepository extends JpaRepository<ActionRegistryEntry, String> {
    List<ActionRegistryEntry> findAllByImplementedTrue();
}
