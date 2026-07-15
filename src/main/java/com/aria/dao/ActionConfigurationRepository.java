package com.aria.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ActionConfiguration;

@Repository
public interface ActionConfigurationRepository extends JpaRepository<ActionConfiguration,Long> {

	@Query(value = "select p from ActionConfiguration p ")
	Page<ActionConfiguration> findAllWithFilters(Pageable pageable);

	@Query(value = "select p from ActionConfiguration p where p.taskName=:taskName")
	Optional<ActionConfiguration> findByTaskName(@Param("taskName") String taskName);

}
