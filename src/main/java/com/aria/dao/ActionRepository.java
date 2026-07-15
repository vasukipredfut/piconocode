package com.aria.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Action;
import com.aria.model.ActionConfiguration;

@Repository
public interface ActionRepository extends JpaRepository<Action,Long> {


	@Query(value = "select p from Action p ")
	Page<Action> findAllWithFilters(Pageable pageable);
	
	@Query(value = "select p from Action p where p.uniqueRef = :ref")
	Optional<Action> findByReference(@Param("ref") String ref);

}
