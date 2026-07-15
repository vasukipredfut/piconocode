package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.RodentMonitor;

@Repository
public interface RodentMonitorRepository extends JpaRepository<RodentMonitor, Long> {

	@Query(value = "select p from RodentMonitor p where "
			+ " (:goDown is null or (lower(p.godown.name) LIKE ('%' || lower(:goDown) || '%'))) "
			+ " AND  (:reason is null or (lower(p.reason) LIKE ('%' || lower(:reason) || '%'))) ")
	Page<RodentMonitor> findAllWithFilters(Pageable pageable,
			@Param("goDown") String goDown,
			@Param("reason") String reason);
	

}
