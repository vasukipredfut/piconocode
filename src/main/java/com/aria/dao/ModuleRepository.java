package com.aria.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.Modules;

public interface ModuleRepository extends JpaRepository<Modules, Long> {

	Optional<Modules> findByNameIgnoreCase(String moduleName);

}
