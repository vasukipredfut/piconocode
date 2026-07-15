package com.aria.m0.action.dao;

import com.aria.m0.action.model.ActionRegistryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionRegistryRepository extends JpaRepository<ActionRegistryEntry, String> {
    List<ActionRegistryEntry> findAllByImplementedTrue();
}
