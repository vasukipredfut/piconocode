package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.SubModules;

public interface SubModuleRepository extends JpaRepository<SubModules, Long> {

	@Query(value = "SELECT * FROM sub_modules WHERE module_id = :moduleId", nativeQuery = true)
	List<SubModules> findByModuleId(@Param("moduleId") Long moduleId);

	Optional<SubModules> findBySubmoduleNameIgnoreCase(String name);

}
